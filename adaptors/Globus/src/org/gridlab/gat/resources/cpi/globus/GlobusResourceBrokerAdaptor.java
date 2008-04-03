/*
 * Created on Oct 14, 2004
 */
package org.gridlab.gat.resources.cpi.globus;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.common.ResourceManagerContact;
import org.globus.gram.Gram;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.internal.GRAMConstants;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.PostStagedFile;
import org.gridlab.gat.resources.cpi.PostStagedFileSet;
import org.gridlab.gat.resources.cpi.PreStagedFile;
import org.gridlab.gat.resources.cpi.PreStagedFileSet;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperSubmitter;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * @author rob
 */
public class GlobusResourceBrokerAdaptor extends ResourceBrokerCpi {

	protected static Logger logger = Logger
			.getLogger(GlobusResourceBrokerAdaptor.class);

	static boolean shutdownInProgress = false;

	private WrapperSubmitter submitter;

	public static void init() {
		GATEngine.registerUnmarshaller(GlobusJob.class);
	}

	public GlobusResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
			throws GATObjectCreationException {
		super(gatContext, brokerURI);
		if (brokerURI == null) {
			throw new GATObjectCreationException("brokerURI is null");
		}
	}

	public void beginMultiJob() throws GATInvocationException {
		if (submitter != null && submitter.isMultiJob()) {
			throw new GATInvocationException("Multi job started twice!");
		}
		submitter = new WrapperSubmitter(gatContext, brokerURI, true);
	}

	public Job endMultiJob() throws GATInvocationException {
		if (submitter == null) {
			throw new GATInvocationException(
					"Multi job ended, without being started!");
		}
		Job job = submitter.flushJobSubmission();
		submitter = null;
		return job;
	}

	protected String createRSL(JobDescription description, String host,
			Sandbox sandbox, PreStagedFileSet pre, PostStagedFileSet post)
			throws GATInvocationException {
		SoftwareDescription sd = description.getSoftwareDescription();

		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		String rsl = "";
		String args = "";

		rsl += "& (executable = " + getExecutable(description) + ")";

		// parse the arguments
		String[] argsA = getArgumentsArray(description);

		if (argsA != null) {
			for (int i = 0; i < argsA.length; i++) {
				args += (" \"" + argsA[i] + "\" ");
			}
		}
		if (args.length() != 0) {
			rsl += (" (arguments = " + args + ")");
		}

		rsl += " (count = " + getCPUCount(description) + ")";

		rsl += " (hostCount = " + getHostCount(description) + ")";

		String jobType = getStringAttribute(description, "job.type", null);
		if (jobType != null) {
			rsl += " (jobType = " + jobType + ")";
		}

		if (sandbox != null) {
			rsl += " (directory = " + sandbox.getSandbox() + ")";
		}

		long maxTime = getLongAttribute(description, "time.max", -1);
		if (maxTime > 0) {
			rsl += " (maxTime = " + maxTime + ")";
		}

		long maxWallTime = getLongAttribute(description, "walltime.max", -1);
		if (maxWallTime > 0) {
			rsl += " (maxWallTime = " + maxWallTime + ")";
		}

		long maxCPUTime = getLongAttribute(description, "cputime.max", -1);
		if (maxCPUTime > 0) {
			rsl += " (maxCPUTime = " + maxCPUTime + ")";
		}

		// stage in files with gram
		// if the files are staged using gat, pre and post in this method are
		// null
		if (pre != null) {
			for (int i = 0; i < pre.size(); i++) {
				PreStagedFile f = pre.getFile(i);

				if (!f.getResolvedSrc().toGATURI().refersToLocalHost()) {
					rsl += " (file_stage_in = (gsiftp://"
							+ f.getResolvedSrc().toGATURI().getHost() + "/"
							+ f.getResolvedSrc().getPath() + " "
							+ f.getResolvedDest().getPath() + "))";
				} else {

					rsl += " (file_stage_in = (file:///"
							+ f.getResolvedSrc().getPath() + " "
							+ f.getResolvedDest().getPath() + "))";
				}
			}
		}

		if (post != null) {
			for (int i = 0; i < post.size(); i++) {
				PostStagedFile f = post.getFile(i);

				if (!f.getResolvedSrc().toGATURI().refersToLocalHost()) {
					rsl += " (file_stage_out = ("
							+ f.getResolvedDest().getPath() + " " + "gsiftp://"
							+ f.getResolvedSrc().toGATURI().getHost() + "/"
							+ f.getResolvedSrc().getPath() + "))";
				} else {
					rsl += " (file_stage_out = ("
							+ f.getResolvedDest().getPath() + " gsiftp://"
							+ GATEngine.getLocalHostName() + "/"
							+ f.getResolvedSrc().getPath() + "))";
				}
			}
		}

		if (sd.getStdout() != null || sd.getStdoutStream() != null) {
			// if (sandbox != null && sd.getStdout() != null) {
			rsl += (" (stdout = " + sandbox.getRelativeStdout().getPath() + ")");
			// } else {
			// rsl += (" (stdout = stdout)");
			// }
		}

		if (sd.getStderr() != null || sd.getStderrStream() != null) {
			// if (sandbox != null && sd.getStderr() != null) {
			rsl += (" (stderr = " + sandbox.getRelativeStderr().getPath() + ")");
			// } else {
			// rsl += (" (stderr = stderr)");
			// }
		}

		org.gridlab.gat.io.File stdin = sd.getStdin();
		if (stdin != null) {
			if (sandbox != null) {
				rsl += (" (stdin = " + sandbox.getRelativeStdin().getPath() + ")");
			}
		}

		// set the environment
		Map<String, Object> env = sd.getEnvironment();
		if (env != null && !env.isEmpty()) {
			Set<String> s = env.keySet();
			Object[] keys = (Object[]) s.toArray();
			rsl += "(environment = ";

			for (int i = 0; i < keys.length; i++) {
				String val = (String) env.get(keys[i]);
				rsl += "(" + keys[i] + " \"" + val + "\")";
			}
			rsl += ")";
		}

		String queue = getStringAttribute(description, "globus.queue", null);
		if (queue != null) {
			rsl += " (queue = " + queue + ")";
		}

		String timeout = getStringAttribute(description,
				"globus.proxy.timeout", null);
		if (queue != null) {
			rsl += " (proxy_timeout = " + timeout + ")";
		}

		if (logger.isDebugEnabled()) {
			logger.debug("RSL: " + rsl);
		}

		return rsl;
	}

