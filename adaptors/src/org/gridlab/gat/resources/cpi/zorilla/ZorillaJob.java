/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.zorilla;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import nl.vu.zorilla.zoni.JobInfo;
import nl.vu.zorilla.zoni.ZoniConnection;
import nl.vu.zorilla.zoni.ZoniException;
import nl.vu.zorilla.zoni.ZoniProtocol;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;

/**
 * @author ndrost
 */
public class ZorillaJob extends JobCpi implements Runnable {

	private static final Logger logger = Logger.getLogger(ZorillaJob.class);

	private static final long serialVersionUID = 1L;

	ZorillaResourceBrokerAdaptor broker;

	JobDescription description;

	MetricDefinition statusMetricDefinition;

	Metric statusMetric;

	private String jobID;

	private JobInfo info;

	private int lastState = -1;

	// GAT state in parent class...
	// protected int state;

	GATInvocationException error = null;

	// converts a GAT file to the string representation of its URI.
	// will add the full path in case of a local file
	private static String filetoString(File file) throws GATInvocationException {
		if (file == null) {
			return null;
		}

		if (!file.toGATURI().isLocal()) {
			throw new GATInvocationException(
					"zorilla can only handle local files");
		}

		return file.getAbsolutePath();
	}

	private static String virtualPath(File file) throws GATInvocationException {
		if (file == null) {
			return null;
		}
		return "/" + file.getName();
	}

	// private Map<String, String(URI)> toStringMap(Map<File, File>);
	private static Map<String, String> toStringMap(Map<File, File> fileMap)
			throws GATInvocationException {
		Map<String, String> result = new HashMap<String, String>();

		Iterator<Map.Entry<File, File>> iterator = fileMap.entrySet()
				.iterator();

		while (iterator.hasNext()) {
			Map.Entry<File, File> entry = (Map.Entry<File, File>) iterator
					.next();

			File key = (File) entry.getKey();
			File value = (File) entry.getValue();

			// value is "virtual" path in zorilla
			String virtualPath = virtualPath(key);
			String physicalPath = filetoString(value);

			result.put(virtualPath, physicalPath);
		}
		return result;
	}

	ZorillaJob(GATContext gatContext, Preferences preferences,
			ZorillaResourceBrokerAdaptor broker, JobDescription description
			)
			throws GATInvocationException {
		super(gatContext, preferences, description, null);
		this.broker = broker;
		this.description = description;

		logger.debug("creating zorilla job");

		// Tell the engine that we provide job.status events
		HashMap<String, Object> returnDef = new HashMap<String, Object>();
		returnDef.put("status", String.class);
		statusMetricDefinition = new MetricDefinition("job.status",
				MetricDefinition.DISCRETE, "String", null, null, returnDef);
		statusMetric = statusMetricDefinition.createMetric(null);
		GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);

		// data needed to submit a job
		URI executable;
		String[] arguments;
		Map<String, Object> attributes; // Map<String, String>
		Map<String, Object> environment; // Map<String, String>
		Map<String, String> preStageFiles; // Map<String, String> (virtual file
											// path, physical
		// file path)
		Map<String, String> postStageFiles; // Map<String, String> (file path,
											// physical file
		// path)
		String stdout;
		String stdin;
		String stderr;

		SoftwareDescription soft = description.getSoftwareDescription();

		executable = soft.getLocation();
		environment = soft.getEnvironment();
		if (environment == null) {
			environment = new HashMap<String, Object>();
		}

		preStageFiles = toStringMap(soft.getPreStaged());
		postStageFiles = toStringMap(soft.getPostStaged());

		stdout = filetoString(soft.getStdout());
		stdin = filetoString(soft.getStdin());
		stderr = filetoString(soft.getStderr());

		arguments = soft.getArguments();
		if (arguments == null) {
			arguments = new String[0];
		}

		attributes = soft.getAttributes();
		if (attributes == null) {
			attributes = new HashMap<String, Object>();
		}

		try {
			ZoniConnection connection = new ZoniConnection(broker
					.getNodeSocketAddress(), null, ZoniProtocol.TYPE_CLIENT);

			jobID = connection.submitJob(executable.toString(), arguments,
					environment, attributes, preStageFiles, postStageFiles,
					stdout, stdin, stderr);
			connection.close();
		} catch (IOException e) {
			throw new GATInvocationException(
					"cannot submit job to zorilla node", e);
		} catch (ZoniException e) {
			throw new GATInvocationException(
					"cannot submit job to zorilla node", e);
		}

