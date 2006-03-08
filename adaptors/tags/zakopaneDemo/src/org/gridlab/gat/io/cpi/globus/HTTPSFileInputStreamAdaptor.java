package org.gridlab.gat.io.cpi.globus;

import java.io.InputStream;
import java.net.URI;

import org.globus.io.streams.GassInputStream;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.IPUtils;

public class HTTPSFileInputStreamAdaptor extends GlobusFileInputStreamAdaptor {

	public HTTPSFileInputStreamAdaptor(GATContext gatContext,
			Preferences preferences, URI location)
			throws AdaptorCreationException {
		super(gatContext, preferences, location);

		checkName("https");

		// now try to create a stream.
		try {
			in = createStream();
		} catch (GATInvocationException e) {
			throw new AdaptorCreationException(e);
		}
	}

	protected InputStream createStream() throws GATInvocationException {
		String host = location.getHost();
		String path = IPUtils.getPath(location);
		GATInvocationException gridFTPException = null;

		try {
			int port = GlobusFileAdaptor.DEFAULT_GRIDFTP_PORT;

			// allow port override
			if (location.getPort() != -1) {
				port = location.getPort();
			}

			// @@@ this uses default credential!
			GassInputStream input = new GassInputStream(host, port, path);

			return input;
		} catch (Exception e) {
			throw new GATInvocationException("https", e);
		}
	}
}