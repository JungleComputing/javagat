package org.gridlab.gat.io;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * An abstract representation of a set of identical physical files.
 * <p>
 * A LogicalFile is an abstract representation of a set of identical physical
 * files. This abstraction is useful for a number of reasons. For example, if
 * one wishes to replicate a physical file which is at one URI to a second URI.
 * Normally, one takes all the data at the first URI and replicates it to the
 * second URI even though the "network distance" between the first and second
 * URI may be great. A better solution to this problem is to have a set of
 * identical physical files distributed at different locations in "network
 * space". If one then wishes to replicate a physical file from one URI to a
 * second URI, GAT can then first determine which physical file is closest in
 * "network space" to the second URI, chose that physical file as the source
 * file, and copy it to the destination URI. Similarly, the construct of a
 * LogicalFile allows for migrating programs to use the physical file in
 * "network space" that is closest to its physical location.
 */
public interface LogicalFile extends Monitorable, Serializable, Advertisable {
    /** open, if logical file exists */
    static final int OPEN = 0;

    /** create new, if logical file does not exist */
    static final int CREATE = 1;

    /** create new, if logical file exists */
    static final int TRUNCATE = 2;

    /**
     * Adds the passed File instance to the set of physical files represented by
     * this LogicalFile instance.
     * 
     * @param file
     *                A File instance to add to the set of physical files
     *                represented by this LogicalFile instance.
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws IOException
     *                 if an IO operation fails
     */
    public void addFile(File file) throws GATInvocationException, IOException;

    /**
     * Adds the physical file at the passed URI to the set of physical files
     * represented by this LogicalFile instance.
     * 
     * @param location
     *                The URI of a physical file to add to the set of physical
     *                files represented by this LogicalFile instance.
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws IOException
     *                 if an IO operation fails
     */
    public void addURI(URI location) throws GATInvocationException, IOException;

    /**
     * Removes the passed File instance from the set of physical files
     * represented by this LogicalFile instance.
     * 
     * @param file
     *                A File instance to remove from the set of physical files
     *                represented by this LogicalFile instance.
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws IOException
     *                 if an IO operation fails
     */
    public void removeFile(File file) throws GATInvocationException,
            IOException;

    /**
     * Removes the physical file at the passed URI from the set of physical
     * files represented by this LogicalFile instance.
     * 
     * @param location
     *                The URI of a physical file to remove from the set of
     *                physical files represented by this LogicalFile instance.
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws IOException
     *                 if an IO operation fails
     */
    public void removeURI(URI location) throws GATInvocationException,
            IOException;

    /**
     * Replicates the logical file represented by this instance to the physical
     * file specified by the passed URI.
     * 
     * @param location
     *                The URI of the new physical file
     * @throws GATInvocationException
     *                 Upon non-remote IO problem
     * @throws IOException
     *                 if an IO operation fails
     */
    public void replicate(URI location) throws GATInvocationException,
            IOException;

    /**
     * Returns a java.util.List of URI instances each of which is the URI of a
     * physical file represented by this instance.
     * 
     * @return The java.util.List of URIs
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws IOException
     *                 if an IO operation fails
     */
    public List<URI> getURIs() throws GATInvocationException, IOException;

    /**
     * Returns a java.util.List of File instances each of which is a File
     * corresponding to a physical file represented by this instance.
     * 
     * @return The java.util.List of URIs
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws IOException
     *                 if an IO operation fails
     */
    public List<File> getFiles() throws GATInvocationException, IOException;

    /**
     * Returns the {@link URI} with the smallest network distance to given
     * {@link URI}.
     * 
     * @param loc
     *                the URI to compare with
     * @return the URI with the smallest network distance to loc
     * @throws GATInvocationException
     *                 if the method fails.
     */
    public URI getClosestURI(URI loc) throws GATInvocationException;

    /**
     * Returns a {@link java.util.List} of {@link URI}s ordered from close to
     * less closer with respect to the given URI.
     * 
     * @param location
     *                the location that's used to determine the distance
     * @return a {@link java.util.List} of {@link URI}s ordered from close to
     *         less closer with respect to the given URI.
     * @throws GATInvocationException
     */
    public List<URI> getOrderedURIs(URI location) throws GATInvocationException;

    /**
     * Returns the name of this logical file
     * 
     * @return the name of this logical file
     * @throws GATInvocationException
     */
    public String getName() throws GATInvocationException;
}