	protected String createChmodRSL(JobDescription description, String host,
			String chmodLocation, Sandbox sandbox, String executable) {
		String rsl = "& (executable = " + chmodLocation + ")";

		if (sandbox != null) {
			rsl += " (directory = " + sandbox.getSandbox() + ")";
		}

		rsl += " (arguments = \"+x\" \"" + executable + "\")";

		if (logger.isDebugEnabled()) {
			logger.debug("CHMOD RSL: " + rsl);
		}

		return rsl;
	}

	private void runGramJobPolling(GSSCredential credential, String rsl,
			String contact) throws GATInvocationException {
		GramJob j = new GramJob(credential, rsl);
		try {
			Gram.request(contact, j);
		} catch (GramException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("could not run job: "
						+ GramError.getGramErrorString(e.getErrorCode()));
			}

			return;
		} catch (GSSException e2) {
			throw new CouldNotInitializeCredentialException("globus", e2);
		}

		int errorCount = 0;
		while (errorCount <= 3) {
			try {
				Gram.jobStatus(j);
				int status = j.getStatus();
				if (logger.isDebugEnabled()) {
					logger.debug("job status = " + status);
				}
				if (status == GRAMConstants.STATUS_DONE
						|| status == GRAMConstants.STATUS_FAILED) {
					return;
				}
			} catch (Exception e) {
				if (j.getError() == GramError.GRAM_JOBMANAGER_CONNECTION_FAILURE) {
					return;
				}

				if (logger.isDebugEnabled()) {
					logger.debug("got exception: " + e);
				}

				// ignore other errors
				errorCount++;
			}

			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private void submitChmodJob(GSSCredential credential,
			JobDescription description, String host, String chmodLocation,
			Sandbox sandbox, String path) throws GATInvocationException {
		if (logger.isDebugEnabled()) {
			logger.debug("running " + chmodLocation + " on " + host
					+ "/jobmanager-fork to set executable bit on ");
		}
		String chmodRsl = createChmodRSL(description, host, chmodLocation,
				sandbox, path);

		runGramJobPolling(credential, chmodRsl, host + "/jobmanager-fork");
		if (logger.isDebugEnabled()) {
			logger.debug("done");
		}
	}

	public Job submitJob(JobDescription description, MetricListener listener,
			String metricDefinitionName) throws GATInvocationException {
		boolean useGramSandbox = false;
		String s = (String) gatContext.getPreferences().get(
				"globus.sandbox.gram");
		if (s != null && s.equalsIgnoreCase("true")) {
			useGramSandbox = true;
		}
		if (useGramSandbox) {
			return submitJobGramSandbox(description, listener,
					metricDefinitionName);
		} else {
			return submitJobGatSandbox(description, listener,
					metricDefinitionName);
		}
	}

	private boolean isExitValueEnabled(JobDescription description) {
		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null) {
			return false;
		}
		if (sd.getAttributes().containsKey("globus.exitvalue.enable")) {
			if (((String) sd.getAttributes().get("globus.exitvalue.enable"))
					.equalsIgnoreCase("true")) {
				return true;
			}
		}
		return false;
	}

