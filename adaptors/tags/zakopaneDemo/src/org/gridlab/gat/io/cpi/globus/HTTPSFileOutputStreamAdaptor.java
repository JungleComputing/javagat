package org.gridlab.gat.io.cpi.globus;

import java.io.OutputStream;
import java.net.URI;

import org.globus.io.streams.GassOutputStream;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.IPUtils;

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

	public HTTPSFileOutputStreamAdaptor(GATContext gatContext,
			Preferences preferences, URI location, Boolean append)
			throws AdaptorCreationException {
		super(gatContext, preferences, location, append);

		checkName("https");

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
			int port = GlobusFileAdaptor.DEFAULT_GRIDFTP_PORT;

			// allow port override
			if (location.getPort() != -1) {
				port = location.getPort();
			}

			// @@@ this uses default credential!
			GassOutputStream output = new GassOutputStream(host, port, path,
					-1, append); // length unknown

			return output;
		} catch (Exception e) {
			throw new GATInvocationException("https", e);
		}
	}
}