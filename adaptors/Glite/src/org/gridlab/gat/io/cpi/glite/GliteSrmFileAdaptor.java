package org.gridlab.gat.io.cpi.glite;

import java.io.IOException;
import java.util.Map;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.glite.srm.SrmConnector;
import org.gridlab.gat.io.permissions.attribute.FileAttributeView;
import org.gridlab.gat.io.permissions.attribute.PosixFileAttributeView;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.security.glite.GliteSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for the SRM (v2) protocol for JavaGAT.
 * <p>
 * Please note: This adapter is to be considered experimental. It may or may not
 * work for you, and there is no guarantee that the interface or parameters will
 * be compatible with future versions.
 * <p>
 * Please read the documentation in <tt>/doc/GliteAdaptor</tt> for further
 * information!
 * 
 * @author: Max Berger
 */
@SuppressWarnings("serial")
public class GliteSrmFileAdaptor extends FileCpi {

    private static final String SRM_PROTOCOL = "srm";

    private static final String CANNOT_HANDLE_THIS_URI = "cannot handle this URI: ";

    private static final String GLITE_SRM_FILE_ADAPTOR = "GliteSrmFileAdaptor";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GliteSrmFileAdaptor.class);

    private final boolean localFile;

    private final SrmConnector connector;

    public GliteSrmFileAdaptor(GATContext gatCtx, URI location)
            throws GATObjectCreationException {
        super(gatCtx, location);
             
        if (location.isCompatible("file") && location.refersToLocalHost()) {
            localFile = true;
        } else {
            localFile = false;
            if (!location.isCompatible(GliteSrmFileAdaptor.SRM_PROTOCOL)) {
                throw new AdaptorNotApplicableException(
                        GliteSrmFileAdaptor.CANNOT_HANDLE_THIS_URI + location);
            }
        }
        
        this.connector = new SrmConnector(GliteSecurityUtils.getProxyPath(gatContext));        
        
        LOGGER.info("Instantiated gLiteSrmFileAdaptor for " + location);
    }

    /** {@inheritDoc} */
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("copy", true);
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
                if (!dest.isCompatible(GliteSrmFileAdaptor.SRM_PROTOCOL)) {
                    throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR
                            + ": " + GliteSrmFileAdaptor.CANNOT_HANDLE_THIS_URI
                            + dest);
                }
                @SuppressWarnings("unused")
                String proxyFile = GliteSecurityUtils.touchVomsProxy(gatContext);
                LOGGER.info("SRM/Copy: Uploading " + location + " to " + dest);
                String turl = connector.getTURLForFileUpload(location, dest);
                LOGGER.info("SRM/Copy: TURL: " + turl);
                GATContext newContext = (GATContext) gatContext.clone();
                newContext.addPreference("File.adaptor.name", "GridFTP");
                GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext);
                File transportFile = GAT.createFile(newContext, location);
                transportFile.copy(new URI(turl));
                connector.finalizeFileUpload(dest);
            } else {
                @SuppressWarnings("unused")
                String proxyFile = GliteSecurityUtils.touchVomsProxy(gatContext);
                LOGGER
                        .info("SRM/Copy: Downloading " + location + " to "
                                + dest);
                String turl = connector.getTURLForFileDownload(location);
                LOGGER.info("SRM/Copy: TURL: " + turl);
                GATContext newContext = (GATContext) gatContext.clone();
                newContext.addPreference("File.adaptor.name", "GridFTP");
                GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext);
                File transportFile = GAT.createFile(newContext, turl);
                transportFile.copy(new URI(dest.getPath()));
            }
        } catch (Exception e) {
            throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR, e);
        }
    }

    /** {@inheritDoc} */
    public boolean delete() throws GATInvocationException {
        if (localFile) {
            throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR + ": "
                    + GliteSrmFileAdaptor.CANNOT_HANDLE_THIS_URI + location);
        }
        try {
            connector.delete(location);
        } catch (IOException e) {
            // throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR, e);
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
	public <V extends FileAttributeView> V getFileAttributeView(Class<V> type, boolean followSymbolicLinks)  throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	if(PosixFileAttributeView.class.equals(type)){
    		return (V) new PosixSrmFileAttributeView(location, followSymbolicLinks, connector, gatContext);
    	}
    	return null;
    }
}
