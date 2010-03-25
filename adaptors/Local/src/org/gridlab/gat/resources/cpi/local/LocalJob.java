/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.StreamForwarder;
import org.gridlab.gat.io.File;
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

    protected static Logger logger = LoggerFactory.getLogger(LocalJob.class);

    private Process p;

    private int exitStatus = 0;

    private MetricDefinition statusMetricDefinition;

    private Metric statusMetric;

    private int processID;
    
    private final String triggerDirectory;
    
    private final String jobName;
    
    private StreamForwarder outputStreamFile;
    
    private StreamForwarder errorStreamFile;
    
    protected LocalJob(GATContext gatContext, JobDescription description,
            Sandbox sandbox) {
        super(gatContext, description, sandbox);
        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        registerMetric("getJobStatus", statusMetricDefinition);
        triggerDirectory = description.getSoftwareDescription().getStringAttribute(
                "triggerDirectory", null);
        jobName = description.getSoftwareDescription().getStringAttribute(
                "job.name", null);
    }
    
    void setErrorStream(StreamForwarder err) {
        errorStreamFile = err;
    }
        
    void setOutputStream(StreamForwarder out) {
        outputStreamFile = out;
    }
    
    // Wait for the creation of a special file (by the application).
    void waitForTrigger(JobState state) throws GATInvocationException {
        
        if (triggerDirectory == null) {
            return;
        }
        if (jobName == null) {
            return;
        }
        File file;
        try {
            file = GAT.createFile(gatContext, new URI(triggerDirectory + "/" + jobName + "."
                    + state.toString().substring(0,3)));
        } catch (Throwable e) {
            throw new GATInvocationException("Could not wait for trigger base", e);
        }

        long interval = 500;
        int  maxcount = 64;
        int count = 0;
        
        for (;;) {
            synchronized(this.getClass()) {
                // avoid simultaneous access
                if (file.exists()) {
                    break;
                }
            }    
            try {
                Thread.sleep(interval);
            } catch(InterruptedException e) {
                // ignored
            }
            count++;
            if (count == maxcount) {
                // back-off a bit.
                if (interval < 8000) {
                    maxcount += maxcount;
                    interval += interval;
                }
                count = 0;
            }
        }
        synchronized(this.getClass()) {
            file.delete();
        }
    }

    protected void setProcess(Process p) {
        this.p = p;
        Field f = null;
        try {
            f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            processID = Integer.parseInt(f.get(p).toString()); // toString
            // ignore exceptions // necessary?
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
    }

    protected void setState(JobState state) {
        if(state == this.state) {
            //state already set to this value
            return;
        }
        
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
    public synchronized Map<String, Object> getInfo()
            throws GATInvocationException {
        HashMap<String, Object> m = new HashMap<String, Object>();

        // update state
        getState();

        m.put("adaptor.job.id", processID);
        m.put("state", state.toString());
        if (state != JobState.RUNNING) {
            m.put("hostname", null);
        } else {
            m.put("hostname", GATEngine.getLocalHostName());
        }
        if (state == JobState.INITIAL || state == JobState.UNKNOWN) {
            m.put("submissiontime", null);
        } else {
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
        if (state != JobState.STOPPED) {
            throw new GATInvocationException("not in RUNNING state");
        }
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
        
        try {
            p.getOutputStream().close();
        } catch (IOException e) {
            // ignore
        }
        
        p.destroy();
        
        if (outputStreamFile != null) {
            outputStreamFile.waitUntilFinished();
            try {
                outputStreamFile.close();
            } catch(Throwable e) {
                // ignored
            }
        }
        if (errorStreamFile != null) {
            errorStreamFile.waitUntilFinished();
            try {
                errorStreamFile.close();
            } catch(Throwable e) {
                // ignored
            }
        }
        
        if (!skipPostStage) {
            setState(JobState.POST_STAGING);
            waitForTrigger(JobState.POST_STAGING);
            sandbox.retrieveAndCleanup(this);
        }

        setStopTime();
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
            setName("local state monitor: "
                    + jobDescription.getSoftwareDescription().getExecutable());
            setDaemon(true);
            start();
        }

        public void run() {
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("p.waitFor is interrupted (local)");
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
                LocalJob.this.stop(false);
            } catch (GATInvocationException e) {
                e.printStackTrace();
                if (logger.isDebugEnabled()) {
                    logger.debug("unable to stop job: " + e);
                }
            }
        }
    }

}
