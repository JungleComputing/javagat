package org.gridlab.gat.resources.cpi.gliteMultiUser;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.glite.wms.wmproxy.JobIdStructType;
import org.glite.wms.wmproxy.WMProxyAPI;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingLocator;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingPortType;
import org.glite.wsdl.types.lb.GenericFault;
import org.glite.wsdl.types.lb.JobFlags;
import org.glite.wsdl.types.lb.JobStatus;
import org.glite.wsdl.types.lb.StatName;
import org.globus.axis.transport.HTTPSSender;
import org.globus.gsi.GlobusCredential;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.SerializedJob;
import org.gridlab.gat.resources.security.gliteMultiUser.GliteSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents gLite 3.1 job instance.
 * 
 * @author Stefan Bozic
 */
@SuppressWarnings("serial")
public class GliteJob extends JobCpi {

	/**
	 * Logging and Bookkeeping service port
	 */
	private final static int LB_PORT = 9003;

	/**
	 * The Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GliteJob.class);

	/**
	 * The job description
	 */
	private JDL gLiteJobDescription;

	/**
	 * The job software description
	 */
	private SoftwareDescription swDescription;

	/** The job state */
	private volatile String gLiteState = null;

	/** The {@link URL} to the Workload Management System (WMS) */
	private URL wmsURL = null;

	/**
	 * is <code>true</code> if the Job is done and the poststaging has been done.
	 */
	private boolean outputDone = false;

	/** The id of the job. */
	private String gliteJobID;

	/** The WMProxy client */
	private WMProxyAPI api;

	/** The submission-time of the job */
	private volatile long submissiontime = -1L;

	/** The start-time of the job */
	private volatile long starttime = -1L;

	/** The stop-time of the job */
	private volatile long stoptime = -1L;

	/** An exception that might occurs. */
	private volatile GATInvocationException postStageException = null;

	/** The address of the CE where the job will be executed. */
	private volatile String destination = null;

	/** An id for the delegated voms-proxy-certificate */
	private final static String DELEGATION_ID = "delegationId";

	/** The path to the voms-proxy */
	private String vomsProxyPath = "";

	/** The voms proxy instance */
	private GlobusCredential vomsProxy = null;

	/** Web Service Port_Type */
	private LoggingAndBookkeepingPortType lbPortType = null;

	/**
	 * Logging and Bookkeeping service url
	 */
	private URL lbURL;

	/**
	 * @param gatContext
	 * @param jobDescription
	 * @param sandbox
	 * @param brokerURI
	 * @throws GATInvocationException
	 * @throws GATObjectCreationException
	 */
	protected GliteJob(final GATContext gatContext, final JobDescription jobDescription, final Sandbox sandbox,
			final String brokerURI) throws GATInvocationException, GATObjectCreationException {
		super(gatContext, jobDescription, sandbox);
		System.setProperty("sslProtocol", "SSLv3");
		// make it work with the axis services
		// the axis service will only accept the uri if the protocol is
		// known to them while any:// is not known to them, https:// will work
		String axisBrokerURI = brokerURI.replaceFirst("any://", "https://");

		try {
			this.wmsURL = new URL(axisBrokerURI);
			LOGGER.info("Create a new GLiteJob instance. The WMS-URL is: " + wmsURL.toString());
		} catch (MalformedURLException e) {
			throw new GATInvocationException("WMS-URL is not valid!", e);
		}

		initSecurity(gatContext);

		this.swDescription = this.jobDescription.getSoftwareDescription();

		if (swDescription.getExecutable() == null) {
			throw new GATInvocationException("The Job description does not contain an executable");
		}

		// Create Job Description Language File ...
		long jdlID = System.currentTimeMillis();
		String voName = GliteConstants.getVO(gatContext);

		ResourceDescription rd = jobDescription.getResourceDescription();
		this.gLiteJobDescription = new JDL(jdlID, swDescription, voName, rd);
	}

