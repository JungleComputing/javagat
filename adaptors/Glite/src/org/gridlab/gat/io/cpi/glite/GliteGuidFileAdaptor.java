package org.gridlab.gat.io.cpi.glite;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnector;

/**
 * 
 * @author Max Berger
 *
 */
public class GliteGuidFileAdaptor extends FileCpi {

    private static final String GLITE_GUID_FILE_ADAPTOR = "GliteGuidFileAdaptor";

    protected static Logger logger = Logger.getLogger(GliteGuidFileAdaptor.class);

    private final String guid;

    private final LfcConnector lfcConnector;
    
    public GliteGuidFileAdaptor(GATContext gatCtx, URI location)
            throws GATObjectCreationException {
        super(gatCtx, location);
        if (!location.isCompatible("guid")) {
            throw new AdaptorNotApplicableException("cannot handle this URI: "
                    + location);
        }
        logger.info("Instantiated gLiteGuidFileAdaptor for " + location);
        guid = location.getRawSchemeSpecificPart();
        lfcConnector = new LfcConnector(gatCtx);
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
            logger.info("Copying " + guid + " to " + dest);
            Collection<String> srmUris = lfcConnector.listReplicas(guid);
            logger.info("SRM URIs: " + srmUris);            
            String someSrm = srmUris.iterator().next();
            GliteSrmFileAdaptor srmFile = new GliteSrmFileAdaptor(gatContext, new URI(someSrm));
            srmFile.copy(dest);
        } catch (Exception e) {
            throw new GATInvocationException(GLITE_GUID_FILE_ADAPTOR, e);
        }
    }

}
