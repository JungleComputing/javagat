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
//		some changes and enhancements
//
////////////////////////////////////////////////////////////////////

// requires lb_1_5_3

package org.gridlab.gat.resources.cpi.glite;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.rpc.ServiceException;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.BasicClientConfig;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.glite.security.delegation.GrDPX509Util;
import org.glite.security.delegation.GrDProxyGenerator;
import org.glite.security.trustmanager.ContextWrapper;
import org.glite.wms.wmproxy.JobIdStructType;
import org.glite.wms.wmproxy.StringAndLongList;
import org.glite.wms.wmproxy.StringAndLongType;
import org.glite.wms.wmproxy.WMProxyLocator;
import org.glite.wms.wmproxy.WMProxy_PortType;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingLocator;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingPortType;
import org.glite.wsdl.types.lb.GenericFault;
import org.glite.wsdl.types.lb.JobFlags;
import org.glite.wsdl.types.lb.JobStatus;
import org.glite.wsdl.types.lb.StatName;
import org.globus.axis.transport.HTTPSSender;
import org.globus.common.CoGProperties;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.security.glite.VomsProxyManager;
import org.gridsite.www.namespaces.delegation_1.DelegationSoapBindingStub;


@SuppressWarnings("serial")
public class GliteJob extends JobCpi {
	
    private final static int LB_PORT = 9003;
	private final static int STANDARD_PROXY_LIFETIME = 12*3600;
	/** If a proxy is going to be reused it should at least have a remaining lifetime of 5 minutes */
	private final static int MINIMUM_PROXY_REMAINING_LIFETIME=5*60;
	
	private java.net.URL lbURL;
	private JDL gLiteJobDescription;
	private SoftwareDescription swDescription;
	private String jobID;
	private String gLiteState;
	private URL wmsURL = null;
	private String proxyFile = null;
	private boolean outputDone = false;
	private Metric metric;
	
	private WMProxy_PortType serviceStub = null; 
	private DelegationSoapBindingStub grstStub = null; 
	private LoggingAndBookkeepingPortType lbPortType = null;
	
	private boolean jobKilled = false;
	private long submissiontime = -1L;
	private long starttime = -1L;
	private long stoptime = -1L;
	private GATInvocationException postStageException = null;
	
	private GATContext context = null;
	
	
	 class JobStatusLookUp extends Thread {
		private GliteJob polledJob;
		private int pollIntMilliSec;
		private long afterJobKillCounter = 0;
		
		/** if the job has been stopped, allow the thread to still do update for this time interval terminating */
		final static long UPDATE_INTV_AFTER_JOB_KILL = 40000;
		 
		public JobStatusLookUp(final GliteJob job) {
			super();
					
			this.polledJob = job;
			
			String pollingIntervalStr = System.getProperty("glite.pollIntervalSecs");
			
			if (pollingIntervalStr == null) {
				this.pollIntMilliSec = 3000;
			} else {
				this.pollIntMilliSec = Integer.parseInt(pollingIntervalStr)*1000; 
			}
			
			this.start();
		}

		public void run() {
			while (true) {
				if (state == Job.JobState.STOPPED) {
					break;
				}
				if (state == Job.JobState.SUBMISSION_ERROR) {
					break;
				}
				
				// if the job has been killed and the maximum time at which the job should be canceled
				// has been reached, cancel
				if (jobKilled) {
					afterJobKillCounter += pollIntMilliSec;
					
					if (afterJobKillCounter >= UPDATE_INTV_AFTER_JOB_KILL) {
						break;
					}
				}
				
				polledJob.updateState();
				
				MetricEvent event = new MetricEvent(polledJob, state, metric, System.currentTimeMillis());
				GATEngine.fireMetric(polledJob, event);
				
				if (state == Job.JobState.POST_STAGING) {
					polledJob.receiveOutput();
					polledJob.outputDone = true;
				}
				
				try {
					sleep(this.pollIntMilliSec);
				} catch (InterruptedException e) {
					logger.error("Error while executing job status poller thread!", e);
				}
			}
		}
	}
	 
