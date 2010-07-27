package org.gridlab.gat.io.cpi.gliteMultiUser.srm;

import gov.lbl.srm.StorageResourceManager.ArrayOfTGroupPermission;
import gov.lbl.srm.StorageResourceManager.ArrayOfTUserPermission;
import gov.lbl.srm.StorageResourceManager.TPermissionMode;
import gov.lbl.srm.StorageResourceManager.TPermissionType;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInfo;
import org.gridlab.gat.io.cpi.gliteMultiUser.srm.SrmConnection.SRMPosixFile;

/**
 * GAT-Specific functionality for accessing an SRM.
 * 
 * @author Stefan Bozic
 */
public class SrmConnector {

	/** {@link Map} which stores all the active uploads. */
	private final Map<URI, SrmConnection> activeUploads = new TreeMap<URI, SrmConnection>();

	/** The path to the proxy credentials. */
	private final String proxyPath;

	/**
	 * Default constructor.
	 * 
	 * @param proxyPath the path to the proxy credentials.
	 */
	public SrmConnector(final String proxyPath) {
		this.proxyPath = proxyPath;
	}

	/**
	 * Create a Transport URL to download the given file.
	 * 
	 * @param srmURI the URI of the file, including the srm:// prefix.
	 * @return a Transport URL, which is a GridFTP path.
	 * @throws IOException if no TURL can be created.
	 */
	public String getTURLForFileDownload(URI srmURI) throws IOException {
		SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
		return connection.getTURLForFileDownload(srmURI.toString());
	}

	/**
	 * Create a Transport URL to upload the given file. Please note: you must call {@link #finalizeFileUpload(URI)} with
	 * the same URI when the upload is done!
	 * 
	 * @param source Source file URI, must be local.
	 * @param dest Destination file URI, must be srm://
	 * @return a Transport URL, which is a GridFTP path.
	 * @throws IOException if no TURL can be created.
	 */
	public String getTURLForFileUpload(URI source, URI dest) throws IOException {
		SrmConnection connection = new SrmConnection(dest.getHost(), proxyPath);
		activeUploads.put(dest, connection);
		return connection.getTURLForFileUpload(source != null ? source.getPath() : null, dest.toString()).toString();
	}

	/**
	 * Finalize a file upload.
	 * 
	 * @param dest the same URI which has been given to {@link #getTURLForFileUpload(URI, URI)}.
	 * @throws IOException if the file upload cannot be finalized.
	 */
	public void finalizeFileUpload(URI dest) throws IOException {
		try {
			SrmConnection connection = activeUploads.get(dest);
			if (connection == null) {
				throw new IOException("No active upload to " + dest);
			} else {
				connection.finalizeFileUpload();
			}
		} finally {
			// Jerome:TODO activeUploads can induce an OutOfMemorryException ...
			activeUploads.remove(dest);
		}
	}

	/**
	 * Delete a file on a SRM.
	 * 
	 * @param srmURI the URI for the file.
	 * @throws IOException if the file cannot be deleted (e.g. it does not exist).
	 */
	public void delete(URI srmURI) throws IOException {
		SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
		connection.removeFile(srmURI.toString());
	}

	/**
	 * Get the permissions associated to a file.
	 * 
	 * @param srmURI the URI for the file.
	 * @return the permissions
	 * @throws IOException if a problem occurs
	 */
	public SRMPosixFile ls(URI srmURI) throws IOException {
		SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
		return connection.ls(srmURI.toString());
	}

	/**
	 * Creates a new directory on a SRM resource.
	 * 
	 * @param srmURI the URI for the directory to create.
	 * @return <code>true</code> if the directory has been created successfully
	 * 
	 * @throws IOException an exception that might occurs
	 * @throws GATInvocationException an exception that might occurs
	 */
	public boolean mkDir(URI srmURI) throws IOException, GATInvocationException {
		SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
		return connection.mkDir(srmURI.toString());
	}

	/**
	 * Lists the file names to a given {@link URI}.
	 * 
	 * @param srmURI the URI for the file.
	 * @return the permissions
	 * @throws IOException if a problem occurs
	 */
	public List<String> listFileNames(URI srmURI) throws IOException {
		SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
		return connection.listFileNames(srmURI.toString());
	}

	/**
	 * Lists all the {@link FileInfo}s to a given {@link URI}.
	 * 
	 * @param srmURI the URI for the file.
	 * @return the permissions
	 * @throws IOException if a problem occurs
	 */
	public List<FileInfo> listFileInfos(URI srmURI) throws IOException {
		SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
		return connection.listFileInfos(srmURI.toString());
	}

	/**
	 * Checks if the given path exists.
	 * 
	 * @param srmURI the {@link URI} to check
	 * @return <code>true</code> if the given path exists on the storage resource.
	 * @throws IOException an exception that might occurs
	 * @throws GATInvocationException an exception that might occurs
	 */
	public boolean exists(URI srmURI) throws IOException, GATInvocationException {
		SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
		return connection.exists(srmURI.toString());
	}

	/**
	 * Checks if the given path is a file.
	 * 
	 * @param srmURI the {@link URI} to check
	 * @return <code>true</code> if the given path is a file
	 * @throws IOException an exception that might occurs
	 * @throws GATInvocationException an exception that might occurs
	 */
	public boolean isFile(URI srmURI) throws IOException, GATInvocationException {
		SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
		return connection.isFile(srmURI.toString());
	}

	/**
	 * Checks if the given path is a directory.
	 * 
	 * @param srmURI the {@link URI} to check
	 * @return <code>true</code> if the given path is a directory
	 * @throws IOException an exception that might occurs
	 * @throws GATInvocationException an exception that might occurs
	 */
	public boolean isDirectory(URI srmURI) throws IOException, GATInvocationException {
		SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
		return connection.isDirectory(srmURI.toString());
	}

	/**
	 * Renames a file.
	 * 
	 * @param oldURI the {@link URI} of the old filename
	 * @param newURI the {@link URI} of the new filename
	 * 
	 * @return <code>true</code> if the file has been renamed successfully
	 * @throws IOException an exception that might occurs
	 * @throws GATInvocationException an exception that might occurs
	 */
	public boolean renameTo(URI oldURI, URI newURI) throws IOException, GATInvocationException {
		SrmConnection connection = new SrmConnection(oldURI.getHost(), proxyPath);
		return connection.moveFile(oldURI.toString(), newURI.toString());
	}

	/**
	 * @param srmURI the URI for the file.
	 * @param tPermissionType How do we set the following permissions (add, delete or change them).
	 * @param ownerTPermissionMode The owner permissions
	 * @param arrayOfTGroupPermissions Array of group permissions
	 * @param otherTPermissionMode The other permissions
	 * @param arrayOfTUserPermissions Array of user permissions
	 * @throws IOException If a problem occurs
	 */
	public void setPermissions(URI srmURI, TPermissionType tPermissionType, TPermissionMode ownerTPermissionMode,
			ArrayOfTGroupPermission arrayOfTGroupPermissions, TPermissionMode otherTPermissionMode,
			ArrayOfTUserPermission arrayOfTUserPermissions) throws IOException {
		SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
		connection.setPermissions(srmURI.toString(), tPermissionType, ownerTPermissionMode, arrayOfTGroupPermissions,
				otherTPermissionMode, arrayOfTUserPermissions);
	}
}
