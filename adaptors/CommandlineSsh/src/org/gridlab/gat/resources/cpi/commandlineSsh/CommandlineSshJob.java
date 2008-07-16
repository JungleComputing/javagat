/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.commandlineSsh;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
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

    JobDescription description;

    int jobID;

    Process p;

    int exitStatus = 0;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    CommandlineSshJob(GATContext gatContext, JobDescription description,
            Sandbox sandbox) {
        super(gatContext, description, sandbox);

        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
    }

    protected void setProcess(Process p) {
        this.p = p;
    }

    protected synchronized void setState(JobState state) {
        this.state = state;
        MetricEvent v = new MetricEvent(this, state, statusMetric, System
                .currentTimeMillis());
        GATEngine.fireMetric(this, v);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getInfo()
     */
    public synchronized Map<String, Object> getInfo() {
        HashMap<String, Object> m = new HashMap<String, Object>();

        m.put("state", state.toString());
        if (state != JobState.RUNNING) {
            m.put("hostname", null);
        } else {
            m.put("hostname", "not available");
        }
        if (state == JobState.INITIAL || state == JobState.UNKNOWN) {
            m.put("submissiontime", null);
        } else {
            m.put("id", jobID);
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
        m.put("resourcebroker", "CommandlineSsh");
        try {
            m.put("exitvalue", "" + getExitStatus());
        } catch (GATInvocationException e) {
            // ignore
        }
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
            throw new GATInvocationException("not in RUNNING state");
        return exitStatus;
    }

    public synchronized void stop() throws GATInvocationException {
        setState(JobState.POST_STAGING);
        sandbox.retrieveAndCleanup(this);
        p.destroy();
        setState(JobState.STOPPED);
        finished();
    }

    public OutputStream getStdin() throws GATInvocationException {
        if (jobDescription.getSoftwareDescription().streamingStdinEnabled()) {
            return p.getOutputStream();
        } else {
            throw new GATInvocationException("stdin streaming is not enabled!");
        }
    }

    public InputStream getStdout() throws GATInvocationException {
        if (jobDescription.getSoftwareDescription().streamingStdoutEnabled()) {
            return p.getInputStream();
        } else {
            throw new GATInvocationException("stdout streaming is not enabled!");
        }
    }

    public InputStream getStderr() throws GATInvocationException {
        if (jobDescription.getSoftwareDescription().streamingStderrEnabled()) {
            return p.getErrorStream();
        } else {
            throw new GATInvocationException("stderr streaming is not enabled!");
        }
    }

    protected void monitorState() {
        new StateMonitor();
    }

    class StateMonitor extends Thread {

        StateMonitor() {
            setName("command line ssh state monitor: "
                    + jobDescription.getSoftwareDescription().getExecutable());
            setDaemon(true);
            start();
        }

        public void run() {
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("p.waitFor is interrupted (commandline ssh)");
                }
            }
            try {
                exitStatus = p.exitValue();
            } catch (NullPointerException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("unable to retrieve exit status");
                }
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

    // public void startOutputWaiter(StreamForwarder outForwarder,
    // StreamForwarder errForwarder) {
    // new OutputWaiter(outForwarder, errForwarder);
    // }
    //
    // class OutputWaiter extends Thread {
    //
    // StreamForwarder outForwarder, errForwarder;
    //
    // OutputWaiter(StreamForwarder outForwarder, StreamForwarder errForwarder)
    // {
    // setName("LocalJob OutputForwarderWaiter");
    // setDaemon(true);
    // this.outForwarder = outForwarder;
    // this.errForwarder = errForwarder;
    // start();
    // }
    //
    // public void run() {
    // if (outForwarder != null) {
    // outForwarder.waitUntilFinished();
    // }
    // if (errForwarder != null) {
    // errForwarder.waitUntilFinished();
    // }
    // try {
    // p.waitFor();
    // } catch (InterruptedException e) {
    // // ignore
    // }
    // try {
    // CommandlineSshJob.this.stop();
    // } catch (GATInvocationException e) {
    // if (logger.isDebugEnabled()) {
    // logger.debug("unable to stop job: " + e);
    // }
    // }
    // }
    // }
}
