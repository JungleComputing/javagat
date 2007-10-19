/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.commandlineSshPrun;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.OutputForwarder;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * @author rob
 */
@SuppressWarnings("serial")
public class CommandlineSshPrunJob extends JobCpi {

	protected static Logger logger = Logger
			.getLogger(CommandlineSshPrunJob.class);

	CommandlineSshPrunResourceBrokerAdaptor broker;

	GATInvocationException postStageException = null;

	JobDescription description;

	int jobID;

	Process p;

	int exitVal = 0;

	MetricDefinition statusMetricDefinition;

	Metric statusMetric;

	OutputForwarder out;

	OutputForwarder err;

	CommandlineSshPrunJob(GATContext gatContext, Preferences preferences,
			CommandlineSshPrunResourceBrokerAdaptor broker,
			JobDescription description, Process p, Sandbox sandbox,
			OutputForwarder out, OutputForwarder err, MetricListener listener, Metric metric) {
		super(gatContext, preferences, description, sandbox, listener, metric);
		this.broker = broker;
		jobID = allocJobID();
		state = RUNNING;
		this.p = p;
		this.out = out;
		this.err = err;

		// Tell the engine that we provide job.status events
		HashMap<String, Object> returnDef = new HashMap<String, Object>();
		returnDef.put("status", String.class);
		statusMetricDefinition = new MetricDefinition("job.status",
				MetricDefinition.DISCRETE, "String", null, null, returnDef);
		statusMetric = statusMetricDefinition.createMetric(null);
		GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);

		new ProcessWaiter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#getExitStatus()
	 */
	public synchronized int getExitStatus() throws GATInvocationException {
		if (state != STOPPED)
			throw new GATInvocationException("not in RUNNING state");
		return exitVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#getInfo()
	 */
	public synchronized Map<String, Object> getInfo() {
		HashMap<String, Object> m = new HashMap<String, Object>();

		// update state
		getState();

		m.put("state", getStateString(state));
		m.put("resManState", getStateString(state));
		m.put("resManName", "Local");
		m.put("exitValue", "" + exitVal);
		m.put("hostname", GATEngine.getLocalHostName());

		if (postStageException != null) {
			m.put("postStageError", postStageException);
		}

		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#getJobID()
	 */
	public String getJobID() {
		return "" + jobID;
	}

	void finished(int exitValue) {
		MetricValue v = null;

		synchronized (this) {
			exitVal = exitValue;
			state = POST_STAGING;
			v = new MetricValue(this, getStateString(state), statusMetric,
					System.currentTimeMillis());
			if (logger.isDebugEnabled()) {
				logger.debug("default job callback: firing event: " + v);
			}
		}
		GATEngine.fireMetric(this, v);

		sandbox.retrieveAndCleanup(this);

		synchronized (this) {
			state = STOPPED;
			v = new MetricValue(this, getStateString(state), statusMetric,
					System.currentTimeMillis());
			if (logger.isDebugEnabled()) {
				logger.debug("default job callback: firing event: " + v);
			}
		}
		GATEngine.fireMetric(this, v);

		finished();
	}

	public void stop() throws GATInvocationException {
		MetricValue v = null;

		synchronized (this) {
			p.destroy();
			state = POST_STAGING;
			v = new MetricValue(this, getStateString(state), statusMetric,
					System.currentTimeMillis());
			if (logger.isDebugEnabled()) {
				logger.debug("default job callback: firing event: " + v);
			}
		}
		GATEngine.fireMetric(this, v);

		sandbox.retrieveAndCleanup(this);

		synchronized (this) {
			state = STOPPED;
			v = new MetricValue(this, getStateString(state), statusMetric,
					System.currentTimeMillis());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("commandline ssh job callback: firing event: " + v);
		}

		GATEngine.fireMetric(this, v);
		finished();
	}

	class ProcessWaiter extends Thread {
		ProcessWaiter() {
			start();
		}

		public void run() {
			try {
				int exitValue = p.waitFor();

				// Wait for the output forwarders to finish!
				// You may lose output if you don't -- Jason
				if (out != null) {
					out.waitUntilFinished();
				}

				if (err != null) {
					err.waitUntilFinished();
				}

				finished(exitValue);
			} catch (InterruptedException e) {
				// Cannot happen
			}
		}
	}
}
