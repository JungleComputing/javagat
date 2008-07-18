/*
 * MPA Source File: PbsJob.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    14.10.2005 (13:03:54) by doerl $
 * Last Change: 1/14/08 (2:29:20 PM) by doerl
 * $Packaged to JavaGat: 16/07/08 by Alexander Beck-Ratzka, AEI.
 */

package org.gridlab.gat.resources.cpi.pbs;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

public class PbsJob extends JobCpi {
	private static final long serialVersionUID = -5229286816606473700L;
	private ABrokerAdaptor mBroker;
	private String mId;

	public PbsJob( GATContext gatContext, ABrokerAdaptor broker, JobDescription description, String id, Sandbox sandbox) {
		super( gatContext, description, sandbox);
		mBroker = broker;
		mId = id;
	}

	public void hold() throws GATInvocationException {
		if (getState() != RUNNING) {
			throw new GATInvocationException( "Job is not running");
		}
		try {
			PbsMessage res = mBroker.holdJob( mId);
			if (!res.isDeleted()) {
				throw new GATInvocationException( res.getMessage());
			}
		}
		catch (IOException ex) {
			throw new GATInvocationException( "pbs", ex);
		}
	}

	public Map getInfo() throws GATInvocationException {
		Map result = mBroker.getInfo( mId);
		result.put( "state", getStateString( getState()));
		for (Iterator i = result.keySet().iterator(); i.hasNext();) {
			Object key = i.next();
			try {
				if (((String) key).startsWith( "PBS_O_")) {
					i.remove();
				}
			}
			catch (ClassCastException ex) {
			}
		}
		Object obj = result.remove( "submission_time");
		if (obj != null) {
			result.put( "submissiontime", obj);
		}
		obj = result.remove( "qsub_time");
		if (obj != null) {
			result.put( "submissiontime", obj);
		}
		obj = result.remove( "start_time");
		if (obj != null) {
			result.put( "starttime", obj);
		}
		obj = result.remove( "end_time");
		if (obj != null) {
			result.put( "stoptime", obj);
		}
		obj = result.remove( "exit_status");
		if (obj != null) {
			result.put( "exitValue", obj);
		}
		return result;
	}
    
    public int getExitStatus() throws GATInvocationException  {
        Map result =  mBroker.getInfo(mId);
        int aa = (Integer) result.get("exit_status");
        
        return aa;
    }


	public String getJobID() throws GATInvocationException {
		return mId;
	}

	public String marshal() {
		throw new Error( "Not implemented");
	}

	public void release() throws GATInvocationException {
		if (getState() != RUNNING) {
			throw new GATInvocationException( "Job is not running");
		}
		try {
			PbsMessage res = mBroker.releaseJob( mId);
			if (!res.isDeleted()) {
				throw new GATInvocationException( res.getMessage());
			}
		}
		catch (IOException ex) {
			throw new GATInvocationException( "pbs", ex);
		}
		state = INITIAL;
	}

	public synchronized int getState() {
		return mBroker.getState( mId);
	}

	public void stop() throws GATInvocationException {
		if (getState() != RUNNING) {
			throw new GATInvocationException( "Job is not running");
		}
		try {
			PbsMessage res = mBroker.cancelJob( mId);
			if (!res.isDeleted()) {
				throw new GATInvocationException( res.getMessage());
			}
		}
		catch (IOException ex) {
			throw new GATInvocationException( "pbs", ex);
		}
	}
}
