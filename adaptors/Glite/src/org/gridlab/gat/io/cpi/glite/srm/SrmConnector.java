package org.gridlab.gat.io.cpi.glite.srm;

import java.io.IOException;

import org.gridlab.gat.URI;

/**
 * 
 * @author Max Berger
 *
 */
public class SrmConnector {

    public SrmConnector() {
    }

    public String getTURLForFileDownload(URI srmURI) throws IOException {
        SrmConnection connection = new SrmConnection(srmURI.getHost());
        return connection.getTURLForFileDownload(srmURI.toString());
    }
}
