package org.gridlab.gat.io.cpi.glite.lfc;

import java.io.IOException;
import java.util.Collection;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;

public class LfcConnector {

    private final String server;
    private final int port;

    public LfcConnector(GATContext gatContext)
            throws GATObjectCreationException {
        server = (String) gatContext.getPreferences().get("LfcServer");
        if (server == null) {
            throw new GATObjectCreationException(
                    "Please specify an LFC Server in the LfcServer preference");
        }
        String portStr = (String) gatContext.getPreferences().get(
                "LfcServerPort", "5010");
        port = Integer.parseInt(portStr);
    }   
    

    public Collection<String> listReplicas(String guid) throws IOException{
        LfcConnection connection = new LfcConnection(server,port);
        return connection.srmLookup(null, guid);
    }

}
