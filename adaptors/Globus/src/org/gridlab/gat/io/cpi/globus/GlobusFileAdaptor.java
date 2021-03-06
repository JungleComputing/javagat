package org.gridlab.gat.io.cpi.globus;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.globus.ftp.Buffer;
import org.globus.ftp.DataSink;
import org.globus.ftp.DataSource;
import org.globus.ftp.FTPClient;
import org.globus.ftp.FileInfo;
import org.globus.ftp.HostPort;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.FTPException;
import org.globus.ftp.exception.ServerException;
import org.globus.ftp.vanilla.Reply;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.CommandRunner;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInterface;
import org.gridlab.gat.io.cpi.FileCpi;

@SuppressWarnings("serial")
public abstract class GlobusFileAdaptor extends FileCpi {
    
    public static final int DEFAULT_WAIT_INTERVAL = 10;

    public static Preferences getSupportedPreferences() {
        Preferences p = FileCpi.getSupportedPreferences();
        p.put("ftp.connection.passive", "false");
        p.put("ftp.server.old", "false");
        p.put("ftp.server.noauthentication", "false");
        p.put("file.chmod", "<default is target umask>");
        p.put("ftp.clientwaitinterval", "" + DEFAULT_WAIT_INTERVAL);
        return p;
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("copy", true);
        capabilities.put("lastModified", true);
        capabilities.put("delete", true);
        capabilities.put("list", true);
        capabilities.put("isDirectory", true);
        capabilities.put("isFile", true);
        capabilities.put("createNewFile", true);
        capabilities.put("canRead", true);
        capabilities.put("canWrite", true);
        capabilities.put("length", true);
        capabilities.put("mkdir", true);
        capabilities.put("exists", true);
        capabilities.put("getAbsolutePath", true);
        capabilities.put("renameTo", true);
        return capabilities;
    }

    // Unfortunately, FTPClient.exists() does not give a reliable answer, depending on
    // the FTP server used. So, we cannot use it.
    
    protected static Logger logger = LoggerFactory.getLogger(GlobusFileAdaptor.class);

    static final int DEFAULT_GRIDFTP_PORT = 2811;

    static final int DEFAULT_FTP_PORT = 21;

    static final int DEFAULT_HTTP_PORT = 80;

    static final int DEFAULT_HTTPS_PORT = 443;

    static final int NO_SUCH_FILE_OR_DIRECTORY = 550;

    // cache dir info, getting it can be an expensive operation, especially on
    // old servers.
    static HashMap<URI, Integer> isDirCache = new HashMap<URI, Integer>();

    protected FileInfo cachedInfo = null;
    
    private static class EmptySource implements DataSource {

        public void close() {          
        }

        public Buffer read() {
            return null;
        }
        
        public long totalSize() {
            return 0L;
        }       
    }
    
    static final EmptySource emptySource = new EmptySource();
    
    boolean localFile = false;

    /**
     * Constructs a LocalFileAdaptor instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param location
     *                A URI which represents the URI corresponding to the
     *                physical file.
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this LocalFileAdaptor.
     */
    public GlobusFileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {
        super(gatContext, location);

        cachedInfo = (FileInfo) gatContext.getPreferences().get(
                "GAT_INTERNAL_FILE_INFO");
        gatContext.getPreferences().remove("GAT_INTERNAL_FILE_INFO");
        if (toURI().isCompatible("file") && toURI().refersToLocalHost()) {
            localFile = true;
        }
    }

    static protected boolean isPassive(Preferences preferences) {
        boolean passive;

        String tmp = (String) preferences.get("ftp.connection.passive");

        if ((tmp != null) && tmp.equalsIgnoreCase("false")) {
            passive = false;
        } else {
            passive = true;
        }

        return passive;
    }

    protected abstract URI fixURI(URI in);

    /**
     * Create an FTP Client with the default preferences of this file object.
     * 
     * @param hostURI
     *                the uri of the FTP host
     * 
     */
    protected FTPClient createClient(URI hostURI)
            throws GATInvocationException, InvalidUsernameOrPasswordException {
        return createClient(gatContext, null, hostURI);
    }

    /**
     * Create an FTP Client.
     * 
     * @param hostURI
     *                the uri of the FTP host with the given preferences
     * 
     */
    protected abstract FTPClient createClient(GATContext gatContext,
            Preferences additionalPreferences, URI hostURI)
            throws GATInvocationException, InvalidUsernameOrPasswordException;

    /**
     * Destroy a client that was created with a createClient call. This might,
     * for instance, put the client back in a cache.
     */
    protected abstract void destroyClient(GATContext context, FTPClient c,
            URI hostURI)
        throws CouldNotInitializeCredentialException,
              CredentialExpiredException, InvalidUsernameOrPasswordException;

