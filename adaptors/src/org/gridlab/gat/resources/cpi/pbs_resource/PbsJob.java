/*
 * MPA Source File: SgeJob.java Copyright (c) 2003-2005 by MPA Garching
 *
 * $Created:	14.10.2005 (13:03:54) by doerl $ Last Change: 14.10.2005 (13:03:54) by doerl
 */
package org.gridlab.gat.resources.cpi.pbs_resource;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;

/**
 * @author  doerl
 */
public class PbsJob extends Job {
    private static final long serialVersionUID = -5229286816606473700L;
    private PbsBrokerAdaptor mBroker;
    private JobDescription mDescription;
    private String mId;
    //	private Metric mMetric;

    public PbsJob(PbsBrokerAdaptor broker, JobDescription description, String id) {
        mBroker = broker;
        mDescription = description;
        mId = id;
        state = INITIAL;
    }

    public Map getInfo() throws GATInvocationException, IOException {
        Map result = mBroker.getInfo(mId);
        result.put("state", getStateString(getState()));
        for (Iterator i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            try {
                if (((String) key).startsWith("PBS_O_")) {
                    i.remove();
                }
            }
            catch (ClassCastException ex) {
            }
        }
        Object obj = result.remove("submission_time");
        if (obj != null) {
            result.put("submissiontime", obj);
        }
        obj = result.remove("qsub_time");
        if (obj != null) {
            result.put("submissiontime", obj);
        }
        obj = result.remove("start_time");
        if (obj != null) {
            result.put("starttime", obj);
        }
        obj = result.remove("end_time");
        if (obj != null) {
            result.put("stoptime", obj);
        }
        obj = result.remove("exit_status");
        if (obj != null) {
            result.put("exitValue", obj);
        }
        return result;
    }

    public JobDescription getJobDescription() {
        return mDescription;
    }

    public String getJobID() throws GATInvocationException, IOException {
        return mId;
    }

    public int getState() throws GATInvocationException, IOException {
        synchronized (this) {
            state = mBroker.getState(mId);
        }
        return state;
    }

    public String marshal() {
        throw new Error("Not implemented");
    }

    public void stop() throws GATInvocationException, IOException {
        if (getState() != RUNNING) {
            throw new GATInvocationException("Job is not running");
        }
        PbsMessage res = mBroker.cancelJob(mId);
        if (!res.isDeleted()) {
            throw new GATInvocationException(res.getMessage());
        }
        state = INITIAL;
    }

    public void hold() throws GATInvocationException, IOException {
        if (getState() != RUNNING) {
            throw new GATInvocationException("Job is not running");
        }
        //        PbsMessage res = mBroker.holdJob(mId);
//         if (!res.isDeleted()) {
//             throw new GATInvocationException(res.getMessage());
//         }
        state = INITIAL;
    }

    public void release() throws GATInvocationException, IOException {
        if (getState() != RUNNING) {
            throw new GATInvocationException("Job is not running");
        }
        //        PbsMessage res = mBroker.releaseJob(mId);
//         if (!res.isDeleted()) {
//             throw new GATInvocationException(res.getMessage());
//         }
        state = INITIAL;
    }

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
}
