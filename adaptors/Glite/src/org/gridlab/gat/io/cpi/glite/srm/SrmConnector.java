package org.gridlab.gat.io.cpi.glite.srm;

import gov.lbl.srm.StorageResourceManager.ArrayOfTGroupPermission;
import gov.lbl.srm.StorageResourceManager.ArrayOfTUserPermission;
import gov.lbl.srm.StorageResourceManager.TPermissionMode;
import gov.lbl.srm.StorageResourceManager.TPermissionType;

import org.gridlab.gat.io.cpi.glite.srm.SrmConnection.SRMPosixFile;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.gridlab.gat.URI;

/**
 * GAT-Specific functionality for accessing an SRM.
 * 
 * @author Max Berger
 */
public class SrmConnector {

    private final Map<URI, SrmConnection> activeUploads = new TreeMap<URI, SrmConnection>();

    private final String proxyPath;
    /**
     * Default constructor.
     */
    public SrmConnector(final String proxyPath) {
    	this.proxyPath = proxyPath;
    }

    /**
     * Create a Transport URL to download the given file.
     * 
     * @param srmURI
     *            the URI of the file, including the srm:// prefix.
     * @return a Transport URL, which is a GridFTP path.
     * @throws IOException
     *             if no TURL can be created.
     */
    public String getTURLForFileDownload(URI srmURI) throws IOException {
        SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
        return connection.getTURLForFileDownload(srmURI.toString());
    }

    /**
     * Create a Transport URL to upload the given file. Please note: you must
     * call {@link #finalizeFileUpload(URI)} with the same URI when the upload
     * is done!
     * 
     * @param source
     *            Source file URI, must be local.
     * @param dest
     *            Destination file URI, must be srm://
     * @return a Transport URL, which is a GridFTP path.
     * @throws IOException
     *             if no TURL can be created.
     */
    public String getTURLForFileUpload(URI source, URI dest) throws IOException {
        SrmConnection connection = new SrmConnection(dest.getHost(), proxyPath);
        activeUploads.put(dest, connection);
        return connection.getTURLForFileUpload(source.getPath(),
                dest.toString()).toString();
    }

    /**
     * Finalize a file upload.
     * 
     * @param dest
     *            the same URI which has been given to
     *            {@link #getTURLForFileUpload(URI, URI)}.
     * @throws IOException
     *             if the file upload cannot be finalized.
     */
    public void finalizeFileUpload(URI dest) throws IOException {
    	try{
	        SrmConnection connection = activeUploads.get(dest);
	        if (connection == null) {
	            throw new IOException("No active upload to " + dest);
	        } else {
	            connection.finalizeFileUpload();
	        }
    	}finally{
    		//Jerome:TODO activeUploads can induce an OutOfMemorryException ...
    		activeUploads.remove(dest);
    	}
    }

    /**
     * Delete a file on a SRM.
     * 
     * @param srmURI
     *            the URI for the file.
     * @throws IOException
     *             if the file cannot be deleted (e.g. it does not exist).
     */
    public void delete(URI srmURI) throws IOException {
        SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
        connection.removeFile(srmURI.toString());
    }
    
    /**
     * Get the permissions associated to a file.
     * @param srmURI
     * 			the URI for the file.
     * @return the permissions
     * @throws IOException if a problem occurs
     */
    public SRMPosixFile ls(URI srmURI) throws IOException {
    	SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
        return connection.ls(srmURI.toString());
    }
    
    /**
     * @param srmURI
     * 			the URI for the file.
     * @param tPermissionType
     * 			How do we set the following permissions (add, delete or change them).
     * @param ownerTPermissionMode
     * 			The owner permissions
     * @param arrayOfTGroupPermissions
     * 			Array of group permissions
     * @param otherTPermissionMode
     * 			The other permissions
     * @param arrayOfTUserPermissions
     * 			Array of user permissions
     * @throws IOException If a problem occurs
     */
    public void setPermissions(URI srmURI, TPermissionType tPermissionType, 
    		TPermissionMode ownerTPermissionMode,
    		ArrayOfTGroupPermission arrayOfTGroupPermissions,
    		TPermissionMode otherTPermissionMode,
    		ArrayOfTUserPermission arrayOfTUserPermissions) throws IOException {
    	SrmConnection connection = new SrmConnection(srmURI.getHost(), proxyPath);
        connection.setPermissions(srmURI.toString(), tPermissionType, ownerTPermissionMode, arrayOfTGroupPermissions, otherTPermissionMode, arrayOfTUserPermissions);
    }
}
