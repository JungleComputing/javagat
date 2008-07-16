package org.gridlab.gat.resources.cpi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.WrapperJobDescription.WrappedJobInfo;

/**
 * A Job object of a job that is submitted by a Wrapper.
 * 
 * To use a Wrapper to submit a (regular) job, the preference "useWrapper" must
 * have the value "true". JavaGAT then creates a (wrapper) job that executes a
 * Wrapper at the location where the (regular) job should execute. The Wrapper
 * executes and submits locally one or more (regular) jobs. The submission of a
 * (regular) job will return a job object of the type WrappedJob.
 * 
 * The WrappedJob has some special methods besides the standard Job
 * functionality. It is possible to retrieve the Job of the Wrapper, which may
 * come in handy if you use multi jobs and you want to know when the WrapperJob
 * is finished (i.e. when all WrappedJobs are finished). You can also add a
 * MetricListener to the WrapperJob.
 * 
 * The WrappedJob listens to its status by monitoring (polling) a status file.
 * The Wrapper, which listens to the local (regular) job, writes the state
 * changes to this status file as a way to communicate with the WrappedJob
 * object. The status files typically are located at $HOME and look like:
 * ".JavaGATstatus" + jobID
 * 
 * @author rkemp
 */

@SuppressWarnings("serial")
public class WrappedJobCpi extends JobCpi implements Runnable {

    private MetricDefinition statusMetricDefinition;

    private Metric statusMetric;

    private String statusFileName;

    /**
     * Creates a new WrappedJob.
     * 
     * This constructor is used by the WrapperSubmitter.
     * 
     * @param gatContext
     * @param jobDescription
     */
    public WrappedJobCpi(WrappedJobInfo info) {
        super(null, info.getJobDescription(), null);

        this.statusFileName = info.getJobStateFileName();

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);

        // start a thread that monitors the job state, by monitoring a file
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("Wrapped Job State Monitor " + getJobID());
        thread.start();
    }

    private void fireStateMetric(JobState state) {
        MetricEvent v = new MetricEvent(this, state.toString(), statusMetric,
                System.currentTimeMillis());
        GATEngine.fireMetric(this, v);
    }

    public void run() {
        do {
            JobState newstate = null;
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(new FileInputStream(statusFileName));
                newstate = (JobState) in.readObject();
            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    logger.info(e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    if (logger.isInfoEnabled()) {
                        logger.info(e);
                    }
                }
            }
            if (newstate != null) {
                File monitorFile = new File(statusFileName);
                if (!monitorFile.delete()) {
                    logger.fatal("Could not delete job status file!");
                }

                state = newstate;
                fireStateMetric(state);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (logger.isInfoEnabled()) {
                    logger.info(e);
                }
            }
        } while (state != JobState.STOPPED
                && state != JobState.SUBMISSION_ERROR);
    }
}
