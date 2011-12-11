/**
 * SshPbsJob.java
 *
 * Created on June 10th, 2010
 *
 */

package de.mpg.aei.gat.resources.cpi.sshpbs;

import ibis.util.ThreadPool;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
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
import org.gridlab.gat.engine.util.CommandRunner;
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

    // private static String regexString = "[ ][ ]";
    private static String regexString = "\\s\\s*";

    private static final long serialVersionUID = 1L;

    private String jobID;
    private MetricDefinition statusMetricDefinition;
    Metric statusMetric;
    private SoftwareDescription Soft;
    
    private JobListener jsl;
    
    Integer exitStatus = null;

    private URI brokerURI;
    
    private Map<String, String> securityInfo;
    
    private final String returnValueFile;

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
	    Sandbox sandbox, Map<String, String> securityInfo, String returnValueFile) {
	super(gatContext, jobDescription, sandbox);
	this.securityInfo = securityInfo;
	this.returnValueFile = returnValueFile;

	// brokerURI necessary for security context which is required for getting a
	// ssh connection for off-line monitoring.

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
	this.returnValueFile = sj.getReturnValueFile();
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
     * The JobListener runs in a thread and checks the job's state.
     */
    private class JobListener implements Runnable {

	final int SLEEP = 5000;

	String jobID = null;
	SoftwareDescription Soft = null;

	private boolean terminated;
	
	private boolean finished;

	public JobListener(String jobID, SoftwareDescription Soft) {
	    // this.session = session;
	    this.jobID = jobID;
	    this.Soft = Soft;
	}

	public void run() {
	    try {
		for (;;) {
		    try {
			setState();
		    } catch (GATInvocationException e) {
			logger.debug("GATInvocationException caught in thread jobListener");
			setState(JobState.SUBMISSION_ERROR);
		    } catch(InterruptedException e) {
			// Try again ...
		    }
		    if (logger.isDebugEnabled()) {
			logger.debug("Job Status is:  " + state.toString());
		    }
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
	    } finally {
		synchronized(this) {
		    finished = true;
		    notifyAll();
		}
	    } 
	}
	
	private synchronized void terminate(boolean fromThread, boolean mustPoststage) {
           
            if (terminated) {
        	return;
            }

            terminated = true;
            
            if (! fromThread) {
        	while (! finished) {
        	    try {
        		wait();
        	    } catch(Throwable e) {
        		// ignore
        	    }
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
            SshPbsJobStop();
            if (logger.isDebugEnabled()) {
        	logger.debug("SshPbs Job " + jobID + " stopped by user");
            }
            terminate(false, mustPoststage);
        }
    }

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
	ThreadPool.createNew(jsl, "JobListener " + this.jobID);
    }

    public synchronized JobState getState() {
	if (this.jobID != null) {
	    try {
		setState();
	    } catch (Throwable e) {
		logger.debug("setState failed in getState", e);
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
		    sandbox, jobID, submissiontime, starttime, stoptime, Soft, brokerURI, returnValueFile);

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
	// the job.
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

    protected synchronized void setState() throws GATInvocationException, InterruptedException {
	
        if (state == JobState.POST_STAGING || state == JobState.STOPPED
                || state == JobState.SUBMISSION_ERROR) {
            return;
        }

	logger.debug("Getting task status in setState()");

	//  getting the status via ssh ... qstat

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

	JobState s;
	try {
	    String pbsState[] = singleResult(command);
	    s = mapPbsStatetoGAT(pbsState);
	    if (s != JobState.STOPPED) {
		setState(s);
	    } else {
		setState(JobState.POST_STAGING);
	    }
	} catch (IOException e) {
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

    private boolean sawJob = false;
    private int missedJob = 0;
    
    private JobState mapPbsStatetoGAT(String[] pbsState) {

	String pbsLine = null;
	String[] splits = null;

	if (pbsState == null) {
	    logger.error("Error in mapPbsStatetoGAT: no PbsState returned");
	    return JobState.UNKNOWN;
	} else {
	    for (int ii = 0; ii < pbsState.length; ii++) {
		pbsLine = removeBlanksToOne(pbsState[ii]);
		splits = pbsLine.split(" ");
		// Note: PBS qstat sometimes does not print the complete job identifier.
		// On lisa.sara.nl, for example, if the job identifier is 5823458.batch1.irc.sara.nl,
		// qstat only prints 5823458.batch1. --Ceriel
		if (this.jobID.startsWith(splits[0])) {
                    logger.debug("Found job: " + splits[0] + ", JobID = " + this.jobID);
		    sawJob = true;
		    break;
		}
		splits = null;
	    }
	    if (splits == null) {
		logger.debug("no job status information for '" + this.jobID
			+ "' found.");
		// if we saw it before, assume it is finished now.
		if (sawJob) {
		    logger.debug("But is was present earlier, so we assume it finished.");
		    return JobState.STOPPED;
		}
		missedJob++;
		if (missedJob >= 5) {
		    // arbitrary threshold. Problem is, there may be a gap between successful
		    // submission of the job and its appearance in qstat output. But it may
		    // also not appear because it is already finished ...
		    logger.debug("But is was not present for a while, so we assume it finished.");
		    return JobState.STOPPED;
		}
		// Return current state.
		return state;
	    } else {
		// For SGE, format is:
		// JobID Prio JobName JobOwner JobState .....
		// For PBS, format is
		// JobID JobName JobOwner CpuTime JobState ....
		// So, in both cases, the 5'th column gives the job state.
		// Below is combined job-state determination for SGE and PBS ...
		if (splits.length < 5) {
		    return JobState.UNKNOWN;
		}
		String status = splits[4];
		if (status.indexOf('t') >= 0 || status.indexOf('T') >= 0) {	// transfer
		    return JobState.SCHEDULED;
		}
		if (status.indexOf('w') >= 0 || status.indexOf('W') >= 0) {	// waiting
		    return JobState.SCHEDULED;
		}
		if (status.indexOf('Q') >= 0) { // Queued
		    return JobState.SCHEDULED;
		}
		if (status.indexOf('h') >= 0 || status.indexOf('H') >= 0) {	// hold
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
		if (status.indexOf('x') >= 0 || status.indexOf('C') >= 0) {	// exit
		    return JobState.STOPPED;
		}
		if (status.indexOf('E') >= 0) {	// error or exiting
		    return JobState.STOPPED;
		}
		return JobState.UNKNOWN;
	    }
	}
    }

    /**
     * Method removeBlanksToOne: exchange several blanks in a string a let only
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

	result = changeString.trim().replaceAll(regexString, " ");
        /*
	while (result.contains("  ")) {
	    result = result.replaceAll(regexString, " ");
	}
        */

	return (result);
    }

    public void stop() throws GATInvocationException {
	
	try {
	    setState();
	} catch(Throwable e) {
	    // ignore
	}
		
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
	    state = JobState.STOPPED;
	    if (stoptime == 0L) {
		stoptime = System.currentTimeMillis();
	    }
	    logger.debug("SshPbs Job " + jobID + " stopped by user");
	}
    }

    /**
     * Stop pbs jobs started via an ssh command on a remote cluster by emitting
     * a qdel command via ssh. Only necessary is the jobID.
     * 
     * The job itself is executed using the method singleResult.
     * 
     * @author A. Beck-Ratzka, AEI, July 2010, created.
     */

    private synchronized void SshPbsJobStop() {

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

	command.add("qdel");
	command.add(jobID);

	try {
	    singleResult(command);
	} catch (Throwable e) {
	    logger.info("Failed to stop sshPbs job: " + jobID, e);
	    // TODO: what to do here?
	}
    }

    private synchronized void poststageFiles(SoftwareDescription sd) {

	// let the sandbox do the poststage...

	setState(JobState.POST_STAGING);
	sandbox.retrieveAndCleanup(this);

	// Retrieve the exit status.

	String marker = "retvalue = ";
	String line = null;
	int rc = -1;

	BufferedReader rExit = null;
	java.io.File fi = new java.io.File(returnValueFile);
	try {
	    rExit = new BufferedReader(new FileReader(fi));

	    line = rExit.readLine().toString();

	    String rc_String = line.substring(marker.length());
	    if (rc_String != null) {
		rc = Integer.parseInt(rc_String);
	    }
	    exitStatus = new Integer(rc);
	} catch (FileNotFoundException e) {
	    logger.debug("SshPbs adaptor: exit value file " + returnValueFile
		    + " not found!");
	    exitStatus = null;
	} catch (IOException e) {
	    exitStatus = null;
	} finally {
	    try {
		rExit.close();
	    } catch(Throwable e) {
		// ignore
	    }
	    fi.delete();
	}
    }

    /**
     * getExitstatus - retrieves the exits status of an application run.
     */

    public synchronized int getExitStatus() throws GATInvocationException {
        if (state != JobState.STOPPED && state != JobState.SUBMISSION_ERROR) {
            throw new GATInvocationException(
                    "not in STOPPED or SUBMISSION_ERROR state");
        }
        
	if (exitStatus != null) {
	    return exitStatus.intValue();
	}
	
	return -1;
    }

    public Map<String, Object> getInfo() throws GATInvocationException {

	HashMap<String, Object> m = new HashMap<String, Object>();

	if (state != JobState.STOPPED && state != JobState.POST_STAGING && state != JobState.SUBMISSION_ERROR) {
	    try {
		setState();
	    } catch (Throwable e) {
		// ignore?
	    }
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
	
	
	if (logger.isDebugEnabled()) {
	    logger.debug("Running command: " + Arrays.toString(command.toArray(new String[command.size()])));
	}
	
	CommandRunner run;
	
	try {
	    run = new CommandRunner(command);
	} catch (GATInvocationException e1) {
	    throw new IOException("Command not found");
	}
	
	BufferedReader br = null;
	ArrayList<String> result = new ArrayList<String>();
	try {
	    String line;	   
	    if (run.getExitCode() == 0) {
		br = new BufferedReader(new StringReader(run.getStdout()));
	    } else {
		br = new BufferedReader(new StringReader(run.getStderr()));
	    }
	    while ((line = br.readLine()) != null) {
		result.add(line);
	    }
	    if (run.getExitCode() != 0 && (!command.get(0).toString().contains("scp")
		    || !command.get(0).toString().contains("rm"))) {
		throw new IOException("rejected: "
			+ Arrays.toString(result.toArray(new String[0])));
	    }
	} finally {
	    if (br != null) {
		br.close();
	    }
	}
	String[] retval = result.toArray(new String[result.size()]);
	if (logger.isDebugEnabled()) {
	    for (int i = 0; i < retval.length; i++) {
		logger.debug("Result[" + i + "] = " + retval[i]);
	    }
	}
	return retval;
    }
}
