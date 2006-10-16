/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.local;

import ibis.util.IPUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.util.OutputForwarder;

/**
 * @author rob
 */
public class LocalJob extends Job {
    class ProcessWaiter extends Thread {
        ProcessWaiter() {
            start();
        }

        public void run() {
            try {
                int exitValue = p.waitFor();
                
                // Wait for the output forwarders to finish!
                // You may lose output if you don't -- Jason
                if (out != null) {
                        out.waitUntilFinished();
                }

                if (err != null) {
                        err.waitUntilFinished();
                }

                finished(exitValue);
            } catch (InterruptedException e) {
                // Cannot happen
            }
        }
    }

    LocalResourceBrokerAdaptor broker;

    GATInvocationException postStageException = null;
    GATInvocationException deleteException = null;
    GATInvocationException wipeException = null;

    JobDescription description;

    int jobID;

    Process p;

    int exitVal = 0;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    OutputForwarder out;

    OutputForwarder err;
    
    LocalJob(LocalResourceBrokerAdaptor broker, JobDescription description,
            Process p, OutputForwarder out, OutputForwarder err) {
        this.broker = broker;
        this.description = description;
        jobID = allocJobID();
        state = RUNNING;
        this.p = p;
        this.out = out;
        this.err = err;
        
        // Tell the engine that we provide job.status events
        HashMap returnDef = new HashMap();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
            MetricDefinition.DISCRETE, "String", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);

        new ProcessWaiter();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.Job#getInfo()
     */
    public synchronized Map getInfo() {
        HashMap m = new HashMap();

        // update state
        getState();

        m.put("state", getStateString(state));
        m.put("resManState", getStateString(state));
        m.put("resManName", "Local");
        m.put("exitValue", "" + exitVal);
        m.put("hostname", IPUtils.getLocalHostName());

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

    /* (non-Javadoc)
     * @see org.gridlab.gat.resources.Job#getExitStatus()
     */
    public synchronized int getExitStatus() throws GATInvocationException {
         if(state != STOPPED) throw new GATInvocationException("not in RUNNING state");
         return exitVal;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.Job#getJobDescription()
     */
    public JobDescription getJobDescription() {
        return description;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.Job#getJobID()
     */
    public String getJobID() throws GATInvocationException, IOException {
        return "" + jobID;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.Job#getState()
     */
    public synchronized int getState() {
        return state;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        // TODO Auto-generated method stub
        return null;
    }

    void finished(int exitValue) {
        MetricValue v = null;

        synchronized (this) {
            exitVal = exitValue;
            state = POST_STAGING;
            v = new MetricValue(this, getStateString(state), statusMetric, System
                .currentTimeMillis());
            if (GATEngine.DEBUG) {
                System.err.println("default job callback: firing event: " + v);
            }
        }
        GATEngine.fireMetric(this, v);

        GATInvocationException tmpExc = null;

        if (GATEngine.VERBOSE) {
            System.err.println("job: poststage starting");
        }
        try {
            broker.postStageFiles(description, "localhost");
        } catch (GATInvocationException e) {
            tmpExc = e;
        }

        if (GATEngine.VERBOSE) {
            System.err.println("job: delete/wipe starting");
        }
        try {
            broker.deleteFiles(description, broker.getHostname(description));
        } catch (GATInvocationException e) {
            deleteException = e;
        }

        try {
            broker.wipeFiles(description, broker.getHostname(description));
        } catch (GATInvocationException e) {
            wipeException = e;
        }


        
        
        synchronized (this) {
            postStageException = tmpExc;
            state = STOPPED;
            v = new MetricValue(this, getStateString(state), statusMetric, System
                .currentTimeMillis());

            if (GATEngine.DEBUG) {
                System.err.println("default job callback: firing event: " + v);
            }
        }
        GATEngine.fireMetric(this, v);
    }

    public void stop() throws GATInvocationException, IOException {
        MetricValue v;
        
        synchronized (this) {
            if (p!= null) p.destroy();
            state = STOPPED;
            v = new MetricValue(this, getStateString(state), statusMetric, System
                .currentTimeMillis());
        }

        if (GATEngine.VERBOSE) {
            System.err.println("globus job stop: delete/wipe starting");
        }

        try {
            broker.deleteFiles(description, broker.getHostname(description));
        } catch (GATInvocationException e) {
            deleteException = e;
        }

        try {
            broker.wipeFiles(description, broker.getHostname(description));
        } catch (GATInvocationException e) {
            wipeException = e;
        }

        
        if (GATEngine.DEBUG) {
            System.err.println("default job callback: firing event: " + v);
        }

        GATEngine.fireMetric(this, v);
    }
}
