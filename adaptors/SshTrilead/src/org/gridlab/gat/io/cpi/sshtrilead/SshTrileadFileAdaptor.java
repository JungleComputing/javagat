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
import java.util.Properties;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.CommandRunner;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInterface;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.security.sshtrilead.SshTrileadSecurityUtils;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

public class SshTrileadFileAdaptor extends FileCpi {

    /**
     * On the server side, the "scp" program must be in the PATH.
     */
    private static final long serialVersionUID = 7343449503574566274L;

    private static Logger logger = Logger
            .getLogger(SshTrileadFileAdaptor.class);

    private static final int SSH_PORT = 22;

    private static final int STDOUT = 0, STDERR = 1, EXIT_VALUE = 2;

    private static Map<String, Connection> connections = new HashMap<String, Connection>();

    private static Map<URI, Boolean> isDirCache = new HashMap<URI, Boolean>();

    private static Map<URI, Boolean> isFileCache = new HashMap<URI, Boolean>();

    private static Map<URI, Boolean> existsCache = new HashMap<URI, Boolean>();

    private static Map<URI, Boolean> canReadCache = new HashMap<URI, Boolean>();

    private static Map<URI, Boolean> canWriteCache = new HashMap<URI, Boolean>();

    private static Map<String, Boolean> isWindowsCache = new HashMap<String, Boolean>();

    private static Map<URI, String[]> listCache = new HashMap<URI, String[]>();

    private static boolean canReadCacheEnable = true;

    private static boolean canWriteCacheEnable = true;

    private static boolean existsCacheEnable = false;

    private static boolean isDirCacheEnable = true;

    private static boolean isFileCacheEnable = true;

    private static boolean listCacheEnable = false;

    private static boolean isWindowsCacheEnable = true;

    private static String[] client2serverCiphers = new String[] { "aes256-ctr",
            "aes192-ctr", "aes128-ctr", "blowfish-ctr", "aes256-cbc",
            "aes192-cbc", "aes128-cbc", "blowfish-cbc" };

    private static String[] server2clientCiphers = new String[] { "aes256-ctr",
            "aes192-ctr", "aes128-ctr", "blowfish-ctr", "aes256-cbc",
            "aes192-cbc", "aes128-cbc", "blowfish-cbc" };

    private static boolean tcpNoDelay = true;

