package org.gridlab.gat.resources.cpi.proactive;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
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
 * @see org.gridlab.gat.resources.Job
 */
@SuppressWarnings("serial")
public class ProActiveJob extends JobCpi {
    /** Counter for generating job identifications. */
    private static int jobCounter;

    /** For JavaGat monitoring. */
    private MetricDefinition statusMetricDefinition;

    /** For JavaGat monitoring. */
    private Metric statusMetric;

    /** Map for supplying the result of the getInfo() method. */
    private HashMap<String, Object> infoMap = new HashMap<String, Object>();

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
    // unused --Rob
    // private ProActiveResourceBrokerAdaptor broker;
    /** Number of nodes on which to run. */
    private int nNodes = 1;

    /** Set if the number of nodes only indicates a maximum. */
    boolean softHostCount = false;

    /** When set, use the Sandbox mechanism for stage-in and state-out. */
    private boolean wantsSandbox = true;

    /** List of nodes on which the job instances are run. */
    private ArrayList<NodeInfo> nodes = new ArrayList<NodeInfo>();

    /** Identification of this Gatjob. */
    private String jobID = null;

    /** Set when the application is actually started. */
    private boolean started = false;

    /** Counts how many instances are finished. */
    private int finished = 0;

    /** Set when staging must be done on all nodes. */
    private boolean stageOnAll = false;

    /**
     * Node on which staging is done, in case <code>stageOnAll</code> is
     * false.
     */
    private NodeInfo stageNode = null;

    /** Maps instance ids to nodes. */
    private HashMap<String, NodeInfo> id2Node = new HashMap<String, NodeInfo>();

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

    /** Input handler started? */
    private boolean inputHandlerStarted = false;

    /** Must be set when the application needs standard input. */
    private boolean needsStdin = false;

    class LauncherThread implements Runnable {
        StringWrapper[] results;

        int i;

        NodeInfo node;

        String id;

        public LauncherThread(StringWrapper[] results, int i, NodeInfo node,
                String id) {
            this.results = results;
            this.i = i;
            this.node = node;
            this.id = id;
        }

        public void run() {
            try {
                synchronized (node) {
                    results[i] = node.launcher.launch(className, jvmArgs
                            + " -Dibis.pool.cluster=" + node.descriptor,
                            progArgs, classPath, id);
                }
            } catch (Exception e) {
                // Dealt with later: results[i] stays null.
            }
        }
    }

    /** Input provider thread for job. */
    class InputHandler extends Thread {
        private boolean done = false;

        private BufferedReader inputReader;

        ArrayList<String> messages = new ArrayList<String>();

        NodeInfo[] currentNodes;

        int lastSize = 0;

        InputHandler(BufferedReader reader) {
            inputReader = reader;
            setName("ProActive Input Handler");
            setDaemon(true);
        }

        private String getInput() {
            if (inputReader == null) {
                done = true;
                return null;
            }
            String s = null;
            if (!done) {
                try {
                    s = inputReader.readLine();
                } catch (Exception e) {
                    done = true;
                }
            }
            return s;
        }

        synchronized void doStop() {
            done = true;
            try {
                inputReader.close();
            } catch (Exception e) {
                // ignored
            }
        }

        private synchronized void addMessage(String input) {
            for (int i = 0; i < currentNodes.length; i++) {
                // TODO: make this multithreaded?
                NodeInfo node = currentNodes[i];
                synchronized (node) {
                    node.launcher.provideInput(node.getInstanceID(), input);
                }
            }
            messages.add(input);
        }

        public synchronized void addedNodes() {
            currentNodes = (NodeInfo[]) nodes.toArray(new NodeInfo[0]);
            for (int i = lastSize; i < currentNodes.length; i++) {
                NodeInfo node = currentNodes[i];
                for (int j = 0; j < messages.size(); j++) {
                    String m = (String) messages.get(j);
                    synchronized (node) {
                        node.launcher.provideInput(node.getInstanceID(), m);
                    }
                }
            }
            lastSize = currentNodes.length;
        }

