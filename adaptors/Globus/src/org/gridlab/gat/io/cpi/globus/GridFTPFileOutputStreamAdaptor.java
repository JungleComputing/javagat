package org.gridlab.gat.io.cpi.globus;

import java.io.IOException;
import java.io.OutputStream;

import org.globus.common.ChainedIOException;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.exception.FTPException;
import org.globus.io.streams.FTPOutputStream;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;

class GridFTPOutputStream extends FTPOutputStream {
    public GridFTPOutputStream(String file, boolean passive, int type, GridFTPClient gridFtp, boolean append) throws IOException, FTPException {
        ftp = gridFtp;

        put(passive, type, file, append);
    }
    
    @Override
    public void close() throws IOException {
    	if (this.output != null) {
    	    try {
    		this.output.close();
    	    } catch(Exception e) {}
    	}

    	try {
    	    if (this.state != null) {
    		this.state.waitForEnd();
    	    }
    	} catch (FTPException e) {
    	    throw new ChainedIOException("close failed.", e);
    	} 
    }
    
}

public class GridFTPFileOutputStreamAdaptor extends GlobusFileOutputStreamAdaptor {
    GridFTPClient c;

    public GridFTPFileOutputStreamAdaptor(GATContext gatContext, URI location, Boolean append) throws GATObjectCreationException {
        super(gatContext, location, append);

        if (!location.isCompatible("gsiftp")) {
            throw new AdaptorNotApplicableException("cannot handle this URI: "
                    + location);
        }

        // now try to create a stream.
        try {
            out = createStream();
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("grid ftp outputstream", e);
        }
    }

    protected OutputStream createStream() throws GATInvocationException {
        String path = location.getPath();

        try {
            // get a cached client, and reuse it for the stream
            // for some reason the cog kit crashes when we setPassive twice,
            // so ask for an active stream from the cache, set it to active
            // later.
            // this only works with passive = false
            Preferences additionalPreferences = new Preferences();
            additionalPreferences.put("ftp.connection.passive", "false");

            c = GridFTPFileAdaptor.doCreateLongClient(gatContext, additionalPreferences, location);

            GridFTPOutputStream output = new GridFTPOutputStream(path, true /* passive */, GridFTPSession.TYPE_IMAGE, c, append);

            return output;
        } catch (Exception e) {
            throw new GATInvocationException("gridftp", e);
        }
    }

    public void close() throws GATInvocationException {
        super.close();
        GridFTPFileAdaptor.doDestroyLongClient(gatContext, c, location, gatContext.getPreferences());
    }
}