    static {
        Properties sshTrileadProperties = new Properties();
        try {
            sshTrileadProperties.load(new java.io.FileInputStream(System
                    .getProperty("gat.adaptor.path")
                    + File.separator
                    + "SshTrileadAdaptor"
                    + File.separator
                    + "sshtrilead.properties"));
            if (logger.isDebugEnabled()) {
                logger.debug("reading properties file for sshtrilead adaptor");
            }
            canReadCacheEnable = ((String) sshTrileadProperties.getProperty(
                    "caching.canread", "true")).equalsIgnoreCase("true");
            canWriteCacheEnable = ((String) sshTrileadProperties.getProperty(
                    "caching.canwrite", "true")).equalsIgnoreCase("true");
            existsCacheEnable = ((String) sshTrileadProperties.getProperty(
                    "caching.exists", "true")).equalsIgnoreCase("true");
            isDirCacheEnable = ((String) sshTrileadProperties.getProperty(
                    "caching.isdirectory", "true")).equalsIgnoreCase("true");
            isFileCacheEnable = ((String) sshTrileadProperties.getProperty(
                    "caching.isfile", "true")).equalsIgnoreCase("true");
            listCacheEnable = ((String) sshTrileadProperties.getProperty(
                    "caching.list", "true")).equalsIgnoreCase("true");
            String client2serverCipherString = ((String) sshTrileadProperties
                    .getProperty(
                            "cipher.client2server",
                            "aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc"));
            client2serverCiphers = client2serverCipherString.split(",");
            String server2clientCipherString = ((String) sshTrileadProperties
                    .getProperty(
                            "cipher.server2client",
                            "aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc"));
            server2clientCiphers = server2clientCipherString.split(",");
            tcpNoDelay = ((String) sshTrileadProperties.getProperty(
                    "tcp.nodelay", "true")).equalsIgnoreCase("true");
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("exception while trying to read property file: "
                        + e);
            }
        }
    }

    private URI fixedURI;

    public SshTrileadFileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException, GATInvocationException {
        super(gatContext, location);
        fixedURI = fixURI(location, null);
        if (!location.isCompatible("ssh") && !location.isCompatible("file")) {
            throw new AdaptorNotApplicableException("cannot handle this URI: "
                    + location);
        }
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
                // not supported
                throw new GATInvocationException(
                        "cannot perform third party file transfers!");
            }
        }
    }

    private void put(URI destination) throws Exception {
        logger.debug("destination: " + destination);
        Connection connection = getConnection(destination, gatContext);

        SCPClient client = connection.createSCPClient();

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
        String mode = "0600";
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
        File sourceFile = GAT.createFile(gatContext, location);
        if (destinationFile.isDirectory() && sourceFile.isFile()) {
            logger.debug("put " + getFixedPath() + ", " + remoteDir + ", "
                    + mode);
            client.put(getFixedPath(), remoteDir, mode);
        } else if (sourceFile.isDirectory()) {
            copyDir(destination);
        } else if (sourceFile.isFile()) {
            logger.debug("put " + getFixedPath() + ", " + remoteFileName + ", "
                    + remoteDir + ", " + mode);
            client.put(getFixedPath(), remoteFileName, remoteDir, mode);
        } else {
            throw new GATInvocationException("cannot copy this file '"
                    + fixedURI + "' to '" + remoteFileName + "' in dir '"
                    + remoteDir + "'! (Reason: src is file: "
                    + sourceFile.isFile() + ", src is dir: "
                    + sourceFile.isDirectory() + ", dest is dir: "
                    + destinationFile.isDirectory());
        }
    }

    private void get(URI destination) throws Exception {
        Connection connection = getConnection(fixedURI, gatContext);
        SCPClient client = connection.createSCPClient();
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

        String mode = "0600";
        if (!isWindows(gatContext, destination)) {
            if (mode.length() == 3) {
                mode = "0" + mode;
            }
            if (mode.length() != 4 || !mode.startsWith("0")) {
                throw new GATInvocationException("invalid mode: '"
                        + gatContext.getPreferences().get("file.chmod")
                        + "'. Should be like '0xxx' or 'xxx'");
            }
        }
        File sourceFile = GAT.createFile(gatContext, location);
        if (destinationFile.isDirectory() && sourceFile.isFile()) {
            createNewFile(destination.getPath() + "/" + sourceFile.getName(),
                    mode);
            client.get(getFixedPath(), new java.io.FileOutputStream(destination
                    .getPath()
                    + "/" + sourceFile.getName()));
        } else if (sourceFile.isDirectory()) {
            copyDir(destination);
        } else if (sourceFile.isFile()) {
            createNewFile(destination.getPath(), mode);
            client.get(getFixedPath(), new java.io.FileOutputStream(destination
                    .getPath()));
        } else {
            throw new GATInvocationException("cannot copy this file '"
                    + fixedURI + "' to '" + destination
                    + "'! (Reason: target is file: " + sourceFile.isFile()
                    + ", target is dir: " + sourceFile.isDirectory()
                    + ", src is dir: " + destinationFile.isDirectory());
        }
    }

    private static boolean isWindows(GATContext context, URI destination)
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
            boolean isWindows = result[STDOUT].length() == 0;
            if (isWindowsCacheEnable) {
                isWindowsCache.put(host, isWindows);
            }
            return isWindows;

        }
        return false;
    }

    private void createNewFile(String localfile, String mode)
            throws GATInvocationException {
        new CommandRunner("touch " + localfile);
        new CommandRunner("chmod " + mode + " " + localfile);
    }

    public static Connection getConnection(URI fixedURI, GATContext context)
            throws Exception {
        String host = fixedURI.getHost();
        if (host == null) {
            host = fixedURI.resolveHost();
        }
        logger.info("getting connection for host: " + host);
        if (connections.containsKey(host)) {
            logger.info("returning cached connection");
            return connections.get(host);
        } else {
            Connection newConnection = new Connection(host, fixedURI
                    .getPort(SSH_PORT));

            newConnection.setClient2ServerCiphers(client2serverCiphers);
            newConnection.setServer2ClientCiphers(server2clientCiphers);
            newConnection.setTCPNoDelay(tcpNoDelay);
            newConnection.connect();
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
                                .debug("exception caught during authentication with password: "
                                        + e);
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
                                .debug("exception caught during authentication with public key: "
                                        + e);
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
            // TODO: add interactive authentication?
            if (!connected) {
                throw new Exception("unable to authenticate");
            } else {
                logger.info("putting connection for host " + host
                        + " into cache");
                connections.put(host, newConnection);
            }
            return newConnection;
        }
    }

    private String[] execCommand(String cmd) throws IOException, Exception {
        logger.info("command: " + cmd + ", uri: " + fixedURI);
        String[] result = new String[3];
        Session session = getConnection(fixedURI, gatContext).openSession();
        session.execCommand(cmd);
        // see http://www.trilead.com/Products/Trilead-SSH-2-Java/FAQ/#blocking
        InputStream stdout = new StreamGobbler(session.getStdout());
        InputStream stderr = new StreamGobbler(session.getStderr());
        BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
        result[STDOUT] = "";
        result[STDERR] = "";
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            result[STDOUT] += line + "\n";
        }
        br = new BufferedReader(new InputStreamReader(stderr));
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            result[STDERR] += line + "\n";
        }
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
                result = execCommand("test -r " + getFixedPath() + " && echo 0");
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
        if (isWindows(gatContext, location)) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            String[] result;
            try {
                result = execCommand("test ! -d " + getFixedPath()
                        + " && test ! -f " + getFixedPath() + " && touch "
                        + getFixedPath());
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
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
                result = execCommand("rm -rf " + getFixedPath());
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            return result[STDERR].length() == 0;
        }
    }

    public boolean exists() throws GATInvocationException {
        logger.info("exists: ls " + getFixedPath());

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
                result = execCommand("ls " + getFixedPath());
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            // no stderr, and >=0 stdout mean that the file exists
            boolean exists = result[STDERR].length() == 0
                    && result[STDOUT].length() >= 0;
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
            String[] result;
            try {
                result = execCommand("echo ~");
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            return result[STDOUT].replace("\n", "") + "/" + getFixedPath();
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
                result = execCommand("test -d " + getFixedPath());
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

    private final String getFixedPath() {
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
                result = execCommand("test -f " + getFixedPath());
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
                result = execCommand("wc -c < " + getFixedPath());
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            return Long.parseLong(result[STDOUT].replaceAll("[ \t\n\f\r]", ""));
        }
    }

    public String[] list() throws GATInvocationException {
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
                result = execCommand("ls -1 " + getFixedPath());
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            if (result[STDOUT].equals("")) {
                return null;
            }
            String[] list = result[STDOUT].split("\n");
            if (listCacheEnable) {
                listCache.put(fixedURI, list);
            }
            return list;
        }
    }

    public File[] listFiles() throws GATInvocationException {
        if (!isDirectory()) {
            throw new GATInvocationException("this is not a directory: "
                    + fixedURI);
        }

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
                result = execCommand("mkdir " + getFixedPath());
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            boolean mkdir = result[STDERR].length() == 0;
            if (isDirCacheEnable && mkdir) {
                isDirCache.put(fixedURI, mkdir);
            }
            return mkdir;
        }
    }

    public boolean mkdirs() throws GATInvocationException {
        if (isWindows(gatContext, location)) {
            return super.mkdirs();
        } else {
            String[] result;
            try {
                result = execCommand("mkdir -p " + getFixedPath());
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            boolean mkdirs = result[STDERR].length() == 0;
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
                        + getFixedPath());
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            return result[STDERR].length() == 0;
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
                result = execCommand("chmod a-w " + getFixedPath());
            } catch (Exception e) {
                throw new GATInvocationException("sshtrilead", e);
            }
            return result[STDERR].length() == 0;
        }
    }

    // protected void copyDirContents(URI destination)
    // throws GATInvocationException {
    // // list all the files and copy recursively.
    // if (logger.isInfoEnabled()) {
    // logger.info("copyDirectory contents '" + fixedURI + "' to '"
    // + destination + "'");
    // }
    // File[] files = (File[]) listFiles();
    // if (files == null) {
    // if (logger.isInfoEnabled()) {
    // logger.info("copyDirectory: no files in src directory: "
    // + fixedURI);
    // }
    // return;
    // }
    // for (File file : files) {
    // FileInterface f = file.getFileInterface();
    // if (logger.isInfoEnabled()) {
    // logger.info("copyDirectory: file to copy = " + f);
    // }
    // try {
    // f.copy(new URI(destination + "/" + f.getName()));
    // } catch (URISyntaxException e) {
    // // would not happen
    // }
    // }
    // }
}
