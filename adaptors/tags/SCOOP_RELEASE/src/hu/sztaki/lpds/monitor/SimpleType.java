package hu.sztaki.lpds.monitor;

/**
 * Represents a simple type in a Mercury type definition tree.
 *
 * <code>SimpleType</code> is a wrapper around the values of {@link BasicType}.
 *
 * @author Gábor Gombás
 * @version $Id: SimpleType.java,v 1.1 2005/06/13 01:48:47 aagapi Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */

public class SimpleType extends MonitorType {

	/** The real type of this descriptor. */
	private BasicType type;

	/**
	 * Constructs a new <code>SimpleType</code>.
	 *
	 * @param name		the name of the type.
	 * @param type		the real data type.
	 */
	public SimpleType(String name, BasicType type) {
		super(name);
		this.type = type;
	}

	/**
	 * Constructs a new <code>SimpleType</code> with the data
	 * type being {@link BasicType#VOID}.
	 */
	public SimpleType() {
		super(null);
		this.type = BasicType.VOID;
	}

	/**
	 * Returns the real data type.
	 *
	 * @return		the data type.
	 */
	public BasicType getType() {
		return type;
	}

	protected void internalToString(StringBuffer str) {
		str.append(type.toString());
		if (getName() != null) {
			str.append(" ");
			str.append(getName());
		}
	}

	protected void internalToString(StringBuffer str, Object value) {
		if (type == BasicType.STRING) {
			str.append("\"");
			str.append((String)value);
			str.append("\"");
		}
		else
			str.append(value.toString());
	}
}
