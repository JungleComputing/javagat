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
 * The {@link ResourceDescription} interface forms the base for the
 * {@link SoftwareResourceDescription}s and {@link HardwareResourceDescription}s
 * classes; these are used to specify and find resources which may then be used,
 * for example, to submit a {@link Job} to. It has an associated {@link Map}
 * whose key/value pairs describe the resource.
 * <p>
 * A {@link Job} may have many requirements, both on software and hardware,
 * which need to be satisfied to run, e.g. specific versions of the operating
 * system, minimum amount of memory, presence of specific compilers or libraries
 * on a system, etc. Each one of these requirements may itself possibly in turn
 * depend on some other software or hardware requirement. Sometimes there may be
 * possible alternatives, for example the {@link Job} may be able to use any one
 * of a set of possible system libraries which might be installed. Hence, a
 * complete {@link ResourceDescription} requires a list of possible
 * specifications, and, ideally, some way of specifying allowable alternatives.
 * In order to accommodate this, a {@link ResourceDescription} has, as well as a
 * table describing software of hardware resource requirements, a list of child
 * {@link ResourceDescription}s, at least one of which must be satisfied in
 * addition to the requirements of this {@link ResourceDescription}.
 * <p>
 * Thus, a {@link ResourceDescription} is a tree, and it is matched if a path
 * exists from the root of the tree to any leaf where the requirements of every
 * node on that path are met.
 * 
 * @author rob
 */
public abstract class ResourceDescription implements java.io.Serializable {
    /**
     * This member variable holds the Map which describes this resource".
     */
    private HashMap<String, Object> attributes = null;

    /**
     * This member variable holds a List of ResourceDescriptions that also are
     * required
     * 
     */
    private List<ResourceDescription> resourceDescriptions = null;

    /**
     * Constructs a ResourceDescription associated without attributes and
     * dependencies.
     */
    protected ResourceDescription() {
        this.attributes = new HashMap<String, Object>();
    }

    /**
     * Constructs a ResourceDescription associated with the passed attributes.
     * There are no further dependencies
     * 
     * @param attributes
     *                A java.util.Map, which describes the attributes of this
     *                resource
     */
    protected ResourceDescription(Map<String, Object> attributes) {
        this.attributes = new HashMap<String, Object>(attributes);
    }

    /**
     * Constructs a ResourceDescription associated with the passed objects.
     * 
     * @param attributes
     *                A java.util.Map, which describes the attributes of this
     *                resource
     * 
     * @param resourceDescriptions
     *                A java.util.List, which is a list of resourceDescriptions
     *                each of which describes a software component upon which
     *                this software component depends.
     */
    protected ResourceDescription(Map<String, Object> attributes,
            List<ResourceDescription> resourceDescriptions) {
        this.attributes = new HashMap<String, Object>(attributes);
        this.resourceDescriptions = new ArrayList<ResourceDescription>(
                resourceDescriptions);
    }

    /**
     * Tests this {@link ResourceDescription} for equality with the passed
     * {@link Object}.
     * <p>
     * If the given object is not a {@link SoftwareResourceDescription}, then
     * this method immediately returns false.
     * <p>
     * If the passed object is a {@link SoftwareResourceDescription}, then it
     * is equal if it has an equivalent {@link SoftwareResourceDescription},
     * and an equivalent {@link SoftwareResourceDescription}s, and an
     * equivalent {@link HardwareResourceDescription}s.
     * 
     * @param object
     *                The {@link Object} to test for equality
     * @return A boolean indicating equality
     */
    public boolean equals(Object object) {
        ResourceDescription sResourceDescription = null;

        if (false == (object instanceof ResourceDescription)) {
            return false;
        }

        sResourceDescription = (ResourceDescription) object;

        if (!attributes.equals(sResourceDescription.attributes)) {
            return false;
        }

        if (!resourceDescriptions
                .equals(sResourceDescription.resourceDescriptions)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return attributes.hashCode();
    }

    /**
     * Gets the attributes associated with this instance
     * 
     * @return A {@link java.util.Map}<{@link String}, {@link Object}>, the
     *         {@link SoftwareResourceDescription} associated with this instance
     */
    public Map<String, Object> getDescription() {
        return attributes;
    }

    /**
     * Sets the attributes associated with this instance. Overwrites any earlier
     * attributes.
     * 
     * @param attributes
     *                The new {@link java.util.Map}<{@link String},
     *                {@link Object}>, the {@link SoftwareResourceDescription}
     *                associated with this instance
     */
    public void setDescription(Map<String, Object> attributes) {
        this.attributes = new HashMap<String, Object>(attributes);
    }

    /**
     * Adds the name/value pair to the {@link java.util.Map}<{@link String},
     * {@link Object}> of name/value pairs which describe this {@link Resource}.
     * 
     * @param name
     *                the key, a {@link java.lang.String}, to add to the
     *                name/value pairs which describe this {@link Resource}.
     * @param value
     *                the value, an {@link Object}, to add to the name/value
     *                pairs which describes this {@link Resource}.
     */
    public void addResourceAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Returns an attribute of this description given the key.
     * 
     * @param name
     *                the name of the attribute
     * @return the attribute value, or <code>null</code> if the key is not
     *         present
     */
    public Object getResourceAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Removes the name/value pair with the passed name from the
     * {@link java.util.Map}<{@link String}, {@link Object}> of name/value
     * pairs which describe this {@link Resource}.
     * 
     * @param name
     *                The key, a {@link java.lang.String}, to of the name/value
     *                pair to remove from the name/value pairs which describe
     *                this {@link Resource}.
     */
    public void removeResourceAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * Adds the passed {@link HardwareResourceDescription} to the
     * {@link java.util.List} of {@link HardwareResourceDescription}s which
     * describe this {@link Resource}.
     * 
     * @param hardwareResourceDescription
     *                The {@link HardwareResourceDescription} to add to the
     *                {@link java.util.List} of
     *                {@link HardwareResourceDescription}s which describe this
     *                {@link Resource}.
     */
    public void addResourceDescription(
            ResourceDescription hardwareResourceDescription) {
        resourceDescriptions.add(hardwareResourceDescription);
    }

    /**
     * Removes the passed {@link HardwareResourceDescription} from the
     * {@link java.util.List} of {@link HardwareResourceDescription}s which
     * describe this {@link Resource}.
     * 
     * @param hardwareResourceDescription
     *                The {@link HardwareResourceDescription} to remove from the
     *                {@link java.util.List} of
     *                {@link HardwareResourceDescription}s which describe this
     *                {@link Resource}.
     * @throws NoSuchElementException
     *                 the element to be removed cannot be found
     */
    public void removeResourceDescription(
            ResourceDescription hardwareResourceDescription)
            throws NoSuchElementException {
        resourceDescriptions.remove(hardwareResourceDescription);
    }

    public String toString() {
        String res = "ResourceDescription(";

        res += "attributes: "
                + (attributes == null ? "null" : attributes.toString());
        res += ", resourceDescriptions: "
                + (resourceDescriptions == null ? "null" : resourceDescriptions
                        .toString());

        res += ")";

        return res;
    }
}
