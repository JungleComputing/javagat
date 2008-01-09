/*
 * Created on Jul 29, 2005
 */
package org.gridlab.gat.security.globus;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
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
import org.gridlab.gat.Preferences;
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

    protected static Logger logger = Logger
            .getLogger(GlobusContextCreator.class);

    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
            Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        // automatically try and insert the default credential if it was not
        // there.
        GSSCredential cred = GlobusSecurityUtils.getDefaultCredential(
                gatContext, preferences);
        CredentialSecurityContext c = new CredentialSecurityContext();
        c.putDataObject("globus", cred);
        return c;
    }

    public Object createUserData(GATContext gatContext,
            Preferences preferences, URI location, SecurityContext inContext)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        // we need to try to create the credential given the securityContext
        // if it fails, just try the next one on the list.
        if (inContext instanceof CredentialSecurityContext) {
            CredentialSecurityContext c = (CredentialSecurityContext) inContext;
            Object credentialObject = c.getCredential();
            if (credentialObject != null) {
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
                                logger.debug(e);
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
                            logger.debug(e);
                        }
                        return null;
                    } catch (GSSException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(e);
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

    protected static Logger logger = Logger
            .getLogger(GlobusSecurityUtils.class);

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
            Preferences preferences, String adaptorName, URI location,
            int defaultPort) throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Object data = SecurityContextUtils.getSecurityUserData(context,
                preferences, adaptorName, "globus", location, defaultPort,
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
    protected static GSSCredential getDefaultCredential(GATContext gatContext,
            Preferences preferences)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException {
        GSSCredential credential = null;

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
            credential = getCredential(proxyLocation);
        }
        if (credential != null) {
            return credential;
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
            credential = manager
                    .createCredential(GSSCredential.INITIATE_AND_ACCEPT);
        } catch (GSSException x) {
            if (logger.isDebugEnabled()) {
                logger.debug("default credential failed: " + x);
            }

            // handled below
        }

        if (credential == null) {
            throw new CouldNotInitializeCredentialException(
                    "globus",
                    new CouldNotInitializeCredentialException(
                            "can't get proxy credential (did you do a grid-proxy-init?)"));
        }

        return credential;
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

    // try to get the credential passed in through the gat preferences
    public static GSSCredential getCredentialFromPreferences(
            GATContext context, Preferences preferences) {
        GSSCredential credential = null;

        try {
            String val = (String) preferences.get("globusCert");
            if (val == null)
                return null;
            GlobusCredential globusCred = new GlobusCredential(
                    new ByteArrayInputStream(val.getBytes("UTF-8")));

            if (logger.isDebugEnabled())
                logger.debug("Found proxy.  Good for "
                        + globusCred.getTimeLeft());
            credential = new GlobusGSSCredentialImpl(globusCred,
                    GSSCredential.INITIATE_AND_ACCEPT);
        } catch (Exception x) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("loading credential from preferences failed: "
                                + x);
                x.printStackTrace();
            }

            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("loaded credential from preferences");
        }

        return credential;
    }
}
