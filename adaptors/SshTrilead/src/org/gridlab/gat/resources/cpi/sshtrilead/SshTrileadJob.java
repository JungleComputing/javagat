/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.sshtrilead;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.StreamForwarder;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
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

    protected SshTrileadJob(GATContext gatContext, JobDescription description,
            Sandbox sandbox) {
        super(gatContext, description, sandbox);

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
        MetricEvent v = new MetricEvent(this, getStateString(state),
                statusMetric, System.currentTimeMillis());
        GATEngine.fireMetric(this, v);
    }

    public String getJobID() {
        return "" + jobID;
    }

    public void startOutputWaiter(StreamForwarder outForwarder,
            StreamForwarder errForwarder) {
        new OutputWaiter(outForwarder, errForwarder);
    }

    public synchronized int getExitStatus() throws GATInvocationException {
        if (state != STOPPED && state != SUBMISSION_ERROR) {
            throw new GATInvocationException(
                    "not in STOPPED or SUBMISSION_ERROR state");
        }
        return exitStatus;
    }

    public synchronized void stop() throws GATInvocationException {
        session.close();
        setState(POST_STAGING);
        sandbox.retrieveAndCleanup(this);
        setState(STOPPED);
        finished();
    }

    class OutputWaiter extends Thread {

        StreamForwarder outForwarder, errForwarder;

        OutputWaiter(StreamForwarder outForwarder, StreamForwarder errForwarder) {
            setName("SshTrileadJob OutputForwarderWaiter");
            setDaemon(true);
            this.outForwarder = outForwarder;
            this.errForwarder = errForwarder;
            start();
        }

        public void run() {
            outForwarder.waitUntilFinished();
            errForwarder.waitUntilFinished();
            session.waitForCondition(ChannelCondition.EXIT_STATUS, 5000);
            try {
                exitStatus = session.getExitStatus();
            } catch (NullPointerException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("unable to retrieve exit status");
                }
            }
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
