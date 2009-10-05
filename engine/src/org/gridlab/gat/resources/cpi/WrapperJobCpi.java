package org.gridlab.gat.resources.cpi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;
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
public class WrapperJobCpi extends MonitorableCpi implements WrapperJob, MetricListener {

    protected final int jobID = JobCpi.allocJobID();
    
    protected GATContext gatContext;

    private Job wrapperJob;

    private Map<JobDescription, WrappedJobCpi> wrappedJobs = new HashMap<JobDescription, WrappedJobCpi>();

    public WrapperJobCpi(GATContext gatContext, Job wrapperJob) {
        this.gatContext = gatContext;
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        MetricDefinition statusMetricDefinition = new MetricDefinition(
                "job.status", MetricDefinition.DISCRETE, "JobState", null,
                null, returnDef);
        registerMetric("getJobStatus", statusMetricDefinition);

        this.wrapperJob = wrapperJob;

        WrapperJobDescription wrapperDescription = (WrapperJobDescription) wrapperJob
                .getJobDescription();
        for (WrappedJobInfo info : wrapperDescription.getJobInfos()) {
            WrappedJobCpi wrappedJob = new WrappedJobCpi(gatContext, info, this);
            wrappedJobs.put(info.getJobDescription(), wrappedJob);
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
        System.out.println("forwarding metric event " + event);
        fireMetric(event);
    }

}
