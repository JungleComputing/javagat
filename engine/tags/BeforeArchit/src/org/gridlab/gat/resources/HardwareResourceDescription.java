package org.gridlab.gat.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An instance of this class is a description of a hardware resource, a physical
 * thing, which a may be required by a hardware or software component.
 * <p>
 * 
 * To clarify the concept of a HardwareResourceDescription, let us give various
 * examples. Memory is described by a HardwareResourceDescription; a network is
 * described by a HardwareResourceDescription; disk space is described by a
 * HardwareResourceDescription; a monitor is described a
 * HardwareResourceDescription\ldots However, an application is not described by
 * a HardwareResourceDescription. In general any resource which corresponds to a
 * physical thing is described by a HardwareResourceDescription.
 * <p>
 * Hardware is generally useless without software. For example, a disk drive
 * without the appropriate software driver is all but useless. Similarly,
 * hardware often depends upon other hardware. For example, having a disk drive
 * without a computer again is not of much use. Hence, in describing a hardware
 * component one needs to also describe the software and hardware that this
 * hardware component requires. This is reflected in the fact that a
 * HardwareResourceDescription contains in addition to a description of a
 * hardware component a list of HardwareResourceDescriptions, each element of
 * which describes a hardware component upon which the parent hardware component
 * depends, and a list of SoftwareResourceDescriptions, each element of which
 * describes a software component upon which the parent hardware component
 * depends. Hence, the entire structure is recursive.
 * <p>
 * To construct an instance of a HardwareResourceDescription one requires three
 * quantities:
 * <ul>
 * <li>A map which contains a set of name/value pairs, detailed later, which
 * describe a hardware resource.</li>
 * <li>A list of SoftwareResourceDescription instances each of which describes
 * a software resource upon which the parent hardware resource depends.</li>
 * <i>A list of HardwareResourceDescription instances each of which describes a
 * hardware resource upon which the parent hardware resource depends.</li>
 * </ul>
 * The GAT-API defines a minimum set of supported name/value pairs which can be
 * included in the java.util.Map used to construct a HardwareResourceDescription
 * instance. This minimum set of name/value pairs MUST be supported by any
 * implementation of the GAT-API. This minimum set of supported name/values is
 * given in the table
 * 
 * <TABLE border="2" frame="box" rules="groups" summary="Minimum set of
 * supported name/value pairs"> <CAPTION>Minimum set of supported name/value
 * pairs </CAPTION> <COLGROUP align="left"> <COLGROUP align="center"> <COLGROUP
 * align="left" > <THEAD valign="top">
 * <TR>
 * <TH>Name
 * <TH>Type
 * <TH>Description <TBODY>
 * <TR>
 * <TD>memory.size
 * <TD>java.lang.Float
 * <TD>The minimum memory in GB.
 * <TR>
 * <TD>memory.accesstime
 * <TD>java.lang.Float
 * <TD>The minimum memory access time in ns.
 * <TR>
 * <TD>memory.str
 * <TD>java.lang.Float
 * <TD>The minimum sustained transfer rate in GB/s.
 * <TR>
 * <TD>machine.type
 * <TD>java.lang.String
 * <TD>The machine type as returned from uname -m
 * <TR>
 * <TD>machine.node
 * <TD>java.lang.String
 * <TD>The machine node as returned from uname -n
 * <TR>
 * <TD>cpu.type
 * <TD>java.lang.String
 * <TD>The generic cpu type as returned from uname -p
 * <TR>
 * <TD>cpu.speed
 * <TD>java.lang.Float
 * <TD>The minimum cpu speed in GHz.
 * <TR>
 * <TD>disk.size
 * <TD>java.lang.Float
 * <TD>The minimum size of the hard drive in GB.
 * <TR>
 * <TD>disk.accesstime
 * <TD>java.lang.Float
 * <TD>The minimum disk access time in ms.
 * <TR>
 * <TD>disk.str
 * <TD>java.lang.Float
 * <TD>The minimum sustained transfer rate in MB/s. <TBODY></TABLE>
 */
public class HardwareResourceDescription implements ResourceDescription {

	/**
	 * This member variable holds the Map which describes the "parent" hardware
	 * component
	 */
	private Map hardwareResourceDescription = null;

