package org.gridlab.gat.io.cpi.globus;

import java.io.IOException;
import java.io.InputStream;

import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.exception.FTPException;
import org.globus.io.streams.FTPInputStream;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;

class GridFTPInputStream extends FTPInputStream {
    public GridFTPInputStream(String file, boolean passive, int type,
            GridFTPClient gridFtp) throws IOException, FTPException {
        ftp = gridFtp;

        get(passive, type, file);
    }

    public void close() throws IOException {
        // ignore close
    }
}

public class GridFTPFileInputStreamAdaptor extends GlobusFileInputStreamAdaptor {
    GridFTPClient c;

    public GridFTPFileInputStreamAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {
        super(gatContext, location);

        if (!location.isCompatible("gsiftp")) {
            throw new AdaptorNotApplicableException("cannot handle this URI: "
                    + location);
        }

        // now try to create a stream.
        try {
            in = createStream();
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("grid ftp inputstream", e);
        }
    }

    protected InputStream createStream() throws GATInvocationException {
        String path = location.getPath();

        try {
            // get a cached client, and reuse it for the stream
            // for some reason the cog kit crashes when we setPassive twice,
            // so ask for an active stream from the cache, set it to active
            // later.
            // this only works with passive = false
            Preferences additionalPreferences = new Preferences();
            additionalPreferences.put("ftp.connection.passive", "false");

            c = GridFTPFileAdaptor.doWorkCreateClient(gatContext,
                    additionalPreferences, location);

            GridFTPInputStream input = new GridFTPInputStream(path,
                    true /* passive */, GridFTPSession.TYPE_IMAGE, c);
            return input;
        } catch (Exception e) {
            throw new GATInvocationException("gridftp", e);
        }
    }

    public void close() throws GATInvocationException {
        super.close();
        GridFTPFileAdaptor.doWorkDestroyClient(gatContext, c, location, gatContext
                .getPreferences());
    }
}
