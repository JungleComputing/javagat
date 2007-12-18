/*
 * SGEBrokerAdaptor.java
 *
 * Created on May 16, 2006, 11:25 AM
 *
 */

package org.gridlab.gat.resources.cpi.sge;

// org.ggf.drmaa imports
import java.util.List;

import org.apache.log4j.Logger;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
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

/**
 * 
 * @author ole.weidner
 * 
 * renamed to SGEResourceBrokerAdaptor by Roelof Kemp in order to be consistent
 * with JavaGAT naming
 */

public class SGEResourceBrokerAdaptor extends ResourceBrokerCpi {

    protected static Logger logger = Logger
            .getLogger(SGEResourceBrokerAdaptor.class);

    private Session SGEsession;

    public SGEResourceBrokerAdaptor(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        super(gatContext, preferences);

        SessionFactory factory = SessionFactory.getFactory();
        SGEsession = factory.getSession();

        // Properties props = System.getProperties();
        // props.setProperty("org.ggf.drmaa.SessionFactory","NONE");

    }

    public Reservation reserveResource(ResourceDescription resourceDescription,
            TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Reservation reserveResource(Resource resource, TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<HardwareResource> findResources(
            ResourceDescription resourceDescription) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Job submitJob(JobDescription description, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {

        SoftwareDescription sd = description.getSoftwareDescription();
        String host = getHostname(description);

        URI hostURI;
        Sandbox sandbox = null;

        try {
            hostURI = new URI(host);
        } catch (Exception e) {
            throw new GATInvocationException("SGEResourceBrokerAdaptor", e);
        }

        /* Handle pre-/poststaging */
        if (host != null) {
            sandbox = new Sandbox(gatContext, preferences, description, host,
                    null, false, true, true, true);
        }
        SGEJob job = new SGEJob(gatContext, preferences, description, sandbox);
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }
        job.setState(Job.PRE_STAGING);
        job.setSession(SGEsession);
        sandbox.prestage();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        try {

            SGEsession.init(hostURI.toString());
            JobTemplate jt = SGEsession.createJobTemplate();

            if (sd.getLocation() != null) {
                jt.setRemoteCommand(sd.getLocation().getPath());
            }
            if (logger.isInfoEnabled()) {
                logger.info("Remote command: " + jt.getRemoteCommand());
            }

            if (sd.getArguments() != null) {
                jt.setArgs(sd.getArguments());
            }

            if (sd.getStdout() != null) {
                jt.setOutputPath(host + ":" + sd.getStdout().getName());
            }

            job.setJobID(SGEsession.runJob(jt));
            job.setState(Job.SCHEDULED);
            job.startListener();

            SGEsession.deleteJobTemplate(jt);
            return job;
        } catch (DrmaaException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("exception in SGEResourceBrokerAdaptor");
                logger.debug(e);
            }
            throw new GATInvocationException("SGEResourceBrokerAdaptor", e);
        }
    }
}
