package hu.sztaki.lpds.monitor;

/**
 * Represents a formal parameter that has a name and a data type.
 *
 * @author G??bor Gomb??s
 * @version $Id: MonitorParameter.java 857 2006-04-19 09:43:22Z rob $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class MonitorParameter {
    /** The name of the parameter. */
    private String name;

    /** The type of the parameter. */
    private BasicType type;

    /** If true, this parameter may be specified multiple times. */
    private boolean multivalued;

    /** If true, this parameter may be omitted. */
    private boolean optional;

    /**
     * Constructs a new <code>MonitorParameter</code>.
     *
     * @param name                the name of the parameter.
     * @param type                the type of the parameter.
     * @param multivalued        if <code>true</code>, this parameter may be
     *                        specified multiple times.
     * @param optional        if <code>true</code>, this parameter may be
     *                        omitted.
     */
    public MonitorParameter(String name, BasicType type, boolean multivalued,
            boolean optional) {
        this.name = name;
        this.type = type;
        this.multivalued = multivalued;
        this.optional = optional;
    }

    /**
     * Returns the name of the parameter.
     *
     * @return                the parameter's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the parameter.
     *
     * @return                the parameter's type.
     */
    public BasicType getType() {
        return type;
    }

    /**
     * Checks if this parameter is multi-valued.
     *
     * @return                <code>true</code> if this parameter may be
     *                        specified multiple times.
     */
    public boolean isMultiValued() {
        return multivalued;
    }

    /**
     * Checks if this parameter is optional.
     *
     * @return                <code>true</code> if this parameter may be
     *                        omitted.
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * Converts the parameter definition to a textual form.
     *
     * @return                the parameter definition as a string.
     */
    public String toString() {
        String val = type + " " + name;

        if (optional) {
            val += " OPTIONAL";
        }

        if (multivalued) {
            val += " MULTIVAL";
        }

        return val;
    }
}
