/*
 * Created on Oct 18, 2006
 */
package org.gridlab.gat.resources.cpi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;

public abstract class JobCpi implements Job {

    protected static Logger logger = Logger.getLogger(JobCpi.class);

    protected JobDescription jobDescription;

    protected Sandbox sandbox;

    protected GATInvocationException postStageException = null;

    protected GATInvocationException deleteException = null;

    protected GATInvocationException wipeException = null;

    protected GATInvocationException removeSandboxException = null;

    protected static int globalJobID = 0;

    protected GATContext gatContext;

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

    protected JobCpi() {

    }

    protected JobCpi(GATContext gatContext, JobDescription jobDescription,
            Sandbox sandbox) {
        this.gatContext = gatContext;
        this.jobDescription = jobDescription;
        this.sandbox = sandbox;
        // better make this an attribute!
        if (jobDescription.getSoftwareDescription().getBooleanAttribute(
                "job.stop.on.exit", true)) {
            synchronized (JobCpi.class) {
                if (shutdownInProgress) {
                    throw new Error(
                            "jobCpi: cannot create new jobs when shutdown is in progress");
                }
                jobList.add(this);
            }
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
        return "gat job, id is " + jobID;
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
        throw new Error(
                "marshalling of this object is not supported by this adaptor");
    }

    protected void finished() {
        synchronized (JobCpi.class) {
            if (jobList.contains(this)) {
                jobList.remove(this);
            }
        }
    }

    static class JobShutdownHook extends Thread {
        public void run() {
            synchronized (JobCpi.class) {
                shutdownInProgress = true;
            }
            while (true) {
                Job j;
                synchronized (JobCpi.class) {
                    if (jobList.size() == 0)
                        break;
                    j = (Job) jobList.remove(0);
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

    public MetricEvent getMeasurement(Metric metric)
            throws GATInvocationException {
        if (metric.getDefinition().getMeasurementType() == MetricDefinition.DISCRETE) {
            return GATEngine.getMeasurement(this, metric);
        }

        throw new UnsupportedOperationException("Not implemented");
    }

    public final List<MetricDefinition> getMetricDefinitions()
            throws GATInvocationException {
        return GATEngine.getMetricDefinitions(this);
    }

    public final MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        return GATEngine.getMetricDefinitionByName(this, name);
    }

    public final void addMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        GATEngine.addMetricListener(this, metricListener, metric);
    }

    public final void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        GATEngine.removeMetricListener(this, metricListener, metric);
    }

}
