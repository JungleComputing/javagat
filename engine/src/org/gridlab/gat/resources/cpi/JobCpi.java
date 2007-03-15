/*
 * Created on Oct 18, 2006
 */
package org.gridlab.gat.resources.cpi;

import java.util.ArrayList;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
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

    protected static ArrayList jobList = new ArrayList();

    protected static boolean shutdownInProgress = false;
    
    static {
        Runtime.getRuntime().addShutdownHook(new JobShutdownHook());
    }
    
    protected static synchronized int allocJobID() {
        return globalJobID++;
    }

    protected JobCpi(GATContext gatContext, Preferences preferences, JobDescription jobDescription, Sandbox sandbox) {
        this.gatContext = gatContext;
        this.preferences = preferences;
        this.jobDescription = jobDescription;
        this.sandbox = sandbox;
        
        String pref = (String) preferences.get("killJobsOnExit");
        if(pref == null || pref.equalsIgnoreCase("true")) {
            synchronized (JobCpi.class) {
                if(shutdownInProgress) {
                    throw new Error("jobCpi: cannot create new jobs when shutdown is in progress");
                }
                jobList.add(this);
            }
        }
    }

    public final JobDescription getJobDescription() {
        return jobDescription;
    }

    public synchronized int getState() {
        return state;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        return null;
    }

    protected void finished() {
        jobList.remove(this); 
    }
    
    static class JobShutdownHook extends Thread {
        public void run() {
            synchronized (JobCpi.class) {
                shutdownInProgress = true;
            }
            while (true) {
                if (jobList.size() == 0) break;
                Job j;
                try {
                    j = (Job) jobList.remove(0);
                    if(GATEngine.VERBOSE) {
                        System.err.println("stopping job: " + j); 
                    }
                    j.stop();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
