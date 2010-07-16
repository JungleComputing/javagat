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

    public static String getDescription() {
        return "The SftpTrilead File Adaptor implements the File object using the SFTP support of the trilead ssh library. Trilead ssh is an open source full java ssh library. On the server side, the 'scp' program must be in the PATH. Connections with a remote ssh server can be made by using the username + password, username + keyfile, or with only a username, depending on the client and server settings.";
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("createNewFile", true);
        capabilities.put("copy", true);
        capabilities.put("delete", true);
        capabilities.put("exists", true);
        capabilities.put("isDirectory", true);
        capabilities.put("isFile", true);
        capabilities.put("lastModified", true);
        capabilities.put("length", true);
        capabilities.put("list", true);
        capabilities.put("mkdir", true);
        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = FileCpi.getSupportedPreferences();

        preferences.put("sftptrilead.strictHostKeyChecking", "false");
        preferences.put("sftptrilead.noHostKeyChecking", "true");
        preferences.put("sftptrilead.cipher.client2server", "<sftp default>");
        preferences.put("sftptrilead.cipher.server2client", "<sftp default>");
        preferences.put("sftptrilead.tcp.nodelay", "true");
        preferences.put("sftptrilead.connect.timeout", "5000");
        preferences.put("sftptrilead.kex.timeout", "5000");
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

    private boolean haveAttrs;

    private SFTPv3FileAttributes attrs;

    private String[] client2serverCiphers;

    private String[] server2clientCiphers;

    private boolean tcpNoDelay;
    
    public SftpTrileadFileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {

        super(gatContext, location);
        
        Preferences p = gatContext.getPreferences();
        boolean noHostKeyChecking = ((String) p.get("sftptrilead.noHostKeyChecking", "true"))
                .equalsIgnoreCase("true");
        boolean strictHostKeyChecking = ((String) p.get("sftptrilead.strictHostKeyChecking", "false"))
                .equalsIgnoreCase("true");
        String client2serverCipherString = (String) p.get("sftptrilead.cipher.client2server");
        client2serverCiphers = client2serverCipherString == null ? null 
                : client2serverCipherString.split(",");
        String server2clientCipherString = (String) p.get("sftptrilead.cipher.server2client");
        server2clientCiphers = server2clientCipherString == null ? null
                : server2clientCipherString.split(",");
        tcpNoDelay = ((String) p.get("sftptrilead.tcp.nodelay", "true"))
                .equalsIgnoreCase("true");
        
        verifier = new SftpTrileadHostVerifier(false, strictHostKeyChecking, noHostKeyChecking);
    }

    private static String getClientKey(URI hostURI, Preferences prefs) {
        return hostURI.resolveHost() + ":" + hostURI.getPort(SSH_PORT) + "-" + prefs;
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
    
    private SftpTrileadConnection openConnection() throws GATInvocationException {
        return getConnection(gatContext, location, verifier,
                client2serverCiphers, server2clientCiphers, tcpNoDelay);
    }

    public static SftpTrileadConnection getConnection(GATContext gatContext,
            URI location, SftpTrileadHostVerifier verifier,
            String[] client2serverCiphers, String[] server2clientCiphers,
            boolean tcpNoDelay) throws GATInvocationException {
        if (!USE_CLIENT_CACHING) {
            return doWorkcreateConnection(gatContext, location,  verifier,
                    client2serverCiphers, server2clientCiphers, tcpNoDelay);
        }

        SftpTrileadConnection c = null;

        String key = getClientKey(location, gatContext.getPreferences());
        c = getFromCache(key);

        /*
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
        */

        if (c == null) {
            c = doWorkcreateConnection(gatContext, location, verifier,
                    client2serverCiphers, server2clientCiphers, tcpNoDelay);
        }

        return c;
    }

    private static synchronized SftpTrileadConnection doWorkcreateConnection(
            GATContext gatContext, URI location, ServerHostKeyVerifier verifier,
            String[] client2serverCiphers, String[] server2clientCiphers,
            boolean tcpNoDelay) throws GATInvocationException {
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

        int connectTimeout = 5000;
        String connectTimeoutString = (String) gatContext.getPreferences().get(
                "sftptrilead.connect.timeout");
        if (connectTimeoutString != null) {
            try {
                connectTimeout = Integer.parseInt(connectTimeoutString);
            } catch (Throwable t) {
                logger
                .info("'sftptrilead.connect.timeout' set, but could not be parsed: "
                        + t);
            }
        }
        int kexTimeout = 5000;
        String kexTimeoutString = (String) gatContext.getPreferences().get(
        "sftptrilead.kex.timeout");
        if (kexTimeoutString != null) {
            try {
                kexTimeout = Integer.parseInt(kexTimeoutString);
            } catch (Throwable t) {
                logger
                .info("'sftptrilead.kex.timeout' set, but could not be parsed: "
                        + t);
            }
        }
        try {
            res.connection.connect(verifier, connectTimeout, kexTimeout);
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
            if (client2serverCiphers != null) {
                res.connection.setClient2ServerCiphers(client2serverCiphers);
            }
            if (server2clientCiphers != null) {
                res.connection.setServer2ClientCiphers(server2clientCiphers);
            }
            res.connection.setTCPNoDelay(tcpNoDelay);
            res.sftpClient = new SFTPv3Client(res.connection);
        } catch (IOException e) {
            res.connection.close();
            throw new GATInvocationException("sftpTrilead", e);
        }

        return res;
    }

    public static void closeConnection(SftpTrileadConnection c, Preferences prefs)
            throws GATInvocationException {
        if (c == null) {
            return;
        }
        if (!USE_CLIENT_CACHING) {
            doWorkCloseConnection(c);
            return;
        }

        String key = getClientKey(c.remoteMachine, prefs);

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
    
    private SFTPv3FileAttributes getAttrs() throws GATInvocationException {
        if (! haveAttrs) {
            SftpTrileadConnection c = openConnection();
            try {
                attrs = c.sftpClient
                        .stat(fixURI(location, null).getPath());
            } catch (SFTPException x) {
                if (x.getServerErrorCode() == ErrorCodes.SSH_FX_NO_SUCH_FILE) {
                } else {
                    throw new GATInvocationException("sftpTrilead", x);
                }
            } catch (IOException e) {
                throw new GATInvocationException("sftpTrilead", e);
            } finally {
                closeConnection(c, gatContext.getPreferences());
            }
        }
        haveAttrs = true;
        return attrs;        
    }
    

    
    public boolean mkdir() throws GATInvocationException {
        SFTPv3FileAttributes a = getAttrs();
        if (a != null) {
            return false;
        }
        SftpTrileadConnection c = openConnection();
        haveAttrs = false;
        try {
            c.sftpClient.mkdir(fixURI(location, null).getPath(), 0700);
        } catch (IOException e) {
            return false;
        } finally {
            closeConnection(c, gatContext.getPreferences());
        }
        return true;
    }

    public boolean createNewFile() throws GATInvocationException {
        SFTPv3FileAttributes a = getAttrs();
        if (a != null) {
            return false;
        }
        haveAttrs = false;
        SftpTrileadConnection c = openConnection();
        try {
            c.sftpClient.closeFile(c.sftpClient.createFileTruncate(location.getPath()));
        } catch (IOException e) {
            return false;
        } finally {
            closeConnection(c, gatContext.getPreferences());
        }
        return true;
    }
    
    public long lastModified() throws GATInvocationException {
        SFTPv3FileAttributes a = getAttrs();
        if (a == null) {
            return 0L;
        }
        return a.mtime;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#isDirectory()
     */
    public boolean isDirectory() throws GATInvocationException {
        SFTPv3FileAttributes a = getAttrs();
        return (a != null && a.isDirectory());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#delete()
     */
    public boolean delete() throws GATInvocationException {
        SFTPv3FileAttributes a = getAttrs();
        if (a == null) {
            return false;
        }
        
        SftpTrileadConnection c = openConnection();

        try {
            if (a.isDirectory()) {
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
            closeConnection(c, gatContext.getPreferences());
        }
        haveAttrs = false;
        return true;
    }
    
    public void recursivelyDeleteDirectory() throws GATInvocationException {
        SFTPv3FileAttributes a = getAttrs();
        if (! a.isDirectory()) {
            throw new GATInvocationException("Not a directory!");
        }
        SftpTrileadConnection c = openConnection();
        String path = fixURI(location, null).getPath();
        try {
            remove(c.sftpClient, path);
        } catch (IOException e) {
            throw new GATInvocationException("sftpTrilead", e);
        } finally {
            closeConnection(c, gatContext.getPreferences());
        }
    }
    
    private void remove(SFTPv3Client c, String path) throws IOException {
        Vector<?> list = c.ls(path);
        for (int i = 0; i < list.size(); i++) {
            SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) list.get(i);
            if (entry.filename.equals(".") || entry.filename.equals("..")) {
                continue;
            }
            String name = path + "/" + entry.filename;
            SFTPv3FileAttributes a = c.stat(name);
            if (a.isDirectory()) {
                remove(c, name);
            } else {
                c.rm(name);
            }
        }
        c.rmdir(path);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#exists()
     */
    public boolean exists() throws GATInvocationException {
        return getAttrs() != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#list()
     */
    public String[] list() throws GATInvocationException {
        SFTPv3FileAttributes a = getAttrs();
        if (a == null || ! a.isDirectory()) {
            return null;
        }
        SftpTrileadConnection c = openConnection();
        try {
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
            closeConnection(c, gatContext.getPreferences());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#isFile()
     */
    public boolean isFile() throws GATInvocationException {
        SFTPv3FileAttributes a = getAttrs();
        return a != null && a.isRegularFile();
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
                SftpTrileadConnection c = openConnection();
                try {
                    c.sftpClient.mv(getPath(), dest.getPath());
                    haveAttrs = false;
                    return;
                } catch(Throwable e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("move: got exception", e);
                    }
                    throw new GATInvocationException("move", e);
                } finally {
                    closeConnection(c, gatContext.getPreferences());
                }
            }
        }
        if (uri.refersToLocalHost() && ! dest.refersToLocalHost()) {
            if (recognizedScheme(dest.getScheme(), getSupportedSchemes())) {
                super.move(dest);
                return;
            }
        }
        if (! uri.refersToLocalHost() && dest.refersToLocalHost()) {
            super.move(dest);
            return;
        }
        throw new GATInvocationException("sftptrilead: cannot do third party move");
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#length()
     */
    public long length() throws GATInvocationException {
        SFTPv3FileAttributes a = getAttrs();
        if (a == null) {
            return -1;
        }
        return a.size.longValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#copy(java.net.URI)
     */
    public void copy(URI dest) throws GATInvocationException {
        URI uri = toURI();
        if (uri.refersToLocalHost()) {
            // We don't have to handle the local case, the GAT engine will select
            // the local adaptor.
            if (dest.refersToLocalHost()) {
                throw new GATInvocationException(
                        "sftpTrilead cannot copy local files");
            }

            // create a seperate file object to determine whether the source
            // is a directory. This is needed, because the source might be a local
            // file, and sftp might not be installed locally.
            // This goes wrong for local -> remote copies.
            if (determineIsDirectory()) {
                copyDirectory(gatContext, null, uri, dest);
                return;
            }
            if (recognizedScheme(dest.getScheme(), getSupportedSchemes())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("sftpTrilead file: copy local to remote");
                }
                copyToRemote(fixURI(uri, null), fixURI(dest, null));

                return;
            }
            throw new GATInvocationException("sftptrilead: remote scheme not recognized: " + dest.getScheme());
        }
       
        if (dest.refersToLocalHost()) {
            SFTPv3FileAttributes a = getAttrs();
            if (a == null) {
                throw new GATInvocationException("sftptrilead: file does not exist");
            }
            if (a.isDirectory()) {
                copyDirectory(gatContext, null, uri, dest);
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("sftpTrilead file: copy remote to local");
            }

            copyToLocal(fixURI(uri, null), fixURI(dest, null));

            return;
        }
        
        // One special case can be done here: copy a file to the same remote host.

        if (uri.getScheme().equals(dest.getScheme()) &&
                uri.getAuthority().equals(dest.getAuthority())) {
            SFTPv3FileAttributes a = getAttrs();
            if (a == null || a.isDirectory()) {
                throw new GATInvocationException("sftptrilead: cannot do third party copy");
            }
            SftpTrileadConnection c = openConnection();
            try {
                try {                               
                    a = c.sftpClient.stat(fixURI(dest, null).getPath());    
                } catch (SFTPException x) {
                    if (x.getServerErrorCode() == ErrorCodes.SSH_FX_NO_SUCH_FILE) {
                        a = null;
                    } else {
                        throw new GATInvocationException("sftpTrilead", x);
                    }
                }
                if (a == null || a.isRegularFile()) {
                    // OK
                } else if (a.isDirectory()) {
                    try {
                        dest = dest.setPath(dest.getPath() + "/" + getName());
                    } catch(Throwable e) {
                        throw new GATInvocationException("sftpTrilead", e);
                    }
                } else {
                    throw new GATInvocationException("sftptrilead: cannot do third party copy");
                }
                SFTPv3FileHandle handleW = c.sftpClient.createFileTruncate(fixURI(dest, null).getPath());
                SFTPv3FileHandle handleR = c.sftpClient.openFileRO(fixURI(uri, null).getPath());
                
                long done = 0;
                byte[] copyBuf = new byte[32768];
                for (;;) {
                    int len = c.sftpClient.read(handleR, done, copyBuf, 0, copyBuf.length);
                    if (len <= 0) {
                        break;
                    }
                    c.sftpClient.write(handleW, done, copyBuf, 0, len);
                    done += len;
                }
                c.sftpClient.closeFile(handleR);
                c.sftpClient.closeFile(handleW);

            } catch (IOException e) {
                throw new GATInvocationException("sftpTrilead", e);
            } finally {
                closeConnection(c, gatContext.getPreferences());
            }
            return;
        }
        // source is remote, dest is remote.
        throw new GATInvocationException("sftptrilead: cannot do third party copy");
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
            c = openConnection();
            handle = c.sftpClient.openFileRO(src.getPath());

            long bytesWritten = 0;
            byte[] buf = new byte[32768];

            while (bytesWritten != length) {
                int len = c.sftpClient.read(handle, bytesWritten, buf, 0,
                        buf.length);
                outBuf.write(buf, 0, len);
                bytesWritten += len;
            }
            outBuf.flush();
            try {
                long l = lastModified();
                if (l != 0L) {
                    destinationFile.setLastModified(l);
                }
            } catch(Throwable e) {
                // O well, we tried
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
                closeConnection(c, gatContext.getPreferences());
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
            c = getConnection(gatContext, dest, verifier, client2serverCiphers,
                    server2clientCiphers, tcpNoDelay);
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
            byte[] buf = new byte[32768];

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
                closeConnection(c, gatContext.getPreferences());
            }
        }
    }
}
