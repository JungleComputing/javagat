package org.gridlab.gat.io.cpi.globus;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.FTPClient;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.exception.ServerException;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
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
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.gridlab.gat.security.globus.VomsSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides {@link File} operations via an {@link GridFTPClient} to GAT.
 * 
 * Locking: Because of a limited number of allowed open gridftp connections for a client to a gridftp server
 * (default=10, but this can be defined by the resource administrator), this adaptor allows only one File-operation to a
 * gridftp server at the same time. This will be forced by using an {@link ReentrantLock}. The locks are stored in a
 * {@link Hashtable} which are accessible by the hostname.
 * 
 * Caching: To optimize the Connection Handling, this class provides a Cache for opened {@link GridFTPClient}. The
 * clients will be stores in a {@link Hashtable} where the userId and the hostname are used as key and the
 * {@link GridFTPClient} as value. The cache will automatically cleanup unused {@link GridFTPClient} every {@literal
 * GridFTPFileAdaptor#CLEANUP_INTERVALL} by a special thread.
 * 
 * @author Stefan Bozic
 */
@SuppressWarnings("serial")
public class GridFTPFileAdaptor extends GlobusFileAdaptor {

	/** The logger instance. */
	protected static Logger logger = LoggerFactory.getLogger(GridFTPFileAdaptor.class);

	/** Flag that indicated that this adaptor is configured to cache {@link FTPClient} */
	protected static boolean USE_CLIENT_CACHING = true;

	/** CONSTANT */
	public static final String HOSTNAME_USER_SEPARATOR = "@@";

	/** The cache. */
	private static Hashtable<String, CachedFTPClient> clienttable = new Hashtable<String, CachedFTPClient>();

	/** This map is used to synchronize file operation per host. */
	private static Hashtable<String, ReentrantLock> lockTable = new Hashtable<String, ReentrantLock>();

	/** Indicated that {@link GAT#end()} has been called. */
	private static boolean end = false;

	/** Perform cleanup in this interval */
	public static final int CLEANUP_INTERVALL = 60 * 1000;

	/** Connection lifetime in the Cache */
	public static final int CONNECTION_LIFETIME_IN_CACHE = 120 * 1000;

	/**
	 * Shutdown hook for closing all open
	 */
	private static Runnable shutdownRunnable = new Runnable() {

		public void run() {
			logger.info("ShutdownHook: Delete the cache and close all opened FTPClients.");
			deleteCache();
		}

	};

	// Add a shutdown hook to the Runtime
	static {
		if (USE_CLIENT_CACHING) {
			Thread shutdownThread = new Thread(shutdownRunnable, "ShutdownHookGridFTPFileAdaptor");
			Runtime.getRuntime().addShutdownHook(shutdownThread);
		}
	}

	/**
	 * Runnable that perform cleanup operation on the Client cache.
	 */
	private static Runnable cacheCleanupRunnable = new Runnable() {

		public void run() {
			List<String> clientToRemove = new ArrayList<String>();
			Date now;

			// Do this until end() has been called
			while (end == false) {
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
						logger.debug("No FTPClients need to remove from the cache!");
					}

					for (String key : clientToRemove) {
						CachedFTPClient cachedClient = clienttable.remove(key);
						logger.debug("Remove client from cache: " + key);

						try {
							// Wait until the connection is really close
							logger.debug("Close client for: " + key);
							cachedClient.getClient().close(true);
							logger.debug("Close client for: " + key + " DONE!");
						} catch (Exception e) {
							logger.warn("Cannot close client for: " + key);
						}

						cachedClient = null;
					}
				}// End synchronized

				try {
					logger.debug("Sleep " + CLEANUP_INTERVALL + " ms");
					Thread.sleep(CLEANUP_INTERVALL);
				} catch (InterruptedException e) {
					logger.debug("Sleeping Interrupted! Stop Cleanup Thread.");
					return;
				}
			}

		}
	};

	/** This thread perform cache cleanup operations. */
	private static Thread cacheCleaner = null;

	// Start the cache-cleanup-thread.
	static {
		if (USE_CLIENT_CACHING) {
			cacheCleaner = new Thread(cacheCleanupRunnable);
			cacheCleaner.start();
		}
	}

	/**
	 * Constructs a LocalFileAdaptor instance which corresponds to the physical file identified by the passed URI and
	 * whose access rights are determined by the passed GATContext.
	 * 
	 * @param location A URI which represents the URI corresponding to the physical file.
	 * @param gatContext A GATContext which is used to determine the access rights for this LocalFileAdaptor.
	 * @throws GATObjectCreationException An exception that might occurs.
	 */
	public GridFTPFileAdaptor(GATContext gatContext, URI location) throws GATObjectCreationException {
		super(gatContext, location);

		if (!location.isCompatible("gsiftp") && !location.isCompatible("file")) {
			throw new AdaptorNotApplicableException("cannot handle this URI: " + location);
		}

		/*
		 * Don't try to get the credential if we are dealing with a local file. In that case, getting the credential
		 * here is too expensive.
		 */
		if (location.isCompatible("file") && location.refersToLocalHost()) {
			return;
		}
		/*
		 * try to get the credential to see whether we need to instantiate this adaptor alltogether.
		 */
		try {
			GlobusSecurityUtils.getGlobusCredential(gatContext, "gridftp", location, DEFAULT_GRIDFTP_PORT);
		} catch (Exception e) {
			throw new GATObjectCreationException("gridftp", e);
		}
	}

	/**
	 * Sets gsiftp as protocoll for the given {@link URI}. {@link FileCpi#fixURI(URI, String)} does also some additional
	 * modification to the {@link URI}.
	 */
	protected URI fixURI(URI in) {
		return fixURI(in, "gsiftp");
	}

	/**
	 * Sets connection options to a {@link GridFTPClient}.
	 * 
	 * @param c the client to configure
	 * @param preferences configuration attributes
	 * 
	 * @throws Exception An exception that might occurs.
	 */
	private static void setConnectionOptions(GridFTPClient c, Preferences preferences) throws Exception {
		c.setType(GridFTPSession.TYPE_IMAGE);
		// c.setMode(GridFTPSession.MODE_BLOCK);
	}

	/**
	 * Returns a {@link Preferences} instance with the supported attributes, e.g. the number of authentication retries.
	 * 
	 * @return a {@link Preferences} instance with the supported attributes.
	 */
	public static Preferences getSupportedPreferences() {
		Preferences p = GlobusFileAdaptor.getSupportedPreferences();
		p.put("gridftp.authentication.retry", "0");
		return p;
	}

	/**
	 * Set security parameters such as data channel authentication (defined by the GridFTP protocol) and data channel
	 * protection (defined by RFC 2228). If you do not specify these, data channels are authenticated by default.
	 * 
	 * @param c the client
	 * @param preferences might include configuration settings
	 * 
	 * @throws Exception An exception that might occurs.
	 */
	private static void setSecurityOptions(GridFTPClient c, Preferences preferences) throws Exception {
		if (isOldServer(preferences)) {
			if (logger.isDebugEnabled()) {
				logger.debug("setting localNoChannelAuthentication (for old servers)");
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
				logger.debug("setting data channel proptection to mode " + mode);
			}

			c.setDataChannelProtection(mode);
		}
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

		// Retry this operation every second
		while (copyDone == false) {
			boolean srcCanLock = false;
			boolean destCanLock = false;

			try {
				srcLock = createHostLock(src);
				destLock = createHostLock(dest);

				srcCanLock = srcLock.tryLock();
				logger.debug("source can be locked: " + srcCanLock);
				destCanLock = destLock.tryLock();
				logger.debug("dest can be locked: " + srcCanLock);

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
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						logger.warn("An exception occurs while retrying to obtain the locks for copying.", e);
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
	 * Returns a {@link ReentrantLock} to a given host. This method will create a new {@link ReentrantLock} if it doesnt
	 * exists in the lock table
	 * 
	 * @return the monitor object
	 * @param uri the uri of the host
	 */
	private static ReentrantLock createHostLock(URI uri) {
		String host = uri.getHost();
		ReentrantLock lock = null;

		synchronized (lockTable) {
			if (null != host && !host.isEmpty()) {
				lock = lockTable.get(host);
			} else {
				host = "localhost";
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

	/**
	 * Returns the {@link ReentrantLock} to a given host from the lock table
	 * 
	 * @return the reentrant lock
	 * @param uri the uri of the host
	 */
	private static ReentrantLock getHostLock(URI uri) {
		String host = uri.getHost();
		ReentrantLock lock = null;

		synchronized (lockTable) {
			if (null != host && !host.isEmpty()) {
				lock = lockTable.get(host);
			} else {
				host = "localhost";
				lock = lockTable.get(host);
			}
		}

		return lock;
	}

	/**
	 * Creates an FTP Client to a given URI.
	 * 
	 * This method use {@link ReentrantLock} for allowing only one thread to communicate to an gridftp server at one
	 * time. This is done due to limited connections allowed from one client to a gridftp server.
	 * 
	 * @param hostURI the uri of the FTP host
	 */
	protected FTPClient createClient(GATContext gatContext, Preferences additionalPreferences, URI hostURI)
			throws GATInvocationException, InvalidUsernameOrPasswordException {
		// try to get the lock for this host
		URI src = fixURI(toURI());
		ReentrantLock lock = null;
		lock = createHostLock(src);
		lock.lock();

		return doWorkCreateClient(gatContext, additionalPreferences, hostURI);
	}

	/**
	 * Destroy the {@link FTPClient} or put it in the cache if caching is used. This method will also release the
	 * {@link ReentrantLock} from the host.
	 */
	@Override
	protected void destroyClient(GATContext context, FTPClient c, URI hostURI, Preferences preferences)
			throws CouldNotInitializeCredentialException, CredentialExpiredException,
			InvalidUsernameOrPasswordException {
		logger.debug("destroyClient");

		// First destroy the client, do this before releasing the lock to avoid problems with other threads that might
		// use the same client from cache!
		doWorkDestroyClient(context, c, hostURI, preferences);

		// then remove the lock for the URI
		URI src = fixURI(toURI());
		ReentrantLock lock = null;

		lock = getHostLock(src);

		if (lock != null && lock.isLocked()) {
			lock.unlock();
			logger.debug("called unlock() in mkdirs");
		}
	}

	/**
	 * Return a {@link GridFTPClient} instance from the cache if an entry to the key exists. Return <code>null</code> if
	 * there is no cache value for the given key.
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
	 * Puts a {@link GridFTPClient} instance inside the cache, but only if the cache contains no other value for key.
	 * Returns <code>true</code> if the client has been successfully stored inside the cache, returns <code>false</code>
	 * otherwise.
	 * 
	 * 
	 * @param key indicates an instance in the cache.
	 * @param c the client to put inside the cache.
	 * @return <code>true</code> if the client has been successfully stored inside the cache, returns <code>false</code>
	 *         otherwise.
	 */
	private static boolean putInCache(String key, FTPClient c) {
		logger.debug("putInCache( " + key + " )");

		synchronized (clienttable) {
			if (!clienttable.containsKey(key)) {
				CachedFTPClient cachedClient = new CachedFTPClient(c);
				clienttable.put(key, cachedClient);
				logger.debug("putInCache: " + true);
				return true;
			}
		}

		logger.debug("putInCache: " + false);
		return false;
	}

	/**
	 * Returns a {@link GridFTPClient} instance, configured for the given parameters.
	 * 
	 * @param context the context with security option etc.
	 * @param additionalPreferences attributes for the connection
	 * @param hostURI the host to connect to
	 * @return an instance of {@link GridFTPClient} to the given parameters
	 * @throws GATInvocationException an exception that might occurs
	 * @throws InvalidUsernameOrPasswordException an exception that might occurs
	 */
	protected static GridFTPClient doWorkCreateClient(GATContext context, Preferences additionalPreferences, URI hostURI)
			throws GATInvocationException, InvalidUsernameOrPasswordException {
		try {
			GATContext gatContext = (GATContext) context.clone();
			gatContext.addPreferences(additionalPreferences);
			
			GSSCredential credential = getCredential(context, hostURI);
			
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
							logger.warn("could not reuse cached client: " + except);
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
	 * Retrieves the users credential.
	 * When a vomsServerUrl is set as preference in the {@link GATContext}, the method returns a voms-proxy-credential.
	 * Otherwise a standard globus credential is returned.
	 * 
	 * @param context the gat context
	 * @param hostURI the host to connect to
	 * @return a Credential 
	 * 
	 * @throws GSSException
	 * @throws GATInvocationException
	 */
	private static GSSCredential getCredential(GATContext context, URI hostURI) throws GSSException, GATInvocationException {
		GSSCredential credential = null;
		//If voms parameters are set create a voms proxy, else use standard globus proxy. 
		if (context.getPreferences().containsKey("vomsServerURL")) {
			GlobusCredential globusCred = VomsSecurityUtils.getVOMSProxy(context, true);
			credential = new GlobusGSSCredentialImpl(globusCred, GSSCredential.INITIATE_AND_ACCEPT);
		} 
		else {
			credential = GlobusSecurityUtils.getGlobusCredential(context, "gridftp", hostURI, DEFAULT_GRIDFTP_PORT);
		}
		
		return credential;
	}
	
	/**
	 * The key of the caching table is build up from the hostUri and the User-DN-Name.
	 * 
	 * @param hostURI the {@link URI} of the host to connect to
	 * @param userName the user name
	 * @return the key for the connection cache
	 * @throws GSSException an exception that might occurs
	 */
	private static String getCacheKey(URI hostURI, String userName) throws GSSException {
		return hostURI.getHost() + HOSTNAME_USER_SEPARATOR + userName;
	}

	/**
	 * Creates a new instance of {@link GridFTPClient}.
	 * 
	 * @param host the name of the host to connect to
	 * @param port the number of the port to connect to
	 * @param credential the user credential
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
	 * Destroys the {@link FTPClient} or put it in the cache, if caching is available.
	 * 
	 * @param context the GATContext
	 * @param c the client
	 * @param hostURI the host URI
	 * @param preferences some special GAT Preferences
	 * @throws CouldNotInitializeCredentialException an Exception that might occurs
	 * @throws CredentialExpiredException an Exception that might occurs
	 * @throws InvalidUsernameOrPasswordException an Exception that might occurs
	 */
	protected static void doWorkDestroyClient(GATContext context, FTPClient c, URI hostURI, Preferences preferences)
			throws CouldNotInitializeCredentialException, CredentialExpiredException,
			InvalidUsernameOrPasswordException {

		if (logger.isDebugEnabled()) {
			logger.debug("doWorkDestroyClient");
		}

		//The credential and the context will be needed to retrieve the DN of the user
		//which is part of the key for the ftpclient in the cache
		GATContext gatContext = (GATContext) context.clone();
		gatContext.addPreferences(preferences);

		String cacheKey = null;

		try {
			GSSCredential credential = getCredential(gatContext, hostURI);			
			cacheKey = getCacheKey(hostURI, credential.getName().toString());
		} catch (GSSException e1) {
			logger.error("Cannot obtain credential to create cache key.", e1);
		} catch (GATInvocationException e) {
			logger.error("Cannot obtain credential to create cache key.", e);
		}

		if (!USE_CLIENT_CACHING || null == cacheKey || !putInCache(cacheKey, c)) {
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

	/***
	 * Performs cleanup operations on the {@link FTPClient} cache if {@link GAT#end()} has been called. Removes all
	 * cached {@link FTPClient} from the {@link HashMap} and closes their connection. At least set the {@link HashMap}
	 * to null.
	 */
	public static void end() {
		end = true;

		if (logger.isDebugEnabled()) {
			logger.debug("GridFtpAdaptor.end()");
		}

		// destroy the cache
		USE_CLIENT_CACHING = false;
		deleteCache();

		cacheCleaner.interrupt();
	}

	/**
	 * Delete the {@link GridFTPClient} cache. Closes all open {@link GridFTPClient} and remove the from the
	 * {@link HashMap}. At last set the instance to <code>null</code>
	 */
	private static void deleteCache() {
		if (logger.isDebugEnabled()) {
			logger.debug("deleteCache()");
		}
		if (clienttable == null) {
			return;
		}

		synchronized (clienttable) {
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
						logger.debug("end of gridftp adaptor, closing client, got exception (ignoring): " + x);
					}

					// ignore
				}
			}

			clienttable.clear();
			clienttable = null;
		}
	}

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
		 * 
		 * @param client the instance to wrap.
		 */
		public CachedFTPClient(FTPClient client) {
			this.client = client;
		}

		/**
		 * Return the {@link Date} of the last usage of the client
		 * 
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

}
