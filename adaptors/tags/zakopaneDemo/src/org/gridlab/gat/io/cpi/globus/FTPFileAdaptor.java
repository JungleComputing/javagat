package org.gridlab.gat.io.cpi.globus;

import java.net.URI;

import org.globus.ftp.FTPClient;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;

public class FTPFileAdaptor extends GlobusFileAdaptor {

	/**
	 * Constructs a LocalFileAdaptor instance which corresponds to the physical
	 * file identified by the passed URI and whose access rights are determined
	 * by the passed GATContext.
	 * 
	 * @param location
	 *            A URI which represents the URI corresponding to the physical
	 *            file.
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this LocalFileAdaptor.
	 */
	public FTPFileAdaptor(GATContext gatContext, Preferences preferences,
			URI location) throws AdaptorCreationException {
		super(gatContext, preferences, location);

		checkName("ftp");
	}

	protected URI fixURI(URI in) {
		return fixURI(in, "ftp");
	}

	protected static void setChannelOptions(FTPClient client) throws Exception {
		// @@@ handle normal ftp case
	}

	protected FTPClient createClient(URI hostURI) throws GATInvocationException {
		String host = hostURI.getHost();
		GATInvocationException gridFTPException = null;

		int port = DEFAULT_FTP_PORT;

		// allow port override
		if (hostURI.getPort() != -1) {
			port = hostURI.getPort();
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

			FTPClient client = new FTPClient(host, port);
			client.authorize(user, password);

			setChannelOptions(client);
			return client;
		} catch (Exception e) {
			// ouch, both failed.
			throw new GATInvocationException("ftp", e);
		}
	}
}