package org.gridlab.gat.io.cpi.globus;

import java.net.URI;

import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.FTPClient;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.gridforum.jgss.ExtendedGSSManager;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.IPUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class GridFTPFileAdaptor extends GlobusFileAdaptor {

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
	public GridFTPFileAdaptor(GATContext gatContext, Preferences preferences,
			URI location) throws AdaptorCreationException {
		super(gatContext, preferences, location);

		checkName("gridftp");
	}

	protected URI fixURI(URI in) {
		return fixURI(in, "gridftp");
	}

	protected static void setChannelOptions(FTPClient client) throws Exception {
		GridFTPClient c = (GridFTPClient) client;

		/**
		 * Set security parameters such as data channel authentication (defined
		 * by the GridFTP protocol) and data channel protection (defined by RFC
		 * 2228). If you do not specify these, data channels are authenticated
		 * by default.
		 */
		c.setProtectionBufferSize(16384);
		c.setDataChannelAuthentication(DataChannelAuthentication.SELF);
		c.setDataChannelProtection(GridFTPSession.PROTECTION_SAFE);

		//client.setType(GridFTPSession.TYPE_IMAGE); //transfertype
		//client.setMode(GridFTPSession.MODE_EBLOCK); //transfermode
	}

	protected FTPClient createClient(URI hostURI) throws GATInvocationException {
		try {
			GSSCredential credential = GetCredential();
			String host = hostURI.getHost();
			if (host == null) {
				host = IPUtils.getLocalHostName();
			}

			int port = DEFAULT_GRIDFTP_PORT;

			// allow port override
			if (hostURI.getPort() != -1) {
				port = hostURI.getPort();
			}

			GridFTPClient client = new GridFTPClient(host, port);

			// authenticate to the server
			client.authenticate(credential);

			setChannelOptions(client);

			return client;
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	}

	// @@@ move this to a GlobusUtils class, all globus adaptors should use
	// this.
	// cache credentials in context, look for credentials...
	protected GSSCredential GetCredential() throws GATInvocationException {

		GSSCredential credential = null;
		GATInvocationException noCredentialException = new GATInvocationException(
				"can't get proxy credential; neither user default"
						+ " nor retrieving one from MyProxy server worked");

		try {
			// Get the user credential
			ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
					.getInstance();

			// try to get default user proxy certificate from file in //tmp
			credential = manager
					.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
		} catch (GSSException e) {

			try {
				// try to get user credential from MyProxyServer

				// TODO: get user name, passwd, server name from
				// GATContext/Security Context
				MyProxy proxy = new MyProxy();
				proxy.setHost("my proxy server name");
				proxy.setPort(MyProxy.DEFAULT_PORT);
				credential = proxy.get("user", "passwd", 2);
			} catch (MyProxyException e2) {
				throw noCredentialException;
			}
		}

		if (credential == null)
			throw noCredentialException;

		return credential;
	}
}