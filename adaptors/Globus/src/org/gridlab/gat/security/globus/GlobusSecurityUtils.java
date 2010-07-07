/*
 * Created on Jul 29, 2005
 */
package org.gridlab.gat.security.globus;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.gridforum.jgss.ExtendedGSSManager;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.cpi.SecurityContextUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines globus specific security methods.
 * 
 * @author rob
 * @author Stefan Bozic
 */
public class GlobusSecurityUtils {

	/** LOGGER */
	protected static Logger logger = LoggerFactory.getLogger(GlobusSecurityUtils.class);

	/** The cached default credential for the user. Only for single user usage of GAT! */
	private static GSSCredential cachedDefaultCredential = null;

	/** Indicates whether default credential has been created. */
	private static boolean didDefaultCredential = false;

	/**
	 * Handling of all globus certificates goes through this method.
	 * 
	 * @param context the gat context
	 * @param adaptorName the name of the adaptor which will use the credential
	 * @param location the host/port to create the credential for
	 * @param defaultPort the port for the protocol used by the adaptor
	 * @return a instance of GSSCredential
	 * 
	 * @throws CouldNotInitializeCredentialException an exception that might occurs
	 * @throws CredentialExpiredException an exception that might occurs
	 * @throws InvalidUsernameOrPasswordException an exception that might occurs
	 */
	public static GSSCredential getGlobusCredential(GATContext context, String adaptorName, URI location,
			int defaultPort) throws CouldNotInitializeCredentialException, CredentialExpiredException,
			InvalidUsernameOrPasswordException {
		Object data = SecurityContextUtils.getSecurityUserData(context, adaptorName, "globus", location, defaultPort,
				new GlobusContextCreator());

		GSSCredential c = (GSSCredential) data;

		if (logger.isDebugEnabled()) {
			logger.debug("getGlobusCredential: got credential: \n"
					+ (c == null ? "NULL" : ((GlobusGSSCredentialImpl) c).getGlobusCredential().toString()));
		}

		int remaining = 0;
		try {
			remaining = c.getRemainingLifetime();
		} catch (Exception e) {
			throw new CouldNotInitializeCredentialException("globus", e);
		}

		if (remaining == 0) {
			throw new CredentialExpiredException("globus credential expired");
		}

		return (GSSCredential) data;
	}

	/**
	 * This method returns the default globus credential. The strategy used is as follows.
	 * 
	 * <P>
	 * First, it tries to use the CredentialSecurityContext to retrieve the credential
	 * 
	 * <P>
	 * Next, it tries to read the proxy from the location specified in the "X509_USER_PROXY" environment variable. This
	 * variable is used by the globus commandline tools (e.g., grid-proxy-init, globus-url-copy) as well.
	 * 
	 * <P>
	 * Finally, it tries to get the default proxy from the default location.
	 * 
	 * @return the default globus credential
	 * @throws CouldNotInitializeCredentialException an exception that might occurs
	 * @throws CredentialExpiredException an exception that might occurs
	 */
	protected static synchronized GSSCredential getDefaultCredential() throws CouldNotInitializeCredentialException,
			CredentialExpiredException {

		if (!didDefaultCredential) {
			didDefaultCredential = true;

			// Now, try to get the credential from the specified environment
			// variable
			if (logger.isDebugEnabled()) {
				logger.debug("trying to get credential from location specified in environment");
			}

			String proxyLocation = System.getenv("X509_USER_PROXY");

			if (proxyLocation == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("no credential location found in environment");
				}
			} else {
				cachedDefaultCredential = getCredential(proxyLocation);
			}
			if (cachedDefaultCredential != null) {
				return cachedDefaultCredential;
			}

			// next try to get default credential
			if (logger.isDebugEnabled()) {
				logger.debug("trying to get default credential");
			}

			try {
				// Get the user credential
				ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();

				// try to get default user proxy certificate from file in /tmp
				cachedDefaultCredential = manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
			} catch (GSSException x) {
				if (logger.isDebugEnabled()) {
					logger.debug("default credential failed: " + x);
				}

				// handled below
			}
		}

