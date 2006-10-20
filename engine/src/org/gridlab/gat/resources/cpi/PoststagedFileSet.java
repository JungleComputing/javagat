/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class PoststagedFileSet {
    GATContext gatContext;

    Preferences preferences;

    JobDescription description;

    String host;

    String sandbox;

    ArrayList files = new ArrayList();; // elements are of type PostStageFile.

    public PoststagedFileSet(GATContext gatContext, Preferences preferences,
        JobDescription description, String host, String sandbox)
        throws GATInvocationException {
        this.gatContext = gatContext;
        this.preferences = preferences;
        this.description = description;
        this.host = host;
        this.sandbox = sandbox;
        
        resolve();
        
        if(GATEngine.VERBOSE) {
            System.err.println(this);
        }
    }

    public PoststagedFileSet(GATContext gatContext, Preferences preferences,
        ArrayList files, String host, String sandbox)
        throws GATInvocationException {
        this.gatContext = gatContext;
        this.preferences = preferences;
        this.host = host;
        this.sandbox = sandbox;

        resolveFiles(files);
    }

    /* also adds stdout, stderrto set of files to preStage */
    private void resolve() throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        Map post = sd.getPostStaged();

        if (post != null) {
            Set keys = post.keySet();
            Iterator i = keys.iterator();

            while (i.hasNext()) {
                File srcFile = (File) i.next();
                File destFile = (File) post.get(srcFile);

                files.add(new PoststagedFile(gatContext, preferences, srcFile,
                    destFile, host, sandbox, false, false));
            }
        }

        File stdout = sd.getStdout();
        if (stdout != null) {
            files.add(new PoststagedFile(gatContext, preferences, null, stdout,
                host, sandbox, true, false));
        }

        File stderr = sd.getStderr();
        if (stderr != null) {
            files.add(new PoststagedFile(gatContext, preferences, null, stderr,
                host, sandbox, false, true));
        }
    }

    private void resolveFiles(ArrayList f) throws GATInvocationException {
        if (f == null) return;

        for (int i = 0; i < f.size(); i++) {
            File srcFile = (File) f.get(i);
            files.add(new PoststagedFile(gatContext, preferences, srcFile,
                null, host, sandbox, false, false));
        }
    }

    protected void poststage() throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        for (int i = 0; i < files.size(); i++) {
            PoststagedFile f = (PoststagedFile) files.get(i);

            GATInvocationException exceptions = new GATInvocationException();
            try {
                f.poststage();
            } catch (Throwable e) {
                exceptions.add("resource broker cpi", e);
            }
            if (exceptions.getNrChildren() != 0) {
                throw exceptions;
            }
        }
    }

    public void delete(boolean onlySandbox) throws GATInvocationException {
        GATInvocationException e = new GATInvocationException();
        for (int i = 0; i < files.size(); i++) {
            try {
                ((PoststagedFile) files.get(i)).delete(onlySandbox);
            } catch (Exception x) {
                e.add("resource broker", x);
            }
        }

        if (e.getNrChildren() != 0) throw e;
    }

    public void wipe(boolean onlySandbox) throws GATInvocationException {
        GATInvocationException e = new GATInvocationException();
        for (int i = 0; i < files.size(); i++) {
            try {
                ((PoststagedFile) files.get(i)).wipe(onlySandbox);
            } catch (Exception x) {
                e.add("resource broker", x);
            }
        }

        if (e.getNrChildren() != 0) throw e;
    }

    File getResolvedStdout() {
        for(int i=0; i<files.size(); i++) {
            PoststagedFile f = (PoststagedFile) files.get(i);
            if(f.isStdout) {
                return f.resolvedSrc;
            }
        }
        
        return null;
    }

    File getResolvedStderr() {
        for(int i=0; i<files.size(); i++) {
            PoststagedFile f = (PoststagedFile) files.get(i);
            if(f.isStderr) {
                return f.resolvedSrc;
            }
        }
        
        return null;
    }
    
    public String toString() {
        String res = "";
        res += "PostStagedFileSet:\n";
        for(int i=0; i<files.size(); i++) {
            res += files.get(i) + "\n";
        }
        return res;
    }
}
