package org.gridlab.gat.io.cpi.glite;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnector;
import org.gridlab.gat.resources.cpi.glite.GliteConstants;
import org.gridlab.gat.resources.cpi.glite.LDAPResourceFinder;
import org.gridlab.gat.security.glite.GliteSecurityUtils;

/**
 * Adapter for the Glite LFCs, accessed via guid: for JavaGAT.
 * 
 * @author Max Berger
 */
@SuppressWarnings("serial")
public class GliteGuidFileAdaptor extends FileCpi {

    private static final String GLITE_GUID_FILE_ADAPTOR = "GliteGuidFileAdaptor";

    private static final String GUID = "guid";
    private static final String CANNOT_HANDLE_THIS_URI = "cannot handle this URI: ";

    protected static Logger logger = Logger
            .getLogger(GliteGuidFileAdaptor.class);

    private final LfcConnector lfcConnector;

    private boolean localFile;

    public GliteGuidFileAdaptor(GATContext gatCtx, URI location)
            throws GATObjectCreationException {
        super(gatCtx, location);
        if (!location.isCompatible(GUID)) {
            throw new AdaptorNotApplicableException("cannot handle this URI: "
                    + location);
        }
        localFile = false;
        String vo = (String) gatContext.getPreferences().get(
                GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION);
        String server = fetchServer(vo, gatContext);
        String portStr = (String) gatContext.getPreferences().get(
                "LfcServerPort", "5010");
        int port = Integer.parseInt(portStr);
        lfcConnector = new LfcConnector(server, port, vo);
        logger.info("Instantiated gLiteGuidFileAdaptor for " + location);
    }

    private String fetchServer(String vo, GATContext gatContext)
            throws GATObjectCreationException {
        String retVal;
        retVal = (String) gatContext.getPreferences().get("LfcServer");
        if (retVal == null) {
            try {
                List<String> lfcs = new LDAPResourceFinder(null).fetchLFCs(vo);
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

    /** {@inheritDoc} */
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("copy", true);
        capabilities.put("createNewFile", true);
        capabilities.put("delete", true);
        return capabilities;
    }

    /** {@inheritDoc} */
    public void copy(URI dest) throws GATInvocationException {
        try {
            if (localFile) {
                if (!dest.isCompatible(GUID)) {
                    throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR
                            + ": " + CANNOT_HANDLE_THIS_URI + dest);
                }
                throw new UnsupportedOperationException("Uploads are TBD!");
            } else {
                String guid = location.getAuthority();
                GliteSecurityUtils.touchVomsProxy(gatContext);
                logger.info("Copying " + guid + " to " + dest);
                Collection<String> srmUris = lfcConnector.listReplicas(guid);
                logger.info("SRM URIs: " + srmUris);
                String someSrm = srmUris.iterator().next();
                GliteSrmFileAdaptor srmFile = new GliteSrmFileAdaptor(
                        gatContext, new URI(someSrm));
                srmFile.copy(dest);
            }
        } catch (Exception e) {
            throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR, e);
        }
    }

    /** {@inheritDoc} */
    public boolean createNewFile() throws GATInvocationException {
        logger.info("createNewFile called");
        try {
            GliteSecurityUtils.touchVomsProxy(gatContext);
            String guid = lfcConnector.create();
            this.location = new URI(GUID + "://" + guid + "/");
        } catch (URISyntaxException e) {
            throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR, e);
        } catch (IOException e) {
            throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR, e);
        }
        return true;
    }

    /** {@inheritDoc} */
    public boolean delete() throws GATInvocationException {
        if (localFile) {
            throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
        try {
            String guid = location.getAuthority();
            GliteSecurityUtils.touchVomsProxy(gatContext);
            logger.info("Deleting " + guid);
            return lfcConnector.delete(guid);
        } catch (IOException e) {
            // throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR, e);
            return false;
        }
    }

}
