package org.gridlab.gat.io.cpi.sshtrilead;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.CommandRunner;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInterface;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.security.sshtrilead.SshTrileadSecurityUtils;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

public class SshTrileadFileAdaptor extends FileCpi {

	/**
	 * On the server side, the "scp" program must be in the PATH.
	 */
	private static final long serialVersionUID = 7343449503574566274L;

	private static Logger logger = Logger
			.getLogger(SshTrileadFileAdaptor.class);

	private static final int SSH_PORT = 22;

	private static final int STDOUT = 0, STDERR = 1, EXIT_VALUE = 2;

	private static Map<URI, Connection> connections = new HashMap<URI, Connection>();

	private static Map<URI, Boolean> isDirCache = new HashMap<URI, Boolean>();

	private static Map<URI, Boolean> isFileCache = new HashMap<URI, Boolean>();

	private static Map<URI, Boolean> existsCache = new HashMap<URI, Boolean>();

	private static Map<URI, Boolean> canReadCache = new HashMap<URI, Boolean>();

	private static Map<URI, Boolean> canWriteCache = new HashMap<URI, Boolean>();

	private static Map<URI, String[]> listCache = new HashMap<URI, String[]>();

	private static boolean canReadCacheEnable = true;

	private static boolean canWriteCacheEnable = true;

	private static boolean existsCacheEnable = true;

	private static boolean isDirCacheEnable = true;

	private static boolean isFileCacheEnable = true;

	private static boolean listCacheEnable = true;

	private static String[] client2serverCiphers = new String[] { "aes256-ctr",
			"aes192-ctr", "aes128-ctr", "blowfish-ctr", "aes256-cbc",
			"aes192-cbc", "aes128-cbc", "blowfish-cbc" };

	private static String[] server2clientCiphers = new String[] { "aes256-ctr",
			"aes192-ctr", "aes128-ctr", "blowfish-ctr", "aes256-cbc",
			"aes192-cbc", "aes128-cbc", "blowfish-cbc" };

	private static boolean tcpNoDelay = true;

