package org.gridlab.gat.io.cpi.glite;

import java.util.Collection;
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
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnector;
import org.gridlab.gat.io.cpi.glite.srm.SrmConnector;

/**
 * @author: Max Berger
 */
public class GliteSrmFileAdaptor extends FileCpi {

    private static final String GLITE_SRM_FILE_ADAPTOR = "GliteSrmFileAdaptor";

    protected static Logger logger = Logger.getLogger(GliteSrmFileAdaptor.class);
    
    public GliteSrmFileAdaptor(GATContext gatCtx, URI location)
            throws GATObjectCreationException {
        super(gatCtx, location);
        if (!location.isCompatible("srm")) {
            throw new AdaptorNotApplicableException("cannot handle this URI: "
                    + location);
        }
        logger.info("Instantiated gLiteSrmFileAdaptor for " + location);
    }

    /** {@inheritDoc} */
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("copy", true);
        return capabilities;
    }

    /** {@inheritDoc} */
    public void copy(URI dest) throws GATInvocationException {
        try {
            logger.info("Copying " + location + " to " + dest);
            SrmConnector connector = new SrmConnector();
            String turl = connector.getTURLForFileDownload(location);
            logger.info("TURL: "+turl);
            File transportFile = GAT.createFile(gatContext, turl);
            transportFile.copy(dest);
        } catch (Exception e) {
            throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR, e);
        }
    }

}
