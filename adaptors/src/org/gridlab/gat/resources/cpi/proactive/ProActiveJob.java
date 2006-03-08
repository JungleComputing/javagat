package org.gridlab.gat.resources.cpi.proactive;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.objectweb.proactive.GATAdaptor.ProActiveLauncher;
import org.objectweb.proactive.core.node.Node;

public class ProActiveJob extends Job {
    static int jobsAlive = 0;

    ProActiveLauncher launcher;

    JobDescription jobDescription;

    // GramJob j;

    Node node;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    GATInvocationException postStageException = null;

    boolean postStageFinished = false;

    String jobID;

    public ProActiveJob(ProActiveLauncher launcher,
            JobDescription jobDescription, String jobID, Node node)
            throws GATInvocationException {
        this.launcher = launcher;
        this.jobDescription = jobDescription;
        this.jobID = jobID;
        state = SCHEDULED;
        try {
            //this.node = launcher.getNode();
            this.node = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        jobsAlive++;

        // Tell the engine that we provide job.status events
        HashMap returnDef = new HashMap();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
            MetricDefinition.DISCRETE, "String", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);
    }

    public JobDescription getJobDescription() {
        return jobDescription;
    }

    protected int getProActiveState() {
        return launcher.getStatus(jobID);
    }

    public synchronized Map getInfo() throws GATInvocationException,
            IOException {
        HashMap m = new HashMap();
        setState(); // update the state
        m.put("state", getStateString());
        m.put("proActiveState", "" + getProActiveState());
        // m.put("error", "" + j.getError());
        m.put("id", jobID);
        /* 
         if (getState() == RUNNING) {
         m.put("hostname", node.getNodeInformation().getURL());
         }
         */
        if (postStageException != null) {
            m.put("postStageError", postStageException);
        }
        return m;
    }

    public String getJobID() throws GATInvocationException, IOException {
        return jobID;
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
        throw new Error("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.Advertisable#unmarshal(java.lang.String)
     */
    public Advertisable unmarshal(String input) {
        throw new Error("Not implemented");
    }

    protected void setState() {
        state = launcher.getStatus(jobID);
    }

    public void stop() throws GATInvocationException, IOException {
        throw new Error("Not implemented");
    }

    public void unSchedule() throws GATInvocationException, IOException {
        throw new Error("Not implemented");
    }
}
