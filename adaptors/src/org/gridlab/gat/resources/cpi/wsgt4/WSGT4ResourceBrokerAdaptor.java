package org.gridlab.gat.resources.cpi.wsgt4;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.exec.generated.JobDescriptionType;
import org.globus.exec.utils.rsl.RSLHelper;
import org.globus.exec.utils.rsl.RSLParseException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperSubmitter;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;

/**
 * Implements the <code>ResourceBrokerCpi</code> abstract class.
 * 
 * @author Balazs Bokodi
 * @version 1.0
 * @since 1.0
 */
public class WSGT4ResourceBrokerAdaptor extends ResourceBrokerCpi {
    protected static Logger logger = Logger
            .getLogger(WSGT4ResourceBrokerAdaptor.class);

    private WrapperSubmitter submitter;
    static final int DEFAULT_GRIDFTP_PORT = 2811;

    protected GSSCredential getCred(JobDescription jobDescription)
            throws GATInvocationException {
        GSSCredential cred = null;
        URI location = null;
        try {
            location = new URI(getHostname());
        } catch (Exception e) {
            throw new GATInvocationException(
                    "WSGT4Job: getSecurityContext, initialization of location failed, "
                            + e);
        }
        try {
            cred = GlobusSecurityUtils.getGlobusCredential(gatContext,
                    preferences, "globus", location, DEFAULT_GRIDFTP_PORT);
        } catch (Exception e) {
            throw new GATInvocationException(
                    "WSGT4Job: could not initialize credentials, " + e);
        }
        return cred;
    }

    public WSGT4ResourceBrokerAdaptor(GATContext gatContext,
            Preferences preferences, URI brokerURI)
            throws GATObjectCreationException {
        super(gatContext, preferences, brokerURI);
        String globusLocation = System.getenv("GLOBUS_LOCATION");
        if (globusLocation == null) {
            throw new GATObjectCreationException("$GLOBUS_LOCATION is not set");
        }
        System.setProperty("GLOBUS_LOCATION", globusLocation);
        System.setProperty("axis.ClientConfigFile", globusLocation
                + "/client-config.wsdd");
    }

