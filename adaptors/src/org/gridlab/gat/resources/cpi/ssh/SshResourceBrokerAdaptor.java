package org.gridlab.gat.resources.cpi.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.InputForwarder;
import org.gridlab.gat.engine.util.OutputForwarder;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.io.cpi.ssh.SSHSecurityUtils;
import org.gridlab.gat.io.cpi.ssh.SshUserInfo;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperSubmitter;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * An instance of this class is used to execute remote jobs.
 */

public class SshResourceBrokerAdaptor extends ResourceBrokerCpi {

    protected static Logger logger = Logger
            .getLogger(SshResourceBrokerAdaptor.class);

    public static final int IN = 0, ERR = 1, OUT = 2;

    public static final int SSH_PORT = 22;

    private String host = null;

    private JSch jsch;

    private Session session;

    private Channel channel;

    private SshUserInfo sui;

    private int port;

    private WrapperSubmitter submitter;

    /**
     * This method constructs a SshResourceBrokerAdaptor instance corresponding
     * to the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which will be used to execute remote jobs
     */
    public SshResourceBrokerAdaptor(GATContext gatContext,
            Preferences preferences, URI brokerURI) throws Exception {
        super(gatContext, preferences, brokerURI);

        if (!brokerURI.isCompatible("ssh") && brokerURI.getScheme() != null
                || (brokerURI.refersToLocalHost() && (brokerURI == null))) {
            throw new GATObjectCreationException(
                    "cannot handle the scheme, scheme is: "
                            + brokerURI.getScheme());
        }
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

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(JobDescription description, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {
        try {
            SoftwareDescription sd = description.getSoftwareDescription();
            if (sd == null) {
                throw new GATInvocationException(
                        "The job description does not contain a software description");
            }

            if (getBooleanAttribute(description, "wrapper.enable", false)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("wrapper enabled, using wrapper application");
                }
                if (submitter == null) {
                    submitter = new WrapperSubmitter(gatContext, preferences,
                            brokerURI, false);
                }
                return submitter.submitJob(description);
            }

            /* decide where to run */
            try {
                prepareSession(brokerURI);
                host = brokerURI.resolveHost();
            } catch (Exception e) {
                throw new GATInvocationException("SshResourceBrokerAdaptor" + e);
            }
            String path = null;
            path = getExecutable(description);

            Sandbox sandbox = new Sandbox(gatContext, preferences, description,
                    host, null, true, false, false, false);
            SshJob job = new SshJob(gatContext, preferences, description,
                    sandbox);
            if (listener != null && metricDefinitionName != null) {
                Metric metric = job.getMetricDefinitionByName(
                        metricDefinitionName).createMetric(null);
                job.addMetricListener(listener, metric);
            }
            job.setState(Job.PRE_STAGING);
            sandbox.prestage();
            String command = "cd " + sandbox.getSandbox() + " && ";
            Map<String, Object> env = sd.getEnvironment();
            if (env != null && !env.isEmpty()) {
                Set<String> s = env.keySet();
                Object[] keys = (Object[]) s.toArray();

                for (int i = 0; i < keys.length; i++) {
                    String val = (String) env.get(keys[i]);
                    command += "export " + keys[i] + "=" + val + " && ";
                }
            }
            command += "exec " + path + " " + getArguments(description);

            if (logger.isInfoEnabled()) {
                logger.info("running command: " + command);
            }
            Object[] streams = execCommand(command);
            job.setState(Job.RUNNING);

            org.gridlab.gat.io.File stdin = sd.getStdin();
            org.gridlab.gat.io.File stdout = sd.getStdout();
            org.gridlab.gat.io.File stderr = sd.getStderr();

            if (logger.isInfoEnabled()) {
                logger.info("start setting stdin");
            }

            if (stdin == null) {
                // close stdin.
                try {
                    channel.getOutputStream().close();
                } catch (Throwable e) {
                    logger.error("Error trying to close stdin" + e);
                }
            } else {
                try {
                    FileInputStream fin = GAT.createFileInputStream(gatContext,
                            preferences, stdin.toGATURI());

                    new InputForwarder(((OutputStream) streams[OUT]), fin);

                    // channel.setInputStream(fin);
                } catch (GATObjectCreationException e) {
                    throw new GATInvocationException(
                            "SshResourceBrokerAdaptor", e);
                }
            }

            if (logger.isInfoEnabled()) {
                logger.info("finished setting stdin");
            }

            // we must always read the output and error streams to avoid
            // deadlocks
            if (stdout == null) {
                new OutputForwarder((InputStream) streams[IN], false); // throw
                // away
                // output
            } else {
                try {
                    FileOutputStream out = GAT.createFileOutputStream(
                            gatContext, preferences, stdout.toGATURI());

                    new OutputForwarder((InputStream) streams[IN], out);

                } catch (GATObjectCreationException e) {
                    throw new GATInvocationException(
                            "SshResourceBrokerAdaptor", e);
                }
            }

            if (logger.isInfoEnabled()) {
                logger.info("finished setting stdout");
            }

            // we must always read the output and error streams to avoid
            // deadlocks
            if (stderr == null) {
                new OutputForwarder((InputStream) streams[ERR], false); // throw
                // away
                // output
            } else {
                try {
                    FileOutputStream out = GAT.createFileOutputStream(
                            gatContext, preferences, stderr.toGATURI());

                    new OutputForwarder((InputStream) streams[ERR], out);

                } catch (GATObjectCreationException e) {
                    throw new GATInvocationException(
                            "SshResourceBrokerAdaptor", e);
                }
            }

            if (logger.isInfoEnabled()) {
                logger.info("finished setting stderr");
            }
            job.setChannel(channel);
            job.setSession(session);
            job.startProcessWaiter();
            return job;
        } catch (Exception e) {
            throw new GATInvocationException("SshResourceBrokerAdaptor", e);
        }
    }

    protected void prepareSession(URI loc) throws GATInvocationException {
        String host = loc.resolveHost();
        jsch = new JSch();

        Hashtable<String, String> configJsch = new Hashtable<String, String>();
        configJsch.put("StrictHostKeyChecking", "no");
        JSch.setConfig(configJsch);

        sui = null;

        try {
            sui = SSHSecurityUtils.getSshCredential(gatContext, preferences,
                    "ssh", loc, SSH_PORT);
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("SshFileAdaptor: failed to retrieve credentials"
                        + e);
            }
        }

        if (sui == null) {
            throw new GATInvocationException(
                    "Unable to retrieve user info for authentication");
        }

        try {
            if (sui.privateKeyfile != null) {
                jsch.addIdentity(sui.privateKeyfile);
            }

            // to be modified, this part goes inside the SSHSecurityUtils
            if (loc.getUserInfo() != null) {
                sui.username = loc.getUserInfo();
            }

            // to be modified, this part goes inside the SSHSecurityUtils
            if (loc.getUserInfo() != null) {
                sui.username = loc.getUserInfo();
            }

            if (sui.username == null) {
                sui.username = System.getProperty("user.name");
            }

            // no passphrase
            /* allow port override */
            port = loc.getPort();

            /* it will always return -1 for user@host:path */
            if (port == -1) {
                port = SSH_PORT;
            }

            // portNumber = port + "";
            if (logger.isDebugEnabled()) {
                logger
                        .debug("Prepared session for location " + loc
                                + " with username: " + sui.username
                                + "; host: " + host);
            }
            session = jsch.getSession(sui.username, host, port);
            session.setUserInfo(sui);
        } catch (Exception e) {
            if (e instanceof JSchException) {
                if (e.getMessage().equals("Auth fail")) {
                    throw new InvalidUsernameOrPasswordException(e);
                }
            } else {
                throw new GATInvocationException("SshResourceBrokerAdaptor", e);
            }
        }
    }

