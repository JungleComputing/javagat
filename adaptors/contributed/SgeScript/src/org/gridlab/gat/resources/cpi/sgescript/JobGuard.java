/*
 * MPA Source File: JobGuard.java Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:	10/4/04 (3:19:36 PM) by doerl $ Last Change: 1/14/08 (1:42:38 PM) by doerl
 * $Packaged to JavaGat: 18/07/08 by Alexander Beck-Ratzka, AEI.
 *
 */

package org.gridlab.gat.resources.cpi.sgescript;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobGuard extends Thread {
	private static final Logger LOGGER = Logger.getLogger( JobGuard.class.getName());
	private static final String DEBUG_LEVEL = System.getProperty( "planck.sge.adaptor.guard.debug", "OFF");
	private static final long DEFAULT_TIME_SLEEP = 300; // 300ms
	private static final long DEFAULT_FACTOR_CHECK = 10; // 10 * DEFAULT_TIME_SLEEP = 3s
	private static final long DEFAULT_FACTOR_DELETE = 600; // 600 * DEFAULT_TIME_SLEEP = 3min
	private static final long TIME_SLEEP = Long.getLong( "planck.sge.adaptor.guard.sleep", DEFAULT_TIME_SLEEP).intValue();
	private static final long TIME_CHECK = Long.getLong( "planck.sge.adaptor.guard.check", DEFAULT_FACTOR_CHECK * TIME_SLEEP).intValue();
	private static final long TIME_DELETE = Long.getLong( "planck.sge.adaptor.guard.delete", DEFAULT_FACTOR_DELETE * TIME_SLEEP).intValue();
	private static final int MAX_ACCT = 5;
	static final JobGuard GUARD = new JobGuard();

	static {
		LOGGER.setLevel( Level.parse( DEBUG_LEVEL));
		Runtime.getRuntime().addShutdownHook( new Thread() {
				public void run() {
					if (GUARD.isAlive())
						GUARD.interrupt();
					Utils.log( LOGGER, Level.FINE, "[SGE Guard] shutdown hook");
				}
			});
		GUARD.start();
	}

	private List mAcct = new LinkedList();
	private int mCount;
	private boolean mIsAllSaved;
	private Map mJobs = new HashMap();

	public JobGuard() {
		this( "SGE JobQueue");
	}

	public JobGuard( String name) {
		super( name);
		setDaemon( true);
		setPriority( Thread.NORM_PRIORITY - 2);
	}

	public synchronized void addJob( Object id) {
		Utils.log( LOGGER, Level.FINE, "[SGE Guard] addJob: {0}", id);
		mJobs.put( id, new SgeResponse());
		mIsAllSaved = false;
		notifyAll();
	}

	private void checkJob( Object id) {
		if (id != null) {
			Utils.log( LOGGER, Level.FINE, "[SGE Guard] check Job: {0}", id);
			try {
				Vector params = Executer.allResults( "/qacct -j " + id);
				if (params.isEmpty())
					return;
				String first = (String) params.get( 0);
				if (first.startsWith( "error:"))
					return;
				params.remove( 0);
				setJob( id, params);
			}
			catch (IOException ex) {
				Utils.log( LOGGER, Level.WARNING, "error.sge.execute", ex);
			}
		}
	}

	private synchronized void checkTime() {
		long current = System.currentTimeMillis();
		long refresh = current - TIME_DELETE;
		long check = current - TIME_CHECK;
		mIsAllSaved = true;
		for (Iterator i = mJobs.entrySet().iterator(); i.hasNext();) {
			Entry entry = (Entry) i.next();
			Object key = entry.getKey();
			SgeResponse job = (SgeResponse) entry.getValue();
			if (job.isSaved()) {
				if (job.getLastRefresh() < refresh) {
					i.remove();
					mAcct.remove( key);
					Utils.log( LOGGER, Level.FINE, "[SGE Guard] remove Job: {0}", key);
				}
			}
			else {
				mIsAllSaved = false;
				if ((job.getLastRefresh() < check) && !mAcct.contains( key)) {
					Utils.log( LOGGER, Level.FINE, "[SGE Guard] prepair for looking Job: {0}", key);
					mAcct.add( key);
				}
			}
		}
	}

	public synchronized SgeResponse getJob( Object id) {
		SgeResponse res = (SgeResponse) mJobs.get( id);
		if (res == null)
			Utils.log( LOGGER, Level.FINE, "[SGE Guard] job not found: {0}", id);
		return res;
	}

	private synchronized void setJob( Object id, String line) {
		SgeResponse job = (SgeResponse) mJobs.get( id);
		if (job != null) {
			Utils.log( LOGGER, Level.FINE, "[SGE Guard] parse Job: {0}", id);
			job.parseLine( line);
		}
	}

	private synchronized void setJob( Object id, Vector params) {
		SgeResponse job = (SgeResponse) mJobs.get( id);
		if (job != null) {
			Utils.log( LOGGER, Level.FINE, "[SGE Guard] check Job: {0}", id);
			job.parseVector( params);
		}
	}

	private static Object parseId( String line) {
		Object id = null;
		StringTokenizer st = new StringTokenizer( line);
		try {
			id = st.nextElement();
		}
		catch (RuntimeException ex) {
			Utils.log( LOGGER, Level.WARNING, "error parsing: {0} on {1}", ex, line);
		}
		return id;
	}

	private boolean parseJobs() {
		boolean result = true;
		Utils.log( LOGGER, Level.FINE, "[SGE Guard] parse all Job");
		try {
			Vector jobs = Executer.allResults( "/qstat");
			for (int i = 2; i < jobs.size(); ++i) {
				String line = (String) jobs.get( i);
				Object id = parseId( line);
				if (id != null)
					setJob( id, line);
			}
		}
		catch (IOException ex) {
			Utils.log( LOGGER, Level.WARNING, "error execute: {0}", ex);
			result = false;
		}
		return result;
	}

	public void run() {
		try {
			while (!interrupted()) {
				Object id = null;
				synchronized (this) {
					wait( TIME_SLEEP);
					while (mIsAllSaved) {
						Utils.log( LOGGER, Level.FINE, "[SGE Guard] sleeps");
						wait( 0);
					}
					if (--mCount < 0)
						mCount = Math.min( MAX_ACCT, mAcct.size());
					else if (mAcct.size() > 0)
						id = mAcct.remove( 0);
				}
				if (id != null)
					checkJob( id);
				else if (parseJobs())
					checkTime();
			}
		}
		catch (InterruptedException ex) {
		}
	}
}
