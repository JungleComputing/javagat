/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: Arguments.java,v 1.3 2004/10/27 10:30:04 rob Exp $
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
 * @version $Revision: 1.3 $ $Date: 2004/10/27 10:30:04 $
 */
public class Arguments implements java.io.Serializable {

	//--------------------------/
	//- Class/Member Variables -/
	//--------------------------/

	/**
	 * Field _valueList
	 */
	private java.util.Vector _valueList;

	/**
	 * Field _fileList
	 */
	private java.util.Vector _fileList;

	/**
	 * Not implemented yet
	 */
	private java.util.Vector _directoryList;

	//----------------/
	//- Constructors -/
	//----------------/

	public Arguments() {
		super();
		_valueList = new Vector();
		_fileList = new Vector();
		_directoryList = new Vector();
	} //-- grms_schema.Arguments()

	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Method addDirectory
	 * 
	 * @param vDirectory
	 */
	public void addDirectory(grms_schema.Directory vDirectory)
			throws java.lang.IndexOutOfBoundsException {
		_directoryList.addElement(vDirectory);
	} //-- void addDirectory(grms_schema.Directory)

	/**
	 * Method addDirectory
	 * 
	 * @param index
	 * @param vDirectory
	 */
	public void addDirectory(int index, grms_schema.Directory vDirectory)
			throws java.lang.IndexOutOfBoundsException {
		_directoryList.insertElementAt(vDirectory, index);
	} //-- void addDirectory(int, grms_schema.Directory)

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
	 * Method addValue
	 * 
	 * @param vValue
	 */
	public void addValue(grms_schema.Value vValue)
			throws java.lang.IndexOutOfBoundsException {
		_valueList.addElement(vValue);
	} //-- void addValue(grms_schema.Value)

	/**
	 * Method addValue
	 * 
	 * @param index
	 * @param vValue
	 */
	public void addValue(int index, grms_schema.Value vValue)
			throws java.lang.IndexOutOfBoundsException {
		_valueList.insertElementAt(vValue, index);
	} //-- void addValue(int, grms_schema.Value)

	/**
	 * Method enumerateDirectory
	 */
	public java.util.Enumeration enumerateDirectory() {
		return _directoryList.elements();
	} //-- java.util.Enumeration enumerateDirectory()

	/**
	 * Method enumerateFile
	 */
	public java.util.Enumeration enumerateFile() {
		return _fileList.elements();
	} //-- java.util.Enumeration enumerateFile()

	/**
	 * Method enumerateValue
	 */
	public java.util.Enumeration enumerateValue() {
		return _valueList.elements();
	} //-- java.util.Enumeration enumerateValue()

	/**
	 * Method getDirectory
	 * 
	 * @param index
	 */
	public grms_schema.Directory getDirectory(int index)
			throws java.lang.IndexOutOfBoundsException {
		//-- check bounds for index
		if ((index < 0) || (index > _directoryList.size())) {
			throw new IndexOutOfBoundsException();
		}

		return (grms_schema.Directory) _directoryList.elementAt(index);
	} //-- grms_schema.Directory getDirectory(int)

	/**
	 * Method getDirectory
	 */
	public grms_schema.Directory[] getDirectory() {
		int size = _directoryList.size();
		grms_schema.Directory[] mArray = new grms_schema.Directory[size];
		for (int index = 0; index < size; index++) {
			mArray[index] = (grms_schema.Directory) _directoryList
					.elementAt(index);
		}
		return mArray;
	} //-- grms_schema.Directory[] getDirectory()

	/**
	 * Method getDirectoryCount
	 */
	public int getDirectoryCount() {
		return _directoryList.size();
	} //-- int getDirectoryCount()

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
	 * Method getValue
	 * 
	 * @param index
	 */
	public grms_schema.Value getValue(int index)
			throws java.lang.IndexOutOfBoundsException {
		//-- check bounds for index
		if ((index < 0) || (index > _valueList.size())) {
			throw new IndexOutOfBoundsException();
		}

		return (grms_schema.Value) _valueList.elementAt(index);
	} //-- grms_schema.Value getValue(int)

