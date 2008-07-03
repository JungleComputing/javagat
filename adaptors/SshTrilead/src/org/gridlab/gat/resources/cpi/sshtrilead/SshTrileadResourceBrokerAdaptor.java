package org.gridlab.gat.resources.cpi.sshtrilead;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.StreamForwarder;
import org.gridlab.gat.io.cpi.sshtrilead.SshTrileadFileAdaptor;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperSubmitter;

import com.trilead.ssh2.Session;

/**
 * An instance of this class is used to execute remote jobs.
 */

public class SshTrileadResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("beginMultiJob", true);
        capabilities.put("endMultiJob", true);
        capabilities.put("submitJob", true);

        return capabilities;
    }

    public static String getDescription() {
        return "The SshTrilead ResourceBroker Adaptor implements the ResourceBroker object using the trilead ssh library. Trilead ssh is an open source full java ssh library. The ssh trilead ResourceBroker adaptor can only submit to single machines, however if you invoke a command like 'qsub' on a headnode, it might result in an application running on multiple machines. Connections with a remote ssh server can be made by using the username + password, username + keyfile, or with only a username, depending on the client and server settings.";
    }

    protected static Logger logger = Logger
            .getLogger(SshTrileadResourceBrokerAdaptor.class);

    public static final int IN = 0, ERR = 1, OUT = 2;

    public static final int SSH_PORT = 22;

    private boolean connectionCacheEnable;

    private String[] client2serverCiphers;

    private String[] server2clientCiphers;

    private boolean tcpNoDelay;

    private WrapperSubmitter submitter;

    /**
     * This method constructs a SshResourceBrokerAdaptor instance corresponding
     * to the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which will be used to execute remote jobs
     */
    public SshTrileadResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws Exception {
        super(gatContext, brokerURI);

        // accept if broker URI is compatible with ssh or with file
        if (!(brokerURI.isCompatible("ssh") || brokerURI.isCompatible("file"))) {
            throw new AdaptorNotApplicableException("cannot handle this URI: "
                    + brokerURI);
        }
        // init from preferences
        Preferences p = gatContext.getPreferences();
        String client2serverCipherString = ((String) p
                .get(
                        "sshtrilead.cipher.client2server",
                        "aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc"));
        client2serverCiphers = client2serverCipherString.split(",");
        String server2clientCipherString = ((String) p
                .get(
                        "sshtrilead.cipher.server2client",
                        "aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc"));
        server2clientCiphers = server2clientCipherString.split(",");
        tcpNoDelay = ((String) p.get("sshtrilead.tcp.nodelay", "true"))
                .equalsIgnoreCase("true");
        connectionCacheEnable = ((String) p.get(
                "sshtrilead.use.cached.connections", "true"))
                .equalsIgnoreCase("true");
    }

    public void beginMultiJob() {
        submitter = new WrapperSubmitter(gatContext, brokerURI, true);
    }

    public Job endMultiJob() throws GATInvocationException {
        Job job = submitter.flushJobSubmission();
        submitter = null;
        return job;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(AbstractJobDescription abstractDescription,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {
        // TODO: this broker is not Windows compatible (&&, export)

        if (!(abstractDescription instanceof JobDescription)) {
            throw new GATInvocationException(
                    "can only handle JobDescriptions: "
                            + abstractDescription.getClass());
        }

        JobDescription description = (JobDescription) abstractDescription;

        // check whether there's a software description in the job
        // description
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        if (getProcessCount(description) != 1) {
            throw new GATInvocationException(
                    "Value of attribute 'process.count' cannot be handled: "
                            + getProcessCount(description));
        }
        if (getHostCount(description) != 1) {
            throw new GATInvocationException(
                    "Value of attribute 'host.count' cannot be handled: "
                            + getHostCount(description));
        }
        if (sd.getAttributes().containsKey("cores.per.process")) {
            logger.info("ignoring attribute 'cores.per.process'");
        }

        boolean separateOutput = "true".equalsIgnoreCase((String) gatContext
                .getPreferences().get("sshtrilead.separate.output"));
        boolean stoppable = "true".equalsIgnoreCase((String) gatContext
                .getPreferences().get("sshtrilead.stoppable"));

        if (stoppable && separateOutput) {
            throw new GATInvocationException(
                    "The preferences 'sshtrilead.separate.output' and 'sshtrilead.stoppable' cannot both be set to 'true'.");
        }

        // create the sandbox
        Sandbox sandbox = new Sandbox(gatContext, description, getAuthority(),
                null, true, false, false, false);
        // create the job object
        SshTrileadJob job = new SshTrileadJob(gatContext, description, sandbox);
        // add the listener to the job if specified
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }
        // and now do the prestaging
        job.setState(Job.PRE_STAGING);
        sandbox.prestage();

        // construct the ssh command
        // 1. cd to the execution dir
        String command = "cd " + sandbox.getSandbox() + " && ";
        // 2. set necessary env variables using export
        Map<String, Object> env = sd.getEnvironment();
        if (env != null && !env.isEmpty()) {
            Set<String> s = env.keySet();
            Object[] keys = (Object[]) s.toArray();

            for (int i = 0; i < keys.length; i++) {
                String val = (String) env.get(keys[i]);
                command += "export " + keys[i] + "=" + val + " && ";
            }
        }
        // 3. and finally add the executable with its arguments
        command += "exec " + getExecutable(description) + " "
                + getArguments(description);

        if (logger.isInfoEnabled()) {
            logger.info("running command: " + command);
        }

        Session session;
        try {
            session = SshTrileadFileAdaptor.getConnection(brokerURI,
                    gatContext, connectionCacheEnable, tcpNoDelay,
                    client2serverCiphers, server2clientCiphers).openSession();
            if (stoppable) {
                logger.info("starting dumb pty");
                session.requestDumbPTY();
            }
            // session.startShell();
        } catch (Exception e) {
            throw new GATInvocationException("Unable to connect!", e);
        }

        if (!sd.streamingStderrEnabled()) {
            // read away the stderr

            try {
                if (sd.getStderr() != null) {
                    // to file
                    new StreamForwarder(session.getStderr(), GAT
                            .createFileOutputStream(sd.getStderr()));
                } else {
                    // or throw it away
                    new StreamForwarder(session.getStderr(), null);
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
                    new StreamForwarder(session.getStdout(), GAT
                            .createFileOutputStream(sd.getStdout()));
                } else {
                    // or throw it away
                    new StreamForwarder(session.getStdout(), null);
                }
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Unable to create file output stream for stdout!", e);
            }
        }

        if (!sd.streamingStdinEnabled() && sd.getStdin() != null) {
            // forward the stdin from file
            try {
                new StreamForwarder(GAT.createFileInputStream(sd.getStdin()),
                        session.getStdin());
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Unable to create file input stream for stdin!", e);
            }
        }

        job.setSession(session);
        job.monitorState();

        try {
            session.execCommand(command);
            // session.getStdin().write((command + "\n").getBytes());
        } catch (IOException e) {
            throw new GATInvocationException("execution failed!", e);
        }

        job.setState(Job.RUNNING);
        return job;
    }
    // /*
    // * (non-Javadoc)
    // *
    // * @see
    // org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
    // */
    // public Job submitJob(JobDescription description, MetricListener listener,
    // String metricDefinitionName) throws GATInvocationException {
    // // TODO: this broker is not Windows compatible (&&, export)
    //
    // // check whether there's a software description in the job
    // // description
    // SoftwareDescription sd = description.getSoftwareDescription();
    // if (sd == null) {
    // throw new GATInvocationException(
    // "The job description does not contain a software description");
    // }
    //
    // boolean separateOutput = "true".equalsIgnoreCase((String) gatContext
    // .getPreferences().get("sshtrilead.separate.output"));
    // boolean stoppable = "true".equalsIgnoreCase((String) gatContext
    // .getPreferences().get("sshtrilead.stoppable"));
    //
    // if (stoppable && separateOutput) {
    // throw new GATInvocationException(
    // "The preferences 'sshtrilead.separate.output' and 'sshtrilead.stoppable'
    // cannot both be set to 'true'.");
    // }
    //
    // // create the sandbox
    // Sandbox sandbox = new Sandbox(gatContext, description, getAuthority(),
    // null, true, false, false, false);
    // // create the job object
    // SshTrileadJob job = new SshTrileadJob(gatContext, description, sandbox);
    // // add the listener to the job if specified
    // if (listener != null && metricDefinitionName != null) {
    // Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
    // .createMetric(null);
    // job.addMetricListener(listener, metric);
    // }
    // // and now do the prestaging
    // job.setState(Job.PRE_STAGING);
    // sandbox.prestage();
    //
    // // construct the ssh command
    // // 1. cd to the execution dir
    // String command = "cd " + sandbox.getSandbox() + " && ";
    // // 2. set necessary env variables using export
    // Map<String, Object> env = sd.getEnvironment();
    // if (env != null && !env.isEmpty()) {
    // Set<String> s = env.keySet();
    // Object[] keys = (Object[]) s.toArray();
    //
    // for (int i = 0; i < keys.length; i++) {
    // String val = (String) env.get(keys[i]);
    // command += "export " + keys[i] + "=" + val + " && ";
    // }
    // }
    // // 3. and finally add the executable with its arguments
    // command += "exec " + getExecutable(description) + " "
    // + getArguments(description);
    //
    // if (logger.isInfoEnabled()) {
    // logger.info("running command: " + command);
    // }
    //
    // InputStream userIn = null;
    // OutputStream userOut = null;
    // OutputStream userErr = null;
    //
    // if (sd.getStdinStream() != null) {
    // userIn = sd.getStdinStream();
    // } else if (sd.getStdinFile() != null) {
    // try {
    // userIn = GAT.createFileInputStream(sd.getStdinFile());
    // } catch (GATObjectCreationException e) {
    // throw new GATInvocationException(
    // "failed to create inputstream to read input from file: '"
    // + sd.getStdinFile() + "'", e);
    // }
    // }
    // if (sd.getStdoutStream() != null) {
    // userOut = sd.getStdoutStream();
    // } else if (sd.getStdoutFile() != null) {
    // try {
    // userOut = GAT.createFileOutputStream(sd.getStdoutFile());
    // } catch (GATObjectCreationException e) {
    // throw new GATInvocationException(
    // "failed to create outputstream to write in output file: '"
    // + sd.getStdoutFile() + "'", e);
    // }
    // }
    // if (sd.getStderrStream() != null) {
    // userErr = sd.getStderrStream();
    // } else if (sd.getStderrFile() != null) {
    // try {
    // userErr = GAT.createFileOutputStream(sd.getStderrFile());
    // } catch (GATObjectCreationException e) {
    // throw new GATInvocationException(
    // "failed to create outputstream to write in error file: '"
    // + sd.getStderrFile() + "'", e);
    // }
    // }
    // Session session;
    // try {
    // session = SshTrileadFileAdaptor
    // .getConnection(brokerURI, gatContext).openSession();
    // if (stoppable) {
    // logger.info("starting dumb pty");
    // session.requestDumbPTY();
    // }
    // // session.startShell();
    // } catch (Exception e) {
    // throw new GATInvocationException("Unable to connect!", e);
    // }
    // job.setSession(session);
    //
    // // see http://www.trilead.com/Products/Trilead-SSH-2-Java/FAQ/#blocking
    // InputStream stdout = new StreamGobbler(session.getStdout());
    // InputStream stderr = new StreamGobbler(session.getStderr());
    // try {
    // session.execCommand(command);
    // // session.getStdin().write((command + "\n").getBytes());
    // } catch (IOException e) {
    // throw new GATInvocationException("execution failed!", e);
    // }
    //
    // String executable = sd.getExecutable();
    // if (sd instanceof JavaSoftwareDescription) {
    // executable = ((JavaSoftwareDescription) sd).getJavaMain();
    // }
    //
    // if (userIn != null) {
    // new StreamForwarder(userIn, session.getStdin(), "ssh input for '"
    // + executable + "'");
    // }
    //
    // StreamForwarder outForwarder = new StreamForwarder(stdout, userOut,
    // "ssh output for '" + executable + "'");
    // StreamForwarder errForwarder = new StreamForwarder(stderr, userErr,
    // "ssh error for '" + executable + "'");
    // job.startOutputWaiter(outForwarder, errForwarder);
    // job.setState(Job.RUNNING);
    // return job;
    // }
}
