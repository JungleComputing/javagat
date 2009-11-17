package org.gridlab.gat.resources.cpi.wsgt4new;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
 * Implements JobCpi abstract class.
 * 
 * @author Roelof Kemp
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WSGT4newJob extends JobCpi implements GramJobListener {

    private MetricDefinition statusMetricDefinition;

    private GramJob job;

    private int exitStatus;

    private Metric statusMetric;

    private StateEnumeration jobState = StateEnumeration.Unsubmitted;

    private String submissionID;

    protected WSGT4newJob(GATContext gatContext, JobDescription jobDescription,
            Sandbox sandbox) {
        super(gatContext, jobDescription, sandbox);
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
    public WSGT4newJob(GATContext gatContext, SerializedJob sj)
            throws GATObjectCreationException {
        super(gatContext, sj.getJobDescription(), sj.getSandbox());
        
        if (sandbox != null) {
        	sandbox.setContext(gatContext);
        }

        if (System.getProperty("GLOBUS_LOCATION") == null) {
            String globusLocation = System.getProperty("gat.adaptor.path")
                    + java.io.File.separator + "GlobusAdaptor"
                    + java.io.File.separator;
            System.setProperty("GLOBUS_LOCATION", globusLocation);
        }

        if (System.getProperty("axis.ClientConfigFile") == null) {
            String axisClientConfigFile = System
                    .getProperty("gat.adaptor.path")
                    + java.io.File.separator
                    + "GlobusAdaptor"
                    + java.io.File.separator + "client-config.wsdd";
            System.setProperty("axis.ClientConfigFile", axisClientConfigFile);
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
            URL u = new URL(submissionID);
            hostUri = new URI(u.getHost());
        } catch (Exception e) {
            throw new GATObjectCreationException("globus job", e);
        }

        GSSCredential credential = null;
        try {
            credential = GlobusSecurityUtils.getGlobusCredential(gatContext,
                "ws-gram", hostUri, ResourceManagerContact.DEFAULT_PORT);
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
        StateEnumeration newState = job.getState();
        logger.debug("jobState: " + newState);
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
    
    synchronized void finishJob() {
        // To be called when submit fails ...
        finished();
        if (sandbox != null) {
            try {
                sandbox.removeSandboxDir();
            } catch(Throwable e) {
                // ignored
            }
        }
    }

    private synchronized void stop(boolean skipPostStage)
            throws GATInvocationException {
        if (state != JobState.STOPPED && state != JobState.SUBMISSION_ERROR) {
            try {
                job.cancel();
            } catch (Exception e) {
                finished();
                throw new GATInvocationException("WSGT4newJob", e);
            }
            if (state != JobState.POST_STAGING && !skipPostStage) {
                setState(JobState.POST_STAGING);
                sandbox.retrieveAndCleanup(this);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("job not running anymore!");
            }
        }
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
        // state stuff.
        // If we get here, we apparently get upcalls, so maybe we can cancel
        // the poller here? TODO!
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
    	if (jobState != null && jobState.equals(newState)) {
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
//            setState(JobState.POST_STAGING);
//            sandbox.retrieveAndCleanup(this);
            setState(JobState.STOPPED);
            setStopTime();
        } else if (jobState.equals(StateEnumeration.Failed)) {
//            setState(JobState.POST_STAGING);
//            sandbox.retrieveAndCleanup(this);
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


    @Override
    public synchronized org.gridlab.gat.resources.Job.JobState getState() {    	    	
    	logger.debug("Refresh status for job!");    
    		try {
				job.refreshStatus();				
				StateEnumeration newState = job.getState();
				
				if (null != newState) {
					doStateChange(newState);
				}
			} catch (Exception e) {
				logger.error("Cannot refresh status for job!", e);
			}
    	return this.state;
    }
    

}
