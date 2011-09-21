/**
 * SshPbsJob.java
 *
 * Created on June 10th, 2010
 *
 */

package de.mpg.aei.gat.resources.cpi.sshpbs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * @author Alexander Beck-Ratzka, AEI, June 10th 2010, created
 */

public class SshPbsJob extends JobCpi {

    private static String regexString = "[ ][ ]";

    private static final long serialVersionUID = 1L;

    private static final String homeDir = System.getProperty("user.home");

    private String jobID;
    private MetricDefinition statusMetricDefinition;
    Metric statusMetric;
    private SoftwareDescription Soft;
    
    boolean use_sge;
    
    private JobListener jsl;
    
    Integer exitStatus = null;

    /**
     * security stuff for offline monitoring
     */

    private URI brokerURI;
    private Map<String, String> securityInfo;

    /**
     * constructor of SshPbsJob
     * 
     * @param gatContext
     *            The gatContext
     * @param jobDescription
     * @param sandbox
     */
    protected SshPbsJob(GATContext gatContext,
	    SshPbsResourceBrokerAdaptor broker, JobDescription jobDescription,
	    Sandbox sandbox, Map<String, String> securityInfo, boolean use_sge) {
	super(gatContext, jobDescription, sandbox);
	this.securityInfo = securityInfo;
	this.use_sge = use_sge;

	/**
	 * broker necessary for security context which is required for getting a
	 * ssh connection for offline monitoring.
	 */

	brokerURI = broker.getBrokerURI();
	state = JobState.INITIAL;

	HashMap<String, Object> returnDef = new HashMap<String, Object>();
	returnDef.put("status", JobState.class);
	statusMetricDefinition = new MetricDefinition("job.status",
		MetricDefinition.DISCRETE, "String", null, null, returnDef);
	statusMetric = statusMetricDefinition.createMetric(null);
	registerMetric("getJobStatus", statusMetricDefinition);
    }

    /**
     * Constructor for unmarshalled jobs.
     */

    private SshPbsJob(GATContext gatContext, SerializedSshPbsJob sj)
	    throws GATObjectCreationException {
	super(gatContext, sj.getJobDescription(), sj.getSandbox());
	if (sandbox != null) {
	    sandbox.setContext(gatContext);
	}
	if (logger.isDebugEnabled()) {
	    logger.debug("reconstructing SshPbsJob: " + sj);
	}

	try {
	    this.brokerURI = new URI(sj.getBrokerURI());
	} catch (URISyntaxException e) {
	    throw new GATObjectCreationException("Could not create brokerURI", e);
	}
	this.use_sge = sj.isUse_sge();
	this.jobID = sj.getJobId();
	this.starttime = sj.getStarttime();
	this.stoptime = sj.getStoptime();
	this.submissiontime = sj.getSubmissiontime();
	this.securityInfo = SshPbsResourceBrokerAdaptor.getSecurityInfo(gatContext, brokerURI);

	// reconstruct enough of the software description to be able to
	// poststage.

	Soft = new SoftwareDescription();
	String s = sj.getStdout();
	if (s != null) {
	    Soft.setStdout(GAT.createFile(gatContext, s));
	}
	s = sj.getStderr();
	if (s != null) {
	    Soft.setStderr(GAT.createFile(gatContext, s));
	}

	String[] toStageOut = sj.getToStageOut();
	String[] stagedOut = sj.getStagedOut();
	if (toStageOut != null) {
	    for (int i = 0; i < toStageOut.length; i++) {
		Soft.addPostStagedFile(
			GAT.createFile(gatContext, toStageOut[i]),
			GAT.createFile(gatContext, stagedOut[i]));
	    }
	}

	// Tell the engine that we provide job.status events
	HashMap<String, Object> returnDef = new HashMap<String, Object>();
	returnDef.put("status", JobState.class);
	statusMetricDefinition = new MetricDefinition("job.status",
		MetricDefinition.DISCRETE, "String", null, null, returnDef);
	registerMetric("getJobStatus", statusMetricDefinition);
	statusMetric = statusMetricDefinition.createMetric(null);
	startListener();
    }

