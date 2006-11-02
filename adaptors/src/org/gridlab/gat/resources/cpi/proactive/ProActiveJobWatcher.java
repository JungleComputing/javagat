package org.gridlab.gat.resources.cpi.proactive;

import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;


/**
 * This class provides a thread that initiates the cluster information.
 * In addition, it provides callbacks for ProActiveJobs.
 */
public class ProActiveJobWatcher implements java.io.Serializable,
        RunActive {

    /** Maps job id's to jobs. */
    private HashMap jobs = new HashMap();

    Body body = null;

    private int jobCounter = 0;

    public ProActiveJobWatcher() {
    }

    String getNewID() {
        String retval = "_" + jobCounter;
        jobCounter++;
        return retval;

    }
    /**
     * Adds a job to the job list to be watched by the ProActiveJobWatcher
     * thread.
     * @param jobID identification of the job.
     * @param job the job to be watched.
     */
    synchronized void addJob(String jobID, ProActiveJob job) {
        jobs.put(jobID, job);
    }

    synchronized void removeJob(String jobID) {
        jobs.remove(jobID);
    }

    public void runActivity(Body body) {
        Service service = new Service(body);

        this.body = body;

        while (body.isAlive() && body.isActive()) {
            service.blockingServeOldest();
        }
    }

    public void finishedJob(String jobID, int exitStatus) {
        ProActiveJob job;

        synchronized(this) {
            job = (ProActiveJob) jobs.get(jobID);
            jobs.remove(jobID);
        }

        if (job != null) {
            job.initiatePostStaging(exitStatus);
        }
    }

    public void startedJob(String jobID) {
        ProActiveJob job = (ProActiveJob) jobs.get(jobID);
        if (job != null) {
            job.setStarted();
        }
    }
}
