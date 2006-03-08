package org.gridlab.gat.resources.cpi;

import java.io.IOException;
import java.util.List;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.util.TimePeriod;

/**
 * Capability provider interface to the ResourceBroker class.
 * <p>
 * Capability provider wishing to provide the functionality of the
 * ResourceBroker class must extend this class and implement all of the abstract
 * methods in this class. Each abstract method in this class mirrors the
 * corresponding method in this ResourceBroker class and will be used to
 * implement the corresponding method in the ResourceBroker class at runtime.
 */
public abstract class ResourceBrokerCpi implements ResourceBroker {

	GATContext gatContext;

	Preferences preferences;

	/**
	 * This method constructs a ResourceBrokerCpi instance corresponding to the
	 * passed GATContext.
	 * 
	 * @param gatContext
	 *            A GATContext which will be used to broker resources
	 * @param preferences the preferences to be associated with this resource broker
	 * @throws AdaptorCreationException no adaptor could be loaded
	 */
	protected ResourceBrokerCpi(GATContext gatContext, Preferences preferences)
			throws AdaptorCreationException {
		this.gatContext = gatContext;
		this.preferences = preferences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#findResources(org.gridlab.gat.resources.ResourceDescription)
	 */
	public List findResources(ResourceDescription resourceDescription)
			throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#reserveResource(org.gridlab.gat.resources.Resource,
	 *      org.gridlab.gat.util.TimePeriod)
	 */
	public Reservation reserveResource(Resource resource, TimePeriod timePeriod)
			throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#reserveResource(org.gridlab.gat.resources.ResourceDescription,
	 *      org.gridlab.gat.util.TimePeriod)
	 */
	public Reservation reserveResource(ResourceDescription resourceDescription,
			TimePeriod timePeriod) throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
	 */
	public Job submitJob(JobDescription description)
			throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	// utility methods
	protected String getLocation(JobDescription description)
			throws GATInvocationException {

		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		String location = sd.getLocation().toString();
		if (location == null) {
			throw new GATInvocationException(
					"The Job description does not contain a location");
		}
		return location;
	}

	protected String[] getArgumentsArray(JobDescription description)
			throws GATInvocationException {
		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		return sd.getArguments();
	}

	protected String getArguments(JobDescription description)
			throws GATInvocationException {

		String[] arguments = getArgumentsArray(description);
		String argString = "";

		if (arguments == null) {
			return "";
		}

		for (int i = 0; i < arguments.length; i++) {
			argString += " " + arguments[i];
		}

		return argString;
	}

	protected void checkName(String adaptor) throws AdaptorCreationException {
		String name = (String) preferences.get("resources.adaptor.name");
		if (name != null && !name.equals(adaptor)) {
			throw new AdaptorCreationException("this adaptor (" + adaptor
					+ ") was not selected by the user.");
		}
	}
}