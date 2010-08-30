package org.gridlab.gat.resources.cpi.gliteMultiUser;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisProperties;
import org.apache.axis.EngineConfigurationFactory;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.glite.wms.wmproxy.AuthenticationFaultException;
import org.glite.wms.wmproxy.AuthorizationFaultException;
import org.glite.wms.wmproxy.InvalidArgumentFaultException;
import org.glite.wms.wmproxy.JobIdStructType;
import org.glite.wms.wmproxy.JobUnknownFaultException;
import org.glite.wms.wmproxy.OperationNotAllowedFaultException;
import org.glite.wms.wmproxy.ServerOverloadedFaultException;
import org.glite.wms.wmproxy.StringAndLongList;
import org.glite.wms.wmproxy.StringAndLongType;
import org.glite.wms.wmproxy.WMProxyAPI;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingLocator;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingPortType;
import org.glite.wsdl.types.lb.GenericFault;
import org.glite.wsdl.types.lb.JobFlags;
import org.glite.wsdl.types.lb.JobStatus;
import org.glite.wsdl.types.lb.StatName;
import org.glite.wsdl.types.lb.StateEnterTimesItem;
import org.globus.axis.transport.HTTPSSender;
import org.globus.gsi.GlobusCredential;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
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

	/** The id of the job. */
	private String gliteJobID;

	/** The exit status */
	private int exitStatus;

	/** the spended cputime */
	private volatile long cpuTime = -1L;

	/** The location of the sandbox */
	private String sandboxUri = null;

	/** The submission-time of the job */
	private volatile long submissiontime = -1L;

	/** The start-time of the job */
	private volatile long starttime = -1L;

	/** The stop-time of the job */
	private volatile long stoptime = -1L;

	/** The cancel-time of the job */
	private volatile long canceltime = -1L;

	/** An exception that might occurs. */
	private volatile GATInvocationException postStageException = null;

	/** The address of the CE where the job will be executed. */
	private volatile String destination = null;

	/** The path to the voms-proxy */
	private String vomsProxyPath = "";

	/** The voms proxy instance */
	private GlobusCredential vomsProxy = null;

	/** Web Service Port_Type */
	private LoggingAndBookkeepingPortType lbPortType = null;

	private URI stdOut;

	private URI stdErr;

	/**
	 * Logging and Bookkeeping service url
	 */
	private URL lbURL;

	/** The WmProxy instance */
	private WMProxyAPI api = null;

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

		initProperties();

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

		initProperties();

		if (logger.isDebugEnabled()) {
			logger.debug("reconstructing wsgt4newjob: " + sj);
		}

		this.gliteJobID = sj.getJobId();
		this.starttime = sj.getStarttime();
		this.stoptime = sj.getStoptime();
		this.submissiontime = sj.getSubmissiontime();

		try {
			this.wmsURL = new URL(sj.getBrokerUri());
		} catch (MalformedURLException e) {
			throw new GATInvocationException("Error during unmarshalling the job.", e);
		}

		// Tell the engine that we provide job.status events
		HashMap<String, Object> returnDef = new HashMap<String, Object>();
		returnDef.put("status", JobState.class);

		initSecurity(gatContext);
		jobDescription = sj.getJobDescription();
	}

	/**
	 * Initialize the system properties
	 */
	private void initProperties() {
		if (sandbox != null) {
			sandbox.setContext(gatContext);
		}

		if (System.getProperty("axis.socketSecureFactory") == null) {
			System.setProperty("axis.socketSecureFactory", "org.glite.security.trustmanager.axis.AXISSocketFactory");
		}

		if (System.getProperty("sslProtocol") == null) {
			System.setProperty("sslProtocol", "SSLv3");
		}

		AxisProperties.setProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME,
				GLiteEngineConfigurationFactory.class.getName());
		logger.info("set axis.EngineConfigFactory: " + GLiteEngineConfigurationFactory.class.getName());
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
		if (this.api == null) {
			WMProxyAPI api = null;
			try {
				final String caDirectory = System.getProperty("CADIR");

				if (null == caDirectory || caDirectory.isEmpty()) {
					throw new GATInvocationException("No CA directory is specified as System property!");
				}

				String delegationId = getDelegationId();

				api = new WMProxyAPI(wmsURL.toString(), vomsProxyPath, caDirectory);

				String proxy = api.grstGetProxyReq(delegationId);
				api.grstPutProxy(delegationId, proxy);

				this.api = api;
			} catch (Exception e) {
				throw new GATInvocationException("An error occurs during creating a new instance of GLiteJob.", e);
			}
		}

		return this.api;
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
			submissiontime = System.currentTimeMillis();
			WMProxyAPI api = getWmProxy();

			JobIdStructType jobIdStruct = api.jobRegister(gLiteJobDescription.getJdlString(), getDelegationId());

			stageInSandboxFiles(jobIdStruct.getId());

			api.jobStart(jobIdStruct.getId());

			gliteJobID = jobIdStruct.getId();
			LOGGER.info("Job sucessfully submitted with id: " + gliteJobID);

			return gliteJobID;
		} catch (Exception e) {
			LOGGER.error("Ann error occurs during submitting the job.", e);
			throw new GATInvocationException("Ann error occurs during submitting the job.", e);
		}
	}

	/**
	 * Stage in the files that are described in the swDescription of the job.
	 * 
	 * @param sandboxJobID the sandbox path
	 */
	private void stageInSandboxFiles(String sandboxJobID) {
		List<File> sandboxFiles = new ArrayList<File>();
		GATContext newContext = (GATContext) gatContext.clone();
		newContext.addPreference("File.adaptor.name", "GridFTP");

		GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext, vomsProxyPath);
		try {
			LOGGER.debug("Staging in files");
			if (swDescription.getStdin() != null) {
				File f = GAT.createFile(newContext, swDescription.getStdin().getName());
				sandboxFiles.add(f);
			}

			Map<File, File> map = swDescription.getPreStaged();

			for (File orig : map.keySet()) {
				File newF = GAT.createFile(newContext, orig.toGATURI());
				sandboxFiles.add(newF);
			}

			String[] sl;

			sl = api.getSandboxDestURI(sandboxJobID, "gsiftp").getItem();
			if (sl.length > 0) {
				sandboxUri = sl[0];
				LOGGER.info("Sandbox located at : " + sandboxUri);
			}

			for (File sandboxFile : sandboxFiles) {
				URI tempURI = new URI(sl[0] + "/" + sandboxFile.getName());
				URI destURI = new URI(tempURI.getScheme() + "://" + tempURI.getHost() + ":" + tempURI.getPort() + "//"
						+ tempURI.getPath());
				LOGGER.debug("Uploading " + sandboxFile + " to " + destURI);
				File destFile = GAT.createFile(newContext, destURI);
				sandboxFile.copy(destFile.toGATURI());
			}
		} catch (URISyntaxException e) {
			LOGGER.error("URI error while resolving pre-staged file set", e);
		} catch (GATObjectCreationException e) {
			LOGGER.error("Could not create pre-staged file set", e);
		} catch (GATInvocationException e) {
			LOGGER.error("Could not copy files to input sandbox", e);
		} catch (AuthorizationFaultException e) {
			LOGGER.error("Could not authorize to job resource", e);
		} catch (AuthenticationFaultException e) {
			LOGGER.error("Could not autheticate to input sandbox", e);
		} catch (OperationNotAllowedFaultException e) {
			LOGGER.error("Operation not allowed on input sandbox", e);
		} catch (InvalidArgumentFaultException e) {
			LOGGER.error("Invalid Argument while accessing sandbox", e);
		} catch (JobUnknownFaultException e) {
			LOGGER.error("Job unknown for sandbox", e);
		} catch (org.glite.wms.wmproxy.ServiceException e) {
			LOGGER.error("WMS Service exception", e);
		} catch (ServerOverloadedFaultException e) {
			LOGGER.error("Server overload exception", e);
		}
	}

	/**
	 * Returns the delegation id for this job
	 * 
	 * @return the delegation id for this job
	 */
	private String getDelegationId() {
		return "gatjob" + submissiontime;
	}

	/**
	 * @see Job#getExitStatus()
	 */
	@Override
	public int getExitStatus() throws GATInvocationException {
		return exitStatus;
	};

	/**
	 * @see JobCpi#getInfo()
	 */
	public Map<String, Object> getInfo() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("state", this.state);
		map.put("glite.state", this.gLiteState);
		map.put("jobID", jobID);
		map.put("adaptor.job.id", gliteJobID);
		map.put("submissionTime", submissiontime);
		map.put("startTime", starttime);
		map.put("stopTime", stoptime);
		map.put("cancelTime", canceltime);
		map.put("poststage.exception", postStageException);
		if (state == JobState.RUNNING) {
			map.put("hostname", destination);
		}
		map.put("destination", destination);
		map.put("exitStatus", exitStatus);
		map.put("sandBoxUri", sandboxUri);
		map.put("cpuTime", cpuTime);

		try {
			// if the job has been finished, try to retrieve the uris for stdOut and stdErr
			if ((this.state == Job.JobState.DONE_SUCCESS) || (this.state == Job.JobState.DONE_FAILURE)) {
				if (null == stdOut) {
					stdOut = getFileOutputUri("std.out");
				}

				if (null == stdErr) {
					stdErr = getFileOutputUri("std.err");
				}

				map.put("stdOutUri", stdOut);
				map.put("stdErrUri", stdErr);
			}
		} catch (Exception e) {
			LOGGER.error(e.toString());
		}

		return map;
	}

	/**
	 * Queries the current state of the job via WebService.
	 */
	private void queryState() {
		if (null == lbPortType) {
			initLBSoapService();
		}

		try {
			final JobStatus js = lbPortType.jobStatus(gliteJobID, new JobFlags());
			final StatName state = js.getState();
			this.gLiteState = state.toString();
			this.destination = js.getDestination();
			String failure = js.getFailureReasons();

			if (null != failure && !failure.isEmpty()) {
				LOGGER.error(failure);
			}

			processStateTimes(js.getStateEnterTimes());

			if (state == StatName.DONE) {
				exitStatus = js.getExitCode();
				cpuTime = js.getCpuTime();
			}
		} catch (GenericFault e) {
			LOGGER.error(e.toString());
		} catch (RemoteException e) {
			LOGGER.error("gLite Error: LoggingAndBookkeeping service only works in glite 3.1 or higher", e);
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
			state = Job.JobState.READY;
		} else if ("Scheduled".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.SCHEDULED;
		} else if ("Running".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.RUNNING;
		} else if ("Done (Failed)".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.DONE_FAILURE;
		} else if ("Submitted".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.INITIAL;
		} else if ("Aborted".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.ABORTED;
		} else if ("DONE".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.DONE_SUCCESS;
		} else if ("Done (Success)".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.DONE_SUCCESS;
		} else if ("Cancelled".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.STOPPED;
		} else if ("Cleared".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.CLEARED;
		} else {
			this.state = Job.JobState.UNKNOWN;
		}
	}

	/**
	 * Retrieves the times for the interesting states
	 * 
	 * @param stateTimes the times when the job enters a state
	 */
	private void processStateTimes(StateEnterTimesItem[] stateTimes) {
		for (StateEnterTimesItem item : stateTimes) {
			if (item.getState().equals(StatName.DONE)) {
				stoptime = item.getTime().getTimeInMillis();
			} else if (item.getState().equals(StatName.RUNNING)) {
				starttime = item.getTime().getTimeInMillis();
			} else if (item.getState().equals(StatName.SUBMITTED)) {
				submissiontime = item.getTime().getTimeInMillis();
			} else if (item.getState().equals(StatName.CANCELLED)) {
				canceltime = item.getTime().getTimeInMillis();
			}

		}
	}

	/**
	 * Stop the job submitted by this class Independent from whether the WMS will actually cancel the job and report the
	 * CANCELLED state back, the JobStatus poll thread will terminate after a fixed number of job updates after this has
	 * been called. The time interval the lookup thread will still wait till cancelation ater calling stop() is defined
	 * in the UDPATE_INTV_AFTER_JOB_KILL variable in the JobStatusLookUp Thread. This has become necessary because some
	 * jobs would hang forever in a state even after calling the stop method.
	 * 
	 * @throws GATInvocationException an exception that might occurs
	 */
	public void stop() throws GATInvocationException {
		if (state == JobState.POST_STAGING || state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) {
			return;
		}

		try {
			getWmProxy().jobCancel(gliteJobID);
		} catch (Exception e) {
			throw new GATInvocationException("Could not cancel job!", e);
		}
	}

	/**
	 * @see org.gridlab.gat.advert.Advertisable#marshal()
	 */
	public String marshal() {
		SerializedJob sj;

		sj = new SerializedJob(getClass().getName(), jobDescription, sandbox, gliteJobID, submissiontime, starttime,
				stoptime);

		sj.setBrokerUri(wmsURL.toString());

		Sandbox sandbox = new Sandbox();
		sandbox.setSandbox(sandboxUri);
		sj.setSandbox(sandbox);

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
			getWmProxy();
			queryState();
			updateState();
		} catch (Exception e) {
			logger.error("Cannot refresh status for job!", e);
			throw new RuntimeException(e);
		}
		return this.state;
	}

	/**
	 * @see org.gridlab.gat.resources.cpi.JobCpi#getStdout()
	 */
	@Override
	public InputStream getStdout() throws GATInvocationException {
		FileInputStream retVal = null;

		try {
			GATContext newContext = new GATContext();
			newContext.addPreference("file.adaptor.name", "gridftp");

			GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext, vomsProxyPath);

			File stdOut = GAT.createFile(newContext, getFileOutputUri("std.out"));
			retVal = GAT.createFileInputStream(stdOut);
		} catch (Exception e) {
			throw new GATInvocationException("An error occurs during receifing the StdOut.", e);
		}

		return retVal;
	}

	/**
	 * @see org.gridlab.gat.resources.cpi.JobCpi#getStderr()
	 */
	@Override
	public InputStream getStderr() throws GATInvocationException {
		FileInputStream retVal = null;

		try {
			GATContext newContext = new GATContext();
			newContext.addPreference("file.adaptor.name", "gridftp");

			GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext, vomsProxyPath);

			File stdOut = GAT.createFile(newContext, getFileOutputUri("std.err"));
			retVal = GAT.createFileInputStream(stdOut);
		} catch (Exception e) {
			throw new GATInvocationException("An error occurs during receifing the StdOut.", e);
		}

		return retVal;
	}

	/***
	 * Return the output {@link URI} to a given filename
	 * 
	 * @param filename the filename for which to retrieve the URI
	 * @return the {@link URI} to a given output file
	 * 
	 * @throws OperationNotAllowedFaultException this exception occurs if the job is in a wrong state to retrieve output
	 *             informations.
	 * @throws Exception an other exception that might occurs
	 */
	private URI getFileOutputUri(String filename) throws OperationNotAllowedFaultException, Exception {
		URI stdErrUri = null;
		StringAndLongType[] list = null;

		StringAndLongList sl = getWmProxy().getOutputFileList(gliteJobID, "gsiftp");
		list = sl.getFile();

		for (int i = 0; i < list.length; i++) {
			URI uri1 = new URI(list[i].getName());
			URI uri2 = new URI(uri1.getScheme() + "://" + uri1.getHost() + ":" + uri1.getPort() + "//" + uri1.getPath());

			if (uri1.getRawPath().contains(filename)) {
				stdErrUri = uri2;
			}
		}

		return stdErrUri;
	}
}
