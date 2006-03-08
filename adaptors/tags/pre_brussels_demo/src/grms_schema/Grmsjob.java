/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: Grmsjob.java,v 1.1 2004/06/21 09:05:33 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Job Description for Gridlab Resource Management System
 * 
 * @version $Revision: 1.1 $ $Date: 2004/06/21 09:05:33 $
 */
public class Grmsjob implements java.io.Serializable {

	//--------------------------/
	//- Class/Member Variables -/
	//--------------------------/

	/**
	 * Field _appid
	 */
	private java.lang.String _appid;

	/**
	 * Description of Simple Job
	 */
	private grms_schema.Simplejob _simplejob;

	/**
	 * Description of Workflow Job (not implemented yet)
	 */
	private grms_schema.Workflowjob _workflowjob;

	//----------------/
	//- Constructors -/
	//----------------/

	public Grmsjob() {
		super();
	} //-- grms_schema.Grmsjob()

	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Returns the value of field 'appid'.
	 * 
	 * @return the value of field 'appid'.
	 */
	public java.lang.String getAppid() {
		return this._appid;
	} //-- java.lang.String getAppid()

	/**
	 * Returns the value of field 'simplejob'. The field 'simplejob' has the
	 * following description: Description of Simple Job
	 * 
	 * @return the value of field 'simplejob'.
	 */
	public grms_schema.Simplejob getSimplejob() {
		return this._simplejob;
	} //-- grms_schema.Simplejob getSimplejob()

	/**
	 * Returns the value of field 'workflowjob'. The field 'workflowjob' has the
	 * following description: Description of Workflow Job (not implemented yet)
	 * 
	 * @return the value of field 'workflowjob'.
	 */
	public grms_schema.Workflowjob getWorkflowjob() {
		return this._workflowjob;
	} //-- grms_schema.Workflowjob getWorkflowjob()

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
	 * Sets the value of field 'appid'.
	 * 
	 * @param appid
	 *            the value of field 'appid'.
	 */
	public void setAppid(java.lang.String appid) {
		this._appid = appid;
	} //-- void setAppid(java.lang.String)

	/**
	 * Sets the value of field 'simplejob'. The field 'simplejob' has the
	 * following description: Description of Simple Job
	 * 
	 * @param simplejob
	 *            the value of field 'simplejob'.
	 */
	public void setSimplejob(grms_schema.Simplejob simplejob) {
		this._simplejob = simplejob;
	} //-- void setSimplejob(grms_schema.Simplejob)

	/**
	 * Sets the value of field 'workflowjob'. The field 'workflowjob' has the
	 * following description: Description of Workflow Job (not implemented yet)
	 * 
	 * @param workflowjob
	 *            the value of field 'workflowjob'.
	 */
	public void setWorkflowjob(grms_schema.Workflowjob workflowjob) {
		this._workflowjob = workflowjob;
	} //-- void setWorkflowjob(grms_schema.Workflowjob)

	/**
	 * Method unmarshal
	 * 
	 * @param reader
	 */
	public static java.lang.Object unmarshal(java.io.Reader reader)
			throws org.exolab.castor.xml.MarshalException,
			org.exolab.castor.xml.ValidationException {
		return (grms_schema.Grmsjob) Unmarshaller.unmarshal(
				grms_schema.Grmsjob.class, reader);
	} //-- java.lang.Object unmarshal(java.io.Reader)

	/**
	 * Method validate
	 */
	public void validate() throws org.exolab.castor.xml.ValidationException {
		org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
		validator.validate(this);
	} //-- void validate()

}