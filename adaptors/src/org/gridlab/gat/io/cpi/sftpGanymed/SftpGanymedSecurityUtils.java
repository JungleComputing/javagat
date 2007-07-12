/*
 * Created on Aug 2, 2005
 */
package org.gridlab.gat.io.cpi.sftpGanymed;

import java.io.File;

import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextCreator;
import org.gridlab.gat.security.cpi.SecurityContextUtils;

/**
 * @author rob
 */
class SftpGanymedContextCreator implements SecurityContextCreator {
    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
            Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException, CredentialExpiredException {
        SftpGanymedUserInfo cred = SftpGanymedSecurityUtils.getDefaultUserInfo(gatContext,
            preferences, location);
        CertificateSecurityContext c = new CertificateSecurityContext();
        c.putDataObject("sftpGanymed", cred);

        return c;
    }

    public Object createUserData(GATContext gatContext,
            Preferences preferences, URI location, SecurityContext inContext)
            throws CouldNotInitializeCredentialException, CredentialExpiredException {
        SftpGanymedUserInfo info;

        if (inContext instanceof CertificateSecurityContext) {
            CertificateSecurityContext c = (CertificateSecurityContext) inContext;

            if (c.getKeyfile() == null) { // must be a password (is possible, default info may be stored like that)
                info = new SftpGanymedUserInfo();
                info.username = SecurityContextUtils.getUser(gatContext, preferences, inContext, location);
                info.password = c.getPassword();

                return info;
            } else { // public / private key

                if (!c.getKeyfile().refersToLocalHost()) {
                    System.err
                        .println("WARNING: URI for key file does not refer to local host, skipping this security context");
                } else {
                    info = new SftpGanymedUserInfo();
                    info.username = c.getUsername();
                    info.privateKey = SftpGanymedSecurityUtils.loadKey(c.getKeyfile()
                        .getPath());

                    return info;
                }
            }
        } else if (inContext instanceof PasswordSecurityContext) {
            PasswordSecurityContext c = (PasswordSecurityContext) inContext;
            info = new SftpGanymedUserInfo();
            info.username = SecurityContextUtils.getUser(gatContext, preferences, inContext, location);
            info.password = c.getPassword();

            return info;
        }

        return null;
    }
}

public class SftpGanymedSecurityUtils {
    protected static SftpGanymedUserInfo getSftpCredential(GATContext context,
            Preferences preferences, String adaptorName, URI location,
            int defaultPort) throws CouldNotInitializeCredentialException, CredentialExpiredException {
        Object data = SecurityContextUtils.getSecurityUserData(context,
            preferences, adaptorName, "sftpGanymed", location, defaultPort,
            new SftpGanymedContextCreator());

        return (SftpGanymedUserInfo) data;
    }

    protected static SftpGanymedUserInfo getDefaultUserInfo(GATContext gatContext,
            Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException, CredentialExpiredException {
        SftpGanymedUserInfo info = new SftpGanymedUserInfo();
        info.privateKey = getDefaultPrivateKey(gatContext, preferences);
        info.username = SecurityContextUtils.getUser(gatContext, preferences, null, location);
        return info;
    }

    private static File getDefaultPrivateKey(GATContext context,
            Preferences preferences) throws CouldNotInitializeCredentialException, CredentialExpiredException {
        String keyfile = null;

        if (preferences != null) {
            keyfile = (String) preferences.get("defaultIdentityFile");
            if (keyfile != null) {
                return loadKey(keyfile);
            }
        }

        // no key file given, try id_dsa and id_rsa
        String home = System.getProperty("user.home");

        if (home == null) {
            home = "";
        } else {
            home += File.separator;
        }

        try {
            keyfile = home + ".ssh" + File.separator + "identity";

            return loadKey(keyfile);
        } catch (Exception e) {
            // Ignore
        }

        try {
            keyfile = home + ".ssh" + File.separator + "id_dsa";

            return loadKey(keyfile);
        } catch (Exception e) {
            // Ignore
        }

        try {
            keyfile = home + ".ssh" + File.separator + "id_rsa";

            return loadKey(keyfile);
        } catch (Exception e) {
            // Ignore
        }

        try {
            keyfile = home + "ssh" + File.separator + "identity";

            return loadKey(keyfile);
        } catch (Exception e) {
            // Ignore
        }

        try {
            keyfile = home + "ssh" + File.separator + "id_dsa";

            return loadKey(keyfile);
        } catch (Exception e) {
            // Ignore
        }

        try {
            keyfile = home + "ssh" + File.separator + "id_rsa";

            return loadKey(keyfile);
        } catch (Exception e) {
            // Ignore
        }

        return null;
    }

    protected static File loadKey(String keyfile)
            throws CouldNotInitializeCredentialException, CredentialExpiredException {
        if (GATEngine.DEBUG) {
            System.err.println("trying to load ssh key from: " + keyfile);
        }

        java.io.File keyf = new java.io.File(keyfile);

        if (!keyf.exists()) {
            throw new CouldNotInitializeCredentialException("could not find private key");
        }

        return keyf;
    }
}