	 /**
	  * Construct the service stubs necessary to communicate with the workload management (WM)
	  * node
	  * @param brokerURI The URI of the WM
	  * @throws GATInvocationException
	  */
	private void initWMSoapServices(final String brokerURI) throws GATInvocationException {
		try {
			
			// make it work with the axis services
			// the axis service will only accept the uri if the protocol is known to them
			// while any:// is not known to them, https:// will work
			String axisBrokerURI = brokerURI.replaceFirst("any://", "https://");
			this.wmsURL = new URL(axisBrokerURI);
			
			// use engine configuration with settings hardcoded for a client
			// this seems to resolve multithreading issues
			WMProxyLocator serviceLocator = new WMProxyLocator(new BasicClientConfig());
			
			serviceStub = serviceLocator.getWMProxy_PortType(wmsURL);
			
			grstStub = (DelegationSoapBindingStub) serviceLocator
													.getWMProxyDelegation_PortType(wmsURL);
			
		} catch (MalformedURLException e) {
			throw new GATInvocationException("Broker URI is malformed!", e);
		} catch (ServiceException e) {
			throw new GATInvocationException("Could not get service stub for WMS-Node!", e);
		}
	}
	
	/**
	 * Instantiate the logging and bookkeeping service classes, which are used for
	 * status updates
	 * @param jobID the JobID from which the LB URL will be constructed
	 */
	private void initLBSoapService(final String jobID) {
		// instantiate the logging and bookkeeping service
		try {
			java.net.URL jobUrl = new java.net.URL(jobID);
			lbURL = new java.net.URL(jobUrl.getProtocol(), jobUrl.getHost(), LB_PORT, "/");
			
			// Set provider
			SimpleProvider provider = new SimpleProvider();
			SimpleTargetedChain c = null;
			c = new SimpleTargetedChain(new HTTPSSender());
			provider.deployTransport("https",c);
			c = new SimpleTargetedChain(new HTTPSender());
			provider.deployTransport("http",c);
			
			// get LB Stub
			LoggingAndBookkeepingLocator loc = new LoggingAndBookkeepingLocator(provider);
			
			lbPortType = loc.getLoggingAndBookkeeping(lbURL);
		} catch (MalformedURLException e) {
			logger.error("Problem instantiating Logging and Bookkeeping service " + e.toString());
		} catch (ServiceException e) {
			logger.error("Problem instantiating Logging and Bookkeeping service " + e.toString());
		} 
	}
	
 
	protected GliteJob(final GATContext gatContext, 
					   final JobDescription jobDescription, 
					   final Sandbox sandbox, 
					   final String brokerURI)
			throws GATInvocationException {
		
		super(gatContext, jobDescription, sandbox);
		
		context = gatContext;
		this.swDescription = this.jobDescription.getSoftwareDescription();
		
		// have to replace brokerURI parts that are not AXIS-compliant

		initWMSoapServices(brokerURI);
				
		if (swDescription.getExecutable() == null) {
			throw new GATInvocationException(
					"The Job description does not contain an executable");
		} 
		
		touchVomsProxy();
		
		Map<String, Object> returnDef = new HashMap<String, Object>();
		returnDef.put("status", String.class);
		MetricDefinition statusMetricDefinition = new MetricDefinition(
				"job.status", MetricDefinition.DISCRETE, "String", null, null,
				returnDef);
		this.metric = new Metric(statusMetricDefinition, null);
		GATEngine.registerMetric(this, "submitJob", statusMetricDefinition);
	
		
		// Create Job Description Language File ...
		long jdlID = System.currentTimeMillis();
		String voName = (String) context.getPreferences().get("VirtualOrganisation");
		
		ResourceDescription rd = jobDescription.getResourceDescription();
		
		this.gLiteJobDescription = new JDL(jdlID,
									  	   swDescription,
									  	   voName,
									       rd);
		
		String deleteOnExitStr = System.getProperty("glite.deleteJDL");

		// save the file only to disk if deleteJDL has not been specified
		if (!Boolean.parseBoolean(deleteOnExitStr)) {
			this.gLiteJobDescription.saveToDisk();
		}
		
		
		jobID = submitJob();
		initLBSoapService(jobID);
		logger.info("jobID " + jobID);
		// start status lookup thread
		new JobStatusLookUp(this);
	}


