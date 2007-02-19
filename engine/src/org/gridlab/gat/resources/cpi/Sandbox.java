/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import java.net.URISyntaxException;
import java.util.Map;

import org.gridlab.gat.FilePrestageException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class Sandbox {
    GATContext gatContext;

    Preferences preferences;

    JobDescription jobDescription;

    String host;

    String sandbox;

    PreStagedFileSet pre;

    PostStagedFileSet post;

    String sandboxRoot;

    boolean createSandboxDir;

    public Sandbox(GATContext gatContext, Preferences preferences,
            JobDescription jobDescription, String host, String sandboxRoot,
            boolean createSandboxDir, boolean preStageStdin,
            boolean postStageStdout, boolean postStageStderr)
            throws GATInvocationException {
        this.jobDescription = jobDescription;
        this.gatContext = gatContext;
        this.preferences = preferences;
        this.host = host;

        // The user preference sandboxRoot overwrites the one specified by the adaptor.
        String sandboxRootPref = null;
        String sandboxDisabledPref = null;
        SoftwareDescription sd = jobDescription.getSoftwareDescription();
        if (sd != null) {
            Map attr = sd.getAttributes();
            if (attr != null) {
                sandboxRootPref = (String) attr.get("sandboxRoot");
                sandboxDisabledPref = (String) attr.get("disableSandbox");
            }
        }
        if (sandboxRootPref != null) {
            this.sandboxRoot = sandboxRootPref;
            if (GATEngine.DEBUG) {
                System.err.println("set sandboxRoot to " + sandboxRootPref);
            }
        } else {
            this.sandboxRoot = sandboxRoot;
        }

        if (sandboxDisabledPref != null) {
            if (sandboxDisabledPref.equalsIgnoreCase("true")) {
                this.createSandboxDir = false;
            } else {
                this.createSandboxDir = createSandboxDir;
            }
        } else {
            this.createSandboxDir = createSandboxDir;
        }

        initSandbox();

        pre =
                new PreStagedFileSet(gatContext, preferences, jobDescription,
                        host, sandbox, preStageStdin);
        post =
                new PostStagedFileSet(gatContext, preferences, jobDescription,
                        host, sandbox, postStageStdout, postStageStderr);

        createSandbox();
    }

    private void createSandboxDir(String host) throws GATInvocationException {

        if (host == null) {
            throw new FilePrestageException("sandbox",
                    new GATInvocationException(
                            "cannot create a sandbox without a host name"));
        }

        for (int i = 0; i < 10; i++) {
            sandbox = getSandboxName();

            try {
                URI location = new URI("any://" + host + "/" + sandbox);
                File f = GAT.createFile(gatContext, location);
                if (f.mkdir()) {
                    return;
                }
            } catch (Exception e) {
                throw new GATInvocationException("sandbox", e);
            }
        }

        throw new GATInvocationException("could not create a sandbox");
    }

    private void removeSandboxDir() throws GATInvocationException {
        URI location = null;
        try {
            location = new URI("any://" + host + "/" + sandbox);
        } catch (URISyntaxException e) {
            throw new GATInvocationException("sandbox", e);
        }

        FileCpi.recursiveDeleteDirectory(gatContext, preferences, location);
    }

    private String getSandboxName() {
        String res = "";
        if (sandboxRoot != null) {
            res += sandboxRoot + "/";
        }
        res += ".JavaGAT_SANDBOX_" + Math.random();
        return res;
    }

    private void initSandbox() throws GATInvocationException {
        if (!createSandboxDir) {
            if (GATEngine.VERBOSE) {
                System.err.println("sandbox: NO SANDBOX");
            }
            sandbox = sandboxRoot;
            return;
        }

        try {
            createSandboxDir(host);
        } catch (Exception e) {
            throw new FilePrestageException("sandbox", e);
        }

        if (GATEngine.VERBOSE) {
            System.err.println("sandbox: " + sandbox);
        }
    }

    /** Creates a complete sandbox directory. This requires prestaging of the requested files. 
     */
    private void createSandbox() throws GATInvocationException {
        if (GATEngine.VERBOSE) {
            System.err.println("deleting post stage files outside sandbox");
        }
        try {
            post.delete(); // only delete files that aren't going in the sandbox
        } catch (Exception e) {
            if (GATEngine.VERBOSE) {
                System.err.println("warning, delete poststage failed: " + e);
            }
            // ignore, maybe the files did not exist anyway
        }
        if (GATEngine.VERBOSE) {
            System.err
                    .println("deleting post stage files outside sandbox done");
        }

        if (GATEngine.VERBOSE) {
            System.err.println("pre stage starting");
        }
        try {
            pre.prestage();
        } catch (Exception e) {
            if (GATEngine.VERBOSE) {
                System.err.println("prestage FAILED, cleaning up");
            }
            // remove / wipe files we already prestaged.
            retrieveAndCleanup(null);
            throw new FilePrestageException("sandbox", e);
        }
        if (GATEngine.VERBOSE) {
            System.err.println("pre stage done (SUCCESS)");
        }
    }

    // we always wipe everything, also files that are in the sandbox
    private void wipe() throws GATInvocationException {
        SoftwareDescription sd = jobDescription.getSoftwareDescription();
        GATInvocationException wipeException = new GATInvocationException();

        if (GATEngine.VERBOSE) {
            System.err.println("wipe starting");
        }

        try {
            if (sd.wipePreStaged()) {
                pre.wipe();
            }
        } catch (GATInvocationException e) {
            wipeException.add("sandbox", e);
        }

        try {
            if (sd.wipePostStaged()) {
                post.wipe();
            }
        } catch (GATInvocationException e) {
            wipeException.add("sandbox", e);
        }

        try {
            PostStagedFileSet toWipe =
                    new PostStagedFileSet(gatContext, preferences, sd
                            .getWipedFiles(), host, sandbox);
            toWipe.wipe();
        } catch (GATInvocationException e) {
            wipeException.add("sandbox", e);
        }

        if (GATEngine.VERBOSE) {
            System.err.println("wipe done "
                    + (wipeException.getNrChildren() == 0 ? "(SUCCESS)"
                            : "(FAILURE)"));
        }

        if (wipeException.getNrChildren() != 0) {
            throw wipeException;
        }
    }

    // there is no need to delete files inside the sandbox, they will be
    // deleted when the sandbox is cleaned up
    private void delete() throws GATInvocationException {
        SoftwareDescription sd = jobDescription.getSoftwareDescription();
        GATInvocationException deleteException = new GATInvocationException();

        if (GATEngine.VERBOSE) {
            System.err.println("delete starting");
        }

        if (sd.deletePreStaged()) {
            try {
                pre.delete();
            } catch (GATInvocationException e) {
                deleteException.add("delete", e);
            }
        }

        if (sd.deletePostStaged()) {
            try {
                post.delete();
            } catch (GATInvocationException e) {
                deleteException.add("delete", e);
            }
        }

        try {
            PostStagedFileSet del =
                    new PostStagedFileSet(gatContext, preferences, sd
                            .getDeletedFiles(), host, sandbox);
            del.delete();
        } catch (GATInvocationException e) {
            deleteException.add("delete", e);
        }

        if (GATEngine.VERBOSE) {
            System.err.println("delete done "
                    + (deleteException.getNrChildren() == 0 ? "(SUCCESS)"
                            : "(FAILURE)"));
        }

        if (deleteException.getNrChildren() != 0) {
            throw deleteException;
        }
    }

    public void retrieveAndCleanup(JobCpi j) {
        GATInvocationException poststageException = null;
        GATInvocationException wipeException = null;
        GATInvocationException deleteException = null;
        GATInvocationException removeSandboxException = null;

        if (GATEngine.VERBOSE) {
            System.err.println("post stage starting");
        }
        try {
            post.poststage();
        } catch (GATInvocationException e) {
            poststageException = e;
        }
        if (GATEngine.VERBOSE) {
            System.err.println("post stage done "
                    + (poststageException == null ? "(SUCCESS)" : "(FAILURE)"));
        }

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

        if (GATEngine.VERBOSE) {
            System.err.println("removing sandbox dir");
        }
        try {
            removeSandboxDir();
        } catch (GATInvocationException e) {
            removeSandboxException = e;
        }
        if (GATEngine.VERBOSE) {
            System.err.println("removing sandbox dir done "
                    + (removeSandboxException == null ? "(SUCCESS)"
                            : "(FAILURE)"));
        }

        if (j != null) {
            j.deleteException = deleteException;
            j.wipeException = wipeException;
            j.postStageException = poststageException;
            j.removeSandboxException = removeSandboxException;
        }
    }

    public String getSandbox() {
        return sandbox;
    }

    public File getResolvedExecutable() {
        PreStagedFile f = pre.getExecutable();
        if (f == null)
            return null;
        return f.resolvedDest;
    }

    public File getResolvedStdin() {
        PreStagedFile f = pre.getStdin();
        if (f == null)
            return null;
        return f.resolvedDest;
    }

    public File getResolvedStdout() {
        PostStagedFile f = post.getStdout();
        if (f == null)
            return null;
        return f.resolvedSrc;
    }

    public File getResolvedStderr() {
        PostStagedFile f = post.getStderr();
        if (f == null)
            return null;
        return f.resolvedSrc;
    }

    /** returns the URI relative to the sandbox, or an absolute path if it was absolute. */
    public URI getRelativeStdin() {
        PreStagedFile f = pre.getStdin();
        if (f == null)
            return null;
        return f.relativeURI;
    }

    /** returns the URI relative to the sandbox, or an absolute path if it was absolute. */
    public URI getRelativeStdout() {
        PostStagedFile f = post.getStdout();
        if (f == null)
            return null;
        return f.relativeURI;
    }

    /** returns the URI relative to the sandbox, or an absolute path if it was absolute. */
    public URI getRelativeStderr() {
        PostStagedFile f = post.getStderr();
        if (f == null)
            return null;
        return f.relativeURI;
    }

    public String getHost() {
        return host;
    }

    public PreStagedFileSet getPrestagedFileSet() {
        return pre;
    }

    public PostStagedFileSet getPostStagedFileSet() {
        return post;
    }
}
