package org.gridlab.gat.io.cpi;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.FileOutputStreamInterface;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * Capability provider interface to the FileStream class.
 * <p>
 * Capability provider wishing to provide the functionality of the FileStream
 * class must extend this class and implement all of the abstract methods in
 * this class. Each abstract method in this class mirrors the corresponding
 * method in this FileStream class and will be used to implement the
 * corresponding method in the FileStream class at runtime.
 */
public abstract class FileOutputStreamCpi implements FileOutputStreamInterface {
	protected GATContext gatContext;

	protected Preferences preferences;

	protected URI location;

	protected boolean append;

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
	protected FileOutputStreamCpi(GATContext gatContext,
			Preferences preferences, URI location, Boolean append) {
		this.gatContext = gatContext;
		this.preferences = preferences;
		this.location = location;
		this.append = append.booleanValue();
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
		if (!(object instanceof FileOutputStreamCpi))
			return false;

		FileOutputStreamCpi s = (FileOutputStreamCpi) object;
		return location.equals(s.location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileOutputStreamInterface#close()
	 */
	public void close() throws IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileOutputStreamInterface#flush()
	 */
	public void flush() throws IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileOutputStreamInterface#write(byte[], int, int)
	 */
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileOutputStreamInterface#write(byte[])
	 */
	public void write(byte[] arg0) throws IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.FileOutputStreamInterface#write(int)
	 */
	public void write(int arg0) throws IOException {
		throw new Error("Not implemented");
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

	protected void checkName(String adaptor) throws AdaptorCreationException {
		GATEngine.checkName(preferences, "FileOutputStream", adaptor);
	}
}