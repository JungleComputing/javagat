package org.gridlab.gat.resources.cpi.wsgt4new;

import java.util.HashMap;
import java.util.Map;

import org.globus.exec.client.GramJob;
import org.globus.exec.client.GramJobListener;
import org.globus.exec.generated.StateEnumeration;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;


/**
 * Implements JobCpi abstract class.
 * 
 * @author Roelof Kemp
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WSGT4newJob extends JobCpi implements GramJobListener{
    
    private MetricDefinition statusMetricDefinition;
	private GramJob job;

    protected WSGT4newJob(GATContext gatContext, Preferences preferences,
            JobDescription jobDescription, Sandbox sandbox) {
        super(gatContext, preferences, jobDescription, sandbox);

        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetricDefinition.createMetric(null);
    }


    protected synchronized void setState(int state) {
        this.state = state;
    }

    public void stop() throws GATInvocationException {
        sandbox.retrieveAndCleanup(this);
    }

    public synchronized int getExitStatus() throws GATInvocationException {
        if (state != STOPPED)
            throw new GATInvocationException("not in RUNNING state");
        return 0; 
        // We have to assume that the job ran correctly. Globus does
        // not return the exit code.
    }

    public synchronized Map<String, Object> getInfo()
            throws GATInvocationException {
        HashMap<String, Object> m = new HashMap<String, Object>();
        return m;
    }

	public void stateChanged(GramJob arg0) {
        StateEnumeration jobState = job.getState();
        boolean holding = job.isHolding();
        System.out.println("========== State Notification ==========");
        String holdString = "";
        if (holding) holdString = "HOLD ";
        System.out.println("Job State: " + holdString + jobState.getValue());
        System.out.println("========================================");
        if (jobState.equals(StateEnumeration.Done) || jobState.equals(StateEnumeration.Failed)) {
        		System.out.println("Exit Code: " + Integer.toString(job.getExitCode()));
        }
        // if we a running an interactive job,
        // prevent a hold from hanging the client
        if (holding) {
            logger.debug("Automatically releasing hold for interactive job");
            try {
                job.release();
            } catch (Exception e) {
               String errorMessage = "Unable to release job from hold";
               logger.debug(errorMessage, e);
               System.err.println(errorMessage + " - " + e.getMessage());
            }
        }
    }

	protected void setGramJob(GramJob job) {
		this.job = job;
	}
		
}
