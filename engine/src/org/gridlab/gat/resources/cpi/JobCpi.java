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

    protected int state = INITIAL;

    protected long submissiontime;

    protected long starttime;

    protected long stoptime;

    protected static ArrayList<Job> jobList = new ArrayList<Job>();

    protected static boolean shutdownInProgress = false;

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
                "kill.on.exit", true)) {
            synchronized (JobCpi.class) {
                if (shutdownInProgress) {
                    throw new Error(
                            "jobCpi: cannot create new jobs when shutdown is in progress");
                }
                jobList.add(this);
            }
        }
    }

    public String getStateString(int state) {
        switch (state) {
        case INITIAL:
            return INITIAL_STRING;
        case SCHEDULED:
            return SCHEDULED_STRING;
        case RUNNING:
            return RUNNING_STRING;
        case STOPPED:
            return STOPPED_STRING;
        case SUBMISSION_ERROR:
            return SUBMISSION_ERROR_STRING;
        case ON_HOLD:
            return ON_HOLD_STRING;
        case PRE_STAGING:
            return PRE_STAGING_STRING;
        case POST_STAGING:
            return POST_STAGING_STRING;
        case UNKNOWN:
            return UNKNOWN_STRING;
        default:
            throw new RuntimeException("unknown job state in getStateString");
        }
    }

    public final void unSchedule() throws GATInvocationException {
        stop();
    }

    public void stop() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Map<String, Object> getInfo() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getJobID() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void checkpoint() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void migrate() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void migrate(HardwareResourceDescription hardwareResourceDescription)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

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
        String res = "gat job";

        String id = null;
        try {
            id = getJobID();
        } catch (Exception e) {
            // ignore
        }
        if (id != null)
            res += ", id is " + id;
        else {
            res += ", " + "not initialized";
        }

        return res;
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

    public int getState() {
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
