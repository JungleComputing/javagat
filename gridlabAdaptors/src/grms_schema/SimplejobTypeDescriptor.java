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

/**
 * Class SimplejobTypeDescriptor.
 *
 * @version $Revision: 1.10 $ $Date$
 */
public class SimplejobTypeDescriptor extends
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
    public SimplejobTypeDescriptor() {
        super();
        xmlName = "simplejobType";

        //-- set grouping compositor
        setCompositorAsSequence();

        org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
        org.exolab.castor.xml.XMLFieldHandler handler = null;
        org.exolab.castor.xml.FieldValidator fieldValidator = null;

        //-- initialize attribute descriptors
        //-- initialize element descriptors
        //-- _resourceList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            grms_schema.Resource.class, "_resourceList", "resource",
            org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                SimplejobType target = (SimplejobType) object;

                return target.getResource();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    SimplejobType target = (SimplejobType) object;
                    target.addResource((grms_schema.Resource) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new grms_schema.Resource();
            }
        });
        desc.setHandler(handler);
        desc.setMultivalued(true);
        addFieldDescriptor(desc);

        //-- validation code for: _resourceList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        desc.setValidator(fieldValidator);

        //-- _executable
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            grms_schema.Executable.class, "_executable", "executable",
            org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                SimplejobType target = (SimplejobType) object;

                return target.getExecutable();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    SimplejobType target = (SimplejobType) object;
                    target.setExecutable((grms_schema.Executable) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new grms_schema.Executable();
            }
        });
        desc.setHandler(handler);
        desc.setRequired(true);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);

        //-- validation code for: _executable
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        desc.setValidator(fieldValidator);

        //-- _executionTime
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(
            grms_schema.ExecutionTime.class, "_executionTime", "executionTime",
            org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                SimplejobType target = (SimplejobType) object;

                return target.getExecutionTime();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    SimplejobType target = (SimplejobType) object;
                    target.setExecutionTime((grms_schema.ExecutionTime) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new grms_schema.ExecutionTime();
            }
        });
        desc.setHandler(handler);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);

        //-- validation code for: _executionTime
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        desc.setValidator(fieldValidator);
    } //-- grms_schema.SimplejobTypeDescriptor()

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
        return grms_schema.SimplejobType.class;
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
