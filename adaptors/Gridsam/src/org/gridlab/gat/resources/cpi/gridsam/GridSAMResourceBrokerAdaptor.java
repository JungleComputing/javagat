package org.gridlab.gat.resources.cpi.gridsam;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
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

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("submitJob", true);

        return capabilities;
    }

    private Logger logger = Logger
            .getLogger(GridSAMResourceBrokerAdaptor.class);

    private ClientSideJobManager jobManager;

    private GridSAMConf gridSAMConf = new GridSAMConf();

    /**
     * This method constructs a LocalResourceBrokerAdaptor instance
     * corresponding to the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which will be used to broker resources
     */
    public GridSAMResourceBrokerAdaptor(GATContext gatContext,
            org.gridlab.gat.URI brokerURI) throws GATObjectCreationException {

        super(gatContext, brokerURI);

        if (!brokerURI.isCompatible("https")) {
            throw new AdaptorNotApplicableException("cannot handle this URI: "
                    + brokerURI);
        }
    }

    URI getBroker() {
        return brokerURI;
    }

    /**
     * This method attempts to reserve the specified hardware resource for the
     * specified time period. Upon reserving the specified hardware resource
     * this method returns a Reservation. Upon failing to reserve the specified
     * hardware resource this method returns an error.
     * 
     * @param resourceDescription
     *                A description, a HardwareResourceDescription, of the
     *                hardware resource to reserve
     * @param timePeriod
     *                The time period, a TimePeriod , for which to reserve the
     *                hardware resource
     */
    public Reservation reserveResource(ResourceDescription resourceDescription,
            TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method attempts to find one or more matching hardware resources.
     * Upon finding the specified hardware resource(s) this method returns a
     * java.util.List of HardwareResource instances. Upon failing to find the
     * specified hardware resource this method returns an error.
     * 
     * @param resourceDescription
     *                A description, a HardwareResoucreDescription, of the
     *                hardware resource(s) to find
     * @return java.util.List of HardwareResources upon success
     */
    public List<HardwareResource> findResources(
            ResourceDescription resourceDescription) {
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
    public Job submitJob(AbstractJobDescription abstractDescription,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {
        if (!(abstractDescription instanceof JobDescription)) {
            throw new GATInvocationException(
                    "can only handle JobDescriptions: "
                            + abstractDescription.getClass());
        }

        JobDescription description = (JobDescription) abstractDescription;

        // long start = System.currentTimeMillis();
        SoftwareDescription sd = description.getSoftwareDescription();

        GridSAMJSDLGenerator jsdlGenerator = new GridSAMJSDLGeneratorImpl(
                gridSAMConf);

        logger.info("starting job submit...");

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        URI uri = brokerURI;

        if (!"https".equals(brokerURI.getScheme())) {
            try {
                uri = brokerURI.setScheme("https");
            } catch (Throwable e) {
                logger.debug("Should not happen");
            }
        }

        String gridSAMWebServiceURL = uri.toString();
        String sandboxRoot = getSandboxRoot(gatContext.getPreferences());

        if (logger.isInfoEnabled()) {
            logger.info("url='" + gridSAMWebServiceURL + "'");
            logger.info("sandboxRoot=" + sandboxRoot);
        }

        JobInstance jobInstance = null;
        Sandbox sandbox = null;
        try {
            jobManager = new ClientSideJobManager(new String[] { "-s",
                    gridSAMWebServiceURL }, ClientSideJobManager
                    .getStandardOptions());

            // we have to add stdin/stderr/stdout to staged files
            addIOFiles(description);

            // sandbox host can be set explicitly. This allows us to
            // use ssh tunnels. For instance: localhost:4567
            // Fortunately, the sandbox code works with this.
            String sandboxHostname = (String) gatContext.getPreferences().get(
                    "ResourceBroker.sandbox.host");
            if (sandboxHostname == null) {
                sandboxHostname = getHostname();
                if (sandboxHostname == null || sandboxHostname.equals("")) {
                    throw new GATInvocationException(
                            "Could not get a hostname from "
                                    + gridSAMWebServiceURL);
                }
            }
            if (logger.isDebugEnabled()) {
                logger
                        .debug("host used for file staging is "
                                + sandboxHostname);
            }

            sandbox = new Sandbox(gatContext, description, sandboxHostname,
                    sandboxRoot, true, false, false, false);

            JobDefinitionDocument jobDefinitionDocument = jsdlGenerator.generate(description, sandbox);

            if (logger.isDebugEnabled()) {
                logger.debug("jobDefinitionDocument = "
                        + jobDefinitionDocument.toString());
            }

            // Take care of axis.ClientConfigFile system property: it may
            // be set by some Globus adaptor, but GridSAM cannot stand that.
            // So, save and restore it.
            String saved = System.getProperty("axis.ClientConfigFile");
            if (saved != null) {
                System.clearProperty("axis.ClientConfigFile");
            }

            sandbox.prestage();

            jobInstance = jobManager.submitJob(jobDefinitionDocument);

            if (saved != null) {
                System.setProperty("axis.ClientConfigFile", saved);
            }

            String jobID = jobInstance.getID();
            if (logger.isInfoEnabled()) {
                logger.info("jobID = " + jobID);
            }

        } catch (SubmissionException e) {
            logger.info("Got submission exception", e);
            throw new GATInvocationException(
                    "Unable to submit job to GridSAM server", e);
        } catch (JobManagerException e) {
            logger.error("gridSAM exception caught", e);
            throw new GATInvocationException("gridSAM exception caught", e);
        } catch (UnsupportedFeatureException e) {
            logger.error("gridSAM exception caught", e);
            throw new GATInvocationException("gridSAM exception caught", e);
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("unable to create IO files", e);
        }

        GridSAMJob job = new GridSAMJob(gatContext, description, sandbox, this,
                jobInstance);
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }

        return job;
    }

    private void addIOFiles(JobDescription description)
            throws GATObjectCreationException {
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd.getStdin() != null) {
            org.gridlab.gat.io.File stdin = GAT.createFile(gatContext,
                    gridSAMConf.getJavaGATStdin());
            sd.addPreStagedFile(sd.getStdin(), stdin);
            sd.getAttributes().put("stdin", gridSAMConf.getJavaGATStdin());
        }

        if (sd.getStdout() != null) {
            org.gridlab.gat.io.File stdout = GAT.createFile(gatContext,
                    gridSAMConf.getJavaGATStdout());
            sd.addPostStagedFile(stdout, sd.getStdout());
            sd.getAttributes().put("stdout", gridSAMConf.getJavaGATStdout());
        }

        if (sd.getStderr() != null) {
            org.gridlab.gat.io.File stderr = GAT.createFile(gatContext,
                    gridSAMConf.getJavaGATStderr());
            sd.addPostStagedFile(stderr, sd.getStderr());
            sd.getAttributes().put("stderr", gridSAMConf.getJavaGATStderr());
        }

    }

    private String getSandboxRoot(Preferences prefs)
            throws GATInvocationException {
        Object tmp = prefs.get("resourcebroker.sandbox.root");
        if (tmp == null || !(tmp instanceof String)) {
            // Default if user did not set this preference.
            tmp = "/tmp";
        }
        File fTmp = new File(tmp.toString());
        if (!fTmp.isAbsolute()) {
            logger
                    .info("resourcebroker.sandbox.root has to be an absolute path");
            throw new GATInvocationException(
                    "resourcebroker.sandbox.root has to be an absolute path");
        }
        return tmp.toString();
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
