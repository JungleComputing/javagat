/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: ExecutionTimeTypeDescriptor.java,v 1.2 2005/10/07 11:05:55 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.validators.DurationValidator;

/**
 * Class ExecutionTimeTypeDescriptor.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/10/07 11:05:55 $
 */
public class ExecutionTimeTypeDescriptor extends
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

    public ExecutionTimeTypeDescriptor() {
        super();
        xmlName = "executionTimeType";

        //-- set grouping compositor
        setCompositorAsSequence();
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
        org.exolab.castor.xml.XMLFieldHandler handler = null;
        org.exolab.castor.xml.FieldValidator fieldValidator = null;
        //-- initialize attribute descriptors

        //-- initialize element descriptors

        //-- _timeSlot
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            grms_schema.TimeSlot.class, "_timeSlot", "timeSlot",
            org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                throws IllegalStateException {
                ExecutionTimeType target = (ExecutionTimeType) object;
                return target.getTimeSlot();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                throws IllegalStateException, IllegalArgumentException {
                try {
                    ExecutionTimeType target = (ExecutionTimeType) object;
                    target.setTimeSlot((grms_schema.TimeSlot) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new grms_schema.TimeSlot();
            }
        });
        desc.setHandler(handler);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);

        //-- validation code for: _timeSlot
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _execDuration
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            org.exolab.castor.types.Duration.class, "_execDuration",
            "execDuration", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                throws IllegalStateException {
                ExecutionTimeType target = (ExecutionTimeType) object;
                return target.getExecDuration();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                throws IllegalStateException, IllegalArgumentException {
                try {
                    ExecutionTimeType target = (ExecutionTimeType) object;
                    target
                        .setExecDuration((org.exolab.castor.types.Duration) value);
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

        //-- validation code for: _execDuration
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        { //-- local scope
            DurationValidator typeValidator = new DurationValidator();
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);
        //-- _timePeriod
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            grms_schema.TimePeriod.class, "_timePeriod", "timePeriod",
            org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                throws IllegalStateException {
                ExecutionTimeType target = (ExecutionTimeType) object;
                return target.getTimePeriod();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                throws IllegalStateException, IllegalArgumentException {
                try {
                    ExecutionTimeType target = (ExecutionTimeType) object;
                    target.setTimePeriod((grms_schema.TimePeriod) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new grms_schema.TimePeriod();
            }
        });
        desc.setHandler(handler);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);

        //-- validation code for: _timePeriod
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
    } //-- grms_schema.ExecutionTimeTypeDescriptor()

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
        return grms_schema.ExecutionTimeType.class;
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
