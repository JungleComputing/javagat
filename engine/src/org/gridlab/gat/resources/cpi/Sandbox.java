/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

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

    PrestagedFileSet pre;

    PoststagedFileSet post;

    String sandboxRoot;

    public Sandbox(GATContext gatContext, Preferences preferences,
        JobDescription jobDescription, String host, String sandboxRoot,
        boolean preStageStdin, boolean postStageStdout, boolean postStageStderr)
        throws GATInvocationException {
        this.jobDescription = jobDescription;
        this.gatContext = gatContext;
        this.preferences = preferences;
        this.host = host;
        this.sandboxRoot = sandboxRoot;

        initSandbox();

        pre = new PrestagedFileSet(gatContext, preferences, jobDescription,
            host, sandbox, preStageStdin);
        post = new PoststagedFileSet(gatContext, preferences, jobDescription,
            host, sandbox, postStageStdout, postStageStderr);
        createSandbox();
    }

    private void createSandboxDir(String host) throws GATInvocationException {
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
            System.out.println("sandbox: " + sandbox);
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

            PoststagedFileSet del = new PoststagedFileSet(gatContext,
                preferences, sd.getWipedFiles(), host, sandbox);
            del.wipe(false);
        } catch (GATInvocationException e) {
            wipeException = e;
        }

        try {
            pre.delete(!sd.deletePreStaged());
            post.delete(!sd.deletePostStaged());
            PoststagedFileSet del = new PoststagedFileSet(gatContext,
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

    public File getResolvedStdin() {
        PrestagedFile f = pre.getStdin();
        if (f == null) return null;
        return f.resolvedDest;
    }

    public File getResolvedStdout() {
        PoststagedFile f = post.getStdout();
        if(f == null) return null;
        return f.resolvedSrc;
    }

    public File getResolvedStderr() {
        PoststagedFile f = post.getStderr();
        if(f == null) return null;
        return f.resolvedSrc;
    }

    /** returns the URI relative to the sandbox, or an absolute path if it was absolute. */
    public URI getRelativeStdin() {
        PrestagedFile f = pre.getStdin();
        if(f == null) return null;
        return f.relativeURI;
    }

    /** returns the URI relative to the sandbox, or an absolute path if it was absolute. */
    public URI getRelativeStdout() {
        PoststagedFile f = post.getStdout();
        if(f == null) return null;
        return f.relativeURI;
    }
    
    /** returns the URI relative to the sandbox, or an absolute path if it was absolute. */
    public URI getRelativeStderr() {
        PoststagedFile f = post.getStderr();
        if(f == null) return null;
        return f.relativeURI;
    }

    public String getHost() {
        return host;
    }
}
