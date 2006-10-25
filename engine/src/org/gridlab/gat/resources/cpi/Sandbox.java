/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import java.util.Map;

import org.gridlab.gat.FilePrestageException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
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
        SoftwareDescription sd = jobDescription.getSoftwareDescription();
        if(sd != null) {
            Map attr = sd.getAttributes();
            if(attr != null) {
                sandboxRootPref = (String) attr.get("sandboxRoot");
            }
        }
        if(sandboxRootPref != null) {
            this.sandboxRoot = sandboxRootPref;
        } else {
            this.sandboxRoot = sandboxRoot;
        }
        this.createSandboxDir = createSandboxDir;

        initSandbox();

        pre = new PreStagedFileSet(gatContext, preferences, jobDescription,
            host, sandbox, preStageStdin);
        post = new PostStagedFileSet(gatContext, preferences, jobDescription,
            host, sandbox, postStageStdout, postStageStderr);
        createSandbox();
    }

    private void createSandboxDir(String host) throws GATInvocationException {
        for (int i = 0; i < 10; i++) {
            sandbox = getSandboxName();

            try {
                URI location = new URI("any://" + host + "/" + sandbox);
                File f = GAT.createFile(gatContext, location);
                if (f.mkdir()) {
                    return;
                }
            } catch (Exception e) {
                throw new GATInvocationException("resource broker", e);
            }
        }

        throw new GATInvocationException("could not create a sandbox");
    }

    private void removeSandboxDir(String host, String sandbox)
        throws GATInvocationException {
        try {
            URI location = new URI("any://" + host + "/" + sandbox);
            File f = GAT.createFile(gatContext, location);
            if (f.delete()) {
                if (GATEngine.VERBOSE) {
                    System.err.println("deleted sandbox dir");
                }

                return;
            }
        } catch (Exception e) {
            throw new GATInvocationException("resource broker", e);
        }

        throw new GATInvocationException("could not create a sandbox");
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
        if(!createSandboxDir) {
            if (GATEngine.VERBOSE) {
                System.err.println("sandbox: NO SANDBOX");
            }
            sandbox = sandboxRoot;
            return;
        }
        
        if (host == null) {
            throw new GATInvocationException(
                "cannot create a sandbox without a host name");
        }

        try {
            createSandboxDir(host);
        } catch (Exception e) {
            throw new FilePrestageException("resource broker", e);
        }

        if (GATEngine.VERBOSE) {
            System.err.println("sandbox: " + sandbox);
        }
    }

    /** Creates a complete sandbox directory. This requires prestaging of the requested files. 
     */
    private void createSandbox() throws GATInvocationException {
        try {
            post.delete(false); // only delete files that aren't going in the sandbox
        } catch (GATInvocationException e) {
            // ignore, maybe the files did not exist anyway
        }

        try {
            pre.prestage();
        } catch (Exception e) {
            if (GATEngine.VERBOSE) {
                System.err.println("prestage failed, cleaning up");
            }
            // remove / wipe files we already prestaged.
            retrieveAndCleanup(null);
            throw new FilePrestageException("resource broker", e);
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
            System.err.println("delete/wipe starting");
        }

        SoftwareDescription sd = jobDescription.getSoftwareDescription();

        try {
            if (sd.wipePreStaged()) {
                pre.wipe(false);
            }
            if (sd.wipePostStaged()) {
                post.wipe(false);
            }

            PostStagedFileSet del = new PostStagedFileSet(gatContext,
                preferences, sd.getWipedFiles(), host, sandbox);
            del.wipe(false);
        } catch (GATInvocationException e) {
            wipeException = e;
        }

        try {
            pre.delete(!sd.deletePreStaged());
            post.delete(!sd.deletePostStaged());
            PostStagedFileSet del = new PostStagedFileSet(gatContext,
                preferences, sd.getDeletedFiles(), host, sandbox);
            del.delete(false);
        } catch (GATInvocationException e) {
            deleteException = e;
        }

        try {
            removeSandboxDir(host, sandbox);
        } catch (GATInvocationException e) {
            removeSandboxException = e;
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
        if (f == null) return null;
        return f.resolvedDest;
    }
    
    public File getResolvedStdin() {
        PreStagedFile f = pre.getStdin();
        if (f == null) return null;
        return f.resolvedDest;
    }

    public File getResolvedStdout() {
        PostStagedFile f = post.getStdout();
        if (f == null) return null;
        return f.resolvedSrc;
    }

    public File getResolvedStderr() {
        PostStagedFile f = post.getStderr();
        if (f == null) return null;
        return f.resolvedSrc;
    }

    /** returns the URI relative to the sandbox, or an absolute path if it was absolute. */
    public URI getRelativeStdin() {
        PreStagedFile f = pre.getStdin();
        if (f == null) return null;
        return f.relativeURI;
    }

    /** returns the URI relative to the sandbox, or an absolute path if it was absolute. */
    public URI getRelativeStdout() {
        PostStagedFile f = post.getStdout();
        if (f == null) return null;
        return f.relativeURI;
    }

    /** returns the URI relative to the sandbox, or an absolute path if it was absolute. */
    public URI getRelativeStderr() {
        PostStagedFile f = post.getStderr();
        if (f == null) return null;
        return f.relativeURI;
    }

    public String getHost() {
        return host;
    }
}
