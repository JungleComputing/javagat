/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: CheckpointItem.java,v 1.1 2005/04/07 13:48:18 rob Exp $
 */

package grms_schema;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/


/**
 * Class CheckpointItem.
 * 
 * @version $Revision: 1.1 $ $Date: 2005/04/07 13:48:18 $
 */
public class CheckpointItem implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Checkpoint directory (not implemented yet)
     */
    private grms_schema.Directory _directory;

    /**
     * Checkpoint files description
     */
    private grms_schema.File _file;


      //----------------/
     //- Constructors -/
    //----------------/

    public CheckpointItem() {
        super();
    } //-- grms_schema.CheckpointItem()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'directory'. The field
     * 'directory' has the following description: Checkpoint
     * directory (not implemented yet)
     * 
     * @return Directory
     * @return the value of field 'directory'.
     */
    public grms_schema.Directory getDirectory()
    {
        return this._directory;
    } //-- grms_schema.Directory getDirectory() 

    /**
     * Returns the value of field 'file'. The field 'file' has the
     * following description: Checkpoint files description
     * 
     * @return File
     * @return the value of field 'file'.
     */
    public grms_schema.File getFile()
    {
        return this._file;
    } //-- grms_schema.File getFile() 

    /**
     * Sets the value of field 'directory'. The field 'directory'
     * has the following description: Checkpoint directory (not
     * implemented yet)
     * 
     * @param directory the value of field 'directory'.
     */
    public void setDirectory(grms_schema.Directory directory)
    {
        this._directory = directory;
    } //-- void setDirectory(grms_schema.Directory) 

    /**
     * Sets the value of field 'file'. The field 'file' has the
     * following description: Checkpoint files description
     * 
     * @param file the value of field 'file'.
     */
    public void setFile(grms_schema.File file)
    {
        this._file = file;
    } //-- void setFile(grms_schema.File) 

}
