/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id$
 */
package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/
import org.exolab.castor.xml.validators.DurationValidator;

/**
 * Class TimePeriodChoiceDescriptor.
 *
 * @version $Revision: 1.4 $ $Date$
 */
public class TimePeriodChoiceDescriptor extends
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
    public TimePeriodChoiceDescriptor() {
        super();

        //-- set grouping compositor
        setCompositorAsChoice();

        org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
        org.exolab.castor.xml.XMLFieldHandler handler = null;
        org.exolab.castor.xml.FieldValidator fieldValidator = null;

        //-- initialize attribute descriptors
        //-- initialize element descriptors
        //-- _periodEnd
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            java.util.Date.class, "_periodEnd", "periodEnd",
            org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                TimePeriodChoice target = (TimePeriodChoice) object;

                return target.getPeriodEnd();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    TimePeriodChoice target = (TimePeriodChoice) object;
                    target.setPeriodEnd((java.util.Date) value);
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
        desc.setRequired(true);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);

        //-- validation code for: _periodEnd
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        desc.setValidator(fieldValidator);

        //-- _periodDuration
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            org.exolab.castor.types.Duration.class, "_periodDuration",
            "periodDuration", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                TimePeriodChoice target = (TimePeriodChoice) object;

                return target.getPeriodDuration();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    TimePeriodChoice target = (TimePeriodChoice) object;
                    target
                        .setPeriodDuration((org.exolab.castor.types.Duration) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.exolab.castor.types.Duration();
            }
        });
        desc.setHandler(handler);
        desc.setRequired(true);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);

        //-- validation code for: _periodDuration
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);

        { //-- local scope

            DurationValidator typeValidator = new DurationValidator();
            fieldValidator.setValidator(typeValidator);
        }

        desc.setValidator(fieldValidator);
    } //-- grms_schema.TimePeriodChoiceDescriptor()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method getAccessMode
     *
     *
     *
     * @return AccessMode
     */
    public org.exolab.castor.mapping.AccessMode getAccessMode() {
        return null;
    } //-- org.exolab.castor.mapping.AccessMode getAccessMode() 

    /**
     * Method getExtends
     *
     *
     *
     * @return ClassDescriptor
     */
    public org.exolab.castor.mapping.ClassDescriptor getExtends() {
        return null;
    } //-- org.exolab.castor.mapping.ClassDescriptor getExtends() 

    /**
     * Method getIdentity
     *
     *
     *
     * @return FieldDescriptor
     */
    public org.exolab.castor.mapping.FieldDescriptor getIdentity() {
        return identity;
    } //-- org.exolab.castor.mapping.FieldDescriptor getIdentity() 

    /**
     * Method getJavaClass
     *
     *
     *
     * @return Class
     */
    public java.lang.Class getJavaClass() {
        return grms_schema.TimePeriodChoice.class;
    } //-- java.lang.Class getJavaClass() 

    /**
     * Method getNameSpacePrefix
     *
     *
     *
     * @return String
     */
    public java.lang.String getNameSpacePrefix() {
        return nsPrefix;
    } //-- java.lang.String getNameSpacePrefix() 

    /**
     * Method getNameSpaceURI
     *
     *
     *
     * @return String
     */
    public java.lang.String getNameSpaceURI() {
        return nsURI;
    } //-- java.lang.String getNameSpaceURI() 

    /**
     * Method getValidator
     *
     *
     *
     * @return TypeValidator
     */
    public org.exolab.castor.xml.TypeValidator getValidator() {
        return this;
    } //-- org.exolab.castor.xml.TypeValidator getValidator() 

    /**
     * Method getXMLName
     *
     *
     *
     * @return String
     */
    public java.lang.String getXMLName() {
        return xmlName;
    } //-- java.lang.String getXMLName() 
}
