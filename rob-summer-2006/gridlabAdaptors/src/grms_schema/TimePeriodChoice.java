/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id$
 */
package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class TimePeriodChoice.
 *
 * @version $Revision: 1.4 $ $Date$
 */
public class TimePeriodChoice implements java.io.Serializable {
    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _periodEnd
     */
    private java.util.Date _periodEnd;

    /**
     * Field _periodDuration
     */
    private org.exolab.castor.types.Duration _periodDuration;

    //----------------/
    //- Constructors -/
    //----------------/
    public TimePeriodChoice() {
        super();
    } //-- grms_schema.TimePeriodChoice()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'periodDuration'.
     *
     * @return Duration
     * @return the value of field 'periodDuration'.
     */
    public org.exolab.castor.types.Duration getPeriodDuration() {
        return this._periodDuration;
    } //-- org.exolab.castor.types.Duration getPeriodDuration() 

    /**
     * Returns the value of field 'periodEnd'.
     *
     * @return Date
     * @return the value of field 'periodEnd'.
     */
    public java.util.Date getPeriodEnd() {
        return this._periodEnd;
    } //-- java.util.Date getPeriodEnd() 

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
     * Sets the value of field 'periodDuration'.
     *
     * @param periodDuration the value of field 'periodDuration'.
     */
    public void setPeriodDuration(
            org.exolab.castor.types.Duration periodDuration) {
        this._periodDuration = periodDuration;
    } //-- void setPeriodDuration(org.exolab.castor.types.Duration) 

    /**
     * Sets the value of field 'periodEnd'.
     *
     * @param periodEnd the value of field 'periodEnd'.
     */
    public void setPeriodEnd(java.util.Date periodEnd) {
        this._periodEnd = periodEnd;
    } //-- void setPeriodEnd(java.util.Date) 

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
        return (grms_schema.TimePeriodChoice) Unmarshaller.unmarshal(
            grms_schema.TimePeriodChoice.class, reader);
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
