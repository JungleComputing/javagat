package org.gridlab.gat.io.cpi.glite;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnector;
import org.gridlab.gat.io.cpi.glite.lfc.LfcUtil;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.LFCReplica;
import org.gridlab.gat.resources.cpi.glite.GliteConstants;
import org.gridlab.gat.resources.cpi.glite.LDAPResourceFinder;
import org.gridlab.gat.resources.cpi.glite.LDAPResourceFinder.SEInfo;
import org.gridlab.gat.security.glite.GliteSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for the Glite LFCs, accessed via guid: for JavaGAT.
 * <p>
 * To follow the URI standard, the GUID URI is specified as:
 * </p>
 * <p>
 * <tt>guid://&lt;lfcserver&gt;&lt;:lfcport&gt;/&lt;guid&gt;</tt>
 * </p>
 * The LFC server / port is optional.
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

    public static final String GUID = "guid";
    private static final String CANNOT_HANDLE_THIS_URI = "cannot handle this URI: ";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GliteGuidFileAdaptor.class);
    

    public static String getDescription() {
        return "The GliteGUID File Adaptor implements the File object for the Glite LFCs, accessed via the guid: scheme.";
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("copy", true);
        capabilities.put("createNewFile", true);
        capabilities.put("delete", true);
        return capabilities;
    }

    public static String[] getSupportedSchemes() {
        return new String[] { "gliteguid", GUID, "file", ""};
    }
    
    private LfcConnector lfcConnector;

    private boolean localFile;

    private final String vo;

    public GliteGuidFileAdaptor(GATContext gatCtx, URI location)
            throws GATObjectCreationException {
        super(gatCtx, location);
        vo = GliteConstants.getVO(gatContext);

        if (location.isCompatible("file") && location.refersToLocalHost()) {
            localFile = true;
        } else {
            localFile = false;
            this.lfcConnector = LfcUtil.initLfcConnector(gatContext, location,
                    vo);
            LOGGER.info("Instantiated gLiteGuidFileAdaptor for " + location);
        }
    }

    /**
     * Used by CreateDefaultPropertiesFile to generate default
     * javagat.properties.
     * 
     * @return Properties and their default values.
     */
    public static Preferences getSupportedPreferences() {
        Preferences preferences = FileCpi.getSupportedPreferences();
        GliteSecurityUtils.addGliteSecurityPreferences(preferences);
        return preferences;
    }

    /** {@inheritDoc} */
    public synchronized void copy(URI dest) throws GATInvocationException {
        try {
            if (localFile) {
                if (!dest.isCompatible(GUID)) {
                    throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR
                            + ": " + CANNOT_HANDLE_THIS_URI + dest);
                }
                final File source = new File(location.getPath());
                final long filesize = source.length();
                if (this.lfcConnector == null) {
                    this.lfcConnector = LfcUtil.initLfcConnector(gatContext,
                            dest, vo);
                }
                List<SEInfo> ses = new LDAPResourceFinder(gatContext).fetchSEs(
                        vo, filesize);
                if (ses.isEmpty()) {
                    throw new GATInvocationException(
                            "Could not find any usable SE in the BDII "
                                    + "(Possible reasons can be: no available SE, "
                                    + "not enougth free space in the available SEs "
                                    + "or available SEs are not part of the GATContext)!");
                }
                String guid = dest.getPath();
                URI target = LfcUtil.upload(location,guid,ses,gatContext);                
                LOGGER.info("Adding replica...");
                lfcConnector.addReplica(guid, target);
            } else {
                String guid = location.getPath();
                GliteSecurityUtils.touchVomsProxy(gatContext);
                LOGGER.info("Copying " + guid + " to " + dest);
                Collection<LFCReplica> replicas = lfcConnector.listReplicas(
                        null, guid);
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
    public synchronized boolean createNewFile() throws GATInvocationException {
        if (localFile) {
            throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
        LOGGER.info("createNewFile called");
        try {
            GliteSecurityUtils.touchVomsProxy(gatContext);
            String guid = lfcConnector.create(0);
            this.location = new URI(GUID, null, lfcConnector.getServer(),
                    lfcConnector.getPort(), '/' + guid, null, null);
        } catch (URISyntaxException e) {
            throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR, e);
        } catch (IOException e) {
            throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR, e);
        }
        return true;
    }

    /** {@inheritDoc} */
    public synchronized boolean delete() throws GATInvocationException {
        if (localFile) {
            throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
        try {
            String guid = location.getPath();
            GliteSecurityUtils.touchVomsProxy(gatContext);
            LOGGER.info("Deleting " + guid);
            return lfcConnector.deleteGuid(guid);
        } catch (IOException e) {
            LOGGER.info(e.toString());
            // throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR, e);
            return false;
        }
    }

}
