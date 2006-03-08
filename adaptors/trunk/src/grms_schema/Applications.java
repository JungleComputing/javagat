/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Applications.java,v 1.4 2006/01/23 11:05:53 rob Exp $
 */
package grms_schema;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/
import java.util.Vector;

/**
 * Class Applications.
 *
 * @version $Revision: 1.4 $ $Date: 2006/01/23 11:05:53 $
 */
public class Applications implements java.io.Serializable {
    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _applicationList
     */
    private java.util.Vector _applicationList;

    //----------------/
    //- Constructors -/
    //----------------/
    public Applications() {
        super();
        _applicationList = new Vector();
    } //-- grms_schema.Applications()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addApplication
     *
     *
     *
     * @param vApplication
     */
    public void addApplication(grms_schema.Application vApplication)
            throws java.lang.IndexOutOfBoundsException {
        _applicationList.addElement(vApplication);
    } //-- void addApplication(grms_schema.Application) 

    /**
     * Method addApplication
     *
     *
     *
     * @param index
     * @param vApplication
     */
    public void addApplication(int index, grms_schema.Application vApplication)
            throws java.lang.IndexOutOfBoundsException {
        _applicationList.insertElementAt(vApplication, index);
    } //-- void addApplication(int, grms_schema.Application) 

    /**
     * Method enumerateApplication
     *
     *
     *
     * @return Enumeration
     */
    public java.util.Enumeration enumerateApplication() {
        return _applicationList.elements();
    } //-- java.util.Enumeration enumerateApplication() 

    /**
     * Method getApplication
     *
     *
     *
     * @param index
     * @return Application
     */
    public grms_schema.Application getApplication(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _applicationList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (grms_schema.Application) _applicationList.elementAt(index);
    } //-- grms_schema.Application getApplication(int) 

    /**
     * Method getApplication
     *
     *
     *
     * @return Application
     */
    public grms_schema.Application[] getApplication() {
        int size = _applicationList.size();
        grms_schema.Application[] mArray = new grms_schema.Application[size];

        for (int index = 0; index < size; index++) {
            mArray[index] = (grms_schema.Application) _applicationList
                .elementAt(index);
        }

        return mArray;
    } //-- grms_schema.Application[] getApplication() 

    /**
     * Method getApplicationCount
     *
     *
     *
     * @return int
     */
    public int getApplicationCount() {
        return _applicationList.size();
    } //-- int getApplicationCount() 

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
     * Method removeAllApplication
     *
     */
    public void removeAllApplication() {
        _applicationList.removeAllElements();
    } //-- void removeAllApplication() 

    /**
     * Method removeApplication
     *
     *
     *
     * @param index
     * @return Application
     */
    public grms_schema.Application removeApplication(int index) {
        java.lang.Object obj = _applicationList.elementAt(index);
        _applicationList.removeElementAt(index);

        return (grms_schema.Application) obj;
    } //-- grms_schema.Application removeApplication(int) 

    /**
     * Method setApplication
     *
     *
     *
     * @param index
     * @param vApplication
     */
    public void setApplication(int index, grms_schema.Application vApplication)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _applicationList.size())) {
            throw new IndexOutOfBoundsException();
        }

        _applicationList.setElementAt(vApplication, index);
    } //-- void setApplication(int, grms_schema.Application) 

    /**
     * Method setApplication
     *
     *
     *
     * @param applicationArray
     */
    public void setApplication(grms_schema.Application[] applicationArray) {
        //-- copy array
        _applicationList.removeAllElements();

        for (int i = 0; i < applicationArray.length; i++) {
            _applicationList.addElement(applicationArray[i]);
        }
    } //-- void setApplication(grms_schema.Application) 

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
        return (grms_schema.Applications) Unmarshaller.unmarshal(
            grms_schema.Applications.class, reader);
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
