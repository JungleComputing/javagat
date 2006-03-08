package org.gridlab.gat.resources.cpi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.IPUtils;
import org.gridlab.gat.io.File;
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

	protected GATContext gatContext;

	protected Preferences preferences;

	/**
	 * This method constructs a ResourceBrokerCpi instance corresponding to the
	 * passed GATContext.
	 * 
	 * @param gatContext
	 *            A GATContext which will be used to broker resources
	 * @param preferences
	 *            the preferences to be associated with this resource broker
	 * @throws AdaptorCreationException
	 *             no adaptor could be loaded
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
	protected URI getLocationURI(JobDescription description)
			throws GATInvocationException {

		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		URI u = sd.getLocation();

		if (u == null) {
			throw new GATInvocationException(
					"The Job description does not contain a location");
		}

		return u;
	}

	protected String getLocation(JobDescription description)
			throws GATInvocationException {

		URI u = getLocationURI(description);

		if (u == null) {
			throw new GATInvocationException(
					"The Job description does not contain a location");
		}

		String location = u.toString();
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

	protected void preStageFiles(JobDescription description, String host)
			throws GATInvocationException {
		Vector v = new Vector();
		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		/*
		 * Do not add exe, if the user wants that, (s)he should prestage it.
		 * 
		 * File exe = null; try { exe = GAT.createFile(gatContext,
		 * getLocationURI(description)); } catch (GATObjectCreationException e) {
		 * throw new GATInvocationException("resource broker cpi", e); }
		 * 
		 * v.add(exe);
		 */

		org.gridlab.gat.io.File stdin = sd.getStdin();
		if (stdin != null) {
			v.add(stdin);
		}

		File[] pre = sd.getPreStaged();
		if (pre != null) {
			for (int i = 0; i < pre.length; i++) {
				v.add(pre[i]);
			}
		}

		// now copy everything in v to destination
		for (int i = 0; i < v.size(); i++) {
			try {
				File srcFile = (File) v.get(i);
				URI src = srcFile.toURI();
				String path = new java.io.File(IPUtils.getPath(src)).getName();

				String dest = "file://";
				dest += (src.getUserInfo() == null ? "" : src.getUserInfo());
				dest += host;
				dest += (src.getPort() == -1 ? "" : ":" + src.getPort());
				dest += "/" + path;

				URI destURI = new URI(dest);

				if (GATEngine.VERBOSE) {
					System.err.println("resource broker cpi presage:");
					System.err.println("  copy " + srcFile.toURI() + " to "
							+ destURI);
				}
				srcFile.copy(destURI);
			} catch (Exception e) {
				throw new GATInvocationException("resource broker cpi", e);
			}
		}
	}

	private File toRemoteFile(File f, String host)
			throws GATInvocationException {
		File res = null;

		if (host == null || host.equals("")) {
			return f;
		}

		URI src = f.toURI();
		if (src.getHost() != null)
			return f;

		String dest = "file://";
		dest += (src.getUserInfo() == null ? "" : src.getUserInfo());
		dest += host;
		dest += (src.getPort() == -1 ? "" : ":" + src.getPort());
		dest += "/" + IPUtils.getPath(src);

		URI destURI = null;
		try {
			destURI = new URI(dest);
		} catch (URISyntaxException e) {
			throw new GATInvocationException("resource broker cpi", e);
		}

		try {
			res = GAT.createFile(gatContext, preferences, destURI);
		} catch (GATObjectCreationException e) {
			throw new GATInvocationException("resource broker cpi", e);
		}

		return res;
	}

	public void postStageFiles(JobDescription description, String host)
			throws GATInvocationException {
		Vector v = new Vector();
		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		org.gridlab.gat.io.File[] post = sd.getPostStaged();
		if (post != null) {
			for (int i = 0; i < post.length; i++) {
				v.add(toRemoteFile(post[i], host));
			}
		}

		org.gridlab.gat.io.File stdout = sd.getStdout();
		if (stdout != null) {
			v.add(toRemoteFile(stdout, host));
		}

		org.gridlab.gat.io.File stderr = sd.getStderr();
		if (stderr != null) {
			v.add(toRemoteFile(stderr, host));
		}

		// now copy everything in v back to local machine
		for (int i = 0; i < v.size(); i++) {
			try {
				File srcFile = (File) v.get(i);

				String dest = "file:///";
				dest += srcFile.getName();
				URI destURI = new URI(dest);

				if (GATEngine.VERBOSE) {
					System.err.println("resource broker cpi postsage:");
					System.err.println("  copy " + srcFile.toURI() + " to "
							+ destURI);
				}
				srcFile.copy(destURI);
			} catch (Exception e) {
				throw new GATInvocationException("resource broker cpi", e);
			}
		}
	}

	protected void checkName(String adaptor) throws AdaptorCreationException {
		GATEngine.checkName(preferences, "ResourceBroker", adaptor);
	}
}