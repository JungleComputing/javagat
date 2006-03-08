/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: ReservationDescriptor.java,v 1.1 2004/06/21 09:05:33 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.validators.DoubleValidator;

/**
 * Class ReservationDescriptor.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/06/21 09:05:33 $
 */
public class ReservationDescriptor extends
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

	public ReservationDescriptor() {
		super();
		xmlName = "reservation";
		org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
		org.exolab.castor.xml.XMLFieldHandler handler = null;
		org.exolab.castor.xml.FieldValidator fieldValidator = null;
		//-- initialize attribute descriptors

		//-- initialize element descriptors

		//-- _deadline
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.util.Date.class, "_deadline", "deadline",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Reservation target = (Reservation) object;
				return target.getDeadline();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Reservation target = (Reservation) object;
					target.setDeadline((java.util.Date) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new java.util.Date();
			}
		});
		desc.setHandler(new org.exolab.castor.xml.handlers.DateFieldHandler(
				handler));
		desc.setImmutable(true);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _deadline
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _estime
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.util.Date.class, "_estime", "estime",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Reservation target = (Reservation) object;
				return target.getEstime();
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Reservation target = (Reservation) object;
					target.setEstime((java.util.Date) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new java.util.Date();
			}
		});
		desc.setHandler(new org.exolab.castor.xml.handlers.DateFieldHandler(
				handler));
		desc.setImmutable(true);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);

		//-- validation code for: _estime
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _datasize
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
				java.lang.Double.TYPE, "_datasize", "datasize",
				org.exolab.castor.xml.NodeType.Element);
		handler = (new org.exolab.castor.xml.XMLFieldHandler() {
			public java.lang.Object getValue(java.lang.Object object)
					throws IllegalStateException {
				Reservation target = (Reservation) object;
				if (!target.hasDatasize())
					return null;
				return new java.lang.Double(target.getDatasize());
			}

			public void setValue(java.lang.Object object, java.lang.Object value)
					throws IllegalStateException, IllegalArgumentException {
				try {
					Reservation target = (Reservation) object;
					// if null, use delete method for optional primitives
					if (value == null) {
						target.deleteDatasize();
						return;
					}
					target
							.setDatasize(((java.lang.Double) value)
									.doubleValue());
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

		//-- validation code for: _datasize
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			DoubleValidator typeValidator = new DoubleValidator();
			fieldValidator.setValidator(typeValidator);
		}
		desc.setValidator(fieldValidator);
	} //-- grms_schema.ReservationDescriptor()

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
		return grms_schema.Reservation.class;
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