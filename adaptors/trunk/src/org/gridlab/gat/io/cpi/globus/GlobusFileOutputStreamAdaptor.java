package org.gridlab.gat.io.cpi.globus;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;

import java.io.IOException;
import java.io.OutputStream;

public abstract class GlobusFileOutputStreamAdaptor extends FileOutputStreamCpi {
    OutputStream out;

    public GlobusFileOutputStreamAdaptor(GATContext gatContext,
            Preferences preferences, URI location, Boolean append)
            throws GATObjectCreationException {
        super(gatContext, preferences, location, append);

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
