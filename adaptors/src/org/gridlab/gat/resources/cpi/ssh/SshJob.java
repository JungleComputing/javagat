/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.ssh;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;

/**
 * @author rob
 */
@SuppressWarnings("serial")
public class SshJob extends JobCpi {

    protected static Logger logger = Logger.getLogger(SshJob.class);

    class ProcessWaiter extends Thread {

        ProcessWaiter() {
            setName("ssh resourceBroker adaptor waiter");
            setDaemon(true);
            start();
        }

        public void run() {
            try {
                while (true) {
                    if (channel.isEOF()) {
                        finished(channel.getExitStatus());
                        if (channel != null)
                            channel.disconnect();
                        if (session != null)
                            session.disconnect();
                        break;
                    }
                }
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("SshJob: while waiting for EOF of channel,"
                            + " an error occurred: " + e);
                }
            }
        }
    }

    SshResourceBrokerAdaptor broker;

    GATInvocationException postStageException = null;

    int jobID;

    Channel channel;

    Session session;

    int exitVal = 0;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    protected SshJob(GATContext gatContext, Preferences preferences,
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

    protected void startProcessWaiter() {
        new ProcessWaiter();
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    protected void setChannel(Channel channel) {
        this.channel = channel;
    }

    protected synchronized void setState(int state) {
        this.state = state;
        MetricValue v = new MetricValue(this, getStateString(state),
                statusMetric, System.currentTimeMillis());
        GATEngine.fireMetric(this, v);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getInfo()
     */
    public synchronized Map<String, Object> getInfo() {
        HashMap<String, Object> m = new HashMap<String, Object>();
        // update state
        getState();

        m.put("state", getStateString(state));
        m.put("exitValue", "" + exitVal);
        m.put("hostname", sandbox.getHost());

        if (postStageException != null) {
            m.put("postStageError", postStageException);
        }

        return m;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getJobID()
     */
    public String getJobID() {
        return "" + jobID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getExitStatus()
     */
    public synchronized int getExitStatus() throws GATInvocationException {
        if (state != STOPPED)
            throw new GATInvocationException("not in RUNNING state");
        return exitVal;
    }

    void finished(int exitValue) {
        MetricValue v = null;

        synchronized (this) {
            exitVal = exitValue;
            state = POST_STAGING;
            v = new MetricValue(this, getStateString(state), statusMetric,
                    System.currentTimeMillis());
            if (logger.isDebugEnabled()) {
                logger.debug("default job callback: firing event: " + v);
            }
        }
        GATEngine.fireMetric(this, v);

        sandbox.retrieveAndCleanup(this);

        synchronized (this) {
            state = STOPPED;
            v = new MetricValue(this, getStateString(state), statusMetric,
                    System.currentTimeMillis());
            if (logger.isDebugEnabled()) {
                logger.debug("default job callback: firing event: " + v);
            }
        }
        GATEngine.fireMetric(this, v);
        finished();
    }

    public void stop() throws GATInvocationException {
        MetricValue v = null;
        if (channel != null) {
            try {
                /*
                 * KNOWN BUG: The sshjob sends a "SIGTERM" using the ssh
                 * protocol (which supports signals). Unfortunately _no_ ssh
                 * server or client supports these signals. The
                 * CommandlineSshAdaptor does support signals because it uses an
                 * ssh "pseudo terminal".
                 */
                channel.sendSignal("TERM");
                Thread.sleep(1000); // give the process some time to cleanup
            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    logger.info("exception while sending KILL signal: " + e);
                }

                // ignore it, close the channel
            }
            channel.disconnect();
        }

        if (session != null) {
            session.disconnect();
        }

        synchronized (this) {
            state = POST_STAGING;
            v = new MetricValue(this, getStateString(state), statusMetric,
                    System.currentTimeMillis());
            if (logger.isDebugEnabled()) {
                logger.debug("default job callback: firing event: " + v);
            }
        }
        GATEngine.fireMetric(this, v);

        sandbox.retrieveAndCleanup(this);

        synchronized (this) {
            state = STOPPED;
            v = new MetricValue(this, getStateString(state), statusMetric,
                    System.currentTimeMillis());
            if (logger.isDebugEnabled()) {
                logger.debug("default job callback: firing event: " + v);
            }
        }
        GATEngine.fireMetric(this, v);
        finished();
    }
}
