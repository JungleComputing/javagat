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

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.rpc.ServiceException;

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
import org.gridlab.gat.monitoring.MetricDefinition;
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


public class GliteJob extends JobCpi {
	
	private LoggingAndBookkeepingPortType lbService = null;
	private JDL gLiteJobDescription;
	private JobDescription jobDescription;
	private SoftwareDescription swDescription;
	private GATContext context;
	private long jdlFileId;
	private String jobID;
	private String gLiteState;
	private String gLiteUI = null;
	private String proxyFile = null;
	private boolean outputDone = false;

	private WMProxy_PortType serviceStub = null; 
	private DelegationSoapBindingStub grstStub = null; 
	
	 class JobStatusLookUp extends Thread {
		private GliteJob polledJob;
		 
		public JobStatusLookUp(GliteJob job) {
			this.polledJob = job;
			this.start();
		}

		public void run() {
			while (true) {
				if (state == Job.STOPPED)
					break;
				if (state == Job.SUBMISSION_ERROR)
					break;
				state = polledJob.getStatus();
				if (state == Job.POST_STAGING) {
					polledJob.receiveOutput();
					polledJob.outputDone = true;
				}
				
				try {
					sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	 
	protected GliteJob(GATContext gatContext, JobDescription jobDescription, Sandbox sandbox, String brokerURI)
			throws GATInvocationException {
		super(gatContext, jobDescription, sandbox);
		
		this.context = gatContext;
		this.gLiteUI = brokerURI;
		
		createVomsProxy();
		
		// Metric not used yet
		HashMap returnDef = new HashMap();
		returnDef.put("status", String.class);
		MetricDefinition statusMetricDefinition = new MetricDefinition(
				"job.status", MetricDefinition.DISCRETE, "String", null, null,
				returnDef);
		GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
	
	
		this.jobDescription = jobDescription;
		swDescription = this.jobDescription.getSoftwareDescription();
	
		// Create Job Description Language File ...
		jdlFileId = System.currentTimeMillis();
		String jdlFileName = new String("gatjob_" + jdlFileId + ".jdl");
		gLiteJobDescription = new JDL(jdlFileName, jdlFileId);
		createGliteJobDescContent();
	
		jobID = submitJob(jdlFileName);
		System.err.println("jobID " + jobID);

		// start status lookup thread
		new JobStatusLookUp(this);
	}

	private void createGliteJobDescContent() throws GATInvocationException {
		
		if (swDescription.getExecutable() == null) {
			throw new GATInvocationException(
					"The Job description does not contain an executable");
		}
		
		// ... and add content
		gLiteJobDescription.setExecutable(swDescription.getExecutable()
				.toString());
		if (context.getPreferences().get("VirtualOrganisation") != null)
			gLiteJobDescription.setVirtualOrganisation((String) context
					.getPreferences().get("VirtualOrganisation"));
		if (swDescription.getStdin() != null)
			gLiteJobDescription.setStdInputFile(swDescription.getStdin().getAbsolutePath());
		try {
			gLiteJobDescription.addInputFiles(swDescription.getPreStaged());
		} catch (Exception e1) {
			System.err.println(e1.toString());
		}
		gLiteJobDescription.addOutputFiles(swDescription.getPostStaged());
	
		// add stdOutput files
		if (swDescription.getStdout() != null)
			gLiteJobDescription.addOutputFile("std_" + jdlFileId + ".out",
					swDescription.getStdout().getName());
		else
			gLiteJobDescription.addOutputFile("std_" + jdlFileId + ".out");
	
		// add stdError files
		if (swDescription.getStderr() != null)
			gLiteJobDescription.addOutputFile("std_" + jdlFileId + ".err",
					swDescription.getStderr().getName());
		else
			gLiteJobDescription.addOutputFile("std_" + jdlFileId + ".err");
	
		// add environment and arguments
		if (swDescription.getEnvironment() != null)
			gLiteJobDescription.addEnviroment(swDescription.getEnvironment());
		if (swDescription.getArguments() != null)
			gLiteJobDescription.setArguments(swDescription.getArguments());
	
		// map GAT resource description to gLite glue schema and add it to
		// gLiteJobDescription
		ResourceDescription rd = jobDescription.getResourceDescription();
		GATResDescription2GlueSchema(rd.getDescription(), gLiteJobDescription);
	
		// creates the .jdl file
		gLiteJobDescription.create();
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
	private void createVomsProxy() throws GATInvocationException {
		CoGProperties properties = CoGProperties.getDefault();
		CertificateSecurityContext secContext = null;
		
		for (SecurityContext c : context.getSecurityContexts()) {
			if (c instanceof CertificateSecurityContext) {
				secContext = (CertificateSecurityContext) c;
			}
		}
		
		Preferences prefs = context.getPreferences();
		String userkey = secContext.getKeyfile().getPath();
		String usercert = secContext.getCertfile().getPath();
		String lifetimeStr = (String) prefs.get("vomsLifetime");
		
		int lifetime = 12*3600;
		
		if (lifetimeStr != null) {
			lifetime = Integer.parseInt(lifetimeStr);
		}
		
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
			
			this.proxyFile = System.getenv("X509_USER_PROXY");
			
			if (this.proxyFile == null) {
				this.proxyFile = properties.getProxyFile();
			}
			
			manager.saveProxyToFile(proxyFile);
			context.addPreference("globusCert", proxyFile); // for gridFTP adaptor
			System.setProperty("gridProxyFile", proxyFile);  // for glite security JARs
			
		} catch (Exception e) {
			throw new GATInvocationException("Could not create VOMS proxy!", e);
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
	private String submitJob(String jdlfileName)
			throws GATInvocationException {

		System.out.println("called APIsubmitJob");
				
		// set the CA-certificates
		setCACerticateProperties();

		System.setProperty("axis.socketSecureFactory",
				"org.glite.security.trustmanager.axis.AXISSocketFactory");
		System.setProperty("sslProtocol", "SSLv3");
		

		JobIdStructType jobId = null;
		try {
			URI wmsURI = new URI(gLiteUI);
			URL wmsURL = wmsURI.toURL();

			WMProxyLocator serviceLocator = new WMProxyLocator();
			serviceStub = serviceLocator.getWMProxy_PortType(wmsURL);
			grstStub = (DelegationSoapBindingStub) serviceLocator
					.getWMProxyDelegation_PortType(wmsURL);

			String delegationId = "gatjob" + jdlFileId;

			String certReq = grstStub.getProxyReq(delegationId);

			byte[] x509Cert = null;
			GrDProxyGenerator proxyGenerator = new GrDProxyGenerator();

			try {
				x509Cert = proxyGenerator
						.x509MakeProxyCert(certReq.getBytes(), GrDPX509Util
								.getFilesBytes(new java.io.File(proxyFile)), "");
			} catch (Exception e) {
				e.printStackTrace();
			}

			String proxyString = new String(x509Cert);

			grstStub.putProxy(delegationId, proxyString);

			FileReader fr = new FileReader(jdlfileName);

			StringWriter sw = new StringWriter();
			int c;
			while ((c = fr.read()) != -1)
				sw.write((char) c);
			fr.close();
			String jdlString = sw.toString();
			sw.close();

			// jobId = serviceStub.jobSubmit(jdlString, delegationId);
			jobId = serviceStub.jobRegister(jdlString, delegationId);

			String[] sl = serviceStub
					.getSandboxDestURI(jobId.getId(), "gsiftp").getItem();

			// copy input files
			URI tempURI;
			URI destURI;

			if (swDescription.getStdin() != null) {
				URI srcURI = new URI(swDescription.getStdin().getName());
				File f = GAT.createFile(context, srcURI);

				tempURI = new URI(sl[0] + "/"
						+ swDescription.getStdin().getName());
				destURI = new URI(tempURI.getScheme() + "://"
						+ tempURI.getHost() + ":" + tempURI.getPort() + "//"
						+ tempURI.getPath());
				File f2 = GAT.createFile(context, destURI);
				f.copy(f2.toGATURI());
			}

			Map<File, File> map = swDescription.getPreStaged();
			System.out.println("traversing prestaged files");
			Iterator it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				org.gridlab.gat.io.File srcFile = (org.gridlab.gat.io.File) entry
						.getKey();
				tempURI = new URI(sl[0] + "/" + srcFile.getName());
				destURI = new URI(tempURI.getScheme() + "://"
						+ tempURI.getHost() + ":" + tempURI.getPort() + "//"
						+ tempURI.getPath());
				File destFile = GAT.createFile(context, destURI);
				srcFile.copy(destFile.toGATURI());
			}

			serviceStub.jobStart(jobId.getId());

		} catch (IOException e) {
			e.printStackTrace();
		} catch (javax.xml.rpc.ServiceException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (GATObjectCreationException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return jobId.getId();
	}

	// Map the "GAT requirements" to the glue schema and add the requirements to
	// the gLiteJobDescription
	private void GATResDescription2GlueSchema(Map map, JDL gliteJobDescription) {
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			entry.getKey();
			entry.getValue();
			if ((String) entry.getKey() == "os.name")
				gLiteJobDescription
						.addRequirements("other.GlueHostOperatingSystemName ==  \""
								+ (String) entry.getValue() + "\"");
			if ((String) entry.getKey() == "os.release")
				gLiteJobDescription
						.addRequirements("other.GlueHostOperatingSystemRelease ==  \""
								+ (String) entry.getValue() + "\"");
			if ((String) entry.getKey() == "os.version")
				gLiteJobDescription
						.addRequirements("other.GlueHostOperatingSystemVersion ==  \""
								+ (String) entry.getValue() + "\"");
			if ((String) entry.getKey() == "os.type")
				gLiteJobDescription
						.addRequirements("other.GlueHostProcessorModel ==  \""
								+ (String) entry.getValue() + "\"");
			if ((String) entry.getKey() == "cpu.type")
				gLiteJobDescription
						.addRequirements("other.GlueHostProcessorModel == \""
								+ (String) entry.getValue() + "\"");
			if ((String) entry.getKey() == "machine.type")
				gLiteJobDescription
						.addRequirements("other.GlueHostProcessorModel ==  \""
								+ (String) entry.getValue() + "\"");
			if ((String) entry.getKey() == "machine.node")
				// other.GlueCEInfoHostName or other.GlueCEUniqueID ??
				gLiteJobDescription
						.addRequirements("other.GlueCEInfoHostName == \""
								+ (String) entry.getValue() + "\"");
			if ((String) entry.getKey() == "cpu.speed") {
				// gat: float & GHz
				// gLite: int & Mhz
				float gatspeed = new Float((String) entry.getValue());
				int gLiteSpeed = (int) (gatspeed * 1000);
				gLiteJobDescription
						.addRequirements("other.GlueHostProcessorClockSpeed >= "
								+ gLiteSpeed);
			}
			if ((String) entry.getKey() == "memory.size") {
				// gat: float & GB
				// gLite: int & MB
				float gatRAM = (Float) entry.getValue();
				int gLiteRAM = (int) (gatRAM * 1024);
				gLiteJobDescription
						.addRequirements("other.GlueHostMainMemoryRAMSize >= "
								+ gLiteRAM);
			}
			if ((String) entry.getKey() == "disk.size") {
				float gatDS = new Float((String) entry.getValue());
				int gLiteDS = (int) gatDS;
				gLiteJobDescription.addRequirements("other.GlueSESizeFree >= "
						+ gLiteDS); // or other.GlueSEUsedOnlineSize
			}
		}
	}

	public Map getInfo() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("state", getStateString(getState()));
		map.put("gLiteState", getGliteState());
		map.put("hostname", GATEngine.getLocalHostName());
		// map.put("resManName", "gLite");
		map.put("jobID", jobID);
		return map;
	}

	private int getStatus() {
		gLiteState = getGliteStatus();


		int state = Job.UNKNOWN;
		if (gLiteState.equalsIgnoreCase("Waiting"))
			state = Job.INITIAL;
		if (gLiteState.equalsIgnoreCase("Ready"))
			state = Job.INITIAL;
		if (gLiteState.equalsIgnoreCase("Scheduled"))
			state = Job.SCHEDULED;
		if (gLiteState.equalsIgnoreCase("Running"))
			state = Job.RUNNING;
		if (gLiteState.equalsIgnoreCase("Done (Failed)"))
			state = Job.SUBMISSION_ERROR;
		if (gLiteState.equalsIgnoreCase("Submitted"))
			state = Job.INITIAL;
		if (gLiteState.equalsIgnoreCase("Aborted"))
			state = Job.SUBMISSION_ERROR;
		if (gLiteState.equalsIgnoreCase("DONE")) // API only
			state = Job.POST_STAGING;
		if (gLiteState.equalsIgnoreCase("Done (Success)"))
			state = Job.POST_STAGING;
		if (gLiteState.equalsIgnoreCase("Canceled"))
			state = Job.SUBMISSION_ERROR;
		if (gLiteState.equalsIgnoreCase("Cleared"))
			state = Job.STOPPED;
		return state;
	}

	public int getState() {
		return state;
	}

	// via API
	private String getGliteStatus() {		
		if (outputDone) { // API is ready with POST STAGING
			return "Cleared";
		}

		// construct a logging and bookkeeping port type object, if it does not exist yet
		if (this.lbService == null) {
			try {
				/* (thomas) This has already been done and periodically checking the crls leads to massive heap memory usage
				 * and eventually to a OutOfMemoryError. Hence, don't check the CRLs again at every status lookup
				*/
				System.setProperty(ContextWrapper.CRL_ENABLED, "false");
				URL jobUrl = new URL(jobID);
				URL lbURL = new URL(jobUrl.getProtocol(), jobUrl.getHost(),
						9003, "/");
				LoggingAndBookkeepingLocator locator = new LoggingAndBookkeepingLocator();
				this.lbService = locator.getLoggingAndBookkeeping(lbURL);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				e.printStackTrace();
			}
		}
		
		JobStatus js = null;
		String gliteState = "";
			
		try {
			js = lbService.jobStatus(jobID, new JobFlags());
			gLiteState = js.getState().toString();
			// helps against memory leak?
			js = null;
		} catch (GenericFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			System.err.println("gLite Error: LoggingAndBookkeeping service only works in glite 3.1 or higher");
			e.printStackTrace();
		} 
		
		return gLiteState;
	}

	public String getGliteState() {
		return gLiteState;
	}

	
	// via API
	public void receiveOutput() {
		StringAndLongList sl;
		StringAndLongType[] list = null;
		
		try {

			String delegationId = "gatjob" + jdlFileId;

			String certReq = grstStub.getProxyReq(delegationId);

			byte[] x509Cert = null;
			GrDProxyGenerator proxyGenerator = new GrDProxyGenerator();

			try {
				x509Cert = proxyGenerator
						.x509MakeProxyCert(certReq.getBytes(), GrDPX509Util
								.getFilesBytes(new java.io.File(proxyFile)), "");
			} catch (Exception e) {
				e.printStackTrace();
			}

			String proxyString = new String(x509Cert);

			grstStub.putProxy(delegationId, proxyString);

			sl = serviceStub.getOutputFileList(jobID, "gsiftp");
			list = (StringAndLongType[]) sl.getFile();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				URI uri1;
				URI uri2;
				try {
					uri1 = new URI(list[i].getName());
					uri2 = new URI(uri1.getScheme()+"://" + uri1.getHost() + ":"
							+ uri1.getPort() + "//" + uri1.getPath());
					System.out.println(uri2.toString());

					File f = GAT.createFile(context, uri2);
					int name_begin = uri2.getPath().lastIndexOf('/') + 1;
					File f2 = GAT.createFile(context, new URI(uri2.getPath()
							.substring(name_begin)));
					
					//f.copy(destForPostStagedFile(f2));
					f.copy(f2.toGATURI());
				} catch (GATInvocationException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (GATObjectCreationException e) {
					e.printStackTrace();
				} 
			}
		}
		outputDone = true;
	}
	
	private URI destForPostStagedFile(File output) {
		Map<File, File> postStagedFiles = swDescription.getPostStaged();
		
		for (Map.Entry<File,File> psFile : postStagedFiles.entrySet()) {
			if (psFile != null && psFile.getValue() != null && psFile.getKey() != null) {
				if (psFile.getValue().getName().equals(output.getName())) {
					return psFile.getKey().toGATURI();
				}
			}
		}
		
		return output.toGATURI();
	}
}
