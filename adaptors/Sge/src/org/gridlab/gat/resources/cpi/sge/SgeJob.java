/*
 * SGEJob.java
 *
 * Created on May 16, 2006, 11:24 AM
 *
 */

package org.gridlab.gat.resources.cpi.sge;

// org.ggf.drmaa imports
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.ExitTimeoutException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.Session;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * 
 * @author ole.weidner
 */
@SuppressWarnings("serial")
public class SgeJob extends JobCpi {

    private String jobID = "";

    private String hostname;

    private MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    private Session session;
    
    private JobListener jsl;

    private Hashtable<String, Long> time = new Hashtable<String, Long>();

    /**
     * The JobListener runs in a thread and checks the job's state. When it
     * detects a state transition from SCHEDULED to RUN, it notes the time and
     * waits for the job to terminate.
     */
    private class JobListener extends Thread {

        private int state = 0x00;

        private static final int SLEEP = 1000;

        private boolean done = false;
        
        private boolean mustPoststage = true;
        
        public void run() {
            
            JobInfo info = null;
            
            while (state != Session.RUNNING) {
        	// Busy wait loop, but only until job is running.
                try {
                    state = session.getJobProgramStatus(jobID);
                    if (state == Session.FAILED) {
                        // TODO
                        break;
                    } else if (state == Session.DONE) {
                        // TODO
                        break;
                    }
                    Thread.sleep(SLEEP);
                } catch (DrmaaException e) {
                    // TODO
                } catch (InterruptedException e) {
                    // TODO
                }
            }
            // Now we're in RUNNING state - set the time and start the
            // jobStopListener
            time.put("start", new Long(System.currentTimeMillis()));
            
            if (state == Session.RUNNING) {
                setState(JobState.RUNNING);
                try {
                    info = session.wait(jobID, Session.TIMEOUT_WAIT_FOREVER);
                } catch (DrmaaException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("-- SGEJob EXCEPTION --");
                        logger.debug("Got an exception while waiting", e);
                    }
                }
            }
            
            boolean poststage;
            synchronized(this) {
        	 poststage = mustPoststage;	
            }

            if (poststage) {
        	setState(JobState.POST_STAGING);
        	if (sandbox != null) {
        	    sandbox.retrieveAndCleanup(SgeJob.this);
        	}
            }
            if (state == Session.FAILED || (info != null && info.wasAborted())) {
        	setState(JobState.SUBMISSION_ERROR);
            } else {
                setState(JobState.STOPPED);
            }
            finished();
            // Now we're in a final state - set the time and exit
            time.put("stop", new Long(System.currentTimeMillis()));
        }
        
        public synchronized void stop(boolean mustPoststage) {
            this.mustPoststage = mustPoststage;
            try {
                session.control(jobID, Session.TERMINATE);
            } catch (DrmaaException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("-- SGEJob EXCEPTION --");
                    logger.debug(
                            "Got an exception while trying to TERMINATE job:",
                            e);
                }
            }
            try {
		jsl.join();
	    } catch (InterruptedException e1) {
		// ignore
	    }
        }
    }

    protected SgeJob(GATContext gatContext, JobDescription jobDescription,
            Sandbox sandbox) {
        super(gatContext, jobDescription, sandbox);

        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        registerMetric("getJobStatus", statusMetricDefinition);
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    protected void setJobID(String jobID) {
        this.jobID = jobID;
        time.put("submission", new Long(System.currentTimeMillis()));
    }

    protected void startListener() {
        jsl = new JobListener();
        new Thread(jsl).start();
    }

    protected synchronized void setState(JobState state) {
        this.state = state;
        MetricEvent v = new MetricEvent(this, state, statusMetric, System
                .currentTimeMillis());
        fireMetric(v);
    }

    public String marshal() {
        throw new UnsupportedOperationException("Not implemented");
    }

    // protected void setState() {
    // try {
    // int status = session.getJobProgramStatus(jobID);
    //
    // switch (status) {
    //
    // case Session.RUNNING:
    // state = RUNNING;
    // break;
    //
    // case Session.FAILED:
    // state = SUBMISSION_ERROR;
    // break;
    //
    // case Session.DONE:
    // state = STOPPED;
    // break;
    //
    // /* Job is active but suspended */
    //
    // case Session.SYSTEM_SUSPENDED:
    // state = STOPPED;
    // break;
    // case Session.USER_SUSPENDED:
    // state = STOPPED;
    // break;
    // case Session.USER_SYSTEM_SUSPENDED:
    // state = STOPPED;
    // break;
    //
    // /* Job is in the queue states */
    //
    // case Session.QUEUED_ACTIVE:
    // state = SCHEDULED;
    // break;
    // case Session.SYSTEM_ON_HOLD:
    // state = ON_HOLD;
    // break;
    // case Session.USER_ON_HOLD:
    // state = ON_HOLD;
    // break;
    // case Session.USER_SYSTEM_ON_HOLD:
    // state = ON_HOLD;
    // break;
    // default:
    // if (logger.isDebugEnabled()) {
    // logger.debug("WARNING: SGE Job: unknown DRMAA state: "
    // + status);
    // }
    // }
    // } catch (DrmaaException e) {
    // if (logger.isDebugEnabled()) {
    // logger.debug("-- SGEJob EXCEPTION --");
    // logger
    // .debug("Got an exception while retrieving resource manager status:");
    // logger.debug(e);
    // }
    // }
    // }

    protected void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Map<String, Object> getInfo() {

        HashMap<String, Object> m = new HashMap<String, Object>();
        // setState();

        try {
            m.put("hostname", hostname);
            m.put("checkpointable", "0");
            m.put("scheduletime", null);
            m.put("resManName", "Sun Grid Engine");
            m.put("state", state.toString());
            m.put("adaptor.job.id", jobID);
            m.put("starttime", time.get("start"));
            m.put("stoptime", time.get("stop"));
            m.put("submissiontime", time.get("submission"));
            m.put("poststage.exception", postStageException);
            m.put("resManState", Integer.toString(session
                    .getJobProgramStatus(jobID)));
        } catch (DrmaaException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("-- SGEJob EXCEPTION --");
                logger
                        .debug(
                                "Got an exception while retrieving resource manager status:",
                                e);
            }
        }
        return m;
    }

    public int getExitStatus() {
        JobInfo info = null;
        int retVal = -255;
        try {
            info = session.wait(jobID, Session.TIMEOUT_NO_WAIT);
            if (info != null && info.hasExited()) {
                retVal = info.getExitStatus();
            }
        } catch (ExitTimeoutException ete) {
            /*
             * This exception is OK - it's always thrown, when the job is still
             * running. Just ignore and do nothing...
             */
        } catch (DrmaaException e) {
            /* This kind of exception is NOT OK - something ugly happened... */
            if (logger.isDebugEnabled()) {
                logger.debug("-- SGEJob EXCEPTION --");
                logger.debug("Got an exception while retrieving JobInfo:", e);
            }
        }

        return retVal;
    }

    public void stop() throws GATInvocationException {
        if (state == JobState.POST_STAGING || state == JobState.STOPPED
                || state == JobState.SUBMISSION_ERROR) {
            return;
        }
        if (!(state == JobState.RUNNING) || (state == JobState.ON_HOLD)
                || (state == JobState.SCHEDULED)) {
            throw new GATInvocationException(
                    "Cant stop(): job is not in a running state");
        } else {
            jsl.stop(!(gatContext.getPreferences().containsKey("job.stop.poststage")
        	    && gatContext.getPreferences().get("job.stop.poststage").equals("false")));
        }
    }
}
