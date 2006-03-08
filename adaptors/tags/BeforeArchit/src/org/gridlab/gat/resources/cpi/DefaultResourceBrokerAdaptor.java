package org.gridlab.gat.resources.cpi;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.net.RemoteException;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
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
 * instance of the class DefaultResourceBrokerAdaptor with the appropriate
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
 * DefaultResourceBrokerAdaptor with the appropriate instance of
 * HardwareResourceDescription.
 */
public class DefaultResourceBrokerAdaptor extends ResourceBrokerCpi {

	/**
	 * This method constructs a DefaultResourceBrokerAdaptor instance
	 * corresponding to the passed GATContext.
	 * 
	 * @param gatContext
	 *            A GATContext which will be used to broker resources
	 */
	public DefaultResourceBrokerAdaptor(GATContext gatContext,
			Preferences preferences) throws Exception {
		super(gatContext, preferences);
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
		DefaultHardwareResource dhr = new DefaultHardwareResource(null, null);
		HardwareResourceDescription hrd = (HardwareResourceDescription) dhr
				.getResourceDescription();
		Map defaultMap = hrd.getDescription();
		Set defaultSet = defaultMap.entrySet();

		Map passedMap = resourceDescription.getDescription();
		Set passedSet = passedMap.entrySet();

		if (defaultSet.containsAll(passedSet)) {
			return new DefaultReservation();
		}

		return null;
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
		Vector vector = new Vector();

		DefaultHardwareResource dhr = new DefaultHardwareResource(null, null);
		HardwareResourceDescription hrd = (HardwareResourceDescription) dhr
				.getResourceDescription();
		Map defaultMap = hrd.getDescription();
		Set defaultSet = defaultMap.entrySet();

		Map passedMap = resourceDescription.getDescription();
		Set passedSet = passedMap.entrySet();

		if (defaultSet.containsAll(passedSet)) {
			vector.add(new DefaultHardwareResource(null, null));
		}

		return vector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
	 */
	public Job submitJob(JobDescription description) throws RemoteException,
			IOException {
		throw new UnsupportedOperationException("Not implemented");
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