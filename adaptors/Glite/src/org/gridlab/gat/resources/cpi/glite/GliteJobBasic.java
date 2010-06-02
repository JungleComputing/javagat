////////////////////////////////////////////////////////////////////
//
// GliteJob.java
// 
// Contributor(s):
// Dec/2007 - Andreas Havenstein 
//     for Max Planck Institute for Gravitational Physics
//     (Albert Einstein Institute) 
//     Astrophysical Relativity / eScience
// Jun,Jul/2008 - Thomas Zangerl 
//		for Distributed and Parallel Systems Research Group
//		University of Innsbruck
//      major enhancements
//
////////////////////////////////////////////////////////////////////

// requires lb_1_5_3

package org.gridlab.gat.resources.cpi.glite;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.glite.wms.wmproxy.JobIdStructType;
import org.glite.wsdl.types.lb.GenericFault;
import org.glite.wsdl.types.lb.JobStatus;
import org.glite.wsdl.types.lb.StatName;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.security.glite.GliteSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GliteJobBasic extends JobCpi implements GliteJobInterface {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(GliteJobBasic.class);

	private JDL_Basic gLiteJobDescription = null;
	private SoftwareDescription swDescription = null;
	protected volatile String gLiteState = "";
	
	private Metric statusMetric = null;
	protected final JobIdStructType jobIdStructType;

	private boolean jobKilled = false;

	private volatile long submissiontime = -1L;
	private volatile long starttime = -1L;
	private volatile long stoptime = -1L;
	
	private volatile GATInvocationException postStageException = null;
	private volatile String destination = null;
	private volatile String statusSuspendReason = null;
	private volatile String statusCancelReason = null;
	private volatile String statusFailureReason = null;
	private volatile String statusCondorReason = null;
	private volatile String statusPBSReason = null;
	
	private GliteJobHelper gliteJobHelper = null;
	private LBService lbService = null;
	
	protected GliteJobBasic(final GATContext gatContext, final JobDescription jobDescription, JobIdStructType jobIdStructType, GliteJobHelper gliteJobHelper){
		super(gatContext, jobDescription, null);
		this.jobIdStructType = jobIdStructType;
		this.swDescription = this.jobDescription.getSoftwareDescription();
		this.gliteJobHelper = gliteJobHelper;
	}
	
	protected GliteJobBasic(final GATContext gatContext, final JobDescription jobDescription, final String brokerURI) throws GATInvocationException, GATObjectCreationException {

		super(gatContext, jobDescription, null);

		this.swDescription = this.jobDescription.getSoftwareDescription();

		if (this.swDescription.getExecutable() == null) {
			throw new GATInvocationException("The Job description does not contain an executable");
		}

		Map<String, Object> returnDef = new HashMap<String, Object>();
		returnDef.put("status", String.class);
		MetricDefinition statusMetricDefinition = new MetricDefinition("job.status", MetricDefinition.DISCRETE, "String", null, null, returnDef);
		this.statusMetric = new Metric(statusMetricDefinition, null);
		registerMetric("submitJob", statusMetricDefinition);

		// Create Job Description Language File ...
		String voName = GliteConstants.getVO(gatContext);

		ResourceDescription rd = jobDescription.getResourceDescription();

		this.gLiteJobDescription = new JDL_Basic(swDescription, voName, rd);

		String deleteOnExitStr = (String) gatContext.getPreferences().get("glite.deleteJDL");

		// save the file only to disk if deleteJDL has not been specified
		if (deleteOnExitStr != null && !Boolean.parseBoolean(deleteOnExitStr)) {
			this.gLiteJobDescription.saveToDisk();
		}
		String proxyFile = null;
		try{
			proxyFile = GliteSecurityUtils.touchVomsProxy(gatContext);
		}catch (GATInvocationException e) {
			throw e;
		}
		GSSCredential userCredential;
		try {
			userCredential = new GlobusGSSCredentialImpl(new GlobusCredential(proxyFile), GSSCredential.INITIATE_AND_ACCEPT);
		} catch (GSSException e) {
            LOGGER.error(e.toString());
            throw new GATInvocationException("Failed to load credentials");
        } catch (GlobusCredentialException e) {
            LOGGER.error(e.toString());
            throw new GATInvocationException("Failed to load credentials");
        }
		
		this.gliteJobHelper = new GliteJobHelper(gatContext, brokerURI, proxyFile, userCredential);
		
		//Delegate the certificate to WMS
		String delegationId = gliteJobHelper.delegateCredential();
		//Register the new Job inside WMS
		this.jobIdStructType = gliteJobHelper.registerNewJob(gLiteJobDescription, delegationId);
		//Upload the needed InputSandBoxFiles
		gliteJobHelper.stageInSandboxFiles(jobIdStructType.getId(), swDescription);
		//Start the Job
		gliteJobHelper.submitJob(jobIdStructType);	
		
		LOGGER.info("jobID " + jobIdStructType.getId());

		//Creation of the LB stub for the job monitoring
		this.lbService = new LBService(jobIdStructType.getId(), userCredential);
		
		//Start status lookup
		new JobStatusLookUp(this,gatContext);
	}

	public Map<String, Object> getInfo() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put(Job.STATE, state);
		map.put("glite.state", gLiteState);
		map.put("jobID", jobID);
		map.put(Job.ADAPTOR_JOB_ID, jobIdStructType.getId());
		map.put(Job.SUBMISSIONTIME, submissiontime);
		map.put(Job.STARTTIME, starttime);
		map.put(Job.STOPTIME, stoptime);
		map.put(Job.POSTSTAGE_EXCEPTION, postStageException);
		if (state == JobState.RUNNING) {
			map.put(Job.HOSTNAME, destination);
		}
		map.put("glite.destination", destination);
		if(statusCancelReason != null){
			map.put("glite.status.cancel.reason", statusCancelReason);
		}
		if(statusFailureReason != null){
			map.put("glite.status.failure.reason", statusFailureReason);
		}
		if(statusSuspendReason != null){
			map.put("glite.status.suspend.reason", statusSuspendReason);
		}
		if(statusCondorReason != null){
			map.put("glite.status.condor.reason", statusCondorReason);
		}
		if(statusPBSReason != null){
			map.put("glite.status.pbs.reason", statusPBSReason);
		}
		return map;
	}

	public synchronized void updateState() {
		JobStatus jobStatus = null;
		try{
			jobStatus = lbService.queryJobState(jobIdStructType);
		} catch (GenericFault e) {
			LOGGER.error(e.toString());
		} catch (RemoteException e) {
			LOGGER.error("gLite Error: LoggingAndBookkeeping service only works in glite 3.1 or higher", e);
		}
		if(processJobStatus(jobStatus)){
			MetricEvent event = new MetricEvent(this, state, statusMetric, System.currentTimeMillis());
			fireMetric(event);
		}
	}
	
	protected synchronized boolean processJobStatus(JobStatus jobStatus){
		
		if(jobStatus == null){
			return false;
		}
		
		gLiteState = jobStatus.getState().getValue();

		JobState s = gliteJobHelper.generateJobStateFromGLiteState(gLiteState);
        if (s == state) {
            // Don't generate events for unchanged state.
            return false;
        }

        state = s;

		for (int i = 0; i < jobStatus.getStateEnterTimes().length; i++) {
			if (jobStatus.getStateEnterTimes(i).getTime().getTimeInMillis() != 0) {
				if(StatName.SUBMITTED.equals(jobStatus.getStateEnterTimes(i).getState())) {
					if (submissiontime == -1L) {
						submissiontime = jobStatus.getStateEnterTimes(i).getTime().getTimeInMillis();
					}
				}else if(StatName.RUNNING.equals(jobStatus.getStateEnterTimes(i).getState())) {
					if (starttime == -1L) {
						starttime = jobStatus.getStateEnterTimes(i).getTime().getTimeInMillis();
					}
				}else if(StatName.DONE.equals(jobStatus.getStateEnterTimes(i).getState()) ||
						StatName.CANCELLED.equals(jobStatus.getStateEnterTimes(i).getState()) ||
						StatName.ABORTED.equals(jobStatus.getStateEnterTimes(i).getState())) {
					if (stoptime == -1L) {
						stoptime = jobStatus.getStateEnterTimes(i).getTime().getTimeInMillis();
					}
				}
			}
		}
		statusCancelReason = jobStatus.getCancelReason();
		statusFailureReason = jobStatus.getFailureReasons();
		statusSuspendReason = jobStatus.getSuspendReason();
		statusCondorReason = jobStatus.getCondorReason();
		statusPBSReason = jobStatus.getPbsReason();
		destination = jobStatus.getDestination();
		
		return true;
	}

	public synchronized void receiveOutput() {
		this.postStageException = gliteJobHelper.receiveOutput(new JobIdStructType[]{jobIdStructType}, new SoftwareDescription[]{swDescription});
	}

	public Metric getStatusMetric() {
		return statusMetric;
	}
	
	public boolean isJobKilled() {
		return jobKilled;
	}

	/**
	 * Stop the job submitted by this class Independent from whether the WMS
	 * will actually cancel the job and report the CANCELLED state back, the
	 * JobStatus poll thread will terminate after a fixed number of job updates
	 * after this has been called. The time interval the lookup thread will
	 * still wait till cancellation after calling stop() is defined in the
	 * UDPATE_INTV_AFTER_JOB_KILL variable in the JobStatusLookUp Thread. This
	 * has become necessary because some jobs would hang forever in a state even
	 * after calling the stop method.
	 */
	public void stop() throws GATInvocationException {
		if (state == JobState.POST_STAGING || state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) {
			return;
		}
		gliteJobHelper.stop(jobIdStructType);
		jobKilled = true;
	}

}
