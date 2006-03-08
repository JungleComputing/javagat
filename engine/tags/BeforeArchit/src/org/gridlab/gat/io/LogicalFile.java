package org.gridlab.gat.io;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * An abstract representation of a set of identical physical files.
 * <p>
 * A LogicalFile is an abstract representation of a set of identical physical
 * files. This abstraction is useful for a number of reasons. For example, if
 * one wishes to replicate a physical file which is at one URI to a second URI.
 * Normally, one takes all the data at the first URI and replicates it to the
 * second URI even though the "network distance," between the first and second
 * URI may be great. A better solution to this problem is to have a set of
 * identical physical files distributed at different locations in "network
 * space." If one then wishes to replicate a physical file from one URI to a
 * second URI, GAT can then first determine which physical file is closest in
 * "network space" to the second URI, chose that physical file as the source
 * file, and copy it to the destination URI. Similarly, the construct of a
 * LogicalFile allows for migrating programs to, while at a given point in
 * "network space," use the closest physical file in "network space" to its
 * physical location.
 */
public interface LogicalFile extends Monitorable {

	/**
	 * Adds the passed File instance to the set of physical files represented by
	 * this LogicalFile instance.
	 * 
	 * @param file
	 *            A File instance to add to the set of physical files
	 *            represented by this LogicalFile instance.
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public void addFile(File file) throws GATInvocationException, IOException;

	/**
	 * Adds the physical file at the passed URI to the set of physical files
	 * represented by this LogicalFile instance.
	 * 
	 * @param location
	 *            The URI of a physical file to add to the set of physical files
	 *            represented by this LogicalFile instance.
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public void addURI(URI location) throws GATInvocationException, IOException;

	/**
	 * Removes the passed File instance from the set of physical files
	 * represented by this LogicalFile instance.
	 * 
	 * @param file
	 *            A File instance to remove from the set of physical files
	 *            represented by this LogicalFile instance.
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public void removeFile(File file) throws GATInvocationException,
			IOException;

	/**
	 * Removes the physical file at the passed URI from the set of physical
	 * files represented by this LogicalFile instance.
	 * 
	 * @param location
	 *            The URI of a physical file to remove from the set of physical
	 *            files represented by this LogicalFile instance.
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public void removeURI(URI location) throws GATInvocationException,
			IOException;

	/**
	 * Replicates the logical file represented by this instance to the physical
	 * file specified by the passed URI.
	 * 
	 * @param location
	 *            The URI of the new physical file
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public void replicate(URI location) throws GATInvocationException,
			IOException;

	/**
	 * Returns a java.util.List of URI instances each of which is the URI of a
	 * physical file represented by this instance.
	 * 
	 * @return The java.util.List of URIs
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public List getURIs() throws GATInvocationException, IOException;

	/**
	 * Returns a java.util.List of File instances each of which is a File
	 * corresponding to a physical file represented by this instance.
	 * 
	 * @return The java.util.List of URIs
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 */
	public List getFiles() throws GATInvocationException, IOException;
}