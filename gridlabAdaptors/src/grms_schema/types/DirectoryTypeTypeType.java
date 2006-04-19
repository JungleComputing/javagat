/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id$
 */
package grms_schema.types;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/
import java.util.Hashtable;

/**
 * Class DirectoryTypeTypeType.
 *
 * @version $Revision: 1.9 $ $Date$
 */
public class DirectoryTypeTypeType implements java.io.Serializable {
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
    public static final DirectoryTypeTypeType IN = new DirectoryTypeTypeType(
        IN_TYPE, "in");

    /**
     * The out type
     */
    public static final int OUT_TYPE = 1;

    /**
     * The instance of the out type
     */
    public static final DirectoryTypeTypeType OUT = new DirectoryTypeTypeType(
        OUT_TYPE, "out");

    /**
     * The inout type
     */
    public static final int INOUT_TYPE = 2;

    /**
     * The instance of the inout type
     */
    public static final DirectoryTypeTypeType INOUT = new DirectoryTypeTypeType(
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
    private DirectoryTypeTypeType(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- grms_schema.types.DirectoryTypeTypeType(int, java.lang.String)

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method enumerate
     *
     * Returns an enumeration of all possible instances of
     * DirectoryTypeTypeType
     *
     * @return Enumeration
     */
    public static java.util.Enumeration enumerate() {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Method getType
     *
     * Returns the type of this DirectoryTypeTypeType
     *
     * @return int
     */
    public int getType() {
        return this.type;
    } //-- int getType() 

    /**
     * Method init
     *
     *
     *
     * @return Hashtable
     */
    private static java.util.Hashtable init() {
        Hashtable members = new Hashtable();
        members.put("in", IN);
        members.put("out", OUT);
        members.put("inout", INOUT);

        return members;
    } //-- java.util.Hashtable init() 

    /**
     * Method readResolve
     *
     *  will be called during deserialization to replace the
     * deserialized object with the correct constant instance.
     * <br/>
     *
     * @return Object
     */
    private java.lang.Object readResolve() {
        return valueOf(this.stringValue);
    } //-- java.lang.Object readResolve() 

    /**
     * Method toString
     *
     * Returns the String representation of this
     * DirectoryTypeTypeType
     *
     * @return String
     */
    public java.lang.String toString() {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Method valueOf
     *
     * Returns a new DirectoryTypeTypeType based on the given
     * String value.
     *
     * @param string
     * @return DirectoryTypeTypeType
     */
    public static grms_schema.types.DirectoryTypeTypeType valueOf(
            java.lang.String string) {
        java.lang.Object obj = null;

        if (string != null) {
            obj = _memberTable.get(string);
        }

        if (obj == null) {
            String err = "'" + string
                + "' is not a valid DirectoryTypeTypeType";
            throw new IllegalArgumentException(err);
        }

        return (DirectoryTypeTypeType) obj;
    } //-- grms_schema.types.DirectoryTypeTypeType valueOf(java.lang.String) 
}
