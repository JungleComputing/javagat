package org.gridlab.gat.io.cpi.globus;

import java.io.InputStream;

import org.globus.io.streams.HTTPInputStream;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;

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
public class HTTPFileInputStreamAdaptor extends GlobusFileInputStreamAdaptor {
	public HTTPFileInputStreamAdaptor(GATContext gatContext, URI location)
			throws GATObjectCreationException {
		super(gatContext, location);

		if (!location.isCompatible("http")) {
			throw new GATObjectCreationException("cannot handle this URI");
		}

		// now try to create a stream.
		try {
			in = createStream();
		} catch (GATInvocationException e) {
			throw new GATObjectCreationException("http inputstream", e);
		}
	}

	protected InputStream createStream() throws GATInvocationException {
		String host = location.resolveHost();
		String path = location.getPath();

		try {
			int port = location.getPort(GlobusFileAdaptor.DEFAULT_HTTP_PORT);

			HTTPInputStream input = new HTTPInputStream(host, port, path);

			return input;
		} catch (Exception e) {
			throw new GATInvocationException("http", e);
		}
	}
}
