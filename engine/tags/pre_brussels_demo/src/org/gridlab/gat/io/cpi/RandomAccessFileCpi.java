package org.gridlab.gat.io.cpi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.RandomAccessFile;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * Capability provider interface to the File class.
 * <p>
 * Capability provider wishing to provide the functionality of the File class
 * must extend this class and implement all of the abstract methods in this
 * class. Each abstract method in this class mirrors the corresponding method in
 * this File class and will be used to implement the corresponding method in the
 * File class at runtime.
 */
public abstract class RandomAccessFileCpi implements RandomAccessFile {

	GATContext gatContext;

	Preferences preferences;

	File file;

	String mode;

	/**
	 * Constructs a FileCpi instance which corresponds to the physical file
	 * identified by the passed Location and whose access rights are determined
	 * by the passed GATContext.
	 * 
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this FileCpi.
	 * @param preferences
	 *            preferences to be associated with this file
	 * @param file
	 *            the file to create this random access file on
	 * @param mode
	 *            see RandomAccessFile
	 * @throws FileNotFoundException
	 *             Thrown upon creation problems
	 */
	public RandomAccessFileCpi(GATContext gatContext, Preferences preferences,
			File file, String mode) {
		this.gatContext = gatContext;
		this.preferences = preferences;
		this.file = file;
		this.mode = mode;
	}

	public boolean equals(Object object) {
		if (!(object instanceof org.gridlab.gat.io.RandomAccessFile))
			return false;

		org.gridlab.gat.io.RandomAccessFile rf = (org.gridlab.gat.io.RandomAccessFile) object;
		return file.equals(rf.getFile());	
	}

	public int hashCode() {
		return file.hashCode();
	}
	
	public org.gridlab.gat.io.File getFile() {
		return file;
	}
	
	/* (non-Javadoc)
	 * @see org.gridlab.gat.io.RandomAccessFile#close()
	 */
	public void close() throws IOException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.io.RandomAccessFile#getFilePointer()
	 */
	public long getFilePointer() throws IOException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.io.RandomAccessFile#length()
	 */
	public long length() throws IOException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.io.RandomAccessFile#read()
	 */
	public int read() throws IOException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.io.RandomAccessFile#read(byte[], int, int)
	 */
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.io.RandomAccessFile#read(byte[])
	 */
	public int read(byte[] arg0) throws IOException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.io.RandomAccessFile#seek(long)
	 */
	public void seek(long arg0) throws IOException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.io.RandomAccessFile#setLength(long)
	 */
	public void setLength(long arg0) throws IOException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.io.RandomAccessFile#skipBytes(int)
	 */
	public int skipBytes(int arg0) throws IOException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.io.RandomAccessFile#write(byte[], int, int)
	 */
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.io.RandomAccessFile#write(byte[])
	 */
	public void write(byte[] arg0) throws IOException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.io.RandomAccessFile#write(int)
	 */
	public void write(int arg0) throws IOException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.monitoring.Monitorable#addMetricListener(org.gridlab.gat.monitoring.MetricListener, org.gridlab.gat.monitoring.Metric)
	 */
	public void addMetricListener(MetricListener metricListener, Metric metric)
			throws RemoteException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.monitoring.Monitorable#getMetrics()
	 */
	public List getMetrics() throws RemoteException {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.monitoring.Monitorable#removeMetricListener(org.gridlab.gat.monitoring.MetricListener, org.gridlab.gat.monitoring.Metric)
	 */
	public void removeMetricListener(MetricListener metricListener,
			Metric metric) throws RemoteException {
		throw new Error("Not implemented");
	}
}