    protected synchronized void setState(JobState state) {
	if (submissiontime == 0) {
	    setSubmissionTime();
	}
	if (this.state != state) {
	    this.state = state;
	    if (state == JobState.RUNNING || state == JobState.POST_STAGING || state == JobState.STOPPED) {
		if (starttime == 0) {
		    setStartTime();
		}
	    }
	    if (state == JobState.STOPPED) {
		setStopTime();
	    }
	    MetricEvent v = new MetricEvent(this, state, statusMetric, System
		    .currentTimeMillis());
	    fireMetric(v);
	}
    }

    /**
     * The jobListener runs in a thread and checks the job's state.
     */
    private class JobListener extends Thread {

	final int SLEEP = 1000;

	String jobID = null;
	SoftwareDescription Soft = null;

	private boolean terminated;

	public JobListener(String jobID, SoftwareDescription Soft) {
	    // this.session = session;
	    this.jobID = jobID;
	    this.Soft = Soft;
	}

	public void run() {
	    for (;;) {
		try {
		    setState();
		} catch (GATInvocationException e) {
		    logger.debug("GATInvocationException caught in thread jobListener");
		    setState(JobState.SUBMISSION_ERROR);
		}		    
		logger.debug("Job Status is:  " + state.toString());
		if (state != JobState.RUNNING && state != JobState.SCHEDULED) {
		    if (state == JobState.SUBMISSION_ERROR) {
			logger.debug("Job submission failed");
			break;
		    } else if (state == JobState.STOPPED || state == JobState.POST_STAGING) {
			break;
		    }
		} else {
		    break;
		}
		try {
		    Thread.sleep(SLEEP);
		} catch(InterruptedException e) {
		    // ignore
		}
	    }

	    // Now we're in RUNNING state - set the time and start the
	    // jobListener

	    setStartTime();
	    
	    try {
		while (state != JobState.STOPPED && state != JobState.POST_STAGING) {
		    if (state == JobState.SUBMISSION_ERROR) {
			logger.error("SshPbs job " + jobID + "failed");
			break;
		    }
		    logger.debug("SshPbs Job still running");
		    Thread.sleep(SLEEP);
		    setState();
		}
		terminate(true, true);
	    } catch (InterruptedException e) {
		logger.debug("InterruptedException caught in thread jobListener");
		setState(JobState.SUBMISSION_ERROR);
	    } catch (GATInvocationException e) {
		logger.debug("GATInvocationException caught in thread jobListener");
		setState(JobState.SUBMISSION_ERROR);
	    }
	}
	
	private synchronized void terminate(boolean fromThread, boolean mustPoststage) {
           
            if (terminated) {
        	return;
            }

            terminated = true;
            
            if (! fromThread) {
        	try {
        	    jsl.join();
        	} catch (Throwable e1) {
        	    // ignore
        	}
            }
            
            if (mustPoststage) {
        	setState(JobState.POST_STAGING);
        	poststageFiles(Soft);
            }
	    setState(JobState.STOPPED);
            finished();
            try {
        	getExitStatus();
            } catch(Throwable e) {
        	// Should not happen.
            }
            if (logger.isInfoEnabled()) {
        	logger.info("Finished job ID: " + jobID);
            }
        }
        
        public void stop(boolean mustPoststage) {
            SshPbsJobStop(); // SshPbsJobStop still needs to be implemented.
	    logger.debug("SshPbs Job " + jobID + " stopped by user");
            terminate(false, mustPoststage);
        }
    }


    /*
     * public String getJobID() { return jobID; }
     */

    /**
     * @param jobID
     * @uml.property name="jobID"
     */

    protected synchronized void setJobID(String jobID) {
	this.jobID = jobID;
	notifyAll();
    }

    protected void startListener() {
	jsl = new JobListener(this.jobID, this.Soft);
	Thread t = new Thread(jsl);
	t.setDaemon(true);
	t.start();
    }

    public synchronized JobState getState() {
	if (this.jobID != null) {
	    try {
		setState();
	    } catch (GATInvocationException e) {
		logger.debug("setState failed in getState");
	    }
	}
	return state;
    }

