package org.gridlab.gat.resources.cpi.globus;

import java.util.HashMap;
import java.util.Map;

import org.globus.gram.Gram;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.globus.gram.internal.GRAMConstants;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * This thread actively polls the globus state of a job. this is needed in case of firewalls.
 * 
 * @author rob
 *
 */
class JobPoller extends Thread {
    private GlobusJob j;

    private boolean die = false;

    JobPoller(GlobusJob j) {
        this.j = j;
        setDaemon(true);
    }

    public void run() {
        while (true) {
            if (j.getState() == Job.STOPPED) return;
            if (j.getState() == Job.SUBMISSION_ERROR) return;
            j.getStateActive();
            if (j.getState() == Job.STOPPED) return;
            if (j.getState() == Job.SUBMISSION_ERROR) return;

            synchronized (this) {
                try {
                    wait(20 * 1000);
                } catch (Exception e) {
                    // Ignore
                }
                if (die) {
                    if (GATEngine.DEBUG) {
                        System.err.println("Job poller killed");
                    }
                    return;
                }
            }
        }
    }

    synchronized void die() {
        die = true;
        notifyAll();
    }
}

/**
 * @author rob
 */
public class GlobusJob extends JobCpi implements GramJobListener,
        org.globus.gram.internal.GRAMConstants {

    static final int GRAM_JOBMANAGER_CONNECTION_FAILURE = 79;

    static int jobsAlive = 0;

    GlobusResourceBrokerAdaptor broker;

    GramJob j;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    boolean postStageFinished = false;

    boolean postStageStarted = false;

    String jobID;

    JobPoller poller;

    public GlobusJob(GATContext gatContext, Preferences preferences,
            GlobusResourceBrokerAdaptor broker, JobDescription jobDescription,
            GramJob j, Sandbox sandbox) {
        super(gatContext, preferences, jobDescription, sandbox);
        this.broker = broker;
        this.j = j;
        jobID = j.getIDAsString();
        state = SCHEDULED;
        jobsAlive++;

        // Tell the engine that we provide job.status events
        HashMap returnDef = new HashMap();
        returnDef.put("status", String.class);
        statusMetricDefinition =
                new MetricDefinition("job.status", MetricDefinition.DISCRETE,
                    "String", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);

        poller = new JobPoller(this);
        poller.start();
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.resources.Job#getExitStatus()
     */
    public synchronized int getExitStatus() throws GATInvocationException {
        if (state != STOPPED)
            throw new GATInvocationException("not in RUNNING state");
        return 0; // We have to assume that the job ran correctly. Globus does not return the exit code.
    }

    protected synchronized String getGlobusState() {
        if ((j.getStatus() == STATUS_DONE) && !postStageFinished) {
            return "POST_STAGING";
        }

        return j.getStatusAsString();
    }

    public synchronized Map getInfo() throws GATInvocationException {
        HashMap m = new HashMap();
        setState(); // update the state
        m.put("state", getStateString(state));
        m.put("resManState", getGlobusState());
        m.put("resManName", "Globus");
        m.put("resManError", GramError.getGramErrorString(j.getError()));
        m.put("resManErrorNr", "" + j.getError());
        m.put("resManId", j.getIDAsString());
        m.put("id", j.getIDAsString());

        if (getState() == RUNNING) {
            m.put("hostname", j.getID().getHost());
        }

        if (postStageException != null) {
            m.put("postStageError", postStageException);
        }
        if (deleteException != null) {
            m.put("deleteError", deleteException);
        }
        if (wipeException != null) {
            m.put("wipeError", wipeException);
        }

        return m;
    }

    public String getJobID() {
        return jobID;
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

    protected synchronized void setState() {
        if (j.getError() == GRAM_JOBMANAGER_CONNECTION_FAILURE) {
            // assume the job was done, and gram exited
            if (postStageFinished) {
                state = STOPPED;
            } else {
                state = POST_STAGING;
            }
            return;
        }

        switch (j.getStatus()) {
        case STATUS_ACTIVE:
            state = RUNNING;
            break;
        case STATUS_DONE:
            if (postStageFinished) {
                state = STOPPED;
            } else {
                state = POST_STAGING;
            }
            break;
        case STATUS_FAILED:
            if (postStageFinished) {
                state = SUBMISSION_ERROR;
            } else {
                state = POST_STAGING;
            }
            break;
        case STATUS_PENDING:
            state = SCHEDULED;
            break;
        case STATUS_STAGE_IN:
            state = PRE_STAGING;
            break;
        case STATUS_STAGE_OUT:
            state = POST_STAGING;
            break;
        case STATUS_SUSPENDED:
            state = ON_HOLD;
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
        try {
            if (j != null) j.cancel();
        } catch (Exception e) {
            if (GATEngine.VERBOSE) {
                System.err.println("got an exception while cancelling job: "
                    + e);
            }

            try {
                j.signal(GRAMConstants.SIGNAL_CANCEL);
            } catch (Exception e2) {
                if (GATEngine.VERBOSE) {
                    System.err
                        .println("got an exception while sending signal to job: "
                            + e2);
                }

                GATInvocationException x = new GATInvocationException();
                x.add("globus job", e);
                x.add("globus job", e2);
                throw x;
            }
        }

        stopHandlers();

        if (GATEngine.VERBOSE) {
            System.err.println("globus job stop: delete/wipe starting");
        }

        sandbox.retrieveAndCleanup(this);

        state = INITIAL;
        finished();
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
     * we need this if there is a firewall/NAT blocking traffic from the job to the 
     * local machine
     *
     */
    void getStateActive() {
        if (GATEngine.DEBUG) {
            System.err.println("polling state of globus job");
        }
        try {
            if (j != null) {
                Gram.jobStatus(j); // this call will trigger the listeners if the state changed.
            }
        } catch (NullPointerException x) {
            // ignore, fore some reason the first time, gram throws a null pointer exception.
        } catch (Exception e) {
            if (GATEngine.DEBUG) {
                System.err
                    .println("WARNING, could not get state of globus job: " + e);
            }

            if (j.getError() == GRAM_JOBMANAGER_CONNECTION_FAILURE) {
                // this means we could not contact the job manager, assume the job has been finished.
                // report that the status has changed
                handleStatusChange(j);
            }
        }
    }

    /**
     * This method handes callbacks from globus.
     * @see org.globus.gram.GramJobListener#statusChanged(org.globus.gram.GramJob)
     */
    public void statusChanged(GramJob newJob) {
        // If we ever receive a gram callback, we can kill the job poller thread; we are not 
        // behind a firewall.
        // OOPS, this is not possible, the poll also generates this callback!
//        poller.die();

        handleStatusChange(newJob);
    }
    
    private void handleStatusChange(GramJob newJob) {
        jobID = j.getIDAsString();
        String stateString = null;
        int globusState = newJob.getStatus();

        if (newJob.getError() == GRAM_JOBMANAGER_CONNECTION_FAILURE) {
            globusState = STATUS_DONE;
        }

        synchronized (this) {
            if (postStageStarted) return; // we don't want to postStage twice (can happen with jobpoller)
            if (GATEngine.VERBOSE) {
                System.err.println("globus job callback: new Job id: "
                    + newJob.getIDAsString() + ", state = "
                    + newJob.getStatusAsString() + " error = "
                    + GramError.getGramErrorString(newJob.getError()));
            }

            setState();
            stateString = getStateString(state);

            if ((globusState == STATUS_DONE) || (globusState == STATUS_FAILED)) {
                stopHandlers();
                postStageStarted = true;
            }
        }

        MetricValue v =
                new MetricValue(this, stateString, statusMetric, System
                    .currentTimeMillis());

        if (GATEngine.DEBUG) {
            System.err.println("globus job callback: firing event: " + v);
        }
        GATEngine.fireMetric(this, v);

        if (globusState == STATUS_DONE || globusState == STATUS_FAILED) {
            sandbox.retrieveAndCleanup(this);

            synchronized (this) {
                postStageFinished = true;

                if (GATEngine.VERBOSE) {
                    System.err
                        .println("globus job callback: post stage finished");
                }

                setState();
                stateString = getStateString(state);
            }

            MetricValue v2 =
                    new MetricValue(this, stateString, statusMetric, System
                        .currentTimeMillis());

            if (GATEngine.DEBUG) {
                System.err.println("globus job callback: firing event: " + v2);
            }

            GATEngine.fireMetric(this, v2);
            finished();
        }
    }
}
