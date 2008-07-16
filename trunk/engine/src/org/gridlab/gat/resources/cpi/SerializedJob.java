package org.gridlab.gat.resources.cpi;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.resources.JobDescription;

@SuppressWarnings("serial")
public class SerializedJob implements Serializable, Advertisable {

    protected static Logger logger = Logger.getLogger(SerializedJob.class);

    private JobDescription jobDescription;

    private Sandbox sandbox;

    private String jobId;

    private long starttime, stoptime, submissiontime;

    // we need this constructor for castor
    public SerializedJob() {
    }

    public SerializedJob(JobDescription jobDescription, Sandbox sandbox,
            String jobId, long submissiontime, long starttime, long stoptime) {
        this.jobDescription = jobDescription;
        this.sandbox = sandbox;
        this.jobId = jobId;
        this.submissiontime = starttime;
        this.starttime = starttime;
        this.stoptime = stoptime;

        if (logger.isDebugEnabled()) {
            logger.debug("created serialized job: " + this);
        }
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
     * @param jobId
     *                the jobId to set
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
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
        res += " jobId: " + jobId;
        res += " submissiontime: " + submissiontime;
        res += " starttime: " + starttime;
        res += " stoptime: " + stoptime;

        return res;
    }

    public long getStarttime() {
        return starttime;
    }

    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    public long getStoptime() {
        return stoptime;
    }

    public void setStoptime(long stoptime) {
        this.stoptime = stoptime;
    }

    public long getSubmissiontime() {
        return submissiontime;
    }

    public void setSubmissiontime(long submissiontime) {
        this.submissiontime = submissiontime;
    }
}
