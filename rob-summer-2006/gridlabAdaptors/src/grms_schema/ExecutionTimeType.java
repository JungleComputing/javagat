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
 * Class ExecutionTimeType.
 *
 * @version $Revision: 1.4 $ $Date$
 */
public class ExecutionTimeType implements java.io.Serializable {
    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Information when to start application
     */
    private grms_schema.TimeSlot _timeSlot;

    /**
     * Field _execDuration
     */
    private org.exolab.castor.types.Duration _execDuration;

    /**
     * Field _timePeriod
     */
    private grms_schema.TimePeriod _timePeriod;

    //----------------/
    //- Constructors -/
    //----------------/
    public ExecutionTimeType() {
        super();
    } //-- grms_schema.ExecutionTimeType()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'execDuration'.
     *
     * @return Duration
     * @return the value of field 'execDuration'.
     */
    public org.exolab.castor.types.Duration getExecDuration() {
        return this._execDuration;
    } //-- org.exolab.castor.types.Duration getExecDuration() 

    /**
     * Returns the value of field 'timePeriod'.
     *
     * @return TimePeriod
     * @return the value of field 'timePeriod'.
     */
    public grms_schema.TimePeriod getTimePeriod() {
        return this._timePeriod;
    } //-- grms_schema.TimePeriod getTimePeriod() 

    /**
     * Returns the value of field 'timeSlot'. The field 'timeSlot'
     * has the following description: Information when to start
     * application
     *
     * @return TimeSlot
     * @return the value of field 'timeSlot'.
     */
    public grms_schema.TimeSlot getTimeSlot() {
        return this._timeSlot;
    } //-- grms_schema.TimeSlot getTimeSlot() 

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
     * Sets the value of field 'execDuration'.
     *
     * @param execDuration the value of field 'execDuration'.
     */
    public void setExecDuration(org.exolab.castor.types.Duration execDuration) {
        this._execDuration = execDuration;
    } //-- void setExecDuration(org.exolab.castor.types.Duration) 

    /**
     * Sets the value of field 'timePeriod'.
     *
     * @param timePeriod the value of field 'timePeriod'.
     */
    public void setTimePeriod(grms_schema.TimePeriod timePeriod) {
        this._timePeriod = timePeriod;
    } //-- void setTimePeriod(grms_schema.TimePeriod) 

    /**
     * Sets the value of field 'timeSlot'. The field 'timeSlot' has
     * the following description: Information when to start
     * application
     *
     * @param timeSlot the value of field 'timeSlot'.
     */
    public void setTimeSlot(grms_schema.TimeSlot timeSlot) {
        this._timeSlot = timeSlot;
    } //-- void setTimeSlot(grms_schema.TimeSlot) 

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
        return (grms_schema.ExecutionTimeType) Unmarshaller.unmarshal(
            grms_schema.ExecutionTimeType.class, reader);
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
