package org.gridlab.gat.resources.cpi.wsgt4new;

import ibis.util.ThreadPool;

import java.security.Provider;
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
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
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
import org.gridlab.gat.security.globusws.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;

/**
 * Implements JobCpi abstract class.
 * 
 * @author Roelof Kemp
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WSGT4newJob extends JobCpi implements GramJobListener, Runnable {
    
    // instance initializer sets personalized
    // EngineConfigurationFactory for the axis client.
    static {
        if (System.getProperty("GLOBUS_LOCATION") == null) {
            String globusLocation = System.getProperty("gat.adaptor.path")
                    + java.io.File.separator + "GlobusWSAdaptor"
                    + java.io.File.separator;
            System.setProperty("GLOBUS_LOCATION", globusLocation);
        }
        if (AxisProperties.getProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME) == null) {
            AxisProperties.setProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME,
            "org.gridlab.gat.resources.cpi.wsgt4new.GlobusEngineConfigurationFactory");
        }
    }

    private MetricDefinition statusMetricDefinition;

    private GramJob job;

    private int exitStatus;

    private Metric statusMetric;

    private StateEnumeration jobState = StateEnumeration.Unsubmitted;

    private String submissionID;

    boolean stopped = false;

    protected WSGT4newJob(GATContext gatContext, JobDescription jobDescription,
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
     * Constructor for unmarshalled jobs.
     */
    public WSGT4newJob(GATContext gatContext, SerializedJob sj)
            throws GATObjectCreationException {
        super(gatContext, sj.getJobDescription(), sj.getSandbox());
        
        if (sandbox != null) {
            sandbox.setContext(gatContext);
        }
        
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
             hostUri = new URI(submissionID);
        } catch (Exception e) {
            throw new GATObjectCreationException("globus job", e);
        }

        GSSCredential credential = null;
        try {
            credential = GlobusSecurityUtils.getGlobusCredential(gatContext,
                "wsgt4new", hostUri, ResourceManagerContact.DEFAULT_PORT);
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
        } else if (jobState != null && 
                !(jobState.equals(StateEnumeration.Done)
                        || jobState.equals(StateEnumeration.Failed))) {
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
                logger.debug("wsgt4new job callback: firing event: " + v);
            }
            fireMetric(v);
            if (state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) {
                finished();
                ScheduledExecutor.remove(this);
                if (job != null) {
                    job.removeListener(this);
                    job.setDelegationEnabled(false);
                    try {
                        job.destroy();
                        job = null;
                    } catch (Throwable e) {
                        // ignore
                    }
                }
            }
        }
    }

    public synchronized void stop() throws GATInvocationException {
        stop(gatContext.getPreferences().containsKey("job.stop.poststage")
                && gatContext.getPreferences().get("job.stop.poststage")
                        .equals("false"));
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
        if (job != null) {
            job.setDelegationEnabled(false);
            try {
        	job.destroy();
                job = null;
            } catch(Throwable e) {
        	// ignore
            }
        }
        setState(JobState.STOPPED);
    }

    private synchronized void stop(boolean skipPostStage)
            throws GATInvocationException {
        stopped = true;
        if (state != JobState.STOPPED && state != JobState.SUBMISSION_ERROR) {
            if (job != null) {
                job.setDelegationEnabled(false);
                job.removeListener(this);
                try {
                    job.destroy();
                    job = null;
                } catch (Throwable e) {
                    if (state != JobState.SCHEDULED && state != JobState.POST_STAGING && !skipPostStage) {
                        sandbox.retrieveAndCleanup(this);
                    }
                    setState(JobState.SUBMISSION_ERROR);
                    finished();
                    ScheduledExecutor.remove(this);
                    
                    throw new GATInvocationException("WSGT4newJob", e);
                }
            }
            if (state == JobState.PRE_STAGING || state == JobState.SCHEDULED) {
        	// Don't post-stage, the program has not done anything yet.
        	finishJob();
            } else if (state != JobState.POST_STAGING && !skipPostStage) {
                setState(JobState.POST_STAGING);
                sandbox.retrieveAndCleanup(this);
            }
            setState(JobState.STOPPED);
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

        m.put(ADAPTOR_JOB_ID, submissionID);
        m.put(STATE, state.toString());
        m.put("globus.state", jobState);
        if (state != JobState.RUNNING) {
            m.put(HOSTNAME, null);
        } else {
            m.put(HOSTNAME, job.getEndpoint().getAddress().getHost());
        }
        if (state == JobState.INITIAL || state == JobState.UNKNOWN) {
            m.put(SUBMISSIONTIME, null);
        } else {
            m.put(SUBMISSIONTIME, submissiontime);
        }
        if (state == JobState.INITIAL || state == JobState.UNKNOWN
                || state == JobState.SCHEDULED) {
            m.put(STARTTIME, null);
        } else {
            m.put(STARTTIME, starttime);
        }
        if (state != JobState.STOPPED) {
            m.put(STOPTIME, null);
        } else {
            m.put(STOPTIME, stoptime);
        }
        m.put(POSTSTAGE_EXCEPTION, postStageException);
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
        // state stuff.
        // If we get here, we apparently get upcalls, so maybe we can cancel
        // the poller here? NO! We also get here if poller finds a state change.
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
        if (jobState != null && 
                (jobState.equals(StateEnumeration.Done)
                || jobState.equals(StateEnumeration.Failed))) {
            ScheduledExecutor.remove(this);
            return;
        } else if (jobState != null && jobState.equals(newState)) {
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
            // Create separate thread to do post-staging. We don't want to keep the
            // ScheduledThread busy too long. 
            ScheduledExecutor.remove(this);
            ThreadPool.createNew(new FinishUp(JobState.STOPPED), "finisher");
        } else if (jobState.equals(StateEnumeration.Failed)) {
            ScheduledExecutor.remove(this);
            ThreadPool.createNew(new FinishUp(JobState.SUBMISSION_ERROR), "finisher");
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
    
    private class FinishUp implements Runnable {
	private JobState state;

	public FinishUp(JobState state) {
	    this.state = state;
	}
	
	public void run() {
            if (job != null) {
                job.setDelegationEnabled(false);
                job.removeListener(WSGT4newJob.this);
                Provider p = GlobusSecurityUtils.setSecurityProvider();
                try {
                    job.destroy();
                    job = null;
                } catch(Throwable e) {
                    // ignore
                } finally {
                    GlobusSecurityUtils.restoreSecurityProvider(p);
                }
            }
	    setState(JobState.POST_STAGING);
            sandbox.retrieveAndCleanup(WSGT4newJob.this);
            if (state == JobState.STOPPED) {
        	setStopTime();
            }
            setState(state);
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
            SerializedJob.class, s, WSGT4newJob.class.getName());
        
        // if this job was created within this JVM, just return a reference to
        // the job
        synchronized (JobCpi.class) {
            for (int i = 0; i < jobList.size(); i++) {
                JobCpi j = (JobCpi) jobList.get(i);
                if (j instanceof WSGT4newJob) {
                    WSGT4newJob gj = (WSGT4newJob) j;
                    if (sj.getJobId().equals(gj.submissionID)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("returning existing job: " + gj);
                        }
                        try {
                            // Its not possible to reset the credentials to a job object.
                            // So create a WSGT4 instance if the credential is getting expired.
                            GSSCredential credential = gj.job.getCredentials();
                            if (credential.getRemainingLifetime() == 0) {
                                logger.debug("Credential expired. Create a new Job instance.");
                                jobList.remove(gj);
                                gj = null;
                                return new WSGT4newJob(context, sj);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Cannot retrieve new credentials for job.", e);
                        }                          
                            
                        return gj;
                    }
                }
            }
        }
        return new WSGT4newJob(context, sj);
    }

    protected synchronized void setGramJob(GramJob job) {
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
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                Provider p = GlobusSecurityUtils.setSecurityProvider();
                try {
                    job.refreshStatus();
                } catch(Throwable e) {
                    // ignored
                } finally {
                    GlobusSecurityUtils.restoreSecurityProvider(p);
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
