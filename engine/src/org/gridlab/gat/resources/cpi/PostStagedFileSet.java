/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class PostStagedFileSet {

    protected static Logger logger = Logger.getLogger(PostStagedFileSet.class);

    private GATContext gatContext;

    private JobDescription description;

    private String host;

    private String sandbox;

    private boolean postStageStdout;

    private boolean postStageStderr;

    private PostStagedFile[] files = new PostStagedFile[0];

    public PostStagedFileSet() {
        // constructor needed for castor marshalling, do *not* use
    }

    public PostStagedFileSet(GATContext gatContext, JobDescription description,
            String host, String sandbox, boolean postStageStdout,
            boolean postStageStderr) throws GATInvocationException {
        this.gatContext = gatContext;
        this.description = description;
        this.host = host;
        this.sandbox = sandbox;
        this.postStageStdout = postStageStdout;
        this.postStageStderr = postStageStderr;

        resolve();

        if (logger.isInfoEnabled()) {
            logger.info(this);
        }
    }

    public PostStagedFileSet(GATContext gatContext, List<File> files,
            String host, String sandbox) throws GATInvocationException {
        this.gatContext = gatContext;
        this.host = host;
        this.sandbox = sandbox;

        resolveFiles(files);
    }

    /** also adds stdout, stderrto set of files to preStage if needed. */
    private void resolve() throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        Map<File, File> post = sd.getPostStaged();

        ArrayList<PostStagedFile> tmp = new ArrayList<PostStagedFile>();

        if (post != null) {
            Set<File> keys = post.keySet();
            Iterator<File> i = keys.iterator();

            while (i.hasNext()) {
                File srcFile = (File) i.next();
                File destFile = (File) post.get(srcFile);

                tmp.add(new PostStagedFile(gatContext, srcFile, destFile, host,
                        sandbox, false, false));
            }
        }

        // if (postStageStdout) {
        File resolvedStdout = null;
        if (sd.getStdout() != null) {
            try {
                resolvedStdout = GAT.createFile(sd.getStdout()
                        .getFileInterface().getGATContext(), new URI("any:///"
                        + sd.getStdout().getName()));
            } catch (Exception e) {
                throw new GATInvocationException("postStagedFileSet", e);
            }
        }
        if (resolvedStdout != null) {
            tmp.add(new PostStagedFile(gatContext, resolvedStdout, sd
                    .getStdout(), host, sandbox, true, false));
        }

        File resolvedStderr = null;
        if (sd.getStderr() != null) {
            try {
                resolvedStderr = GAT.createFile(sd.getStderr()
                        .getFileInterface().getGATContext(), new URI("any:///"
                        + sd.getStderr().getName()));
            } catch (Exception e) {
                throw new GATInvocationException("postStagedFileSet", e);
            }
        }
        if (resolvedStderr != null) {
            tmp.add(new PostStagedFile(gatContext, resolvedStderr, sd
                    .getStderr(), host, sandbox, false, true));
        }

        // }

        // // if (postStageStderr) {
        // File stderr = sd.getStderr();
        // if (stderr != null) {
        // try {
        // File f = GAT.createFile(gatContext, preferences, new URI(
        // "any:///" + stderr.getName()));
        // tmp.add(new PostStagedFile(gatContext, preferences, f, stderr,
        // host, sandbox, false, true));
        // } catch (Exception e) {
        // throw new GATInvocationException("postStagedFileSet", e);
        // }
        // }
        // // }

        files = (PostStagedFile[]) tmp.toArray(new PostStagedFile[] {});
    }

    private void resolveFiles(List<File> f) throws GATInvocationException {
        if (f == null)
            return;

        int startPos = 0;
        if (files == null) {
            files = new PostStagedFile[f.size()];
        } else {
            PostStagedFile[] tmp = new PostStagedFile[files.length + f.size()];
            for (int i = 0; i < files.length; i++) {
                tmp[i] = files[i];
            }
            startPos = files.length;
            files = tmp;
        }
        for (int i = 0; i < f.size(); i++) {
            File srcFile = (File) f.get(i);
            files[startPos + i] = new PostStagedFile(gatContext, srcFile, null,
                    host, sandbox, false, false);
        }
    }

    protected void poststage() throws GATInvocationException {
        GATInvocationException exceptions = new GATInvocationException();
        for (int i = 0; i < files.length; i++) {
            try {
                // post stage only if needed!
                if (!(files[i].isStdout() && !postStageStdout || files[i]
                        .isStderr()
                        && !postStageStderr)) {
                    files[i].poststage();
                }
            } catch (Throwable e) {
                exceptions.add("resource broker cpi", e);
            }
        }
        if (exceptions.getNrChildren() != 0) {
            throw exceptions;
        }
    }

    public void delete() throws GATInvocationException {
        GATInvocationException e = new GATInvocationException();
        for (int i = 0; i < files.length; i++) {
            try {
                files[i].delete();
            } catch (Exception x) {
                e.add("resource broker", x);
            }
        }

        if (e.getNrChildren() != 0)
            throw e;
    }

    public void wipe() throws GATInvocationException {
        GATInvocationException e = new GATInvocationException();
        for (int i = 0; i < files.length; i++) {
            try {
                files[i].wipe();
            } catch (Exception x) {
                e.add("resource broker", x);
            }
        }

        if (e.getNrChildren() != 0)
            throw e;
    }

    PostStagedFile getStdout() {
        for (int i = 0; i < files.length; i++) {
            if (files[i].isStdout()) {
                return files[i];
            }
        }

        return null;
    }

    PostStagedFile getStderr() {
        for (int i = 0; i < files.length; i++) {
            if (files[i].isStderr()) {
                return files[i];
            }
        }

        return null;
    }

    public int size() {
        return files.length;
    }

    public PostStagedFile getFile(int i) {
        return files[i];
    }

    public String toString() {
        String res = "";
        res += "PostStagedFileSet:\n";
        for (int i = 0; i < files.length; i++) {
            res += files[i] + "\n";
        }
        return res;
    }

    /**
     * @return the files
     */
    public PostStagedFile[] getFiles() {
        return files;
    }

    /**
     * @param files
     *                the files to set
     */
    public void setFiles(PostStagedFile[] files) {
        this.files = files;
    }

    /**
     * @return the postStageStderr
     */
    public boolean isPostStageStderr() {
        return postStageStderr;
    }

    /**
     * @param postStageStderr
     *                the postStageStderr to set
     */
    public void setPostStageStderr(boolean postStageStderr) {
        this.postStageStderr = postStageStderr;
    }

    /**
     * @return the postStageStdout
     */
    public boolean isPostStageStdout() {
        return postStageStdout;
    }

    /**
     * @param postStageStdout
     *                the postStageStdout to set
     */
    public void setPostStageStdout(boolean postStageStdout) {
        this.postStageStdout = postStageStdout;
    }
}
