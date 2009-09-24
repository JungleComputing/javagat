/*
 * MPA Source File: SgeResponse.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    14.10.2005 (13:52:12) by doerl $
 * Last Change: 1/14/08 (1:43:02 PM) by doerl
 * $Packaged to JavaGat: 18/07/08 by Alexander Beck-Ratzka, AEI.
 *
 */

package org.gridlab.gat.resources.cpi.sgescript;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gridlab.gat.resources.Job;

class SgeResponse {
	private static final Logger LOGGER = Logger.getLogger( SgeResponse.class.getName());
	private Map mInfo = new HashMap();
	private boolean mIsSaved;
	private long mLastRefresh;
	private String mState;

	SgeResponse() {
	}

	synchronized Map getInfo() {
		return new HashMap( mInfo);
	}

	synchronized long getLastRefresh() {
		return mLastRefresh;
	}

	synchronized void parseLine( String msg) {
		mLastRefresh = System.currentTimeMillis();
		StringTokenizer st = new StringTokenizer( msg);
		try {
			st.nextElement(); // ignore id
			mInfo.put( "priority", st.nextElement());
			mInfo.put( "jobname", st.nextElement());
			mInfo.put( "owner", st.nextElement());
			mState = (String) st.nextElement();
			StringBuffer sb = new StringBuffer();
			sb.append( st.nextElement());
			sb.append( " ");
			sb.append( st.nextElement());
			mInfo.put( "starttime", sb.toString());
			if (st.hasMoreTokens()) {
				mInfo.put( "qname", st.nextElement());
			}
			if (st.hasMoreTokens()) {
				mInfo.put( "master", st.nextElement());
			}
			if (st.hasMoreTokens()) {
				mInfo.put( "jaid", st.nextElement());
			}
		}
		catch (RuntimeException ex) {
			Utils.log( LOGGER, Level.WARNING, "error parse: {0} on {1}", ex, msg);
		}
	}

	synchronized void parseVector( Vector params) {
		mLastRefresh = System.currentTimeMillis();
		mIsSaved = true;
		mState = "_";
		try {
			for (Enumeration elem = params.elements(); elem.hasMoreElements();) {
				String line = (String) elem.nextElement();
				int pos = line.indexOf( ' ');
				if (pos >= 0) {
					String key = line.substring( 0, pos).trim();
					String value = line.substring( pos + 1).trim();
					if ("priority".equals( key)) {
						mInfo.put( "priority", value);
					}
					else if ("jobname".equals( key)) {
						mInfo.put( "jobname", value);
					}
					else if ("owner".equals( key)) {
						mInfo.put( "owner", value);
					}
					else if ("qname".equals( key)) {
						mInfo.put( "qname", value);
					}
					else if ("submission_time".equals( key)) {
						mInfo.put( "submissiontime", value);
					}
					else if ("qsub_time".equals( key)) {
						mInfo.put( "submissiontime", value);
					}
					else if ("start_time".equals( key)) {
						mInfo.put( "starttime", value);
					}
					else if ("end_time".equals( key)) {
						mInfo.put( "stoptime", value);
					}
					else if ("exit_status".equals( key)) {
						mInfo.put( "exitValue", value);
					}
				}
			}
		}
		catch (RuntimeException ex) {
			Utils.log( LOGGER, Level.WARNING, "error parse: {0} on map", ex);
		}
	}

	synchronized boolean isSaved() {
		return mIsSaved;
	}

	synchronized Job.JobState getState() {
		if (mState == null) {
			Utils.log( LOGGER, Level.FINEST, "error state");
			return Job.JobState.SCHEDULED;
		}
		if (mState.indexOf( 'E') >= 0) { // Error
			return Job.JobState.SUBMISSION_ERROR;
		}
		if (mState.indexOf( 'r') >= 0) { // running
			return Job.JobState.RUNNING;
		}
		if (mState.indexOf( 't') >= 0) { // transfering
			return Job.JobState.SCHEDULED;
		}
		if (mState.indexOf( 's') >= 0) { // suspended
			return Job.JobState.SCHEDULED;
		}
		if (mState.indexOf( 'S') >= 0) { // Suspended
			return Job.JobState.SCHEDULED;
		}
		if (mState.indexOf( 'd') >= 0) { // deletion
			return Job.JobState.SCHEDULED;
		}
		if (mState.indexOf( 'T') >= 0) { // Threshold
			return Job.JobState.SCHEDULED;
		}
		if (mState.indexOf( 'x') >= 0) { // exited
			return Job.JobState.SCHEDULED;
		}
		if (mState.indexOf( 'h') >= 0) { // hold
			return Job.JobState.INITIAL;
		}
		if (mState.indexOf( 'w') >= 0) { // waiting
			return Job.JobState.INITIAL;
		}
		if (mState.indexOf( 'R') >= 0) { // Restarted
			return Job.JobState.INITIAL;
		}
		if (mState.indexOf( '_') >= 0) { //
			return Job.JobState.STOPPED;
		}
		return Job.JobState.SCHEDULED;
	}
}
