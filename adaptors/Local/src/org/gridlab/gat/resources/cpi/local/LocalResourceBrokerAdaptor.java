package org.gridlab.gat.resources.cpi.local;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.CommandNotFoundException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.CommandRunner;
import org.gridlab.gat.engine.util.InputForwarder;
import org.gridlab.gat.engine.util.OutputForwarder;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * An instance of this class is used to reserve resources.
 * <p>
 * A resource can either be a hardware resource or a software resource. A
 * software resource is simply an executable it makes little sense to reserve
 * such. Thus an instance of this class can currently only reserve a hardware
 * resource.
 * <p>
 * If one wishes to reserve a hardware resource, one must first describe the
 * hardware resource that one wishes to reserve. This is accomplished by
 * creating an instance of the class HardwareResourceDescription which describes
 * the hardware resource that one wishes to reserve. After creating such an
 * instance of the class HardwareResourceDescription that describes the hardware
 * resource one wishes to reserve, one must specify the time period for which
 * one wishes to reserve the hardware resource. This is accomplished by creating
 * an instance of the class TimePeriod which specifies the time period for which
 * one wishes to reserve the hardware resource. Finally, one must obtain a
 * reservation for the desired hardware resource for the desired time period.
 * This is accomplished by calling the method ReserveHardwareResource() on an
 * instance of the class LocalResourceBrokerAdaptor with the appropriate
 * instance of HardwareResourceDescription and the appropriate instance of
 * TimePeriod.
 * <p>
 * In addition an instance of this class can be used to find hardware resources.
 * This is accomplished using the method FindHardwareResources(). This is
 * accomplished by creating an instance of the class HardwareResourceDescription
 * which describes the hardware resource that one wishes to find. After creating
 * such an instance of the class HardwareResourceDescription that describes the
 * hardware resource one wishes to find, one must find the corresponding
 * hardware resource. This is accomplished by calling the method
 * FindHardwareResources() on an instance of the class
 * LocalResourceBrokerAdaptor with the appropriate instance of
 * HardwareResourceDescription.
 */
public class LocalResourceBrokerAdaptor extends ResourceBrokerCpi {

	protected static Logger logger = Logger
			.getLogger(LocalResourceBrokerAdaptor.class);

	/**
	 * This method constructs a LocalResourceBrokerAdaptor instance
	 * corresponding to the passed GATContext.
	 * 
	 * @param gatContext
	 *            A GATContext which will be used to broker resources
	 */
	public LocalResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
			throws GATObjectCreationException {
		super(gatContext, brokerURI);

		// the brokerURI should point to the local host else throw exception
		if (!brokerURI.refersToLocalHost()) {
			throw new GATObjectCreationException(
					"The LocalResourceBrokerAdaptor doesn't refer to localhost, but to a remote host: "
							+ brokerURI.toString());
		}
	}

