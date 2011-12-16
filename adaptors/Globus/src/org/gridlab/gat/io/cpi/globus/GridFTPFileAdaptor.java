package org.gridlab.gat.io.cpi.globus;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.FTPClient;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.exception.ServerException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.ScheduledExecutor;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class GridFTPFileAdaptor extends GlobusFileAdaptor {

    public static String getDescription() {
        return "The GridFTP File Adaptor implements the File object using Globus GSIFTP.";
    }

    public static Preferences getSupportedPreferences() {
        Preferences p = GlobusFileAdaptor.getSupportedPreferences();
        p.put("ftp.connection.protection", "<default taken from globus>");
        p.put("gridftp.authentication.retry", "0");
        return p;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "globus", "gsiftp", "file", ""};
    }
    
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = GlobusFileAdaptor
                .getSupportedCapabilities();
        capabilities.put("exists", true);
        capabilities.put("move", true);
        return capabilities;
    }
    
    protected static Logger logger = LoggerFactory.getLogger(GridFTPFileAdaptor.class);

    static boolean USE_CLIENT_CACHING = true;
    
    /**
     * Static inner class that wraps a {@link FTPClient} for caching usage.
     * 
     * @author Stefan Bozic
     */
    private static class CachedFTPClient {

        /** The {@link FTPClient} to cache. */
        private FTPClient client = null;

        /** Indicates the {@link Date} when this instance is inserted into the cache. */
        private Date lastUsage = new Date();

        /**
         * Constructor 
         * @param client the instance to wrap.
         */
        public CachedFTPClient(FTPClient client) {
            this.client = client;
        }

        /**
         * Return the {@link Date} of the last usage of the client
         * @return the {@link Date} of the last usage of the client
         */
        public Date getLastUsage() {
            return lastUsage;
        }

        /**
         * Return the wrapped {@link FTPClient} instance.
         * 
         * @return the client
         */
        public FTPClient getClient() {
            return client;
        }
    }

    /** The connection cache. */
    private static Hashtable<String, CachedFTPClient> clienttable = new Hashtable<String, CachedFTPClient>();

    /** This map is used to synchronize file operation per host. */
    private static Hashtable<String, ReentrantLock> lockTable = new Hashtable<String, ReentrantLock>();
    
    /** Indicates that {@link GAT#end()} has been called.*/
    private static boolean end = false;
    
    /** Perform cleanup in this interval. */
    public static final int CLEANUP_INTERVAL = 60 * 1000;
    
    /** Connection lifetime in the cache. */
    public static final int CONNECTION_LIFETIME_IN_CACHE = 120 * 1000;
    
    private static boolean cleanupStarted = false;
    
    /**
     * Runnable that perform cleanup operation on the Client cache.
     */
    private static Runnable cacheCleanupRunnable;
    
    static {
        if (USE_CLIENT_CACHING) cacheCleanupRunnable = new Runnable() {
            public void run() {
                List<String> clientToRemove = new ArrayList<String>();
                Date now;

                if (end) {
                    ScheduledExecutor.remove(this);
                    return;
                }

                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                logger.debug("Run Cache Cleanup");
                synchronized (clienttable) {
                    Set<String> keys = clienttable.keySet();
                    now = new Date();
                    clientToRemove.clear();

                    for (String key : keys) {
                        CachedFTPClient cachedClient = clienttable.get(key);
                        if (cachedClient != null) {
                            long timespan = now.getTime() - cachedClient.getLastUsage().getTime();

                            if (timespan > CONNECTION_LIFETIME_IN_CACHE) {
                                clientToRemove.add(key);
                            }
                        }
                    }

                    if (clientToRemove.size() == 0) {
                	if (logger.isDebugEnabled()) {
                	    logger.debug("No FTPClients need to remove from the cache!");
                	}
                    }

                    for (String key : clientToRemove) {
                        CachedFTPClient cachedClient = clienttable.remove(key);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Remove client from cache: " + key);
                        }

                        try {
                            // Wait until the connection is really close
                            if (logger.isDebugEnabled()) {
                        	logger.debug("Close client for: " + key);
                            }
                            cachedClient.getClient().close(true);
                            if (logger.isDebugEnabled()) {
                        	logger.debug("Close client for: " + key + " DONE!");
                            }
                        } catch (Exception e) {
                            if (logger.isDebugEnabled()) {
                        	logger.debug("Cannot close client for: " + key, e);
                            }
                        }
                    }
                }// End synchronized
            }
        };
    };

    
    /**
     * Constructs a GridFTPFileAdaptor instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     * This adaptor only allows a single simultaneous file operation per client, since the
     * total number of open gridftp connections to a host is limited.
     * 
     * To optimize the connection handling, this class provides a cache for opened gridftp
     * clients, which will be cleaned up regularly.
     * 
     * @param location
     *                A URI which represents the URI corresponding to the
     *                physical file.
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this GridFTPFileAdaptor.
     */
    public GridFTPFileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {
        super(gatContext, location);

        /*
         * Don't try to get the credential if we are dealing with a local file.
         * In that case, getting the credential here is too expensive.
         */
        if (location.isCompatible("file") && location.refersToLocalHost()) {
            return;
        }
        /*
         * try to get the credential to see whether we need to instantiate this
         * adaptor alltogether.
         */
        try {
            GlobusSecurityUtils.getGlobusCredential(gatContext, "gridftp",
                    location, DEFAULT_GRIDFTP_PORT);
        } catch (Exception e) {
            throw new GATObjectCreationException("gridftp", e);
        }
    }

    protected URI fixURI(URI in) {
        return fixURI(in, "gsiftp");
    }
    
    public void move(URI dest) throws GATInvocationException {
        URI uri = toURI();
        if (! uri.refersToLocalHost() && ! dest.refersToLocalHost()) {
            if (uri.getScheme().equals(dest.getScheme()) &&
                    uri.getAuthority().equals(dest.getAuthority())) {
                FTPClient client = null;
                try {
                    client = createClient(uri);
                    // setActiveOrPassive(client, gatContext.getPreferences());
                    if (client.exists(dest.getPath())) {
                        String dir = client.getCurrentDir();
                        try {
                            client.changeDir(dest.getPath());
                            client.changeDir(dir);
                            // Success, so dest was a directory.
                            dest = dest.setPath(dest.getPath() + "/" + getName());
                        } catch(Throwable e) {
                            // ignored
                        }
                    }
                    client.rename(getPath(), dest.getPath());
                    isDirCache.remove(uri);
                    return;
                } catch(Throwable e) {
                } finally {
                    if (client != null) {
                        destroyClient(client, uri);
                    }
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
        throw new GATInvocationException("GridFTP: cannot do third party move");
    }

    /**
     * @see GlobusFileAdaptor#mkdirs
     */
    @Override
    public boolean mkdirs() throws GATInvocationException {
        logger.debug("mkdirs()");

        boolean retVal;
        URI src = fixURI(toURI());
        ReentrantLock lock = null;

        try {
            // try to get the lock for this host
            lock = getHostLock(src);
            lock.lock();
            logger.debug("called lock() in mkdirs");                
            retVal = super.mkdirs();

        } finally {
            logger.debug("try to call unlock() in mkdirs");         
            // release the lock for this host
            if (lock != null && lock.isLocked()) {
                lock.unlock();
                logger.debug("called unlock() in mkdirs");      
            }
        }
        return retVal;
    }

    /**
     * @see GlobusFileAdaptor#copy(URI)
     */
    @Override
    public void copy(URI dest) throws GATInvocationException {
        logger.debug("copy(URI dest)");

        URI src = fixURI(toURI());
        ReentrantLock srcLock = null;
        ReentrantLock destLock = null;

        // try to obtain the locks for both hosts

        boolean copyDone = false;

        // Retry this operation every half second
        while (copyDone == false) {
            boolean srcCanLock = false;
            boolean destCanLock = false;

            try {
                srcLock = getHostLock(src);
                destLock = getHostLock(dest);

                srcCanLock = srcLock.tryLock();
                logger.debug("source can be locked: " + srcCanLock);
                destCanLock = destLock.tryLock();
                logger.debug("dest can be locked: " + destCanLock);                              

            } finally {
                // we cannot obtain both locks
                if (!srcCanLock || !destCanLock) {
                    if (srcCanLock) {
                        srcLock.unlock();
                    }

                    if (destCanLock) {
                        destLock.unlock();
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // e.printStackTrace();
                    }
                } else {
                    try {
                        super.copy(dest);
                        copyDone = true;
                    } finally {
                        srcLock.unlock();
                        destLock.unlock();
                    }
                }
            }
        }
    }

    /**
     * Gets the {@link ReentrantLock} to a given host.
     * 
     * @return the monitor object
     * @param uri the uri of the host
     */
    private static ReentrantLock getHostLock(URI uri) {
        final String host = uri.getHost();
        ReentrantLock lock = null;

        synchronized (lockTable) {
            if (null != host) {
                lock = lockTable.get(host);
            }

            if (lock == null) {
                lock = new ReentrantLock(true);

                if (null != host) {
                    lockTable.put(host, lock);
                }
            }
        }
        return lock;
    }



    public boolean exists() throws GATInvocationException {
        if (cachedInfo != null) {
            return true;
        }

        FTPClient client = null;

        try {
            String remotePath = getPath();

            if (logger.isDebugEnabled()) {
                logger.debug("getINFO: remotePath = " + remotePath
                        + ", creating client to: " + toURI());
            }

            client = createClient(toURI());

            if (logger.isDebugEnabled()) {
                logger.debug("exists: client created");
            }
            return client.exists(remotePath);
        } catch (Exception e) {
            throw new GATInvocationException("globus", e);
        } finally {
            if (client != null) {
                destroyClient(client, toURI());
            }
        }
    }
    
    private static void setConnectionOptions(GridFTPClient c,
            Preferences preferences) throws Exception {
        c.setType(GridFTPSession.TYPE_IMAGE);
        // c.setMode(GridFTPSession.MODE_BLOCK);
    }
    

    private static int getProtectionMode(Preferences preferences) {
        String mode = (String) preferences.get("ftp.connection.protection");

        if (mode == null) {
            return -1;
        }

        if (mode.equalsIgnoreCase("clear")) {
            return GridFTPSession.PROTECTION_CLEAR;
        } else if (mode.equalsIgnoreCase("confidential")) {
            return GridFTPSession.PROTECTION_CONFIDENTIAL;
        } else if (mode.equalsIgnoreCase("private")) {
            return GridFTPSession.PROTECTION_PRIVATE;
        } else if (mode.equalsIgnoreCase("safe")) {
            return GridFTPSession.PROTECTION_SAFE;
        } else {
            throw new Error("Illegal channel protection mode: " + mode);
        }
    }

    /**
     * Set security parameters such as data channel authentication (defined by
     * the GridFTP protocol) and data channel protection (defined by RFC 2228).
     * If you do not specify these, data channels are authenticated by default.
     */
    private static void setSecurityOptions(GridFTPClient c,
            Preferences preferences) throws Exception {
        if (isOldServer(preferences)) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("setting localNoChannelAuthentication (for old servers)");
            }

            c.setLocalNoDataChannelAuthentication();
        }

        if (noAuthentication(preferences)) {
            if (logger.isDebugEnabled()) {
                logger.debug("setting data channelAuthentication to none");
            }

            c.setDataChannelAuthentication(DataChannelAuthentication.NONE);
        }

        int mode = getProtectionMode(preferences);

        if (mode > 0) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("setting data channel proptection to mode "
                                + mode);
            }

            c.setDataChannelProtection(mode);
        }

        // c.setProtectionBufferSize(16384);
        // c.setDataChannelAuthentication(DataChannelAuthentication.SELF);
        // c.setDataChannelProtection(GridFTPSession.PROTECTION_SAFE);
        // c.setType(GridFTPSession.TYPE_IMAGE); //transfertype
        // c.setMode(GridFTPSession.MODE_EBLOCK); //transfermode
    }

    /**
     * Create an FTP Client.
     * 
     * @param hostURI
     *                the uri of the FTP host
     */
    protected FTPClient createClient(GATContext gatContext,
            Preferences additionalPreferences, URI hostURI)
            throws GATInvocationException, InvalidUsernameOrPasswordException {
        FTPClient client = doWorkCreateClient(gatContext, additionalPreferences, hostURI);
        //Only create a lock when a client has been successfully created.
        if (client != null) {
            URI src = fixURI(hostURI);
            // try to get the lock for this host
            ReentrantLock lock = getHostLock(src);
            lock.lock();
        }
        return client;
    }

    /**
     * Returns a {@link GridFTPClient} instance from the cache if an entry to the key exists.
     * Returns <code>null</code> if there is no cache value for the given key. 
     * 
     * @param key indicates an instance in the cache.
     * @return a {@link GridFTPClient} instance from the cache.
     */
    private static GridFTPClient getFromCache(String key) {
        logger.debug("getFromCache( " + key + " )");
        CachedFTPClient cachedClient;

        synchronized (clienttable) {
            if (clienttable.containsKey(key)) {
                logger.debug("getFromCache=true");
                cachedClient = clienttable.remove(key);
                return (GridFTPClient) cachedClient.getClient();
            } else {
                logger.debug(" getFromCache=false");
            }
        }
        return null;
    }



    /**
     * Puts a {@link GridFTPClient} instance in the cache, but only if the cache contains
     * no other value for key.
     * Returns <code>true</code> if the client has been successfully stored inside the cache,
     * returns <code>false</code> otherwise. 
     * 
     * @param key indicates an instance in the cache.
     * @param c the client to put inside the cache.
     * @return <code>true</code> if the client has been successfully stored inside the cache,
     * returns <code>false</code> otherwise.
     */
    private static boolean putInCache(String key, FTPClient c) {
	if (logger.isDebugEnabled()) {
	    logger.debug("putInCache( " + key + " )");
	}

        synchronized (clienttable) {
            if (!clienttable.containsKey(key)) {
                CachedFTPClient cachedClient = new CachedFTPClient(c);
                clienttable.put(key, cachedClient);
                if (logger.isDebugEnabled()) {
                    logger.debug("putInCache: true");  
                }
                if (! cleanupStarted) {
                    cleanupStarted = true;
                    ScheduledExecutor.schedule(cacheCleanupRunnable, CONNECTION_LIFETIME_IN_CACHE,
                            CLEANUP_INTERVAL);
                }
                return true;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("putInCache: false");
        }
        return false;
    }

    /**
     * Returns a {@link GridFTPClient} instance, configured for the given parameters.
     * 
     * @param context the context with security option etc.
     * @param additionalPreferences attributes for the connection
     * @param hostURI the host to connect to
     * @return an instance of {@link GridFTPClient} to the given parameters
     * @throws GATInvocationException
     * @throws InvalidUsernameOrPasswordException
     */
    protected static GridFTPClient doWorkCreateClient(GATContext context, Preferences additionalPreferences, URI hostURI)
    throws GATInvocationException, InvalidUsernameOrPasswordException {
        try {
            GATContext gatContext = context;
            if (additionalPreferences != null) {
                gatContext = (GATContext) context.clone();
                gatContext.addPreferences(additionalPreferences);
            }
            GSSCredential credential = GlobusSecurityUtils.getGlobusCredential(gatContext, "gridftp", hostURI,
                    DEFAULT_GRIDFTP_PORT);

            if (logger.isDebugEnabled()) {
                logger.debug("credential: \n"
                        + (credential == null ? "NULL" : ((GlobusGSSCredentialImpl) credential).getGlobusCredential()
                                .toString()));
            }

            String host = hostURI.resolveHost();
            int port = hostURI.getPort(DEFAULT_GRIDFTP_PORT);

            if (logger.isDebugEnabled()) {
                logger.debug("doWorkCreateClient() " + host + ":" + port);
            }

            GridFTPClient client = null;

            if (USE_CLIENT_CACHING) {
                String cacheKey = getCacheKey(hostURI, credential.getName().toString());
                client = getFromCache(cacheKey);
                if (client != null) {
                    try {
                        // test if the client is still alive
                        // this is needed to check if the socket is still open
                        client.getCurrentDir();

                        if (logger.isDebugEnabled()) {
                            logger.debug("using cached client");
                        }
                    } catch (Exception except) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("could not reuse cached client: ", except);
                        }

                        client = null;
                    }
                }
            }

            if (client == null) {
                client = createNewFtpClient(host, port, credential, gatContext);
            }

            return client;
        } catch (Exception e) {
            throw new GATInvocationException("gridftp", e);
        }
    }

    /**
     * The key of the caching table is build up from the hostUri and the
     * User-DN-Name.
     * 
     * @param hostURI the {@link URI} of the host to connect to
     * @param userName the user name 
     * @return the key for the connection cache
     * @throws GSSException
     */
    private static String getCacheKey(URI hostURI, String userName) throws GSSException {
        return hostURI.getAuthority() + "@@" + userName;
    }

    /**
     * Creates a new instance of {@link GridFTPClient}.
     * 
     * @param host the name of the host to connect to
     * @param port the number of the port to connect to
     * @param credential the user credentials
     * @param gatContext the {@link GATContext}
     * @return an instance of GridFTPClient
     * @throws Exception an exception that might occurs
     */
    private static GridFTPClient createNewFtpClient(String host, int port, GSSCredential credential,
            GATContext gatContext) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("createNewFtpClient( " + host + " ");
            logger.debug("setting up a client to host: '" + host + "' at port: '" + port + "'");                    
        }

        GridFTPClient client = null;

        try {
            client = new GridFTPClient(host, port);
        } catch (Exception e) {
            logger.debug("Error creating Client to " + host, e);
            throw e;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("authenticating, preferences=" + gatContext.getPreferences());
        }

        setSecurityOptions(client, gatContext.getPreferences());

        if (logger.isDebugEnabled()) {
            logger.debug("security options set");
        }

        // authenticate to the server
        int retry = 1;
        String tmp = (String) gatContext.getPreferences().get("gridftp.authenticate.retry");
        if (logger.isDebugEnabled()) {
            logger.debug("gridftp.authenticate.retry=" + tmp);
        }
        if ((tmp != null)) {
            try {
                retry = Integer.parseInt(tmp);
            } catch (NumberFormatException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("failed to parse value '" + tmp + "' for key 'gridftp.authenticate.retry'");
                }
            }
        }
        for (int i = 0; i < retry; i++) {
            try {
                client.authenticate(credential);
                if (logger.isDebugEnabled()) {
                    logger.debug("authenticating done using credential " + credential);
                }
            } catch (ServerException se) {
                if (se.getMessage().contains("451 active connection to server failed")) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    continue;
                }
            }
            break;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("setting channel options");
        }

        setConnectionOptions(client, gatContext.getPreferences());

        if (logger.isDebugEnabled()) {
            logger.debug("done");
        }

        return client;
    }

    /**
     * @see GlobusFileAdaptor#destroyClient(GATContext, FTPClient, URI,
     *      Preferences)
     */
    @Override
    protected void destroyClient(GATContext context, FTPClient c, URI hostURI)
        throws CouldNotInitializeCredentialException, CredentialExpiredException,
            InvalidUsernameOrPasswordException {
        logger.debug("destroyClient");
        try {
            if (c != null) {
                doWorkDestroyClient(context, c, hostURI);
            }
        } finally {
            URI src = fixURI(hostURI);
            ReentrantLock lock = getHostLock(src);
            if (lock != null && lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    /**
     * 
     * @param context
     * @param c
     * @param hostURI
     * @param preferences
     * @throws CouldNotInitializeCredentialException an Exception that might
     *             occurs
     * @throws CredentialExpiredException an Exception that might occurs
     * @throws InvalidUsernameOrPasswordException an Exception that might occurs
     */
    protected static void doWorkDestroyClient(GATContext context, FTPClient c, URI hostURI)
    throws CouldNotInitializeCredentialException, CredentialExpiredException,
    InvalidUsernameOrPasswordException {

        if (logger.isDebugEnabled()) {
            logger.debug("doWorkDestroyClient");
        }
        
        if (! USE_CLIENT_CACHING || hostURI == null) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("FTPClient.close()");
                }
                c.close(true);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("doWorkDestroyClient, closing client, got exception (ignoring): " + e);
                }
                logger.debug("Cannot close connection", e);
            }
            return;
        }

        GSSCredential credential = GlobusSecurityUtils.getGlobusCredential(context, "gridftp", hostURI,
                DEFAULT_GRIDFTP_PORT);

        String cacheKey = null;

        try {
            cacheKey = getCacheKey(hostURI, credential.getName().toString());
        } catch (GSSException e1) {
            logger.error("Cannot obtain credential to create cache key.", e1);
        }

        if (null == cacheKey || !putInCache(cacheKey, c)) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("FTPClient.close()");
                }
                c.close(true);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("doWorkDestroyClient, closing client, got exception (ignoring): " + e);
                }
                logger.debug("Cannot close connection", e);
            }
        }
    }

    public static void end() {
        if (logger.isDebugEnabled()) {
            logger.debug("end of gridftp adaptor");
        }

        USE_CLIENT_CACHING = false;
        deleteCache();
        ScheduledExecutor.remove(cacheCleanupRunnable);
    }
    
    /**
     * Deletes the {@link GridFTPClient} cache.
     */
    private static void deleteCache() {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteCache()");
        }
        
        if (clienttable == null) {
            return;
        }
        
        synchronized(clienttable) {

            Enumeration<CachedFTPClient> e = clienttable.elements();

            while (e.hasMoreElements()) {
                CachedFTPClient cachedClient = e.nextElement();
                FTPClient c = cachedClient.getClient();

                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("end of gridftp adaptor, closing client");
                    }

                    c.close(true);
                } catch (Exception x) {
                    if (logger.isDebugEnabled()) {
                        logger
                        .debug("end of gridftp adaptor, closing client, got exception (ignoring): "
                                + x);
                    }

                    // ignore
                }
            }
        }

        clienttable.clear();
        clienttable = null;
    }
}
