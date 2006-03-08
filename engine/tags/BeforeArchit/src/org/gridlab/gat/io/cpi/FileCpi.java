package org.gridlab.gat.io.cpi;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.net.RemoteException;

/**
 * Capability provider interface to the File class.
 * <p>
 * Capability provider wishing to provide the functionality of the File class
 * must extend this class and implement all of the abstract methods in this
 * class. Each abstract method in this class mirrors the corresponding method in
 * this File class and will be used to implement the corresponding method in the
 * File class at runtime.
 */
public abstract class FileCpi /* extends java.io.File */implements File {

	private static class DeleteHook extends Thread {
		FileCpi f;

		DeleteHook(FileCpi f) {
			this.f = f;
		}

		public void run() {
			try {
				f.delete();
			} catch (Throwable t) {
				// Ignore
			}
		}
	}

	GATContext gatContext;

	Preferences preferences;

	URI location;

	/**
	 * Constructs a FileCpi instance which corresponds to the physical file
	 * identified by the passed Location and whose access rights are determined
	 * by the passed GATContext.
	 * 
	 * @param location
	 *            A Location which represents the URI corresponding to the
	 *            physical file.
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this FileCpi.
	 * @throws java.lang.Exception
	 *             Thrown upon creation problems
	 */
	protected FileCpi(GATContext gatContext, Preferences preferences,
			URI location) throws AdaptorCreationException {
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
		if (!(object instanceof org.gridlab.gat.io.File))
			return false;

		org.gridlab.gat.io.File file = (org.gridlab.gat.io.File) object;
		return location.equals(file.toURI());
	}

	/**
	 * This method returns the URI of this File
	 * 
	 * @return The URI of this File
	 */
	public URI toURI() {
		return location;
	}

	/**
	 * This method moves the physical file represented by this File instance to
	 * a physical file identified by the passed URI.
	 * 
	 * @param location
	 *            The URI to which to move the physical file corresponding to
	 *            this File instance
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public void move(URI location) throws IOException, GATInvocationException {
		// Step 1: Copy the original file

		copy(location);

		// Step 2: Delete the original file
		delete();

		// Step 3: Update location
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#copy(java.net.URI)
	 */
	public void copy(URI loc) throws GATInvocationException, IOException {
		throw new Error("Not implemented");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#addMetricListener(org.gridlab.gat.monitoring.MetricListener,
	 *      org.gridlab.gat.monitoring.Metric)
	 */
	public void addMetricListener(MetricListener metricListener, Metric metric)
			throws RemoteException {
		throw new Error("Not implemented");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#getMetrics()
	 */
	public List getMetrics() throws RemoteException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.monitoring.Monitorable#removeMetricListener(org.gridlab.gat.monitoring.MetricListener,
	 *      org.gridlab.gat.monitoring.Metric)
	 */
	public void removeMetricListener(MetricListener metricListener,
			Metric metric) throws RemoteException {
		throw new Error("Not implemented");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#canRead()
	 */
	public boolean canRead() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#canWrite()
	 */
	public boolean canWrite() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#compareTo(java.io.File)
	 */
	public int compareTo(java.io.File other) {
		return location.compareTo(other.toURI());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object other) {
		return location.compareTo(((FileCpi) other).location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#createNewFile()
	 */
	public boolean createNewFile() throws IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#delete()
	 */
	public boolean delete() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#deleteOnExit()
	 */
	public void deleteOnExit() {
		Runtime.getRuntime().addShutdownHook(new DeleteHook(this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#exists()
	 */
	public boolean exists() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getAbsoluteFile()
	 */
	public File getAbsoluteFile() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getAbsolutePath()
	 */
	public String getAbsolutePath() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getCanonicalFile()
	 */
	public File getCanonicalFile() throws IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getCanonicalPath()
	 */
	public String getCanonicalPath() throws IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getName()
	 */
	public String getName() {
		String path = location.getPath();
		return new java.io.File(path).getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getParent()
	 */
	public String getParent() {
		String path = location.getPath();
		return new java.io.File(path).getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getParentFile()
	 */
	public File getParentFile() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getPath()
	 */
	public String getPath() {
		return location.getPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.net.URI#hashCode()
	 */
	public int hashCode() {
		return location.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#isAbsolute()
	 */
	public boolean isAbsolute() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#isDirectory()
	 */
	public boolean isDirectory() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#isFile()
	 */
	public boolean isFile() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#isHidden()
	 */
	public boolean isHidden() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#lastModified()
	 */
	public long lastModified() throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#length()
	 */
	public long length() throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#list()
	 */
	public String[] list() throws IOException, GATInvocationException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#list(java.io.FilenameFilter)
	 */
	public String[] list(FilenameFilter arg0) {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#listFiles()
	 */
	public File[] listFiles() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#listFiles(java.io.FileFilter)
	 */
	public File[] listFiles(FileFilter arg0) {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#listFiles(java.io.FilenameFilter)
	 */
	public File[] listFiles(FilenameFilter arg0) {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#mkdir()
	 */
	public boolean mkdir() throws GATInvocationException, IOException {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#mkdirs()
	 */
	public boolean mkdirs() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#renameTo(java.io.File)
	 */
	public boolean renameTo(java.io.File arg0) {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#setLastModified(long)
	 */
	public boolean setLastModified(long arg0) {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#setReadOnly()
	 */
	public boolean setReadOnly() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#toURL()
	 */
	public URL toURL() throws MalformedURLException {
		throw new Error("Not implemented");
	}
}