/*
 * Created on Jun 28, 2005
 */
package org.gridlab.gat.io.cpi.sftp;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;

import java.io.IOException;
import java.io.InputStream;

import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import com.sshtools.j2ssh.sftp.SftpFileInputStream;
import com.sshtools.j2ssh.sftp.SftpFile;

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

        checkName("sftp");

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
            throw new GATObjectCreationException("grid ftp inputstream", e);
        }
    }

    protected InputStream createStream() throws GATInvocationException {
        String path = location.getPath();

        try {
            c = SftpFileAdaptor.openConnection(gatContext, preferences,
                location);

            /*
             // no need to buffer this, it is only writing to memory
             ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024);

             c.sftp.get(path, baos);

             ByteArrayInputStream is =
             new ByteArrayInputStream(baos.toByteArray());

             return is;
             */
            SftpSubsystemClient ssc = c.ssh.openSftpChannel();
            SftpFile sf = ssc.openFile(path, SftpSubsystemClient.OPEN_READ);
            filesize = sf.getAttributes().getSize().longValue();
            available = filesize;
            SftpFileInputStream sfis = new SftpFileInputStream(sf);

            return sfis;

        } catch (Exception e) {
            throw new GATInvocationException("sftp file inputstream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#available()
     */
    public int available() throws GATInvocationException {
        /* 
         try {
         return in.available();
         } catch (IOException e) {
         throw new GATInvocationException("GlobusFileInputStream", e);
         }
         */
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
            throw new GATInvocationException("GlobusFileInputStream", e);
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
            int res = in.read();
            if (res > -1) available--;
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("GlobusFileInputStream", e);
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
            available -= res;
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("GlobusFileInputStream", e);
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
            available -= res;
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("GlobusFileInputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#reset()
     */
    public synchronized void reset() throws GATInvocationException {
        try {
            available = filesize;
            in.reset();
        } catch (IOException e) {
            throw new GATInvocationException("GlobusFileInputStream", e);
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
            throw new GATInvocationException("GlobusFileInputStream", e);
        }
    }
}
