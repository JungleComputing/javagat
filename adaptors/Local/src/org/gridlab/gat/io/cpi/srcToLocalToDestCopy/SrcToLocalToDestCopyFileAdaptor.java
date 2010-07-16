/*
 * Created on Mar 21, 2007 by rob
 */
package org.gridlab.gat.io.cpi.srcToLocalToDestCopy;

import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;

@SuppressWarnings("serial")
public class SrcToLocalToDestCopyFileAdaptor extends FileCpi {

    public static String getDescription() {
        return "The SrcToLocalToDest File Adaptor only implements the File copy operation, via a local temporary file. Its purpose is to allow copying of a file from one adaptor to another.";
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("copy", true);
        return capabilities;
    }

    public SrcToLocalToDestCopyFileAdaptor(GATContext gatContext, URI location) {
        super(gatContext, location);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#copy(org.gridlab.gat.URI)
     */
    public void copy(URI dest) throws GATInvocationException {
        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        if (dest.refersToLocalHost()) {
            throw new GATInvocationException(
                    "SrcToLocalToDestCopyFileAdaptor destination refers to localhost");
        }

        if (toURI().refersToLocalHost()) {
            throw new GATInvocationException(
                    "SrcToLocalToDestCopyFileAdaptor source refers to localhost");
        }

        File tmpFile = null;

        try {
            // use a local tmp file.
            java.io.File tmp = null;
            tmp = java.io.File.createTempFile("GATgridFTP", ".tmp");
            URI tmpFileURI = new URI("any:///" + tmp.getPath());

            File srcFile = GAT.createFile(gatContext, location);
            srcFile.copy(tmpFileURI);

            tmpFile = GAT.createFile(gatContext, tmpFileURI);
            tmpFile.copy(dest);
        } catch (Exception e) {
            throw new GATInvocationException("SrcToLocalToDestCopyFileAdaptor",
                    e);
        } finally {
            if (tmpFile != null) {
                try {
                    tmpFile.delete();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
