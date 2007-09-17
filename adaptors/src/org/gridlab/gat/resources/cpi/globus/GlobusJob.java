package org.gridlab.gat.resources.cpi.globus;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.common.ResourceManagerContact;
import org.globus.gram.Gram;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.globus.gram.internal.GRAMConstants;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.SerializedJob;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;

/**
 * @author rob
 */
public class GlobusJob extends JobCpi implements GramJobListener,
        org.globus.gram.internal.GRAMConstants {

	protected static Logger logger = Logger.getLogger(GlobusJob.class);
	
	private static int jobsAlive = 0;

    private GramJob j;

    private MetricDefinition statusMetricDefinition;

    private Metric statusMetric;

    private boolean postStageFinished = false;

    private boolean postStageStarted = false;

    private String jobID;

    private JobPoller poller;

    private long queueTime;

    private long runTime;

    private long startTime;

    public GlobusJob(GATContext gatContext, Preferences preferences,
            GlobusResourceBrokerAdaptor broker, JobDescription jobDescription,
            GramJob j, Sandbox sandbox, long startTime) {
        super(gatContext, preferences, jobDescription, sandbox);
        this.startTime = startTime;
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

    /** constructor for unmarshalled jobs
     */
    public GlobusJob(GATContext gatContext, Preferences preferences,
            SerializedJob sj) throws GATObjectCreationException {
        super(gatContext, preferences, sj.getJobDescription(), sj.getSandbox());

        if (logger.isDebugEnabled()) {
            logger.debug("reconstructing globusjob: " + sj);
        }

        this.postStageFinished = sj.isPostStageFinished();
        this.jobID = sj.getJobId();
        this.queueTime = sj.getQueueTime();
        this.runTime = sj.getRunTime();
        this.startTime = sj.getStartTime();

        jobsAlive++;

        // Tell the engine that we provide job.status events
        HashMap returnDef = new HashMap();
        returnDef.put("status", String.class);
        statusMetricDefinition =
                new MetricDefinition("job.status", MetricDefinition.DISCRETE,
                        "String", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);

        j = new GramJob("");

        try {
            j.setID(jobID);
        } catch (Exception e) {
            throw new GATObjectCreationException("globus job", e);
        }

        URI hostUri;
        try {
            URL u = new URL(jobID);
            hostUri = new URI(u.getHost());
        } catch (Exception e) {
            throw new GATObjectCreationException("globus job", e);
        }

        GSSCredential credential = null;
        try {
            credential =
                    GlobusSecurityUtils.getGlobusCredential(gatContext,
                            preferences, "gram", hostUri,
                            ResourceManagerContact.DEFAULT_PORT);
        } catch (CouldNotInitializeCredentialException e) {
            throw new GATObjectCreationException("globus", e);
        } catch (CredentialExpiredException e) {
            throw new GATObjectCreationException("globus", e);
        }

        j.setCredentials(credential);
        j.addListener(this);
        
        getStateActive();

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

    protected synchronized void setState() {
        if (j.getError() == GramError.GRAM_JOBMANAGER_CONNECTION_FAILURE) {
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
            if (postStageFinished) {
                state = STOPPED;
            } else if (postStageStarted) {
                state = POST_STAGING;
            } else {
                state = RUNNING;
            }
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
            logger.warn("WARNING: Globus job: unknown state: "
                    + j.getStatus() + " (" + j.getStatusAsString() + ")");
        }
    }

    public void stop() throws GATInvocationException {
        String stateString = null;
        synchronized (this) {
            // we don't want to postStage twice (can happen with jobpoller)
            if (postStageStarted) {
                return;
            }
            postStageStarted = true;
            state = POST_STAGING;
            stateString = getStateString(state);
        }
        stopHandlers();

        MetricValue v =
                new MetricValue(this, stateString, statusMetric, System
                        .currentTimeMillis());

        if (logger.isDebugEnabled()) {
            logger.debug("globus job stop: firing event: " + v);
        }

        GATEngine.fireMetric(this, v);

        GATInvocationException x = null;
        try {
            if (j != null)
                j.cancel();
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("got an exception while cancelling job: "
                        + e);
            }

            try {
                j.signal(GRAMConstants.SIGNAL_CANCEL);
            } catch (Exception e2) {
                if (logger.isInfoEnabled()) {
                    System.err
                            .println("got an exception while sending signal to job: "
                                    + e2);
                }

                x = new GATInvocationException();
                x.add("globus job", e);
                x.add("globus job", e2);
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("globus job stop: delete/wipe starting");
        }

        // do cleanup, callback handler has been uninstalled
        sandbox.retrieveAndCleanup(this);

        synchronized (this) {
            postStageFinished = true;

            if (logger.isInfoEnabled()) {
                logger.info("globus job stop: post stage finished");
            }

            state = STOPPED;
            stateString = getStateString(state);
        }

        MetricValue v2 =
                new MetricValue(this, stateString, statusMetric, System
                        .currentTimeMillis());

        if (logger.isDebugEnabled()) {
            logger.debug("globus job stop: firing event: " + v2);
        }

        GATEngine.fireMetric(this, v2);

        finished();
        /*
         if (x != null) {
         throw x;
         }
         */
    }

    protected void stopHandlers() {
        try {
            Gram.unregisterListener(j);
        } catch (Throwable t) {
            if (logger.isInfoEnabled()) {
                System.err
                        .println("WARNING, globus job could not unbind: " + t);
            }
        }

        jobsAlive--;

        if (jobsAlive == 0) {
            try {
                Gram.deactivateAllCallbackHandlers();
            } catch (Throwable t) {
                if (logger.isInfoEnabled()) {
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
        if (logger.isDebugEnabled()) {
            logger.debug("polling state of globus job");
        }
        try {
            if (j != null) {
                Gram.jobStatus(j); // this call will trigger the listeners if the state changed.
            }
        } catch (NullPointerException x) {
            // ignore, fore some reason the first time, gram throws a null pointer exception.
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                System.err
                        .println("WARNING, could not get state of globus job: "
                                + e);
            }

            if (j.getError() == GramError.GRAM_JOBMANAGER_CONNECTION_FAILURE) {
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

        if (newJob.getError() == GramError.GRAM_JOBMANAGER_CONNECTION_FAILURE) {
            globusState = STATUS_DONE;
        }

        synchronized (this) {
            if (postStageStarted)
                return; // we don't want to postStage twice (can happen with jobpoller)
            if (logger.isInfoEnabled()) {
                logger.info("globus job callback: new Job id: "
                        + newJob.getIDAsString() + ", state = "
                        + newJob.getStatusAsString() + " error = "
                        + GramError.getGramErrorString(newJob.getError()));
            }

            setState();
            stateString = getStateString(state);

            if (state == SCHEDULED) {
                queueTime = System.currentTimeMillis();
            }
            if (state == RUNNING) {
                runTime = System.currentTimeMillis();
                queueTime = runTime - queueTime;
            }

            if ((globusState == STATUS_DONE) || (globusState == STATUS_FAILED)) {
                runTime = System.currentTimeMillis() - runTime;
                stopHandlers();
                if (poller != null)
                    poller.die();
                postStageStarted = true;
            }

            notifyAll();
        }

        MetricValue v =
                new MetricValue(this, stateString, statusMetric, System
                        .currentTimeMillis());

        if (logger.isDebugEnabled()) {
            logger.debug("globus job callback: firing event: " + v);
        }
        GATEngine.fireMetric(this, v);

        if (globusState == STATUS_DONE || globusState == STATUS_FAILED) {
            if (sandbox != null) {
                sandbox.retrieveAndCleanup(this);
            }

            synchronized (this) {
                postStageFinished = true;

                if (logger.isInfoEnabled()) {
                    System.err
                            .println("globus job callback: post stage finished");
                }

                setState();
                stateString = getStateString(state);

                notifyAll();
            }

            MetricValue v2 =
                    new MetricValue(this, stateString, statusMetric, System
                            .currentTimeMillis());

            if (logger.isDebugEnabled()) {
                logger.debug("globus job callback: firing event: " + v2);
            }

            GATEngine.fireMetric(this, v2);
            finished();

            if (GATEngine.TIMING) {
                System.err.println("TIMING: job " + jobID + ":" + " preStage: "
                        + sandbox.getPreStageTime() + " queue: " + queueTime
                        + " run: " + runTime + " postStage: "
                        + sandbox.getPostStageTime() + " wipe: "
                        + sandbox.getWipeTime() + " delete: "
                        + sandbox.getDeleteTime() + " total: "
                        + (System.currentTimeMillis() - startTime));
            }
        }
    }

    /*
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        SerializedJob sj;
        synchronized (this) {

            // we have to wait until the job is in a safe state
            // we cannot marshal it if it is halfway during the poststage process
            while (true) {
                if (jobID != null) {
                    if (!postStageStarted)
                        break;
                    if (postStageFinished)
                        break;
                }

                try {
                    wait();
                } catch (Exception e) {
                    // ignore
                }
            }

            sj =
                    new SerializedJob(jobDescription, sandbox,
                            postStageFinished, jobID, queueTime, runTime,
                            startTime);
        }
        String res = GATEngine.defaultMarshal(sj);
        if (logger.isDebugEnabled()) {
            logger.debug("marshalled seralized job: " + res);
        }
        return res;
    }

    public static Advertisable unmarshal(GATContext context,
            Preferences preferences, String s)
            throws GATObjectCreationException {
        if (logger.isDebugEnabled()) {
            logger.debug("unmarshalled seralized job: " + s);
        }

        SerializedJob sj =
                (SerializedJob) GATEngine.defaultUnmarshal(SerializedJob.class,
                        s);

        // if this job was created within this JVM, just return a reference to the job
        synchronized (JobCpi.class) {
            for (int i = 0; i < jobList.size(); i++) {
                JobCpi j = (JobCpi) jobList.get(i);
                if (j instanceof GlobusJob) {
                    GlobusJob gj = (GlobusJob) j;
                    if (gj.jobID.equals(sj.getJobId())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("returning existing job: " + gj);
                        }
                        return gj;
                    }
                }
            }
        }

        return new GlobusJob(context, preferences, sj);
    }
}
