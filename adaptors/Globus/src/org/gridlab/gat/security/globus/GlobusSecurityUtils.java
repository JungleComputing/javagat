/*
 * Created on Jul 29, 2005
 */
package org.gridlab.gat.security.globus;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.gridforum.jgss.ExtendedGSSManager;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.Environment;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.CredentialSecurityContext;
import org.gridlab.gat.security.MyProxyServerCredentialSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextCreator;
import org.gridlab.gat.security.cpi.SecurityContextUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

class GlobusContextCreator implements SecurityContextCreator {

    protected static Logger logger = LoggerFactory
            .getLogger(GlobusContextCreator.class);
    
    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
            URI location) throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        // automatically try and insert the default credential if it was not
        // there.
        GSSCredential cred = GlobusSecurityUtils
                .getDefaultCredential(gatContext);
        CredentialSecurityContext c = new CredentialSecurityContext();
        c.putDataObject("globus", cred);
        return c;
    }

    public Object createUserData(GATContext gatContext, URI location,
            SecurityContext inContext)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        // we need to try to create the credential given the securityContext
        // if it fails, just try the next one on the list.
        if (inContext instanceof CredentialSecurityContext) {
            CredentialSecurityContext c = (CredentialSecurityContext) inContext;
            Object credentialObject = c.getCredential();
            if (credentialObject != null) {
                // Added check if it already is a credential object.
                // If so, just return it. --Ceriel
                if (credentialObject instanceof GSSCredential) {
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("CredentialSecurityContext credential is instance of GSSCredential");
                    }
                    return credentialObject;
                }
                if (credentialObject instanceof byte[]
                        || credentialObject instanceof String) {
                    // if it is of the type String or byte[] we can try to
                    // derive a GSSCredential from it
                    if (credentialObject instanceof String) {
                        if (logger.isDebugEnabled()) {
                            logger
                                    .debug("CredentialSecurityContext credential is instance of String");
                        }
                        try {
                            credentialObject = ((String) credentialObject)
                                    .getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Got exception", e);
                            }
                            return null;
                        }
                    }
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("CredentialSecurityContext credential is instance of byte[]");
                    }
                    try {
                        GlobusCredential globusCred = new GlobusCredential(
                                new ByteArrayInputStream(
                                        (byte[]) credentialObject));
                        GSSCredential result = new GlobusGSSCredentialImpl(
                                globusCred, GSSCredential.INITIATE_AND_ACCEPT);
                        return result;
                    } catch (GlobusCredentialException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Got exception", e);
                        }
                        return null;
                    } catch (GSSException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Got exception", e);
                        }
                        return null;
                    }
                }
            }
        }
        if (inContext instanceof CertificateSecurityContext) {
            CertificateSecurityContext c = (CertificateSecurityContext) inContext;
            String passphrase = c.getPassword();
            String keyFile = c.getKeyfile().toString();
            String certFile = c.getCertfile().toString();
            try {
                GATGridProxyModel model = new GATGridProxyModel();
                GlobusCredential globusCred = model.createProxy(passphrase,
                        certFile, keyFile);
                GSSCredential result = new GlobusGSSCredentialImpl(globusCred,
                        GSSCredential.INITIATE_AND_ACCEPT);

                if (logger.isDebugEnabled()) {
                    logger.debug("Passphrase: SUCCESS");
                }
                return result;
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Passphrase: FAILED " + e);
                }
                return null;
            }
        } else if (inContext instanceof MyProxyServerCredentialSecurityContext) {
            MyProxyServerCredentialSecurityContext c = (MyProxyServerCredentialSecurityContext) inContext;

            GSSCredential cred = GlobusSecurityUtils
                    .getCredentialFromMyProxyServer(c.getHost(), c.getPort(), c
                            .getUsername(), c.getPassword());

            return cred;
        }
        return null; // unknown security context type
    }
}

/**
 * @author rob
 */
public class GlobusSecurityUtils {

    protected static Logger logger = LoggerFactory
            .getLogger(GlobusSecurityUtils.class);
    
    private static GSSCredential cachedDefaultCredential = null;
    
    private static boolean didDefaultCredential = false;


    /**
     * Handling of all globus certicicates goes through this method
     * 
     * @param context
     * @param preferences
     * @param adaptorName
     * @param securityContextType
     * @param host
     * @param port
     * @return
     * @throws GATInvocationException
     */
    public static GSSCredential getGlobusCredential(GATContext context,
            String adaptorName, URI location, int defaultPort)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Object data = SecurityContextUtils.getSecurityUserData(context,
                adaptorName, "globus", location, defaultPort,
                new GlobusContextCreator());

        GSSCredential c = (GSSCredential) data;

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
     * This method returns the default globus credential. The strategy used is
     * as follows.
     * 
     * <P>
     * First, it tries to use the CredentialSecurityContext to retrieve the
     * credential
     * 
     * <P>
     * Next, it tries to read the proxy from the location specified in the
     * "X509_USER_PROXY" environment variable. This variable is used by the
     * globus commandline tools (e.g., grid-proxy-init, globus-url-copy) as
     * well.
     * 
     * <P>
     * Finally, it tries to get the default proxy from the default location.
     */
    protected static synchronized GSSCredential getDefaultCredential(GATContext gatContext)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException {
        
        if (! didDefaultCredential) {
            didDefaultCredential = true;


            // Now, try to get the credential from the specified environment
            // variable
            if (logger.isDebugEnabled()) {
                logger
                .debug("trying to get credential from location specified in environment");
            }

            Environment e = new Environment();
            String proxyLocation = e.getVar("X509_USER_PROXY");

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
                ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
                .getInstance();

                // try to get default user proxy certificate from file in /tmp
                cachedDefaultCredential = manager
                .createCredential(GSSCredential.INITIATE_AND_ACCEPT);
            } catch (GSSException x) {
                if (logger.isDebugEnabled()) {
                    logger.debug("default credential failed: " + x);
                }

                // handled below
            }
        }

        if (cachedDefaultCredential == null) {
            throw new CouldNotInitializeCredentialException(
                    "globus",
                    new CouldNotInitializeCredentialException(
                            "can't get proxy credential (did you do a grid-proxy-init?)"));
        }
        

        return cachedDefaultCredential;
    }

    public static GSSCredential getCredential(String file) {
        GSSCredential credential = null;

        try {
            GlobusCredential globusCred = new GlobusCredential(file);
            credential = new GlobusGSSCredentialImpl(globusCred,
                    GSSCredential.INITIATE_AND_ACCEPT);
        } catch (Throwable t) {
            if (logger.isDebugEnabled()) {
                logger.debug("loading credential from file " + file
                        + " failed: " + t);
            }

            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("loaded credential from file " + file);
        }

        return credential;
    }

    // try to get user credential from MyProxyServer
    public static GSSCredential getCredentialFromMyProxyServer(String host,
            int port, String user, String password)
            throws CouldNotInitializeCredentialException {
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

            return proxy.get(user, password, 2 /* lifetime */);
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
