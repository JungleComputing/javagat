// This class does not have to do anything, the CPI already provides all needed functionality.

package org.gridlab.gat.io.cpi;

import java.net.URI;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;

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
public class DefaultLogicalFileAdaptor extends LogicalFileCpi {

	/**
	 * This constructor creates a DefaultLogicalFileAdaptor corresponding to the
	 * passed URI instance and uses the passed GATContext to broker resources.
	 * 
	 * @param location
	 *            The URI of one physical file in this DefaultLogicalFileAdaptor
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @throws java.lang.Exception
	 *             Thrown upon creation problems
	 */
	public DefaultLogicalFileAdaptor(GATContext gatContext,
			Preferences preferences, URI location) throws Exception {
		super(gatContext, preferences, location);
	}
}