/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: CheckpointDescriptor.java,v 1.4 2004/10/27 10:30:05 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

/**
 * Class CheckpointDescriptor.
 * 
 * @version $Revision: 1.4 $ $Date: 2004/10/27 10:30:05 $
 */
public class CheckpointDescriptor extends
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

	public CheckpointDescriptor() {
		super();
		xmlName = "checkpoint";

		//-- set grouping compositor
		setCompositorAsChoice();
		org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
		org.exolab.castor.xml.XMLFieldHandler handler = null;
		org.exolab.castor.xml.FieldValidator fieldValidator = null;
		//-- initialize attribute descriptors

		//-- initialize element descriptors

		//-- _directory
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.Directory.class, "_directory", "directory",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Checkpoint target = (Checkpoint) object;
				return target.getDirectory();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Checkpoint target = (Checkpoint) object;
					target.setDirectory((grms_schema.Directory) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.Directory();
			}
		});
		desc.setHandler(handler);
		desc.setRequired(true);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _directory
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(1);
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
				Checkpoint target = (Checkpoint) object;
				return target.getFile();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Checkpoint target = (Checkpoint) object;
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
		desc.setRequired(true);
		desc.setMultivalued(true);
		addFieldDescriptor(desc);

		//-- validation code for: _fileList
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(1);
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
	} //-- grms_schema.CheckpointDescriptor()

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
		return grms_schema.Checkpoint.class;
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