/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: LocalrmnameType.java,v 1.6 2005/04/07 13:48:19 rob Exp $
 */

package grms_schema.types;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Hashtable;

/**
 * Class LocalrmnameType.
 * 
 * @version $Revision: 1.6 $ $Date: 2005/04/07 13:48:19 $
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
    public static final LocalrmnameType FORK = new LocalrmnameType(FORK_TYPE, "fork");

    /**
     * The lsf type
     */
    public static final int LSF_TYPE = 1;

    /**
     * The instance of the lsf type
     */
    public static final LocalrmnameType LSF = new LocalrmnameType(LSF_TYPE, "lsf");

    /**
     * The pbs type
     */
    public static final int PBS_TYPE = 2;

    /**
     * The instance of the pbs type
     */
    public static final LocalrmnameType PBS = new LocalrmnameType(PBS_TYPE, "pbs");

    /**
     * The condor type
     */
    public static final int CONDOR_TYPE = 3;

    /**
     * The instance of the condor type
     */
    public static final LocalrmnameType CONDOR = new LocalrmnameType(CONDOR_TYPE, "condor");

    /**
     * The sge type
     */
    public static final int SGE_TYPE = 4;

    /**
     * The instance of the sge type
     */
    public static final LocalrmnameType SGE = new LocalrmnameType(SGE_TYPE, "sge");

    /**
     * The ccs type
     */
    public static final int CCS_TYPE = 5;

    /**
     * The instance of the ccs type
     */
    public static final LocalrmnameType CCS = new LocalrmnameType(CCS_TYPE, "ccs");

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
     * Method enumerate
     * 
     * Returns an enumeration of all possible instances of
     * LocalrmnameType
     * 
     * @return Enumeration
     */
    public static java.util.Enumeration enumerate()
    {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Method getType
     * 
     * Returns the type of this LocalrmnameType
     * 
     * @return int
     */
    public int getType()
    {
        return this.type;
    } //-- int getType() 

    /**
     * Method init
     * 
     * 
     * 
     * @return Hashtable
     */
    private static java.util.Hashtable init()
    {
        Hashtable members = new Hashtable();
        members.put("fork", FORK);
        members.put("lsf", LSF);
        members.put("pbs", PBS);
        members.put("condor", CONDOR);
        members.put("sge", SGE);
        members.put("ccs", CCS);
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
    private java.lang.Object readResolve()
    {
        return valueOf(this.stringValue);
    } //-- java.lang.Object readResolve() 

    /**
     * Method toString
     * 
     * Returns the String representation of this LocalrmnameType
     * 
     * @return String
     */
    public java.lang.String toString()
    {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Method valueOf
     * 
     * Returns a new LocalrmnameType based on the given String
     * value.
     * 
     * @param string
     * @return LocalrmnameType
     */
    public static grms_schema.types.LocalrmnameType valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid LocalrmnameType";
            throw new IllegalArgumentException(err);
        }
        return (LocalrmnameType) obj;
    } //-- grms_schema.types.LocalrmnameType valueOf(java.lang.String) 

}
