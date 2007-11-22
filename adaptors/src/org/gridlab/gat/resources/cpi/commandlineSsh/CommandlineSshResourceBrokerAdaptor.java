package org.gridlab.gat.resources.cpi.commandlineSsh;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.CommandNotFoundException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.MethodNotApplicableException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.InputForwarder;
import org.gridlab.gat.engine.util.OutputForwarder;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.io.cpi.ssh.SSHSecurityUtils;
import org.gridlab.gat.io.cpi.ssh.SshUserInfo;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

public class CommandlineSshResourceBrokerAdaptor extends ResourceBrokerCpi {

	protected static Logger logger = Logger
			.getLogger(CommandlineSshResourceBrokerAdaptor.class);

	public static final int SSH_PORT = 22;

	private SshUserInfo sui;

	private boolean windows = false;

	/**
	 * This method constructs a CommandlineSshResourceBrokerAdaptor instance
	 * corresponding to the passed GATContext.
	 * 
	 * @param gatContext
	 *            A GATContext which will be used to broker resources
	 */
	public CommandlineSshResourceBrokerAdaptor(GATContext gatContext,
			Preferences preferences) throws GATObjectCreationException {
		super(gatContext, preferences);

		String osname = System.getProperty("os.name");
		if (osname.startsWith("Windows"))
			windows = true;
	}

	
	public Job submitJob(JobDescription description, MetricListener listener,
			String metricDefinitionName) throws GATInvocationException {
		SoftwareDescription sd = description.getSoftwareDescription();

		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		// we do not support environment yet
		Map<String, Object> env = sd.getEnvironment();
		if (env != null && !env.isEmpty()) {
			throw new MethodNotApplicableException("cannot handle environment");
		}

		URI location = getLocationURI(description);
		String path = null;

		path = location.getPath();

		String host = getHostname(description);
		if (host == null) {
			host = "localhost";
		}

		try {
			sui = SSHSecurityUtils.getSshCredential(gatContext, preferences,
					"ssh", location, SSH_PORT);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("SshFileAdaptor: failed to retrieve credentials"
						+ e);
			}
		}

		if (sui == null) {
			throw new GATInvocationException(
					"Unable to retrieve user info for authentication");
		}

		if (sui.privateKeyfile != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("key file argument not supported yet");
			}
		}

		// to be modified, this part goes inside the SSHSecurityUtils
		if (location.getUserInfo() != null) {
			sui.username = location.getUserInfo();
		}

		/* allow port override */
		int port = location.getPort();
		/* it will always return -1 for user@host:path */
		if (port == -1) {
			port = SSH_PORT;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Prepared session for location " + location
					+ " with username: " + sui.username + "; host: " + host);
		}

		Sandbox sandbox = new Sandbox(gatContext, preferences, description,
				host, null, true, false, false, false);

		String command = null;
		if (windows) {
			command = "sexec " + sui.username + "@" + host + " -unat=yes -cmd="
					+ path + " " + getArguments(description);
			if (sui.getPassword() == null) { // public/private key
				int slot = sui.getPrivateKeySlot();
				if (slot == -1) { // not set by the user, assume he only has
					// one key
					slot = 0;
				}
				command += " -pk=" + slot;
			} else { // password
				command += " -pw=" + sui.getPassword();
			}
		} else {
			// we must use the -t option to ssh (allocates pseudo TTY).
			// If we don't, there is no way to kill the remote process.
			command = "ssh -o BatchMode=yes -o StrictHostKeyChecking=yes -t -t "
					+ host + " " + path + " " + getArguments(description);
		}

		if (logger.isInfoEnabled()) {
			logger.info("running command: " + command);
		}

		Process p = null;
		try {
			p = Runtime.getRuntime().exec(command.toString());
		} catch (IOException e) {
			throw new CommandNotFoundException("CommandlineSshResourceBrokerAdaptor", e);
		}

		org.gridlab.gat.io.File stdin = sd.getStdin();
		org.gridlab.gat.io.File stdout = sd.getStdout();
		org.gridlab.gat.io.File stderr = sd.getStderr();

		if (stdin == null) {
			// close stdin.
			try {
				p.getOutputStream().close();
			} catch (Throwable e) {
				// ignore
			}
		} else {
			try {
				FileInputStream fin = GAT.createFileInputStream(gatContext,
						preferences, stdin.toGATURI());
				OutputStream out = p.getOutputStream();
				new InputForwarder(out, fin);
			} catch (GATObjectCreationException e) {
				throw new GATInvocationException("commandlineSsh broker", e);
			}
		}

		OutputForwarder outForwarder = null;

		// we must always read the output and error streams to avoid deadlocks
		if (stdout == null) {
			new OutputForwarder(p.getInputStream(), false); // throw away output
		} else {
			try {
				FileOutputStream out = GAT.createFileOutputStream(gatContext,
						preferences, stdout.toGATURI());
				outForwarder = new OutputForwarder(p.getInputStream(), out);
			} catch (GATObjectCreationException e) {
				throw new GATInvocationException("commandlineSsh broker", e);
			}
		}

		OutputForwarder errForwarder = null;

		// we must always read the output and error streams to avoid deadlocks
		if (stderr == null) {
			new OutputForwarder(p.getErrorStream(), false); // throw away output
		} else {
			try {
				FileOutputStream out = GAT.createFileOutputStream(gatContext,
						preferences, stderr.toGATURI());
				errForwarder = new OutputForwarder(p.getErrorStream(), out);
			} catch (GATObjectCreationException e) {
				throw new GATInvocationException("commandlineSsh broker", e);
			}
		}

		return new CommandlineSshJob(gatContext, preferences, this,
				description, p, sandbox, outForwarder, errForwarder);
	}
	
}
