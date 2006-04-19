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
 * Class Capacity.
 *
 * @version $Revision: 1.9 $ $Date$
 */
public class Capacity implements java.io.Serializable {
    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
     */
    private int _content;

    /**
     * keeps track of state for field: _content
     */
    private boolean _has_content;

    /**
     * Field _hostname
     */
    private java.lang.String _hostname;

    //----------------/
    //- Constructors -/
    //----------------/
    public Capacity() {
        super();
    } //-- grms_schema.Capacity()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method deleteContent
     *
     */
    public void deleteContent() {
        this._has_content = false;
    } //-- void deleteContent() 

    /**
     * Returns the value of field 'content'. The field 'content'
     * has the following description: internal content storage
     *
     * @return int
     * @return the value of field 'content'.
     */
    public int getContent() {
        return this._content;
    } //-- int getContent() 

    /**
     * Returns the value of field 'hostname'.
     *
     * @return String
     * @return the value of field 'hostname'.
     */
    public java.lang.String getHostname() {
        return this._hostname;
    } //-- java.lang.String getHostname() 

    /**
     * Method hasContent
     *
     *
     *
     * @return boolean
     */
    public boolean hasContent() {
        return this._has_content;
    } //-- boolean hasContent() 

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
    public void setContent(int content) {
        this._content = content;
        this._has_content = true;
    } //-- void setContent(int) 

    /**
     * Sets the value of field 'hostname'.
     *
     * @param hostname the value of field 'hostname'.
     */
    public void setHostname(java.lang.String hostname) {
        this._hostname = hostname;
    } //-- void setHostname(java.lang.String) 

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
        return (grms_schema.Capacity) Unmarshaller.unmarshal(
            grms_schema.Capacity.class, reader);
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
