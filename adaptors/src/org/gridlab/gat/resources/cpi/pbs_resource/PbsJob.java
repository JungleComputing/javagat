/*
 * MPA Source File: SgeJob.java Copyright (c) 2003-2005 by MPA Garching
 *
 * $Created:	14.10.2005 (13:03:54) by doerl $ Last Change: 14.10.2005 (13:03:54) by doerl
 */
package org.gridlab.gat.resources.cpi.pbs_resource;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * @author  doerl
 */
public class PbsJob extends JobCpi {
    private static final long serialVersionUID = -5229286816606473700L;

    private PbsBrokerAdaptor mBroker;

    private String mId;

    //	private Metric mMetric;

    public PbsJob(GATContext gatContext, Preferences preferences, PbsBrokerAdaptor broker, JobDescription description, String id, Sandbox sandbox) {
        super(gatContext, preferences, description, sandbox);
        mBroker = broker;
        mId = id;
        state = INITIAL;
    }

    public Map<String, Object> getInfo() throws GATInvocationException {
        Map<String, Object> result = mBroker.getInfo(mId);
        result.put("state", getStateString(getState()));
        for (Iterator<String> i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            try {
                if (((String) key).startsWith("PBS_O_")) {
                    i.remove();
                }
            } catch (ClassCastException ex) {
            }
        }
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

    public String getJobID() throws GATInvocationException {
        return mId;
    }

    public int getState() {
        synchronized (this) {
            try {
                state = mBroker.getState(mId);
            } catch (IOException e) {
                state = UNKNOWN;
            }
        }
        return state;
    }

    public String marshal() {
        throw new Error("Not implemented");
    }

    public void stop() throws GATInvocationException {
        if (getState() == SCHEDULED) {
            PbsMessage res = mBroker.unScheduleJob(mId);
            if (!res.isDeleted()) {
                throw new GATInvocationException(res.getMessage());
            }
            state = INITIAL;
            return;
        }

        if (getState() == RUNNING) {
            PbsMessage res = mBroker.cancelJob(mId);
            if (!res.isDeleted()) {
                throw new GATInvocationException(res.getMessage());
            }
            state = INITIAL;
            return;
        }

        throw new GATInvocationException("Job is not running or scheduled");
    }

    public void hold() throws GATInvocationException {
        if (getState() != RUNNING) {
            throw new GATInvocationException("Job is not running");
        }
        //        PbsMessage res = mBroker.holdJob(mId);
        //         if (!res.isDeleted()) {
        //             throw new GATInvocationException(res.getMessage());
        //         }
        state = INITIAL;
    }

    public void release() throws GATInvocationException {
        if (getState() != RUNNING) {
            throw new GATInvocationException("Job is not running");
        }
        //        PbsMessage res = mBroker.releaseJob(mId);
        //         if (!res.isDeleted()) {
        //             throw new GATInvocationException(res.getMessage());
        //         }
        state = INITIAL;
    }
/*
    public void unSchedule() throws GATInvocationException, IOException {
        if (getState() != SCHEDULED) {
            throw new GATInvocationException("Job is not in schedule state");
        }
        PbsMessage res = mBroker.unScheduleJob(mId);
        if (!res.isDeleted()) {
            throw new GATInvocationException(res.getMessage());
        }
        state = INITIAL;
    }
    */
}