    /**
     * Destroy a client that was created with a createClient call. This might,
     * for instance, put the client back in a cache.
     */
    protected void destroyClient(FTPClient c,
            URI hostURI)
        throws CouldNotInitializeCredentialException,
              CredentialExpiredException, InvalidUsernameOrPasswordException {
        destroyClient(gatContext, c, hostURI);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#copy(java.net.URI)
     */
    public void copy(URI dest) throws GATInvocationException {
        boolean destIsLocal = dest.isCompatible("file") && dest.refersToLocalHost();
        if (localFile && destIsLocal) {
            if (logger.isDebugEnabled()) {
                logger.debug("Globus file: copy local to local");
            }
            throw new GATInvocationException("cannot copy from local ('"
                    + toURI() + "') to local ('" + dest + "')");
        }

        if (determineIsDirectory()) {
            copyDirectory(gatContext, null, toURI(), dest);
            return;
        }

        if (destIsLocal) {
            if (logger.isDebugEnabled()) {
                logger.debug("Globus file: copy remote to local");
            }

            copyToLocal(fixURI(toURI()), fixURI(dest));

            return;
        }
        
        if (localFile) {
            if (logger.isDebugEnabled()) {
                logger.debug("Globus file: copy local to remote");
            }
            if (recognizedScheme(dest.getScheme(), 
                    (this instanceof GridFTPFileAdaptor
                            ? GridFTPFileAdaptor.getSupportedSchemes()
                                    : FTPFileAdaptor.getSupportedSchemes()
                                    ))) {
                copyToRemote(fixURI(toURI()), fixURI(dest));
                return;
            }
            throw new GATInvocationException("Globus file: remote scheme not recognized: " + dest.getScheme());
        }

        // source is remote, dest is remote.
        if (logger.isDebugEnabled()) {
            logger.debug("Globus file: copy remote to remote");
        }

        copyThirdParty(fixURI(toURI()), fixURI(dest));
        setIsDir(fixURI(dest), false);
    }

    // first try efficient 3rd party transfer.
    // If that fails, try copying using temp file.
    protected void copyThirdParty(URI src, URI dest)
            throws GATInvocationException {
        
        FTPClient srcClient = null;
        FTPClient destClient = null;
        File tmpFile = null;

        try {
            srcClient = createClient(src);
            destClient = createClient(dest);

            HostPort hp = destClient.setPassive();
            srcClient.setActive(hp);

            String remoteSrcFile = src.getPath();
            String remoteDestFile = dest.getPath();

            srcClient.transfer(remoteSrcFile, destClient, remoteDestFile,
                    false, null);
        } catch (Exception e) {
            try {
                // use a local tmp file.
                java.io.File tmp = null;
                tmp = java.io.File.createTempFile("GATgridFTP", ".tmp");
                URI u = new URI("any:///" + tmp.getPath());
                if (logger.isDebugEnabled()) {
                    logger.debug("thirdparty copy failed, using temp file: "
                            + u, e);
                }
                tmpFile = GAT.createFile(gatContext, u);

                copyToLocal(src, u);
                tmpFile.copy(dest);
            } catch (Exception e2) {
                GATInvocationException oops = new GATInvocationException();
                oops.add("Globus file", e);
                oops.add("Globus file", e2);

                throw oops;
            }
        } finally {
            if (srcClient != null) {
        	// kill client, we fiddled with the passive/active settings.
                destroyClient(srcClient, null);
            }
            if (destClient != null) {
        	// kill client, we fiddled with the passive/active settings.
                destroyClient(destClient, null);
            }
            if (tmpFile != null) {
                try {
                    tmpFile.delete();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
    
    public boolean createNewFile() throws GATInvocationException {
        if (exists()) {
            return false;
        }
        URI dest = toURI();
        FTPClient client = createClient(dest);
        try {
            setImage(client);
            setActiveOrPassive(client, gatContext.getPreferences());
            client.put(dest.getPath(), emptySource, null);
            if (gatContext.getPreferences().containsKey("file.chmod")) {
                chmod(client, dest.getPath(), gatContext);
            }
            setIsDir(dest, false);
        } catch (Exception e) {
            throw new GATInvocationException("gridftp", e);
        } finally {
            destroyClient(client, dest);
        }
        return true;
        
    }

    protected void copyToRemote(URI src, URI dest)
            throws GATInvocationException {
        FTPClient client = null;

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("copying from " + src.getPath() + " to "
                        + dest.getPath());
            }

            java.io.File sourceFile = new java.io.File(src.getPath());
            FileInterface destFile = GAT.createFile(gatContext, dest)
                    .getFileInterface();

            if (destFile.isDirectory()
                    || (dest.toString().endsWith(File.separator))) {
                String sourcePath = src.getPath();
                if (sourcePath.endsWith(File.separator)) {
                    sourcePath = sourcePath.substring(0,
                            sourcePath.length() - 1);
                }
                if (sourcePath.length() > 0) {
                    int start = sourcePath.lastIndexOf(File.separator) + 1;
                    String separator = "";
                    if (!dest.toString().endsWith(File.separator)) {
                        separator = File.separator;
                    }
                    try {
                        dest = new URI(dest.toString() + separator
                                + sourcePath.substring(start));
                    } catch (URISyntaxException e) {
                        // should not happen
                    }
                }
            }
            if (gatContext.getPreferences().containsKey("file.create")) {
                if (((String) gatContext.getPreferences().get("file.create"))
                        .equalsIgnoreCase("true")) {
                    destFile = GAT.createFile(gatContext, dest)
                            .getFileInterface();
                    File destinationParentFile = destFile.getParentFile();
                    if (destinationParentFile != null) {
                        destinationParentFile.getFileInterface().mkdirs();
                    }
                }
            }
            if (gatContext.getPreferences().containsKey("file.chmod")) {
                client = createClient(dest);
                java.io.File emptyFile = null;
                try {
                    setImage(client);
                    setActiveOrPassive(client, gatContext.getPreferences());
                    emptyFile = java.io.File.createTempFile(".JavaGAT", null);
                    client.put(emptyFile, dest.getPath(), false); // overwrite
                    
                    chmod(client, dest.getPath(), gatContext);
                } finally {
                    if (emptyFile != null) {
                        emptyFile.delete();
                    }
                    destroyClient(client, dest);
                    client = null;
                }
            }
            client = createClient(dest);
            setActiveOrPassive(client, gatContext.getPreferences());
            setImage(client);
            client.put(sourceFile, dest.getPath(), false); // overwrite
        } catch (Exception e) {
            throw new GATInvocationException("gridftp", e);
        } finally {
            if (client != null) {
                destroyClient(client, dest);
            }
        }
    }

    protected void copyToLocal(URI src, URI dest) throws GATInvocationException {
        FTPClient client = null;

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("copying from " + src.getPath() + " to "
                        + dest.getPath());
            }

            java.io.File destFile = new java.io.File(dest.getPath());

            if (destFile.isDirectory()
                    || (dest.toString().endsWith(File.separator))) {
                String sourcePath = src.getPath();
                if (sourcePath.endsWith(File.separator)) {
                    sourcePath = sourcePath.substring(0,
                            sourcePath.length() - 1);
                }
                if (sourcePath.length() > 0) {
                    int start = sourcePath.lastIndexOf(File.separator) + 1;
                    String separator = "";
                    if (!dest.toString().endsWith(File.separator)) {
                        separator = File.separator;
                    }
                    try {
                        dest = new URI(dest.toString() + separator
                                + sourcePath.substring(start));
                    } catch (URISyntaxException e) {
                        // should not happen
                    }
                }
            }
            if (gatContext.getPreferences().containsKey("file.create")) {
                if (((String) gatContext.getPreferences().get("file.create"))
                        .equalsIgnoreCase("true")) {
                    java.io.File destParentFile = destFile.getParentFile();
                    if (destParentFile != null) {
                        destParentFile.mkdirs();
                    }
                }
            }
            // first create an empty file and set the mode
            if (gatContext.getPreferences().containsKey("file.chmod")) {
                java.io.File empty = new java.io.File(dest.getPath());
                if (!empty.createNewFile()) {
                    // failed;
                }
                try {
                    new CommandRunner("chmod",
                            gatContext.getPreferences().get("file.chmod").toString(),
                            dest.getPath());
                } catch (Throwable t) {
                    // ignore
                }
            }
            client = createClient(src);
            setImage(client);
            setActiveOrPassive(client, gatContext.getPreferences());
            destFile = new java.io.File(dest.getPath());
            client.get(src.getPath(), destFile);
            if (gatContext.getPreferences().containsKey("file.copytime")) {
                if (((String) gatContext.getPreferences().get("file.copytime"))
                        .equalsIgnoreCase("true")) {
                    try {
                        destFile.setLastModified(lastModified());
                    } catch(Throwable e) {
                        // ignored
                    }
                }
            }
        } catch (Exception e) {
            throw new GATInvocationException("gridftp", e);
        } finally {
            if (client != null) {
                destroyClient(client, src);
            }
        }
    }
    
    protected void setImage(FTPClient client) throws GATInvocationException {
        // default version is empty.
    }

    // /*
    // * (non-Javadoc)
    // *
    // * @see org.gridlab.gat.io.File#copy(java.net.URI)
    // */
    // protected void copyToLocal(URI src, URI dest) throws
    // GATInvocationException {
    // FTPClient client = null;
    //
    // // copy from a remote machine to the local machine
    // try {
    // String remotePath = src.getPath();
    // String localPath = dest.getPath();
    // java.io.File localFile = new java.io.File(localPath);
    //
    // if (logger.isDebugEnabled()) {
    // logger.debug("copying from " + remotePath + " to " + localPath);
    // }
    //
    // client = createClient(src);
    // client.getCurrentDir(); // to ensure a command has been executed
    //
    // client.get(remotePath, localFile);
    // } catch (Exception e) {
    // throw new GATInvocationException("gridftp", e);
    // } finally {
    // if (client != null)
    // destroyClient(client, src);
    // }
    // }

    static void chmod(FTPClient client, String path, GATContext gatContext) {
        try {
            Reply r = client.site("CHMOD "
                    + gatContext.getPreferences().get("file.chmod") + " "
                    + path);
            if (r.getMessage().startsWith("250")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("CHMOD "
                            + gatContext.getPreferences().get("file.chmod")
                            + " " + path + " successful");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("CHMOD "
                            + gatContext.getPreferences().get("file.chmod")
                            + " " + path + " failed (no exception)");
                }
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("CHMOD "
                        + gatContext.getPreferences().get("file.chmod") + " "
                        + path + " failed (exception: " + e + ")");
            }
        }
    }

