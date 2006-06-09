package org.gridlab.gat.io.cpi.zorilla;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.cpi.FileCpi;

import java.net.URISyntaxException;

/**
 * Zorilla File Adaptor. Cannot do anything except "exist"
 */
public class ZorillaFileAdaptor extends FileCpi {

    private static final long serialVersionUID = 1L;

    /**
     * @param gatContext
     * @param preferences
     * @param location
     */
    public ZorillaFileAdaptor(GATContext gatContext, Preferences preferences,
            URI location) throws GATObjectCreationException {
        super(gatContext, preferences, location);
        
        if (!location.isLocal()) {
            throw new GATObjectCreationException("cannot specify host in Zorilla file adaptor");
        }
        
        if (!location.isCompatible("zorilla")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }
        
        if (!location.isAbsolute()) {
            throw new GATObjectCreationException("zorilla file must be absolute");
        }
        
        if (GATEngine.DEBUG) {
            System.err.println("ZorillaFileAdaptor: LOCATION = " + location);
        }

    }
}
