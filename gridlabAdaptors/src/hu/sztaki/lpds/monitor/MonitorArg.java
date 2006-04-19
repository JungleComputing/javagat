package hu.sztaki.lpds.monitor;

/**
 * Represents a metric argument (a name-value pair).
 *
 * Metric arguments define the actual value of the formal metric parameters.
 * Every argument has a name, a data type and a value.
 *
 * @author G??bor Gomb??s
 * @version $Id$
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class MonitorArg {
    /** The name of the argument. */
    private String name;

    /** The value of the argument. */
    private Object value;

    /** The monitoring type of the argument. Used by the native code. */
    private BasicType type;

    /**
     * Constructs a new <code>MonitorArg</code>.
     *
     * @param name                the name of the argument.
     * @param value                the value of the argument.
     *
     * @throws IllegalArgumentException if the class of the value cannot be mapped
     *        to a simple Mercury type.
     * @see BasicType
     */
    public MonitorArg(String name, Object value) {
        this.name = name;
        this.value = value;

        /* This makes it easier for the native code to check
         * the type of the argument */
        if (value instanceof Integer) {
            this.type = BasicType.INT32;
        } else if (value instanceof UnsignedInteger) {
            this.type = BasicType.UINT32;
        } else if (value instanceof Long) {
            this.type = BasicType.INT64;
        } else if (value instanceof UnsignedLong) {
            this.type = BasicType.UINT64;
        } else if (value instanceof Double) {
            this.type = BasicType.DOUBLE;
        } else if (value instanceof Boolean) {
            this.type = BasicType.BOOLEAN;
        } else if (value instanceof String) {
            this.type = BasicType.STRING;
        } else {
            throw new IllegalArgumentException("Unknown data type");
        }
    }

    /**
     * Returns the name of the argument.
     *
     * @return                the argument's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the argument.
     *
     * @return                the argument's value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the type of the argument.
     *
     * @return                the argument's type.
     */
    public BasicType getType() {
        return type;
    }

    /**
     * Converts the argument to a textual form.
     *
     * @return                the argument as a string.
     */
    public String toString() {
        return type + " " + name + " = " + value;
    }
}