    /*
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
	SerializedSshPbsJob sj;
	synchronized (this) {

	    // Wait until initial stages are passed.
	    while (state == JobState.INITIAL || state == JobState.PRE_STAGING) {
		// || state == JobState.SCHEDULED) {
		try {
		    wait();
		} catch (Exception e) {
		    // ignore
		}
	    }

	    sj = new SerializedSshPbsJob(getClass().getName(), jobDescription,
		    sandbox, jobID, submissiontime, starttime, stoptime, Soft, brokerURI, use_sge);

	}
	String res = GATEngine.defaultMarshal(sj);
	if (logger.isDebugEnabled()) {
	    logger.debug("marshalled seralized job: " + res);
	}
	return res;
    }

    public static Advertisable unmarshal(GATContext context, String s)
	    throws GATObjectCreationException {
	if (logger.isDebugEnabled()) {
	    logger.debug("unmarshalled seralized job: " + s);
	}

	SerializedSshPbsJob sj = (SerializedSshPbsJob) GATEngine
		.defaultUnmarshal(SerializedSshPbsJob.class, s);

	// if this job was created within this JVM, just return a reference to
	// the job
	synchronized (JobCpi.class) {
	    for (int i = 0; i < jobList.size(); i++) {
		JobCpi j = (JobCpi) jobList.get(i);
		if (j instanceof SshPbsJob) {
		    SshPbsJob gj = (SshPbsJob) j;
		    if (sj.getJobId().equals(gj.getJobID())) {
			if (logger.isDebugEnabled()) {
			    logger.debug("returning existing job: " + gj);
			}
			return gj;
		    }
		}
	    }
	}
	return new SshPbsJob(context, sj);
    }

    /**
     * @param Soft
     * @uml.property name="soft"
     */
    protected void setSoft(SoftwareDescription Soft) {
	this.Soft = Soft;
    }

    protected synchronized void setState() throws GATInvocationException {
	
        if (state == JobState.POST_STAGING || state == JobState.STOPPED
                || state == JobState.SUBMISSION_ERROR) {
            return;
        }

	logger.debug("Getting task status in setState()");

	/**
	 * getting the status via ssh ... qstat
	 */

	String username = securityInfo.get("username");
	String host = brokerURI.getHost();

	if (host == null) {
	    host = "localhost";
	}
	ArrayList<String> command = new ArrayList<String>();

	command.add("/usr/bin/ssh");
	command.add("-o");
	command.add("BatchMode=yes");
	// command.add("-t");
	// command.add("-t");
	command.add(username + "@" + host);

	command.add("qstat");
	if (! use_sge) {
	    command.add(this.jobID);
	}

	JobState s;
	try {
	    String pbsState[] = singleResult(command);
	    s = mapPbsStatetoGAT(pbsState);
	    if (s != JobState.STOPPED) {
		setState(s);
	    } else {
		setState(JobState.POST_STAGING);
	    }
	} catch (Throwable e) {
	    logger.debug("retrieving job status sshpbsjob failed");
	    throw new GATInvocationException(
		    "Unable to retrieve the Job Status", e);
	}
    }

    /**
     * mapPbsStateToGAT maps a job status of PBS to a GAT job status.
     * 
     * @param String
     *            pbsState
     * @return JobState (GAT)
     * @author A. Beck-Ratzka, AEI, 14.09.2010
     */

