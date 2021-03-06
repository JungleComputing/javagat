package org.gridlab.gat.io.cpi.globus;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;

public abstract class GlobusFileOutputStreamAdaptor extends FileOutputStreamCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileOutputStreamCpi
                .getSupportedCapabilities();
        capabilities.put("close", true);
        capabilities.put("flush", true);
        capabilities.put("write", true);

        return capabilities;
    }

    OutputStream out;

    public GlobusFileOutputStreamAdaptor(GATContext gatContext, URI location,
            Boolean append) throws GATObjectCreationException {
        super(gatContext, location, append);

        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        if (location.getHost() == null) {
            throw new GATObjectCreationException(
                    "this adaptor cannot copy local files");
        }
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
            throw new GATInvocationException("globus output stream", e);
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
            throw new GATInvocationException("globus output stream", e);
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
            throw new GATInvocationException("globus output stream", e);
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
            throw new GATInvocationException("globus output stream", e);
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
            throw new GATInvocationException("globus output stream", e);
        }
    }
}
