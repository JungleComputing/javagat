/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.IPUtils;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;

/**
 * @author rob
 */
public class LocalJob extends Job {
	class ProcessWaiter extends Thread {

		ProcessWaiter() {
			start();
		}

		public void run() {
			try {
				int exitValue = p.waitFor();
				finished(exitValue);
			} catch (InterruptedException e) {
				// Cannot happen
			}
		}
	}

	LocalResourceBrokerAdaptor broker;

	GATInvocationException postStageException = null;

	JobDescription description;

	int jobID;

	Process p;

	static int globalJobID = 0;

	int exitVal = 0;

	boolean exited = false;

	MetricDefinition statusMetricDefinition;

	Metric statusMetric;

	static synchronized int allocJobID() {
		return globalJobID++;
	}

	LocalJob(LocalResourceBrokerAdaptor broker, JobDescription description,
			Process p) throws GATInvocationException {
		this.broker = broker;
		this.description = description;
		jobID = allocJobID();
		state = RUNNING;
		this.p = p;

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
	public synchronized Map getInfo() throws GATInvocationException, IOException {
		HashMap m = new HashMap();
		// update state
		getState();

		m.put("state", getStateString());
        m.put("resManState", getStateString());
        m.put("resManName", "Local");
		m.put("exitValue", "" + exitVal);
		m.put("hostname", IPUtils.getLocalHostName());
		
		if (postStageException != null) {
			m.put("postStageError", postStageException);
		}

		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#getJobDescription()
	 */
	public JobDescription getJobDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#getJobID()
	 */
	public String getJobID() throws GATInvocationException, IOException {
		return "" + jobID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#getState()
	 */
	public synchronized int getState() throws GATInvocationException,
			IOException {
		return state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.Advertisable#marshal()
	 */
	public String marshal() {
		// TODO Auto-generated method stub
		return null;
	}

	void finished(int exitValue) {
	    GATInvocationException tmpExc = null;
		MetricValue v = null;

		try {
			broker.postStageFiles(description, "");
		} catch (GATInvocationException e) {
			tmpExc = e;
		}

		synchronized (this) {
		    postStageException = tmpExc;
			exited = true;
			exitVal = exitValue;
			state = STOPPED;
			v = new MetricValue(this, getStateString(),
					statusMetric, System.currentTimeMillis());
		}
		if (GATEngine.DEBUG) {
			System.err.println("default job callback: firing event: " + v);
		}

		GATEngine.fireMetric(this, v);
	}
}