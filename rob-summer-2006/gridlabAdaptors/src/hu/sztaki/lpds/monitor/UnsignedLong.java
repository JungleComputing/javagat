package hu.sztaki.lpds.monitor;

import java.math.BigInteger;

/**
 * Represents a 64-bit unsigned integer value.
 *
 * This class uses {@link BigInteger} to store the value.
 *
 * @author G??bor Gomb??s
 * @version $Id$
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class UnsignedLong extends Number implements Comparable {
    /* The real value */
    private BigInteger value;

    /**
     * Translates a byte array containing the two's-complement binary representation of an
     * unsigned 64-bit integer to an <code>UnsignedLong</code>.
     *
     * @param value                the binary representation in big-endian byte order.
     */
    public UnsignedLong(byte[] value) {
        if (value.length > 8) {
            throw new IllegalArgumentException("Value cannot be "
                + "represented as a 64-bit unsigned integer");
        }

        /* We explicitely give the signum to avoid the first bit of the
         * value being interpreted as a sign bit */
        this.value = new BigInteger(1, value);
    }

    /**
     * Converts this <code>UnsignedLong</code> to <code>byte[8]</code>.
     *
     * @return                the <code>UnsignedLong</code> as a byte array of length 8.
     */
    public byte[] toByteArray() {
        byte[] val = value.toByteArray();

        if (val.length != 8) {
            byte[] tmp = new byte[8];
            int len = val.length;

            if (len > 8) {
                len = 8;
            }

            System.arraycopy(val, 0, tmp, 0, len);
            val = tmp;
        }

        return val;
    }

    /**
     * Converts this <code>UnsignedLong</code> to a <code>byte</code>.
     *
     * @return                the <code>UnsignedLong</code> as a
     *                        <code>byte</code>.
     */
    public byte byteValue() {
        return value.byteValue();
    }

    /**
     * Converts this <code>UnsignedLong</code> to an <code>int</code>.
     *
     * @return                the <code>UnsignedLong</code> as an
     *                        <code>int</code>.
     */
    public int intValue() {
        return value.intValue();
    }

    /**
     * Converts this <code>UnsignedLong</code> to a <code>long</code>.
     *
     * @return                the <code>UnsignedLong</code> as a
     *                        <code>long</code>.
     */
    public long longValue() {
        return value.longValue();
    }

    /**
     * Converts this <code>UnsignedLong</code> to a <code>float</code>.
     *
     * @return                the <code>UnsignedLong</code> as a
     *                        <code>float</code>.
     */
    public float floatValue() {
        return value.floatValue();
    }

    /**
     * Converts this <code>UnsignedLong</code> to a <code>double</code>.
     *
     * @return                the <code>UnsignedLong</code> as a
     *                        <code>double</code>.
     */
    public double doubleValue() {
        return value.doubleValue();
    }

    /**
     * Tests the equality of two <code>UnsignedLong</code> values.
     *
     * @param obj                the value to compare to.
     * @return                <code>true</code> if the passed object equals
     *                        to this <code>UnsignedLong</code>.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof UnsignedLong)) {
            return false;
        }

        return value.equals(((UnsignedLong) obj).value);
    }

    /**
     * Returns a hash code for this <code>UnsignedLong</code>.
     *
     * @return                a hash code value for this object.
     */
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Compares two <code>UnsignedLong</code> values.
     *
     * @param obj                the value to compare to.
     * @return                -1, 0, or 1 if this <code>UnsignedLong</code>
     *                        is smaller, equal or larger than the passed
     *                        value.
     *
     * @throws NullPointerException if the passed object is <code>null</code>.
     */
    public int compareTo(Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }

        return value.compareTo(((UnsignedLong) obj).value);
    }

    /**
     * Converts this <code>UnsignedLong</code> to a string.
     *
     * @return                the <code>UnsignedLong</code> as a string.
     */
    public String toString() {
        return value.toString();
    }
}
