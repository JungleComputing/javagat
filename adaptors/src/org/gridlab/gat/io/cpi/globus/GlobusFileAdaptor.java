package org.gridlab.gat.io.cpi.globus;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.globus.ftp.Buffer;
import org.globus.ftp.DataSink;
import org.globus.ftp.FTPClient;
import org.globus.ftp.FileInfo;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.HostPort;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.FTPException;
import org.globus.ftp.exception.ServerException;
import org.globus.ftp.vanilla.FTPControlChannel;
import org.globus.gsi.gssapi.GlobusGSSManagerImpl;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.globus.gsi.gssapi.auth.SelfAuthorization;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;

public abstract class GlobusFileAdaptor extends FileCpi {

	protected static Logger logger = Logger.getLogger(GlobusFileAdaptor.class);

	static final int DEFAULT_GRIDFTP_PORT = 2811;

	static final int DEFAULT_FTP_PORT = 21;

	static final int DEFAULT_HTTP_PORT = 80;

	static final int DEFAULT_HTTPS_PORT = 443;

	static final int NO_SUCH_FILE_OR_DIRECTORY = 550;

	// cache dir info, getting it can be an expensive operation, especially on
	// old servers.
	private static HashMap isDirCache = new HashMap();

	private FileInfo cachedInfo = null;

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
	public GlobusFileAdaptor(GATContext gatContext, Preferences preferences,
			URI location) throws GATObjectCreationException {
		super(gatContext, preferences, location);

		cachedInfo = (FileInfo) preferences.get("GAT_INTERNAL_FILE_INFO");
		preferences.remove("GAT_INTERNAL_FILE_INFO");
	}

	static protected boolean isPassive(Preferences preferences) {
		boolean passive;

		String tmp = (String) preferences.get("ftp.connection.passive");

		if ((tmp != null) && tmp.equalsIgnoreCase("false")) {
			passive = false;
		} else {
			passive = true;
		}

		return passive;
	}

	protected abstract URI fixURI(URI in);

	/**
	 * Create an FTP Client with the default preferences of this file object
	 * 
	 * @param hostURI
	 *            the uri of the FTP host
	 * 
	 */
	protected FTPClient createClient(URI hostURI) throws GATInvocationException {
		return createClient(gatContext, preferences, hostURI);
	}

	/**
	 * Create an FTP Client
	 * 
	 * @param hostURI
	 *            the uri of the FTP host with the fiven preferences
	 * 
	 */
	protected abstract FTPClient createClient(GATContext gatContext,
			Preferences preferences, URI hostURI) throws GATInvocationException;

	/**
	 * Destroy a client that was created with a createClient call. This might,
	 * for instance, put the client back in a cache.
	 */
	protected abstract void destroyClient(FTPClient c, URI hostURI,
			Preferences preferences);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#copy(java.net.URI)
	 */
	public void copy(URI dest) throws GATInvocationException {
		// We don't have to handle the local case, the GAT engine will select
		// the local adaptor.
		// Maybe it is better to just allow this.
		// especially fot the case where the user enforces the use of
		// this adaptor
		// if (dest.refersToLocalHost() && (toURI().refersToLocalHost())) {
		// throw new GATInvocationException("gridftp cannot copy local files");
		// }

		// create a seperate file object to determine whether the source
		// is a directory. This is needed, because the source might be a local
		// file, and gridftp might not be installed locally.
		// This goes wrong for local -> remote copies.
		if (determineIsDirectory()) {
			copyDirectory(gatContext, preferences, toURI(), dest);
			return;
		}

		if (dest.refersToLocalHost()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Globus file: copy remote to local");
			}

			copyToLocal(fixURI(toURI()), fixURI(dest));

			return;
		}

		if (toURI().refersToLocalHost()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Globus file: copy local to remote");
			}

			copyToRemote(fixURI(toURI()), fixURI(dest));

