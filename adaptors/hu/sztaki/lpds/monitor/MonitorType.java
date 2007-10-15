package hu.sztaki.lpds.monitor;

/**
 * Represents a node in a Mercury type definition tree.
 *
 * <code>MonitorType</code> is the base class of Mercury data types.
 * Mercury data types are represented as trees where leaf nodes are simple
 * types and non-leaf nodes are compound types whose components are specified
 * by their children in the tree. <code>MonitorType</code> represents a
 * generic node in the tree.
 *
 * @author G??bor Gomb??s
 * @version $Id$
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public abstract class MonitorType {
    /** The name of this (sub)type. */
    private String name;

    /**
     * Constructs a new type descriptior.
     *
     * @param name                the name of this (sub)type.
     */
    public MonitorType(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this node.
     *
     * @return                the name of this node.
     */
    public String getName() {
        return name;
    }

    /**
     * Parses a Mercury type specification.
     *
     * @param type                the type specification as a string.
     * @return                the type descriptor a Java object.
     *
     * @throws MonitorException if the passed string is not a valid Mercury
     *        type specification.
     */
    public static native MonitorType parseType(String type)
            throws MonitorException;

    /**
     * Creates the textual representation of the monitor type, without
     * a terminating semicolon. This method is invoked by the
     * {@link #toString()} method internally.
     *
     * @param str                a <code>StringBuffer</code> to hold the result.
     */
    protected abstract void internalToString(StringBuffer str);

    /**
     * Creates the textual representation of an object according to the
     * type definition. This method is invoked by the
     * {@link #toString(Object)} method internally.
     *
     * @param str                a <code>StringBuffer</code> to hold the result.
     * @param value                the object to stringify.
     *
     * @throws ClassCastException if the passed object does not conform
     *        to the type description.
     */
    protected abstract void internalToString(StringBuffer str, Object value);

    /**
     * Returns the textual representation of the monitor type.
     *
     * @return                the type as a string.
     */
    public final String toString() {
        StringBuffer str = new StringBuffer();
        internalToString(str);
        str.append(";");

        return str.toString();
    }

    /**
     * Returns the textual representation of an object according to the
     * type definition.
     *
     * @param value                the value to stringify.
     * @return                the type as a string.
     *
     * @throws ClassCastException if the passed object does not conform
     *        to the type description.
     */
    public final String toString(Object value) {
        StringBuffer str = new StringBuffer();
        internalToString(str, value);

        return str.toString();
    }
};
