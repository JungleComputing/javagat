package org.gridlab.gat.resources.cpi.ssh;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.io.cpi.ssh.SSHSecurityUtils;
import org.gridlab.gat.io.cpi.ssh.SshUserInfo;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.util.InputForwarder;
import org.gridlab.gat.util.OutputForwarder;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * An instance of this class is used to execute remote jobs. 
 */

public class SshResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static final int SSH_PORT = 22;

    private SshUserInfo sui;

    /**
     * This method constructs a SshResourceBrokerAdaptor instance
     * corresponding to the passed GATContext.
     * 
     * @param gatContext
     *            A GATContext which will be used to execute remote jobs
     */
    public SshResourceBrokerAdaptor(GATContext gatContext, Preferences preferences)
        throws Exception {
        super(gatContext, preferences);
    }

    /**
     * This method attempts to reserve the specified hardware resource for the
     * specified time period. Upon reserving the specified hardware resource
     * this method returns a Reservation. Upon failing to reserve the specified
     * hardware resource this method returns an error.
     * 
     * @param resourceDescription
     *            A description, a HardwareResourceDescription, of the hardware
     *            resource to reserve
     * @param timePeriod
     *            The time period, a TimePeriod , for which to reserve the
     *            hardware resource
     */
    public Reservation reserveResource(ResourceDescription resourceDescription,
        TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method attempts to find one or more matching hardware resources.
     * Upon finding the specified hardware resource(s) this method returns a
     * java.util.List of HardwareResource instances. Upon failing to find the
     * specified hardware resource this method returns an error.
     * 
     * @param resourceDescription
     *            A description, a HardwareResoucreDescription, of the hardware
     *            resource(s) to find
     * @return java.util.List of HardwareResources upon success
     */
    public List findResources(ResourceDescription resourceDescription) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(JobDescription description)
        throws GATInvocationException, IOException {
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        // we do not support environment yet
        Map env = sd.getEnvironment();
        if(env != null && !env.isEmpty()) {
            throw new AdaptorNotApplicableException("cannot handle environment");
        }

        URI location = getLocationURI(description);
        ResourceDescription rd = description.getResourceDescription();
        String host = null;

        if (rd != null) {
            Object res = rd.getDescription().get("machine.node");
            if (res instanceof String) {
                host = (String) res;
            } else if (res instanceof String[]) {
                host = ((String[]) res)[0];
            }
        }

        if (!location.isCompatible("ssh")
            || (location.refersToLocalHost() && (host == null))) {
            throw new GATInvocationException("not a remote file, scheme is: "
                + location.getScheme());
        }

        Session session;
        /*decide where to run*/
        try {
            if (host != null) session = prepareSession(new URI("any://" + host
                + "/"));
            else {
                session = prepareSession(location);
                host = location.getHost();
            }
        } catch (Exception e) {
            throw new GATInvocationException(
                "could not prepare a SSH session: " + e);
        }

        String path = null;
        path = location.getPath();
        String command = path + " " + getArguments(description);

        if (GATEngine.VERBOSE) {
            System.err.println("running command: " + command);
        }

        int retry = 0;
        Channel channel = null;
        while (true) {
            try {
                session.connect();
                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                
                // ok, no exception, all is well
                break;
            } catch (Exception e) {
                // due to a bug in the jsch lib, it can sometimes throw an exveption
                // that the version string is invalid, while this is not the case.
                // The real problem is that the ssh daemon has reached the maximum number of
                // unauthenticated connections. Just retry a couple of times.
                if (e.getMessage().equals("invalid server's version string")) {
                    retry++;
                    if (retry > 3) {
                        session.disconnect();
                        throw new GATInvocationException(
                            "could not open a SSH channel (after 3 retries): "
                                + e);
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (Exception x) { 
                        //Ignore
                    }
                    if(GATEngine.VERBOSE) {
                        System.err.println("retry SSH connect");
                    }
                } else {
                    session.disconnect();
                    throw new GATInvocationException(
                        "could not open a SSH channel: " + e);
                }
            }
        }

        org.gridlab.gat.io.File stdin = sd.getStdin();
        org.gridlab.gat.io.File stdout = sd.getStdout();
        org.gridlab.gat.io.File stderr = sd.getStderr();

        if (GATEngine.VERBOSE) {
            System.err.println("start setting stdin");
        }

        if (stdin == null) {
            // close stdin.
            try {
                channel.getOutputStream().close();
            } catch (Throwable e) {
                System.err.println("Error trying to close stdin");
            }
        } else {
            try {
                FileInputStream fin = GAT.createFileInputStream(gatContext,
                    preferences, stdin.toURI());

                OutputStream out = channel.getOutputStream();
                new InputForwarder(out, fin);

                //channel.setInputStream(fin);
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException("Ssh broker", e);
            }
        }

        if (GATEngine.VERBOSE) {
            System.err.println("finished setting stdin");
        }

        // we must always read the output and error streams to avoid deadlocks
        if (stdout == null) {
            new OutputForwarder(channel.getInputStream(), false); // throw away output
        } else {
            try {
                FileOutputStream out = GAT.createFileOutputStream(gatContext,
                    preferences, stdout.toURI());

                new OutputForwarder(channel.getInputStream(), out);

            } catch (GATObjectCreationException e) {
                throw new GATInvocationException("Ssh broker", e);
            }
        }

        if (GATEngine.VERBOSE) {
            System.err.println("finished setting stdout");
        }

        // we must always read the output and error streams to avoid deadlocks
        if (stderr == null) {
            new OutputForwarder(((ChannelExec) channel).getErrStream(), false); // throw away output
        } else {
            try {
                FileOutputStream out = GAT.createFileOutputStream(gatContext,
                    preferences, stderr.toURI());

                new OutputForwarder(((ChannelExec) channel).getErrStream(), out);

            } catch (GATObjectCreationException e) {
                throw new GATInvocationException("Ssh broker", e);
            }
        }

        if (GATEngine.VERBOSE) {
            System.err.println("finished setting stderr");
        }

        Sandbox sandbox = new Sandbox(gatContext, preferences, description, host, null);

        try {
            channel.connect();
        } catch (Exception e) {
            throw new GATInvocationException(
                "Ssh broker: could not connect on " + "channel using SSH", e);
        }

        Job j = new SshJob(this, description, session, channel, sandbox);
        return j;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#reserveResource(org.gridlab.gat.resources.Resource,
     *      org.gridlab.gat.util.TimePeriod)
     */
    public Reservation reserveResource(Resource resource, TimePeriod timePeriod)
        throws RemoteException, IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    protected Session prepareSession(URI loc) throws GATInvocationException {
        JSch jsch;
        Session session;
        String host = loc.getHost();
        int port;

        //opens a ssh connection (using jsch)
        jsch = new JSch();
        java.util.Hashtable configJsch = new java.util.Hashtable(0);
        configJsch.put("StrictHostKeyChecking", "no");
        JSch.setConfig(configJsch);

        sui = null;

        try {
            sui = SSHSecurityUtils.getSshCredential(gatContext, preferences,
                "ssh", loc, SSH_PORT);
        } catch (Exception e) {
            System.out.println("SshFileAdaptor: failed to retrieve credentials"
                + e);
        }

        if (sui == null) {
            throw new GATInvocationException(
                "Unable to retrieve user info for authentication");
        }

        try {
            if (sui.privateKeyfile != null) {
                jsch.addIdentity(sui.privateKeyfile);
            }

            //to be modified, this part goes inside the SSHSecurityUtils
            if (loc.getUserInfo() != null) {
                sui.username = loc.getUserInfo();
            }

            //no passphrase		
            /*allow port override*/
            port = loc.getPort();
            /*it will always return -1 for user@host:path*/
            if (port == -1) {
                port = SSH_PORT;
            }

            if (GATEngine.DEBUG) {
                System.err.println("Prepared session for location " + loc
                    + " with username: " + sui.username + "; host: " + host);
            }

            session = jsch.getSession(sui.username, host, port);
            session.setUserInfo(sui);
            return session;

        } catch (JSchException jsche) {
            throw new GATInvocationException(
                "internal error in SshResourceBrokerAdaptor: " + jsche);
        }
    }

    protected void setUserInfo(Session session) {
        session.setUserInfo(new SshUserInfo());
    }

    /* does not add stdin to set of files to preStage */
    protected Map resolvePreStagedFiles(JobDescription description, String host)
        throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        Map result = new HashMap();
        Map pre = sd.getPreStaged();
        if (pre != null) {
            Set keys = pre.keySet();
            Iterator i = keys.iterator();
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

    protected Map resolvePostStagedFiles(JobDescription description, String host)
        throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        Map result = new HashMap();
        Map post = sd.getPostStaged();
        if (post != null) {

            Set keys = post.keySet();
            Iterator i = keys.iterator();
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
    /* should be protected in the ResourceBrokerCpi class*/
    protected File resolvePreStagedFile(File srcFile, String host)
        throws GATInvocationException {
        URI src = srcFile.toURI();
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
            throw new GATInvocationException(
                "Resource broker generic preStage", e);
        }
    }

    protected File resolvePostStagedFile(File f, String host)
        throws GATInvocationException {
        File res = null;

        URI src = f.toURI();

        if (host == null) host = "";

        String dest = "any://";
        dest += (src.getUserInfo() == null ? sui.username : src.getUserInfo());
        dest += host;
        dest += (src.getPort() == -1 ? ":" + SSH_PORT : ":" + src.getPort());
        dest += "/" + f.getName();

        URI destURI = null;
        try {
            destURI = new URI(dest);
        } catch (URISyntaxException e) {
            throw new GATInvocationException("resource broker cpi", e);
        }

        try {
            res = GAT.createFile(gatContext, preferences, destURI);
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("resource broker cpi", e);
        }

        return res;
    }
}
