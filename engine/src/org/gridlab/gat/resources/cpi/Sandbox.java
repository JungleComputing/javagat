/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.FilePrestageException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInterface;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class Sandbox {

    protected static Logger logger = Logger.getLogger(Sandbox.class);

    private GATContext gatContext;

    private String authority;

    private String sandbox;

    private URI sandboxURI;

    private PreStagedFileSet pre;

    private PostStagedFileSet post;

    private PostStagedFileSet toWipe;

    private boolean wipePreStaged;

    private boolean wipePostStaged;

    private PostStagedFileSet toDelete;

    private boolean deletePreStaged;

    private boolean deletePostStaged;

    private String sandboxRoot;

    private boolean createSandboxDir, deleteSandboxDir;

    private long preStageTime, postStageTime, wipeTime, deleteTime;

    private String sandboxNameWithoutRoot;

    public Sandbox() {
        // constructor needed for castor marshalling, do *not* use
    }

    public Sandbox(GATContext gatContext, JobDescription jobDescription,
            String authority, String sandboxRoot, boolean createSandboxDir,
            boolean preStageStdin, boolean postStageStdout,
            boolean postStageStderr) throws GATInvocationException {
        this.gatContext = gatContext;
        this.authority = authority;

        this.createSandboxDir = createSandboxDir; // default value taken from
        // adaptor
        this.deleteSandboxDir = true; // default value
        this.sandboxRoot = sandboxRoot; // default value taken from adaptor

        // The user preference sandboxRoot overwrites the one specified by the
        // adaptor.
        String sandboxRootPref = null;
        String sandboxUseRootPref = null;
        String sandboxDeletePref = null;
        SoftwareDescription sd = jobDescription.getSoftwareDescription();
        if (sd != null) {
            Map<String, Object> attr = sd.getAttributes();
            if (attr != null) {
                sandboxRootPref = (String) attr.get("sandbox.root");
                sandboxUseRootPref = (String) attr.get("sandbox.useroot");
                sandboxDeletePref = (String) attr.get("sandbox.delete");
            }
        }

        if (sandboxRootPref != null) {
            this.sandboxRoot = sandboxRootPref;
            if (logger.isDebugEnabled()) {
                logger.debug("set sandboxRoot to " + sandboxRootPref);
            }
        }

        if (sandboxUseRootPref != null) {
            if (sandboxUseRootPref.equalsIgnoreCase("true")) {
                this.createSandboxDir = false;
                // change the default, if no sandbox is created, no sandbox will
                // be deleted, unless you overwrite it using the
                // 'sandbox.delete' preference.
                this.deleteSandboxDir = false;
            }
        }

        if (sandboxDeletePref != null) {
            if (sandboxDeletePref.equalsIgnoreCase("false")) {
                this.deleteSandboxDir = false;
            } else if (sandboxDeletePref.equalsIgnoreCase("true")) {
                this.deleteSandboxDir = true;
            }
        }

        initSandbox();

        pre = new PreStagedFileSet(gatContext, jobDescription, authority,
                sandbox, preStageStdin);
        post = new PostStagedFileSet(gatContext, jobDescription, authority,
                sandbox, postStageStdout, postStageStderr);

        wipePreStaged = sd.wipePreStaged();
        wipePostStaged = sd.wipePostStaged();
        toWipe = new PostStagedFileSet(gatContext, sd.getWipedFiles(),
                authority, sandbox);

        deletePreStaged = sd.deletePreStaged();
        deletePostStaged = sd.deletePostStaged();
        toDelete = new PostStagedFileSet(gatContext, sd.getDeletedFiles(),
                authority, sandbox);

        // createSandbox();
    }

    private void createSandboxDir(String authority)
            throws GATInvocationException {
        if (authority == null) {
            throw new FilePrestageException("Sandbox",
                    new GATInvocationException(
                            "cannot create a sandbox without a host name"));
        }

        for (int i = 0; i < 10; i++) {
            sandbox = getSandboxName();
            try {
                sandboxURI = new URI("any://" + authority + "/" + sandbox);
                if (logger.isDebugEnabled()) {
                    logger.debug("sandbox dir: " + sandboxURI);
                }
                FileInterface f = GAT.createFile(gatContext, sandboxURI)
                        .getFileInterface();
                if (f.mkdir()) {
                    return;
                }
            } catch (Exception e) {
                throw new GATInvocationException("Sandbox", e);
            }
        }

        throw new GATInvocationException("could not create a sandbox directory");
    }

    private void removeSandboxDir() throws GATInvocationException {
        URI location = null;
        try {
            location = new URI("any://" + authority + "/" + sandbox);
        } catch (URISyntaxException e) {
            throw new GATInvocationException("sandbox", e);
        }

        FileCpi.recursiveDeleteDirectory(gatContext, location);
    }

    private String getSandboxName() {
        if (logger.isInfoEnabled()) {
            logger.info("creating sandbox name, sandbox root = " + sandboxRoot);
        }
        String res = "";
        if (sandboxRoot != null) {
            res += new java.io.File(sandboxRoot).toURI().getPath() + "/";
        }
        sandboxNameWithoutRoot = ".JavaGAT_SANDBOX_" + Math.random();
        res += sandboxNameWithoutRoot;

        return res;
    }

    private void initSandbox() throws GATInvocationException {
        if (!createSandboxDir) {
            if (logger.isInfoEnabled()) {
                logger.info("sandbox: NO SANDBOX");
            }
            sandbox = sandboxRoot;
            return;
        }

        try {
            createSandboxDir(authority);
        } catch (Exception e) {
            throw new FilePrestageException("sandbox", e);
        }

        if (logger.isInfoEnabled()) {
            logger.info("sandbox: " + sandbox);
        }
    }

    /**
     * Creates a complete sandbox directory. This requires prestaging of the
     * requested files.
     */
    private void doPrestage() throws GATInvocationException {
        if (logger.isInfoEnabled()) {
            logger.info("pre stage starting");
        }
        long start = System.currentTimeMillis();
        try {
            pre.prestage();
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("prestage FAILED, cleaning up");
            }
            // remove / wipe files we already prestaged.
            retrieveAndCleanup(null);
            throw new FilePrestageException("Sandbox", e);
        } finally {
            preStageTime = System.currentTimeMillis() - start;
        }
        if (logger.isInfoEnabled()) {
            logger.info("pre stage done (SUCCESS)");
        }
    }

    // we always wipe everything, also files that are in the sandbox
    private void wipe() throws GATInvocationException {
        GATInvocationException wipeException = new GATInvocationException();

        if (logger.isInfoEnabled()) {
            logger.info("wipe starting");
        }
        long start = System.currentTimeMillis();

        try {
            if (wipePreStaged) {
                pre.wipe();
            }
        } catch (GATInvocationException e) {
            wipeException.add("sandbox", e);
        }

        try {
            if (wipePostStaged) {
                post.wipe();
            }
        } catch (GATInvocationException e) {
            wipeException.add("sandbox", e);
        }

        try {
            toWipe.wipe();
        } catch (GATInvocationException e) {
            wipeException.add("sandbox", e);
        }

        if (logger.isInfoEnabled()) {
            logger.info("wipe done "
                    + (wipeException.getNrChildren() == 0 ? "(SUCCESS)"
                            : "(FAILURE)"));
        }

        wipeTime = System.currentTimeMillis() - start;

        if (wipeException.getNrChildren() != 0) {
            throw wipeException;
        }
    }

    // there is no need to delete files inside the sandbox, they will be
    // deleted when the sandbox is cleaned up
    private void delete() throws GATInvocationException {
        GATInvocationException deleteException = new GATInvocationException();

        if (logger.isInfoEnabled()) {
            logger.info("delete starting");
        }

        long start = System.currentTimeMillis();

        if (deletePreStaged) {
            try {
                pre.delete();
            } catch (GATInvocationException e) {
                deleteException.add("delete", e);
            }
        }

        if (deletePostStaged) {
            try {
                post.delete();
            } catch (GATInvocationException e) {
                deleteException.add("delete", e);
            }
        }

        try {
            toDelete.delete();
        } catch (GATInvocationException e) {
            deleteException.add("delete", e);
        }

        if (logger.isInfoEnabled()) {
            logger.info("delete done "
                    + (deleteException.getNrChildren() == 0 ? "(SUCCESS)"
                            : "(FAILURE)"));
        }

        deleteTime = System.currentTimeMillis() - start;

        if (deleteException.getNrChildren() != 0) {
            throw deleteException;
        }
    }

    public void prestage() throws GATInvocationException {
        doPrestage();
    }

    public void retrieveAndCleanup(JobCpi j) {
        GATInvocationException poststageException = null;
        GATInvocationException wipeException = null;
        GATInvocationException deleteException = null;
        GATInvocationException removeSandboxException = null;

        if (logger.isInfoEnabled()) {
            logger.info("post stage starting");
        }

        long start = System.currentTimeMillis();

        try {
            post.poststage();
        } catch (GATInvocationException e) {
            poststageException = e;
        }
        if (logger.isInfoEnabled()) {
            logger.info("post stage done "
                    + (poststageException == null ? "(SUCCESS)" : "(FAILURE)"));
            logger.info("poststage exception: " + poststageException);
        }

        postStageTime = System.currentTimeMillis() - start;

        try {
            wipe();
        } catch (GATInvocationException e) {
            wipeException = e;
        }

        try {
            delete();
        } catch (GATInvocationException e) {
            deleteException = e;
        }

        if (deleteSandboxDir) {
            if (logger.isInfoEnabled()) {
                logger.info("removing sandbox dir: " + "any://" + authority
                        + "/" + sandbox);
            }
            try {
                removeSandboxDir();
            } catch (GATInvocationException e) {
                removeSandboxException = e;
            }
            if (logger.isInfoEnabled()) {
                logger.info("removing sandbox dir done "
                        + (removeSandboxException == null ? "(SUCCESS)"
                                : "(FAILURE)"));
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("sandbox persists at: " + "any://" + authority
                        + "/" + sandbox);
            }
        }

        if (j != null) {
            j.deleteException = deleteException;
            j.wipeException = wipeException;
            j.postStageException = poststageException;
            j.removeSandboxException = removeSandboxException;
        }
    }

    public String getSandbox() {
        return sandboxNameWithoutRoot;
    }

    public String getSandboxPath() {
        return sandbox;
    }

    public URI getSandboxURI() {
        return sandboxURI;
    }

    public File getResolvedExecutable() {
        PreStagedFile f = pre.getExecutable();
        if (f == null)
            return null;
        return f.getResolvedDest();
    }

    public File getResolvedStdin() {
        PreStagedFile f = pre.getStdin();
        if (f == null)
            return null;
        return f.getResolvedDest();
    }

    public File getResolvedStdout() {
        PostStagedFile f = post.getStdout();
        if (f == null)
            return null;
        return f.getResolvedSrc();
    }

    public File getResolvedStderr() {
        PostStagedFile f = post.getStderr();
        if (f == null)
            return null;
        return f.getResolvedSrc();
    }

    /**
     * returns the URI relative to the sandbox, or an absolute path if it was
     * absolute.
     */
    public URI getRelativeStdin() {
        PreStagedFile f = pre.getStdin();
        if (f == null)
            return null;
        return f.relativeURI;
    }

    /**
     * returns the URI relative to the sandbox, or an absolute path if it was
     * absolute.
     */
    public URI getRelativeStdout() {
        PostStagedFile f = post.getStdout();
        if (f == null)
            return null;
        return f.relativeURI;
    }

    /**
     * returns the URI relative to the sandbox, or an absolute path if it was
     * absolute.
     */
    public URI getRelativeStderr() {
        PostStagedFile f = post.getStderr();
        if (f == null)
            return null;
        return f.relativeURI;
    }

    public String getAuthority() {
        return authority;
    }

    public PreStagedFileSet getPrestagedFileSet() {
        return pre;
    }

    public PostStagedFileSet getPostStagedFileSet() {
        return post;
    }

    public long getDeleteTime() {
        return deleteTime;
    }

    public long getPostStageTime() {
        return postStageTime;
    }

    public long getPreStageTime() {
        return preStageTime;
    }

    public long getWipeTime() {
        return wipeTime;
    }

    /**
     * @return the createSandboxDir
     */
    public boolean isCreateSandboxDir() {
        return createSandboxDir;
    }

    /**
     * @param createSandboxDir
     *                the createSandboxDir to set
     */
    public void setCreateSandboxDir(boolean createSandboxDir) {
        this.createSandboxDir = createSandboxDir;
    }

    /**
     * @return the post
     */
    public PostStagedFileSet getPost() {
        return post;
    }

    /**
     * @param post
     *                the post to set
     */
    public void setPost(PostStagedFileSet post) {
        this.post = post;
    }

    /**
     * @return the pre
     */
    public PreStagedFileSet getPre() {
        return pre;
    }

    /**
     * @param pre
     *                the pre to set
     */
    public void setPre(PreStagedFileSet pre) {
        this.pre = pre;
    }

    /**
     * @return the sandboxRoot
     */
    public String getSandboxRoot() {
        return sandboxRoot;
    }

    /**
     * @param sandboxRoot
     *                the sandboxRoot to set
     */
    public void setSandboxRoot(String sandboxRoot) {
        this.sandboxRoot = sandboxRoot;
    }

    /**
     * @param deleteTime
     *                the deleteTime to set
     */
    public void setDeleteTime(long deleteTime) {
        this.deleteTime = deleteTime;
    }

    /**
     * @param authority
     *                the authority to set (authority part of a URI, hostname +
     *                some extra information)
     */
    public void setAuthority(String authority) {
        this.authority = authority;
    }

    /**
     * @param postStageTime
     *                the postStageTime to set
     */
    public void setPostStageTime(long postStageTime) {
        this.postStageTime = postStageTime;
    }

    /**
     * @param preStageTime
     *                the preStageTime to set
     */
    public void setPreStageTime(long preStageTime) {
        this.preStageTime = preStageTime;
    }

    /**
     * @param sandbox
     *                the sandbox to set
     */
    public void setSandbox(String sandbox) {
        this.sandbox = sandbox;
    }

    /**
     * @param wipeTime
     *                the wipeTime to set
     */
    public void setWipeTime(long wipeTime) {
        this.wipeTime = wipeTime;
    }

    /**
     * @return the deletePostStaged
     */
    public boolean isDeletePostStaged() {
        return deletePostStaged;
    }

    /**
     * @param deletePostStaged
     *                the deletePostStaged to set
     */
    public void setDeletePostStaged(boolean deletePostStaged) {
        this.deletePostStaged = deletePostStaged;
    }

    /**
     * @return the deletePreStaged
     */
    public boolean isDeletePreStaged() {
        return deletePreStaged;
    }

    /**
     * @param deletePreStaged
     *                the deletePreStaged to set
     */
    public void setDeletePreStaged(boolean deletePreStaged) {
        this.deletePreStaged = deletePreStaged;
    }

    /**
     * @return the toDelete
     */
    public PostStagedFileSet getToDelete() {
        return toDelete;
    }

    /**
     * @param toDelete
     *                the toDelete to set
     */
    public void setToDelete(PostStagedFileSet toDelete) {
        this.toDelete = toDelete;
    }

    /**
     * @return the toWipe
     */
    public PostStagedFileSet getToWipe() {
        return toWipe;
    }

    /**
     * @param toWipe
     *                the toWipe to set
     */
    public void setToWipe(PostStagedFileSet toWipe) {
        this.toWipe = toWipe;
    }

    /**
     * @return the wipePostStaged
     */
    public boolean isWipePostStaged() {
        return wipePostStaged;
    }

    /**
     * @param wipePostStaged
     *                the wipePostStaged to set
     */
    public void setWipePostStaged(boolean wipePostStaged) {
        this.wipePostStaged = wipePostStaged;
    }

    /**
     * @return the wipePreStaged
     */
    public boolean isWipePreStaged() {
        return wipePreStaged;
    }

    /**
     * @param wipePreStaged
     *                the wipePreStaged to set
     */
    public void setWipePreStaged(boolean wipePreStaged) {
        this.wipePreStaged = wipePreStaged;
    }
}
