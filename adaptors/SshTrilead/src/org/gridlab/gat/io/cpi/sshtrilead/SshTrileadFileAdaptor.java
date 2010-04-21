package org.gridlab.gat.io.cpi.sshtrilead;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.CommandRunner;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInterface;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.security.sshtrilead.HostKeyVerifier;
import org.gridlab.gat.security.sshtrilead.SshTrileadSecurityUtils;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

public class SshTrileadFileAdaptor extends FileCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("copy", true);
        capabilities.put("canRead", true);
        capabilities.put("canWrite", true);
        capabilities.put("createNewFile", true);
        capabilities.put("delete", true);
        capabilities.put("exists", true);
        capabilities.put("getAbsoluteFile", true);
        capabilities.put("getAbsolutePath", true);
        capabilities.put("isDirectory", true);
        capabilities.put("isFile", true);
        capabilities.put("isHidden", true);
        capabilities.put("lastModified", true);
        capabilities.put("length", true);
        capabilities.put("list", true);
        capabilities.put("mkdir", true);
        capabilities.put("mkdirs", true);
        capabilities.put("move", true);
        capabilities.put("renameTo", true);
        capabilities.put("setLastModified", true);
        capabilities.put("setReadOnly", true);
        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = FileCpi.getSupportedPreferences();
        preferences.put("sshtrilead.caching.canread", "true");
        preferences.put("sshtrilead.caching.canwrite", "true");
        preferences.put("sshtrilead.caching.exists", "true");
        preferences.put("sshtrilead.caching.isdirectory", "true");
        preferences.put("sshtrilead.caching.isfile", "true");
        preferences.put("sshtrilead.caching.ishidden", "true");
        preferences.put("sshtrilead.caching.lastmodified", "true");
        preferences.put("sshtrilead.caching.length", "true");
        preferences.put("sshtrilead.caching.list", "true");
        preferences.put("sshtrilead.caching.iswindows", "true");
        preferences.put("sshtrilead.caching.iscsh", "true");
        preferences
                .put(
                        "sshtrilead.cipher.client2server",
                        "aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc");
        preferences
                .put(
                        "sshtrilead.cipher.server2client",
                        "aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc");
        preferences.put("sshtrilead.tcp.nodelay", "true");
        preferences.put("sshtrilead.use.cached.connections", "true");
        preferences.put("sshtrilead.connect.timeout", "0");
        preferences.put("sshtrilead.kex.timeout", "0");
        
        // Added: preferences for hostkey checking. Defaults are what used to be ....
        preferences.put("sshtrilead.strictHostKeyChecking", "false");
        preferences.put("sshtrilead.noHostKeyChecking", "true");
        
        preferences.put("file.chmod", DEFAULT_MODE);
        return preferences;
    }

    public static String getDescription() {
        return "The SshTrilead File Adaptor implements the File object using the trilead ssh library. Trilead ssh is an open source full java ssh library. On the server side, the 'scp' program must be in the PATH. Connections with a remote ssh server can be made by using the username + password, username + keyfile, or with only a username, depending on the client and server settings.";
    }

    /**
     * On the server side, the "scp" program must be in the PATH.
     */
    private static final long serialVersionUID = 7343449503574566274L;
    
    public static String[] getSupportedSchemes() {
        return new String[] { "sshtrilead", "ssh", "file", ""};
    }
    
    private static Logger logger = LoggerFactory
            .getLogger(SshTrileadFileAdaptor.class);

    private static final int SSH_PORT = 22;

    static final int STDOUT = 0, STDERR = 1, EXIT_VALUE = 2;

    static final String DEFAULT_MODE = "0700";
    
    private static class ConnectionKey {
        final String host;
        final GATContext context;
        
        public ConnectionKey(String host, GATContext context) {
            this.host = host;
            this.context = context;
        }
        
        public int hashCode() {
            return host.hashCode();
        }
        
        public boolean equals(Object o) {
            if (! (o instanceof ConnectionKey)) {
                return false;
            }
            ConnectionKey k = (ConnectionKey) o;
            return k.host.equals(host) && k.context.equals(context);
        }
    }
    
    private static Map<ConnectionKey, Connection> connections = new HashMap<ConnectionKey, Connection>();

    private static Map<URI, Boolean> isDirCache = new HashMap<URI, Boolean>();

    private static Map<URI, Boolean> isFileCache = new HashMap<URI, Boolean>();

    private static Map<URI, Boolean> existsCache = new HashMap<URI, Boolean>();

    private static Map<URI, Boolean> canReadCache = new HashMap<URI, Boolean>();

    private static Map<URI, Boolean> canWriteCache = new HashMap<URI, Boolean>();

    private static Map<String, Boolean> isWindowsCache = new HashMap<String, Boolean>();
    
    private static Map<String, Boolean> isCshCache = new HashMap<String, Boolean>();

    private static Map<URI, String[]> listCache = new HashMap<URI, String[]>();
    
    public static void end() {
        connections.clear();
        isDirCache.clear();
        isFileCache.clear();
        existsCache.clear();
        canReadCache.clear();
        canWriteCache.clear();
        isWindowsCache.clear();
        isCshCache.clear();
        listCache.clear();
    }

    private boolean canReadCacheEnable;

    private boolean canWriteCacheEnable;

    private boolean existsCacheEnable;

    private boolean isDirCacheEnable;

    private boolean isFileCacheEnable;

    private boolean listCacheEnable;

    private boolean isWindowsCacheEnable;

    private boolean connectionCacheEnable;

    private String[] client2serverCiphers;

    private String[] server2clientCiphers;

    private boolean tcpNoDelay;

    URI fixedURI;
    
    private HostKeyVerifier verifier;

    public SshTrileadFileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException, GATInvocationException {
        super(gatContext, location);
        fixedURI = fixURI(location, null);

        boolean noHostKeyChecking;
        boolean strictHostKeyChecking;
        
        // init from preferences ...
        Preferences p = gatContext.getPreferences();
        canReadCacheEnable = ((String) p.get("sshtrilead.caching.canread",
                "false")).equalsIgnoreCase("true");
        canWriteCacheEnable = ((String) p.get("sshtrilead.caching.canwrite",
                "false")).equalsIgnoreCase("true");
        existsCacheEnable = ((String) p
                .get("sshtrilead.caching.exists", "false"))
                .equalsIgnoreCase("true");
        isDirCacheEnable = ((String) p.get("sshtrilead.caching.isdirectory",
                "false")).equalsIgnoreCase("true");
        isFileCacheEnable = ((String) p
                .get("sshtrilead.caching.isfile", "false"))
                .equalsIgnoreCase("true");
        listCacheEnable = ((String) p.get("sshtrilead.caching.list", "false"))
                .equalsIgnoreCase("true");
        isWindowsCacheEnable = ((String) p.get("sshtrilead.caching.iswindows", "true"))
                .equalsIgnoreCase("true");
        String client2serverCipherString = ((String) p
                .get(
                        "sshtrilead.cipher.client2server",
                        "aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc"));
        client2serverCiphers = client2serverCipherString.split(",");
        String server2clientCipherString = ((String) p
                .get(
                        "sshtrilead.cipher.server2client",
                        "aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc"));
        server2clientCiphers = server2clientCipherString.split(",");
        tcpNoDelay = ((String) p.get("sshtrilead.tcp.nodelay", "true"))
                .equalsIgnoreCase("true");
        connectionCacheEnable = ((String) p.get(
                "sshtrilead.use.cached.connections", "true"))
                .equalsIgnoreCase("true");
        noHostKeyChecking = ((String) p.get("sshtrilead.noHostKeyChecking", "true"))
                .equalsIgnoreCase("true");
        strictHostKeyChecking = ((String) p.get("sshtrilead.strictHostKeyChecking", "false"))
                .equalsIgnoreCase("true");

        verifier = new HostKeyVerifier(false, strictHostKeyChecking, noHostKeyChecking);
    }

    public void copy(URI destination) throws GATInvocationException {
        destination = fixURI(destination, null);
        if (fixedURI.refersToLocalHost()) {
            // put the file
            try {
                put(destination);
            } catch (Exception e) {
                if (e instanceof GATInvocationException) {
                    throw (GATInvocationException) e;
                }
                throw new GATInvocationException("SshTrileadFileAdaptor", e);
            }
        } else {
            if (destination.refersToLocalHost()) {
                // get the file
                try {
                    get(destination);
                } catch (Exception e) {
                    if (e instanceof GATInvocationException) {
                        throw (GATInvocationException) e;
                    }
                    throw new GATInvocationException("SshTrileadFileAdaptor", e);
                }
            } else {
                remoteScp(destination);
            }
        }
    }

    private void put(URI destination) throws Exception {
        logger.debug("destination: " + destination);
        SCPClient client = null;
        try {
            client = getConnection(destination, gatContext,
                    connectionCacheEnable, tcpNoDelay, client2serverCiphers,
                    server2clientCiphers, verifier).createSCPClient();
        } catch (IOException e) {
            client = getConnection(destination, gatContext, false, tcpNoDelay,
                    client2serverCiphers, server2clientCiphers, verifier)
                    .createSCPClient();
        }

        FileInterface destinationFile = GAT.createFile(gatContext, destination)
                .getFileInterface();
        String remoteDir = null;
        String remoteFileName = null;

        if (gatContext.getPreferences().containsKey("file.create")) {
            if (((String) gatContext.getPreferences().get("file.create"))
                    .equalsIgnoreCase("true")) {
                File destinationParentFile = destinationFile.getParentFile();
                if (destinationParentFile != null) {
                    destinationParentFile.getFileInterface().mkdirs();
                }
            }
        }
        if (destination.hasAbsolutePath()) {
            if (destinationFile.isDirectory()) {
                remoteDir = destinationFile.getPath();
            } else {
                remoteDir = destinationFile.getParent();
            }
        } else {
            if (destinationFile.isDirectory()) {
                remoteDir = destinationFile.getPath();
            } else {
                remoteDir = ".";
                String parent = destinationFile.getParent();
                if (parent != null) {
                    String separator;
                    if (isWindows(gatContext, destination)) {
                        separator = "\\";
                    } else {
                        separator = "/";
                    }
                    if (parent.startsWith(separator)) {
                        remoteDir = parent;
                    } else {
                        remoteDir += separator + parent;
                    }
                }

            }
        }
        remoteFileName = destinationFile.getName();
        String mode = getMode(gatContext, DEFAULT_MODE);
        java.io.File sourceFile = new java.io.File(getFixedPath());
        if (destinationFile.isDirectory() && sourceFile.isFile()) {
            logger.debug("put " + getFixedPath() + ", " + remoteDir + ", "
                    + mode);
            client.put(getFixedPath(), remoteDir, mode);
            try {
                String path = remoteDir + "/" + getName();
                URI d = destination.setPath(path);
                destinationFile = GAT.createFile(gatContext, d).getFileInterface();
                destinationFile.setLastModified(lastModified());
            } catch(Throwable e) {
                // ignored
            }
        } else if (sourceFile.isDirectory()) {
            copyDir(destination);
        } else if (sourceFile.isFile()) {
            logger.debug("put " + getFixedPath() + ", " + remoteFileName + ", "
                    + remoteDir + ", " + mode);
            client.put(getFixedPath(), remoteFileName, remoteDir, mode);
            try {
                destinationFile.setLastModified(lastModified());
            } catch(Throwable e) {
                // ignored
            }
        } else {
            throw new GATInvocationException("cannot copy this file '"
                    + fixedURI + "' to '" + remoteFileName + "' in dir '"
                    + remoteDir + "'! (Reason: src is file: "
                    + sourceFile.isFile() + ", src is dir: "
                    + sourceFile.isDirectory() + ", dest is dir: "
                    + destinationFile.isDirectory());
        }
    }

    private String getMode(GATContext gatContext, String defaultMode)
            throws GATInvocationException {
        String mode = defaultMode;
        if (gatContext.getPreferences().containsKey("file.chmod")) {
            mode = (String) gatContext.getPreferences().get("file.chmod");
            if (mode.length() == 3) {
                mode = "0" + mode;
            }
            if (mode.length() != 4 || !mode.startsWith("0")) {
                throw new GATInvocationException("invalid mode: '"
                        + gatContext.getPreferences().get("file.chmod")
                        + "'. Should be like '0xxx' or 'xxx'");
            }
        }
        return mode;
    }

    private void get(URI destination) throws Exception {
        SCPClient client = null;
        try {
            client = getConnection(fixedURI, gatContext, connectionCacheEnable,
                    tcpNoDelay, client2serverCiphers, server2clientCiphers, verifier)
                    .createSCPClient();
        } catch (IOException e) {
            client = getConnection(fixedURI, gatContext, false, tcpNoDelay,
                    client2serverCiphers, server2clientCiphers, verifier)
                    .createSCPClient();
        }
        FileInterface destinationFile = GAT.createFile(gatContext, destination)
                .getFileInterface();

        if (gatContext.getPreferences().containsKey("file.create")) {
            if (((String) gatContext.getPreferences().get("file.create"))
                    .equalsIgnoreCase("true")) {
                File destinationParentFile = destinationFile.getParentFile();
                if (destinationParentFile != null) {
                    destinationParentFile.getFileInterface().mkdirs();
                }
            }
        }

        String dest = destination.getPath();
        if (destinationFile.isDirectory() && isFile()) {
            dest = dest + "/" + getName();
            if (java.io.File.separator.equals("/")) {
                createNewFile(dest, getMode(gatContext,
                        DEFAULT_MODE));
            }
            client.get(getFixedPath(), new java.io.FileOutputStream(dest));
            try {
                new java.io.File(dest).setLastModified(lastModified());
            } catch(Throwable e) {
                // ignored
            }
        } else if (isDirectory()) {
            copyDir(destination);
        } else if (isFile()) {
            if (java.io.File.separator.equals("/")) {
                createNewFile(dest, getMode(gatContext, DEFAULT_MODE));
            }
            client.get(getFixedPath(), new java.io.FileOutputStream(dest));
            try {
                new java.io.File(dest).setLastModified(lastModified());
            } catch(Throwable e) {
                // ignored
            }
        } else {
            throw new GATInvocationException("cannot copy this file '"
                    + fixedURI + "' to '" + destination
                    + "'! (Reason: src is file: " + isFile()
                    + ", src is dir: " + isDirectory()
                    + ", target is dir: " + destinationFile.isDirectory());
        }
    }

    private boolean isWindows(GATContext context, URI destination)
            throws GATInvocationException {
        return isWindows(context, destination, isWindowsCacheEnable);
    }
    
    public static boolean isWindows(GATContext context, URI destination, boolean isWindowsCacheEnable)
            throws GATInvocationException {
        // if 'ls' gives stderr and 'dir' doesn't then we guess it's Windows
        // else we assume it's non-Windows

        String host = destination.getHost();
        if (host == null) {
            host = destination.resolveHost();
        }
        logger.info("is Windows for host: " + host);
        if (isWindowsCacheEnable) {
            if (isWindowsCache.containsKey(host)) {
                logger.debug("got is Windows from cache");
                return isWindowsCache.get(host);
            }
        }
        SshTrileadFileAdaptor file;
        try {
            file = new SshTrileadFileAdaptor(context, destination);
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("failed to create ssh file", e);
        }
        String[] result;
        try {
            result = file.execCommand("ls");
        } catch (Exception e) {
            throw new GATInvocationException("sshtrilead", e);
        }
        if (result[STDERR].length() != 0) {
            try {
                result = file.execCommand("dir");
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            boolean isWindows = result[STDERR].length() == 0;
            if (isWindowsCacheEnable) {
                isWindowsCache.put(host, isWindows);
            }
            return isWindows;

        }
        if (isWindowsCacheEnable) {
            isWindowsCache.put(host, false);
        }
        return false;
    }
    
    
    public static boolean isCsh(GATContext context, URI destination, boolean isCshCacheEnable)
            throws GATInvocationException {
        // if 'echo ________*_______' has the word "match" in its output,
        // a csh is used.

        String host = destination.getHost();
        if (host == null) {
            host = destination.resolveHost();
        }
        logger.info("isCsh for host: " + host);
        if (isCshCacheEnable) {
            if (isCshCache.containsKey(host)) {
                logger.debug("got isCsh from cache");
                return isCshCache.get(host);
            }
        }
        SshTrileadFileAdaptor file;
        try {
            file = new SshTrileadFileAdaptor(context, destination);
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("failed to create ssh file", e);
        }
        String[] result;
        try {
            result = file.execCommand("echo ___________*_____________");
        } catch (Exception e) {
            throw new GATInvocationException("sshtrilead", e);
        }
        boolean isCsh = result[STDOUT].contains("match");         
        if (isCshCacheEnable) {
            isCshCache.put(host, isCsh);
        }
        return isCsh;
    }
    
       

    private void createNewFile(String localfile, String mode)
            throws GATInvocationException {
        new CommandRunner("touch", localfile);
        new CommandRunner("chmod",  mode, localfile);
    }
    
    Session getSession() throws Exception {
        try {
            return getConnection(fixedURI, gatContext,
                    connectionCacheEnable, tcpNoDelay, client2serverCiphers,
                    server2clientCiphers, verifier).openSession();
        } catch (IOException e) {
            return getConnection(fixedURI, gatContext, false, tcpNoDelay,
                    client2serverCiphers, server2clientCiphers, verifier).openSession();
        }
    }
 
    public static Connection getConnection(URI fixedURI, GATContext context,
            boolean useCachedConnection, boolean tcpNoDelay,
            String[] client2server, String[] server2client,
            HostKeyVerifier verifier) throws Exception {
        String host = fixedURI.getHost();
        if (host == null) {
            host = fixedURI.resolveHost();
        }
        logger.info("getting connection for host: " + host);
        ConnectionKey key = new ConnectionKey(host, (GATContext) context.clone());
        if (useCachedConnection) {
            Connection c = connections.get(key);
            if (c != null) {
                logger.info("returning cached connection");
                return c;
            }
        }
        Connection newConnection = new Connection(host, fixedURI
                .getPort(SSH_PORT));
        newConnection.setClient2ServerCiphers(client2server);
        newConnection.setServer2ClientCiphers(server2client);
        newConnection.setTCPNoDelay(tcpNoDelay);
        int connectTimeout = 0;
        String connectTimeoutString = (String) context.getPreferences().get(
                "sshtrilead.connect.timeout");
        if (connectTimeoutString != null) {
            try {
                connectTimeout = Integer.parseInt(connectTimeoutString);
            } catch (Throwable t) {
                logger
                .info("'sshtrilead.connect.timeout' set, but could not be parsed: "
                        + t);
            }
        }
        int kexTimeout = 0;
        String kexTimeoutString = (String) context.getPreferences().get(
        "sshtrilead.kex.timeout");
        if (kexTimeoutString != null) {
            try {
                kexTimeout = Integer.parseInt(kexTimeoutString);
            } catch (Throwable t) {
                logger
                .info("'sshtrilead.kex.timeout' set, but could not be parsed: "
                        + t);
            }
        }
        newConnection.connect(verifier, connectTimeout, kexTimeout);
        Map<String, Object> securityInfo = SshTrileadSecurityUtils
        .getSshTrileadCredential(context, "sshtrilead", fixedURI,
                fixedURI.getPort(SSH_PORT));
        String username = (String) securityInfo.get("username");
        String password = (String) securityInfo.get("password");
        java.io.File keyFile = (java.io.File) securityInfo.get("keyfile");

        boolean connected = false;

        if (username != null && password != null) {
            try {
                connected = newConnection.authenticateWithPassword(
                        username, password);
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger
                    .debug("exception caught during authentication with password: ",
                            e);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("authentication with password: " + connected);
            }
        }
        if (!connected && username != null && keyFile != null) {
            try {
                connected = newConnection.authenticateWithPublicKey(
                        username, keyFile, password);
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger
                    .debug("exception caught during authentication with public key: ",
                            e);
                }
            }
            if (logger.isDebugEnabled()) {
                logger
                .debug("authentication with public key: "
                        + connected);
            }
        }
        if (!connected && username != null) {
            try {
                connected = newConnection.authenticateWithNone(username);
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger
                    .debug("exception caught during authentication with username: "
                            + e);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("authentication with username: " + connected);
            }
        }

        if (!connected) {
            throw new Exception("unable to authenticate");
        } else {
            logger.info("putting connection for host " + host
                    + " into cache");
            connections.put(key, newConnection);
        }
        if (logger.isInfoEnabled()) {
            long start = System.currentTimeMillis();
            try {
                newConnection.ping();
                logger.info("ping connection: "
                        + (System.currentTimeMillis() - start) + " ms");
            } catch (Exception e) {
                logger.info("ping failed: " + e);
            }

        }
        return newConnection;
    }

    private String[] execCommand(String cmd) throws IOException, Exception {
        logger.info("command: " + cmd + ", uri: " + fixedURI);
        String[] result = new String[3];
        Session session = null;
        try {
            session = getConnection(fixedURI, gatContext,
                    connectionCacheEnable, tcpNoDelay, client2serverCiphers,
                    server2clientCiphers, verifier).openSession();
        } catch (IOException e) {
            session = getConnection(fixedURI, gatContext, false, tcpNoDelay,
                    client2serverCiphers, server2clientCiphers, verifier).openSession();
        }
        session.execCommand(cmd);
        // see http://www.trilead.com/Products/Trilead-SSH-2-Java/FAQ/#blocking
        InputStream stdout = new StreamGobbler(session.getStdout());
        InputStream stderr = new StreamGobbler(session.getStderr());
        BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
        result[STDOUT] = "";
        result[STDERR] = "";
        StringBuffer out = new StringBuffer();
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            out.append(line);
            out.append("\n");
        }
        result[STDOUT] = out.toString();
        br = new BufferedReader(new InputStreamReader(stderr));
        StringBuffer err = new StringBuffer();
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            err.append(line);
            err.append("\n");
        }
        result[STDERR] = err.toString();
        while (session.getExitStatus() == null) {
            Thread.sleep(500);
        }
        result[EXIT_VALUE] = "" + session.getExitStatus();
        session.close();
        if (logger.isDebugEnabled()) {
            logger.debug("STDOUT: " + result[STDOUT]);
            logger.debug("STDERR: " + result[STDERR]);
            logger.debug("EXIT:   " + result[EXIT_VALUE]);
        }
        return result;
    }

    public boolean canRead() throws GATInvocationException {
        if (canReadCacheEnable) {
            if (canReadCache.containsKey(fixedURI)) {
                return canReadCache.get(fixedURI);
            }
        }
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("test -r " + protectAgainstShellMetas(getFixedPath()) + " && echo 0");
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            boolean canread = result[STDOUT].length() != 0;
            if (canReadCacheEnable) {
                canReadCache.put(fixedURI, canread);
            }
            return canread;
        }
    }

    private void remoteScp(URI dest) throws GATInvocationException {
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("scp -r " + protectAgainstShellMetas(getFixedPath()) + " ${USER}@"
                        + dest.getHost() + ":" + protectAgainstShellMetas(dest.getPath()));
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            if (result[STDERR].length() != 0
                    && ! result[STDERR].startsWith("Warning:")) {
                throw new GATInvocationException(
                        "Third party transfer failed: " + result[STDERR]);
            }
        }
    }

    public boolean canWrite() throws GATInvocationException {
        if (canWriteCacheEnable) {
            if (canWriteCache.containsKey(fixedURI)) {
                return canWriteCache.get(fixedURI);
            }
        }
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("test -w " + getFixedPath() + " && echo 0");
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            boolean canwrite = result[STDOUT].length() != 0;
            if (canWriteCacheEnable) {
                canWriteCache.put(fixedURI, canwrite);
            }
            return canwrite;
        }
    }

    public boolean createNewFile() throws GATInvocationException {
        if (exists()) {
            return false;
        }
        // TODO: this is not atomic.
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            String path = protectAgainstShellMetas(getFixedPath());
            try {
                result = execCommand("test ! -d " + path
                        + " && test ! -f " + path + " && touch "
                        + path + " && chmod "
                        + getMode(gatContext, DEFAULT_MODE) + " " + path);
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            if (existsCacheEnable && result[STDERR].length() == 0) {
                existsCache.put(fixedURI, result[STDERR].length() == 0);
            }
            return result[STDERR].length() == 0;
        }
    }

    public boolean delete() throws GATInvocationException {
        if (!exists()) {
            return false;
        }
        if (existsCacheEnable) {
            if (existsCache.containsKey(fixedURI)) {
                existsCache.remove(fixedURI);
            }
        }
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("rm -rf " + protectAgainstShellMetas(getFixedPath()));
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            return result[STDERR].length() == 0;
        }
    }

    public boolean exists() throws GATInvocationException {
        logger.info("exists: ls -d " + getFixedPath());

        if (existsCacheEnable) {
            if (existsCache.containsKey(fixedURI)) {
                return existsCache.get(fixedURI);
            }
        }
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("ls -d " + protectAgainstShellMetas(getFixedPath()));
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            // no stderr, and >0 stdout mean that the file exists
            boolean exists = result[STDERR].length() == 0
                    && result[STDOUT].length() > 0;
            if (existsCacheEnable) {
                existsCache.put(fixedURI, exists);
            }
            return exists;
        }
    }

    public org.gridlab.gat.io.File getAbsoluteFile()
            throws GATInvocationException {
        String absUri = fixedURI.toString().replace(fixedURI.getPath(),
                getAbsolutePath());
        try {
            return GAT.createFile(gatContext, new URI(absUri));
        } catch (Exception e) {
            return null; // never executed
        }
    }

    public String getAbsolutePath() throws GATInvocationException {
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String fixed = getFixedPath();
            if (fixed.startsWith("/")) {
                return fixed;
            }
            String[] result;
            try {
                result = execCommand("echo ~");
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            return result[STDOUT].replace("\n", "") + "/" + fixed;
        }
    }

    public boolean isDirectory() throws GATInvocationException {
        logger.debug("isDirectory " + getFixedPath() + " (uri:" + fixedURI
                + ")");
        if (isDirCacheEnable) {
            if (isDirCache.containsKey(fixedURI)) {
                logger.debug("got isDirectory from cache");
                return isDirCache.get(fixedURI);
            }
        }
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("test -d " + protectAgainstShellMetas(getFixedPath()));
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            // 0=dir 1=other
            boolean isDir = result[EXIT_VALUE].equals("0");
            if (isDirCacheEnable) {
                isDirCache.put(fixedURI, isDir);
            }
            return isDir;
        }
    }

    final String getFixedPath() {
        String res = fixedURI.getPath();
        if (res == null) {
            throw new Error("path not specified correctly in URI: " + fixedURI);
        }
        logger.debug("fixed path = " + res);
        return res;
    }

    public boolean isFile() throws GATInvocationException {
        if (isFileCacheEnable) {
            if (isFileCache.containsKey(fixedURI)) {
                return isFileCache.get(fixedURI);
            }
        }
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("test -f " + protectAgainstShellMetas(getFixedPath()));
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            // 0=file 1=other
            boolean isFile = result[EXIT_VALUE].equals("0");
            if (isFileCacheEnable) {
                isFileCache.put(fixedURI, isFile);
            }
            return isFile;
        }
    }

    public boolean isHidden() throws GATInvocationException {
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            return getName().startsWith(".");
        }
    }

    public long length() throws GATInvocationException {
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("wc -c < " + protectAgainstShellMetas(getFixedPath()));
            } catch (Throwable e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            try {
                return Long.parseLong(result[STDOUT].replaceAll("[ \t\n\f\r]", ""));
            } catch(Throwable e) {
                return 0L;
            }
        }
    }

    public String[] list() throws GATInvocationException {
        if (!isDirectory()) {
            return null;
        }
        if (listCacheEnable) {
            if (listCache.containsKey(fixedURI)) {
                return listCache.get(fixedURI);
            }
        }
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("ls -1 " + protectAgainstShellMetas(getFixedPath()));
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            if (result[STDOUT].equals("")) {
                return new String[0];
            }
            String[] list = result[STDOUT].split("\n");
            if (listCacheEnable) {
                listCache.put(fixedURI, list);
            }
            return list;
        }
    }

    public File[] listFiles() throws GATInvocationException {
        try {
            String[] f = list();
            if (f == null) {
                return null;
            }
            File[] res = new File[f.length];

            for (int i = 0; i < f.length; i++) {
                String uri = fixedURI.toString();

                if (!uri.endsWith("/")) {
                    uri += "/";
                }

                uri += f[i];
                res[i] = GAT.createFile(gatContext, new URI(uri));
            }

            return res;
        } catch (Exception e) {
            throw new GATInvocationException("file cpi", e);
        }
    }

    public boolean mkdir() throws GATInvocationException {
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("mkdir -m "
                        + getMode(gatContext, DEFAULT_MODE) + " "
                        + protectAgainstShellMetas(getFixedPath()));
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            boolean mkdir = result[STDERR].length() == 0;
            if (existsCacheEnable && mkdir) {
                existsCache.put(fixedURI, mkdir);
            }
            if (isDirCacheEnable && mkdir) {
                isDirCache.put(fixedURI, mkdir);
            }
            return mkdir;
        }
    }

    public boolean mkdirs() throws GATInvocationException {
        if (exists()) {
            return false;
        }
        if (isWindows(gatContext, location)) {
            return super.mkdirs();
        } else {
            String[] result;
            try {
                result = execCommand("mkdir -m "
                        + getMode(gatContext, DEFAULT_MODE) + " -p "
                        + protectAgainstShellMetas(getFixedPath()));
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            boolean mkdirs = result[STDERR].length() == 0;
            if (existsCacheEnable && mkdirs) {
                existsCache.put(fixedURI, mkdirs);
            }
            if (isDirCacheEnable && mkdirs) {
                isDirCache.put(fixedURI, mkdirs);
            }
            return mkdirs;
        }
    }

    public void move(URI destination) throws GATInvocationException {
        FileInterface destinationFile = null;
        try {
            destinationFile = GAT.createFile(gatContext, destination)
                    .getFileInterface();
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("sshtrilead", e);
        }
        boolean movedIntoExistingDir = (destinationFile.exists() && destinationFile
                .isDirectory());
        copy(destination);
        if (!delete()) {
            throw new GATInvocationException(
                    "internal error in SshTrileadFileAdaptor: could not move file "
                            + toURI() + " to " + destination);
        }
        // remove from cache...
        if (canReadCacheEnable) {
            canReadCache.remove(fixedURI);
        }
        if (canWriteCacheEnable) {
            canWriteCache.remove(fixedURI);
        }
        if (isDirCacheEnable) {
            isDirCache.remove(fixedURI);
        }
        if (isFileCacheEnable) {
            isFileCache.remove(fixedURI);
        }
        if (existsCacheEnable) {
            existsCache.remove(fixedURI);
        }
        if (listCacheEnable) {
            listCache.remove(fixedURI);
        }
        // now let the fixedURI point to the new fixedURI (destination)
        if (movedIntoExistingDir) {
            try {
                fixedURI = new URI(destination + "/" + getName());
            } catch (URISyntaxException e) {
                // should not occur
            }
        } else {
            fixedURI = destination;
        }
        if (existsCacheEnable) {
            existsCache.put(fixedURI, true);
        }
        return;
    }

    public void renameTo(URI destination) throws GATInvocationException {
        move(destination);
        return;
    }

    public boolean renameTo(File file) throws GATInvocationException {
        copy(file.toGATURI());
        if (!delete()) {
            return false;
        }
        fixedURI = file.toGATURI();
        return true;
    }

    public boolean setLastModified(long lastModified)
            throws GATInvocationException {
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("touch -t "
                        + toTouchDateFormat(lastModified) + " "
                        + protectAgainstShellMetas(getFixedPath()));
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            return result[STDERR].length() == 0;
        }
    }

    public long lastModified() throws GATInvocationException {
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("perl -e 'print ((stat $ARGV[0])[9]);' "
                        + protectAgainstShellMetas(getFixedPath()));
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            try {
                return Long.parseLong(result[STDOUT].substring(0,
                        result[STDOUT].length() - 1)) * 1000;
            } catch (Throwable e) {
                return 0L;
            }
        }
    }

    private String toTouchDateFormat(long date) {
        Date d = new Date(date);
        // see man touch for the date format
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm.ss");
        return formatter.format(d);
    }

    public boolean setReadOnly() throws GATInvocationException {
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("chmod a-w " + protectAgainstShellMetas(getFixedPath()));
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            return result[STDERR].length() == 0;
        }
    }
    
    static String protectAgainstShellMetas(String s) {
        char[] chars = s.toCharArray();
        StringBuffer b = new StringBuffer();
        b.append('\'');
        for (char c : chars) {
            if (c == '\'') {
                b.append('\'');
                b.append('\\');
                b.append('\'');
            }
            b.append(c);
        }
        b.append('\'');
        return b.toString();
    }
}