	/**
	 * Method getValue
	 */
	public grms_schema.Value[] getValue() {
		int size = _valueList.size();
		grms_schema.Value[] mArray = new grms_schema.Value[size];
		for (int index = 0; index < size; index++) {
			mArray[index] = (grms_schema.Value) _valueList.elementAt(index);
		}
		return mArray;
	} //-- grms_schema.Value[] getValue()

	/**
	 * Method getValueCount
	 */
	public int getValueCount() {
		return _valueList.size();
	} //-- int getValueCount()

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
	 * Method removeAllDirectory
	 */
	public void removeAllDirectory() {
		_directoryList.removeAllElements();
	} //-- void removeAllDirectory()

	/**
	 * Method removeAllFile
	 */
	public void removeAllFile() {
		_fileList.removeAllElements();
	} //-- void removeAllFile()

	/**
	 * Method removeAllValue
	 */
	public void removeAllValue() {
		_valueList.removeAllElements();
	} //-- void removeAllValue()

	/**
	 * Method removeDirectory
	 * 
	 * @param index
	 */
	public grms_schema.Directory removeDirectory(int index) {
		java.lang.Object obj = _directoryList.elementAt(index);
		_directoryList.removeElementAt(index);
		return (grms_schema.Directory) obj;
	} //-- grms_schema.Directory removeDirectory(int)

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
	 * Method removeValue
	 * 
	 * @param index
	 */
	public grms_schema.Value removeValue(int index) {
		java.lang.Object obj = _valueList.elementAt(index);
		_valueList.removeElementAt(index);
		return (grms_schema.Value) obj;
	} //-- grms_schema.Value removeValue(int)

	/**
	 * Method setDirectory
	 * 
	 * @param index
	 * @param vDirectory
	 */
	public void setDirectory(int index, grms_schema.Directory vDirectory)
			throws java.lang.IndexOutOfBoundsException {
		//-- check bounds for index
		if ((index < 0) || (index > _directoryList.size())) {
			throw new IndexOutOfBoundsException();
		}
		_directoryList.setElementAt(vDirectory, index);
	} //-- void setDirectory(int, grms_schema.Directory)

	/**
	 * Method setDirectory
	 * 
	 * @param directoryArray
	 */
	public void setDirectory(grms_schema.Directory[] directoryArray) {
		//-- copy array
		_directoryList.removeAllElements();
		for (int i = 0; i < directoryArray.length; i++) {
			_directoryList.addElement(directoryArray[i]);
		}
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
	 * Method setValue
	 * 
	 * @param index
	 * @param vValue
	 */
	public void setValue(int index, grms_schema.Value vValue)
			throws java.lang.IndexOutOfBoundsException {
		//-- check bounds for index
		if ((index < 0) || (index > _valueList.size())) {
			throw new IndexOutOfBoundsException();
		}
		_valueList.setElementAt(vValue, index);
	} //-- void setValue(int, grms_schema.Value)

	/**
	 * Method setValue
	 * 
	 * @param valueArray
	 */
	public void setValue(grms_schema.Value[] valueArray) {
		//-- copy array
		_valueList.removeAllElements();
		for (int i = 0; i < valueArray.length; i++) {
			_valueList.addElement(valueArray[i]);
		}
	} //-- void setValue(grms_schema.Value)

	/**
	 * Method unmarshal
	 * 
	 * @param reader
	 */
	public static java.lang.Object unmarshal(java.io.Reader reader)
			throws org.exolab.castor.xml.MarshalException,
			org.exolab.castor.xml.ValidationException {
		return (grms_schema.Arguments) Unmarshaller.unmarshal(
				grms_schema.Arguments.class, reader);
	} //-- java.lang.Object unmarshal(java.io.Reader)

	/**
	 * Method validate
	 */
	public void validate() throws org.exolab.castor.xml.ValidationException {
		org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
		validator.validate(this);
	} //-- void validate()

}