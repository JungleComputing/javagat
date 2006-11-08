/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;

public class PreStagedFile extends StagedFile {
    boolean isExecutable;

    boolean isStdIn;

    URI exe;
    

    public PreStagedFile(GATContext context, Preferences preferences, File src,
        File dest, String host, String sandbox, boolean isStdIn,
        URI exe) throws GATInvocationException {
        super(context, preferences, src, dest, host, sandbox);

        this.isStdIn = isStdIn;
        this.exe = exe;
        
        resolve();
    }

    /* Creates a file object for the destination of the preStaged src file */
    private void resolve() throws GATInvocationException {
        resolvedSrc = origSrc;

        if (origDest != null) { // already set manually
            if (origDest.isAbsolute()) {
                inSandbox = false;
            } else {
                inSandbox = true;
            }
            resolvedDest = resolve(origDest, false);
        } else {
            inSandbox = true;
            resolvedDest = resolve(origSrc, true);
        }

        if(inSandbox) {
            if(exe.getPath().startsWith("/")) {
                // file is relative, exe is absolute
                isExecutable = false;
                return;
            }
        
            if(relativeURI.getPath().equals(exe.getPath())) {
                isExecutable = true;
            }
        } else {
            if(resolvedDest.getPath().equals(exe.getPath())) {
                isExecutable = true;
            }
        }
    }

    protected void prestage() throws GATInvocationException {
        if (GATEngine.VERBOSE) {
            System.err.println("prestage:");
            System.err.println("  copy " + resolvedSrc.toURI() + " to "
                + resolvedDest.toURI());
        }

        // create any directories if needed.
        if(resolvedSrc.isDirectory()) {
            // dest is also a dir, create it.
            if(GATEngine.VERBOSE) {
                System.err.println("creating dir: " + resolvedDest);
            }
            resolvedDest.mkdirs();
        } else {
            // src is a file, dest is also a file.
            File dir = resolvedDest.getParentFile();
            if(dir != null) {
                if(GATEngine.VERBOSE) {
                    System.err.println("creating dir: " + dir);
                }
                dir.mkdirs();
            }
        }
            
        resolvedSrc.copy(resolvedDest.toURI());
    }

    protected void delete(boolean onlySandbox) throws GATInvocationException {
        if (!onlySandbox || (onlySandbox && inSandbox)) {

            if (GATEngine.VERBOSE) {
                System.err.println("DELETE_FILE:" + resolvedDest);
            }
            resolvedDest.delete();
        }
    }

    protected void wipe(boolean onlySandbox) throws GATInvocationException {
        if (!onlySandbox || (onlySandbox && inSandbox)) {
            if (GATEngine.VERBOSE) {
                System.err.println("WIPE_FILE:" + resolvedDest);
            }
            wipe(resolvedDest);
        }
    }

    public String toString() {
        return "PreStaged: " + resolvedSrc.toURI() + " -> " + resolvedDest.toURI()
            + (isStdIn ? " (STDIN)" : "") + (isExecutable ? " (EXE)" : "") + (inSandbox ? " (IN SANDBOX)" : " (OUTSIDE SANDBOX)");
    }
}
