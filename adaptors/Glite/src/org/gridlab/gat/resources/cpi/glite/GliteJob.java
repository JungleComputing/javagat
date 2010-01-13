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
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.security.glite.GliteSecurityUtils;
import org.gridsite.www.namespaces.delegation_1.DelegationSoapBindingStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class GliteJob extends JobCpi {

    /**
     *  Logging and Bookkeeping service port
     */
    private final static int LB_PORT = 9003;
    
    /**
     * The Logger 
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(GliteJob.class);

    /**
     * Logging and Bookkeeping service url
     */
    private java.net.URL lbURL;
    
    /**
     * The job description
     */
    private JDL gLiteJobDescription;
    
    /**
     * The job software description
     */
    private SoftwareDescription swDescription;
    
    /**
     * The job state
     */
    private volatile String gLiteState = "";
    
    /** The {@link URL} to the Workload Management System (WMS) */
    private URL wmsURL = null;
    
    /** Path to the voms proxy certificate */
    private String proxyFile = null;
    
    /** is <code>true</code> if the Job is done and the poststaging has been done. */
    private boolean outputDone = false;
    
    /** A Metric for Monitoring */
    private Metric metric;
    
    /** The id of the job. */
    private final String gliteJobID;

    /** Web Service Port_Type */
    private WMProxy_PortType serviceStub = null;
    
    /** Web Service Stub */    
    private DelegationSoapBindingStub grstStub = null;
    
    /** Web Service Port_Type */    
    private LoggingAndBookkeepingPortType lbPortType = null;

    /** is <code>true</code> if the Job has been killed. */
    private boolean jobKilled = false;
    
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


    /**
     * Construct the service stubs necessary to communicate with the workload
     * management (WM) node
     * 
     * @param brokerURI
     *            The URI of the WM
     * @throws GATInvocationException
     */
    private void initWMSoapServices(final String brokerURI)
            throws GATInvocationException {
        try {

            // make it work with the axis services
            // the axis service will only accept the uri if the protocol is
            // known to them
            // while any:// is not known to them, https:// will work
            String axisBrokerURI = brokerURI.replaceFirst("any://", "https://");
            this.wmsURL = new URL(axisBrokerURI);

            // use engine configuration with settings hardcoded for a client
            // this seems to resolve multithreading issues
            WMProxyLocator serviceLocator = new WMProxyLocator(
                    new BasicClientConfig());

            serviceStub = serviceLocator.getWMProxy_PortType(wmsURL);

            grstStub = (DelegationSoapBindingStub) serviceLocator
                    .getWMProxyDelegation_PortType(wmsURL);

        } catch (MalformedURLException e) {
            throw new GATInvocationException("Broker URI is malformed!", e);
        } catch (ServiceException e) {
            throw new GATInvocationException(
                    "Could not get service stub for WMS-Node!", e);
        }
    }

    /**
     * Instantiate the logging and bookkeeping service classes, which are used
     * for status updates
     * 
     * @param jobIDWithLB
     *            the JobID from which the LB URL will be constructed
     */
    private void initLBSoapService(final String jobIDWithLB) {
        // instantiate the logging and bookkeeping service
        try {
            java.net.URL jobUrl = new java.net.URL(jobIDWithLB);
            lbURL = new java.net.URL(jobUrl.getProtocol(), jobUrl.getHost(),
                    LB_PORT, "/");

            // Set provider
            SimpleProvider provider = new SimpleProvider();
            SimpleTargetedChain c = null;
            c = new SimpleTargetedChain(new HTTPSSender());
            provider.deployTransport("https", c);
            c = new SimpleTargetedChain(new HTTPSender());
            provider.deployTransport("http", c);

            // get LB Stub
            LoggingAndBookkeepingLocator loc = new LoggingAndBookkeepingLocator(
                    provider);

            lbPortType = loc.getLoggingAndBookkeeping(lbURL);
        } catch (MalformedURLException e) {
            LOGGER
                    .error("Problem instantiating Logging and Bookkeeping service "
                            + e.toString());
        } catch (ServiceException e) {
            LOGGER
                    .error("Problem instantiating Logging and Bookkeeping service "
                            + e.toString());
        }
    }

    /**
     * @param gatContext
     * @param jobDescription
     * @param sandbox
     * @param brokerURI
     * @throws GATInvocationException
     * @throws GATObjectCreationException
     */
    protected GliteJob(final GATContext gatContext,
            final JobDescription jobDescription, final Sandbox sandbox,
            final String brokerURI) throws GATInvocationException,
            GATObjectCreationException {

        super(gatContext, jobDescription, sandbox);

        this.swDescription = this.jobDescription.getSoftwareDescription();

        // have to replace brokerURI parts that are not AXIS-compliant

        initWMSoapServices(brokerURI);

        if (swDescription.getExecutable() == null) {
            throw new GATInvocationException(
                    "The Job description does not contain an executable");
        }

        proxyFile = GliteSecurityUtils.touchVomsProxy(this.gatContext);

        Map<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        MetricDefinition statusMetricDefinition = new MetricDefinition(
                "job.status", MetricDefinition.DISCRETE, "String", null, null,
                returnDef);
        this.metric = new Metric(statusMetricDefinition, null);
        registerMetric("submitJob", statusMetricDefinition);

        // Create Job Description Language File ...
        long jdlID = System.currentTimeMillis();
        String voName = GliteConstants.getVO(gatContext);

        ResourceDescription rd = jobDescription.getResourceDescription();

        this.gLiteJobDescription = new JDL(jdlID, swDescription, voName, rd);

        String deleteOnExitStr = (String) gatContext.getPreferences().get(
                "glite.deleteJDL");

        // save the file only to disk if deleteJDL has not been specified
        if (!Boolean.parseBoolean(deleteOnExitStr)) {
            this.gLiteJobDescription.saveToDisk();
        }

        gliteJobID = submitJob();
        initLBSoapService(gliteJobID);
        LOGGER.info("jobID " + gliteJobID);
        // start status lookup thread
        // new Thread(new JobStatusLookUp(this)).start();
        //new JobStatusLookUp(this);
    }

    /**
     * The CA-certificate path is needed in the glite security JARs Get the
     * certificate path from the cog.properties file So the path to the CA
     * certificats should only be given once, in the cog.properties file
     * 
     * @author thomas
     */
    private void setCACerticateProperties() {

        CoGProperties properties = CoGProperties.getDefault();
        String certLocations = properties.getCaCertLocations();
        String caCerts[] = certLocations.split(",");

        if (!caCerts[0].endsWith(System.getProperty("file.separator"))) {
            caCerts[0] = caCerts[0]
                    .concat(System.getProperty("file.separator"));
        }

        String certsWithoutCRLs = caCerts[0] + "*.0";
        String caCRLs = caCerts[0] + "*.r0";
        gatContext.addPreference("CA-Certificates", certsWithoutCRLs);

        System.setProperty(ContextWrapper.CA_FILES, certsWithoutCRLs);
        System.setProperty(ContextWrapper.CRL_FILES, caCRLs);
        System.setProperty(ContextWrapper.CRL_REQUIRED, "false");

        /*
         * If the crl update interval is not set to 0s, timer tasks for crl
         * updates will be started in the background which are not terminated
         * appropriately. This means, each status update will create a new
         * daemon thread which will exist until the application terminates.
         * Since each such daemon thread requires ~ 300 KB of heap memory,
         * eventually an OutOfMemory error will be caused. So it is best to
         * leave this value at 0 seconds.
         */
        System.setProperty(ContextWrapper.CRL_UPDATE_INTERVAL, "0s");

    }

    /**
     * @param sandboxJobID
     */
    private void stageInSandboxFiles(String sandboxJobID) {
        List<File> sandboxFiles = new ArrayList<File>();
        GATContext newContext = (GATContext) gatContext.clone();
        newContext.addPreference("File.adaptor.name", "GridFTP");
        GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext);
        try {
            LOGGER.debug("Staging in files");
            if (swDescription.getStdin() != null) {
                File f = GAT.createFile(newContext, swDescription.getStdin()
                        .getName());
                sandboxFiles.add(f);
            }

            Map<File, File> map = swDescription.getPreStaged();

            for (File orig : map.keySet()) {
                File newF = GAT.createFile(newContext, orig.toGATURI());
                sandboxFiles.add(newF);
            }

            String[] sl = serviceStub.getSandboxDestURI(sandboxJobID, "gsiftp")
                    .getItem();

            for (File sandboxFile : sandboxFiles) {
                URI tempURI = new URI(sl[0] + "/" + sandboxFile.getName());
                URI destURI = new URI(tempURI.getScheme() + "://"
                        + tempURI.getHost() + ":" + tempURI.getPort() + "//"
                        + tempURI.getPath());
                LOGGER.debug("Uploading " + sandboxFile + " to " + destURI);
                File destFile = GAT.createFile(newContext, destURI);
                sandboxFile.copy(destFile.toGATURI());
            }
        } catch (URISyntaxException e) {
            LOGGER.error("URI error while resolving pre-staged file set", e);
        } catch (GATObjectCreationException e) {
            LOGGER.error("Could not create pre-staged file set", e);
        } catch (RemoteException e) {
            LOGGER.error("Problem while communicating with SOAP services", e);
        } catch (GATInvocationException e) {
            LOGGER.error("Could not copy files to input sandbox", e);
        }
    }

    /**
     * JobSubmit via API {@link ResourceBroker#submitJob(org.gridlab.gat.resources.AbstractJobDescription)}
     * 
     * @return the id of the job
     * @throws GATInvocationException An exception that migt occurs
     * @throws GATObjectCreationException An exception that might occurs
     */
    private String submitJob() throws GATInvocationException, GATObjectCreationException {

        LOGGER.debug("called submitJob");

        // set the CA-certificates
        setCACerticateProperties();

        System.setProperty("axis.socketSecureFactory",
                "org.glite.security.trustmanager.axis.AXISSocketFactory");
        System.setProperty("sslProtocol", "SSLv3");

        JobIdStructType jobIdStruct = null;

        try {
            String delegationId = "gatjob" + gLiteJobDescription.getJdlID();
            String certReq = grstStub.getProxyReq(delegationId);

            GrDProxyGenerator proxyGenerator = new GrDProxyGenerator();
            byte[] x509Cert = proxyGenerator.x509MakeProxyCert(certReq
                    .getBytes(), GrDPX509Util.getFilesBytes(new java.io.File(
                    proxyFile)), "");

            String proxyString = new String(x509Cert);
            grstStub.putProxy(delegationId, proxyString);

            String jdlString = gLiteJobDescription.getJdlString();
            jobIdStruct = serviceStub.jobRegister(jdlString, delegationId);

            stageInSandboxFiles(jobIdStruct.getId());

            serviceStub.jobStart(jobIdStruct.getId());
        } catch (IOException e) {
            LOGGER.error("Problem while copying input files to " + this.wmsURL,
                    e);
            throw new GATObjectCreationException(
                    GliteResourceBrokerAdaptor.GLITE_RESOURCE_BROKER_ADAPTOR, e);
        } catch (GeneralSecurityException e) {
            LOGGER.error("security problem while copying input files", e);
            throw new GATInvocationException(
                    GliteResourceBrokerAdaptor.GLITE_RESOURCE_BROKER_ADAPTOR, e);
        }

        return jobIdStruct.getId();
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
        if (outputDone) { // API is ready with POST STAGING
            this.gLiteState = "Cleared";
        } else {
            try {
                final JobStatus js = lbPortType.jobStatus(gliteJobID,
                        new JobFlags());
                final StatName state = js.getState();
                this.gLiteState = state.toString();
                this.destination = js.getDestination();
            } catch (GenericFault e) {
                LOGGER.error(e.toString());
            } catch (RemoteException e) {
                LOGGER
                        .error(
                                "gLite Error: LoggingAndBookkeeping service only works in glite 3.1 or higher",
                                e);
            }
        }
    }

    /**
	 * Update the status of the job.
	 * Depended on the requested state some action, like file staging or sandbox cleanup, has to be done. 
	 */
	private synchronized void updateState() {

        queryState();

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
        } else {
            this.state = Job.JobState.UNKNOWN;
        }
    }

    /**
     * Copies the output files like stdout and stderr to the defined URI via gridftp. 
     */
    public void receiveOutput() {
        StringAndLongType[] list = null;

        try {
            StringAndLongList sl = serviceStub.getOutputFileList(gliteJobID,
                    "gsiftp");
            list = sl.getFile();
        } catch (Exception e) {
            LOGGER
                    .error("Could not receive output due to security problems",
                            e);
        }

        if (list != null) {
            GATContext newContext = (GATContext) gatContext.clone();
            newContext.addPreference("File.adaptor.name", "GridFTP");
            // Remove all the existing contexts and use the new one..
            GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext);

            for (int i = 0; i < list.length; i++) {
                try {
                    URI uri1 = new URI(list[i].getName());
                    URI uri2 = new URI(uri1.getScheme() + "://"
                            + uri1.getHost() + ":" + uri1.getPort() + "//"
                            + uri1.getPath());

                    File f = GAT.createFile(newContext, uri2);
                    int name_begin = uri2.getPath().lastIndexOf('/') + 1;
                    File f2 = GAT.createFile(newContext, new URI(uri2.getPath()
                            .substring(name_begin)));

                    f.copy(destForPostStagedFile(f2));
                } catch (GATInvocationException e) {
                    postStageException = e;
                    LOGGER.error(e.toString());
                } catch (URISyntaxException e) {
                    postStageException = new GATInvocationException(e
                            .toString());
                    LOGGER
                            .error(
                                    "An error occured when building URIs for the poststaged files",
                                    e);
                } catch (GATObjectCreationException e) {
                    postStageException = new GATInvocationException(e
                            .toString());
                    LOGGER.error(
                            "Could not create GAT file when retrieving output",
                            e);
                }
            }
        }
        outputDone = true;
    }

    /**
     * Lookup the (local) destination to where the staged out file should be
     * copied
     * 
     * @param output
     *            The staged out file
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

            for (Map.Entry<File, File> psFile : postStagedFiles.entrySet()) {
                if (psFile != null && psFile.getValue() != null
                        && psFile.getKey() != null) {
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
     * Stop the job submitted by this class Independent from whether the WMS
     * will actually cancel the job and report the CANCELLED state back, the
     * JobStatus poll thread will terminate after a fixed number of job updates
     * after this has been called. The time interval the lookup thread will
     * still wait till cancelation ater calling stop() is defined in the
     * UDPATE_INTV_AFTER_JOB_KILL variable in the JobStatusLookUp Thread. This
     * has become necessary because some jobs would hang forever in a state even
     * after calling the stop method.
     */
    public void stop() throws GATInvocationException {
        if (state == JobState.POST_STAGING || state == JobState.STOPPED
                || state == JobState.SUBMISSION_ERROR) {
            return;
        }
        try {
            serviceStub.jobCancel(gliteJobID);
            jobKilled = true;

        } catch (Exception e) {
            throw new GATInvocationException("Could not cancel job!", e);
        }
    }

}
