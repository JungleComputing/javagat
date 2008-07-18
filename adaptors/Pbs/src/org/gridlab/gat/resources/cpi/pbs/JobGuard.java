/*
 * MPA Source File: JobGuard.java Copyright (c) 2003-2007 by MPA Garching
 *
 * $Created:	10/4/04 (3:19:36 PM) by doerl $ Last Change: 9/10/07 (4:57:57 PM) by doerl
 * $Packaged to JavaGat: 16/07/08 by Alexander Beck-Ratzka, AEI.
 */

package org.gridlab.gat.resources.cpi.pbs;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobGuard extends Thread {
	private static final Logger LOGGER = Logger.getLogger( JobGuard.class.getName());
	private static final String DEBUG_LEVEL = System.getProperty( "planck.pbs.adaptor.guard.debug", "OFF");
	private static final long DEFAULT_NORM_SLEEP = 300; // 300ms
	private static final long DEFAULT_FACTOR_PASSIV = 5; // 3 * DEFAULT_TIME_SLEEP = 1500ms
	private static final long DEFAULT_FACTOR_CHECK = 30; // 30 * DEFAULT_TIME_SLEEP = 9s
	private static final long DEFAULT_FACTOR_DELETE = 600; // 600 * DEFAULT_TIME_SLEEP = 3min
	private static final long DEFAULT_FACTOR_LONG = 2000; // 2000 * DEFAULT_TIME_SLEEP = 10min
	private static final long TIME_SLEEP = Long.getLong( "planck.pbs.adaptor.guard.sleep", DEFAULT_NORM_SLEEP).intValue();
	private static final long TIME_CHECK = Long.getLong( "planck.pbs.adaptor.guard.check", DEFAULT_FACTOR_CHECK * TIME_SLEEP).intValue();
	private static final long TIME_DELETE = Long.getLong( "planck.pbs.adaptor.guard.delete", DEFAULT_FACTOR_DELETE * TIME_SLEEP).intValue();
	private static final long LONG_SLEEP = Long.getLong( "planck.pbs.adaptor.guard.long", DEFAULT_FACTOR_LONG * TIME_SLEEP).intValue();
	private static long sNormSleep = TIME_SLEEP;
	static final JobGuard GUARD = new JobGuard();

	static {
		LOGGER.setLevel( Level.parse( DEBUG_LEVEL));
		GUARD.start();
		Utils.log( LOGGER, Level.FINE, "[PBS Guard] is running now");
	}

	private Vector mJobs = new Vector();

	public JobGuard() {
		super( "PBS JobQueue");
		setDaemon( true);
		setPriority( Thread.NORM_PRIORITY - 1);
	}

	public synchronized void addJob( String id) {
		Utils.log( LOGGER, Level.FINE, "[PBS Guard] addJob: {0}", id);
		mJobs.add( new PbsResponse( id));
		notifyAll();
	}

	private synchronized void checkTime() {
		long current = System.currentTimeMillis();
		long refresh = current - TIME_DELETE;
		long check = current - TIME_CHECK;
		sNormSleep = DEFAULT_FACTOR_PASSIV * TIME_SLEEP;
		for (Iterator i = mJobs.iterator(); i.hasNext();) {
			PbsResponse job = (PbsResponse) i.next();
			if (job.getLastRefresh() < refresh) {
				i.remove();
				Utils.log( LOGGER, Level.FINE, "[PBS Guard] remove Job: {0}", job.getId());
			}
			else if (job.getLastRefresh() < check) {
				Utils.log( LOGGER, Level.FINE, "[PBS Guard] set stopped Job: {0}", job.getId());
				job.setStopped();
			}
			else
				sNormSleep = TIME_SLEEP;
		}
	}

	public synchronized PbsResponse getJob( String id) {
		for (Iterator i = mJobs.iterator(); i.hasNext();) {
			PbsResponse job = (PbsResponse) i.next();
			if (job.getId().startsWith( id))
				return job;
		}
		return null;
	}

	private synchronized void setJob( String id, String line) {
		PbsResponse job = (PbsResponse) getJob( id);
		if (job != null) {
			Utils.log( LOGGER, Level.FINE, "[PBS Guard] parse Job: {0}", id);
			job.parseLine( line);
		}
	}

	private static String parseId( String line) {
		String id = null;
		StringTokenizer st = new StringTokenizer( line);
		try {
			id = (String) st.nextElement();
		}
		catch (RuntimeException ex) {
			Utils.log( LOGGER, Level.WARNING, "error parsing: {0} on {1}", ex, line);
		}
		return id;
	}

	private boolean parseJobs() {
		boolean result = true;
		Utils.log( LOGGER, Level.FINE, "[PBS Guard] parse all Jobs");
		try {
			Vector jobs = Executer.allResults( "qstat");
			for (int i = 2; i < jobs.size(); ++i) {
				String line = (String) jobs.get( i);
				String id = parseId( line);
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
				synchronized (this) {
					wait( sNormSleep);
					while (mJobs.isEmpty()) {
						Utils.log( LOGGER, Level.FINE, "[PBS Guard] sleeps");
						wait( LONG_SLEEP);
					}
				}
				if (parseJobs())
					checkTime();
			}
		}
		catch (InterruptedException ex) {
		}
	}
}
