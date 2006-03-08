package org.gridlab.gat.io.cpi;

import java.util.List;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.FileInputStreamInterface;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;

public abstract class FileInputStreamCpi implements FileInputStreamInterface {
	protected GATContext gatContext;

	protected Preferences preferences;

	protected URI location;

	/**
	 * Constructs a FileInputStream Cpi instance which corresponds to the
	 * physical file identified by the passed Location and whose access rights
	 * are determined by the passed GATContext.
	 * 
	 * @param location
	 *            A Location which represents the URI corresponding to the
	 *            physical file.
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this FileCpi.
	 * @param preferences
	 *            the preferences to be associated with this adaptor
	 */
	protected FileInputStreamCpi(GATContext gatContext,
			Preferences preferences, URI location) {
		this.gatContext = gatContext;
		this.preferences = preferences;
		this.location = location;
	}

	/**
	 * Tests this File for equality with the passed Object.
	 * <p>
	 * If the given object is not a File, then this method immediately returns
	 * false.
	 * <p>
	 * If the given object is a File, then it is deemed equal to this instance
	 * if a URI object constructed from this File's location and a URI object
	 * constructed from the passed File's URI are equal as determined by the
	 * Equals method of URI.
	 * 
	 * @param object
	 *            The Object to test for equality
	 * @return A boolean indicating equality
	 */
	public boolean equals(Object object) {
		if (!(object instanceof FileInputStreamCpi))
			return false;

		FileInputStreamCpi s = (FileInputStreamCpi) object;
		return location.equals(s.location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#addMetricListener(org.gridlab.gat.monitoring.MetricListener,
	 *      org.gridlab.gat.monitoring.Metric)
	 */
	public void addMetricListener(MetricListener metricListener, Metric metric)
			throws GATInvocationException {
		throw new Error("Not implemented");
	}

	public MetricDefinition getMetricDefinitionByName(String name)
			throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#getMetrics()
	 */
	public List getMetricDefinitions() throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#removeMetricListener(org.gridlab.gat.monitoring.MetricListener,
	 *      org.gridlab.gat.monitoring.Metric)
	 */
	public void removeMetricListener(MetricListener metricListener,
			Metric metric) throws GATInvocationException {
		throw new Error("Not implemented");
	}

	public MetricValue getMeasurement(Metric metric)
			throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileInputStream#available()
	 */
	public int available() throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileInputStream#close()
	 */
	public void close() throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileInputStream#mark(int)
	 */
	public void mark(int readlimit) {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileInputStream#markSupported()
	 */
	public boolean markSupported() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileInputStream#read()
	 */
	public int read() throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileInputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileInputStream#read(byte[])
	 */
	public int read(byte[] b) throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileInputStream#reset()
	 */
	public void reset() throws GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileInputStream#skip(long)
	 */
	public long skip(long n) throws GATInvocationException {
		throw new Error("Not implemented");
	}

	protected void checkName(String adaptor) throws GATObjectCreationException {
		GATEngine.checkName(preferences, "FileInputStream", adaptor);
	}
}