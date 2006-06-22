/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.zorilla;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.IPUtils;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author rob
 */
public class ZorillaJob extends Job implements Runnable {

    private static final Logger logger = Logger.getLogger(ZorillaJob.class);

    private static final long serialVersionUID = 1L;

    ZorillaResourceBrokerAdaptor broker;

    JobDescription description;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    // status of job

    private final String id; // UUID

    private String executable; // URI

    private Map attributes; // Map<String, String>

    private Map environment; // Map<String, String>

    private Map status; // Map<String, String>

    private int phase; // zorilla phase (NOT GAT phase)

    private final ServerSocket serverSocket;

    private final ConnectionManager connectionManager;

    // GAT state in parent class...
    // protected int state;

    GATInvocationException error = null;

    // converts a GAT file to the string representation of its URI.
    // will add the full path in case of a local file
    private static String filetoString(File file) throws GATInvocationException {
        if (file == null) {
            return "";
        }

        if (file.toURI().isLocal() && !file.toURI().isAbsolute()) {
            file = file.getAbsoluteFile();
        }

        return file.toURI().toString();
    }

    private static String virtualPath(URI uri) throws GATInvocationException {
        String[] pathElements = uri.getPath().split("/");

        if (pathElements.length == 0) {
            throw new GATInvocationException(
                "could not find filename in given uri: " + uri);
        }

        return "/" + pathElements[pathElements.length - 1];
    }