    private JobState mapPbsStatetoGAT(String[] pbsState) {

	String pbsLine = null;

	if (pbsState == null) {
	    logger.error("Error in mapPbsStatetoGAT: no PbsState returned");
	    return JobState.UNKNOWN;
	} else {
	    for (int ii = 0; ii < pbsState.length; ii++) {
		pbsLine = removeBlanksToOne(pbsState[ii]);
		if (pbsLine.contains(this.jobID)) {
		    break;
		}
		pbsLine = null;
	    }
	    if (pbsLine == null) {
		logger.debug("no job status information for '" + this.jobID
			+ "' found.");
		// if was running, assume it is finished now.
		if (state == JobState.RUNNING || state == JobState.SCHEDULED) {
		    logger.debug("But is was present earlier, so we assume it finished.");
		    return JobState.STOPPED;
		}
		return JobState.UNKNOWN;
	    } else {
		if (use_sge) {
		    // Format is:
		    // JobID Prio JobName JobOwner JobState .....
		    String[] splits = pbsLine.split(" ");
		    if (splits.length < 5) {
			return JobState.UNKNOWN;
		    }
		    String status = splits[4];
		    if (status.indexOf('t') >= 0) {	// transfer
			return JobState.SCHEDULED;
		    }
		    if (status.indexOf('w') >= 0) {	// waiting
			return JobState.SCHEDULED;
		    }
		    if (status.indexOf('h') >= 0) {	// hold
			return JobState.ON_HOLD;
		    }
		    if (status.indexOf('r') >= 0) {	// running
			return JobState.RUNNING;
		    }
		    if (status.indexOf('R') >= 0) {	// restarted
			return JobState.RUNNING;
		    }
		    if (status.indexOf('s') >= 0 || status.indexOf('S') >= 0) {	// suspended
			return JobState.RUNNING;
		    }
		    if (status.indexOf('x') >= 0) {	// exit
			return JobState.STOPPED;
		    }
		    if (status.indexOf('E') >= 0) {	// error
			return JobState.SUBMISSION_ERROR;
		    }
		    return JobState.UNKNOWN;
		} else {
		    if (pbsLine.indexOf('W') >= 0) { // waiting
			return JobState.SCHEDULED;
		    } else if (pbsLine.indexOf('Q') >= 0) { // queued
			return JobState.SCHEDULED;
		    } else if (pbsLine.indexOf('T') >= 0) { // transition
			return JobState.SCHEDULED;
		    }
		    // if (mState.indexOf('H') >= 0) {
		    // return Job.HOLD;
		    // }
		    else if (pbsLine.indexOf('S') >= 0) { // suspended
			return JobState.RUNNING;
		    } else if (pbsLine.indexOf('R') >= 0) { // running
			return JobState.RUNNING;
		    } else if (pbsLine.indexOf('E') >= 0) { // exiting
			return JobState.STOPPED;
		    } else if (pbsLine.indexOf('C') >= 0) { // exiting
			return JobState.STOPPED;
		    } else {
			return JobState.UNKNOWN;
		    }
		}
	    }
	}
    }

    /**
     * method removeBlanksToOne: exchange several blanks in a string a let only
     * one as a delimiter between word.
     * 
     * @author - A. Beck-Ratzka, AEI.
     * @param String
     *            changeString - String with more then one blank as delimiter.
     * @return String result - strings with only single blanks as delimiters
     *         between words.
     */

    private static String removeBlanksToOne(String changeString) {

	String result = null;

	result = changeString.replaceAll(regexString, " ");
	while (result.contains("  ")) {
	    result = result.replaceAll(regexString, " ");
	}

	return (result);
    }

    public synchronized void stop() throws GATInvocationException {
	
	setState();
		
        if (state == JobState.POST_STAGING || state == JobState.STOPPED
                || state == JobState.SUBMISSION_ERROR) {
            return;
        }
	if ((state != JobState.RUNNING)
		&& (state != JobState.ON_HOLD)
		&& (state != JobState.SCHEDULED)) {
	    throw new GATInvocationException(
		    "Cant stop(): job is not in a running state");
	} else {
	    jsl.stop(!(gatContext.getPreferences().containsKey("job.stop.poststage")
        	    && gatContext.getPreferences().get("job.stop.poststage").equals("false")));
	    SshPbsJobStop(); // SshPbsJobStop still needs to be implemented.
	    state = JobState.STOPPED;
	    if (stoptime == 0L) {
		stoptime = System.currentTimeMillis();
	    }
	    logger.debug("SshPbs Job " + jobID + " stopped by user");
	}
    }

    /**
     * SshPbsJobStop
     * 
     * Stop pbs jobs started via an ssh command on a remote cluster by omitting
     * a qdel command via ssh. Only necessary is the jobID.
     * 
     * The job itself is executed using the method singleResult.
     * 
     * @author A. Beck-Ratzka, AEI, July 2010, created.
     */

