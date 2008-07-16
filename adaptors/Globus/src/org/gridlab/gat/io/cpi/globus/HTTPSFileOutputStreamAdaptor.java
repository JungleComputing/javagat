package org.gridlab.gat.io.cpi.globus;

import java.io.OutputStream;

import org.globus.gsi.gssapi.auth.SelfAuthorization;
import org.globus.io.streams.GassOutputStream;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;

/**
 * A DefaultFileStreamAdaptor represents a connection to open file, the file may
 * be either remote or local.
 * <p>
 * A DefaultFileStreamAdaptor represents a seekable connection to a file and has
 * semantics similar to a standard Unix filedescriptor. It provides methods to
 * query the current position in the file and to seek to new positions.
 * <p>
 * To Write data to a DefaultFileStreamAdaptor it is necessary to construct a
 * Buffer and pack it with data. Similarly, to read data a buffer must be
 * created to store the read data. Writes and reads may either be blocking, or
 * asynchronous. Asynchronous writes or reads must be completed by appropriate
 * call.
 */
public class HTTPSFileOutputStreamAdaptor extends GlobusFileOutputStreamAdaptor {
    public HTTPSFileOutputStreamAdaptor(GATContext gatContext, URI location,
            Boolean append) throws GATObjectCreationException {
        super(gatContext, location, append);

        if (!location.isCompatible("https")) {
            throw new AdaptorNotApplicableException("cannot handle this URI: "
                    + location);
        }

        // now try to create a stream.
        try {
            out = createStream();
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("https outputstream", e);
        }
    }

    protected OutputStream createStream() throws GATInvocationException {
        String host = location.resolveHost();
        String path = location.getPath();

        try {
            int port = location.getPort(GlobusFileAdaptor.DEFAULT_GRIDFTP_PORT);

            GSSCredential credential = GlobusSecurityUtils.getGlobusCredential(
                    gatContext, "gridftp", location,
                    GlobusFileAdaptor.DEFAULT_HTTPS_PORT);

            GassOutputStream output = new GassOutputStream(credential,
                    SelfAuthorization.getInstance(), host, port, path, -1,
                    append); // length unknown

            return output;
        } catch (Exception e) {
            throw new GATInvocationException("https", e);
        }
    }
}
