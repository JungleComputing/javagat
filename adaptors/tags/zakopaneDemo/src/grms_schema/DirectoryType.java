/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: DirectoryType.java,v 1.3 2004/10/27 10:30:05 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Element that describes a directory
 * 
 * @version $Revision: 1.3 $ $Date: 2004/10/27 10:30:05 $
 */
public class DirectoryType implements java.io.Serializable {

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
	private grms_schema.types.DirectoryTypeTypeType _type;

	/**
	 * Url location of directory
	 */
	private java.lang.String _url;

	/**
	 * Collection from Replica Management System - not implemented yet
	 */
	private grms_schema.Collection _collection;

	//----------------/
	//- Constructors -/
	//----------------/

	public DirectoryType() {
		super();
	} //-- grms_schema.DirectoryType()

	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Returns the value of field 'collection'. The field 'collection' has the
	 * following description: Collection from Replica Management System - not
	 * implemented yet
	 * 
	 * @return the value of field 'collection'.
	 */
	public grms_schema.Collection getCollection() {
		return this._collection;
	} //-- grms_schema.Collection getCollection()

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
	public grms_schema.types.DirectoryTypeTypeType getType() {
		return this._type;
	} //-- grms_schema.types.DirectoryTypeTypeType getType()

	/**
	 * Returns the value of field 'url'. The field 'url' has the following
	 * description: Url location of directory
	 * 
	 * @return the value of field 'url'.
	 */
	public java.lang.String getUrl() {
		return this._url;
	} //-- java.lang.String getUrl()

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
	 * Sets the value of field 'collection'. The field 'collection' has the
	 * following description: Collection from Replica Management System - not
	 * implemented yet
	 * 
	 * @param collection
	 *            the value of field 'collection'.
	 */
	public void setCollection(grms_schema.Collection collection) {
		this._collection = collection;
	} //-- void setCollection(grms_schema.Collection)

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
	public void setType(grms_schema.types.DirectoryTypeTypeType type) {
		this._type = type;
	} //-- void setType(grms_schema.types.DirectoryTypeTypeType)

	/**
	 * Sets the value of field 'url'. The field 'url' has the following
	 * description: Url location of directory
	 * 
	 * @param url
	 *            the value of field 'url'.
	 */
	public void setUrl(java.lang.String url) {
		this._url = url;
	} //-- void setUrl(java.lang.String)

	/**
	 * Method unmarshal
	 * 
	 * @param reader
	 */
	public static java.lang.Object unmarshal(java.io.Reader reader)
			throws org.exolab.castor.xml.MarshalException,
			org.exolab.castor.xml.ValidationException {
		return (grms_schema.DirectoryType) Unmarshaller.unmarshal(
				grms_schema.DirectoryType.class, reader);
	} //-- java.lang.Object unmarshal(java.io.Reader)

	/**
	 * Method validate
	 */
	public void validate() throws org.exolab.castor.xml.ValidationException {
		org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
		validator.validate(this);
	} //-- void validate()

}