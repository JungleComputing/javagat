/*
 * Created on Oct 14, 2004
 */
package org.gridlab.gat.resources.cpi.globus;

import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

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
import org.gridlab.gat.Preferences;
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
import org.gridlab.gat.resources.cpi.RemoteSandboxSubmitter;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
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

	private RemoteSandboxSubmitter submitter;

	public static void init() {
		GATEngine.registerUnmarshaller(GlobusJob.class);
	}

	public GlobusResourceBrokerAdaptor(GATContext gatContext,
			Preferences preferences) throws GATObjectCreationException {
		super(gatContext, preferences);
	}

	public void beginMultiJob() throws GATInvocationException {
		if (submitter != null && submitter.isMulticore()) {
			throw new GATInvocationException("MultiCore job started twice!");
		}
		submitter = new RemoteSandboxSubmitter(gatContext, preferences, true);
	}

	public Job endMultiJob() throws GATInvocationException {
		if (submitter == null) {
			throw new GATInvocationException(
					"MultiCore job ended, without being started!");
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

		if (isJavaApplication(description)) {
			URI javaHome = (URI) sd.getAttributes().get("java.home");
			if (javaHome == null) {
				throw new GATInvocationException("java.home not set");
			}

			rsl += "& (executable = " + javaHome.getPath() + "/bin/java)";

			String javaFlags = getStringAttribute(description, "java.flags", "");
			if (javaFlags.length() != 0) {
				StringTokenizer t = new StringTokenizer(javaFlags);
				while (t.hasMoreTokens()) {
					args += " \"" + t.nextToken() + "\"";
				}
			}

			// classpath
			String javaClassPath = getStringAttribute(description,
					"java.classpath", "");
			if (javaClassPath.length() != 0) {
				args += " \"-classpath\" \"" + javaClassPath + "\"";
			} else {
				// TODO if not set, use jar files in prestaged set
			}

			// set the environment
			Map<String, Object> env = sd.getEnvironment();
			if (env != null && !env.isEmpty()) {
				Set<String> s = env.keySet();
				Object[] keys = (Object[]) s.toArray();

				for (int i = 0; i < keys.length; i++) {
					String val = (String) env.get(keys[i]);
					args += " \"-D" + keys[i] + "=" + val + "\"";
				}
			}

			// main class name
			args += " \"" + getLocationURI(description).getSchemeSpecificPart()
					+ "\"";
		} else {
			String exe = getLocationURI(description).getPath();
			rsl += "& (executable = " + exe + ")";
		}

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

		String jobType = getStringAttribute(description, "jobType", null);
		if (jobType != null) {
			rsl += " (jobType = " + jobType + ")";
		}

		if (sandbox != null) {
			rsl += " (directory = " + sandbox.getSandbox() + ")";
		}

		long maxTime = getLongAttribute(description, "maxTime", -1);
		if (maxTime > 0) {
			rsl += " (maxTime = " + maxTime + ")";
		}

		long maxWallTime = getLongAttribute(description, "maxWallTime", -1);
		if (maxWallTime > 0) {
			rsl += " (maxWallTime = " + maxWallTime + ")";
		}

		long maxCPUTime = getLongAttribute(description, "maxCPUTime", -1);
		if (maxCPUTime > 0) {
			rsl += " (maxCPUTime = " + maxCPUTime + ")";
		}

		// stage in files with gram
		if (pre != null) {
			for (int i = 0; i < pre.size(); i++) {
				PreStagedFile f = pre.getFile(i);

				if (!f.getResolvedSrc().toGATURI().refersToLocalHost()) {
					throw new GATInvocationException(
							"Currently, we cannot stage in remote files with gram");
				}

				String s = "(file_stage_in = (file:///"
						+ f.getResolvedSrc().getPath() + " "
						+ f.getResolvedDest().getPath() + "))";
				rsl += s;
			}
		}

		if (post != null) {
			for (int i = 0; i < post.size(); i++) {
				PostStagedFile f = post.getFile(i);

				if (!f.getResolvedDest().toGATURI().refersToLocalHost()) {
					throw new GATInvocationException(
							"Currently, we cannot stage out remote files with gram");
				}

				String s = "(file_stage_out = (" + f.getResolvedSrc().getPath()
						+ " gsiftp://" + GATEngine.getLocalHostName() + "/"
						+ f.getResolvedDest().getPath() + "))";
				rsl += s;
			}
		}

		org.gridlab.gat.io.File stdout = sd.getStdout();
		if (stdout != null) {
			if (sandbox != null) {
				rsl += (" (stdout = " + sandbox.getRelativeStdout().getPath() + ")");
			}
		}

		org.gridlab.gat.io.File stderr = sd.getStderr();
		if (stderr != null) {
			if (sandbox != null) {
				rsl += (" (stderr = " + sandbox.getRelativeStderr().getPath() + ")");
			}
		}

		org.gridlab.gat.io.File stdin = sd.getStdin();
		if (stdin != null) {
			if (sandbox != null) {
				rsl += (" (stdin = " + sandbox.getRelativeStdin().getPath() + ")");
			}
		}

		if (!isJavaApplication(description)) {
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
		}

		String queue = getStringAttribute(description, "queue", null);
		if (queue != null) {
			rsl += " (queue = " + queue + ")";
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

	protected String getResourceManagerContact(JobDescription description)
			throws GATInvocationException {
		String res = null;
		String contact = (String) preferences
				.get("ResourceBroker.jobmanagerContact");
		String jobManager = (String) preferences
				.get("ResourceBroker.jobmanager");
		Object jobManagerPort = preferences
				.get("ResourceBroker.jobmanagerPort");

		// if the contact string is set, ignore all other properties
		if (contact != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Resource manager contact = " + contact);
			}
			return contact;
		}

		String hostname = getHostname(description);

		if (hostname != null) {
			res = hostname;

			if (jobManagerPort != null) {
				res += (":" + jobManagerPort);
			}

			if (jobManager != null) {
				res += ("/jobmanager-" + jobManager);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Resource manager contact = " + res);
			}

			return res;
		}

		throw new GATInvocationException(
				"The Globus resource broker needs a hostname");
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
			Metric metric) throws GATInvocationException {
		boolean useGramSandbox = false;
		String s = (String) preferences.get("useGramSandbox");
		if (s != null && s.equalsIgnoreCase("true")) {
			useGramSandbox = true;
		}

		if (useGramSandbox) {
			return submitJobGramSandbox(description, listener, metric);
		} else {
			return submitJobGatSandbox(description, listener, metric);
		}
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
					preferences, "gram", hostUri,
					ResourceManagerContact.DEFAULT_PORT);
		} catch (CouldNotInitializeCredentialException e) {
			throw new GATInvocationException("globus", e);
		} catch (CredentialExpiredException e) {
			throw new GATInvocationException("globus", e);
		}
		return credential;
	}

	private void runChmod(GSSCredential credential, JobDescription description,
			String host, Sandbox sandbox) {

		String exe = null;

		try {
			exe = getLocationURI(description).getPath();
		} catch (Exception e) {
			return;
		}

		try {
			submitChmodJob(credential, description, host, "/bin/chmod",
					sandbox, exe);
		} catch (Exception e) {
			// ignore
		}
		try {
			submitChmodJob(credential, description, host, "/usr/bin/chmod",
					sandbox, exe);
		} catch (Exception e) {
			// ignore
		}
	}

	public Job submitJobGramSandbox(JobDescription description, MetricListener listener,
			Metric metric)
			throws GATInvocationException {
		long start = System.currentTimeMillis();
		String host = getHostname(description);
		String contact = getResourceManagerContact(description);

		URI hostUri;
		try {
			hostUri = new URI(host);
		} catch (Exception e) {
			throw new GATInvocationException("globus broker", e);
		}

		GSSCredential credential = null;
		try {
			credential = GlobusSecurityUtils.getGlobusCredential(gatContext,
					preferences, "gram", hostUri,
					ResourceManagerContact.DEFAULT_PORT);
		} catch (CouldNotInitializeCredentialException e) {
			throw new GATInvocationException("globus", e);
		} catch (CredentialExpiredException e) {
			throw new GATInvocationException("globus", e);
		}

		PreStagedFileSet pre = new PreStagedFileSet(gatContext, preferences,
				description, host, null, false);

		PostStagedFileSet post = new PostStagedFileSet(gatContext, preferences,
				description, host, null, false, false);

		String rsl = createRSL(description, host, null, pre, post);
		GramJob j = new GramJob(credential, rsl);
		GlobusJob res = new GlobusJob(gatContext, preferences, this,
				description, j, null, start, listener, metric);
		j.addListener(res);

		try {
			Gram.request(contact, j);
		} catch (GramException e) {
			throw new GATInvocationException("globus", e); // no idea what went
			// wrong
		} catch (GSSException e2) {
			throw new GATInvocationException("globus",
					new CouldNotInitializeCredentialException("globus", e2));
		}

		return res;
	}

	public Job submitJobGatSandbox(JobDescription description, MetricListener listener,
			Metric metric)
			throws GATInvocationException {
		if (getBooleanAttribute(description, "useRemoteSandbox", false)) {
			if (logger.isDebugEnabled()) {
				logger.debug("useRemoteSandbox, using wrapper application");
			}
			if (submitter == null) {
				submitter = new RemoteSandboxSubmitter(gatContext, preferences,
						false);
			}
			return submitter.submitJob(description, listener, metric);
		}
		long start = System.currentTimeMillis();
		// choose the first of the set descriptions to retrieve the hostname
		// etc.
		String host = getHostname(description);
		String contact = getResourceManagerContact(description);
		GSSCredential credential = getCredential(host);

		Sandbox sandbox = new Sandbox(gatContext, preferences, description,
				host, null, true, true, true, true);

		// If we staged in the executable, we have to do a chmod.
		// Globus loses the executable bit :-(
		if (sandbox.getResolvedExecutable() != null) {
			runChmod(credential, description, host, sandbox);
		}

		String rsl = createRSL(description, host, sandbox, null, null);
		GramJob j = new GramJob(credential, rsl);
		GlobusJob res = new GlobusJob(gatContext, preferences, this,
				description, j, sandbox, start, listener, metric);
		j.addListener(res);

		try {
			Gram.request(contact, j);
		} catch (GramException e) {
			throw new GATInvocationException("globus", e); // no idea what went
			// wrong
		} catch (GSSException e2) {
			throw new GATInvocationException("globus",
					new CouldNotInitializeCredentialException("globus", e2));
		}

		return res;
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
		RemoteSandboxSubmitter.end();
	}
}
