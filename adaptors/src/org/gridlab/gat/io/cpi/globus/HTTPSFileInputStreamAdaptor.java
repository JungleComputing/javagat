package org.gridlab.gat.io.cpi.globus;

import org.globus.gsi.gssapi.auth.SelfAuthorization;

import org.globus.io.streams.GassInputStream;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;

import org.ietf.jgss.GSSCredential;

import java.io.InputStream;

public class HTTPSFileInputStreamAdaptor extends GlobusFileInputStreamAdaptor {
    public HTTPSFileInputStreamAdaptor(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        super(gatContext, preferences, location);

        checkName("https");

        if (!location.isCompatible("https")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }

        // now try to create a stream.
        try {
            in = createStream();
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("https inputstream", e);
        }
    }

    protected InputStream createStream() throws GATInvocationException {
        String host = location.getHost();
        String path = location.getPath();

        try {
            int port = location.getPort(GlobusFileAdaptor.DEFAULT_HTTPS_PORT);

            GSSCredential credential = GlobusSecurityUtils.getGlobusCredential(
                gatContext, preferences, "gridftp", location,
                GlobusFileAdaptor.DEFAULT_HTTPS_PORT);

            GassInputStream input = new GassInputStream(credential,
                SelfAuthorization.getInstance(), host, port, path);

            return input;
        } catch (Exception e) {
            throw new GATInvocationException("https", e);
        }
    }
}