        public void run() {
            // Save all input in case nodes are added.
            // boolean newInput = false; // not used --Rob
            for (;;) {
                String input = getInput();
                synchronized (this) {
                    if (input == null) {
                        done = true;
                    }
                    addMessage(input);
                    if (done) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Constructor. There is no Sandbox here, because at this point it is not
     * known on which node(s) the job will be run. The sandbox will be created
     * when the job is actually scheduled.
     * 
     * @param gatContext
     *                the GAT context.
     * @param jobDescription
     *                the job description.
     * @param broker
     *                the resource broker that initiated this job.
     * @exception GATInvocationException
     *                    when something goes wrong.
     */
    public ProActiveJob(GATContext gatContext, JobDescription jobDescription,
            ProActiveResourceBrokerAdaptor broker)
            throws GATInvocationException {

        // No sandbox, we don't know the node(s) yet.
        super(gatContext, jobDescription, null);
        // this.broker = broker;

        if (gatContext.getPreferences().get("proactive.nosandbox") != null) {
            wantsSandbox = false;
        }
        if (gatContext.getPreferences().get("proactive.stageonall") != null) {
            stageOnAll = true;
        }
        String s = (String) gatContext.getPreferences().get(
                "proactive.needs.stdin");
        if (s != null && !s.equals("")) {
            needsStdin = true;
        }

        SoftwareDescription soft = jobDescription.getSoftwareDescription();
        Map<File, File> preStageFiles = soft.getPreStaged();
        Map<File, File> postStageFiles = soft.getPostStaged();

        needsPreStage = preStageFiles != null && preStageFiles.size() != 0;
        needsPostStage = postStageFiles != null && postStageFiles.size() != 0;

        // Tell the engine that we provide job.status events
        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", JobState.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "JobState", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);

        // Set status and submission time.
        setState(JobState.INITIAL);
        infoMap.put("submissiontime", new Long(System.currentTimeMillis()));

        // Get everything we need from the job description.
        // TODO changes lines below
        URI executable = null;
        try {
            executable = new URI("");// soft.getLocation();
        } catch (URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String scheme = executable.getScheme();
        if (!"java".equalsIgnoreCase(scheme)) {
            throw new GATInvocationException(
                    "Executable should be \"java:<classname>\"");
        }

        className = executable.getSchemeSpecificPart();

        File stdout = soft.getStdout();
        if (stdout != null) {
            try {
                OutputStream o = new BufferedOutputStream(new FileOutputStream(
                        stdout.getPath()));
                myStdout = new PrintStream(o, true);
            } catch (Exception e) {
                throw new GATInvocationException("Could not create file "
                        + stdout, e);
            }
        }

        File stderr = soft.getStderr();
        if (stderr != null) {
            try {
                OutputStream o = new BufferedOutputStream(new FileOutputStream(
                        stderr.getPath()));
                myStderr = new PrintStream(o, true);
            } catch (Exception e) {
                throw new GATInvocationException("Could not create file "
                        + stderr, e);
            }
        }

        Reader reader = null;
        File stdin = soft.getStdin();
        if (stdin != null) {
            try {
                reader = new FileReader(stdin.getPath());
            } catch (Exception e) {
                throw new GATInvocationException("Could not open input file "
                        + stdin, e);
            }
        } else if (needsStdin) {
            reader = new InputStreamReader(System.in);
        }
        if (reader != null) {
            inputHandler = new InputHandler(new BufferedReader(reader));
        }

        Map<String, Object> environment;
        environment = soft.getEnvironment();
        if (environment == null) {
            environment = new HashMap<String, Object>();
        }

        // Get program arguments as a string.
        progArgs = "";
        String[] arguments;
        arguments = soft.getArguments();
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                progArgs = progArgs + arguments[i];
                if (i < arguments.length - 1) {
                    progArgs = progArgs + " ";
                }
            }
        }

        // Get JVM arguments.
        jvmArgs = "-server";
        for (Iterator<Map.Entry<String, Object>> i = environment.entrySet()
                .iterator(); i.hasNext();) {
            Map.Entry<String, Object> e = (Map.Entry<String, Object>) i.next();
            jvmArgs = jvmArgs + " -D" + (String) e.getKey() + "="
                    + (String) e.getValue();
        }

        Map<String, Object> attributes;
        attributes = soft.getAttributes();
        if (attributes != null) {
            // Get more JVM arguments.
            for (Iterator<String> i = attributes.keySet().iterator(); i
                    .hasNext();) {
                String key = (String) i.next();
                if (key.equalsIgnoreCase("memory.min")) {
                    Integer minMem = (Integer) attributes.get(key);
                    if (minMem != null) {
                        jvmArgs = jvmArgs + " -Xms" + minMem.intValue() + "M";
                    }
                } else if (key.equalsIgnoreCase("memory.max")) {
                    Integer maxMem = (Integer) attributes.get(key);
                    if (maxMem != null) {
                        jvmArgs = jvmArgs + " -Xmx" + maxMem.intValue() + "M";
                    }
                } else if (key.equalsIgnoreCase("host.count")) {
                    // Number of hosts. If not specified, you get the number
                    // of instances.
                    Integer hostCount = (Integer) attributes.get(key);
                    if (hostCount != null) {
                        nNodes = hostCount.intValue();
                        if (nNodes <= 0) {
                            throw new GATInvocationException(
                                    "Illegal host.count");
                        }
                    }
                } else if (key.equalsIgnoreCase("proactive.host.count.soft")) {
                    softHostCount = true;
                } else if (key.equalsIgnoreCase("proactive.classpath")) {
                    classPath = (String) attributes.get(key);
                }
            }
        }
    }

    synchronized void stdout(String out) {
        myStdout.print(out);
        myStdout.flush();
    }

    synchronized void stderr(String err) {
        myStderr.print(err);
        myStderr.flush();
    }

    /**
     * Returns the number of nodes still required to run this job.
     * 
     * @return the number of nodes required.
     */
    int getNumNodes() {
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
            sandbox = new Sandbox(gatContext, jobDescription, node.hostName,
                    null, true, false, false, false);
        } else {
            SoftwareDescription soft = jobDescription.getSoftwareDescription();
            Map<File, File> preStageFiles = soft.getPreStaged();
            if (preStageFiles != null && preStageFiles.size() != 0) {
                java.io.File[] srcFiles = new java.io.File[preStageFiles.size()];
                java.io.File[] dstFiles = new java.io.File[preStageFiles.size()];
                int index = 0;
                for (Iterator<Map.Entry<File, File>> i = preStageFiles
                        .entrySet().iterator(); i.hasNext();) {
                    Map.Entry<File, File> e = (Map.Entry<File, File>) i.next();
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
                } catch (Exception e) {
                    throw new GATInvocationException("preStage copy failed", e);
                }
            }
        }
    }

    /**
     * Sets the SUBMISSION_ERROR state for this job, with the exception that was
     * the cause of this.
     * 
     * @param e
     *                the exception.
     */
    void submissionError(GATInvocationException e) {
        setState(JobState.SUBMISSION_ERROR);
        infoMap.put("submissionError", e);
    }

    /**
     * Called by the resource broker to run this job on the specified nodes.
     * 
     * @param newNodes
     *                the nodes to start the job on.
     * @exception GATInvocationException
     *                    is thrown when something goes wrong.
     * @return the number of nodes on which launching succeeded.
     */
    void startJob(NodeInfo[] newNodes) throws GATInvocationException {
        // Check of number of nodes makes sense.
        if (nodes.size() + newNodes.length > nNodes) {
            submissionError(new GATInvocationException(
                    "Wrong number of nodes allocated"));
            return;
        }

        if (needsPreStage) {
            if (stageOnAll) {
                if (nodes.size() == 0) {
                    setState(JobState.PRE_STAGING);
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
            setState(JobState.SCHEDULED);
            infoMap.put("hostname", stageNode.hostName);
        }

        // Launch jobs, asynchronously.

        // boolean failed = false; // not used --Rob

        StringWrapper[] results = new StringWrapper[newNodes.length];

        int maxThreads = 1;
        String tmp = (String) gatContext.getPreferences().get(
                "proactive.launch.parallel");
        if (tmp != null) {
            maxThreads = Integer.parseInt(tmp);
        }
        Threader threader = Threader.createThreader(maxThreads);

        for (int i = 0; i < newNodes.length; i++) {
            final NodeInfo node = newNodes[i];
            final String id = getNewID();
            node.watcher.addJob(id, this);
            node.setID(jobID, id);
            threader.submit(new LauncherThread(results, i, node, id));
        }

        threader.waitForAll();

        if (softHostCount) {
            // Check launch results on this batch. When the hostCount is soft,
            // it does not really matter if a launch fails so the run
            // continues.
            for (int i = 0; i < newNodes.length; i++) {
                NodeInfo node = newNodes[i];
                String id = newNodes[i].getInstanceID();
                if (results[i].stringValue() == null) {
                    node.watcher.removeJob(id);
                    node.release(true);
                } else {
                    nodes.add(node);
                    id2Node.put(id, node);
                }
            }
            if (nodes.size() > 0 && inputHandler != null) {
                if (!inputHandlerStarted) {
                    inputHandlerStarted = true;
                    inputHandler.start();
                }
                inputHandler.addedNodes();
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
                    node.watcher.removeJob(id);
                    if (results[i] == null || results[i].stringValue() == null) {
                        node.release(true);
                    } else {
                        synchronized (node) {
                            node.launcher.stopJob(id);
                        }
                        node.release(false);
                    }
                }
            } else {
                for (int i = 0; i < newNodes.length; i++) {
                    id2Node.put(newNodes[i].getInstanceID(), newNodes[i]);
                    nodes.add(newNodes[i]);
                }
                if (inputHandler != null) {
                    if (!inputHandlerStarted) {
                        inputHandler.start();
                        inputHandlerStarted = true;
                    }
                    inputHandler.addedNodes();
                }
            }
        }
    }

    /** Notifies that the job is actually running. */
    synchronized void setStarted() {
        if (!started) {
            infoMap.put("starttime", new Long(System.currentTimeMillis()));
            setState(JobState.RUNNING);
            started = true;
        } else if (state == JobState.STOPPED) {
            stop();
        }
    }

    /**
     * Notifies a state change.
     * 
     * @param newState
     *                the new state.
     */
    private void setState(JobState newState) {
        MetricEvent v = null;

        synchronized (this) {
            state = newState;
            v = new MetricEvent(this, state, statusMetric, System
                    .currentTimeMillis());
            if (state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) {
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
            try {
                synchronized (node) {
                    node.launcher.stopJob(node.getInstanceID());
                }
            } catch (Exception e) {
                // Ignored, continue with other nodes.
                // Probably does not happen anyway, because the stopJob
                // method is asynchronous and void, so there is no
                // result.
            }
        }
    }

    /**
     * This method is invoked by a JobWatcher when it detects that an instance
     * has finished. The exit status is supplied.
     * 
     * @param id
     *                the instance id that is finished.
     * @param exitStatus
     *                the exit status of the instance.
     */
    synchronized void finish(String id, int exitStatus) {
        if (exitStatus != 0 && this.exitStatus == 0) {
            this.exitStatus = exitStatus;
        }
        NodeInfo node = (NodeInfo) id2Node.get(id);
        if (node != null) {
            finished++;
            if (state != JobState.STOPPED && needsPostStage && stageOnAll) {
                postStage(node);
            }
            node.release(false);
            if (finished == nodes.size()) {
                // All are done.
                if (state != JobState.STOPPED && needsPostStage && !stageOnAll) {
                    postStage(stageNode);
                }
                setStopped();
            }
        }
    }

    /**
     * Runs the post-staging phase from the specified node.
     * 
     * @param node
     *                the source node.
     * @exception GATInvocationException
     *                    is thrown when something goes wrong.
     */
    private void postStage(NodeInfo node) {
        setState(JobState.POST_STAGING);
        if (sandbox != null) {
            sandbox.retrieveAndCleanup(this);
            return;
        }

        SoftwareDescription soft = jobDescription.getSoftwareDescription();
        Map<File, File> postStageFiles = soft.getPostStaged();

        if (postStageFiles != null && postStageFiles.size() != 0) {
            java.io.File[] srcFiles = new java.io.File[postStageFiles.size()];
            java.io.File[] dstFiles = new java.io.File[postStageFiles.size()];
            int index = 0;
            try {
                for (Iterator<Map.Entry<File, File>> i = postStageFiles
                        .entrySet().iterator(); i.hasNext();) {
                    Map.Entry<File, File> e = (Map.Entry<File, File>) i.next();
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
                final FileVector fileVector = FileTransfer.pullFiles(node.node,
                        srcFiles, dstFiles);
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
            } catch (Exception e) {
                postStageException = new GATInvocationException(
                        "Failed postStage", e);
            }
        }
    }

    /**
     * Notifies that the Job is done. This method can be called several times
     * when dealing with "soft" host counts.
     */
    private synchronized void setStopped() {
        if (state != JobState.STOPPED) {
            infoMap.remove("hostname");
            infoMap.put("stoptime", new Long(System.currentTimeMillis()));
            finished();
            setState(JobState.STOPPED);
            if (inputHandler != null) {
                inputHandler.doStop();
            }
        }

    }

    /**
     * Gat user entry point: obtains a map with some job information.
     * 
     * @return job information.
     */
    public synchronized Map<String, Object> getInfo()
            throws GATInvocationException {
        infoMap.put("state", state);
        if (jobID != null) {
            infoMap.put("id", jobID);
        }
        if (postStageException != null) {
            infoMap.put("postStageError", postStageException);
        }
        return infoMap;
    }

    /**
     * Returns the exit status of the job. Actually, it returns one of the exit
     * statusses of the instances. The first non-zero exit status is remembered.
     * 
     * @return the exit status.
     * @exception GATInvocationException
     *                    is thrown if this method is called when it should not
     *                    be.
     */
    public int getExitStatus() throws GATInvocationException {
        if (state != JobState.STOPPED) {
            throw new GATInvocationException("getExitStatus called when "
                    + "state != STOPPED");
        }
        return exitStatus;
    }

    /**
     * Obtains and returns a new job id.
     * 
     * @return the new job id.
     */
    private String getNewID() {
        String retval = "_" + jobCounter;
        jobCounter++;
        return retval;
    }

}
