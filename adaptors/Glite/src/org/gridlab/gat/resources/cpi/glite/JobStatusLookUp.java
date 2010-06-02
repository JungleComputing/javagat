package org.gridlab.gat.resources.cpi.glite;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.engine.util.ScheduledExecutor;
import org.gridlab.gat.resources.Job;

class JobStatusLookUp implements Runnable {
	private GliteJobInterface polledJob;
	private int pollIntMilliSec;
	private long afterJobKillCounter = 0;

	/**
	 * if the job has been stopped, allow the thread to still do update for
	 * this time interval terminating
	 */
	final static long UPDATE_INTV_AFTER_JOB_KILL = 40000;

	public JobStatusLookUp(final GliteJobInterface job, GATContext gatContext) {
		this.polledJob = job;

		String pollingIntervalStr = (String) gatContext.getPreferences().get(GliteConstants.PREFERENCE_POLL_INTERVAL_SECS);

		if (pollingIntervalStr == null) {
			this.pollIntMilliSec = 30000;
		} else {
			this.pollIntMilliSec = Integer.parseInt(pollingIntervalStr) * 1000;
		}
		//update at least properly the status 1 time before returning to the client the job.
		this.run();
		ScheduledExecutor.schedule(this, pollIntMilliSec, pollIntMilliSec);
	}

	public void run() {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		if (polledJob.getState() == Job.JobState.STOPPED || polledJob.getState() == Job.JobState.SUBMISSION_ERROR) {
			ScheduledExecutor.remove(this);
			return;
		}

		// if the job has been killed and the maximum time at which the
		// job should be canceled
		// has been reached, cancel
		if (polledJob.isJobKilled()) {
			afterJobKillCounter += pollIntMilliSec;

			if (afterJobKillCounter >= UPDATE_INTV_AFTER_JOB_KILL) {
				ScheduledExecutor.remove(this);
				return;
			}
		}

		polledJob.updateState();

		if (polledJob.getState() == Job.JobState.POST_STAGING) {
			polledJob.receiveOutput();
		}
	}
}
