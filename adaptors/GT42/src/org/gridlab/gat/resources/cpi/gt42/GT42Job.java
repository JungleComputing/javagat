package org.gridlab.gat.resources.cpi.gt42;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis.AxisProperties;
import org.apache.axis.EngineConfigurationFactory;
import org.globus.common.ResourceManagerContact;
import org.globus.exec.client.GramJob;
import org.globus.exec.client.GramJobListener;
import org.globus.exec.generated.StateEnumeration;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;

import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.ScheduledExecutor;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.SerializedJob;
import org.gridlab.gat.security.gt42.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;

public class GT42Job extends JobCpi implements GramJobListener, Runnable {

    private static final long serialVersionUID = 1L;

    // instance initializer sets personalized
    // EngineConfigurationFactory for the axis client.
    static {
        if (System.getProperty("GT42_LOCATION") == null) {
            String globusLocation = System.getProperty("gat.adaptor.path")
                    + java.io.File.separator + "GT42Adaptor"
                    + java.io.File.separator;
            System.setProperty("GT42_LOCATION", globusLocation);
        }
        if (AxisProperties.getProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME) == null) {
            AxisProperties.setProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME,
            "org.gridlab.gat.resources.cpi.gt42.GlobusEngineConfigurationFactory");
        }
    }
    private MetricDefinition statusMetricDefinition;

    private GramJob job;

    private int exitStatus;

    private Metric statusMetric;

    private StateEnumeration jobState = StateEnumeration.Unsubmitted;

    private String submissionID;

    protected GT42Job(GATContext gatContext, JobDescription jobDescription,
            Sandbox sandbox) {
        super(gatContext, jobDescription, sandbox);
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        registerMetric("getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);
        ScheduledExecutor.schedule(this, 5000, 5000);
    }

    /**
     * constructor for unmarshalled jobs
     */
    public GT42Job(GATContext gatContext, SerializedJob sj)
        throws GATObjectCreationException {
        super(gatContext, sj.getJobDescription(), sj.getSandbox());
        sandbox.setContext(gatContext);

        if (logger.isDebugEnabled()) {
            logger.debug("reconstructing wsgt4newjob: " + sj);
        }

        this.submissionID = sj.getJobId();
        this.starttime = sj.getStarttime();
        this.stoptime = sj.getStoptime();
        this.submissiontime = sj.getSubmissiontime();

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        registerMetric("getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);

        job = new GramJob();

        try {
            job.setHandle(submissionID);
        } catch (Exception e) {
            throw new GATObjectCreationException("globus job", e);
        }

        URI hostUri;
        try {
            URL u = new URL(submissionID);
            hostUri = new URI(u.getHost());
        } catch (Exception e) {
            throw new GATObjectCreationException("globus job", e);
        }

        GSSCredential credential = null;
        try {
            credential = GlobusSecurityUtils.getGlobusCredential(gatContext,
                    "ws-gram42", hostUri, ResourceManagerContact.DEFAULT_PORT);
        } catch (CouldNotInitializeCredentialException e) {
            throw new GATObjectCreationException("globus", e);
        } catch (CredentialExpiredException e) {
            throw new GATObjectCreationException("globus", e);
        } catch (InvalidUsernameOrPasswordException e) {
            throw new GATObjectCreationException("globus", e);
        }
        if (credential != null) {
            job.setCredentials(credential);
        } else {        
            job.setAuthorization(HostAuthorization.getInstance());
        }

        job.setMessageProtectionType(Constants.ENCRYPTION);
        job.setDelegationEnabled(true);

        job.addListener(this);
        try {
            job.refreshStatus();
        } catch(Throwable e) {
            logger.debug("refreshStatus gave exception: ", e);
        }
        StateEnumeration newState = job.getState();
        logger.debug("jobState: " + newState);
        if (newState == null) {
            // happens if the job is already done ...
            doStateChange(StateEnumeration.Done);
        } else {
            doStateChange(newState);
            ScheduledExecutor.schedule(this, 5000, 5000);
        }
    }

    protected synchronized void setState(JobState state) {
        if (submissiontime == 0L) {
            setSubmissionTime();
        }
        if (state.equals(JobState.RUNNING)) {
            if (starttime == 0L) {
                setStartTime();
            }
        }
        else if (state.equals(JobState.STOPPED)) {
            if (stoptime == 0L) {
                setStopTime();
            }
        }

        if (this.state != state) {
            this.state = state;
            MetricEvent v = new MetricEvent(this, state, statusMetric, System
                    .currentTimeMillis());

            if (logger.isDebugEnabled()) {
                logger.debug("GT4.2 job callback: firing event: " + v);
            }
            fireMetric(v);
            if (state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) {
                try {
                    stop(false);
                } catch (GATInvocationException e) {
                    // ignore
                }
            }
        }
    }

    synchronized void finishJob() {
        // To be called when submit fails ...
        finished();
        ScheduledExecutor.remove(this);
        if (sandbox != null) {
            try {
                sandbox.removeSandboxDir();
            } catch(Throwable e) {
                // ignored
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
                // Equivalent of job.cancel, which is deprecated.
                job.terminate(true, false, job.isDelegationEnabled());
                job.unbind();
            } catch (Exception e) {
                if (state != JobState.POST_STAGING && !skipPostStage) {
                    sandbox.retrieveAndCleanup(this);
                }
                setState(JobState.SUBMISSION_ERROR);
                finished();
                ScheduledExecutor.remove(this);
                throw new GATInvocationException("GT4.2 Job", e);
            }
            if (state != JobState.POST_STAGING && !skipPostStage) {
                setState(JobState.POST_STAGING);
                sandbox.retrieveAndCleanup(this);
                setState(JobState.STOPPED);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("job not running anymore!");
            }
        }
        finished();
        ScheduledExecutor.remove(this);
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
            /*
             * Commented out to avoid recursive calls to doStateChange --Ceriel
             * (suggestion to do this was by Brian Carpenter). try {
             * job.refreshStatus(); } catch (Exception e) { // ignore }
             */
            StateEnumeration newState = job.getState();
            logger.debug("jobState (upcall): " + newState);
            doStateChange(newState);
        }
    }

    private void doStateChange(StateEnumeration newState) {
        // Don't allow "updates" from final states.
        // These were probably caused by the refreshStatus call in
        // stateChanged(),
        // but there. It does no harm to test ...
        if (jobState != null && jobState.equals(StateEnumeration.Done)
                || jobState.equals(StateEnumeration.Failed)
                || jobState.equals(newState)) {
            return;
                }
        jobState = newState;

        boolean holding = job.isHolding();
        if (jobState != null && jobState.equals(StateEnumeration.Done)
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

    /*
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        SerializedJob sj;
        synchronized (this) {

            // we have to wait until the job is in a safe state
            // we cannot marshal it if it is halfway during the poststage
            // process
            while (submissionID == null) {
                try {
                    wait();
                } catch (Exception e) {
                    // ignore
                }
            }

            sj = new SerializedJob(getClass().getName(),
                    jobDescription, sandbox, submissionID,
                    submissiontime, starttime, stoptime);
        }
        String res = GATEngine.defaultMarshal(sj);
        if (logger.isDebugEnabled()) {
            logger.debug("marshalled seralized job: " + res);
        }
        return res;
    }

    public static Advertisable unmarshal(GATContext context, String s)
        throws GATObjectCreationException {
        if (logger.isDebugEnabled()) {
            logger.debug("unmarshalled serialized job: " + s);
        }

        SerializedJob sj = (SerializedJob) GATEngine.defaultUnmarshal(
                SerializedJob.class, s, GT42Job.class.getName());

        // if this job was created within this JVM, just return a reference to
        // the job
        synchronized (JobCpi.class) {
            for (int i = 0; i < jobList.size(); i++) {
                JobCpi j = (JobCpi) jobList.get(i);
                if (j instanceof GT42Job) {
                    GT42Job gj = (GT42Job) j;
                    if (sj.getJobId().equals(gj.submissionID)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("returning existing job: " + gj);
                        }
                        return gj;
                    }
                }
            }
        }
        return new GT42Job(context, sj);
    }



    protected void setGramJob(GramJob job) {
        this.job = job;
    }

    protected synchronized void submitted() {
        setSubmissionTime();
    }

    public void setSubmissionID(String submissionID) {
        this.submissionID = submissionID;

    }

    private int count = 0;

    public void run() {

        synchronized (this) {
            if (submissiontime != 0) {
                try {
                    job.refreshStatus();
                } catch(Throwable e) {
                    // ignored
                }

                try {
                    // TODO
                    StateEnumeration newState = job.getState();
                    logger.debug("jobState (poller): " + newState);
                    if (newState == null) {
                        if (count > 3) {
                            doStateChange(StateEnumeration.Done);
                        } else {
                            count++;
                        }
                    } else {
                        count = 0;
                        doStateChange(newState);
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
