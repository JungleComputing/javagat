/*
 * Created on Apr 16, 2004
 *  
 */
package org.gridlab.gat.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author rob
 */

/** The ResourceDescription interface forms the base for the
 * SoftwareResourceDescriptions and HardwareResourceDescriptions classes;
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
 * accommodate this, a ResourceDescription has, as well as a table describing
 * software of hardware resource requirements, a list of child
 * ResourceDescriptions, at least one of which must be satisfied in addition
 * to the requirements of this ResourceDescription. I.e. a
 * ResourceDescription is a tree, and it is matched if a path exists from the
 * root of the tree to any leaf where the requirements of every node on that
 * path are met.
 */
public abstract class ResourceDescription implements java.io.Serializable {

    /**
     * This member variable holds the Map which describes this resource".
     *
     */
    private HashMap attributes = null;

    /**
     * This member variable holds a List of ResourceDescriptions that also
     * are required
     * 
     */
    private List resourceDescriptions = null;

    /**
     * Constructs a ResourceDescription associated without attributes and 
     * dependencies.
     */
    protected ResourceDescription() {
        this.attributes = new HashMap();
    }

    /**
     * Constructs a ResourceDescription associated with the passed
     * attributes. There are no further dependencies
     *
     * @param attributes A java.util.Map, which describes the attributes of this
     * resource
     */
    protected ResourceDescription(Map attributes) {
        this.attributes = new HashMap(attributes);
    }

    /**
     * Constructs a ResourceDescription associated with the passed
     * objects.
     *
     * @param attributes A java.util.Map, which describes the attributes of this
     * resource
     * 
     * @param resourceDescriptions A java.util.List, which is a list of 
     * resourceDescriptions each of which describes a software component upon 
     * which this software component depends.
     */
    protected ResourceDescription(Map attributes, List resourceDescriptions) {
        this.attributes = new HashMap(attributes);
        this.resourceDescriptions = new ArrayList(resourceDescriptions);
    }

    /**
     * Tests this ResourceDescription for equality with the passed
     * Object.
     * <p>
     * If the given object is not a SoftwareResourceDescription, then this
     * method immediately returns false.
     * <p>
     * If the passed object is a SoftwareResourceDescription, then it is
     * equal if it has an equivalent SoftwareResourceDescription, and an equivalent
     * SoftwareResourceDescriptions, and an equivalent HardwareResourceDescriptions.
     * 
     * @param object
     *            The Object to test for equality
     * @return A boolean indicating equality
     */
    public boolean equals(Object object) {
        ResourceDescription sResourceDescription = null;

        if (false == (object instanceof ResourceDescription)) return false;

        sResourceDescription = (ResourceDescription) object;

        if (!attributes.equals(sResourceDescription.attributes)) return false;
        if (!resourceDescriptions
            .equals(sResourceDescription.resourceDescriptions)) return false;

        return true;
    }

    public int hashCode() {
        return attributes.hashCode();
    }

    /**
     * Gets the attributes associated with this instance
     * 
     * @return A java.util.Map, the SoftwareResourceDescription associated with
     *         this instance
     */
    public Map getDescription() {
        return attributes;
    }

    /**
     * Sets the attributes associated with this instance. Overwrites any 
     * earlier attributes.
     * 
     * @param attributes
     *            The new java.util.Map, the SoftwareResourceDescription
     *            associated with this instance
     */
    public void setDescription(Map attributes) {
        this.attributes = new HashMap(attributes);
    }

    /**
     * Adds the name/value pair to the java.util.Map of name/value pairs which
     * describe this resource.
     * 
     * @param name
     *            The Name, a java.lang.String, to add to the name/value pairs
     *            which describe this resource
     * @param value
     *            The Value, an Object, to add to the name/value pairs which
     *            describes this resource
     */
    public void addResourceAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Removes the name/value pair with the passed name from the java.util.Map
     * of name/value pairs which describe this resource.
     * 
     * @param name
     *            The Name, a java.lang.String, to of the name/value pair to
     *            remove from the name/value pairs which describe this resource.
     */
    public void removeResourceAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * Adds the passed HardwareResourceDescription to the java.util.List of
     * HardwareResourceDescriptions which describe this resource.
     * 
     * @param hardwareResourceDescription
     *            The HardwareResourceDescription to add to the java.util.List
     *            of HardwareResourceDescriptions which describe this resource.
     */
    public void addResourceDescription(
        ResourceDescription hardwareResourceDescription) {
        resourceDescriptions.add(hardwareResourceDescription);
    }

    /**
     * Removes the passed HardwareResourceDescription from the java.util.List of
     * HardwareResourceDescriptions which describe this resource.
     * 
     * @param hardwareResourceDescription
     *            The HardwareResourceDescription to remove from the
     *            java.util.List of HardwareResourceDescriptions which describe
     *             this resource.
     * @throws NoSuchElementException
     *             the element to be removed cannot be found
     */
    public void removeResourceDescription(
        ResourceDescription hardwareResourceDescription)
        throws NoSuchElementException {
        resourceDescriptions.remove(hardwareResourceDescription);
    }
}
