/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.localQ;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.CommandRunner;
import org.gridlab.gat.engine.util.InputForwarder;
import org.gridlab.gat.engine.util.OutputForwarder;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * @author ndrost
 */
@SuppressWarnings("serial")
public class LocalQJob extends JobCpi implements Runnable,
        Comparable<LocalQJob> {

    private final static Logger logger = Logger.getLogger(LocalQJob.class);

    private final MetricDefinition statusMetricDefinition;

    private final Metric statusMetric;

    private final int priority;

    private final int jobID;

    private int exitVal = 0;

    private long runTime;

    private boolean stopped = false;

    private Process p = null;

    LocalQJob(GATContext gatContext, LocalQResourceBrokerAdaptor broker,
            JobDescription description, Sandbox sandbox) {
        super(gatContext, description, sandbox);

        this.jobID = allocJobID();

        priority = description.getSoftwareDescription().getIntAttribute(
                "localq.job.priority", 0);

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        setSubmissionTime();
        // cheat and start job right now :)
        // Thread thread = new Thread(this);
        // thread.start();
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
        m.put("exitvalue", "" + exitVal);
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

    public void stop() throws GATInvocationException {
        synchronized (this) {
            if (p != null) {
                p.destroy();
            }
            stopped = true;
        }
    }

    protected void setState(int state) {
        MetricEvent metricEvent = null;
        synchronized (this) {
            this.state = state;
            metricEvent = new MetricEvent(this, getStateString(state),
                    statusMetric, System.currentTimeMillis());

            if (logger.isDebugEnabled()) {
                logger.debug("default job callback: firing event: "
                        + metricEvent);
            }
        }
        GATEngine.fireMetric(this, metricEvent);
    }

    // start running this job
    public void run() {
        logger.debug("running job with priority: " + priority);
        SoftwareDescription description = jobDescription
                .getSoftwareDescription();

        ProcessBuilder processBuilder = new ProcessBuilder();

        if (jobDescription.getSoftwareDescription().getEnvironment() != null) {

            processBuilder.environment().clear();
            for (Map.Entry<String, Object> entry : jobDescription
                    .getSoftwareDescription().getEnvironment().entrySet()) {
                processBuilder.environment().put(entry.getKey(),
                        (String) entry.getValue());
            }
        }

        String exe;
        if (sandbox.getResolvedExecutable() != null) {
            exe = sandbox.getResolvedExecutable().getPath();
        } else {
            exe = description.getExecutable();
        }

        // try to set the executable bit, it might be lost
        try {
            new CommandRunner("/bin/chmod +x " + exe);
        } catch (Throwable t) {
            // ignore
        }
        try {
            new CommandRunner("/usr/bin/chmod +x " + exe);
        } catch (Throwable t) {
            // ignore
        }

        processBuilder.command().add(exe);
        if (description.getArguments() != null) {
            for (String argument : description.getArguments()) {
                processBuilder.command().add(argument);
            }
        }

        java.io.File workingDirectory = new java.io.File(System
                .getProperty("user.home")
                + java.io.File.separator + sandbox.getSandbox());
        processBuilder.directory(workingDirectory);

        if (logger.isDebugEnabled()) {
            logger.debug("running command: ");

            for (String element : processBuilder.command()) {
                logger.debug("    " + element);
            }
        }

        // long startRun = System.currentTimeMillis();

        Process p;
        synchronized (this) {
            if (stopped) {
                // we were already stopped...
                return;
            }

            try {
                p = processBuilder.start();
                this.p = p;
                setStartTime();
            } catch (IOException e) {
                logger.error(e);
                setState(SUBMISSION_ERROR);

                // FIXME: also cleanup sandbox?

                return;
            }
        }

        org.gridlab.gat.io.File stdin = sandbox.getResolvedStdin();
        org.gridlab.gat.io.File stdout = sandbox.getResolvedStdout();
        org.gridlab.gat.io.File stderr = sandbox.getResolvedStderr();

        if (stdin == null) {
            // close stdin.
            try {
                p.getOutputStream().close();
            } catch (Throwable e) {
                // ignore
            }
        } else {
            try {
                java.io.FileInputStream fin = new java.io.FileInputStream(stdin
                        .getAbsolutePath());
                OutputStream out = p.getOutputStream();
                new InputForwarder(out, fin);
            } catch (Exception e) {
                logger.error(e);
                setState(SUBMISSION_ERROR);
                return;
            }
        }

        OutputForwarder outForwarder = null;

        // we must always read the output and error streams to avoid deadlocks
        if (stdout == null) {
            new OutputForwarder(p.getInputStream(), false); // throw away output
        } else {
            try {
                java.io.FileOutputStream out = new java.io.FileOutputStream(
                        stdout.getAbsolutePath(), true);
                outForwarder = new OutputForwarder(p.getInputStream(), out);
            } catch (Exception e) {
                logger.error(e);
                setState(SUBMISSION_ERROR);
                return;
            }
        }

        OutputForwarder errForwarder = null;

        // we must always read the output and error streams to avoid deadlocks
        if (stderr == null) {
            new OutputForwarder(p.getErrorStream(), false); // throw away output
        } else {
            try {
                java.io.FileOutputStream out = new java.io.FileOutputStream(
                        stderr.getAbsolutePath());
                errForwarder = new OutputForwarder(p.getErrorStream(), out);
            } catch (Exception e) {
                setState(SUBMISSION_ERROR);
                return;
            }
        }

        setState(RUNNING);

        int exitValue = 0;
        try {
            exitValue = p.waitFor();
            setStopTime();
        } catch (InterruptedException e) {
            // CANNOT HAPPEN
        }

        synchronized (this) {
            this.exitVal = exitValue;
            // FIXME: huh?
            this.runTime = System.currentTimeMillis() - runTime;
        }

        // Wait for the output forwarders to finish!
        // You may lose output if you don't -- Jason
        if (outForwarder != null) {
            outForwarder.waitUntilFinished();
        }

        if (errForwarder != null) {
            errForwarder.waitUntilFinished();
        }

        setState(POST_STAGING);

        sandbox.retrieveAndCleanup(this);

        finished();

        setState(STOPPED);

        if (logger.isDebugEnabled()) {
            logger.debug("TIMING: job " + jobID + ":" + " preStage: "
                    + sandbox.getPreStageTime() + " run: "
                    + (stoptime - starttime) + " postStage: "
                    + sandbox.getPostStageTime() + " wipe: "
                    + sandbox.getWipeTime() + " delete: "
                    + sandbox.getDeleteTime() + " total: "
                    + (System.currentTimeMillis() - starttime));
        }

    }

    // sort decending on priority, then ascending on jobID
    public int compareTo(LocalQJob other) {
        if (priority != other.priority) {
            return other.priority - priority;
        }
        return jobID - other.jobID;
    }

}
