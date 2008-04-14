/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.local;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.OutputForwarder;
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
public class LocalJob extends JobCpi {

    protected static Logger logger = Logger.getLogger(LocalJob.class);

    private int jobID;

    private Process p;

    private int exitStatus = 0;

    private MetricDefinition statusMetricDefinition;

    private Metric statusMetric;

    protected LocalJob(GATContext gatContext, JobDescription description,
            Sandbox sandbox) {
        super(gatContext, description, sandbox);
        jobID = allocJobID();

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
    }

    protected void setProcess(Process p) {
        this.p = p;
        Field f = null;
        try {
            f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            jobID = Integer.parseInt(f.get(p).toString()); // toString
            // ignore exceptions // necessary?
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
    }

    protected void setState(int state) {
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
    public synchronized Map<String, Object> getInfo()
            throws GATInvocationException {
        HashMap<String, Object> m = new HashMap<String, Object>();

        // update state
        getState();

        m.put("state", getStateString(state));
        if (state != RUNNING) {
            m.put("hostname", null);
        } else {
            m.put("hostname", GATEngine.getLocalHostName());
        }
        if (state == INITIAL || state == UNKNOWN) {
            m.put("submissiontime", null);
        } else {
            m.put("submissiontime", submissiontime);
        }
        if (state == INITIAL || state == UNKNOWN || state == SCHEDULED) {
            m.put("starttime", null);
        } else {
            m.put("starttime", starttime);
        }
        if (state != STOPPED) {
            m.put("stoptime", null);
        } else {
            m.put("stoptime", stoptime);
        }
        m.put("poststage.exception", postStageException);
        m.put("resourcebroker", "Local");
        m.put("exitvalue", "" + exitStatus);
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
        if (state != STOPPED) {
            throw new GATInvocationException("not in RUNNING state");
        }
        return exitStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getJobID()
     */
    public String getJobID() throws GATInvocationException {
        return "" + jobID;
    }

    public synchronized void stop() throws GATInvocationException {
        setState(POST_STAGING);
        sandbox.retrieveAndCleanup(this);
        // TODO: check exit value for proper value
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

    public void startOutputWaiter(OutputForwarder outForwarder,
            OutputForwarder errForwarder) {
        new OutputWaiter(outForwarder, errForwarder);
    }

    class OutputWaiter extends Thread {

        OutputForwarder outForwarder, errForwarder;

        OutputWaiter(OutputForwarder outForwarder, OutputForwarder errForwarder) {
            setName("LocalJob OutputForwarderWaiter");
            setDaemon(true);
            this.outForwarder = outForwarder;
            this.errForwarder = errForwarder;
            start();
        }

        public void run() {
            outForwarder.waitUntilFinished();
            errForwarder.waitUntilFinished();
            try {
                LocalJob.this.stop();
            } catch (GATInvocationException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("unable to stop job: " + e);
                }
            }
        }
    }
}
