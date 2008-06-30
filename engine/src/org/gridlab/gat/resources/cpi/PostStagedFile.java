/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

public class PostStagedFile extends StagedFile {

    protected static Logger logger = Logger.getLogger(PostStagedFile.class);

    private boolean isStdout;

    private boolean isStderr;

    public PostStagedFile() {
        // constructor needed for castor marshalling, do *not* use
    }

    public PostStagedFile(GATContext context, File origSrc, File origDest,
            String host, String sandbox, boolean isStdOut, boolean isStderr)
            throws GATInvocationException {
        super(context, origSrc, origDest, host, sandbox);
        this.isStdout = isStdOut;
        this.isStderr = isStderr;
        this.inSandbox = inSandbox(origSrc.getPath());
        resolve();
    }

    /*
     * Creates a file object for the destination of the postStaged file. Src
     * cannot be null, dest can be.
     */
    private void resolve() throws GATInvocationException {
        setResolvedSrc(resolve(origSrc, false));

        String cwd = new java.io.File(System.getProperty("user.dir")).toURI()
                .getPath();
        if (cwd == null) {
            throw new GATInvocationException(
                    "cannot get current working directory");
        }

        if (origDest == null) {
            // file with same name in CWD
            try {
                URI resolvedDestURI = new URI("any:///" + cwd
                        + origSrc.getName());
                setResolvedDest(GAT.createFile(gatContext, resolvedDestURI));
            } catch (Exception e) {
                throw new GATInvocationException("poststagedFile", e);
            }
        } else {
            if (origDest.isAbsolute()) {
                setResolvedDest(origDest);
            } else {
                // TODO: check for post staging to other machine

                // file with same name in CWD
                try {
                    String destURIString = "any://";
                    if (origDest.toGATURI().getHost() == null) {
                        destURIString += "/" + cwd;
                    } else {
                        destURIString += origDest.toGATURI().getHost() + "/";
                    }

                    destURIString += origDest.getPath();
                    setResolvedDest(GAT.createFile(origSrc.getFileInterface()
                            .getGATContext(), new URI(destURIString)));
                } catch (Exception e) {
                    throw new GATInvocationException("poststagedFile", e);
                }
            }
        }
    }

    protected void poststage() throws GATInvocationException {
        if (logger.isInfoEnabled()) {
            logger.info("  exists: " + getResolvedSrc().exists());
            logger.info("  copy " + getResolvedSrc().toGATURI() + " to "
                    + getResolvedDest().toGATURI());
        }
        getResolvedSrc().copy(getResolvedDest().toGATURI());
    }

    protected void delete() throws GATInvocationException {
        if (inSandbox) {
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("DELETE_FILE: " + getResolvedSrc());
        }
        getResolvedSrc().delete();
    }

    protected void wipe() throws GATInvocationException {
        if (logger.isInfoEnabled()) {
            logger.info("WIPE_FILE:" + getResolvedSrc());
        }
        wipe(getResolvedSrc());
    }

    public String toString() {
        String srcURI = getResolvedSrc() == null ? "" : getResolvedSrc()
                .toGATURI().toString();
        String destURI = getResolvedDest() == null ? "" : getResolvedDest()
                .toGATURI().toString();

        return "PostStaged: " + srcURI + " -> " + destURI
                + (isStdout ? " (STDOUT)" : "") + (isStderr ? " (STDERR)" : "")
                + (inSandbox ? " (IN SANDBOX)" : " (OUTSIDE SANDBOX)");
    }

    /**
     * @return the isStderr
     */
    public boolean isStderr() {
        return isStderr;
    }

    /**
     * @param isStderr
     *                the isStderr to set
     */
    public void setStderr(boolean isStderr) {
        this.isStderr = isStderr;
    }

    /**
     * @return the isStdout
     */
    public boolean isStdout() {
        return isStdout;
    }

    /**
     * @param isStdout
     *                the isStdout to set
     */
    public void setStdout(boolean isStdout) {
        this.isStdout = isStdout;
    }
}
