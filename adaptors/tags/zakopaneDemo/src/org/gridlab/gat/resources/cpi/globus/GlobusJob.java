package org.gridlab.gat.resources.cpi.globus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.globus.gram.Gram;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;

/**
 * @author rob
 */
public class GlobusJob extends Job implements GramJobListener,
		org.globus.gram.internal.GRAMConstants {

	GlobusBrokerAdaptor broker;

	JobDescription jobDescription;

	GramJob j;

	MetricDefinition statusMetricDefinition;

	Metric statusMetric;

	GATInvocationException postStageException = null;

	public GlobusJob(GlobusBrokerAdaptor broker, JobDescription jobDescription,
			GramJob j) throws GATInvocationException {
		this.broker = broker;
		this.jobDescription = jobDescription;
		this.j = j;
		state = SCHEDULED;

		// Tell the engine that we provide job.status events
		HashMap returnDef = new HashMap();
		returnDef.put("status", String.class);
		statusMetricDefinition = new MetricDefinition("job.status",
				MetricDefinition.DISCRETE, "String", null, null, returnDef);
		statusMetric = statusMetricDefinition.createMetric(null);
		GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
	}

	public JobDescription getJobDescription() {
		return jobDescription;
	}

	public synchronized Map getInfo() throws GATInvocationException,
			IOException {
		HashMap m = new HashMap();
		setState(); // update the state
		m.put("state", getStateString());
		m.put("globusState", j.getStatusAsString());
		m.put("error", "" + j.getError());
		m.put("id", j.getIDAsString());

		if (postStageException != null) {
			m.put("postStageError", postStageException);
		}
		return m;
	}

    public synchronized int getState() {
	return state;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.Advertisable#marshal()
	 */
	public String marshal() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.Advertisable#unmarshal(java.lang.String)
	 */
	public Advertisable unmarshal(String input) {
		throw new Error("Not implemented");
	}

	protected void setState() {
		switch (j.getStatus()) {
		case STATUS_ACTIVE:
			state = RUNNING;
			break;
		case STATUS_DONE:
			state = STOPPED;
			break;
		case STATUS_FAILED:
			state = SUBMISSION_ERROR;
			break;
		case STATUS_PENDING:
			state = SCHEDULED;
			break;
		case STATUS_STAGE_IN:
			state = SCHEDULED;
			break;
		case STATUS_STAGE_OUT:
			state = SCHEDULED;
			break;
		case STATUS_SUSPENDED:
			state = STOPPED;
			break;
		case 0: // unknown (no constant :-( )
			state = INITIAL;
		case STATUS_UNSUBMITTED:
			state = INITIAL;
			break;
		default:
			System.err.println("WARNING: Globus job: unknown state: "
					+ j.getStatus() + " (" + j.getStatusAsString() + ")");
		}
	}

	public void stop() throws GATInvocationException, IOException {
		if (getState() != RUNNING) {
			throw new GATInvocationException("Job is not running");
		}

		try {
			j.cancel();
		} catch (Exception e) {
			throw new GATInvocationException("globus job", e);
		}

		stopHandlers();

		state = INITIAL;
	}

	public void unSchedule() throws GATInvocationException, IOException {
		if (getState() != SCHEDULED) {
			throw new GATInvocationException("Job is not in SHCEDULED state");
		}

		try {
			j.cancel();
		} catch (Exception e) {
			throw new GATInvocationException("globus job", e);
		}

		stopHandlers();

		state = INITIAL;
	}

	protected void stopHandlers() {
		try {
			Gram.unregisterListener(j);
		} catch (Throwable t) {
			if (GATEngine.VERBOSE) {
				System.err
						.println("WARNING, globus job could not unbind: " + t);
			}
		}
		try {
			Gram.deactivateAllCallbackHandlers();
		} catch (Throwable t) {
			if (GATEngine.VERBOSE) {
				System.err
						.println("WARNING, globus job could not deactivate callback: "
								+ t);
			}
		}
	}

	/**
	 * @see org.globus.gram.GramJobListener#statusChanged(org.globus.gram.GramJob)
	 */
	public synchronized void statusChanged(GramJob newJob) {
		setState();
		if (state == STOPPED || state == SUBMISSION_ERROR) {
			stopHandlers();
		}

		if (state == STOPPED) {
			try {
				broker.postStageFiles(jobDescription, broker
						.getHostname(jobDescription));
			} catch (GATInvocationException e) {
				postStageException = e;
			}
		}

		MetricValue v = new MetricValue(this, getStateString(), statusMetric,
				System.currentTimeMillis());

		if (GATEngine.DEBUG) {
			System.err.println("globus job callback: firing event: " + v);
		}

		GATEngine.fireMetric(this, v);
	}

	/*
	 * public List getMetricDefinitions() throws GATInvocationException { return
	 * new Vector(metricDefinitions.values()); }
	 * 
	 * public MetricDefinition getMetricDefinitionByName(String name) throws
	 * GATInvocationException { return (MetricDefinition)
	 * metricDefinitions.get(name); }
	 */
}
