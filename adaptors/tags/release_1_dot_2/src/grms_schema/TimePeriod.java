/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: TimePeriod.java,v 1.2 2005/10/07 11:05:55 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class TimePeriod.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/10/07 11:05:55 $
 */
public class TimePeriod implements java.io.Serializable {

    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _periodStart
     */
    private java.util.Date _periodStart;

    /**
     * Field _timePeriodChoice
     */
    private grms_schema.TimePeriodChoice _timePeriodChoice;

    /**
     * Field _excluding
     */
    private grms_schema.Excluding _excluding;

    /**
     * Field _including
     */
    private grms_schema.Including _including;

    //----------------/
    //- Constructors -/
    //----------------/

    public TimePeriod() {
        super();
    } //-- grms_schema.TimePeriod()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'excluding'.
     * 
     * @return Excluding
     * @return the value of field 'excluding'.
     */
    public grms_schema.Excluding getExcluding() {
        return this._excluding;
    } //-- grms_schema.Excluding getExcluding() 

    /**
     * Returns the value of field 'including'.
     * 
     * @return Including
     * @return the value of field 'including'.
     */
    public grms_schema.Including getIncluding() {
        return this._including;
    } //-- grms_schema.Including getIncluding() 

    /**
     * Returns the value of field 'periodStart'.
     * 
     * @return Date
     * @return the value of field 'periodStart'.
     */
    public java.util.Date getPeriodStart() {
        return this._periodStart;
    } //-- java.util.Date getPeriodStart() 

    /**
     * Returns the value of field 'timePeriodChoice'.
     * 
     * @return TimePeriodChoice
     * @return the value of field 'timePeriodChoice'.
     */
    public grms_schema.TimePeriodChoice getTimePeriodChoice() {
        return this._timePeriodChoice;
    } //-- grms_schema.TimePeriodChoice getTimePeriodChoice() 

    /**
     * Method isValid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean isValid() {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param out
     */
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException,
        org.exolab.castor.xml.ValidationException {

        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException,
        org.exolab.castor.xml.ValidationException {

        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'excluding'.
     * 
     * @param excluding the value of field 'excluding'.
     */
    public void setExcluding(grms_schema.Excluding excluding) {
        this._excluding = excluding;
    } //-- void setExcluding(grms_schema.Excluding) 

    /**
     * Sets the value of field 'including'.
     * 
     * @param including the value of field 'including'.
     */
    public void setIncluding(grms_schema.Including including) {
        this._including = including;
    } //-- void setIncluding(grms_schema.Including) 

    /**
     * Sets the value of field 'periodStart'.
     * 
     * @param periodStart the value of field 'periodStart'.
     */
    public void setPeriodStart(java.util.Date periodStart) {
        this._periodStart = periodStart;
    } //-- void setPeriodStart(java.util.Date) 

    /**
     * Sets the value of field 'timePeriodChoice'.
     * 
     * @param timePeriodChoice the value of field 'timePeriodChoice'
     */
    public void setTimePeriodChoice(
        grms_schema.TimePeriodChoice timePeriodChoice) {
        this._timePeriodChoice = timePeriodChoice;
    } //-- void setTimePeriodChoice(grms_schema.TimePeriodChoice) 

    /**
     * Method unmarshal
     * 
     * 
     * 
     * @param reader
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException,
        org.exolab.castor.xml.ValidationException {
        return (grms_schema.TimePeriod) Unmarshaller.unmarshal(
            grms_schema.TimePeriod.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     * 
     */
    public void validate() throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
