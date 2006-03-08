/*
 * Created on Apr 16, 2004
 *  
 */
package org.gridlab.gat.resources;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author rob
 * 
 * The GATResourceDescription interface forms the base for the
 * GATSoftwareResourceDescriptions and GATHardwareResourceDescriptions classes;
 * these are used to specify and find resources which may then be used, for
 * example, to submit a Job to. It has an associated Map whose key/value pairs
 * describe the resource.
 * 
 * A GAT Job may have many requirements, both on software and hardware, which
 * need to be satisfied to run, e.g. specific versions of the operating system,
 * minimum amount of memory, presence of specific compilers or libraries on a
 * system, etc. Each one of these requirements may itself possibly in turn
 * depend on some other software or hardware requirement. Sometimes there may be
 * possible alternatives, for example the GAT Job may be able to use any one of
 * a set of possible system libraries which might be installed. Hence, a
 * complete resource description requires a list of possible specifications,
 * and, ideally, some way of specifying allowable alternatives. In order to
 * accommodate this, a GATResourceDescription has, as well as a table describing
 * software of hardware resource requirements, a list of child
 * GATResourceDescriptions, at least one of which must be satisfied in addition
 * to the requirements of this GATResourceDescription. I.e. a
 * GATResourceDescription is a tree, and it is matched if a path exists from the
 * root of the tree to any leaf where the requirements of every node on that
 * path are met.
 */

public interface ResourceDescription extends java.io.Serializable {

	/**
	 * Do equality test.
	 * 
	 * @param o
	 *            The object to compare to.
	 * @return boolean: equal or not.
	 */
	public boolean equals(Object o);

	/**
	 * Sets the ResourceDescription of this instance.
	 * 
	 * @param description
	 *            the description
	 */
	void setDescription(Map description);

	/**
	 * Get the ResourceDescription of this instance.
	 * 
	 * @return the description.
	 */
	Map getDescription();

	/**
	 * Adds the name/value pair to the set of name/value pairs which describe
	 * the "parent" component.
	 * 
	 * @param name
	 *            The Name, a java.lang.String, to add to the name/value pairs
	 *            which describe the "parent" component.
	 * @param value
	 *            The Value, an Object, to add to the name/value pairs which
	 *            describe the "parent" component.
	 */
	void addResourceAttribute(String name, Object value);

	/**
	 * Removes the name/value pair with the passed name from the java.util.Map
	 * of name/value pairs which describe the "parent" hardware component.
	 * 
	 * @param name
	 *            The Name, a java.lang.String, to of the name/value pair to
	 *            remove from the name/value pairs which describe the "parent"
	 *            hardware component.
	 * @throws NoSuchElementException
	 *             the name cannot be found.
	 */
	void removeResourceAttribute(String name) throws NoSuchElementException;

	/**
	 * Adds the passed ResourceDescription to the list of ResourceDescriptions
	 * which describe this ResourceDescription.
	 * 
	 * @param ResourceDescription
	 *            The ResourceDescription to add to the list of
	 *            ResourceDescriptions which describe this ResourceDescription.
	 */
	void addResourceDescription(ResourceDescription description);

	/**
	 * Removes the passed ResourceDescription from the list of
	 * ResourceDescriptions which describe this ResourceDescription.
	 * 
	 * @param description
	 *            The ResourceDescription to remove from the list of
	 *            ResourceDescriptions which describe this ResourceDescription.
	 * @throws NoSuchElementException
	 *             The description could not be found.
	 */
	void removeResourceDescription(ResourceDescription description)
			throws NoSuchElementException;
}