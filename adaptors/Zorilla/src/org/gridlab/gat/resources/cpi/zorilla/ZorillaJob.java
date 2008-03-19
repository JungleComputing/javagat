/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.zorilla;

import ibis.zorilla.zoni.CallbackReceiver;
import ibis.zorilla.zoni.InputForwarder;
import ibis.zorilla.zoni.JobInfo;
import ibis.zorilla.zoni.OutputForwarder;
import ibis.zorilla.zoni.ZoniConnection;
import ibis.zorilla.zoni.ZoniFileInfo;
import ibis.zorilla.zoni.ZoniInputFile;
import ibis.zorilla.zoni.ZoniProtocol;
import ibis.zorilla.zoni.ZorillaJobDescription;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * @author ndrost
 */
public class ZorillaJob extends JobCpi {

    private static final Logger logger = Logger.getLogger(ZorillaJob.class);

    private static final long serialVersionUID = 1L;

    private final ZorillaResourceBrokerAdaptor broker;

    private final ZorillaJobDescription zorillaJobDescription;

    private final MetricDefinition statusMetricDefinition;

    private final Metric statusMetric;

    private String jobID;

    private JobInfo info;

    private int lastState = -1;

    private boolean postStaged = false;

    // GAT state in parent class...
    // protected int state;

    GATInvocationException error = null;

    InputForwarder stdinForwarder;
    OutputForwarder stdoutForwarder;
    FileOutputStream outStream;
    OutputForwarder stderrForwarder;
    FileOutputStream errStream;

    private static void addInputFile(File src, String sandboxPath,
            ZorillaJobDescription job) throws GATInvocationException {

        if (sandboxPath == null) {
            sandboxPath = src.getName();
        }

        if (src.isDirectory()) {
            for (File child : src.getFileInterface().listFiles()) {
                addInputFile(child, sandboxPath + "/" + child.getName(), job);
            }
        } else {
            try {
                job.addInputFile(new ZoniInputFile(sandboxPath, GAT
                        .createFileInputStream(src.getFileInterface()
                                .getGATContext(), src), src.getFileInterface()
                        .length()));
            } catch (Exception e) {
                throw new GATInvocationException(
                        "could not create input file for " + sandboxPath, e);
            }
        }
    }

    /**
     * Convert a <String, Object> map to a <String, String> map by calling
     * toString() on all the values
     */
    private static Map<String, String> createStringMap(
            Map<String, Object> objectMap) {
        Map<String, String> result = new HashMap<String, String>();

        if (objectMap == null) {
            return result;
        }

        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            if (entry.getValue() == null) {
                result.put(entry.getKey(), null);
            } else {
                result.put(entry.getKey(), entry.getValue().toString());
            }
        }

