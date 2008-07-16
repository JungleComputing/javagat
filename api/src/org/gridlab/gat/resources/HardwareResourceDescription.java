package org.gridlab.gat.resources;

import java.util.List;
import java.util.Map;

/**
 * An instance of this class is a description of a hardware resource, a physical
 * entity, which a may be required by a hardware or software component.
 * <p>
 * 
 * To clarify the concept of a {@link HardwareResourceDescription}, let us give
 * various examples. Memory is described by a
 * {@link HardwareResourceDescription}; a network is described by a
 * {@link HardwareResourceDescription}; disk space is described by a
 * {@link HardwareResourceDescription}; a monitor is described a
 * {@link HardwareResourceDescription}\ldots However, an application is not
 * described by a {@link HardwareResourceDescription}. In general any resource
 * which corresponds to a physical entity is described by a
 * {@link HardwareResourceDescription}.
 * <p>
 * Hardware is generally useless without software. For example, a disk drive
 * without the appropriate software driver is all but useless. Similarly,
 * hardware often depends upon other hardware. For example, having a disk drive
 * without a computer again is not of much use. Hence, in describing a hardware
 * component one needs to also describe the software and hardware that this
 * hardware component requires. This is reflected in the fact that a
 * {@link HardwareResourceDescription} contains in addition to a description of
 * a hardware component a list of {@link HardwareResourceDescription}s, each
 * element of which describes a hardware component upon which the parent
 * hardware component depends. It also contains a list of
 * {@link SoftwareResourceDescription}s, each element of which describes a
 * software component upon which the parent hardware component depends. Hence,
 * the entire structure is recursive.
 * <p>
 * To construct an instance of a {@link HardwareResourceDescription} one
 * requires three quantities:
 * <ul>
 * <li>A map which contains a set of name/value pairs, detailed later, which
 * describe a hardware resource.</li>
 * <li>A list of {@link SoftwareResourceDescription} instances each of which
 * describes a software resource upon which the parent hardware resource
 * depends.</li>
 * <li>A list of {@link HardwareResourceDescription} instances each of which
 * describes a hardware resource upon which the parent hardware resource
 * depends.</li>
 * </ul>
 * The GAT-API defines a minimum set of supported name/value pairs which can be
 * included in the Map used to construct a HardwareResourceDescription instance.
 * This minimum set of name/value pairs MUST be supported by any implementation
 * of the GAT-API. This minimum set of supported name/values is given in the
 * table.
 * <p>
 * <TABLE border="2" frame="box" rules="groups" summary="Minimum set of
 * supported name/value pairs"> <CAPTION>Minimum set of supported name/value
 * pairs </CAPTION> <COLGROUP align="left"> <COLGROUP align="center"> <COLGROUP
 * align="left" > <THEAD valign="top">
 * <TR>
 * <TH>Name
 * <TH>Type
 * <TH>Description <TBODY>
 * <TR>
 * <TD><code>memory.size</code>
 * <TD>{@link java.lang.Float}
 * <TD>The minimum memory in GB.
 * <TR>
 * <TD><code>memory.accesstime</code>
 * <TD>{@link java.lang.Float}
 * <TD>The minimum memory access time in ns.
 * <TR>
 * <TD><code>memory.str</code>
 * <TD>{@link java.lang.Float}
 * <TD>The minimum sustained transfer rate in GB/s.
 * <TR>
 * <TD><code>machine.type</code>
 * <TD>{@link java.lang.String}
 * <TD>The machine type as returned from <code>uname -m</code>
 * <TR>
 * <TD><code>machine.node</code>
 * <TD>{@link java.lang.String}
 * <TD>The machine node as returned from <code>uname -n</code>,
 * alternatively, this can be an array of {@link java.lang.String}s, if the job
 * can run on multiple hosts.
 * <TR>
 * <TD><code>cpu.type</code>
 * <TD>{@link java.lang.String}
 * <TD>The generic cpu type as returned from <code>uname -p</code>
 * <TR>
 * <TD><code>cpu.count</code>
 * <TD>{@link Integer}
 * <TD>the number of proccessors to use
 * <TR>
 * <TD><code>cpu.speed</code>
 * <TD>{@link java.lang.Float}
 * <TD>The minimum cpu speed in GHz.
 * <TR>
 * <TD><code>disk.size</code>
 * <TD>{@link java.lang.Float}
 * <TD>The minimum size of the hard drive in GB.
 * <TR>
 * <TD><code>disk.accesstime</code>
 * <TD>{@link java.lang.Float}
 * <TD>The minimum disk access time in ms.
 * <TR>
 * <TD><code>disk.str</code>
 * <TD>{@link java.lang.Float}
 * <TD>The minimum sustained transfer rate in MB/s. </TBODY></TABLE>
 */
@SuppressWarnings("serial")
public class HardwareResourceDescription extends ResourceDescription {
    /**
     * Constructs a {@link HardwareResourceDescription} associated without
     * attributes and dependencies.
     */
    public HardwareResourceDescription() {
        super();
    }

    /**
     * Constructs a {@link HardwareResourceDescription} associated with the
     * passed attributes. There are no further dependencies
     * 
     * @param attributes
     *                A {@link java.util.Map}<{@link String}, {@link Object}>,
     *                which describes the attributes of this {@link Resource}.
     */
    public HardwareResourceDescription(Map<String, Object> attributes) {
        super(attributes);
    }

    /**
     * Constructs a HardwareResourceDescription associated with the passed
     * objects.
     * 
     * @param attributes
     *                A {@link java.util.Map}<{@link String}, {@link Object}>,
     *                which describes the attributes of this {@link Resource}.
     * 
     * @param resourceDescriptions
     *                A {@link java.util.List}, which is a list of
     *                {@link ResourceDescription}s each of which describes a
     *                software component upon which this software component
     *                depends.
     */
    public HardwareResourceDescription(Map<String, Object> attributes,
            List<ResourceDescription> resourceDescriptions) {
        super(attributes, resourceDescriptions);
    }
}
