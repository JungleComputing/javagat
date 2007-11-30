package org.gridlab.gat.resources.cpi.gt4;

import java.util.HashMap;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ExecutionTaskHandler;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
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

/**
 * Implements the JavaCog <code>StatusListener</code> interface. It listens a
 * submitted job, and is notified at status changes.
 * 
 * @author Balazs Bokodi
 * @version 1.0
 * @since 1.0
 */
class GT4StatusListener implements StatusListener {
    Task task;
    GT4Job job;

    /**
     * Initializes the <code>GT4StatusListener</code> object.
     * 
     * @param j
     *                a <code>GT4Job</code> object. The state of the job
     *                changes every time when the submitted job state is
     *                changed.
     */
    public GT4StatusListener(GT4Job j) {
        job = j;
    }

    /**
     * This callback method is invoked when the submitted job's state is
     * changed. The matches between the JavaCog states and JavaGAT states are
     * arbitrary.
     * 
     * @param event
     *                the passed <code>StatusEvent</code> has the
     *                <code>State</code> object.
     */
    public void statusChanged(StatusEvent event) {
        job.setState(event.getStatus());
    }
}

/**
 * Implements JobCpi abstract class. Wrappers a JavaCog task.
 * 
 * @author Balazs Bokodi
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("serial")
public class GT4Job extends JobCpi {
    GT4ResourceBrokerAdaptor broker;
    Task task;

    private GT4StatusListener statusListener;
    protected GT4JobPoller poller;

    private MetricDefinition statusMetricDefinition;

    private Metric statusMetric;

    private boolean postStageStarted = false;

    /**
     * Initializes a job. Creates a task, sets up the listener and submits it.
     */
    public GT4Job(GATContext gatContext, Preferences preferences,
            JobDescription jobDescription, Sandbox sandbox,
            JobSpecification spec, Service service)
            throws GATInvocationException {
        super(gatContext, preferences, jobDescription, sandbox);

        // Tell the engine that we provide job.status events

        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);

        task = new TaskImpl("gatgt4jobtest", Task.JOB_SUBMISSION);
        task.setSpecification(spec);
        task.setService(Service.JOB_SUBMISSION_SERVICE, service);
        TaskHandler handler = new ExecutionTaskHandler();
        statusListener = new GT4StatusListener(this);
        task.addStatusListener(statusListener);

        poller = new GT4JobPoller(this);
        poller.start();

        try {
            handler.submit(task);
        } catch (IllegalSpecException e) {
            throw new GATInvocationException("GT4Job illegal spec: " + e);
        } catch (InvalidSecurityContextException e) {
            throw new GATInvocationException("GT4Job invalid security: " + e);
        } catch (InvalidServiceContactException e) {
            throw new GATInvocationException("GT4Job invalid service: " + e);
        } catch (TaskSubmissionException e) {
            throw new GATInvocationException("GT4Job task submission: " + e);
        }

    }

    /**
     * The <code>GT4StatusListener</code> calls this function to set the state
     * of the job.
     * 
     * @param state
     */

    public void stop() throws GATInvocationException {
        String stateString = null;
        synchronized (this) {
            // we don't want to postStage twice (can happen with jobpoller)
            if (postStageStarted) {
                return;
            }
            postStageStarted = true;
            state = POST_STAGING;
            stateString = getStateString(state);
        }
        task.removeStatusListener(statusListener);

        MetricValue v = new MetricValue(this, stateString, statusMetric, System
                .currentTimeMillis());

        if (logger.isDebugEnabled()) {
            logger.debug("gt4 job stop: firing event: " + v);
        }

        GATEngine.fireMetric(this, v);

        if (logger.isInfoEnabled()) {
            logger.info("gt4 job stop: delete/wipe starting");
        }

        // do cleanup, callback handler has been uninstalled
        sandbox.retrieveAndCleanup(this);

        synchronized (this) {

            if (logger.isInfoEnabled()) {
                logger.info("globus job stop: post stage finished");
            }

            state = STOPPED;
            stateString = getStateString(state);
        }

        MetricValue v2 = new MetricValue(this, stateString, statusMetric,
                System.currentTimeMillis());

        if (logger.isDebugEnabled()) {
            logger.debug("globus job stop: firing event: " + v2);
        }

        GATEngine.fireMetric(this, v2);

        finished();

        if (poller != null) {
            poller.die();
        }
    }

    protected synchronized void setState(int state) {
        this.state = state;

        MetricValue v = new MetricValue(this, getStateString(state),
                statusMetric, System.currentTimeMillis());
        GATEngine.fireMetric(this, v);
    }

    protected synchronized void setState(Status status) {
        System.out.println(status.getStatusString());
        switch (status.getStatusCode()) {
        case Status.ACTIVE:
            setState(GT4Job.RUNNING);
            break;
        case Status.CANCELED:
            setState(GT4Job.STOPPED);
            break;
        case Status.COMPLETED:
            setState(GT4Job.STOPPED);
            break;
        case Status.FAILED:
            setState(GT4Job.SUBMISSION_ERROR);
            break;
        case Status.RESUMED:
            setState(GT4Job.RUNNING);
            break;
        case Status.SUBMITTED:
            setState(GT4Job.SCHEDULED);
            break;
        case Status.SUSPENDED:
            setState(GT4Job.ON_HOLD);
            break;
        case Status.UNKNOWN:
            setState(GT4Job.UNKNOWN);
            break;
        case Status.UNSUBMITTED:
            setState(GT4Job.UNKNOWN);
            break;
        default:
            setState(GT4Job.UNKNOWN);
        }
    }

    protected void getStateActive() {
        if (logger.isDebugEnabled()) {
            logger.debug("polling state of globus job");
        }
        if (task != null) {
            Status status = task.getStatus();
            setState(status);
        }

    }
}
