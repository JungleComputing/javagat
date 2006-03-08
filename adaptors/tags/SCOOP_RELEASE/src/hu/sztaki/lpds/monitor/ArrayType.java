package hu.sztaki.lpds.monitor;

/**
 * Represents an array in a Mercury type definition tree.
 *
 * Just like native arrays, arrays in Mercury has a base type. But unlike Java,
 * Mercury arrays can have either static or dynamic lengths. If the data type
 * indicates dynamic length, the actual length of the array is encoded in the
 * value. For fixed length arrays the array length is encoded in the type
 * description.
 *
 * Values of Mercury arrays are represented as <code>Object[]</code> arrays in
 * Java where every element of the array must be compatible with array's base
 * type. For fixed length arrays the length of the array value must be the same
 * as the length encoded in the type descriptor.
 *
 * @author Gábor Gombás
 * @version $Id: ArrayType.java,v 1.1 2005/06/13 01:48:47 aagapi Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */

public class ArrayType extends MonitorType {

	/** Holds the base type of the array. */
	private MonitorType baseType;
	/** The length of the array. If it is &lt;0, the array is dynamic. */
	private int length;

	/**
	 * Constructs a new array type.
	 *
	 * The new array will have the specified length and base type. The new
	 * type will have the specified name; the name of the base type is
	 * ignored.
	 *
	 * @param name		the name of this array type.
	 * @param baseType	the base type of the array.
	 * @param length	the length of the array, &lt;0 means variable-size.
	 */
	public ArrayType(String name, MonitorType baseType, int length) {
		super(name);
		this.baseType = baseType;
		this.length = length;
	}

	/**
	 * Returns tye base type of the array.
	 *
	 * @return		the type of the elements of the array.
	 */
	public MonitorType getBaseType() {
		return baseType;
	}

	/**
	 * Returns the length of the array.
	 *
	 * @return		the length of the array. &lt;0 means the
	 *			length is dynamic.
	 */
	public int getLength() {
		return length;
	}

	protected void internalToString(StringBuffer str) {
		baseType.internalToString(str);
		if (getName() != null) {
			str.append(" ");
			str.append(getName());
		}
		str.append("[");
		if (length >= 0)
			str.append(Integer.toString(length));
		str.append("]");
	}

	protected void internalToString(StringBuffer str, Object value) {
		int len = length;
		if (len < 0)
			len = ((Object[])value).length;
		str.append("[ ");
		for (int i = 0; i < len; i++) {
			baseType.internalToString(str, ((Object[])value)[i]);
			if (i < len - 1)
				str.append(", ");
		}
		str.append(" ]");
	}
}
