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
 * Checkpint files location
 *
 * @version $Revision: 1.9 $ $Date$
 */
public class Checkpoint implements java.io.Serializable {
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
    public Checkpoint() {
        super();
        _items = new Vector();
    } //-- grms_schema.Checkpoint()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addCheckpointItem
     *
     *
     *
     * @param vCheckpointItem
     */
    public void addCheckpointItem(grms_schema.CheckpointItem vCheckpointItem)
            throws java.lang.IndexOutOfBoundsException {
        _items.addElement(vCheckpointItem);
    } //-- void addCheckpointItem(grms_schema.CheckpointItem) 

    /**
     * Method addCheckpointItem
     *
     *
     *
     * @param index
     * @param vCheckpointItem
     */
    public void addCheckpointItem(int index,
            grms_schema.CheckpointItem vCheckpointItem)
            throws java.lang.IndexOutOfBoundsException {
        _items.insertElementAt(vCheckpointItem, index);
    } //-- void addCheckpointItem(int, grms_schema.CheckpointItem) 

    /**
     * Method enumerateCheckpointItem
     *
     *
     *
     * @return Enumeration
     */
    public java.util.Enumeration enumerateCheckpointItem() {
        return _items.elements();
    } //-- java.util.Enumeration enumerateCheckpointItem() 

    /**
     * Method getCheckpointItem
     *
     *
     *
     * @param index
     * @return CheckpointItem
     */
    public grms_schema.CheckpointItem getCheckpointItem(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (grms_schema.CheckpointItem) _items.elementAt(index);
    } //-- grms_schema.CheckpointItem getCheckpointItem(int) 

    /**
     * Method getCheckpointItem
     *
     *
     *
     * @return CheckpointItem
     */
    public grms_schema.CheckpointItem[] getCheckpointItem() {
        int size = _items.size();
        grms_schema.CheckpointItem[] mArray = new grms_schema.CheckpointItem[size];

        for (int index = 0; index < size; index++) {
            mArray[index] = (grms_schema.CheckpointItem) _items
                .elementAt(index);
        }

        return mArray;
    } //-- grms_schema.CheckpointItem[] getCheckpointItem() 

    /**
     * Method getCheckpointItemCount
     *
     *
     *
     * @return int
     */
    public int getCheckpointItemCount() {
        return _items.size();
    } //-- int getCheckpointItemCount() 

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
     * Method removeAllCheckpointItem
     *
     */
    public void removeAllCheckpointItem() {
        _items.removeAllElements();
    } //-- void removeAllCheckpointItem() 

    /**
     * Method removeCheckpointItem
     *
     *
     *
     * @param index
     * @return CheckpointItem
     */
    public grms_schema.CheckpointItem removeCheckpointItem(int index) {
        java.lang.Object obj = _items.elementAt(index);
        _items.removeElementAt(index);

        return (grms_schema.CheckpointItem) obj;
    } //-- grms_schema.CheckpointItem removeCheckpointItem(int) 

    /**
     * Method setCheckpointItem
     *
     *
     *
     * @param index
     * @param vCheckpointItem
     */
    public void setCheckpointItem(int index,
            grms_schema.CheckpointItem vCheckpointItem)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }

        _items.setElementAt(vCheckpointItem, index);
    } //-- void setCheckpointItem(int, grms_schema.CheckpointItem) 

    /**
     * Method setCheckpointItem
     *
     *
     *
     * @param checkpointItemArray
     */
    public void setCheckpointItem(
            grms_schema.CheckpointItem[] checkpointItemArray) {
        //-- copy array
        _items.removeAllElements();

        for (int i = 0; i < checkpointItemArray.length; i++) {
            _items.addElement(checkpointItemArray[i]);
        }
    } //-- void setCheckpointItem(grms_schema.CheckpointItem) 

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
        return (grms_schema.Checkpoint) Unmarshaller.unmarshal(
            grms_schema.Checkpoint.class, reader);
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
