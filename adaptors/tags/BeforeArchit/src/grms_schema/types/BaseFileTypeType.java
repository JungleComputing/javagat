/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: BaseFileTypeType.java,v 1.1 2004/06/21 09:05:33 rob Exp $
 */

package grms_schema.types;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Hashtable;

/**
 * Class BaseFileTypeType.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/06/21 09:05:33 $
 */
public class BaseFileTypeType implements java.io.Serializable {

	//--------------------------/
	//- Class/Member Variables -/
	//--------------------------/

	/**
	 * The in type
	 */
	public static final int IN_TYPE = 0;

	/**
	 * The instance of the in type
	 */
	public static final BaseFileTypeType IN = new BaseFileTypeType(IN_TYPE,
			"in");

	/**
	 * The out type
	 */
	public static final int OUT_TYPE = 1;

	/**
	 * The instance of the out type
	 */
	public static final BaseFileTypeType OUT = new BaseFileTypeType(OUT_TYPE,
			"out");

	/**
	 * The inout type
	 */
	public static final int INOUT_TYPE = 2;

	/**
	 * The instance of the inout type
	 */
	public static final BaseFileTypeType INOUT = new BaseFileTypeType(
			INOUT_TYPE, "inout");

	/**
	 * Field _memberTable
	 */
	private static java.util.Hashtable _memberTable = init();

	/**
	 * Field type
	 */
	private int type = -1;

	/**
	 * Field stringValue
	 */
	private java.lang.String stringValue = null;

	//----------------/
	//- Constructors -/
	//----------------/

	private BaseFileTypeType(int type, java.lang.String value) {
		super();
		this.type = type;
		this.stringValue = value;
	} //-- grms_schema.types.BaseFileTypeType(int, java.lang.String)

	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Method enumerateReturns an enumeration of all possible instances of
	 * BaseFileTypeType
	 */
	public static java.util.Enumeration enumerate() {
		return _memberTable.elements();
	} //-- java.util.Enumeration enumerate()

	/**
	 * Method getTypeReturns the type of this BaseFileTypeType
	 */
	public int getType() {
		return this.type;
	} //-- int getType()

	/**
	 * Method init
	 */
	private static java.util.Hashtable init() {
		Hashtable members = new Hashtable();
		members.put("in", IN);
		members.put("out", OUT);
		members.put("inout", INOUT);
		return members;
	} //-- java.util.Hashtable init()

	/**
	 * Method readResolve will be called during deserialization to replace the
	 * deserialized object with the correct constant instance. <br/>
	 */
	private java.lang.Object readResolve() {
		return valueOf(this.stringValue);
	} //-- java.lang.Object readResolve()

	/**
	 * Method toStringReturns the String representation of this BaseFileTypeType
	 */
	public java.lang.String toString() {
		return this.stringValue;
	} //-- java.lang.String toString()

	/**
	 * Method valueOfReturns a new BaseFileTypeType based on the given String
	 * value.
	 * 
	 * @param string
	 */
	public static grms_schema.types.BaseFileTypeType valueOf(
			java.lang.String string) {
		java.lang.Object obj = null;
		if (string != null)
			obj = _memberTable.get(string);
		if (obj == null) {
			String err = "'" + string + "' is not a valid BaseFileTypeType";
			throw new IllegalArgumentException(err);
		}
		return (BaseFileTypeType) obj;
	} //-- grms_schema.types.BaseFileTypeType valueOf(java.lang.String)

}