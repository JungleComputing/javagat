package org.gridlab.gat.resources.cpi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.util.ThreadPool;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.FileWaiter;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
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
    
    private final SimpleDateFormat dateFormatter 
            = new SimpleDateFormat("yyyyMMdd-HHmmss");

    public static final String WRAPPER_COMMON_DIR = "WRAPPER_COMMON_DIR";
    
    public static final String WRAPPER_START_TIME = "WRAPPER_START_TIME";
    
    private static Logger logger = LoggerFactory.getLogger(Wrapper.class);

    private URI initiator;

    private ScheduledType scheduledType;

    private int numberJobs;

    private int wrapperId;

    private int jobsDone;
    
    private URI triggerDirectory;
    
    private String wrapperCommonSrc;
    
    private String wrapperCommonDest;
    
    private final String startTime = dateFormatter.format(new Date());

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
        
        String wrapperCommonTrigger;
        
        if (logger.isDebugEnabled()) {
            logger.debug("Starting JavaGAT Wrapper Application");
        }
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(
        	new FileInputStream("wrapper.info")));
        this.initiator = (URI) in.readObject();
        int level = in.readInt();
        wrapperId = in.readInt();
        wrapperCommonSrc = (String) in.readObject();
        wrapperCommonDest = (String) in.readObject();
        wrapperCommonTrigger = (String) in.readObject();
        triggerDirectory = (URI) in.readObject();      
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
        preferences.put("file.directory.copy", "contents");
               
        if (infos.size() != 0 && wrapperCommonSrc != null && wrapperCommonDest != null) {            
            File wrapperCommonSrcFile = GAT.createFile(preferences, wrapperCommonSrc);
            wrapperCommonDest = wrapperCommonDest + java.io.File.separator
                    + ".JavaGAT_SANDBOX_" + Math.random();
            java.io.File wrapperCommonDestFile = new java.io.File(wrapperCommonDest);
            if (! wrapperCommonDestFile.mkdirs()) {
                throw new Exception("Could not create directory " + wrapperCommonDest);
            }
            wrapperCommonDest = wrapperCommonDestFile.getPath();
            if ("true".equals(wrapperCommonTrigger)) {
                File file = null;
                try {
                    file = GAT.createFile(infos.get(0).getPreferences(), triggerDirectory);
                    FileWaiter w = FileWaiter.createFileWaiter(file);
                    w.waitFor("WrapperCommonTrigger." + wrapperId);
                    file = GAT.createFile(infos.get(0).getPreferences(),
                	    triggerDirectory.setPath(triggerDirectory.getPath() + "/WrapperCommonTrigger." + wrapperId));
                    file.delete();
                } catch (Throwable e) {
                    logger.warn("Could not wait for trigger", e);
                }
            }
            wrapperCommonSrcFile.copy(new URI(wrapperCommonDest));
        } else {
            wrapperCommonDest = null;
        }

        for (int i = 0; i < infos.size(); i++) {
            WrappedJobInfo info = infos.get(i);
            if (scheduledType == ScheduledType.COORDINATED) {
                SoftwareDescription sd 
                        = info.getJobDescription().getSoftwareDescription();
                sd.addAttribute("triggerDirectory", triggerDirectory.toString());
            }
            ThreadPool.createNew(new Submitter(info, i), info.getJobStateFileName().getPath());
            if (i < infos.size() - 1) {
                // Sleep a bit, just to prevent huge simultaneous access to servers.
                try {
                    Thread.sleep(1000);
                } catch(Throwable e) {
                    // ignore-
                }
            }
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
        if (wrapperCommonDest != null) {
            File wrapperCommonDestFile = GAT.createFile(preferences, wrapperCommonDest);
            wrapperCommonDestFile.recursivelyDeleteDirectory();
        }
    }
    

    private AbstractJobDescription modify(Preferences prefs,
            AbstractJobDescription description, URI origin, int id) {
        if (!(description instanceof JobDescription)) {
            return description;
        }
        JobDescription jobDescription = (JobDescription) description;
        Map<String, Object> env = jobDescription.getSoftwareDescription().getEnvironment();
        if (env == null) {
            env = new HashMap<String, Object>();
            jobDescription.getSoftwareDescription().setEnvironment(env);
            env = jobDescription.getSoftwareDescription().getEnvironment();
        }
        env.put(WRAPPER_START_TIME, startTime);
        if (wrapperCommonDest != null) {            
            env.put(WRAPPER_COMMON_DIR, wrapperCommonDest);
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
        java.io.File sandbox = new java.io.File(".");
        String sandboxPath = sandbox.getAbsolutePath();
        File stdout = jobDescription.getSoftwareDescription().getStdout();
        String jobName = jobDescription.getSoftwareDescription().getStringAttribute(
                "job.name", null);
        if (stdout != null) {
            String outName = jobName == null ? (".stdout_" + id) : (jobName + ".stdout");
            try {
                File out = GAT.createFile(prefs, new URI(sandboxPath + "/" + outName));
                jobDescription.getSoftwareDescription().setStdout(out);
                postStaged.put(out,
                        GAT.createFile(prefs, rewriteURI(stdout.toGATURI(), origin)));
            } catch (Throwable e) {
                logger.error("Got Exception", e);
            }
        }
        File stderr = jobDescription.getSoftwareDescription().getStderr();
        if (stderr != null) {
            String errName = jobName == null ? (".stderr_" + id) : (jobName + ".stderr");
            try {
                File err = GAT.createFile(prefs, new URI(sandboxPath + "/" + errName));
                jobDescription.getSoftwareDescription().setStderr(err);
                postStaged.put(err,
                        GAT.createFile(prefs, rewriteURI(stderr.toGATURI(), origin)));
            } catch (Throwable e) {
                logger.error("Got Exception", e);
            }
        }
        File stdin = jobDescription.getSoftwareDescription().getStdin();
        if (stdin != null) {
            String inName = jobName == null ? (".stdin_" + id) : (jobName + ".stdin");
            try {
                File in = GAT.createFile(prefs, new URI(sandboxPath + "/" + inName));
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

    class Submitter implements Runnable {

        private WrappedJobInfo info;
        private int wrappedId;
        
        public Submitter(WrappedJobInfo info, int wrappedId) {
            this.info = info;
            this.wrappedId = wrappedId;
        }

        public void run() {
            ResourceBroker broker = null;
            Preferences prefs = info.getPreferences();
            try {
                broker = GAT.createResourceBroker(prefs, info.getBrokerURI());
            } catch (GATObjectCreationException e) {
        	System.err.println("Could not create resource broker to submit wrapped job " + wrapperId);
        	System.err.println("Its job description: " + info.getJobDescription());
                System.err.println("The exception: " + e);
                e.printStackTrace(System.err);
                // Could not create resource broker, so no job started.
                // Act like job is done, or wrapper will not terminate.
                synchronized(Wrapper.this) {
                    jobsDone++;
                    Wrapper.this.notifyAll();
                }
            }

            try {
                broker.submitJob(modify(prefs, info.getJobDescription(), initiator, wrappedId),
                        new JobListener(info),
                        "job.status");
            } catch (GATInvocationException e) {
        	System.err.println("Could not submit wrapped job " + wrapperId);
        	System.err.println("Its job description: " + info.getJobDescription());
                System.err.println("The exception: " + e);
                e.printStackTrace(System.err);
                // No job started, so act like it is done, otherwise wrapper will not terminate.
                synchronized(Wrapper.this) {
                    jobsDone++;
                    Wrapper.this.notifyAll();
                }
            }
        }
    }

    class JobListener implements MetricListener {

        private URI filename;
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
                URI dest = this.filename;
                java.io.File tmp = java.io.File.createTempFile(".JavaGAT",
                        "jobstate");
                tmp.createNewFile();
                out = new ObjectOutputStream(new BufferedOutputStream(
                	new java.io.FileOutputStream(tmp)));
                out.writeObject(((Job)(event.getSource())).getInfo());
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
                    logger.debug("Created status file " + dest + " for event " + event);
                }
                tmp.delete();
                // Wait until submitter has seen the state change before notifying wrapper.
                count = 0;
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
