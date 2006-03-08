package org.gridlab.gat.resources.cpi.local;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.IPUtils;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.util.InputForwarder;
import org.gridlab.gat.util.OutputForwarder;
import org.gridlab.gat.util.TimePeriod;

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

	/**
	 * This method constructs a LocalResourceBrokerAdaptor instance
	 * corresponding to the passed GATContext.
	 * 
	 * @param gatContext
	 *            A GATContext which will be used to broker resources
	 */
	public LocalResourceBrokerAdaptor(GATContext gatContext,
			Preferences preferences) throws Exception {
		super(gatContext, preferences);

		checkName("local");
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
	public List findResources(ResourceDescription resourceDescription) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
	 */
	public Job submitJob(JobDescription description)
			throws GATInvocationException, IOException {
		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		preStageFiles(description, "");

		URI location = getLocationURI(description);
		String path = null;

		if (location.getScheme() == null || location.getScheme().equals("file")) {
			path = IPUtils.getPath(location);
		} else {
			throw new GATInvocationException("not a local file, scheme is: "
					+ location.getScheme());
		}

		String command = path + " " + getArguments(description);

		if (GATEngine.VERBOSE) {
			System.err.println("running command: " + command);
		}

		Process p = Runtime.getRuntime().exec(command.toString());

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
						preferences, stdin.toURI());
				OutputStream out = p.getOutputStream();
				InputForwarder input = new InputForwarder(out, fin);
			} catch (GATObjectCreationException e) {
				throw new GATInvocationException("default broker", e);
			}
		}

		// we must always read the output and error streams to avoid deadlocks
		if (stdout == null) {
			OutputForwarder output = new OutputForwarder(p.getInputStream(),
					false); // throw away output
		} else {
			try {
				FileOutputStream out = GAT.createFileOutputStream(gatContext,
						preferences, stdout.toURI());
				OutputForwarder output = new OutputForwarder(
						p.getInputStream(), out);
			} catch (GATObjectCreationException e) {
				throw new GATInvocationException("default broker", e);
			}
		}

		// we must always read the output and error streams to avoid deadlocks
		if (stderr == null) {
			OutputForwarder output = new OutputForwarder(p.getErrorStream(),
					false); // throw away output
		} else {
			try {
				FileOutputStream out = GAT.createFileOutputStream(gatContext,
						preferences, stderr.toURI());
				OutputForwarder output = new OutputForwarder(
						p.getErrorStream(), out);
			} catch (GATObjectCreationException e) {
				throw new GATInvocationException("default broker", e);
			}
		}

		return new LocalJob(this, description, p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#reserveResource(org.gridlab.gat.resources.Resource,
	 *      org.gridlab.gat.util.TimePeriod)
	 */
	public Reservation reserveResource(Resource resource, TimePeriod timePeriod)
			throws RemoteException, IOException {
		throw new UnsupportedOperationException("Not implemented");
	}
}