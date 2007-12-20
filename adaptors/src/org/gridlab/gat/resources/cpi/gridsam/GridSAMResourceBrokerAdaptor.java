package org.gridlab.gat.resources.cpi.gridsam;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.icenigrid.gridsam.client.common.ClientSideJobManager;
import org.icenigrid.gridsam.core.ConfigurationException;
import org.icenigrid.gridsam.core.JobInstance;
import org.icenigrid.gridsam.core.JobManagerException;
import org.icenigrid.gridsam.core.SubmissionException;
import org.icenigrid.gridsam.core.UnsupportedFeatureException;
import org.icenigrid.schema.jsdl.y2005.m11.JobDefinitionDocument;

public class GridSAMResourceBrokerAdaptor extends ResourceBrokerCpi {

    private static final String SANDBOX_ROOT = "sandboxRoot";
    private Logger logger = Logger.getLogger(GridSAMResourceBrokerAdaptor.class);
    private ClientSideJobManager jobManager;
    private GridSAMConf gridSAMConf = new GridSAMConf();

    /**
     * This method constructs a LocalResourceBrokerAdaptor instance
     * corresponding to the passed GATContext.
     * 
     * @param gatContext
     *            A GATContext which will be used to broker resources
     */
    public GridSAMResourceBrokerAdaptor(GATContext gatContext, Preferences preferences) throws GATObjectCreationException {
        super(gatContext, preferences);
        System.out.println("gridsam starting...");
    }

    /**
     * This method attempts to reserve the specified hardware resource for the
     * specified time period. Upon reserving the specified hardware resource
     * this method returns a Reservation. Upon failing to reserve the specified
     * hardware resource this method returns an error.
     * 
     * @param resourceDescription
     *            A description, a HardwareResourceDescription, of the hardware
     *            resource to reserve
     * @param timePeriod
     *            The time period, a TimePeriod , for which to reserve the
     *            hardware resource
     */
    public Reservation reserveResource(ResourceDescription resourceDescription, TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method attempts to find one or more matching hardware resources.
     * Upon finding the specified hardware resource(s) this method returns a
     * java.util.List of HardwareResource instances. Upon failing to find the
     * specified hardware resource this method returns an error.
     * 
     * @param resourceDescription
     *            A description, a HardwareResoucreDescription, of the hardware
     *            resource(s) to find
     * @return java.util.List of HardwareResources upon success
     */
    public List<HardwareResource> findResources(ResourceDescription resourceDescription) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public ClientSideJobManager getJobManager() throws ConfigurationException {
        if (jobManager == null) {
            throw new RuntimeException("no jobManager found");
        }
        return jobManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(JobDescription description, MetricListener listener, String metricDefinitionName) throws GATInvocationException {
//        long start = System.currentTimeMillis();
        SoftwareDescription sd = description.getSoftwareDescription();

        GridSAMJSDLGenerator jsdlGenerator = new GridSAMJSDLGeneratorImpl(gridSAMConf);

        logger.info("starting job submit...");

        if (sd == null) {
            throw new GATInvocationException("The job description does not contain a software description");
        }

        String gridSAMWebServiceURL = getGridSAMWebServiceURL(description);
        String sandboxRoot = getSandboxRoot(description);
        
        if (logger.isInfoEnabled()) {
            logger.info("url='" + gridSAMWebServiceURL + "'; sandboxRoot=" + sandboxRoot);
        }

        JobInstance jobInstance = null;
        Sandbox sandbox = null;
        try {
            jobManager = new ClientSideJobManager(new String[] {"-s", gridSAMWebServiceURL}, ClientSideJobManager.getStandardOptions());
            
            // we have to add stdin/stderr/stdout to staged files
            addIOFiles(description);

            // standard getHostname(JobDescription description) method failse for URLs with port number in it...
            String sandboxHostname = getHostname(gridSAMWebServiceURL);
            if (logger.isDebugEnabled()) {
                logger.debug("host used for file staging is " + sandboxHostname);
            }
            sandbox = new Sandbox(gatContext, preferences, description, sandboxHostname, sandboxRoot, true, false, false, false);
            
            String jsdl = jsdlGenerator.generate(description, sandbox);
            JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.parse(jsdl);

            if (logger.isDebugEnabled()) {
                logger.debug("jobDefinitionDocument = " + jobDefinitionDocument.toString());
            }

            jobInstance = jobManager.submitJob(jobDefinitionDocument);

            String jobID = jobInstance.getID();
            if (logger.isInfoEnabled()) {
                logger.info("jobID = " + jobID);
            }

        } catch (SubmissionException e) {
            logger.info("Got submission exception", e);
            throw new GATInvocationException("Unable to submit job to GridSAM server", e);
        } catch (JobManagerException e) {
            logger.error("gridSAM exception caught", e);
            throw new GATInvocationException("gridSAM exception caught", e);
        } catch (UnsupportedFeatureException e) {
            logger.error("gridSAM exception caught", e);
            throw new GATInvocationException("gridSAM exception caught", e);
        } catch (XmlException e) {
            logger.error("unable to parse jsdl", e);
            throw new GATInvocationException("unable to parse jsdl", e);
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("unable to create IO files", e);
        }

        GridSAMJob job = new GridSAMJob(gatContext, preferences, description, sandbox, this, jobInstance);
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName).createMetric(null);
            job.addMetricListener(listener, metric);
        }

        return job;
    }

