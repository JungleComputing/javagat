package org.gridlab.gat.io.cpi.sftpTrilead;

import java.io.File;
import java.io.IOException;

import com.trilead.ssh2.KnownHosts;
import com.trilead.ssh2.ServerHostKeyVerifier;

public class SftpTrileadHostVerifier implements ServerHostKeyVerifier {
    
    private final KnownHosts database = new KnownHosts();
    
    // Are we going to use this?
    private boolean writeKnownHosts;
    
    // Set when strict host key checking is to be enforced.
    private boolean strictHostKeyChecking;
    
    // Set when no host key checking is to be enforced.
    private boolean noHostKeyChecking;
    
    private File knownHosts = new File(System.getProperty("user.home")
            + File.separator + ".ssh" + File.separator
            + "known_hosts");


    public SftpTrileadHostVerifier(boolean writeKnownHosts, boolean strictHostKeyChecking,
            boolean noHostKeyChecking) {
        this.writeKnownHosts = writeKnownHosts;
        this.strictHostKeyChecking = strictHostKeyChecking;
        this.noHostKeyChecking = noHostKeyChecking;
        if (knownHosts.exists()) {
            try {
                database.addHostkeys(knownHosts);
            } catch (IOException e) {
                this.writeKnownHosts = false;
                // O well, we tried.
            }
        }
    }

    public boolean verifyServerHostKey(String hostname, int port,
            String serverHostKeyAlgorithm, byte[] serverHostKey)
            throws Exception {
        
        if (noHostKeyChecking) {
            return true;
        }
        
        int result = database.verifyHostkey(hostname, serverHostKeyAlgorithm,
                serverHostKey);

        switch (result) {
        case KnownHosts.HOSTKEY_IS_OK:
            return true;

        case KnownHosts.HOSTKEY_IS_NEW:
            if (strictHostKeyChecking) {
                return false;
            }
            String[] hostnames = new String[] { hostname };
            database.addHostkey(hostnames,
                    serverHostKeyAlgorithm, serverHostKey);
            if (writeKnownHosts) {
                KnownHosts.addHostkeyToFile(knownHosts, hostnames,
                        serverHostKeyAlgorithm, serverHostKey);
            }
            return true;

        case KnownHosts.HOSTKEY_HAS_CHANGED:
            
            // Close the connection if the hostkey has changed.
            return false;
            
        default:
            // should not happen.
            return false;
        }
    }

}