	/**
	 * Create a VOMS proxy (with ACs) and store on the position on 
	 * the filesystem indicated by the global X509_USER_PROXY variable.
	 * All the necessary parameters for the voms proxy creation such as
	 * path to the user certificate, user key, password, desired lifetime and server
	 * specific data such as host-dn, URL of the server and server port
	 * are expected to be given as global preferences to the gat context.
	 * User key and certificate location, as well as the key's password are expected to be
	 * given within a CertificateSecurityContext that is part of the GATContext.
	 * 
	 * <p>The preferences keys passed in the gatContext are expected to look as follows
	 * (with String as their datatype, also for port and lifetime):</p>
	 * 
	 * <table>
	 * <tr>
	 * <td>vomsLifetime</td><td>the desired proxy lifetime in seconds (optional)</td>
	 * </tr>
	 * <tr>
	 * <td>vomsHostDN</td><td>the distinguished name of the VOMS host, 
	 * (e.g. /DC=cz/DC=cesnet-ca/O=CESNET/CN=skurut19.cesnet.cz)</td>
	 * </tr>
	 * <tr>
	 * <td>vomsServerURL</td><td>the URL of the voms server, without protocol (e.g. skurut19.cesnet.cz)</td>
	 * </tr>
	 * <tr>
	 * <td>vomsServerPort</td><td>the port on which to connect to the voms server</td>
	 * </tr>
	 * <tr>
	 * <td>VirtualOrganisation</td><td>The name of the virtual organisation for which the voms proxy is created
	 * (e.g. voce)</td>
	 * </tr>
	 * </table>
	 * 
	 * @author thomas
	 */
	private void createVomsProxy(int lifetime) throws GATInvocationException {
		logger.info("Creating new VOMS proxy with lifetime (seconds): " + lifetime);
		
		CertificateSecurityContext secContext = null;
		
		if (context.getSecurityContexts() == null) {
			throw new GATInvocationException("Error: found no security contexts in GAT Context!");
		}
		
		for (SecurityContext c : context.getSecurityContexts()) {
			if (c instanceof CertificateSecurityContext) {
				secContext = (CertificateSecurityContext) c;
			}
		}
		
		Preferences prefs = context.getPreferences();
		String userkey = secContext.getKeyfile().getPath();
		String usercert = secContext.getCertfile().getPath();
		
		String hostDN = (String) prefs.get("vomsHostDN");
		String serverURI = (String) prefs.get("vomsServerURL");
		String serverPortStr = (String) prefs.get("vomsServerPort");
		int serverPort = Integer.parseInt(serverPortStr);
		
		String voName = (String) prefs.get("VirtualOrganisation");

		try {
			VomsProxyManager manager = new VomsProxyManager(usercert,
															userkey,
															secContext.getPassword(),
															lifetime,
															hostDN,
															serverURI,
															serverPort);
			manager.makeProxyCredential(voName);
			manager.saveProxyToFile(proxyFile);
			
		} catch (Exception e) {
			throw new GATInvocationException("Could not create VOMS proxy!", e);
		}
	}
	
