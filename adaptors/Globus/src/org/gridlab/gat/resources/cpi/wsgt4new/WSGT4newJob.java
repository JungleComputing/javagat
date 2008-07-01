package org.gridlab.gat.resources.cpi.wsgt4new;

import java.util.HashMap;
import java.util.Map;

import org.globus.exec.client.GramJob;
import org.globus.exec.client.GramJobListener;
import org.globus.exec.generated.StateEnumeration;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * Implements JobCpi abstract class.
 * 
 * @author Roelof Kemp
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WSGT4newJob extends JobCpi implements GramJobListener, Runnable {

    private MetricDefinition statusMetricDefinition;

    private GramJob job;

    private int exitStatus;

    private Metric statusMetric;

    private Thread poller;

    private boolean finished = false;

    private String jobID;

    private StateEnumeration jobState = StateEnumeration.Unsubmitted;

    protected WSGT4newJob(GATContext gatContext, JobDescription jobDescription,
            Sandbox sandbox) {
        super(gatContext, jobDescription, sandbox);
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);
        poller = new Thread(this);
        poller.setDaemon(true);
        poller.setName("WSGT4 Job - "
                + jobDescription.getSoftwareDescription().getExecutable());
        poller.start();
    }

    protected synchronized void setState(int state) {
        if (this.state != state) {
            this.state = state;
            MetricEvent v = new MetricEvent(this, getStateString(state),
                    statusMetric, System.currentTimeMillis());

            if (logger.isDebugEnabled()) {
                logger.debug("wsgt4new job callback: firing event: " + v);
            }
            GATEngine.fireMetric(this, v);
            if (state == STOPPED || state == SUBMISSION_ERROR) {
                try {
                    stop();
                } catch (GATInvocationException e) {
                    // ignore
                }
            }
        }
    }

    public synchronized void stop() throws GATInvocationException {
        if (state != STOPPED && state != SUBMISSION_ERROR) {
            try {
                job.cancel();
            } catch (Exception e) {
                finished();
                finished = true;
                throw new GATInvocationException("WSGT4newJob", e);
            }
            sandbox.retrieveAndCleanup(this);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("job not running anymore!");
            }
        }
        finished = true;
        finished();
    }

    public synchronized int getExitStatus() throws GATInvocationException {
        if (getState() != STOPPED && getState() != SUBMISSION_ERROR) {
            throw new GATInvocationException("exit status not yet available");
        }
        return exitStatus;
    }

    public synchronized Map<String, Object> getInfo()
            throws GATInvocationException {
        HashMap<String, Object> m = new HashMap<String, Object>();

        m.put("state", getStateString(state));
        m.put("globusstate", jobState);
        if (state != RUNNING) {
            m.put("hostname", null);
        } else {
            m.put("hostname", job.getEndpoint().getAddress().getHost());
        }
        if (state == INITIAL || state == UNKNOWN) {
            m.put("submissiontime", null);
        } else {
            m.put("submissiontime", submissiontime);
        }
        if (state == INITIAL || state == UNKNOWN || state == SCHEDULED) {
            m.put("starttime", null);
        } else {
            m.put("starttime", starttime);
        }
        if (state != STOPPED) {
            m.put("stoptime", null);
        } else {
            m.put("stoptime", stoptime);
        }
        m.put("poststage.exception", postStageException);
        m.put("resourcebroker", "WSGT4new");
        m
                .put(
                        "exitvalue",
                        (getState() != STOPPED && getState() != SUBMISSION_ERROR) ? null
                                : "" + getExitStatus());
        if (deleteException != null) {
            m.put("delete.exception", deleteException);
        }
        if (wipeException != null) {
            m.put("wipe.exception", wipeException);
        }
        return m;
    }

    public void stateChanged(GramJob job) {
        // don't let the upcall and the poller interfere, so synchronize the
        // state stuff
        synchronized (this) {
            try {
                job.refreshStatus();
            } catch (Exception e) {
                // ignore
            }
            StateEnumeration newState = job.getState();
            logger.debug("jobState (upcall): " + newState);
            doStateChange(newState);
        }
    }

    private void doStateChange(StateEnumeration newState) {
        if (newState.equals(jobState)) {
            return;
        }
        jobState = newState;

        boolean holding = job.isHolding();
        if (jobState.equals(StateEnumeration.Done)
                || jobState.equals(StateEnumeration.Failed)) {
            this.exitStatus = job.getExitCode();
        }
        // if we a running an interactive job,
        // prevent a hold from hanging the client
        if (holding) {
            logger.debug("Automatically releasing hold for interactive job");
            try {
                job.release();
            } catch (Exception e) {
                String errorMessage = "Unable to release job from hold";
                logger.debug(errorMessage, e);
            }
        }
        if (jobState.equals(StateEnumeration.Pending)) {
            setState(SCHEDULED);
        } else if (jobState.equals(StateEnumeration.Active)) {
            setStartTime();
            setState(RUNNING);
        } else if (jobState.equals(StateEnumeration.CleanUp)) {
            // setState(POST_STAGING);
            // sandbox.retrieveAndCleanup(this);
            // setState(STOPPED);
            // setStopTime();
        } else if (jobState.equals(StateEnumeration.Done)) {
            setState(POST_STAGING);
            sandbox.retrieveAndCleanup(this);
            setState(STOPPED);
            setStopTime();
        } else if (jobState.equals(StateEnumeration.Failed)) {
            setState(POST_STAGING);
            sandbox.retrieveAndCleanup(this);
            setState(SUBMISSION_ERROR);
        } else if (jobState.equals(StateEnumeration.StageIn)) {
            setState(PRE_STAGING);
        } else if (jobState.equals(StateEnumeration.StageOut)) {
            setState(POST_STAGING);
        } else if (jobState.equals(StateEnumeration.Suspended)) {
            setState(ON_HOLD);
        } else if (jobState.equals(StateEnumeration.Unsubmitted)
                && state != PRE_STAGING) {
            setState(INITIAL);
        }
    }

    protected void setGramJob(GramJob job) {
        this.job = job;
    }

    protected void submitted() {
        setSubmissionTime();
    }

    public void setJobID(String submissionID) {
        this.jobID = submissionID;

    }

    public String getJobID() {
        if (jobID == null) {
            return "unknown wsgt4 job";
        }
        return jobID;
    }

    public void run() {
        while (!finished) {
            synchronized (this) {
                try {
                    job.refreshStatus();

                    // TODO
                    StateEnumeration newState = job.getState();
                    logger.debug("jobState (poller): " + newState);
                    doStateChange(newState);
                } catch (Exception e) {
                    // ignore
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
