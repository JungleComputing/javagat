/*
 * Created on Mar 21, 2007 by rob
 */
package org.gridlab.gat.io.cpi.sftpTrilead;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInterface;
import org.gridlab.gat.io.cpi.FileCpi;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SFTPException;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3DirectoryEntry;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;
import com.trilead.ssh2.ServerHostKeyVerifier;
import com.trilead.ssh2.sftp.ErrorCodes;

@SuppressWarnings("serial")
public class SftpTrileadFileAdaptor extends FileCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("createNewFile", true);
        capabilities.put("copy", true);
        capabilities.put("delete", true);
        capabilities.put("exists", true);
        capabilities.put("isDirectory", true);
        capabilities.put("isFile", true);
        capabilities.put("length", true);
        capabilities.put("list", true);
        capabilities.put("mkdir", true);
        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = FileCpi.getSupportedPreferences();

        preferences.put("sftptrilead.strictHostKeyChecking", "false");
        preferences.put("sftptrilead.noHostKeyChecking", "true");
        return preferences;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "sftptrilead", "sftp", "file"};
    }
    
    protected static Logger logger = LoggerFactory
            .getLogger(SftpTrileadFileAdaptor.class);

    static final int SSH_PORT = 22;

    static final boolean USE_CLIENT_CACHING = true;
    
    private final SftpTrileadHostVerifier verifier;

    private static Hashtable<String, SftpTrileadConnection> clienttable = new Hashtable<String, SftpTrileadConnection>();

    public SftpTrileadFileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {

        super(gatContext, location);
        
        Preferences p = gatContext.getPreferences();
        boolean noHostKeyChecking = ((String) p.get("sftptrilead.noHostKeyChecking", "true"))
                .equalsIgnoreCase("true");
        boolean strictHostKeyChecking = ((String) p.get("sftptrilead.strictHostKeyChecking", "false"))
                .equalsIgnoreCase("true");
        
        verifier = new SftpTrileadHostVerifier(false, strictHostKeyChecking, noHostKeyChecking);
    }

    private static String getClientKey(URI hostURI) {
        return hostURI.resolveHost() + ":" + hostURI.getPort(SSH_PORT);
    }

    private static synchronized SftpTrileadConnection getFromCache(String key) {
        SftpTrileadConnection client = null;
        if (clienttable.containsKey(key)) {
            client = clienttable.remove(key);
        }
        return client;
    }

    private static synchronized boolean putInCache(String key,
            SftpTrileadConnection c) {
        if (!clienttable.containsKey(key)) {
            clienttable.put(key, c);
            return true;
        }
        return false;
    }

    public static SftpTrileadConnection openConnection(GATContext gatContext,
            URI location, SftpTrileadHostVerifier verifier) throws GATInvocationException {
        if (!USE_CLIENT_CACHING) {
            return doWorkcreateConnection(gatContext, location,  verifier);
        }

        SftpTrileadConnection c = null;

        String key = getClientKey(location);
        c = getFromCache(key);

        if (c != null) {
            try {
                // test if the client is still alive
                c.sftpClient.stat(".");

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
            c = doWorkcreateConnection(gatContext, location, verifier);
        }

        return c;
    }

    private static synchronized SftpTrileadConnection doWorkcreateConnection(
            GATContext gatContext, URI location, ServerHostKeyVerifier verifier) throws GATInvocationException {
        SftpTrileadConnection res = new SftpTrileadConnection();
        res.remoteMachine = location;
        int port = location.getPort();
        if (port == -1) {
            port = SSH_PORT;
        }
        try {
            res.userInfo = SftpTrileadSecurityUtils.getSftpCredential(
                    gatContext, "sftpTrilead", location, port);
        } catch (CouldNotInitializeCredentialException e) {
            throw new GATInvocationException("sftpTrilead", e);
        } catch (CredentialExpiredException e2) {
            throw new GATInvocationException("sftpTrilead", e2);
        }

        res.connection = new Connection(location.resolveHost(), port);

        try {
            res.connection.connect(verifier);
        } catch (IOException e) {
            throw new GATInvocationException("sftpTrilead", e);
        }
        boolean authenticated = false;
        try {
            if (res.userInfo.password != null
                    && res.connection.isAuthMethodAvailable(
                            res.userInfo.username, "password")) {
                authenticated = res.connection.authenticateWithPassword(
                        res.userInfo.username, res.userInfo.password);
            }
        } catch (IOException e) {
            // ignored
        }
        if (!authenticated) {
            // try key-based authentication
            try {
                authenticated = res.connection.authenticateWithPublicKey(
                        res.userInfo.username, res.userInfo.privateKey,
                        res.userInfo.password);
            } catch (IOException e) {
                res.connection.close();
                throw new GATInvocationException("sftpTrilead", e);
            }
        }
        if (!authenticated)
            throw new GATInvocationException("Unable to authenticate");

        try {
            res.sftpClient = new SFTPv3Client(res.connection);
        } catch (IOException e) {
            res.connection.close();
            throw new GATInvocationException("sftpTrilead", e);
        }

        return res;
    }

    public static void closeConnection(SftpTrileadConnection c)
            throws GATInvocationException {
        if (!USE_CLIENT_CACHING) {
            doWorkCloseConnection(c);
            return;
        }

        String key = getClientKey(c.remoteMachine);

        if (!putInCache(key, c)) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("closing client");
                }

                doWorkCloseConnection(c);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("end of sftpTrilead adaptor, closing client, got exception (ignoring): "
                                    + e);
                }

                // ignore
            }
        }
    }

    private static void doWorkCloseConnection(SftpTrileadConnection c) {
        c.sftpClient.close();
        c.sftpClient = null;
        c.connection.close();
        c.connection = null;
    }

    public boolean mkdir() throws GATInvocationException {
        SftpTrileadConnection c = openConnection(gatContext, location, verifier);
        try {
            c.sftpClient.mkdir(fixURI(location, null).getPath(), 0700);
        } catch (IOException e) {
            return false;
        } finally {
            closeConnection(c);
        }
        return true;
    }

    public boolean createNewFile() throws GATInvocationException {
        SftpTrileadConnection c = openConnection(gatContext, location, verifier);
        try {
            c.sftpClient.stat(fixURI(location, null).getPath());
            closeConnection(c);
            return false;   // stat did not give an exception --> already exists.
        } catch (SFTPException x) {
            if (x.getServerErrorCode() == ErrorCodes.SSH_FX_NO_SUCH_FILE) {
            } else {
                closeConnection(c);
                throw new GATInvocationException("sftpTrilead", x);
            }
        } catch (IOException e) {
            closeConnection(c);
            throw new GATInvocationException("sftpTrilead", e);
        }
        try {
            SFTPv3FileHandle handle = c.sftpClient.createFileTruncate(location.getPath());
            c.sftpClient.closeFile(handle);
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
     * @see org.gridlab.gat.io.cpi.FileCpi#isDirectory()
     */
    public boolean isDirectory() throws GATInvocationException {
        if (!exists()) {
            return false;
        }
        SftpTrileadConnection c = openConnection(gatContext, location, verifier);
        try {
            SFTPv3FileAttributes attr = c.sftpClient
                    .stat(fixURI(location, null).getPath());
            return attr.isDirectory();
        } catch (IOException e) {
            throw new GATInvocationException("sftpTrilead", e);
        } finally {
            closeConnection(c);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#delete()
     */
    public boolean delete() throws GATInvocationException {
        SftpTrileadConnection c = openConnection(gatContext, location, verifier);

        try {
            SFTPv3FileAttributes attr = c.sftpClient
                    .stat(fixURI(location, null).getPath());
            if (attr.isDirectory()) {
                c.sftpClient.rmdir(fixURI(location, null).getPath());
            } else {
                c.sftpClient.rm(fixURI(location, null).getPath());
            }
        } catch (IOException e) {
            if (e instanceof SFTPException) {
                if (((SFTPException) e).getServerErrorCode() == ErrorCodes.SSH_FX_NO_SUCH_FILE) {
                     return false;
                }
            }
            throw new GATInvocationException("sftpTrilead", e);
        } finally {
            closeConnection(c);
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#exists()
     */
    public boolean exists() throws GATInvocationException {
        SftpTrileadConnection c = openConnection(gatContext, location, verifier);
        try {
            c.sftpClient.stat(fixURI(location, null).getPath());
        } catch (SFTPException x) {
            if (x.getServerErrorCode() == ErrorCodes.SSH_FX_NO_SUCH_FILE) {
                return false;
            } else {
                throw new GATInvocationException("sftpTrilead", x);
            }
        } catch (IOException e) {
            throw new GATInvocationException("sftpTrilead", e);
        } finally {
            closeConnection(c);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#list()
     */
    public String[] list() throws GATInvocationException {
        SftpTrileadConnection c = openConnection(gatContext, location, verifier);
        try {
            SFTPv3FileAttributes attr = c.sftpClient
                    .stat(fixURI(location, null).getPath());
            if (!attr.isDirectory()) {
                return null;
            }

            Vector<?> result = c.sftpClient
                    .ls(fixURI(location, null).getPath());
            Vector<String> newRes = new Vector<String>();
            for (int i = 0; i < result.size(); i++) {
                SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) result
                        .get(i);
                if (!entry.filename.equals(".") && !entry.filename.equals("..")) {
                    newRes.add(entry.filename);
                }
            }

            String[] res = new String[newRes.size()];
            for (int i = 0; i < newRes.size(); i++) {
                res[i] = newRes.get(i);
            }
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("sftpTrilead", e);
        } finally {
            closeConnection(c);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#isFile()
     */
    public boolean isFile() throws GATInvocationException {
        if (!exists()) {
            return false;
        }
        SftpTrileadConnection c = openConnection(gatContext, location, verifier);
        try {
            SFTPv3FileAttributes attr = c.sftpClient
                    .stat(fixURI(location, null).getPath());
            return attr.isRegularFile();
        } catch (IOException e) {
            throw new GATInvocationException("sftpTrilead", e);
        } finally {
            closeConnection(c);
        }
    }
    
    public void move(URI dest) throws GATInvocationException {
        URI uri = toURI();
        if (logger.isDebugEnabled()) {
            logger.debug("move " + uri + " " + dest);
        }
        if (! uri.refersToLocalHost() && ! dest.refersToLocalHost()) {
            if (uri.getScheme().equals(dest.getScheme()) &&
                    uri.getAuthority().equals(dest.getAuthority())) {
                try {
                    File destFile = GAT.createFile(gatContext, dest);
                    if (destFile.isDirectory()) {
                        dest = dest.setPath(dest.getPath() + "/" + getName());
                    }
                } catch(Throwable e) {
                    throw new GATInvocationException("move", e);
                }
                SftpTrileadConnection c = openConnection(gatContext, location, verifier);
                try {
                    c.sftpClient.mv(getPath(), dest.getPath());
                } catch(Throwable e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("move: got exception", e);
                    }
                    throw new GATInvocationException("move", e);
                } finally {
                    closeConnection(c);
                }
            }
        }
        
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#length()
     */
    public long length() throws GATInvocationException {
        SftpTrileadConnection c = openConnection(gatContext, location, verifier);
        try {
            SFTPv3FileAttributes attr = c.sftpClient
                    .stat(fixURI(location, null).getPath());
            return attr.size.longValue();
        } catch (IOException e) {
            throw new GATInvocationException("sftpTrilead", e);
        } finally {
            closeConnection(c);
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
            throw new GATInvocationException(
                    "sftpTrilead cannot copy local files");
        }

        // create a seperate file object to determine whether the source
        // is a directory. This is needed, because the source might be a local
        // file, and sftp might not be installed locally.
        // This goes wrong for local -> remote copies.
        if (determineIsDirectory()) {
            copyDirectory(gatContext, null, toURI(), dest);
            return;
        }

        if (dest.refersToLocalHost()) {
            if (logger.isDebugEnabled()) {
                logger.debug("sftpTrilead file: copy remote to local");
            }

            copyToLocal(fixURI(toURI(), null), fixURI(dest, null));

            return;
        }

        if (toURI().refersToLocalHost()) {
            if (logger.isDebugEnabled()) {
                logger.debug("sftpTrilead file: copy local to remote");
            }

            copyToRemote(fixURI(toURI(), null), fixURI(dest, null));

            return;
        }

        // source is remote, dest is remote.
        throw new GATInvocationException("sftpNew: cannot do third party copy");
    }

    protected void copyToLocal(URI src, URI dest) throws GATInvocationException {
        BufferedOutputStream outBuf = null;
        SFTPv3FileHandle handle = null;
        SftpTrileadConnection c = null;

        // copy from a remote machine to the local machine
        try {
            // Create destination file
            String destPath = dest.getPath();
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
            java.io.File destinationFile = new java.io.File(destPath);

            if (gatContext.getPreferences().containsKey("file.create")) {
                if (((String) gatContext.getPreferences().get("file.create"))
                        .equalsIgnoreCase("true")) {
                    if (destinationFile.getParentFile() != null) {
                        destinationFile.getParentFile().mkdirs();
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("creating local file " + destinationFile);
            }
            destinationFile.createNewFile();
            FileOutputStream out = new FileOutputStream(destinationFile);
            outBuf = new BufferedOutputStream(out);

            long length = length();
            c = openConnection(gatContext, location, verifier);
            handle = c.sftpClient.openFileRO(src.getPath());

            long bytesWritten = 0;
            byte[] buf = new byte[32000];

            while (bytesWritten != length) {
                int len = c.sftpClient.read(handle, bytesWritten, buf, 0,
                        buf.length);
                outBuf.write(buf, 0, len);
                bytesWritten += len;
            }
        } catch (IOException e) {
            throw new GATInvocationException("sftpTrilead", e);
        } finally {
            try {
                if (outBuf != null) {
                    outBuf.close();
                }
            } catch (IOException e) {
                throw new GATInvocationException("sftpTrilead", e);
            }

            try {
                if (handle != null && c != null) {
                    c.sftpClient.closeFile(handle);
                }
            } catch (IOException e) {
                throw new GATInvocationException("sftpTrilead", e);
            }

            if (c != null) {
                closeConnection(c);
            }
        }
    }

    protected void copyToRemote(URI src, URI dest)
            throws GATInvocationException {

        SFTPv3FileHandle handle = null;
        BufferedInputStream inBuf = null;

        SftpTrileadConnection c = null;

        // copy from the local machine to a remote machine.
        try {
            FileInputStream in = new FileInputStream(src.getPath());
            inBuf = new BufferedInputStream(in);
            long length = new java.io.File(src.getPath()).length();
            c = openConnection(gatContext, dest, verifier);
            String destPath = dest.getPath();
            FileInterface destFile = GAT.createFile(gatContext, dest)
                    .getFileInterface();
            if ((destFile.exists() && destFile.isDirectory())
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

            if (gatContext.getPreferences().containsKey("file.create")) {
                if (((String) gatContext.getPreferences().get("file.create"))
                        .equalsIgnoreCase("true")) {
                    destFile = GAT.createFile(gatContext,
                            dest.toString().replace(dest.getPath(), destPath))
                            .getFileInterface();
                    org.gridlab.gat.io.File destinationParentFile = destFile
                            .getParentFile();
                    if (destinationParentFile != null) {
                        destinationParentFile.getFileInterface().mkdirs();
                    }
                }
            }

            handle = c.sftpClient.createFileTruncate(destPath);

            long bytesWritten = 0;
            byte[] buf = new byte[32000];

            while (bytesWritten != length) {
                int len = inBuf.read(buf, 0, buf.length);
                c.sftpClient.write(handle, bytesWritten, buf, 0, len);
                bytesWritten += len;
            }
        } catch (Exception e) {
            throw new GATInvocationException("sftpTrilead", e);
        } finally {
            try {
                if (inBuf != null) {
                    inBuf.close();
                }
            } catch (IOException e) {
                throw new GATInvocationException("sftpTrilead", e);
            }

            try {
                if (handle != null && c != null) {
                    c.sftpClient.closeFile(handle);
                }
            } catch (IOException e) {
                throw new GATInvocationException("sftpTrilead", e);
            }

            if (c != null) {
                closeConnection(c);
            }
        }
    }
}
