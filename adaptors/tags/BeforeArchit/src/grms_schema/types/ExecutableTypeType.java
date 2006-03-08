/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: ExecutableTypeType.java,v 1.1 2004/06/21 09:05:33 rob Exp $
 */

package grms_schema.types;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Hashtable;

/**
 * Class ExecutableTypeType.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/06/21 09:05:33 $
 */
public class ExecutableTypeType implements java.io.Serializable {

	//--------------------------/
	//- Class/Member Variables -/
	//--------------------------/

	/**
	 * The single type
	 */
	public static final int SINGLE_TYPE = 0;

	/**
	 * The instance of the single type
	 */
	public static final ExecutableTypeType SINGLE = new ExecutableTypeType(
			SINGLE_TYPE, "single");

	/**
	 * The multiple type
	 */
	public static final int MULTIPLE_TYPE = 1;

	/**
	 * The instance of the multiple type
	 */
	public static final ExecutableTypeType MULTIPLE = new ExecutableTypeType(
			MULTIPLE_TYPE, "multiple");

	/**
	 * The mpi type
	 */
	public static final int MPI_TYPE = 2;

	/**
	 * The instance of the mpi type
	 */
	public static final ExecutableTypeType MPI = new ExecutableTypeType(
			MPI_TYPE, "mpi");

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

	private ExecutableTypeType(int type, java.lang.String value) {
		super();
		this.type = type;
		this.stringValue = value;
	} //-- grms_schema.types.ExecutableTypeType(int, java.lang.String)

	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Method enumerateReturns an enumeration of all possible instances of
	 * ExecutableTypeType
	 */
	public static java.util.Enumeration enumerate() {
		return _memberTable.elements();
	} //-- java.util.Enumeration enumerate()

	/**
	 * Method getTypeReturns the type of this ExecutableTypeType
	 */
	public int getType() {
		return this.type;
	} //-- int getType()

	/**
	 * Method init
	 */
	private static java.util.Hashtable init() {
		Hashtable members = new Hashtable();
		members.put("single", SINGLE);
		members.put("multiple", MULTIPLE);
		members.put("mpi", MPI);
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
	 * Method toStringReturns the String representation of this
	 * ExecutableTypeType
	 */
	public java.lang.String toString() {
		return this.stringValue;
	} //-- java.lang.String toString()

	/**
	 * Method valueOfReturns a new ExecutableTypeType based on the given String
	 * value.
	 * 
	 * @param string
	 */
	public static grms_schema.types.ExecutableTypeType valueOf(
			java.lang.String string) {
		java.lang.Object obj = null;
		if (string != null)
			obj = _memberTable.get(string);
		if (obj == null) {
			String err = "'" + string + "' is not a valid ExecutableTypeType";
			throw new IllegalArgumentException(err);
		}
		return (ExecutableTypeType) obj;
	} //-- grms_schema.types.ExecutableTypeType valueOf(java.lang.String)

}