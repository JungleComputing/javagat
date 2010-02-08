package org.gridlab.gat.resources.cpi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.resources.WrapperJobDescription.ScheduledType;
import org.gridlab.gat.resources.WrapperJobDescription.WrappedJobInfo;

/**
 * A Wrapper can be started at a remote location, where it starts local jobs.
 * 
 * If you want to execute a job on a grid that processes on files, you add those
 * files as so called pre stage files to the software description of the job
 * description. The JavaGAT uses the job description to start up the job on the
 * grid and also takes care of copying the pre stage files to the grid. The pre
 * stage files will be located on the front-node, but are accessible from
 * worker-nodes (to which the job will be scheduled).
 * 
 * There might be some situations in which you might want the pre stage files on
 * the local node instead of the front node, for example: a. the performance of
 * the local disk is better than the front disk (nfs) b. the disk size of the
 * front node is too small c. pre staging can be done in parallel
 * 
 * JavaGAT supports the use of the local disk using a Wrapper. A Wrapper is in
 * fact a special job that is started using JavaGAT instead of the normal job.
 * The wrapper job is scheduled to a worker node and then executes. It gets the
 * pre stage files directly to the local node where it is scheduled and then
 * starts the normal job. The files will directly be copied from the submission
 * machine to the worker node.
 * 
 * Wrappers can also support multicore jobs. Multicore jobs operate in separate
 * sandboxes on the same node. If a node has multiple cores, those cores will be
 * used.
 * 
 * @author rkemp
 */

public class Wrapper {

    public static final String WRAPPER_SANDBOX_COPY_ENV_NAME = "WRAPPER_COMMON_SANDBOX";
    
    private static Logger logger = LoggerFactory.getLogger(Wrapper.class);

    private URI initiator;

    private ScheduledType scheduledType;

    private int numberJobs;

    private int wrapperId;

    private int jobsDone;
    
    private String triggerDirectory;
    
    private String sandboxCommon;
    
    private String sandboxPath;

