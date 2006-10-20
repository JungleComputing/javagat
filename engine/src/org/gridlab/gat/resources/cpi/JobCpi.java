/*
 * Created on Oct 18, 2006
 */
package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;

public abstract class JobCpi extends Job {
    protected JobDescription jobDescription;

    protected Sandbox sandbox;

    protected GATInvocationException postStageException = null;
    protected GATInvocationException deleteException = null;
    protected GATInvocationException wipeException = null;
    protected GATInvocationException removeSandboxException = null;

    protected static int globalJobID = 0;

    protected GATContext gatContext;

    protected Preferences preferences;

    protected int state = INITIAL;

    protected static synchronized int allocJobID() {
        return globalJobID++;
    }
    
    protected JobCpi(JobDescription jobDescription, Sandbox sandbox) {
        this.jobDescription = jobDescription;
        this.sandbox = sandbox;
    }
    
    public final JobDescription getJobDescription() {
        return jobDescription;
    }
    
    public synchronized int getState() {
        return state;
    }
}