    private synchronized void SshPbsJobStop() {

	/**
	 * securityinfo, getAuthority and getHostname still requires a
	 * solution..
	 */

	String username = securityInfo.get("username");

	String host = brokerURI.getHost();
	if (host == null) {
	    host = "localhost";
	}

	// String host = "buran.aei.mpg.de";

	ArrayList<String> command = new ArrayList<String>();

	command.add("/usr/bin/ssh");
	command.add("-o");
	command.add("BatchMode=yes");
	// command.add("-t");
	// command.add("-t");
	command.add(username + "@" + host);

	command.add("qdel");
	command.add(jobID);

	try {
	    String[] outStr = singleResult(command);
	    logger.info("SshPbsJob stop request returned:" + outStr[0]);
	} catch (IOException e) {
	    logger.error("Failed to stop sshPbs job: " + jobID);
	    e.printStackTrace();
	}
    }

    private synchronized void poststageFiles(SoftwareDescription sd) {

	/**
	 * let the sandbox do the poststage...
	 */

	setState(JobState.POST_STAGING);
	sandbox.retrieveAndCleanup(this);

	/**
	 * poststage the file which contains the exit status of the
	 * aplication...
	 */

	String username = securityInfo.get("username");
	int ssh_port = SshPbsResourceBrokerAdaptor.getPort(gatContext, brokerURI);
	String host = brokerURI.getHost();
	if (host == null) {
	    host = "localhost";
	}

	ArrayList<String> scpCommand = new ArrayList<String>();

	scpCommand.add("/usr/bin/scp");
	scpCommand.add(username + "@" + host + ":.rc." + jobID);
	scpCommand.add(homeDir + "/.rc." + jobID);

	String[] outStr;
	try {
	    outStr = singleResult(scpCommand);
	    logger.debug("SshPbsJob scp of exit status file request returned:"
		    + Arrays.toString(outStr));
	} catch (IOException e) {
	    logger.debug("Failed scp " + username + "@" + host + ":.rc."
		    + jobID + " " + homeDir + "/.rc." + jobID);

	} finally {
	    // delete the remote file with the exit status

	    ArrayList<String> rmCommand = new ArrayList<String>();
	    rmCommand.add("/usr/bin/ssh");
	    if (ssh_port != SshPbsResourceBrokerAdaptor.SSH_PORT) {
		rmCommand.add("-p");
		rmCommand.add("" + ssh_port);
	    }
	    rmCommand.add("-o");
	    rmCommand.add("BatchMode=yes");
	    // rmCommand.add("-t");
	    // rmCommand.add("-t");
	    rmCommand.add(username + "@" + host);
	    rmCommand.add("rm " + ".rc." + jobID);
	    try {
		outStr = singleResult(rmCommand);
	    } catch (IOException e) {
		logger.debug("failed ssh " + username + "@" + host
			+ " rm .rc." + jobID, e);
		// ignore?
	    }
	}
    }

    /**
     * getExitstatus - retrieves the exits status of an application run via
     * sshPBS on a remote cluster by taking the exit value from the dataset
     * 
     * $HOME/.rc.PBS_JOBID
     * 
     * The location of the dataset containing the exit status is defined in the
     * qsub script (see method createQsubScript in SshPbsResourceBrokerAdaptor).
     * 
     * Before evaluating the file content, the file must be copied via scp from
     * the executing host to the submitting machines home directory.
     * 
     * @return int rc
     * @author Alexander Beck-Ratzka, AEI, 21.9.2010.
     * @throws GATInvocationException 
     * 
     */

