package org.gridlab.gat.io.cpi;

import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.RandomAccessFileInterface;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;

/**
 * Capability provider interface to the File class.
 * <p>
 * Capability provider wishing to provide the functionality of the File class
 * must extend this class and implement all of the abstract methods in this
 * class. Each abstract method in this class mirrors the corresponding method in
 * this File class and will be used to implement the corresponding method in the
 * File class at runtime.
 */
public abstract class RandomAccessFileCpi extends MonitorableCpi implements RandomAccessFileInterface {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
        capabilities.put("toURI", true);
        capabilities.put("getFile", true);
        capabilities.put("close", false);
        capabilities.put("getFilePointer", false);
        capabilities.put("length", false);
        capabilities.put("read", false);
        capabilities.put("seek", false);
        capabilities.put("setLength", false);
        capabilities.put("skipBytes", false);
        capabilities.put("write", false);
        return capabilities;
    }

    protected GATContext gatContext;
    
    protected URI location;

    protected String mode;

    /**
     * Constructs a FileCpi instance which corresponds to the physical file
     * identified by the passed Location and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this FileCpi.
     * @param file
     *                the file to create this random access file on
     * @param mode
     *                see RandomAccessFile
     */
    public RandomAccessFileCpi(GATContext gatContext, URI location, String mode) {
        this.gatContext = gatContext;
        this.location = location;
        this.mode = mode;
    }

    public boolean equals(Object object) {
        if (!(object instanceof org.gridlab.gat.io.RandomAccessFile)) {
            return false;
        }

        org.gridlab.gat.io.RandomAccessFile rf = (org.gridlab.gat.io.RandomAccessFile) object;

        return location.equals(rf.toURI());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFileInterface#toURI()
     */
    public URI toURI() {
        return location;
    }

    public int hashCode() {
        return location.hashCode();
    }

    public org.gridlab.gat.io.File getFile() throws GATInvocationException {
        try {
            return GAT.createFile(gatContext, location);
        } catch (Exception e) {
            throw new GATInvocationException("random access file cpi", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFile#close()
     */
    public void close() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFile#getFilePointer()
     */
    public long getFilePointer() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFile#length()
     */
    public long length() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFile#read()
     */
    public int read() throws GATInvocationException {
        byte[] buf = new byte[1];
        int len = read(buf);
        if (len <= 0) {
            return -1;
        }
        return buf[0] & 0377;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFile#read(byte[], int, int)
     */
    public int read(byte[] arg0, int arg1, int arg2)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFile#read(byte[])
     */
    public int read(byte[] arg0) throws GATInvocationException {
        return read(arg0, 0, arg0.length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFile#seek(long)
     */
    public void seek(long arg0) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFile#setLength(long)
     */
    public void setLength(long arg0) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFile#skipBytes(int)
     */
    public int skipBytes(int arg0) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFile#write(byte[], int, int)
     */
    public void write(byte[] arg0, int arg1, int arg2)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFile#write(byte[])
     */
    public void write(byte[] arg0) throws GATInvocationException {
        write(arg0, 0, arg0.length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.RandomAccessFile#write(int)
     */
    public void write(int arg0) throws GATInvocationException {
        byte[] buf = new byte[1];
        buf[0] = (byte) arg0;
        write(buf);
    }

    public String toString() {
        return location.toString();
    }
}
