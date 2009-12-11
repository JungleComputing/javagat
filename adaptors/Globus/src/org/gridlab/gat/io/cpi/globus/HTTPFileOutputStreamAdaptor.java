package org.gridlab.gat.io.cpi.globus;

import java.io.OutputStream;

import org.globus.io.streams.HTTPOutputStream;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;

public class HTTPFileOutputStreamAdaptor extends GlobusFileOutputStreamAdaptor {
    
    public static String[] getSupportedSchemes() {
        return new String[] { "http", "file", ""};
    }
    
    public HTTPFileOutputStreamAdaptor(GATContext gatContext, URI location,
            Boolean append) throws GATObjectCreationException {
        super(gatContext, location, append);

        // now try to create a stream.
        try {
            out = createStream();
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("http outputstream", e);
        }
    }

    protected OutputStream createStream() throws GATInvocationException {
        String host = location.resolveHost();
        String path = location.getPath();

        try {
            int port = location.getPort(GlobusFileAdaptor.DEFAULT_HTTP_PORT);

            HTTPOutputStream output = new HTTPOutputStream(host, port, path,
                    -1, append); // length not known

            return output;
        } catch (Exception e) {
            throw new GATInvocationException("http", e);
        }
    }
}
