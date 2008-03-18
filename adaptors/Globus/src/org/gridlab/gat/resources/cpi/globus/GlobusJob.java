package org.gridlab.gat.resources.cpi.globus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import org.gridlab.gat.InvalidUsernameOrPasswordException;
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
@SuppressWarnings("serial")
public class GlobusJob extends JobCpi implements GramJobListener,
        org.globus.gram.internal.GRAMConstants {

    protected static Logger logger = Logger.getLogger(GlobusJob.class);

    private static int jobsAlive = 0;

    private GramJob j;

    private MetricDefinition statusMetricDefinition;

    private Metric statusMetric;

    private String jobID;

    private JobPoller poller;

    private int globusJobState = 0;

    private boolean exitStatusEnabled = false;

    private String exitStatusFile;

    private int exitStatusFromFile;

    // shouldn't interfere with other JavaGAT job states
    private static final int GLOBUS_JOB_STOPPED = 1984;
    private static final int GLOBUS_JOB_SUBMISSION_ERROR = 1985;

    protected void setGramJob(GramJob j) {
        this.j = j;
        j.addListener(this);
    }

    protected void startPoller() {
        poller = new JobPoller(this);
        poller.start();
    }

    protected GlobusJob(GATContext gatContext, Preferences preferences,
            JobDescription jobDescription, Sandbox sandbox) {
        super(gatContext, preferences, jobDescription, sandbox);
        state = SCHEDULED;
        jobsAlive++;

        logger.debug("--new version--");

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);
    }

    /**
     * constructor for unmarshalled jobs
     */
    public GlobusJob(GATContext gatContext, Preferences preferences,
            SerializedJob sj) throws GATObjectCreationException {
        super(gatContext, preferences, sj.getJobDescription(), sj.getSandbox());

        if (logger.isDebugEnabled()) {
            logger.debug("reconstructing globusjob: " + sj);
        }

        this.jobID = sj.getJobId();
        this.starttime = sj.getStarttime();
        this.stoptime = sj.getStoptime();
        this.submissiontime = sj.getSubmissiontime();

        jobsAlive++;

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
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
            credential = GlobusSecurityUtils.getGlobusCredential(gatContext,
                    preferences, "gram", hostUri,
                    ResourceManagerContact.DEFAULT_PORT);
        } catch (CouldNotInitializeCredentialException e) {
            throw new GATObjectCreationException("globus", e);
        } catch (CredentialExpiredException e) {
            throw new GATObjectCreationException("globus", e);
        } catch (InvalidUsernameOrPasswordException e) {
            throw new GATObjectCreationException("globus", e);
        }

        j.setCredentials(credential);
        j.addListener(this);

        getStateActive();

        poller = new JobPoller(this);
        poller.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getExitStatus()
     */
    public synchronized int getExitStatus() throws GATInvocationException {
        if (!(state == STOPPED || state == SUBMISSION_ERROR)) {
            throw new GATInvocationException("not in STOPPED state");
        }
        if (exitStatusEnabled) {
            return exitStatusFromFile;
        } else {
            return 0;
        }
        // We have to assume that the job ran correctly. Globus does
        // not return the exit code.
    }

    private synchronized String getGlobusState() {
        return j.getStatusAsString();
    }

    public synchronized Map<String, Object> getInfo()
            throws GATInvocationException {
        HashMap<String, Object> m = new HashMap<String, Object>();
        getStateActive(); // update the state

        m.put("state", getStateString(state));
        if (state != RUNNING) {
            m.put("hostname", null);
        } else {
            m.put("hostname", j.getID().getHost());
        }
        if (state == INITIAL || state == UNKNOWN) {
            m.put("globus.state", null);
            m.put("globus.error", null);
            m.put("globus.errorno", null);
            m.put("globus.id", null);
            m.put("id", null);
            m.put("submissiontime", null);
        } else {
            m.put("globus.state", getGlobusState());
            m.put("globus.error", GramError.getGramErrorString(j.getError()));
            m.put("globus.errorno", "" + j.getError());
            m.put("globus.id", j.getIDAsString());
            m.put("id", j.getIDAsString());
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
        m.put("resourcebroker", "Globus");
        m.put("exitvalue", "" + getExitStatus());
        if (deleteException != null) {
            m.put("delete.exception", deleteException);
        }
        if (wipeException != null) {
            m.put("wipe.exception", wipeException);
        }
        return m;
    }

    public String getJobID() {
        if (jobID == null) {
            jobID = j.getIDAsString();
        }
        if (jobID == null) {
            return "not yet known";
        }
        return jobID;
    }

    public void stop() throws GATInvocationException {
        if (j != null) {
            try {
                j.cancel();
            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    logger.info("got an exception while cancelling job: " + e);
                }
                try {
                    j.signal(GRAMConstants.SIGNAL_CANCEL);
                } catch (Exception e2) {
                    if (logger.isInfoEnabled()) {
                        logger
                                .info("got an exception while sending signal to job: "
                                        + e2);
                    }
                    GATInvocationException x = new GATInvocationException();
                    x.add("globus job", e);
                    x.add("globus job", e2);
                    x.add("globus job", new Exception(
                            "There may be jobs still running!"));
                    finished();
                    throw x;
                }
            }
            // the signal has been sent. Now we wait for the termination to
            // complete. This is indicated when the job enters the STOPPED state
            waitForJobCompletion();
        } else {
            // this can happen if an exception is thrown after the creation of
            // this job in the submitjob method, simply remove the job from the list.
            finished();
        }
    }

    private synchronized void waitForJobCompletion() {
        while (state != STOPPED && state != SUBMISSION_ERROR) {
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private void readExitStatus() throws GATInvocationException {
        java.io.File file = new java.io.File(exitStatusFile);
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(exitStatusFile));
        } catch (FileNotFoundException e) {
            throw new GATInvocationException("GlobusJob", e);
        }
        try {
            exitStatusFromFile = Integer.parseInt(bufferedReader.readLine());
        } catch (NumberFormatException e) {
            throw new GATInvocationException("GlobusJob", e);
        } catch (IOException e) {
            throw new GATInvocationException("GlobusJob", e);
        }
        if (!file.delete()) {
            logger.info("file '" + exitStatusFile
                    + "' holding the exit value, could not be deleted.");
        }
    }

    private void stopHandlers() {
        try {
            Gram.unregisterListener(j);
        } catch (Throwable t) {
            if (logger.isInfoEnabled()) {
                logger.info("WARNING, globus job could not unbind: " + t);
            }
        }

        jobsAlive--;

        if (jobsAlive == 0) {
            try {
                Gram.deactivateAllCallbackHandlers();
            } catch (Throwable t) {
                if (logger.isInfoEnabled()) {
                    logger
                            .info("WARNING, globus job could not deactivate callback: "
                                    + t);
                }
            }
        }
    }

    /**
     * we need this if there is a firewall/NAT blocking traffic from the job to
     * the local machine
     * 
     */
    protected void getStateActive() {
        if (logger.isDebugEnabled()) {
            logger.debug("polling state of globus job");
        }
        try {
            if (j != null) {
                Gram.jobStatus(j); // this call will trigger the listeners if
                // the state changed.
            }
        } catch (NullPointerException x) {
            // ignore, fore some reason the first time, gram throws a null
            // pointer exception.
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("WARNING, could not get state of globus job: "
                                + e);
            }
            if (j.getError() == GramError.GRAM_JOBMANAGER_CONNECTION_FAILURE) {
                // this means we could not contact the job manager, assume the
                // job has been finished.
                // report that the status has changed
                statusChanged(j);
            }
        }
    }

    /**
     * This method handes callbacks from globus.
     * 
     * @see org.globus.gram.GramJobListener#statusChanged(org.globus.gram.GramJob)
     */
    public void statusChanged(GramJob newJob) {
        handleStatusChanged(newJob);
    }

    private synchronized void handleStatusChanged(GramJob newJob) {
        // if the job is already done, simply return
        // because we remove the listeners will this ever be executed?
        if (state == STOPPED || state == SUBMISSION_ERROR) {
            return;
        }
        int status = newJob.getStatus();
        boolean stateChanged = false;
        if (newJob.getError() == GramError.GRAM_JOBMANAGER_CONNECTION_FAILURE) {
            if (globusJobState == GLOBUS_JOB_SUBMISSION_ERROR) {
                stateChanged = setState(SUBMISSION_ERROR);
            } else if (globusJobState == GLOBUS_JOB_STOPPED) {
                stateChanged = setState(STOPPED);
            } else {
                globusJobState = GLOBUS_JOB_STOPPED;
            }
        } else {
            stateChanged = setState(convertGram2Gat(status));
        }
        if (!stateChanged && globusJobState == 0) {
            return;
        } else if (state == SCHEDULED) {
            setSubmissionTime();
        } else if (state == RUNNING) {
            setStartTime();
        } else if (globusJobState == GLOBUS_JOB_STOPPED
                || globusJobState == SUBMISSION_ERROR) {
            if (exitStatusEnabled && globusJobState == GLOBUS_JOB_STOPPED) {
                try {
                    readExitStatus();
                } catch (GATInvocationException e) {
                    logger
                            .info("reading the exit status from file failed: ",
                                    e);
                }
            }
            setState(POST_STAGING);
            if (sandbox != null) {
                sandbox.retrieveAndCleanup(this);
            }
            if (globusJobState == GLOBUS_JOB_STOPPED) {
                setState(STOPPED);
            } else {
                setState(SUBMISSION_ERROR);
            }
            setStopTime();
            globusJobState = 0;
            stopHandlers();
            if (poller != null) {
                poller.die();
            }
            finished();
        }
        if (GATEngine.TIMING) {
            System.err.println("TIMING: job " + getJobID() + ":"
                    + " preStage: " + sandbox.getPreStageTime() + " queue: "
                    + (starttime - submissiontime) + " run: "
                    + (System.currentTimeMillis() - starttime) + " postStage: "
                    + sandbox.getPostStageTime() + " wipe: "
                    + sandbox.getWipeTime() + " delete: "
                    + sandbox.getDeleteTime() + " total: "
                    + (System.currentTimeMillis() - submissiontime));
        }
        notifyAll();
    }

    private int convertGram2Gat(int gramStatus) {
        switch (gramStatus) {
        case STATUS_ACTIVE:
            return RUNNING;
        case STATUS_DONE:
            globusJobState = GLOBUS_JOB_STOPPED;
            return state;
        case STATUS_FAILED:
            globusJobState = GLOBUS_JOB_SUBMISSION_ERROR;
            return state;
        case STATUS_PENDING:
            return SCHEDULED;
        case STATUS_STAGE_IN:
            return PRE_STAGING;
        case STATUS_STAGE_OUT:
            return POST_STAGING;
        case STATUS_SUSPENDED:
            return ON_HOLD;
        case 0: // unknown (no constant :-( )
            return UNKNOWN;
        case STATUS_UNSUBMITTED:
            return INITIAL;
        default:
            logger.warn("WARNING: Globus job: unknown state: " + gramStatus);
            return UNKNOWN;
        }
    }

    protected boolean setState(int state) {
        if (this.state == state) {
            return false;
        }
        this.state = state;
        MetricValue v = new MetricValue(this, getStateString(state),
                statusMetric, System.currentTimeMillis());
        GATEngine.fireMetric(this, v);
        return true;
    }

    /*
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        SerializedJob sj;
        synchronized (this) {

            // we have to wait until the job is in a safe state
            // we cannot marshal it if it is halfway during the poststage
            // process
            while (true) {
                if (jobID != null) {
                    break;
                }

                try {
                    wait();
                } catch (Exception e) {
                    // ignore
                }
            }

            sj = new SerializedJob(jobDescription, sandbox, jobID,
                    submissiontime, starttime, stoptime);
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

        SerializedJob sj = (SerializedJob) GATEngine.defaultUnmarshal(
                SerializedJob.class, s);

        // if this job was created within this JVM, just return a reference to
        // the job
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

    public void setExitValueEnabled(boolean exitValueEnabled,
            String exitValueFile) {
        this.exitStatusEnabled = exitValueEnabled;
        this.exitStatusFile = exitValueFile;
    }
}
