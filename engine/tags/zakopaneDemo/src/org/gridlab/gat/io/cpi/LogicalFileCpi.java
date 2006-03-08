package org.gridlab.gat.io.cpi;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Vector;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.LogicalFile;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * Capability provider interface to the LogicalFile class.
 * <p>
 * Capability provider wishing to provide the functionality of the LogicalFile
 * class must extend this class and implement all of the abstract methods in
 * this class. Each abstract method in this class mirrors the corresponding
 * method in this LogicalFile class and will be used to implement the
 * corresponding method in the LogicalFile class at runtime.
 */
public abstract class LogicalFileCpi implements LogicalFile, Monitorable {
	protected GATContext gatContext;

	protected Preferences preferences;

	/**
	 * Files in the LogicalFile
	 */
	protected Vector files = null;

	/**
	 * This constructor creates a LogicalFileCpi corresponding to the passed URI
	 * instance and uses the passed GATContext to broker resources.
	 * 
	 * @param location
	 *            The URI of one physical file in this LogicalFileCpi
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param preferences
	 *            the preferences to be associated with this adaptor
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public LogicalFileCpi(GATContext gatContext, Preferences preferences,
			URI location) throws GATObjectCreationException {
		this.gatContext = gatContext;
		this.preferences = preferences;
		files = new Vector();

		if (location != null) {
			files.add(GAT.createFile(gatContext, preferences, location));
		}
	}

	/**
	 * Adds the passed File instance to the set of physical files represented by
	 * this LogicalFile instance.
	 * 
	 * @param file
	 *            A File instance to add to the set of physical files
	 *            represented by this LogicalFile instance.
	 */
	public void addFile(File file) {
		files.add(file);
	}

	/**
	 * Adds the physical file at the passed URI to the set of physical files
	 * represented by this LogicalFile instance.
	 * 
	 * @param location
	 *            The URI of a physical file to add to the set of physical files
	 *            represented by this LogicalFile instance.
	 * @throws GATInvocationException
	 *             Thrown upon problems accessing the remote instance
	 */
	public void addURI(URI location) throws GATInvocationException {
		try {
			files.add(GAT.createFile(gatContext, preferences, location));
		} catch (GATObjectCreationException e) {
			throw new GATInvocationException("", e);
		}
	}

	public MetricDefinition getMetricDefinitionByName(String name)
			throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/**
	 * Removes the passed File instance from the set of physical files
	 * represented by this LogicalFile instance.
	 * 
	 * @param file
	 *            A File instance to remove from the set of physical files
	 *            represented by this LogicalFile instance.
	 */
	public void removeFile(File file) {
		files.remove(file);
	}

	/**
	 * Removes the physical file at the passed URI from the set of physical
	 * files represented by this LogicalFile instance.
	 * 
	 * @param location
	 *            The URI of a physical file to remove from the set of physical
	 *            files represented by this LogicalFile instance.
	 * @throws GATInvocationException
	 *             Thrown upon problems accessing the remote instance
	 */
	public void removeURI(URI location) throws GATInvocationException {
		try {
			files.remove(GAT.createFile(gatContext, preferences, location));
		} catch (GATObjectCreationException e) {
			throw new GATInvocationException("", e);
		}
	}

	/**
	 * Replicates the logical file represented by this instance to the physical
	 * file specified by the passed URI.
	 * 
	 * @param loc
	 *            The URI of the new physical file
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 * @throws GATInvocationException
	 *             Thrown upon problems accessing the remote instance
	 */
	public void replicate(URI loc) throws IOException, GATInvocationException {
		if (files.size() == 0) {
			throw new IOException("Must have at least one source file");
		}

		File f = (File) files.get(0);
		f.copy(loc);
	}

	/**
	 * Returns a java.util.List of URI instances each of which is the URI of a
	 * physical file represented by this instance.
	 * 
	 * @return The java.util.List of URIs
	 */
	public List getURIs() {
		Vector locations = new Vector();
		for (int i = 0; i < files.size(); i++) {
			File f = (File) files.get(i);
			locations.add(f.toURI());
		}
		return locations;
	}

	/**
	 * Returns a java.util.List of File instances each of which is a File
	 * corresponding to a physical file represented by this instance.
	 * 
	 * @return The java.util.List of Files
	 */
	public List getFiles() {
		return files;
	}

	/**
	 * This method adds the passed instance of a MetricListener to the
	 * java.util.List of MetricListeners which are notified of MetricEvents by
	 * an instance of this class. The passed MetricListener is only notified of
	 * MetricEvents which correspond to Metric instance passed to this method.
	 * 
	 * @param metricListener
	 *            The MetricListener to notify of MetricEvents
	 * @param metric
	 *            The Metric corresponding to the MetricEvents for which the
	 *            passed MetricListener will be notified
	 */
	public void addMetricListener(MetricListener metricListener, Metric metric) {
		throw new Error("Not implemented");
	}

	/**
	 * Removes the passed MetricListener from the java.util.List of
	 * MetricListeners which are notified of MetricEvents corresponding to the
	 * passed Metric instance.
	 * 
	 * @param metricListener
	 *            The MetricListener to notify of MetricEvents
	 * @param metric
	 *            The Metric corresponding to the MetricEvents for which the
	 *            passed MetricListener will be notified
	 */
	public void removeMetricListener(MetricListener metricListener,
			Metric metric) {
		throw new Error("Not implemented");
	}

	/**
	 * This method returns a java.util.List of Metric instances. Each Metric
	 * instance in this java.util.List is a Metric which can be monitored on
	 * this instance.
	 * 
	 * @return An java.util.List of Metric instances. Each Metric instance in
	 *         this java.util.List is a Metric which can be monitored on this
	 *         instance.
	 */
	public List getMetricDefinitions() {
		throw new Error("Not implemented");
	}

	public MetricValue getMeasurement(Metric metric)
			throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.Advertisable#marshal()
	 */
	public String marshal() {
		throw new Error("Not implemented");
	}

	protected void checkName(String adaptor) throws AdaptorCreationException {
		GATEngine.checkName(preferences, "LogicalFile", adaptor);
	}
}