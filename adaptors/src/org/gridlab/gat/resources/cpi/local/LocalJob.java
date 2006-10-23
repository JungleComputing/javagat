/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.local;

import ibis.util.IPUtils;

import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.util.OutputForwarder;

/**
 * @author rob
 */
public class LocalJob extends JobCpi {
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

    int jobID;

    Process p;

    int exitVal = 0;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    OutputForwarder out;

    OutputForwarder err;
    
    LocalJob(LocalResourceBrokerAdaptor broker, JobDescription description,
            Process p, String host, Sandbox sandbox, OutputForwarder out, OutputForwarder err) {
        super(description, sandbox);
        this.broker = broker;
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
    public synchronized Map getInfo() throws GATInvocationException {
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
     * @see org.gridlab.gat.resources.Job#getJobID()
     */
    public String getJobID() throws GATInvocationException {
        return "" + jobID;
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

        sandbox.retrieveAndCleanup(this);
        
        synchronized (this) {
            state = STOPPED;
            v = new MetricValue(this, getStateString(state), statusMetric, System
                .currentTimeMillis());

            if (GATEngine.DEBUG) {
                System.err.println("default job callback: firing event: " + v);
            }
        }
        GATEngine.fireMetric(this, v);
    }

    public void stop() throws GATInvocationException {
        MetricValue v;
        
        synchronized (this) {
            if (p!= null) p.destroy();
            state = STOPPED;
            v = new MetricValue(this, getStateString(state), statusMetric, System
                .currentTimeMillis());
        }

        if (GATEngine.VERBOSE) {
            System.err.println("local job stop: delete/wipe starting");
        }

        sandbox.retrieveAndCleanup(this);
        
        if (GATEngine.DEBUG) {
            System.err.println("default job callback: firing event: " + v);
        }

        GATEngine.fireMetric(this, v);
    }
}