		if (cachedDefaultCredential == null) {
			throw new CouldNotInitializeCredentialException("globus", new CouldNotInitializeCredentialException(
					"can't get proxy credential (did you do a grid-proxy-init?)"));
		}

		return cachedDefaultCredential;
	}

	/**
	 * Trys to get {@link GSSCredential} from a given path.
	 * 
	 * @param file the path to the credential.
	 * 
	 * @return an instance of {@link GSSCredential}
	 */
	public static GSSCredential getCredential(String file) {
		GSSCredential credential = null;

		try {
			GlobusCredential globusCred = new GlobusCredential(file);
			credential = new GlobusGSSCredentialImpl(globusCred, GSSCredential.INITIATE_AND_ACCEPT);
		} catch (Throwable t) {
			if (logger.isDebugEnabled()) {
				logger.debug("loading credential from file " + file + " failed: " + t);
			}

			return null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("loaded credential from file " + file);
		}

		return credential;
	}

	/**
	 * try to get a GSSCredential from a MyProxyServer
	 * 
	 * @deprecated this method is deprecated please use
	 *             {@link #getCredentialFromMyProxyServer(GATContext, String, int, String, String)}
	 * 
	 * @param host hostname of the MyProxy Server
	 * @param port port of the MyProxy Server
	 * @param user username under which the credential is stored
	 * @param password associated password
	 * @return retrieved GSSCredential
	 * @throws CouldNotInitializeCredentialException an exception that might occurs
	 */
	public static GSSCredential getCredentialFromMyProxyServer(String host, int port, String user, String password)
			throws CouldNotInitializeCredentialException {
		return getCredentialFromMyProxyServer(null, host, port, user, password);
	}

	/**
	 * try to get a GSSCredential from a MyProxyServer to use host authentication the context need two parameters:
	 * myproxy.hostcertfile absolute path on local machine to the certificate file myproxy.hostkeyfile absolute path on
	 * local machine to the key file
	 * 
	 * @param gatContext the {@link GATContext} (needed if the MyProxy Server requires host authentication). If null the
	 *            MyProxy Server is accessed anonymous
	 * @param host hostname of the MyProxy Server
	 * @param port port of the MyProxy Server
	 * @param user username under which the credential is stored
	 * @param password associated password
	 * @return retrieved GSSCredential
	 * @throws CouldNotInitializeCredentialException an exception that might occurs
	 */
	public static GSSCredential getCredentialFromMyProxyServer(GATContext gatContext, String host, int port,
			String user, String password) throws CouldNotInitializeCredentialException {
		if (logger.isDebugEnabled()) {
			logger.debug("trying to get credential from MyProxyServer");
		}

		try {
			if (port < 0) {
				port = MyProxy.DEFAULT_PORT;
			}

			MyProxy proxy = new MyProxy();
			proxy.setHost(host);
			proxy.setPort(port);

			GSSCredential hostGSSCred = null;
			if (gatContext != null) {
				String portalCertFile = (String) gatContext.getPreferences().get("myproxy.hostcertfile");
				String portalKeyFile = (String) gatContext.getPreferences().get("myproxy.hostkeyfile");
				logger.info("checking server credential: " + portalCertFile + " /// " + portalKeyFile);
				if ((portalCertFile != null) && (portalKeyFile != null)) {
					try {
						GlobusCredential hostCred = new GlobusCredential(portalCertFile, portalKeyFile);
						hostGSSCred = new GlobusGSSCredentialImpl(hostCred, GSSCredential.INITIATE_AND_ACCEPT);
						logger.info("using Server credential: " + hostGSSCred);
					} catch (GlobusCredentialException e) {
						throw new CouldNotInitializeCredentialException(e.getMessage(), e);
					} catch (GSSException e) {
						throw new CouldNotInitializeCredentialException(e.getMessage(), e);
					}
				}
			}
			GSSCredential credRetrieved = proxy.get(hostGSSCred, user, password, 2 /* lifetime */);
			return credRetrieved;

			// return proxy.get(user, password, 2 /* lifetime */);
		} catch (MyProxyException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("getting credential from MyProxyServer failed: " + e);
			}

			throw new CouldNotInitializeCredentialException("getCredentialFromMyProxyServer", e);
		}
	}
}
