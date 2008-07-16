/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.WrapperJobDescription;

public class PreStagedFileSet {

    protected static Logger logger = Logger.getLogger(PreStagedFileSet.class);

    private GATContext gatContext;

    private JobDescription description;

    private String host;

    private String sandbox;

    private boolean preStageStdin;

    private PreStagedFile[] files = new PreStagedFile[0];

    public PreStagedFileSet() {
        // constructor needed for castor marshalling, do *not* use
    }

    /**
     * @return the files
     */
    public PreStagedFile[] getFiles() {
        return files;
    }

    /**
     * @param files
     *                the files to set
     */
    public void setFiles(PreStagedFile[] files) {
        this.files = files;
    }

    public PreStagedFileSet(GATContext gatContext, JobDescription description,
            String host, String sandbox, boolean preStageStdin)
            throws GATInvocationException {
        this.gatContext = gatContext;
        this.description = description;
        this.host = host;
        this.sandbox = sandbox;
        this.preStageStdin = preStageStdin;

        resolve();

        if (logger.isInfoEnabled()) {
            logger.info(this);
        }
    }

    /** Also adds stdin to set of files to preStage if needed */
    private void resolve() throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        String exe = sd.getExecutable();
        if (exe == null) {
            throw new GATInvocationException(
                    "The job description does not contain an executable location");
        }

        ArrayList<PreStagedFile> tmp = new ArrayList<PreStagedFile>();
        Map<File, File> pre = sd.getPreStaged();
        if (pre != null) {
            Set<File> keys = pre.keySet();
            Iterator<File> i = keys.iterator();

            while (i.hasNext()) {
                File srcFile = (File) i.next();
                File destFile = (File) pre.get(srcFile);
                tmp.add(new PreStagedFile(gatContext, srcFile, destFile, host,
                        sandbox, false, exe));
            }
        }

        if (preStageStdin) {
            File stdin = sd.getStdin();

            if (stdin != null) {
                tmp.add(new PreStagedFile(gatContext, stdin, null, host,
                        sandbox, true, exe));
            }
        }

        if (description instanceof WrapperJobDescription) {
            File wrapperInfoFile = ((WrapperJobDescription) description)
                    .getInfoFile();
            try {
                tmp
                        .add(new PreStagedFile(gatContext, wrapperInfoFile, GAT
                                .createFile("wrapper.info"), host, sandbox,
                                false, exe));
            } catch (GATObjectCreationException e) {
                // ignore
            }
        }

        files = (PreStagedFile[]) tmp.toArray(new PreStagedFile[] {});
    }

    protected void prestage() throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        for (int i = 0; i < files.length; i++) {
            try {
                files[i].prestage();
            } catch (Throwable e) {
                if (logger.isInfoEnabled()) {
                    logger
                            .info("prestage failed, removing already staged files: "
                                    + e);
                }

                throw new GATInvocationException("resource broker cpi", e);
            }
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

    PreStagedFile getStdin() {
        for (int i = 0; i < files.length; i++) {
            if (files[i].isStdIn()) {
                return files[i];
            }
        }

        return null;
    }

    PreStagedFile getExecutable() {
        for (int i = 0; i < files.length; i++) {

            if (files[i].isExecutable()) {
                return files[i];
            }
        }

        return null;
    }

    public int size() {
        return files.length;
    }

    public PreStagedFile getFile(int pos) {
        return files[pos];
    }

    public String toString() {
        String res = "";
        res += "PreStagedFileSet:\n";
        for (int i = 0; i < files.length; i++) {
            res += files[i] + "\n";
        }
        return res;
    }
}
