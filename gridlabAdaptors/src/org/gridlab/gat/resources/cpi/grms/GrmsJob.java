package org.gridlab.gat.resources.cpi.grms;

import grms_pkg.Grms;
import grms_pkg.GrmsResponse;
import grms_pkg.JobHistory;
import grms_pkg.JobInformation;
import grms_pkg.JobStatusType;
import grms_pkg.holders.JobInformationHolder;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * @author rob
 */
public class GrmsJob extends JobCpi {
    GrmsBrokerAdaptor broker;

    String jobId;

    Grms grms;

    public GrmsJob(GATContext gatContext, Preferences preferences, GrmsBrokerAdaptor broker, JobDescription jobDescription,
        String jobId, Sandbox sandbox) {
        super(gatContext, preferences, jobDescription, sandbox);
        this.broker = broker;
        this.jobId = jobId;
        this.jobDescription = jobDescription;
        this.grms = broker.getGrms();
        state = SCHEDULED;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.Job#stop()
     */
    public void stop() throws GATInvocationException {
        if (getState() != RUNNING) {
            throw new GATInvocationException("Job is not running");
        }

        try {
            GrmsResponse res = grms.cancelJob(jobId);
            if (res.getErrorCode() != 0) {
                throw new GATInvocationException(res.getErrorMessage());
            }

            state = INITIAL;
        } catch (RemoteException e) {
            throw new GATInvocationException("grms", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.Job#getState()
     */
    public int getState() {
        try {
            // get info, this will update the state.
            getInfo();

            return state;
        } catch (GATInvocationException e) {
            return UNKNOWN;
        }
    }

    private Map createInfoMap(JobInformation jobInfo) {
        Map res = new TreeMap();

        String status = jobInfo.getJobStatus().getValue();
        updateState(status);
        res.put("state", getStateString(state));

        res.put("userDn", jobInfo.getUserDn());
        res.put("resManState", status);
        res.put("resManName", "GRMS");

        res.put("errorDescription", jobInfo.getErrorDescription());

        Calendar schedTime = jobInfo.getSubmissionTime();

        if (schedTime != null) {
            res.put("scheduletime", "" + schedTime.getTime());
            res.put("scheduletimeMillis", "" + schedTime.getTimeInMillis());
        }

        Calendar stopTime = jobInfo.getFinishTime();

        if (stopTime != null) {
            res.put("stoptime", "" + stopTime.getTime());
            res.put("stoptimeMillis", "" + stopTime.getTimeInMillis());
        }

        JobHistory h = jobInfo.getLastHistory();
        res.put("hostname", h.getHostName());

        Calendar startTime = h.getStartTime();

        if (startTime != null) {
            res.put("starttime", "" + startTime.getTime());
            res.put("starttimeMillis", "" + startTime.getTimeInMillis());
        }

        Calendar localSubTime = h.getLocalSubmissionTime();

        if (localSubTime != null) {
            res.put("localSubmissiontime", "" + localSubTime.getTime());
            res.put("localSubmissiontimeMillis", ""
                + localSubTime.getTimeInMillis());
        }

        Calendar localStartTime = h.getLocalStartTime();

        if (localStartTime != null) {
            res.put("localStarttime", "" + localStartTime.getTime());
            res.put("localStarttimeMillis", ""
                + localStartTime.getTimeInMillis());
        }

        Calendar localFinishTime = h.getLocalFinishTime();

        if (localFinishTime != null) {
            res.put("localStoptime", "" + localFinishTime.getTime());
            res.put("localStoptimeMillis", ""
                + localFinishTime.getTimeInMillis());
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
            System.err.println("Internal error in grms adaptor, "
                + "unknown state returned: " + status);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.Job#getInfo()
     */
    public Map getInfo() throws GATInvocationException {
        try {
            grms_pkg.holders.JobInformationHolder jobInfoH = new JobInformationHolder();
            GrmsResponse res = grms.getJobInfo(jobId, jobInfoH);

            if (res.getErrorCode() != 0) {
                throw new GATInvocationException(res.getErrorMessage());
            }

            JobInformation jobInfo = jobInfoH.value;
            Map m = createInfoMap(jobInfo);

            return m;
        } catch (RemoteException e) {
            throw new GATInvocationException("grms", e);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.Job#getJobID()
     */
    public String getJobID() {
        return jobId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.Job#migrate()
     */
    public void migrate() throws GATInvocationException {
        try {
            GrmsResponse res = grms.migrateJob(jobId);

            if (res.getErrorCode() != 0) {
                throw new GATInvocationException(res.getErrorMessage());
            }
        } catch (RemoteException e) {
            throw new GATInvocationException("grms", e);
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
