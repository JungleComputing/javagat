package org.gridlab.gat.resources.security.gliteMultiUser;

import org.globus.myproxy.MyProxy;
import org.ietf.jgss.GSSCredential;

/**
 * This class supply methods for myproxy related security issues. 
 * 
 * @author Stefan Bozic
 */
public class MyProxySecurityUtils {

	/**
	 * Retrieve GSSCredential from a MyProxyServer
	 * 
	 * @param host hostname of the MyProxy Server
	 * @param port port of the MyProxy Server
	 * @param user username under which the credential is stored
	 * @param password associated password
	 * 
	 * @return retrieved GSSCredential
	 */
	public static GSSCredential getCredentialFromMyProxyServer(String host, int port, String user, String password) {// NOPMD
		try {
			if (port < 0) {
				port = MyProxy.DEFAULT_PORT;
			}

			MyProxy proxy = new MyProxy();
			proxy.setHost(host);
			proxy.setPort(port);

			GSSCredential hostGSSCred = null;

			GSSCredential credRetrieved = proxy.get(hostGSSCred, user, password, GliteSecurityUtils.STANDARD_NEW_PROXY_LIFETIME);
			return credRetrieved;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);// NOPMD
		}
	}	
	
}
