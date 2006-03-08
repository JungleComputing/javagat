package org.gridlab.gat.resources;

import java.util.List;
import java.util.Map;

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
public class SoftwareResourceDescription extends ResourceDescription {

    /**
     * Constructs a SoftwareResourceDescription associated without attributes and 
     * dependencies.
     */
    public SoftwareResourceDescription() {
        super();
    }

    /**
     * Constructs a SoftwareResourceDescription associated with the passed
     * attributes. There are no further dependencies
     *
     * @param attributes A java.util.Map, which describes the attributes of this
     * resource
     */
    public SoftwareResourceDescription(Map attributes) {
        super(attributes);
    }

    /**
     * Constructs a SoftwareResourceDescription associated with the passed
     * objects.
     *
     * @param attributes A java.util.Map, which describes the attributes of this
     * resource
     * 
     * @param resourceDescriptions A java.util.List, which is a list of 
     * resourceDescriptions each of which describes a software component upon 
     * which this software component depends.
     */
    public SoftwareResourceDescription(Map attributes, List resourceDescriptions) {
        super(attributes, resourceDescriptions);
    }
}
