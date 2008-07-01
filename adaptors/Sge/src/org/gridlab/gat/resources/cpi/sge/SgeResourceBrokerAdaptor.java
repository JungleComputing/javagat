/*
 * SGEBrokerAdaptor.java
 *
 * Created on May 16, 2006, 11:25 AM
 *
 */

package org.gridlab.gat.resources.cpi.sge;

// org.ggf.drmaa imports
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ggf.drmaa.AlreadyActiveSessionException;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * 
 * @author ole.weidner
 * 
 * renamed to SGEResourceBrokerAdaptor by Roelof Kemp in order to be consistent
 * with JavaGAT naming
 * 
 * added functionality (correct state notification, pre/poststaging,
 * environment, working directory)
 */

public class SgeResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("submitJob", true);

        return capabilities;
    }

    public static String getDescription() {
        return "This ResourceBroker uses the DRMAA interface to speak to the Sun Grid Engine. It can only be used on a machine, that has SGE installed. There might be link problems using this adaptor, because the adaptor depends on the SGE installation. In particular, the drmaa.jar that's included in the source (adaptors/Sge/external/drmaa.jar) should be compatible with the $SGE_ROOT/lib/$ARCH/libdrmaa.so, which means that they should be compiled for the same java version (also the 32/64 bit should be equal). Furthermore, this adaptor only works if it's executed using the same java version as the one used for compiling drmaa.jar. A small note about the arguments of the executable: the SGE adaptor automatically puts quotes around each argument.";
    }

    protected static Logger logger = Logger
            .getLogger(SgeResourceBrokerAdaptor.class);

    private Session SGEsession;

    public SgeResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws GATObjectCreationException {
        super(gatContext, brokerURI);
        SessionFactory factory = SessionFactory.getFactory();
        SGEsession = factory.getSession();
    }

    public Job submitJob(JobDescription description, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {

        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        if (sd.getAttributes().containsKey("cores.per.process")) {
            logger.info("ignoring attribute 'cores.per.process'");
        }

        String host = getHostname();

        Sandbox sandbox = null;

        /* Handle pre-/poststaging */
        if (host != null) {
            sandbox = new Sandbox(gatContext, description, host, null, true,
                    true, true, true);
        }
        SgeJob job = new SgeJob(gatContext, description, sandbox);
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }
        job.setHostname(getHostname());
        job.setState(Job.PRE_STAGING);
        job.setSession(SGEsession);
        sandbox.prestage();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        try {
            SGEsession.init(brokerURI.toString());
        } catch (DrmaaException e) {
            if (!(e instanceof AlreadyActiveSessionException)) {
                sandbox.retrieveAndCleanup(job);
                job.setState(Job.SUBMISSION_ERROR);
                throw new GATInvocationException("SGEResourceBrokerAdaptor", e);
            }
        }
        try {
            JobTemplate jt = SGEsession.createJobTemplate();
            jt.setRemoteCommand(getExecutable(description));
            jt.setWorkingDirectory(System.getProperty("user.home") + "/"
                    + sandbox.getSandbox());
            if (logger.isInfoEnabled()) {
                logger.info("Remote command: " + jt.getRemoteCommand());
            }
            if (sd.getArguments() != null) {
                jt.setArgs(sd.getArguments());
            }

            if (sd.getEnvironment() != null) {
                Properties environment = new Properties();
                for (String key : sd.getEnvironment().keySet()) {
                    environment.put(key, sd.getEnvironment().get(key));
                }
                jt.setJobEnvironment(environment);
            }

            if (sd.getStdout() != null) {
                jt.setOutputPath(host + ":" + sd.getStdout().getName());
            }

            if (sd.getStderr() != null) {
                jt.setErrorPath(host + ":" + sd.getStderr().getName());
            }
            if (sd.getAttributes().containsKey("host.count")) {
                jt.setNativeSpecification("-pe * " + getHostCount(description));
            } else {
                jt.setNativeSpecification("-pe * "
                        + getProcessCount(description));
            }

            job.setJobID(SGEsession.runJob(jt));
            job.setState(Job.SCHEDULED);
            job.startListener();
            SGEsession.deleteJobTemplate(jt);
            return job;
        } catch (DrmaaException e) {
            throw new GATInvocationException("SGEResourceBrokerAdaptor", e);
        }
    }
}
