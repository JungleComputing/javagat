/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.commandlineSsh;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.StreamForwarder;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * @author rob
 */
@SuppressWarnings("serial")
public class CommandlineSshJob extends JobCpi {

    protected static Logger logger = Logger.getLogger(CommandlineSshJob.class);

    CommandlineSshResourceBrokerAdaptor broker;

    JobDescription description;

    int jobID;

    Process p;

    int exitStatus = 0;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    CommandlineSshJob(GATContext gatContext, JobDescription description,
            Sandbox sandbox) {
        super(gatContext, description, sandbox);
        jobID = allocJobID();

        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
    }

    protected void setProcess(Process p) {
        this.p = p;
    }

    protected synchronized void setState(int state) {
        this.state = state;
        MetricEvent v = new MetricEvent(this, getStateString(state),
                statusMetric, System.currentTimeMillis());
        GATEngine.fireMetric(this, v);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getInfo()
     */
    public synchronized Map<String, Object> getInfo() {
        HashMap<String, Object> m = new HashMap<String, Object>();

        // update state
        getState();

        m.put("state", getStateString(state));
        m.put("resManState", getStateString(state));
        m.put("resManName", "CommandlineSsh");
        m.put("exitValue", "" + exitStatus);
        m.put("hostname", broker.getHostname());

        if (postStageException != null) {
            m.put("postStageError", postStageException);
        }

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
        return exitStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getJobID()
     */
    public String getJobID() {
        return "" + jobID;
    }

    public synchronized void stop() throws GATInvocationException {
        setState(POST_STAGING);
        sandbox.retrieveAndCleanup(this);
        p.destroy();
        try {
            exitStatus = p.exitValue();
        } catch (IllegalThreadStateException e) {
            // IGNORE
            exitStatus = 0;
        }
        setState(STOPPED);
        finished();
    }

    public void startOutputWaiter(StreamForwarder outForwarder,
            StreamForwarder errForwarder) {
        new OutputWaiter(outForwarder, errForwarder);
    }

    class OutputWaiter extends Thread {

        StreamForwarder outForwarder, errForwarder;

        OutputWaiter(StreamForwarder outForwarder, StreamForwarder errForwarder) {
            setName("LocalJob OutputForwarderWaiter");
            setDaemon(true);
            this.outForwarder = outForwarder;
            this.errForwarder = errForwarder;
            start();
        }

        public void run() {
            if (outForwarder != null) {
                outForwarder.waitUntilFinished();
            }
            if (errForwarder != null) {
                errForwarder.waitUntilFinished();
            }
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                // ignore
            }
            try {
                CommandlineSshJob.this.stop();
            } catch (GATInvocationException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("unable to stop job: " + e);
                }
            }
        }
    }
}
