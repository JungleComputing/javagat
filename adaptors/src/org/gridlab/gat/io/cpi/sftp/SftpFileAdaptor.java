/*
 * Created on Jun 27, 2005
 */
package org.gridlab.gat.io.cpi.sftp;

import java.io.IOException;
import java.util.Iterator;

import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredExeption;
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

    public SftpFileAdaptor(GATContext gatContext, Preferences preferences,
            URI location) throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("sftp") && !location.isCompatible("file")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }
    }

    protected static SftpConnection openConnection(GATContext context,
            Preferences preferences, URI location)
            throws GATInvocationException {
        SftpConnection res = new SftpConnection();

        res.ssh = new SshClient();

        HostKeyVerification hkv = new HostKeyVerification() {
            public boolean verifyHost(String name, SshPublicKey key) {
                return true;
            }
        };

        SftpUserInfo info;

        try {
            info = SftpSecurityUtils.getSftpCredential(context, preferences,
                "sftp", location, SSH_PORT);
        } catch (CouldNotInitializeCredentialException e) {
            throw new GATInvocationException("sftp", e);
        } catch (CredentialExpiredExeption e2) {
            throw new GATInvocationException("sftp", e2);
        }

        SshConnectionProperties connectionProp = new SshConnectionProperties();
        connectionProp.setHost(location.getHost());

        int port = location.getPort();

        if (port == -1) {
            port = SSH_PORT;
        }

        connectionProp.setPort(port);

        try {
            res.ssh.connect(connectionProp, hkv);

            if (info.password != null) {
                PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
                pwd.setUsername(info.username);
                pwd.setPassword(info.password);

                int result = res.ssh.authenticate(pwd);

                if (result != AuthenticationProtocolState.COMPLETE) {
                    throw new GATInvocationException("Unable to authenticate");
                }
            } else { // no password, try key-based authentication

                PublicKeyAuthenticationClient pkey = new PublicKeyAuthenticationClient();
                pkey.setKey(info.privateKey);
                pkey.setUsername(info.username);

                int result = res.ssh.authenticate(pkey);

                if (result != AuthenticationProtocolState.COMPLETE) {
                    throw new GATInvocationException("Unable to authenticate");
                }
            }

            res.sftp = res.ssh.openSftpClient();
        } catch (IOException e) {
            throw new GATInvocationException("sftp", e);
        }

        return res;
    }

    public static void closeConnection(SftpConnection c) {
        c.sftp = null;
        c.ssh.disconnect();
    }

    public String[] list() throws GATInvocationException {

        SftpConnection c = openConnection(gatContext, preferences, location);

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
        } finally {
            closeConnection(c);
        }
    }

    public boolean exists() throws GATInvocationException {
        SftpConnection c = openConnection(gatContext, preferences, location);
        try {
            c.sftp.stat(location.getPath());
            return true;
        } catch (IOException e) {
            //added by Ana
            return false;
        } finally {
            closeConnection(c);
        }
    }

    public boolean isDirectory() throws GATInvocationException {
        SftpConnection c = openConnection(gatContext, preferences, location);
        try {
            FileAttributes attr = c.sftp.stat(location.getPath());

            return attr.isDirectory();
        } catch (IOException e) {
            throw new GATInvocationException("sftp", e);
        } finally {
            closeConnection(c);
        }
    }

    public long length() throws GATInvocationException {
        SftpConnection c = openConnection(gatContext, preferences, location);
        try {
            FileAttributes attr = c.sftp.stat(location.getPath());

            return attr.getSize().longValue();
        } catch (IOException e) {
            throw new GATInvocationException("sftp", e);
        } finally {
            closeConnection(c);
        }
    }

    public boolean mkdir() throws GATInvocationException {
        SftpConnection c = openConnection(gatContext, preferences, location);
        try {
            c.sftp.mkdir(getPath());
        } catch (IOException e) {
            return false;
        } finally {
            closeConnection(c);
        }

        return true;
    }

    public boolean mkdirs() throws GATInvocationException {
        SftpConnection c = openConnection(gatContext, preferences, location);
        String dir = getPath();
        java.util.StringTokenizer tokens = new java.util.StringTokenizer(dir,
            "/");
        String path = dir.startsWith("/") ? "/" : "";

        while (tokens.hasMoreElements()) {
            path += (String) tokens.nextElement();
            try {
                c.sftp.stat(path);
            } catch (IOException ex) {
                /*the directory does not exist
                 * we create it
                 */
                try {
                    c.sftp.mkdir(path);
                } catch (IOException ex2) {
                    /* we can't create it
                     */
                    closeConnection(c);
                    return false;
                }
            }

            path += "/";
        }

        closeConnection(c);

        return true;
    }

    public boolean delete() throws GATInvocationException {
        SftpConnection c = openConnection(gatContext, preferences, location);
        try {
            c.sftp.rm(getPath());
        } catch (IOException e) {
            return false;
        } finally {
            closeConnection(c);
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
        if (dest.refersToLocalHost() && (toURI().refersToLocalHost())) {
            throw new GATInvocationException("sftp cannot copy local files");
        }

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

        if (dest.refersToLocalHost()) {
            if (GATEngine.DEBUG) {
                System.err.println("sftp file: copy remote to local");
            }

            copyToLocal(toURI(), dest);

            return;
        }

        if (toURI().refersToLocalHost()) {
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
        SftpConnection c = openConnection(gatContext, preferences, location);
        // copy from a remote machine to the local machine
        try {
            // if it is a relative path, we must make it an absolute path.
            // the sftp library uses paths relative to the user's home dir.
            String destPath = dest.getPath();

            if (!destPath.startsWith("/")) {
                java.io.File f = new java.io.File(destPath);
                destPath = f.getCanonicalPath();
            }

            c.sftp.get(src.getPath(), destPath);
        } catch (IOException e) {
            throw new GATInvocationException("sftp", e);
        } finally {
            closeConnection(c);
        }
    }

    // Try copying using temp file.
    protected void copyThirdParty(URI src, URI dest)
            throws GATInvocationException {
        java.io.File tmp = null;
        SftpConnection tmpCon = null;

        try {
            // use a local tmp file.
            tmp = java.io.File.createTempFile("GAT_SFTP_", ".tmp");

            URI tmpURI = new URI(tmp.getCanonicalPath()); // convert to GAT URI

            copyToLocal(src, tmpURI);

            tmpCon = openConnection(gatContext, preferences, dest);
            tmpCon.sftp.put(tmpURI.getPath(), dest.getPath());
        } catch (Exception e2) {
            throw new GATInvocationException("sftp", e2);
        } finally {
            tmp.delete();
            if (tmpCon != null) closeConnection(tmpCon);
        }
    }

    protected void copyToRemote(URI src, URI dest)
            throws GATInvocationException {

        SftpConnection tmpCon = null;

        // copy from the local machine to a remote machine.
        try {
            // if it is a relative path, we must make it an absolute path.
            // the sftp library uses paths relative to the user's home dir.
            String srcPath = src.getPath();

            if (!srcPath.startsWith("/")) {
                java.io.File f = new java.io.File(srcPath);
                srcPath = f.getCanonicalPath();
            }

            tmpCon = openConnection(gatContext, preferences, dest);
            tmpCon.sftp.put(srcPath, dest.getPath());

        } catch (Exception e) {
            throw new GATInvocationException("sftp", e);
        } finally {
            if (tmpCon != null) closeConnection(tmpCon);
        }
    }
}
