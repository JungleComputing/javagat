/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.zorilla;

import ibis.smartsockets.virtual.VirtualSocketFactory;
import ibis.zorilla.zoni.CallbackReceiver;
import ibis.zorilla.zoni.JobInfo;
import ibis.zorilla.zoni.ZoniConnection;
import ibis.zorilla.zoni.ZoniFileInfo;
import ibis.zorilla.zoni.ZoniInputFile;
import ibis.zorilla.zoni.ZoniProtocol;
import ibis.zorilla.zoni.ZorillaJobDescription;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.InputForwarder;
import org.gridlab.gat.engine.util.OutputForwarder;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileInterface;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * @author ndrost
 */
public class ZorillaJob extends JobCpi {

    private static final Logger logger = LoggerFactory.getLogger(ZorillaJob.class);

    private static final long serialVersionUID = 1L;

    private final ZorillaResourceBrokerAdaptor broker;

    private final ZorillaJobDescription zorillaJobDescription;

    private final MetricDefinition statusMetricDefinition;

    private final Metric statusMetric;

    private String jobID;

    private JobInfo info;

    private JobState lastState = JobState.UNKNOWN;

    private boolean postStaged = false;

    // GAT state in parent class...
    // protected int state;

    GATInvocationException error = null;

    private OutputStream stdin = null;

    private InputStream stdout = null;

    private InputStream stderr = null;

    FileOutputStream outputFile = null;

    FileOutputStream errorFile = null;

    FileInputStream inputFile = null;

