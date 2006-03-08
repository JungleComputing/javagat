package org.gridlab.gat.io.cpi.globus;

import java.io.OutputStream;
import java.net.URI;

import org.globus.io.streams.FTPOutputStream;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.IPUtils;

public class FTPFileOutputStreamAdaptor extends GlobusFileOutputStreamAdaptor {

	public FTPFileOutputStreamAdaptor(GATContext gatContext,
			Preferences preferences, URI location, Boolean append)
			throws AdaptorCreationException {
		super(gatContext, preferences, location, append);

		checkName("ftp");

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

			FTPOutputStream output = new FTPOutputStream(host, port, user,
					password, path, append);

			return output;
		} catch (Exception e) {
			throw new GATInvocationException("ftp", e);
		}
	}
}