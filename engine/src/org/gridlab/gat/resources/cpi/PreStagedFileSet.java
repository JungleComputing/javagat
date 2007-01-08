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
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class PreStagedFileSet {
    GATContext gatContext;

    Preferences preferences;

    JobDescription description;

    String host;

    String sandbox;

    boolean preStageStdin;

    ArrayList files; // elements are of type PreStageFile.

    public PreStagedFileSet(GATContext gatContext, Preferences preferences,
        JobDescription description, String host, String sandbox,
        boolean preStageStdin) throws GATInvocationException {
        this.gatContext = gatContext;
        this.preferences = preferences;
        this.description = description;
        this.host = host;
        this.sandbox = sandbox;
        this.preStageStdin = preStageStdin;

        resolve();

        if (GATEngine.VERBOSE) {
            System.err.println(this);
        }
    }

    /** Also adds stdin to set of files to preStage if needed */
    private void resolve() throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        URI exe = sd.getLocation();
        if(exe == null) {
            throw new GATInvocationException(
            "The job description does not contain an executable location");    
        }
        
        files = new ArrayList();
        Map pre = sd.getPreStaged();

        if (pre != null) {
            Set keys = pre.keySet();
            Iterator i = keys.iterator();

            while (i.hasNext()) {
                File srcFile = (File) i.next();
                File destFile = (File) pre.get(srcFile);
                files.add(new PreStagedFile(gatContext, preferences, srcFile,
                    destFile, host, sandbox, false, exe)); 
            }
        }

        if (preStageStdin) {
            File stdin = sd.getStdin();

            if (stdin != null) {
                files.add(new PreStagedFile(gatContext, preferences, stdin,
                    null, host, sandbox, true, exe));
            }
        }
    }

    protected void prestage() throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        for (int i = 0; i < files.size(); i++) {
            PreStagedFile f = (PreStagedFile) files.get(i);

            try {
                f.prestage();
            } catch (Throwable e) {
                if (GATEngine.VERBOSE) {
                    System.err
                        .println("prestage failed, removing already staged files.");
                }

                throw new GATInvocationException("resource broker cpi", e);
            }
        }
    }

    public void delete(boolean onlySandbox) throws GATInvocationException {
        GATInvocationException e = new GATInvocationException();
        for (int i = 0; i < files.size(); i++) {
            try {
                ((PreStagedFile) files.get(i)).delete(onlySandbox);
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
                ((PreStagedFile) files.get(i)).wipe(onlySandbox);
            } catch (Exception x) {
                e.add("resource broker", x);
            }
        }

        if (e.getNrChildren() != 0) throw e;
    }

    PreStagedFile getStdin() {
        for (int i = 0; i < files.size(); i++) {
            PreStagedFile f = (PreStagedFile) files.get(i);
            if (f.isStdIn) {
                return f;
            }
        }

        return null;
    }

    PreStagedFile getExecutable() {
        for (int i = 0; i < files.size(); i++) {
            PreStagedFile f = (PreStagedFile) files.get(i);
            if (f.isExecutable) {
                return f;
            }
        }

        return null;
    }

    public int size() {
        return files.size();
    }
    
    public PreStagedFile getFile(int pos) {
        return (PreStagedFile) files.get(pos);
    }
    
    public String toString() {
        String res = "";
        res += "PreStagedFileSet:\n";
        for (int i = 0; i < files.size(); i++) {
            res += files.get(i) + "\n";
        }
        return res;
    }
}
