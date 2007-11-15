/*
 * Created on Aug 11, 2004
 */
package org.gridlab.gat.io.cpi.local;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;

/**
 * @author rob
 */
@SuppressWarnings("serial")
public class LocalRandomAccessFileAdaptor extends RandomAccessFileCpi {
    RandomAccessFile rf;

    public LocalRandomAccessFileAdaptor(GATContext gatContext,
        Preferences preferences, URI location, String mode)
        throws GATObjectCreationException {
        super(gatContext, preferences, location, mode);

        if (!location.refersToLocalHost()) {
            throw new AdaptorNotApplicableException(
                "Cannot use remote files with the local file adaptor");
        }

        if (!location.isCompatible("file")) {
            throw new AdaptorNotApplicableException("cannot handle this URI");
        }

        java.io.File f;

        if (location.getScheme() != null) {
            f = new java.io.File(location.toJavaURI());
        } else {
            f = new java.io.File(location.getPath());
        }

        try {
            rf = new RandomAccessFile(f, mode);
        } catch (FileNotFoundException e) {
            throw new GATObjectCreationException("local randomaccess file", e);
        }
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.RandomAccessFile#toURI()
     */
    public URI toURI() {
        return location;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.RandomAccessFile#close()
     */
    public void close() throws GATInvocationException {
        try {
            rf.close();
        } catch (Exception e) {
            throw new GATInvocationException("local random access file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.RandomAccessFile#getFilePointer()
     */
    public long getFilePointer() throws GATInvocationException {
        try {
            return rf.getFilePointer();
        } catch (Exception e) {
            throw new GATInvocationException("local random access file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.RandomAccessFile#length()
     */
    public long length() throws GATInvocationException {
        try {
            return rf.length();
        } catch (Exception e) {
            throw new GATInvocationException("local random access file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.RandomAccessFile#read()
     */
    public int read() throws GATInvocationException {
        try {
            return rf.read();
        } catch (Exception e) {
            throw new GATInvocationException("local random access file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.RandomAccessFile#read(byte[], int, int)
     */
    public int read(byte[] arg0, int arg1, int arg2)
        throws GATInvocationException {
        try {
            return rf.read(arg0, arg1, arg2);
        } catch (Exception e) {
            throw new GATInvocationException("local random access file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.RandomAccessFile#read(byte[])
     */
    public int read(byte[] arg0) throws GATInvocationException {
        try {
            return rf.read(arg0);
        } catch (Exception e) {
            throw new GATInvocationException("local random access file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.RandomAccessFile#seek(long)
     */
    public void seek(long arg0) throws GATInvocationException {
        try {
            rf.seek(arg0);
        } catch (Exception e) {
            throw new GATInvocationException("local random access file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.RandomAccessFile#setLength(long)
     */
    public void setLength(long arg0) throws GATInvocationException {
        try {
            rf.setLength(arg0);
        } catch (Exception e) {
            throw new GATInvocationException("local random access file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.RandomAccessFile#skipBytes(int)
     */
    public int skipBytes(int arg0) throws GATInvocationException {
        try {
            return rf.skipBytes(arg0);
        } catch (Exception e) {
            throw new GATInvocationException("local random access file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.RandomAccessFile#write(byte[], int, int)
     */
    public void write(byte[] arg0, int arg1, int arg2)
        throws GATInvocationException {
        try {
            rf.write(arg0, arg1, arg2);
        } catch (Exception e) {
            throw new GATInvocationException("local random access file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.RandomAccessFile#write(byte[])
     */
    public void write(byte[] arg0) throws GATInvocationException {
        try {
            rf.write(arg0);
        } catch (Exception e) {
            throw new GATInvocationException("local random access file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.RandomAccessFile#write(int)
     */
    public void write(int arg0) throws GATInvocationException {
        try {
            rf.write(arg0);
        } catch (Exception e) {
            throw new GATInvocationException("local random access file", e);
        }
    }
}
