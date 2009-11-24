package org.gridlab.gat.resources.cpi.glite;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.glite.wms.wmproxy.JobIdStructType;
import org.glite.wsdl.types.lb.GenericFault;
import org.glite.wsdl.types.lb.JobStatus;
import org.glite.wsdl.types.lb.StatName;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GliteJobDAG extends CoScheduleJobCpi implements GliteJobInterface {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(GliteJobDAG.class);

	private DAG_JDL gLiteJobDescription = null;
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
	
	private GliteJobHelper gliteJobHelper = null;
	private LBService lbService = null;
	
	protected GliteJobDAG(final GATContext gatContext, final OrderedCoScheduleJobDescription dagJobDescription, final String brokerURI) throws GATInvocationException, GATObjectCreationException {
		super(gatContext, dagJobDescription);

		if(dagJobDescription.getJobDescriptions().size() == 0){
			throw new GATInvocationException("The DAG Job description does not contain any sub job!");
		}
		
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

		this.gLiteJobDescription = new DAG_JDL(dagJobDescription, voName);

		String deleteOnExitStr = (String) gatContext.getPreferences().get("glite.deleteJDL");

		// save the file only to disk if deleteJDL has not been specified
		if (deleteOnExitStr != null && !Boolean.parseBoolean(deleteOnExitStr)) {
			this.gLiteJobDescription.saveToDisk();
		}

		this.gliteJobHelper = new GliteJobHelper(gatContext, brokerURI);
		
		//Delegate the certificate to WMS
		String delegationId = gliteJobHelper.delegateCredential();
		//Register the new Job inside WMS
		this.jobIdStructType = gliteJobHelper.registerNewJob(gLiteJobDescription, delegationId);
		//Upload the needed InputSandBoxFiles
		gliteJobHelper.stageInSandboxFiles(jobIdStructType.getId(), swDescriptions);
		//Start the Job
		gliteJobHelper.submitJob(jobIdStructType);	
		
		LOGGER.info("jobID " + jobIdStructType.getId());

		//Creation of the LB stub for the job monitoring
		this.lbService = new LBService(jobIdStructType.getId());
		
		//Start status lookup
		new JobStatusLookUp(this,gatContext);
	}

	public Map<String, Object> getInfo() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("state", this.state);
		map.put("glite.state", this.gLiteState);
		map.put("jobID", jobID);
		map.put("adaptor.job.id", jobIdStructType.getId());
		map.put("submissiontime", submissiontime);
		map.put("starttime", starttime);
		map.put("stoptime", stoptime);
		map.put("poststage.exception", postStageException);
		if (state == JobState.RUNNING) {
			map.put("hostname", destination);
		}
		map.put("glite.destination", destination);
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
		
		if(jobStatus == null){
			return;
		}
		
		gLiteState = jobStatus.getState().getValue();
		JobState s = gliteJobHelper.generateJobStateFromGLiteState(gLiteState);
                if (s == state) {
                    // Don't generate event for unchanged state.
                    return;
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
		MetricEvent event = new MetricEvent(this, state, statusMetric, System.currentTimeMillis());
		fireMetric(event);
	}

	public synchronized void receiveOutput() {
		this.postStageException = gliteJobHelper.receiveOutput(jobIdStructType, swDescriptions);
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
		throw new UnsupportedOperationException("Not implemented");
	}

}