	/**
	 * This member variable holds a List of SoftwareResourceDescription's
	 * describing each of which describes a software component upon which the
	 * "parent" hardware component depends.
	 */
	private List softwareResourceDescriptions = new ArrayList();

	/**
	 * This member variable holds a List of HardwareResourceDescription's
	 * describing each of which describes a hardware component upon which the
	 * "parent" hardware component depends.
	 */
	private List hardwareResourceDescriptions = new ArrayList();

	/**
	 * Constructs a HardwareResourceDescription associated with the passed
	 * objects:
	 * <ul>
	 * <li><em>HardwareResourceDescription</em> --- A java.util.Map, which
	 * describes the "parent" hardware component.</li>
	 * <li><em>SoftwareResourceDescriptions</em> --- A java.util.List, which
	 * is a list of SoftwareResourceDescriptions each of which describes a
	 * software component upon which the "parent" hardware component depends.
	 * </li>
	 * <li>
	 * <em>HardwareResourceDescriptions</li> --- A java.util.List, which is a list 
	 * of HardwareResourceDescriptions each of which describes a hardware component 
	 * upon which the "parent" hardware component depends.</li>
	 * </ul>
	 *
	 * @param hardwareResourceDescription A java.util.Map, which describes the "parent"
	 * hardware component.
	 * @param softwareResourceDescriptions A java.util.List, which is a list of 
	 * SoftwareResourceDescriptions each of which describes a software component upon 
	 * which the "parent" hardware component depends.
	 * @param hardwareResourceDescriptions A java.util.List, which is a list of 
	 * HardwareResourceDescriptions each of which describes a hardware component upon 
	 * which the "parent" hardware component depends.
	 */
	public HardwareResourceDescription(Map hardwareResourceDescription) {
		this.hardwareResourceDescription = hardwareResourceDescription;
	}

	/**
	 * Tests this HardwareResourceDescription for equality with the passed
	 * Object.
	 * <p>
	 * If the given object is not a HardwareResourceDescription, then this
	 * method immediately returns false.
	 * <p>
	 * If the passed object is a HardwareResourceDescription, then it is deemed
	 * equal if it has an equivalent HardwareResourceDescription, as determined
	 * by the Equals method on java.util.Map, and an equivalent
	 * SoftwareResourceDescriptions, as determined by the Equals method on
	 * java.util.List, and an equivalent HardwareResourceDescriptions, as
	 * determined by the Equals method on java.util.List.
	 * 
	 * @param object
	 *            The Object to test for equality
	 * @return A boolean indicating equality
	 */
	public boolean equals(Object object) {
		HardwareResourceDescription hResourceDescription = null;

		if (false == (object instanceof HardwareResourceDescription))
			return false;

		hResourceDescription = (HardwareResourceDescription) object;

		if (false == hardwareResourceDescription
				.equals(hResourceDescription.hardwareResourceDescription))
			return false;
		if (false == softwareResourceDescriptions
				.equals(hResourceDescription.softwareResourceDescriptions))
			return false;
		if (false == hardwareResourceDescriptions
				.equals(hResourceDescription.hardwareResourceDescriptions))
			return false;

		return true;
	}

	/**
	 * Returns the HardwareResourceDescription of this instance
	 * 
	 * @return The HardwareResourceDescription of this instance
	 */
	public Map getDescription() {
		return hardwareResourceDescription;
	}

	/**
	 * Sets the HardwareResourceDescription of this instance
	 * 
	 * @param hrd
	 *            The HardwareResourceDescription of this instance
	 */
	public void setDescription(Map hrd) {
		hardwareResourceDescription = hrd;
	}

	/**
	 * Adds the name/value pair to the java.util.Map of name/value pairs which
	 * describe the "parent" hardware component.
	 * 
	 * @param name
	 *            The Name, a java.lang.String, to add to the name/value pairs
	 *            which describe the "parent" hardware component.
	 * @param value
	 *            The Value, an Object, to add to the name/value pairs which
	 *            describe the "parent" hardware component.
	 */
	public void addResourceAttribute(String name, Object value) {
		hardwareResourceDescription.put(name, value);
	}

