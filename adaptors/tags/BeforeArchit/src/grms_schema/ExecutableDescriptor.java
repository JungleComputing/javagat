/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: ExecutableDescriptor.java,v 1.1 2004/06/21 09:05:33 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.validators.IntegerValidator;

/**
 * Class ExecutableDescriptor.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/06/21 09:05:33 $
 */
public class ExecutableDescriptor extends
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

	public ExecutableDescriptor() {
		super();
		xmlName = "executable";

		//-- set grouping compositor
		setCompositorAsSequence();
		org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
		org.exolab.castor.xml.XMLFieldHandler handler = null;
		org.exolab.castor.xml.FieldValidator fieldValidator = null;
		//-- initialize attribute descriptors

		//-- _type
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.types.ExecutableTypeType.class, "_type", "type",
				org.exolab.castor.xml.NodeType.Attribute);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Executable target = (Executable) object;
				return target.getType();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Executable target = (Executable) object;
					target
							.setType((grms_schema.types.ExecutableTypeType) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(new org.exolab.castor.xml.handlers.EnumFieldHandler(
				grms_schema.types.ExecutableTypeType.class, handler));
		desc.setImmutable(true);
		desc.setRequired(true);
		addFieldDescriptor(desc);

		//-- validation code for: _type
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(1);
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _count
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.Integer.TYPE, "_count", "count",
				org.exolab.castor.xml.NodeType.Attribute);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Executable target = (Executable) object;
				if (!target.hasCount())
					return null;
				return new java.lang.Integer(target.getCount());
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Executable target = (Executable) object;
					// if null, use delete method for optional primitives
					if (value == null) {
						target.deleteCount();
						return;
					}
					target.setCount(((java.lang.Integer) value).intValue());
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(handler);
		addFieldDescriptor(desc);

		//-- validation code for: _count
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			IntegerValidator typeValidator = new IntegerValidator();
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
		//-- initialize element descriptors

		//-- _file
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.File.class, "_file", "file",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Executable target = (Executable) object;
				return target.getFile();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Executable target = (Executable) object;
					target.setFile((grms_schema.File) value);
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
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _file
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(1);
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _arguments
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.Arguments.class, "_arguments", "arguments",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Executable target = (Executable) object;
				return target.getArguments();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Executable target = (Executable) object;
					target.setArguments((grms_schema.Arguments) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.Arguments();
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _arguments
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _stdin
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.Stdin.class, "_stdin", "stdin",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Executable target = (Executable) object;
				return target.getStdin();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Executable target = (Executable) object;
					target.setStdin((grms_schema.Stdin) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.Stdin();
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _stdin
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _stdout
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.Stdout.class, "_stdout", "stdout",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Executable target = (Executable) object;
				return target.getStdout();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Executable target = (Executable) object;
					target.setStdout((grms_schema.Stdout) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.Stdout();
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _stdout
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _stderr
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.Stderr.class, "_stderr", "stderr",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Executable target = (Executable) object;
				return target.getStderr();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Executable target = (Executable) object;
					target.setStderr((grms_schema.Stderr) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.Stderr();
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _stderr
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _environment
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.Environment.class, "_environment", "environment",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Executable target = (Executable) object;
				return target.getEnvironment();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Executable target = (Executable) object;
					target.setEnvironment((grms_schema.Environment) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.Environment();
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _environment
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _checkpoint
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.Checkpoint.class, "_checkpoint", "checkpoint",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Executable target = (Executable) object;
				return target.getCheckpoint();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Executable target = (Executable) object;
					target.setCheckpoint((grms_schema.Checkpoint) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.Checkpoint();
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _checkpoint
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
	} //-- grms_schema.ExecutableDescriptor()

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
		return grms_schema.Executable.class;
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