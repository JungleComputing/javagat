
package org.gridlab.gat.resources.cpi;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.ScheduledExecutor;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * This is a base class for a "simple" job.
 */
public abstract class SimpleJobBase extends JobCpi {

    private static final long serialVersionUID = 1L;

    protected String jobID;
    private MetricDefinition statusMetricDefinition;
    private Metric statusMetric;
    private SoftwareDescription Soft;
    
    private JobListener jsl;
    
    private Integer exitStatus = null;

    protected URI brokerURI;

    private final String returnValueFile;

    /**
     * constructor of SimpleJobBase
     * 
     * @param gatContext
     *            The gatContext
     * @param jobDescription
     * @param sandbox
     */
    protected SimpleJobBase(GATContext gatContext,
	    URI brokerURI, JobDescription jobDescription,
	    Sandbox sandbox, String returnValueFile) {
	super(gatContext, jobDescription, sandbox);
	this.returnValueFile = returnValueFile;

	// brokerURI necessary for security context which is required for getting a
	// ssh connection for off-line monitoring.

	this.brokerURI = brokerURI;
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

    protected SimpleJobBase(GATContext gatContext, SerializedSimpleJobBase sj)
	    throws GATObjectCreationException {
	super(gatContext, sj.getJobDescription(), sj.getSandbox());
	if (sandbox != null) {
	    sandbox.setContext(gatContext);
	}
	if (logger.isDebugEnabled()) {
	    logger.debug("reconstructing Job: " + sj);
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
    }

    protected abstract void getJobState(String jobID) throws GATInvocationException;
        
    protected abstract void kill(String jobID);
    
    protected abstract Integer retrieveExitStatus(String returnValueFile);

    protected void setState(JobState state) {
        synchronized(this) {
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
		getJobState(jobID);
	    } catch (GATInvocationException e) {
		logger.debug("GATInvocationException caught in jobListener");
		if (state != JobState.STOPPED && state != JobState.POST_STAGING) {
		    setState(JobState.SUBMISSION_ERROR);
		}
	    }
	    if (state == JobState.SUBMISSION_ERROR) {
		logger.error("Job " + jobID + "failed");
		return;
	    }
	    if (state == JobState.STOPPED || state == JobState.POST_STAGING) {
		terminate(true, true);
		synchronized(this) {
		    finished = true;
		    notifyAll();
		}
	    } else {
		ScheduledExecutor.schedule(this, SLEEP);
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
            kill(jobID);
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
	ScheduledExecutor.schedule(jsl, 10L);
    }

    public synchronized JobState getState() {
	if (this.jobID != null) {
	    try {
		getJobState(jobID);
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
	SerializedSimpleJobBase sj;
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

	    sj = new SerializedSimpleJobBase(getClass().getName(), jobDescription,
		    sandbox, jobID, submissiontime, starttime, stoptime, Soft, brokerURI, returnValueFile);

	}
	String res = GATEngine.defaultMarshal(sj);
	if (logger.isDebugEnabled()) {
	    logger.debug("marshalled seralized job: " + res);
	}
	return res;
    }

    public static Advertisable unmarshal(GATContext context, String s, ClassLoader classLoader)
	    throws GATObjectCreationException {
	if (logger.isDebugEnabled()) {
	    logger.debug("unmarshalled seralized job: " + s);
	}

	SerializedSimpleJobBase sj = (SerializedSimpleJobBase) GATEngine
		.defaultUnmarshal(SerializedSimpleJobBase.class, s);

	// if this job was created within this JVM, just return a reference to
	// the job.
	synchronized (JobCpi.class) {
	    for (int i = 0; i < jobList.size(); i++) {
		JobCpi j = (JobCpi) jobList.get(i);
		if (j instanceof SimpleJobBase) {
		    SimpleJobBase gj = (SimpleJobBase) j;
		    if (sj.getJobId().equals(gj.getJobID())) {
			if (logger.isDebugEnabled()) {
			    logger.debug("returning existing job: " + gj);
			}
			return gj;
		    }
		}
	    }
	}
	String jobClass = sj.getClassname();
	try {
            Class<?> cl = Class.forName(jobClass, true, classLoader);
	    return (Advertisable) cl.getConstructor(GATContext.class, SerializedSimpleJobBase.class).newInstance(context, sj);
	} catch(Throwable e) {
	    throw new GATObjectCreationException("Could not deserialize job", e);
	}
    }

    /**
     * @param Soft
     * @uml.property name="soft"
     */
    protected void setSoft(SoftwareDescription Soft) {
	this.Soft = Soft;
    }

    public void stop() throws GATInvocationException {
	
	try {
	    getJobState(jobID);
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
	    logger.debug("Job " + jobID + " stopped by user");
	}
    }

    private synchronized void poststageFiles(SoftwareDescription sd) {

	// let the sandbox do the poststage...

	setState(JobState.POST_STAGING);
	sandbox.retrieveAndCleanup(this);
	exitStatus = retrieveExitStatus(returnValueFile);
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
		getJobState(jobID);
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
}
