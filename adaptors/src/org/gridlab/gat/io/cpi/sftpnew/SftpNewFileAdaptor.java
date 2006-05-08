package org.gridlab.gat.io.cpi.sftpnew;

import java.util.Hashtable;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.ssh.SSHSecurityUtils;
import org.gridlab.gat.io.cpi.ssh.SshUserInfo;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;

public class SftpNewFileAdaptor extends FileCpi {
    public static final int SSH_PORT = 22;

    /**
     * @param gatContext
     * @param preferences
     * @param location
     */
    public SftpNewFileAdaptor(GATContext gatContext, Preferences preferences,
            URI location) throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("sftp") && !location.isCompatible("file")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }
    }

    protected static SftpConnection createChannel(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATInvocationException {
        JSch jsch = new JSch();
        Hashtable configJsch = new Hashtable();
        configJsch.put("StrictHostKeyChecking", "no");
        JSch.setConfig(configJsch);

        SshUserInfo sui = null;

        try {
            sui = SSHSecurityUtils.getSshCredential(gatContext, preferences,
                "ssh", location, SSH_PORT);
        } catch (Exception e) {
            System.out.println("SshFileAdaptor: failed to retrieve credentials"
                + e);
        }

        if (sui == null) {
            throw new GATInvocationException(
                "Unable to retrieve user info for authentication");
        }

        try {
            if (sui.getPrivateKeyfile() != null) {
                jsch.addIdentity(sui.getPrivateKeyfile());
            }

            if (location.getUserInfo() != null) {
                sui.username = location.getUserInfo();
            }

            Session session = jsch.getSession(sui.username, location.getHost(),
                location.getPort(SSH_PORT));
            session.setUserInfo(sui);
            session.connect();

            Channel c = session.openChannel("sftp");
            c.connect();

            SftpConnection res = new SftpConnection();
            res.channel = (ChannelSftp) c;
            res.session = session;
            res.jsch = jsch;
            res.userInfo = sui;

            return res;
        } catch (JSchException jsche) {
            throw new GATInvocationException(
                "internal error in SftpnewFileAdaptor: " + jsche);
        }
    }

    public static void closeChannel(SftpConnection connection) throws GATInvocationException {
        if (connection.channel != null) {
            try {
                connection.channel.disconnect();
            } catch (Throwable t) { // ignore
            }
        }

        if (connection.session != null) {
            try {
                connection.session.disconnect();
            } catch (Throwable t) { // ignore
            }
        }
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
            throw new GATInvocationException("sftpnew cannot copy local files");
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
            throw new GATInvocationException("sftpnew", e);
        }

        if (dest.refersToLocalHost()) {
            if (GATEngine.DEBUG) {
                System.err.println("sftpnew file: copy remote to local");
            }

            copyToLocal(toURI(), dest);

            return;
        }

        if (toURI().refersToLocalHost()) {
            if (GATEngine.DEBUG) {
                System.err.println("sftpnew file: copy local to remote");
            }

            copyToRemote(toURI(), dest);

            return;
        }

        // source is remote, dest is remote.
        if (GATEngine.DEBUG) {
            System.err.println("sftpnew file: copy remote to remote");
        }

        copyThirdParty(toURI(), dest);
    }

    protected void copyToLocal(URI src, URI dest) throws GATInvocationException {
        SftpConnection c = createChannel(gatContext, preferences, src);
        // copy from a remote machine to the local machine
        try {
            // if it is a relative path, we must make it an absolute path.
            // the sftp library uses paths relative to the user's home dir.
            String destPath = dest.getPath();

            if (!destPath.startsWith("/")) {
                java.io.File f = new java.io.File(destPath);
                destPath = f.getCanonicalPath();
            }

            c.channel.get(src.getPath(), destPath);
            
        } catch (Exception e) {
            throw new GATInvocationException("sftpnew", e);
        } finally {
            closeChannel(c);
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

            tmpCon = createChannel(gatContext, preferences, dest);
            tmpCon.channel.put(tmpURI.getPath(), dest.getPath());
        } catch (Exception e2) {
            throw new GATInvocationException("sftpnew", e2);
        } finally {
            tmp.delete();
            if (tmpCon != null) closeChannel(tmpCon);
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

            tmpCon = createChannel(gatContext, preferences, dest);
            tmpCon.channel.put(srcPath, dest.getPath());

        } catch (Exception e) {
            throw new GATInvocationException("sftpnew", e);
        } finally {
            if (tmpCon != null) closeChannel(tmpCon);
        }
    }

    public long length() throws GATInvocationException {
        SftpConnection c = createChannel(gatContext, preferences, location);
        try {
            SftpATTRS attr = c.channel.lstat(location.getPath());

            return attr.getSize();
        } catch (Exception e) {
            throw new GATInvocationException("sftpnew", e);
        } finally {
            closeChannel(c);
        }
    }

    public boolean isDirectory() throws GATInvocationException {
        SftpConnection c = createChannel(gatContext, preferences, location);
        try {
            SftpATTRS attr = c.channel.lstat(location.getPath());

            return attr.isDir();
        } catch (Exception e) {
            throw new GATInvocationException("sftpnew", e);
        } finally {
            closeChannel(c);
        }
    }
}
