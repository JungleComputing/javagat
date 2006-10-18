package org.gridlab.gat.resources.cpi.proactive;

import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.objectweb.proactive.GATAdaptor.ProActiveLauncher;
import org.objectweb.proactive.core.node.Node;

public class ProActiveJob extends JobCpi {
    static int jobsAlive = 0;

    ProActiveLauncher launcher;

    // GramJob j;

    Node node;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    boolean postStageFinished = false;

    String jobID;

    public ProActiveJob(ProActiveLauncher launcher,
            JobDescription jobDescription, String jobID, Node node)
            throws GATInvocationException {
        super(jobDescription, null, null);
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

    protected int getProActiveState() {
        return launcher.getStatus(jobID);
    }

    public synchronized Map getInfo() throws GATInvocationException {
        HashMap m = new HashMap();
        setState(); // update the state
        m.put("state", getStateString(state));
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

    public String getJobID() {
        return jobID;
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
}