	/**
	 * Create a new proxy or reuse the old one if the lifetime is still longer than the lifetime specified in
	 * the vomsLifetime preference OR, if the vomsLifetime preference is not specified, the remaining lifetime is
	 * longer than the MINIMUM_PROXY_REMAINING_LIFETIME specified in this class
	 * @throws GATInvocationException
	 */
	private void touchVomsProxy() throws GATInvocationException {
		CoGProperties properties = CoGProperties.getDefault();
		this.proxyFile = System.getenv("X509_USER_PROXY");
		
		if (this.proxyFile == null) {
			this.proxyFile = properties.getProxyFile();
		}
		
		context.addPreference("globusCert", proxyFile); // for gridFTP adaptor
		System.setProperty("gridProxyFile", proxyFile);  // for glite security JARs
		System.setProperty(ContextWrapper.CREDENTIALS_PROXY_FILE, proxyFile);
		
		Preferences prefs = context.getPreferences();
		String lifetimeStr = (String) prefs.get("vomsLifetime");
		int lifetime = STANDARD_PROXY_LIFETIME;
		
		boolean createNew = Boolean.parseBoolean(System.getProperty("glite.createNewProxy"));
		long existingLifetime = -1;
		
		// determine the lifetime of the existing proxy only if the user wants to reuse the
		// old proxy
		if (!createNew) {
			existingLifetime = VomsProxyManager.getExistingProxyLifetime(proxyFile);
		} 
		
		if (lifetimeStr == null) { // if a valid proxy exists, create a new one only if the old one is below the minimum lifetime
			if (existingLifetime < MINIMUM_PROXY_REMAINING_LIFETIME) {
				createVomsProxy(lifetime);
			} else {
				logger.info("Reusing old voms proxy with lifetime (seconds): " + existingLifetime);
			}
		} else { // if a valid proxy exists, create a new one only if the old one is below the specified lifetime
			lifetime = Integer.parseInt(lifetimeStr);
			
			if (existingLifetime < lifetime) {
				createVomsProxy(lifetime);
			} else  {
				logger.info("Reusing old voms proxy with lifetime (seconds): " + existingLifetime);
			}
		}	
	}
	
	/**
	 * The CA-certificate path is needed in the glite security JARs 
	 * Get the certificate path from the cog.properties file
	 * So the path to the CA certificats should only be given once, in the cog.properties file
	 * @param context The GATContext
	 * @author thomas
	 */
	private void setCACerticateProperties() {
		
		CoGProperties properties = CoGProperties.getDefault();
		String certLocations = properties.getCaCertLocations();
		String caCerts[] = certLocations.split(",");
		
		if (!caCerts[0].endsWith(System.getProperty("file.separator"))) {
			caCerts[0] = caCerts[0].concat(System.getProperty("file.separator"));
		}
		
		String certsWithoutCRLs = caCerts[0]  + "*.0";
		String caCRLs = caCerts[0] + "*.r0";
		context.addPreference("CA-Certificates", certsWithoutCRLs);

			
		System.setProperty(ContextWrapper.CA_FILES, certsWithoutCRLs);
		System.setProperty(ContextWrapper.CRL_FILES, caCRLs);
		System.setProperty(ContextWrapper.CRL_REQUIRED, "false");
		
		/* If the crl update interval is not set to 0s, timer tasks for
		   crl updates will be started in the background which are not terminated appropriately.
		   This means, each status update will create a new daemon thread which will exist
		   until the application terminates. Since each such daemon thread requires
		   ~ 300 KB of heap memory, eventually an OutOfMemory error will be caused.
		   So it is best to leave this value at 0 seconds.
	    */
		System.setProperty(ContextWrapper.CRL_UPDATE_INTERVAL, "0s");
		
	}
	
