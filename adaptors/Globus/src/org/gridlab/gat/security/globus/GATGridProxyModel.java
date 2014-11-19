package org.gridlab.gat.security.globus;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.globus.gsi.util.CertificateLoadUtil;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.X509Credential;
import org.globus.gsi.OpenSSLKey;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;

public class GATGridProxyModel {

    public X509Credential createProxy(String passphrase, String certFile,
            String keyFile) throws Exception {
        X509Certificate userCert = CertificateLoadUtil.loadCertificate(certFile);
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

        return factory.createCredential(new X509Certificate[] { userCert },
                userKey, 512, 12 * 3600, GSIConstants.DelegationType.FULL, (X509ExtensionSet) null);
    }
}
