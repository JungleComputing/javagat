package org.gridlab.gat.resources.cpi;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.cpi.SerializedBase;
import org.gridlab.gat.resources.JobDescription;

/**
 * Class that represents a serializable GAT job.
 *  
 * @author Stefan Bozic
 */
@SuppressWarnings("serial")
public class SerializedJob extends SerializedBase implements Serializable, Advertisable {

	/** Logger instance. */
    protected static Logger logger = LoggerFactory.getLogger(SerializedJob.class);

    /** JobDescription which includes further elements to serialize */
    private JobDescription jobDescription;

    /** Sandbox which includes further elements to serialize */    
    private Sandbox sandbox;

    /** The id of the job. */
    private String jobId;

    /** Some times that indicates status changes of the job. */
    private long starttime, stoptime, submissiontime;

    /** The broker uri where the job has been submitted */
    private String brokerUri;
    
	/**
	 * Constructor for Castor
	 */
    public SerializedJob() {
    }

    /**
     * Constructor
     * 
     * @param classname the name of the class to serialize
     * @param jobDescription a  job description
     * @param sandbox a sandbox
     * @param jobId a job id
     * @param submissiontime the job submission time
     * @param starttime the job starttime
     * @param stoptime the job stoptime
     */
    public SerializedJob(String classname, JobDescription jobDescription, Sandbox sandbox,
            String jobId, long submissiontime, long starttime, long stoptime) {
        super(classname);
        this.jobDescription = jobDescription;
        this.sandbox = sandbox;
        this.jobId = jobId;
        this.submissiontime = submissiontime;
        this.starttime = starttime;
        this.stoptime = stoptime;

        if (logger.isDebugEnabled()) {
            logger.debug("created serialized job: " + this);
        }
    }

    /**
     * Marshaling method.
     */
    @Override
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

    /**
     * @return the job description
     */
    public JobDescription getJobDescription() {
        return jobDescription;
    }

    /**
     * @param jobDescription the job description to set
     */
    public void setJobDescription(JobDescription jobDescription) {
        this.jobDescription = jobDescription;
    }

    /**
     * @return the sandbox
     */
    public Sandbox getSandbox() {
        return sandbox;
    }

    /**
     * @param sandbox the sandbox to set
     */
    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    /**
     * @return the starttime
     */
    public long getStarttime() {
        return starttime;
    }

    /**
     * @param starttime the starttime to set
     */
    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    /**
     * @return the stoptime
     */
    public long getStoptime() {
        return stoptime;
    }

    /**
     * @param stoptime the stoptime to set
     */
    public void setStoptime(long stoptime) {
        this.stoptime = stoptime;
    }

    /**
     * @return the submission time
     */
    public long getSubmissiontime() {
        return submissiontime;
    }

    /**
     * @param submissiontime the submission time to set
     */
    public void setSubmissiontime(long submissiontime) {
        this.submissiontime = submissiontime;
    }

	/**
	 * @param brokerUri the brokerUri to set
	 */
	public void setBrokerUri(String brokerUri) {
		this.brokerUri = brokerUri;
	}

	/**
	 * @return the brokerUri
	 */
	public String getBrokerUri() {
		return brokerUri;
	}
	
    /**
     * @see java.lang.Object#toString()
     */
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
}
