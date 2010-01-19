package org.gridlab.gat.resources.security.gliteMultiUser;

import java.util.ArrayList;
import java.util.List;

import org.glite.voms.contact.VOMSProxyBuilder;
import org.glite.voms.contact.VOMSProxyInit;
import org.glite.voms.contact.VOMSRequestOptions;
import org.glite.voms.contact.VOMSServerInfo;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.cpi.gliteMultiUser.GliteConstants;
import org.gridlab.gat.security.MyProxyServerCredentialSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class supply methods for glite related security issues.
 * 
 * @author Stefan Bozic
 */
public class GliteSecurityUtils {

	/** The Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(GliteSecurityUtils.class);

	/** The standard lifetime of a new created proxy is 12 hour. */
	public final static int STANDARD_NEW_PROXY_LIFETIME = 12 * 3600;

	/** The prefix of a voms-proxy file name. */
	public final static String PROXY_PREFIX = "x509_";

	/**
	 * If a proxy is going to be reused it should at least have a remaining
	 * lifetime of 20 minutes. Otherwise the jobs will be rejected by the system
	 */
	public final static int MINIMUM_PROXY_REMAINING_LIFETIME = 20 * 60;

	/**
	 * Retrieves a voms-proxy. Try to reuse an existing voms-proxy from disk if
	 * the remaining lifetime is big enough.
	 * 
	 * @param context The GAT context.
	 * @param useCache marks that the disk should be search for an existing
	 *            voms-proxy
	 * @return a voms-proxy
	 * @throws GATInvocationException an exception that might occurs.
	 */
	public static GlobusCredential getVOMSProxy(GATContext context, boolean useCache) throws GATInvocationException {
		String userVomsProxyLocation = getPathToUserVomsProxy(context);

		GlobusCredential proxyCredential = null;

		if (false == useCache) {
			LOGGER.info("Dont use an existing proxy. Creating a new one.");
			proxyCredential = createVOMSProxy(context);
		} else {
			try {
				LOGGER.info("Reuse an existing proxy.");
				// Read an existing proxy from the filesystem.
				GlobusCredential storedCredential = new GlobusCredential(userVomsProxyLocation);
				LOGGER.info("The remainig lifetime of the proxy is: " + storedCredential.getTimeLeft());

				if (storedCredential.getTimeLeft() > MINIMUM_PROXY_REMAINING_LIFETIME) {
					proxyCredential = storedCredential;
				} else {
					LOGGER.info("The remainig lifetime of the proxy is too small. Create a new proxy.");
					proxyCredential = createVOMSProxy(context);
				}
			} catch (Exception e) {
				LOGGER.info("Cannot read existing proxy from disk. Creating a new one.");
				proxyCredential = createVOMSProxy(context);
			}
		}

		return proxyCredential;
	}

	/**
	 * Creates a voms-proxy.
	 * 
	 * @param context The GAT context
	 * @return <code>true</code> if the voms-proxy has been created successfully
	 * @throws GATInvocationException an exception that might occurs.
	 */
	private static GlobusCredential createVOMSProxy(GATContext context) throws GATInvocationException {
		// Must be a unique ID!
		final String vo = (String) context.getPreferences().get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION);
		final String voServerUrl = (String) context.getPreferences().get(
				GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_URL);
		final String voHostDN = (String) context.getPreferences().get(
				GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_HOST_DN);
		final String voServerPort = (String) context.getPreferences().get(
				GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION_SERVER_PORT);
		final String userVomsProxyLocation = getPathToUserVomsProxy(context);

		List<SecurityContext> securityContexts = context.getSecurityContexts();

		MyProxyServerCredentialSecurityContext myproxyContext = null;

		for (SecurityContext securityContext : securityContexts) {
			if (securityContext instanceof MyProxyServerCredentialSecurityContext) {
				myproxyContext = (MyProxyServerCredentialSecurityContext) securityContext;
			}
		}

		if (null == myproxyContext) {
			return null;
		}

		final String myProxyHost = myproxyContext.getHost();
		final int myProxyPort = myproxyContext.getPort();
		final String myProxyUser = myproxyContext.getUsername();
		final String myProxyPwd = myproxyContext.getPassword();

		// Retrieve the proxy from the myproxy server
		GSSCredential myProxyCred = MyProxySecurityUtils.getCredentialFromMyProxyServer(myProxyHost, myProxyPort,
				myProxyUser, myProxyPwd);

		// Cast the proxy to a globus credential
		GlobusCredential globusCred = ((GlobusGSSCredentialImpl) myProxyCred).getGlobusCredential();

		// Specify the voms settings for creating a voms-proxy
		VOMSServerInfo serverInfo = new VOMSServerInfo();
		serverInfo.setHostName(voServerUrl);
		serverInfo.setHostDn(voHostDN);
		serverInfo.setPort(Integer.valueOf(voServerPort));
		serverInfo.setVoName(vo);

		VOMSRequestOptions requestOptions = new VOMSRequestOptions();
		requestOptions.setVoName(vo);

		ArrayList<VOMSRequestOptions> options = new ArrayList<VOMSRequestOptions>();
		options.add(requestOptions);

		VOMSProxyInit vomsProxyInit = VOMSProxyInit.instance(globusCred);
		vomsProxyInit.setProxyOutputFile(userVomsProxyLocation);
		vomsProxyInit.setProxyType(VOMSProxyBuilder.GT2_PROXY);
		vomsProxyInit.setDelegationType(GSIConstants.DELEGATION_FULL);
		vomsProxyInit.setProxyLifetime(STANDARD_NEW_PROXY_LIFETIME);
		vomsProxyInit.addVomsServer(serverInfo);

		GlobusCredential vomsCredential = vomsProxyInit.getVomsProxy(options);

		return vomsCredential;
	}

	/**
	 * Creates the path to the user voms-proxy on the disk. The userId is stored
	 * as {@link Preferences} in the {@link GATContext}
	 * 
	 * @param context the gat context
	 * @return the path to the user voms-proxy
	 * @throws GATInvocationException an exception that might occurs.
	 */
	public static String getPathToUserVomsProxy(GATContext context) throws GATInvocationException {
		// Must be a unique ID!
		String userId = (String) context.getPreferences().get(GliteConstants.PREFERENCE_PROXY_USER_ID);
		String proxyDirectory = (String) context.getPreferences().get(GliteConstants.PREFERENCE_VOMS_PROXY_DIRECTORY);

		if (null == userId) {
			throw new GATInvocationException(
					"For retrieving the path to a user voms-proxy, there must be a unique userId specified in the GATContext Preferences.");
		}

		return proxyDirectory + PROXY_PREFIX + userId;
	}

}
