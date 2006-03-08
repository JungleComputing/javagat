package org.gridlab.gat.io;

import java.io.IOException;

import org.gridlab.gat.monitoring.Monitorable;

/**
 * An abstract representation of a physical file.
 * <p>
 * An instance of this class presents an abstract, system-independent view of a
 * physical file. User interfaces and operating systems use system-dependent
 * pathname strings to identify physical files. GAT, however, uses an operating
 * system independent pathname string to identify a physical file. A physical
 * file in GAT is identified by a URI.
 * <p>
 * An instance of this File class allows for various high-level operations to be
 * preformed on a physical file. For example, one can, with a single API call,
 * copy a physical file from one location to a second location, move a physical
 * file from one location to a second location, delete a physical file, and
 * preform various other operations on a physical file. The utility of this
 * high-level view of a physical file is multi-fold. The client of an instance
 * of this class does not have to concern themselves with the details of reading
 * every single byte of a physical file when all they wish to do is copy the
 * physical file to a new location. Similarly, a client does not have to deal
 * with all the various error states that can occur when moving a physical file (
 * Have all the various bytes been read correctly? Have all the various bytes
 * been saved correctly? Did the deletion of the original file proceed
 * correctly? ); the client simply has to call a single API call and the
 * physical file is moved.
 */
public interface RandomAccessFile 
		extends Monitorable, java.io.Serializable {


	/**
	 * Tests this RandomAccessFile for equality with the passed Object.
	 * <p>
	 * If the given object is not a RandomAccessFile, then this method immediately returns
	 * false.
	 * <p>
	 * If the given object is a RandomAccessFile, then it is deemed equal to this instance
	 * if a URI object constructed from this RandomAccessFile's location and a URI object
	 * constructed from the passed RandomAccessFile's URI are equal as determined by the
	 * Equals method of URI.
	 * 
	 * @param object
	 *            The Object to test for equality
	 * @return A boolean indicating equality
	 */
	public boolean equals(Object object);
	/* (non-Javadoc)
	 * @see java.io.RandomAccessFile#close()
	 */

	public org.gridlab.gat.io.File getFile();
	
	public void close() throws IOException;
	/* (non-Javadoc)
	 * @see java.io.RandomAccessFile#getFilePointer()
	 */
	public long getFilePointer() throws IOException;
	/* (non-Javadoc)
	 * @see java.io.RandomAccessFile#length()
	 */
	public long length() throws IOException;
	/* (non-Javadoc)
	 * @see java.io.RandomAccessFile#read()
	 */
	public int read() throws IOException;
	/* (non-Javadoc)
	 * @see java.io.RandomAccessFile#read(byte[], int, int)
	 */
	public int read(byte[] arg0, int arg1, int arg2) throws IOException;
	/* (non-Javadoc)
	 * @see java.io.RandomAccessFile#read(byte[])
	 */
	public int read(byte[] arg0) throws IOException;
	/* (non-Javadoc)
	 * @see java.io.RandomAccessFile#seek(long)
	 */
	public void seek(long arg0) throws IOException;
	/* (non-Javadoc)
	 * @see java.io.RandomAccessFile#setLength(long)
	 */
	public void setLength(long arg0) throws IOException;
	/* (non-Javadoc)
	 * @see java.io.DataInput#skipBytes(int)
	 */
	public int skipBytes(int arg0) throws IOException;
	/* (non-Javadoc)
	 * @see java.io.DataOutput#write(byte[], int, int)
	 */
	public void write(byte[] arg0, int arg1, int arg2) throws IOException;
	/* (non-Javadoc)
	 * @see java.io.DataOutput#write(byte[])
	 */
	public void write(byte[] arg0) throws IOException;
	/* (non-Javadoc)
	 * @see java.io.DataOutput#write(int)
	 */
	public void write(int arg0) throws IOException;
}