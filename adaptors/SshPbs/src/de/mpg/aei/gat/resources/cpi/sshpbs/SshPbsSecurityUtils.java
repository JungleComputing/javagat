/*
 * Created on Aug 2, 2005
 */
package de.mpg.aei.gat.resources.cpi.sshpbs;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.CredentialSecurityContext;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextCreator;
import org.gridlab.gat.security.cpi.SecurityContextUtils;

/**
 * @author roelof; taken to sshpbs by Alexander
 */
class SshPbsContextCreator implements SecurityContextCreator {

    protected static Logger logger = LoggerFactory
            .getLogger(SshPbsSecurityUtils.class);

    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
            URI location) throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Map<String, String> credentials = SshPbsSecurityUtils
                .getDefaultUserInfo(gatContext, location);
        CredentialSecurityContext c = new CredentialSecurityContext();
        c.putDataObject("sshpbs", credentials);
        return c;
    }

    public Object createUserData(GATContext gatContext, URI location,
            SecurityContext inContext)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Map<String, String> credentials;
        if (inContext instanceof CredentialSecurityContext) {
            return inContext.getDataObject("sshpbs");
        } else if (inContext instanceof CertificateSecurityContext) {
            CertificateSecurityContext c = (CertificateSecurityContext) inContext;
            if (c.getKeyfile() == null) {
                // must be a password (is possible, default info may be stored
                // like that)
                credentials = new HashMap<String, String>();
                credentials.put("username", SecurityContextUtils.getUser(
                        gatContext, inContext, location));
                credentials.put("password", c.getPassword());
                credentials.put("privatekeyslot", "" + c.getPrivateKeySlot());
                return credentials;
            } else { // public / private key
                if (!c.getKeyfile().refersToLocalHost()) {
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("WARNING: URI for key file does not refer to local host, skipping this security context");
                    }
                } else {
                    credentials = new HashMap<String, String>();
                    credentials.put("username", SecurityContextUtils.getUser(
                            gatContext, inContext, location));
                    credentials.put("privatekeyfile", ""
                            + c.getKeyfile().getPath());
                    credentials.put("privatekeyslot", ""
                            + c.getPrivateKeySlot());
                    return credentials;
                }
            }
        } else if (inContext instanceof PasswordSecurityContext) {
            PasswordSecurityContext c = (PasswordSecurityContext) inContext;
            credentials = new HashMap<String, String>();
            credentials.put("username", SecurityContextUtils.getUser(
                    gatContext, inContext, location));
            credentials.put("password", "" + c.getPassword());
            return credentials;
        }
        return null;
    }
}

public class SshPbsSecurityUtils {

    protected static Logger logger = LoggerFactory
            .getLogger(SshPbsSecurityUtils.class);
    
    private static String defaultPrivateKeyfile = null;
    
    private static boolean didDefaultPrivateKeyfile = false;

    @SuppressWarnings("unchecked")
    public static Map<String, String> getSshCredential(GATContext context,
            String adaptorName, URI location, int defaultPort)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Object data = SecurityContextUtils.getSecurityUserData(context,
                adaptorName, "sshpbs", location, defaultPort,
                new SshPbsContextCreator());
        return (Map<String, String>) data;
    }

    protected static Map<String, String> getDefaultUserInfo(
            GATContext gatContext, URI location)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Map<String, String> credential = new HashMap<String, String>();
        credential.put("privatekeyfile", getDefaultPrivateKeyfile(gatContext));
        credential.put("username", SecurityContextUtils.getUser(gatContext,
                null, location));
        return credential;
    }

    private static synchronized String getDefaultPrivateKeyfile(GATContext context) {
        if ( ! didDefaultPrivateKeyfile) {

            didDefaultPrivateKeyfile = true;

            String keyfile = null;

            // no key file given, try id_dsa and id_rsa
            String home = System.getProperty("user.home");
            String fileSep = System.getProperty("file.separator");

            if (home == null) {
                home = "";
            } else {
                home += fileSep;
            }

            keyfile = home + ".ssh" + fileSep + "id_dsa";

            java.io.File keyf = new java.io.File(keyfile);

            if (!keyf.exists()) {
                keyfile = home + ".ssh" + fileSep + "id_rsa";
                keyf = new java.io.File(keyfile);

                if (!keyf.exists()) {
                    keyfile = home + ".ssh" + fileSep + "identity";
                    keyf = new java.io.File(keyfile);

                    if (!keyf.exists()) {
                        keyfile = home + "ssh" + fileSep + "id_dsa";
                        keyf = new java.io.File(keyfile);

                        if (!keyf.exists()) {
                            keyfile = home + "ssh" + fileSep + "id_rsa";
                            keyf = new java.io.File(keyfile);

                            if (!keyf.exists()) {
                                keyfile = home + "ssh" + fileSep + "identity";
                                keyf = new java.io.File(keyfile);

                                if (!keyf.exists()) {
                                    keyfile = null;
                                }
                            }
                        }
                    }
                }
            }

            defaultPrivateKeyfile = keyfile;
        }
        return defaultPrivateKeyfile;
    }
}
