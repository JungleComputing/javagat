/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.ssh;

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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;

/**
 * @author rob
 */
public class SshJob extends Job {
    class ProcessWaiter extends Thread {

        ProcessWaiter() {
            start();
        }

        public void run() {
            try {
                while (true) {
                    if (channel.isEOF()) {
                        finished(channel.getExitStatus());
                        if (channel != null) channel.disconnect();
                        if (session != null) session.disconnect();
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("SshJob: while waiting for EOF of channel,"
                    + " an error occurred: " + e);
            }
        }
    }

    SshBrokerAdaptor broker;

    GATInvocationException postStageException = null;

    JobDescription description;

    int jobID;

    Channel channel;

    Session session;

    String host;

    static int globalJobID = 0;

    int exitVal = 0;

    boolean exited = false;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    static synchronized int allocJobID() {
        return globalJobID++;
    }

    SshJob(SshBrokerAdaptor broker, JobDescription description,
        Session session, Channel channel, String host)
        throws GATInvocationException {
        this.broker = broker;
        this.description = description;
        jobID = allocJobID();
        state = RUNNING;
        this.session = session;
        this.channel = channel;
        this.host = host;

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
    public synchronized Map getInfo() throws GATInvocationException,
        IOException {
        HashMap m = new HashMap();
        // update state
        getState();

        m.put("state", getStateString(state));
        m.put("exitValue", "" + exitVal);
        m.put("hostname", host);

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
    public synchronized int getState() throws GATInvocationException,
        IOException {
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
        if (channel != null) {
            try {
                channel.sendSignal("TERM");
                Thread.sleep(1000); // give the process some time to cleanup
            } catch (Exception e) {
                if(GATEngine.VERBOSE) {
                    System.err.println("exception while sending KILL signal: " + e);
                }
                
                // ignore it, close the channel
            }
            channel.disconnect();
        }
        
        if (session != null) {
            session.disconnect();
        }
        
        synchronized (this) {
            state = STOPPED;
        }
    }
}
