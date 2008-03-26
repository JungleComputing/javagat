/*
 * Created on Aug 2, 2005
 */
package org.gridlab.gat.security.sftp;

import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.CredentialSecurityContext;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextCreator;
import org.gridlab.gat.security.cpi.SecurityContextUtils;

/**
 * @author rob
 * 
 * THIS FILE IS SIMILAR TO SSHSECURITYUTILS
 * PLEASE FIX BUGS IN BOTH FILES!!
 */
class SftpContextCreator implements SecurityContextCreator {
    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
            Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        SftpUserInfo cred = SftpSecurityUtils.getDefaultUserInfo(gatContext,
                preferences, location);
        CredentialSecurityContext c = new CredentialSecurityContext();
        c.putDataObject("sftp", cred);

        return c;
    }

    public Object createUserData(GATContext gatContext,
            Preferences preferences, URI location, SecurityContext inContext)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        SftpUserInfo info;

        if (inContext instanceof CredentialSecurityContext) {
            return inContext.getDataObject("sftp");
        } else if (inContext instanceof CertificateSecurityContext) {
            CertificateSecurityContext c = (CertificateSecurityContext) inContext;

            if (c.getKeyfile() == null) { // must be a password (is possible,
                // default info may be stored like
                // that)
                info = new SftpUserInfo();
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
                    info = new SftpUserInfo();
                    info.username = SecurityContextUtils.getUser(gatContext,
                            preferences, inContext, location);
                    info.privateKeyfile = c.getKeyfile().getPath();
                    info.privateKeySlot = c.getPrivateKeySlot();

                    return info;
                }
            }
        } else if (inContext instanceof PasswordSecurityContext) {
            PasswordSecurityContext c = (PasswordSecurityContext) inContext;
            info = new SftpUserInfo();
            info.username = SecurityContextUtils.getUser(gatContext,
                    preferences, inContext, location);
            info.password = c.getPassword();

            return info;
        }

        return null;
    }
}

public class SftpSecurityUtils {
    public static SftpUserInfo getSshCredential(GATContext context,
            Preferences preferences, String adaptorName, URI location,
            int defaultPort) throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Object data = SecurityContextUtils.getSecurityUserData(context,
                preferences, adaptorName, "sftp", location, defaultPort,
                new SftpContextCreator());

        return (SftpUserInfo) data;
    }

    protected static SftpUserInfo getDefaultUserInfo(GATContext gatContext,
            Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        SftpUserInfo info = new SftpUserInfo();
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
