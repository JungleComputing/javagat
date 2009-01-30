package org.gridlab.gat.io.cpi;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInputStreamInterface;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;

public abstract class FileInputStreamCpi extends MonitorableCpi implements FileInputStreamInterface {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = MonitorableCpi.getSupportedCapabilities();
        capabilities.put("available", false);
        capabilities.put("close", false);
        capabilities.put("mark", false);
        capabilities.put("markSupported", false);
        capabilities.put("read", false);
        capabilities.put("reset", false);
        capabilities.put("skip", false);
        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = MonitorableCpi.getSupportedPreferences();
        preferences.put("FileInputStream.adaptor.name", "<no default>");
        preferences.put("adaptors.local", "false");

        return preferences;
    }

    protected static Logger logger = Logger.getLogger(FileInputStreamCpi.class);
    
    protected GATContext gatContext;

    protected URI location;

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
     * @param preferences
     *                the preferences to be associated with this adaptor
     */
    protected FileInputStreamCpi(GATContext gatContext, URI location) {
        this.gatContext = gatContext;
        this.location = location;

        if (logger.isDebugEnabled()) {
            logger.debug("FileInputStreamCpi: creating stream with URI "
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
        if (!(object instanceof FileInputStreamCpi)) {
            return false;
        }

        FileInputStreamCpi s = (FileInputStreamCpi) object;

        return location.equals(s.location);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileInputStream#available()
     */
    public int available() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileInputStream#close()
     */
    public void close() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileInputStream#mark(int)
     */
    public void mark(int readlimit) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileInputStream#markSupported()
     */
    public boolean markSupported() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileInputStream#read()
     */
    public int read() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileInputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileInputStream#read(byte[])
     */
    public int read(byte[] b) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileInputStream#reset()
     */
    public void reset() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.FileInputStream#skip(long)
     */
    public long skip(long n) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String toString() {
        return location.toString();
    }

}
