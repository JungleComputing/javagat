package org.gridlab.gat.resources.cpi.glite;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.glite.security.delegation.GrDPConstants;
import org.glite.security.delegation.GrDPX509Util;
import org.glite.wms.wmproxy.JobIdStructType;
import org.glite.wms.wmproxy.StringAndLongList;
import org.glite.wms.wmproxy.StringAndLongType;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.security.glite.GliteSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GliteJobHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(GliteJobHelper.class);
	
	private final GATContext gatContext;
	
	private final String proxyFile;
	private final WMSService wmsService;

	
	public GliteJobHelper(GATContext gatContext, String brokerURI, String proxyFile, GSSCredential userCredential) throws GATInvocationException {
		this.gatContext = gatContext;
		this.wmsService = new WMSService(brokerURI, proxyFile, userCredential);
		this.proxyFile = proxyFile;
	}

//Try to avoid global system properties that cause a lot of problem in a multi-threaded environment!
//	/**
//	 * The CA-certificate path is needed in the glite security JARs Get the
//	 * certificate path from the cog.properties file So the path to the CA
//	 * certificates should only be given once, in the cog.properties file
//	 */
//	private void setCACerticateProperties() {
//
//		CoGProperties properties = CoGProperties.getDefault();
//		String certLocations = properties.getCaCertLocations();
//		String caCerts[] = certLocations.split(",");
//
//		if (!caCerts[0].endsWith(System.getProperty("file.separator"))) {
//			caCerts[0] = caCerts[0].concat(System.getProperty("file.separator"));
//		}
//
//		String certsWithoutCRLs = caCerts[0] + "*.0";
//		String caCRLs = caCerts[0] + "*.r0";
//		gatContext.addPreference("CA-Certificates", certsWithoutCRLs);
//
//		System.setProperty(ContextWrapper.CA_FILES, certsWithoutCRLs);
//		System.setProperty(ContextWrapper.CRL_FILES, caCRLs);
//		System.setProperty(ContextWrapper.CRL_REQUIRED, "false");
//
//		/*
//		 * If the crl update interval is not set to 0s, timer tasks for crl
//		 * updates will be started in the background which are not terminated
//		 * appropriately. This means, each status update will create a new
//		 * daemon thread which will exist until the application terminates.
//		 * Since each such daemon thread requires ~ 300 KB of heap memory,
//		 * eventually an OutOfMemory error will be caused. So it is best to
//		 * leave this value at 0 seconds.
//		 */
//		System.setProperty(ContextWrapper.CRL_UPDATE_INTERVAL, "0s");
//
//	}
	
	public void stageInSandboxFiles(String gliteJobID, SoftwareDescription softwareDescription) throws GATInvocationException {
		HashMap<File,String> sandboxFiles = new HashMap<File,String>();
		GATContext newContext = (GATContext) gatContext.clone();
		newContext.addPreference("File.adaptor.name", "GridFTP");
		GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext);
		try {
			LOGGER.debug("Staging in files");
			if (softwareDescription.getStdin() != null) {
				File f = GAT.createFile(newContext, softwareDescription.getStdin().getName());
				sandboxFiles.put(f,null);
			}

			Map<File, File> map = softwareDescription.getPreStaged();

			for (File orig : map.keySet()) {
				//Test if it is a file that comes from another job.
	        	if(!orig.getName().toLowerCase().startsWith("root.nodes.") 
	        			&& !orig.getName().toLowerCase().startsWith("root.inputsandbox")){
	        		File origFile = GAT.createFile(newContext, orig.toGATURI());
	        		String dest = null;
	        		if (map.get(orig) != null) {
	        			dest = map.get(orig).getPath();
	        		}
					sandboxFiles.put(origFile,dest);
	        	}
			}

			String[] sl = wmsService.getWMProxyServiceStub().getSandboxDestURI(gliteJobID, "gsiftp").getItem();

			for (File sandboxFile : sandboxFiles.keySet()) {
				URI tempURI;
				if(sandboxFiles.get(sandboxFile) == null){
					tempURI = new URI(sl[0] + "/" + sandboxFile.getName());
				}else{
					tempURI = new URI(sl[0] + "/" + sandboxFiles.get(sandboxFile));
				}
				URI destURI = new URI(tempURI.getScheme() + "://" + tempURI.getHost() + ":" + tempURI.getPort() + "//" + tempURI.getPath());
				LOGGER.debug("Uploading " + sandboxFile + " to " + destURI);
				File destFile = GAT.createFile(newContext, destURI);
				sandboxFile.copy(destFile.toGATURI());
			}
		} catch (URISyntaxException e) {
			throw new GATInvocationException("URI error while resolving pre-staged file set", e);
		} catch (GATObjectCreationException e) {
			throw new GATInvocationException("Could not create pre-staged file set", e);
		} catch (RemoteException e) {
			throw new GATInvocationException("Problem while communicating with SOAP services", e);
		} catch (GATInvocationException e) {
			throw new GATInvocationException("Could not copy files to input sandbox", e);
		}
	}
	
	public String delegateCredential() throws GATObjectCreationException{
		LOGGER.debug("Delegating credential for Job submition");
//Try to avoid global system properties that cause a lot of problem in a multi-threaded environment!
//		// set the CA-certificates
//		setCACerticateProperties();
		
//		System.setProperty("axis.socketSecureFactory", "org.glite.security.trustmanager.axis.AXISSocketFactory");
//		System.setProperty("sslProtocol", "SSLv3");
		
		String delegationId = "gatjob-"+UUID.randomUUID().toString();
		try {
			String certReq = wmsService.getDelegationServiceStub().getProxyReq(delegationId);

// This was not working properly if javaGAT is used inside a Globus container... 
// I don't know why so I bypassed it.
//			GrDProxyGenerator proxyGenerator = new GrDProxyGenerator();
//			byte[] x509Cert = proxyGenerator.x509MakeProxyCert(certReq.getBytes(), GrDPX509Util.getFilesBytes(new java.io.File(proxyFile)), "");
//
//			String proxyString = new String(x509Cert);
//			wmsService.getDelegationServiceStub().putProxy(delegationId, proxyString);
			
			X509Certificate[] userCerts = CertUtil.loadCertificates(proxyFile);
			PrivateKey key = new BouncyCastleOpenSSLKey(proxyFile).getPrivateKey();
			
			BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory.getDefault();
			
			X509Certificate certificate = factory.createCertificate(new ByteArrayInputStream(GrDPX509Util.readPEM(
                    new ByteArrayInputStream(certReq.getBytes()), GrDPConstants.CRH,
                    GrDPConstants.CRF)),userCerts[0], key, 12*3600, GSIConstants.GSI_2_PROXY); //12 hours proxy

			X509Certificate[] finalCerts = new X509Certificate[userCerts.length+1];
			finalCerts[0] = certificate;
			for (int index = 1; index <= userCerts.length; ++index){
				finalCerts[index] = userCerts[index - 1];
			}
			wmsService.getDelegationServiceStub().putProxy(delegationId, new String(GrDPX509Util.certChainToByte(finalCerts)));

		} catch (Exception e) {
			LOGGER.error("Problem while delegating a certificate to WMS: " + wmsService.getWmsURL(), e);
			throw new GATObjectCreationException(GliteResourceBrokerAdaptor.GLITE_RESOURCE_BROKER_ADAPTOR, e);
		}
		LOGGER.debug("Delegation DONE - ID: "+delegationId);
		return delegationId;
			
	}
	
	public JobIdStructType registerNewJob(AbstractJDL gLiteJobDescription, String delegationId) throws GATInvocationException{
		LOGGER.debug("Registering a new job with Delegation ID: "+ delegationId);
		String jdlString = gLiteJobDescription.getJdlString();
		
		LOGGER.debug("Register JDL Job: \n"+ jdlString);
		
		JobIdStructType jobIdStructType= null;
		try {
			jobIdStructType = wmsService.getWMProxyServiceStub().jobRegister(jdlString, delegationId);
		} catch (Exception e) {
			LOGGER.error("Problem while registering the new job: " + wmsService.getWmsURL(), e);
			throw new GATInvocationException("Problem while registering the new job: " + wmsService.getWmsURL(), e);
		}
		LOGGER.debug("New job registered: "+ jobIdStructType.getId());
		return jobIdStructType;
	}	
	
	// jobSubmit via API
	public void submitJob(JobIdStructType jobIdStructType) throws GATInvocationException {
		LOGGER.debug("Starting job "+ jobIdStructType.getId());
		try {
			wmsService.getWMProxyServiceStub().jobStart(jobIdStructType.getId());
		} catch (Exception e) {
			LOGGER.error("Problem starting the job " + jobIdStructType.getId() + " on "+wmsService.getWmsURL(), e);
			throw new GATInvocationException("Problem starting the job " + jobIdStructType.getId() + " on "+wmsService.getWmsURL(), e);
		}
		LOGGER.debug("Job started: "+ jobIdStructType.getId());
	}
	
	public synchronized GATInvocationException receiveOutput(JobIdStructType[] jobIdStructTypes, SoftwareDescription[] softwareDescriptions) {
		StringAndLongType[] list = null;
		GATInvocationException postStageException = null;
		for (int i = 0; i < jobIdStructTypes.length; i++) {
			try {
				StringAndLongList sl = wmsService.getWMProxyServiceStub().getOutputFileList(jobIdStructTypes[i].getId(), "gsiftp");
				list = sl.getFile();
			} catch (Exception e) {
				LOGGER.error("Could not receive output due to security problems", e);
				postStageException = new GATInvocationException(e.toString());
			}
		
			if (list != null) {
				GATContext newContext = (GATContext) gatContext.clone();
				newContext.addPreference("File.adaptor.name", "GridFTP");
				// Remove all the existing contexts and use the new one..
				GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext);
		
				for (int j = 0; j < list.length; j++) {
					try {
						URI uri1 = new URI(list[j].getName());
						URI uri2 = new URI(uri1.getScheme() + "://" + uri1.getHost() + ":" + uri1.getPort() + "//" + uri1.getPath());
		
						File f = GAT.createFile(newContext, uri2);
						int name_begin = uri2.getPath().lastIndexOf('/') + 1;
						File f2 = GAT.createFile(newContext, new URI(uri2.getPath().substring(name_begin)));
		
						f.copy(destForPostStagedFile(f2,softwareDescriptions));
					} catch (GATInvocationException e) {
						postStageException = e;
						LOGGER.error(e.toString());
					} catch (URISyntaxException e) {
						postStageException = new GATInvocationException(e.toString());
						LOGGER.error("An error occured when building URIs for the poststaged files", e);
					} catch (GATObjectCreationException e) {
						postStageException = new GATInvocationException(e.toString());
						LOGGER.error("Could not create GAT file when retrieving output", e);
					}
				}
			}
		}
		for (int i = 0; i < jobIdStructTypes.length; i++) {
			if(postStageException == null){
				try {
					wmsService.getWMProxyServiceStub().jobPurge(jobIdStructTypes[i].getId());
				} catch (Exception e) {
					LOGGER.error("Unable to purge the job!", e);
				}
			}
		}
		return postStageException;
	}
	
	/**
	 * Lookup the (local) destination to where the staged out file should be
	 * copied
	 * 
	 * @param output
	 *            The staged out file
	 * @return The URI on the local harddrive to where the file should be copied
	 */
	private URI destForPostStagedFile(File output, SoftwareDescription[] softwareDescriptions) {
		URI destURI = null;
		for (int i = 0; i < softwareDescriptions.length; i++) {
			Map<File, File> postStagedFiles = softwareDescriptions[i].getPostStaged();
			File stdout = softwareDescriptions[i].getStdout();
			File stderr = softwareDescriptions[i].getStderr();
			String outputName = output.getName();
	
			if (stdout != null && outputName.equals(stdout.getName())) {
				destURI = stdout.toGATURI();
			} else if (stderr != null && outputName.equals(stderr.getName())) {
				destURI = stderr.toGATURI();
			} else {
				
				for (Map.Entry<File, File> psFile : postStagedFiles.entrySet()) {
					if (psFile != null && psFile.getValue() != null && psFile.getKey() != null) {
						String psFileName = psFile.getValue().getName();
	
						if (outputName.equals(psFileName)) {
							destURI = psFile.getValue().toGATURI();
							break;
						}
					}
				}
			}
		}
		
		if (destURI == null) {
			destURI = output.toGATURI();
		}

		return destURI;
	}
	
	public JobState generateJobStateFromGLiteState(String gliteState){
		JobState state = null;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("gLite job state: " + gliteState);
                }
		if ("Waiting".equalsIgnoreCase(gliteState)) {
			state = Job.JobState.INITIAL;
		} else if ("Ready".equalsIgnoreCase(gliteState)) {
			state = Job.JobState.INITIAL;
		} else if ("Scheduled".equalsIgnoreCase(gliteState)) {
			state = Job.JobState.SCHEDULED;
		} else if ("Running".equalsIgnoreCase(gliteState)) {
			state = Job.JobState.RUNNING;
		} else if ("Done (Failed)".equalsIgnoreCase(gliteState)) {
			state = Job.JobState.SUBMISSION_ERROR;
		} else if ("Submitted".equalsIgnoreCase(gliteState)) {
			state = Job.JobState.INITIAL;
		} else if ("Aborted".equalsIgnoreCase(gliteState)) {
			state = Job.JobState.SUBMISSION_ERROR;
		} else if ("Done".equalsIgnoreCase(gliteState)) {
			state = Job.JobState.POST_STAGING;
		} else if ("Done (Success)".equalsIgnoreCase(gliteState)) {
			state = Job.JobState.POST_STAGING;
		} else if ("Cancelled".equalsIgnoreCase(gliteState)) {
			state = Job.JobState.SUBMISSION_ERROR;
		} else if ("Cleared".equalsIgnoreCase(gliteState)) {
			state = Job.JobState.STOPPED;
		} else {
			state = Job.JobState.UNKNOWN;
		}
		return state;
	}
	
	public void stop(JobIdStructType jobIdStructType) throws GATInvocationException{
		try {
			wmsService.getWMProxyServiceStub().jobCancel(jobIdStructType.getId());
		} catch (Exception e) {
			throw new GATInvocationException("Could not cancel job!", e);
		}
	}
	
}