    private String getHostname(String gridSAMWebServiceURL) throws GATInvocationException {
        try {
            URI u = new URI(gridSAMWebServiceURL);
            String host = u.getHost();
            if (host == null || host.length() == 0) {
                logger.error("unable to get hostname from url, url=" + gridSAMWebServiceURL);
                throw new GATInvocationException("unable to get hostname from url, url=" + gridSAMWebServiceURL);
            }
            return host;
        } catch (URISyntaxException e) {
            logger.error("unable to get hostname from url, url=" + gridSAMWebServiceURL, e);
            throw new GATInvocationException("unable to get hostname from url, url=" + gridSAMWebServiceURL, e);
        }
        
    }

    private void addIOFiles(JobDescription description) throws GATObjectCreationException {
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd.getStdin() != null) {
            org.gridlab.gat.io.File stdin = GAT.createFile(gatContext, gridSAMConf.getJavaGATStdin());
            sd.addPreStagedFile(sd.getStdin(), stdin);
            sd.getAttributes().put("stdin", gridSAMConf.getJavaGATStdin());
        }
        
        if (sd.getStdout() != null) {
            org.gridlab.gat.io.File stdout = GAT.createFile(gatContext, gridSAMConf.getJavaGATStdout());
            sd.addPostStagedFile(stdout, sd.getStdout());
            sd.getAttributes().put("stdout", gridSAMConf.getJavaGATStdout());
        }
        
        if (sd.getStderr() != null) {
            org.gridlab.gat.io.File stderr = GAT.createFile(gatContext, gridSAMConf.getJavaGATStderr());
            sd.addPostStagedFile(stderr, sd.getStderr());
            sd.getAttributes().put("stderr", gridSAMConf.getJavaGATStderr());
        }
        
    }

    private String getGridSAMWebServiceURL(JobDescription description) throws GATInvocationException {
        //Map<String, Object> attributes = description.getSoftwareDescription().getAttributes();
        Object url = preferences.get("ResourceBroker.jobmanagerContact");
        if (url == null || ! (url instanceof String)) {
            logger.info("no url to gridsam web service set");
            throw new GATInvocationException("no url to gridsam web service set");
        }
        return (String) url;
    }

    private String getSandboxRoot(JobDescription description) throws GATInvocationException {
        Object tmp = getAttribute(description, SANDBOX_ROOT);
        if (tmp == null || ! (tmp instanceof String)) {
            logger.info("unable to get sandbox root directory");
            throw new GATInvocationException("unable to get sandbox root directory");
        }
        File fTmp = new File(tmp.toString());
        if (! fTmp.isAbsolute()) {
            logger.info("sandboxRoot has to be an absolute path");
            throw new GATInvocationException("sandboxRoot has to be an absolute path");
        }
        return tmp.toString();
    }

    private Object getAttribute(JobDescription description, String name) {
        if (description.getSoftwareDescription().getAttributes() == null) {
            return null;
        }
        return description.getSoftwareDescription().getAttributes().get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#reserveResource(org.gridlab.gat.resources.Resource,
     *      org.gridlab.gat.engine.util.TimePeriod)
     */
    public Reservation reserveResource(Resource resource, TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
