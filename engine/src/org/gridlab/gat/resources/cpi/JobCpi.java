/*
 * Created on Oct 18, 2006
 */
package org.gridlab.gat.resources.cpi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;

public abstract class JobCpi extends MonitorableCpi implements Job {

    protected static Logger logger = LoggerFactory.getLogger(JobCpi.class);
    
    protected GATContext gatContext;

    protected JobDescription jobDescription;

    protected Sandbox sandbox;

    protected GATInvocationException postStageException = null;

    protected GATInvocationException deleteException = null;

    protected GATInvocationException wipeException = null;

    protected GATInvocationException removeSandboxException = null;

    protected static int globalJobID = 0;

    protected JobState state = JobState.INITIAL;

    protected long submissiontime;

    protected long starttime;

    protected long stoptime;

    protected static ArrayList<Job> jobList = new ArrayList<Job>();

    protected static boolean shutdownInProgress = false;

    protected final int jobID = allocJobID();

    static {
        Runtime.getRuntime().addShutdownHook(new JobShutdownHook());
    }

    protected static synchronized int allocJobID() {
        return globalJobID++;
    }
    
    protected JobCpi(GATContext gatContext) {
        this.gatContext = gatContext;
    }

    protected JobCpi(GATContext gatContext, JobDescription jobDescription,
            Sandbox sandbox) {
        this(gatContext);
        this.jobDescription = jobDescription;
        this.sandbox = sandbox;
        boolean stopOnExit = true;
        Preferences prefs = gatContext.getPreferences();

        if (prefs.containsKey("job.stop.on.exit")
                && prefs.get("job.stop.on.exit").equals("false")) {
            stopOnExit = false;
        }

        // better make this an attribute!
        // NOTE: jobDescription may be null, since this class is extended by CoScheduleJob, and 
        // (for reasons I don't understand) CoScheduleJobDescription does NOT extend JobDescription
        //
        // -- Jason
        //

        if (jobDescription != null
                && !jobDescription.getSoftwareDescription().getBooleanAttribute(
                "job.stop.on.exit", true)) {
            stopOnExit = false;
        }
        if (stopOnExit) {
            synchronized (JobCpi.class) {
                if (shutdownInProgress) {
                    throw new Error(
                            "jobCpi: cannot create new jobs when shutdown is in progress");
                }
                jobList.add(this);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Created job " + this);
        }
        
    }

    public final int getJobID() {
        return jobID;
    }

    /**
     * @deprecated
     */
    public final void unSchedule() throws GATInvocationException {
        stop();
    }

    public void stop() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Map<String, Object> getInfo() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @deprecated
     */
    public void checkpoint() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @deprecated
     */
    public void migrate() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @deprecated
     */
    public void migrate(HardwareResourceDescription hardwareResourceDescription)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @deprecated
     */
    public Job cloneJob(HardwareResource resource) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void hold() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void resume() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int getExitStatus() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String toString() {
        return "job " + getClass().getName() + ", id is " + jobID;
    }

    public InputStream getStdout() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public InputStream getStderr() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public OutputStream getStdin() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public final JobDescription getJobDescription() {
        return jobDescription;
    }

    public JobState getState() {
        return state;
    }

    public void setStartTime() {
        starttime = System.currentTimeMillis();
    }

    public void setSubmissionTime() {
        submissiontime = System.currentTimeMillis();
    }

    public void setStopTime() {
        stoptime = System.currentTimeMillis();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        throw new UnsupportedOperationException(
                "marshalling of this object is not supported by this adaptor");
    }

    protected void finished() {
        synchronized (JobCpi.class) {
            if (jobList.contains(this)) {
                jobList.remove(this);
                if (logger.isDebugEnabled()) {
                    logger.debug("Finished job " + this);
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("Finished non-existent job? " + this);
            }
        }
    }

    static class JobShutdownHook extends Thread {
        public void run() {
            synchronized (JobCpi.class) {
                if (shutdownInProgress) {
                    return;
                }
                shutdownInProgress = true;
            }
            while (true) {
                Job j;
                synchronized (JobCpi.class) {
                    if (jobList.size() == 0)
                        break;
                    j = jobList.remove(0);
                }
                if (logger.isInfoEnabled()) {
                    logger.info("stopping job: " + j);
                }
                try {
                    j.stop();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