	private void stageInSandboxFiles(String jobID)  {
		List<File> sandboxFiles = new ArrayList<File>();
		
		try {
			logger.info("Staging in files");
			if (swDescription.getStdin() != null) {
				File f = GAT.createFile(context, swDescription.getStdin().getName());
				sandboxFiles.add(f);
			}
			
			Map<File, File> map = swDescription.getPreStaged();
			sandboxFiles.addAll(map.keySet());
		
			String[] sl = serviceStub
					.getSandboxDestURI(jobID, "gsiftp").getItem();
			
			for (File sandboxFile : sandboxFiles) {
				URI tempURI = new URI(sl[0] + "/" + sandboxFile.getName());
				URI destURI = new URI(tempURI.getScheme() + "://"
					+ tempURI.getHost() + ":" + tempURI.getPort() + "//"
					+ tempURI.getPath());
				File destFile = GAT.createFile(context, destURI);
				sandboxFile.copy(destFile.toGATURI());
			}
		} catch (URISyntaxException e) {
			logger.error("URI error while resolving pre-staged file set", e);
		} catch (GATObjectCreationException e) {
			logger.error("Could not create pre-staged file set", e);
		} catch (RemoteException e) {
			logger.error("Problem while communicating with SOAP services", e);
		} catch (GATInvocationException e) {
			logger.error("Could not copy files to input sandbox", e);
		}
	}
			
	// jobSubmit via API
	private String submitJob()
			throws GATInvocationException {

		logger.debug("called submitJob");
				
		// set the CA-certificates
		setCACerticateProperties();

		System.setProperty("axis.socketSecureFactory",
				"org.glite.security.trustmanager.axis.AXISSocketFactory");
		System.setProperty("sslProtocol", "SSLv3");
		

		JobIdStructType jobId = null;
		
		try {
			String delegationId = "gatjob" + gLiteJobDescription.getJdlID();
			String certReq = grstStub.getProxyReq(delegationId);
			
			GrDProxyGenerator proxyGenerator = new GrDProxyGenerator();
			byte[] x509Cert = proxyGenerator.x509MakeProxyCert(certReq.getBytes(), GrDPX509Util
															  .getFilesBytes(new java.io.File(proxyFile)), "");

			String proxyString = new String(x509Cert);
			grstStub.putProxy(delegationId, proxyString);

			String jdlString = gLiteJobDescription.getJdlString();
			jobId = serviceStub.jobRegister(jdlString, delegationId);
			
			stageInSandboxFiles(jobId.getId());
			
			serviceStub.jobStart(jobId.getId());

		} catch (IOException e) {
			logger.error("Problem while copying input files", e);
		} catch (GeneralSecurityException e) {
			logger.error("security problem while copying input files", e);
		}  
		
		return jobId.getId();
	}
	
	public Map<String, Object> getInfo() {
		Map<String, Object> map = new WeakHashMap<String, Object>();
		
		URL idURL = null;
		
		if (jobID == null) {
			map.put("hostname", null);
		} else {
			try {
				idURL = new URL(jobID);
				map.put("hostname", idURL.getHost());
			} catch (MalformedURLException e) {
				logger.error("Could not parse hostname from jobID", e);
				map.put("hostname", null);
			}
		}
		
		map.put("state", this.state);
		map.put("gLiteState", this.gLiteState);
		map.put("jobID", jobID);
		map.put("submissiontime", submissiontime);
		map.put("starttime", starttime);
		map.put("stoptime", stoptime);
		map.put("poststage.exception", postStageException);
		return map;
	}
	
	
	private void queryState() {
		if (outputDone) { // API is ready with POST STAGING
			this.gLiteState = "Cleared";
		} else {

			JobStatus js = null;
				
			try {
				js = lbPortType.jobStatus(jobID, new JobFlags());
				StatName state = js.getState();
				this.gLiteState = state.toString();
				js = null;
				state = null;
			} catch (GenericFault e) {
				logger.error(e.toString());
			} catch (RemoteException e) {
				logger.error("gLite Error: LoggingAndBookkeeping service only works in glite 3.1 or higher", e);
			} 
		}
	}
	
