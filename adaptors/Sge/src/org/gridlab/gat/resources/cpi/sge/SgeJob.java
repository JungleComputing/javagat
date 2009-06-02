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

    private Hashtable<String, Long> time = new Hashtable<String, Long>();

    /**
     * The jobStartListener runs in a thread and checks the job's state. When it
     * detects a state transition from SCHEDULED to RUN, it writes the time and
     * exits
     */
    private class jobStartListener implements Runnable {

        int state = 0x00;

        final int SLEEP = 250;

        Session session = null;

        String jobID = null;

        Hashtable<String, Long> time = null;

        public jobStartListener(Session session, String jobID,
                Hashtable<String, Long> time) {
            this.session = session;
            this.jobID = jobID;
            this.time = time;
        }

        public void run() {
            while (state != Session.RUNNING) {
                try {
                    state = session.getJobProgramStatus(jobID);
                    if (state == Session.FAILED) {
                        // TODO
                    } else if (state == Session.DONE) {
                        // TODO
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
            setState(JobState.RUNNING);

            jobStopListener jsl = new jobStopListener(this.session, this.jobID,
                    time);
            new Thread(jsl).start();
        }
    }

    /**
     * The jobStopListener runs in a thread and checks the job's state. When it
     * detects a state transition from RUN to STOP, it writes the time and exits
     */
    private class jobStopListener implements Runnable {

        int state = 0x00;

        final int SLEEP = 250;

        Session session = null;

        String jobID = null;

        Hashtable<String, Long> time = null;

        public jobStopListener(Session session, String jobID,
                Hashtable<String, Long> time) {
            this.session = session;
            this.jobID = jobID;
            this.time = time;
        }

        public void run() {
            while (state != Session.DONE) {
                try {
                    state = session.getJobProgramStatus(jobID);
                    if (state == Session.FAILED) {
                        // TODO
                    }
                    Thread.sleep(SLEEP);
                } catch (DrmaaException e) {
                    // TODO
                } catch (InterruptedException e) {
                    // TODO
                }
            }
            setState(JobState.POST_STAGING);
            sandbox.retrieveAndCleanup(SgeJob.this);
            setState(JobState.STOPPED);
            // Now we're in STOPPED state - set the time and exit
            time.put("stop", new Long(System.currentTimeMillis()));
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
        jobStartListener jsl = new jobStartListener(this.session, this.jobID,
                time);
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
            m.put("resManState", Integer.toString(session
                    .getJobProgramStatus(jobID)));
            m.put("adaptor.job.id", jobID);
            m.put("starttime", time.get("start"));
            m.put("stoptime", time.get("stop"));
            m.put("submissiontime", time.get("submission"));
            m.put("poststage.exception", postStageException);

        } catch (DrmaaException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("-- SGEJob EXCEPTION --");
                logger.debug("Got an exception while retrieving resource manager status:", e);
            }
        }
        return m;
    }

    public int getExitStatus() {
        JobInfo info = null;
        int retVal = -255;
        try {
            info = session.wait(jobID, Session.TIMEOUT_NO_WAIT);
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
        if (info != null) {
            if (info.hasExited()) {
                retVal = info.getExitStatus();
            } else {
                retVal = -255;
            }
        }
        return retVal;
    }

    public void stop() throws GATInvocationException {
        if (state == JobState.POST_STAGING
                || state == JobState.STOPPED
                || state == JobState.SUBMISSION_ERROR) {
            return;
        }
        if ((state != JobState.RUNNING)
                || (state != JobState.ON_HOLD)
                || (state != JobState.SCHEDULED)) {
            throw new GATInvocationException(
                    "Cant stop(): job is not in a running state");
        } else {
            try {
                session.control(jobID, Session.TERMINATE);
                if (!(gatContext.getPreferences().containsKey(
                        "job.stop.poststage") && gatContext.getPreferences()
                        .get("job.stop.poststage").equals("false"))) {
                    setState(JobState.POST_STAGING);
                    sandbox.retrieveAndCleanup(this);
                }
                setState(JobState.STOPPED);
            } catch (DrmaaException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("-- SGEJob EXCEPTION --");
                    logger.debug("Got an exception while trying to TERMINATE job:", e);
                }
            }
        }
    }
}
