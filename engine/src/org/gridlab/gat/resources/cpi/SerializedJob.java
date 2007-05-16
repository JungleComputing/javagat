package org.gridlab.gat.resources.cpi;

import java.io.Serializable;

import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.resources.JobDescription;

public class SerializedJob implements Serializable, Advertisable {
    private JobDescription jobDescription;

    private Sandbox sandbox;

    private boolean postStageFinished;

    private String jobId;

    private long queueTime;

    private long runTime;

    private long startTime;

    // we need this constructor for castor
    public SerializedJob() {
    }

    public SerializedJob(JobDescription jobDescription, Sandbox sandbox,
        boolean postStageFinished, String jobId, long queueTime, long runTime,
        long startTime) {
        this.jobDescription = jobDescription;
        this.sandbox = sandbox;
        this.postStageFinished = postStageFinished;
        this.jobId = jobId;
        this.queueTime = queueTime;
        this.runTime = runTime;
        this.startTime = startTime;
        
        System.err.println("created serialized job: " + this);
    }

    public String marshal() {
        throw new Error("Should not be called");
    }

    /**
     * @return the jobId
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * @param jobId the jobId to set
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public boolean isPostStageFinished() {
        return postStageFinished;
    }

    public void setPostStageFinished(boolean postStageFinished) {
        this.postStageFinished = postStageFinished;
    }

    public long getQueueTime() {
        return queueTime;
    }

    public void setQueueTime(long queueTime) {
        this.queueTime = queueTime;
    }

    public long getRunTime() {
        return runTime;
    }

    public void setRunTime(long runTime) {
        this.runTime = runTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public JobDescription getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(JobDescription jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Sandbox getSandbox() {
        return sandbox;
    }

    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    public String toString() {
        String res = "";

        res += "descr = " + jobDescription;
        res += " sandbox = " + sandbox;
        res += " postStagedFinished: " + postStageFinished;
        res += " jobId: " + jobId;
        res += " queueTime: " + queueTime;
        res += " runTime: " + runTime;
        res += " startTime: " + startTime;

        return res;
    }
}