	private void updateState() {		
		
		queryState();
			
		this.state = Job.JobState.UNKNOWN;
		if ("Waiting".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.INITIAL;
		} else if ("Ready".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.INITIAL;
		} else if ("Scheduled".equalsIgnoreCase(gLiteState)) {
			
			// if state appears the first time, set the submission time appropriately
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
			
			state = Job.JobState.SUBMISSION_ERROR;
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
			state = Job.JobState.POST_STAGING;
		} else if ("Done (Success)".equalsIgnoreCase(gLiteState)) {
			
			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}
			
			state = Job.JobState.POST_STAGING;
		} else if ("Cancelled".equalsIgnoreCase(gLiteState)) {
			
			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}
			
			state = Job.JobState.SUBMISSION_ERROR;
		} else if ("Cleared".equalsIgnoreCase(gLiteState)) {
			
			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}
			
			state = Job.JobState.STOPPED;
		}
		
	}
	

	public void receiveOutput() {
		StringAndLongType[] list = null;
		
		try {
			StringAndLongList sl = serviceStub.getOutputFileList(jobID, "gsiftp");
			list = (StringAndLongType[]) sl.getFile();
		} catch (Exception e) {
			logger.error("Could not receive output due to security problems", e);
		}
		
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				try {
					URI uri1 = new URI(list[i].getName());
					URI uri2 = new URI(uri1.getScheme()+"://" + uri1.getHost() + ":"
							+ uri1.getPort() + "//" + uri1.getPath());

					File f = GAT.createFile(context, uri2);
					int name_begin = uri2.getPath().lastIndexOf('/') + 1;
					File f2 = GAT.createFile(context, new URI(uri2.getPath()
							.substring(name_begin)));
					
					f.copy(destForPostStagedFile(f2));
				} catch (GATInvocationException e) {
					postStageException = e;
					logger.error(e.toString());
				} catch (URISyntaxException e) {
					postStageException = new GATInvocationException(e.toString());
					logger.error("An error occured when building URIs for the poststaged files", e);
				} catch (GATObjectCreationException e) {
					postStageException = new GATInvocationException(e.toString());
					logger.error("Could not create GAT file when retrieving output", e);
				} 
			}
		}
		outputDone = true;
	}
	
	/**
	 * Lookup the (local) destination to where the staged out file should
	 * be copied
	 * @param output The staged out file
	 * @return The URI on the local harddrive to where the file should be copied
	 */
	private URI destForPostStagedFile(File output) {
		Map<File, File> postStagedFiles = swDescription.getPostStaged();
		File stdout = swDescription.getStdout();
		File stderr = swDescription.getStderr();
		String outputName = output.getName();
		URI destURI = null;
		
		if (stdout != null && outputName.equals(stdout.getName())) {
			destURI = stdout.toGATURI();
		} else if (stderr != null && outputName.equals(stderr.getName())) {
			destURI = stderr.toGATURI();
		} else {
		
			for (Map.Entry<File,File> psFile : postStagedFiles.entrySet()) {
				if (psFile != null && psFile.getValue() != null && psFile.getKey() != null) {
					String psFileName = psFile.getKey().getName();
					
					if (outputName.equals(psFileName)) {
						destURI = psFile.getValue().toGATURI();
						break;
					}
				}
			}
		}
		
		if (destURI == null) {
			destURI = output.toGATURI();
		}
		
		return destURI;
	}


    /**
     * Stop the job submitted by this class
     * Independent from whether the WMS will actually cancel the job and report the CANCELLED 
     * state back, the JobStatus poll thread will terminate after a fixed number of job updates
     * after this has been called.
     * The number of updates still done after calling stop()  is defined in the 
     * UDPATES_AFTER_JOB_KILL variable in the JobStatusLookUp Thread.
     * This has become necessary because some jobs would hang forever in a state
     * even after calling the stop method.
     */
    public void stop() throws GATInvocationException {;
    	
    	try {
			serviceStub.jobCancel(jobID);
			jobKilled = true;
			
		} catch (Exception e) {
			throw new GATInvocationException("Could not cancel job!", e);
		} 
    }

}
