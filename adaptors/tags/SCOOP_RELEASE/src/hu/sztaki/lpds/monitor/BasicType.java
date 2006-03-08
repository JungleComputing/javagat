package hu.sztaki.lpds.monitor;

/**
 * The <code>BasicType</code> class defines the basic data types of Mercury.
 * These definitions are wrappers around the codes used by the native code.
 *
 * @author Gábor Gombás
 * @version $Id: BasicType.java,v 1.1 2005/06/13 01:48:47 aagapi Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */

public final class BasicType {

	/* The codes must match the C definitions. They are not private
	 * because e.g. Buffer uses them */
	static final int MON_T_INT32 = 1;
	static final int MON_T_UINT32 = 2;
	static final int MON_T_INT64 = 3;
	static final int MON_T_UINT64 = 4;
	static final int MON_T_DOUBLE = 5;
	static final int MON_T_BOOLEAN = 6;
	static final int MON_T_STRING = 7;
	static final int MON_T_OPAQUE = 8;
	static final int MON_T_REC = 9;
	static final int MON_T_ARRAY = 10;
	static final int MON_T_VOID = 11;

	/**
	 * 32-bit signed integer type.
	 * It is represented in Java using the {@link Integer} class.
	 */
	public static final BasicType INT32 = new BasicType(MON_T_INT32);
	/**
	 * 32-bit unsigned integer type.
	 * It is represented in Java using the {@link UnsignedInteger} class.
	 */
	public static final BasicType UINT32 = new BasicType(MON_T_UINT32);
	/**
	 * 64-bit signed integer type.
	 * It is represented in Java using the {@link Long} class.
	 */
	public static final BasicType INT64 = new BasicType(MON_T_INT64);
	/**
	 * 64-bit unsigned integer type.
	 * It is represented in Java using the {@link UnsignedLong} class.
	 */
	public static final BasicType UINT64 = new BasicType(MON_T_UINT64);
	/**
	 * 64-bit double precision floating point number.
	 * It is represented in Java using the {@link Double} class.
	 */
	public static final BasicType DOUBLE = new BasicType(MON_T_DOUBLE);
	/**
	 * 32-bit boolean value.
	 * A raw value of 0 means false, non-0 means true.
	 * It is represented in Java using the {@link Boolean} class.
	 */
	public static final BasicType BOOLEAN = new BasicType(MON_T_BOOLEAN);
	/**
	 * String of arbitrary length.
	 * It is represented in Java using the {@link String} class.
	 */
	public static final BasicType STRING = new BasicType(MON_T_STRING);
	/**
	 * Opaque data.
	 * It is represented in Java using a <code>byte[]</code> array.
	 */
	public static final BasicType OPAQUE = new BasicType(MON_T_OPAQUE);
	/**
	 * Void data type.
	 */
	public static final BasicType VOID = new BasicType(MON_T_VOID);

	/**
	 * Record type.
	 * Included for completeness, should not be publicly available.
	 * It is represented in Java using an <code>Object[]</code> array.
	 */
	static final BasicType REC = new BasicType(MON_T_REC);
	/**
	 * Array type.
	 * Included for completeness, should not be publicly available.
	 * It is represented in Java using an <code>Object[]</code> array.
	 */
	static final BasicType ARRAY = new BasicType(MON_T_ARRAY);

	/**
	 * Names of the basic types.
	 * The indexes must match the codes defined above.
	 */
	private static final String[] names = {
		"", "int32", "uint32", "int64", "uint64", "double", "boolean",
		"string", "opaque", "rec", "array", "void"
	};

	/**
	 * Internal code of the data type.
	 * It is used by the native code.
	 */
	private int code;

	/**
	 * Creates a new basic type.
	 *
	 * @param code		the internal code of the type. Must match the
	 *			C definitions.
	 */
	private BasicType(int code) {
		this.code = code;
	}

	/**
	 * Returns the internal code of the type.
	 *
	 * @return		the internal code.
	 */
	int getCode() {
		return code;
	}

	/**
	 * Converts a basic type to a string.
	 *
	 * @return		the name of the type as used by Mercury.
	 */
	public String toString() {
		return names[code];
	}
}