    public long lastModified() throws GATInvocationException {
        FTPClient client = null;

        try {
            client = createClient(toURI());
            return client.getLastModified(getPath()).getTime();
        } catch(ServerException e) {
            if (e.getCode() == ServerException.SERVER_REFUSED) {
                return 0L;
            }
            throw new GATInvocationException("gridftp", e);
        } catch (Exception e) {
            throw new GATInvocationException("gridftp", e);
        } finally {
            if (client != null)
                destroyClient(client, toURI());
        }
    }
    
    private void removeDir(FTPClient client, String path) throws Exception {

        setActiveOrPassive(client, gatContext.getPreferences());

        Vector<?> v = client.list(path);

        for (Object f : v) {
            FileInfo finfo = (FileInfo) f;
            String name = finfo.getName();
            if (name.equals(".") || name.equals("..")) {
                continue;
            }
            name = path + "/" + name;
            if (! finfo.isDirectory()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Removing " + name);
                }
                client.deleteFile(name);
                isDirCache.remove(toURI().setPath(name));
            } else {
                removeDir(client, name);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Removing directory " + path);
        }
        client.deleteDir(path);
        isDirCache.remove(toURI().setPath(path));
    }

    public void recursivelyDeleteDirectory() throws GATInvocationException {
        if (! isDirectory()) {
            throw new GATInvocationException("Not a directory!");
        }
        
        FTPClient client = createClient(toURI());
        
        try {
            removeDir(client, toURI().getPath());
        } catch(GATInvocationException e) {
            throw e;
        } catch(Exception e) {
            throw new GATInvocationException("recursivelyDeleteDirectory", e);
        } finally {
            destroyClient(client, toURI());
        }
    }
    
