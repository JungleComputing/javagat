package org.gridlab.gat.io.cpi.glite.lfc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.AccessType;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.LFCFile;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.LFCReplica;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.ReceiveException;
import org.gridlab.gat.io.cpi.glite.srm.SrmConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a High-Level view of an LFC server.
 * 
 * @author Max Berger
 * @author Jerome Revillard
 */
public class LfcConnector {
	private static final Logger LOGGER = LoggerFactory.getLogger(LfcConnector.class);

	private final String vo;
	private final String server;
	private final int port;
	private final String proxyPath;

	/**
	 * Create a new LfcConector
	 * 
	 * @param server Server to connect to
	 * @param port Port to connect to
	 * @param vo VO of this server
	 */
	public LfcConnector(String server, int port, String vo, String proxyPath) {
		this.server = server;
		this.port = port;
		this.vo = vo;
		this.proxyPath = proxyPath;
	}

	/**
	 * Get the list of file replicas according to the file path or to the file guid
	 * 
	 * @param path Path of the file
	 * @param guid GUID of the file
	 * @return A list of SRM URIs
	 * @throws IOException if anything goes wrong
	 */
	public Collection<LFCReplica> listReplicas(String path, String guid) throws IOException {
		final LfcConnection connection = new LfcConnection(server, port, proxyPath);
		final Collection<LFCReplica> retVal;
		try {
			retVal = connection.listReplica(path, guid);
		} finally {
			connection.close();
		}
		return retVal;
	}

	/**
	 * Get the different file/directory/symbolic link attributes
	 * 
	 * @param path The file path
	 * @param followSymbolicLink If <code>true</code> and If the path represent a symbolic link, return the pointed
	 *            file/directory attributes, otherwise, return the symbolic link attributes
	 * @return
	 * @throws IOException
	 */
	public LFCFile stat(String path, boolean followSymbolicLink) throws IOException {
		final LfcConnection connection = new LfcConnection(server, port, proxyPath);
		final LFCFile file;
		try {
			if (followSymbolicLink) {
				file = connection.stat(path);
			} else {
				file = connection.lstat(path);
			}
		} finally {
			connection.close();
		}
		return file;
	}

	/**
	 * Test if the file or the directory can be read.
	 * 
	 * @param path
	 * @return <code>true</code> if it can be read
	 * @throws IOException if a problem occurs
	 */
	public boolean canRead(String path) throws IOException {
		final LfcConnection connection = new LfcConnection(server, port, proxyPath);
		try {
			connection.access(path, AccessType.READ_OK);
		} catch (IOException e) {
			if (e instanceof ReceiveException) {
				if (((ReceiveException) e).getError() == 13) { // Permission denied
					return false;
				}
			}
			throw e;
		} finally {
			connection.close();
		}
		return true;
	}

	/**
	 * Test if the file or the directory can be written.
	 * 
	 * @param path
	 * @return <code>true</code> if it can be written
	 * @throws IOException if a problem occurs
	 */
	public boolean canWrite(String path) throws IOException {
		final LfcConnection connection = new LfcConnection(server, port, proxyPath);
		try {
			connection.access(path, AccessType.WRITE_OK);
		} catch (IOException e) {
			if (e instanceof ReceiveException) {
				if (((ReceiveException) e).getError() == 13) { // Permission denied
					return false;
				}
			}
			throw e;
		} finally {
			connection.close();
		}
		return true;
	}

	/**
	 * Test the existence of a path
	 * 
	 * @param path path to test
	 * @return <code>true</code> if the path exists
	 * @throws IOException if anything goes wrong
	 */
	public boolean exist(String path) throws IOException {
		final LfcConnection connection = new LfcConnection(server, port, proxyPath);
		try {
			connection.access(path, AccessType.EXIST_OK);
		} catch (IOException e) {
			if (e instanceof ReceiveException) {
				if (((ReceiveException) e).getError() == 2) { // No such file or directory
					return false;
				}
			}
			throw e;
		} finally {
			connection.close();
		}
		return true;
	}

	/**
	 * Get the group names which correspond to specific gids.
	 */
	public Collection<String> getGrpByGids(int[] gids) throws IOException {
		final LfcConnection connection = new LfcConnection(server, port, proxyPath);
		try {
			return connection.getGrpByGids(gids);
		} finally {
			connection.close();
		}
	}

	public String getUsrByUid(int uid) throws IOException {
		final LfcConnection connection = new LfcConnection(server, port, proxyPath);
		try {
			return connection.getUsrByUid(uid);
		} finally {
			connection.close();
		}
	}

	/**
	 * Get the content of a directory. If the given path is not a directory it will return <code>null</code>
	 * 
	 * @param path path of the directory
	 * @return A collection of files or directories inside the given path or null if the given path is not a directory
	 * @throws IOException if anything goes wrong
	 */
	public Collection<LFCFile> list(String path, boolean followSymbolicLink) throws IOException {
		final LFCFile file = this.stat(path, followSymbolicLink);
		if (!file.isDirectory()) {
			return null;
		}

		final LfcConnection lfcConnection = new LfcConnection(server, port, proxyPath);
		final Collection<LFCFile> files;
		try {
			final long fileID = lfcConnection.opendir(path, null);
			files = lfcConnection.readdir(fileID);
			lfcConnection.closedir();
		} finally {
			lfcConnection.close();
		}
		return files;
	}

