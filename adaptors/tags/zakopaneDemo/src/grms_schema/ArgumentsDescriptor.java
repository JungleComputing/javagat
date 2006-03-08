/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: ArgumentsDescriptor.java,v 1.4 2004/10/27 10:30:04 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

/**
 * Class ArgumentsDescriptor.
 * 
 * @version $Revision: 1.4 $ $Date: 2004/10/27 10:30:04 $
 */
public class ArgumentsDescriptor extends
		org.exolab.castor.xml.util.XMLClassDescriptorImpl {

	//--------------------------/
	//- Class/Member Variables -/
	//--------------------------/

	/**
	 * Field nsPrefix
	 */
	private java.lang.String nsPrefix;

	/**
	 * Field nsURI
	 */
	private java.lang.String nsURI;

	/**
	 * Field xmlName
	 */
	private java.lang.String xmlName;

	/**
	 * Field identity
	 */
	private org.exolab.castor.xml.XMLFieldDescriptor identity;

	//----------------/
	//- Constructors -/
	//----------------/

	public ArgumentsDescriptor() {
		super();
		xmlName = "arguments";

		//-- set grouping compositor
		setCompositorAsSequence();
		org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
		org.exolab.castor.xml.XMLFieldHandler handler = null;
		org.exolab.castor.xml.FieldValidator fieldValidator = null;
		//-- initialize attribute descriptors

		//-- initialize element descriptors

		//-- _valueList
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.Value.class, "_valueList", "value",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Arguments target = (Arguments) object;
				return target.getValue();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Arguments target = (Arguments) object;
					target.addValue((grms_schema.Value) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.Value();
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(true);
		addFieldDescriptor(desc);

		//-- validation code for: _valueList
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(0);
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _fileList
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.File.class, "_fileList", "file",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Arguments target = (Arguments) object;
				return target.getFile();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Arguments target = (Arguments) object;
					target.addFile((grms_schema.File) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.File();
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(true);
		addFieldDescriptor(desc);

		//-- validation code for: _fileList
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(0);
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _directoryList
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.Directory.class, "_directoryList", "directory",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Arguments target = (Arguments) object;
				return target.getDirectory();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Arguments target = (Arguments) object;
					target.addDirectory((grms_schema.Directory) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.Directory();
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(true);
		addFieldDescriptor(desc);

		//-- validation code for: _directoryList
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(0);
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
	} //-- grms_schema.ArgumentsDescriptor()

	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Method getAccessMode
	 */
	public org.exolab.castor.mapping.AccessMode getAccessMode() {
		return null;
	} //-- org.exolab.castor.mapping.AccessMode getAccessMode()

	/**
	 * Method getExtends
	 */
	public org.exolab.castor.mapping.ClassDescriptor getExtends() {
		return null;
	} //-- org.exolab.castor.mapping.ClassDescriptor getExtends()

	/**
	 * Method getIdentity
	 */
	public org.exolab.castor.mapping.FieldDescriptor getIdentity() {
		return identity;
	} //-- org.exolab.castor.mapping.FieldDescriptor getIdentity()

	/**
	 * Method getJavaClass
	 */
	public java.lang.Class getJavaClass() {
		return grms_schema.Arguments.class;
	} //-- java.lang.Class getJavaClass()

	/**
	 * Method getNameSpacePrefix
	 */
	public java.lang.String getNameSpacePrefix() {
		return nsPrefix;
	} //-- java.lang.String getNameSpacePrefix()

	/**
	 * Method getNameSpaceURI
	 */
	public java.lang.String getNameSpaceURI() {
		return nsURI;
	} //-- java.lang.String getNameSpaceURI()

	/**
	 * Method getValidator
	 */
	public org.exolab.castor.xml.TypeValidator getValidator() {
		return this;
	} //-- org.exolab.castor.xml.TypeValidator getValidator()

	/**
	 * Method getXMLName
	 */
	public java.lang.String getXMLName() {
		return xmlName;
	} //-- java.lang.String getXMLName()

}