    protected void setUserInfo(Session session) {
        session.setUserInfo(new SshUserInfo());
    }

    /* does not add stdin to set of files to preStage */
    protected Map<File, File> resolvePreStagedFiles(JobDescription description,
            String host) throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        Map<File, File> result = new HashMap<File, File>();
        Map<File, File> pre = sd.getPreStaged();
        if (pre != null) {
            Set<File> keys = pre.keySet();
            Iterator<File> i = keys.iterator();
            while (i.hasNext()) {
                File srcFile = (File) i.next();
                File destFile = (File) pre.get(srcFile);
                if (destFile != null) { // already set manually
                    result.put(srcFile, destFile);
                    continue;
                }

                result.put(srcFile, resolvePreStagedFile(srcFile, host));
            }
        }

        return result;
    }

    protected Map<File, File> resolvePostStagedFiles(
            JobDescription description, String host)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        Map<File, File> result = new HashMap<File, File>();
        Map<File, File> post = sd.getPostStaged();
        if (post != null) {

            Set<File> keys = post.keySet();
            Iterator<File> i = keys.iterator();
            while (i.hasNext()) {
                File destFile = (File) i.next();
                File srcFile = (File) post.get(destFile);
                if (srcFile != null) { // already set manually
                    result.put(destFile, srcFile);
                    continue;
                }

                result.put(destFile, resolvePostStagedFile(destFile, host));
            }
        }

        return result;
    }

    /* Creates a file object for the destination of the preStaged src file */
    /* should be protected in the ResourceBrokerCpi class */
    protected File resolvePreStagedFile(File srcFile, String host)
            throws GATInvocationException {
        URI src = srcFile.toGATURI();
        String path = new java.io.File(src.getPath()).getName();

        String dest = "any://";
        dest += (src.getUserInfo() == null ? sui.username : src.getUserInfo());
        dest += host;
        dest += (src.getPort() == -1 ? ":" + SSH_PORT : ":" + src.getPort());
        dest += "/" + path;

        try {
            URI destURI = new URI(dest);
            return GAT.createFile(gatContext, preferences, destURI);
        } catch (Exception e) {
            throw new GATInvocationException("SshResourceBrokerAdaptor", e);
        }
    }

    protected File resolvePostStagedFile(File f, String host)
            throws GATInvocationException {
        File res = null;

        URI src = f.toGATURI();

        if (host == null)
            host = "";

        String dest = "any://";
        dest += (src.getUserInfo() == null ? sui.username : src.getUserInfo());
        dest += host;
        dest += (src.getPort() == -1 ? ":" + SSH_PORT : ":" + src.getPort());
        dest += "/" + f.getName();

        URI destURI = null;
        try {
            destURI = new URI(dest);
        } catch (URISyntaxException e) {
            throw new GATInvocationException("SshResourceBrokerAdaptor", e);
        }

        try {
            res = GAT.createFile(gatContext, preferences, destURI);
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("SshResourceBrokerAdaptor", e);
        }

        return res;
    }

    private Object[] execCommand(String command) throws JSchException,
            IOException, GATInvocationException {
        Object[] result = new Object[3];
        session = jsch.getSession(sui.username, host, port);
        session.setUserInfo(sui);
        session.connect();
        channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        result[IN] = ((ChannelExec) channel).getInputStream();
        result[ERR] = ((ChannelExec) channel).getErrStream();
        result[OUT] = ((ChannelExec) channel).getOutputStream();

        channel.connect();
        return result;
    }

}
