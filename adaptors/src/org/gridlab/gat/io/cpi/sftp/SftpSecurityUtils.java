/*
 * Created on Aug 2, 2005
 */
package org.gridlab.gat.io.cpi.sftp;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
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

import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

/**
 * @author rob
 */
class SftpContextCreator implements SecurityContextCreator {

	public SecurityContext createDefaultSecurityContext(GATContext gatContext,
            Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException, CredentialExpiredException {
        SftpUserInfo cred = SftpSecurityUtils.getDefaultUserInfo(gatContext,
            preferences, location);
        CertificateSecurityContext c = new CertificateSecurityContext();
        c.putDataObject("sftp", cred);

        return c;
    }

    public Object createUserData(GATContext gatContext,
            Preferences preferences, URI location, SecurityContext inContext)
            throws CouldNotInitializeCredentialException, CredentialExpiredException {
        SftpUserInfo info;

        if (inContext instanceof CertificateSecurityContext) {
            CertificateSecurityContext c = (CertificateSecurityContext) inContext;

            URI keyURI = c.getKeyfile();

            if (keyURI == null) { // must be a password (is possible, default info may be stored like that)
                info = new SftpUserInfo();
                info.username = SecurityContextUtils.getUser(gatContext, preferences, inContext, location);
                info.password = c.getPassword();

                return info;
            } else { // public / private key

                if (!keyURI.refersToLocalHost()) {
                    System.err
                        .println("WARNING: URI for key file does not refer to local host, skipping this security context");
                } else {
                    info = new SftpUserInfo();
                    info.username = SecurityContextUtils.getUser(gatContext, preferences, inContext, location);
                    info.privateKey = SftpSecurityUtils.loadKey(c.getKeyfile()
                        .getPath());

                    return info;
                }
            }
        } else if (inContext instanceof PasswordSecurityContext) {
            PasswordSecurityContext c = (PasswordSecurityContext) inContext;
            info = new SftpUserInfo();
            info.username = SecurityContextUtils.getUser(gatContext, preferences, inContext, location);
            info.password = c.getPassword();

            return info;
        }

        return null;
    }
}

public class SftpSecurityUtils {
	
	protected static Logger logger = Logger.getLogger(SftpSecurityUtils.class);
	
    protected static SftpUserInfo getSftpCredential(GATContext context,
            Preferences preferences, String adaptorName, URI location,
            int defaultPort) throws CouldNotInitializeCredentialException, CredentialExpiredException {
        Object data = SecurityContextUtils.getSecurityUserData(context,
            preferences, adaptorName, "sftp", location, defaultPort,
            new SftpContextCreator());

        return (SftpUserInfo) data;
    }

    protected static SftpUserInfo getDefaultUserInfo(GATContext gatContext,
            Preferences preferences, URI location)
            throws CouldNotInitializeCredentialException, CredentialExpiredException {
        SftpUserInfo info = new SftpUserInfo();
        info.privateKey = getDefaultPrivateKey(gatContext, preferences);
        info.username = SecurityContextUtils.getUser(gatContext, preferences, null, location);
        return info;
    }

    private static SshPrivateKey getDefaultPrivateKey(GATContext context,
            Preferences preferences) throws CouldNotInitializeCredentialException, CredentialExpiredException {
        String keyfile = null;

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

    protected static SshPrivateKey loadKey(String keyfile)
            throws CouldNotInitializeCredentialException, CredentialExpiredException {
        if (logger.isDebugEnabled()) {
            logger.debug("trying to load ssh key from: " + keyfile);
        }

        java.io.File keyf = new java.io.File(keyfile);

        if (!keyf.exists()) {
            throw new CouldNotInitializeCredentialException("could not find private key");
        }

        try {
            SshPrivateKeyFile key = SshPrivateKeyFile.parse(keyf);

            return key.toPrivateKey("");
        } catch (IOException e) {
            throw new CouldNotInitializeCredentialException("sftp", e);
        }
    }
}