	/**
	 * Removes the name/value pair with the passed name from the java.util.Map
	 * of name/value pairs which describe the "parent" hardware component.
	 * 
	 * @param name
	 *            The Name, a java.lang.String, to of the name/value pair to
	 *            remove from the name/value pairs which describe the "parent"
	 *            hardware component.
	 */
	public void removeResourceAttribute(String name)
			throws NoSuchElementException {
		hardwareResourceDescription.remove(name);
	}

	/**
	 * Adds the passed HardwareResourceDescription to the java.util.List of
	 * HardwareResourceDescriptions which describe this
	 * HardwareResourceDescription.
	 * 
	 * @param hardwareResourceDescription
	 *            The HardwareResourceDescription to add to the java.util.List
	 *            of HardwareResourceDescriptions which describe this
	 *            HardwareResourceDescription.
	 */
	public void addResourceDescription(
			ResourceDescription hardwareResourceDescription) {
		hardwareResourceDescriptions.add(hardwareResourceDescription);
	}

	/**
	 * Removes the passed HardwareResourceDescription from the java.util.List of
	 * HardwareResourceDescriptions which describe this
	 * HardwareResourceDescription.
	 * 
	 * @param hardwareResourceDescription
	 *            The HardwareResourceDescription to remove from the
	 *            java.util.List of HardwareResourceDescriptions which describe
	 *            this HardwareResourceDescription.
	 */
	public void removeResourceDescription(
			ResourceDescription hardwareResourceDescription) {
		hardwareResourceDescriptions.remove(hardwareResourceDescription);
	}

	/**
	 * Sets the java.util.List of HardwareResourceDescriptions which describe
	 * this HardwareResourceDescription to the passed java.util.List.
	 * 
	 * @param hardwareResourceDescriptions
	 *            The new java.util.List of HardwareResourceDescriptions which
	 *            describe this HardwareResourceDescription.
	 */
	public void setHardwareResourceDescriptions(
			List hardwareResourceDescriptions) {
		this.hardwareResourceDescriptions = hardwareResourceDescriptions;
	}

	/**
	 * Gets the java.util.List of HardwareResourceDescriptions which describe
	 * this HardwareResourceDescription.
	 * 
	 * @return The java.util.List of HardwareResourceDescriptions which describe
	 *         this HardwareResourceDescription.
	 */
	public List getHardwareResourceDescriptions() {
		return hardwareResourceDescriptions;
	}

	/**
	 * Adds the passed SoftwareResourceDescription to the java.util.List of
	 * SoftwareResourceDescriptions which describe this
	 * HardwareResourceDescription.
	 * 
	 * @param softwareResourceDescription
	 *            The SoftwareResourceDescription to add to the java.util.List
	 *            of SoftwareResourceDescriptions which describe this
	 *            HardwareResourceDescription.
	 */
	public void addSoftwareResourceDescription(
			SoftwareResourceDescription softwareResourceDescription) {
		softwareResourceDescriptions.add(softwareResourceDescription);
	}

	/**
	 * Removes the passed SoftwareResourceDescription from the java.util.List of
	 * SoftwareResourceDescriptions which describe this
	 * HardwareResourceDescription.
	 * 
	 * @param softwareResourceDescription
	 *            The SoftwareResourceDescription to remove from the
	 *            java.util.List of SoftwareResourceDescriptions which describe
	 *            this HardwareResourceDescription.
	 */
	public void removeSoftwareResourceDescription(
			SoftwareResourceDescription softwareResourceDescription) {
		softwareResourceDescriptions.remove(softwareResourceDescription);
	}

	/**
	 * Sets the java.util.List of SoftwareResourceDescriptions which describe
	 * this HardwareResourceDescription to the passed java.util.List.
	 * 
	 * @param softwareResourceDescriptions
	 *            The new java.util.List of SoftwareResourceDescriptions which
	 *            describe this HardwareResourceDescription.
	 */
	public void setSoftwareResourceDescriptions(
			List softwareResourceDescriptions) {
		this.softwareResourceDescriptions = softwareResourceDescriptions;
	}

	/**
	 * Gets the java.util.List of SoftwareResourceDescriptions which describe
	 * this HardwareResourceDescription.
	 * 
	 * @return The java.util.List of SoftwareResourceDescriptions which describe
	 *         this HardwareResourceDescription.
	 */
	public List getSoftwareResourceDescriptions() {
		return softwareResourceDescriptions;
	}
}