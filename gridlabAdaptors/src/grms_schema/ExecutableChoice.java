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
 * Class ExecutableChoice.
 *
 * @version $Revision: 1.4 $ $Date$
 */
public class ExecutableChoice implements java.io.Serializable {
    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _application
     */
    private grms_schema.Application _application;

    /**
     * Location of executable of application
     */
    private grms_schema.File _file;

    //----------------/
    //- Constructors -/
    //----------------/
    public ExecutableChoice() {
        super();
    } //-- grms_schema.ExecutableChoice()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'application'.
     *
     * @return Application
     * @return the value of field 'application'.
     */
    public grms_schema.Application getApplication() {
        return this._application;
    } //-- grms_schema.Application getApplication() 

    /**
     * Returns the value of field 'file'. The field 'file' has the
     * following description: Location of executable of application
     *
     * @return File
     * @return the value of field 'file'.
     */
    public grms_schema.File getFile() {
        return this._file;
    } //-- grms_schema.File getFile() 

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
     * Sets the value of field 'application'.
     *
     * @param application the value of field 'application'.
     */
    public void setApplication(grms_schema.Application application) {
        this._application = application;
    } //-- void setApplication(grms_schema.Application) 

    /**
     * Sets the value of field 'file'. The field 'file' has the
     * following description: Location of executable of application
     *
     * @param file the value of field 'file'.
     */
    public void setFile(grms_schema.File file) {
        this._file = file;
    } //-- void setFile(grms_schema.File) 

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
        return (grms_schema.ExecutableChoice) Unmarshaller.unmarshal(
            grms_schema.ExecutableChoice.class, reader);
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
