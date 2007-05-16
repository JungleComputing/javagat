package org.gridlab.gat.resources.cpi;

import java.io.Serializable;

import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.resources.JobDescription;

public class SerializedJob implements Serializable, Advertisable {
    private JobDescription jobDescription;

    private Sandbox sandbox;

    private boolean postStageFinished;

    private String jobID;

    private long queueTime;

    private long runTime;

    private long startTime;

    // we need this constructor for castor
    public SerializedJob() {
    }
    
    public SerializedJob(JobDescription jobDescription, Sandbox sandbox,
            boolean postStageFinished, String jobID, long queueTime,
            long runTime, long startTime) {
        this.jobDescription = jobDescription;
        this.sandbox = sandbox;
        this.postStageFinished = postStageFinished;
        this.jobID = jobID;
        this.queueTime = queueTime;
        this.runTime = runTime;
        this.startTime = startTime;
    }

    public String marshal() {
        throw new Error("Should not be called");
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
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
}
