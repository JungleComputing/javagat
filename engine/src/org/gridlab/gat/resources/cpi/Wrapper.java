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
    
    private static Logger logger = LoggerFactory.getLogger(Wrapper.class);

    private URI initiator;

    private ScheduledType scheduledType;

    private int numberJobs;

    private int wrapperId;

    private int jobsDone;
    
    private String triggerDirectory;
    
    private String sandboxCopy;

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
        if (logger.isDebugEnabled()) {
            logger.debug("Starting JavaGAT Wrapper Application");
        }
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(
                "wrapper.info"));
        this.initiator = (URI) in.readObject();
        int level = in.readInt();
        wrapperId = in.readInt();
        sandboxCopy = (String) in.readObject();
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
        
        if (sandboxCopy != null) {
            java.io.File sandboxCopyFile = new java.io.File(sandboxCopy);
            if (!sandboxCopyFile.exists()) {
                if (! sandboxCopyFile.mkdirs()) {
                    throw new Exception("Could not create sandbox.copy directory.");
                }
            } else {
                throw new Exception(
                        "sandbox.copy directory already exists!");
            }
            sandboxCopy = sandboxCopyFile.getPath();
            File sandbox = GAT.createFile(".");
            sandbox.copy(new URI(sandboxCopy));
        }

        String triggerDirURI = rewriteURI(new URI(triggerDirectory), initiator).toString();
        for (int i = 0; i < infos.size(); i++) {
            WrappedJobInfo info = infos.get(i);
            if (scheduledType == ScheduledType.COORDINATED) {
                SoftwareDescription sd 
                        = info.getJobDescription().getSoftwareDescription();
                sd.addAttribute("triggerDirectory", triggerDirURI);
            }
            new Submitter(info).start();
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
        if (sandboxCopy != null) {
            File sandboxCopyFile = GAT.createFile(sandboxCopy);
            sandboxCopyFile.recursivelyDeleteDirectory();
        }
    }

    private AbstractJobDescription modify(AbstractJobDescription description,
            URI origin) {
        if (!(description instanceof JobDescription)) {
            return description;
        }
        JobDescription jobDescription = (JobDescription) description;
        Map<File, File> preStaged = jobDescription.getSoftwareDescription().getPreStaged();
        
        Map<String, Object> env = jobDescription.getSoftwareDescription().getEnvironment();
        if (env == null) {
            env = new HashMap<String, Object>();
            jobDescription.getSoftwareDescription().setEnvironment(env);
            env = jobDescription.getSoftwareDescription().getEnvironment();
        }
        env.put("SANDBOX_COPY", sandboxCopy);
        if (preStaged != null) {
            ArrayList<File> keys = new ArrayList<File>(preStaged.keySet());
            for (File file : keys) {
                if (file.toGATURI().refersToLocalHost()) {
                    File target = preStaged.get(file);
                    preStaged.remove(file);
                    try {
                        preStaged.put(GAT.createFile(rewriteURI(file
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
                                        GAT.createFile(origin + "/"
                                                + file.getName()));
                    } catch (GATObjectCreationException e) {
                        logger.error("Got Exception", e);
                    }
                } else if (target.toGATURI().refersToLocalHost()) {
                    postStaged.remove(file);
                    try {
                        postStaged.put(file,
                                        GAT.createFile(rewriteURI(target
                                                .toGATURI(), origin)));
                    } catch (GATObjectCreationException e) {
                        logger.error("Got Exception", e);
                    }
                }
            }
        }
        if (jobDescription.getSoftwareDescription().getStdout() != null) {
            try {
                jobDescription.getSoftwareDescription().setStdout(
                        GAT.createFile(rewriteURI(jobDescription
                                .getSoftwareDescription().getStdout()
                                .toGATURI(), origin)));
            } catch (GATObjectCreationException e) {
                logger.error("Got Exception", e);
            }
        }
        if (jobDescription.getSoftwareDescription().getStderr() != null) {
            try {
                jobDescription.getSoftwareDescription().setStderr(
                        GAT.createFile(rewriteURI(jobDescription
                                .getSoftwareDescription().getStderr()
                                .toGATURI(), origin)));
            } catch (GATObjectCreationException e) {
                logger.error("Got Exception", e);
            }
        }
        if (jobDescription.getSoftwareDescription().getStdin() != null) {
            try {
                jobDescription.getSoftwareDescription().setStdin(
                        GAT.createFile(rewriteURI(
                                jobDescription.getSoftwareDescription()
                                        .getStdin().toGATURI(), origin)));
            } catch (GATObjectCreationException e) {
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
        
        public Submitter(WrappedJobInfo info) {
            this.info = info;
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
                broker.submitJob(modify(info.getJobDescription(), initiator),
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
                while (remoteFile.exists()) {
                    if (count > 30) {
                        // Something wrong with submitter???
                        // Just continue.
                        break;
                    }
                    count++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
                localFile.copy(dest);
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
