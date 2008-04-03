package org.gridlab.gat.resources.cpi.localQ;

import java.util.List;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.MethodNotApplicableException;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
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
public class LocalQResourceBrokerAdaptor extends ResourceBrokerCpi implements
		Runnable {

	protected static Logger logger = Logger
			.getLogger(LocalQResourceBrokerAdaptor.class);

	private static boolean ended = false;

	private static synchronized boolean hasEnded() {
		return ended;
	}

	// called by the gat engine
	public static synchronized void end() {
		ended = true;
	}

	private final PriorityQueue<LocalQJob> queue;

	/**
	 * This method constructs a LocalResourceBrokerAdaptor instance
	 * corresponding to the passed GATContext.
	 * 
	 * @param gatContext
	 *            A GATContext which will be used to broker resources
	 */
	public LocalQResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
			throws GATObjectCreationException {
		super(gatContext, brokerURI);

		queue = new PriorityQueue<LocalQJob>();

		Integer maxConcurrentJobs = (Integer) gatContext.getPreferences().get(
				"maxConcurrentJobs");

		if (maxConcurrentJobs == null) {
			maxConcurrentJobs = Runtime.getRuntime().availableProcessors() + 1;
		}

		if (maxConcurrentJobs <= 0) {
			throw new GATObjectCreationException(
					"cannot create local Q resource broker with "
							+ maxConcurrentJobs + " concurrent jobs");
		}

		for (int i = 0; i < maxConcurrentJobs; i++) {
			Thread thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
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
		long start = System.currentTimeMillis();
		SoftwareDescription sd = description.getSoftwareDescription();

		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		String host = getHostname();
		if (host != null) {
			if (!host.equals("localhost")
					&& !host.equals(GATEngine.getLocalHostName())) {
				throw new MethodNotApplicableException(
						"cannot run jobs on remote machines with the local adaptor");
			}
		}

		String home = System.getProperty("user.home");
		if (home == null) {
			throw new GATInvocationException(
					"local broker could not get user home dir");
		}

		Sandbox sandbox = new Sandbox(gatContext, description, "localhost",
				home, true, true, true, true);

		LocalQJob result = new LocalQJob(gatContext, this, description,
				sandbox, start);

		// add to queue
		synchronized (this) {
			queue.add(result);
			notifyAll();
		}

		return result;
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

	private synchronized LocalQJob getJob() {
		while (!hasEnded()) {
			LocalQJob result = queue.poll();

			if (result != null) {
				return result;
			}

			try {
				wait(100);
			} catch (InterruptedException e) {
				// IGNORE
			}
		}
		return null;
	}

	public void run() {
		while (true) {
			LocalQJob next = getJob();

			if (next == null) {
				// resource broker has ended, exit
				return;
			}

			try {
				next.run();
			} catch (Throwable t) {
				logger.error("error while running job: " + t);
			}
		}
	}
}
