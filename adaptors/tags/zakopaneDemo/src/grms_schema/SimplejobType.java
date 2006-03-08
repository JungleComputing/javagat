/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: SimplejobType.java,v 1.3 2004/10/27 10:30:05 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Description of Simple Job
 * 
 * @version $Revision: 1.3 $ $Date: 2004/10/27 10:30:05 $
 */
public class SimplejobType implements java.io.Serializable {

	//--------------------------/
	//- Class/Member Variables -/
	//--------------------------/

	/**
	 * Resource requirement section
	 */
	private grms_schema.Resource _resource;

	/**
	 * Application description section
	 */
	private grms_schema.Executable _executable;

	/**
	 * Parameters for advnced scheduling
	 */
	private grms_schema.Reservation _reservation;

	//----------------/
	//- Constructors -/
	//----------------/

	public SimplejobType() {
		super();
	} //-- grms_schema.SimplejobType()

	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Returns the value of field 'executable'. The field 'executable' has the
	 * following description: Application description section
	 * 
	 * @return the value of field 'executable'.
	 */
	public grms_schema.Executable getExecutable() {
		return this._executable;
	} //-- grms_schema.Executable getExecutable()

	/**
	 * Returns the value of field 'reservation'. The field 'reservation' has the
	 * following description: Parameters for advnced scheduling
	 * 
	 * @return the value of field 'reservation'.
	 */
	public grms_schema.Reservation getReservation() {
		return this._reservation;
	} //-- grms_schema.Reservation getReservation()

	/**
	 * Returns the value of field 'resource'. The field 'resource' has the
	 * following description: Resource requirement section
	 * 
	 * @return the value of field 'resource'.
	 */
	public grms_schema.Resource getResource() {
		return this._resource;
	} //-- grms_schema.Resource getResource()

	/**
	 * Method isValid
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
	 * @param handler
	 */
	public void marshal(org.xml.sax.ContentHandler handler)
			throws java.io.IOException, org.exolab.castor.xml.MarshalException,
			org.exolab.castor.xml.ValidationException {

		Marshaller.marshal(this, handler);
	} //-- void marshal(org.xml.sax.ContentHandler)

	/**
	 * Sets the value of field 'executable'. The field 'executable' has the
	 * following description: Application description section
	 * 
	 * @param executable
	 *            the value of field 'executable'.
	 */
	public void setExecutable(grms_schema.Executable executable) {
		this._executable = executable;
	} //-- void setExecutable(grms_schema.Executable)

	/**
	 * Sets the value of field 'reservation'. The field 'reservation' has the
	 * following description: Parameters for advnced scheduling
	 * 
	 * @param reservation
	 *            the value of field 'reservation'.
	 */
	public void setReservation(grms_schema.Reservation reservation) {
		this._reservation = reservation;
	} //-- void setReservation(grms_schema.Reservation)

	/**
	 * Sets the value of field 'resource'. The field 'resource' has the
	 * following description: Resource requirement section
	 * 
	 * @param resource
	 *            the value of field 'resource'.
	 */
	public void setResource(grms_schema.Resource resource) {
		this._resource = resource;
	} //-- void setResource(grms_schema.Resource)

	/**
	 * Method unmarshal
	 * 
	 * @param reader
	 */
	public static java.lang.Object unmarshal(java.io.Reader reader)
			throws org.exolab.castor.xml.MarshalException,
			org.exolab.castor.xml.ValidationException {
		return (grms_schema.SimplejobType) Unmarshaller.unmarshal(
				grms_schema.SimplejobType.class, reader);
	} //-- java.lang.Object unmarshal(java.io.Reader)

	/**
	 * Method validate
	 */
	public void validate() throws org.exolab.castor.xml.ValidationException {
		org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
		validator.validate(this);
	} //-- void validate()

}