    public boolean delete() throws GATInvocationException {
        FTPClient client = null;

        try {
            String remotePath = getPath();
            client = createClient(toURI());
            
            if (logger.isDebugEnabled()) {
                logger.debug("Globus: deleting " + remotePath);
            }

            if (isDirectory()) {
                client.deleteDir(remotePath);
            } else {
                client.deleteFile(remotePath);
            }
            isDirCache.remove(toURI());
        } catch (ServerException s) {
            if (s.getCode() == ServerException.SERVER_REFUSED) { // file not
                // found
                if (logger.isDebugEnabled()) {
                    logger.debug("delete failed?", s);
                }
                return false;
            } else {
                throw new GATInvocationException("gridftp", s);
            }
        } catch (Exception e) {
            throw new GATInvocationException("gridftp", e);
        } finally {
            if (client != null)
                destroyClient(client, toURI());
        }

        return true;
    }

    // aarg, the COG returns a flakey name for links.
    protected String getName(FileInfo info) {
        if (info.isSoftLink()) {
            int pos = info.getName().indexOf(" ->");

            if (pos != -1) {
                return info.getName().substring(0, pos);
            }
        }

        return info.getName();
    }

    // first time, don't fiddle with active/passive settings, if it fails, then
    // change it the second time.
    public String[] list() throws GATInvocationException {
        try {
            return list(false);
        } catch (GATInvocationException e) {
            return list(true);
        }
    }

