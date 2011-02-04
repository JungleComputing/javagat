/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.sshtrilead;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
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

    protected static Logger logger = LoggerFactory.getLogger(SshTrileadJob.class);

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    private Session session;

    // the default exit status is -1
    private int exitStatus = -1;

    protected SshTrileadJob(GATContext gatContext, JobDescription description,
            Sandbox sandbox) {
        super(gatContext, description, sandbox);

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        registerMetric("getJobStatus", statusMetricDefinition);
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    protected synchronized void setState(JobState state) {
        if (this.state == state) {
            return;
        }
        this.state = state;
        MetricEvent v = new MetricEvent(this, state, statusMetric, System
                .currentTimeMillis());
        fireMetric(v);
    }

    public synchronized Map<String, Object> getInfo()
            throws GATInvocationException {
        HashMap<String, Object> m = new HashMap<String, Object>();

        m.put("state", state.toString());
        if (state != JobState.RUNNING) {
            m.put("hostname", null);
        } else {
            m.put("hostname", "not available");
        }
        if (state == JobState.INITIAL || state == JobState.UNKNOWN) {
            m.put("submissiontime", null);
        } else {
            // Only the GAT job id is available.
            m.put("adaptor.job.id", "" + jobID);
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
        m.put("resourcebroker", "SshTrilead");
        try {
            m.put("exitvalue", "" + getExitStatus());
        } catch (GATInvocationException e) {
            // ignore
        }
        if (deleteException != null) {
            m.put("delete.exception", deleteException);
        }
        if (wipeException != null) {
            m.put("wipe.exception", wipeException);
        }
        return m;
    }

    // public void startOutputWaiter(StreamForwarder outForwarder,
    // StreamForwarder errForwarder) {
    // new OutputWaiter(outForwarder, errForwarder);
    // }

    public synchronized int getExitStatus() throws GATInvocationException {
        if (state != JobState.STOPPED && state != JobState.SUBMISSION_ERROR) {
            throw new GATInvocationException(
                    "not in STOPPED or SUBMISSION_ERROR state");
        }
        return exitStatus;
    }

    public synchronized void stop() throws GATInvocationException {
        stop(gatContext.getPreferences().containsKey("job.stop.poststage")
                && gatContext.getPreferences().get("job.stop.poststage")
                        .equals("false"));
    }

    private synchronized void stop(boolean skipPostStage)
            throws GATInvocationException {
        if (state == JobState.POST_STAGING
                || state == JobState.STOPPED
                || state == JobState.SUBMISSION_ERROR) {
            return;
        }
        if (session != null) {
            session.close();
            // Give job some time to actually finish/cleanup.
            try {
		Thread.sleep(5000);
	    } catch (InterruptedException e) {
		// ignored
	    }
        }
        if (!skipPostStage) {
            setState(JobState.POST_STAGING);
            sandbox.retrieveAndCleanup(this);
        }
        setState(JobState.STOPPED);
        finished();
    }

    public OutputStream getStdin() throws GATInvocationException {
        if (jobDescription.getSoftwareDescription().streamingStdinEnabled()) {
            return session.getStdin();
        } else {
            throw new GATInvocationException("stdin streaming is not enabled!");
        }
    }

    public InputStream getStdout() throws GATInvocationException {
        if (jobDescription.getSoftwareDescription().streamingStdoutEnabled()) {
            return session.getStdout();
        } else {
            throw new GATInvocationException("stdout streaming is not enabled!");
        }
    }

    public InputStream getStderr() throws GATInvocationException {
        if (jobDescription.getSoftwareDescription().streamingStderrEnabled()) {
            return session.getStderr();
        } else {
            throw new GATInvocationException("stderr streaming is not enabled!");
        }
    }

    protected void monitorState(StreamForwarder stdout, StreamForwarder stderr) {
        new StateMonitor(stdout, stderr);
    }

    class StateMonitor extends Thread {
        
        final StreamForwarder stdout;
        final StreamForwarder stderr;
        
        StateMonitor(StreamForwarder stdout, StreamForwarder stderr) {
            setName("ssh state monitor: "
                    + jobDescription.getSoftwareDescription().getExecutable());
            setDaemon(true);
            this.stdout = stdout;
            this.stderr = stderr;
            start();
        }

        public void run() {
            session.waitForCondition(ChannelCondition.EXIT_STATUS, 0);
            if (stdout != null) {
                stdout.waitUntilFinished();
                stdout.close();
            }
            if (stderr != null) {
                stderr.waitUntilFinished();
                stderr.close();
            }
            try {
                exitStatus = session.getExitStatus();
            } catch (NullPointerException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("unable to retrieve exit status");
                }
            }
            try {
                SshTrileadJob.this.stop(false);
            } catch (GATInvocationException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("unable to stop job: " + e);
                }
            }
        }
    }
}
