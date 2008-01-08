package org.gridlab.gat.resources.cpi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.WrappedJob;

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
public class WrappedJobImpl extends JobCpi implements MetricListener, WrappedJob {

    // this class variable is used to give each WrappedJob a unique ID
    private static int id = 0;

    private Job wrapperJob;
    private int jobID;
    private String jobString;

    private MetricDefinition statusMetricDefinition;
    private Metric statusMetric;
    private JobStateMonitor monitor;

    private synchronized static int getID() {
        return id++;
    }

    /**
     * Creates a new WrappedJob.
     * 
     * This constructor is used by the WrapperSubmitter.
     * 
     * @param gatContext
     * @param preferences
     * @param jobDescription
     */
    protected WrappedJobImpl(GATContext gatContext, Preferences preferences,
            JobDescription jobDescription) {
        super(gatContext, preferences, jobDescription, null);

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);

        // set the jobID
        jobID = getID();

        // start a thread that monitors the job state, by monitoring a file
        monitor = new JobStateMonitor();
        monitor.start();
    }

    /**
     * Associates the specified WrapperJob to this WrappedJob
     * 
     * @param j
     *                the WrapperJob
     * @throws GATInvocationException
     */
    protected void setWrapperJob(Job j) throws GATInvocationException {
        // each WrappedJob belongs to exactly one WrapperJob, but a
        // WrapperJob may be linked to more WrappedJobs. The
        // WrappedJob listens to the WrapperJob, because when it ends
        // abruptly, the WrapperJob should also end
        this.wrapperJob = j;
        MetricDefinition md = wrapperJob
                .getMetricDefinitionByName("job.status");
        wrapperJob.addMetricListener(this, md.createMetric());
    }

    /**
     * gets the WrapperJob
     * 
     * @return the WrapperJob
     */
    public Job getWrapperJob() {
        return wrapperJob;
    }

    /**
     * add a MetricListener to the WrapperJob
     * 
     * @param metricListener
     * @param metric
     * @throws GATInvocationException
     */
    public final void addMetricListenerToWrapperJob(
            MetricListener metricListener, Metric metric)
            throws GATInvocationException {
        // if you want to listen explicit to the wrapper job
        wrapperJob.addMetricListener(metricListener, metric);
    }

    /**
     * process the incoming metrics from the WrapperJob. Only do something if
     * the WrapperJob is stopped or if there's a submission error. Change the
     * state of the WrappedJob according to the state of the WrapperJob and fire
     * a metric to the application that listens to the WrappedJob.
     */
    public void processMetricEvent(MetricValue val) {
        if (state == STOPPED || state == SUBMISSION_ERROR) {
            return;
        }
        if (wrapperJob.getState() == Job.STOPPED
                || wrapperJob.getState() == Job.SUBMISSION_ERROR) {
            try {
                MetricDefinition md = wrapperJob
                        .getMetricDefinitionByName("job.status");
                wrapperJob.removeMetricListener(this, md.createMetric());
                wrapperJob.stop();
                fireStateMetric(wrapperJob.getState());
                finished();
            } catch (GATInvocationException e) {
                if (logger.isInfoEnabled()) {
                    logger.info(e);
                }
            }
        }
    }

    /**
     * gets the JobID of this Job
     * 
     * @return the JobID
     */
    public String getJobID() {
        if (jobString == null) {
            jobString = "WrapperJob" + jobID + "_" + Math.random();
        }
        return jobString;
    }

    private void fireStateMetric(int state) {
        MetricValue v = new MetricValue(this, Job.getStateString(state),
                statusMetric, System.currentTimeMillis());
        GATEngine.fireMetric(this, v);
    }

    private class JobStateMonitor extends Thread {

        public JobStateMonitor() {
            // set this thread to be a deamon thread, it will close if only
            // deamon threads are running. If it isn't set to be deamon thread
            // the GAT application will hang unless an explicit System.exit is
            // done.
            setDaemon(true);
        }

        public void run() {
            String statusFileName = null;

            statusFileName = System.getProperty("user.home") + File.separator
                    + ".JavaGATstatus" + getJobID();

            do {
                int newstate = -666;
                FileInputStream in = null;
                try {
                    in = new FileInputStream(statusFileName);
                    newstate = in.read();
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
                if (newstate >= 0) {
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
            } while (state != Job.STOPPED && state != Job.SUBMISSION_ERROR);
        }
    }
}
