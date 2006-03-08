/*
 * Created on Jun 28, 2005
 */
package org.gridlab.gat.io.cpi.sftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;

/**
 * @author rob
 */
public class SftpFileOutputStreamAdaptor extends FileOutputStreamCpi {
    ByteArrayOutputStream out;

    SftpConnection c;

    public SftpFileOutputStreamAdaptor(GATContext gatContext,
            Preferences preferences, URI location, Boolean append)
            throws GATObjectCreationException {
        super(gatContext, preferences, location, append);

        checkName("sftp");

        if (!location.isCompatible("sftp")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }

        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        if (location.getHost() == null) {
            throw new GATObjectCreationException(
                    "this adaptor cannot handle local files");
        }

        out = new ByteArrayOutputStream();
    }

    protected void writeStream() throws GATInvocationException {
        String path = location.getPath();

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(out
                    .toByteArray());

            c = SftpFileAdaptor.openConnection(gatContext, preferences,
                    location);

            c.sftp.put(in, path);
        } catch (Exception e) {
            throw new GATInvocationException("sftp file inputstream", e);
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
        out.write(b, offset, len);
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
        out.write(arg0);
    }

}