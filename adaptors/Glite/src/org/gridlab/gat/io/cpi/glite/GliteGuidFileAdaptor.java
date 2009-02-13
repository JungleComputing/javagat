package org.gridlab.gat.io.cpi.glite;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnector;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.LFCReplica;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.glite.GliteConstants;
import org.gridlab.gat.resources.cpi.glite.LDAPResourceFinder;
import org.gridlab.gat.resources.cpi.glite.LDAPResourceFinder.SEInfo;
import org.gridlab.gat.security.glite.GliteSecurityUtils;

/**
 * Adapter for the Glite LFCs, accessed via guid: for JavaGAT.
 * <p>
 * Please note: This adapter is to be considered experimental. It may or may not
 * work for you, and there is no guarantee that the interface or parameters will
 * be compatible with future versions.
 * <p>
 * Please read the documentation in <tt>/doc/GliteAdaptor</tt> for further
 * information!
 * 
 * @author Max Berger
 */
@SuppressWarnings("serial")
public class GliteGuidFileAdaptor extends FileCpi {

    private static final String GLITE_GUID_FILE_ADAPTOR = "GliteGuidFileAdaptor";

    private static final String GUID = "guid";
    private static final String CANNOT_HANDLE_THIS_URI = "cannot handle this URI: ";

    private static final Logger LOGGER = Logger
            .getLogger(GliteGuidFileAdaptor.class);

    private LfcConnector lfcConnector;

    private boolean localFile;

    private final String vo;

    public GliteGuidFileAdaptor(GATContext gatCtx, URI location)
            throws GATObjectCreationException {
        super(gatCtx, location);
        vo = (String) gatContext.getPreferences().get(
                GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION);

        if (location.isCompatible("file") && location.refersToLocalHost()) {
            localFile = true;
        } else {
            localFile = false;
            if (!location.isCompatible(GUID)) {
                throw new AdaptorNotApplicableException(
                        "cannot handle this URI: " + location);
            }
            initLfcConnector();
            LOGGER.info("Instantiated gLiteGuidFileAdaptor for " + location);
        }
    }

    private void initLfcConnector() throws GATObjectCreationException {
        if (lfcConnector == null) {
            String server = fetchServer();
            String portStr = (String) gatContext.getPreferences().get(
                    GliteConstants.PREFERENCE_LFC_SERVER_PORT, "5010");
            int port = Integer.parseInt(portStr);
            lfcConnector = new LfcConnector(server, port, vo);
        }
    }

    private String fetchServer() throws GATObjectCreationException {
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

    /** {@inheritDoc} */
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("copy", true);
        capabilities.put("createNewFile", true);
        capabilities.put("delete", true);
        return capabilities;
    }

    /**
     * Used by CreateDefaultPropertiesFile to generate default
     * javagat.properties.
     * 
     * @return Properties and their default values.
     */
    public static Preferences getSupportedPreferences() {
        Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();
        GliteSecurityUtils.addGliteSecurityPreferences(preferences);
        return preferences;
    }
    
    /** {@inheritDoc} */
    public void copy(URI dest) throws GATInvocationException {
        try {
            if (localFile) {
                if (!dest.isCompatible(GUID)) {
                    throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR
                            + ": " + CANNOT_HANDLE_THIS_URI + dest);
                }
                final File source = new File(location.getPath());
                final long filesize = source.length();
                this.initLfcConnector();

                List<SEInfo> ses = new LDAPResourceFinder(gatContext)
                        .fetchSEs(vo);

                // JEROME: preferred SE
                String preferredSEID = (String) gatContext.getPreferences()
                        .get(GliteConstants.PREFERENCE_PREFERRED_SE_ID);
                if (preferredSEID != null) {
                    LOGGER
                            .info("A preferred SE was provided in the context, will use it if exists");
                    Iterator<SEInfo> iterator = ses.iterator();
                    List<SEInfo> newses = new ArrayList<SEInfo>();
                    while (iterator.hasNext()) {
                        SEInfo info = (SEInfo) iterator.next();
                        if (info.getSeUniqueId().equals(preferredSEID)) {
                            newses.add(info);
                            break;
                        }
                    }
                    if (newses.isEmpty()) {
                        throw new GATInvocationException(
                                "Unable to find the preferred SE in the BDII!");
                    }
                    ses = newses;
                } else {
                    // TEMP SOLUTION
                    Collections.shuffle(ses);
                    // END TEMP SOLUTION
                }

                SEInfo pickedSE = null;
                Iterator<SEInfo> seIt = ses.iterator();
                while ((pickedSE == null) && (seIt.hasNext())) {
                    SEInfo now = seIt.next();
                    try {
                        long freeSpace = Long.parseLong(now.getSpace());
                        if (freeSpace > filesize)
                            pickedSE = now;
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
                if (pickedSE == null) {
                    throw new GATInvocationException(
                            "Could not find a SE with " + filesize
                                    + " bytes available!");
                }
                String guid = dest.getAuthority();
                URI target = new URI("srm://" + pickedSE.getSeUniqueId()
                        + pickedSE.getPath() + "/file-" + guid);
                LOGGER.info("Uploading " + guid + " to " + target);

                GATContext newContext = (GATContext) gatContext.clone();
                newContext.addPreference("File.adaptor.name", "GliteSrm");
                org.gridlab.gat.io.File transportFile = GAT.createFile(
                        newContext, location);
                transportFile.copy(target);
                LOGGER.info("Adding replica...");
                lfcConnector.addReplica(guid, target);
            } else {
                String guid = location.getAuthority();
                GliteSecurityUtils.touchVomsProxy(gatContext);
                LOGGER.info("Copying " + guid + " to " + dest);
                Collection<LFCReplica> replicas = lfcConnector.listReplicas(null,guid);
                LOGGER.info("SRM URIs: " + replicas);
                LFCReplica replica = replicas.iterator().next();
                GliteSrmFileAdaptor srmFile = new GliteSrmFileAdaptor(
                        gatContext, new URI(replica.getSfn()));
                srmFile.copy(dest);
            }
        } catch (Exception e) {
            throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR, e);
        }
    }

    /** {@inheritDoc} */
    public boolean createNewFile() throws GATInvocationException {
        if (localFile) {
            throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
        LOGGER.info("createNewFile called");
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
            LOGGER.info("Deleting " + guid);
            return lfcConnector.delete(guid);
        } catch (IOException e) {
            LOGGER.info(e.toString());
            // throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR, e);
            return false;
        }
    }

}
