/*
 * MPA Source File: SgeJob.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    14.10.2005 (13:03:54) by doerl $
 * Last Change: 1/14/08 (1:54:57 PM) by doerl
 * $Packaged to JavaGat: 18/07/08 by Alexander Beck-Ratzka, AEI.
 *
 */

package org.gridlab.gat.resources.cpi.sgescript;

import java.io.IOException;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

public class SgeJob extends JobCpi {
	private static final long serialVersionUID = -8405778045835933081L;
	private ABrokerAdaptor mBroker;
	private String mId;
//	private Metric mMetric;

	public SgeJob( GATContext gatContext, ABrokerAdaptor broker, JobDescription description, String id, Sandbox sandbox) {
		super( gatContext, description, sandbox);
		mBroker = broker;
		mId = id;
	}

	public void hold() throws GATInvocationException {
		if (getState() != JobState.SCHEDULED) {
			throw new GATInvocationException( "Job is not scheduling");
		}
		try {
			SgeMessage res = mBroker.holdJob( mId);
			if (!res.isModified()) {
				throw new GATInvocationException( res.getMessage());
			}
		}
		catch (IOException ex) {
			throw new GATInvocationException( "sge", ex);
		}
	}

	public Map<String, Object> getInfo() throws GATInvocationException {
		Map<String, Object> result = mBroker.getInfo( mId);
		result.put("adaptor.job.id", mId);
		result.put( "state", state);
		return result;
	}

        public int getExitStatus() throws GATInvocationException  {
            Map result =  this.getInfo();
            int rc  = Integer.parseInt(result.get("exitValue").toString());     
            return rc;
        }

        /**
         * getJobId only returns integers in Release 2.0. Use Map info = job.getInfo(); and info.get("adaptor.job.id"); instead.
         */
/*	public String getJobID() throws GATInvocationException {
		return mId;
	}*/

	public String marshal() {
		throw new Error( "Not implemented");
	}

	public void release() throws GATInvocationException {
		if (getState() != JobState.INITIAL) {
			throw new GATInvocationException( "Job is not holding");
		}
		try {
			SgeMessage res = mBroker.releaseJob( mId);
			if (!res.isModified()) {
				throw new GATInvocationException( res.getMessage());
			}
		}
		catch (IOException ex) {
			throw new GATInvocationException( "sge", ex);
		}
	}

    public JobState getState() {
        synchronized (this) {
            state = mBroker.getState(mId);
        }
        return state;
    }
	public void stop() throws GATInvocationException {
		if (getState() != JobState.RUNNING) {
			throw new GATInvocationException( "Job is not running");
		}
		try {
			SgeMessage res = mBroker.cancelJob( mId);
			if (!res.isDeleted()) {
				throw new GATInvocationException( res.getMessage());
			}
		}
		catch (IOException ex) {
			throw new GATInvocationException( "sge", ex);
		}
	}
}
