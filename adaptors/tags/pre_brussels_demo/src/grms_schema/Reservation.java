/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: Reservation.java,v 1.1 2004/06/21 09:05:33 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Parameters for advnced scheduling
 * 
 * @version $Revision: 1.1 $ $Date: 2004/06/21 09:05:33 $
 */
public class Reservation implements java.io.Serializable {

	//--------------------------/
	//- Class/Member Variables -/
	//--------------------------/

	/**
	 * Deadline for job execution
	 */
	private java.util.Date _deadline;

	/**
	 * Earliest possible time for job execution.
	 */
	private java.util.Date _estime;

	/**
	 * Size of the data that need to be transfered to the resource
	 */
	private double _datasize;

	/**
	 * keeps track of state for field: _datasize
	 */
	private boolean _has_datasize;

	//----------------/
	//- Constructors -/
	//----------------/

	public Reservation() {
		super();
	} //-- grms_schema.Reservation()

	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Method deleteDatasize
	 */
	public void deleteDatasize() {
		this._has_datasize = false;
	} //-- void deleteDatasize()

	/**
	 * Returns the value of field 'datasize'. The field 'datasize' has the
	 * following description: Size of the data that need to be transfered to the
	 * resource
	 * 
	 * @return the value of field 'datasize'.
	 */
	public double getDatasize() {
		return this._datasize;
	} //-- double getDatasize()

	/**
	 * Returns the value of field 'deadline'. The field 'deadline' has the
	 * following description: Deadline for job execution
	 * 
	 * @return the value of field 'deadline'.
	 */
	public java.util.Date getDeadline() {
		return this._deadline;
	} //-- java.util.Date getDeadline()

	/**
	 * Returns the value of field 'estime'. The field 'estime' has the following
	 * description: Earliest possible time for job execution.
	 * 
	 * @return the value of field 'estime'.
	 */
	public java.util.Date getEstime() {
		return this._estime;
	} //-- java.util.Date getEstime()

	/**
	 * Method hasDatasize
	 */
	public boolean hasDatasize() {
		return this._has_datasize;
	} //-- boolean hasDatasize()

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
	 * Sets the value of field 'datasize'. The field 'datasize' has the
	 * following description: Size of the data that need to be transfered to the
	 * resource
	 * 
	 * @param datasize
	 *            the value of field 'datasize'.
	 */
	public void setDatasize(double datasize) {
		this._datasize = datasize;
		this._has_datasize = true;
	} //-- void setDatasize(double)

	/**
	 * Sets the value of field 'deadline'. The field 'deadline' has the
	 * following description: Deadline for job execution
	 * 
	 * @param deadline
	 *            the value of field 'deadline'.
	 */
	public void setDeadline(java.util.Date deadline) {
		this._deadline = deadline;
	} //-- void setDeadline(java.util.Date)

	/**
	 * Sets the value of field 'estime'. The field 'estime' has the following
	 * description: Earliest possible time for job execution.
	 * 
	 * @param estime
	 *            the value of field 'estime'.
	 */
	public void setEstime(java.util.Date estime) {
		this._estime = estime;
	} //-- void setEstime(java.util.Date)

	/**
	 * Method unmarshal
	 * 
	 * @param reader
	 */
	public static java.lang.Object unmarshal(java.io.Reader reader)
			throws org.exolab.castor.xml.MarshalException,
			org.exolab.castor.xml.ValidationException {
		return (grms_schema.Reservation) Unmarshaller.unmarshal(
				grms_schema.Reservation.class, reader);
	} //-- java.lang.Object unmarshal(java.io.Reader)

	/**
	 * Method validate
	 */
	public void validate() throws org.exolab.castor.xml.ValidationException {
		org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
		validator.validate(this);
	} //-- void validate()

}