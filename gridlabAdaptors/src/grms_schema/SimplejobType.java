/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id$
 */
package grms_schema;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/
import java.util.Vector;

/**
 * Description of Simple Job
 *
 * @version $Revision: 1.9 $ $Date$
 */
public class SimplejobType implements java.io.Serializable {
    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Resource requirement section
     */
    private java.util.Vector _resourceList;

    /**
     * Application description section
     */
    private grms_schema.Executable _executable;

    /**
     * Field _executionTime
     */
    private grms_schema.ExecutionTime _executionTime;

    //----------------/
    //- Constructors -/
    //----------------/
    public SimplejobType() {
        super();
        _resourceList = new Vector();
    } //-- grms_schema.SimplejobType()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addResource
     *
     *
     *
     * @param vResource
     */
    public void addResource(grms_schema.Resource vResource)
            throws java.lang.IndexOutOfBoundsException {
        _resourceList.addElement(vResource);
    } //-- void addResource(grms_schema.Resource) 

    /**
     * Method addResource
     *
     *
     *
     * @param index
     * @param vResource
     */
    public void addResource(int index, grms_schema.Resource vResource)
            throws java.lang.IndexOutOfBoundsException {
        _resourceList.insertElementAt(vResource, index);
    } //-- void addResource(int, grms_schema.Resource) 

    /**
     * Method enumerateResource
     *
     *
     *
     * @return Enumeration
     */
    public java.util.Enumeration enumerateResource() {
        return _resourceList.elements();
    } //-- java.util.Enumeration enumerateResource() 

    /**
     * Returns the value of field 'executable'. The field
     * 'executable' has the following description: Application
     * description section
     *
     * @return Executable
     * @return the value of field 'executable'.
     */
    public grms_schema.Executable getExecutable() {
        return this._executable;
    } //-- grms_schema.Executable getExecutable() 

    /**
     * Returns the value of field 'executionTime'.
     *
     * @return ExecutionTime
     * @return the value of field 'executionTime'.
     */
    public grms_schema.ExecutionTime getExecutionTime() {
        return this._executionTime;
    } //-- grms_schema.ExecutionTime getExecutionTime() 

    /**
     * Method getResource
     *
     *
     *
     * @param index
     * @return Resource
     */
    public grms_schema.Resource getResource(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (grms_schema.Resource) _resourceList.elementAt(index);
    } //-- grms_schema.Resource getResource(int) 

    /**
     * Method getResource
     *
     *
     *
     * @return Resource
     */
    public grms_schema.Resource[] getResource() {
        int size = _resourceList.size();
        grms_schema.Resource[] mArray = new grms_schema.Resource[size];

        for (int index = 0; index < size; index++) {
            mArray[index] = (grms_schema.Resource) _resourceList
                .elementAt(index);
        }

        return mArray;
    } //-- grms_schema.Resource[] getResource() 

    /**
     * Method getResourceCount
     *
     *
     *
     * @return int
     */
    public int getResourceCount() {
        return _resourceList.size();
    } //-- int getResourceCount() 

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
     * Method removeAllResource
     *
     */
    public void removeAllResource() {
        _resourceList.removeAllElements();
    } //-- void removeAllResource() 

    /**
     * Method removeResource
     *
     *
     *
     * @param index
     * @return Resource
     */
    public grms_schema.Resource removeResource(int index) {
        java.lang.Object obj = _resourceList.elementAt(index);
        _resourceList.removeElementAt(index);

        return (grms_schema.Resource) obj;
    } //-- grms_schema.Resource removeResource(int) 

    /**
     * Sets the value of field 'executable'. The field 'executable'
     * has the following description: Application description
     * section
     *
     * @param executable the value of field 'executable'.
     */
    public void setExecutable(grms_schema.Executable executable) {
        this._executable = executable;
    } //-- void setExecutable(grms_schema.Executable) 

    /**
     * Sets the value of field 'executionTime'.
     *
     * @param executionTime the value of field 'executionTime'.
     */
    public void setExecutionTime(grms_schema.ExecutionTime executionTime) {
        this._executionTime = executionTime;
    } //-- void setExecutionTime(grms_schema.ExecutionTime) 

    /**
     * Method setResource
     *
     *
     *
     * @param index
     * @param vResource
     */
    public void setResource(int index, grms_schema.Resource vResource)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _resourceList.size())) {
            throw new IndexOutOfBoundsException();
        }

        _resourceList.setElementAt(vResource, index);
    } //-- void setResource(int, grms_schema.Resource) 

    /**
     * Method setResource
     *
     *
     *
     * @param resourceArray
     */
    public void setResource(grms_schema.Resource[] resourceArray) {
        //-- copy array
        _resourceList.removeAllElements();

        for (int i = 0; i < resourceArray.length; i++) {
            _resourceList.addElement(resourceArray[i]);
        }
    } //-- void setResource(grms_schema.Resource) 

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
        return (grms_schema.SimplejobType) Unmarshaller.unmarshal(
            grms_schema.SimplejobType.class, reader);
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
