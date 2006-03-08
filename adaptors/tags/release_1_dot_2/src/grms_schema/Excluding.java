/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Excluding.java,v 1.2 2005/10/07 11:05:55 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Excluding.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/10/07 11:05:55 $
 */
public class Excluding implements java.io.Serializable {

    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _items
     */
    private java.util.Vector _items;

    //----------------/
    //- Constructors -/
    //----------------/

    public Excluding() {
        super();
        _items = new Vector();
    } //-- grms_schema.Excluding()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addExcludingItem
     * 
     * 
     * 
     * @param vExcludingItem
     */
    public void addExcludingItem(grms_schema.ExcludingItem vExcludingItem)
        throws java.lang.IndexOutOfBoundsException {
        _items.addElement(vExcludingItem);
    } //-- void addExcludingItem(grms_schema.ExcludingItem) 

    /**
     * Method addExcludingItem
     * 
     * 
     * 
     * @param index
     * @param vExcludingItem
     */
    public void addExcludingItem(int index,
        grms_schema.ExcludingItem vExcludingItem)
        throws java.lang.IndexOutOfBoundsException {
        _items.insertElementAt(vExcludingItem, index);
    } //-- void addExcludingItem(int, grms_schema.ExcludingItem) 

    /**
     * Method enumerateExcludingItem
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateExcludingItem() {
        return _items.elements();
    } //-- java.util.Enumeration enumerateExcludingItem() 

    /**
     * Method getExcludingItem
     * 
     * 
     * 
     * @param index
     * @return ExcludingItem
     */
    public grms_schema.ExcludingItem getExcludingItem(int index)
        throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (grms_schema.ExcludingItem) _items.elementAt(index);
    } //-- grms_schema.ExcludingItem getExcludingItem(int) 

    /**
     * Method getExcludingItem
     * 
     * 
     * 
     * @return ExcludingItem
     */
    public grms_schema.ExcludingItem[] getExcludingItem() {
        int size = _items.size();
        grms_schema.ExcludingItem[] mArray = new grms_schema.ExcludingItem[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (grms_schema.ExcludingItem) _items.elementAt(index);
        }
        return mArray;
    } //-- grms_schema.ExcludingItem[] getExcludingItem() 

    /**
     * Method getExcludingItemCount
     * 
     * 
     * 
     * @return int
     */
    public int getExcludingItemCount() {
        return _items.size();
    } //-- int getExcludingItemCount() 

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
     * Method removeAllExcludingItem
     * 
     */
    public void removeAllExcludingItem() {
        _items.removeAllElements();
    } //-- void removeAllExcludingItem() 

    /**
     * Method removeExcludingItem
     * 
     * 
     * 
     * @param index
     * @return ExcludingItem
     */
    public grms_schema.ExcludingItem removeExcludingItem(int index) {
        java.lang.Object obj = _items.elementAt(index);
        _items.removeElementAt(index);
        return (grms_schema.ExcludingItem) obj;
    } //-- grms_schema.ExcludingItem removeExcludingItem(int) 

    /**
     * Method setExcludingItem
     * 
     * 
     * 
     * @param index
     * @param vExcludingItem
     */
    public void setExcludingItem(int index,
        grms_schema.ExcludingItem vExcludingItem)
        throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        _items.setElementAt(vExcludingItem, index);
    } //-- void setExcludingItem(int, grms_schema.ExcludingItem) 

    /**
     * Method setExcludingItem
     * 
     * 
     * 
     * @param excludingItemArray
     */
    public void setExcludingItem(grms_schema.ExcludingItem[] excludingItemArray) {
        //-- copy array
        _items.removeAllElements();
        for (int i = 0; i < excludingItemArray.length; i++) {
            _items.addElement(excludingItemArray[i]);
        }
    } //-- void setExcludingItem(grms_schema.ExcludingItem) 

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
        return (grms_schema.Excluding) Unmarshaller.unmarshal(
            grms_schema.Excluding.class, reader);
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
