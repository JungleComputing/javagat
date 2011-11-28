package org.gridlab.gat.resources.cpi;

import ibis.util.ThreadPool;

import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.FileWaiter;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.WrapperJobDescription.WrappedJobInfo;

/**
 * A Job object of a job that is submitted by a Wrapper.
 * 
 * To use a Wrapper to submit a (regular) job, the preference "useWrapper" must
 * have the value "true". JavaGAT then creates a (wrapper) job that executes a
 * Wrapper at the location where the (regular) job should execute. The Wrapper
 * executes and submits locally one or more (regular) jobs. The submission of a
 * (regular) job will return a job object of the type WrappedJob.
 * 
 * The WrappedJob has some special methods besides the standard Job
 * functionality. It is possible to retrieve the Job of the Wrapper, which may
 * come in handy if you use multi jobs and you want to know when the WrapperJob
 * is finished (i.e. when all WrappedJobs are finished). You can also add a
 * MetricListener to the WrapperJob.
 * 
 * The WrappedJob listens to its status by monitoring (polling) a status file.
 * The Wrapper, which listens to the local (regular) job, writes the state
 * changes to this status file as a way to communicate with the WrappedJob
 * object. The status files typically are located at $HOME and look like:
 * ".JavaGATstatus" + jobID
 * 
 * @author rkemp
 */

@SuppressWarnings("serial")
public class WrappedJobCpi extends JobCpi implements Runnable {

    private MetricDefinition statusMetricDefinition;

    private Metric statusMetric;
    
    private WrappedJobInfo info;
    
    Map<String, Object> jobInfo = null;

    /**
     * Creates a new WrappedJob.
     * 
     * This constructor is used by the WrapperSubmitter.
     * 
     * @param gatContext
     */
    public WrappedJobCpi(GATContext gatContext, WrappedJobInfo info,
            WrapperJobCpi wrapper) {
        super(gatContext, info.getJobDescription(), null);
        
        this.info = info;

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        registerMetric("getJobStatus", statusMetricDefinition);
        try {
            addMetricListener(wrapper, statusMetric);
        } catch (GATInvocationException e) {
            // ignored
        }

        // start a thread that monitors the job state, by monitoring a file
        ThreadPool.createNew(this, "Wrapped Job State Monitor " + getJobID());
    }

    private void fireStateMetric(JobState state) {
        if (logger.isInfoEnabled()) {
            logger.info("WrappedJob firing metric " + state);
        }
        MetricEvent v = new MetricEvent(this, state, statusMetric,
                System.currentTimeMillis());
        fireMetric(v);
    }

    FileWaiter waiter = null;
    
    @SuppressWarnings("unchecked")
    public void run() {
	// Since the WrappedJobCpi runs on the submitting host, the jobstate filename
	// (which may be relative, and if so, is relative to where the wrapper runs),
	// must be rewritten.
	URI jobState = info.getJobStateFileName();
	if (! jobState.isAbsolute()) {
	    String host = info.getBrokerURI().getHost();
	    URI hostURI = null;
	    try {
		hostURI = new URI("any://" + host + "/blabla");
	    } catch (URISyntaxException e) {
		// Should not happen
		hostURI = info.getBrokerURI();
	    }
	    jobState = Wrapper.rewriteURI(jobState, hostURI);
	}

	String s = jobState.toString();
	int index = s.lastIndexOf("/");
	String directory = s.substring(0, index);
	String file = s.substring(index+1);
	try {
	    waiter = FileWaiter.createFileWaiter(GAT.createFile(gatContext, directory));
	} catch (Throwable e) {
	    logger.debug("Should not happen: ", e);
	    return;
	}
	
        do {
            waiter.waitFor(file);
            JobState newstate = null;
            ObjectInputStream din = null;
            FileInputStream fin = null;
            try {
        	fin = GAT.createFileInputStream(gatContext, jobState);
                din = new ObjectInputStream(new BufferedInputStream(fin));
                
                jobInfo = (Map<String, Object>) din.readObject();
                s = (String) jobInfo.get("state");
                newstate = JobState.valueOf(s);
            } catch (Throwable e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("", e);
                }
            } finally {
        	if (fin != null) {
        	    if (din != null) {
        		try {
        		    din.close();
        		} catch(Throwable e) {
        		    // ignored
        		}
        	    }
        	    try {
        		fin.close();
        	    } catch (Throwable e) {
        		// ignored
        	    }
        	}
            }
            if (newstate != null) {        	
                state = newstate;
                fireStateMetric(state);
                
                try {
                    File monitorFile = GAT.createFile(gatContext, info.getJobStateFileName());
                    if (!monitorFile.delete()) {
                	logger.info("Could not delete job status file!");
                    }
                } catch(Throwable e) {
                    logger.info("Could not delete job status file!");
                }
            }
        } while (state != JobState.STOPPED
                && state != JobState.SUBMISSION_ERROR);
    }
    
    public Map<String, Object> getInfo() {
	return new HashMap<String, Object>(jobInfo);
    }
    
    public String toString() {
        if (info == null) {
            return super.toString();
        }
        return "Wrapped job, index " + info.getWrappedJobIndex()
                + ", wrapper job index " + info.getWrapperJobIndex()
                + ", id is " + jobID;
    }

}
