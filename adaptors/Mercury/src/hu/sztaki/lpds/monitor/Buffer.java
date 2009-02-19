package hu.sztaki.lpds.monitor;

/**
 * Converts between Java objects and Mercury data types.
 *
 * The <code>Buffer</code> class provides functions to encode/decode Java
 * objects to/from the representation used by Mercury.
 *
 * @see BasicType
 * @see MonitorType
 * @author G??bor Gomb??s
 * @version $Id: Buffer.java 857 2006-04-19 09:43:22Z rob $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class Buffer {
    /** The encoded contents of the buffer.  */
    private byte[] data;

    /** The index of the first not-yet-decoded byte.  */
    private int start;

    /** The index of the first unused byte. */
    private int end;

    /**
     * Creates an empty buffer. The new buffer can be used for encoding
     * Java objects.
     */
    public Buffer() {
        data = null;
    }

    /**
     * Creates a non-empty buffer. The buffer can be used for decoding
     * Java objects.
     *
     * @param data                the raw, encoded data.
     */
    public Buffer(byte[] data) {
        this.data = data;
    }

    /**
     * Returns the raw contents of the buffer. This method gives the
     * result after encoding a Java object.
     *
     * @return                the buffer contents.
     */
    public byte[] getData() {
        byte[] tmp = new byte[end - start];

        for (int i = start; i < end; i++)
            tmp[i - start] = data[i];

        return tmp;
    }

    /**
     * Sets the contents of the buffer. This method resets the internal
     * state of the buffer so it is ready to decode the new contents.
     *
     * @param data                the new contents of the buffer.
     */
    public void setData(byte[] data) {
        this.data = data;
        start = 0;
        end = 0;
    }

    /* ==================== Encoding functions ==================== */

    /**
     * Ensures that the buffer has enough free space for the encoding
     * functions. The buffer size grows exponentially for efficiency.
     *
     * @param needed        number of free bytes needed.
     */
    public void ensureFree(int needed) {
        /* Enlarge the buffer if needed */
        if (data == null) {
            data = new byte[16];
        }

        if ((end + needed) < data.length) {
            return;
        }

        int newlen = data.length;

        while ((end + needed) >= newlen)
            newlen *= 2;

        byte[] tmp = new byte[newlen];
        System.arraycopy(data, 0, tmp, 0, end);
        data = tmp;
    }

    /**
     * Appends one byte to the buffer.
     */
    private void addByte(int value) {
        data[end++] = (byte) value;
    }

    /**
     * Appends a 32-bit signed integer to the buffer. The value is encoded
     * using network byte order.
     *
     * @param value                the value to add.
     */
    public void addInt(int value) {
        ensureFree(4);
        addByte((value >> 24) & 0xff);
        addByte((value >> 16) & 0xff);
        addByte((value >> 8) & 0xff);
        addByte(value & 0xff);
    }

    /**
     * Appends a 32-bit unsigned integer to the buffer. The value is
     * passed as a <code>long</code> but only the low 32 bits are used.
     *
     * @param value                the value to add.
     */
    public void addUnsigned(long value) {
        ensureFree(4);
        addByte((int) (value >> 24) & 0xff);
        addByte((int) (value >> 16) & 0xff);
        addByte((int) (value >> 8) & 0xff);
        addByte((int) value & 0xff);
    }

    /**
     * Appends a 64-bit signed integer to the buffer.
     *
     * @param value                the value to add.
     */
    public void addLong(long value) {
        addInt((int) ((value >> 32) & 0xffff));
        addInt((int) (value & 0xffff));
    }

    /**
     * Appends a 64-bit double precision floating point number to the buffer.
     *
     * @param value                the value to add.
     */
    public void addDouble(double value) {
        addLong(Double.doubleToLongBits(value));
    }

    /**
     * Appends an opaque array of bytes to the buffer. The encoded value
     * contains first the length of the array as a 32-bit unsigned integer,
     * then come the contents of the array. The encoded value is padded to
     * a 4-byte boundary using 0 bytes if needed.
     *
     * @param value                the value to add.
     */
    public void addOpaque(byte[] value) {
        addInt(value.length);
        ensureFree((value.length + 3) & ~3);
        System.arraycopy(value, 0, data, end, value.length);
        end += value.length;

        /* Pad the value */
        while ((end & 3) != 0)
            data[end++] = 0;
    }

    /**
     * Appends a {@link String} to the buffer. The encoding is the
     * same as described at {@link #addOpaque}.
     *
     * @param value                the string to add.
     */
    public void addString(String value) {
        addOpaque(value.getBytes());
    }

    /**
     * Encodes a Java object into the buffer according to the type
     * description. See {@link BasicType} for the classes required to
     * encode simple types. Arrays must be passed as <code>Object[]</code>.
     * Records are also <code>Object[]</code> arrays with the elements o
     * the array being the fields of the record in the order they appear in
     * the record definition.
     *
     * @param value                the object to encode.
     * @param type                the type description to use.
     *
     * @throws ClassCastException if the passed value is not an
     *        instance of the class requested by the type description.
     * @throws IllegalArgumentException if the type description is not an instance
     *        of {@link SimpleType}, {@link ArrayType} or {@link RecordType}.
     */
    public void encode(Object value, MonitorType type) {
        if (type instanceof ArrayType) {
            encodeArray((Object[]) value, (ArrayType) type);
        } else if (type instanceof RecordType) {
            encodeRecord((Object[]) value, (RecordType) type);
        } else if (type instanceof SimpleType) {
            encodeSimple(value, (SimpleType) type);
        } else {
            throw new IllegalArgumentException("Unknown type definition");
        }
    }

    /**
     * Encodes an array. The value must be an <code>Object[]</code> array.
     *
     * @param value                the elements of the array.
     * @param type                the descriptor of the array.
     */
    private void encodeArray(Object[] value, ArrayType type) {
        /* If the array is dynamic, we have to store the length first */
        if (type.getLength() < 0) {
            addInt(value.length);
        }

        for (int i = 0; i < value.length; i++)
            encode(value[i], type.getBaseType());
    }

    /**
     * Encodes a record. The value must be an <code>Object[]</code> array.
     *
     * @param value                the fields of the record.
     * @param type                the descriptor of the record.
     *
     * @throws IllegalArgumentException if the number of elements in the
     *        value array does not match the number of fields.
     */
    private void encodeRecord(Object[] value, RecordType type) {
        MonitorType[] fields = type.getFields();

        if (fields.length != value.length) {
            throw new IllegalArgumentException("Wrong number of fields");
        }

        for (int i = 0; i < fields.length; i++)
            encode(value[i], fields[i]);
    }

    /**
     * Encodes a simple data type.
     *
     * @param value                the value to encode.
     * @param type                the type description.
     *
     * @throws IllegalArgumentException if the passed type is invalid.
     */
    private void encodeSimple(Object value, SimpleType type) {
        switch (type.getType().getCode()) {
        case BasicType.MON_T_INT32:
            addInt(((Integer) value).intValue());

            break;

        case BasicType.MON_T_UINT32:
            addUnsigned(((UnsignedInteger) value).longValue());

            break;

        case BasicType.MON_T_INT64:
            addLong(((Long) value).longValue());

            break;

        case BasicType.MON_T_UINT64:

            byte[] tmp = ((UnsignedLong) value).toByteArray();
            System.arraycopy(tmp, 0, data, end, 8);
            end += 8;

            break;

        case BasicType.MON_T_DOUBLE:
            addDouble(((Double) value).doubleValue());

            break;

        case BasicType.MON_T_STRING:
            addString((String) value);

            break;

        case BasicType.MON_T_OPAQUE:
            addOpaque((byte[]) value);

            break;

        case BasicType.MON_T_VOID:
            break;

        default:
            throw new IllegalArgumentException("Unknown data type");
        }
    }

    /* ==================== Decoding functions ==================== */

    /**
     * Extracts one byte from the buffer.
     *
     * @return                the extracted byte.
     */
    private int getByte() {
        int val = data[start++];

        if (val < 0) {
            val += 256;
        }

        return val;
    }

    /**
     * Extracts a 32-bit signed integer from the buffer.
     *
     * @return                the decoded value.
     */
    public int getInt() {
        return (getByte() << 24) + (getByte() << 16) + (getByte() << 8)
            + getByte();
    }

    /**
     * Extracts a 32-bit unsigned integer from the buffer. The value
     * is returned as a <code>long</code>, but only the low 32 bits are
     * used.
     *
     * @return                the decoded value.
     */
    public long getUnsigned() {
        return ((long) getByte() << 24) + ((long) getByte() << 16)
            + ((long) getByte() << 8) + getByte();
    }

    /**
     * Extracts a 64-bit signed integer from the buffer.
     *
     * @return                the decoded value.
     */
    public long getLong() {
        return (getInt() << 32) + getInt();
    }

    /**
     * Extracts a 64-bit unsigned integer from the buffer.
     *
     * @return                the decoded value.
     */
    public double getDouble() {
        return Double.longBitsToDouble(getLong());
    }

    /**
     * Extracts an opaque array of bytes from the buffer. The buffer must
     * first contain the length of an array as a 32-bit unsigned integer,
     * then the array itself. The terminating padding bytes are skipped if
     * present.
     *
     * @return                the decoded value.
     */
    public byte[] getOpaque() {
        int len = getInt();
        byte[] result = new byte[len];

        for (int i = 0; i < len; i++)
            result[i] = data[start++];

        /* Skip the padding */
        start = (start + 3) & ~3;

        return result;
    }

    /**
     * Extracts a <code>String</code> from the buffer. The buffer must have
     * the same structure as described with {@link #getOpaque}.
     *
     * @return                the decoded value.
     */
    public String getString() {
        return new String(getOpaque());
    }

    /**
     * Extracts a Java object from the buffer.
     *
     * @param type                describes the data type of the buffer.
     * @return                the decoded value.
     *
     * @throws IllegalArgumentException if the type description is not an instance
     *        of {@link SimpleType}, {@link ArrayType} or {@link RecordType}.
     * @throws ArrayIndexOutOfBoundsException if the buffer is
     *        truncated.
     */
    public Object decode(MonitorType type) {
        if (type instanceof ArrayType) {
            return decodeArray((ArrayType) type);
        } else if (type instanceof RecordType) {
            return decodeRecord((RecordType) type);
        } else if (type instanceof SimpleType) {
            return decodeSimple((SimpleType) type);
        } else {
            throw new IllegalArgumentException("Unknown type definition");
        }
    }

    /**
     * Decodes an array.
     *
     * @param type                the descriptor of the array.
     * @return                the decoded array as <code>Object[]</code>.
     */
    private Object decodeArray(ArrayType type) {
        int len = type.getLength();

        if (len < 0) {
            len = getInt();
        }

        Object[] result = new Object[len];

        for (int i = 0; i < len; i++)
            result[i] = decode(type.getBaseType());

        return result;
    }

    /**
     * Decodes a record.
     *
     * @param type                the descriptor of the record.
     * @return                the fields of the record as <code>Object[]</code>.
     */
    private Object decodeRecord(RecordType type) {
        MonitorType[] fields = type.getFields();
        Object[] result = new Object[fields.length];

        for (int i = 0; i < fields.length; i++)
            result[i] = decode(fields[i]);

        return result;
    }

    /**
     * Decodes a simple data type.
     *
     * @param type                the type descriptor.
     * @return                the decoded Java object.
     *
     * @throws IllegalArgumentException if the passed type descriptor
     *        is invalid.
     */
    private Object decodeSimple(SimpleType type) {
        switch (type.getType().getCode()) {
        case BasicType.MON_T_INT32:
            return new Integer(getInt());

        case BasicType.MON_T_UINT32:
            return new UnsignedInteger(getUnsigned());

        case BasicType.MON_T_INT64:
            return new Long(getLong());

        case BasicType.MON_T_UINT64:

            byte[] tmp = new byte[8];
            System.arraycopy(data, start, tmp, 0, 8);
            start += 8;

            return new UnsignedLong(tmp);

        case BasicType.MON_T_DOUBLE:
            return new Double(getDouble());

        case BasicType.MON_T_STRING:
            return getString();

        case BasicType.MON_T_OPAQUE:
            return getOpaque();

        case BasicType.MON_T_VOID:
            return null;

        default:
            throw new IllegalArgumentException("Unknown data type");
        }
    }
}
