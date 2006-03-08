package org.gridlab.gat.io;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.gridlab.gat.GATInvocationException;
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
 * with all the various error states that can occur when moving a physical file (
 * Have all the various bytes been read correctly? Have all the various bytes
 * been saved correctly? Did the deletion of the original file proceed
 * correctly? ); the client simply has to call a single API call and the
 * physical file is moved.
 */
public interface File extends Monitorable, Serializable {

	/**
	 * This method returns the URI of this File
	 * 
	 * @return The URI of this File
	 */
	public URI toURI();

	/**
	 * This method copies the physical file represented by this File instance to
	 * a physical file identified by the passed URI.
	 * 
	 * @param loc
	 *            The new location
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public void copy(URI loc) throws GATInvocationException, IOException;

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
	public void move(URI location) throws GATInvocationException, IOException;

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
	public boolean equals(Object object);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#canRead()
	 */
	public boolean canRead();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#canWrite()
	 */
	public boolean canWrite();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#compareTo(java.io.File)
	 */
	public int compareTo(java.io.File arg0);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#createNewFile()
	 */
	public boolean createNewFile() throws IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#delete()
	 */
	public boolean delete();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#deleteOnExit()
	 */
	public void deleteOnExit();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#exists()
	 */
	public boolean exists();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getAbsoluteFile()
	 */
	public File getAbsoluteFile();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getAbsolutePath()
	 */
	public String getAbsolutePath();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getCanonicalFile()
	 */
	public File getCanonicalFile() throws IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getCanonicalPath()
	 */
	public String getCanonicalPath() throws IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getName()
	 */
	public String getName();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getParent()
	 */
	public String getParent();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getParentFile()
	 */
	public File getParentFile();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#getPath()
	 */
	public String getPath();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#isAbsolute()
	 */
	public boolean isAbsolute();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#isDirectory()
	 */
	public boolean isDirectory();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#isFile()
	 */
	public boolean isFile();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#isHidden()
	 */
	public boolean isHidden();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#lastModified()
	 */
	public long lastModified() throws GATInvocationException, IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#length()
	 */
	public long length() throws GATInvocationException, IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#list()
	 */
	public String[] list() throws GATInvocationException, IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#list(java.io.FilenameFilter)
	 */
	public String[] list(FilenameFilter arg0);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#listFiles()
	 */
	public File[] listFiles();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#listFiles(java.io.FileFilter)
	 */
	public File[] listFiles(FileFilter arg0);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#listFiles(java.io.FilenameFilter)
	 */
	public File[] listFiles(FilenameFilter arg0);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#mkdir()
	 */
	public boolean mkdir() throws GATInvocationException, IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#mkdirs()
	 */
	public boolean mkdirs();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#renameTo(java.io.File)
	 */
	public boolean renameTo(java.io.File arg0);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#setLastModified(long)
	 */
	public boolean setLastModified(long arg0);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#setReadOnly()
	 */
	public boolean setReadOnly();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#toURL()
	 */
	public URL toURL() throws MalformedURLException;
}