	private GSSCredential getCredential(String host)
			throws GATInvocationException {
		URI hostUri;
		try {
			hostUri = new URI(host);
		} catch (Exception e) {
			throw new GATInvocationException("globus broker", e);
		}

		GSSCredential credential = null;
		try {
			credential = GlobusSecurityUtils.getGlobusCredential(gatContext,
					"gram", hostUri, ResourceManagerContact.DEFAULT_PORT);
		} catch (CouldNotInitializeCredentialException e) {
			throw new GATInvocationException("globus", e);
		} catch (CredentialExpiredException e) {
			throw new GATInvocationException("globus", e);
		}
		return credential;
	}

	private void runChmod(GSSCredential credential, JobDescription description,
			String host, Sandbox sandbox) {

		try {
			submitChmodJob(credential, description, host, "/bin/chmod",
					sandbox, getExecutable(description));
		} catch (Exception e) {
			// ignore
		}
		try {
			submitChmodJob(credential, description, host, "/usr/bin/chmod",
					sandbox, getExecutable(description));
		} catch (Exception e) {
			// ignore
		}
	}

	public Job submitJobGramSandbox(JobDescription description,
			MetricListener listener, String metricDefinitionName)
			throws GATInvocationException {
		// long start = System.currentTimeMillis();
		String host = getHostname();
		String contact = brokerURI.toString();
		if (brokerURI.getScheme() != null) {
			contact = contact.replace(brokerURI.getScheme() + "://", "");
		}

		URI hostUri;
		try {
			hostUri = new URI(host);
		} catch (Exception e) {
			throw new GATInvocationException("globus broker", e);
		}
		GlobusJob job = new GlobusJob(gatContext, description, null);

		// if special preference "globus.exitvalue.enable" is set to true,
		// modify the softwaredescription
		String random = "" + Math.random();
		if (isExitValueEnabled(description)) {
			description.getSoftwareDescription().toWrapper(gatContext,
					".JavaGAT-wrapper-script-" + random,
					".JavaGAT-exit-value-" + random);
			job.setExitValueEnabled(isExitValueEnabled(description),
					".JavaGAT-exit-value-" + random);
		}

		if (listener != null && metricDefinitionName != null) {
			Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
					.createMetric(null);
			job.addMetricListener(listener, metric);
		}
		job.setState(Job.PRE_STAGING);
		PreStagedFileSet pre = new PreStagedFileSet(gatContext, description,
				host, null, false);

		PostStagedFileSet post = new PostStagedFileSet(gatContext, description,
				host, null, false, false);

		String rsl = createRSL(description, host, null, pre, post);
		if (logger.isInfoEnabled()) {
			logger.info("RSL: " + rsl);
		}

		GSSCredential credential = null;
		try {
			credential = GlobusSecurityUtils.getGlobusCredential(gatContext,
					"gram", hostUri, ResourceManagerContact.DEFAULT_PORT);
		} catch (CouldNotInitializeCredentialException e) {
			throw new GATInvocationException("globus", e);
		} catch (CredentialExpiredException e) {
			throw new GATInvocationException("globus", e);
		}

		GramJob j = new GramJob(credential, rsl);
		job.setGramJob(j);
		j.addListener(job);
		try {
			Gram.request(contact, j);
			if (!new java.io.File(".JavaGAT-wrapper-script-" + random).delete()) {
				logger.info("failed to delete the wrapper script: '"
						+ ".JavaGAT-wrapper-script-" + random + "'.");
			}
		} catch (GramException e) {
			throw new GATInvocationException("globus", e); // no idea what went
			// wrong
		} catch (GSSException e2) {
			throw new GATInvocationException("globus",
					new CouldNotInitializeCredentialException("globus", e2));
		}
		job.startPoller();
		return job;
	}

