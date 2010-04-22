package org.gridlab.gat.resources.cpi.gridsam;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.icenigrid.gridsam.core.ConfigurationException;
import org.icenigrid.gridsam.core.ControlException;
import org.icenigrid.gridsam.core.JobInstance;
import org.icenigrid.gridsam.core.JobManager;
import org.icenigrid.gridsam.core.JobManagerException;
import org.icenigrid.gridsam.core.JobStage;
import org.icenigrid.gridsam.core.UnknownJobException;

public class GridSAMJob extends JobCpi {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // TODO: replace polling thread with call to ScheduledExecutor.
    
    private class PollingThread extends Thread {
        private Logger logger = LoggerFactory.getLogger(PollingThread.class);

        private GridSAMJob parent;

        org.icenigrid.gridsam.core.JobState currentGridSAMJobState = null;

        // how many events have we fired already ?
        private int firedEventCount = 0;

        public PollingThread(GridSAMJob parent) {
            setDaemon(true);
            setName("GridSAM Job Poller");
            this.parent = parent;
        }

        private void fireEvent(JobState state) {
            MetricEvent v = new MetricEvent(this, state, statusMetric, System
                    .currentTimeMillis());
            parent.fireMetric(v);
            if (logger.isDebugEnabled()) {
                logger.debug("firing event, event=" + v.toString());
            }
        }

        /**
         * sets new job state and fires an event if the state is different than
         * the previous one
         * 
         * @param state
         */
        private void setState(JobState state) {
            synchronized (this.parent) {
                parent.state = state;
            }
        }

