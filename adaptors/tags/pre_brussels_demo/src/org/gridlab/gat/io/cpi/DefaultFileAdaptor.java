package org.gridlab.gat.io.cpi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * An abstract representation of a physical file.
 * <p>
 * An instance of this class presents an abstract, system-independent view of a
 * physical file. User interfaces and operating systems use system-dependent
 * pathname strings to identify physical files. GAT, however, uses an operating
 * system independent pathname string to identify a physical file. A physical
 * file in GAT is identified by a URI.
 * <p>
 * An instance of this File class allows for various high-level operations to be
 * preformed on a physical file. For example, one can, with a single API call,
 * copy a physical file from one location to a second location, move a physical
 * file from one location to a second location, delete a physical file, and
 * preform various other operations on a physical file. The utility of this
 * high-level view of a physical file is multi-fold. The client of an instance
 * of this class does not have to concern themselves with the details of reading
 * every single byte of a physical file when all they wish to do is copy the
 * physical file to a new location. Similarly, a client does not have to deal
 * with all the various error states that can occur when moving a physical file (
 * Have all the various bytes been read correctly? Have all the various bytes
 * been saved correctly? Did the deletion of the original file proceed
 * correctly? ); the client simply has to call a single API call and the
 * physical file is moved.
 */
public class DefaultFileAdaptor extends FileCpi {

	File f;

	/**
	 * @param gatContext
	 * @param preferences
	 * @param location
	 */
	public DefaultFileAdaptor(GATContext gatContext, Preferences preferences,
			URI location) throws AdaptorCreationException {
		super(gatContext, preferences, location);

		location = correctURI(location);

		if (GATEngine.DEBUG) {
			System.err.println("DefaultFileAdaptor: LOCATION = " + location);
		}

		if (!location.getScheme().equals("file") || location.getHost() != null) {
			throw new AdaptorCreationException(
					"The DefaultFileAdaptor can only handle local files.");
		}

		f = new File(location);
	}

	// Make life a bit easier for the programmer:
	// If the URI does not have a scheme part, just consider it a local
	// file.
	// The ctor of java.io.file does not accept this.
	protected URI correctURI(URI in) {
		URI tmpLocation = in;

		if (in.getScheme() == null) {
			java.io.File tmp = new java.io.File(tmpLocation.toString());
			tmpLocation = tmp.toURI();
		}

		return tmpLocation;
	}

