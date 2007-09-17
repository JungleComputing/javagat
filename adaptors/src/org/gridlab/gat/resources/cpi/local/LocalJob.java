/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.local;

import java.lang.reflect.Field;
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
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * @author rob
 */
public class LocalJob extends JobCpi {

	protected static Logger logger = Logger.getLogger(LocalJob.class);

	class ProcessWaiter extends Thread {
		ProcessWaiter() {
			start();
		}

		public void run() {
			try {
				int exitValue = p.waitFor();
				runTime = System.currentTimeMillis() - runTime;

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

	// private LocalResourceBrokerAdaptor broker;

	private int jobID;

	private Process p;

	private int exitVal = 0;

	private MetricDefinition statusMetricDefinition;

	private Metric statusMetric;

	private OutputForwarder out;

	private OutputForwarder err;

	private long startTime;
	private long runTime;

	LocalJob(GATContext gatContext, Preferences preferences,
			LocalResourceBrokerAdaptor broker, JobDescription description,
			Process p, String host, Sandbox sandbox, OutputForwarder out,
			OutputForwarder err, long startTime, long startRun) {
		super(gatContext, preferences, description, sandbox);
		// this.broker = broker;
		jobID = allocJobID();
		Field f = null;
		try {
			f = p.getClass().getDeclaredField("pid");
			f.setAccessible(true);
			jobID = Integer.parseInt(f.get(p).toString()); // toString
			// ignore exceptions // necessary?
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}

		state = RUNNING;
		this.p = p;
		this.out = out;
		this.err = err;
		this.startTime = startTime;
		this.runTime = startRun;

		// Tell the engine that we provide job.status events
		HashMap returnDef = new HashMap();
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
	 * @see org.gridlab.gat.resources.Job#getInfo()
	 */
	public synchronized Map getInfo() throws GATInvocationException {
		HashMap m = new HashMap();

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
		if (deleteException != null) {
			m.put("deleteError", deleteException);
		}
		if (wipeException != null) {
			m.put("wipeError", wipeException);
		}

		return m;
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
	 * @see org.gridlab.gat.resources.Job#getJobID()
	 */
	public String getJobID() throws GATInvocationException {
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

		if (GATEngine.TIMING) {
			System.err.println("TIMING: job " + jobID + ":" + " preStage: "
					+ sandbox.getPreStageTime() + " run: " + runTime
					+ " postStage: " + sandbox.getPostStageTime() + " wipe: "
					+ sandbox.getWipeTime() + " delete: "
					+ sandbox.getDeleteTime() + " total: "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	public void stop() throws GATInvocationException {
		MetricValue v = null;

		synchronized (this) {
			if (p != null)
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
			logger.debug("default job callback: firing event: " + v);
		}

		GATEngine.fireMetric(this, v);
		finished();
	}
}
