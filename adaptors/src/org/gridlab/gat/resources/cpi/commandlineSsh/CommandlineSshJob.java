/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.commandlineSsh;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.IPUtils;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.util.OutputForwarder;

/**
 * @author rob
 */
public class CommandlineSshJob extends Job {
    static int globalJobID = 0;

    CommandlineSshResourceBrokerAdaptor broker;

    GATInvocationException postStageException = null;

    JobDescription description;

    int jobID;

    Process p;

    int exitVal = 0;

    boolean exited = false;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    OutputForwarder out;

    OutputForwarder err;
    
    CommandlineSshJob(CommandlineSshResourceBrokerAdaptor broker, JobDescription description,
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

    static synchronized int allocJobID() {
        return globalJobID++;
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

        return m;
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
        GATInvocationException tmpExc = null;
        MetricValue v = null;


        try {
            String host = broker.getHostname(description);
            if (host == null) {
                host = "localhost";
            }

            broker.postStageFiles(description, host);
        } catch (GATInvocationException e) {
            tmpExc = e;
        }

        synchronized (this) {
            postStageException = tmpExc;
            exited = true;
            exitVal = exitValue;
            state = STOPPED;
            v = new MetricValue(this, getStateString(state), statusMetric, System
                .currentTimeMillis());
        }

        if (GATEngine.DEBUG) {
            System.err.println("default job callback: firing event: " + v);
        }

        GATEngine.fireMetric(this, v);
    }

    public void stop() throws GATInvocationException, IOException {
        MetricValue v;
        
        synchronized (this) {
            p.destroy();
            state = STOPPED;
            v = new MetricValue(this, getStateString(state), statusMetric, System
                .currentTimeMillis());
        }

        if (GATEngine.DEBUG) {
            System.err.println("commandline ssh job callback: firing event: " + v);
        }

        GATEngine.fireMetric(this, v);
    }

    public void unSchedule() throws GATInvocationException, IOException {
        throw new GATInvocationException("not in scheduled state");
    }

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
}
