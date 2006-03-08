/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: IncludingItem.java,v 1.2 2005/10/07 11:05:55 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

/**
 * Class IncludingItem.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/10/07 11:05:55 $
 */
public class IncludingItem implements java.io.Serializable {

    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _weekDay
     */
    private grms_schema.types.WeekDayType _weekDay;

    /**
     * Field _dateDay
     */
    private org.exolab.castor.types.Date _dateDay;

    //----------------/
    //- Constructors -/
    //----------------/

    public IncludingItem() {
        super();
    } //-- grms_schema.IncludingItem()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'dateDay'.
     * 
     * @return Date
     * @return the value of field 'dateDay'.
     */
    public org.exolab.castor.types.Date getDateDay() {
        return this._dateDay;
    } //-- org.exolab.castor.types.Date getDateDay() 

    /**
     * Returns the value of field 'weekDay'.
     * 
     * @return WeekDayType
     * @return the value of field 'weekDay'.
     */
    public grms_schema.types.WeekDayType getWeekDay() {
        return this._weekDay;
    } //-- grms_schema.types.WeekDayType getWeekDay() 

    /**
     * Sets the value of field 'dateDay'.
     * 
     * @param dateDay the value of field 'dateDay'.
     */
    public void setDateDay(org.exolab.castor.types.Date dateDay) {
        this._dateDay = dateDay;
    } //-- void setDateDay(org.exolab.castor.types.Date) 

    /**
     * Sets the value of field 'weekDay'.
     * 
     * @param weekDay the value of field 'weekDay'.
     */
    public void setWeekDay(grms_schema.types.WeekDayType weekDay) {
        this._weekDay = weekDay;
    } //-- void setWeekDay(grms_schema.types.WeekDayType) 

}
