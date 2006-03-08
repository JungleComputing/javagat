/*
 * Created on Aug 2, 2005
 */
package org.gridlab.gat.io.cpi.sftp;

import java.io.IOException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextCreator;
import org.gridlab.gat.security.cpi.SecurityContextUtils;

import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

/**
 * @author rob
 */

class SftpContextCreator implements SecurityContextCreator {

    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
        Preferences preferences, URI location) throws GATInvocationException {

        SftpUserInfo cred = SFTPSecurityUtils.getDefaultUserInfo(gatContext,
            preferences, location);
        CertificateSecurityContext c = new CertificateSecurityContext();
        c.putDataObject("sftp", cred);

        return c;
    }

    public Object createUserData(GATContext gatContext,
        Preferences preferences, URI location, SecurityContext inContext)
        throws GATInvocationException {
        SftpUserInfo info;
        if (inContext instanceof CertificateSecurityContext) {
            CertificateSecurityContext c = (CertificateSecurityContext) inContext;

            URI keyURI = c.getKeyfile();
            if (keyURI == null) { // must be a password (is possible, default info may be stored like that)
                info = new SftpUserInfo();
                info.username = c.getUsername();
                info.password = c.getPassphrase();
                return info;
            } else { // public / private key
                if (!keyURI.refersToLocalHost()) {
                    System.err
                        .println("WARNING: URI for key file does not refer to local host, skipping this security context");
                } else {
                    info = new SftpUserInfo();
                    info.username = c.getUsername();
                    info.privateKey = SFTPSecurityUtils.loadKey(c.getKeyfile()
                        .getPath());
                    return info;
                }
            }
        } else if (inContext instanceof PasswordSecurityContext) {
            PasswordSecurityContext c = (PasswordSecurityContext) inContext;
            info = new SftpUserInfo();
            info.username = c.getUsername();
            info.password = c.getPassword();
            return info;
        }

        return null;
    }
}

public class SFTPSecurityUtils {

    protected static SftpUserInfo getSftpCredential(GATContext context,
        Preferences preferences, String adaptorName, URI location,
        int defaultPort) throws GATInvocationException {
        Object data = SecurityContextUtils.getSecurityUserData(context,
            preferences, adaptorName, "sftp", location, defaultPort,
            new SftpContextCreator());
        return (SftpUserInfo) data;
    }

    protected static SftpUserInfo getDefaultUserInfo(GATContext gatContext,
        Preferences preferences, URI location) throws GATInvocationException {
        SftpUserInfo info = new SftpUserInfo();
        info.privateKey = getDefaultPrivateKey(gatContext, preferences);
        info.username = getUser(gatContext, preferences, location);
        info.password = (String) preferences.get("password");
        return info;
    }

    private static SshPrivateKey getDefaultPrivateKey(GATContext context,
        Preferences preferences) throws GATInvocationException {

        String keyfile = (String) preferences.get("defaultIdentityFile");
        if (keyfile != null) {
            return loadKey(keyfile);
        }

        // no key file given, try id_dsa and id_rsa
        String home = System.getProperty("user.home");
        if (home == null) {
            home = "";
        } else {
            home += "/";
        }

        try {
            keyfile = home + ".ssh/id_dsa";
            return loadKey(keyfile);
        } catch (GATInvocationException e) {
            // Ignore
        }

        keyfile = home + ".ssh/id_rsa";
        return loadKey(keyfile);
    }

    protected static SshPrivateKey loadKey(String keyfile)
        throws GATInvocationException {

        java.io.File keyf = new java.io.File(keyfile);
        if (!keyf.exists()) {
            throw new GATInvocationException("could not find private key");
        }

        try {
            SshPrivateKeyFile key = SshPrivateKeyFile.parse(keyf);
            return key.toPrivateKey("");
        } catch (IOException e) {
            throw new GATInvocationException("sftp", e);
        }
    }

    private static String getUser(GATContext context, Preferences preferences,
        URI location) throws GATInvocationException {
        String user = location.getUserInfo();
        if (user == null) {
            user = (String) preferences.get("user");
            if (user == null) {
                user = System.getProperty("user.name");
            }
        }

        if (user == null) {
            throw new GATInvocationException("Could not get user name");
        }

        return user;
    }
}
