/*
 * Created on Aug 2, 2005
 */
package org.gridlab.gat.io.cpi.sftpTrilead;

import java.io.File;

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
 * @author rob
 */
class SftpTrileadContextCreator implements SecurityContextCreator {

    protected static Logger logger = LoggerFactory
            .getLogger(SftpTrileadContextCreator.class);

    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
            URI location) throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        SftpTrileadUserInfo cred = SftpTrileadSecurityUtils.getDefaultUserInfo(
                gatContext, location);
        CredentialSecurityContext c = new CredentialSecurityContext();
        c.putDataObject("sftpTrilead", cred);

        return c;
    }

    public Object createUserData(GATContext gatContext, URI location,
            SecurityContext inContext)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        SftpTrileadUserInfo info = new SftpTrileadUserInfo();

        if (inContext instanceof CredentialSecurityContext) {
            return inContext.getDataObject("sshtrilead");
        } else if (inContext instanceof CertificateSecurityContext) {
            CertificateSecurityContext c = (CertificateSecurityContext) inContext;
            if (c.getUsername() != null) {
                info.username = c.getUsername();
            } else {
                info.username = SecurityContextUtils.getUser(
                        gatContext, inContext, location);
            }
            if (c.getPassword() != null) {
                info.password = c.getPassword();
            }
            if (c.getKeyfile() != null && c.getKeyfile().refersToLocalHost()) {
                info.privateKey = new java.io.File(c.getKeyfile().getPath());
            } else {
                info.privateKey = SftpTrileadSecurityUtils.getDefaultPrivateKey();
            }
            return info;
        } else if (inContext instanceof PasswordSecurityContext) {
            PasswordSecurityContext c = (PasswordSecurityContext) inContext;
            if (c.getUsername() != null) {
                info.username = c.getUsername();
            } else {
                info.username = SecurityContextUtils.getUser(
                        gatContext, inContext, location);
            }
            if (c.getPassword() != null) {
                info.password = c.getPassword();
            }
            return info;
        }
        return null;
    }
}

public class SftpTrileadSecurityUtils {

    protected static Logger logger = LoggerFactory
            .getLogger(SftpTrileadSecurityUtils.class);

    protected static SftpTrileadUserInfo getSftpCredential(GATContext context,
            String adaptorName, URI location, int defaultPort)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Object data = SecurityContextUtils.getSecurityUserData(context,
                adaptorName, "sftpTrilead", location, defaultPort,
                new SftpTrileadContextCreator());

        return (SftpTrileadUserInfo) data;
    }

    protected static SftpTrileadUserInfo getDefaultUserInfo(
            GATContext gatContext, URI location)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        SftpTrileadUserInfo info = new SftpTrileadUserInfo();
        info.privateKey = getDefaultPrivateKey();
        info.username = SecurityContextUtils
                .getUser(gatContext, null, location);
        info.defaultContext = true;
        return info;
    }

    static File getDefaultPrivateKey()
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException {
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

    protected static File loadKey(String keyfile)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException {
        if (logger.isDebugEnabled()) {
            logger.debug("trying to load ssh key from: " + keyfile);
        }

        java.io.File keyf = new java.io.File(keyfile);

        if (!keyf.exists()) {
            throw new CouldNotInitializeCredentialException(
                    "could not find private key");
        }

        return keyf;
    }
}
