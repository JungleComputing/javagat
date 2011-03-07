package org.gridlab.gat.resources.cpi.unicore6;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

import eu.unicore.hila.Location;
import eu.unicore.hila.exceptions.HiLAException;
import eu.unicore.hila.grid.Job;
import eu.unicore.hila.grid.Site;
import eu.unicore.hila.grid.TaskStatus;

/**
 * Job class for Unicore 6
 * 
 * @author Andreas Bender
 * 
 */
public class Unicore6Job extends JobCpi {

	private static final long serialVersionUID = 4207105584275137771L;

	/**
	 * the global boolean b_PostStage is used for marking the poststage for a job having taken place. This is necessary
	 * to avoid a secondary poststaging which will occur with offline monitoring, because the unmarschalling and the
	 * getInfo method try to poststage.
	 */

	private boolean b_PostStage = false;

	private String jobID;
	private String hostname;
	private MetricDefinition statusMetricDefinition;
	private Metric statusMetric;
	private Site site;
	private Job job;
	private SoftwareDescription softwareDescription;

	{
		starttime = -1L;
		stoptime = -1L;
		submissiontime = -1L;
	}

	/**
	 * constructor of UnicoreJob
	 * 
	 * @param gatContext The gatContext
	 * @param jobDescription
	 * @param sandbox
	 */
	protected Unicore6Job(GATContext gatContext, JobDescription jobDescription, Sandbox sandbox) {
		super(gatContext, jobDescription, sandbox);

		state = JobState.INITIAL;

		HashMap<String, Object> returnDef = new HashMap<String, Object>();
		returnDef.put("status", JobState.class);
		statusMetricDefinition = new MetricDefinition("job.status", MetricDefinition.DISCRETE, "String", null, null,
				returnDef);
		setStatusMetric(statusMetricDefinition.createMetric(null));
		registerMetric("getJobStatus", statusMetricDefinition);
	}

	/**
	 * Constructor for unmarshalled jobs.
	 */
	private Unicore6Job(GATContext gatContext, SerializedUnicoreJob serializedJob) throws GATObjectCreationException {
		super(gatContext, serializedJob.getJobDescription(), serializedJob.getSandbox());
		if (sandbox != null) {
			sandbox.setContext(gatContext);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("reconstructing UnicoreJob: " + serializedJob);
		}

		this.starttime = serializedJob.getStarttime();
		this.stoptime = serializedJob.getStoptime();
		this.submissiontime = serializedJob.getSubmissiontime();

		// Set the context classloader to the classloader that loaded the
		// UnicoreJob
		// class, otherwise the HiLA libraries will not find any HiLA
		// implementations.
		// --Ceriel
		ClassLoader saved = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		try {
			// Note: the job id used here is actually a Location.

			this.job = (Job) new Location(serializedJob.getJobId()).locate();
			this.jobID = this.job.getId();
		} catch (Throwable e) {
			throw new GATObjectCreationException("Got exception from HiLA: ", e);
		} finally {
			Thread.currentThread().setContextClassLoader(saved);
		}
		if (this.job == null) {
			throw new GATObjectCreationException("Unmarshalling UnicoreJob: job = null");
		}

		// reconstruct enough of the software description to be able to
		// poststage.
		softwareDescription = new SoftwareDescription();
		String s = serializedJob.getStdout();
		if (s != null) {
			softwareDescription.setStdout(GAT.createFile(gatContext, s));
		}
		s = serializedJob.getStderr();
		if (s != null) {
			softwareDescription.setStderr(GAT.createFile(gatContext, s));
		}

		String[] toStageOut = serializedJob.getToStageOut();
		String[] stagedOut = serializedJob.getStagedOut();
		if (toStageOut != null) {
			for (int i = 0; i < toStageOut.length; i++) {
				softwareDescription.addPostStagedFile(GAT.createFile(gatContext, toStageOut[i]),
						GAT.createFile(gatContext, stagedOut[i]));
			}
		}
	}

	/*
	 * @see org.gridlab.gat.advert.Advertisable#marshal()
	 */
	public String marshal() {
		SerializedUnicoreJob sj;
		synchronized (this) {

			// Wait until initial stages are passed.
			while (state == JobState.INITIAL || state == JobState.PRE_STAGING) {
				// || state == JobState.SCHEDULED) {
				try {
					wait();
				} catch (Exception e) {
					// ignore
				}
			}

			sj = new SerializedUnicoreJob(getClass().getName(), jobDescription, sandbox, job.getLocation().toString(),
					submissiontime, starttime, stoptime, softwareDescription);

		}
		String res = GATEngine.defaultMarshal(sj);
		if (logger.isDebugEnabled()) {
			logger.debug("marshalled seralized job: " + res);
		}
		return res;
	}

