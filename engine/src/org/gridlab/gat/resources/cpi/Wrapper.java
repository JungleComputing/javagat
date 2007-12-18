package org.gridlab.gat.resources.cpi;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

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

public class Wrapper implements MetricListener {
    protected static Logger logger = Logger.getLogger(Wrapper.class);

    boolean verbose = false;

    boolean debug = false;

    boolean timing = false;

    private String initiator;

    private int jobsStopped = 0;

    private Map<Job, String> jobMap = new HashMap<Job, String>();

    /**
     * Starts a wrapper with given arguments
     * 
     * @param args
     */
    public static void main(String[] args) {
        new Wrapper().start(args);
    }

    /**
     * Processes the incoming metrics of the jobs the wrapper submitted.
     * 
     * @param val
     *                The MetricValue received from the job
     */
    public synchronized void processMetricEvent(MetricValue val) {
        Job job = (Job) val.getSource();
        GATContext gatContext = new GATContext();
        FileWriter writer = null;
        try {
            // create a new file and write the state to it. This file is copied
            // to the location of the submitter of the wrapper. It is monitored
            // by the submitter application (WrapperJob), which will delete the
            // file once the state is read. Therefore this method waits as long
            // as the file exists, once the state is read, the new state can be
            // written.
            URI local = new URI(".JavaGATstatus" + jobMap.get(job));
            URI dest = new URI("any://" + initiator + "/.JavaGATstatus"
                    + jobMap.get(job));
            File localFile = GAT.createFile(gatContext, local);
            File remoteFile = GAT.createFile(gatContext, dest);
            localFile.createNewFile();
            writer = new FileWriter(localFile);
            writer.write(job.getState());
            writer.flush();
            writer.close();
            while (remoteFile.exists()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            localFile.copy(dest);
            localFile.delete();
        } catch (GATObjectCreationException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e);
            }
        } catch (IOException e) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(e1);
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug(e);
            }
        } catch (URISyntaxException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e);
            }
        } catch (GATInvocationException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e);
            }
        }
        // if the metric indicates that a job has stopped, increment the
        // jobsstopped.
        if (job.getState() == Job.STOPPED
                || job.getState() == Job.SUBMISSION_ERROR) {
            jobsStopped++;
        }
        notifyAll();
    }

    // returns the number of stopped jobs.
    private synchronized int getJobsStopped() {
        return jobsStopped;
    }

    private File rewriteStagedFile(GATContext gatContext,
            Preferences preferences, File origSrc, File origDest,
            String destHostname, String remoteCWD) {
        if (origSrc == null && origDest == null) {
            return null;
        }

        // leave remote files untouched
        if (origDest != null && origDest.toGATURI().getHost() != null
                && !origDest.toGATURI().getHost().equals("localhost")) {
            return origDest;
        }

        String newPath = null;
        if (origDest == null) {
            newPath = origSrc.getName();
        } else {
            newPath = origDest.toGATURI().getPath();

            // if we have a relative path without a hostname in the URI,
            // it means that the file is relative to CWD.
            if (origDest.toGATURI().getHost() == null && !origDest.isAbsolute()) {
                newPath = remoteCWD + "/" + newPath;
            }
        }

        String newLocation = "any://" + destHostname + "/" + newPath;

        File res = null;
        try {
            URI newURI = new URI(newLocation);

            if (logger.isInfoEnabled()) {
                logger.info("rewrite of " + newPath + " to " + newURI);
            }
            res = GAT.createFile(gatContext, preferences, newURI);
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("could not rewrite poststage file" + newPath + ":"
                        + e);
            }
            System.exit(1);
        }

        return res;
    }

    public synchronized void start(String[] args) {
        final String descriptorFile = args[0];
        final String initiator = args[1];
        final String remoteCWD = args[2];
        this.initiator = initiator;
        verbose = args[3].equalsIgnoreCase("true");
        debug = args[4].equalsIgnoreCase("true");
        timing = args[5].equalsIgnoreCase("true");
        String jobIDsString = args[6];
        if (verbose) {
            System.setProperty("gat.verbose", "true");
            logger.setLevel(Level.INFO);
        }
        if (debug) {
            System.setProperty("gat.debug", "true");
            logger.setLevel(Level.DEBUG);
        }
        if (timing) {
            System.setProperty("gat.timing", "true");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Wrapper started, initiator: " + initiator);
        }

        JobDescription[] descriptions = null;
        Preferences preferences = null;
        String[] preStageDoneLocations = null;
        try {
            if (logger.isInfoEnabled()) {
                logger.info("opening descriptor file: " + descriptorFile);
            }
            java.io.FileInputStream tmp = new java.io.FileInputStream(
                    descriptorFile);
            ObjectInputStream in = new ObjectInputStream(tmp);
            descriptions = (JobDescription[]) in.readObject();
            preferences = (Preferences) in.readObject();
            preferences.remove("ResourceBroker.jobmanagerContact");
            preStageDoneLocations = (String[]) in.readObject();
            in.close();
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("an error occurred: " + e);
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                logger.debug(e.toString());
            }
            System.exit(1);
        }
        String[] jobIDs = jobIDsString.split(",");

        int concurrentJobsPerNode = 0;
        String concurrentJobsPerNodeString = (String) preferences
                .get("concurrentJobsPerNode");
        if (concurrentJobsPerNodeString != null) {
            try {
                concurrentJobsPerNode = Integer
                        .parseInt(concurrentJobsPerNodeString);
            } catch (NumberFormatException n) {
                // not a number -> default value
            }
        }
        // default value
        if (concurrentJobsPerNode <= 0) {
            concurrentJobsPerNode = Runtime.getRuntime().availableProcessors();
        }
        int submitted = 0;
        while (submitted < descriptions.length) {
            GATContext gatContext = new GATContext();
            modifyJobDescription(descriptions[submitted], gatContext,
                    preferences, remoteCWD);
            ResourceBroker broker = null;
            try {
                broker = GAT.createResourceBroker(gatContext, preferences);
            } catch (GATObjectCreationException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("could not create broker: " + e);
                }
                System.exit(1);
            }
            if (descriptions[submitted].getSoftwareDescription()
                    .getBooleanAttribute("waitForPreStage", false)) {
                submitJob(broker, descriptions[submitted], gatContext,
                        preferences, jobIDs[submitted],
                        preStageDoneLocations[submitted]);
            } else {
                submitJob(broker, descriptions[submitted], jobIDs[submitted]);
            }
            submitted++;
            while (submitted - getJobsStopped() == concurrentJobsPerNode) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("an exception occurred: " + e);
                    }
                    System.exit(1);
                }
            }
        }
        while (getJobsStopped() != descriptions.length) {
            try {
                wait();
            } catch (InterruptedException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("an exception occurred: " + e);
                }
                System.exit(1);
            }
        }
        GAT.end();
        System.exit(0);
    }

    private void modifyJobDescription(JobDescription jd, GATContext gatContext,
            Preferences preferences, String remoteCWD) {
        SoftwareDescription sd = jd.getSoftwareDescription();
        sd.addAttribute("useWrapper", "false");
        preferences.put("ResourceBroker.adaptor.name", "local");

        // rewrite poststage files to go directly to their original
        // destination
        // also stdout and stderr
        sd.setStderr(rewriteStagedFile(gatContext, preferences, null, sd
                .getStderr(), initiator, remoteCWD));
        sd.setStdout(rewriteStagedFile(gatContext, preferences, null, sd
                .getStdout(), initiator, remoteCWD));
        sd.setStdin(rewriteStagedFile(gatContext, preferences, sd.getStdin(),
                null, initiator, remoteCWD));

        Map<File, File> pre = sd.getPreStaged();
        Set<File> tmp = pre.keySet();
        Object[] keys = tmp.toArray();
        File[] srcs = new File[keys.length];
        File[] dests = new File[keys.length];
        for (int i = 0; i < keys.length; i++) {
            File src = (File) keys[i];
            File dest = (File) pre.get(src);
            srcs[i] = rewriteStagedFile(gatContext, preferences, dest, src,
                    initiator, remoteCWD);
            dests[i] = dest;
        }
        pre.clear();
        for (int i = 0; i < keys.length; i++) {
            pre.put(srcs[i], dests[i]);
        }

        Map<File, File> post = sd.getPostStaged();
        tmp = post.keySet();
        keys = tmp.toArray();
        for (int i = 0; i < keys.length; i++) {
            File src = (File) keys[i];
            File dest = (File) post.get(src);
            dest = rewriteStagedFile(gatContext, preferences, src, dest,
                    initiator, remoteCWD);
            post.put(src, dest);
        }
    }

    private void submitJob(ResourceBroker broker, JobDescription jd,
            String jobID) {
        if (logger.isInfoEnabled()) {
            logger.info("not waiting for preStage");
        }
        Job job = null;
        try {
            job = broker.submitJob(jd);
        } catch (GATInvocationException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("an exception occurred: " + e);
            }
            System.exit(1);
        }
        jobMap.put(job, jobID);
        try {
            MetricDefinition md = job.getMetricDefinitionByName("job.status");
            Metric m = md.createMetric(null);
            job.addMetricListener(this, m);
        } catch (GATInvocationException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("an exception occurred: " + e);
            }
            System.exit(1);
        }
    }

    private void submitJob(ResourceBroker broker, JobDescription jd,
            GATContext gatContext, Preferences preferences, String jobID,
            String preStageDoneLocation) {
        Job job = null;
        try {
            File preStageDoneFile = GAT.createFile(gatContext, preferences,
                    "any://" + initiator + "/" + preStageDoneLocation);
            while (!preStageDoneFile.exists()) {
                Thread.sleep(500);
            }
            job = broker.submitJob(jd);
            jobMap.put(job, jobID);

            // wrapper is created and prestagedfiles are prestaged,
            // during
            // the submitJob method

            if (logger.isDebugEnabled()) {
                logger.debug("deleting prestageDoneFile at "
                        + preStageDoneLocation);
            }

            if (!preStageDoneFile.delete()) {
                if (logger.isInfoEnabled()) {
                    logger.info("could not delete preStageDone file");
                }
                System.exit(1);
            }
            if (logger.isInfoEnabled()) {
                logger.info("deleting prestageDoneFile at "
                        + preStageDoneLocation + " DONE");
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("an exception occurred: " + e);
            }
            System.exit(1);
        }
        try {
            MetricDefinition md = job.getMetricDefinitionByName("job.status");
            Metric m = md.createMetric(null);
            job.addMetricListener(this, m);
        } catch (GATInvocationException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("an exception occurred: " + e);
            }
            System.exit(1);
        }
    }
}
