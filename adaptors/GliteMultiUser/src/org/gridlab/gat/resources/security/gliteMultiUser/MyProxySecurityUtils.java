package org.gridlab.gat.resources.security.gliteMultiUser;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.GATContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class supply methods for myproxy related security issues. 
 * 
 * @author Stefan Bozic
 */
public class MyProxySecurityUtils {

    protected static Logger logger = LoggerFactory.getLogger(MyProxySecurityUtils.class);

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
	public static GSSCredential getCredentialFromMyProxyServer(GATContext gatContext, String host, int port, String user, String password) throws CouldNotInitializeCredentialException {// NOPMD
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

            
//            return proxy.get(user, password, 2 /* lifetime */);
        } catch (MyProxyException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getting credential from MyProxyServer failed: "
                        + e);
            }

            throw new CouldNotInitializeCredentialException(
                    "getCredentialFromMyProxyServer", e);
        }
	}	
	
}