    public synchronized int getExitStatus() throws GATInvocationException {
        if (state != JobState.STOPPED && state != JobState.SUBMISSION_ERROR) {
            throw new GATInvocationException(
                    "not in STOPPED or SUBMISSION_ERROR state");
        }
        
	if (exitStatus != null) {
	    return exitStatus.intValue();
	}

	// Retrieve the exit status from the file $HOME/.rc.PBS_JOBID

	String marker = "retvalue = ";
	String line = null;
	int rc = -1;

	String fileName = homeDir + "/.rc." + jobID;

	BufferedReader rExit = null;
	try {

	    java.io.File fi = new java.io.File(fileName); // for deleting it
							  // after getting the
							  // rc

	    rExit = new BufferedReader(new FileReader(fileName));

	    line = rExit.readLine().toString();

	    String rc_String = line.substring(marker.length());
	    if (rc_String != null) {
		rc = Integer.parseInt(rc_String);
	    }
	    fi.delete();
	    exitStatus = new Integer(rc);
	    return rc;

	} catch (FileNotFoundException e) {
	    logger.debug("SshPbs adaptor: exit value file " + fileName
		    + " not found!");
	    exitStatus = new Integer(-1);
	    return -1;
	} catch (IOException e) {
	    try {
		rExit.close();
	    } catch (IOException e1) {
		logger.debug("SshPbs adaptor: Close error after read error on "
			+ fileName);
	    }
	    exitStatus = new Integer(-1);
	    return -1;
	}
    }

    /**
     * somewhat as a dummy; gets the outcome files (for test purposes)
     */

    public Map<String, Object> getInfo() throws GATInvocationException {

	HashMap<String, Object> m = new HashMap<String, Object>();

	if (state != JobState.STOPPED && state != JobState.POST_STAGING && state != JobState.SUBMISSION_ERROR) {
	    setState();
	}
	m.put(ADAPTOR_JOB_ID, jobID);
	if (state == JobState.RUNNING) {
	    String host = brokerURI.getHost();
	    if (host == null) {
		host = "localhost";
	    }
	    m.put(HOSTNAME, host);
	} else {
	    m.put(HOSTNAME, null);
	}

        m.put(STATE, state.toString());

        if (state == JobState.INITIAL || state == JobState.UNKNOWN
                || state == JobState.SCHEDULED) {
            m.put(STARTTIME, null);
        } else {
            m.put(STARTTIME, starttime);
        }
        if (state != JobState.STOPPED) {
            m.put(STOPTIME, null);
        } else {
            m.put(STOPTIME, stoptime);
        }
        if (submissiontime != 0) {
            m.put(SUBMISSIONTIME, submissiontime);
        } else {
            m.put(SUBMISSIONTIME, null);
        }
        m.put(POSTSTAGE_EXCEPTION, postStageException);
        if (deleteException != null) {
            m.put("delete.exception", deleteException);
        }
        if (wipeException != null) {
            m.put("wipe.exception", wipeException);
        }

        return m;
    }

    /**
     * The Method SingleResult submits a single SshPbs Job.
     * 
     * @param command
     *            the command line.
     * @return the output of the command.
     * @throws IOException
     * 
     * @author Alexander Beck-Ratzka, AEI, July 2010
     */

    public static synchronized String[] singleResult(ArrayList<String> command)
	    throws IOException {
	
	ArrayList<String> result = new ArrayList<String>();
	String line = null;
	BufferedReader br = null;
	
	ProcessBuilder builder = new ProcessBuilder(command);
	
        try {
            if (logger.isDebugEnabled()) {
        	logger.debug("Running command: " + Arrays.toString(command.toArray(new String[command.size()])));
            }
	    Process proc = builder.start();
	    proc.waitFor();
	    if (proc.exitValue() == 0) {
		br = new BufferedReader(new InputStreamReader(
			proc.getInputStream()));
		while ((line = br.readLine()) != null) {
		    result.add(line);
		}
	    } else {
		br = new BufferedReader(new InputStreamReader(
			proc.getErrorStream()));
		while ((line = br.readLine()) != null) {
		    result.add(line);
		}
		if (!command.get(0).toString().contains("scp")
			|| !command.get(0).toString().contains("rm")) {
		    throw new IOException("rejected: "
			    + Arrays.toString(result.toArray(new String[0])));
		}
	    }
	} catch (InterruptedException ex) {
	    throw new IOException("process was interupted");
	} finally {
	    if (br != null) {
		br.close();
	    }
	}
	return result.toArray(new String[result.size()]);
    }
}
