/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.resources.cpi.zorilla;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ibis.zorilla.zoni.JobInfo;
import ibis.zorilla.zoni.ZoniConnection;
import ibis.zorilla.zoni.ZoniException;
import ibis.zorilla.zoni.ZoniProtocol;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;

/**
 * @author ndrost
 */
public class ZorillaJob extends JobCpi {

    private static final Logger logger = Logger.getLogger(ZorillaJob.class);

    private static final long serialVersionUID = 1L;

    ZorillaResourceBrokerAdaptor broker;

    JobDescription description;

    MetricDefinition statusMetricDefinition;

    Metric statusMetric;

    private String jobID;

    private JobInfo info;

    private int lastState = -1;

    // GAT state in parent class...
    // protected int state;

    GATInvocationException error = null;

    /**
     * Returns the path of a file.
     */
    private static String getPath(File file) throws GATInvocationException {
        if (file == null) {
            return null;
        }

        if (!file.toGATURI().isLocal()) {
            throw new GATInvocationException(
                    "zorilla can only handle local files");
        }

        return file.getAbsolutePath();
    }

    /**
     * Creates a string map from a file map for pre stage files. SRC (key) in
     * the file map is a "real" file, DST (value) in this map is a file in the
     * "zorilla virtual file system"
     */
    private static Map<String, String> createPreStageMap(Map<File, File> fileMap)
            throws GATInvocationException {
        Map<String, String> result = new HashMap<String, String>();

        for (Map.Entry<File, File> entry : fileMap.entrySet()) {
            File src = entry.getKey();
            File dst = entry.getValue();

            // src of a pre-stage file must be an actual local file
            String srcPath = getPath(src);

            String dstPath;

            if (dst == null) {
                // default for destination is simply the filename
                // (root of sandbox)
                dstPath = src.getName();
            } else if (dst.isAbsolute()) {
                throw new GATInvocationException(
                        "absolute files not alowed as destination for pre-stage files: "
                                + dst);
            } else if (dst.toGATURI().getScheme() != null
                    || dst.toGATURI().getHost() != null) {
                throw new GATInvocationException(
                        "no scheme or hostname allowed in pre-stage destination: "
                                + dst);
            } else {
                dstPath = dst.getPath();
            }

            result.put(srcPath, dstPath);
        }

        return result;
    }

    /**
     * Creates a string map from a file map for pre stage files. SRC (key) in
     * the file map is a "virtual" file, DST (value) in this map is a "real"
     * file
     */
    private static Map<String, String> createPostStageMap(
            Map<File, File> fileMap) throws GATInvocationException {
        Map<String, String> result = new HashMap<String, String>();

        for (Map.Entry<File, File> entry : fileMap.entrySet()) {
            File src = entry.getKey();
            File dst = entry.getValue();

            // src of a post-stage file must be a relative path
            String srcPath;
            if (src.isAbsolute()) {
                throw new GATInvocationException(
                        "absolute files not alowed as source for post-stage files: "
                                + src);
            } else if (src.toGATURI().getScheme() != null
                    || src.toGATURI().getHost() != null) {
                throw new GATInvocationException(
                        "no scheme or hostname allowed in post-stage source: "
                                + src);
            } else {
                srcPath = src.getPath();
            }

            String dstPath;
            if (dst == null) {
                // default for destination is a file in CWD with the
                // filename of the source
                String userDir = System.getProperty("user.dir");

                if (userDir == null) {
                    throw new GATInvocationException(
                            "could not get current working directory");
                }

                dstPath = userDir + File.separator + src.getName();
            } else {
                dstPath = dst.getAbsolutePath();
            }

            result.put(srcPath, dstPath);
        }

        return result;
    }

    /**
     * Convert a <String, Object> map to a <String, String> map by calling
     * toString() on all the values
     */
    private static Map<String, String> objectMapToStringMap(
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

    ZorillaJob(GATContext gatContext, Preferences preferences,
            ZorillaResourceBrokerAdaptor broker, JobDescription description)
            throws GATInvocationException {
        super(gatContext, preferences, description, null);
        this.broker = broker;
        this.description = description;

        logger.debug("creating zorilla job");

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition =
            new MetricDefinition("job.status", MetricDefinition.DISCRETE,
                    "String", null, null, returnDef);
        statusMetric = statusMetricDefinition.createMetric(null);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);

        // data needed to submit a job
        URI executable;
        String[] arguments;
        Map<String, String> attributes;
        Map<String, String> environment;
        Map<String, String> preStageFiles;
        Map<String, String> postStageFiles;
        String stdout;
        String stdin;
        String stderr;

        SoftwareDescription soft = description.getSoftwareDescription();

        executable = soft.getLocation();
        environment = objectMapToStringMap(soft.getEnvironment());

        preStageFiles = createPreStageMap(soft.getPreStaged());
        postStageFiles = createPostStageMap(soft.getPostStaged());

        stdout = getPath(soft.getStdout());
        stdin = getPath(soft.getStdin());
        stderr = getPath(soft.getStderr());

        arguments = soft.getArguments();
        if (arguments == null) {
            arguments = new String[0];
        }

        attributes = objectMapToStringMap(soft.getAttributes());

        try {
            ZoniConnection connection =
                new ZoniConnection(broker.getNodeSocketAddress(), null,
                        ZoniProtocol.TYPE_CLIENT);

            jobID =
                connection.submitJob(executable.toString(), arguments,
                    environment, attributes, preStageFiles, postStageFiles,
                    stdin, stdout, stderr, broker.getCallbackReceiver());
            connection.close();
        } catch (IOException e) {
            throw new GATInvocationException(
                    "cannot submit job to zorilla node", e);
        } catch (ZoniException e) {
            throw new GATInvocationException(
                    "cannot submit job to zorilla node", e);
        }

        logger.debug("done creating job");
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
        try {
            return phase2State(info.getPhase());
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

    synchronized void setInfo(JobInfo info) {
        this.info = info;
    }

    synchronized boolean hasEnded() {
        return info.getPhase() >= ZoniProtocol.PHASE_COMPLETED;
    }

    void fireStatusMetric() throws GATInvocationException {
        MetricValue v = null;

        synchronized (this) {
            int state = getState();

            if (state == lastState) {
                // no need to do callback, no significant change
                return;
            }
            lastState = state;
            v =
                new MetricValue(this, getStateString(state), statusMetric,
                        System.currentTimeMillis());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("default job callback: firing event: " + v);
        }

        GATEngine.fireMetric(this, v);

    }

    public void stop() {
        try {
            ZoniConnection connection =
                new ZoniConnection(broker.getNodeSocketAddress(), null,
                        ZoniProtocol.TYPE_CLIENT);

            connection.cancelJob(jobID);
        } catch (Exception e) {
            logger.debug("cannot stop zorilla job", e);
        }

    }
}
