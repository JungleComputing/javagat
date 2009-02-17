/*
 * Created on Aug 2, 2005
 */
package org.gridlab.gat.io.cpi.sftpGanymed;

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
class SftpGanymedContextCreator implements SecurityContextCreator {

    protected static Logger logger = LoggerFactory
            .getLogger(SftpGanymedContextCreator.class);

    public SecurityContext createDefaultSecurityContext(GATContext gatContext,
            URI location) throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        SftpGanymedUserInfo cred = SftpGanymedSecurityUtils.getDefaultUserInfo(
                gatContext, location);
        CredentialSecurityContext c = new CredentialSecurityContext();
        c.putDataObject("sftpGanymed", cred);

        return c;
    }

    public Object createUserData(GATContext gatContext, URI location,
            SecurityContext inContext)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        SftpGanymedUserInfo info;

        if (inContext instanceof CredentialSecurityContext) {
            return inContext.getDataObject("sftpGanymed");
        } else if (inContext instanceof CertificateSecurityContext) {
            CertificateSecurityContext c = (CertificateSecurityContext) inContext;

            if (c.getKeyfile() == null) { // must be a password (is possible,
                // default info may be stored like
                // that)
                info = new SftpGanymedUserInfo();
                info.username = SecurityContextUtils.getUser(gatContext,
                        inContext, location);
                info.password = c.getPassword();

                return info;
            } else { // public / private key

                if (!c.getKeyfile().refersToLocalHost()) {
                    logger
                            .info("WARNING: URI for key file does not refer to local host, skipping this security context");
                } else {
                    info = new SftpGanymedUserInfo();
                    info.username = c.getUsername();
                    info.privateKey = SftpGanymedSecurityUtils.loadKey(c
                            .getKeyfile().getPath());

                    return info;
                }
            }
        } else if (inContext instanceof PasswordSecurityContext) {
            PasswordSecurityContext c = (PasswordSecurityContext) inContext;
            info = new SftpGanymedUserInfo();
            info.username = SecurityContextUtils.getUser(gatContext, inContext,
                    location);
            info.password = c.getPassword();

            return info;
        }

        return null;
    }
}

public class SftpGanymedSecurityUtils {

    protected static Logger logger = LoggerFactory
            .getLogger(SftpGanymedSecurityUtils.class);

    protected static SftpGanymedUserInfo getSftpCredential(GATContext context,
            String adaptorName, URI location, int defaultPort)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        Object data = SecurityContextUtils.getSecurityUserData(context,
                adaptorName, "sftpGanymed", location, defaultPort,
                new SftpGanymedContextCreator());

        return (SftpGanymedUserInfo) data;
    }

    protected static SftpGanymedUserInfo getDefaultUserInfo(
            GATContext gatContext, URI location)
            throws CouldNotInitializeCredentialException,
            CredentialExpiredException, InvalidUsernameOrPasswordException {
        SftpGanymedUserInfo info = new SftpGanymedUserInfo();
        info.privateKey = getDefaultPrivateKey(gatContext);
        info.username = SecurityContextUtils
                .getUser(gatContext, null, location);
        return info;
    }

    private static File getDefaultPrivateKey(GATContext context)
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
