package org.gridlab.gat.resources.cpi.globus;

import java.util.HashMap;
import java.util.Map;

import org.globus.gram.Gram;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;

/**
 * @author rob
 */
public class GlobusJob extends Job implements GramJobListener,
        org.globus.gram.internal.GRAMConstants {
    static int jobsAlive = 0;

    GlobusBrokerAdaptor broker;

    JobDescription jobDescription;

    GramJob j;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    GATInvocationException postStageException = null;

    boolean postStageFinished = false;

    String jobID;

    public GlobusJob(GlobusBrokerAdaptor broker, JobDescription jobDescription,
            GramJob j) {
        this.broker = broker;
        this.jobDescription = jobDescription;
        this.j = j;
        jobID = j.getIDAsString();
        state = SCHEDULED;
        jobsAlive++;

        // Tell the engine that we provide job.status events
        HashMap returnDef = new HashMap();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
            MetricDefinition.DISCRETE, "String", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);
    }

    public JobDescription getJobDescription() {
        return jobDescription;
    }

    protected String getGlobusState() {
        if ((j.getStatus() == STATUS_DONE) && !postStageFinished) {
            return "POST_STAGING";
        }

        return j.getStatusAsString();
    }

    public synchronized Map getInfo() {
        HashMap m = new HashMap();
        setState(); // update the state
        m.put("state", getStateString());
        m.put("resManState", getGlobusState());
        m.put("resManName", "Globus");
        m.put("error", "" + j.getError());
        m.put("id", j.getIDAsString());

        if (getState() == RUNNING) {
            m.put("hostname", j.getID().getHost());
        }

        if (postStageException != null) {
            m.put("postStageError", postStageException);
        }

        return m;
    }

    public String getJobID() {
        return jobID;
    }

    public synchronized int getState() {
        return state;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        throw new Error("Not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.Advertisable#unmarshal(java.lang.String)
     */
    public Advertisable unmarshal(String input) {
        throw new Error("Not implemented");
    }

    protected void setState() {
        switch (j.getStatus()) {
        case STATUS_ACTIVE:
            state = RUNNING;

            break;

        case STATUS_DONE:

            if (postStageFinished) {
                state = STOPPED;
            } else {
                state = RUNNING;
            }

            break;

        case STATUS_FAILED:
            state = SUBMISSION_ERROR;

            break;

        case STATUS_PENDING:
            state = SCHEDULED;

            break;

        case STATUS_STAGE_IN:
            state = SCHEDULED;

            break;

        case STATUS_STAGE_OUT:
            state = SCHEDULED;

            break;

        case STATUS_SUSPENDED:
            state = STOPPED;

            break;

        case 0: // unknown (no constant :-( )
            state = INITIAL;

        case STATUS_UNSUBMITTED:
            state = INITIAL;

            break;

        default:
            System.err.println("WARNING: Globus job: unknown state: "
                + j.getStatus() + " (" + j.getStatusAsString() + ")");
        }
    }

    public void stop() throws GATInvocationException {
        if (getState() != RUNNING) {
            throw new GATInvocationException("Job is not running");
        }

        try {
            j.cancel();
        } catch (Exception e) {
            throw new GATInvocationException("globus job", e);
        }

        stopHandlers();

        state = INITIAL;
    }

    public void unSchedule() throws GATInvocationException {
        if (getState() != SCHEDULED) {
            throw new GATInvocationException("Job is not in SHCEDULED state");
        }

        try {
            j.cancel();
        } catch (Exception e) {
            throw new GATInvocationException("globus job", e);
        }

        stopHandlers();

        state = INITIAL;
    }

    protected void stopHandlers() {
        try {
            Gram.unregisterListener(j);
        } catch (Throwable t) {
            if (GATEngine.VERBOSE) {
                System.err
                    .println("WARNING, globus job could not unbind: " + t);
            }
        }

        jobsAlive--;

        if (jobsAlive == 0) {
            try {
                Gram.deactivateAllCallbackHandlers();
            } catch (Throwable t) {
                if (GATEngine.VERBOSE) {
                    System.err
                        .println("WARNING, globus job could not deactivate callback: "
                            + t);
                }
            }
        }
    }

    /**
     * @see org.globus.gram.GramJobListener#statusChanged(org.globus.gram.GramJob)
     */
    public void statusChanged(GramJob newJob) {
        String stateString = null;
        int globusState = newJob.getStatus();

        synchronized (this) {
            if (GATEngine.VERBOSE) {
                System.err.println("globus job callback: new Job id: "
                    + newJob.getIDAsString() + ", state = "
                    + newJob.getStatusAsString());
            }

            setState();
            stateString = getStateString();

            if ((globusState == STATUS_DONE) || (globusState == STATUS_FAILED)) {
                stopHandlers();
            }
        }

        MetricValue v = new MetricValue(this, stateString, statusMetric, System
            .currentTimeMillis());

        if (GATEngine.DEBUG) {
            System.err.println("globus job callback: firing event: " + v);
        }

        GATEngine.fireMetric(this, v);

        if (j.getStatus() == STATUS_DONE) {
            if (GATEngine.VERBOSE) {
                System.err.println("globus job callback: post stage starting");
            }

            try {
                broker.postStageFiles(jobDescription, broker
                    .getHostname(jobDescription));
            } catch (GATInvocationException e) {
                postStageException = e;
            }

            synchronized (this) {
                postStageFinished = true;

                if (GATEngine.VERBOSE) {
                    System.err
                        .println("globus job callback: post stage finished");
                }

                setState();
                stateString = getStateString();
            }

            MetricValue v2 = new MetricValue(this, stateString, statusMetric,
                System.currentTimeMillis());

            if (GATEngine.DEBUG) {
                System.err.println("globus job callback: firing event: " + v2);
            }

            GATEngine.fireMetric(this, v2);
        }
    }
}
