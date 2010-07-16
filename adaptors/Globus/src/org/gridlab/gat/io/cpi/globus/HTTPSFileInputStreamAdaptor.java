package org.gridlab.gat.io.cpi.globus;

import java.io.InputStream;

import org.globus.gsi.gssapi.auth.SelfAuthorization;
import org.globus.io.streams.GassInputStream;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;

public class HTTPSFileInputStreamAdaptor extends GlobusFileInputStreamAdaptor {
    
    public static String getDescription() {
        return "The HTTPS FileInputStream Adaptor implements the FileInputStream object using the https: protocol.";
    }

    public static String[] getSupportedSchemes() {
        return new String[] { "https", "file", ""};
    }
    
    public HTTPSFileInputStreamAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {
        super(gatContext, location);

        // now try to create a stream.
        try {
            in = createStream();
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("https inputstream", e);
        }
    }

    protected InputStream createStream() throws GATInvocationException {
        String host = location.resolveHost();
        String path = location.getPath();

        try {
            int port = location.getPort(GlobusFileAdaptor.DEFAULT_HTTPS_PORT);

            GSSCredential credential = GlobusSecurityUtils.getGlobusCredential(
                    gatContext, "gridftp", location,
                    GlobusFileAdaptor.DEFAULT_HTTPS_PORT);

            GassInputStream input = new GassInputStream(credential,
                    SelfAuthorization.getInstance(), host, port, path);

            return input;
        } catch (Exception e) {
            throw new GATInvocationException("https", e);
        }
    }
}