	public Job submitJobGatSandbox(JobDescription description,
			MetricListener listener, String metricDefinitionName)
			throws GATInvocationException {
		if (getBooleanAttribute(description, "wrapper.enable", false)) {
			if (logger.isDebugEnabled()) {
				logger.debug("wrapper enabled: using wrapper application.");
			}
			if (submitter == null) {
				submitter = new WrapperSubmitter(gatContext, brokerURI, false);
			}
			return submitter.submitJob(description);
		}
		// long start = System.currentTimeMillis();
		// choose the first of the set descriptions to retrieve the hostname
		// etc.
		String host = getHostname();

		String contact = brokerURI.toString();
		if (brokerURI.getScheme() != null) {
			contact = contact.replace(brokerURI.getScheme() + "://", "");
		}

		GSSCredential credential = getCredential(host);

		String random = "" + Math.random();
		// if special preference "globus.exitvalue.enable" is set to true,
		// modify the softwaredescription
		if (isExitValueEnabled(description)) {
			description.getSoftwareDescription().toWrapper(gatContext,
					".JavaGAT-wrapper-script-" + random,
					".JavaGAT-exit-value-" + random);
		}

		// code to handle streaming err and out
		SoftwareDescription sd = description.getSoftwareDescription();

		// if output should be streamed, don't poststage it!
		Sandbox sandbox = new Sandbox(gatContext, description, host, null,
				true, true, !sd.stdoutIsStreaming(), !sd.stderrIsStreaming());
		GlobusJob job = new GlobusJob(gatContext, description, sandbox);
		if (isExitValueEnabled(description)) {
			job.setExitValueEnabled(isExitValueEnabled(description),
					".JavaGAT-exit-value-" + random);
		}
		if (listener != null && metricDefinitionName != null) {
			Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
					.createMetric(null);
			job.addMetricListener(listener, metric);
		}

		if (sd.stderrIsStreaming()) {
			job.startOutputForwarder(sandbox.getResolvedStderr(), sd
					.getStderrStream());
		}
		if (sd.stdoutIsStreaming()) {
			job.startOutputForwarder(sandbox.getResolvedStdout(), sd
					.getStdoutStream());
		}

		job.setState(Job.PRE_STAGING);
		try {
			sandbox.prestage();
		} catch (GATInvocationException e) {
			// prestaging fails cleanup before throwing the exception.
			job.setState(Job.POST_STAGING);
			sandbox.retrieveAndCleanup(job);
			job.setState(Job.SUBMISSION_ERROR);
			throw e;
		}
		// after the prestaging we can safely delete the wrapper script if we
		// did create it
		if (isExitValueEnabled(description)) {
			logger.info("deleting '" + ".JavaGAT-wrapper-script-" + random
					+ "'");
			logger.info("file exists: "
					+ new java.io.File(".JavaGAT-wrapper-script-" + random)
							.exists());
			if (!(new java.io.File(".JavaGAT-wrapper-script-" + random)
					.delete())) {
				logger.info("failed to delete the wrapper script: '"
						+ ".JavaGAT-wrapper-script-" + random + "'.");
			}
		}

		// If we staged in the executable, we have to do a chmod.
		// Globus loses the executable bit :-(

		// better use the preference "gridftp.chmod", "..." for creating the
		// file object of the executable

		if (sandbox.getResolvedExecutable() != null) {
			runChmod(credential, description, host, sandbox);
		}

		String rsl = createRSL(description, host, sandbox, null, null);
		GramJob j = new GramJob(credential, rsl);
		job.setGramJob(j);
		j.addListener(job);
		job.startPoller();
		try {
			j.request(contact);
			// Gram.request(contact, j);
		} catch (GramException e) {
			throw new GATInvocationException("globus", e); // no idea what went
			// wrong
		} catch (GSSException e2) {
			throw new GATInvocationException("globus",
					new CouldNotInitializeCredentialException("globus", e2));
		}
		return job;
	}

	public static void end() {
		if (logger.isDebugEnabled()) {
			logger.debug("globus broker adaptor end");
		}

		shutdownInProgress = true;

		try {
			Gram.deactivateAllCallbackHandlers();
		} catch (Throwable t) {
			if (logger.isDebugEnabled()) {
				logger
						.debug("WARNING, globus job could not deactivate callback: "
								+ t);
			}
		}
		WrapperSubmitter.end();
	}
}
