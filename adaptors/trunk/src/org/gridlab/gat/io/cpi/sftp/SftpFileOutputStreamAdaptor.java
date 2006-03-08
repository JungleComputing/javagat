/*
 * Created on Jun 28, 2005
 */
package org.gridlab.gat.io.cpi.sftp;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;

import java.io.OutputStream;
import java.io.IOException;

import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import com.sshtools.j2ssh.sftp.SftpFileOutputStream;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.io.UnsignedInteger32;

/**
 * @author rob
 */
public class SftpFileOutputStreamAdaptor extends FileOutputStreamCpi {
    OutputStream out;

    SftpConnection c;

    boolean newfile;

    // Default permissions is determined by default_permissions ^ umask
    int umask = 0022;

    int default_permissions = 0777;

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
        try {
            out = createOutputStream();
        } catch (Exception e) {
            throw new GATObjectCreationException(
                "sftp file output stream adaptor", e);
        }
    }

    protected OutputStream createOutputStream() throws GATInvocationException {

        String path = location.getPath();
        SftpFileOutputStream sfos;
        FileAttributes attrs;

        try {
            c = SftpFileAdaptor.openConnection(gatContext, preferences,
                location);
            SftpSubsystemClient ssc = c.ssh.openSftpChannel();
            try {
                attrs = c.sftp.stat(path);
                sfos = new SftpFileOutputStream(ssc.openFile(path,
                    SftpSubsystemClient.OPEN_CREATE
                        | SftpSubsystemClient.OPEN_TRUNCATE
                        | SftpSubsystemClient.OPEN_WRITE));
                return sfos;
            } catch (IOException ex) {
                attrs = new FileAttributes();
                newfile = true;
                attrs.setPermissions(new UnsignedInteger32(default_permissions
                    ^ umask));
                sfos = new SftpFileOutputStream(ssc.openFile(path,
                    SftpSubsystemClient.OPEN_CREATE
                        | SftpSubsystemClient.OPEN_WRITE, attrs));
                return sfos;
            }
        } catch (Exception e) {
            throw new GATInvocationException("sftp file outputstream", e);
        }
    }

    /*
     protected void doFlush() throws GATInvocationException {
     String path = location.getPath();

     try {
     c = SftpFileAdaptor.openConnection(
     gatContext, preferences, location
     );

     ByteArrayInputStream in =
     new ByteArrayInputStream(out.toByteArray());
     c.sftp.put(in, path);
     } catch (Exception e) {
     throw new GATInvocationException("sftp file outputstream", e);
     }
     }
     */

    /*
     * (non-Javadoc)
     *
     * @see java.io.OutputStream#close()
     */
    public void close() throws GATInvocationException {
        try {
            out.close();
            if (newfile) {
                c.sftp.chmod(default_permissions ^ umask, location.getPath());
            }
            //doFlush();
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
            throw new GATInvocationException("sftp output stream", e);
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
            throw new GATInvocationException("sftp output stream", e);
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
            throw new GATInvocationException("sftp output stream", e);
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
            throw new GATInvocationException("sftp output stream", e);
        }
    }
}
