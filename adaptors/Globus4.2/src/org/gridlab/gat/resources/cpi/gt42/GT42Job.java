package org.gridlab.gat.resources.cpi.gt42;

import java.util.HashMap;
import java.util.Map;

import org.globus.exec.client.GramJob;
import org.globus.exec.client.GramJobListener;
import org.globus.exec.generated.StateEnumeration;


import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

public class GT42Job extends JobCpi implements GramJobListener, Runnable{

	private MetricDefinition statusMetricDefinition;

    private GramJob job;

    private int exitStatus;

    private Metric statusMetric;

    private Thread poller;

    private boolean finished = false;

    private StateEnumeration jobState = StateEnumeration.Unsubmitted;

    private String submissionID;
	
    protected GT42Job(GATContext gatContext, JobDescription jobDescription,
            Sandbox sandbox) {
        super(gatContext, jobDescription, sandbox);
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        registerMetric("getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);
        poller = new Thread(this);
        poller.setDaemon(true);
        poller.setName("WSGT4.2 Job - "
                + jobDescription.getSoftwareDescription().getExecutable());
        poller.start();
    }
    
    protected synchronized void setState(JobState state) {
        if (this.state != state) {
            this.state = state;
            MetricEvent v = new MetricEvent(this, state, statusMetric, System
                    .currentTimeMillis());

            if (logger.isDebugEnabled()) {
                logger.debug("wsgt4new job callback: firing event: " + v);
            }
            fireMetric(v);
            if (state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) {
                try {
                    stop(false);
                } catch (GATInvocationException e) {
                    // ignore
                }
            }
        }
    }
    
    public synchronized void stop() throws GATInvocationException {
        stop(gatContext.getPreferences().containsKey("job.stop.poststage")
                && gatContext.getPreferences().get("job.stop.poststage")
                        .equals("false"));
    }
    
    private synchronized void stop(boolean skipPostStage)
    	throws GATInvocationException {
    	if (state != JobState.STOPPED && state != JobState.SUBMISSION_ERROR) {
    		try {
    			job.cancel();
    		} catch (Exception e) {
    			finished();
    			finished = true;
        throw new GATInvocationException("WSGT4newJob", e);
    		}
    		if (state != JobState.POST_STAGING && !skipPostStage) {
    			setState(JobState.POST_STAGING);
    			sandbox.retrieveAndCleanup(this);
    		}
    	} else {
    		if (logger.isDebugEnabled()) {
    			logger.debug("job not running anymore!");
    		}
    	}
    	finished = true;
    	finished();
    }

    public synchronized int getExitStatus() throws GATInvocationException {
        if (getState() != JobState.STOPPED
                && getState() != JobState.SUBMISSION_ERROR) {
            throw new GATInvocationException("exit status not yet available");
        }
        return exitStatus;
    }
    
    
    public synchronized Map<String, Object> getInfo()
		    throws GATInvocationException {
		HashMap<String, Object> m = new HashMap<String, Object>();
		
		m.put("adaptor.job.id", submissionID);
		m.put("state", state.toString());
		m.put("globus.state", jobState);
		if (state != JobState.RUNNING) {
		    m.put("hostname", null);
		} else {
		   // m.put("hostname", job.getEndpoint().getAddress().getHost());
		}
		if (state == JobState.INITIAL || state == JobState.UNKNOWN) {
		    m.put("submissiontime", null);
		} else {
		    m.put("submissiontime", submissiontime);
		}
		if (state == JobState.INITIAL || state == JobState.UNKNOWN
		        || state == JobState.SCHEDULED) {
		    m.put("starttime", null);
		} else {
		    m.put("starttime", starttime);
		}
		if (state != JobState.STOPPED) {
		    m.put("stoptime", null);
		} else {
		    m.put("stoptime", stoptime);
		}
		m.put("poststage.exception", postStageException);
		m.put("resourcebroker", "WSGT4new");
		m
		        .put(
		                "exitvalue",
		                (getState() != JobState.STOPPED && getState() != JobState.SUBMISSION_ERROR) ? null
		                        : "" + getExitStatus());
		if (deleteException != null) {
		    m.put("delete.exception", deleteException);
		}
		if (wipeException != null) {
		    m.put("wipe.exception", wipeException);
		}
		return m;
}
    
    
    
    
	@Override
	public void stateChanged(GramJob arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
