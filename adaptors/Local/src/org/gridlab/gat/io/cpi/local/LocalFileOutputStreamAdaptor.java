package org.gridlab.gat.io.cpi.local;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;

public class LocalFileOutputStreamAdaptor extends FileOutputStreamCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileOutputStreamCpi
                .getSupportedCapabilities();
        capabilities.put("close", true);
        capabilities.put("flush", true);
        capabilities.put("write", true);

        return capabilities;
    }

    FileOutputStream out;

    public LocalFileOutputStreamAdaptor(GATContext gatContext, URI location,
            Boolean append) throws IOException, GATObjectCreationException {
        super(gatContext, location, append);

        if (!location.refersToLocalHost()) {
            throw new AdaptorNotApplicableException(
                    "Cannot use remote files with the local file output stream adaptor");
        }

        if (!location.isCompatible("file")) {
            throw new AdaptorNotApplicableException("cannot handle this URI: "
                    + location);
        }

        String path = location.getPath();

        java.io.File f = new java.io.File(path);

        out = new FileOutputStream(f, append.booleanValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#close()
     */
    public void close() throws GATInvocationException {
        try {
            out.close();
        } catch (IOException e) {
            throw new GATInvocationException("LocalFileOutputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws GATInvocationException {
        try {
            out.flush();
        } catch (IOException e) {
            throw new GATInvocationException("LocalFileOutputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte[] b, int offset, int len)
            throws GATInvocationException {
        try {
            out.write(b, offset, len);
        } catch (IOException e) {
            throw new GATInvocationException("LocalFileOutputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte[] arg0) throws GATInvocationException {
        try {
            out.write(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("LocalFileOutputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(int)
     */
    public void write(int arg0) throws GATInvocationException {
        try {
            out.write(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("LocalFileOutputStream", e);
        }
    }
}
