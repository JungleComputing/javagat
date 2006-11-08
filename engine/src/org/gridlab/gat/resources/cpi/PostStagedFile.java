/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;

public class PostStagedFile extends StagedFile {
    boolean isStdout;

    boolean isStderr;

    public PostStagedFile(GATContext context, Preferences preferences,
            File origSrc, File origDest, String host, String sandbox,
            boolean isStdOut, boolean isStderr) throws GATInvocationException {
        super(context, preferences, origSrc, origDest, host, sandbox);
        this.isStdout = isStdOut;
        this.isStderr = isStderr;
        resolve();
    }

    /* Creates a file object for the destination of the postStaged file.
     * Src cannot be null, dest can be. */
    private void resolve() throws GATInvocationException {
        if (origSrc.isAbsolute()) {
            inSandbox = false;
        } else {
            inSandbox = true;
        }
        resolvedSrc = resolve(origSrc, false);

        String dir = System.getProperty("user.dir");
        if (dir == null) {
            throw new GATInvocationException("cannot get current working directory");
        }

        if (origDest == null) {
            // file with same name in CWD
            try {
                URI resolvedDestURI =
                        new URI("any:///" + dir + "/" + origSrc.getName());
                resolvedDest = GAT.createFile(gatContext, preferences,
                                resolvedDestURI);
            } catch (Exception e) {
                throw new GATInvocationException("poststagedFile", e);
            }
        } else {
            if (origDest.isAbsolute()) {
                resolvedDest = origDest;
            } else {
                // file with same name in CWD
                try {
                    URI resolvedDestURI =
                            new URI("any:///" + dir + "/" + origSrc.getPath());
                    resolvedDest =
                            GAT
                                .createFile(gatContext, preferences,
                                    resolvedDestURI);
                } catch (Exception e) {
                    throw new GATInvocationException("poststagedFile", e);
                }
            }
        }
    }

    protected void poststage() throws GATInvocationException {
        if (GATEngine.VERBOSE) {
            System.err.println("  copy " + resolvedSrc.toURI() + " to "
                + resolvedDest.toURI());
        }

        resolvedSrc.copy(resolvedDest.toURI());
    }

    protected void delete(boolean deleteFilesInSandbox)
            throws GATInvocationException {
        if (deleteFilesInSandbox || !inSandbox) {
            if (GATEngine.VERBOSE) {
                System.err.println("DELETE_FILE:" + resolvedSrc);
            }
            resolvedSrc.delete();
        }
    }

    protected void wipe(boolean onlySandbox) throws GATInvocationException {
        if (!onlySandbox || (onlySandbox && inSandbox)) {
            if (GATEngine.VERBOSE) {
                System.err.println("WIPE_FILE:" + resolvedSrc);
            }
            wipe(resolvedSrc);
        }
    }

    public String toString() {
        String srcURI =
                resolvedSrc == null ? "" : resolvedSrc.toURI().toString();
        String destURI =
                resolvedDest == null ? "" : resolvedDest.toURI().toString();

        return "PostStaged: " + srcURI + " -> " + destURI
            + (isStdout ? " (STDOUT)" : "") + (isStderr ? " (STDERR)" : "") + (inSandbox ? " (IN SANDBOX)" : " (OUTSIDE SANDBOX)");
    }
}
