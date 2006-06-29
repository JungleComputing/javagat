/*
 * Created on Jul 29, 2005
 */
package org.gridlab.gat.security.globus;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.gridforum.jgss.ExtendedGSSManager;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredExeption;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.MyProxyServerCredentialSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextCreator;
import org.gridlab.gat.security.cpi.SecurityContextUtils;
import org.gridlab.gat.util.Environment;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

class GlobusContextCreator implements SecurityContextCreator {
    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
            Preferences preferences, URI location)
    throws CouldNotInitializeCredentialException, CredentialExpiredExeption {
        // automatically try and insert the default credential if it was not there.
        GSSCredential cred = GlobusSecurityUtils.getDefaultCredential();
        CertificateSecurityContext c = new CertificateSecurityContext();
        c.putDataObject("globus", cred);

        return c;
    }

    public Object createUserData(GATContext gatContext,
            Preferences preferences, URI location, SecurityContext inContext)
    throws CouldNotInitializeCredentialException, CredentialExpiredExeption {
        // we need to try to create the credential given the securityContext
        // if it fails, just try the next one on the list.
        if (inContext instanceof CertificateSecurityContext) {
            CertificateSecurityContext c = (CertificateSecurityContext) inContext;

            URI keyURI = c.getKeyfile();

            if (keyURI == null) { // no key file specified, use default location

                GSSCredential cred = GlobusSecurityUtils.getDefaultCredential();

                return cred;
            } else if (!keyURI.refersToLocalHost()) {
                System.err
                    .println("WARNING: URI for key file does not refer to local host, skipping this security context");
            } else {
                if(GATEngine.VERBOSE) {
                    System.err.println("globus security: getting certificate from: " + keyURI
                        .getPath());
                }
                   
                GSSCredential cred = GlobusSecurityUtils.getCredential(keyURI
                    .getPath());

                return cred;
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
    /** Handling of all globus certicicates goes through this method
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
            int defaultPort) throws CouldNotInitializeCredentialException, CredentialExpiredExeption {
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
        
        if(remaining == 0) {
            throw new CredentialExpiredExeption(
                "gridftp credential expired");
        }
        
        return (GSSCredential) data;
    }

    /** This method returns the default globus credential.
     * The strategy used is as follows.
     *
     * <P> First, it tries to read the proxy from the location specified in the
     * "X509_USER_PROXY" environment variable. This variable is used by the
     * globus commandline tools (e.g., grid-proxy-init, globus-url-copy)
     * as well.
     *
     * <P> Next, it tries to get the default proxy from the default location.
     */
    protected static GSSCredential getDefaultCredential()
    throws CouldNotInitializeCredentialException, CredentialExpiredExeption {
        GSSCredential credential = null;

        // First, try to get the credential from the specified environment variable
        if (GATEngine.DEBUG) {
            System.err
                .println("trying to get credential from specified location");
        }

        Environment e = new Environment();
        String proxyLocation = e.getVar("X509_USER_PROXY");

        if (proxyLocation == null) {
            if (GATEngine.DEBUG) {
                System.err
                    .println("no credential location found in environment");
            }
        } else {
            credential = getCredential(proxyLocation);
        }

        // next try to get default credential
        if (credential == null) {
            if (GATEngine.DEBUG) {
                System.err.println("trying to get default credential");
            }

            try {
                // Get the user credential
                ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
                    .getInstance();

                // try to get default user proxy certificate from file in /tmp
                credential = manager
                    .createCredential(GSSCredential.INITIATE_AND_ACCEPT);
            } catch (GSSException x) {
                if (GATEngine.DEBUG) {
                    System.err.println("default credential failed: " + x);
                }

                // handled below
            }
        }

        if (credential == null) {
            throw new CouldNotInitializeCredentialException(
                "can't get proxy credential (did you do a grid-proxy-init?)");
        }

        return credential;
    }

    public static GSSCredential getCredential(String file) {
        GSSCredential credential = null;

        try {
            GlobusCredential globusCred = new GlobusCredential(file);
            credential = new GlobusGSSCredentialImpl(globusCred,
                GSSCredential.INITIATE_AND_ACCEPT);
        } catch (Exception x) {
            if (GATEngine.DEBUG) {
                System.err.println("loading credential from file " + file
                    + " failed: " + x);
            }

            return null;
        }

        if (GATEngine.DEBUG) {
            System.err.println("loaded credential from file " + file);
        }

        return credential;
    }

    // try to get user credential from MyProxyServer
    public static GSSCredential getCredentialFromMyProxyServer(String host,
            int port, String user, String password)
            throws CouldNotInitializeCredentialException {
        if (GATEngine.DEBUG) {
            System.err.println("trying to get credential from MyProxyServer");
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
            if (GATEngine.DEBUG) {
                System.err
                    .println("getting credential from MyProxyServer failed: "
                        + e);
            }

            throw new CouldNotInitializeCredentialException("getCredentialFromMyProxyServer", e);
        }
    }
}