			return;
		}

		// source is remote, dest is remote.
		if (logger.isDebugEnabled()) {
			logger.debug("Globus file: copy remote to remote");
		}

		copyThirdParty(fixURI(toURI()), fixURI(dest));
	}

	// first try efficient 3rd party transfer.
	// If that fails, try copying using temp file.
	protected void copyThirdParty(URI src, URI dest)
			throws GATInvocationException {
		// this only works with passive = false
		Preferences p2 = (Preferences) preferences.clone();
		p2.put("ftp.connection.passive", "false");

		FTPClient srcClient = null;
		FTPClient destClient = null;
		File tmpFile = null;

		try {
			srcClient = createClient(gatContext, p2, src);
			destClient = createClient(gatContext, p2, dest);

			HostPort hp = destClient.setPassive();
			srcClient.setActive(hp);

			boolean append = true;
			String remoteSrcFile = src.getPath();
			String remoteDestFile = dest.getPath();

			srcClient.transfer(remoteSrcFile, destClient, remoteDestFile,
					append, null);
		} catch (Exception e) {
			try {
				// use a local tmp file.
				java.io.File tmp = null;
				tmp = java.io.File.createTempFile("GATgridFTP", ".tmp");
				URI u = new URI("any:///" + tmp.getPath());
				if (logger.isDebugEnabled()) {
					logger.debug("thirdparty copy failed, using temp file: "
							+ u);
				}
				tmpFile = GAT.createFile(gatContext, preferences, u);

				copyToLocal(src, u);
				tmpFile.copy(dest);
			} catch (Exception e2) {
				GATInvocationException oops = new GATInvocationException();
				oops.add("Globus file", e);
				oops.add("Globus file", e2);

				throw oops;
			}
		} finally {
			if (srcClient != null)
				destroyClient(srcClient, src, p2);
			if (destClient != null)
				destroyClient(destClient, dest, p2);
			if (tmpFile != null) {
				try {
					tmpFile.delete();
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}

	protected void copyToRemote(URI src, URI dest)
			throws GATInvocationException {
		// copy from the local machine to a remote machine.
		FTPClient client = null;

		try {
			String remotePath = dest.getPath();
			String localPath = src.getPath();
			java.io.File localFile = new java.io.File(localPath);

			if (logger.isDebugEnabled()) {
				logger.debug("copying from " + localPath + " to " + remotePath);
			}

			client = createClient(dest);
			client.getCurrentDir(); // to ensure a command has been executed
			setActiveOrPassive(client, preferences);
			client.put(localFile, remotePath, false); // overwrite
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		} finally {
			if (client != null) {
				destroyClient(client, dest, preferences);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#copy(java.net.URI)
	 */
	protected void copyToLocal(URI src, URI dest) throws GATInvocationException {
		FTPClient client = null;

		// copy from a remote machine to the local machine
		try {
			String remotePath = src.getPath();
			String localPath = dest.getPath();
			java.io.File localFile = new java.io.File(localPath);

			if (logger.isDebugEnabled()) {
				logger.debug("copying from " + remotePath + " to " + localPath);
			}

			client = createClient(src);
			client.getCurrentDir(); // to ensure a command has been executed
			setActiveOrPassive(client, preferences);

			client.get(remotePath, localFile);
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		} finally {
			if (client != null)
				destroyClient(client, src, preferences);
		}
	}

	public long lastModified() throws GATInvocationException {
		FTPClient client = null;

		try {
			client = createClient(toURI());
			return client.getLastModified(getPath()).getTime();
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		} finally {
			if (client != null)
				destroyClient(client, toURI(), preferences);
		}
	}

	public boolean delete() throws GATInvocationException {
		FTPClient client = null;

		try {
			String remotePath = getPath();
			client = createClient(toURI());

			if (!client.exists(remotePath)) {
				return false;
			}

			if (isDirectory()) {
				client.deleteDir(remotePath);
			} else {
				client.deleteFile(remotePath);
			}
		} catch (ServerException s) {
			if (s.getCode() == ServerException.SERVER_REFUSED) { // file not
																	// found
				return false;
			} else {
				throw new GATInvocationException("gridftp", s);
			}
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		} finally {
			if (client != null)
				destroyClient(client, toURI(), preferences);
		}

		return true;
	}

	// aarg, the COG returns a flakey name for links.
	protected String getName(FileInfo info) {
		if (info.isSoftLink()) {
			int pos = info.getName().indexOf(" ->");

			if (pos != -1) {
				return info.getName().substring(0, pos);
			}
		}

		return info.getName();
	}

	public String[] list() throws GATInvocationException {
		FTPClient client = null;

		try {
			if (!isDirectory()) {
				return null;
			}
			String remotePath = getPath();

			client = createClient(toURI());

			if (!remotePath.equals("")) {
				client.changeDir(remotePath);
			}

			setActiveOrPassive(client, preferences);

			Vector v = null;

			// we know it is a dir, so we can use this call.
			// for some reason, on old servers the list() method returns
			// an empty list if there are many files.
			v = listNoMinusD(client, remotePath);

			Vector result = new Vector();

			for (int i = 0; i < v.size(); i++) {
				FileInfo info = ((FileInfo) v.get(i));
				if (info.getName().equals("."))
					continue;
				if (info.getName().equals(".."))
					continue;

				result.add(getName(info));
			}

			String[] res = new String[result.size()];
			for (int i = 0; i < result.size(); i++) {
				res[i] = (String) result.get(i);
			}
			return res;
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		} finally {
			if (client != null) {
				destroyClient(client, toURI(), preferences);
			}
		}
	}

	public File[] listFiles() throws GATInvocationException {
		if (logger.isInfoEnabled()) {
			logger.info("list files of: " + location);
		}
		FTPClient client = null;
		String CWD = null;

		try {
			if (!isDirectory()) {
				if (logger.isDebugEnabled()) {
					logger.debug("listFiles: not a directory");
				}
				return null;
			}
			String remotePath = getPath();

			client = createClient(toURI());
			CWD = client.getCurrentDir();

			if (!remotePath.equals("")) {
				client.changeDir(remotePath);
			}

			setActiveOrPassive(client, preferences);

			Vector v = null;

			// we know it is a dir, so we can use this call.
			// for some reason, on old servers the list() method returns
			// an empty list if there are many files.
			// v = listNoMinusD(client, remotePath);
			v = client.list();

			Vector result = new Vector();

			for (int i = 0; i < v.size(); i++) {
				FileInfo info = ((FileInfo) v.get(i));

				if (info.getName().equals("."))
					continue;
				if (info.getName().equals(".."))
					continue;

				String uri = location.toString();
				if (!uri.endsWith("/")) {
					uri += "/";
				}
				uri += getName(info);

				// Improve the performance of further file accesses to the list.
				// pass the FileInfo object via the preferences.
				Preferences newPrefs = (Preferences) preferences.clone();
				newPrefs.put("GAT_INTERNAL_FILE_INFO", info);
				result.add(GAT.createFile(gatContext, newPrefs, new URI(uri)));
			}

			File[] res = new File[result.size()];
			for (int i = 0; i < result.size(); i++) {
				res[i] = (File) result.get(i);
			}
			return res;
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		} finally {
			if (client != null) {
				if (CWD != null) {
					try {
						client.changeDir(CWD);
					} catch (Exception e) {
						// ignore
					}
				}
				destroyClient(client, toURI(), preferences);
			}
		}
	}

	protected FileInfo getInfo() throws GATInvocationException {
		if (cachedInfo != null) {
			return cachedInfo;
		}

		if (isOldServer(preferences)) {
			if (isDirectorySlow()) {
				throw new GATInvocationException(
						"an old server cannot get info for a directory because it does not "
								+ "support the \"list -d\" command\n"
								+ "If you need this functionality, please upgrade your server.");
			}
		}
		FTPClient client = null;

		try {
			String remotePath = getPath();

			if (logger.isDebugEnabled()) {
				logger.debug("getINFO: remotePath = " + remotePath
						+ ", creating client to: " + toURI());
			}

			client = createClient(toURI());

			if (logger.isDebugEnabled()) {
				logger.debug("getINFO: client created");
			}

			setActiveOrPassive(client, preferences);

			Vector v = null;

			if (isOldServer(preferences)) {
				v = listNoMinusD(client, remotePath);
			} else {
				v = client.list(remotePath);
			}

			if (v.size() == 0) {
				throw new GATInvocationException("File not found");
			} else if (v.size() != 1) {
				// just use the info for "."
				for (int i = 0; i < v.size(); i++) {
					FileInfo tmp = (FileInfo) v.get(i);
					if (tmp.getName().equals(".")) {
						tmp.setName(getName());
						cachedInfo = tmp;
						break;
					}
				}

				if (cachedInfo == null) {
					throw new GATInvocationException(
							"Internal error: size of list is not 1 and could not find \".\", remotePath = "
									+ remotePath + ", list is: " + v);
				}

				// System.err.println("USING CACHED INFO FOR .");
			} else {
				cachedInfo = (FileInfo) v.get(0);
			}

			// System.err.println("INFO: " + cachedInfo);
			return cachedInfo;
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		} finally {
			if (client != null)
				destroyClient(client, toURI(), preferences);
		}
	}

	private boolean isDirectorySlow() throws GATInvocationException {
		// return cached value if we know it.
		int isDirVal = isDir(location);
		if (isDirVal == 0) {
			return false;
		} else if (isDirVal == 1) {
			return true;
		}
		// don't know yet... Try the slow method now

		boolean dir = true;
		String remotePath = getPath();

		if (logger.isDebugEnabled()) {
			logger.debug("getINFO: remotePath = " + remotePath
					+ ", creating client to: " + toURI());
		}

		FTPClient client = createClient(toURI());

		if (logger.isDebugEnabled()) {
			logger.debug("getINFO: client created");
		}

		String cwd = null;

		try {
			cwd = client.getCurrentDir();
		} catch (Exception e) {
			if (client != null)
				destroyClient(client, toURI(), preferences);
			throw new GATInvocationException("gridftp", e);
		}

		try {
			client.changeDir(remotePath);
		} catch (Exception e) {
			// ok, it was not a dir :-)
			dir = false;
		}

		try {
			client.changeDir(cwd);
		} catch (Exception e) {
			if (client != null)
				destroyClient(client, toURI(), preferences);
			throw new GATInvocationException("gridftp", e);
		}

		if (client != null)
			destroyClient(client, toURI(), preferences);

		setIsDir(location, dir);
		return dir;
	}

	public boolean isDirectory() throws GATInvocationException {

		// return cached value if we know it.
		int isDirVal = isDir(location);
		if (isDirVal == 0) {
			return false;
		} else if (isDirVal == 1) {
			return true;
		}
		// don't know yet...

		// create a seperate file object to determine whether this file
		// is a directory. This is needed, because the source might be a local
		// file, and some adaptors might not work locally (like gridftp).
		// This goes wrong for local -> remote copies.
		if (toURI().refersToLocalHost()) {
			try {
				java.io.File f = new java.io.File(getPath());
				boolean res = f.isDirectory();
				setIsDir(location, res);
				return res;
			} catch (Exception e) {
				throw new GATInvocationException("globus", e);
			}
		} else {
			return realIsDirectory();
		}
	}

	private boolean realIsDirectory() throws GATInvocationException {
		if (logger.isDebugEnabled()) {
			logger.debug("real isDir on " + toURI());
		}

		// First, try the "fast" method.
		try {
			FileInfo info = getInfo();
			if (info.isDirectory()) {
				setIsDir(location, true);
				return true;
			} else if (info.isDevice()) {
				setIsDir(location, false);
				return false;
			} else if (info.isFile()) {
				setIsDir(location, false);
				return false;
			}

			// it can also be a link, so continue with slow method
		} catch (GATInvocationException e) {
			if (e.getMessage().equals("File not found: " + location)) {
				if (logger.isDebugEnabled()) {
					logger.debug("file not found in isDirectory: "
							+ location);
				}
				return false;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("fast isDirectory failed, falling back to slower version: "
								+ e);
			}
		}

		return isDirectorySlow();
	}

	public boolean isFile() throws GATInvocationException {
		return !isDirectory();
	}

	public boolean canRead() throws GATInvocationException {
		try {
			FileInfo info = getInfo();

			return info.userCanRead();
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	}

	public long length() throws GATInvocationException {
		try {
			if (isDirectory())
				return 0;
		} catch (Exception e) {
			// Hmm, that did not work.
			// let's assume it is a file, and continue.
		}

		try {
			FileInfo info = getInfo();

			return info.getSize();
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	}

	public boolean mkdir() throws GATInvocationException {
		FTPClient client = null;

		try {
			String remotePath = getPath();
			client = createClient(toURI());

			client.makeDir(remotePath);
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		} finally {
			if (client != null)
				destroyClient(client, toURI(), preferences);
		}

		return true;
	}

	public boolean canWrite() throws GATInvocationException {
		try {
			FileInfo info = getInfo();

			return info.userCanWrite();
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	}

	public boolean exists() throws GATInvocationException {
		if (cachedInfo != null) {
			return true;
		}

		FTPClient client = null;

		try {
			String remotePath = getPath();

			if (logger.isDebugEnabled()) {
				logger.debug("getINFO: remotePath = " + remotePath
						+ ", creating client to: " + toURI());
			}

			client = createClient(toURI());

			if (logger.isDebugEnabled()) {
				logger.debug("getINFO: client created");
			}

			return client.exists(remotePath);
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		} finally {
			if (client != null)
				destroyClient(client, toURI(), preferences);
		}
	}

	public String getAbsolutePath() throws GATInvocationException {
		if (getPath().startsWith("/")) {
			return getPath();
		}

		FTPClient client = null;

		try {
			client = createClient(toURI());

			String dir = client.getCurrentDir();

			return dir + "/" + getPath();
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		} finally {
			if (client != null)
				destroyClient(client, toURI(), preferences);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.cpi.FileCpi#renameTo(org.gridlab.gat.io.File)
	 */
	public boolean renameTo(File dest) throws GATInvocationException {
		FTPClient client = null;

		try {
			client = createClient(location);
			client.rename(getPath(), dest.getPath());
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		} finally {
			if (client != null)
				destroyClient(client, toURI(), preferences);
		}

		return true;
	}

	static protected int getProtectionMode(Preferences preferences) {
		String mode = (String) preferences.get("ftp.connection.protection");

		if (mode == null) {
			return -1;
		}

		if (mode.equalsIgnoreCase("clear")) {
			return GridFTPSession.PROTECTION_CLEAR;
		} else if (mode.equalsIgnoreCase("confidential")) {
			return GridFTPSession.PROTECTION_CONFIDENTIAL;
		} else if (mode.equalsIgnoreCase("private")) {
			return GridFTPSession.PROTECTION_PRIVATE;
		} else if (mode.equalsIgnoreCase("safe")) {
			return GridFTPSession.PROTECTION_SAFE;
		} else {
			throw new Error("Illegal channel protection mode: " + mode);
		}
	}

	static protected boolean isOldServer(Preferences preferences) {
		boolean old;
		String tmp = (String) preferences.get("ftp.server.old");

		if ((tmp != null) && tmp.equalsIgnoreCase("true")) {
			old = true;
		} else {
			old = false;
		}

		return old;
	}

	static protected boolean noAuthentication(Preferences preferences) {
		boolean noAuth;
		String tmp = (String) preferences.get("ftp.server.noauthentication");

		if ((tmp != null) && tmp.equalsIgnoreCase("true")) {
			noAuth = true;
		} else {
			noAuth = false;
		}

		return noAuth;
	}

	protected static void setActiveOrPassive(FTPClient c,
			Preferences preferences) throws GATInvocationException {
		if (isPassive(preferences)) {
			if (logger.isDebugEnabled()) {
				logger.debug("gridftp: using local active / remote PASSIVE");
			}

			/** Assume the local host is behind a firewall */
			try {
				c.setPassive();
				c.setLocalActive();
			} catch (Exception e) {
				throw new GATInvocationException("globus", e);
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("gridftp: using local passive / remote ACTIVE");
			}

			try {
				c.setLocalPassive();
				c.setActive();
			} catch (Exception e) {
				throw new GATInvocationException("globus", e);
			}
		}
	}

	/**
	 * This method is used for old servers that do not support the "list -d"
	 * command. The problem is that is does not work for directories, only for
	 * files.
	 */
	private Vector listNoMinusD(FTPClient c, String filter)
			throws ServerException, ClientException, IOException {
		final ByteArrayOutputStream received = new ByteArrayOutputStream(1000);

		// unnamed DataSink subclass will write data channel content
		// to "received" stream.
		DataSink sink = new DataSink() {
			public void write(Buffer buffer) throws IOException {
				received.write(buffer.getBuffer(), 0, buffer.getLength());
			}

			public void close() throws IOException {
			};
		};

		c.list(filter, "", sink);

		// transfer done. Data is in received stream.
		// convert it to a vector.

		// System.err.println("result of list " + filter + " is: "
		// + received.toString());

		BufferedReader reader = new BufferedReader(new StringReader(received
				.toString()));

		Vector fileList = new Vector();
		FileInfo fileInfo = null;
		String line = null;

		while ((line = reader.readLine()) != null) {
			if (line.startsWith("total")) {
				continue;
			}

			try {
				fileInfo = new FileInfo(fixListReply(line));
				fileList.addElement(fileInfo);
			} catch (org.globus.ftp.exception.FTPException e) {
				System.err
						.println("globus file adaptor: WARNING, could not create FileInfo for: "
								+ line);
			}
		}

		return fileList;
	}

	/**
	 * Some servers seem to return a strange format directory listing that the
	 * COG does not parse. For instance, the NCSA grid ftp server returns
	 * something like:
	 * 
	 * -rw------- 1 ccguser ac DK common 861 Jul 26 16:56 qcrjm.hist
	 * 
	 * while the cog only parses:
	 * 
	 * drwxr-xr-x 2 guest other 1536 Jan 31 15:15 run.bat
	 * 
	 * This method removes the unused tokens from the reply
	 */
	public String fixListReply(String reply) throws FTPException {
		StringTokenizer tokens = new StringTokenizer(reply);

		if (logger.isDebugEnabled()) {
			logger.debug("fixing old ftp server list reply: " + reply
					+ "#tokens = " + tokens.countTokens());
		}

		if (tokens.countTokens() < 10) {
			return reply;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("COG workaround parsing old ftp server list reply.");
		}

		String res = "";

		res += tokens.nextToken(); // permissions
		res += " " + tokens.nextToken(); // ???
		res += " " + tokens.nextToken(); // owner
		tokens.nextToken(); // skip
		tokens.nextToken(); // skip

		/*
		 * res += " " + tokens.nextToken(); // group res += " " +
		 * tokens.nextToken(); // size res += " " + tokens.nextToken(); // month
		 * res += " " + tokens.nextToken(); // day res += " " +
		 * tokens.nextToken(); // time res += " " + tokens.nextToken(); //
		 * filename
		 */

		// if there are more tokens, just add them
		while (tokens.hasMoreTokens()) {
			res += " " + tokens.nextToken();
		}

		if (logger.isDebugEnabled()) {
			logger.debug("fixed version is: " + res);
		}

		return res;
	}

	/**
	 * 
	 * @param location
	 * @return 1 if dir, 0 if not, -1 if unknown
	 */
	private synchronized static int isDir(URI location) {
		Integer val = (Integer) isDirCache.get(location);
		if (val == null)
			return -1;

		if (logger.isDebugEnabled()) {
			logger.debug("cached isDir of " + location + " result = "
					+ val);
		}

		if (val.intValue() == 1)
			return 1;
		if (val.intValue() == 0)
			return 0;

		throw new Error("Internal error, illegal value in isDir");
	}

	private synchronized static void setIsDir(URI location, boolean isDir) {
		if (logger.isDebugEnabled()) {
			logger.debug("set cached dir of " + location + " to " + isDir);
		}
		if (isDirCache.size() > 5000) {
			isDirCache.clear();
		}
		int val = -1;
		if (isDir)
			val = 1;
		else
			val = 0;

		isDirCache.put(location, new Integer(val));
	}
}
