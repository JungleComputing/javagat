/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: ArgumentsItem.java,v 1.4 2006/01/23 11:05:53 rob Exp $
 */
package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

/**
 * Class ArgumentsItem.
 *
 * @version $Revision: 1.4 $ $Date: 2006/01/23 11:05:53 $
 */
public class ArgumentsItem implements java.io.Serializable {
    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _value
     */
    private grms_schema.Value _value;

    /**
     * Field _file
     */
    private grms_schema.File _file;

    /**
     * Not implemented yet
     */
    private grms_schema.Directory _directory;

    //----------------/
    //- Constructors -/
    //----------------/
    public ArgumentsItem() {
        super();
    } //-- grms_schema.ArgumentsItem()

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'directory'. The field
     * 'directory' has the following description: Not implemented
     * yet
     *
     * @return Directory
     * @return the value of field 'directory'.
     */
    public grms_schema.Directory getDirectory() {
        return this._directory;
    } //-- grms_schema.Directory getDirectory() 

    /**
     * Returns the value of field 'file'.
     *
     * @return File
     * @return the value of field 'file'.
     */
    public grms_schema.File getFile() {
        return this._file;
    } //-- grms_schema.File getFile() 

    /**
     * Returns the value of field 'value'.
     *
     * @return Value
     * @return the value of field 'value'.
     */
    public grms_schema.Value getValue() {
        return this._value;
    } //-- grms_schema.Value getValue() 

    /**
     * Sets the value of field 'directory'. The field 'directory'
     * has the following description: Not implemented yet
     *
     * @param directory the value of field 'directory'.
     */
    public void setDirectory(grms_schema.Directory directory) {
        this._directory = directory;
    } //-- void setDirectory(grms_schema.Directory) 

    /**
     * Sets the value of field 'file'.
     *
     * @param file the value of field 'file'.
     */
    public void setFile(grms_schema.File file) {
        this._file = file;
    } //-- void setFile(grms_schema.File) 

    /**
     * Sets the value of field 'value'.
     *
     * @param value the value of field 'value'.
     */
    public void setValue(grms_schema.Value value) {
        this._value = value;
    } //-- void setValue(grms_schema.Value) 
}
