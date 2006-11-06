package org.gridlab.gat.resources.cpi.proactive;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.filetransfer.FileTransfer;
import org.objectweb.proactive.filetransfer.FileVector;

/**
 * Internal representation of a job submitted to the JavaGAT.
 *
 * @see org.gridlab.gat.resources.Job.
 */
public class Job extends JobCpi {

    /** Counter for generating job identifications. */
    private static int jobCounter;

    /** For JavaGat monitoring. */
    private MetricDefinition statusMetricDefinition;

    /** For JavaGat monitoring. */
    private Metric statusMetric;

    /** Map for supplying the result of the getInfo() method. */
    private HashMap infoMap = new HashMap();

    /** Exit status of this job. */
    private int exitStatus = 0;

    /** Class name of the application. */
    private String className;

    /** JVM parameters. */
    private String jvmArgs;

    /** Application arguments. */
    private String progArgs;

    /** Class path. */
    private String classPath = null;

    /** The broker that submitted this job. */
    private ProActiveResourceBrokerAdaptor broker;

    /** Number of instances of the application. */
    private int nInstances = 1;

    /** Number of nodes on which these instances must be run. */
    private int nNodes = -1;

    /** When set, use the Sandbox mechanism for stage-in and state-out. */
    private boolean wantsSandbox = true;

    /** List of nodes on which the job instances are run. */
    private NodeInfo[] nodes = null;

    /** Identification of the job instances. */
    private String[] jobIDs = null;

    /** Identification of this Gatjob. */
    private String jobID = null;

    /** Set when the application is actually started. */
    private boolean started = false;

    /** Counts how many instances are finished. */
    private int finished = 0;

    /**
     * Constructor. There is no Sandbox here, because at this point
     * it is not known on which node(s) the job will be run. The sandbox
     * will be created when the job is actually scheduled.
     * @param gatContext the GAT context.
     * @param preferences the preferences.
     * @param jobDescription the job description.
     * @param broker the resource broker that initiated this job.
     * @exception GATInvocationException when something goes wrong.
     */
    public Job(GATContext gatContext, Preferences preferences,
            JobDescription jobDescription,
            ProActiveResourceBrokerAdaptor broker)
            throws GATInvocationException {

        // No sandbox, we don't know the node(s) yet.
        super(gatContext, preferences, jobDescription, null);
        this.broker = broker;

        if (preferences.get("noSandbox") != null) {
            wantsSandbox = false;
        }

        // Tell the engine that we provide job.status events
        HashMap returnDef = new HashMap();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
            MetricDefinition.DISCRETE, "String", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);

        // Set status and submission time.
        setState(INITIAL);
        infoMap.put("submissiontime", new Long(System.currentTimeMillis()));

        // Get everything we need from the job description.
        SoftwareDescription soft = jobDescription.getSoftwareDescription();

        URI executable = soft.getLocation();
        String scheme = executable.getScheme();
        if (! "java".equalsIgnoreCase(scheme)) {
            throw new GATInvocationException(
                    "Executable should be \"java:<classname>\"");
        }

        className = executable.getSchemeSpecificPart();

        if (soft.getStdin() != null
                || soft.getStdout() != null
                || soft.getStderr() != null) {
            throw new GATInvocationException(
                    "Redirection of standard input, output or error is not "
                    + "supported.");
        }

        Map environment;
        environment = soft.getEnvironment();
        if (environment == null) {
            environment = new HashMap();
        }

