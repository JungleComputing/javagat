package org.gridlab.gat.resources.cpi.proactive;

import java.util.HashMap;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;


/**
 * This class keeps track of jobs submitted to a particular cluster.
 * This class must be public, or else the ProActive stub cannot access it.
 */
public class JobWatcher implements java.io.Serializable, RunActive {

    /** Maps job id's to Jobs. */
    private HashMap jobs = new HashMap();

    /** Public noargs constructor, required by ProActive. */
    public JobWatcher() {
    }

    /**
     * Adds a job instance to the list of job instances to be watched.
     * Note: each instance of a Job has a separate id, so several
     * ids can refer to the same Job.
     * @param instanceID identification of the job instance.
     * @param job the Job that spawned this instance.
     */
    synchronized void addJob(String instanceID, Job job) {
        jobs.put(instanceID, job);
    }

    /**
     * Removes the specified job instance from the joblist.
     * @param instanceID identification of the job instance.
     */
    synchronized void removeJob(String instanceID) {
        jobs.remove(instanceID);
    }

    /**
     * ProActive activity handler.
     * @param body the active body.
     */
    public void runActivity(Body body) {
        Service service = new Service(body);

        while (body.isAlive() && body.isActive()) {
            service.blockingServeOldest();
        }
    }

    /**
     * Notifies that the specified job instance finished with the specified
     * exit status.
     * @param instanceID identification of the job instance.
     * @param exitStatus its exit status.
     */
    public void finishedJob(String instanceID, int exitStatus) {
        Job job;

        synchronized(this) {
            job = (Job) jobs.get(instanceID);
            jobs.remove(instanceID);
        }

        if (job != null) {
            job.finish(instanceID, exitStatus);
        }
    }

    /**
     * Notifies that the specified job instance is started.
     * @param instanceID identification of the job.
     */
    public void startedJob(String instanceID) {
        Job job = (Job) jobs.get(instanceID);
        if (job != null) {
            job.setStarted();
        }
    }

    public void addOutput(String instanceID, String out) {
        Job job = (Job) jobs.get(instanceID);
        if (job != null) {
            job.stdout(out);
        } else {
            System.out.print(out);
        }
    }

    public void addError(String instanceID, String err) {
        Job job = (Job) jobs.get(instanceID);
        if (job != null) {
            job.stderr(err);
        } else {
            System.err.print(err);
        }
    }
}
