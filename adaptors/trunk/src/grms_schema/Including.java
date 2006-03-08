/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Including.java,v 1.4 2006/01/23 11:05:53 rob Exp $
 */
package grms_schema;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/
import java.util.Vector;

/**
 * Class Including.
 *
 * @version $Revision: 1.4 $ $Date: 2006/01/23 11:05:53 $
 */
public class Including implements java.io.Serializable {
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
    public Including() {
        super();
        _items = new Vector();
    } //-- grms_schema.Including()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addIncludingItem
     *
     *
     *
     * @param vIncludingItem
     */
    public void addIncludingItem(grms_schema.IncludingItem vIncludingItem)
            throws java.lang.IndexOutOfBoundsException {
        _items.addElement(vIncludingItem);
    } //-- void addIncludingItem(grms_schema.IncludingItem) 

    /**
     * Method addIncludingItem
     *
     *
     *
     * @param index
     * @param vIncludingItem
     */
    public void addIncludingItem(int index,
            grms_schema.IncludingItem vIncludingItem)
            throws java.lang.IndexOutOfBoundsException {
        _items.insertElementAt(vIncludingItem, index);
    } //-- void addIncludingItem(int, grms_schema.IncludingItem) 

    /**
     * Method enumerateIncludingItem
     *
     *
     *
     * @return Enumeration
     */
    public java.util.Enumeration enumerateIncludingItem() {
        return _items.elements();
    } //-- java.util.Enumeration enumerateIncludingItem() 

    /**
     * Method getIncludingItem
     *
     *
     *
     * @param index
     * @return IncludingItem
     */
    public grms_schema.IncludingItem getIncludingItem(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (grms_schema.IncludingItem) _items.elementAt(index);
    } //-- grms_schema.IncludingItem getIncludingItem(int) 

    /**
     * Method getIncludingItem
     *
     *
     *
     * @return IncludingItem
     */
    public grms_schema.IncludingItem[] getIncludingItem() {
        int size = _items.size();
        grms_schema.IncludingItem[] mArray = new grms_schema.IncludingItem[size];

        for (int index = 0; index < size; index++) {
            mArray[index] = (grms_schema.IncludingItem) _items.elementAt(index);
        }

        return mArray;
    } //-- grms_schema.IncludingItem[] getIncludingItem() 

    /**
     * Method getIncludingItemCount
     *
     *
     *
     * @return int
     */
    public int getIncludingItemCount() {
        return _items.size();
    } //-- int getIncludingItemCount() 

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
     * Method removeAllIncludingItem
     *
     */
    public void removeAllIncludingItem() {
        _items.removeAllElements();
    } //-- void removeAllIncludingItem() 

    /**
     * Method removeIncludingItem
     *
     *
     *
     * @param index
     * @return IncludingItem
     */
    public grms_schema.IncludingItem removeIncludingItem(int index) {
        java.lang.Object obj = _items.elementAt(index);
        _items.removeElementAt(index);

        return (grms_schema.IncludingItem) obj;
    } //-- grms_schema.IncludingItem removeIncludingItem(int) 

    /**
     * Method setIncludingItem
     *
     *
     *
     * @param index
     * @param vIncludingItem
     */
    public void setIncludingItem(int index,
            grms_schema.IncludingItem vIncludingItem)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }

        _items.setElementAt(vIncludingItem, index);
    } //-- void setIncludingItem(int, grms_schema.IncludingItem) 

    /**
     * Method setIncludingItem
     *
     *
     *
     * @param includingItemArray
     */
    public void setIncludingItem(grms_schema.IncludingItem[] includingItemArray) {
        //-- copy array
        _items.removeAllElements();

        for (int i = 0; i < includingItemArray.length; i++) {
            _items.addElement(includingItemArray[i]);
        }
    } //-- void setIncludingItem(grms_schema.IncludingItem) 

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
        return (grms_schema.Including) Unmarshaller.unmarshal(
            grms_schema.Including.class, reader);
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
