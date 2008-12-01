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

    private StateEnumeration jobState = StateEnumeration.Unsubmitted;

    private String submissionID;

    protected WSGT4newJob(GATContext gatContext, JobDescription jobDescription,
            Sandbox sandbox) {
        super(gatContext, jobDescription, sandbox);
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);
        poller = new Thread(this);
        poller.setDaemon(true);
        poller.setName("WSGT4 Job - "
                + jobDescription.getSoftwareDescription().getExecutable());
        poller.start();
    }

    protected synchronized void setState(JobState state) {
        if (this.state != state) {
            this.state = state;
            MetricEvent v = new MetricEvent(this, state, statusMetric, System
                    .currentTimeMillis());

            if (logger.isDebugEnabled()) {
                logger.debug("wsgt4new job callback: firing event: " + v);
            }
            GATEngine.fireMetric(this, v);
            if (state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) {
                try {
                    stop(false);
                } catch (GATInvocationException e) {
                    // ignore
                }
            }
        }
    }

    public synchronized void stop() throws GATInvocationException {
        stop(gatContext.getPreferences().containsKey("job.stop.poststage")
                && gatContext.getPreferences().get("job.stop.poststage")
                        .equals("false"));
    }

    private synchronized void stop(boolean skipPostStage)
            throws GATInvocationException {
        if (state != JobState.STOPPED && state != JobState.SUBMISSION_ERROR) {
            try {
                job.cancel();
            } catch (Exception e) {
                finished();
                finished = true;
                throw new GATInvocationException("WSGT4newJob", e);
            }
            if (!skipPostStage) {
                setState(JobState.POST_STAGING);
                sandbox.retrieveAndCleanup(this);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("job not running anymore!");
            }
        }
        finished = true;
        finished();
    }

    public synchronized int getExitStatus() throws GATInvocationException {
        if (getState() != JobState.STOPPED
                && getState() != JobState.SUBMISSION_ERROR) {
            throw new GATInvocationException("exit status not yet available");
        }
        return exitStatus;
    }

    public synchronized Map<String, Object> getInfo()
            throws GATInvocationException {
        HashMap<String, Object> m = new HashMap<String, Object>();

        m.put("adaptor.job.id", submissionID);
        m.put("state", state.toString());
        m.put("globus.state", jobState);
        if (state != JobState.RUNNING) {
            m.put("hostname", null);
        } else {
            m.put("hostname", job.getEndpoint().getAddress().getHost());
        }
        if (state == JobState.INITIAL || state == JobState.UNKNOWN) {
            m.put("submissiontime", null);
        } else {
            m.put("submissiontime", submissiontime);
        }
        if (state == JobState.INITIAL || state == JobState.UNKNOWN
                || state == JobState.SCHEDULED) {
            m.put("starttime", null);
        } else {
            m.put("starttime", starttime);
        }
        if (state != JobState.STOPPED) {
            m.put("stoptime", null);
        } else {
            m.put("stoptime", stoptime);
        }
        m.put("poststage.exception", postStageException);
        m.put("resourcebroker", "WSGT4new");
        m
                .put(
                        "exitvalue",
                        (getState() != JobState.STOPPED && getState() != JobState.SUBMISSION_ERROR) ? null
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
            /* Commented out to avoid recursive calls to doStateChange --Ceriel
             * (suggestion to do this was by Brian Carpenter).
            try {
                job.refreshStatus();
            } catch (Exception e) {
                // ignore
            }
            */
            StateEnumeration newState = job.getState();
            logger.debug("jobState (upcall): " + newState);
            doStateChange(newState);
        }
    }

    private void doStateChange(StateEnumeration newState) {
        // Don't allow "updates" from final states.
        // These were probably caused by the refreshStatus call in stateChanged(),
        // but there. It does no harm to test ...
        if (jobState.equals(StateEnumeration.Done)
                || jobState.equals(StateEnumeration.Failed)
                || newState.equals(jobState)) {
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
            setState(JobState.SCHEDULED);
        } else if (jobState.equals(StateEnumeration.Active)) {
            setStartTime();
            setState(JobState.RUNNING);
        } else if (jobState.equals(StateEnumeration.CleanUp)) {
            // setState(POST_STAGING);
            // sandbox.retrieveAndCleanup(this);
            // setState(STOPPED);
            // setStopTime();
        } else if (jobState.equals(StateEnumeration.Done)) {
            setState(JobState.POST_STAGING);
            sandbox.retrieveAndCleanup(this);
            setState(JobState.STOPPED);
            setStopTime();
        } else if (jobState.equals(StateEnumeration.Failed)) {
            setState(JobState.POST_STAGING);
            sandbox.retrieveAndCleanup(this);
            setState(JobState.SUBMISSION_ERROR);
        } else if (jobState.equals(StateEnumeration.StageIn)) {
            setState(JobState.PRE_STAGING);
        } else if (jobState.equals(StateEnumeration.StageOut)) {
            setState(JobState.POST_STAGING);
        } else if (jobState.equals(StateEnumeration.Suspended)) {
            setState(JobState.ON_HOLD);
        } else if (jobState.equals(StateEnumeration.Unsubmitted)
                && state != JobState.PRE_STAGING) {
            setState(JobState.INITIAL);
        }
    }

    protected void setGramJob(GramJob job) {
        this.job = job;
    }

    protected void submitted() {
        setSubmissionTime();
    }

    public void setSubmissionID(String submissionID) {
        this.submissionID = submissionID;

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
