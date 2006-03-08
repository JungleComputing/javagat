/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: LocalrmnameType.java,v 1.1 2004/06/21 09:05:33 rob Exp $
 */

package grms_schema.types;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Hashtable;

/**
 * Class LocalrmnameType.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/06/21 09:05:33 $
 */
public class LocalrmnameType implements java.io.Serializable {

	//--------------------------/
	//- Class/Member Variables -/
	//--------------------------/

	/**
	 * The fork type
	 */
	public static final int FORK_TYPE = 0;

	/**
	 * The instance of the fork type
	 */
	public static final LocalrmnameType FORK = new LocalrmnameType(FORK_TYPE,
			"fork");

	/**
	 * The lsf type
	 */
	public static final int LSF_TYPE = 1;

	/**
	 * The instance of the lsf type
	 */
	public static final LocalrmnameType LSF = new LocalrmnameType(LSF_TYPE,
			"lsf");

	/**
	 * The pbs type
	 */
	public static final int PBS_TYPE = 2;

	/**
	 * The instance of the pbs type
	 */
	public static final LocalrmnameType PBS = new LocalrmnameType(PBS_TYPE,
			"pbs");

	/**
	 * The condor type
	 */
	public static final int CONDOR_TYPE = 3;

	/**
	 * The instance of the condor type
	 */
	public static final LocalrmnameType CONDOR = new LocalrmnameType(
			CONDOR_TYPE, "condor");

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

	private LocalrmnameType(int type, java.lang.String value) {
		super();
		this.type = type;
		this.stringValue = value;
	} //-- grms_schema.types.LocalrmnameType(int, java.lang.String)

	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Method enumerateReturns an enumeration of all possible instances of
	 * LocalrmnameType
	 */
	public static java.util.Enumeration enumerate() {
		return _memberTable.elements();
	} //-- java.util.Enumeration enumerate()

	/**
	 * Method getTypeReturns the type of this LocalrmnameType
	 */
	public int getType() {
		return this.type;
	} //-- int getType()

	/**
	 * Method init
	 */
	private static java.util.Hashtable init() {
		Hashtable members = new Hashtable();
		members.put("fork", FORK);
		members.put("lsf", LSF);
		members.put("pbs", PBS);
		members.put("condor", CONDOR);
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
	 * Method toStringReturns the String representation of this LocalrmnameType
	 */
	public java.lang.String toString() {
		return this.stringValue;
	} //-- java.lang.String toString()

	/**
	 * Method valueOfReturns a new LocalrmnameType based on the given String
	 * value.
	 * 
	 * @param string
	 */
	public static grms_schema.types.LocalrmnameType valueOf(
			java.lang.String string) {
		java.lang.Object obj = null;
		if (string != null)
			obj = _memberTable.get(string);
		if (obj == null) {
			String err = "'" + string + "' is not a valid LocalrmnameType";
			throw new IllegalArgumentException(err);
		}
		return (LocalrmnameType) obj;
	} //-- grms_schema.types.LocalrmnameType valueOf(java.lang.String)

}