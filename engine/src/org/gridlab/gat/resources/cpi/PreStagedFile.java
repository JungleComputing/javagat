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
import org.gridlab.gat.io.cpi.FileCpi;

public class PreStagedFile extends StagedFile {
    private boolean isExecutable;

    private boolean isStdIn;

    private URI exe;


    public PreStagedFile() {
        // constructor needed for castor marshalling, do *not* use
    }

    public PreStagedFile(GATContext context, Preferences preferences, File src,
            File dest, String host, String sandbox, boolean isStdIn, URI exe)
            throws GATInvocationException {
        super(context, preferences, src, dest, host, sandbox);

        this.isStdIn = isStdIn;
        this.exe = exe;

        resolve();
    }

    /**
     * @return the exe
     */
    public URI getExe() {
        return exe;
    }

    /**
     * @param exe the exe to set
     */
    public void setExe(URI exe) {
        this.exe = exe;
    }

    /**
     * @return the isExecutable
     */
    public boolean isExecutable() {
        return isExecutable;
    }

    /**
     * @param isExecutable the isExecutable to set
     */
    public void setExecutable(boolean isExecutable) {
        this.isExecutable = isExecutable;
    }

    /**
     * @return the isStdIn
     */
    public boolean isStdIn() {
        return isStdIn;
    }

    /**
     * @param isStdIn the isStdIn to set
     */
    public void setStdIn(boolean isStdIn) {
        this.isStdIn = isStdIn;
    }

    /* Creates a file object for the destination of the preStaged src file */
    private void resolve() throws GATInvocationException {
        setResolvedSrc(origSrc);

        if (origDest != null) { // already set manually
            if (origDest.isAbsolute()) {
                inSandbox = false;
            } else {
                inSandbox = true;
            }
            setResolvedDest(resolve(origDest, false));
        } else {
            inSandbox = true;
            setResolvedDest(resolve(origSrc, true));
        }

        if (inSandbox) {
            if (exe.getPath() == null) {
                // can happen with java executables
                isExecutable = false;
                return;
            }

            if (exe.getPath().startsWith("/")) {
                // file is relative, exe is absolute
                isExecutable = false;
                return;
            }

            if (getResolvedSrc().isFile() && relativeURI.getPath().equals(exe.getPath())) {
                isExecutable = true;
            }
        } else {
            if (getResolvedSrc().isFile() && getResolvedDest().getPath().equals(exe.getPath())) {
                isExecutable = true;
            }
        }
    }

    protected void prestage() throws GATInvocationException {
        if (GATEngine.VERBOSE) {
            System.err.println("prestage:");
            System.err.println("  copy " + getResolvedSrc().toGATURI() + " to "
                    + getResolvedDest().toGATURI());
        }

        // create any directories if needed.
        if (getResolvedSrc().isDirectory()) {
            // dest is also a dir, create it.
            if (GATEngine.VERBOSE) {
                System.err.println("creating dir: " + getResolvedDest());
            }
            getResolvedDest().mkdirs();
        } else {
            // src is a file, dest is also a file.
            File dir = (File) getResolvedDest().getParentFile();
            if (dir != null) {
                if (GATEngine.VERBOSE) {
                    System.err.println("creating dir: " + dir);
                }
                dir.mkdirs();
            }
        }

        getResolvedSrc().copy(getResolvedDest().toGATURI());
    }

    protected void delete() throws GATInvocationException {
        if (inSandbox) {
            return;
        }
        
        if (getResolvedDest().isDirectory()) {
            if (GATEngine.VERBOSE) {
                System.err.println("DELETE_DIR:" + getResolvedDest());
            }
            FileCpi.recursiveDeleteDirectory(gatContext, preferences,
                    getResolvedDest());
        } else {
            if (GATEngine.VERBOSE) {
                System.err.println("DELETE_FILE:" + getResolvedDest());
            }
            getResolvedDest().delete();
        }
    }

    protected void wipe() throws GATInvocationException {
        if (GATEngine.VERBOSE) {
            System.err.println("WIPE_FILE:" + getResolvedDest());
        }
        wipe(getResolvedDest());
    }

    public String toString() {
        return "PreStaged: " + getResolvedSrc().toGATURI() + " -> "
                + getResolvedDest().toGATURI() + (isStdIn ? " (STDIN)" : "")
                + (isExecutable ? " (EXE)" : "")
                + (inSandbox ? " (IN SANDBOX)" : " (OUTSIDE SANDBOX)");
    }
}
