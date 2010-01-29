/*
 * Created on Oct 14, 2004
 */
package org.gridlab.gat.resources.cpi.globus;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.globus.common.ResourceManagerContact;
import org.globus.gram.Gram;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.internal.GRAMConstants;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.cpi.PostStagedFile;
import org.gridlab.gat.resources.cpi.PostStagedFileSet;
import org.gridlab.gat.resources.cpi.PreStagedFile;
import org.gridlab.gat.resources.cpi.PreStagedFileSet;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperJobCpi;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * @author rob
 */
public class GlobusResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("beginMultiJob", true);
        capabilities.put("endMultiJob", true);
        capabilities.put("submitJob", true);

        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();
        preferences.put("globus.sandbox.gram", "false");
        preferences.put("globus.exitvalue.enable", "false");
        return preferences;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "globus", "http", "https", "gram"};
    }

    protected static Logger logger = LoggerFactory
            .getLogger(GlobusResourceBrokerAdaptor.class);

    static boolean shutdownInProgress = false;

    public static void init() {
        GATEngine.registerUnmarshaller(GlobusJob.class);
    }

    public GlobusResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws GATObjectCreationException {
        super(gatContext, brokerURI);
    }

    protected String createRSL(JobDescription description, String host,
            Sandbox sandbox, PreStagedFileSet pre, PostStagedFileSet post)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        String rsl = "";
        String args = "";

        rsl += "& (executable = " + getExecutable(description) + ")";

        // parse the arguments
        String[] argsA = getArgumentsArray(description);

        if (argsA != null) {
            for (int i = 0; i < argsA.length; i++) {
                args += (" \"" + argsA[i] + "\" ");
            }
        }
        if (args.length() != 0) {
            rsl += (" (arguments = " + args + ")");
        }

        rsl += " (count = " + description.getProcessCount() + ")";

        rsl += " (hostCount = " + description.getResourceCount() + ")";

        String jobType = getStringAttribute(description, SoftwareDescription.JOB_TYPE, null);
        if (jobType != null) {
            rsl += " (jobType = " + jobType + ")";
        }

        if (sandbox != null) {
            rsl += " (directory = " + sandbox.getSandbox() + ")";
        }

        long maxTime = getLongAttribute(description, SoftwareDescription.TIME_MAX, -1);
        if (maxTime > 0) {
            rsl += " (maxTime = " + maxTime + ")";
        }

        long maxWallTime = getLongAttribute(description, SoftwareDescription.WALLTIME_MAX, -1);
        if (maxWallTime > 0) {
            rsl += " (maxWallTime = " + maxWallTime + ")";
        }

        long maxCPUTime = getLongAttribute(description, SoftwareDescription.CPUTIME_MAX, -1);
        if (maxCPUTime > 0) {
            rsl += " (maxCPUTime = " + maxCPUTime + ")";
        }

        // stage in files with gram
        // if the files are staged using gat, pre and post in this method are
        // null
        if (pre != null) {
            for (int i = 0; i < pre.size(); i++) {
                PreStagedFile f = pre.getFile(i);

                if (!f.getResolvedSrc().toGATURI().refersToLocalHost()) {
                    rsl += " (file_stage_in = (gsiftp://"
                            + f.getResolvedSrc().toGATURI().getHost() + "/"
                            + f.getResolvedSrc().getPath() + " "
                            + f.getResolvedDest().getPath() + "))";
                } else {

                    rsl += " (file_stage_in = (file:///"
                            + f.getResolvedSrc().getPath() + " "
                            + f.getResolvedDest().getPath() + "))";
                }
            }
        }

        if (post != null) {
            for (int i = 0; i < post.size(); i++) {
                PostStagedFile f = post.getFile(i);

                if (!f.getResolvedSrc().toGATURI().refersToLocalHost()) {
                    rsl += " (file_stage_out = ("
                            + f.getResolvedDest().getPath() + " " + "gsiftp://"
                            + f.getResolvedSrc().toGATURI().getHost() + "/"
                            + f.getResolvedSrc().getPath() + "))";
                } else {
                    rsl += " (file_stage_out = ("
                            + f.getResolvedDest().getPath() + " gsiftp://"
                            + GATEngine.getLocalHostName() + "/"
                            + f.getResolvedSrc().getPath() + "))";
                }
            }
        }

        if (sd.streamingStdoutEnabled()) {
            rsl += (" (stdout = stdout)");
        } else if (sd.getStdout() != null) {
            rsl += (" (stdout = " + sd.getStdout().getName() + ")");
        }

        if (sd.streamingStderrEnabled()) {
            rsl += (" (stderr = stderr)");
        } else if (sd.getStderr() != null) {
            rsl += (" (stderr = " + sd.getStderr().getName() + ")");
        }

        if (sd.getStdin() != null) {
            rsl += (" (stdin = " + sd.getStdin().getName() + ")");
        }

        // set the environment
        Map<String, Object> env = sd.getEnvironment();
        if (env != null && !env.isEmpty()) {
            Set<String> s = env.keySet();
            Object[] keys = s.toArray();
            rsl += "(environment = ";

            for (int i = 0; i < keys.length; i++) {
                String val = (String) env.get(keys[i]);
                rsl += "(" + keys[i] + " \"" + val + "\")";
            }
            rsl += ")";
        }

        String queue = getStringAttribute(description, "globus.queue", null);
        if (queue != null) {
            rsl += " (queue = " + queue + ")";
        }

        String timeout = getStringAttribute(description,
                "globus.proxy.timeout", null);
        if (timeout != null) {
            rsl += " (proxy_timeout = " + timeout + ")";
        }

        if (logger.isDebugEnabled()) {
            logger.debug("RSL: " + rsl);
        }

        return rsl;
    }

    protected String createChmodRSL(JobDescription description, String host,
            String chmodLocation, Sandbox sandbox, String executable) {
        String rsl = "& (executable = " + chmodLocation + ")";

        if (sandbox != null) {
            rsl += " (directory = " + sandbox.getSandbox() + ")";
        }

        rsl += " (arguments = \"+x\" \"" + executable + "\")";

        if (logger.isDebugEnabled()) {
            logger.debug("CHMOD RSL: " + rsl);
        }

        return rsl;
    }

    private void runGramJobPolling(GSSCredential credential, String rsl,
            String contact) throws GATInvocationException {
        GramJob j = new GramJob(credential, rsl);
        try {
            Gram.request(contact, j);
        } catch (GramException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("could not run job: "
                        + GramError.getGramErrorString(e.getErrorCode()));
            }

            return;
        } catch (GSSException e2) {
            throw new CouldNotInitializeCredentialException("globus", e2);
        }

        int errorCount = 0;
        while (errorCount <= 3) {
            try {
                Gram.jobStatus(j);
                int status = j.getStatus();
                if (logger.isDebugEnabled()) {
                    logger.debug("job status = " + status);
                }
                if (status == GRAMConstants.STATUS_DONE
                        || status == GRAMConstants.STATUS_FAILED) {
                    return;
                }
            } catch (Exception e) {
                if (j.getError() == GramError.GRAM_JOBMANAGER_CONNECTION_FAILURE) {
                    return;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("got exception: " + e);
                }

                // ignore other errors
                errorCount++;
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private void submitChmodJob(GSSCredential credential,
            JobDescription description, String host, String chmodLocation,
            Sandbox sandbox, String path) throws GATInvocationException {
        if (logger.isDebugEnabled()) {
            logger.debug("running " + chmodLocation + " on " + host
                    + "/jobmanager-fork to set executable bit on ");
        }
        String chmodRsl = createChmodRSL(description, host, chmodLocation,
                sandbox, path);

        runGramJobPolling(credential, chmodRsl, host + "/jobmanager-fork");
        if (logger.isDebugEnabled()) {
            logger.debug("done");
        }
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

        boolean useGramSandbox = false;
        String s = (String) gatContext.getPreferences().get(
                "globus.sandbox.gram");
        if (s != null && s.equalsIgnoreCase("true")) {
            useGramSandbox = true;
        }
        if (useGramSandbox) {
            return submitJobGramSandbox(description, listener,
                    metricDefinitionName);
        } else {
            return submitJobGatSandbox(description, listener,
                    metricDefinitionName);
        }
    }

    private boolean isExitValueEnabled(JobDescription description) {
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            return false;
        }
        if (sd.getAttributes().containsKey("globus.exitvalue.enable")) {
            if (((String) sd.getAttributes().get("globus.exitvalue.enable"))
                    .equalsIgnoreCase("true")) {
                return true;
            }
        }
        return false;
    }

    private GSSCredential getCredential(String host)
            throws GATInvocationException {
        URI hostUri;
        try {
            hostUri = new URI(host);
        } catch (Exception e) {
            throw new GATInvocationException("globus broker", e);
        }

        GSSCredential credential = null;
        try {
            credential = GlobusSecurityUtils.getGlobusCredential(gatContext,
                    "gram", hostUri, ResourceManagerContact.DEFAULT_PORT);
        } catch (CouldNotInitializeCredentialException e) {
            throw new GATInvocationException("globus", e);
        } catch (CredentialExpiredException e) {
            throw new GATInvocationException("globus", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("credential = " + (credential != null ? credential : "null"));
        }
        return credential;
    }

    private void runChmod(GSSCredential credential, JobDescription description,
            String host, Sandbox sandbox) {

        try {
            submitChmodJob(credential, description, host, "/bin/chmod",
                    sandbox, getExecutable(description));
        } catch (Exception e) {
            // ignore
        }
        try {
            submitChmodJob(credential, description, host, "/usr/bin/chmod",
                    sandbox, getExecutable(description));
        } catch (Exception e) {
            // ignore
        }
    }

    public Job submitJobGramSandbox(JobDescription description,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {
        // long start = System.currentTimeMillis();
        String host = getHostname();
        String contact = brokerURI.toString();
        if (brokerURI.getScheme() != null) {
            contact = contact.replace(brokerURI.getScheme() + "://", "");
        }

        URI hostUri;
        try {
            hostUri = new URI(host);
        } catch (Exception e) {
            throw new GATInvocationException("globus broker", e);
        }
        GlobusJob job = new GlobusJob(gatContext, description, null);

        // if special preference "globus.exitvalue.enable" is set to true,
        // modify the softwaredescription
        String random = "" + Math.random();
        if (isExitValueEnabled(description)) {
            description.getSoftwareDescription().toWrapper(gatContext,
                    ".JavaGAT-wrapper-script-" + random,
                    ".JavaGAT-exit-value-" + random);
            job.setExitValueEnabled(isExitValueEnabled(description),
                    ".JavaGAT-exit-value-" + random);
        }

        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }
        job.setState(Job.JobState.PRE_STAGING);
        PreStagedFileSet pre = new PreStagedFileSet(gatContext, description,
                host, null, false);

        PostStagedFileSet post = new PostStagedFileSet(gatContext, description,
                host, null, false, false);

        String rsl = createRSL(description, host, null, pre, post);
        if (logger.isInfoEnabled()) {
            logger.info("RSL: " + rsl);
        }

        GSSCredential credential = null;
        try {
            credential = GlobusSecurityUtils.getGlobusCredential(gatContext,
                    "gram", hostUri, ResourceManagerContact.DEFAULT_PORT);
        } catch (CouldNotInitializeCredentialException e) {
            throw new GATInvocationException("globus", e);
        } catch (CredentialExpiredException e) {
            throw new GATInvocationException("globus", e);
        }

        GramJob j = new GramJob(credential, rsl);
        job.setGramJob(j);
        try {
            Gram.request(contact, j);
            if (!new java.io.File(".JavaGAT-wrapper-script-" + random).delete()) {
                logger.info("failed to delete the wrapper script: '"
                        + ".JavaGAT-wrapper-script-" + random + "'.");
            }
        } catch (GramException e) {
            throw new GATInvocationException("globus", e); // no idea what went
            // wrong
        } catch (GSSException e2) {
            throw new GATInvocationException("globus",
                    new CouldNotInitializeCredentialException("globus", e2));
        }
        // Try if we can contact the job manager. If not, this is a problem, because
        // we cannot monitor the job. In this case, the job submission fails, but the
        // job may be running ... we try to kill it ...
        try {
            Gram.jobStatus(j);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("WARNING, could not get state of globus job: "
                                + e);
            }
            if (j.getError() == GramError.GRAM_JOBMANAGER_CONNECTION_FAILURE) {
                // This means we could not contact the job manager.
                try {
                    j.cancel();
                } catch (Throwable e1) {
                    // ignored, we tried ...
                }
                try {
                    Gram.cancel(j);
                } catch (Throwable e1) {
                    // ignored, we tried ...
                }
                throw new GATInvocationException("globus: cannot contact job manager.");
            }
        }
        job.startPoller();
        return job;
    }

    public Job submitJobGatSandbox(JobDescription description,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {
        // long start = System.currentTimeMillis();
        // choose the first of the set descriptions to retrieve the hostname
        // etc.
        String host = getHostname();

        String contact = brokerURI.toString();
        if (brokerURI.getScheme() != null) {
            contact = contact.replace(brokerURI.getScheme() + "://", "");
        }

        GSSCredential credential = getCredential(host);

        String random = "" + Math.random();
        // if special preference "globus.exitvalue.enable" is set to true,
        // modify the softwaredescription
        if (isExitValueEnabled(description)) {
            description.getSoftwareDescription().toWrapper(gatContext,
                    ".JavaGAT-wrapper-script-" + random,
                    ".JavaGAT-exit-value-" + random);
        }

        // code to handle streaming err and out
        SoftwareDescription sd = description.getSoftwareDescription();

        // if output should be streamed, don't poststage it!
        Sandbox sandbox = new Sandbox(gatContext, description, host, null,
                true, true, sd.getStdout() != null, sd.getStderr() != null);
        GlobusJob globusJob = new GlobusJob(gatContext, description, sandbox);
        Job job = null;
        if (description instanceof WrapperJobDescription) {
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, globusJob,
                    listener, metricDefinitionName);
            job = tmp;
            listener = tmp;
        } else {
            job = globusJob;
        }
        if (listener != null && metricDefinitionName != null) {
            Metric metric = globusJob.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            globusJob.addMetricListener(listener, metric);
        }

        if (isExitValueEnabled(description)) {
            globusJob.setExitValueEnabled(isExitValueEnabled(description),
                    ".JavaGAT-exit-value-" + random);
        }

        globusJob.setState(Job.JobState.PRE_STAGING);
        try {
            sandbox.prestage();
        } catch (GATInvocationException e) {
            // prestaging fails cleanup before throwing the exception.
            globusJob.setState(Job.JobState.POST_STAGING);
            sandbox.retrieveAndCleanup(globusJob);
            globusJob.setState(Job.JobState.SUBMISSION_ERROR);
            throw e;
        }
        // after the prestaging we can safely delete the wrapper script if we
        // did create it
        if (isExitValueEnabled(description)) {
            logger.info("deleting '" + ".JavaGAT-wrapper-script-" + random
                    + "'");
            logger.info("file exists: "
                    + new java.io.File(".JavaGAT-wrapper-script-" + random)
                            .exists());
            if (!(new java.io.File(".JavaGAT-wrapper-script-" + random)
                    .delete())) {
                logger.info("failed to delete the wrapper script: '"
                        + ".JavaGAT-wrapper-script-" + random + "'.");
            }
        }

        // If we staged in the executable, we have to do a chmod.
        // Globus loses the executable bit :-(

        // better use the preference "gridftp.chmod", "..." for creating the
        // file object of the executable

        if (sandbox.getResolvedExecutable() != null) {
            runChmod(credential, description, host, sandbox);
        }

        String rsl = createRSL(description, host, sandbox, null, null);
        if (logger.isDebugEnabled()) {
            logger.debug("submitJob: credential = " + (credential != null ? credential : "null"));
        }
        GramJob j = new GramJob(credential, rsl);
        globusJob.setGramJob(j);
        globusJob.setState(Job.JobState.SCHEDULED);
        try {
            j.request(contact);
            // Gram.request(contact, j);
        } catch (GramException e) {
            throw new GATInvocationException("globus", e); // no idea what went
            // wrong
        } catch (GSSException e2) {
            throw new GATInvocationException("globus",
                    new CouldNotInitializeCredentialException("globus", e2));
        }

        // Try if we can contact the job manager. If not, this is a problem, because
        // we cannot monitor the job. In this case, the job submission fails, but the
        // job may be running ... we try to kill it ...
        try {
            Gram.jobStatus(j);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("WARNING, could not get state of globus job: "
                                + e);
            }
            if (j.getError() == GramError.GRAM_JOBMANAGER_CONNECTION_FAILURE) {
                // This means we could not contact the job manager.
                try {
                    j.cancel();
                } catch (Throwable e1) {
                    // ignored, we tried ...
                }
                try {
                    Gram.cancel(j);
                } catch (Throwable e1) {
                    // ignored, we tried ...
                }
                throw new GATInvocationException("globus: cannot contact job manager.");
            }
        }
        globusJob.startPoller();
        if (sd.streamingStderrEnabled()) {
            try {
                URI err = brokerURI.setScheme("gsiftp");
                err = err.setPath(sandbox.getSandbox() + "/stderr");
                globusJob.startStderrForwarder(GAT.createFile(gatContext, err));
            } catch (GATObjectCreationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (sd.streamingStdoutEnabled()) {
            try {
                URI out = brokerURI.setScheme("gsiftp");
                out = out.setPath(sandbox.getSandbox() + "/stdout");
                globusJob.startStdoutForwarder(GAT.createFile(gatContext, out));
            } catch (GATObjectCreationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return job;
    }

    public static void end() {
        if (logger.isDebugEnabled()) {
            logger.debug("globus broker adaptor end");
        }

        shutdownInProgress = true;

        try {
            Gram.deactivateAllCallbackHandlers();
        } catch (Throwable t) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("WARNING, globus job could not deactivate callback: "
                                + t);
            }
        }
    }

}
