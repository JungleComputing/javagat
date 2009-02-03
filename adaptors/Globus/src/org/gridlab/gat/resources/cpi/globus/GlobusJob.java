package org.gridlab.gat.resources.cpi.globus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
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

    private WritableInputStream stderrStream = new WritableInputStream();

    private WritableInputStream stdoutStream = new WritableInputStream();

    private GramJob j;

    private MetricDefinition statusMetricDefinition;

    private Metric statusMetric;

    private String globusJobID;

    private JobPoller poller;

    private int globusJobState = 0;

    private boolean exitStatusEnabled = false;

    private String exitStatusFile;

    private int exitStatusFromFile;

    private int streamingOutputs = 0;

    // shouldn't interfere with other JavaGAT job states
    private static final int GLOBUS_JOB_STOPPED = 1984;

    private static final int GLOBUS_JOB_SUBMISSION_ERROR = 1985;

    protected void startPoller() {
        poller = new JobPoller(this);
        poller.start();
    }

    protected GlobusJob(GATContext gatContext, JobDescription jobDescription,
            Sandbox sandbox) {
        super(gatContext, jobDescription, sandbox);
        state = JobState.SCHEDULED;
        jobsAlive++;

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        registerMetric("getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);
    }

    /**
     * constructor for unmarshalled jobs
     */
    public GlobusJob(GATContext gatContext, SerializedJob sj)
            throws GATObjectCreationException {
        super(gatContext, sj.getJobDescription(), sj.getSandbox());

        if (logger.isDebugEnabled()) {
            logger.debug("reconstructing globusjob: " + sj);
        }

        this.globusJobID = sj.getJobId();
        this.starttime = sj.getStarttime();
        this.stoptime = sj.getStoptime();
        this.submissiontime = sj.getSubmissiontime();

        jobsAlive++;

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        registerMetric("getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);

        j = new GramJob("");

        try {
            j.setID(globusJobID);
        } catch (Exception e) {
            throw new GATObjectCreationException("globus job", e);
        }

        URI hostUri;
        try {
            URL u = new URL(globusJobID);
            hostUri = new URI(u.getHost());
        } catch (Exception e) {
            throw new GATObjectCreationException("globus job", e);
        }

        GSSCredential credential = null;
        try {
            credential = GlobusSecurityUtils.getGlobusCredential(gatContext,
                    "gram", hostUri, ResourceManagerContact.DEFAULT_PORT);
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

    protected void setGramJob(GramJob j) {
        this.j = j;
        j.addListener(this);
        this.globusJobID = j.getIDAsString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getExitStatus()
     */
    public synchronized int getExitStatus() throws GATInvocationException {
        if (!(state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR)) {
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

        m.put("state", state.toString());
        if (state != JobState.RUNNING) {
            m.put("hostname", null);
        } else {
            m.put("hostname", j.getID().getHost());
        }
        if (state == JobState.INITIAL || state == JobState.UNKNOWN) {
            m.put("globus.state", null);
            m.put("globus.error", null);
            m.put("globus.errorno", null);
            m.put("submissiontime", null);
        } else {
            m.put("globus.state", getGlobusState());
            m.put("globus.error", GramError.getGramErrorString(j.getError()));
            m.put("globus.errorno", "" + j.getError());
            m.put("adaptor.job.id", globusJobID);
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
        m.put("resourcebroker", "Globus");
        try {
            m.put("exitvalue", "" + getExitStatus());
        } catch (GATInvocationException e) {
            // ignore
        }
        if (deleteException != null) {
            m.put("delete.exception", deleteException);
        }
        if (wipeException != null) {
            m.put("wipe.exception", wipeException);
        }
        return m;
    }

    public synchronized void stop() throws GATInvocationException {
        if (state == JobState.POST_STAGING
                || state == JobState.STOPPED
                || state == JobState.SUBMISSION_ERROR) {
            return;
        }
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
            /* The code below is wrong! waitForJobCompletion waits until the job is
             * either in state SUBMISSION_ERROR or STOPPED. If in state STOPPED,
             * poststaging is done if necessary. If in state SUBMISSION_ERROR, no
             * poststaging should be done. So: commented out the code below. --Ceriel
             * 
            if (!(gatContext.getPreferences().containsKey("job.stop.poststage") && gatContext
                    .getPreferences().get("job.stop.poststage").equals("false"))) {
                setState(JobState.POST_STAGING);
                sandbox.retrieveAndCleanup(this);
            }
            */
        } else {
            // this can happen if an exception is thrown after the creation of
            // this job in the submitjob method, simply remove the job from the
            // list.
            finished();
        }
    }

    private synchronized void waitForJobCompletion() {
        while (state != JobState.STOPPED && state != JobState.SUBMISSION_ERROR) {
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
        if (state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) {
            return;
        }
        int status = newJob.getStatus();
        boolean stateChanged = false;
        if (newJob.getError() == GramError.GRAM_JOBMANAGER_CONNECTION_FAILURE) {
            if (globusJobState != GLOBUS_JOB_SUBMISSION_ERROR
                    && globusJobState != GLOBUS_JOB_STOPPED) {
                globusJobState = GLOBUS_JOB_STOPPED;
            }
        } else {
            stateChanged = setState(convertGram2Gat(status));
        }
        if (!stateChanged && globusJobState == 0) {
            return;
        } else if (stateChanged && state == JobState.SCHEDULED) {
            globusJobID = j.getIDAsString();
            setSubmissionTime();
        } else if (stateChanged && state == JobState.RUNNING) {
            setStartTime();
        } else if (globusJobState == GLOBUS_JOB_STOPPED
                || globusJobState == GLOBUS_JOB_SUBMISSION_ERROR) {
            while (streamingOutputs > 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            setState(JobState.POST_STAGING);
            if (sandbox != null) {
                sandbox.retrieveAndCleanup(this);
            }
            if (globusJobState == GLOBUS_JOB_STOPPED) {
                setState(JobState.STOPPED);
            } else {
                setState(JobState.SUBMISSION_ERROR);
            }
            if (exitStatusEnabled && globusJobState == GLOBUS_JOB_STOPPED) {
                try {
                    readExitStatus();
                } catch (GATInvocationException e) {
                    logger
                            .info("reading the exit status from file failed: ",
                                    e);
                }
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

    private JobState convertGram2Gat(int gramStatus) {
        switch (gramStatus) {
        case STATUS_ACTIVE:
            return JobState.RUNNING;
        case STATUS_DONE:
            globusJobState = GLOBUS_JOB_STOPPED;
            return state;
        case STATUS_FAILED:
            globusJobState = GLOBUS_JOB_SUBMISSION_ERROR;
            return state;
        case STATUS_PENDING:
            return JobState.SCHEDULED;
        case STATUS_STAGE_IN:
            return JobState.PRE_STAGING;
        case STATUS_STAGE_OUT:
            return JobState.POST_STAGING;
        case STATUS_SUSPENDED:
            return JobState.ON_HOLD;
        case 0: // unknown (no constant :-( )
            return JobState.UNKNOWN;
        case STATUS_UNSUBMITTED:
            return JobState.INITIAL;
        default:
            logger.warn("WARNING: Globus job: unknown state: " + gramStatus);
            return JobState.UNKNOWN;
        }
    }

    protected boolean setState(JobState state) {
        if (this.state == state) {
            return false;
        }
        this.state = state;
        MetricEvent v = new MetricEvent(this, state, statusMetric, System
                .currentTimeMillis());
        fireMetric(v);
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
                if (globusJobID != null) {
                    break;
                }

                try {
                    wait();
                } catch (Exception e) {
                    // ignore
                }
            }

            sj = new SerializedJob(jobDescription, sandbox, globusJobID,
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
                    if (gj.globusJobID.equals(sj.getJobId())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("returning existing job: " + gj);
                        }
                        return gj;
                    }
                }
            }
        }
        return new GlobusJob(context, sj);
    }

    public void setExitValueEnabled(boolean exitValueEnabled,
            String exitValueFile) {
        this.exitStatusEnabled = exitValueEnabled;
        this.exitStatusFile = exitValueFile;
    }

    public InputStream getStdout() throws GATInvocationException {
        if (jobDescription.getSoftwareDescription().streamingStdoutEnabled()) {
            return stdoutStream;
        } else {
            throw new GATInvocationException("stdout streaming is not enabled!");
        }
    }

    public InputStream getStderr() throws GATInvocationException {
        if (jobDescription.getSoftwareDescription().streamingStderrEnabled()) {
            return stderrStream;
        } else {
            throw new GATInvocationException("stderr streaming is not enabled!");
        }
    }

    protected void startStderrForwarder(File err) {
        new OutputForwarder(err, stderrStream);
    }

    protected void startStdoutForwarder(File out) {
        new OutputForwarder(out, stdoutStream);
    }

    // protected void startOutputForwarder(File out, File err) {
    // logger.debug("starting output forwarder!");
    // new OutputForwarder(in, out);
    // }

    class OutputForwarder extends Thread {

        File source;

        WritableInputStream target;

        OutputForwarder(File source, WritableInputStream target) {
            setName("GlobusJob Output Waiter");
            setDaemon(true);
            this.source = source;
            this.target = target;
            synchronized (GlobusJob.this) {
                streamingOutputs++;
            }
            start();
        }

        public void run() {
            int totalBytesRead = 0;
            byte[] buffer = new byte[1024];
            // wait until the remote output file exists
            while (!source.exists()
                    && GlobusJob.this.getState() != GlobusJob.JobState.STOPPED
                    && GlobusJob.this.getState() != GlobusJob.JobState.SUBMISSION_ERROR) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {

                }
            }
            // now continuously check if there's new data to read
            while (true) {
                int bytesRead;
                // remember the state before you read, because if the job is
                // already stopped before the read AND there's no new data to be
                // read, we're done
                int globusStateBeforeRead = globusJobState;
                InputStream inStream = null;
                try {
                    inStream = GAT.createFileInputStream(source);
                } catch (GATObjectCreationException e2) {
                    logger.debug("unable to stream output/error: " + e2);
                    return;
                }
                try {
                    logger
                            .debug("before skipping " + totalBytesRead
                                    + " bytes");
                    inStream.skip(totalBytesRead);
                    logger.debug("before reading");
                    bytesRead = inStream.read(buffer);
                    logger.debug("bytes read: " + bytesRead);
                    logger.debug("before closing");
                    inStream.close();
                    logger.debug("after closing");
                } catch (IOException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("failed to read: " + e);
                    }
                    try {
                        sleep(1000);
                    } catch (InterruptedException e1) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("failed to sleep: " + e1);
                        }
                    }
                    continue;
                }
                if (bytesRead == -1) {
                    if (globusStateBeforeRead == GLOBUS_JOB_STOPPED
                            || globusStateBeforeRead == GLOBUS_JOB_SUBMISSION_ERROR) {
                        break;
                    } else {
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("failed to sleep: " + e);
                            }
                        }
                    }
                } else {
                    totalBytesRead += bytesRead;
                    target.write(buffer, 0, bytesRead);
                }
            }
            target.finished();
            synchronized (GlobusJob.this) {
                streamingOutputs--;
                GlobusJob.this.notifyAll();
            }
        }
    }
}
