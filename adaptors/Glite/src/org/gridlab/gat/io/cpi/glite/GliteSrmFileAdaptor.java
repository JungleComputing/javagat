package org.gridlab.gat.io.cpi.glite;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.glite.srm.SrmConnector;
import org.gridlab.gat.security.glite.GliteSecurityUtils;

/**
 * Adapter for the SRM (v2) protocol for JavaGAT.
 * 
 * @author: Max Berger
 */
@SuppressWarnings("serial")
public class GliteSrmFileAdaptor extends FileCpi {

    private static final String SRM_PROTOCOL = "srm";

    private static final String CANNOT_HANDLE_THIS_URI = "cannot handle this URI: ";

    private static final String GLITE_SRM_FILE_ADAPTOR = "GliteSrmFileAdaptor";

    protected static Logger logger = Logger
            .getLogger(GliteSrmFileAdaptor.class);

    private final boolean localFile;

    private final SrmConnector connector = new SrmConnector();

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
        logger.info("Instantiated gLiteSrmFileAdaptor for " + location);
    }

    /** {@inheritDoc} */
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("copy", true);
        capabilities.put("delete", true);
        return capabilities;
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
                GliteSecurityUtils.touchVomsProxy(gatContext);
                logger.info("SRM/Copy: Uploading " + location + " to " + dest);
                String turl = connector.getTURLForFileUpload(location, dest);
                logger.info("SRM/Copy: TURL: " + turl);
                GATContext newContext = (GATContext) gatContext.clone();
                newContext.addPreference("File.adaptor.name", "GridFTP");
                File transportFile = GAT.createFile(newContext, location);
                transportFile.copy(new URI(turl));
            } else {
                GliteSecurityUtils.touchVomsProxy(gatContext);
                logger
                        .info("SRM/Copy: Downloading " + location + " to "
                                + dest);
                String turl = connector.getTURLForFileDownload(location);
                logger.info("SRM/Copy: TURL: " + turl);
                GATContext newContext = (GATContext) gatContext.clone();
                newContext.addPreference("File.adaptor.name", "GridFTP");
                File transportFile = GAT.createFile(newContext, turl);
                transportFile.copy(dest);
                connector.finalizeFileUpload(location);
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

}
