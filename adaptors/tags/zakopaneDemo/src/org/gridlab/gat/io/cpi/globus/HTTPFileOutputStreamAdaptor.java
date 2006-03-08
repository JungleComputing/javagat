package org.gridlab.gat.io.cpi.globus;

import java.io.OutputStream;
import java.net.URI;

import org.globus.io.streams.HTTPOutputStream;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.IPUtils;

public class HTTPFileOutputStreamAdaptor extends GlobusFileOutputStreamAdaptor {

	public HTTPFileOutputStreamAdaptor(GATContext gatContext,
			Preferences preferences, URI location, Boolean append)
			throws AdaptorCreationException {
		super(gatContext, preferences, location, append);

		checkName("http");

		// now try to create a stream.
		try {
			out = createStream();
		} catch (GATInvocationException e) {
			throw new AdaptorCreationException(e);
		}
	}

	protected OutputStream createStream() throws GATInvocationException {
		String host = location.getHost();
		String path = IPUtils.getPath(location);
		GATInvocationException gridFTPException = null;

		try {
			int port = GlobusFileAdaptor.DEFAULT_HTTP_PORT;

			// allow port override
			if (location.getPort() != -1) {
				port = location.getPort();
			}

			HTTPOutputStream output = new HTTPOutputStream(host, port, path,
					-1, append); //length not known

			return output;
		} catch (Exception e) {
			throw new GATInvocationException("http", e);
		}
	}
}