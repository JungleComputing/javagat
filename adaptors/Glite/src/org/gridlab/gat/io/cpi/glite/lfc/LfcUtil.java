package org.gridlab.gat.io.cpi.glite.lfc;

import java.net.URISyntaxException;
import java.util.List;

import javax.naming.NamingException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.cpi.glite.GliteConstants;
import org.gridlab.gat.resources.cpi.glite.LDAPResourceFinder;
import org.gridlab.gat.resources.cpi.glite.LDAPResourceFinder.SEInfo;
import org.gridlab.gat.security.glite.GliteSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utilities to help with LFC management.
 * 
 * @author Max Berger
 */
public final class LfcUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(LfcUtil.class);

    private LfcUtil() {
        // Helper class, do not instantiate.
    }

    public static LfcConnector initLfcConnector(GATContext gatContext, URI uri,
            String vo) throws GATObjectCreationException {
        LfcConnector lfcConnector;
        String host = detectLfcHost(gatContext, uri, vo);
        int port = detectLfcPort(gatContext, uri);
        lfcConnector = new LfcConnector(host, port, vo, GliteSecurityUtils
                .getProxyPath(gatContext));
        return lfcConnector;
    }

    private static int detectLfcPort(GATContext gatContext, URI uri) {
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
        return port;
    }

    private static String detectLfcHost(GATContext gatContext, URI uri,
            String vo) throws GATObjectCreationException {
        String server = uri.getHost();
        if (server == null) {
            server = fetchServer(gatContext, vo);
        }
        if (server == null) {
            server = System.getenv("LFC_HOST");
        }
        if (server == null) {
            throw new GATObjectCreationException(
                    "Could not find any information about LFC server to use.");
        }
        return server;
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
        return retVal;
    }

    public static URI upload(URI location, String guid, List<SEInfo> ses,
            GATContext gatContext) throws GATInvocationException {

        for (SEInfo pickedSE : ses) {
            URI target;
            try {
                target = new URI("srm://" + pickedSE.getSeUniqueId()
                        + pickedSE.getPath() + "/file-" + guid);
                LOGGER.info("Uploading " + guid + " to " + target);
                org.gridlab.gat.io.File transportFile = GAT.createFile(LfcUtil
                        .getSRMContext(gatContext), location);
                transportFile.copy(target);
                return target;
            } catch (URISyntaxException e) {
                LOGGER.warn("Could not create SRM URI: " + e.toString());
            } catch (GATObjectCreationException e) {
                LOGGER.debug(e.toString());
            } catch (GATInvocationException e) {
                LOGGER.debug(e.toString());
            }
            LOGGER.info("SRM ("+pickedSE.getSeUniqueId()+") failed, trying next SRM...");
        }
        throw new GATInvocationException("Could not upload file to any SRM");
    }

    /**
     * Create a GAT context which contains only the SRM file adapter.
     * 
     * @param orig
     *            original context.
     * @return new context.
     */
    public static GATContext getSRMContext(GATContext orig) {
        GATContext newContext = (GATContext) orig.clone();
        newContext.addPreference("File.adaptor.name", "GliteSrm");
        return newContext;
    }
}
