package org.gridlab.gat.resources.cpi.grms;

import grms_pkg.Grms;
import grms_pkg.GrmsResponse;
import grms_pkg.JobHistory;
import grms_pkg.JobInformation;
import grms_pkg.JobStatusType;
import grms_pkg.holders.JobInformationHolder;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;

/**
 * @author rob
 */
public class GrmsJob extends Job {

	GrmsBrokerAdaptor broker;

	String jobId;

	Grms grms;

	JobDescription jobDescription;

	public GrmsJob(GrmsBrokerAdaptor broker, JobDescription jobDescription,
			String jobId) {
		this.broker = broker;
		this.jobId = jobId;
		this.jobDescription = jobDescription;
		this.grms = broker.getGrms();
		state = SCHEDULED;
	}

	public JobDescription getJobDescription() {
		return jobDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#unSchedule()
	 */
	public void unSchedule() throws GATInvocationException, RemoteException,
			IOException {
		if (getState() != SCHEDULED) {
			throw new GATInvocationException("Job is not in SHCEDULED state");
		}

		GrmsResponse res = grms.cancelJob(jobId);

		if (res.getErrorCode() != 0) {
			throw new GATInvocationException(res.getErrorMessage());
		}

		state = INITIAL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#stop()
	 */
	public void stop() throws GATInvocationException, RemoteException,
			IOException {
		if (getState() != RUNNING) {
			throw new GATInvocationException("Job is not running");
		}

		GrmsResponse res = grms.cancelJob(jobId);

		if (res.getErrorCode() != 0) {
			throw new GATInvocationException(res.getErrorMessage());
		}

		state = INITIAL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#getState()
	 */
	public int getState() throws GATInvocationException, RemoteException,
			IOException {
		// get info, this will update the state.
		getInfo();
		return state;
	}

	private Map createInfoMap(JobInformation jobInfo) {
		Map res = new TreeMap();

		String status = jobInfo.getJobStatus().getValue();
		updateState(status);
		res.put("state", getStateString());

		res.put("grmsStatus", status);
		res.put("userDn", jobInfo.getUserDn());

		res.put("errorDescription", jobInfo.getErrorDescription());

		Calendar schedTime = jobInfo.getSubmissionTime();
		if (schedTime != null) {
			res.put("scheduletime", "" + schedTime.getTimeInMillis());
		}

		Calendar stopTime = jobInfo.getFinishTime();
		if (stopTime != null) {
			res.put("stoptime", "" + stopTime.getTimeInMillis());
		}

		JobHistory h = jobInfo.getLastHistory();
		res.put("hostname", h.getHostName());

		Calendar startTime = h.getStartTime();
		if (startTime != null) {
			res.put("starttime", "" + startTime.getTimeInMillis());
		}

		Calendar localSubTime = h.getLocalSubmissionTime();
		if (localSubTime != null) {
			res
					.put("local_submissiontime", ""
							+ localSubTime.getTimeInMillis());
		}

		Calendar localStartTime = h.getLocalStartTime();
		if (localStartTime != null) {
			res.put("local_starttime", "" + localStartTime.getTimeInMillis());
		}

		Calendar localFinishTime = h.getLocalFinishTime();
		if (localFinishTime != null) {
			res.put("local_stoptime", "" + localFinishTime.getTimeInMillis());
		}

		return res;
	}

	private void updateState(String status) {
		if (status.equals(JobStatusType._QUEUED)) {
			state = SCHEDULED;
		} else if (status.equals(JobStatusType._PREPROCESSING)) {
			state = INITIAL;
		} else if (status.equals(JobStatusType._PENDING)) {
			state = INITIAL;
		} else if (status.equals(JobStatusType._RUNNING)) {
			state = RUNNING;
		} else if (status.equals(JobStatusType._STOPPED)) {
			state = STOPPED;
		} else if (status.equals(JobStatusType._POSTPROCESSING)) {
			state = RUNNING;
		} else if (status.equals(JobStatusType._FINISHED)) {
			state = STOPPED;
		} else if (status.equals(JobStatusType._SUSPENDED)) {
			state = STOPPED;
		} else if (status.equals(JobStatusType._FAILED)) {
			state = SUBMISSION_ERROR;
		} else if (status.equals(JobStatusType._CANCELED)) {
			state = INITIAL;
		} else {
			System.err
					.println("Internal error in grms adaptor, unknown state returned: "
							+ status);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#getInfo()
	 */

	public Map getInfo() throws GATInvocationException, RemoteException,
			IOException {
		grms_pkg.holders.JobInformationHolder jobInfoH = new JobInformationHolder();
		GrmsResponse res = grms.getJobInfo(jobId, jobInfoH);

		if (res.getErrorCode() != 0) {
			throw new GATInvocationException(res.getErrorMessage());
		}

		JobInformation jobInfo = jobInfoH.value;
		Map m = createInfoMap(jobInfo);
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#getJobID()
	 */
	public String getJobID() throws GATInvocationException, RemoteException,
			IOException {
		return jobId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Job#migrate()
	 */
	public void migrate() throws GATInvocationException, RemoteException,
			IOException {
		GrmsResponse res = grms.migrateJob(jobId);
		if (res.getErrorCode() != 0) {
			throw new GATInvocationException(res.getErrorMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.Advertisable#marshal()
	 */
	public String marshal() {
		throw new Error("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.Advertisable#unmarshal(java.lang.String)
	 */
	public Advertisable unmarshal(String input) {
		throw new Error("Not implemented");
	}
}