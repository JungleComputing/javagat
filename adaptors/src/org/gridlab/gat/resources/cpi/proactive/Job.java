package org.gridlab.gat.resources.cpi.proactive;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;

import java.util.ArrayList;
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

    /** Number of nodes on which to run. */
    private int nNodes = 1;

    /** Set if the number of nodes only indicates a maximum. */
    boolean softHostCount = false;

    /** When set, use the Sandbox mechanism for stage-in and state-out. */
    private boolean wantsSandbox = true;

    /** List of nodes on which the job instances are run. */
    private ArrayList nodes = new ArrayList();

    /** Identification of this Gatjob. */
    private String jobID = null;

    /** Set when the application is actually started. */
    private boolean started = false;

    /** Counts how many instances are finished. */
    private int finished = 0;

    /** Set when staging must be done on all nodes. */
    private boolean stageOnAll = false;

    /** 
     * Node on which staging is done, in case <code>stageOnAll</code> is false.
     */
    private NodeInfo stageNode = null;

    /** Maps instance ids to nodes. */
    private HashMap id2Node = new HashMap();

    /** Set if preStage is needed. */
    private boolean needsPreStage;

    /** Set if postStage is needed. */
    private boolean needsPostStage;

    /** Stream for standard output stream of application. */
    private PrintStream myStdout = System.out;

    /** Stream for standard error stream of application. */
    private PrintStream myStderr = System.err;

    /** Handler for standard input. */
    private InputHandler inputHandler;

    /** Input provider thread for job. */
    class InputHandler extends Thread {
        private boolean done = false;
        private BufferedReader inputReader;

        InputHandler(BufferedReader reader) {
            inputReader = reader;
            setDaemon(true);
        }

        private String getInput() {
            String s = null;
            if (! done) {
                try {
                    s = inputReader.readLine();
                } catch(Exception e) {
                    done = true;
                }
            }
            return s;
        }

        synchronized void doStop() {
            done = true;
            try {
                inputReader.close();
            } catch(Exception e) {
                // ignored
            }
            notifyAll();
        }

        public void run() {
            for (;;) {
                String input = getInput();
                if (input == null) {
                    done = true;
                }
                for (int i = 0; i < nodes.size(); i++) {
                    NodeInfo node = (NodeInfo) nodes.get(i);
                    synchronized(node) {
                        if (jobID.equals(node.getJobID())) {
                            System.out.println("Providing input for node "
                                    + node.hostName + ": " + input);
                            node.launcher.provideInput(node.getInstanceID(),
                                    input);
                        }
                    }
                }
                synchronized(this) {
                    if (done) {
                        return;
                    }
                }
            }
        }
    }

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

        if (preferences.get("ResourceBroker.ProActive.noSandbox") != null) {
            wantsSandbox = false;
        }
        if (preferences.get("ResourceBroker.ProActive.stageOnAll") != null) {
            stageOnAll = true;
        }

        SoftwareDescription soft = jobDescription.getSoftwareDescription();
        Map preStageFiles = soft.getPreStaged();
        Map postStageFiles = soft.getPostStaged();

        needsPreStage = preStageFiles != null && preStageFiles.size() != 0;
        needsPostStage = postStageFiles != null && postStageFiles.size() != 0;

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
        URI executable = soft.getLocation();
        String scheme = executable.getScheme();
        if (! "java".equalsIgnoreCase(scheme)) {
            throw new GATInvocationException(
                    "Executable should be \"java:<classname>\"");
        }

        className = executable.getSchemeSpecificPart();
 
        File stdout = soft.getStdout();
        if (stdout != null) {
            try {
                OutputStream o = new BufferedOutputStream(
                        new FileOutputStream(stdout.getPath()));
                myStdout = new PrintStream(o, true);
            } catch(Exception e) {
                throw new GATInvocationException(
                        "Could not create file " + stdout, e);
            }
        }

        File stderr = soft.getStderr();
        if (stderr != null) {
            try {
                OutputStream o = new BufferedOutputStream(
                        new FileOutputStream(stderr.getPath()));
                myStderr = new PrintStream(o, true);
            } catch(Exception e) {
                throw new GATInvocationException(
                        "Could not create file " + stderr, e);
            }
        }

        Reader reader;
        File stdin = soft.getStdin();
        if (stdin != null) {
            try {
                reader = new FileReader(stdin.getPath());
            } catch(Exception e) {
                throw new GATInvocationException(
                        "Could not open input file " + stdin, e);
            }
        } else {
            reader = new InputStreamReader(System.in);
        }

        inputHandler = new InputHandler(new BufferedReader(reader));

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
        jvmArgs = "-server";
        for (Iterator i = environment.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            jvmArgs = jvmArgs + " -D" + (String) e.getKey() + "="
                    + (String) e.getValue();
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
                        jvmArgs = jvmArgs + " -Xms" + minMem.intValue()
                                + "M";
                    }
                } else if (key.equalsIgnoreCase("maxMemory")) {
                    Integer maxMem = (Integer) attributes.get(key);
                    if (maxMem != null) {
                        jvmArgs = jvmArgs + " -Xmx" + maxMem.intValue() + "M";
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
                } else if (key.equalsIgnoreCase("softHostCount")) {
                    softHostCount = true;
                } else if (key.equalsIgnoreCase("classpath")) {
                    classPath = (String) attributes.get(key);
                }
            }
        }
    }

    synchronized void stdout(String out) {
        myStdout.println(out);
    }

    synchronized void stderr(String err) {
        myStderr.println(err);
    }

    /**
     * Returns the number of nodes still required to run this job.
     * @return the number of nodes required.
     */
    synchronized int getNumNodes() {
        if (nNodes == nodes.size()) {
            return 0;
        }
        if (finished != 0) {
            return 0;
        }
        return nNodes - nodes.size();
    }

    private void preStage(NodeInfo node) throws GATInvocationException {
        if (wantsSandbox) {
            // Constructor also does pre-staging.
            sandbox = new Sandbox(gatContext, preferences, jobDescription,
                    node.hostName, null, true, false, false, false);
        } else {
            SoftwareDescription soft = jobDescription.getSoftwareDescription();
            Map preStageFiles = soft.getPreStaged();
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
     * Sets the SUBMISSION_ERROR state for this job, with the exception
     * that was the cause of this.
     * @param e the exception.
     */
    void submissionError(GATInvocationException e) {
        setState(SUBMISSION_ERROR);
        infoMap.put("submissionError", e);
    }

    /**
     * Called by the resource broker to run this job on the specified nodes.
     * @param newNodes the nodes to start the job on.
     * @exception GATInvocationException is thrown when something goes wrong.
     * @return the number of nodes on which launching succeeded.
     */
    void startJob(NodeInfo[] newNodes) throws GATInvocationException {
        // Check of number of nodes makes sense.
        if (nodes.size() + newNodes.length > nNodes) {
            submissionError(
                new GATInvocationException("Wrong number of nodes allocated"));
            return;
        }

        if (needsPreStage) {
            if (stageOnAll) {
                if (nodes.size() == 0) {
                    setState(PRE_STAGING);
                }
                for (int i = 0; i < newNodes.length; i++) {
                    preStage(newNodes[i]);
                }
            } else if (nodes.size() == 0) {
                preStage(newNodes[0]);
            }
        }

        if (nodes.size() == 0) {
            // New job.
            stageNode = newNodes[0];
            jobID = getNewID();
            setState(SCHEDULED);
            infoMap.put("hostname", stageNode.hostName);
        }

        // Launch jobs, asynchronously.

        boolean failed = false;

        int start = nodes.size();

        StringWrapper[] results = new StringWrapper[newNodes.length];

        for (int i = 0; i < newNodes.length; i++) {
            NodeInfo node = newNodes[i];
            String id = getNewID();
            node.watcher.addJob(id, this);
            try {
                synchronized(node) {
                    results[i] = node.launcher.launch(className,
                            jvmArgs + " -Dibis.pool.cluster=" + node.descriptor,
                            progArgs, classPath, id);
                }
            } catch(Exception e) {
                // Dealt with later: results[i] stays null.
            }
            nodes.add(node);
            node.setID(jobID, id);
        }

        if (softHostCount) {
            // Check launch results on this batch. When the hostCount is soft,
            // it does not really matter if a launch fails so the run
            // continues.
            for (int i = 0; i < newNodes.length; i++) {
                String id = newNodes[i].getInstanceID();
                NodeInfo node = (NodeInfo) nodes.get(start);
                if (results[i].stringValue() == null) {
                    nodes.remove(start);
                    node.watcher.removeJob(id);
                    node.release(true);
                } else {
                    start++;
                    id2Node.put(id, node);
                }
            }
            if (start > 0 && ! inputHandler.isAlive()) {
                inputHandler.start();
            }
        } else {
            boolean failure = false;

            for (int i = 0; i < newNodes.length; i++) {
                if (results[i] == null || results[i].stringValue() == null) {
                    failure = true;
                }
            }
            if (failure) {
                for (int i = 0; i < newNodes.length; i++) {
                    NodeInfo node = newNodes[i];
                    String id = node.getInstanceID();
                    nodes.remove(0);
                    node.watcher.removeJob(id);
                    if (results[i] == null
                            || results[i].stringValue() == null) {
                        node.release(true);
                    } else {
                        synchronized(node) {
                            node.launcher.stopJob(id);
                        }
                        node.release(false);
                    }
                }
            } else {
                if (! inputHandler.isAlive()) {
                    inputHandler.start();
                }
            }
        }
    }

    /** Notifies that the job is actually running. */
    synchronized void setStarted() {
        if (! started) {
            infoMap.put("starttime", new Long(System.currentTimeMillis()));
            setState(RUNNING);
            started = true;
        } else if (state == STOPPED) {
            stop();
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
        for (int i = 0; i < nodes.size(); i++) {
            NodeInfo node = (NodeInfo) nodes.get(i);
            if (jobID.equals(node.getJobID())) {
                try {
                    synchronized(node) {
                        node.launcher.stopJob(node.getInstanceID());
                    }
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
     * @param id the instance id that is finished.
     * @param exitStatus the exit status of the instance.
     */
    synchronized void finish(String id, int exitStatus) {
        if (exitStatus != 0 && this.exitStatus == 0) {
            this.exitStatus = exitStatus;
        }
        NodeInfo node = (NodeInfo) id2Node.get(id);
        if (node != null) {
            finished++;
            if (state != STOPPED && needsPostStage && stageOnAll) {
                postStage(node);
            }
            node.release(false);
            if (finished == nodes.size()) {
                // All are done.
                if (state != STOPPED && needsPostStage && ! stageOnAll) {
                    postStage(stageNode);
                }
                setStopped();
            }
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
                    }
                };
                t.setDaemon(true);
                t.start();
            } catch(Exception e) {
                postStageException
                        = new GATInvocationException("Failed postStage", e);
            }
        }
    }

    /**
     * Notifies that the Job is done.
     * This method can be called several times when dealing with "soft"
     * host counts.
     */
    private synchronized void setStopped() {
        if (state != STOPPED) {
            infoMap.remove("hostname");
            infoMap.put("stoptime", new Long(System.currentTimeMillis()));
            finished();
            setState(STOPPED);
            inputHandler.doStop();
        }

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
