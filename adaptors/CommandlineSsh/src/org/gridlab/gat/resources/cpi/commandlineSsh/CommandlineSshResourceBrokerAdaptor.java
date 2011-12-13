package org.gridlab.gat.resources.cpi.commandlineSsh;

import java.util.ArrayList;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.MethodNotApplicableException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.SshHelper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandlineSshResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static String getDescription() {
        return "The CommandlineSsh ResourceBroker Adaptor implements the ResourceBroker object using ssh commands.";
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("submitJob", true);

        return capabilities;
    }
    
    private static final String SSH_STRICT_HOST_KEY_CHECKING = "commandlinessh.StrictHostKeyChecking";
    private static final String SSH_PORT_STRING = "commandlinessh.ssh.port";   
    private static final String SSH_STOPPABLE = "commandlinessh.stoppable";

    public static final int SSH_PORT = 22;
    
    public static Preferences getSupportedPreferences() {
        Preferences p = ResourceBrokerCpi.getSupportedPreferences();
        p.put(SSH_STRICT_HOST_KEY_CHECKING, "false");
        p.put(SSH_STOPPABLE, "false");
        p.put(SSH_PORT_STRING, "" + SSH_PORT);
        return p;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "commandlinessh", "ssh"};
    }

    protected static Logger logger = LoggerFactory
            .getLogger(CommandlineSshResourceBrokerAdaptor.class);
    
    private final SshHelper brokerHelper;

    public static void init() {
        // GATEngine.registerUnmarshaller(CommandlineSshJob.class);
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
        
        brokerHelper = new SshHelper(gatContext, brokerURI, "commandlinessh", SSH_PORT_STRING, SSH_STRICT_HOST_KEY_CHECKING);
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

//        System.out.println("env:" + env.toString());
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
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, commandlineSshJob,
                    listener, metricDefinitionName);
            listener = tmp;
            job = tmp;
        } else {
            job = commandlineSshJob;
        }
        if (listener != null && metricDefinitionName != null) {
            Metric metric = commandlineSshJob.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            commandlineSshJob.addMetricListener(listener, metric);
        }

        // set the state to prestaging
        commandlineSshJob.setState(Job.JobState.PRE_STAGING);
        // and let the sandbox prestage the files!
        sandbox.prestage();
        
        boolean stoppable = "true".equalsIgnoreCase((String) gatContext
                .getPreferences().get(SSH_STOPPABLE));
        
        ArrayList<String> command;
        
        if (!sd.streamingStdinEnabled() && sd.getStdin() == null) {
            // Redirect stdin from the job to /dev/null.
            command = brokerHelper.getSshCommand(stoppable, "-n");
        } else {
            command = brokerHelper.getSshCommand(stoppable);
        }
        
        String[] args = getArgumentsArray(description);
        
        if (brokerHelper.onWindows()) {
            // TODO: add detection and support for windows ssh servers.
            command.add("-cmd=" + SshHelper.protectAgainstShellMetas(path));
        } else {
            if (sandbox.getSandboxPath() != null) {
                command.add("cd");
                command.add(SshHelper.protectAgainstShellMetas(sandbox.getSandboxPath()));
                command.add("&&");
            }
            command.add(SshHelper.protectAgainstShellMetas(path));
        }
        Process p = brokerHelper.startSshCommand(command, args);

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
}
