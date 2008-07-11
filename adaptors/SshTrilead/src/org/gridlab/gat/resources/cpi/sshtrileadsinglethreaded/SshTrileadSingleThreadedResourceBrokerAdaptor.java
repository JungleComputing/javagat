package org.gridlab.gat.resources.cpi.sshtrileadsinglethreaded;

import java.io.IOException;
import java.util.HashMap;
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
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperJobCpi;

import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Session;

/**
 * An instance of this class is used to execute remote jobs.
 */

public class SshTrileadSingleThreadedResourceBrokerAdaptor extends
        ResourceBrokerCpi implements Runnable {

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
            .getLogger(SshTrileadSingleThreadedResourceBrokerAdaptor.class);

    // update status of each job every half second
    public static final int TIMEOUT = 500;

    private final Map<String, SshTrileadJob> jobs;

    private static boolean ended = false;

    private static synchronized boolean hasEnded() {
        return ended;
    }

    // called by the gat engine
    public static synchronized void end() {
        ended = true;
    }

    public static final int IN = 0, ERR = 1, OUT = 2;

    public static final int SSH_PORT = 22;

    private boolean connectionCacheEnable;

    private String[] client2serverCiphers;

    private String[] server2clientCiphers;

    private boolean tcpNoDelay;

    /**
     * This method constructs a SshResourceBrokerAdaptor instance corresponding
     * to the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which will be used to execute remote jobs
     */
    public SshTrileadSingleThreadedResourceBrokerAdaptor(GATContext gatContext,
            URI brokerURI) throws Exception {
        super(gatContext, brokerURI);

        jobs = new HashMap<String, SshTrileadJob>();

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
        // start a thread to monitor jobs
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("sshtrileadsinglethreaded job monitor");
        thread.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(AbstractJobDescription abstractDescription,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {
        if (!(abstractDescription instanceof JobDescription)) {
            throw new GATInvocationException(
                    "can only handle JobDescriptions: "
                            + abstractDescription.getClass());
        }

        JobDescription description = (JobDescription) abstractDescription;

        // TODO: this broker is not Windows compatible (&&, export)

        // check whether there's a software description in the job
        // description
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
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
        SshTrileadJob sshJob = new SshTrileadJob(gatContext, description,
                sandbox);
        Job job = null;
        if (description instanceof WrapperJobDescription) {
            WrapperJobCpi tmp = new WrapperJobCpi(sshJob);
            listener = tmp;
            job = tmp;
        } else {
            job = sshJob;
        }
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }

        // add the listener to the job if specified
        if (listener != null && metricDefinitionName != null) {
            Metric metric = sshJob.getMetricDefinitionByName(
                    metricDefinitionName).createMetric(null);
            sshJob.addMetricListener(listener, metric);
        }
        // and now do the prestaging
        sshJob.setState(Job.JobState.PRE_STAGING);
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

        sshJob.setSession(session);
        jobs.put(sshJob.getJobID(), sshJob);
        // job.monitorState();

        try {
            session.execCommand(command);
            // session.getStdin().write((command + "\n").getBytes());
        } catch (IOException e) {
            throw new GATInvocationException("execution failed!", e);
        }

        sshJob.setState(Job.JobState.RUNNING);
        return job;
    }

    public void run() {
        while (!hasEnded()) {
            updateJobInfos();
            try {
                Thread.sleep(TIMEOUT);
            } catch (InterruptedException e) {
                // IGNORE
            }
        }

    }

    private void updateJobInfos() {
        for (SshTrileadJob job : jobs.values()) {
            if ((job.getSession().waitForCondition(
                    ChannelCondition.EXIT_STATUS, 1) & ChannelCondition.EXIT_STATUS) != 0) {
                job.setExitStatus();
                try {
                    job.stop();
                } catch (GATInvocationException e) {

                }
                jobs.remove(job.getJobID());
            }
        }

    }

}
