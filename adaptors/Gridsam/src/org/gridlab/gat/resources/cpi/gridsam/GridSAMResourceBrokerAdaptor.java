package org.gridlab.gat.resources.cpi.gridsam;

import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.icenigrid.gridsam.client.common.ClientSideJobManager;
import org.icenigrid.gridsam.core.ConfigurationException;
import org.icenigrid.gridsam.core.JobInstance;
import org.icenigrid.gridsam.core.JobManagerException;
import org.icenigrid.gridsam.core.SubmissionException;
import org.icenigrid.gridsam.core.UnsupportedFeatureException;
import org.icenigrid.schema.jsdl.y2005.m11.JobDefinitionDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridSAMResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static String getDescription() {
        return "The Gridsam ResourceBroker Adaptor implements the ResourceBroker object on Gridsam.";
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("submitJob", true);

        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();
        preferences.put("gridsam.sandbox.host", "<no default>");
        preferences.put("gridsam.sandbox.root", "/tmp");
        return preferences;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "gridsam", "https"};
    }

    private Logger logger = LoggerFactory
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
    }

    URI getBroker() {
        return brokerURI;
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
        if (!(abstractDescription instanceof JobDescription)
                || abstractDescription instanceof WrapperJobDescription) {
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

        if (logger.isInfoEnabled()) {
            logger.info("url='" + gridSAMWebServiceURL + "'");
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
                    "gridsam.sandbox.host");
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
                    "/tmp", true, false, false, false);

            JobDefinitionDocument jobDefinitionDocument = jsdlGenerator
                    .generate(description, sandbox);

            if (logger.isDebugEnabled()) {
                logger.debug("jobDefinitionDocument = "
                        + jobDefinitionDocument.toString());
            }

            sandbox.prestage();

            jobInstance = jobManager.submitJob(jobDefinitionDocument);

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
        job.setSubmissionTime();

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
}
