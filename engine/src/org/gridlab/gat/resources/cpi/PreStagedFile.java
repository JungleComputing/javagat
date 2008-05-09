/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;

public class PreStagedFile extends StagedFile {

    protected static Logger logger = Logger.getLogger(PreStagedFile.class);

    private boolean isExecutable;

    private boolean isStdIn;

    private String exe;

    public PreStagedFile() {
        // constructor needed for castor marshalling, do *not* use
    }

    public PreStagedFile(GATContext context, File src, File dest, String host,
            String sandbox, boolean isStdIn, String exe)
            throws GATInvocationException {
        super(context, src, dest, host, sandbox);

        this.isStdIn = isStdIn;
        this.exe = exe;

        resolve();
    }

    /**
     * @return the exe
     */
    public String getExe() {
        return exe;
    }

    /**
     * @param exe
     *                the exe to set
     */
    public void setExe(String exe) {
        this.exe = exe;
    }

    /**
     * @return the isExecutable
     */
    public boolean isExecutable() {
        return isExecutable;
    }

    /**
     * @param isExecutable
     *                the isExecutable to set
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
     * @param isStdIn
     *                the isStdIn to set
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
            if (exe == null) {
                // can happen with java executables
                isExecutable = false;
                return;
            }

            if (exe.startsWith("/")) {
                // file is relative, exe is absolute
                isExecutable = false;
                return;
            }

            if (getResolvedSrc().isFile() && relativeURI.getPath().equals(exe)) {
                isExecutable = true;
            }
        } else {
            if (getResolvedSrc().isFile()
                    && getResolvedDest().getPath().equals(exe)) {
                isExecutable = true;
            }
        }
    }

    protected void prestage() throws GATInvocationException {
        if (logger.isInfoEnabled()) {
            logger.info("prestage:\n  copy " + getResolvedSrc().toGATURI()
                    + " to " + getResolvedDest().toGATURI());
        }
        getResolvedSrc().copy(getResolvedDest().toGATURI());
    }

    protected void delete() throws GATInvocationException {
        if (inSandbox) {
            return;
        }

        if (getResolvedDest().isDirectory()) {
            if (logger.isInfoEnabled()) {
                logger.info("DELETE_DIR:" + getResolvedDest());
            }
            FileCpi.recursiveDeleteDirectory(gatContext, getResolvedDest()
                    .getFileInterface());
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("DELETE_FILE:" + getResolvedDest());
            }
            getResolvedDest().delete();
        }
    }

    protected void wipe() throws GATInvocationException {
        if (logger.isInfoEnabled()) {
            logger.info("WIPE_FILE:" + getResolvedDest());
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
