package org.gridlab.gat.security.glite;

/**
 * @author: Max Berger
 */
import org.glite.security.trustmanager.ContextWrapper;
import org.globus.common.CoGProperties;

public final class GliteSecurityUtils {
    private GliteSecurityUtils() {
        // Empty on purpose.
    }

    public static String getProxyPath() {
        CoGProperties properties = CoGProperties.getDefault();
        String proxyFile = System.getenv("X509_USER_PROXY");
        
        if (proxyFile == null) {
            proxyFile = properties.getProxyFile();
        }
        
        System.setProperty("gridProxyFile", proxyFile);  // for glite security JARs
        System.setProperty(ContextWrapper.CREDENTIALS_PROXY_FILE, proxyFile);
        return proxyFile;
    }
    
}