		updateState();

		logger.debug("done creating job");

		new Thread(this).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#getInfo()
	 */
	@SuppressWarnings("unchecked")
	public synchronized Map<String, Object> getInfo()
			throws GATInvocationException {
		HashMap<String, Object> result = new HashMap<String, Object>();

		// stuff from zorilla node
		result.putAll((Map<String, Object>) info.getStatus());

		result.put("state", getStateString(getState()));
		result.put("resManState", result.get("phase"));
		result.put("resManName", "Zorilla");
		result.put("hostname", broker.getNodeSocketAddress().getHostName());
		if (error != null) {
			result.put("resManError", error.getMessage());
		}

		result.put("executable", info.getExecutable());

		// attributes map as a string
		Iterator<Map.Entry<String, Object>> iterator = (Iterator<Map.Entry<String, Object>>) info
				.getAttributes().entrySet().iterator();
		String attributeString = "";
		while (iterator.hasNext()) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator
					.next();
			attributeString += entry.getKey() + "=" + entry.getValue() + "&";
		}
		if (attributeString.length() > 1) {
			attributeString = attributeString.substring(0, attributeString
					.length() - 1);
		}
		result.put("attributes", attributeString);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#getJobID()
	 */
	public synchronized String getJobID() {
		return jobID;
	}

	private void updateState() throws GATInvocationException {
		try {
			ZoniConnection connection = new ZoniConnection(broker
					.getNodeSocketAddress(), null, ZoniProtocol.TYPE_CLIENT);

			JobInfo info = connection.getJobInfo(jobID);
			connection.close();

			synchronized (this) {
				this.info = info;
			}
			doCallBack();

		} catch (IOException e) {
			throw new GATInvocationException("could not update state", e);
		} catch (ZoniException e) {
			throw new GATInvocationException("could not update state", e);
		}

	}

	// convert Zorilla phase to GAT state
	private int state(int phase) throws GATInvocationException {
		if (phase == ZoniProtocol.PHASE_UNKNOWN) {
			return Job.UNKNOWN;
		} else if (phase == ZoniProtocol.PHASE_INITIAL) {
			return Job.INITIAL;
		} else if (phase == ZoniProtocol.PHASE_PRE_STAGE) {
			return Job.PRE_STAGING;
		} else if (phase == ZoniProtocol.PHASE_SCHEDULING) {
			return Job.SCHEDULED;
		} else if (phase == ZoniProtocol.PHASE_RUNNING
				|| phase == ZoniProtocol.PHASE_CLOSED) {
			return Job.RUNNING;
		} else if (phase == ZoniProtocol.PHASE_POST_STAGING) {
			return Job.POST_STAGING;
		} else if (phase == ZoniProtocol.PHASE_COMPLETED
				|| phase == ZoniProtocol.PHASE_CANCELLED) {
			return Job.STOPPED;
		} else if (phase == ZoniProtocol.PHASE_ERROR) {
			return Job.SUBMISSION_ERROR;
		}
		throw new GATInvocationException("unknown Zorilla phase: " + phase);
	}

	public synchronized int getState() {
		try {
			return state(info.getPhase());
		} catch (Exception e) {
			return UNKNOWN;
		}
	}

	private void doCallBack() throws GATInvocationException {
		MetricValue v = null;

		synchronized (this) {
			int state = getState();

			if (state == lastState) {
				// no need to do callback, no significant change
				return;
			}
			lastState = state;
			v = new MetricValue(this, getStateString(state), statusMetric,
					System.currentTimeMillis());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("default job callback: firing event: " + v);
		}

		GATEngine.fireMetric(this, v);

	}

	public void run() {
		while (true) {
			try {
				updateState();
			} catch (Exception e) {
				error = new GATInvocationException("Zorilla", e);
				synchronized (this) {
					state = Job.SUBMISSION_ERROR;
					return;
				}
			}

			synchronized (this) {
				if (info.getPhase() >= ZoniProtocol.PHASE_COMPLETED) {
					logger.debug("Zorilla Job exits..");
					return;
				}

				try {
					wait(5 * 1000);
				} catch (InterruptedException e) {
					// IGNORE
				}
			}
		}

	}
}
