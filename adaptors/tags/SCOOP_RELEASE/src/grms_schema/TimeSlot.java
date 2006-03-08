/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: TimeSlot.java,v 1.1 2005/04/07 13:48:19 rob Exp $
 */

package grms_schema;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Information when to start application
 * 
 * @version $Revision: 1.1 $ $Date: 2005/04/07 13:48:19 $
 */
public class TimeSlot implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _slotStart
     */
    private grms_schema.SlotStart _slotStart;

    /**
     * Field _timeSlotChoice
     */
    private grms_schema.TimeSlotChoice _timeSlotChoice;


      //----------------/
     //- Constructors -/
    //----------------/

    public TimeSlot() {
        super();
    } //-- grms_schema.TimeSlot()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'slotStart'.
     * 
     * @return SlotStart
     * @return the value of field 'slotStart'.
     */
    public grms_schema.SlotStart getSlotStart()
    {
        return this._slotStart;
    } //-- grms_schema.SlotStart getSlotStart() 

    /**
     * Returns the value of field 'timeSlotChoice'.
     * 
     * @return TimeSlotChoice
     * @return the value of field 'timeSlotChoice'.
     */
    public grms_schema.TimeSlotChoice getTimeSlotChoice()
    {
        return this._timeSlotChoice;
    } //-- grms_schema.TimeSlotChoice getTimeSlotChoice() 

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
     * Sets the value of field 'slotStart'.
     * 
     * @param slotStart the value of field 'slotStart'.
     */
    public void setSlotStart(grms_schema.SlotStart slotStart)
    {
        this._slotStart = slotStart;
    } //-- void setSlotStart(grms_schema.SlotStart) 

    /**
     * Sets the value of field 'timeSlotChoice'.
     * 
     * @param timeSlotChoice the value of field 'timeSlotChoice'.
     */
    public void setTimeSlotChoice(grms_schema.TimeSlotChoice timeSlotChoice)
    {
        this._timeSlotChoice = timeSlotChoice;
    } //-- void setTimeSlotChoice(grms_schema.TimeSlotChoice) 

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
        return (grms_schema.TimeSlot) Unmarshaller.unmarshal(grms_schema.TimeSlot.class, reader);
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