	static {
		Properties sshTrileadProperties = new Properties();
		try {
			sshTrileadProperties.load(new java.io.FileInputStream(System
					.getProperty("gat.adaptor.path")
					+ File.separator
					+ "SshTrileadAdaptor"
					+ File.separator
					+ "sshtrilead.properties"));
			if (logger.isDebugEnabled()) {
				logger.debug("reading properties file for sshtrilead adaptor");
			}
			canReadCacheEnable = ((String) sshTrileadProperties.getProperty(
					"caching.canread", "true")).equalsIgnoreCase("true");
			canWriteCacheEnable = ((String) sshTrileadProperties.getProperty(
					"caching.canwrite", "true")).equalsIgnoreCase("true");
			existsCacheEnable = ((String) sshTrileadProperties.getProperty(
					"caching.exists", "true")).equalsIgnoreCase("true");
			isDirCacheEnable = ((String) sshTrileadProperties.getProperty(
					"caching.isdirectory", "true")).equalsIgnoreCase("true");
			isFileCacheEnable = ((String) sshTrileadProperties.getProperty(
					"caching.isfile", "true")).equalsIgnoreCase("true");
			listCacheEnable = ((String) sshTrileadProperties.getProperty(
					"caching.list", "true")).equalsIgnoreCase("true");
			String client2serverCipherString = ((String) sshTrileadProperties
					.getProperty(
							"cipher.client2server",
							"aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc"));
			client2serverCiphers = client2serverCipherString.split(",");
			String server2clientCipherString = ((String) sshTrileadProperties
					.getProperty(
							"cipher.server2client",
							"aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc"));
			server2clientCiphers = server2clientCipherString.split(",");
			tcpNoDelay = ((String) sshTrileadProperties.getProperty(
					"tcp.nodelay", "true")).equalsIgnoreCase("true");
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("exception while trying to read property file: "
						+ e);
			}
		}
	}

	public SshTrileadFileAdaptor(GATContext gatContext, URI location)
			throws GATObjectCreationException, GATInvocationException {
		super(gatContext, fixURI(location, null));
		if (!location.isCompatible("ssh") && !location.isCompatible("file")) {
			throw new AdaptorNotApplicableException("cannot handle this URI");
		}
	}

	public void copy(URI destination) throws GATInvocationException {
		destination = fixURI(destination, null);
		if (location.refersToLocalHost()) {
			// put the file
			try {
				put(destination);
			} catch (Exception e) {
				if (e instanceof GATInvocationException) {
					throw (GATInvocationException) e;
				}
				throw new GATInvocationException("SshTrileadFileAdaptor", e);
			}
		} else {
			if (destination.refersToLocalHost()) {
				// get the file
				try {
					get(destination);
				} catch (Exception e) {
					if (e instanceof GATInvocationException) {
						throw (GATInvocationException) e;
					}
					throw new GATInvocationException("SshTrileadFileAdaptor", e);
				}
			} else {
				// not supported
				throw new GATInvocationException(
						"cannot perform third party file transfers!");
			}
		}
	}

	private void put(URI destination) throws Exception {
		Connection connection = getConnection(destination, gatContext);

		SCPClient client = connection.createSCPClient();
		FileInterface destinationFile = GAT.createFile(gatContext, destination)
				.getFileInterface();
		String remoteDir = null;
		String remoteFileName = null;

		if (gatContext.getPreferences().containsKey("file.create")) {
			if (((String) gatContext.getPreferences().get("file.create"))
					.equalsIgnoreCase("true")) {
				FileInterface destinationParentFile = destinationFile
						.getParentFile().getFileInterface();
				destinationParentFile.mkdirs();
			}
		}

		if (destination.hasAbsolutePath()) {
			if (destinationFile.isDirectory()) {
				remoteDir = destinationFile.getPath();
			} else {
				remoteDir = destinationFile.getParent();
			}
		} else {
			if (destinationFile.isDirectory()) {
				remoteDir = destinationFile.getPath();
			} else {
				remoteDir = ".";
				String parent = destinationFile.getParent();
				if (parent != null) {
					String separator;
					if (isWindows(destination)) {
						separator = "\\";
					} else {
						separator = "/";
					}
					remoteDir += separator + parent;
				}
			}
		}
		remoteFileName = destinationFile.getName();
		String mode = "0600";
		if (gatContext.getPreferences().containsKey("file.chmod")) {
			mode = (String) gatContext.getPreferences().get("file.chmod");
			if (mode.length() == 3) {
				mode = "0" + mode;
			}
			if (mode.length() != 4 || !mode.startsWith("0")) {
				throw new GATInvocationException("invalid mode: '"
						+ gatContext.getPreferences().get("file.chmod")
						+ "'. Should be like '0xxx' or 'xxx'");
			}
		}
		if (destinationFile.isDirectory() && isFile()) {
			client.put(getPath(), remoteDir, mode);
		} else if (isDirectory()) {
			copyDir(destination);
		} else if (isFile()) {
			client.put(getPath(), remoteFileName, remoteDir, mode);
		} else {
			throw new GATInvocationException("cannot copy this file!");
		}
	}

	private void get(URI destination) throws Exception {
		Connection connection = getConnection(location, gatContext);
		SCPClient client = connection.createSCPClient();
		FileInterface destinationFile = GAT.createFile(gatContext, destination)
				.getFileInterface();

		if (gatContext.getPreferences().containsKey("file.create")) {
			if (((String) gatContext.getPreferences().get("file.create"))
					.equalsIgnoreCase("true")) {
				FileInterface destinationParentFile = destinationFile
						.getParentFile().getFileInterface();
				destinationParentFile.mkdirs();
			}
		}

		String mode = "0600";
		if (!isWindows(destination)) {
			if (mode.length() == 3) {
				mode = "0" + mode;
			}
			if (mode.length() != 4 || !mode.startsWith("0")) {
				throw new GATInvocationException("invalid mode: '"
						+ gatContext.getPreferences().get("file.chmod")
						+ "'. Should be like '0xxx' or 'xxx'");
			}
		}
		if (destinationFile.isDirectory() && isFile()) {
			createNewFile(destination.getPath() + "/" + getName(), mode);
			client.get(getPath(), new java.io.FileOutputStream(destination
					.getPath()
					+ "/" + getName()));
		} else if (isDirectory()) {
			copyDir(destination);
		} else if (isFile()) {
			createNewFile(destination.getPath(), mode);
			client.get(getPath(), new java.io.FileOutputStream(destination
					.getPath()));
		} else {
			throw new GATInvocationException("cannot copy this file!");
		}
	}

	private boolean isWindows(URI destination) throws GATInvocationException {
		// if 'ls' gives stderr and 'dir' doesn't then we guess it's Windows
		// else we assume it's non-Windows
		String[] result;
		try {
			result = execCommand("ls");
		} catch (Exception e) {
			throw new GATInvocationException("sshtrilead", e);
		}
		if (result[STDERR].length() != 0) {
			try {
				result = execCommand("dir");
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			return result[STDERR].length() == 0;
		}
		return false;
	}

	private void createNewFile(String localfile, String mode)
			throws GATInvocationException {
		new CommandRunner("touch " + localfile);
		new CommandRunner("chmod " + mode + " " + localfile);
	}

	public static Connection getConnection(URI location, GATContext context)
			throws Exception {
		if (connections.containsKey(location)) {
			return connections.get(location);
		} else {
			String host = location.getHost();
			if (host == null) {
				host = "localhost";
			}
			Connection newConnection = new Connection(host, location
					.getPort(SSH_PORT));

			newConnection.setClient2ServerCiphers(client2serverCiphers);
			newConnection.setServer2ClientCiphers(server2clientCiphers);
			newConnection.setTCPNoDelay(tcpNoDelay);
			newConnection.connect();
			Map<String, Object> securityInfo = SshTrileadSecurityUtils
					.getSshTrileadCredential(context, "sshtrilead", location,
							location.getPort(SSH_PORT));
			String username = (String) securityInfo.get("username");
			String password = (String) securityInfo.get("password");
			java.io.File keyFile = (java.io.File) securityInfo.get("keyfile");

			boolean connected = false;

			if (username != null && password != null) {
				try {
					connected = newConnection.authenticateWithPassword(
							username, password);
				} catch (IOException e) {
					if (logger.isDebugEnabled()) {
						logger
								.debug("exception caught during authentication with password: "
										+ e);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("authentication with password: " + connected);
				}
			}
			if (!connected && username != null && keyFile != null) {
				try {
					connected = newConnection.authenticateWithPublicKey(
							username, keyFile, password);
				} catch (IOException e) {
					if (logger.isDebugEnabled()) {
						logger
								.debug("exception caught during authentication with public key: "
										+ e);
					}
				}
				if (logger.isDebugEnabled()) {
					logger
							.debug("authentication with public key: "
									+ connected);
				}
			}
			if (!connected && username != null) {
				try {
					connected = newConnection.authenticateWithNone(username);
				} catch (IOException e) {
					if (logger.isDebugEnabled()) {
						logger
								.debug("exception caught during authentication with username: "
										+ e);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("authentication with username: " + connected);
				}
			}
			// TODO: add interactive authentication?
			if (!connected) {
				throw new Exception("unable to authenticate");
			} else {
				connections.put(location, newConnection);
			}
			return newConnection;
		}
	}

	private String[] execCommand(String cmd) throws IOException, Exception {
		String[] result = new String[3];
		Session session = getConnection(location, gatContext).openSession();
		session.execCommand(cmd);
		// see http://www.trilead.com/Products/Trilead-SSH-2-Java/FAQ/#blocking
		InputStream stdout = new StreamGobbler(session.getStdout());
		InputStream stderr = new StreamGobbler(session.getStderr());
		BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
		result[STDOUT] = "";
		result[STDERR] = "";
		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}
			result[STDOUT] += line + "\n";
		}
		br = new BufferedReader(new InputStreamReader(stderr));
		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}
			result[STDERR] += line + "\n";
		}
		result[EXIT_VALUE] = "" + session.getExitStatus();
		session.close();
		return result;
	}

	public boolean canRead() throws GATInvocationException {
		if (canReadCacheEnable) {
			if (canReadCache.containsKey(location)) {
				return canReadCache.get(location);
			}
		}
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("test -r " + getPath() + " && echo 0");
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			boolean canread = result[STDOUT].length() != 0;
			if (canReadCacheEnable) {
				canReadCache.put(location, canread);
			}
			return canread;
		}
	}

	public boolean canWrite() throws GATInvocationException {
		if (canWriteCacheEnable) {
			if (canWriteCache.containsKey(location)) {
				return canWriteCache.get(location);
			}
		}
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("test -w " + getPath() + " && echo 0");
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			boolean canwrite = result[STDOUT].length() != 0;
			if (canWriteCacheEnable) {
				canWriteCache.put(location, canwrite);
			}
			return canwrite;
		}
	}

	public boolean createNewFile() throws GATInvocationException {
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("test ! -d " + getPath()
						+ " && test ! -f " + getPath() + " && touch "
						+ getPath());
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			return result[STDERR].length() == 0;
		}
	}

	public boolean delete() throws GATInvocationException {
		if (existsCacheEnable) {
			if (existsCache.containsKey(location)) {
				existsCache.remove(location);
			}
		}
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("rm -rf " + getPath());
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			return result[STDERR].length() == 0;
		}
	}

	public boolean exists() throws GATInvocationException {
		if (existsCacheEnable) {
			if (existsCache.containsKey(location)) {
				return existsCache.get(location);
			}
		}
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("ls " + getPath());
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			// no stderr, and >0 stdout mean that the file exists
			boolean exists = result[STDERR].length() == 0
					&& result[STDOUT].length() > 0;
			if (existsCacheEnable) {
				existsCache.put(location, exists);
			}
			return exists;
		}
	}

	public org.gridlab.gat.io.File getAbsoluteFile()
			throws GATInvocationException {
		String absUri = location.toString().replace(location.getPath(),
				getAbsolutePath());
		try {
			return GAT.createFile(gatContext, new URI(absUri));
		} catch (Exception e) {
			return null; // never executed
		}
	}

	public String getAbsolutePath() throws GATInvocationException {
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("echo ~");
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			return result[STDOUT].replace("\n", "") + "/" + getPath();
		}
	}

	public boolean isDirectory() throws GATInvocationException {
		if (isDirCacheEnable) {
			if (isDirCache.containsKey(location)) {
				return isDirCache.get(location);
			}
		}
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("test -d " + getPath());
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			// 0=dir 1=other
			boolean isDir = result[EXIT_VALUE].equals("0");
			if (isDirCacheEnable) {
				isDirCache.put(location, isDir);
			}
			return isDir;
		}
	}

	public boolean isFile() throws GATInvocationException {
		if (isFileCacheEnable) {
			if (isFileCache.containsKey(location)) {
				return isFileCache.get(location);
			}
		}
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("test -f " + getPath());
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			// 0=file 1=other
			boolean isFile = result[EXIT_VALUE].equals("0");
			if (isFileCacheEnable) {
				isFileCache.put(location, isFile);
			}
			return isFile;
		}
	}

	public boolean isHidden() throws GATInvocationException {
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			return getName().startsWith(".");
		}
	}

	public long length() throws GATInvocationException {
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("wc -c < " + getPath());
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			return Long.parseLong(result[STDOUT].replaceAll("[ \t\n\f\r]", ""));
		}
	}

	public String[] list() throws GATInvocationException {
		if (listCacheEnable) {
			if (listCache.containsKey(location)) {
				return listCache.get(location);
			}
		}
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("ls -1 " + getPath());
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			if (result[STDOUT].equals("")) {
				return null;
			}
			String[] list = result[STDOUT].split("\n");
			if (listCacheEnable) {
				listCache.put(location, list);
			}
			return list;
		}
	}

	public boolean mkdir() throws GATInvocationException {
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("mkdir " + getPath());
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			boolean mkdir = result[STDERR].length() == 0;
			if (isDirCacheEnable && mkdir) {
				isDirCache.put(location, mkdir);
			}
			return mkdir;
		}
	}

	public boolean mkdirs() throws GATInvocationException {
		if (isWindows(location)) {
			return super.mkdirs();
		} else {
			String[] result;
			try {
				result = execCommand("mkdir -p " + getPath());
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			boolean mkdirs = result[STDERR].length() == 0;
			if (isDirCacheEnable && mkdirs) {
				isDirCache.put(location, mkdirs);
			}
			return mkdirs;
		}
	}

	public void move(URI destination) throws GATInvocationException {
		FileInterface destinationFile = null;
		try {
			destinationFile = GAT.createFile(gatContext, destination)
					.getFileInterface();
		} catch (GATObjectCreationException e) {
			throw new GATInvocationException("sshtrilead", e);
		}
		boolean movedIntoExistingDir = (destinationFile.exists() && destinationFile
				.isDirectory());
		copy(destination);
		if (!delete()) {
			throw new GATInvocationException(
					"internal error in SshTrileadFileAdaptor: could not move file "
							+ toURI() + " to " + destination);
		}
		// remove from cache...
		if (canReadCacheEnable) {
			canReadCache.remove(location);
		}
		if (canWriteCacheEnable) {
			canWriteCache.remove(location);
		}
		if (isDirCacheEnable) {
			isDirCache.remove(location);
		}
		if (isFileCacheEnable) {
			isFileCache.remove(location);
		}
		if (existsCacheEnable) {
			existsCache.remove(location);
		}
		if (listCacheEnable) {
			listCache.remove(location);
		}
		// now let the location point to the new location (destination)
		if (movedIntoExistingDir) {
			try {
				location = new URI(destination + "/" + getName());
			} catch (URISyntaxException e) {
				// should not occur
			}
		} else {
			location = destination;
		}
		if (existsCacheEnable) {
			existsCache.put(location, true);
		}
		return;
	}

	public void renameTo(URI destination) throws GATInvocationException {
		move(destination);
		return;
	}

	public boolean renameTo(File file) throws GATInvocationException {
		copy(file.toGATURI());
		if (!delete()) {
			return false;
		}
		location = file.toGATURI();
		return true;
	}

	public boolean setLastModified(long lastModified)
			throws GATInvocationException {
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("touch -t "
						+ toTouchDateFormat(lastModified) + " " + getPath());
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			return result[STDERR].length() == 0;
		}
	}

	private String toTouchDateFormat(long date) {
		Date d = new Date(date);
		// see man touch for the date format
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm.ss");
		return formatter.format(d);
	}

	public boolean setReadOnly() throws GATInvocationException {
		if (isWindows(location)) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			String[] result;
			try {
				result = execCommand("chmod a-w " + getPath());
			} catch (Exception e) {
				throw new GATInvocationException("sshtrilead", e);
			}
			return result[STDERR].length() == 0;
		}
	}
}
