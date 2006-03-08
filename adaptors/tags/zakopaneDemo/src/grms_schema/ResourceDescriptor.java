/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: ResourceDescriptor.java,v 1.3 2004/10/27 10:30:05 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.validators.IntegerValidator;
import org.exolab.castor.xml.validators.StringValidator;

/**
 * Class ResourceDescriptor.
 * 
 * @version $Revision: 1.3 $ $Date: 2004/10/27 10:30:05 $
 */
public class ResourceDescriptor extends
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

	public ResourceDescriptor() {
		super();
		xmlName = "resource";
		org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
		org.exolab.castor.xml.XMLFieldHandler handler = null;
		org.exolab.castor.xml.FieldValidator fieldValidator = null;
		//-- initialize attribute descriptors

		//-- initialize element descriptors

		//-- _osname
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.String.class, "_osname", "osname",
				org.exolab.castor.xml.NodeType.Element);
		desc.setImmutable(true);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				return target.getOsname();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					target.setOsname((java.lang.String) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _osname
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			StringValidator typeValidator = new StringValidator();
			typeValidator.setWhiteSpace("preserve");
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
		//-- _ostype
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.String.class, "_ostype", "ostype",
				org.exolab.castor.xml.NodeType.Element);
		desc.setImmutable(true);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				return target.getOstype();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					target.setOstype((java.lang.String) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _ostype
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			StringValidator typeValidator = new StringValidator();
			typeValidator.setWhiteSpace("preserve");
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
		//-- _osversion
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.String.class, "_osversion", "osversion",
				org.exolab.castor.xml.NodeType.Element);
		desc.setImmutable(true);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				return target.getOsversion();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					target.setOsversion((java.lang.String) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _osversion
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			StringValidator typeValidator = new StringValidator();
			typeValidator.setWhiteSpace("preserve");
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
		//-- _osrelease
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.String.class, "_osrelease", "osrelease",
				org.exolab.castor.xml.NodeType.Element);
		desc.setImmutable(true);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				return target.getOsrelease();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					target.setOsrelease((java.lang.String) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _osrelease
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			StringValidator typeValidator = new StringValidator();
			typeValidator.setWhiteSpace("preserve");
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
		//-- _hostname
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.String.class, "_hostname", "hostname",
				org.exolab.castor.xml.NodeType.Element);
		desc.setImmutable(true);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				return target.getHostname();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					target.setHostname((java.lang.String) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _hostname
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			StringValidator typeValidator = new StringValidator();
			typeValidator.setWhiteSpace("preserve");
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
		//-- _localrmname
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.types.LocalrmnameType.class, "_localrmname",
				"localrmname", org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				return target.getLocalrmname();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					target
							.setLocalrmname((grms_schema.types.LocalrmnameType) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(new org.exolab.castor.xml.handlers.EnumFieldHandler(
				grms_schema.types.LocalrmnameType.class, handler));
		desc.setImmutable(true);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _localrmname
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _memory
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.Integer.TYPE, "_memory", "memory",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				if (!target.hasMemory())
					return null;
				return new java.lang.Integer(target.getMemory());
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					// if null, use delete method for optional primitives
					if (value == null) {
						target.deleteMemory();
						return;
					}
					target.setMemory(((java.lang.Integer) value).intValue());
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _memory
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			IntegerValidator typeValidator = new IntegerValidator();
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
		//-- _cpucount
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.Integer.TYPE, "_cpucount", "cpucount",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				if (!target.hasCpucount())
					return null;
				return new java.lang.Integer(target.getCpucount());
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					// if null, use delete method for optional primitives
					if (value == null) {
						target.deleteCpucount();
						return;
					}
					target.setCpucount(((java.lang.Integer) value).intValue());
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _cpucount
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			IntegerValidator typeValidator = new IntegerValidator();
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
		//-- _cpuspeed
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.Integer.TYPE, "_cpuspeed", "cpuspeed",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				if (!target.hasCpuspeed())
					return null;
				return new java.lang.Integer(target.getCpuspeed());
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					// if null, use delete method for optional primitives
					if (value == null) {
						target.deleteCpuspeed();
						return;
					}
					target.setCpuspeed(((java.lang.Integer) value).intValue());
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _cpuspeed
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			IntegerValidator typeValidator = new IntegerValidator();
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
		//-- _maxtime
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.Integer.TYPE, "_maxtime", "maxtime",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				if (!target.hasMaxtime())
					return null;
				return new java.lang.Integer(target.getMaxtime());
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					// if null, use delete method for optional primitives
					if (value == null) {
						target.deleteMaxtime();
						return;
					}
					target.setMaxtime(((java.lang.Integer) value).intValue());
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _maxtime
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			IntegerValidator typeValidator = new IntegerValidator();
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
		//-- _maxwalltime
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.Integer.TYPE, "_maxwalltime", "maxwalltime",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				if (!target.hasMaxwalltime())
					return null;
				return new java.lang.Integer(target.getMaxwalltime());
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					// if null, use delete method for optional primitives
					if (value == null) {
						target.deleteMaxwalltime();
						return;
					}
					target.setMaxwalltime(((java.lang.Integer) value)
							.intValue());
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _maxwalltime
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			IntegerValidator typeValidator = new IntegerValidator();
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
		//-- _maxcputime
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.Integer.TYPE, "_maxcputime", "maxcputime",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				if (!target.hasMaxcputime())
					return null;
				return new java.lang.Integer(target.getMaxcputime());
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					// if null, use delete method for optional primitives
					if (value == null) {
						target.deleteMaxcputime();
						return;
					}
					target
							.setMaxcputime(((java.lang.Integer) value)
									.intValue());
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _maxcputime
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			IntegerValidator typeValidator = new IntegerValidator();
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
		//-- _bandwidth
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.Bandwidth.class, "_bandwidth", "bandwidth",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				return target.getBandwidth();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					target.setBandwidth((grms_schema.Bandwidth) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.Bandwidth();
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _bandwidth
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _latency
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.Latency.class, "_latency", "latency",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				return target.getLatency();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					target.setLatency((grms_schema.Latency) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.Latency();
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _latency
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _capacity
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				grms_schema.Capacity.class, "_capacity", "capacity",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Resource target = (Resource) object;
				return target.getCapacity();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Resource target = (Resource) object;
					target.setCapacity((grms_schema.Capacity) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new grms_schema.Capacity();
			}
		});
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _capacity
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
	} //-- grms_schema.ResourceDescriptor()

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
		return grms_schema.Resource.class;
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