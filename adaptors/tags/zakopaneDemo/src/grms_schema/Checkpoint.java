/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: Checkpoint.java,v 1.3 2004/10/27 10:30:05 rob Exp $
 */

package grms_schema;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Checkpint files location
 * 
 * @version $Revision: 1.3 $ $Date: 2004/10/27 10:30:05 $
 */
public class Checkpoint implements java.io.Serializable {

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
	private java.util.Vector _fileList;

	//----------------/
	//- Constructors -/
	//----------------/

	public Checkpoint() {
		super();
		_fileList = new Vector();
	} //-- grms_schema.Checkpoint()

	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Method addFile
	 * 
	 * @param vFile
	 */
	public void addFile(grms_schema.File vFile)
			throws java.lang.IndexOutOfBoundsException {
		_fileList.addElement(vFile);
	} //-- void addFile(grms_schema.File)

	/**
	 * Method addFile
	 * 
	 * @param index
	 * @param vFile
	 */
	public void addFile(int index, grms_schema.File vFile)
			throws java.lang.IndexOutOfBoundsException {
		_fileList.insertElementAt(vFile, index);
	} //-- void addFile(int, grms_schema.File)

	/**
	 * Method enumerateFile
	 */
	public java.util.Enumeration enumerateFile() {
		return _fileList.elements();
	} //-- java.util.Enumeration enumerateFile()

	/**
	 * Returns the value of field 'directory'. The field 'directory' has the
	 * following description: Checkpoint directory (not implemented yet)
	 * 
	 * @return the value of field 'directory'.
	 */
	public grms_schema.Directory getDirectory() {
		return this._directory;
	} //-- grms_schema.Directory getDirectory()

	/**
	 * Method getFile
	 * 
	 * @param index
	 */
	public grms_schema.File getFile(int index)
			throws java.lang.IndexOutOfBoundsException {
		//-- check bounds for index
		if ((index < 0) || (index > _fileList.size())) {
			throw new IndexOutOfBoundsException();
		}

		return (grms_schema.File) _fileList.elementAt(index);
	} //-- grms_schema.File getFile(int)

	/**
	 * Method getFile
	 */
	public grms_schema.File[] getFile() {
		int size = _fileList.size();
		grms_schema.File[] mArray = new grms_schema.File[size];
		for (int index = 0; index < size; index++) {
			mArray[index] = (grms_schema.File) _fileList.elementAt(index);
		}
		return mArray;
	} //-- grms_schema.File[] getFile()

	/**
	 * Method getFileCount
	 */
	public int getFileCount() {
		return _fileList.size();
	} //-- int getFileCount()

	/**
	 * Method isValid
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
	 * @param handler
	 */
	public void marshal(org.xml.sax.ContentHandler handler)
			throws java.io.IOException, org.exolab.castor.xml.MarshalException,
			org.exolab.castor.xml.ValidationException {

		Marshaller.marshal(this, handler);
	} //-- void marshal(org.xml.sax.ContentHandler)

	/**
	 * Method removeAllFile
	 */
	public void removeAllFile() {
		_fileList.removeAllElements();
	} //-- void removeAllFile()

	/**
	 * Method removeFile
	 * 
	 * @param index
	 */
	public grms_schema.File removeFile(int index) {
		java.lang.Object obj = _fileList.elementAt(index);
		_fileList.removeElementAt(index);
		return (grms_schema.File) obj;
	} //-- grms_schema.File removeFile(int)

	/**
	 * Sets the value of field 'directory'. The field 'directory' has the
	 * following description: Checkpoint directory (not implemented yet)
	 * 
	 * @param directory
	 *            the value of field 'directory'.
	 */
	public void setDirectory(grms_schema.Directory directory) {
		this._directory = directory;
	} //-- void setDirectory(grms_schema.Directory)

	/**
	 * Method setFile
	 * 
	 * @param index
	 * @param vFile
	 */
	public void setFile(int index, grms_schema.File vFile)
			throws java.lang.IndexOutOfBoundsException {
		//-- check bounds for index
		if ((index < 0) || (index > _fileList.size())) {
			throw new IndexOutOfBoundsException();
		}
		_fileList.setElementAt(vFile, index);
	} //-- void setFile(int, grms_schema.File)

	/**
	 * Method setFile
	 * 
	 * @param fileArray
	 */
	public void setFile(grms_schema.File[] fileArray) {
		//-- copy array
		_fileList.removeAllElements();
		for (int i = 0; i < fileArray.length; i++) {
			_fileList.addElement(fileArray[i]);
		}
	} //-- void setFile(grms_schema.File)

	/**
	 * Method unmarshal
	 * 
	 * @param reader
	 */
	public static java.lang.Object unmarshal(java.io.Reader reader)
			throws org.exolab.castor.xml.MarshalException,
			org.exolab.castor.xml.ValidationException {
		return (grms_schema.Checkpoint) Unmarshaller.unmarshal(
				grms_schema.Checkpoint.class, reader);
	} //-- java.lang.Object unmarshal(java.io.Reader)

	/**
	 * Method validate
	 */
	public void validate() throws org.exolab.castor.xml.ValidationException {
		org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
		validator.validate(this);
	} //-- void validate()

}