    // private Map<String, String(URI)> toStringMap(Map<File, File>);
    private static Map toStringMap(Map fileMap) throws GATInvocationException {
        Map result = new HashMap();

        Iterator iterator = fileMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();

            File key = (File) entry.getKey();
            File value = (File) entry.getValue();

            // value is "virtual" path in zorilla
            String path;

            if (value == null) {
                path = virtualPath(key.toURI());
            } else {
                URI virtualURI = value.toURI();
                String virtualScheme = virtualURI.getScheme();

                if (virtualScheme != null
                    && !virtualScheme.equalsIgnoreCase("zorilla")) {
                    throw new GATInvocationException(
                        "destination scheme can only be \"zorilla\" or null");
                }

                path = virtualURI.getPath();

            }

            result.put(path, filetoString(key));
        }
        return result;
    }

    ZorillaJob(ZorillaResourceBrokerAdaptor broker, JobDescription description)
        throws GATInvocationException {
        this.broker = broker;
        this.description = description;

        logger.debug("creating zorilla job");

        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            throw new GATInvocationException("error on creating zorilla job", e);
        }

        connectionManager = new ConnectionManager(this, serverSocket);

        // Tell the engine that we provide job.status events
        HashMap returnDef = new HashMap();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
            MetricDefinition.DISCRETE, "String", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);

        id = submitJob(description);

        updateState();

        logger.debug("done creating job");

        new Thread(this).start();
    }

    private String submitJob(JobDescription description)
        throws GATInvocationException {

        // data needed to submit a job
        URI executable;
        String[] arguments;
        Map environment; // Map<String, String>
        Map preStageFiles; // Map<String, String> (file path, URI.toString())
        Map postStageFiles; // Map<String, URI> (file path, URI.toString())
        String stdout;
        String stdin;
        String stderr;
        Map attributes; // Map<String, String>

        SoftwareDescription soft = description.getSoftwareDescription();

        executable = soft.getLocation();
        environment = soft.getEnvironment();
        if (environment == null) {
            environment = new HashMap();
        }

        preStageFiles = toStringMap(soft.getPreStaged());
        postStageFiles = toStringMap(soft.getPostStaged());

        stdout = filetoString(soft.getStdout());
        stdin = filetoString(soft.getStdin());
        stderr = filetoString(soft.getStderr());

        arguments = soft.getArguments();
        if (arguments == null) {
            arguments = new String[0];
        }

        attributes = soft.getAttributes();
        if (attributes == null) {
            attributes = new HashMap();
        }

        try {

            Socket socket = new Socket();
            InetSocketAddress address = broker.getNodeSocketAddress();

            logger.debug("connecting to " + address);

            socket.connect(address);

            DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(
                socket.getInputStream()));

            out.writeInt(ClientProtocol.VERSION);
            out.writeInt(ClientProtocol.AUTHENTICATION_NONE);
            out.flush();

            int status = in.readInt();
            String message = in.readUTF();

            if (status != ClientProtocol.STATUS_OK) {
                socket.close();
                throw new GATInvocationException(
                    "error while connecting to node: " + message);
            }

            logger.debug("connection esablished, submitting");

            out.writeInt(ClientProtocol.SUBMIT_JOB);
            out.writeUTF(executable.toString());

            // arguments;
            out.writeInt(arguments.length);
            for (int i = 0; i < arguments.length; i++) {
                out.writeUTF(arguments[i]);
            }

            ClientProtocol.writeStringMap(environment, out);
            ClientProtocol.writeStringMap(preStageFiles, out);
            ClientProtocol.writeStringMap(postStageFiles, out);
            out.writeUTF(stdout);
            out.writeUTF(stdin);
            out.writeUTF(stderr);
            ClientProtocol.writeStringMap(attributes, out);

            // callback address
            out.writeUTF(IPUtils.getLocalHostAddress().getHostAddress());
            // callback port
            out.writeInt(serverSocket.getLocalPort());
            // request state callbacks
            out.writeInt(1);

            out.flush();

            logger.debug("written request, waiting for reply");

            status = in.readInt();
            message = in.readUTF();

            logger.debug("node returned " + status + ": " + message);

            if (status != ClientProtocol.STATUS_OK) {
                socket.close();
                System.exit(1);
                throw new GATInvocationException("node returned " + status
                    + ": " + message);
            }

            String jobID = in.readUTF();

            //close connection
            out.writeInt(ClientProtocol.CLOSE_CONNECTION);
            out.flush();
            socket.close();

            logger.debug("submitted job " + jobID);

            return jobID;
        } catch (IOException e) {
            logger.debug("exception on submitting", e);
            throw new GATInvocationException("Zorilla", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getInfo()
     */
    public synchronized Map getInfo() {
        HashMap m = new HashMap();

        // stuff from zorilla node
        m.putAll(this.status);

        m.put("state", getStateString(getState()));
        m.put("resManState", m.get("phase"));
        m.put("resManName", "Zorilla");
        m.put("hostname", broker.getNodeSocketAddress().getHostName());
        if (error != null) {
            m.put("resManError", error.getMessage());
        }
        return m;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getJobDescription()
     */
    public JobDescription getJobDescription() {
        return description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getJobID()
     */
    public synchronized String getJobID() {
        return id;
    }

    private void updateState() throws GATInvocationException {
        try {

            Socket socket = new Socket();
            socket.connect(broker.getNodeSocketAddress());

            DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(
                socket.getInputStream()));

            out.writeInt(ClientProtocol.VERSION);
            out.writeInt(ClientProtocol.AUTHENTICATION_NONE);
            out.flush();

            int status = in.readInt();
            String message = in.readUTF();

            if (status != ClientProtocol.STATUS_OK) {
                socket.close();
                throw new GATInvocationException(
                    "error while connecting to node: " + message);
            }

            out.writeInt(ClientProtocol.GET_JOB_INFO);
            out.writeUTF(id);
            out.flush();

            status = in.readInt();
            message = in.readUTF();

            if (status != ClientProtocol.STATUS_OK) {
                socket.close();
                throw new GATInvocationException(
                    "error while getting job info: " + message);
            }

            if (!in.readUTF().equalsIgnoreCase(id)) {
                socket.close();
                throw new GATInvocationException(
                    "error while getting job info,"
                        + " received info for wrong job");
            }

            String executable = in.readUTF();
            Map attributes = ClientProtocol.readStringMap(in);
            Map statusMap = ClientProtocol.readStringMap(in);
            int phase = in.readInt();
            
            //close connection
            out.writeInt(ClientProtocol.CLOSE_CONNECTION);
            out.flush();
            socket.close();

            setState(executable, attributes, statusMap, phase);
        } catch (IOException e) {
            throw new GATInvocationException("ioexeption on updating state", e);
        }
    }

    void setState(String executable, Map attributes, Map status, int phase) {
        boolean change;

        synchronized (this) {
            change = (phase != this.phase);

            this.executable = executable;
            this.attributes = attributes;
            this.status = status;
            this.phase = phase;

            // convert Zorilla phase to GAT state
            if (phase == ClientProtocol.PHASE_UNKNOWN) {
                this.state = Job.UNKNOWN;
            } else if (phase == ClientProtocol.PHASE_INITIAL
                || phase == ClientProtocol.PHASE_SCHEDULING) {
                this.state = Job.INITIAL;
            } else if (phase == ClientProtocol.PHASE_RUNNING
                || phase == ClientProtocol.PHASE_CLOSED) {
                this.state = Job.RUNNING;
            } else if (phase == ClientProtocol.PHASE_COMPLETED
                || phase == ClientProtocol.PHASE_CANCELLED) {
                this.state = Job.STOPPED;
                notifyAll();
            } else if (phase == ClientProtocol.PHASE_ERROR) {
                this.state = Job.SUBMISSION_ERROR;
                notifyAll();
            }

        }

        if (change) {
            doCallBack();
        }
    }

    public synchronized int getState() {
        return state;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        // FIXME: Auto-generated method stub
        return null;
    }

    private void doCallBack() {
        MetricValue v = null;

        synchronized (this) {
            v = new MetricValue(this, getStateString(getState()), statusMetric,
                System.currentTimeMillis());
        }

        if (GATEngine.DEBUG) {
            System.err.println("default job callback: firing event: " + v);
        }

        GATEngine.fireMetric(this, v);

    }

    public void run() {
        synchronized (this) {
            while (phase < ClientProtocol.PHASE_COMPLETED) {
                try {
                    updateState();
                } catch (Exception e) {
                    error = new GATInvocationException("Zorilla", e);
                    state = Job.SUBMISSION_ERROR;
                }

                try {
                    // wait for 5 minutes, then poll again
                    // the node should send us updates if there
                    // are significant changes
                    wait(5 * 60 * 1000);
                } catch (InterruptedException e) {
                    // IGNORE
                }

            }
        }
        // one last callback
        doCallBack();

        // FIXME: do this at a sane time
        connectionManager.close();
        logger.debug("Zorilla Job exits..");
    }

}
