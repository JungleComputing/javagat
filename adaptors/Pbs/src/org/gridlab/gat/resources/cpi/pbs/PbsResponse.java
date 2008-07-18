/*
 * MPA Source File: PbsResponse.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    14.10.2005 (13:52:12) by doerl $
 * Last Change: 1/14/08 (2:38:56 PM) by doerl
 * $Packaged to JavaGat: 16/07/08 by Alexander Beck-Ratzka, AEI.
 */

package org.gridlab.gat.resources.cpi.pbs;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gridlab.gat.resources.Job;

class PbsResponse implements Serializable {
	private static final long serialVersionUID = 3490943463736753482L;
	private static final Logger LOGGER = Logger.getLogger( PbsResponse.class.getName());
	private String mId;
	private Map mInfo = new HashMap();
	private long mLastRefresh;
	private String mState;

	PbsResponse( String id) {
		mId = id;
	}

	public String getId() {
		return mId;
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
			mInfo.put( "jobname", st.nextElement());
			mInfo.put( "owner", st.nextElement());
			mInfo.put( "starttime", st.nextElement());
			mState = (String) st.nextElement();
			if (st.hasMoreTokens()) {
				mInfo.put( "qname", st.nextElement());
			}
		}
		catch (RuntimeException ex) {
			Utils.log( LOGGER, Level.WARNING, "error parse: {0} on {1}", ex, msg);
		}
	}

	synchronized void parseVector( Vector params) {
		mLastRefresh = System.currentTimeMillis();
		mState = "_";
		try {
			String lastKey = null;
			for (Enumeration elem = params.elements(); elem.hasMoreElements();) {
				String line = (String) elem.nextElement();
				if (line.length() == 0) {
					continue;
				}
				if (!line.startsWith( "\t")) {
					int pos = line.indexOf( '=');
					if (pos >= 0) {
						String key = line.substring( 0, pos).trim();
						String value = line.substring( pos + 1).trim();
						if ("Job_Name".equals( key)) {
							mInfo.put( "jobname", value);
						}
						else if ("Job_Owner".equals( key)) {
							mInfo.put( "owner", value);
						}
						else if ("queue".equals( key)) {
							mInfo.put( "qname", value);
						}
						else if ("ctime".equals( key)) {
							mInfo.put( "submissiontime", value);
						}
						else if ("etime".equals( key)) {
							mInfo.put( "starttime", value);
						}
						else {
							mInfo.put( key, value);
						}
					}
				}
				else if (lastKey != null) {
					String lastVal = (String) mInfo.get( lastKey);
					mInfo.put( lastKey, lastVal + line.trim());
				}
			}
		}
		catch (RuntimeException ex) {
			Utils.log( LOGGER, Level.WARNING, "error parse: {0} on map", ex);
		}
	}

	synchronized int getState() {
		if (mState == null) {
			Utils.log( LOGGER, Level.FINEST, "error state");
			return Job.SCHEDULED;
		}
		if (mState.indexOf( 'W') >= 0) { // waiting
			return Job.SCHEDULED;
		}
		if (mState.indexOf( 'Q') >= 0) { // queued
			return Job.SCHEDULED;
		}
		if (mState.indexOf( 'T') >= 0) { // transition
			return Job.SCHEDULED;
		}
//		if (mState.indexOf('H') >= 0) { // hold
//			return Job.HOLD;
//		}
		if (mState.indexOf( 'S') >= 0) { // suspended
			return Job.RUNNING;
		}
		if (mState.indexOf( 'R') >= 0) { // running
			return Job.RUNNING;
		}
		if (mState.indexOf( 'E') >= 0) { // exiting
			return Job.STOPPED;
		}
		return Job.STOPPED;
	}

	synchronized void setStopped() {
		mState = "";
	}
}
