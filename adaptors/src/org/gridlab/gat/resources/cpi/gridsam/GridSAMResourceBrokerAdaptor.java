package org.gridlab.gat.resources.cpi.gridsam;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
import org.icenigrid.gridsam.core.SubmissionException;
import org.icenigrid.schema.jsdl.y2005.m11.JobDefinitionDocument;

public class GridSAMResourceBrokerAdaptor extends ResourceBrokerCpi {

    private Logger logger = Logger.getLogger(GridSAMResourceBrokerAdaptor.class);
    private ClientSideJobManager jobManager;

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
            jobManager = new ClientSideJobManager(new String[] { "-s", "https://localhost:18443/gridsam/services/gridsam" }, ClientSideJobManager
                    .getStandardOptions());
        }
        return jobManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(JobDescription description, MetricListener listener, String metricDefinitionName) throws GATInvocationException {
        long start = System.currentTimeMillis();
        SoftwareDescription sd = description.getSoftwareDescription();

        GridSAMJSDLGenerator jsdlGenerator = new GridSAMJSDLGeneratorImpl();

        logger.info("starting job submit...");

        if (sd == null) {
            throw new GATInvocationException("The job description does not contain a software description");
        }

        Map<String, Object> attributes = sd.getAttributes();

        // TODO using attributes for this might be hardcore but for no I find it
        // OK - just want something running
        ClientSideJobManager jobManager = null;
        JobInstance jobInstance = null;
        Sandbox sandbox = null;
        try {
            jobManager = getJobManager();

            // TODO something usefull
            // jsdlFileName =
            // "/home/wojciech/client/gridsam/data/examples/sleep.jsdl";

            sandbox = new Sandbox(gatContext, preferences, description, "das3.localhost:2280", "/tmp", true, false, false, false);

            String jsdl = jsdlGenerator.generate(sd, sandbox);
            JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.parse(jsdl);

            if (logger.isDebugEnabled()) {
                logger.debug("jobDefinitionDocument = " + jobDefinitionDocument.toString());
            }

            // sandbox = new Sandbox(gatContext, preferences, description,
            // "fs0.das3.cs.vu.nl:2280", null, true, false, false, false);

            jobInstance = jobManager.submitJob(jobDefinitionDocument);

            String jobID = jobInstance.getID();
            logger.info("jobID = " + jobID);

        } catch (SubmissionException e) {
            logger.error("Got submission exception: ", e);
            throw new GATInvocationException("Unable to submit job to GridSAM server", e);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        // return new GridSAMJob(gatContext, preferences, this, description, p,
        // host, sandbox, outForwarder, errForwarder, start, startRun);

        GridSAMJob job = new GridSAMJob(gatContext, preferences, description, sandbox, this, jobInstance);
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName).createMetric(null);
            job.addMetricListener(listener, metric);
        }

        return job;
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
