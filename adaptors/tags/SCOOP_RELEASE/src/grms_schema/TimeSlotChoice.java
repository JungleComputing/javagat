/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: TimeSlotChoice.java,v 1.1 2005/04/07 13:48:19 rob Exp $
 */

package grms_schema;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class TimeSlotChoice.
 * 
 * @version $Revision: 1.1 $ $Date: 2005/04/07 13:48:19 $
 */
public class TimeSlotChoice implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _slotEnd
     */
    private org.exolab.castor.types.Time _slotEnd;

    /**
     * Field _slotDuration
     */
    private org.exolab.castor.types.Duration _slotDuration;


      //----------------/
     //- Constructors -/
    //----------------/

    public TimeSlotChoice() {
        super();
    } //-- grms_schema.TimeSlotChoice()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'slotDuration'.
     * 
     * @return Duration
     * @return the value of field 'slotDuration'.
     */
    public org.exolab.castor.types.Duration getSlotDuration()
    {
        return this._slotDuration;
    } //-- org.exolab.castor.types.Duration getSlotDuration() 

    /**
     * Returns the value of field 'slotEnd'.
     * 
     * @return Time
     * @return the value of field 'slotEnd'.
     */
    public org.exolab.castor.types.Time getSlotEnd()
    {
        return this._slotEnd;
    } //-- org.exolab.castor.types.Time getSlotEnd() 

    /**
     * Method isValid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
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
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
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
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'slotDuration'.
     * 
     * @param slotDuration the value of field 'slotDuration'.
     */
    public void setSlotDuration(org.exolab.castor.types.Duration slotDuration)
    {
        this._slotDuration = slotDuration;
    } //-- void setSlotDuration(org.exolab.castor.types.Duration) 

    /**
     * Sets the value of field 'slotEnd'.
     * 
     * @param slotEnd the value of field 'slotEnd'.
     */
    public void setSlotEnd(org.exolab.castor.types.Time slotEnd)
    {
        this._slotEnd = slotEnd;
    } //-- void setSlotEnd(org.exolab.castor.types.Time) 

    /**
     * Method unmarshal
     * 
     * 
     * 
     * @param reader
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (grms_schema.TimeSlotChoice) Unmarshaller.unmarshal(grms_schema.TimeSlotChoice.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     * 
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
