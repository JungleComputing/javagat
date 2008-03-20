package org.gridlab.gat.resources.cpi.commandlineSsh;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.CommandNotFoundException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.MethodNotApplicableException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.InputForwarder;
import org.gridlab.gat.engine.util.OutputForwarder;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.security.commandlinessh.CommandlineSshSecurityUtils;

public class CommandlineSshResourceBrokerAdaptor extends ResourceBrokerCpi {

    protected static Logger logger = Logger
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
            Preferences preferences, URI brokerURI)
            throws GATObjectCreationException {
        super(gatContext, preferences, brokerURI);

        if (!brokerURI.isCompatible("ssh") && brokerURI.getScheme() != null
                || (brokerURI.refersToLocalHost() && (brokerURI == null))) {
            throw new GATObjectCreationException(
                    "cannot handle the scheme, scheme is: "
                            + brokerURI.getScheme());
        }

        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
            windows = true;
        }
    }

    public Job submitJob(JobDescription description, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
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
        Sandbox sandbox = new Sandbox(gatContext, preferences, description,
                authority, null, true, false, false, false);
        // create the job
        CommandlineSshJob job = new CommandlineSshJob(gatContext, preferences,
                description, sandbox);
        // now the job is created, immediately add the listener to it, so that
        // it will receive each state
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }
        // set the state to prestaging
        job.setState(Job.PRE_STAGING);
        // and let the sandbox prestage the files!
        sandbox.prestage();

        Map<String, String> securityInfo = CommandlineSshSecurityUtils.getSshCredential(gatContext, preferences, "commandlinessh", brokerURI, SSH_PORT);
        String username = securityInfo.get("username");
        String password = securityInfo.get("password");
        int privateKeySlot = -1;
        try {
            privateKeySlot = Integer.parseInt(securityInfo.get("privatekeyslot"));
        } catch (NumberFormatException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("unable to parse private key slot: " + e);
            }
        }
        
        String command = null;
        if (windows) {
            command = "sexec " + username + "@" + authority + " -unat=yes -cmd="
                    + path + " " + getArguments(description);
            if (password == null) { // public/private key
                int slot = privateKeySlot;
                if (slot == -1) { // not set by the user, assume he only has
                    // one key
                    slot = 0;
                }
                command += " -pk=" + slot;
            } else { // password
                command += " -pw=" + password;
            }
        } else {
            // we must use the -t option to ssh (allocates pseudo TTY).
            // If we don't, there is no way to kill the remote process.
            command = "ssh -p " + port + " "
                    + "-o BatchMode=yes -o StrictHostKeyChecking=yes -t -t "
                    + username + "@" + host + " " + "cd " + sandbox.getSandbox() + " && " + path
                    + " " + getArguments(description);
        }

        if (logger.isInfoEnabled()) {
            logger.info("running command: " + command);
        }

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command.toString());
        } catch (IOException e) {
            throw new CommandNotFoundException(
                    "CommandlineSshResourceBrokerAdaptor", e);
        }
        job.setState(Job.RUNNING);
        job.setProcess(p);

        org.gridlab.gat.io.File stdin = sd.getStdin();
 
        if (stdin == null) {
            // close stdin.
            try {
                p.getOutputStream().close();
            } catch (Throwable e) {
                // ignore
            }
        } else {
            try {
                FileInputStream fin = GAT.createFileInputStream(gatContext,
                        preferences, stdin.toGATURI());
                OutputStream out = p.getOutputStream();
                new InputForwarder(out, fin);
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException("commandlineSsh broker", e);
            }
        }

        OutputStream userOut;
        OutputStream userErr;
        if (sd.stdoutIsStreaming()) {
            userOut = sd.getStdoutStream();
        } else {
            try {
                userOut = GAT.createFileOutputStream(sd.getStdout());
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException("failed to create outputstream to write in output file: '" + sd.getStdout() + "'", e);
            }
        }
        if (sd.stderrIsStreaming()) {
            userErr = sd.getStderrStream();
        } else {
            try {
                userErr = GAT.createFileOutputStream(sd.getStderr());
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException("failed to create outputstream to write in error file: '" + sd.getStderr() + "'", e);
            }
        }
        OutputForwarder outForwarder = new OutputForwarder(p.getInputStream(), userOut);
        OutputForwarder errForwarder = new OutputForwarder(p.getErrorStream(), userErr);
        job.startOutputWaiter(outForwarder, errForwarder);
        
        return job;
    }
}
