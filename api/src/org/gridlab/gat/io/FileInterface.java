package org.gridlab.gat.io;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * This interface is for internal GAT use only. It has to be public for
 * techinical reasons.
 * 
 * All GAT users should use org.gridlab.gat.io.File
 * 
 * @author rob
 */
public interface FileInterface extends Monitorable, Advertisable,
        Comparable<Object>, java.io.Serializable {

    /**
     * This method returns the GATContext object belonging to this File
     * 
     * @return The GATContext of this File
     */
    public GATContext getGATContext();

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
     *                The new location
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws java.io.IOException
     *                 Upon non-remote IO problem
     */
    public void copy(URI loc) throws GATInvocationException;

    /**
     * This method moves the physical file represented by this File instance to
     * a physical file identified by the passed URI.
     * 
     * @param location
     *                The URI to which to move the physical file corresponding
     *                to this File instance
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws IOException
     *                 Upon non-remote IO problem
     */
    public void move(URI location) throws GATInvocationException;

    /**
     * This method deletes a directory and everything that is in it. This method
     * can only be called on a directory, not on a file.
     * 
     * @throws GATInvocationException
     */
    public void recursivelyDeleteDirectory() throws GATInvocationException;

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
     *                The Object to test for equality
     * @return A boolean indicating equality
     */
    public boolean equals(Object object);

    /** See {@link java.io.File#canRead}. */
    public boolean canRead() throws GATInvocationException;

    /** See {@link java.io.File#canWrite}. */
    public boolean canWrite() throws GATInvocationException;

    /** See {@link java.io.File#compareTo(java.io.File)}. */
    public int compareTo(org.gridlab.gat.io.File arg0);

    /** See {@link java.io.File#compareTo(Object)}. */
    public int compareTo(Object arg0);

    /** See {@link java.io.File#createNewFile}. */
    public boolean createNewFile() throws GATInvocationException;

    /** See {@link java.io.File#delete()}. */
    public boolean delete() throws GATInvocationException;

    /** See {@link java.io.File#deleteOnExit()}. */
    public void deleteOnExit();

    /** See {@link java.io.File#exists()}. */
    public boolean exists() throws GATInvocationException;

    /** See {@link java.io.File#getAbsoluteFile()}. */
    public File getAbsoluteFile() throws GATInvocationException;

    /** See {@link java.io.File#getAbsolutePath()}. */
    public String getAbsolutePath() throws GATInvocationException;

    /** See {@link java.io.File#getCanonicalFile()}. */
    public File getCanonicalFile() throws GATInvocationException;

    /** See {@link java.io.File#getCanonicalPath()}. */
    public String getCanonicalPath() throws GATInvocationException;

    /** See {@link java.io.File#getName()}. */
    public String getName() throws GATInvocationException;

    /** See {@link java.io.File#getParent()}. */
    public String getParent() throws GATInvocationException;

    /** See {@link java.io.File#getParentFile()}. */
    public File getParentFile() throws GATInvocationException;

    /** See {@link java.io.File#getPath()}. */
    public String getPath() throws GATInvocationException;

    /** See {@link java.io.File#hashCode()}. */
    public int hashCode();

    /** See {@link java.io.File#isAbsolute()}. */
    public boolean isAbsolute();

    /** See {@link java.io.File#isDirectory()}. */
    public boolean isDirectory() throws GATInvocationException;

    /** See {@link java.io.File#isFile()}. */
    public boolean isFile() throws GATInvocationException;

    /** See {@link java.io.File#isHidden()}. */
    public boolean isHidden() throws GATInvocationException;

    /** See {@link java.io.File#lastModified()}. */
    public long lastModified() throws GATInvocationException;

    /** See {@link java.io.File#length()}. */
    public long length() throws GATInvocationException;

    /** See {@link java.io.File#list()}. */
    public String[] list() throws GATInvocationException;

    /** See {@link java.io.File#list(java.io.FilenameFilter)}. */
    public String[] list(FilenameFilter arg0) throws GATInvocationException;

    /** See {@link java.io.File#listFiles()}. */
    public File[] listFiles() throws GATInvocationException;

    /** See {@link java.io.File#listFiles(java.io.FileFilter)}. */
    public File[] listFiles(FileFilter arg0) throws GATInvocationException;

    /** See {@link java.io.File#listFiles(java.io.FilenameFilter)}. */
    public File[] listFiles(FilenameFilter arg0) throws GATInvocationException;

    /** See {@link java.io.File#mkdir()}. */
    public boolean mkdir() throws GATInvocationException;

    /** See {@link java.io.File#mkdirs()}. */
    public boolean mkdirs() throws GATInvocationException;

    /** See {@link java.io.File#renameTo(java.io.File)}. */
    public boolean renameTo(File arg0) throws GATInvocationException;

    /** See {@link java.io.File#setLastModified(long)}. */
    public boolean setLastModified(long arg0) throws GATInvocationException;

    /** See {@link java.io.File#setReadOnly()}. */
    public boolean setReadOnly() throws GATInvocationException;

    /** See {@link java.io.File#toString()}. */
    public String toString();

    /** See {@link java.io.File#toURL()}. */
    public URL toURL() throws MalformedURLException;
}
