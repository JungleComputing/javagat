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
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class ApplicationType.
 *
 * @version $Revision: 1.4 $ $Date$
 */
public class ApplicationType implements java.io.Serializable {
    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
     */
    private java.lang.String _content = "";

    /**
     * Field _version
     */
    private java.lang.String _version;

    /**
     * Field _instanceCount
     */
    private int _instanceCount;

    /**
     * keeps track of state for field: _instanceCount
     */
    private boolean _has_instanceCount;

    //----------------/
    //- Constructors -/
    //----------------/
    public ApplicationType() {
        super();
        setContent("");
    } //-- grms_schema.ApplicationType()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method deleteInstanceCount
     *
     */
    public void deleteInstanceCount() {
        this._has_instanceCount = false;
    } //-- void deleteInstanceCount() 

    /**
     * Returns the value of field 'content'. The field 'content'
     * has the following description: internal content storage
     *
     * @return String
     * @return the value of field 'content'.
     */
    public java.lang.String getContent() {
        return this._content;
    } //-- java.lang.String getContent() 

    /**
     * Returns the value of field 'instanceCount'.
     *
     * @return int
     * @return the value of field 'instanceCount'.
     */
    public int getInstanceCount() {
        return this._instanceCount;
    } //-- int getInstanceCount() 

    /**
     * Returns the value of field 'version'.
     *
     * @return String
     * @return the value of field 'version'.
     */
    public java.lang.String getVersion() {
        return this._version;
    } //-- java.lang.String getVersion() 

    /**
     * Method hasInstanceCount
     *
     *
     *
     * @return boolean
     */
    public boolean hasInstanceCount() {
        return this._has_instanceCount;
    } //-- boolean hasInstanceCount() 

    /**
     * Method isValid
     *
     *
     *
     * @return boolean
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
     *
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
     *
     *
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
            throws java.io.IOException, org.exolab.castor.xml.MarshalException,
            org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'content'. The field 'content' has
     * the following description: internal content storage
     *
     * @param content the value of field 'content'.
     */
    public void setContent(java.lang.String content) {
        this._content = content;
    } //-- void setContent(java.lang.String) 

    /**
     * Sets the value of field 'instanceCount'.
     *
     * @param instanceCount the value of field 'instanceCount'.
     */
    public void setInstanceCount(int instanceCount) {
        this._instanceCount = instanceCount;
        this._has_instanceCount = true;
    } //-- void setInstanceCount(int) 

    /**
     * Sets the value of field 'version'.
     *
     * @param version the value of field 'version'.
     */
    public void setVersion(java.lang.String version) {
        this._version = version;
    } //-- void setVersion(java.lang.String) 

    /**
     * Method unmarshal
     *
     *
     *
     * @param reader
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException,
            org.exolab.castor.xml.ValidationException {
        return (grms_schema.ApplicationType) Unmarshaller.unmarshal(
            grms_schema.ApplicationType.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     *
     */
    public void validate() throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 
}
