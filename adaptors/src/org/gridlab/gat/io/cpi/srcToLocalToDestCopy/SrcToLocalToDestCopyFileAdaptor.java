/*
 * Created on Mar 21, 2007 by rob
 */
package org.gridlab.gat.io.cpi.srcToLocalToDestCopy;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;

@SuppressWarnings("serial")
public class SrcToLocalToDestCopyFileAdaptor extends FileCpi {
    public SrcToLocalToDestCopyFileAdaptor (GATContext gatContext,
        Preferences preferences, URI location) {
        super(gatContext, preferences, location);
    }

    /* (non-Javadoc)
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

            File srcFile = GAT.createFile(gatContext, preferences, location);
            srcFile.copy(tmpFileURI);

            tmpFile = GAT.createFile(gatContext, preferences, tmpFileURI);
            tmpFile.copy(dest);
        } catch (Exception e) {
            throw new GATInvocationException("SrcToLocalToDestCopyFileAdaptor", e);
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
