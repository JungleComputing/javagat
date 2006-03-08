/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Arguments.java,v 1.6 2005/04/07 13:48:18 rob Exp $
 */

package grms_schema;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Arguments of execution
 * 
 * @version $Revision: 1.6 $ $Date: 2005/04/07 13:48:18 $
 */
public class Arguments implements java.io.Serializable {


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

    public Arguments() {
        super();
        _items = new Vector();
    } //-- grms_schema.Arguments()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addArgumentsItem
     * 
     * 
     * 
     * @param vArgumentsItem
     */
    public void addArgumentsItem(grms_schema.ArgumentsItem vArgumentsItem)
        throws java.lang.IndexOutOfBoundsException
    {
        _items.addElement(vArgumentsItem);
    } //-- void addArgumentsItem(grms_schema.ArgumentsItem) 

    /**
     * Method addArgumentsItem
     * 
     * 
     * 
     * @param index
     * @param vArgumentsItem
     */
    public void addArgumentsItem(int index, grms_schema.ArgumentsItem vArgumentsItem)
        throws java.lang.IndexOutOfBoundsException
    {
        _items.insertElementAt(vArgumentsItem, index);
    } //-- void addArgumentsItem(int, grms_schema.ArgumentsItem) 

    /**
     * Method enumerateArgumentsItem
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateArgumentsItem()
    {
        return _items.elements();
    } //-- java.util.Enumeration enumerateArgumentsItem() 

    /**
     * Method getArgumentsItem
     * 
     * 
     * 
     * @param index
     * @return ArgumentsItem
     */
    public grms_schema.ArgumentsItem getArgumentsItem(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (grms_schema.ArgumentsItem) _items.elementAt(index);
    } //-- grms_schema.ArgumentsItem getArgumentsItem(int) 

    /**
     * Method getArgumentsItem
     * 
     * 
     * 
     * @return ArgumentsItem
     */
    public grms_schema.ArgumentsItem[] getArgumentsItem()
    {
        int size = _items.size();
        grms_schema.ArgumentsItem[] mArray = new grms_schema.ArgumentsItem[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (grms_schema.ArgumentsItem) _items.elementAt(index);
        }
        return mArray;
    } //-- grms_schema.ArgumentsItem[] getArgumentsItem() 

    /**
     * Method getArgumentsItemCount
     * 
     * 
     * 
     * @return int
     */
    public int getArgumentsItemCount()
    {
        return _items.size();
    } //-- int getArgumentsItemCount() 

    /**
     * Method isValid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
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
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
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
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Method removeAllArgumentsItem
     * 
     */
    public void removeAllArgumentsItem()
    {
        _items.removeAllElements();
    } //-- void removeAllArgumentsItem() 

    /**
     * Method removeArgumentsItem
     * 
     * 
     * 
     * @param index
     * @return ArgumentsItem
     */
    public grms_schema.ArgumentsItem removeArgumentsItem(int index)
    {
        java.lang.Object obj = _items.elementAt(index);
        _items.removeElementAt(index);
        return (grms_schema.ArgumentsItem) obj;
    } //-- grms_schema.ArgumentsItem removeArgumentsItem(int) 

    /**
     * Method setArgumentsItem
     * 
     * 
     * 
     * @param index
     * @param vArgumentsItem
     */
    public void setArgumentsItem(int index, grms_schema.ArgumentsItem vArgumentsItem)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        _items.setElementAt(vArgumentsItem, index);
    } //-- void setArgumentsItem(int, grms_schema.ArgumentsItem) 

    /**
     * Method setArgumentsItem
     * 
     * 
     * 
     * @param argumentsItemArray
     */
    public void setArgumentsItem(grms_schema.ArgumentsItem[] argumentsItemArray)
    {
        //-- copy array
        _items.removeAllElements();
        for (int i = 0; i < argumentsItemArray.length; i++) {
            _items.addElement(argumentsItemArray[i]);
        }
    } //-- void setArgumentsItem(grms_schema.ArgumentsItem) 

    /**
     * Method unmarshal
     * 
     * 
     * 
     * @param reader
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (grms_schema.Arguments) Unmarshaller.unmarshal(grms_schema.Arguments.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     * 
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
