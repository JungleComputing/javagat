/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Environment.java,v 1.9 2006/01/23 11:05:53 rob Exp $
 */
package grms_schema;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/
import java.util.Vector;

/**
 * Environment variables
 *
 * @version $Revision: 1.9 $ $Date: 2006/01/23 11:05:53 $
 */
public class Environment implements java.io.Serializable {
    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _variableList
     */
    private java.util.Vector _variableList;

    //----------------/
    //- Constructors -/
    //----------------/
    public Environment() {
        super();
        _variableList = new Vector();
    } //-- grms_schema.Environment()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addVariable
     *
     *
     *
     * @param vVariable
     */
    public void addVariable(grms_schema.Variable vVariable)
            throws java.lang.IndexOutOfBoundsException {
        _variableList.addElement(vVariable);
    } //-- void addVariable(grms_schema.Variable) 

    /**
     * Method addVariable
     *
     *
     *
     * @param index
     * @param vVariable
     */
    public void addVariable(int index, grms_schema.Variable vVariable)
            throws java.lang.IndexOutOfBoundsException {
        _variableList.insertElementAt(vVariable, index);
    } //-- void addVariable(int, grms_schema.Variable) 

    /**
     * Method enumerateVariable
     *
     *
     *
     * @return Enumeration
     */
    public java.util.Enumeration enumerateVariable() {
        return _variableList.elements();
    } //-- java.util.Enumeration enumerateVariable() 

    /**
     * Method getVariable
     *
     *
     *
     * @param index
     * @return Variable
     */
    public grms_schema.Variable getVariable(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _variableList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (grms_schema.Variable) _variableList.elementAt(index);
    } //-- grms_schema.Variable getVariable(int) 

    /**
     * Method getVariable
     *
     *
     *
     * @return Variable
     */
    public grms_schema.Variable[] getVariable() {
        int size = _variableList.size();
        grms_schema.Variable[] mArray = new grms_schema.Variable[size];

        for (int index = 0; index < size; index++) {
            mArray[index] = (grms_schema.Variable) _variableList
                .elementAt(index);
        }

        return mArray;
    } //-- grms_schema.Variable[] getVariable() 

    /**
     * Method getVariableCount
     *
     *
     *
     * @return int
     */
    public int getVariableCount() {
        return _variableList.size();
    } //-- int getVariableCount() 

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
     * Method removeAllVariable
     *
     */
    public void removeAllVariable() {
        _variableList.removeAllElements();
    } //-- void removeAllVariable() 

    /**
     * Method removeVariable
     *
     *
     *
     * @param index
     * @return Variable
     */
    public grms_schema.Variable removeVariable(int index) {
        java.lang.Object obj = _variableList.elementAt(index);
        _variableList.removeElementAt(index);

        return (grms_schema.Variable) obj;
    } //-- grms_schema.Variable removeVariable(int) 

    /**
     * Method setVariable
     *
     *
     *
     * @param index
     * @param vVariable
     */
    public void setVariable(int index, grms_schema.Variable vVariable)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _variableList.size())) {
            throw new IndexOutOfBoundsException();
        }

        _variableList.setElementAt(vVariable, index);
    } //-- void setVariable(int, grms_schema.Variable) 

    /**
     * Method setVariable
     *
     *
     *
     * @param variableArray
     */
    public void setVariable(grms_schema.Variable[] variableArray) {
        //-- copy array
        _variableList.removeAllElements();

        for (int i = 0; i < variableArray.length; i++) {
            _variableList.addElement(variableArray[i]);
        }
    } //-- void setVariable(grms_schema.Variable) 

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
        return (grms_schema.Environment) Unmarshaller.unmarshal(
            grms_schema.Environment.class, reader);
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
