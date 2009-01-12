package org.gridlab.gat.io.cpi.glite.srm;

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

    /**
     * Default constructor.
     */
    public SrmConnector() {
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
        SrmConnection connection = new SrmConnection(srmURI.getHost());
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
        SrmConnection connection = new SrmConnection(dest.getHost());
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
        SrmConnection connection = activeUploads.get(dest);
        if (connection == null) {
            throw new IOException("No active upload to " + dest);
        } else {
            connection.finalizeFileUpload();
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
        SrmConnection connection = new SrmConnection(srmURI.getHost());
        connection.removeFile(srmURI.toString());
    }
}
