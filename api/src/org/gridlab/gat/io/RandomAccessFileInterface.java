package org.gridlab.gat.io;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.SingleAdaptorPerProxy;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * This interface is used for random access to local and remote files.
 * <p>
 * An instance of this class presents an abstract, system-independent view of a
 * physical file.
 */
public interface RandomAccessFileInterface extends Monitorable,
        java.io.Serializable, SingleAdaptorPerProxy {

    public URI toURI();

    /**
     * Tests this RandomAccessFile for equality with the passed Object.
     * <p>
     * If the given object is not a RandomAccessFile, then this method
     * immediately returns false.
     * <p>
     * If the given object is a RandomAccessFile, then it is deemed equal to
     * this instance if a URI object constructed from this RandomAccessFile's
     * location and a URI object constructed from the passed RandomAccessFile's
     * URI are equal as determined by the Equals method of URI.
     * 
     * @param object
     *                The Object to test for equality
     * @return A boolean indicating equality
     */
    public boolean equals(Object object);

    /** See {@link java.io.RandomAccessFile#close}. */
    public void close() throws GATInvocationException;

    /** See {@link java.io.RandomAccessFile#getFilePointer()}. */
    public long getFilePointer() throws GATInvocationException;

    /** See {@link java.io.RandomAccessFile#length()}. */
    public long length() throws GATInvocationException;

    /** See {@link java.io.RandomAccessFile#read()}. */
    public int read() throws GATInvocationException;

    /** See {@link java.io.RandomAccessFile#read(byte[], int, int)}. */
    public int read(byte[] arg0, int arg1, int arg2)
            throws GATInvocationException;

    /** See {@link java.io.RandomAccessFile#read(byte[])}. */
    public int read(byte[] arg0) throws GATInvocationException;

    /** See {@link java.io.RandomAccessFile#seek}. */
    public void seek(long arg0) throws GATInvocationException;

    /** See {@link java.io.RandomAccessFile#setLength(long)}. */
    public void setLength(long arg0) throws GATInvocationException;

    /** See {@link java.io.RandomAccessFile#skipBytes(int)}. */
    public int skipBytes(int arg0) throws GATInvocationException;

    /** See {@link java.io.RandomAccessFile#write(byte[], int, int)}. */
    public void write(byte[] arg0, int arg1, int arg2)
            throws GATInvocationException;

    /** See {@link java.io.RandomAccessFile#write(byte[])}. */
    public void write(byte[] arg0) throws GATInvocationException;

    /** See {@link java.io.RandomAccessFile#write(int)}. */
    public void write(int arg0) throws GATInvocationException;
}
