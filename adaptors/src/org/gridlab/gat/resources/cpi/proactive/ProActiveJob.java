package org.gridlab.gat.resources.cpi.proactive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.filetransfer.FileTransfer;
import org.objectweb.proactive.filetransfer.FileVector;

public class ProActiveJob extends JobCpi {
    static int jobsAlive = 0;

    private MetricDefinition statusMetricDefinition;

    private Node node;

    private ProActiveLauncher launcher;

    private Metric statusMetric;

    private String jobID;

    private HashMap infoMap = new HashMap();

    private int lastState = -1;

    static ProActiveJobWatcher watcher = new ProActiveJobWatcher();

    public ProActiveJob(GATContext gatContext, Preferences preferences,
            ProActiveLauncher launcher, JobDescription jobDescription, Node node)
            throws GATInvocationException {
        super(gatContext, preferences, jobDescription, null);
        this.jobDescription = jobDescription;
        this.launcher = launcher;
        this.node = node;
        infoMap.put("submissiontime", new Long(System.currentTimeMillis()));
        infoMap.put("hostname", node.getNodeInformation().getHostName());
        state = SCHEDULED;
        jobsAlive++;

        // Tell the engine that we provide job.status events
        HashMap returnDef = new HashMap();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
            MetricDefinition.DISCRETE, "String", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetric = statusMetricDefinition.createMetric(null);

        SoftwareDescription soft = jobDescription.getSoftwareDescription();

        URI executable = soft.getLocation();
        String scheme = executable.getScheme();
        if (! "java".equalsIgnoreCase(scheme)) {
            throw new GATInvocationException(
                    "Executable should be \"java:<classname>\"");
        }

        String className = executable.getSchemeSpecificPart();

        if (soft.getStdin() != null
                || soft.getStdout() != null
                || soft.getStderr() != null) {
            throw new GATInvocationException(
                    "Redirection of standard input, output or error is not "
                    + "supported.");
        }

        Map environment;     // Map<String, String>
        environment = soft.getEnvironment();
        if (environment == null) {
            environment = new HashMap();
        }

        ArrayList args = new ArrayList();
        for (Iterator i = environment.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            args.add("-D" + (String) e.getKey() + "=" + (String) e.getValue());
        }

        String progArgs = "";
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

        Map attributes;      // Map<String, String>
        attributes = soft.getAttributes();
        if (attributes == null) {
            attributes = new HashMap();
        }

        // build jvmArgs.
        String jvmArgs = "";
        for (int i = 0; i < args.size(); i++) {
            jvmArgs = jvmArgs + (String) args.get(i);
            if (i < args.size()-1) {
                jvmArgs = jvmArgs + " ";
            }
        }

        // Get more JVM args from attributes???

        // preStage.
        state = PRE_STAGING;
        Map preStageFiles;   // Map<File, File> (virtual file path, physical
                             // file path)
        preStageFiles = soft.getPreStaged();
        if (preStageFiles != null && preStageFiles.size() != 0) {
            java.io.File[] srcFiles = new java.io.File[preStageFiles.size()];
            java.io.File[] dstFiles = new java.io.File[preStageFiles.size()];
            int index = 0;
            for (Iterator i = preStageFiles.entrySet().iterator(); i.hasNext();) {
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
                FileTransfer.pushFiles(node, srcFiles, dstFiles).waitForAll();
            } catch(Exception e) {
                throw new GATInvocationException("preStage copy failed", e);
            }
        }

        // launch
        jobID = launcher.launch(className, jvmArgs, progArgs, null, node)
                .stringValue();
        watcher.addJob(this);
        setState();
        if (state == RUNNING) {
            infoMap.put("starttime", new Long(System.currentTimeMillis()));
        }
    }

    private void setState() {
        if (state != STOPPED && state != POST_STAGING) {
            int jobState = launcher.getStatus(jobID).intValue();
            switch(jobState) {
            case ProActiveLauncher.RUNNING:
                state = RUNNING;
                break;
            case ProActiveLauncher.ERROR:
                state = SUBMISSION_ERROR;
                break;
            case ProActiveLauncher.FINISHED:
                state = POST_STAGING;
                break;
            }
        }
        doCallBack();
    }

    private void doCallBack() {
        MetricValue v = null;

        synchronized (this) {
            if (state == lastState) {
                // no need to do callback, no significant change
                return;
            }
            lastState = state;
            v = new MetricValue(this, getStateString(state), statusMetric,
                    System.currentTimeMillis());
        }

        GATEngine.fireMetric(this, v);

    }

    public void stop() throws GATInvocationException {
        setState();
        if (state == RUNNING) {
            launcher.stopJob(jobID);
        }
        infoMap.put("stoptime", new Long(System.currentTimeMillis()));
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

    synchronized void initiatePostStaging() {
        SoftwareDescription soft = jobDescription.getSoftwareDescription();
        Map postStageFiles = soft.getPostStaged();

        if (postStageFiles != null && postStageFiles.size() != 0) {
            java.io.File[] srcFiles = new java.io.File[postStageFiles.size()];
            java.io.File[] dstFiles = new java.io.File[postStageFiles.size()];
            int index = 0;
            try {
                for (Iterator i = postStageFiles.entrySet().iterator(); i.hasNext();) {
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
                    = FileTransfer.pullFiles(node, srcFiles, dstFiles);
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
                postStageException = new GATInvocationException("Failed postStage", e);
            }
        } else {
            setStopped();
        }
    }

    synchronized void setStopped() {
        infoMap.remove("hostname");
        infoMap.put("stoptime", new Long(System.currentTimeMillis()));
        state = STOPPED;
        jobsAlive--;
    }

    public synchronized Map getInfo() throws GATInvocationException {
        setState(); // update the state
        infoMap.put("state", getStateString(state));
        infoMap.put("id", jobID);
        if (postStageException != null) {
            infoMap.put("postStageError", postStageException);
        }
        return infoMap;
    }

    public int getExitStatus() throws GATInvocationException {
        if (state != STOPPED) {
            setState();
        }
        if (state != STOPPED) {
            throw new GATInvocationException("getExitStatus called when "
                    + "state != STOPPED");
        }
        return launcher.getExitStatus(jobID).intValue();
    }

    public String getJobID() {
        return jobID;
    }
}