    private String[] list(boolean fiddle) throws GATInvocationException {
        if (!isDirectory()) {
            return null;
        }
        FTPClient client = null;
        client = createClient(toURI());
        String cwd = null;
        try {
            setActiveOrPassive(client, gatContext.getPreferences());
            try {
                cwd = client.getCurrentDir();
            } catch (Exception e) {
                throw new GATInvocationException("gridftp", e);
            }
            if (fiddle) {
                try {
                    if (client.isPassiveMode()) {
                        client.setActive();
                        client.setLocalPassive();
                    } else {
                        client.setLocalActive();
                        client.setPassive();
                    }
                } catch (Exception e) {
                    logger
                    .debug("failed to fiddle with the active/passive settings for the list operation: "
                            + e);
                }
            }
            if (getPath() != null) {
                Vector<FileInfo> list = null;
                try {
                    String path = getPath();
                    if (path.equals("")) {
                        path = "/";
                    }
                    try {
                        cwd = client.getCurrentDir();
                    } catch (Exception e) {
                        throw new GATInvocationException("gridftp", e);
                    }

                    client.changeDir(path);
                    // list = client.list(); // this one gives issues on some
                    // gridftp servers (some used by the d-grid project)
                    // list = client.nlist(); // this one is not guaranteed to be
                    // implemented by the gridftp server.
                    list = listNoMinusD(client, null);
                } catch (ServerException e) {
                    throw new GATInvocationException("Generic globus file adaptor",
                            e);
                } catch (ClientException e) {
                    throw new GATInvocationException("Generic globus file adaptor",
                            e);
                } catch (IOException e) {
                    throw new GATInvocationException("Generic globus file adaptor",
                            e);
                }
                if (list != null) {
                    List<String> result = new ArrayList<String>();
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getName().equals(".")
                                || list.get(i).getName().equals("..")) {
                            continue;
                        } else {
                            result.add(list.get(i).getName());
                        }
                    }
                    return result.toArray(new String[result.size()]);
                }
            }
        } finally {
            if (fiddle) {
        	// Kill this client.
        	destroyClient(client, null);
            } else if (cwd != null) {
        	try {
        	    client.changeDir(cwd);
        	} catch (Throwable e) {
        	    destroyClient(client, null);
        	    client = null;
        	} finally {
        	    if (client != null) {
        		destroyClient(client, toURI());
        	    }
        	}
            } else {
        	destroyClient(client, toURI());
            }
        }
        return null;

    }

    // public String[] oldlist() throws GATInvocationException {
    // FTPClient client = null;
    //
    // try {
    // if (!isDirectory()) {
    // return null;
    // }
    // String remotePath = getPath();
    //
    // client = createClient(toURI());
    //
    // if (!remotePath.equals("")) {
    // client.changeDir(remotePath);
    // }
    //
    // Vector<FileInfo> v = null;
    //
    // // we know it is a dir, so we can use this call.
    // // for some reason, on old servers the list() method returns
    // // an empty list if there are many files.
    // v = listNoMinusD(client, remotePath);
    //
    // Vector<String> result = new Vector<String>();
    //
    // for (int i = 0; i < v.size(); i++) {
    // FileInfo info = ((FileInfo) v.get(i));
    // if (info.getName().equals(".")) {
    // continue;
    // }
    // if (info.getName().equals("..")) {
    // continue;
    // }
    // if (info.getName().startsWith(".") && ignoreHiddenFiles) {
    // continue;
    // }
    // result.add(getName(info));
    // }
    //
    // String[] res = new String[result.size()];
    // for (int i = 0; i < result.size(); i++) {
    // res[i] = (String) result.get(i);
    // }
    // return res;
    // } catch (Exception e) {
    // throw new GATInvocationException("gridftp", e);
    // } finally {
    // if (client != null) {
    // destroyClient(client, toURI());
    // }
    // }
    // }

    // public File[] listFiles() throws GATInvocationException {
    // if (logger.isInfoEnabled()) {
    // logger.info("list files of: " + location);
    // }
    // FTPClient client = null;
    // String CWD = null;
    //
    // try {
    // if (!isDirectory()) {
    // if (logger.isDebugEnabled()) {
    // logger.debug("listFiles: not a directory");
    // }
    // return null;
    // }
    // if (toURI().refersToLocalHost()) {
    // throw new GATInvocationException(
    // "cannot list files of local dir '" + toURI() + "'");
    // }
    // String remotePath = getPath();
    //
    // client = createClient(toURI());
    // CWD = client.getCurrentDir();
    //
    // if (!remotePath.equals("")) {
    // client.changeDir(remotePath);
    // }
    //
    //
    // Vector<?> v = null;
    //
    // // we know it is a dir, so we can use this call.
    // // for some reason, on old servers the list() method returns
    // // an empty list if there are many files.
    // // v = listNoMinusD(client, remotePath);
    // v = client.list();
    //
    // Vector<File> result = new Vector<File>();
    //
    // for (int i = 0; i < v.size(); i++) {
    // FileInfo info = ((FileInfo) v.get(i));
    //
    // if (info.getName().equals(".")) {
    // continue;
    // }
    // if (info.getName().equals("..")) {
    // continue;
    // }
    // if (info.getName().startsWith(".") && ignoreHiddenFiles) {
    // continue;
    // }
    //
    // String uri = location.toString();
    // if (!uri.endsWith("/")) {
    // uri += "/";
    // }
    // uri += getName(info);
    //
    // // Improve the performance of further file accesses to the list.
    // // pass the FileInfo object via the preferences.
    // Preferences additionalPrefs = new Preferences();
    // additionalPrefs.put("GAT_INTERNAL_FILE_INFO", info);
    // result.add(GAT.createFile(gatContext, additionalPrefs, new URI(
    // uri)));
    // }
    //
    // File[] res = new File[result.size()];
    // for (int i = 0; i < result.size(); i++) {
    // res[i] = (File) result.get(i);
    // }
    // return res;
    // } catch (Exception e) {
    // throw new GATInvocationException("gridftp", e);
    // } finally {
    // if (client != null) {
    // if (CWD != null) {
    // try {
    // client.changeDir(CWD);
    // } catch (Exception e) {
    // // ignore
    // }
    // }
    // destroyClient(client, toURI());
    // }
    // }
    // }

    protected FileInfo getInfo() throws GATInvocationException, FileNotFoundException {
        if (cachedInfo != null) {
            return cachedInfo;
        }

        FTPClient client = null;

        try {
            String parent = getParent();
            String remotePath = getPath();

            if (logger.isDebugEnabled()) {
                logger.debug("getINFO: remotePath = " + remotePath
                        + ", creating client to: " + toURI());
            }

            client = createClient(toURI());
            
            setActiveOrPassive(client, gatContext.getPreferences());

            if (logger.isDebugEnabled()) {
                logger.debug("getINFO: client created");
            }

            Vector<?> v;
            
            if (remotePath.equals("") || remotePath.equals("/")) {
                // See if LIST -d can find "."
                v = client.list(parent);
                for (Object o : v) {
                    FileInfo f = (FileInfo) o;
                    if (f.getName().equals(".")) {
                        f.setName(getName());
                        cachedInfo = f;
                        return cachedInfo;
                    }
                }
                throw new GATInvocationException("gridftp: cannot obtain info for " + location);
            }
            
            v = client.list(remotePath);
            if (logger.isDebugEnabled()) {
                logger.debug("client.list() size = " + v.size());
            }
            if (v.size() > 1) {
                // Now we're sure that remotePath was a directory, and
                // the server does not support the -d option.
                // Maybe it has '.'?
                for (int i = 0; i < v.size(); i++) {
                    FileInfo tmp = (FileInfo) v.get(i);
                    if (tmp.getName().equals(".")) {
                        tmp.setName(getName());
                        cachedInfo = tmp;
                        return tmp;
                    }
                }

                // Last resort: try to list the parent directory.
                cachedInfo = tryParent(client, parent);
                if (cachedInfo == null) {
                    throw new GATInvocationException(
                        "gridftp: size of list is not 1 and could not find \".\", remotePath = "
                                + remotePath + ", list is: " + v);
                }
                return cachedInfo;
            }
            if (v.size() == 1) {
                // Now, what does this mean? If the server recognizes the -d
                // flag, we have the right entry. But we could also have listed
                // the contents of a directory which contains a single entry.
                FileInfo tmp = (FileInfo) v.get(0);
                // Device files get ? as name.
                if (tmp.isDevice()) {
                    tmp.setName(remotePath);
                }
                if (tmp.getName().equals(remotePath)) {
                    cachedInfo = tmp;
                    return tmp;
                }
                // Some servers don't include the path in the result.
                if (tmp.getName().equals(getName())) {
                    // This might be the right entry, but it also might indicate
                    // a file or directory within the listed one, with the same name.
                    // TODO! Fix this. But how?
                    cachedInfo = tmp;
                    return tmp;
                }
            }
            // Here, v.size() <= 1 and we have, as a last resort, to list the
            // parent directory. If v.size() == 0, this could mean that
            // remotePath indicates an empty directory, or that it does not
            // exist.
            cachedInfo = tryParent(client, parent);
            if (cachedInfo == null) {
                throw new GATInvocationException(
                    "gridftp: size of list <= 1 and could not list parent, remotePath = "
                            + remotePath);
            }
            return cachedInfo;
        } catch (Throwable e) {
            logger.debug("getInfo() got exception", e);
            if (e instanceof ServerException) {
                if (((ServerException) e).getCode() == ServerException.SERVER_REFUSED) {
                    // This may happen when the file does not exist ...
                    // don't use this client anymore.
                    destroyClient(client, null);
                    client = null;
                    FileNotFoundException e1 = new FileNotFoundException("Got exception");
                    e1.initCause(e);
                    throw e1;
                }
            }
            if (e instanceof FileNotFoundException) {
                throw (FileNotFoundException) e;
            }
            if (e instanceof GATInvocationException) {
                throw (GATInvocationException) e;
            }
            throw new GATInvocationException("gridftp", e);
        } finally {
            if (client != null)
                destroyClient(client, toURI());
        }
    }

    private FileInfo tryParent(FTPClient client, String parent)
            throws FileNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug("tryParent " + parent);
        }
        try {
            setActiveOrPassive(client, gatContext.getPreferences());
            Vector<FileInfo> v = listNoMinusD(client, parent);

            if (v.size() == 0) {
                throw new FileNotFoundException("File not found: " + location);
            }
        
            for (int i = 0; i < v.size(); i++) {
                FileInfo tmp = v.get(i);
                if (tmp.getName().equals(getName())) {
                    return tmp;
                }
            }
            throw new FileNotFoundException("File not found: " + location);
        } catch(Throwable e) {
            if (e instanceof FileNotFoundException) {
                throw (FileNotFoundException) e;
            }
            return null;
        }
    }

    private boolean isDirectorySlow() throws GATInvocationException {
        // return cached value if we know it.
        int isDirVal = isDir(location);
        if (isDirVal == 0) {
            return false;
        } else if (isDirVal == 1) {
            return true;
        }
        // don't know yet... Try the slow method now

        boolean dir = true;
        String remotePath = getPath();
        if (remotePath.equals("")) {
            remotePath = "/";
        }

        FTPClient client = createClient(toURI());
        
        if (logger.isDebugEnabled()) {
            logger.debug("getINFO: client created");
        }

        try {
            String cwd = null;
    
            try {
                cwd = client.getCurrentDir();
            } catch (Exception e) {
                throw new GATInvocationException("gridftp", e);
            }
    
            try {
                client.changeDir(remotePath);
            } catch (Exception e) {
                // ok, it was not a dir :-)
                dir = false;
            }
    
            try {
                client.changeDir(cwd);
            } catch (Exception e) {
                throw new GATInvocationException("gridftp", e);
            }
 
        } finally {
            if (client != null)
                destroyClient(client, toURI());
        }

        setIsDir(location, dir);
        return dir;
    }

    public boolean isDirectory() throws GATInvocationException {
        logger.debug("Globus isDirectory()");
        // return cached value if we know it.
        int isDirVal = isDir(location);
        if (isDirVal == 0) {
            return false;
        } else if (isDirVal == 1) {
            return true;
        }
        // don't know yet...

        // create a seperate file object to determine whether this file
        // is a directory. This is needed, because the source might be a local
        // file, and some adaptors might not work locally (like gridftp).
        // This goes wrong for local -> remote copies.
        if (localFile) {
            try {
                java.io.File f = new java.io.File(getPath());
                boolean res = f.isDirectory();
                setIsDir(location, res);
                return res;
            } catch (Exception e) {
                throw new GATInvocationException("globus", e);
            }
        } else {
            return realIsDirectory();
        }
    }

    private boolean realIsDirectory() throws GATInvocationException {
        if (logger.isDebugEnabled()) {
            logger.debug("real isDir on " + toURI());
        }

        // First, try the "fast" method.
        try {
            FileInfo info = getInfo();
            if (info.isDirectory()) {
                setIsDir(location, true);
                return true;
            } else if (info.isDevice()) {
                setIsDir(location, false);
                return false;
            } else if (info.isFile()) {
                setIsDir(location, false);
                return false;
            }
            // it can also be a link, so continue with slow method
        } catch(FileNotFoundException e) {
            setIsDir(location, false);
            return false;
        } catch(GATInvocationException e) {
            // ignored, try the slow method.
        }
        if (logger.isDebugEnabled()) {
            logger
            .debug("fast isDirectory failed, falling back to slower version");
        }

        return isDirectorySlow();
    }

    public boolean isFile() throws GATInvocationException {
        return exists() && !isDirectory();
    }

    public boolean canRead() throws GATInvocationException {
        FileInfo info;
        
        try {
            info = getInfo();
        } catch(FileNotFoundException e) {
            return false;
        }
    
        try {
            return info.userCanRead();
        } catch(Throwable e) {
            throw new GATInvocationException("gridftp", e);
        }
    }

    public long length() throws GATInvocationException {
        
        if (cachedInfo != null) {
            if (cachedInfo.isDirectory()) {
                return 0L;
            }
            return cachedInfo.getSize();
        }
        
        FTPClient client = null;

        try {
            if (! exists()) {
                return 0;
            }
            if (isDirectory())
                return 0;
        } catch (Exception e) {
            // Hmm, that did not work.
            // let's assume it is a file, and continue.
        }
        
        client = createClient(toURI());
        try {
            return client.getSize(getPath());
        } catch(FileNotFoundException e) {
            return 0L;
        } catch(Exception e) {
            throw new GATInvocationException("length()", e);
        } finally {
            destroyClient(client, toURI());
        }
    }

    public boolean mkdir() throws GATInvocationException {
        FTPClient client = null;

        String remotePath = getPath();
        client = createClient(toURI());
        try {
            client.makeDir(remotePath);
            if (gatContext.getPreferences().containsKey("file.chmod")) {
                chmod(client, remotePath, gatContext);
            }
            setIsDir(toURI(), true);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("gridftp", e);
            }
            return false;
        } finally {
            if (client != null) {
                destroyClient(client, toURI());
            }
        }

        return true;
    }

    // public boolean mkdirs() throws GATInvocationException {
    // try {
    // FileInterface child = GAT.createFile(gatContext, preferences,
    // location).getFileInterface();
    // if (child.getParentFile() == null) {
    // return child.mkdir();
    // }
    // FileInterface parent = child.getParentFile().getFileInterface();
    // if (parent.exists()) {
    // return child.mkdir();
    // } else {
    // if (!parent.mkdirs()) {
    // return false;
    // }
    // return child.mkdir();
    // }
    // } catch (GATObjectCreationException e) {
    // throw new GATInvocationException("GlobusFileAdaptor", e);
    // }
    // }

    public boolean canWrite() throws GATInvocationException {
        FileInfo info;
        
        try {
            info = getInfo();
        } catch(FileNotFoundException e) {
            return false;
        }
    
        try {
            return info.userCanWrite();
        } catch(Throwable e) {
            throw new GATInvocationException("gridftp", e);
        }
    }


    public String getAbsolutePath() throws GATInvocationException {
        if (getPath().startsWith("/")) {
            return getPath();
        }

        FTPClient client = null;

        try {
            client = createClient(toURI());

            String dir = client.getCurrentDir();

            return dir + "/" + getPath();
        } catch (Exception e) {
            throw new GATInvocationException("gridftp", e);
        } finally {
            if (client != null)
                destroyClient(client, toURI());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#renameTo(org.gridlab.gat.io.File)
     */
    public boolean renameTo(File dest) throws GATInvocationException {
        if (location.getHost().equals(dest.toGATURI().getHost())) {
            FTPClient client = null;
            try {
                client = createClient(location);
                client.rename(getPath(), dest.getPath());
            } catch (Exception e) {
                throw new GATInvocationException("gridftp", e);
            } finally {
                if (client != null)
                    destroyClient(client, location);
            }
            return true;
        } else {
            return super.renameTo(dest);
        }
    }

    static protected boolean isOldServer(Preferences preferences) {
        boolean old;
        String tmp = (String) preferences.get("ftp.server.old");

        if ((tmp != null) && tmp.equalsIgnoreCase("true")) {
            old = true;
        } else {
            old = false;
        }

        return old;
    }

    static protected boolean noAuthentication(Preferences preferences) {
        boolean noAuth;
        String tmp = (String) preferences.get("ftp.server.noauthentication");

        if ((tmp != null) && tmp.equalsIgnoreCase("true")) {
            noAuth = true;
        } else {
            noAuth = false;
        }

        return noAuth;
    }

    protected static void setActiveOrPassive(FTPClient c,
            Preferences preferences) throws GATInvocationException {
        if (isPassive(preferences)) {
            if (logger.isDebugEnabled()) {
                logger.debug("gridftp: using local active / remote PASSIVE");
            }

            /** Assume the local host is behind a firewall */
            try {
                c.setPassive();
                c.setLocalActive();
            } catch (Exception e) {
                throw new GATInvocationException("globus", e);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("gridftp: using local passive / remote ACTIVE");
            }

            try {
                c.setLocalPassive();
                c.setActive();
            } catch (Exception e) {
                throw new GATInvocationException("globus", e);
            }
        }
    }

    /**
     * This method is used for old servers that do not support the "list -d"
     * command. The problem is that is does not work for directories, only for
     * files.
     */
    protected Vector<FileInfo> listNoMinusD(FTPClient c, String filter)
            throws ServerException, ClientException, IOException {
        final ByteArrayOutputStream received = new ByteArrayOutputStream(1000);

        // unnamed DataSink subclass will write data channel content
        // to "received" stream.
        DataSink sink = new DataSink() {
            public void write(Buffer buffer) throws IOException {
                received.write(buffer.getBuffer(), 0, buffer.getLength());
            }

            public void close() throws IOException {
            };
        };

        c.list(filter, null, sink);

        // transfer done. Data is in received stream.
        // convert it to a vector.

        BufferedReader reader = new BufferedReader(new StringReader(received
                .toString()));

        Vector<FileInfo> fileList = new Vector<FileInfo>();
        FileInfo fileInfo = null;
        String line = null;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("total")) {
                continue;
            }

            try {
                fileInfo = new FileInfo(fixListReply(line));
                fileList.addElement(fileInfo);
            } catch (org.globus.ftp.exception.FTPException e) {
                logger
                        .debug("globus file adaptor: WARNING, could not create FileInfo for: "
                                + line);
            }
        }

        return fileList;
    }

    /**
     * Some servers seem to return a strange format directory listing that the
     * COG does not parse. For instance, the NCSA grid ftp server returns
     * something like:
     * 
     * -rw------- 1 ccguser ac DK common 861 Jul 26 16:56 qcrjm.hist
     * 
     * while the cog only parses:
     * 
     * drwxr-xr-x 2 guest other 1536 Jan 31 15:15 run.bat
     * 
     * This method removes the unused tokens from the reply
     */
    public String fixListReply(String reply) throws FTPException {
        StringTokenizer tokens = new StringTokenizer(reply);

        if (logger.isDebugEnabled()) {
            logger.debug("fixing old ftp server list reply: " + reply
                    + "#tokens = " + tokens.countTokens());
        }

        if (tokens.countTokens() < 10) {
            return reply;
        }
        
        // We also get here if the file name itself contains spaces! --Ceriel

        String res = "";

        res += tokens.nextToken(); // permissions
        res += " " + tokens.nextToken(); // number of links
        res += " " + tokens.nextToken(); // owner
        
        String tok1 = tokens.nextToken();
        String tok2 = tokens.nextToken();
        
        // So, if tok2 consists of digits only, we don't change
        // the reply. --Ceriel
        
        if (! onlyDigits(tok2)) {
            if (logger.isDebugEnabled()) {
                logger.debug("COG workaround parsing old ftp server list reply.");
            }
        } else {
            res += " " + tok1;
            res += " " + tok2;
        }
        
        // if there are more tokens, just add them
        while (tokens.hasMoreTokens()) {
            String s = tokens.nextToken();
            if (s.equals("->")) {
                // symbolic link! Skip.
                break;
            }
            res += " " + tokens.nextToken();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("fixed version is: " + res);
        }

        return res;
    }
    
    private boolean onlyDigits(String token) {
        char[] chars = token.toCharArray();
        for (char c : chars) {
            if (! Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * @param location
     * @return 1 if dir, 0 if not, -1 if unknown
     */
    private synchronized static int isDir(URI location) {
        Integer val = isDirCache.get(location);
        if (val == null)
            return -1;

        if (logger.isDebugEnabled()) {
            logger.debug("cached isDir of " + location + " result = " + val);
        }

        if (val.intValue() == 1)
            return 1;
        if (val.intValue() == 0)
            return 0;

        throw new Error("Internal error, illegal value in isDir");
    }

    private synchronized static void setIsDir(URI location, boolean isDir) {
        if (logger.isDebugEnabled()) {
            logger.debug("set cached dir of " + location + " to " + isDir);
        }
        if (isDirCache.size() > 5000) {
            isDirCache.clear();
        }
        int val = -1;
        if (isDir)
            val = 1;
        else
            val = 0;

        isDirCache.put(location, new Integer(val));
    }
}