        return result;
    }

    protected ZorillaJob(GATContext gatContext, Preferences preferences,
            JobDescription description, Sandbox sandbox,
            ZorillaResourceBrokerAdaptor broker) throws GATInvocationException {
        super(gatContext, preferences, description, sandbox);

        this.broker = broker;

        if (logger.isDebugEnabled()) {
            logger.debug("creating zorilla job");
        }

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);

        SoftwareDescription soft = jobDescription.getSoftwareDescription();

        zorillaJobDescription = new ZorillaJobDescription();

        zorillaJobDescription.setExecutable(soft.getExecutable());
        zorillaJobDescription.setArguments(soft.getArguments());
        zorillaJobDescription.setEnvironment(createStringMap(soft
                .getEnvironment()));
        zorillaJobDescription.setAttributes(createStringMap(soft
                .getAttributes()));

        if (soft instanceof JavaSoftwareDescription) {
            JavaSoftwareDescription javaSoft = (JavaSoftwareDescription) soft;
            zorillaJobDescription.setJavaOptions(javaSoft.getJavaOptions());
            zorillaJobDescription.setJavaSystemProperties(javaSoft
                    .getJavaSystemProperties());
            zorillaJobDescription.setJavaMain(javaSoft.getJavaMain());
            zorillaJobDescription.setJavaArguments(javaSoft.getJavaArguments());
        }

        for (Map.Entry<File, File> entry : soft.getPreStaged().entrySet()) {
            File dst = entry.getValue();
            if (dst == null) {
                addInputFile(entry.getKey(), null, zorillaJobDescription);
            } else {
                addInputFile(entry.getKey(), dst.getPath(),
                        zorillaJobDescription);
            }
        }

        for (File file : soft.getPostStaged().keySet()) {
            zorillaJobDescription.addOutputFile(file.getPath(), null);
        }

        // TODO:also implement batch jobs
        zorillaJobDescription.setInteractive(true);

        if (logger.isDebugEnabled()) {
            logger.debug("done creating zorilla job");
        }
    }

    protected synchronized void startJob(InetSocketAddress address,
            CallbackReceiver receiver) throws GATInvocationException {
        if (logger.isDebugEnabled()) {
            logger.debug("starting zorilla job");
        }

        try {
            ZoniConnection connection = new ZoniConnection(address, null,
                    ZoniProtocol.TYPE_CLIENT);

            jobID = connection.submitJob(zorillaJobDescription, receiver);
            connection.close();

            SoftwareDescription soft = jobDescription.getSoftwareDescription();

            File stdin = soft.getStdin();

            if (stdin != null) {
                FileInputStream in = GAT.createFileInputStream(stdin
                        .getFileInterface().getGATContext(), stdin);
                stdinForwarder = new InputForwarder(address, jobID, in);
                stdinForwarder.startAsDaemon();
            }

            File stdout = soft.getStdout();
            if (soft.stdoutIsStreaming()) {
                stdoutForwarder = new OutputForwarder(address, jobID, soft
                        .getStdoutStream(), false);
                stdoutForwarder.startAsDaemon();
            } else if (stdout != null) {
                outStream = GAT.createFileOutputStream(stdout
                        .getFileInterface().getGATContext(), stdout);
                stdoutForwarder = new OutputForwarder(address, jobID,
                        outStream, false);
                stdoutForwarder.startAsDaemon();
            }

            File stderr = soft.getStderr();
            if (soft.stderrIsStreaming()) {
                stderrForwarder = new OutputForwarder(address, jobID, soft
                        .getStderrStream(), true);
                stderrForwarder.startAsDaemon();
            } else if (stdout != null) {
                errStream = GAT.createFileOutputStream(stderr
                        .getFileInterface().getGATContext(), stderr);
                stderrForwarder = new OutputForwarder(address, jobID,
                        errStream, true);
                stderrForwarder.startAsDaemon();
            }
        } catch (Exception e) {
            throw new GATInvocationException(
                    "cannot submit job to zorilla node", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("done starting zorilla job");
        }
    }

    private void copyPostStageFile(String sandboxPath, File dst)
            throws Exception {

        logger.debug("copying: " + sandboxPath + " to file " + dst);

        ZoniConnection connection = new ZoniConnection(broker
                .getNodeSocketAddress(), null, ZoniProtocol.TYPE_CLIENT);

        ZoniFileInfo info = connection.getFileInfo(sandboxPath, jobID);

        logger.debug("info = " + info);

        if (info.isDirectory()) {
            for (ZoniFileInfo child : info.getChildren()) {
                URI childLocation = new URI(dst.toGATURI().toString() + "/"
                        + child.getName());
                File childFile = GAT.createFile(dst.getFileInterface()
                        .getGATContext(), childLocation);

                copyPostStageFile(sandboxPath + "/" + child.getName(),
                        childFile);
            }
        } else {
            File parent = dst.getFileInterface().getParentFile();
            if (parent != null) {
                parent.getFileInterface().mkdirs();
            }
            FileOutputStream fileStream = GAT.createFileOutputStream(dst
                    .getFileInterface().getGATContext(), dst);

            connection.getOutputFile(fileStream, sandboxPath, jobID);

            fileStream.close();
        }
    }

    private void postStage() {
        synchronized (this) {
            if (postStaged) {
                return;
            }
            postStaged = true;
        }

        try {

            // close stdout file
            if (outStream != null) {
                outStream.close();
            }

            // close stderr file
            if (errStream != null) {
                errStream.close();
            }

            // copy poststage files
            for (Map.Entry<File, File> entry : jobDescription
                    .getSoftwareDescription().getPostStaged().entrySet()) {
                String sandboxPath = entry.getKey().getPath();
                File src = entry.getKey();
                File dst = entry.getValue();

                if (dst == null) {
                    GATContext context = entry.getKey().getFileInterface()
                            .getGATContext();
                    URI location = new URI(src.getName());
                    dst = GAT.createFile(context, location);
                }

                copyPostStageFile(sandboxPath, dst);
            }

        } catch (Exception e) {
            synchronized (this) {
                error = new GATInvocationException("cannot post-stage files", e);
                logger.warn("Zorilla: error on post staging job " + jobID, e);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getInfo()
     */
    @SuppressWarnings("unchecked")
    public synchronized Map<String, Object> getInfo()
            throws GATInvocationException {
        HashMap<String, Object> result = new HashMap<String, Object>();

        // stuff from zorilla node
        if (info != null) {
            result.putAll(info.getStatus());
            result.put("executable", info.getExecutable());
        }

        result.put("state", getStateString(getState()));
        result.put("resManState", result.get("phase"));
        result.put("resManName", "Zorilla");
        result.put("hostname", broker.getNodeSocketAddress().getHostName());
        if (error != null) {
            result.put("resManError", error.getMessage());
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.Job#getJobID()
     */
    public synchronized String getJobID() {
        return jobID;
    }

    // convert Zorilla phase to GAT state
    private int phase2State(int phase) throws GATInvocationException {
        if (phase == ZoniProtocol.PHASE_UNKNOWN) {
            return Job.UNKNOWN;
        } else if (phase == ZoniProtocol.PHASE_INITIAL) {
            return Job.INITIAL;
        } else if (phase == ZoniProtocol.PHASE_PRE_STAGE) {
            return Job.PRE_STAGING;
        } else if (phase == ZoniProtocol.PHASE_SCHEDULING) {
            return Job.SCHEDULED;
        } else if (phase == ZoniProtocol.PHASE_RUNNING
                || phase == ZoniProtocol.PHASE_CLOSED) {
            return Job.RUNNING;
        } else if (phase == ZoniProtocol.PHASE_POST_STAGING) {
            return Job.POST_STAGING;
        } else if (phase == ZoniProtocol.PHASE_COMPLETED
                || phase == ZoniProtocol.PHASE_CANCELLED) {
            return Job.STOPPED;
        } else if (phase == ZoniProtocol.PHASE_ERROR) {
            return Job.SUBMISSION_ERROR;
        }
        throw new GATInvocationException("unknown Zorilla phase: " + phase);
    }

    public synchronized int getState() {
        if (error != null) {
            return SUBMISSION_ERROR;
        }
        try {
            return phase2State(info.getPhase());
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

    @Override
    public synchronized int getExitStatus() throws GATInvocationException {
        return info.getExitStatus();
    }

    void setInfo(JobInfo info) {
        synchronized (this) {
            this.info = info;
        }
        if (hasEnded()) {
            postStage();
        }

        fireStatusMetric();
    }

    synchronized boolean hasEnded() {
        return info.getPhase() >= ZoniProtocol.PHASE_COMPLETED;
    }

    private void fireStatusMetric() {
        MetricValue v = null;

        synchronized (this) {
            int state = getState();

            if (state == lastState) {
                logger.debug("no need to do callback, no significant change");
                return;
            }
            lastState = state;
            v = new MetricValue(this, getStateString(state), statusMetric,
                    System.currentTimeMillis());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("default job callback: firing event: " + v);
        }

        GATEngine.fireMetric(this, v);

    }

    public synchronized void stop() {
        if (jobID == null) {
            return;
        }

        try {
            ZoniConnection connection = new ZoniConnection(broker
                    .getNodeSocketAddress(), null, ZoniProtocol.TYPE_CLIENT);

            connection.cancelJob(jobID);
        } catch (Exception e) {
            logger.debug("cannot stop zorilla job", e);
        }

    }
}
