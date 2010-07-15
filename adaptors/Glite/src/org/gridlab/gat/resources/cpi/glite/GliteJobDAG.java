package org.gridlab.gat.resources.cpi.glite;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
import org.gridlab.gat.resources.OrderedCoScheduleJobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.CoScheduleJobCpi;
import org.gridlab.gat.security.glite.GliteSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GliteJobDAG extends CoScheduleJobCpi implements GliteJobInterface {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(GliteJobDAG.class);

	private JDL_DAG gLiteJobDescription = null;
//	private OrderedCoScheduleJobDescription dagJobDescription = null;
	private SoftwareDescription[] swDescriptions = null;
	private volatile String gLiteState = "";
	
	private Metric statusMetric = null;
	private final JobIdStructType jobIdStructType;

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
	
	private HashMap<JobDescription,GliteJobDAGTask> childJobs = null;
	private HashMap<String,GliteJobDAGTask> jobIDs = null;
	
	protected GliteJobDAG(final GATContext gatContext, final OrderedCoScheduleJobDescription dagJobDescription, final String brokerURI) throws GATInvocationException, GATObjectCreationException {
		super(gatContext, dagJobDescription);

		if(dagJobDescription.getJobDescriptions().size() == 0){
			throw new GATInvocationException("The DAG Job description does not contain any sub job!");
		}
//		this.dagJobDescription = dagJobDescription;
		
		this.swDescriptions = new SoftwareDescription[dagJobDescription.getJobDescriptions().size()];
		for (int i = 0; i < dagJobDescription.getJobDescriptions().size(); i++) {
			this.swDescriptions[i] = dagJobDescription.getJobDescriptions().get(i).getSoftwareDescription();
		}

		Map<String, Object> returnDef = new HashMap<String, Object>();
		returnDef.put("status", String.class);
		MetricDefinition statusMetricDefinition = new MetricDefinition("job.status", MetricDefinition.DISCRETE, "String", null, null, returnDef);
		this.statusMetric = new Metric(statusMetricDefinition, null);
		registerMetric("submitJob", statusMetricDefinition);

		String voName = GliteConstants.getVO(gatContext);

		this.gLiteJobDescription = new JDL_DAG(dagJobDescription, voName);

		String deleteOnExitStr = (String) gatContext.getPreferences().get("glite.deleteJDL");

		// save the file only to disk if deleteJDL has not been specified
		if (deleteOnExitStr != null && !Boolean.parseBoolean(deleteOnExitStr)) {
			this.gLiteJobDescription.saveToDisk();
		}

		String proxyFile = GliteSecurityUtils.touchVomsProxy(gatContext);
		GSSCredential userCredential;
		try {
			userCredential = new GlobusGSSCredentialImpl(new GlobusCredential(proxyFile), GSSCredential.INITIATE_AND_ACCEPT);
		} catch (GSSException e) {
            LOGGER.info(e.toString());
            throw new GATInvocationException("Failed to load credentials");
        } catch (GlobusCredentialException e) {
            LOGGER.info(e.toString());
            throw new GATInvocationException("Failed to load credentials");
        }
		
		this.gliteJobHelper = new GliteJobHelper(gatContext, brokerURI, proxyFile, userCredential);
		
		
		//Delegate the certificate to WMS
		String delegationId = gliteJobHelper.delegateCredential();
		//Register the new Job inside WMS
		this.jobIdStructType = gliteJobHelper.registerNewJob(gLiteJobDescription, delegationId);
		
		//Populate the child nodes
		childJobs = new HashMap<JobDescription, GliteJobDAGTask>();
		jobIDs = new HashMap<String, GliteJobDAGTask>();
		for (int i = 0; i < jobIdStructType.getChildrenJob().length; i++) {
			JobDescription jobDescriptionChild = gLiteJobDescription.getJobDescription(jobIdStructType.getChildrenJob(i).getName());
			GliteJobDAGTask gliteJobDAGTask = new GliteJobDAGTask(gatContext, jobDescriptionChild, jobIdStructType.getChildrenJob(i),gliteJobHelper);
			childJobs.put(jobDescriptionChild, gliteJobDAGTask);
			jobIDs.put(gliteJobDAGTask.getJobIdStructType().getId(), gliteJobDAGTask);
		}
		
		//Upload InputSandBoxFiles in the correct InputSandBox of each job
		for (Iterator<Entry<String, GliteJobDAGTask>> iterator = jobIDs.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, GliteJobDAGTask> entry =  iterator.next();
			gliteJobHelper.stageInSandboxFiles(entry.getKey(), entry.getValue().getJobDescription().getSoftwareDescription());
		}
		
		//Start the Job
		gliteJobHelper.submitJob(jobIdStructType);	
		
		LOGGER.info("jobID " + jobIdStructType.getId()+" submitted");

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
			LOGGER.info(e.toString());
		} catch (RemoteException e) {
			LOGGER.info("gLite Error: LoggingAndBookkeeping service only works in glite 3.1 or higher", e);
		}
		
		if(jobStatus == null){
			return;
		}
		
		boolean updated = false;
		if(jobStatus.getChildrenStates() != null){
			for (int i = 0; i < jobStatus.getChildrenStates().length; i++) {
				JobStatus jobStatusChild = jobStatus.getChildrenStates(i);
				GliteJobDAGTask gliteJobDAGTask = this.jobIDs.get(jobStatusChild.getJobId());
				boolean isUpdated = gliteJobDAGTask.processJobStatus(jobStatusChild);
				if(!updated && isUpdated){
					updated = true;
				}
			}
		}
		
		gLiteState = jobStatus.getState().getValue();
		JobState s = gliteJobHelper.generateJobStateFromGLiteState(gLiteState);
        if (s == state && !updated) {
            // Don't generate event for unchanged state.
            return;
        }
        if(s != state){
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
        }
		
		MetricEvent event = new MetricEvent(this, state, statusMetric, System.currentTimeMillis());
		fireMetric(event);
	}

	public synchronized void receiveOutput() {
		ArrayList<JobIdStructType> jobIdStructTypes = new ArrayList<JobIdStructType>();
		for (int i = 0; i < jobIdStructType.getChildrenJob().length; i++) {
			String jobID = jobIdStructType.getChildrenJob(i).getId();
			if(jobIDs.get(jobID).getGliteState().toLowerCase().startsWith("done")){
				jobIdStructTypes.add(jobIdStructType.getChildrenJob(i));
			}else{
				LOGGER.warn("Job "+jobID+" state: "+jobIDs.get(jobID).getGliteState()+" => Nothing to retrieve");
			}
		}
		if(jobIdStructTypes.isEmpty()){
			LOGGER.info("None of the Jobs terminated successfully, nothing to retrieve.");
		}
		this.postStageException = gliteJobHelper.receiveOutput(jobIdStructTypes.toArray(new JobIdStructType[jobIdStructTypes.size()]), swDescriptions);
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

	public Job getJob(JobDescription description) {
		if(!childJobs.containsKey(description)){
			return null;
		}
		return childJobs.get(description);
	}
	
	private class GliteJobDAGTask extends GliteJobBasic {

		protected GliteJobDAGTask(GATContext gatContext, final JobDescription jobDescription,JobIdStructType jobIdStructType, GliteJobHelper gliteJobHelper) {
			super(gatContext,jobDescription,jobIdStructType, gliteJobHelper);
		}

		private static final long serialVersionUID = 1L;

		public JobIdStructType getJobIdStructType(){
			return jobIdStructType;
		}
		
		public void updateState() {
			GliteJobDAG.this.updateState();
		}
		
		public String getGliteState(){
			return this.gLiteState;
		}
		
	}

}
