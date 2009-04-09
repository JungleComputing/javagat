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
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
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
    
    private static final String SSH_STRICT_HOST_KEY_CHECKING = "commandlinessh.StrictHostKeyChecking";
    
    private static final String SSH_PORT_STRING = "commandlinessh.ssh.port";    

    public static final int SSH_PORT = 22;
    
    public static Preferences getSupportedPreferences() {
        Preferences p = ResourceBrokerCpi.getSupportedPreferences();
        p.put(SSH_STRICT_HOST_KEY_CHECKING, "yes");
        p.put(SSH_PORT_STRING, "" + SSH_PORT);
        return p;
    }

    protected static Logger logger = LoggerFactory
            .getLogger(CommandlineSshResourceBrokerAdaptor.class);
    
    private final int ssh_port;

    private boolean windows = false;
    
    private final boolean strictHostKeyChecking;
    
    private Map<String, String> securityInfo;

    public static void init() {
        GATEngine.registerUnmarshaller(CommandlineSshJob.class);
    }

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
        
        /* allow port override */
        if (brokerURI.getPort() != -1) {
            ssh_port = brokerURI.getPort();
        } else {
            String port = (String) gatContext.getPreferences().get(SSH_PORT_STRING);
            if (port != null) {
                ssh_port = Integer.parseInt(port);
            } else {
                ssh_port = SSH_PORT;
            }
        }
        
        strictHostKeyChecking = ((String) gatContext.getPreferences().get(SSH_STRICT_HOST_KEY_CHECKING, "true"))
                .equalsIgnoreCase("true");
               
        try {
            securityInfo = CommandlineSshSecurityUtils.getSshCredential(
                    gatContext, "commandlinessh", brokerURI, ssh_port);
        } catch (Throwable e) {
            logger
                    .info("CommandlineSshFileAdaptor: failed to retrieve credentials"
                            + e);
            securityInfo = null;
        }

        if (securityInfo == null) {
            throw new GATObjectCreationException(
                    "Unable to retrieve user info for authentication");
        }

        if (securityInfo.containsKey("privatekeyfile")) {
            if (logger.isDebugEnabled()) {
                logger.debug("key file argument not supported yet");
            }
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

        String username = securityInfo.get("username");
        String password = securityInfo.get("password");
        int privateKeySlot = -1;
        try {
            String v = securityInfo.get("privatekeyslot");
            if (v != null) {
                privateKeySlot = Integer.parseInt(v);
            }
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
            command.add("-P");
            command.add("" + ssh_port);
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
            // Protection is against expansion on the remote host,
            // which (for now) is assumed to be a unix machine.
            // TODO: add detection and support for windows ssh servers.
            command.add("-cmd=" + protectAgainstShellMetas(path));
            String[] args = getArgumentsArray(description);
            if (args != null) {
                for (String arg : args) {
                    command.add(protectAgainstShellMetas(arg));
                }
            }
        } else {
            // we must use the -t option to ssh (allocates pseudo TTY).
            // If we don't, there is no way to kill the remote process.
            command.add("/usr/bin/ssh");
            command.add("-p");
            command.add("" + ssh_port);
            command.add("-o");
            command.add("BatchMode=yes");
            command.add("-o");
            command.add("StrictHostKeyChecking=" + (strictHostKeyChecking ? "yes" : "no"));
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
    
    // Protect against special characters for (most) unix shells.
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
    
    /* Not now. We'll need this when we start supporting windows ssh servers.
    // Protect against special characters for the windows command line interpreter.
    private static String protectAgainstWindowsMetas(String s) {
        char[] chars = s.toCharArray();
        StringBuffer b = new StringBuffer();
        for (char c : chars) {
            if ("\"&()^;| ".indexOf(c) >= 0) {
                b.append('^');
            }
            b.append(c);
        }
        return b.toString();
    }
    */
}