        @SuppressWarnings("unchecked")
        public void run() {
            while (true) {

                // update the manager state of the job
                try {
                    jobInstance = adaptor.getJobManager()
                            .findJobInstance(jobID);
                } catch (JobManagerException e) {
                    logger.error("caught exception", e);
                    throw new RuntimeException(e);
                } catch (UnknownJobException e) {
                    logger.error("caught exception", e);
                    throw new RuntimeException(e);
                }

                if (logger.isDebugEnabled()) {
                    StringBuilder props = new StringBuilder();
                    Map properties = jobInstance.getProperties();
                    Iterator iterator = properties.keySet().iterator();
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        props.append("\n    ").append(next).append("=").append(
                                properties.get(next));
                    }
                    logger.debug("job properties (from GridSAM)="
                            + props.toString());
                }

                List stages = jobInstance.getJobStages();
                if (!isStopped && stages.size() > firedEventCount) {
                    // there are some event that were not fired (and we don't
                    // know about)
                    // lets get that knowledge :)

                    // first we set the new current state - the sooner the
                    // better
                    currentGridSAMJobState = jobInstance.getLastKnownStage()
                            .getState();
                    JobState currentJavaGATJobState = translateState(currentGridSAMJobState);
                    setState(currentJavaGATJobState);

                    /*
                     * and now we fire the event for each of the states we have
                     * found we can get ith element because: 1. this list is not
                     * big 2. GridSAM actually uses ArrayList for this
                     */
                    for (int i = firedEventCount; i < stages.size(); i++) {
                        fireEvent(translateState(((JobStage) stages.get(i))
                                .getState()));
                    }

                    // finally we set that we have fired every event
                    firedEventCount = stages.size();

                    if (isFinishedState(currentGridSAMJobState)) {
                        break;
                    }
                }
                if (isStopped) {
                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("sleeping interrupted");
                    }
                }

            }
            if (isFinishedState(currentGridSAMJobState)) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("polling thread exiting, gridsam job is finished");
                }
                setFinished();
            }
        }
    }

    private Logger logger = LoggerFactory.getLogger(GridSAMJob.class);

    private int exitVal = -1;

    private MetricDefinition statusMetricDefinition;

    private Metric statusMetric;

    private JobInstance jobInstance;

    private String jobID;

    private volatile boolean isStopped = false;

    private GridSAMResourceBrokerAdaptor adaptor;

    private PollingThread pollingThread;

    private long startTime;

    private long submissionTime;

    private long stopTime;

    public GridSAMJob(GATContext gatContext, JobDescription jobDescription,
            Sandbox sandbox,
            GridSAMResourceBrokerAdaptor gridSAMResourceBrokerAdaptor,
            JobInstance jobInstance) {
        super(gatContext, jobDescription, sandbox);
        submissionTime = System.currentTimeMillis();

        this.jobInstance = jobInstance;
        this.jobID = jobInstance.getID();
        this.adaptor = gridSAMResourceBrokerAdaptor;

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        registerMetric("getJobStatus", statusMetricDefinition);

        pollingThread = new PollingThread(this);
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    private boolean isFinishedState(org.icenigrid.gridsam.core.JobState jobState) {
        return jobState == org.icenigrid.gridsam.core.JobState.DONE
                || jobState == org.icenigrid.gridsam.core.JobState.TERMINATED
                || jobState == org.icenigrid.gridsam.core.JobState.FAILED;
    }

    private JobState translateState(org.icenigrid.gridsam.core.JobState jobState) {
        if (jobState == org.icenigrid.gridsam.core.JobState.ACTIVE) {
            setStartTime();
            return JobState.RUNNING;
        } else if (jobState == org.icenigrid.gridsam.core.JobState.DONE) {
            return JobState.POST_STAGING;
        } else if (jobState == org.icenigrid.gridsam.core.JobState.EXECUTED) {
            return JobState.POST_STAGING;
        } else if (jobState == org.icenigrid.gridsam.core.JobState.FAILED) {
            return JobState.SUBMISSION_ERROR;
        } else if (jobState == org.icenigrid.gridsam.core.JobState.PENDING) {
            return JobState.PRE_STAGING;
        } else if (jobState == org.icenigrid.gridsam.core.JobState.STAGED_IN
                || jobState == org.icenigrid.gridsam.core.JobState.STAGING_IN) {
            return JobState.PRE_STAGING;
        } else if (jobState == org.icenigrid.gridsam.core.JobState.STAGED_OUT
                || jobState == org.icenigrid.gridsam.core.JobState.STAGING_OUT) {
            return JobState.POST_STAGING;
        } else if (jobState == org.icenigrid.gridsam.core.JobState.TERMINATED) {
            return JobState.POST_STAGING;
        } else if (jobState == org.icenigrid.gridsam.core.JobState.UNDEFINED) {
            return JobState.UNKNOWN;
        } else {
            logger.warn("unknown job state: " + jobState.toString());
            return JobState.UNKNOWN;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getInfo()
     */
    public synchronized Map<String, Object> getInfo()
            throws GATInvocationException {
        HashMap<String, Object> m = new HashMap<String, Object>();

        getState();

        m.put("state", state);

        m.put("submissiontime", state == JobState.INITIAL
                || state == JobState.UNKNOWN ? null : submissionTime);

        m.put("starttime",
                state == JobState.INITIAL || state == JobState.UNKNOWN
                        || state == JobState.SCHEDULED ? null : startTime);

        m.put("hostname", state == JobState.RUNNING ? adaptor.getBroker()
                .getHost() : null);

        m.put("stoptime", state == JobState.STOPPED ? stopTime : null);

        m.put("poststage.exception", postStageException);

        if (exitVal != -1) {
            m.put("exitvalue", exitVal);
        }
        
        m.put("adaptor.job.id", jobID);

        m.put("resourcebroker", "GridSAM");

        if (deleteException != null) {
            m.put("delete.exception", deleteException);
        }
        if (wipeException != null) {
            m.put("wipe.exception", wipeException);
        }

        return m;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getExitStatus()
     */
    public synchronized int getExitStatus() throws GATInvocationException {
        if (state != JobState.STOPPED)
            throw new GATInvocationException("not in STOPPED state");
        String code = (String) jobInstance.getProperties().get(
                "urn:gridsam:exitcode");
        if (code == null) {
            throw new GATInvocationException(
                    "No exit status code from gridsam available");
        }
        try {
            exitVal = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            logger.error("exit code from gridsam not int, code=" + code);
            throw new GATInvocationException(
                    "No exit status code from gridsam available", e);
        }
        return exitVal;
    }

    private boolean isStillRunning() {
        return false;
    }

    void setFinished() {
        MetricEvent v = null;

        synchronized (this) {
            state = JobState.POST_STAGING;
            v = new MetricEvent(this, state, statusMetric, System
                    .currentTimeMillis());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("default job callback: firing event: " + v);
        }
        fireMetric(v);

        sandbox.retrieveAndCleanup(this);
        isStopped = true;

        synchronized (this) {
            stopTime = System.currentTimeMillis();
            state = JobState.STOPPED;
            v = new MetricEvent(this, state, statusMetric, System
                    .currentTimeMillis());
        }

        fireMetric(v);
        finished();
    }

    public void stop() throws GATInvocationException {

        if (isStillRunning()) {
            if (logger.isDebugEnabled()) {
                logger.debug("job is still running, trying to terminate it");
            }

            JobManager jobManager = null;
            try {
                jobManager = adaptor.getJobManager();
            } catch (ConfigurationException e) {
                logger
                        .error("unable to get jobManager. Shouldn't have happened");
                throw new GATInvocationException(
                        "unable to get jobManager. Shouldn't have happened");
            }
            try {
                jobManager.terminateJob(jobID);
            } catch (JobManagerException e) {
                logger.error("unable to terminate job with id=" + jobID, e);
                throw new GATInvocationException(
                        "unable to terminate job with id=" + jobID, e);
            } catch (ControlException e) {
                logger.error("unable to terminate job with id=" + jobID, e);
                throw new GATInvocationException(
                        "unable to terminate job with id=" + jobID, e);
            } catch (UnknownJobException e) {
                logger.error("unable to terminate job with id=" + jobID, e);
                throw new GATInvocationException(
                        "unable to terminate job with id=" + jobID, e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("job with id=" + jobID
                        + " successfully terminated");
            }
        }

        MetricEvent v = null;

        if (!(gatContext.getPreferences().containsKey("job.stop.poststage") && gatContext
                .getPreferences().get("job.stop.poststage").equals("false"))) {

            synchronized (this) {
                state = JobState.POST_STAGING;
                v = new MetricEvent(this, state, statusMetric, System
                        .currentTimeMillis());
            }

            if (logger.isDebugEnabled()) {
                logger.debug("default job callback: firing event: " + v);
            }
            fireMetric(v);

            sandbox.retrieveAndCleanup(this);
        }
        isStopped = true;

        synchronized (this) {
            stopTime = System.currentTimeMillis();
            state = JobState.STOPPED;
            v = new MetricEvent(this, state, statusMetric, System
                    .currentTimeMillis());
        }

        fireMetric(v);
        finished();
    }
}
