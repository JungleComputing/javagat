package org.gridlab.gat.resources.cpi.unicore;

import java.util.Map;

import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.SerializedJob;

public class SerializedUnicoreJob extends SerializedJob {

    private static final long serialVersionUID = 1L;
    
    private String stdout;
    
    private String stderr;
    
    private String[] toStageOut;
    
    private String[] stagedOut;

    public SerializedUnicoreJob() {
    }

    public SerializedUnicoreJob(String classname, JobDescription jobDescription, Sandbox sandbox,
            String jobId, long submissiontime, long starttime, long stoptime,
            SoftwareDescription sd) {
        super(classname, jobDescription, sandbox, jobId, submissiontime, starttime,
                stoptime);
        
        // Get what is needed from the software description to deal with
        // poststaging.
        File s = sd.getStdout();
        if (s != null) {
            stdout = s.getAbsolutePath();
        }
        s = sd.getStderr();
        if (s != null) {
            stderr = s.getAbsolutePath();
        }
        Map<File, File> postStaged = sd.getPostStaged();
        
        if (postStaged!=null) {
            int sz = postStaged.keySet().size();
            toStageOut = new String[sz];
            stagedOut = new String[sz];
            int index = 0;
            for (java.io.File srcFile : postStaged.keySet()) {
                java.io.File destFile = postStaged.get(srcFile);
                toStageOut[index] = srcFile.getName();
                stagedOut[index] = destFile.getName();
                index++;                  
            }
        }
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public String[] getToStageOut() {
        return toStageOut;
    }

    public void setToStageOut(String[] toStageOut) {
        this.toStageOut = toStageOut;
    }

    public String[] getStagedOut() {
        return stagedOut;
    }

    public void setStagedOut(String[] stagedOut) {
        this.stagedOut = stagedOut;
    }
}
