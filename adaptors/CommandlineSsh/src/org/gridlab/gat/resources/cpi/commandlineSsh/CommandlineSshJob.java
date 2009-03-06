/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.commandlineSsh;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.SerializedJob;

/**
 * @author rob
 */
@SuppressWarnings("serial")
public class CommandlineSshJob extends JobCpi {

    protected static Logger logger = LoggerFactory.getLogger(CommandlineSshJob.class);

    JobDescription description;

    Process p;

    private String processID = null;

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
        registerMetric("getJobStatus", statusMetricDefinition);
    }

    /**
     * Constuctor for unmarshalled jobs.
     */
    CommandlineSshJob(GATContext gatContext, SerializedJob sj) {
        super(gatContext, sj.getJobDescription(), sj.getSandbox());


        this.processID = sj.getJobId().toString();
        this.starttime = sj.getStarttime();
        this.stoptime = sj.getStoptime();
        this.submissiontime = sj.getSubmissiontime();

        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        registerMetric("getJobStatus", statusMetricDefinition);
    }


    protected void setProcess(Process p) {
        this.p = p;
        Field f = null;
        try {
            f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            processID = f.get(p).toString(); // toString
            synchronized(this) {
                // For if a marshaler is waiting.
                notifyAll();
            }
            // ignore exceptions // necessary?
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
    }

    protected synchronized void setState(JobState state) {
        this.state = state;
        MetricEvent v = new MetricEvent(this, state, statusMetric, System
                .currentTimeMillis());
        fireMetric(v);
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
            // This is actually the job ID of the ssh process, not the ID of the
            // remote process.
            if (processID != null) {
                m.put("adaptor.job.id", processID);
            }
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
        stop(gatContext.getPreferences().containsKey("job.stop.poststage")
                && gatContext.getPreferences().get("job.stop.poststage")
                .equals("false"));
    }

    private synchronized void stop(boolean skipPostStage)
    throws GATInvocationException {
        if (state == JobState.POST_STAGING
                || state == JobState.STOPPED
                || state == JobState.SUBMISSION_ERROR) {
            return;
        }
        if (!skipPostStage) {
            setState(JobState.POST_STAGING);
            sandbox.retrieveAndCleanup(this);
        }
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

    /*
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        SerializedJob sj;
        synchronized (this) {

            // we have to wait until the job is in a safe state
            // we cannot marshal it if it is halfway during the poststage
            // process
            while (processID == null) {
                 try {
                    wait();
                } catch (Exception e) {
                    // ignore
                }
            }

            sj = new SerializedJob(jobDescription, sandbox, processID,
                    submissiontime, starttime, stoptime);
        }
        String res = GATEngine.defaultMarshal(sj);
        if (logger.isDebugEnabled()) {
            logger.debug("marshalled seralized job: " + res);
        }
        return res;
    }

    public static Advertisable unmarshal(GATContext context, String s)
            throws GATObjectCreationException {
        if (logger.isDebugEnabled()) {
            logger.debug("serialized job to unmarshal: " + s);
        }

        SerializedJob sj = (SerializedJob) GATEngine.defaultUnmarshal(
                SerializedJob.class, s);
        if (logger.isDebugEnabled()) {
            logger.debug("unmarshalled serialized job: " + sj);
        }

        // if this job was created within this JVM, just return a reference to
        // the job
        synchronized (JobCpi.class) {
            for (int i = 0; i < jobList.size(); i++) {
                JobCpi j = (JobCpi) jobList.get(i);
                if (j instanceof CommandlineSshJob) {
                    CommandlineSshJob gj = (CommandlineSshJob) j;
                    if (sj.getJobId().equals(gj.processID)) {
                        return gj;
                    }
                }
            }
        }
        return new CommandlineSshJob(context, sj);
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