	/**
	 * This method copies the physical file represented by this File instance to
	 * a physical file identified by the passed URI.
	 * 
	 * @param loc
	 *            The new location
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public void copy(URI loc) throws GATInvocationException, IOException {
		// Step 1: Create destination file
		File destinationFile = new File(correctURI(loc));
		destinationFile.createNewFile();

		// Step 3: Copy source to destination
		FileInputStream in = new FileInputStream(f);
		BufferedInputStream inBuf = new BufferedInputStream(in);

		FileOutputStream out = new FileOutputStream(destinationFile);
		BufferedOutputStream outBuf = new BufferedOutputStream(out);

		long length = f.length();

		try {
			for (int i = 0; i < length; i++) {
				int b = inBuf.read();
				outBuf.write(b);
			}
		} finally {
			try {
				outBuf.close();
			} catch (IOException ioException) {
				// Ignore ioException
			}
			try {
				inBuf.close();
			} catch (IOException ioException) {
				// Ignore ioException
			}
		}
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
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public void addMetricListener(MetricListener metricListener, Metric metric)
			throws RemoteException {
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
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public void removeMetricListener(MetricListener metricListener,
			Metric metric) throws RemoteException {
	}

	/**
	 * This method returns a java.util.List of Metric instances. Each Metric
	 * instance in this java.util.List is a Metric which can be monitored on
	 * this instance.
	 * 
	 * @return An java.util.List of Metric instances. Each Metric instance in
	 *         this java.util.List is a Metric which can be monitored on this
	 *         instance.
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public List getMetrics() throws RemoteException {
		return new Vector();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#canRead()
	 */
	public boolean canRead() {
		return f.canRead();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#canWrite()
	 */
	public boolean canWrite() {
		return f.canWrite();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#compareTo(java.io.File)
	 */
	public int compareTo(File other) {
		return f.compareTo(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#compareTo(java.lang.Object)
	 */
	public int compareTo(Object other) {
		return f.compareTo(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#createNewFile()
	 */
	public boolean createNewFile() throws IOException {
		return f.createNewFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#delete()
	 */
	public boolean delete() {
		return f.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		return f.equals(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#exists()
	 */
	public boolean exists() {
		return f.exists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getAbsoluteFile()
	 */
	public org.gridlab.gat.io.File getAbsoluteFile()
			throws GATInvocationException {
		try {
			return GAT.createFile(gatContext, preferences, f.getAbsoluteFile()
					.toURI());
		} catch (AdaptorCreationException e) {
			throw new GATInvocationException("default file", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getAbsolutePath()
	 */
	public String getAbsolutePath() {
		return f.getAbsolutePath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getCanonicalFile()
	 */
	public org.gridlab.gat.io.File getCanonicalFile()
			throws GATInvocationException, IOException {
		try {
			return GAT.createFile(gatContext, preferences, f.getCanonicalFile()
					.toURI());
		} catch (AdaptorCreationException e) {
			throw new GATInvocationException("default file", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getCanonicalPath()
	 */
	public String getCanonicalPath() throws IOException {
		return f.getCanonicalPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getName()
	 */
	public String getName() {
		return f.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getParent()
	 */
	public String getParent() {
		return f.getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getParentFile()
	 */
	public org.gridlab.gat.io.File getParentFile()
			throws GATInvocationException {
		try {
			return GAT.createFile(gatContext, preferences, f.getParentFile()
					.toURI());
		} catch (AdaptorCreationException e) {
			throw new GATInvocationException("default file", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getPath()
	 */
	public String getPath() {
		return f.getPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return f.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#isAbsolute()
	 */
	public boolean isAbsolute() {
		return f.isAbsolute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#isDirectory()
	 */
	public boolean isDirectory() {
		return f.isDirectory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#isFile()
	 */
	public boolean isFile() {
		return f.isFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#isHidden()
	 */
	public boolean isHidden() {
		return f.isHidden();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#lastModified()
	 */
	public long lastModified() throws GATInvocationException, IOException {
		return f.lastModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#length()
	 */
	public long length() throws GATInvocationException, IOException {
		return f.length();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#list()
	 */
	public String[] list() throws IOException, GATInvocationException {
		return f.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#list(java.io.FilenameFilter)
	 */
	public String[] list(FilenameFilter arg0) {
		return f.list(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#listFiles()
	 */
	public org.gridlab.gat.io.File[] listFiles() throws GATInvocationException {
		File[] r = f.listFiles();
		org.gridlab.gat.io.File[] res = new org.gridlab.gat.io.File[r.length];

		for (int i = 0; i < r.length; i++) {
			try {
				res[i] = GAT.createFile(gatContext, preferences, r[i].toURI());
			} catch (AdaptorCreationException e) {
				throw new GATInvocationException("default file", e);
			}
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#listFiles(java.io.FileFilter)
	 */
	public org.gridlab.gat.io.File[] listFiles(FileFilter arg0) throws GATInvocationException {
		File[] r = f.listFiles(arg0);
		org.gridlab.gat.io.File[] res = new org.gridlab.gat.io.File[r.length];

		for (int i = 0; i < r.length; i++) {
			try {
				res[i] = GAT.createFile(gatContext, preferences, r[i].toURI());
			} catch (AdaptorCreationException e) {
				throw new GATInvocationException("default file", e);
			}
		}
		return res;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#listFiles(java.io.FilenameFilter)
	 */
	public org.gridlab.gat.io.File[] listFiles(FilenameFilter arg0) throws GATInvocationException {
		File[] r = f.listFiles(arg0);
		org.gridlab.gat.io.File[] res = new org.gridlab.gat.io.File[r.length];

		for (int i = 0; i < r.length; i++) {
			try {
				res[i] = GAT.createFile(gatContext, preferences, r[i].toURI());
			} catch (AdaptorCreationException e) {
				throw new GATInvocationException("default file", e);
			}
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#mkdir()
	 */
	public boolean mkdir() throws GATInvocationException, IOException {
		return f.mkdir();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#mkdirs()
	 */
	public boolean mkdirs() {
		return f.mkdirs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#move(java.net.URI)
	 */
	public void move(URI location) throws IOException, GATInvocationException {
		File tmp = new File(location);
		boolean res = f.renameTo(tmp);
		if(!res) throw new IOException("Could not move file");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#renameTo(java.io.File)
	 */
	public boolean renameTo(org.gridlab.gat.io.File arg0) throws GATInvocationException {
		File tmp = new File(arg0.toURI());
		return f.renameTo(tmp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#setLastModified(long)
	 */
	public boolean setLastModified(long arg0) {
		return f.setLastModified(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#setReadOnly()
	 */
	public boolean setReadOnly() {
		return f.setReadOnly();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return f.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#toURI()
	 */
	public URI toURI() {
		return f.toURI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#toURL()
	 */
	public URL toURL() throws MalformedURLException {
		return f.toURL();
	}
}