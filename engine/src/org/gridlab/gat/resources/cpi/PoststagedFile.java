/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;

public class PoststagedFile extends StagedFile {
    boolean isStdout;
    boolean isStderr;
    
    public PoststagedFile(GATContext context, Preferences preferences, File origSrc, File origDest, String host, String sandbox, boolean isStdOut, boolean isStderr)  throws GATInvocationException {
        super(context, preferences, origSrc, origDest, host, sandbox);
        this.isStdout = isStdOut;
        this.isStderr = isStderr;
        resolve();
    }

    /* Creates a file object for the destination of the preStaged src file */
    private void resolve() throws GATInvocationException {
        resolvedDest = origDest;

        if (origSrc != null) { // already set manually
            if (origSrc.isAbsolute()) {
                resolvedSrc = origSrc;
                inSandbox = false;
            } else {
                resolvedSrc = resolve(origSrc, false);
                inSandbox = true;
            }
        } else {
            resolvedSrc = resolve(origDest, true);
            inSandbox = true;
        }
    }

    protected void poststage() throws GATInvocationException {
        if (GATEngine.VERBOSE) {
            System.err.println("  copy " + resolvedSrc.toURI() + " to "
                + resolvedDest.toURI());
        }

        resolvedSrc.copy(resolvedDest.toURI());
    }

    protected void delete(boolean deleteFilesInSandbox) throws GATInvocationException {
        if(deleteFilesInSandbox || !inSandbox) {
            if (GATEngine.VERBOSE) {
                System.err.println("DELETE_FILE:" + resolvedSrc);
            }
            resolvedSrc.delete();
        }
    }

    protected void wipe(boolean onlySandbox) throws GATInvocationException {
        if(!onlySandbox || (onlySandbox && inSandbox)) {
            if (GATEngine.VERBOSE) {
                System.err.println("WIPE_FILE:" + resolvedSrc);
            }
            wipe(resolvedSrc);
        }
    }

    public String toString() {
        return "PostStaged: " + resolvedSrc.toURI() + " -> " + resolvedDest.toURI()
            + (isStdout ? " (STDOUT)" : "") + (isStderr ? " (STDERR)" : "");
    }
}
