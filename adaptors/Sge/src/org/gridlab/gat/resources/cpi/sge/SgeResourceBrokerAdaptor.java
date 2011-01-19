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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperJobCpi;

/**
 * 
 * @author ole.weidner
 * 
 *         renamed to SGEResourceBrokerAdaptor by Roelof Kemp in order to be
 *         consistent with JavaGAT naming
 * 
 *         added functionality (correct state notification, pre/poststaging,
 *         environment, working directory)
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

    public static String[] getSupportedSchemes() {
        return new String[] { "sge", "" };
    }

    protected static Logger logger = LoggerFactory
            .getLogger(SgeResourceBrokerAdaptor.class);

    private static Session SGEsession;

    private synchronized static Session getSession()
            throws GATObjectCreationException {

        if (SGEsession == null) {
            SessionFactory factory = SessionFactory.getFactory();
            SGEsession = factory.getSession();
            try {
                SGEsession.init("");
            } catch (DrmaaException e) {
                throw new GATObjectCreationException(
                        "SGEResourceBrokerAdaptor", e);
            }
        }

        return SGEsession;
    }
    
    public static void end() {
        if (logger.isDebugEnabled()) {
            logger.debug("SGEResourceBrokerAdaptor end");
        }

        if (SGEsession != null) {
            try {
                SGEsession.exit();
            } catch (DrmaaException e) {
                logger.debug("SGEsesion.exit gave exception", e);
            }
        }
    }

    public SgeResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws GATObjectCreationException {
        super(gatContext, brokerURI);
        SGEsession = getSession();
    }

    public Job submitJob(AbstractJobDescription abstractDescription,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {
        if (!(abstractDescription instanceof JobDescription)) {
            throw new GATInvocationException(
                    "can only handle JobDescriptions: "
                            + abstractDescription.getClass());
        }

        JobDescription description = (JobDescription) abstractDescription;

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
        sandbox = new Sandbox(gatContext, description, host, null, true, true,
                true, true);
        SgeJob sgeJob = new SgeJob(gatContext, description, sandbox);
        Job job = null;
        if (description instanceof WrapperJobDescription) {
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, sgeJob, listener,
                    metricDefinitionName);
            listener = tmp;
            job = tmp;
        } else {
            job = sgeJob;
        }
        if (listener != null && metricDefinitionName != null) {
            Metric metric = sgeJob.getMetricDefinitionByName(
                    metricDefinitionName).createMetric(null);
            sgeJob.addMetricListener(listener, metric);
        }

        sgeJob.setHostname(getHostname());
        sgeJob.setState(Job.JobState.PRE_STAGING);
        sgeJob.setSession(SGEsession);
        if (sandbox != null) {
            sandbox.prestage();
        }

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        try {
            JobTemplate jt = SGEsession.createJobTemplate();
            jt.setRemoteCommand(getExecutable(description));
            if (sandbox != null) {
        	String s = sandbox.getSandbox();
        	if (s.startsWith("/")) {
        	    jt.setWorkingDirectory(s);
        	} else {
        	    jt.setWorkingDirectory(System.getProperty("user.home") + "/"
        		    + sandbox.getSandbox());
        	}
            }
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
            
            long maxTime = getLongAttribute(description, SoftwareDescription.TIME_MAX, -1);
	    String toNative = "";
            if (maxTime > 0) {
        	// jt.setHardRunDurationLimit(maxTime * 60);	not supported on SGE?
        	// jt.setSoftRunDurationLimit(maxTime * 60);	not supported on SGE?
            }

            long maxWallTime = getLongAttribute(description, SoftwareDescription.WALLTIME_MAX, -1);
            if (maxWallTime > 0) {
        	// jt.setHardWallclockTimeLimit(maxWallTime * 60);  not supported by SGE?
        	// jt.setSoftWallclockTimeLimit(maxWallTime * 60);  not supported by SGE?
		toNative += "-l h_rt=" + (maxWallTime*60) + " ";
            }
            
            String queue = getStringAttribute(description, SoftwareDescription.JOB_QUEUE, null);
            if (queue != null) {
        	toNative += "-l qname=" + queue + " ";
            }

            if (sd.getStdout() != null) {
                jt.setOutputPath(host + ":" + sd.getStdout().getName());
            }

            if (sd.getStderr() != null) {
                jt.setErrorPath(host + ":" + sd.getStderr().getName());
            }
	    toNative += "-pe * " + description.getResourceCount();

            jt.setNativeSpecification(toNative);

            if (logger.isDebugEnabled()) {
        	logger.debug("Starting SGE job: " + jt);
            }
            sgeJob.setJobID(SGEsession.runJob(jt));
            sgeJob.setState(Job.JobState.SCHEDULED);
            sgeJob.startListener();
            SGEsession.deleteJobTemplate(jt);
            return job;
        } catch (DrmaaException e) {
            throw new GATInvocationException("SGEResourceBrokerAdaptor", e);
        }
    }
}
