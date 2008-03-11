package org.gridlab.gat.security.sshtrilead;

import java.util.HashMap;
import java.util.Map;

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
class SshTrileadContextCreator implements SecurityContextCreator {
    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
            Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Map<String, Object> securityInfo = SshTrileadSecurityUtils
                .getDefaultUserInfo(gatContext, preferences, location);
        CertificateSecurityContext c = new CertificateSecurityContext();
        c.putDataObject("sshtrilead", securityInfo);
        return c;
    }

    public Object createUserData(GATContext gatContext,
            Preferences preferences, URI location, SecurityContext inContext)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Map<String, Object> securityInfo = new HashMap<String, Object>();

        if (inContext instanceof CertificateSecurityContext) {
            CertificateSecurityContext c = (CertificateSecurityContext) inContext;

            if (c.getDataObject("sshtrilead") != null) {
                return c.getDataObject("sshtrilead");
            }
            if (c.getUsername() != null) {
                securityInfo.put("username", c.getUsername());
            } else {
                securityInfo.put("username", SecurityContextUtils.getUser(gatContext, preferences, inContext, location));
            }
            if (c.getPassword() != null) {
                securityInfo.put("password", c.getPassword());
            }
            if (c.getKeyfile() != null && c.getKeyfile().refersToLocalHost()) {
                securityInfo.put("keyfile", new java.io.File(c.getKeyfile()
                        .getPath()));
            }
            return securityInfo;
        } else if (inContext instanceof PasswordSecurityContext) {
            PasswordSecurityContext c = (PasswordSecurityContext) inContext;
            if (c.getUsername() != null) {
                securityInfo.put("username", c.getUsername());
            } else {
                securityInfo.put("username", SecurityContextUtils.getUser(gatContext, preferences, inContext, location));
            }
            if (c.getPassword() != null) {
                securityInfo.put("password", c.getPassword());
            }
            return securityInfo;
        }
        return null;
    }
}

public class SshTrileadSecurityUtils {
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getSshTrileadCredential(GATContext context,
            Preferences preferences, String adaptorName, URI location,
            int defaultPort) throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Object data = SecurityContextUtils.getSecurityUserData(context,
                preferences, adaptorName, "sshtrilead", location, defaultPort,
                new SshTrileadContextCreator());

        return (Map<String, Object>) data;
    }

    protected static Map<String, Object> getDefaultUserInfo(GATContext gatContext,
            Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Map<String, Object> securityInfo = new HashMap<String, Object>();
        securityInfo.put("username", SecurityContextUtils.getUser(gatContext, preferences,
                null, location));
        securityInfo.put("keyfile", getDefaultPrivateKeyfile(gatContext, preferences));
        return securityInfo;
    }

    private static java.io.File getDefaultPrivateKeyfile(GATContext context,
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

        return keyf;
    }
}
