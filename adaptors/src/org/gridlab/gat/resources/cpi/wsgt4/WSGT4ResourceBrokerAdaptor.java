package org.gridlab.gat.resources.cpi.wsgt4;

import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.globus.exec.generated.JobDescriptionType;
import org.globus.exec.utils.rsl.RSLHelper;
import org.globus.exec.utils.rsl.RSLParseException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.RemoteSandboxSubmitter;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;

/**
 * Implements the <code>ResourceBrokerCpi</code> abstract class.
 * 
 * @author Balazs Bokodi
 * @version 1.0
 * @since 1.0
 */
public class WSGT4ResourceBrokerAdaptor extends ResourceBrokerCpi {
	protected static Logger logger = Logger
			.getLogger(WSGT4ResourceBrokerAdaptor.class);

	private RemoteSandboxSubmitter submitter;
	static final int DEFAULT_GRIDFTP_PORT = 2811;

	protected GSSCredential getCred(JobDescription jobDescription)
			throws GATInvocationException {
		GSSCredential cred = null;
		URI location = null;
		try {
			location = new URI(getHostname(jobDescription));
		} catch (Exception e) {
			throw new GATInvocationException(
					"WSGT4Job: getSecurityContext, initialization of location failed, "
							+ e);
		}
		try {
			cred = GlobusSecurityUtils.getGlobusCredential(gatContext,
					preferences, "globus", location, DEFAULT_GRIDFTP_PORT);
		} catch (Exception e) {
			throw new GATInvocationException(
					"WSGT4Job: could not initialize credentials, " + e);
		}
		return cred;
	}

	public WSGT4ResourceBrokerAdaptor(GATContext gatContext,
			Preferences preferences) throws GATObjectCreationException {
		super(gatContext, preferences);
		String globusLocation = System.getenv("GLOBUS_LOCATION");
		if (globusLocation == null) {
			throw new GATObjectCreationException("$GLOBUS_LOCATION is not set");
		}
		System.setProperty("GLOBUS_LOCATION", globusLocation);
		System.setProperty("axis.ClientConfigFile", globusLocation
				+ "/client-config.wsdd");
	}

	protected String createRSL(JobDescription description, Sandbox sandbox)
			throws GATInvocationException {
		String rsl = new String("<job>");
		SoftwareDescription sd = description.getSoftwareDescription();

		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		if (isJavaApplication(description)) {
			URI javaHome = (URI) sd.getAttributes().get("java.home");
			if (javaHome == null) {
				throw new GATInvocationException("java.home not set");
			}

			rsl += "<executable>" + javaHome.getPath() + "/bin/java</executable>";

			String javaFlags = getStringAttribute(description, "java.flags", "");
			if (javaFlags.length() != 0) {
				StringTokenizer t = new StringTokenizer(javaFlags);
				while (t.hasMoreTokens()) {
					rsl += "<argument>" + t.nextToken() + "</argument>";
				}
			}

			// classpath
			String javaClassPath = getStringAttribute(description,
					"java.classpath", "");
			if (javaClassPath.length() != 0) {
				rsl += "<argument>-classpath</argument>";
				rsl += "<argument>" + javaClassPath + "</argument>";
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
					rsl += "<argument>-D" + keys[i] + "=" + val + "</argument>";
				}
			}

			// main class name
			rsl += "<argument>" + getLocationURI(description).getSchemeSpecificPart()
					+ "</argument>";
		} else {
			rsl += "<executable>";
			rsl += getLocationURI(description).getPath();
			rsl += "</executable>";
			Map<String, Object> env = sd.getEnvironment();
			if (env != null && !env.isEmpty()) {
				Set<String> s = env.keySet();
				Object[] keys = (Object[]) s.toArray();

				for (int i = 0; i < keys.length; i++) {
					String val = (String) env.get(keys[i]);
					rsl += "<environment>";
					rsl += "<name>" + keys[i] + "</name>";
					rsl += "<value>" + val + "</value>";
					rsl += "</environment>";
				}
			}
		}
		
		String[] argsA = getArgumentsArray(description);

		if (argsA != null) {
			for (int i = 0; i < argsA.length; i++) {
				rsl += "<argument>";
				rsl += argsA[i];
				rsl += "</argument>";
			}
		}

		// set the environment
		rsl += "<count>";
		rsl += getCPUCount(description);
		rsl += "</count>";
		rsl += "<directory>";
		rsl += sandbox.getSandbox();
		rsl += "</directory>";

		org.gridlab.gat.io.File stdout = sd.getStdout();
		if (stdout != null) {
			rsl += "<stdout>";
			rsl += sandbox.getRelativeStdout().getPath();
			rsl += "</stdout>";
		}

		org.gridlab.gat.io.File stderr = sd.getStderr();
		if (stderr != null) {
			rsl += "<stderr>";
			rsl += sandbox.getRelativeStderr().getPath();
			rsl += "</stderr>";
		}

		org.gridlab.gat.io.File stdin = sd.getStdin();
		if (stdin != null) {
			rsl += "<stdin>";
			rsl += sandbox.getRelativeStdin().getPath();
			rsl += "</stdin>";
		}

		if (logger.isInfoEnabled()) {
			logger.info("RSL: " + rsl);
		}

		rsl += "</job>";
		return rsl;
	}

	public void beginMultiCoreJob() {
		submitter = new RemoteSandboxSubmitter(gatContext, preferences, true);
	}

	public void endMultiCoreJob() throws GATInvocationException {
		submitter.flushJobSubmission();
		submitter = null;
	}

	public Job submitJob(JobDescription description, MetricListener listener,
			Metric metric) throws GATInvocationException {
		if (getBooleanAttribute(description, "useLocalDisk", false)) {
			if (logger.isDebugEnabled()) {
				logger.debug("useLocalDisk, using wrapper application");
			}
			if (submitter == null) {
				submitter = new RemoteSandboxSubmitter(gatContext, preferences,
						false);
			}
			return submitter.submitJob(description, listener, metric);
		}
		String host = getHostname(description);
		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null) {
			throw new GATInvocationException(
					"WSGT4ResourceBroker: the job description does not contain a software description");
		}
		Sandbox sandbox = new Sandbox(gatContext, preferences, description,
				host, null, true, true, true, true);

		String rsl = createRSL(description, sandbox);
		JobDescriptionType gjobDescription = null;
		try {
			gjobDescription = RSLHelper.readRSL(rsl);
		} catch (RSLParseException e) {
			throw new GATInvocationException("WSGT4ResourceBroker: " + e);
		}
		GSSCredential cred = getCred(description);

		return new WSGT4Job(gatContext, preferences, description, sandbox,
				gjobDescription, getHostname(description), cred, listener,
				metric);
	}
}