    protected String createRSL(JobDescription description, Sandbox sandbox, boolean useGramSandbox)
            throws GATInvocationException {
        String rsl = new String("<job>");
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        rsl += "<executable>";
        rsl += getExecutable(description);
        rsl += "</executable>";
        Map<String, Object> env = sd.getEnvironment();
        if (env != null && !env.isEmpty()) {
            Set<String> s = env.keySet();
            Object[] keys = (Object[]) s.toArray();

            for (int i = 0; i < keys.length; i++) {
                String val = (String) env.get(keys[i]);
                rsl += "<environment>";
                rsl += "<name>" + keys[i] + "</name>";
                rsl += "<value>" + val + "</value>";
                rsl += "</environment>";
            }
        }

        String[] argsA = getArgumentsArray(description);

        if (argsA != null) {
            for (int i = 0; i < argsA.length; i++) {
                rsl += "<argument>";
                rsl += argsA[i];
                rsl += "</argument>";
            }
        }

        // set the environment
        rsl += "<count>";
        rsl += getCPUCount(description);
        rsl += "</count>";
        rsl += "<directory>";
        if (sandbox.getSandbox().startsWith(File.separator)) {
        		rsl += sandbox.getSandbox();
        } else {
        		rsl += "${GLOBUS_USER_HOME}/";
        }
        rsl += "</directory>";

        File stdout = sd.getStdout();
        if (stdout != null) {
            rsl += "<stdout>";
            rsl += sandbox.getRelativeStdout().getPath();
            rsl += "</stdout>";
        }

        File stderr = sd.getStderr();
        if (stderr != null) {
            rsl += "<stderr>";
            rsl += sandbox.getRelativeStderr().getPath();
            rsl += "</stderr>";
        }

        File stdin = sd.getStdin();
        if (stdin != null) {
            rsl += "<stdin>";
            rsl += sandbox.getRelativeStdin().getPath();
            rsl += "</stdin>";
        }
        
        if (useGramSandbox) {
	        Map<File, File> preStaged = sd.getPreStaged();
	        if (preStaged != null) {
	            Set<File> keys = preStaged.keySet();
	            Iterator<File> i = keys.iterator();
	            rsl += "<fileStageIn>";
	            while (i.hasNext()) {
	                File srcFile = (File) i.next();
	                File destFile = (File) preStaged.get(srcFile);
	                if (destFile == null) {
	                		logger.debug("ignoring prestaged file, no destination set!");
	                		continue;
	                }
	                rsl += "<transfer>";
	                try {
						rsl += "<sourceUrl>" + srcFile.toURL() + "</sourceUrl>";
						String destUrlString = null;
						if (destFile.isAbsolute()) {
							destUrlString = destFile.toURL().toString();
						} else {
							destUrlString = destFile.toURL().toString().replace(destFile.getPath(), "${GLOBUS_USER_HOME}/" + destFile.getPath());
						}
						rsl += "<destinationUrl>" + destUrlString + "</destinationUrl>";
					} catch (MalformedURLException e) {
						throw new GATInvocationException("WSGT4ResourceBrokerAdaptor", e);
					}
					rsl += "</transfer>";
	            }
	            rsl += "</fileStageIn>";
	        }
	
	        Map<File, File> postStaged = sd.getPostStaged();
	        if (preStaged != null) {
	            Set<File> keys = postStaged.keySet();
	            Iterator<File> i = keys.iterator();
	            rsl += "<fileStageOut>";
	            while (i.hasNext()) {
	                File srcFile = (File) i.next();
	                File destFile = (File) postStaged.get(srcFile);
	                if (destFile == null) {
	                		logger.debug("ignoring poststaged file, no destination set!");
	                		continue;
	                }
	                rsl += "<transfer>";
	                try {
						rsl += "<sourceUrl>" + srcFile.toURL() + "</sourceUrl>";
						rsl += "<destinationUrl>" + destFile.toURL() + "</destinationUrl>"; //TODO: Add ${GLOBUS_USER_HOME}
					} catch (MalformedURLException e) {
						throw new GATInvocationException("WSGT4ResourceBrokerAdaptor", e);
					}
					rsl += "</transfer>";
	            }
	            rsl += "</fileStageOut>";
	        }
        }

        rsl += "</job>";

        if (logger.isInfoEnabled()) {
            logger.info("RSL: " + rsl);
        }

        
        return rsl;
    }

    public void beginMultiJob() {
        submitter = new WrapperSubmitter(gatContext, preferences, brokerURI,
                true);
    }

    public Job endMultiJob() throws GATInvocationException {
        Job job = submitter.flushJobSubmission();
        submitter = null;
        return job;
    }

    public Job submitJob(JobDescription description, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {
        if (getBooleanAttribute(description, "wrapper.enable", false)) {
            if (logger.isDebugEnabled()) {
                logger.debug("useWrapper, using wrapper application");
            }
            if (submitter == null) {
                submitter = new WrapperSubmitter(gatContext, preferences,
                        brokerURI, false);
            }
            return submitter.submitJob(description);
        }
        String host = getHostname();
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                    "WSGT4ResourceBroker: the job description does not contain a software description");
        }
        String s = (String) preferences.get("wsgt4.sandbox.gram");
        boolean useGramSandbox = (s != null && s.equalsIgnoreCase("true"));
        Sandbox sandbox = null;
        if (!useGramSandbox) {
	        sandbox = new Sandbox(gatContext, preferences, description,
	                host, null, true, true, true, true);
        } 
        WSGT4Job job = new WSGT4Job(gatContext, preferences, description,
                sandbox);
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }
        if (!useGramSandbox) {
	        job.setState(Job.PRE_STAGING);
	        sandbox.prestage();
        }
        job.setContactString(getHostname());

        String rsl = createRSL(description, sandbox, useGramSandbox);
        JobDescriptionType gjobDescription = null;
        try {
            gjobDescription = RSLHelper.readRSL(rsl);
            job.setJobDescriptionType(gjobDescription);
        } catch (RSLParseException e) {
            throw new GATInvocationException("WSGT4ResourceBroker: " + e);
        }
        job.setCredential(getCred(description));
        return job;
    }
}
