package org.gridlab.gat.resources.cpi.commandlineSsh;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
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
     *            A GATContext which will be used to broker resources
     */
    public CommandlineSshResourceBrokerAdaptor(GATContext gatContext,
            URI brokerURI) throws GATObjectCreationException {
        super(gatContext, brokerURI);

        if (!brokerURI.isCompatible("ssh") && brokerURI.getScheme() != null
                || (brokerURI.refersToLocalHost() && (brokerURI == null))) {
            throw new AdaptorNotApplicableException(
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
        Sandbox sandbox = new Sandbox(gatContext, description, authority, null,
                true, false, false, false);
        // create the job
        CommandlineSshJob job = new CommandlineSshJob(gatContext, description,
                sandbox);
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

        String command = null;
        if (windows) {
            command = "sexec " + username + "@" + authority
                    + " -unat=yes -cmd=" + path + " "
                    + getArguments(description);
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
                    + username + "@" + host + " " + "cd "
                    + sandbox.getSandbox() + " && " + path + " "
                    + getArguments(description);
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

        StreamForwarder outForwarder = null;
        StreamForwarder errForwarder = null;

        // handle the input
        if (sd.getStdinFile() != null) {
            try {
                new StreamForwarder(GAT.createFileInputStream(sandbox
                        .getResolvedStdin()), p.getOutputStream());
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Could not create a FileInputStream to read from the input '"
                                + sandbox.getResolvedStdin() + "'", e);
            }
        } else if (sd.getStdinStream() != null) {
            new StreamForwarder(sd.getStdinStream(), p.getOutputStream());
        } else {
            try {
                p.getOutputStream().close();
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("Failed to close the OutputStream of the process: "
                                    + e);
                }
            }
        }

        if (sd.getStdoutFile() != null) {
            try {
                outForwarder = new StreamForwarder(p.getInputStream(), GAT
                        .createFileOutputStream(sandbox.getResolvedStdout()));
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Could not creat a FileOutputStream to write the output to '"
                                + sandbox.getResolvedStdout() + "'", e);
            }
        } else if (sd.getStdoutStream() != null) {
            outForwarder = new StreamForwarder(p.getInputStream(), sd
                    .getStdoutStream());
        } else {
            try {
                p.getInputStream().close();
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("Failed to close the InputStream of the process: "
                                    + e);
                }
            }
        }

        if (sd.getStderrFile() != null) {
            try {
                errForwarder = new StreamForwarder(p.getErrorStream(), GAT
                        .createFileOutputStream(sandbox.getResolvedStderr()));
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Could not creat a FileOutputStream to write the error to '"
                                + sandbox.getResolvedStderr() + "'", e);
            }
        } else if (sd.getStderrStream() != null) {
            errForwarder = new StreamForwarder(p.getErrorStream(), sd
                    .getStderrStream());
        } else {
            try {
                p.getErrorStream().close();
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("Failed to close the ErrorStream of the process: "
                                    + e);
                }
            }
        }

        job.startOutputWaiter(outForwarder, errForwarder);

        return job;
    }
}
