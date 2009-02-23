package org.gridlab.gat.io.cpi.glite.lfc;

import java.util.List;

import javax.naming.NamingException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.cpi.glite.GliteConstants;
import org.gridlab.gat.resources.cpi.glite.LDAPResourceFinder;
import org.gridlab.gat.security.glite.GliteSecurityUtils;

/**
 * Common utilities to help with LFC management.
 * 
 * @author Max Berger
 */
public final class LfcUtil {

    private LfcUtil() {
        // Helper class, do not instantiate.
    }

    public static LfcConnector initLfcConnector(GATContext gatContext, URI uri,
            String vo) throws GATObjectCreationException {
        LfcConnector lfcConnector;
        String server = uri.getHost();
        if (server == null) {
            server = fetchServer(gatContext, vo);
        }
        int port;
        port = uri.getPort();
        if (port < 0) {
            String portStr = (String) gatContext.getPreferences().get(
                    GliteConstants.PREFERENCE_LFC_SERVER_PORT, "5010");
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException nfe) {
                port = 5010;
            }
        }
        lfcConnector = new LfcConnector(server, port, vo, GliteSecurityUtils
                .getProxyPath(gatContext));
        return lfcConnector;
    }

    private static String fetchServer(GATContext gatContext, String vo)
            throws GATObjectCreationException {
        String retVal;
        retVal = (String) gatContext.getPreferences().get(
                GliteConstants.PREFERENCE_LFC_SERVER);
        if (retVal == null) {
            try {
                List<String> lfcs = new LDAPResourceFinder(gatContext)
                        .fetchLFCs(vo);
                if (lfcs == null) {
                    retVal = null;
                } else if (lfcs.size() < 1)
                    retVal = null;
                else
                    retVal = lfcs.get(0);
            } catch (NamingException e) {
                retVal = null;
            }
        }
        if (retVal == null) {
            throw new GATObjectCreationException(
                    "Failed to find LFC in preferences!");
        }
        return retVal;
    }
}