    /**
     * Starts a wrapper with given arguments
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            new Wrapper().start(args);
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        } finally {
            GAT.end();
        }
    }

    @SuppressWarnings("unchecked")
    public void start(String[] args) throws Exception {
        
        String sandboxCommonTrigger;
        
        if (logger.isDebugEnabled()) {
            logger.debug("Starting JavaGAT Wrapper Application");
        }
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(
                "wrapper.info"));
        this.initiator = (URI) in.readObject();
        int level = in.readInt();
        wrapperId = in.readInt();
        sandboxCommon = (String) in.readObject();
        sandboxCommonTrigger = (String) in.readObject();
        triggerDirectory = (String) in.readObject();      
        scheduledType = (ScheduledType) in.readObject();       
        List<WrappedJobInfo> infos = (List<WrappedJobInfo>) in.readObject();
        in.close();
        
        if (logger.isDebugEnabled()) {
            logger.debug("original host:  " + initiator);
            logger.debug("debug level:    " + level);
            logger.debug("wrapperId:   " + wrapperId);
            logger.debug("triggerdir: " + triggerDirectory);
            logger.debug("staging type:   " + scheduledType);
            logger.debug("# wrapped jobs:" + infos.size());
            for (WrappedJobInfo info : infos) {
                logger.debug("  * " + info.getBrokerURI() + "\t"
                        + info.getJobStateFileName() + "\t" + info.getPreferences()
                        + "\t" + info.getJobDescription());
            }
        }
        
        this.numberJobs = infos.size();
        
        Preferences preferences = new Preferences();
        preferences.put("file.adaptor.name", "local");
        File sandbox = GAT.createFile(preferences, ".");
        sandboxPath = sandbox.getAbsolutePath();
        
        String triggerDirURI = rewriteURI(new URI(triggerDirectory), initiator).toString();
        if (infos.size() != 0 && sandboxCommon != null) {
            sandboxCommon = sandboxCommon + java.io.File.separator
                    + ".JavaGAT_SANDBOX_" + Math.random();
            java.io.File sandboxCopyFile = new java.io.File(sandboxCommon);
            if (! sandboxCopyFile.mkdirs()) {
                throw new Exception("Could not create extra sandbox directory " + sandboxCommon);
            }
            sandboxCommon = sandboxCopyFile.getPath();
            if ("true".equals(sandboxCommonTrigger)) {
                File file = null;
                try {
                    file = GAT.createFile(preferences, new URI(triggerDirURI + "/SandboxCopy." + 
                            + wrapperId));
                } catch (Throwable e) {
                    logger.warn("Could not wait for trigger", e);
                }

                if (file != null) {
                    waitForTrigger(file);
                }
            }
            sandbox.copy(new URI(sandboxCommon));
        }

        for (int i = 0; i < infos.size(); i++) {
            WrappedJobInfo info = infos.get(i);
            if (scheduledType == ScheduledType.COORDINATED) {
                SoftwareDescription sd 
                        = info.getJobDescription().getSoftwareDescription();
                sd.addAttribute("triggerDirectory", triggerDirURI);
            }
            new Submitter(info, i).start();
        }

        synchronized (this) {
            while (jobsDone < numberJobs) {
                if (logger.isDebugEnabled()) {
                    logger.debug("waiting for " + (numberJobs - jobsDone)
                        + " jobs");
                }
                wait();
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("DONE!");
        }
        if (sandboxCommon != null) {
            File sandboxCopyFile = GAT.createFile(preferences, sandboxCommon);
            sandboxCopyFile.recursivelyDeleteDirectory();
        }
    }
    
    void waitForTrigger(File file) {
        
        if (triggerDirectory == null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Waiting for trigger " + file.toGATURI());
        }

        long interval = 500;
        int  maxcount = 64;
        int count = 0;
        
        for (;;) {
            if (file.exists()) {
                break;
            }
            try {
                Thread.sleep(interval);
            } catch(InterruptedException e) {
                // ignored
            }
            count++;
            if (count == maxcount) {
                // back-off a bit.
                if (interval < 8000) {
                    maxcount += maxcount;
                    interval += interval;
                }
                count = 0;
            }
        }
        file.delete();
    }


    private AbstractJobDescription modify(Preferences prefs,
            AbstractJobDescription description, URI origin, int id) {
        if (!(description instanceof JobDescription)) {
            return description;
        }
        JobDescription jobDescription = (JobDescription) description;

        if (sandboxCommon != null) {
            Map<String, Object> env = jobDescription.getSoftwareDescription().getEnvironment();
            if (env == null) {
                env = new HashMap<String, Object>();
                jobDescription.getSoftwareDescription().setEnvironment(env);
                env = jobDescription.getSoftwareDescription().getEnvironment();
            }
            env.put(WRAPPER_SANDBOX_COPY_ENV_NAME, sandboxCommon);
        }
        
        Map<File, File> preStaged = jobDescription.getSoftwareDescription().getPreStaged();
        if (preStaged != null) {
            ArrayList<File> keys = new ArrayList<File>(preStaged.keySet());
            for (File file : keys) {
                if (file.toGATURI().refersToLocalHost()) {
                    File target = preStaged.get(file);
                    preStaged.remove(file);
                    try {
                        preStaged.put(GAT.createFile(prefs, rewriteURI(file
                                                .toGATURI(), origin)), target);
                    } catch (GATObjectCreationException e) {
                        logger.error("Got Exception", e);
                    }
                }

            }
        }
        
        Map<File, File> postStaged = jobDescription.getSoftwareDescription().getPostStaged();
        if (postStaged != null) {
            ArrayList<File> keys = new ArrayList<File>(postStaged.keySet());
            for (File file : keys) {
                File target = postStaged.get(file);
                if (target == null) {
                    try {
                        postStaged.put(file,
                                        GAT.createFile(prefs, origin + "/"
                                                + file.getName()));
                    } catch (GATObjectCreationException e) {
                        logger.error("Got Exception", e);
                    }
                } else if (target.toGATURI().refersToLocalHost()) {
                    postStaged.remove(file);
                    try {
                        postStaged.put(file,
                                        GAT.createFile(prefs, rewriteURI(target
                                                .toGATURI(), origin)));
                    } catch (GATObjectCreationException e) {
                        logger.error("Got Exception", e);
                    }
                }
            }
        }
        File stdout = jobDescription.getSoftwareDescription().getStdout();
        if (stdout != null) {
            try {
                File out = GAT.createFile(prefs, new URI(sandboxPath + "/.stdout_" + id));
                jobDescription.getSoftwareDescription().setStdout(out);
                postStaged.put(out,
                        GAT.createFile(prefs, rewriteURI(stdout.toGATURI(), origin)));
            } catch (Throwable e) {
                logger.error("Got Exception", e);
            }
        }
        File stderr = jobDescription.getSoftwareDescription().getStderr();
        if (stderr != null) {
            try {
                File err = GAT.createFile(prefs, new URI(sandboxPath + "/.stderr_" + id));
                jobDescription.getSoftwareDescription().setStderr(err);
                postStaged.put(err,
                        GAT.createFile(prefs, rewriteURI(stderr.toGATURI(), origin)));
            } catch (Throwable e) {
                logger.error("Got Exception", e);
            }
        }
        File stdin = jobDescription.getSoftwareDescription().getStdin();
        if (stdin != null) {
            try {
                File in = GAT.createFile(prefs, new URI(sandboxPath + "/.stdin_" + id));
                jobDescription.getSoftwareDescription().setStdin(in);
                preStaged.put(
                        GAT.createFile(prefs,
                                rewriteURI(stdin.toGATURI(), origin)), in);
            } catch (Throwable e) {
                logger.error("Got Exception", e);
            }
        }
        return description;
    }

    private URI rewriteURI(URI uri, URI origin) {
        try {
            if (uri.isAbsolute()) {
                String auth = uri.getAuthority();
                if (auth != null && ! "".equals(auth)) {
                    return uri;
                }
            }
            if (uri.hasAbsolutePath()) {
                uri = origin.setPath(uri.getPath());
            } else {
                uri = origin.setPath(origin.getPath() + "/" + uri.getPath());
            }
        } catch (URISyntaxException e) {
            logger.error("Got Exception", e);
        }
        return uri;
    }

    class Submitter extends Thread {

        private WrappedJobInfo info;
        private int wrappedId;
        
        public Submitter(WrappedJobInfo info, int wrappedId) {
            this.info = info;
            this.wrappedId = wrappedId;
            setDaemon(false);
            setName(info.getJobStateFileName());
        }

        @SuppressWarnings("null")
        public void run() {
            ResourceBroker broker = null;
            Preferences prefs = info.getPreferences();
            try {
                broker = GAT.createResourceBroker(prefs, info
                        .getBrokerURI());
            } catch (GATObjectCreationException e) {
                logger.error("Got Exception", e);
                System.exit(1);
            }

            try {
                broker.submitJob(modify(prefs, info.getJobDescription(), initiator, wrappedId),
                        new JobListener(info),
                        "job.status");
            } catch (GATInvocationException e) {
                logger.error("Got Exception", e);
                System.exit(1);
            }
        }
    }

    class JobListener implements MetricListener {

        private String filename;
        private Preferences prefs;

        public JobListener(WrappedJobInfo info) {
            this.filename = info.getJobStateFileName();  
            this.prefs = info.getPreferences();
        }

        public void processMetricEvent(MetricEvent event) {
            ObjectOutputStream out = null;
            try {
                // create a new file and write the state to it. This file is
                // copied
                // to the location of the submitter of the wrapper. It is
                // monitored
                // by the submitter application (WrapperJob), which will delete
                // the
                // file once the state is read. Therefore this method waits as
                // long
                // as the file exists, once the state is read, the new state can
                // be
                // written.
                URI dest = initiator.setPath(filename);
                java.io.File tmp = java.io.File.createTempFile(".JavaGAT",
                        "jobstate");
                tmp.createNewFile();
                out = new ObjectOutputStream(new java.io.FileOutputStream(tmp));
                out.writeObject(event.getValue());
                out.flush();
                out.close();
                File remoteFile = GAT.createFile(prefs, dest);
                File localFile = GAT.createFile(prefs, tmp.getPath());
                int count = 0;
                while (count < 30) {
                    synchronized(this.getClass()) {
                        if (! remoteFile.exists()) {
                            break;
                        }
                    }
                    count++;
                     try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
                synchronized(this.getClass()) {
                    localFile.copy(dest);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Created status file " + dest.getPath());
                }
                tmp.delete();
            } catch (GATObjectCreationException e) {
                logger.error("Got Exception", e);
            } catch (Throwable e) {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e1) {
                        logger.error("Got Exception", e1);
                    }
                }
                logger.error("Got Exception", e);
            }

            // if the metric indicates that a job has stopped, increment the
            // jobsDone.
            if (event.getValue() == JobState.STOPPED
                    || event.getValue() == JobState.SUBMISSION_ERROR) {
                synchronized (Wrapper.this) {
                    jobsDone++;
                    Wrapper.this.notifyAll();
                }
            }
        }
    }
}
