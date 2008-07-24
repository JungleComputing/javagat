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
import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.axis.configuration.BasicClientConfig;
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
	
	private LoggingAndBookkeepingPortType lbService = null;
	private JDL gLiteJobDescription;
	private SoftwareDescription swDescription;
	private String jobID;
	private String gLiteState;
	private String gLiteUI = null;
	private String proxyFile = null;
	private boolean outputDone = false;
	private Metric metric;

	private WMProxy_PortType serviceStub = null; 
	private DelegationSoapBindingStub grstStub = null; 
	
	
	 class JobStatusLookUp extends Thread {
		private GliteJob polledJob;
		private int pollIntMilliSec;
		 
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
				if (state == Job.STOPPED) {
					break;
				}
				if (state == Job.SUBMISSION_ERROR) {
					break;
				}
				
				polledJob.updateState();
				
				Map <String, Object> info = getInfo();
				MetricEvent event = new MetricEvent(polledJob, info, metric, System.currentTimeMillis());
				GATEngine.fireMetric(polledJob, event);
				
				
				if (state == Job.POST_STAGING) {
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
	 
	protected GliteJob(final GATContext gatContext, 
					   final JobDescription jobDescription, 
					   final Sandbox sandbox, 
					   final String brokerURI)
			throws GATInvocationException {
		
		super(gatContext, jobDescription, sandbox);
		this.gLiteUI = brokerURI;
		this.swDescription = this.jobDescription.getSoftwareDescription();
		
		if (swDescription.getExecutable() == null) {
			throw new GATInvocationException(
					"The Job description does not contain an executable");
		} 
		
		touchVomsProxy();
		
		Map<String, Object> returnDef = new HashMap<String, Object>();
		returnDef.put("status", String.class);
		MetricDefinition statusMetricDefinition = new MetricDefinition(
				"job.status", MetricDefinition.DISCRETE, "Map<String, Object>", null, null,
				returnDef);
		this.metric = new Metric(statusMetricDefinition, null);
		GATEngine.registerMetric(this, "submitJob", statusMetricDefinition);
	
		
		// Create Job Description Language File ...
		long jdlID = System.currentTimeMillis();
		String voName = (String) context.getPreferences().get("VirtualOrganisation");
		this.gLiteJobDescription = new JDL(jdlID,
									  	   swDescription,
									  	   voName,
									       jobDescription.getResourceDescription());
		
		String deleteOnExitStr = System.getProperty("glite.deleteJDL");

		// save the file only to disk if deleteJDL has not been specified
		if (!Boolean.parseBoolean(deleteOnExitStr)) {
			this.gLiteJobDescription.saveToDisk();
		}
		
		
		jobID = submitJob();
		logger.info("jobID " + jobID);
		
		// instantiate the logging and bookkeeping service
		try {
			URL jobUrl = new URL(jobID);
			URL lbURL = new URL(jobUrl.getProtocol(), jobUrl.getHost(), LB_PORT, "/");
			this.lbService = new LoggingAndBookkeepingLocator().getLoggingAndBookkeeping(lbURL);
		} catch (MalformedURLException e) {
			logger.error("Problem instantiating Logging and Bookkeeping service " + e.toString());
		} catch (ServiceException e) {
			logger.error(e.toString());
		}

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
		
		Preferences prefs = context.getPreferences();
		String lifetimeStr = (String) prefs.get("vomsLifetime");
		int lifetime = STANDARD_PROXY_LIFETIME;
		long existingLifetime = VomsProxyManager.getExistingProxyLifetime(proxyFile);
		
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
		
		Preferences prefs = context.getPreferences();
		
		// if the CA-Certificates property is not set, deduce it from the cog.properties file
		// since getCaCertLocations can return more than one location, split around the comma by which they are separated
		if (prefs.get("CA-Certificates") == null) {
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
			// set the credential update interval to two hours
			System.setProperty(ContextWrapper.CREDENTIALS_UPDATE_INTERVAL, "2h");
			System.setProperty(ContextWrapper.CRL_UPDATE_INTERVAL, "2h");
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
			URL wmsURL = new URL(gLiteUI);

			// use engine configuration with settings hardcoded for a client
			// this seems to resolve multithreading issues
			WMProxyLocator serviceLocator = new WMProxyLocator(new BasicClientConfig());
			serviceStub = serviceLocator.getWMProxy_PortType(wmsURL);
			
			grstStub = (DelegationSoapBindingStub) serviceLocator
					.getWMProxyDelegation_PortType(wmsURL);

			String delegationId = "gatjob" + gLiteJobDescription.getJdlID();
			String certReq = grstStub.getProxyReq(delegationId);
			
			GrDProxyGenerator proxyGenerator = new GrDProxyGenerator();
			byte[] x509Cert = proxyGenerator.x509MakeProxyCert(certReq.getBytes(), GrDPX509Util
															  .getFilesBytes(new java.io.File(proxyFile)), "");

			String proxyString = new String(x509Cert);
			grstStub.putProxy(delegationId, proxyString);

			String jdlString = gLiteJobDescription.getJdlString();
			jobId = serviceStub.jobRegister(jdlString, delegationId);

			String[] sl = serviceStub
					.getSandboxDestURI(jobId.getId(), "gsiftp").getItem();

			if (swDescription.getStdin() != null) {
				File f = GAT.createFile(context, swDescription.getStdin().getName());
				f.copy(generateGATURI(sl[0], swDescription.getStdin()));
			}

			Map<File, File> map = swDescription.getPreStaged();
			logger.info("traversing prestaged files");
			
			for (File srcFile : map.keySet()) {
				srcFile.copy(generateGATURI(sl[0], srcFile));
			}
			
			serviceStub.jobStart(jobId.getId());

		} catch (IOException e) {
			logger.error("Problem while copying input files", e);
		} catch (URISyntaxException e) {
			logger.error("URI error while resolving pre-staged file set", e);
		} catch (GATObjectCreationException e) {
			logger.error("Could not create pre-staged file set", e);
		} catch (ServiceException e) {
			logger.error("Could not get a SOAP connection for the required services", e);
		} catch (GeneralSecurityException e) {
			logger.error("Could not construct proxy byte sequence", e);
		}  
		
		return jobId.getId();
	}
	
	private org.gridlab.gat.URI generateGATURI(String destFragment, File srcFile) 
												throws URISyntaxException, GATObjectCreationException {
		URI tempURI = new URI(destFragment + "/" + srcFile.getName());
		URI destURI = new URI(tempURI.getScheme() + "://"
				+ tempURI.getHost() + ":" + tempURI.getPort() + "//"
				+ tempURI.getPath());
		File destFile = GAT.createFile(context, destURI);
		return destFile.toGATURI();
	}
	
	public Map<String, Object> getInfo() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("JavaGAT-state", getStateString(this.state));
		map.put("gLiteState", this.gLiteState);
		map.put("hostname", GATEngine.getLocalHostName());
		map.put("jobID", jobID);
		return map;
	}

	public int getState() {
		return state;
	}
	
	private void updateState() {		
		if (outputDone) { // API is ready with POST STAGING
			this.gLiteState = "Cleared";
		} else {

			/* (thomas) This has already been done and periodically checking the crls leads to massive heap memory usage
			 * and eventually to a OutOfMemoryError. Hence, don't check the CRLs again at every status lookup
			*/
			System.setProperty(ContextWrapper.CRL_ENABLED, "false");
			JobStatus js = null;
				
			try {
				js = lbService.jobStatus(jobID, new JobFlags());
				this.gLiteState = js.getState().toString();
			} catch (GenericFault e) {
				logger.error(e.toString());
			} catch (RemoteException e) {
				logger.error("gLite Error: LoggingAndBookkeeping service only works in glite 3.1 or higher", e);
			} 
		}
			
		this.state = Job.UNKNOWN;
		if ("Waiting".equalsIgnoreCase(gLiteState)) {
			state = Job.INITIAL;
		} else if ("Ready".equalsIgnoreCase(gLiteState)) {
			state = Job.INITIAL;
		} else if ("Scheduled".equalsIgnoreCase(gLiteState)) {
			state = Job.SCHEDULED;
		} else if ("Running".equalsIgnoreCase(gLiteState)) {
			state = Job.RUNNING;
		} else if ("Done (Failed)".equalsIgnoreCase(gLiteState)) {
			state = Job.SUBMISSION_ERROR;
		} else if ("Submitted".equalsIgnoreCase(gLiteState)) {
			state = Job.INITIAL;
		} else if ("Aborted".equalsIgnoreCase(gLiteState)) {
			state = Job.SUBMISSION_ERROR;
		} else if ("DONE".equalsIgnoreCase(gLiteState)) {
			state = Job.POST_STAGING;
		} else if ("Done (Success)".equalsIgnoreCase(gLiteState)) {
			state = Job.POST_STAGING;
		} else if ("Canceled".equalsIgnoreCase(gLiteState)) {
			state = Job.SUBMISSION_ERROR;
		} else if ("Cleared".equalsIgnoreCase(gLiteState)) {
			state = Job.STOPPED;
		}
		
	}
	
	// via API
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
					logger.error(e.toString());
				} catch (URISyntaxException e) {
					logger.error("An error occured when building URIs for the poststaged files", e);
				} catch (GATObjectCreationException e) {
					logger.error("Could not create GAT file when retrieving output", e);
				} 
			}
		}
		outputDone = true;
	}
	
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

	/** {@inheritDoc} */
    @Override
    public String getJobID() throws GATInvocationException {
        return this.jobID;
    }

}
