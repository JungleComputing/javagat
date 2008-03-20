/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.sshtrilead;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.OutputForwarder;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Session;

/**
 * @author roelof
 */
public class SshTrileadJob extends JobCpi {

    private static final long serialVersionUID = -4510717445792377245L;

    protected static Logger logger = Logger.getLogger(SshTrileadJob.class);

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    private Session session;
    private int jobID;
    // the default exit status is -1
    private int exitStatus = -1;

    protected SshTrileadJob(GATContext gatContext, Preferences preferences,
            JobDescription description, Sandbox sandbox) {
        super(gatContext, preferences, description, sandbox);

        jobID = allocJobID();

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    protected synchronized void setState(int state) {
        this.state = state;
        MetricValue v = new MetricValue(this, getStateString(state),
                statusMetric, System.currentTimeMillis());
        GATEngine.fireMetric(this, v);
    }

    public String getJobID() {
        return "" + jobID;
    }

    public void startOutputWaiter(OutputForwarder outForwarder,
            OutputForwarder errForwarder) {
        new OutputWaiter(outForwarder, errForwarder);
    }

    public synchronized int getExitStatus() throws GATInvocationException {
        if (state != STOPPED && state != SUBMISSION_ERROR) {
            throw new GATInvocationException("not in STOPPED or SUBMISSION_ERROR state");
        }
        return exitStatus;
    }
    
    public synchronized void stop() throws GATInvocationException {
        setState(POST_STAGING);
        sandbox.retrieveAndCleanup(this);
        try {
            session.waitForCondition(ChannelCondition.EXIT_STATUS, 5000);
            exitStatus = session.getExitStatus();
            setState(STOPPED);
        } catch (NullPointerException e) {
            // unable to retrieve exit status
            setState(SUBMISSION_ERROR);
        } finally {
            session.close();
            finished();
        }
    }

    class OutputWaiter extends Thread {

        OutputForwarder outForwarder, errForwarder;

        OutputWaiter(OutputForwarder outForwarder, OutputForwarder errForwarder) {
            setName("SshTrileadJob OutputForwarderWaiter");
            setDaemon(true);
            this.outForwarder = outForwarder;
            this.errForwarder = errForwarder;
            start();
        }

        public void run() {
            outForwarder.waitUntilFinished();
            errForwarder.waitUntilFinished();
            try {
                SshTrileadJob.this.stop();
            } catch (GATInvocationException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("unable to stop job: " + e);
                }
            }
        }
    }

}
