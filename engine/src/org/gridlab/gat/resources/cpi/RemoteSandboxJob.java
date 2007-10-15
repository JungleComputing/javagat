package org.gridlab.gat.resources.cpi;

import java.io.IOException;
import java.util.HashMap;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;

@SuppressWarnings("serial")
public class RemoteSandboxJob extends JobCpi implements MetricListener {

	// this class variable is used to give each RemoteSandboxJob a unique ID
	private static int id = 0;

	private Job sandboxJob;
	private int jobID;

	private MetricDefinition statusMetricDefinition;
	private Metric statusMetric;
	private JobStateMonitor monitor;
	
	private synchronized static int getID() {
		return id++;
	}
	
	public RemoteSandboxJob(GATContext gatContext, Preferences preferences,
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

	public void setSandboxJob(Job j) throws GATInvocationException {
		// each RemoteSandboxJob belongs to exactly one SandboxJob, but a
		// SandboxJob may be linked to more RemoteSandboxJobs. The
		// RemoteSandboxJob listens to the SandboxJob, because when it ends
		// abruptly, the RemoteSandboxJob should also end
		this.sandboxJob = j;
		MetricDefinition md = sandboxJob
				.getMetricDefinitionByName("job.status");
		sandboxJob.addMetricListener(this, md.createMetric());

	}

	public void processMetricEvent(MetricValue val) {
		// process the incoming metrics from the sandboxJob. Only do something
		// if the sandboxJob is stopped or if there's a submission error. Change
		// the state of the RemoteSandboxJob according to the state of the
		// sandboxJob and fire a metric to the application that listens to the
		// RemoteSandboxJob.
		if (sandboxJob.getState() == Job.STOPPED
				|| sandboxJob.getState() == Job.SUBMISSION_ERROR) {
			fireStateMetric(sandboxJob.getState());
			try {
				MetricDefinition md = sandboxJob
						.getMetricDefinitionByName("job.status");
				sandboxJob.removeMetricListener(this, md.createMetric());
				sandboxJob.stop();
				finished();
			} catch (GATInvocationException e) {
				if (logger.isInfoEnabled()) {
					logger.info(e);
				}
			}
		}
	}

	public String getJobID() throws GATInvocationException {
		// "RSJ" -> Remote Sandbox Job
		return "RSJ" + jobID + "_" + Math.random();
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
			File monitorFile = null;
			try {
				monitorFile = GAT.createFile(gatContext,
						"any://localhost/.JavaGATstatus" + getJobID());
			} catch (GATObjectCreationException e) {
				if (logger.isInfoEnabled()) {
					logger.info(e);
				}
			} catch (GATInvocationException e) {
				if (logger.isInfoEnabled()) {
					logger.info(e);
				}
			}
			int oldState = state;
			do {
				FileInputStream in;
				try {
					in = GAT.createFileInputStream(gatContext, monitorFile);
					state = in.read();
					in.close();
					if (state == -1) {
						// nothing read, revert state to old state, try again...
						state = oldState;
						continue;
					}
					monitorFile.delete();
				} catch (GATObjectCreationException e) {
					if (logger.isInfoEnabled()) {
						logger.info(e);
					}
				} catch (IOException e) {
					if (logger.isInfoEnabled()) {
						logger.info(e);
					}
				}
				if (state != oldState) {
					fireStateMetric(state);
					oldState = state;
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
