package org.gridlab.gat.resources.cpi.gridsam;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.OutputForwarder;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.icenigrid.gridsam.core.JobInstance;
import org.icenigrid.gridsam.core.JobManagerException;
import org.icenigrid.gridsam.core.JobStage;
import org.icenigrid.gridsam.core.JobState;
import org.icenigrid.gridsam.core.UnknownJobException;

/* TODO
 * 
 * exitVal - probably gridSAM does not support it
 */

public class GridSAMJob extends JobCpi {

    private Logger logger = Logger.getLogger(GridSAMJob.class);

    private Process p;

    private int exitVal = 0;

    private MetricDefinition statusMetricDefinition;

    private Metric statusMetric;
    
    private JobInstance jobInstance;
    
    private String jobID;

    private OutputForwarder out;

    private OutputForwarder err;
    
    private GridSAMResourceBrokerAdaptor adaptor;

    private long startTime;
    private long runTime;


    public GridSAMJob(GATContext gatContext, Preferences preferences, JobDescription jobDescription, Sandbox sandbox, GridSAMResourceBrokerAdaptor gridSAMResourceBrokerAdaptor, JobInstance jobInstance) {
        super(gatContext, preferences, jobDescription, sandbox);
        
        this.jobInstance = jobInstance;
        this.jobID = jobInstance.getID();
        this.adaptor = gridSAMResourceBrokerAdaptor;
        
        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getInfo()
     */
    public synchronized Map<String, Object> getInfo() throws GATInvocationException {
        HashMap<String, Object> m = new HashMap<String, Object>();

        getState();
        m.put("state", state);
        m.put("", null);
        
        return m;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getExitStatus()
     */
    public synchronized int getExitStatus() throws GATInvocationException {
        if (state != STOPPED)
            throw new GATInvocationException("not in RUNNING state");
        String code = (String) jobInstance.getProperties().get("urn:gridsam:exitcode");
        if (code == null) {
            throw new GATInvocationException("No exit status code from gridsam available");
        }
        try {
            exitVal = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            logger.error("exit code from gridsam not int, code=" + code);
            throw new GATInvocationException("No exit status code from gridsam available", e);
        }
        return exitVal;
    }
    
    @Override
    public synchronized int getState() {
        
        // update the manager state of the job
        try {
            jobInstance = adaptor.getJobManager().findJobInstance(jobID);
        } catch (JobManagerException e) {
            logger.error("caught exception", e);
            // TODO
            throw new RuntimeException(e);
        } catch (UnknownJobException e) {
            logger.error("caught exception", e);
            // TODO
            throw new RuntimeException(e);
        }
        
        JobStage stage = jobInstance.getLastKnownStage();
        JobState jobState = stage.getState();

        if (logger.isDebugEnabled()) {
            logger.debug("jobState=" + jobState.toString());
            StringBuilder props = new StringBuilder();
            Map properties = jobInstance.getProperties();
            Iterator iterator = properties.keySet().iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                props.append("\n    ").append(next).append("=").append(properties.get(next));
            }
            logger.debug("properties=" + props.toString());
        }
        
        // TODO [wojciech] - verify that those states are correct and that they should be here
        if (jobState == JobState.ACTIVE) {
            state = RUNNING;
        } else if (jobState == JobState.DONE) {
            state = STOPPED;
        } else if (jobState == JobState.EXECUTED) {
            state = STOPPED;
        } else if (jobState == JobState.FAILED) {
            state = SUBMISSION_ERROR;
        } else if (jobState == JobState.PENDING) {
            state = RUNNING;
        } else if (jobState == JobState.STAGED_IN || jobState == JobState.STAGING_IN) {
            state = PRE_STAGING;
        } else if (jobState == JobState.STAGED_OUT || jobState == JobState.STAGING_OUT) {
            state = POST_STAGING;
        } else if (jobState == JobState.TERMINATED) {
            state = STOPPED;
        } else if (jobState == JobState.UNDEFINED) {
            state = UNKNOWN;
        } else {
            logger.warn("unknown job state: " + jobState.toString());
            state = UNKNOWN;
        }
        
        return state;
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getJobID()
     */
    public String getJobID() throws GATInvocationException {
        return jobID;
    }

    void finished(int exitValue) {
        MetricValue v = null;

        synchronized (this) {
            exitVal = exitValue;
            state = POST_STAGING;
            v = new MetricValue(this, getStateString(state), statusMetric, System.currentTimeMillis());
            if (logger.isDebugEnabled()) {
                logger.debug("default job callback: firing event: " + v);
            }
        }
        GATEngine.fireMetric(this, v);

        sandbox.retrieveAndCleanup(this);

        synchronized (this) {
            state = STOPPED;
            v = new MetricValue(this, getStateString(state), statusMetric, System.currentTimeMillis());

            if (logger.isDebugEnabled()) {
                logger.debug("default job callback: firing event: " + v);
            }
        }
        GATEngine.fireMetric(this, v);
        finished();

        if (GATEngine.TIMING) {
            System.err.println("TIMING: job " + jobID + ":" + " preStage: " + sandbox.getPreStageTime() + " run: " + runTime + " postStage: "
                    + sandbox.getPostStageTime() + " wipe: " + sandbox.getWipeTime() + " delete: " + sandbox.getDeleteTime() + " total: "
                    + (System.currentTimeMillis() - startTime));
        }
    }

    public void stop() throws GATInvocationException {
        MetricValue v = null;

        synchronized (this) {
            if (p != null)
                p.destroy();
            state = POST_STAGING;
            v = new MetricValue(this, getStateString(state), statusMetric, System.currentTimeMillis());
            if (logger.isDebugEnabled()) {
                logger.debug("default job callback: firing event: " + v);
            }
        }
        GATEngine.fireMetric(this, v);

        sandbox.retrieveAndCleanup(this);

        synchronized (this) {
            state = STOPPED;
            v = new MetricValue(this, getStateString(state), statusMetric, System.currentTimeMillis());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("default job callback: firing event: " + v);
        }

        GATEngine.fireMetric(this, v);
        finished();
    }
}
