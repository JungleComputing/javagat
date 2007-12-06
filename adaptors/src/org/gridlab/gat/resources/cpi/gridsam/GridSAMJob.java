package org.gridlab.gat.resources.cpi.gridsam;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.PrinterURI;

import org.apache.commons.collections.ListUtils;
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

public class GridSAMJob extends JobCpi {
    
    private class PollingThread extends Thread {
        private Logger logger = Logger.getLogger(PollingThread.class);

        private GridSAMJob parent;
        
        // how many events have we fired already ?
        private int firedEventCount = 0;
        
        public PollingThread(GridSAMJob parent) {
            this.parent = parent;
        }
        
        private void fireEvent(int state) {
            MetricValue v = new MetricValue(this, getStateString(state), statusMetric,
                    System.currentTimeMillis());
            GATEngine.fireMetric(parent, v);
            if (logger.isDebugEnabled()) {
                logger.debug("firing event, event=" + v.toString());
            }
        }
        
        /**
         * sets new job state and fires an event if the state is different than the previous one
         * 
         * @param state
         */
        private void setState(int state) {
            synchronized (this.parent) {
                parent.state = state;
            }
        }
        
        public void run() {
            while (true) {
                
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
                
                if (logger.isDebugEnabled()) {
                    StringBuilder props = new StringBuilder();
                    Map properties = jobInstance.getProperties();
                    Iterator iterator = properties.keySet().iterator();
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        props.append("\n    ").append(next).append("=").append(properties.get(next));
                    }
                    logger.debug("job properties (from GridSAM)=" + props.toString());
                }
                
                List stages = jobInstance.getJobStages();
                if (stages.size() > firedEventCount) {
                    // there are some event that were not fired (and we don't know about)
                    // lets get that knowledge :)
                    
                    // first we set the new current state - the sooner the better
                    int currentState = translateState(jobInstance.getLastKnownStage().getState());
                    setState(currentState);
                    
                    

                    /* and now we fire the event for each of the states we have found
                    * we can get ith element because:
                        * 1. this list is not big
                        * 2. GridSAM actually uses ArrayList for this
                        */ 
                    for (int i = firedEventCount; i < stages.size(); i++) {
                        fireEvent(translateState(((JobStage) stages.get(i)).getState()));
                    }
                    
                    // finally we set that we have fired every event
                    firedEventCount = stages.size();
                    
                    if (currentState == STOPPED || currentState == SUBMISSION_ERROR) {
                        break;
                    }
                    
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("sleeping interrupted");
                    }
                }
     
                
            }
            if (logger.isDebugEnabled()) {
                logger.debug("polling thread exiting, job's finished");
            }
        }
    }

    private Logger logger = Logger.getLogger(GridSAMJob.class);

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

    private PollingThread pollingThread;

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
        
        pollingThread = new PollingThread(this);
        pollingThread.setDaemon(true);
        pollingThread.start();
    }
    
    private int translateState(JobState jobState) {
        if (jobState == JobState.ACTIVE) {
            return RUNNING;
        } else if (jobState == JobState.DONE) {
            return STOPPED;
        } else if (jobState == JobState.EXECUTED) {
            return STOPPED;
        } else if (jobState == JobState.FAILED) {
            return SUBMISSION_ERROR;
        } else if (jobState == JobState.PENDING) {
            return RUNNING;
        } else if (jobState == JobState.STAGED_IN || jobState == JobState.STAGING_IN) {
            return PRE_STAGING;
        } else if (jobState == JobState.STAGED_OUT || jobState == JobState.STAGING_OUT) {
            return POST_STAGING;
        } else if (jobState == JobState.TERMINATED) {
            return STOPPED;
        } else if (jobState == JobState.UNDEFINED) {
            return UNKNOWN;
        } else {
            logger.warn("unknown job state: " + jobState.toString());
            return UNKNOWN;
        }
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
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getJobID()
     */
    public String getJobID() throws GATInvocationException {
        return jobID;
    }

    public void stop() throws GATInvocationException {
        MetricValue v = null;

        synchronized (this) {
            state = POST_STAGING;
            v = new MetricValue(this, getStateString(state), statusMetric, System.currentTimeMillis());
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("default job callback: firing event: " + v);
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