    private static void addInputFile(File src, String sandboxPath,
            ZorillaJobDescription job) throws GATInvocationException {

        if (sandboxPath == null || sandboxPath.equals(".")
                || sandboxPath.equals("./")) {
            sandboxPath = src.getName();
        }

        if (src.isHidden()) {
            // hidden files are ignored
            return;
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

    protected ZorillaJob(GATContext gatContext, JobDescription description,
            Sandbox sandbox, ZorillaResourceBrokerAdaptor broker)
            throws GATInvocationException {
        super(gatContext, description, sandbox);

        this.broker = broker;

        if (logger.isDebugEnabled()) {
            logger.debug("creating zorilla job from: "
                    + description.getSoftwareDescription().toString());
        }

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        registerMetric("getJobStatus", statusMetricDefinition);

        SoftwareDescription soft = jobDescription.getSoftwareDescription();

        zorillaJobDescription = new ZorillaJobDescription();

        zorillaJobDescription.setExecutable(soft.getExecutable());
        zorillaJobDescription.setEnvironment(createStringMap(soft
                .getEnvironment()));
        zorillaJobDescription.setAttributes(createStringMap(soft
                .getAttributes()));

        zorillaJobDescription.getAttributes().put("process.count",
            "" + description.getProcessCount());
        zorillaJobDescription.getAttributes().put("resource.count",
            "" + description.getResourceCount());

        // zorillaJobDescription.getAttributes().put("host.count", "" +
        // description.getResourceCount());

        if (soft instanceof JavaSoftwareDescription) {
            JavaSoftwareDescription javaSoft = (JavaSoftwareDescription) soft;

            zorillaJobDescription.setJavaSystemProperties(javaSoft
                    .getJavaSystemProperties());
            zorillaJobDescription.setJavaMain(javaSoft.getJavaMain());
            zorillaJobDescription.setJavaArguments(javaSoft.getJavaArguments());

            if (javaSoft.getJavaOptions() != null
                    && javaSoft.getJavaOptions().length > 0) {
                String message = "java options ignored by Zorilla adaptor:";

                for (String option : javaSoft.getJavaOptions()) {
                    message += " " + option;
                }

                logger.warn(message);
            }
        } else {
            if (soft.getArguments() == null) {
                zorillaJobDescription.setArguments(new String[0]);
            } else {
                zorillaJobDescription.setArguments(soft.getArguments());
            }
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
            logger.debug("done creating zorilla job: "
                    + zorillaJobDescription.toString());
        }
    }

    public InputStream getStdout() throws GATInvocationException {
        if (!this.getJobDescription().getSoftwareDescription()
                .streamingStdoutEnabled()) {
            throw new GATInvocationException("stdout stream not enabled");
        }

        return stdout;
    }

    public InputStream getStderr() throws GATInvocationException {
        if (!this.getJobDescription().getSoftwareDescription()
                .streamingStderrEnabled()) {
            throw new GATInvocationException("stderr stream not enabled");
        }

        return stderr;
    }

    public OutputStream getStdin() throws GATInvocationException {
        if (!this.getJobDescription().getSoftwareDescription()
                .streamingStdinEnabled()) {
            throw new GATInvocationException("stdin stream not enabled");
        }

        return stdin;
    }

    public synchronized String getZorillaJobID() {
        return jobID;
    }

    protected synchronized void startJob(String address, VirtualSocketFactory factory,
            CallbackReceiver receiver) throws GATInvocationException {
        if (logger.isDebugEnabled()) {
            logger.debug("starting zorilla job:" + zorillaJobDescription);
        }

        ZoniConnection connection = null;
        try {
            connection = new ZoniConnection(address, factory, null);

            jobID = connection.submitJob(zorillaJobDescription, receiver);
            connection.close();

            SoftwareDescription soft = jobDescription.getSoftwareDescription();

            File stdinFile = soft.getStdin();
            if (stdinFile != null) {
                inputFile = GAT.createFileInputStream(stdinFile
                        .getFileInterface().getGATContext(), stdinFile);

                ZoniConnection stdinConnection = new ZoniConnection(address, factory,
                        null);

                OutputStream target = stdinConnection.getInput(jobID);

                new InputForwarder(target, inputFile);
            } else if (soft.streamingStdinEnabled()) {
                ZoniConnection stdinConnection = new ZoniConnection(address,factory,
                        null);

                stdin = stdinConnection.getInput(jobID);
            }

            File stderrFile = soft.getStderr();

            if (stderrFile != null) {
                outputFile = GAT.createFileOutputStream(stderrFile
                        .getFileInterface().getGATContext(), stderrFile);

                ZoniConnection stderrConnection = new ZoniConnection(address,factory,
                        null);

                InputStream source = stderrConnection.getOutput(jobID, true);

                new OutputForwarder(source, outputFile);

            } else if (soft.streamingStderrEnabled()) {
                ZoniConnection stderrConnection = new ZoniConnection(address,factory,
                        null);

                stdout = stderrConnection.getOutput(jobID, true);
            }

            File stdoutFile = soft.getStdout();

            if (stdoutFile != null) {
                outputFile = GAT.createFileOutputStream(stdoutFile
                        .getFileInterface().getGATContext(), stdoutFile);

                ZoniConnection stdoutConnection = new ZoniConnection(address,factory,
                        null);

                InputStream source = stdoutConnection.getOutput(jobID, false);

                new OutputForwarder(source, outputFile);

            } else if (soft.streamingStdoutEnabled()) {
                ZoniConnection stdoutConnection = new ZoniConnection(address,factory,
                        null);

                stdout = stdoutConnection.getOutput(jobID, false);
            }

            // File stdout = soft.getStdout();
            // if (soft.getStdout() != null) {
            // stdoutForwarder = new OutputForwarder(address, jobID, soft
            // .getStdoutStream(), false);
            // stdoutForwarder.startAsDaemon();
            // } else if (stdout != null) {
            // outStream = GAT.createFileOutputStream(stdout
            // .getFileInterface().getGATContext(), stdout);
            // stdoutForwarder = new OutputForwarder(address, jobID,
            // outStream, false);
            // stdoutForwarder.startAsDaemon();
            // }
            //
            // File stderr = soft.getStderr();
            // if (soft.getStderrStream() != null) {
            // stderrForwarder = new OutputForwarder(address, jobID, soft
            // .getStderrStream(), true);
            // stderrForwarder.startAsDaemon();
            // } else if (stdout != null) {
            // errStream = GAT.createFileOutputStream(stderr
            // .getFileInterface().getGATContext(), stderr);
            // stderrForwarder = new OutputForwarder(address, jobID,
            // errStream, true);
            // stderrForwarder.startAsDaemon();
            // }
        } catch (Exception e) {
            throw new GATInvocationException(
                    "cannot submit job to zorilla node", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("done starting zorilla job: " + jobID);
        }
    }

    private void copyPostStageFile(String sandboxPath, File dst)
            throws Exception {

        logger.debug("copying: " + sandboxPath + " to file " + dst);

        ZoniConnection connection = new ZoniConnection(broker
                .getNodeSocketAddress(),broker.getSocketFactory(), null);

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
            FileInterface fileInterface = dst.getFileInterface();

            if (fileInterface == null) {
                throw new Exception("could not get file file interface");
            }

            File parent = fileInterface.getParentFile();
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

            // close stdin file
            if (inputFile != null) {
                inputFile.close();
            }

            // close stdout file
            if (outputFile != null) {
                outputFile.close();
            }

            // close stderr file
            if (errorFile != null) {
                errorFile.close();
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
    public synchronized Map<String, Object> getInfo()
            throws GATInvocationException {
        HashMap<String, Object> result = new HashMap<String, Object>();

        // stuff from zorilla node
        if (info != null) {
            result.putAll(info.getStatus());
            result.put("executable", info.getExecutable());
        }

        result.put("state", state.toString());
        result.put("resManState", result.get("phase"));
        result.put("resManName", "Zorilla");
        result.put("hostname", broker.getNodeSocketAddress());
        if (error != null) {
            result.put("resManError", error.getMessage());
        }
        result.put("poststage.exception", postStageException);
        if (state == JobState.INITIAL || state == JobState.UNKNOWN) {
            result.put("submissiontime", null);
        } else {
            result.put("adaptor.job.id", jobID);
            result.put("submissiontime", submissiontime);
        }
        if (state == JobState.INITIAL || state == JobState.UNKNOWN
                || state == JobState.SCHEDULED) {
            result.put("starttime", null);
        } else {
            result.put("starttime", starttime);
        }
        if (state != JobState.STOPPED) {
            result.put("stoptime", null);
        } else {
            result.put("stoptime", stoptime);
        }

        return result;
    }

    // convert Zorilla phase to GAT state
    private JobState phase2State(int phase) throws GATInvocationException {
        if (phase == ZoniProtocol.PHASE_UNKNOWN) {
            return JobState.UNKNOWN;
        } else if (phase == ZoniProtocol.PHASE_INITIAL) {
            return JobState.INITIAL;
        } else if (phase == ZoniProtocol.PHASE_PRE_STAGE) {
            return JobState.PRE_STAGING;
        } else if (phase == ZoniProtocol.PHASE_SCHEDULING) {
            setSubmissionTime();
            return JobState.SCHEDULED;
        } else if (phase == ZoniProtocol.PHASE_RUNNING
                || phase == ZoniProtocol.PHASE_CLOSED) {
            setStartTime();
            return JobState.RUNNING;
        } else if (phase == ZoniProtocol.PHASE_POST_STAGING) {
            return JobState.POST_STAGING;
        } else if (phase == ZoniProtocol.PHASE_COMPLETED
                || phase == ZoniProtocol.PHASE_CANCELLED) {
            return JobState.STOPPED;
        } else if (phase == ZoniProtocol.PHASE_USER_ERROR) {
            return JobState.STOPPED;
        } else if (phase == ZoniProtocol.PHASE_ERROR) {
            return JobState.SUBMISSION_ERROR;
        }
        throw new GATInvocationException("unknown Zorilla phase: " + phase);
    }

    public synchronized JobState getState() {
        if (error != null) {
            return JobState.SUBMISSION_ERROR;
        }
        try {
            return phase2State(info.getPhase());
        } catch (Exception e) {
            return JobState.UNKNOWN;
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
        MetricEvent v = null;

        synchronized (this) {
            JobState state = getState();

            if (state == lastState) {
                logger.debug("no need to do callback, no significant change");
                return;
            }
            lastState = state;
            v = new MetricEvent(this, state, statusMetric, System
                    .currentTimeMillis());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("default job callback: firing event: " + v);
        }

        fireMetric(v);

    }

    public synchronized void stop() {
        if (jobID == null) {
            return;
        }

        try {
            ZoniConnection connection = new ZoniConnection(broker
                    .getNodeSocketAddress(),broker.getSocketFactory(), null);

            connection.cancelJob(jobID);
            if (!(gatContext.getPreferences().containsKey("job.stop.poststage") && gatContext
                    .getPreferences().get("job.stop.poststage").equals("false"))) {
                logger.error("zorilla can not do poststage on job.stop()");
                // state = JobState.POST_STAGING;
                // fireStatusMetric();
                // sandbox.retrieveAndCleanup(this);
            }
        } catch (Exception e) {
            logger.debug("cannot stop zorilla job", e);
        }
    }
}
