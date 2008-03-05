/*
 * Created on Aug 2, 2005
 */
package org.gridlab.gat.security.commandlinessh;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextCreator;
import org.gridlab.gat.security.cpi.SecurityContextUtils;

/**
 * @author roelof
 */
class CommandlineSshContextCreator implements SecurityContextCreator {

    protected static Logger logger = Logger
            .getLogger(CommandlineSshSecurityUtils.class);

    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
            Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Map<String, String> credentials = CommandlineSshSecurityUtils
                .getDefaultUserInfo(gatContext, preferences, location);
        CertificateSecurityContext c = new CertificateSecurityContext();
        c.putDataObject("commandlinessh", credentials);
        return c;
    }

    public Object createUserData(GATContext gatContext,
            Preferences preferences, URI location, SecurityContext inContext)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Map<String, String> credentials;

        if (inContext instanceof CertificateSecurityContext) {
            CertificateSecurityContext c = (CertificateSecurityContext) inContext;

            if (c.getKeyfile() == null) { // must be a password (is possible,
                // default info may be stored like
                // that)
                credentials = new HashMap<String, String>();
                credentials.put("username", SecurityContextUtils.getUser(
                        gatContext, preferences, inContext, location));
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
                            gatContext, preferences, inContext, location));
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
                    gatContext, preferences, inContext, location));
            credentials.put("password", "" + c.getPassword());
            return credentials;
        }
        return null;
    }
}

public class CommandlineSshSecurityUtils {

    protected static Logger logger = Logger
            .getLogger(CommandlineSshSecurityUtils.class);

    @SuppressWarnings("unchecked")
    public static Map<String, String> getSshCredential(GATContext context,
            Preferences preferences, String adaptorName, URI location,
            int defaultPort) throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Object data = SecurityContextUtils.getSecurityUserData(context,
                preferences, adaptorName, "commandlinessh", location,
                defaultPort, new CommandlineSshContextCreator());
        return (Map<String, String>) data;
    }

    protected static Map<String, String> getDefaultUserInfo(
            GATContext gatContext, Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Map<String, String> credential = new HashMap<String, String>();
        credential.put("privatekeyfile", getDefaultPrivateKeyfile(gatContext,
                preferences));
        credential.put("username", SecurityContextUtils.getUser(gatContext,
                preferences, null, location));
        return credential;
    }

    private static String getDefaultPrivateKeyfile(GATContext context,
            Preferences preferences) {
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
                                return null;
                            }
                        }
                    }
                }
            }
        }

        return keyfile;
    }
}