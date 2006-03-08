package org.gridlab.gat.resources;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An instance of this class is a description of a software resource, which may
 * be required by a hardware or software component. It does not describe the
 * application itself, the SoftwareDescription is used for that.
 * <p>
 * To clarify this concept, let us give various examples. A software library is
 * described by a SoftwareResourceDescription; a helper application is described
 * by a SoftwareResourceDescription; a plugin is described a
 * SoftwareResourceDescription... However, a piece of hardware is not described
 * by a SoftwareResourceDescription.
 * <p>
 * Software is generally useless without hardware. For example, a software
 * driver without the appropriate Disk drive is all but useless. Similarly,
 * software often depends upon other software. For example, having a Photoshop
 * without any plugins is not of much use. Hence, in describing a software
 * component one needs to also describe the software and hardware that this
 * software component requires. This is reflected in the fact that a
 * SoftwareResourceDescription contains in addition to a "parent" description of
 * a software component a list of HardwareResourceDescriptions, each element of
 * which describes a hardware component upon which the parent software component
 * depends, and a list of SoftwareResourceDescriptions, each element of which
 * describes a software component upon which the parent software component
 * depends. Hence, the entire structure is recursive.
 * <p>
 * To construct an instance of a SoftwareResourceDescription one requires three
 * quantities:
 * <ul>
 * <li>A map which contains a set of name/value pairs, detailed later, which
 * describe a software resource.</li>
 * <li>A list of SoftwareResourceDescription instances each of which describes
 * a software resource upon which the parent software resource depends.</li>
 * <li>A list of HardwareResourceDescription instances each of which describes
 * a hardware resource upon which the parent software resource depends.</li>
 * </ul>
 * <p>
 * The GAT-API defines a minimum set of supported name/value pairs which can be
 * included in the map used to construct a SoftwareResourceDescription instance.
 * This minimum set of name/value pairs MUST be supported by any implementation
 * of the GAT-API. This minimum set of supported name/values is given in the
 * table
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
 * <TD>os.name
 * <TD>java.lang.String
 * <TD>The os name as returned from uname -s
 * <TR>
 * <TD>os.type
 * <TD>java.lang.String
 * <TD>The os type as returned from uname -p
 * <TR>
 * <TD>os.version
 * <TD>java.lang.String
 * <TD>The os version as returned from uname -v
 * <TR>
 * <TD>os.release
 * <TD>java.lang.String
 * <TD>The os release as returned from uname -r
 * <TR></TBODY> </TABLE>
 */
public class SoftwareResourceDescription implements ResourceDescription {

	/**
	 * This member variable holds the Map which describes the "parent" software
	 * component
	 */
	private Map softwareResourceDescription = null;

	/**
	 * This member variable holds a List of SoftwareResourceDescription's
	 * describing each of which describes a software component upon which the
	 * "parent" software component depends.
	 */
	private List softwareResourceDescriptions = null;

	/**
	 * This member variable holds a List of HardwareResourceDescription's
	 * describing each of which describes a hardware component upon which the
	 * "parent" software component depends.
	 */
	private List hardwareResourceDescriptions = null;

	/**
	 * Constructs a SoftwareResourceDescription associated with the passed
	 * objects:
	 * <ul>
	 * <li><em>SoftwareResourceDescription</em> --- A java.util.Map, which
	 * describes the "parent" software component.</li>
	 * <li><em>SoftwareResourceDescriptions</em> --- A java.util.List, which
	 * is a list of SoftwareResourceDescriptions each of which describes a
	 * software component upon which the "parent" software component depends.
	 * </li>
	 * <li>
	 * <em>HardwareResourceDescriptions</li> --- A java.util.List, which is a list 
	 * of HardwareResourceDescriptions each of which describes a hardware component 
	 * upon which the "parent" software component depends.</li>
	 * </ul>
	 *
	 * @param softwareResourceDescription A java.util.Map, which describes the "parent"
	 * software component.
	 * @param softwareResourceDescriptions A java.util.List, which is a list of 
	 * SoftwareResourceDescriptions each of which describes a software component upon 
	 * which the "parent" software component depends.
	 * @param hardwareResourceDescriptions A java.util.List, which is a list of 
	 * HardwareResourceDescriptions each of which describes a hardware component upon 
	 * which the "parent" software component depends.
	 */
	public SoftwareResourceDescription(Map softwareResourceDescription,
			List softwareResourceDescriptions, List hardwareResourceDescriptions) {
		this.softwareResourceDescription = softwareResourceDescription;
		this.softwareResourceDescriptions = softwareResourceDescriptions;
		this.hardwareResourceDescriptions = hardwareResourceDescriptions;
	}

	/**
	 * Tests this SoftwareResourceDescription for equality with the passed
	 * Object.
	 * <p>
	 * If the given object is not a SoftwareResourceDescription, then this
	 * method immediately returns false.
	 * <p>
	 * If the passed object is a SoftwareResourceDescription, then it is deemed
	 * equal if it has an equivalent SoftwareResourceDescription, as determined
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
		SoftwareResourceDescription sResourceDescription = null;

		if (false == (object instanceof SoftwareResourceDescription))
			return false;

		sResourceDescription = (SoftwareResourceDescription) object;

		if (false == softwareResourceDescription
				.equals(sResourceDescription.softwareResourceDescription))
			return false;
		if (false == softwareResourceDescriptions
				.equals(sResourceDescription.softwareResourceDescriptions))
			return false;
		if (false == hardwareResourceDescriptions
				.equals(sResourceDescription.hardwareResourceDescriptions))
			return false;

		return true;
	}

	/**
	 * Gets the SoftwareResourceDescription associated with this instance
	 * 
	 * @return A java.util.Map, the SoftwareResourceDescription associated with
	 *         this instance
	 */
	public Map getDescription() {
		return softwareResourceDescription;
	}