        // Get program arguments as a string.
        progArgs = "";
        String[] arguments;
        arguments = soft.getArguments();
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                progArgs = progArgs + arguments[i];
                if (i < arguments.length-1) {
                    progArgs = progArgs + " ";
                }
            }
        }

        // Get JVM arguments.
        jvmArgs = "";
        String space = "";
        for (Iterator i = environment.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            jvmArgs = jvmArgs + space + "-D" + (String) e.getKey() + "="
                    + (String) e.getValue();
            space = " ";
        }

        Map attributes;
        attributes = soft.getAttributes();
        if (attributes != null) {
            // Get more JVM arguments.
            for (Iterator i = attributes.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                if (key.equalsIgnoreCase("minMemory")) {
                    Integer minMem = (Integer) attributes.get(key);
                    if (minMem != null) {
                        jvmArgs = jvmArgs + space + "-Xms" + minMem.intValue()
                                + "M";
                        space = " ";
                    }
                } else if (key.equalsIgnoreCase("maxMemory")) {
                    Integer maxMem = (Integer) attributes.get(key);
                    if (maxMem != null) {
                        jvmArgs = jvmArgs + space + "-Xmx" + maxMem.intValue() + "M";
                        space = " ";
                    }
                } else if (key.equalsIgnoreCase("count")) {
                    // Number of instances to create. If not specified, 1.
                    Integer count = (Integer) attributes.get(key);
                    if (count != null) {
                        nInstances = count.intValue();
                        if (nInstances <= 0) {
                            throw new GATInvocationException("Illegal count");
                        }
                    }
                } else if (key.equalsIgnoreCase("hostCount")) {
                    // Number of hosts. If not specified, you get the number
                    // of instances.
                    Integer hostCount = (Integer) attributes.get(key);
                    if (hostCount != null) {
                        nNodes = hostCount.intValue();
                        if (nNodes <= 0) {
                            throw new GATInvocationException("Illegal hostCount");
                        }
                    }
                } else if (key.equalsIgnoreCase("classpath")) {
                    classPath = (String) attributes.get(key);
                }
            }
        }
        if (nNodes == -1) {
            nNodes = nInstances;
        }
    }

    /**
     * Returns the number of nodes required to run this job.
     * @return the number of nodes required.
     */
    int getNumNodes() {
        return nNodes;
    }

    private void preStage(NodeInfo node) throws GATInvocationException {
        setState(PRE_STAGING);

        if (wantsSandbox) {
            // Constructor also does pre-staging.
            sandbox = new Sandbox(gatContext, preferences, jobDescription,
                    nodes[0].hostName, null, true, false, false, false);
        } else {
            SoftwareDescription soft = jobDescription.getSoftwareDescription();
            Map preStageFiles;   // Map<File, File> (virtual file path, physical
                                 // file path)
            preStageFiles = soft.getPreStaged();
            if (preStageFiles != null && preStageFiles.size() != 0) {
                java.io.File[] srcFiles
                        = new java.io.File[preStageFiles.size()];
                java.io.File[] dstFiles
                        = new java.io.File[preStageFiles.size()];
                int index = 0;
                for (Iterator i = preStageFiles.entrySet().iterator();
                        i.hasNext();) {
                    Map.Entry e = (Map.Entry) i.next();
                    String key = ((File) e.getKey()).getPath();
                    srcFiles[index] = new java.io.File(key);
                    if (e.getValue() == null) {
                        dstFiles[index] = srcFiles[index];
                    } else {
                        String val = ((File) e.getValue()).getPath();
                        dstFiles[index] = new java.io.File(val);
                    }
                    index++;
                }
                try {
                    FileTransfer.pushFiles(node.node, srcFiles, dstFiles)
                            .waitForAll();
                } catch(Exception e) {
                    throw new GATInvocationException("preStage copy failed", e);
                }
            }
        }
    }

    /**
     * Called by the resource broker to run this job on the specified nodes.
     * @param nodes the nodes to run on.
     * @exception GATInvocationException is thrown when something goes wrong.
     */
    void startJob(NodeInfo[] nodes) throws GATInvocationException {
        // Check of number of nodes makes sense.
        if (nodes.length != nNodes || nNodes == 0) {
            setState(SUBMISSION_ERROR);
            infoMap.put("submissionError",
                new GATInvocationException("Wrong number of nodes allocated"));
            return;
        }

        this.nodes = nodes;

        // or prestage on all nodes ???
        preStage(nodes[0]);

        jobID = getNewID();

        setState(SCHEDULED);
        infoMap.put("hostname", nodes[0].hostName);

        // Launch jobs, asynchronously.
        StringWrapper[] result = new StringWrapper[nInstances];
        jobIDs = new String[nInstances];

        boolean failed = false;

        for (int i = 0; i < nInstances; i++) {
            NodeInfo node = nodes[i % nodes.length];
            jobIDs[i] = getNewID();
            node.watcher.addJob(jobIDs[i], this);
            try {
                result[i] = node.launcher.launch(className, jvmArgs, progArgs,
                        null, jobIDs[i], i);
            } catch(Exception e) {
                failed = true;
            }
            node.incrCount();
        }

        // Check launch results.
        for (int i = 0; i < nInstances; i++) {
            NodeInfo node = nodes[i % nodes.length];

            if (result[i] == null || result[i].stringValue() == null) {
                failed = true;
                break;
            }
        }

        if (failed) {
            for (int i = 0; i < nInstances; i++) {
                NodeInfo node = nodes[i % nodes.length];
                node.watcher.removeJob(jobIDs[i]);
                node.launcher.stopJob(jobIDs[i]);
                if (result[i] == null || result[i].stringValue() == null) {
                    node.decrCount(true);
                } else {
                    node.decrCount(false);
                }
            }
            setState(SUBMISSION_ERROR);
            infoMap.put("submissionError",
                new GATInvocationException("launcher failed"));
        } else {
            // Increment node 0 count for postStage.
            nodes[0].incrCount();
        }
    }

    /** Notifies that the job is actually running. */
    synchronized void setStarted() {
        if (! started) {
            infoMap.put("starttime", new Long(System.currentTimeMillis()));
            setState(RUNNING);
            started = true;
        }
    }

    /**
     * Notifies a state change.
     * @param newState the new state.
     */
    private void setState(int newState) {
        MetricValue v = null;

        synchronized(this) {
            state = newState;
            v = new MetricValue(this, getStateString(state), statusMetric,
                    System.currentTimeMillis());
            if (state == STOPPED || state == SUBMISSION_ERROR) {
                jobID = null;
            }
        }
        GATEngine.fireMetric(this, v);
    }

    /**
     * This method is invoked by the GAT user when he/she wants this job
     * stopped.
     */
    public void stop() {
        if (state == RUNNING) {
            for (int i = 0; i < nInstances; i++) {
                NodeInfo node = nodes[i % nodes.length];
                try {
                    node.launcher.stopJob(jobIDs[i]);
                } catch(Exception e) {
                    // Ignored, continue with other nodes.
                    // Probably does not happen anyway, because the stopJob
                    // method is asynchronous and void, so there is no
                    // result.
                }
            }
        }
    }

    /**
     * This method is invoked by a JobWatcher when it detects that an
     * instance has finished. The exit status is supplied.
     * @param instanceNo the instance number that is finished.
     * @param exitStatus the exit status of the instance.
     */
    synchronized void finish(int instanceNo, int exitStatus) {
        if (exitStatus != 0 && this.exitStatus == 0) {
            this.exitStatus = exitStatus;
        }
        finished++;
        int nodeNo = instanceNo % nNodes;
        nodes[nodeNo].decrCount(false);
        if (finished == nInstances) {
            // All are done.
            postStage(nodes[0]);
            // or postStage on all nodes ???
        }
    }

    /**
     * Runs the post-staging phase from the specified node.
     * @param node the source node.
     * @exception GATInvocationException is thrown when something goes wrong.
     */
    private void postStage(NodeInfo node) {
        setState(POST_STAGING);
        if (sandbox != null) {
            sandbox.retrieveAndCleanup(this);
            setStopped();
            return;
        }

        SoftwareDescription soft = jobDescription.getSoftwareDescription();
        Map postStageFiles = soft.getPostStaged();

        if (postStageFiles != null && postStageFiles.size() != 0) {
            java.io.File[] srcFiles = new java.io.File[postStageFiles.size()];
            java.io.File[] dstFiles = new java.io.File[postStageFiles.size()];
            int index = 0;
            try {
                for (Iterator i = postStageFiles.entrySet().iterator();
                        i.hasNext();) {
                    Map.Entry e = (Map.Entry) i.next();
                    String key = ((File) e.getKey()).getPath();
                    srcFiles[index] = new java.io.File(key);
                    if (e.getValue() == null) {
                        dstFiles[index] = srcFiles[index];
                    } else {
                        String val = ((File) e.getValue()).getPath();
                        dstFiles[index] = new java.io.File(val);
                    }
                    index++;
                }
                final FileVector fileVector
                    = FileTransfer.pullFiles(node.node, srcFiles, dstFiles);
                // Spawn thread that waits for the filetransfer to complete
                // and sets the status accordingly.
                Thread t = new Thread("FileTransfer waiter") {
                    public void run() {
                        if (fileVector != null) {
                            fileVector.waitForAll();
                        }
                        setStopped();
                    }
                };
                t.setDaemon(true);
                t.start();
            } catch(Exception e) {
                postStageException
                        = new GATInvocationException("Failed postStage", e);
                setStopped();
            }
        } else {
            setStopped();
        }
    }

    /**
     * Notifies that the Job is done.
     */
    private synchronized void setStopped() {
        infoMap.remove("hostname");
        infoMap.put("stoptime", new Long(System.currentTimeMillis()));
        nodes[0].decrCount(false);
        finished();
        setState(STOPPED);
    }

    /**
     * Gat user entry point: obtains a map with some job information.
     * @return job information.
     */
    public synchronized Map getInfo() throws GATInvocationException {
        infoMap.put("state", getStateString(state));
        if (jobID != null) {
            infoMap.put("id", jobID);
        }
        if (postStageException != null) {
            infoMap.put("postStageError", postStageException);
        }
        return infoMap;
    }

    /**
     * Returns the exit status of the job.
     * Actually, it returns one of the exit statusses of the instances.
     * The first non-zero exit status is remembered.
     * @return the exit status.
     * @exception GATInvocationException is thrown if this method is called
     *   when it should not be.
     */
    public int getExitStatus() throws GATInvocationException {
        if (state != STOPPED) {
            throw new GATInvocationException("getExitStatus called when "
                    + "state != STOPPED");
        }
        return exitStatus;
    }

    /**
     * Obtains and returns a new job id.
     * @return the new job id.
     */
    private String getNewID() {
        String retval = "_" + jobCounter;
        jobCounter++;
        return retval;
    }

    /**
     * Returns the Job identification.
     * @return the job identification.
     * @exception GATInvocationException is thrown if this method is called
     *   when it should not be.
     */
    public synchronized String getJobID() throws GATInvocationException {
        if (jobID != null) {
            return jobID;
        }
        throw new GATInvocationException("getJobID called in state "
                + getStateString(state));
    }

    /**
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        // FIXME: Auto-generated method stub
        return null;
    }
}
