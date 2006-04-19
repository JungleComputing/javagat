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
 * Base element for file describtion
 *
 * @version $Revision: 1.9 $ $Date$
 */
public class BaseFile implements java.io.Serializable {
    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * File from Replica Management system
     */
    private grms_schema.Collectionfile _collectionfile;

    /**
     * Url location of file
     */
    private java.lang.String _url;

    //----------------/
    //- Constructors -/
    //----------------/
    public BaseFile() {
        super();
    } //-- grms_schema.BaseFile()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'collectionfile'. The field
     * 'collectionfile' has the following description: File from
     * Replica Management system
     *
     * @return Collectionfile
     * @return the value of field 'collectionfile'.
     */
    public grms_schema.Collectionfile getCollectionfile() {
        return this._collectionfile;
    } //-- grms_schema.Collectionfile getCollectionfile() 

    /**
     * Returns the value of field 'url'. The field 'url' has the
     * following description: Url location of file
     *
     * @return String
     * @return the value of field 'url'.
     */
    public java.lang.String getUrl() {
        return this._url;
    } //-- java.lang.String getUrl() 

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
     * Sets the value of field 'collectionfile'. The field
     * 'collectionfile' has the following description: File from
     * Replica Management system
     *
     * @param collectionfile the value of field 'collectionfile'.
     */
    public void setCollectionfile(grms_schema.Collectionfile collectionfile) {
        this._collectionfile = collectionfile;
    } //-- void setCollectionfile(grms_schema.Collectionfile) 

    /**
     * Sets the value of field 'url'. The field 'url' has the
     * following description: Url location of file
     *
     * @param url the value of field 'url'.
     */
    public void setUrl(java.lang.String url) {
        this._url = url;
    } //-- void setUrl(java.lang.String) 

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
        return (grms_schema.BaseFile) Unmarshaller.unmarshal(
            grms_schema.BaseFile.class, reader);
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
