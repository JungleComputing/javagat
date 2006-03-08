/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: WeekDayType.java,v 1.2 2005/10/07 11:06:00 rob Exp $
 */

package grms_schema.types;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Hashtable;

/**
 * Class WeekDayType.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/10/07 11:06:00 $
 */
public class WeekDayType implements java.io.Serializable {

    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * The Monday type
     */
    public static final int MONDAY_TYPE = 0;

    /**
     * The instance of the Monday type
     */
    public static final WeekDayType MONDAY = new WeekDayType(MONDAY_TYPE,
        "Monday");

    /**
     * The Tuesday type
     */
    public static final int TUESDAY_TYPE = 1;

    /**
     * The instance of the Tuesday type
     */
    public static final WeekDayType TUESDAY = new WeekDayType(TUESDAY_TYPE,
        "Tuesday");

    /**
     * The Wednesday type
     */
    public static final int WEDNESDAY_TYPE = 2;

    /**
     * The instance of the Wednesday type
     */
    public static final WeekDayType WEDNESDAY = new WeekDayType(WEDNESDAY_TYPE,
        "Wednesday");

    /**
     * The Thursday type
     */
    public static final int THURSDAY_TYPE = 3;

    /**
     * The instance of the Thursday type
     */
    public static final WeekDayType THURSDAY = new WeekDayType(THURSDAY_TYPE,
        "Thursday");

    /**
     * The Friday type
     */
    public static final int FRIDAY_TYPE = 4;

    /**
     * The instance of the Friday type
     */
    public static final WeekDayType FRIDAY = new WeekDayType(FRIDAY_TYPE,
        "Friday");

    /**
     * The Saturday type
     */
    public static final int SATURDAY_TYPE = 5;

    /**
     * The instance of the Saturday type
     */
    public static final WeekDayType SATURDAY = new WeekDayType(SATURDAY_TYPE,
        "Saturday");

    /**
     * The Sunday type
     */
    public static final int SUNDAY_TYPE = 6;

    /**
     * The instance of the Sunday type
     */
    public static final WeekDayType SUNDAY = new WeekDayType(SUNDAY_TYPE,
        "Sunday");

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

    private WeekDayType(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- grms_schema.types.WeekDayType(int, java.lang.String)

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method enumerate
     * 
     * Returns an enumeration of all possible instances of
     * WeekDayType
     * 
     * @return Enumeration
     */
    public static java.util.Enumeration enumerate() {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Method getType
     * 
     * Returns the type of this WeekDayType
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
        members.put("Monday", MONDAY);
        members.put("Tuesday", TUESDAY);
        members.put("Wednesday", WEDNESDAY);
        members.put("Thursday", THURSDAY);
        members.put("Friday", FRIDAY);
        members.put("Saturday", SATURDAY);
        members.put("Sunday", SUNDAY);
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
     * Returns the String representation of this WeekDayType
     * 
     * @return String
     */
    public java.lang.String toString() {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Method valueOf
     * 
     * Returns a new WeekDayType based on the given String value.
     * 
     * @param string
     * @return WeekDayType
     */
    public static grms_schema.types.WeekDayType valueOf(java.lang.String string) {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid WeekDayType";
            throw new IllegalArgumentException(err);
        }
        return (WeekDayType) obj;
    } //-- grms_schema.types.WeekDayType valueOf(java.lang.String) 

}
