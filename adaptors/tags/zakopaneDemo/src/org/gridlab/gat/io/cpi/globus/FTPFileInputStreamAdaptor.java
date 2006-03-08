package org.gridlab.gat.io.cpi.globus;

import java.io.InputStream;
import java.net.URI;

import org.globus.io.streams.FTPInputStream;
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
public class FTPFileInputStreamAdaptor extends GlobusFileInputStreamAdaptor {

	public FTPFileInputStreamAdaptor(GATContext gatContext,
			Preferences preferences, URI location)
			throws AdaptorCreationException {
		super(gatContext, preferences, location);

		checkName("ftp");

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

		int port = GlobusFileAdaptor.DEFAULT_FTP_PORT;

		// allow port override
		if (location.getPort() != -1) {
			port = location.getPort();
		}

		try {
			String user = (String) preferences.get("user");
			if (user == null) {
				throw new GATInvocationException(
						"no user provided in preferences");
			}

			String password = (String) preferences.get("password");
			if (password == null) {
				throw new GATInvocationException(
						"no password provided in preferences");
			}

			FTPInputStream input = new FTPInputStream(host, port, user,
					password, path);

			return input;
		} catch (Exception e) {
			// ouch, both failed.
			throw new GATInvocationException("ftp", e);
		}
	}
}