	/**
	 * Unmarshalls a serialized Unicore Job.
	 * 
	 * @param context
	 * @param s
	 * @return the unmarshalled unicore job
	 * @throws GATObjectCreationException
	 */
	public static Advertisable unmarshal(GATContext context, String s) throws GATObjectCreationException {
		if (logger.isDebugEnabled()) {
			logger.debug("unmarshalled seralized job: " + s);
		}

		SerializedUnicoreJob sj = (SerializedUnicoreJob) GATEngine.defaultUnmarshal(SerializedUnicoreJob.class, s);

		// if this job was created within this JVM, just return a reference to
		// the job
		synchronized (JobCpi.class) {
			for (int i = 0; i < jobList.size(); i++) {
				JobCpi j = (JobCpi) jobList.get(i);
				if (j instanceof Unicore6Job) {
					Unicore6Job gj = (Unicore6Job) j;
					if (sj.getJobId().equals(gj.job.getLocation().toString())) {
						if (logger.isDebugEnabled()) {
							logger.debug("returning existing job: " + gj);
						}
						return gj;
					}
				}
			}
		}
		return new Unicore6Job(context, sj);
	}

	/**
	 * Refreshes the state of the job by calling HiLA.
	 */
	protected synchronized void refreshState() {
		JobState oldState = state;// TODO delete?
		logger.debug("Getting job status in refreshState()");

		try {
			TaskStatus status = job.status();
			logger.debug("Job status in refreshState(): " + status.toString());

			if (status.equals(TaskStatus.RUNNING)) {
				if (starttime == -1L) {
					setStartTime();
				}
				state = JobState.RUNNING;
			} else if (status.equals(TaskStatus.FAILED)) {
				state = JobState.SUBMISSION_ERROR;
			} else if (status.equals(TaskStatus.ABORTED)) {
				if (stoptime == -1L) {
					setStopTime();
				}
				state = JobState.ABORTED;
			} else if (status.equals(TaskStatus.SUCCESSFUL)) {
				if (stoptime == -1L) {
					setStopTime();
				}
				state = JobState.DONE_SUCCESS;
			} else if (status.equals(TaskStatus.NEW)) {
				state = JobState.READY;
			} else if (status.equals(TaskStatus.PENDING)) {
				state = JobState.SCHEDULED;
			}

		} catch (HiLAException e) {
			if (logger.isDebugEnabled()) {
				logger.error("-- UNICOREJob EXCEPTION --");
				logger.error("Got an exception while retrieving resource manager status:");
				logger.error("", e);
			}
		}

		if (state != oldState) {// TODO delete?
			notifyAll();
		}
	}

	/**
	 * @see JobCpi#stop()
	 */
	public synchronized void stop() throws GATInvocationException {
		if ((getState() != JobState.RUNNING) && (getState() != JobState.ON_HOLD) && (getState() != JobState.SCHEDULED)) {
			throw new GATInvocationException("Cant stop(): job is not in a running state");
		} else {
			try {
				job.abort();
				state = JobState.STOPPED;
				if (stoptime == -1L) {
					stoptime = System.currentTimeMillis();
				}
				logger.debug("Unicore Job " + job.getId() + " stopped by user");
			} catch (HiLAException e) {
				if (logger.isDebugEnabled()) {
					logger.error("-- UnicoreJob EXCEPTION --");
					logger.error("Got an exception while trying to TERMINATE job:");
					logger.error("", e);
				}
				throw new GATInvocationException("Can't stop Unicore Job" + jobID, e);
			}
		}
	}

