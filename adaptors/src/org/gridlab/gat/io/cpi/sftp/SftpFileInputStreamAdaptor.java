/*
 * Created on Jun 28, 2005
 */
package org.gridlab.gat.io.cpi.sftp;

import java.io.IOException;
import java.io.InputStream;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;

import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.SftpFileInputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;

/**
 * @author rob
 */
public class SftpFileInputStreamAdaptor extends FileInputStreamCpi {
    InputStream in;

    SftpConnection c;

    private long filesize;

    private long available;

    public SftpFileInputStreamAdaptor(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("sftp")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }

        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        if (location.getHost() == null) {
            throw new GATObjectCreationException(
                "this adaptor cannot read local files");
        }

        // now try to create a stream.
        try {
            in = createStream();
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("SftpFileInputStreamAdaptor", e);
        }
    }

    protected InputStream createStream() throws GATInvocationException {
        String path = location.getPath();

        try {
            c = SftpFileAdaptor.openConnection(gatContext, preferences,
                location);

             // no need to buffer this, it is only writing to memory
            SftpSubsystemClient ssc = c.ssh.openSftpChannel();
            SftpFile sf = ssc.openFile(path, SftpSubsystemClient.OPEN_READ);
            filesize = sf.getAttributes().getSize().longValue();
            available = filesize;
            SftpFileInputStream sfis = new SftpFileInputStream(sf);

            return sfis;

        } catch (Exception e) {
            if (e instanceof GATInvocationException) {
                throw (GATInvocationException) e;
            }
            throw new GATInvocationException("SftpFileInputStreamAdaptor", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#available()
     */
    public int available() throws GATInvocationException {
        return (int) available;
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
            throw new GATInvocationException("SftpFileInputStreamAdaptor", e);
        }
        
        SftpFileAdaptor.closeConnection(c);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read()
     */
    public int read() throws GATInvocationException {
        try {
            int res = in.read();
            if (res >= 0) available--;
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("SftpFileInputStreamAdaptor", e);
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
            int res = in.read(b, offset, len);
            if (res >= 0) available -= res;
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("SftpFileInputStreamAdaptor", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] arg0) throws GATInvocationException {
        try {
            int res = in.read(arg0);
            if (res >= 0) available -= res;
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("SftpFileInputStreamAdaptor", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long arg0) throws GATInvocationException {
        try {
            long res = in.skip(arg0);
            available -= res;
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("SftpFileInputStreamAdaptor", e);
        }
    }
}