	/**
	 * Constructor for serialized jobs.
	 * 
	 * @param gatContext
	 * @param sj
	 * @throws GATObjectCreationException
	 * @throws GATInvocationException
	 */
	public GliteJob(GATContext gatContext, SerializedJob sj) throws GATObjectCreationException, GATInvocationException {
		super(gatContext, sj.getJobDescription(), sj.getSandbox());
		System.setProperty("sslProtocol", "SSLv3");
		if (sandbox != null) {
			sandbox.setContext(gatContext);
		}

		if (System.getProperty("GLOBUS_LOCATION") == null) {
			String globusLocation = System.getProperty("gat.adaptor.path") + java.io.File.separator + "GlobusAdaptor"
					+ java.io.File.separator;
			System.setProperty("GLOBUS_LOCATION", globusLocation);
		}

		if (System.getProperty("axis.ClientConfigFile") == null) {
			String axisClientConfigFile = System.getProperty("gat.adaptor.path") + java.io.File.separator
					+ "GlobusAdaptor" + java.io.File.separator + "client-config.wsdd";
			System.setProperty("axis.ClientConfigFile", axisClientConfigFile);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("reconstructing wsgt4newjob: " + sj);
		}

		this.gliteJobID = sj.getJobId();
		this.starttime = sj.getStarttime();
		this.stoptime = sj.getStoptime();
		this.submissiontime = sj.getSubmissiontime();

		// Tell the engine that we provide job.status events
		HashMap<String, Object> returnDef = new HashMap<String, Object>();
		returnDef.put("status", JobState.class);

		initSecurity(gatContext);
		jobDescription = sj.getJobDescription();
	}

	/**
	 * Initialize the security
	 * 
	 * @param gatContext the GAT context
	 * @throws GATInvocationException an exception that might occurs
	 */
	private void initSecurity(GATContext gatContext) throws GATInvocationException {
		// Create a voms-proxy instance for this job which is stored in proxy
		// directory.
		vomsProxy = GliteSecurityUtils.getVOMSProxy(gatContext, true);

		vomsProxyPath = GliteSecurityUtils.getPathToUserVomsProxy(gatContext);
	}

	/**
	 * Returns an instance of {@link WMProxyAPI}. If its not exits the method creates a new instance.
	 * 
	 * @return an instance of {@link WMProxyAPI}
	 * @throws GATInvocationException an exception that might occurs.
	 */
	private WMProxyAPI getWmProxy() throws GATInvocationException {
		if (api == null) {
			try {

				final String caDirectory = System.getProperty("CADIR");

				if (null == caDirectory || caDirectory.isEmpty()) {
					throw new GATInvocationException("No CA directory is specified as System property!");
				}

				api = new WMProxyAPI(wmsURL.toString(), vomsProxyPath, caDirectory);
				String proxy = api.grstGetProxyReq(DELEGATION_ID);
				api.grstPutProxy(DELEGATION_ID, proxy);
			} catch (Exception e) {
				throw new GATInvocationException("An error occurs during creating a new instance of GLiteJob.");
			}
		}

		return api;
	}

	/**
	 * Instantiate the logging and bookkeeping service classes, which are used for status updates
	 */
	private void initLBSoapService() {
		// instantiate the logging and bookkeeping service
		try {
			URL jobUrl = new URL(gliteJobID);
			lbURL = new URL(jobUrl.getProtocol(), jobUrl.getHost(), LB_PORT, "/");

			// Set provider
			SimpleProvider provider = new SimpleProvider();
			SimpleTargetedChain c = null;
			c = new SimpleTargetedChain(new HTTPSSender());
			provider.deployTransport("https", c);
			c = new SimpleTargetedChain(new HTTPSender());
			provider.deployTransport("http", c);

			// get LB Stub
			LoggingAndBookkeepingLocator loc = new LoggingAndBookkeepingLocator(provider);
			lbPortType = loc.getLoggingAndBookkeeping(lbURL);
		} catch (MalformedURLException e) {
			LOGGER.error("Problem instantiating Logging and Bookkeeping service " + e.toString());
		} catch (ServiceException e) {
			LOGGER.error("Problem instantiating Logging and Bookkeeping service " + e.toString());
		}
	}

