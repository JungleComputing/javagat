/*
 * MPA Source File: SgeJob.java Copyright (c) 2003-2005 by MPA Garching
 *
 * $Created:	14.10.2005 (13:03:54) by doerl $ Last Change: 14.10.2005 (13:03:54) by doerl
 */
package org.gridlab.gat.resources.cpi.pbs_resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * @author doerl
 */
public class PbsJob extends JobCpi {
    private static final long serialVersionUID = -5229286816606473700L;

    private PbsResourceBrokerAdaptor mBroker;

    private String mId;

    private MetricDefinition statusMetricDefinition;

    private Metric statusMetric;

    protected PbsJob(GATContext gatContext, JobDescription description,
            Sandbox sandbox) {
        super(gatContext, description, sandbox);

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        registerMetric("getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);
    }

    protected void setJobID(String jobID) {
        mId = jobID;
    }

    protected void setResourceBroker(PbsResourceBrokerAdaptor broker) {
        mBroker = broker;
    }

    protected synchronized void setState(JobState state) {
        this.state = state;
        MetricEvent v = new MetricEvent(this, state, statusMetric, System
                .currentTimeMillis());
        fireMetric(v);
    }

    public Map<String, Object> getInfo() throws GATInvocationException {
        Map<String, Object> result = mBroker.getInfo(mId);
        result.put("state", state);
        for (Iterator<String> i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            try {
                if (((String) key).startsWith("PBS_O_")) {
                    i.remove();
                }
            } catch (ClassCastException ex) {
            }
        }
        result.put("adaptor.job.id", mId);
        Object object = result.remove("submission_time");
        if (object != null) {
            result.put("submissiontime", object);
        }
        object = result.remove("qsub_time");
        if (object != null) {
            result.put("submissiontime", object);
        }
        object = result.remove("start_time");
        if (object != null) {
            result.put("starttime", object);
        }
        object = result.remove("end_time");
        if (object != null) {
            result.put("stoptime", object);
        }
        object = result.remove("exit_status");
        if (object != null) {
            result.put("exitValue", object);
        }
        return result;
    }

    public JobState getState() {
        synchronized (this) {
            try {
                state = mBroker.getState(mId);
            } catch (IOException e) {
                state = JobState.UNKNOWN;
            }
        }
        return state;
    }

    public String marshal() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void stop() throws GATInvocationException {
        if (getState() == JobState.SCHEDULED) {
            PbsMessage res = mBroker.unScheduleJob(mId);
            if (!res.isDeleted()) {
                throw new GATInvocationException(res.getMessage());
            }
            state = JobState.INITIAL;
            return;
        }

        if (getState() == JobState.RUNNING) {
            PbsMessage res = mBroker.cancelJob(mId);
            if (!res.isDeleted()) {
                throw new GATInvocationException(res.getMessage());
            }
            state = JobState.INITIAL;
            return;
        }

        throw new GATInvocationException("Job is not running or scheduled");
    }

    public void hold() throws GATInvocationException {
        if (getState() != JobState.RUNNING) {
            throw new GATInvocationException("Job is not running");
        }
        // PbsMessage res = mBroker.holdJob(mId);
        // if (!res.isDeleted()) {
        // throw new GATInvocationException(res.getMessage());
        // }
        state = JobState.INITIAL;
    }

    public void release() throws GATInvocationException {
        if (getState() != JobState.RUNNING) {
            throw new GATInvocationException("Job is not running");
        }
        // PbsMessage res = mBroker.releaseJob(mId);
        // if (!res.isDeleted()) {
        // throw new GATInvocationException(res.getMessage());
        // }
        state = JobState.INITIAL;
    }
    /*
     * public void unSchedule() throws GATInvocationException, IOException { if
     * (getState() != SCHEDULED) { throw new GATInvocationException("Job is not
     * in schedule state"); } PbsMessage res = mBroker.unScheduleJob(mId); if
     * (!res.isDeleted()) { throw new GATInvocationException(res.getMessage()); }
     * state = INITIAL; }
     */
}
