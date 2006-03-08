package org.gridlab.gat.io.cpi.globus;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Vector;

import org.globus.ftp.FTPClient;
import org.globus.ftp.FileInfo;
import org.globus.ftp.HostPort;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.IPUtils;
import org.gridlab.gat.io.cpi.FileCpi;

public abstract class GlobusFileAdaptor extends FileCpi {
	static final int DEFAULT_GRIDFTP_PORT = 2811;

	static final int DEFAULT_FTP_PORT = 21;

	static final int DEFAULT_HTTP_PORT = 80;

	static final int DEFAULT_HTTPS_PORT = 443;

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
			URI location) throws AdaptorCreationException {
		super(gatContext, preferences, location);
	}

	protected abstract URI fixURI(URI in);

	protected abstract FTPClient createClient(URI hostURI)
			throws GATInvocationException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#copy(java.net.URI)
	 */
	public void copy(URI dest) throws GATInvocationException, IOException {
		// We don't have to handle the local case, the GAT engine will select
		// the local adaptor.
		if (IPUtils.isLocal(dest) && IPUtils.isLocal(toURI())) {
			throw new GATInvocationException("gridftp cannot copy local files");
		}

		if (isDirectory()) {
			copyDirectory(gatContext, preferences, toURI(), dest);
			return;
		}

		if (IPUtils.isLocal(dest)) {
			if (GATEngine.DEBUG) {
				System.err.println("Globus file: copy remote to local");
			}
			copyToLocal(fixURI(toURI()), fixURI(dest));
			return;
		}

		if (IPUtils.isLocal(toURI())) {
			if (GATEngine.DEBUG) {
				System.err.println("Globus file: copy local to remote");
			}
			copyToRemote(fixURI(toURI()), fixURI(dest));
			return;
		}

		// source is remote, dest is remote.
		if (GATEngine.DEBUG) {
			System.err.println("Globus file: copy remote to remote");
		}
		copyThirdParty(fixURI(toURI()), fixURI(dest));
	}

	// first try efficient 3rd party transfer.
	// If that fails, try copying using temp file.
	protected void copyThirdParty(URI src, URI dest)
			throws GATInvocationException {
		try {
			FTPClient srcClient = createClient(src);
			FTPClient destClient = createClient(dest);
			HostPort hp = destClient.setPassive();
			srcClient.setActive(hp);

			boolean append = true;
			String remoteSrcFile = this.getPath();
			String remoteDestFile = IPUtils.getPath(dest);

			srcClient.transfer(remoteSrcFile, destClient, remoteDestFile,
					append, null);

			destClient.close();
			srcClient.close();

		} catch (Exception e) {
			try {
				// use a local tmp file.
				File tmp = null;
				tmp = java.io.File.createTempFile("GATgridFTP", ".tmp");

				copyToLocal(toURI(), tmp.toURI());
				copyToRemote(tmp.toURI(), dest);
			} catch (Exception e2) {
				GATInvocationException oops = new GATInvocationException();
				oops.add("Globus file", e);
				oops.add("Globus file", e2);

				throw oops;
			}
		}
	}

	protected void copyToRemote(URI src, URI dest)
			throws GATInvocationException {
		// copy from the local machine to a remote machine.

		try {
			String remotePath = IPUtils.getPath(dest);
			String localPath = IPUtils.getPath(src);
			File localFile = new File(localPath);

			if (GATEngine.DEBUG) {
				System.err.println("copying from " + localPath + " to "
						+ remotePath);
			}

			FTPClient client = createClient(dest);
			client.put(localFile, remotePath, false); // overwrite
			client.close();

		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	} /*
	   * (non-Javadoc)
	   * 
	   * @see org.gridlab.gat.io.File#copy(java.net.URI)
	   */

	protected void copyToLocal(URI src, URI dest) throws GATInvocationException {
		// copy from a remote machine to the local machine
		try {
			String remotePath = IPUtils.getPath(src);
			String localPath = IPUtils.getPath(dest);
			File localFile = new File(localPath);

			if (GATEngine.DEBUG) {
				System.err.println("copying from " + remotePath + " to "
						+ localPath);
			}

			FTPClient client = createClient(src);
			client.get(remotePath, localFile);
			client.close();
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	}

	public boolean delete() throws GATInvocationException {
		try {
			String remotePath = getPath();
			FTPClient client = createClient(toURI());

			if (isDirectory()) {
				client.deleteDir(remotePath);
			} else {
				client.deleteFile(remotePath);
			}
			client.close();
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
		return false;
	}

	//  aarg, the COG returns a flakey name for links.
	protected String getName(FileInfo info) {
		if (info.isSoftLink()) {
			int pos = info.getName().indexOf(" ->");
			if (pos != -1) {
				return info.getName().substring(0, pos);
			}
		}

		return info.getName();
	}

	public String[] list() throws IOException, GATInvocationException {
		try {
			if (isDirectory()) {
				String remotePath = getPath();

				FTPClient client = createClient(toURI());
				client.changeDir(remotePath);
				Vector v = client.list();
				client.close();

				String[] res = new String[v.size()];
				for (int i = 0; i < v.size(); i++) {
					FileInfo info = ((FileInfo) v.get(i));
					res[i] = getName(info);
				}

				return res;
			}

			// OK, not a dir, just return the list...
			String[] res = new String[1];
			res[0] = getName(getInfo());
			return res;
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	}

	protected FileInfo getInfo() throws GATInvocationException {
	    if (cachedInfo != null) {
//			System.err.println("INFO: " + cachedInfo);
			return cachedInfo;
	    }

		try {
			String remotePath = getPath();

			FTPClient client = createClient(toURI());
			Vector v = client.list(remotePath);
			client.close();

			if (v.size() != 1) {
				throw new GATInvocationException(
						"Internal error: size of list is not 1");
			}

			cachedInfo = (FileInfo) v.get(0);

//			System.err.println("INFO: " + cachedInfo);
			return cachedInfo;
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	}

	public boolean isDirectory() throws GATInvocationException {
		try {
			FileInfo info = getInfo();
			return info.isDirectory();
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	}

	public boolean isFile() throws GATInvocationException {
		try {
			FileInfo info = getInfo();
			return info.isFile();
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	}

	public boolean canRead() throws GATInvocationException {
		try {
			FileInfo info = getInfo();
			return info.userCanRead();
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	}

	public boolean mkdir() throws GATInvocationException, IOException {
		try {
			String remotePath = getPath();
			FTPClient client = createClient(toURI());

			System.err.println("REMOTEPATH: " + remotePath);

			client.makeDir(remotePath);
			client.close();
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
		return false;
	}

	public boolean canWrite() throws GATInvocationException {
		try {
			FileInfo info = getInfo();
			return info.userCanWrite();
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	}
}