	/**
	 * JobSubmit via API {@link ResourceBroker#submitJob(org.gridlab.gat.resources.AbstractJobDescription)}
	 * 
	 * @return the id of the job
	 * @throws GATInvocationException An exception that might occurs
	 * @throws GATObjectCreationException An exception that might occurs
	 */
	public String submitJob() throws GATInvocationException, GATObjectCreationException {
		try {
			JobIdStructType jobIds = getWmProxy().jobSubmit(gLiteJobDescription.getJdlString(), DELEGATION_ID);

			gliteJobID = jobIds.getId();

			LOGGER.info("Job sucessfully submitted with id: " + gliteJobID);

			return gliteJobID;
		} catch (Exception e) {
			throw new GATInvocationException("Ann error occurs during submitting the job.", e);
		}
	}

	/**
	 * @see JobCpi#getInfo()
	 */
	public Map<String, Object> getInfo() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("state", this.state);
		map.put("glite.state", this.gLiteState);
		map.put("jobID", jobID);
		map.put("adaptor.job.id", gliteJobID);
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

	/**
	 * Queries the current state of the job via WebService.
	 */
	private void queryState() {
		if (null == lbPortType) {
			initLBSoapService();
		}

		if (outputDone) { // API is ready with POST STAGING
			this.gLiteState = "Cleared";
		} else {
			try {
				final JobStatus js = lbPortType.jobStatus(gliteJobID, new JobFlags());
				final StatName state = js.getState();
				this.gLiteState = state.toString();
				this.destination = js.getDestination();
				String failure = js.getFailureReasons();

				if (null != failure && !failure.isEmpty()) {
					LOGGER.error(failure);
				}

			} catch (GenericFault e) {
				LOGGER.error(e.toString());
			} catch (RemoteException e) {
				LOGGER.error("gLite Error: LoggingAndBookkeeping service only works in glite 3.1 or higher", e);
			}
		}
	}

	/**
	 * Update the status of the job. Depended on the requested state some action, like file staging or sandbox cleanup,
	 * has to be done.
	 */
	private synchronized void updateState() {
		if ("Waiting".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.INITIAL;
		} else if ("Ready".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.INITIAL;
		} else if ("Scheduled".equalsIgnoreCase(gLiteState)) {
			// if state appears the first time, set the submission time
			// appropriately
			if (submissiontime == -1L) {
				submissiontime = System.currentTimeMillis();
			}

			state = Job.JobState.SCHEDULED;
		} else if ("Running".equalsIgnoreCase(gLiteState)) {
			// sometimes, scheduled state is skipped
			if (submissiontime == -1L) {
				submissiontime = System.currentTimeMillis();
			}

			// if running for the first time, set the start time appropriately
			if (starttime == -1L) {
				starttime = System.currentTimeMillis();
			}

			state = Job.JobState.RUNNING;
		} else if ("Done (Failed)".equalsIgnoreCase(gLiteState)) {
			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}

			state = Job.JobState.DONE_FAILURE;
		} else if ("Submitted".equalsIgnoreCase(gLiteState)) {
			if (submissiontime == -1L) {
				submissiontime = System.currentTimeMillis();
			}

			state = Job.JobState.INITIAL;
		} else if ("Aborted".equalsIgnoreCase(gLiteState)) {
			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}

