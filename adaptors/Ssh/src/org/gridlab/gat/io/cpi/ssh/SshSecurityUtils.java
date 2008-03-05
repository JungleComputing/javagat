/*
 * Created on Aug 2, 2005
 */
package org.gridlab.gat.io.cpi.ssh;

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
import org.gridlab.gat.io.cpi.ssh.SshContextCreator;
import org.gridlab.gat.io.cpi.ssh.SshSecurityUtils;
import org.gridlab.gat.io.cpi.ssh.SshUserInfo;

/**
 * @author rob
 * 
 * THIS FILE IS SIMILAR TO SFTPSECURITYUTILS
 * PLEASE FIX BUGS IN BOTH FILES!!
 */
class SshContextCreator implements SecurityContextCreator {
    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
            Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        SshUserInfo cred = SshSecurityUtils.getDefaultUserInfo(gatContext,
                preferences, location);
        CertificateSecurityContext c = new CertificateSecurityContext();
        c.putDataObject("ssh", cred);

        return c;
    }

    public Object createUserData(GATContext gatContext,
            Preferences preferences, URI location, SecurityContext inContext)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        SshUserInfo info;

        if (inContext instanceof CertificateSecurityContext) {
            CertificateSecurityContext c = (CertificateSecurityContext) inContext;

            if (c.getKeyfile() == null) { // must be a password (is possible,
                // default info may be stored like
                // that)
                info = new SshUserInfo();
                info.username = SecurityContextUtils.getUser(gatContext,
                        preferences, inContext, location);
                info.password = c.getPassword();
                info.privateKeySlot = c.getPrivateKeySlot();

                return info;
            } else { // public / private key
                if (!c.getKeyfile().refersToLocalHost()) {
                    System.err
                            .println("WARNING: URI for key file does not refer to local host, skipping this security context");
                } else {
                    info = new SshUserInfo();
                    info.username = SecurityContextUtils.getUser(gatContext,
                            preferences, inContext, location);
                    info.privateKeyfile = c.getKeyfile().getPath();
                    info.privateKeySlot = c.getPrivateKeySlot();

                    return info;
                }
            }
        } else if (inContext instanceof PasswordSecurityContext) {
            PasswordSecurityContext c = (PasswordSecurityContext) inContext;
            info = new SshUserInfo();
            info.username = SecurityContextUtils.getUser(gatContext,
                    preferences, inContext, location);
            info.password = c.getPassword();

            return info;
        }

        return null;
    }
}

public class SshSecurityUtils {
    public static SshUserInfo getSshCredential(GATContext context,
            Preferences preferences, String adaptorName, URI location,
            int defaultPort) throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Object data = SecurityContextUtils.getSecurityUserData(context,
                preferences, adaptorName, "ssh", location, defaultPort,
                new SshContextCreator());

        return (SshUserInfo) data;
    }

    protected static SshUserInfo getDefaultUserInfo(GATContext gatContext,
            Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        SshUserInfo info = new SshUserInfo();
        info.privateKeyfile = getDefaultPrivateKeyfile(gatContext, preferences);
        info.username = SecurityContextUtils.getUser(gatContext, preferences,
                null, location);
        return info;
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
