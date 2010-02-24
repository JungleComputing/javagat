package org.gridlab.gat.resources.cpi.sshtrilead;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.gridlab.gat.security.sshtrilead.HostKeyVerifier;

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

    public static Preferences getSupportedPreferences() {
        Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();
        preferences
                .put(
                        "sshtrilead.cipher.client2server",
                        "aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc");
        preferences
                .put(
                        "sshtrilead.cipher.server2client",
                        "aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc");
        preferences.put("sshtrilead.tcp.nodelay", "false");
        preferences.put("sshtrilead.use.cached.connections", "true");
        preferences.put("sshtrilead.separate.output", "true");
        preferences.put("sshtrilead.stoppable", "false");
        preferences.put("sshtrilead.caching.iswindows", "true");
        preferences.put("sshtrilead.caching.iscsh", "true");
        
        // Added: preferences for hostkey checking. Defaults are what used to be ....
        preferences.put("sshtrilead.strictHostKeyChecking", "false");
        preferences.put("sshtrilead.noHostKeyChecking", "true");
        return preferences;
    }

    public static String getDescription() {
        return "The SshTrilead ResourceBroker Adaptor implements the ResourceBroker object using the trilead ssh library. Trilead ssh is an open source full java ssh library. The ssh trilead ResourceBroker adaptor can only submit to single machines, however if you invoke a command like 'qsub' on a headnode, it might result in an application running on multiple machines. Connections with a remote ssh server can be made by using the username + password, username + keyfile, or with only a username, depending on the client and server settings.";
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "sshtrilead", "ssh"};
    }
    
    protected static Logger logger = LoggerFactory
            .getLogger(SshTrileadResourceBrokerAdaptor.class);

    public static final int IN = 0, ERR = 1, OUT = 2;

    public static final int SSH_PORT = 22;

    private boolean connectionCacheEnable;

    private String[] client2serverCiphers;

    private String[] server2clientCiphers;

    private final boolean tcpNoDelay;
    
    private final boolean isWindowsCacheEnable;
    
    private final boolean isCshCacheEnable;
    
    private boolean isWindows;
    
    private HostKeyVerifier verifier;

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
        
        // These used to be the defaults, so there.
        boolean noHostKeyChecking = true;
        boolean strictHostKeyChecking = false;

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
        isWindowsCacheEnable = ((String) p.get("sshtrilead.caching.iswindows", "true"))
                .equalsIgnoreCase("true");
        isCshCacheEnable = ((String) p.get("sshtrilead.caching.iscsh", "true"))
                .equalsIgnoreCase("true");
        noHostKeyChecking = ((String) p.get("sshtrilead.noHostKeyChecking", "true"))
                .equalsIgnoreCase("true");
        strictHostKeyChecking = ((String) p.get("sshtrilead.strictHostKeyChecking", "true"))
                .equalsIgnoreCase("true");
        
        verifier = new HostKeyVerifier(false, strictHostKeyChecking, noHostKeyChecking);
        
        isWindows = SshTrileadFileAdaptor.isWindows(gatContext, brokerURI, isWindowsCacheEnable);
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

        // check whether there's a software description in the job
        // description
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
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, sshJob,
                    listener, metricDefinitionName);
            listener = tmp;
            job = tmp;
        } else {
            job = sshJob;
        }
        if (listener != null && metricDefinitionName != null) {
            Metric metric = sshJob.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            sshJob.addMetricListener(listener, metric);
        }

        // and now do the prestaging
        sshJob.setState(Job.JobState.PRE_STAGING);
        sandbox.prestage();

        // construct the ssh command
        // 1. cd to the execution dir
        String command = "";
        if (sandbox.getSandboxPath() != null) {
            command += "cd " + sandbox.getSandboxPath() + (isWindows ? " & " : " && ");
        }
        // 2. set necessary env variables using export
        Map<String, Object> env = sd.getEnvironment();
        if (env != null && !env.isEmpty()) {
            if (isWindows) {
                throw new GATInvocationException("environment not supported for windows");
            } else {
                Set<String> s = env.keySet();
                Object[] keys = s.toArray();
                boolean isCsh = SshTrileadFileAdaptor.isCsh(gatContext, brokerURI, isCshCacheEnable);
    
                for (int i = 0; i < keys.length; i++) {
                    String val = (String) env.get(keys[i]);
                    // command += "export " + keys[i] + "=" + val + " && ";
                    // Fix: made to work for regular Bourne shell as well --Ceriel
                    if (isCsh) {
                        command += "set " + keys[i] + "=" + val + " && ";
                    } else {
                        command += keys[i] + "=" + val + " && export " + keys[i] + " && ";
                    }
                }
            }
        }
        // 3. and finally add the executable with its arguments
        command += isWindows ? ("\"" + getExecutable(description) + "\"" )
                : ("exec " + protectAgainstShellMetas(getExecutable(description)));
        String[] args = getArgumentsArray(description);
        if (args != null) {
            for (String arg : args) {
                if (isWindows) {
                    command += " " + arg;
                } else {
                    command += " " + protectAgainstShellMetas(arg);
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("running command: " + command);
        }

        Session session = null;
        try {
            try {
                session = SshTrileadFileAdaptor.getConnection(brokerURI,
                        gatContext, connectionCacheEnable, tcpNoDelay,
                        client2serverCiphers, server2clientCiphers, verifier)
                        .openSession();
            } catch (IOException e) {
                session = SshTrileadFileAdaptor.getConnection(brokerURI,
                        gatContext, false, tcpNoDelay, client2serverCiphers,
                        server2clientCiphers, verifier).openSession();
            }
            if (stoppable) {
                logger.info("starting dumb pty");
                session.requestDumbPTY();
            }
            // session.startShell();
        } catch (Exception e) {
            throw new GATInvocationException("Unable to connect!", e);
        }
        
        StreamForwarder stdout = null;
        StreamForwarder stderr = null;
        
        if (!sd.streamingStderrEnabled()) {
            // read away the stderr

            try {
                if (sd.getStderr() != null) {
                    // to file
                    stderr = new StreamForwarder(session.getStderr(), GAT
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
                    stdout = new StreamForwarder(session.getStdout(), GAT
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
        sshJob.monitorState(stdout, stderr);

        try {
            session.execCommand(command);
            // session.getStdin().write((command + "\n").getBytes());
        } catch (IOException e) {
            throw new GATInvocationException("execution failed!", e);
        }
        sshJob.setSubmissionTime();
        sshJob.setStartTime();

        sshJob.setState(Job.JobState.RUNNING);
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
