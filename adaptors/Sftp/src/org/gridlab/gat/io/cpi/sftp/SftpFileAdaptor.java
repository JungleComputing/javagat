/*
 * Created on Jun 27, 2005
 */
package org.gridlab.gat.io.cpi.sftp;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInterface;
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
@SuppressWarnings("serial")
public class SftpFileAdaptor extends FileCpi {

    protected static Logger logger = Logger.getLogger(SftpFileAdaptor.class);

    public static final int SSH_PORT = 22;

    static boolean USE_CLIENT_CACHING = true;

    private static Hashtable<String, SftpConnection> clienttable = new Hashtable<String, SftpConnection>();

    public SftpFileAdaptor(GATContext gatContext, Preferences preferences,
            URI location) throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("sftp") && !location.isCompatible("file")) {
            throw new AdaptorNotApplicableException("cannot handle this URI");
        }

        USE_CLIENT_CACHING = preferences.containsKey("caching");
        logger.info("caching: " + USE_CLIENT_CACHING);
    }

    private static String getClientKey(URI hostURI) {
        return hostURI.resolveHost();
    }

    private static synchronized SftpConnection getFromCache(String key) {
        SftpConnection client = null;
        if (clienttable.containsKey(key)) {
            client = (SftpConnection) clienttable.remove(key);
        }
        return client;
    }

    private static synchronized boolean putInCache(String key, SftpConnection c) {
        if (!clienttable.containsKey(key)) {
            clienttable.put(key, c);
            return true;
        }
        return false;
    }

    protected static SftpConnection openConnection(GATContext context,
            Preferences preferences, URI location)
            throws GATInvocationException {
        logger.info("open connection, caching: " + USE_CLIENT_CACHING);
        if (!USE_CLIENT_CACHING) {
            return doWorkcreateConnection(context, preferences, location);
        }

        SftpConnection c = null;

        String key = getClientKey(location);
        c = getFromCache(key);

        if (c != null) {
            try {
                // test if the client is still alive
                c.sftp.stat(".");

                if (logger.isDebugEnabled()) {
                    logger.debug("using cached client");
                }
            } catch (Exception except) {
                if (logger.isDebugEnabled()) {
                    logger.debug("could not reuse cached client: " + except);
                    except.printStackTrace();
                }

                c = null;
            }
        }
        if (c == null) {
            c = doWorkcreateConnection(context, preferences, location);
            if (putInCache(location.toString(), c)) {
                logger.info("put sftp connection to " + location.toString()
                        + " in cache!");
            }
        }
        return c;
    }

    protected static SftpConnection doWorkcreateConnection(GATContext context,
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
            throw new GATInvocationException("SftpFileAdaptor", e);
        } catch (CredentialExpiredException e2) {
            throw new GATInvocationException("SftpFileAdaptor", e2);
        }
        SshConnectionProperties connectionProp = new SshConnectionProperties();
        connectionProp.setHost(location.resolveHost());

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
                    if (result == AuthenticationProtocolState.FAILED) {
                        throw new InvalidUsernameOrPasswordException(
                                "Invalid username or password");
                    } else {
                        throw new GATInvocationException(
                                "Unable to authenticate");
                    }
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
        } catch (Exception e) {
            throw new GATInvocationException("SftpFileAdaptor", e);
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
            logger.debug("executing list: " + fixURI(location, null).getPath());
            c.sftp.cd(fixURI(location, null).getPath());
            java.util.List<?> dirList = c.sftp.ls();
            Vector<String> newRes = new Vector<String>();
            for (int i = 0; i < dirList.size(); i++) {
                SftpFile entry = (SftpFile) dirList.get(i);
                if (!entry.getFilename().equals(".")
                        && !entry.getFilename().equals("..")) {
                    newRes.add(entry.getFilename());
                }
            }

            String[] res = new String[newRes.size()];
            for (int i = 0; i < newRes.size(); i++) {
                res[i] = (String) newRes.get(i);
            }
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("SftpFileAdaptor", e);
        } finally {
            closeConnection(c);
        }
    }

    public boolean exists() throws GATInvocationException {
        SftpConnection c = openConnection(gatContext, preferences, location);
        try {
            c.sftp.stat(fixURI(location, null).getPath());
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            closeConnection(c);
        }
    }

    public boolean isDirectory() throws GATInvocationException {
        SftpConnection c = openConnection(gatContext, preferences, fixURI(
                location, null));
        try {
            FileAttributes attr = c.sftp.stat(fixURI(location, null).getPath());

            return attr.isDirectory();
        } catch (IOException e) {
            throw new GATInvocationException("SftpFileAdaptor", e);
        } finally {
            closeConnection(c);
        }
    }

    public boolean isFile() throws GATInvocationException {
        SftpConnection c = openConnection(gatContext, preferences, location);
        try {
            FileAttributes attr = c.sftp.stat(fixURI(location, null).getPath());

            return attr.isFile();
        } catch (IOException e) {
            throw new GATInvocationException("SftpFileAdaptor", e);
        } finally {
            closeConnection(c);
        }
    }

    public long length() throws GATInvocationException {
        SftpConnection c = openConnection(gatContext, preferences, location);
        try {
            FileAttributes attr = c.sftp.stat(fixURI(location, null).getPath());

            return attr.getSize().longValue();
        } catch (IOException e) {
            throw new GATInvocationException("SftpFileAdaptor", e);
        } finally {
            closeConnection(c);
        }
    }

    public boolean mkdir() throws GATInvocationException {
        SftpConnection c = openConnection(gatContext, preferences, location);
        try {
            c.sftp.mkdir(fixURI(location, null).getPath());
        } catch (IOException e) {
            return false;
        } finally {
            closeConnection(c);
        }

        return true;
    }

    // public boolean mkdirs() throws GATInvocationException {
    //        
    //        
    // SftpConnection c = openConnection(gatContext, preferences, location);
    // String dir = getPath();
    // java.util.StringTokenizer tokens = new java.util.StringTokenizer(dir,
    // "/");
    // String path = dir.startsWith("/") ? "/" : "";
    //
    // while (tokens.hasMoreElements()) {
    // path += (String) tokens.nextElement();
    // try {
    // c.sftp.stat(path);
    // } catch (IOException ex) {
    // /*
    // * the directory does not exist we create it
    // */
    // try {
    // c.sftp.mkdir(path);
    // } catch (IOException ex2) {
    // /*
    // * we can't create it
    // */
    // closeConnection(c);
    // return false;
    // }
    // }
    //
    // path += "/";
    // }
    //
    // closeConnection(c);
    //
    // return true;
    // }

    public boolean delete() throws GATInvocationException {
        SftpConnection c = openConnection(gatContext, preferences, location);
        try {
            c.sftp.rm(fixURI(location, null).getPath());
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
        if (determineIsDirectory()) {
            copyDirectory(gatContext, preferences, toURI(), dest);
            return;
        }

        if (dest.refersToLocalHost()) {
            if (logger.isDebugEnabled()) {
                logger.debug("sftp file: copy remote to local");
            }

            copyToLocal(fixURI(toURI(), null), fixURI(dest, null));

            return;
        }

        if (toURI().refersToLocalHost()) {
            if (logger.isDebugEnabled()) {
                logger.debug("sftp file: copy local to remote");
            }

            copyToRemote(fixURI(toURI(), null), fixURI(dest, null));

            return;
        }

        // source is remote, dest is remote.
        throw new GATInvocationException("sftp: cannot do third party copy");
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
            if (new java.io.File(destPath).isDirectory()
                    || (destPath.endsWith(File.separator))) {
                String sourcePath = src.getPath();
                if (sourcePath.endsWith(File.separator)) {
                    sourcePath = sourcePath.substring(0,
                            sourcePath.length() - 1);
                }
                if (sourcePath.length() > 0) {
                    int start = sourcePath.lastIndexOf(File.separator) + 1;
                    String separator = "";
                    if (!destPath.endsWith(File.separator)) {
                        separator = File.separator;
                    }
                    destPath = destPath + separator
                            + sourcePath.substring(start);
                }
            }
            if (!new java.io.File(destPath).getParentFile().exists()) {
                // only if the preference is set to true, go on else throw an
                // exception
                if (!(preferences.containsKey("file.create") && ((String) preferences
                        .get("file.create")).equalsIgnoreCase("true"))) {
                    throw new GATInvocationException("'"
                            + new java.io.File(destPath).getParentFile()
                                    .getPath() + "' does not exist");
                }

            }
            c.sftp.get(src.getPath(), destPath);
        } catch (IOException e) {
            throw new GATInvocationException("SftpFileAdaptor", e);
        } finally {
            closeConnection(c);
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
            
            if (preferences.containsKey("file.create")) {
                if (((String) preferences.get("file.create"))
                        .equalsIgnoreCase("true")) {
                    FileInterface destFile = GAT.createFile(gatContext, preferences, dest)
                            .getFileInterface();
                    FileInterface destParentFile = destFile.getParentFile()
                            .getFileInterface();
                    destParentFile.mkdirs();
                }
            }

            tmpCon = openConnection(gatContext, preferences, dest);
            tmpCon.sftp.put(srcPath, dest.getPath());

        } catch (Exception e) {
            throw new GATInvocationException("SftpFileAdaptor", e);
        } finally {
            if (tmpCon != null)
                closeConnection(tmpCon);
        }
    }
}