	/**
	 * This method attempts to reserve the specified hardware resource for the
	 * specified time period. Upon reserving the specified hardware resource
	 * this method returns a Reservation. Upon failing to reserve the specified
	 * hardware resource this method returns an error.
	 * 
	 * @param resourceDescription
	 *            A description, a HardwareResourceDescription, of the hardware
	 *            resource to reserve
	 * @param timePeriod
	 *            The time period, a TimePeriod , for which to reserve the
	 *            hardware resource
	 */
	public Reservation reserveResource(ResourceDescription resourceDescription,
			TimePeriod timePeriod) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * This method attempts to find one or more matching hardware resources.
	 * Upon finding the specified hardware resource(s) this method returns a
	 * java.util.List of HardwareResource instances. Upon failing to find the
	 * specified hardware resource this method returns an error.
	 * 
	 * @param resourceDescription
	 *            A description, a HardwareResoucreDescription, of the hardware
	 *            resource(s) to find
	 * @return java.util.List of HardwareResources upon success
	 */
	public List<HardwareResource> findResources(
			ResourceDescription resourceDescription) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
	 */
	public Job submitJob(JobDescription description, MetricListener listener,
			String metricDefinitionName) throws GATInvocationException {
		SoftwareDescription sd = description.getSoftwareDescription();

		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		// fill in the environment
		String[] environment = null;
		Map<String, Object> env = sd.getEnvironment();
		int index = 0;
		if (env != null) {
			environment = new String[env.size()];
			Set<String> keys = env.keySet();
			Iterator<String> i = keys.iterator();
			while (i.hasNext()) {
				String key = (String) i.next();
				String val = (String) env.get(key);
				environment[index] = key + "=" + val;
				index++;
			}
		}

		String home = System.getProperty("user.home");
		if (home == null) {
			throw new GATInvocationException(
					"local broker could not get user home dir");
		}

		Sandbox sandbox = new Sandbox(gatContext, description, "localhost",
				home, true, true, true, true);

		LocalJob job = new LocalJob(gatContext, description, sandbox);
		if (listener != null && metricDefinitionName != null) {
			Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
					.createMetric(null);
			job.addMetricListener(listener, metric);
		}
		job.setState(Job.PRE_STAGING);
		sandbox.prestage();

		String exe;
		if (sandbox.getResolvedExecutable() != null) {
			exe = sandbox.getResolvedExecutable().getPath();
		} else {
			exe = getExecutable(description);
		}

		// try to set the executable bit, it might be lost
		try {
			new CommandRunner("/bin/chmod +x " + exe);
		} catch (Throwable t) {
			// ignore
		}
		try {
			new CommandRunner("/usr/bin/chmod +x " + exe);
		} catch (Throwable t) {
			// ignore
		}

		String command = exe + " " + getArguments(description);

		java.io.File f = new java.io.File(sandbox.getSandbox());

		if (logger.isInfoEnabled()) {
			logger.info("running command: " + command);

			if (environment != null) {
				logger.info("  environment:");
				for (int i = 0; i < environment.length; i++) {
					logger.info("    " + environment[i]);
				}
			}

			if (home != null) {
				logger.info("working dir is: " + f.getPath());
			}
		}

		Process p = null;
		try {
			p = Runtime.getRuntime().exec(command, environment, f);
			job.setState(Job.RUNNING);
			job.setProcess(p);
			job.setSubmissionTime();
			job.setStartTime();
		} catch (IOException e) {
			throw new CommandNotFoundException("LocalResourceBrokerAdaptor", e);
		}

		org.gridlab.gat.io.File stdin = sandbox.getResolvedStdin();

		if (stdin == null) {
			// close stdin.
			try {
				p.getOutputStream().close();
			} catch (Throwable e) {
				// ignore
			}
		} else {
			try {
				java.io.FileInputStream fin = new java.io.FileInputStream(stdin
						.getAbsolutePath());
				OutputStream out = p.getOutputStream();
				new InputForwarder(out, fin);
			} catch (Exception e) {
				throw new GATInvocationException("local broker", e);
			}
		}

		OutputStream userOut;
		OutputStream userErr;
		if (sd.stdoutIsStreaming()) {
			userOut = sd.getStdoutStream();
		} else {
			try {
				userOut = GAT.createFileOutputStream(sandbox
						.getResolvedStdout());
			} catch (GATObjectCreationException e) {
				throw new GATInvocationException(
						"failed to create outputstream to write in output file: '"
								+ sd.getStdout() + "'", e);
			}
		}
		if (sd.stderrIsStreaming()) {
			userErr = sd.getStderrStream();
		} else {
			try {
				userErr = GAT.createFileOutputStream(sandbox
						.getResolvedStderr());
			} catch (GATObjectCreationException e) {
				throw new GATInvocationException(
						"failed to create outputstream to write in error file: '"
								+ sd.getStderr() + "'", e);
			}
		}
		OutputForwarder outForwarder = new OutputForwarder(p.getInputStream(),
				userOut);
		OutputForwarder errForwarder = new OutputForwarder(p.getErrorStream(),
				userErr);
		job.startOutputWaiter(outForwarder, errForwarder);

		return job;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#reserveResource(org.gridlab.gat.resources.Resource,
	 *      org.gridlab.gat.engine.util.TimePeriod)
	 */
	public Reservation reserveResource(Resource resource, TimePeriod timePeriod) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
