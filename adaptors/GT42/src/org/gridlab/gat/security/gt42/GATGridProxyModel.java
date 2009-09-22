package org.gridlab.gat.security.gt42;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.globus.gsi.CertUtil;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.OpenSSLKey;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.tools.proxy.GridProxyModel;

public class GATGridProxyModel extends GridProxyModel {

    public GlobusCredential createProxy(String pwd) {
        return null;
    }

    public GlobusCredential createProxy(String passphrase, String certFile,
            String keyFile) throws Exception {
        userCert = CertUtil.loadCertificate(certFile);
        OpenSSLKey key = new BouncyCastleOpenSSLKey(keyFile);

        if (key.isEncrypted()) {
            try {
                key.decrypt(passphrase);
            } catch (GeneralSecurityException e) {
                throw new Exception("Wrong password or other security error");
            }
        }

        PrivateKey userKey = key.getPrivateKey();
        BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory
                .getDefault();
        int proxyType = (getLimited()) ? GSIConstants.DELEGATION_LIMITED
                : GSIConstants.DELEGATION_FULL;

        return factory.createCredential(new X509Certificate[] { userCert },
                userKey, 512, 12 * 3600, proxyType, (X509ExtensionSet) null);
    }

}
