/*
 * Created on Aug 11, 2004
 */
package org.gridlab.gat.io.cpi.local;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;

/**
 * @author rob
 */
public class LocalRandomAccessFileAdaptor extends RandomAccessFileCpi {

	RandomAccessFile rf;

	public LocalRandomAccessFileAdaptor(GATContext gatContext,
			Preferences preferences, File file, String mode)
			throws GATObjectCreationException {
		super(gatContext, preferences, file, mode);
		
		checkName("local");

		try {
			java.io.File f = new java.io.File(file.toURI().toJavaURI());
			rf = new RandomAccessFile(f, mode);
		} catch (FileNotFoundException e) {
			throw new GATObjectCreationException("local randomaccess file", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.RandomAccessFile#close()
	 */
	public void close() throws IOException {
		rf.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.RandomAccessFile#getFilePointer()
	 */
	public long getFilePointer() throws IOException {
		return rf.getFilePointer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.RandomAccessFile#length()
	 */
	public long length() throws IOException {
		return rf.length();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.RandomAccessFile#read()
	 */
	public int read() throws IOException {
		return rf.read();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.RandomAccessFile#read(byte[], int, int)
	 */
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		return rf.read(arg0, arg1, arg2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.RandomAccessFile#read(byte[])
	 */
	public int read(byte[] arg0) throws IOException {
		return rf.read(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.RandomAccessFile#seek(long)
	 */
	public void seek(long arg0) throws IOException {
		rf.seek(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.RandomAccessFile#setLength(long)
	 */
	public void setLength(long arg0) throws IOException {
		rf.setLength(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.RandomAccessFile#skipBytes(int)
	 */
	public int skipBytes(int arg0) throws IOException {
		return rf.skipBytes(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.RandomAccessFile#write(byte[], int, int)
	 */
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		rf.write(arg0, arg1, arg2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.RandomAccessFile#write(byte[])
	 */
	public void write(byte[] arg0) throws IOException {
		rf.write(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.RandomAccessFile#write(int)
	 */
	public void write(int arg0) throws IOException {
		rf.write(arg0);
	}
}