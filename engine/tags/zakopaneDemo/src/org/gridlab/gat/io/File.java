package org.gridlab.gat.io;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.gridlab.gat.GATIOException;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.Monitorable;

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
 * with all the various error states that can occur when moving a physical file.
 * Have all the various bytes been read correctly? Have all the various bytes
 * been saved correctly? Did the deletion of the original file proceed
 * correctly? The client simply has to call a single API call and the physical
 * file is moved.
 */
public class File extends java.io.File implements Monitorable, Serializable,
		Advertisable, Comparable {
	FileInterface f;

	public File(FileInterface f) {
		super("dummy");
		this.f = f;
	}

	public void addMetricListener(MetricListener metricListener, Metric metric)
			throws GATInvocationException {
		f.addMetricListener(metricListener, metric);
	}

	public MetricDefinition getMetricDefinitionByName(String name)
			throws GATInvocationException {
		return f.getMetricDefinitionByName(name);
	}

	/** See {@link java.io.File#canRead}. */
	public boolean canRead() {
		try {
			return f.canRead();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#canWrite}. */
	public boolean canWrite() {
		try {
			return f.canWrite();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.lang.Comparable#compareTo}. */
	public int compareTo(Object arg0) {
		return f.compareTo(arg0);
	}

	/** See {@link java.lang.Comparable#compareTo}. */
	public int compareTo(java.io.File arg0) {
		return f.compareTo(arg0);
	}

	/**
	 * This method copies the physical file represented by this File instance to
	 * a physical file identified by the passed URI.
	 * 
	 * @param loc
	 *            The new location
	 * @throws GATInvocationException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public void copy(URI loc) throws GATInvocationException, IOException {
		f.copy(loc);
	}

	/** See {@link java.io.File#createNewFile}. */
	public boolean createNewFile() throws IOException {
		try {
			return f.createNewFile();
		} catch (GATInvocationException e) {
			throw new GATIOException(e);
		}
	}

	/** See {@link java.io.File#delete}. */
	public boolean delete() {
		try {
			return f.delete();
		} catch (GATInvocationException e) {
			return false;
		}
	}

	/** See {@link java.io.File#deleteOnExit}. */
	public void deleteOnExit() {
		try {
			f.deleteOnExit();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#equals}. */
	public boolean equals(Object arg0) {
		return f.equals(arg0);
	}

	/** See {@link java.io.File#exists}. */
	public boolean exists() {
		try {
			return f.exists();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#getAbsoluteFile()}. */
	public java.io.File getAbsoluteFile() {
		try {
			return f.getAbsoluteFile();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#getAbsolutePath()}. */
	public String getAbsolutePath() {
		try {
			return f.getAbsolutePath();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#getCanonicalFile()}. */
	public java.io.File getCanonicalFile() throws IOException {
		try {
			return f.getCanonicalFile();
		} catch (GATInvocationException e) {
			throw new GATIOException(e);
		}
	}

	/** See {@link java.io.File#getCanonicalPath()}. */
	public String getCanonicalPath() throws IOException {
		try {
			return f.getCanonicalPath();
		} catch (GATInvocationException e) {
			throw new GATIOException(e);
		}
	}

	public List getMetricDefinitions() throws GATInvocationException {
		return f.getMetricDefinitions();
	}

	/** See {@link java.io.File#getName}. */
	public String getName() {
		try {
			return f.getName();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#getParent}. */
	public String getParent() {
		try {
			return f.getParent();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#getParentFile}. */
	public java.io.File getParentFile() {
		try {
			return f.getParentFile();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#getPath}. */
	public String getPath() {
		try {
			return f.getPath();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#hashCode}. */
	public int hashCode() {
		return f.hashCode();
	}

	/** See {@link java.io.File#isAbsolute()}. */
	public boolean isAbsolute() {
		try {
			return f.isAbsolute();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#isDirectory()}. */
	public boolean isDirectory() {
		try {
			return f.isDirectory();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#isFile()}. */
	public boolean isFile() {
		try {
			return f.isFile();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#isHidden()}. */
	public boolean isHidden() {
		try {
			return f.isHidden();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#lastModified()}. */
	public long lastModified() {
		try {
			return f.lastModified();
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#length()}. */
	public long length() {
		try {
			return f.length();
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#list()}. */
	public String[] list() {
		try {
			return f.list();
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#list(java.io.FilenameFilter)}. */
	public String[] list(FilenameFilter arg0) {
		try {
			return f.list(arg0);
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#listFiles()}. */
	public java.io.File[] listFiles() {
		try {
			return f.listFiles();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#listFiles(java.io.FileFilter)}. */
	public java.io.File[] listFiles(FileFilter arg0) {
		try {
			return f.listFiles(arg0);
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#list(java.io.FilenameFilter)}. */
	public java.io.File[] listFiles(FilenameFilter arg0) {
		try {
			return f.listFiles(arg0);
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	public String marshal() {
		return f.marshal();
	}

	/** See {@link java.io.File#mkdir}. */
	public boolean mkdir() {
		try {
			return f.mkdir();
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#mkdirs}. */
	public boolean mkdirs() {
		try {
			return f.mkdirs();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/**
	 * This method moves the physical file represented by this File instance to
	 * a physical file identified by the passed URI.
	 * 
	 * @param location
	 *            The URI to which to move the physical file corresponding to
	 *            this File instance
	 * @throws GATInvocationException
	 *             Thrown upon problems accessing the remote instance
	 * @throws IOException
	 *             Upon non-remote IO problem
	 */
	public void move(URI location) throws GATInvocationException, IOException {
		f.move(location);
	}

	public void removeMetricListener(MetricListener metricListener,
			Metric metric) throws GATInvocationException {
		f.removeMetricListener(metricListener, metric);
	}

	/** See {@link java.io.File#renameTo}. */
	public boolean renameTo(File arg0) {
		try {
			return f.renameTo(arg0);
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#setLastModified(long)}. */
	public boolean setLastModified(long arg0) {
		try {
			return f.setLastModified(arg0);
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#setReadOnly()}. */
	public boolean setReadOnly() {
		try {
			return f.setReadOnly();
		} catch (GATInvocationException e) {
			throw new Error(e);
		}
	}

	/** See {@link java.io.File#toString()}. */
	public String toString() {
		return f.toString();
	}

	/** See {@link java.io.File#toURI()}. */
	public URI toURI() {
		return f.toURI();
	}

	/** See {@link java.io.File#toURL()}. */
	public URL toURL() throws MalformedURLException {
		return f.toURL();
	}

	public MetricValue getMeasurement(Metric metric)
			throws GATInvocationException {
		return f.getMeasurement(metric);
	}
}