	private synchronized void poststageFiles(SoftwareDescription sd, Job job) throws GATInvocationException {

		if (b_PostStage) {// FIXME delete?
			logger.debug("no post staging. b_PostStage = " + b_PostStage);
			return;
		}

		eu.unicore.hila.grid.File remoteStdoutFile = null;
		eu.unicore.hila.grid.File remoteStderrFile = null;

		Map<org.gridlab.gat.io.File, org.gridlab.gat.io.File> postStaged = null;

		try {

			/**
			 * poststage stderr and stdout, if desired...
			 */

			// File stdout = sd.getStdout();
			// if (stdout != null) {
			// stdoutFileName = stdout.getAbsolutePath();
			// logger.debug("stdout: " + stdoutFileName);
			// }
			// File stderr = sd.getStderr();
			// if (stderr != null) {
			// stderrFileName = sd.getStderr().getAbsolutePath();
			// logger.debug("stderr: " + stderrFileName);
			// }

			// Why make file objects if you only use the AbsolutePath? --Ceriel
			// if (localStdoutFile != null) {

			// FIXME try sd.getStdout()/err() to test if user wants to get it

			// System.out.println("DEBUGINFO !!!:");
			//
			// System.out.println(sd.getStdout());
			// if (sd.getStdout() != null) {
			// System.out.println(sd.getStdout().getName());
			// System.out.println(sd.getStdout().getAbsolutePath());
			// System.out.println(sd.getStdout().getPath());
			// }
			//
			// System.out.println(job.getStdOut());
			// System.out.println(job.getStdOut().getName());

			remoteStdoutFile = job.getStdOut();
			if (remoteStdoutFile != null) {
				File outputFile = new File(remoteStdoutFile.getName());
				remoteStdoutFile.exportToLocalFile(new java.io.File("C:\\export\\" + outputFile.getName() + ".txt"),
						true).block();// (outputFile,
										// true);// .block;
				// TODO?
				// moveFile(remoteStdoutFile.getAbsolutePath(), stdoutFileName);
			}
			// }
			// if (localStderrFile != null) {
			remoteStderrFile = job.getStdErr();
			if (remoteStderrFile != null) {
				File outputFile = new File(remoteStderrFile.getName());
				remoteStderrFile.exportToLocalFile(new java.io.File("C:\\export\\" + outputFile.getName() + ".txt"),
						true).block();// exportToLocalFile(outputFile,
										// true);// .block;
										// FIXME
				// TODO?
				// moveFile(remoteStderrFile.getAbsolutePath(), stderrFileName);
			}
			// }

			postStaged = sd.getPostStaged();

			if (postStaged != null) {
				eu.unicore.hila.grid.File wd = job.getWorkingDirectory();
				List<eu.unicore.hila.grid.File> wdFiles = wd.ls();
				for (eu.unicore.hila.grid.File file : wdFiles) {

					for (java.io.File srcFile : postStaged.keySet()) {
						if (file.getName().equals(srcFile.getName())) {

							java.io.File destFile = postStaged.get(srcFile);

							logger.debug("PoststageFiles: srcFile: '" + srcFile.getName()
									+ "' destFile (name , path): '" + destFile.getName() + "', "
									+ SerializedUnicoreJob.realPath(((org.gridlab.gat.io.File) destFile).toGATURI())
									+ "'");

							java.io.File realDestFile = new java.io.File(
									SerializedUnicoreJob.realPath(((org.gridlab.gat.io.File) destFile).toGATURI()));
							file.exportToLocalFile(new java.io.File("C:\\export\\" + realDestFile.getName() + ".txt"),
									true).block();// (realDestFile,
													// true).block();FIXME
						}
					}
				}
			}
		} catch (HiLAException e) {
			// e.printStackTrace();
			throw new GATInvocationException("UNICORE Adaptor: loading Storage for poststaging failed", e);
		}

		/**
		 * PostStage was successful. Set the b_PostStage boolean to true...
		 */

		b_PostStage = true;// FIXME delete?
	}

	public int getExitStatus() throws GATInvocationException {

		/**
		 * retrieve the exit status from the file UNICORE_SCRIPT_EXIT_CODE which must be poststaged from the execution
		 * host first
		 * 
		 * That's the way of how to do it within HiLA
		 */

		int rc = -1;

		try {
			rc = job.getExitCode();
		} catch (HiLAException e) {
			// e.printStackTrace();
			rc = -1;
			throw new GATInvocationException("UNICORE Adaptor: loading Storage for poststaging exit value file failed",
					e);
		}
		return rc;
	}

	/**
	 * Get some additional job informations like start/stop time etc.
	 */

	public Map<String, Object> getInfo() throws GATInvocationException {
		HashMap<String, Object> map = new HashMap<String, Object>();
		refreshState();
		try {
			map.put("jobID", jobID);
			map.put("adaptor.job.id", job.getId());
			// map.put("hostname", hostname);
			map.put("submissiontime", submissiontime);
			map.put("starttime", starttime);
			map.put("stoptime", stoptime);
			map.put("state", state.toString());
			// if (state == JobState.INITIAL || state == JobState.UNKNOWN || state == JobState.SCHEDULED) {
			// map.put("starttime", -1L);
			// } else {
			// map.put("starttime", starttime);
			// }
			// if (state != JobState.STOPPED) {
			// map.put("stoptime", -1L);
			// } else {
			// map.put("stoptime", stoptime);
			// // poststageFiles(softwareDescription, job);//TODO
			// }

		} catch (HiLAException e) {
			logger.error("HilaException in getInfo()");
			// e.printStackTrace();
			throw new GATInvocationException("HilaException in getInfo()", e);
		}

		return map;
	}

	protected void setSite(Site site) {
		this.site = site;
	}

	protected Site getSite() {
		return site;
	}

	protected void setStatusMetric(Metric statusMetric) {
		this.statusMetric = statusMetric;
	}

	protected Metric getStatusMetric() {
		return statusMetric;
	}

	protected void setHostname(String hostname) {
		this.hostname = hostname;
	}

	protected void setJob(Job job) {
		this.job = job;
	}

	protected void setSoftwareDescription(SoftwareDescription softDescr) {
		this.softwareDescription = softDescr;
	}

	protected synchronized void setJobID(String jobID) {
		this.jobID = jobID;
		notifyAll();
	}

	protected synchronized void setState(JobState state) {
		this.state = state;
	}

	public synchronized JobState getState() {
		logger.debug("Refresh status for job!");
		refreshState();
		return state;
	}
}