	/**
	 * Create a directory in the LFC
	 * 
	 * @param path path of the directory
	 * @throws IOException if anything goes wrong
	 */
	public void mkdir(String path) throws IOException {
		final LfcConnection connection = new LfcConnection(server, port, proxyPath);
		try {
			connection.mkdir(path, UUID.randomUUID().toString());
		} finally {
			connection.close();
		}
	}

	/**
	 * Delete a file or a directory from the LFC. If it's a file, delete also all the replicas from the DPMs
	 * 
	 * @param path path of the file/directory
	 * @return <code>true</code> is everything was ok.
	 * @throws IOException if anything goes wrong
	 */
	public boolean deletePath(String path) throws IOException {
		if (this.stat(path, true).isDirectory()) {
			final LfcConnection connection = new LfcConnection(server, port, proxyPath);
			try {
				connection.rmdir(path);
				return true;
			} finally {
				connection.close();
			}
		} else {
			final LfcConnection connection = new LfcConnection(server, port, proxyPath);
			final Collection<LFCReplica> replicas;
			try {
				replicas = connection.listReplica(path, null);
			} finally {
				connection.close();
			}
			final SrmConnector connector = new SrmConnector(proxyPath);
			for (LFCReplica replica : replicas) {
				LOGGER.info("Deleting Replica: " + replica.getSfn());
				final LfcConnection connection2 = new LfcConnection(server, port, proxyPath);
				try {
					connection2.delReplica(replica.getGuid(), replica.getSfn());
				} finally {
					connection2.close();
				}
				try {
					connector.delete(new URI(replica.getSfn()));
				} catch (URISyntaxException e) {
					// ignore.
				} catch (IOException e) {
					LOGGER.warn("Failed to delete Replica " + replica.getSfn(), e);
				}
			}
			LOGGER.info("Deleting path: " + path);
			final LfcConnection connection3 = new LfcConnection(server, port, proxyPath);
			try {
				connection3.unlink(path);
			} finally {
				connection3.close();
			}
			return true;
		}
	}

	/**
	 * Try to delete a file.
	 * 
	 * @param guid GUID of the file.
	 * @return true if the file was deleted.
	 * @throws IOException if anything goes wrong
	 */
	public boolean deleteGuid(String guid) throws IOException {
		final LfcConnection connection = new LfcConnection(server, port, proxyPath);
		final Collection<LFCReplica> replicas;
		try {
			replicas = connection.listReplica(null, guid);
		} finally {
			connection.close();
		}
		final SrmConnector connector = new SrmConnector(proxyPath);
		for (LFCReplica replica : replicas) {
			LOGGER.info("Deleting Replica: " + replica.getSfn());
			final LfcConnection connection2 = new LfcConnection(server, port, proxyPath);
			try {
				connection2.delReplica(guid, replica.getSfn());
			} finally {
				connection2.close();
			}
			try {
				connector.delete(new URI(replica.getSfn()));
			} catch (URISyntaxException e) {
				// ignore.
			} catch (IOException e) {
				LOGGER.warn("Failed to delete Replica " + replica.getSfn(), e);
			}
		}
		LOGGER.info("Deleting GUID: " + guid);
		final LfcConnection connection3 = new LfcConnection(server, port, proxyPath);
		try {
			return connection3.delFiles(new String[] { guid }, false);
		} finally {
			connection3.close();
		}
	}

	/**
	 * Change access mode of a LFC directory/file. Symbolic link are not supported yet
	 * 
	 * @param path File path
	 * @param mode Absolute UNIX like mode (octal value)
	 * @throws IOException If anything goes wrong
	 */
	public void chmod(String path, int mode) throws IOException {
		LfcConnection connection = new LfcConnection(server, port, proxyPath);
		try {
			connection.chmod(path, mode);
		} finally {
			connection.close();
		}
	}

	/**
	 * Change owner and group of a file or a directory. At least user name or group name need to be specified (both can
	 * be specified) If a name is <code>null</code> then the name is ignored.
	 * 
	 * @param path Current file name
	 * @param recursive Recursive mode
	 * @param followSymbolicLinks If the path is a symbolic link, changes the ownership of the linked file or directory
	 *            instead of the symbolic link itself
	 * @param usrName The new owner of the file
	 * @param grpName The new group of the file
	 * @throws IOException If a problem occurs
	 */
	public void chown(String path, boolean recursive, boolean followSymbolicLinks, String usrName, String grpName)
			throws IOException {
		LfcConnection connection;
		int new_uid = -1;
		int new_gid = -1;
		if (usrName != null) {
			if (usrName.equals("root")) {
				new_uid = 0;
			} else {
				connection = new LfcConnection(server, port, proxyPath);
				try {
					new_uid = connection.getUsrByName(usrName);
				} catch (Exception e) {
					throw new IOException("Unable to find the uid of " + usrName + " in the LFC");
				} finally {
					connection.close();
				}
			}
		}
		if (grpName != null) {
			if (grpName.equals("root")) {
				new_gid = 0;
			} else {
				connection = new LfcConnection(server, port, proxyPath);
				try {
					new_gid = connection.getGrpByName(grpName);
				} catch (IOException e) {
					throw new IOException("Unable to find the gid of " + grpName + " in the LFC");
				} finally {
					connection.close();
				}
			}
		}
		chown(path, recursive, followSymbolicLinks, new_uid, new_gid);
	}

