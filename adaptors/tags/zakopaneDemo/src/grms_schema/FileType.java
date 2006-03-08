/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: FileType.java,v 1.3 2004/10/27 10:30:05 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Element that describes a file
 * 
 * @version $Revision: 1.3 $ $Date: 2004/10/27 10:30:05 $
 */
public class FileType extends grms_schema.BaseFile implements
		java.io.Serializable {

	//--------------------------/
	//- Class/Member Variables -/
	//--------------------------/

	/**
	 * Field _name
	 */
	private java.lang.String _name;

	/**
	 * Field _type
	 */
	private grms_schema.types.BaseFileTypeType _type;

	//----------------/
	//- Constructors -/
	//----------------/

	public FileType() {
		super();
	} //-- grms_schema.FileType()

	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Returns the value of field 'name'.
	 * 
	 * @return the value of field 'name'.
	 */
	public java.lang.String getName() {
		return this._name;
	} //-- java.lang.String getName()

	/**
	 * Returns the value of field 'type'.
	 * 
	 * @return the value of field 'type'.
	 */
	public grms_schema.types.BaseFileTypeType getType() {
		return this._type;
	} //-- grms_schema.types.BaseFileTypeType getType()

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
	 * Sets the value of field 'name'.
	 * 
	 * @param name
	 *            the value of field 'name'.
	 */
	public void setName(java.lang.String name) {
		this._name = name;
	} //-- void setName(java.lang.String)

	/**
	 * Sets the value of field 'type'.
	 * 
	 * @param type
	 *            the value of field 'type'.
	 */
	public void setType(grms_schema.types.BaseFileTypeType type) {
		this._type = type;
	} //-- void setType(grms_schema.types.BaseFileTypeType)

	/**
	 * Method unmarshal
	 * 
	 * @param reader
	 */
	public static java.lang.Object unmarshal(java.io.Reader reader)
			throws org.exolab.castor.xml.MarshalException,
			org.exolab.castor.xml.ValidationException {
		return (grms_schema.FileType) Unmarshaller.unmarshal(
				grms_schema.FileType.class, reader);
	} //-- java.lang.Object unmarshal(java.io.Reader)

	/**
	 * Method validate
	 */
	public void validate() throws org.exolab.castor.xml.ValidationException {
		org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
		validator.validate(this);
	} //-- void validate()

}