			state = Job.JobState.SUBMISSION_ERROR;
		} else if ("DONE".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.DONE_SUCCESS;
		} else if ("Done (Success)".equalsIgnoreCase(gLiteState)) {
			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}

			state = Job.JobState.DONE_SUCCESS;
		} else if ("Cancelled".equalsIgnoreCase(gLiteState)) {
			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}

			state = Job.JobState.STOPPED;
		} else if ("Cleared".equalsIgnoreCase(gLiteState)) {

			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}

			state = Job.JobState.STOPPED;
		} else {
			this.state = Job.JobState.UNKNOWN;
		}
	}

	/**
	 * Stop the job submitted by this class Independent from whether the WMS will actually cancel the job and report the
	 * CANCELLED state back, the JobStatus poll thread will terminate after a fixed number of job updates after this has
	 * been called. The time interval the lookup thread will still wait till cancelation ater calling stop() is defined
	 * in the UDPATE_INTV_AFTER_JOB_KILL variable in the JobStatusLookUp Thread. This has become necessary because some
	 * jobs would hang forever in a state even after calling the stop method.
	 */
	public void stop() throws GATInvocationException {
		if (state == JobState.POST_STAGING || state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) {
			return;
		}

		try {
			getWmProxy().jobCancel(DELEGATION_ID);
		} catch (Exception e) {
			throw new GATInvocationException("Could not cancel job!", e);
		}
	}

	/*
	 * @see org.gridlab.gat.advert.Advertisable#marshal()
	 */
	public String marshal() {
		SerializedJob sj;

		sj = new SerializedJob(getClass().getName(), jobDescription, sandbox, gliteJobID, submissiontime, starttime,
				stoptime);

		String res = GATEngine.defaultMarshal(sj);
		if (logger.isDebugEnabled()) {
			logger.debug("marshalled seralized job: " + res);
		}
		return res;
	}

	/**
	 * Unmarshal a {@link GliteJob} instance.
	 * 
	 * @param context the gat context
	 * @param s the serialized job
	 * @return An {@link GliteJob} instance
	 * 
	 * @throws GATObjectCreationException
	 */
	public static Advertisable unmarshal(GATContext context, String s) throws GATObjectCreationException {
		Advertisable retVal = null;

		if (logger.isDebugEnabled()) {
			logger.debug("unmarshalled serialized job: " + s);
		}

		SerializedJob sj = (SerializedJob) GATEngine.defaultUnmarshal(SerializedJob.class, s, GliteJob.class.getName());

		// // if this job was created within this JVM, just return a reference to
		// // the job
		// synchronized (JobCpi.class) {
		// for (int i = 0; i < jobList.size(); i++) {
		// JobCpi j = (JobCpi) jobList.get(i);
		// if (j instanceof WSGT4newJob) {
		// WSGT4newJob gj = (WSGT4newJob) j;
		// if (sj.getJobId().equals(gj.g)) {
		// if (logger.isDebugEnabled()) {
		// logger.debug("returning existing job: " + gj);
		// }
		//
		// try {
		// //Its not possible to reset the credentials to a job object.
		// //So create a WSGT4 instance if the credential is getting expired.
		// GSSCredential credential = gj.job.getCredentials();
		// if (credential.getRemainingLifetime() == 0) {
		// logger.debug("Credential expired. Create a new Job instance.");
		// jobList.remove(gj);
		// gj = null;
		// return new WSGT4newJob(context, sj);
		// }
		// } catch (Exception e) {
		// throw new RuntimeException("Cannot retrieve new credentials for job.", e);
		// }
		//
		// return gj;
		// }
		// }
		// }
		// }
		try {
			retVal = new GliteJob(context, sj);
		} catch (GATInvocationException e) {
			e.printStackTrace();
		}

		return retVal;
	}

	@Override
	public synchronized org.gridlab.gat.resources.Job.JobState getState() {
		logger.debug("Refresh status for job!");
		try {
			queryState();
			updateState();
		} catch (Exception e) {
			logger.error("Cannot refresh status for job!", e);
			throw new RuntimeException(e);
		}
		return this.state;
	}
}
