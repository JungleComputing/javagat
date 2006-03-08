/*
 * Created on Jun 27, 2005
 */
package org.gridlab.gat.io.cpi.sftp;

import java.io.IOException;
import java.util.Iterator;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

/**
 * @author rob
 */
public class SftpFileAdaptor extends FileCpi {

    public static final int SSH_PORT = 22;

    SftpConnection c;

    public SftpFileAdaptor(GATContext gatContext, Preferences preferences,
            URI location) throws GATObjectCreationException {
        super(gatContext, preferences, location);

        checkName("sftp");

        if (!location.isCompatible("sftp") && !location.isCompatible("file")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }

        c = openConnection(gatContext, preferences, location);
    }


    protected static SftpConnection openConnection(GATContext context,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        SftpConnection res = new SftpConnection();

        res.ssh = new SshClient();
        HostKeyVerification hkv = new HostKeyVerification() {
            public boolean verifyHost(String name, SshPublicKey key) {
                return true;
            }
        };

        SftpUserInfo info;

        try {
            info = SFTPSecurityUtils.getSftpCredential(context, preferences, "sftp", location, SSH_PORT);
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("sftp", e);
        }

        SshConnectionProperties connectionProp = new SshConnectionProperties();
        connectionProp.setHost(location.getHost());
        int port = location.getPort();
        if (port == -1)
            port = SSH_PORT;
        connectionProp.setPort(port);
        try {
            res.ssh.connect(connectionProp, hkv);

            if (info.password != null) {
                PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
                pwd.setUsername(info.username);
                pwd.setPassword(info.password);
                int result = res.ssh.authenticate(pwd);
                if (result != AuthenticationProtocolState.COMPLETE) {
                    throw new GATObjectCreationException(
                            "Unable to authenticate");
                }
            } else { // no password, try key-based authentication
                PublicKeyAuthenticationClient pkey = new PublicKeyAuthenticationClient();
                pkey.setKey(info.privateKey);
                pkey.setUsername(info.username);

                int result = res.ssh.authenticate(pkey);
                if (result != AuthenticationProtocolState.COMPLETE) {
                    throw new GATObjectCreationException(
                            "Unable to authenticate");
                }
            }
            res.sftp = res.ssh.openSftpClient();
        } catch (IOException e) {
            throw new GATObjectCreationException("sftp", e);
        }

        return res;
    }

    public boolean closeConnection() {
        c.sftp = null;
        c.ssh.disconnect();
        return true;
    }

    public String[] list() throws GATInvocationException {
        try {
            c.sftp.cd(location.getPath());
            java.util.List dirList = c.sftp.ls();

            String[] children = new String[dirList.size()];
            int index = 0;
            for (Iterator i = dirList.iterator(); i.hasNext();) {
                children[index] = ((SftpFile) i.next()).getFilename();
                index++;
            }

            return children;
        } catch (IOException e) {
            throw new GATInvocationException("sftp", e);
        }
    }

    public boolean exists() throws GATInvocationException {
        try {
            c.sftp.stat(location.getPath());
            return true;
        } catch (IOException e) {
            throw new GATInvocationException("sftp", e);
        }
    }

    public boolean isDirectory() throws GATInvocationException {
        try {
            FileAttributes attr = c.sftp.stat(location.getPath());
            return attr.isDirectory();
        } catch (IOException e) {
            throw new GATInvocationException("sftp", e);
        }
    }
    
    public boolean mkdir() throws GATInvocationException {
        try {
            c.sftp.mkdir(getPath());
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public boolean mkdirs() throws GATInvocationException {
        c.sftp.mkdirs(getPath());
        return true;
    }

    public boolean delete() throws GATInvocationException {
        try {
            c.sftp.rm(getPath());
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#copy(java.net.URI)
     */
    public void copy(URI dest) throws GATInvocationException {
        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        if (dest.isLocal() && (toURI().isLocal())) {
            throw new GATInvocationException("sftp cannot copy local files");
        }

        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        /*
         if (dest.refersToLocalHost() && (toURI().refersToLocalHost())) {
         throw new GATInvocationException("sftp cannot copy local files");
         }
         */

        // create a seperate file object to determine whether the source
        // is a directory. This is needed, because the source might be a local
        // file, and sftp might not be installed locally.
        // This goes wrong for local -> remote copies.
        try {
            File f = GAT.createFile(gatContext, preferences, toURI());
            if (f.isDirectory()) {
                copyDirectory(gatContext, preferences, toURI(), dest);
                return;
            }
        } catch (Exception e) {
            throw new GATInvocationException("sftp", e);
        }

        if (dest.isLocal()) {
            if (GATEngine.DEBUG) {
                System.err.println("sftp file: copy remote to local");
            }
            copyToLocal(toURI(), dest);
            return;
        }

        if (toURI().isLocal()) {
            if (GATEngine.DEBUG) {
                System.err.println("sftp file: copy local to remote");
            }
            copyToRemote(toURI(), dest);
            return;
        }

        // source is remote, dest is remote.
        if (GATEngine.DEBUG) {
            System.err.println("sftp file: copy remote to remote");
        }
        copyThirdParty(toURI(), dest);
    }

    protected void copyToLocal(URI src, URI dest) throws GATInvocationException {
        // copy from a remote machine to the local machine
        try {
            c.sftp.get(src.getPath(), dest.getPath());
        } catch (IOException e) {
            throw new GATInvocationException("sftp", e);
        }
    }

    // first try efficient 3rd party transfer.
    // If that fails, try copying using temp file.
    protected void copyThirdParty(URI src, URI dest)
            throws GATInvocationException {
        try {
            // use a local tmp file.
            java.io.File tmp = null;
            tmp = java.io.File.createTempFile("GATsFTP", ".tmp");

            copyToLocal(toURI(), new URI(tmp.toURI()));
            copyToRemote(new URI(tmp.toURI()), dest);
        } catch (Exception e2) {
            throw new GATInvocationException("Globus file", e2);
        }
    }

    protected void copyToRemote(URI src, URI dest)
            throws GATInvocationException {
        // copy from the local machine to a remote machine.
        try {
            c.sftp.put(src.getPath(), dest.getPath());
        } catch (IOException e) {
            throw new GATInvocationException("sftp", e);
        }
    }
}