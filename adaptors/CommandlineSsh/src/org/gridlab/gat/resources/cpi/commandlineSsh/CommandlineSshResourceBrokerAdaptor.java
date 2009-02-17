package org.gridlab.gat.resources.cpi.commandlineSsh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.CommandNotFoundException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.MethodNotApplicableException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.StreamForwarder;
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
import org.gridlab.gat.security.commandlinessh.CommandlineSshSecurityUtils;

public class CommandlineSshResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("submitJob", true);

        return capabilities;
    }

    protected static Logger logger = LoggerFactory
            .getLogger(CommandlineSshResourceBrokerAdaptor.class);

    public static final int SSH_PORT = 22;

    private boolean windows = false;

    /**
     * This method constructs a CommandlineSshResourceBrokerAdaptor instance
     * corresponding to the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which will be used to broker resources
     */
    public CommandlineSshResourceBrokerAdaptor(GATContext gatContext,
            URI brokerURI) throws GATObjectCreationException {
        super(gatContext, brokerURI);

        // if wrong scheme, throw exception!
        if (brokerURI.getScheme() != null) {
            if (!brokerURI.isCompatible("ssh")) {
                throw new GATObjectCreationException(
                        "Unable to handle incompatible scheme '"
                                + brokerURI.getScheme() + "' in broker uri '"
                                + brokerURI.toString() + "'");
            }
        }

        // if (!brokerURI.isCompatible("ssh") && brokerURI.getScheme() != null
        // || (brokerURI.refersToLocalHost() && (brokerURI == null))) {
        // throw new AdaptorNotApplicableException(
        // "cannot handle the scheme, scheme is: "
        // + brokerURI.getScheme());
        // }

        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
            windows = true;
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

        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }
        if (description.getProcessCount() != 1) {
            throw new GATInvocationException(
                    "Adaptor cannot handle: process count > 1: "
                            + description.getProcessCount());
        }

        if (description.getResourceCount() != 1) {
            throw new GATInvocationException(
                    "Adaptor cannot handle: resource count > 1: "
                            + description.getResourceCount());
        }

        // we do not support environment yet
        Map<String, Object> env = sd.getEnvironment();
        if (env != null && !env.isEmpty()) {
            throw new MethodNotApplicableException("cannot handle environment");
        }

        String path = getExecutable(description);
        String authority = getAuthority();
        if (authority == null) {
            authority = "localhost";
        }

        String host = getHostname();
        if (host == null) {
            host = "localhost";
        }

        /* allow port override */
        int port = brokerURI.getPort();
        /* it will always return -1 for user@host:path */
        if (port == -1) {
            port = SSH_PORT;
        }

        // create the sandbox
        Sandbox sandbox = new Sandbox(gatContext, description, authority, null,
                true, false, false, false);
        // create the job
        CommandlineSshJob commandlineSshJob = new CommandlineSshJob(gatContext,
                description, sandbox);
        Job job = null;
        if (description instanceof WrapperJobDescription) {
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, commandlineSshJob);
            listener = tmp;
            job = tmp;
        } else {
            job = commandlineSshJob;
        }
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }
        // set the state to prestaging
        commandlineSshJob.setState(Job.JobState.PRE_STAGING);
        // and let the sandbox prestage the files!
        sandbox.prestage();

        Map<String, String> securityInfo = CommandlineSshSecurityUtils
                .getSshCredential(gatContext, "commandlinessh", brokerURI,
                        SSH_PORT);
        String username = securityInfo.get("username");
        String password = securityInfo.get("password");
        int privateKeySlot = -1;
        try {
            privateKeySlot = Integer.parseInt(securityInfo
                    .get("privatekeyslot"));
        } catch (NumberFormatException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("unable to parse private key slot: " + e);
            }
        }
        
        ArrayList<String> command = new ArrayList<String>();

        if (windows) {
            command.add("sexec");
            command.add(username + "@" + authority);
            command.add("-unat=yes");
            if (password == null) { // public/private key
                int slot = privateKeySlot;
                if (slot == -1) { // not set by the user, assume he only has
                    // one key
                    slot = 0;
                }
                command.add(" -pk=" + slot);
            } else { // password
                command.add(" -pw=" + password);
            } 
            command.add("-cmd=" + path);
            String[] args = getArgumentsArray(description);
            if (args != null) {
                for (String arg : args) {
                    command.add(arg);
                }
            }
        } else {
            // we must use the -t option to ssh (allocates pseudo TTY).
            // If we don't, there is no way to kill the remote process.
            command.add("/usr/bin/ssh");
            command.add("-p");
            command.add("" + port);
            command.add("-o");
            command.add("BatchMode=yes");
            command.add("-o");
            command.add("StrictHostKeyChecking=yes");
            command.add("-t");
            command.add(username + "@" + host);
            if (sandbox.getSandboxPath() != null) {
                command.add("cd");
                command.add(sandbox.getSandboxPath());
                command.add("&&");
            }
            command.add(protectAgainstShellMetas(path));
            String[] args = getArgumentsArray(description);
            if (args != null) {
                for (String arg : args) {
                    command.add(protectAgainstShellMetas(arg));
                }
            }
        }
        
        ProcessBuilder builder = new ProcessBuilder(command);

        if (logger.isInfoEnabled()) {
            logger.info("running command: " + command);
        }

        Process p = null;
        try {
            p = builder.start();
        } catch (IOException e) {
            throw new CommandNotFoundException(
                    "CommandlineSshResourceBrokerAdaptor", e);
        }
        commandlineSshJob.setState(Job.JobState.RUNNING);
        commandlineSshJob.setSubmissionTime();
        commandlineSshJob.setStartTime();
        commandlineSshJob.setProcess(p);

        if (!sd.streamingStderrEnabled()) {
            // read away the stderr

            try {
                if (sd.getStderr() != null) {
                    // to file
                    new StreamForwarder(p.getErrorStream(), GAT
                            .createFileOutputStream(sd.getStderr()));
                } else {
                    // or throw it away
                    new StreamForwarder(p.getErrorStream(), null);
                }
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Unable to create file output stream for stderr!", e);
            }
        }

        if (!sd.streamingStdoutEnabled()) {
            // read away the stdout
            try {
                if (sd.getStdout() != null) {
                    // to file
                    new StreamForwarder(p.getInputStream(), GAT
                            .createFileOutputStream(sd.getStdout()));
                } else {
                    // or throw it away
                    new StreamForwarder(p.getInputStream(), null);
                }
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Unable to create file output stream for stdout!", e);
            }
        }

        if (!sd.streamingStdinEnabled() && sd.getStdin() != null) {
            // forward the stdin from file
            try {
                new StreamForwarder(GAT.createFileInputStream(sd.getStdin()), p
                        .getOutputStream());
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Unable to create file input stream for stdin!", e);
            }
        }

        commandlineSshJob.monitorState();

        return job;
    }
    
    
    private static String protectAgainstShellMetas(String s) {
        char[] chars = s.toCharArray();
        StringBuffer b = new StringBuffer();
        b.append('\'');
        for (char c : chars) {
            if (c == '\'') {
                b.append('\'');
                b.append('\\');
                b.append('\'');
            }
            b.append(c);
        }
        b.append('\'');
        return b.toString();
    }
}
