package org.gridlab.gat.resources.cpi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.WrapperJob;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.WrapperJobDescription.WrappedJobInfo;

/**
 * @author rkemp
 */

@SuppressWarnings("serial")
public class WrapperJobCpi implements WrapperJob, MetricListener {

    protected final int jobID = JobCpi.allocJobID();

    private Job wrapperJob;

    private Map<JobDescription, WrappedJobCpi> wrappedJobs = new HashMap<JobDescription, WrappedJobCpi>();

    public WrapperJobCpi(Job wrapperJob) {
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        MetricDefinition statusMetricDefinition = new MetricDefinition(
                "job.status", MetricDefinition.DISCRETE, "JobState", null,
                null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);

        this.wrapperJob = wrapperJob;

        WrapperJobDescription wrapperDescription = (WrapperJobDescription) wrapperJob
                .getJobDescription();
        for (WrappedJobInfo info : wrapperDescription.getJobInfos()) {
            wrappedJobs.put(info.getJobDescription(), new WrappedJobCpi(info));
        }
    }

    public int getJobID() {
        return jobID;
    }

    public Job getJob(JobDescription description) throws GATInvocationException {
        return wrappedJobs.get(description);
    }

    public boolean wrappedJobsFinishedPreStaging() {
        for (WrappedJobCpi job : wrappedJobs.values()) {
            JobState state = job.getState();
            if (state == JobState.PRE_STAGING || state == JobState.INITIAL) {
                return false;
            }
        }
        return true;
    }

    public void addMetricListener(MetricListener metricListener, Metric metric)
            throws GATInvocationException {
        GATEngine.addMetricListener(this, metricListener, metric);

    }

    /**
     * @deprecated
     */
    public void checkpoint() throws GATInvocationException {
        wrapperJob.checkpoint();
    }

    /**
     * @deprecated
     */
    public Job cloneJob(HardwareResource resource) {
        return wrapperJob.cloneJob(resource);
    }

    public int getExitStatus() throws GATInvocationException {
        return wrapperJob.getExitStatus();
    }

    public Map<String, Object> getInfo() throws GATInvocationException {
        return wrapperJob.getInfo();
    }

    public AbstractJobDescription getJobDescription() {
        return wrapperJob.getJobDescription();
    }

    public MetricEvent getMeasurement(Metric metric)
            throws GATInvocationException {
        if (metric.getDefinition().getMeasurementType() == MetricDefinition.DISCRETE) {
            return GATEngine.getMeasurement(this, metric);
        }

        throw new UnsupportedOperationException("Not implemented");
    }

    public final MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        return GATEngine.getMetricDefinitionByName(this, name);
    }

    public final List<MetricDefinition> getMetricDefinitions()
            throws GATInvocationException {
        return GATEngine.getMetricDefinitions(this);
    }

    public JobState getState() {
        return wrapperJob.getState();
    }

    public InputStream getStderr() throws GATInvocationException {
        return wrapperJob.getStderr();
    }

    public OutputStream getStdin() throws GATInvocationException {
        return wrapperJob.getStdin();
    }

    public InputStream getStdout() throws GATInvocationException {
        return wrapperJob.getStdout();
    }

    public void hold() throws GATInvocationException {
        wrapperJob.hold();
    }

    /**
     * @deprecated
     */
    public void migrate() throws GATInvocationException {
        wrapperJob.migrate();
    }

    /**
     * @deprecated
     */
    public void migrate(HardwareResourceDescription hardwareResourceDescription)
            throws GATInvocationException {
        wrapperJob.migrate(hardwareResourceDescription);
    }

    public final void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        GATEngine.removeMetricListener(this, metricListener, metric);
    }

    public void resume() throws GATInvocationException {
        wrapperJob.resume();
    }

    public void stop() throws GATInvocationException {
        wrapperJob.stop();
    }

    /**
     * @deprecated
     */
    public void unSchedule() throws GATInvocationException {
        wrapperJob.unSchedule();
    }

    public String marshal() {
        return wrapperJob.marshal();
    }

    public void processMetricEvent(MetricEvent event) {
        // forward the metrics from the wrapperjob
        GATEngine.fireMetric(this, event);
    }

}
