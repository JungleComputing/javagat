package org.gridlab.gat.io.cpi.local;

import java.io.FileInputStream;
import java.io.IOException;

import org.gridlab.gat.AdaptorNotSelectedException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;

public class LocalFileInputStreamAdaptor extends FileInputStreamCpi {
    FileInputStream in;

    public LocalFileInputStreamAdaptor(GATContext gatContext,
            Preferences preferences, URI location) throws IOException,
            GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.refersToLocalHost()) {
            throw new AdaptorNotSelectedException(
                "Cannot use remote files with the local file input stream adaptor");
        }

        if (!location.isCompatible("file")) {
            throw new AdaptorNotSelectedException("cannot handle this URI");
        }

        String path = location.getPath();

        java.io.File f = new java.io.File(path);
        in = new FileInputStream(f);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#available()
     */
    public int available() throws GATInvocationException {
        try {
            return in.available();
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#close()
     */
    public void close() throws GATInvocationException {
        try {
            in.close();
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#mark(int)
     */
    public synchronized void mark(int arg0) {
        in.mark(arg0);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return in.markSupported();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read()
     */
    public int read() throws GATInvocationException {
        try {
            return in.read();
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int offset, int len)
            throws GATInvocationException {
        try {
            return in.read(b, offset, len);
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] arg0) throws GATInvocationException {
        try {
            return in.read(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#reset()
     */
    public synchronized void reset() throws GATInvocationException {
        try {
            in.reset();
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long arg0) throws GATInvocationException {
        try {
            return in.skip(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }
}
