package hu.sztaki.lpds.monitor;

/**
 * Represents a 32-bit unsigned integer value.
 *
 * This class uses <code>long</code> internally to hold the unsigned value.
 *
 * @author G??bor Gomb??s
 * @version $Id$
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
@SuppressWarnings({ "serial", "unchecked" })
public class UnsignedInteger extends Number implements Comparable {
    /** The numeric value of the object. */
    private long value;

    /**
     * Creates a new <code>UnsignedInteger</code> from a <code>long</code>.
     *
     * @param value                the unsigned integer value.
     *
     * @throws IllegalArgumentException if the value is negative or cannot
     *        be represented on 32 bits.
     */
    public UnsignedInteger(long value) {
        if ((value < 0) || (value > 4294967295L)) {
            throw new IllegalArgumentException("Value cannot be "
                + "represented as a 32-bit unsigned integer");
        }

        this.value = value;
    }

    /**
     * Converts this <code>UnsignedInteger</code> to a <code>byte</code>.
     *
     * @return                the <code>UnsignedInteger</code> as a
     *                        <code>byte</code>.
     */
    public byte byteValue() {
        return (byte) value;
    }

    /**
     * Converts this <code>UnsignedInteger</code> to an <code>int</code>.
     *
     * @return                the <code>UnsignedInteger</code> as an
     *                        <code>int</code>.
     */
    public int intValue() {
        return (int) value;
    }

    /**
     * Converts this <code>UnsignedInteger</code> to a <code>long</code>.
     *
     * @return                the <code>UnsignedInteger</code> as a
     *                        <code>long</code>.
     */
    public long longValue() {
        return value;
    }

    /**
     * Converts this <code>UnsignedInteger</code> to a <code>float</code>.
     *
     * @return                 the <code>UnsignedInteger</code> as a
     *                        <code>float</code>.
     */
    public float floatValue() {
        return value;
    }

    /**
     * Converts this <code>UnsignedInteger</code> to a <code>double</code>.
     *
     * @return                the <code>UnsignedInteger</code> as a
     *                        <code>double</code>.
     */
    public double doubleValue() {
        return value;
    }

    /**
     * Tests the equality of two <code>UnsignedInteger</code> values.
     *
     * @param obj                the object to compare to.
     * @return                <code>true</code> if the passed object equals
     *                        to this <code>UnsignedInteger</code>.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof UnsignedInteger)) {
            return false;
        }

        return value == ((UnsignedInteger) obj).value;
    }

    /**
     * Returns a hash code for this <code>UnsignedInteger</code>.
     *
     * @return                a hash code value for this object.
     */
    public int hashCode() {
        return new Long(value).hashCode();
    }

    /**
     * Compares two <code>UnsignedInteger</code> values.
     *
     * @param obj                the value to compare to.
     * @return                -1, 0, or 1 if this <code>UnsignedInteger</code>
     *                        is smaller, equal or larger than the passed
     *                        value.
     *
     * @throws NullPointerException if the passed object is <code>null</code>.
     */
    public int compareTo(Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }

        if (((UnsignedInteger) obj).value == value) {
            return 0;
        }

        return (value < ((UnsignedInteger) obj).value) ? (-1) : 1;
    }

    /**
     * Converts this <code>UnsignedInteger</code> to a string
     *
     * @return                the <code>UnsignedInteger</code> as a string.
     */
    public String toString() {
        return Long.toString(value);
    }
}