	/**
	 * Change owner and group of a file or a directory. At least user ID or group ID need to be specified (both can be
	 * specified) If an ID value is inferior to 0 then the ID is ignored.
	 * 
	 * @param path Current file name
	 * @param recursive Recursive mode
	 * @param followSymbolicLinks If the path is a symbolic link, changes the ownership of the linked file or directory
	 *            instead of the symbolic link itself
	 * @param new_uid New owner ID
	 * @param new gid New group ID
	 * @throws IOException If a problem occurs
	 */
	public void chown(String path, boolean recursive, boolean followSymbolicLinks, int new_uid, int new_gid)
			throws IOException {
		if (recursive == true) {
			// TODO ....
			throw new UnsupportedOperationException("not implemented");
		}
		if (new_uid < 0 && new_gid < 0) {
			throw new IllegalArgumentException("You must specify at least a new owner or a new group");
		}
		LfcConnection connection = new LfcConnection(server, port, proxyPath);
		try {
			if (followSymbolicLinks) {
				connection.chown(path, new_uid, new_gid);
			} else {
				connection.lchown(path, new_uid, new_gid);
			}
		} finally {
			connection.close();
		}
	}

	/**
	 * Rename a file
	 * 
	 * @param oldPath current file name
	 * @param newPath new file name
	 * @throws IOException If a problem occurs
	 */
	public void rename(String oldPath, String newPath) throws IOException {
		LfcConnection connection = new LfcConnection(server, port, proxyPath);
		try {
			connection.rename(oldPath, newPath);
		} finally {
			connection.close();
		}
	}

	/**
	 * Create a new File
	 * 
	 * @param fileSize the size of the file. If <0 then 0 will be set
	 * @return a GUID to the new file
	 * @throws IOException if anything goes wrong
	 */
	public String create(long fileSize) throws IOException {
		String guid = UUID.randomUUID().toString();
		String parent = "/grid/" + vo + "/generated/" + dateAsPath();
		LfcConnection connection = new LfcConnection(server, port, proxyPath);
		try {
			connection.mkdir(parent, UUID.randomUUID().toString());
		} catch (IOException e) {
			LOGGER.debug("Creating parent", e);
		} finally {
			connection.close();
		}
		String path = parent + "/file-" + guid;
		URI uri = null;
		try {
			uri = new URI("lfn:///" + path);
		} catch (URISyntaxException e) {
		}
		return create(uri, guid, fileSize);
	}

	/**
	 * Create a new File at a specific location
	 * 
	 * @param location The lfn of the file to create (lfn:////grid/vo/...). Please note the 4 (!) slashes. These are
	 *            required to specify an empty hostname and an absolute directory.
	 * @param guid The guid of the file
	 * @param fileSize The size of the file.If <0 then 0 will be set
	 * @return a GUID to the new file
	 * @throws IOException if anything goes wrong
	 */
	public String create(URI location, String guid, long fileSize) throws IOException {
		String path = location.getPath();
		LOGGER.info("Creating " + guid + " with path " + path);
		LfcConnection connection = new LfcConnection(server, port, proxyPath);
		try {
			connection.creat(path, guid);
		} finally {
			connection.close();
		}
		if (fileSize > 0L) {
			connection = new LfcConnection(server, port, proxyPath);
			try {
				connection.setfsize(location.getPath(), fileSize);
			} catch (IOException e) {
				LOGGER.debug("Creating parent", e);
			} finally {
				connection.close();
			}
		}
		return guid;
	}

	private String dateAsPath() {
		Calendar c = GregorianCalendar.getInstance();
		StringBuilder b = new StringBuilder();
		b.append(c.get(Calendar.YEAR));
		b.append('-');
		// Java counts month starting with 0 !
		int month = c.get(Calendar.MONTH) + 1;
		if (month < 10)
			b.append('0');
		b.append(month);
		b.append('-');
		int day = c.get(Calendar.DAY_OF_MONTH);
		if (day < 10)
			b.append('0');
		b.append(day);
		return b.toString();
	}

	/**
	 * Add a Replica entry for the given file
	 * 
	 * @param guid GUID of the file (without decoration)
	 * @param target an SRM uri.
	 * @throws IOException if anything goes wrong
	 */
	public void addReplica(String guid, URI target) throws IOException {
		final LfcConnection connection = new LfcConnection(server, port, proxyPath);
		try {
			connection.addReplica(guid, target.toJavaURI());
		} finally {
			connection.close();
		}
	}

	/**
	 * @return the server used by this connector.
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @return the port used by this connector.
	 */
	public int getPort() {
		return port;
	}

}
