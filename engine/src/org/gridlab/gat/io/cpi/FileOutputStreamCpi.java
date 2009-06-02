package org.gridlab.gat.io.cpi;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileOutputStreamInterface;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;

/**
 * Capability provider interface to the FileStream class.
 * <p>
 * Capability provider wishing to provide the functionality of the FileStream
 * class must extend this class and implement all of the abstract methods in
 * this class. Each abstract method in this class mirrors the corresponding
 * method in this FileStream class and will be used to implement the
 * corresponding method in the FileStream class at runtime.
 */
public abstract class FileOutputStreamCpi extends MonitorableCpi implements FileOutputStreamInterface {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = MonitorableCpi.getSupportedCapabilities();
        capabilities.put("close", false);
        capabilities.put("flush", false);
        capabilities.put("write", false);
        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = MonitorableCpi.getSupportedPreferences();
        preferences.put("FileOutputStream.adaptor.name", "<no default>");
        preferences.put("adaptors.local", "false");

        return preferences;
    }

    protected static Logger logger = LoggerFactory
            .getLogger(FileOutputStreamCpi.class);
    
    protected GATContext gatContext;

    protected URI location;

    protected boolean append;

    /**
     * Constructs a FileInputStream Cpi instance which corresponds to the
     * physical file identified by the passed Location and whose access rights
     * are determined by the passed GATContext.
     * 
     * @param location
     *                A Location which represents the URI corresponding to the
     *                physical file.
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this FileCpi.
     */
    protected FileOutputStreamCpi(GATContext gatContext, URI location,
            Boolean append) {
        this.gatContext = gatContext;
        this.location = location;
        this.append = append.booleanValue();

        if (logger.isDebugEnabled()) {
            logger.debug("FileOutputStreamCpi: created stream with URI "
                    + location);
        }
    }

    /**
     * Tests this File for equality with the passed Object.
     * <p>
     * If the given object is not a File, then this method immediately returns
     * false.
     * <p>
     * If the given object is a File, then it is deemed equal to this instance
     * if a URI object constructed from this File's location and a URI object
     * constructed from the passed File's URI are equal as determined by the
     * Equals method of URI.
     * 
     * @param object
     *                The Object to test for equality
     * @return A boolean indicating equality
     */
    public boolean equals(Object object) {
        if (!(object instanceof FileOutputStreamCpi)) {
            return false;
        }

        FileOutputStreamCpi s = (FileOutputStreamCpi) object;

        return location.equals(s.location);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileOutputStreamInterface#close()
     */
    public void close() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileOutputStreamInterface#flush()
     */
    public void flush() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileOutputStreamInterface#write(byte[], int, int)
     */
    public void write(byte[] arg0, int arg1, int arg2)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileOutputStreamInterface#write(byte[])
     */
    public void write(byte[] arg0) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileOutputStreamInterface#write(int)
     */
    public void write(int arg0) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String toString() {
        return location.toString();
    }

}