	/**
	 * Sets the SoftwareResourceDescription associated with this instance
	 * 
	 * @param The
	 *            new java.util.Map, the SoftwareResourceDescription associated
	 *            with this instance
	 */
	public void setDescription(Map softwareResourceDescription) {
		this.softwareResourceDescription = softwareResourceDescription;
	}

	/**
	 * Adds the name/value pair to the java.util.Map of name/value pairs which
	 * describe the "parent" software component.
	 * 
	 * @param name
	 *            The Name, a java.lang.String, to add to the name/value pairs
	 *            which describe the "parent" software component.
	 * @param value
	 *            The Value, an Object, to add to the name/value pairs which
	 *            describe the "parent" software component.
	 */
	public void addResourceAttribute(String name, Object value) {
		softwareResourceDescription.put(name, value);
	}

	/**
	 * Removes the name/value pair with the passed name from the java.util.Map
	 * of name/value pairs which describe the "parent" software component.
	 * 
	 * @param name
	 *            The Name, a java.lang.String, to of the name/value pair to
	 *            remove from the name/value pairs which describe the "parent"
	 *            software component.
	 */
	public void removeResourceAttribute(String name) {
		softwareResourceDescription.remove(name);
	}

	/**
	 * Adds the passed HardwareResourceDescription to the java.util.List of
	 * HardwareResourceDescriptions which describe this
	 * SoftwareResourceDescription.
	 * 
	 * @param hardwareResourceDescription
	 *            The HardwareResourceDescription to add to the java.util.List
	 *            of HardwareResourceDescriptions which describe this
	 *            SoftwareResourceDescription.
	 */
	public void addResourceDescription(
			ResourceDescription hardwareResourceDescription) {
		hardwareResourceDescriptions.add(hardwareResourceDescription);
	}

	/**
	 * Removes the passed HardwareResourceDescription from the java.util.List of
	 * HardwareResourceDescriptions which describe this
	 * SoftwareResourceDescription.
	 * 
	 * @param hardwareResourceDescription
	 *            The HardwareResourceDescription to remove from the
	 *            java.util.List of HardwareResourceDescriptions which describe
	 *            this SoftwareResourceDescription.
	 */
	public void removeResourceDescription(
			ResourceDescription hardwareResourceDescription)
			throws NoSuchElementException {
		hardwareResourceDescriptions.remove(hardwareResourceDescription);
	}

	/**
	 * Sets the java.util.List of HardwareResourceDescriptions which describe
	 * this SoftwareResourceDescription to the passed java.util.List.
	 * 
	 * @param hardwareResourceDescriptions
	 *            The new java.util.List of HardwareResourceDescriptions which
	 *            describe this SoftwareResourceDescription.
	 */
	public void setHardwareResourceDescriptions(
			List hardwareResourceDescriptions) {
		this.hardwareResourceDescriptions = hardwareResourceDescriptions;
	}

	/**
	 * Gets the java.util.List of HardwareResourceDescriptions which describe
	 * this SoftwareResourceDescription.
	 * 
	 * @return The java.util.List of HardwareResourceDescriptions which describe
	 *         this SoftwareResourceDescription.
	 */
	public List getHardwareResourceDescriptions() {
		return hardwareResourceDescriptions;
	}

	/**
	 * Adds the passed SoftwareResourceDescription to the java.util.List of
	 * SoftwareResourceDescriptions which describe this
	 * SoftwareResourceDescription.
	 * 
	 * @param softwareResourceDescription
	 *            The SoftwareResourceDescription to add to the java.util.List
	 *            of SoftwareResourceDescriptions which describe this
	 *            SoftwareResourceDescription.
	 */
	public void addSoftwareResourceDescription(
			SoftwareResourceDescription softwareResourceDescription) {
		softwareResourceDescriptions.add(softwareResourceDescription);
	}

	/**
	 * Removes the passed SoftwareResourceDescription from the java.util.List of
	 * SoftwareResourceDescriptions which describe this
	 * SoftwareResourceDescription.
	 * 
	 * @param softwareResourceDescription
	 *            The SoftwareResourceDescription to remove from the
	 *            java.util.List of SoftwareResourceDescriptions which describe
	 *            this SoftwareResourceDescription.
	 */
	public void removeSoftwareResourceDescription(
			SoftwareResourceDescription softwareResourceDescription) {
		softwareResourceDescriptions.remove(softwareResourceDescription);
	}

	/**
	 * Sets the java.util.List of SoftwareResourceDescriptions which describe
	 * this SoftwareResourceDescription to the passed java.util.List.
	 * 
	 * @param softwareResourceDescriptions
	 *            The new java.util.List of SoftwareResourceDescriptions which
	 *            describe this SoftwareResourceDescription.
	 */
	public void setSoftwareResourceDescriptions(
			List softwareResourceDescriptions) {
		this.softwareResourceDescriptions = softwareResourceDescriptions;
	}

	/**
	 * Gets the java.util.List of SoftwareResourceDescriptions which describe
	 * this SoftwareResourceDescription.
	 * 
	 * @return The java.util.List of SoftwareResourceDescriptions which describe
	 *         this SoftwareResourceDescription.
	 */
	public List getSoftwareResourceDescriptions() {
		return softwareResourceDescriptions;
	}
}