package org.gridlab.gat.io.cpi.sftpnew;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.ssh.SSHSecurityUtils;
import org.gridlab.gat.io.cpi.ssh.SshUserInfo;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SftpNewFileAdaptor extends FileCpi {

	protected static Logger logger = Logger.getLogger(SftpNewFileAdaptor.class);

	public static final int SSH_PORT = 22;

	static final boolean USE_CLIENT_CACHING = true;

	private static Hashtable clienttable = new Hashtable();

	/**
	 * @param gatContext
	 * @param preferences
	 * @param location
	 */
	public SftpNewFileAdaptor(GATContext gatContext, Preferences preferences,
			URI location) throws GATObjectCreationException {
		super(gatContext, preferences, location);

		if (!location.isCompatible("sftp") && !location.isCompatible("file")) {
			throw new AdaptorNotApplicableException("cannot handle this URI");
		}
	}

	private static String getClientKey(URI hostURI, Preferences preferences) {
		return hostURI.resolveHost() + ":" + hostURI.getPort(SSH_PORT)
				+ preferences; // include preferences in key
	}

	private static synchronized SftpNewConnection getFromCache(String key) {
		SftpNewConnection client = null;
		if (clienttable.containsKey(key)) {
			client = (SftpNewConnection) clienttable.remove(key);
		}
		return client;
	}

	private static synchronized boolean putInCache(String key,
			SftpNewConnection c) {
		if (!clienttable.containsKey(key)) {
			clienttable.put(key, c);
			return true;
		}
		return false;
	}

	protected static SftpNewConnection createChannel(GATContext gatContext,
			Preferences preferences, URI location)
			throws GATInvocationException {
		if (!USE_CLIENT_CACHING) {
			return doWorkCreateChannel(gatContext, preferences, location);
		}

		SftpNewConnection c = null;

		String key = getClientKey(location, preferences);
		c = getFromCache(key);

		if (c != null) {
			try {
				// test if the client is still alive
				c.channel.lpwd();

				if (logger.isDebugEnabled()) {
					logger.debug("using cached client");
				}
			} catch (Exception except) {
				if (logger.isDebugEnabled()) {
					StringWriter writer = new StringWriter();
					except.printStackTrace(new PrintWriter(writer));
					logger.debug("could not reuse cached client: " + except
							+ "\n" + writer.toString());
				}

				c = null;
			}
		}

		if (c == null) {
			c = doWorkCreateChannel(gatContext, preferences, location);
		}

		return c;
	}

	private static SftpNewConnection doWorkCreateChannel(GATContext gatContext,
			Preferences preferences, URI location)
			throws GATInvocationException {

		if (logger.isDebugEnabled()) {
			logger.debug("sftpnew: creating client to "
					+ location.resolveHost());
		}

		JSch jsch = new JSch();
		Hashtable configJsch = new Hashtable();
		configJsch.put("StrictHostKeyChecking", "no");
		JSch.setConfig(configJsch);

		SshUserInfo sui = null;

		try {
			sui = SSHSecurityUtils.getSshCredential(gatContext, preferences,
					"ssh", location, SSH_PORT);
		} catch (Exception e) {
			System.out.println("SshFileAdaptor: failed to retrieve credentials"
					+ e);
		}

		if (sui == null) {
			throw new GATInvocationException(
					"Unable to retrieve user info for authentication");
		}

		try {
			if (sui.getPrivateKeyfile() != null) {
				jsch.addIdentity(sui.getPrivateKeyfile());
			}

			if (location.getUserInfo() != null) {
				sui.username = location.getUserInfo();
			}

			Session session = jsch.getSession(sui.username, location
					.resolveHost(), location.getPort(SSH_PORT));
			session.setUserInfo(sui);
			session.connect();

			Channel c = session.openChannel("sftp");
			c.connect();

			SftpNewConnection res = new SftpNewConnection();
			res.channel = (ChannelSftp) c;
			res.session = session;
			res.jsch = jsch;
			res.userInfo = sui;
			res.remoteMachine = location;
			res.preferences = preferences;

			return res;
		} catch (JSchException jsche) {
			throw new GATInvocationException(
					"internal error in SftpnewFileAdaptor: " + jsche);
		}
	}

	public static void closeChannel(SftpNewConnection c)
			throws GATInvocationException {
		if (!USE_CLIENT_CACHING) {
			doWorkCloseChannel(c);
			return;
		}

		String key = getClientKey(c.remoteMachine, c.preferences);

		if (!putInCache(key, c)) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("closing client");
				}

				doWorkCloseChannel(c);
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger
							.debug("end of sftpnew adaptor, closing client, got exception (ignoring): "
									+ e);
				}

				// ignore
			}
		}
	}

	private static void doWorkCloseChannel(SftpNewConnection connection)
			throws GATInvocationException {
		if (connection.channel != null) {
			try {
				connection.channel.disconnect();
			} catch (Throwable t) { // ignore
			}
		}

		if (connection.session != null) {
			try {
				connection.session.disconnect();
			} catch (Throwable t) { // ignore
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#copy(java.net.URI)
	 */
	public void copy(URI dest) throws GATInvocationException {
		// We don't have to handle the local case, the GAT engine will select
		// the local adaptor.
		if (dest.refersToLocalHost() && (toURI().refersToLocalHost())) {
			throw new GATInvocationException("sftpnew cannot copy local files");
		}

		// create a seperate file object to determine whether the source
		// is a directory. This is needed, because the source might be a local
		// file, and sftp might not be installed locally.
		// This goes wrong for local -> remote copies.
		if (determineIsDirectory()) {
			copyDirectory(gatContext, preferences, toURI(), dest);
			return;
		}

		if (dest.refersToLocalHost()) {
			if (logger.isDebugEnabled()) {
				logger.debug("sftpnew file: copy remote to local");
			}

			copyToLocal(toURI(), dest);

			return;
		}

		if (toURI().refersToLocalHost()) {
			if (logger.isDebugEnabled()) {
				logger.debug("sftpnew file: copy local to remote");
			}

			copyToRemote(toURI(), dest);

			return;
		}

		// source is remote, dest is remote.
		throw new GATInvocationException("sftpNew: cannot do third party copy");
	}

	protected void copyToLocal(URI src, URI dest) throws GATInvocationException {
		SftpNewConnection c = createChannel(gatContext, preferences, src);
		// copy from a remote machine to the local machine
		try {
			// if it is a relative path, we must make it an absolute path.
			// the sftp library uses paths relative to the user's home dir.
			String destPath = dest.getPath();

			if (!destPath.startsWith("/")) {
				java.io.File f = new java.io.File(destPath);
				destPath = f.getCanonicalPath();
			}

			c.channel.get(src.getPath(), destPath);

		} catch (Exception e) {
			throw new GATInvocationException("sftpnew", e);
		} finally {
			closeChannel(c);
		}
	}

	protected void copyToRemote(URI src, URI dest)
			throws GATInvocationException {

		SftpNewConnection tmpCon = null;

		// copy from the local machine to a remote machine.
		try {
			// if it is a relative path, we must make it an absolute path.
			// the sftp library uses paths relative to the user's home dir.
			String srcPath = src.getPath();

			if (!srcPath.startsWith("/")) {
				java.io.File f = new java.io.File(srcPath);
				srcPath = f.getCanonicalPath();
			}

			tmpCon = createChannel(gatContext, preferences, dest);
			tmpCon.channel.put(srcPath, dest.getPath());

		} catch (Exception e) {
			throw new GATInvocationException("sftpnew", e);
		} finally {
			if (tmpCon != null)
				closeChannel(tmpCon);
		}
	}

	public long length() throws GATInvocationException {
		SftpNewConnection c = createChannel(gatContext, preferences, location);
		try {
			SftpATTRS attr = c.channel.lstat(location.getPath());

			return attr.getSize();
		} catch (Exception e) {
			throw new GATInvocationException("sftpnew", e);
		} finally {
			closeChannel(c);
		}
	}

	public boolean isDirectory() throws GATInvocationException {
		SftpNewConnection c = createChannel(gatContext, preferences, location);
		try {
			SftpATTRS attr = c.channel.lstat(location.getPath());

			return attr.isDir();
		} catch (Exception e) {
			throw new GATInvocationException("sftpnew", e);
		} finally {
			closeChannel(c);
		}
	}

	public boolean canRead() throws GATInvocationException {
		SftpNewConnection c = createChannel(gatContext, preferences, location);
		try {
			SftpATTRS attr = c.channel.lstat(location.getPath());

			String permissions = attr.getPermissionsString();
			return permissions.charAt(1) == 'r';
		} catch (Exception e) {
			throw new GATInvocationException("sftpnew", e);
		} finally {
			closeChannel(c);
		}
	}

	public boolean canWrite() throws GATInvocationException {
		SftpNewConnection c = createChannel(gatContext, preferences, location);
		try {
			SftpATTRS attr = c.channel.lstat(location.getPath());

			String permissions = attr.getPermissionsString();
			return permissions.charAt(2) == 'w';
		} catch (Exception e) {
			throw new GATInvocationException("sftpnew", e);
		} finally {
			closeChannel(c);
		}
	}

	public long lastModified() throws GATInvocationException {
		SftpNewConnection c = createChannel(gatContext, preferences, location);
		try {
			SftpATTRS attr = c.channel.lstat(location.getPath());
			return (long) attr.getMTime() * 1000;
		} catch (Exception e) {
			throw new GATInvocationException("sftpnew", e);
		} finally {
			closeChannel(c);
		}
	}

	public boolean isFile() throws GATInvocationException {
		return !isDirectory();
	}

	public boolean exists() throws GATInvocationException {
		SftpNewConnection c = createChannel(gatContext, preferences, location);
		try {
			c.channel.lstat(location.getPath());
			return true;
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return false;
			}
			throw new GATInvocationException("sftpnew", e);
		} finally {
			closeChannel(c);
		}
	}

	public boolean delete() throws GATInvocationException {
		SftpNewConnection c = createChannel(gatContext, preferences, location);
		boolean isFile = true;
		try {
			isFile = isFile();
		} catch (GATInvocationException e) {
			if (e.getMessage().equals("No such file")) {
				return false;
			}
			throw e;
		}

		try {
			if (isFile) {
				c.channel.rm(location.getPath());
			} else {
				c.channel.rmdir(location.getPath());
			}
			return true;
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return false;
			}
			throw new GATInvocationException("sftpnew", e);
		} finally {
			closeChannel(c);
		}
	}

	public boolean mkdir() throws GATInvocationException {
		SftpNewConnection c = createChannel(gatContext, preferences, location);
		try {
			c.channel.mkdir(location.getPath());
			return true;
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_FAILURE) {
				return false;
			}
			throw new GATInvocationException("sftpnew", e);
		} finally {
			closeChannel(c);
		}
	}

	public boolean mkdirs() throws GATInvocationException {
		SftpNewConnection c = createChannel(gatContext, preferences, location);
		String dir = getPath();
		java.util.StringTokenizer tokens = new java.util.StringTokenizer(dir,
				"/");
		String path = dir.startsWith("/") ? "/" : "";

		while (tokens.hasMoreElements()) {
			path += (String) tokens.nextElement();
			try {
				c.channel.lstat(path);
			} catch (SftpException ex) {
				if (ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {

					/*
					 * The directory does not exist, we create it.
					 */
					try {
						c.channel.mkdir(path);
					} catch (Exception ex2) {
						/*
						 * we can't create it
						 */
						closeChannel(c);
						throw new GATInvocationException("sftpnew", ex2);
					}
				} else {
					throw new GATInvocationException("sftpnew", ex);
				}
			}

			path += "/";
		}

		closeChannel(c);

		return true;
	}

	public String[] list() throws GATInvocationException {

		SftpNewConnection c = createChannel(gatContext, preferences, location);

		try {
			Vector ls = c.channel.ls(location.getPath());

			Vector result = new Vector();
			for (int i = 0; i < ls.size(); i++) {
				if (((LsEntry) ls.get(i)).getFilename().equals("."))
					continue;
				if (((LsEntry) ls.get(i)).getFilename().equals(".."))
					continue;
				result.add(((LsEntry) ls.get(i)).getFilename());
			}
			String[] res = new String[result.size()];
			for (int i = 0; i < result.size(); i++) {
				res[i] = (String) result.get(i);
			}
			return res;
		} catch (Exception e) {
			throw new GATInvocationException("sftp", e);
		} finally {
			closeChannel(c);
		}
	}

	public static void end() {
		if (logger.isDebugEnabled()) {
			logger.debug("end of sftpnew adaptor");
		}

		// destroy the cache
		if (clienttable == null) {
			return;
		}

		Enumeration e = clienttable.elements();

		while (e.hasMoreElements()) {
			SftpNewConnection c = (SftpNewConnection) e.nextElement();

			try {
				if (logger.isDebugEnabled()) {
					logger.debug("end of sftpnew adaptor, closing client");
				}

				doWorkCloseChannel(c);
			} catch (Exception x) {
				if (logger.isDebugEnabled()) {
					logger
							.debug("end of gridftp adaptor, closing client, got exception (ignoring): "
									+ x);
				}

				// ignore
			}
		}

		clienttable.clear();
		clienttable = null;
	}
}
