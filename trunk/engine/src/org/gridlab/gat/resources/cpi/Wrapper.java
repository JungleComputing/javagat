package org.gridlab.gat.resources.cpi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.resources.WrapperJobDescription.StagingType;
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

    private URI initiator;

    int jobsSubmitted = 0;

    int jobsDone = 0;

    int jobsPreStaging = 0;

    int jobsDonePreStaging = 0;

    int maxConcurrentJobs;

    StagingType stagingType;

    private int numberJobs;

    private int preStageIdentifier;

    private int numberPreStageJobs;

    private String preStageDoneDirectory;

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
        System.out.println("Starting JavaGAT Wrapper Application");
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(
                "wrapper.info"));
        this.initiator = (URI) in.readObject();
        System.out.println("wrapper application");
        System.out.println("original host:  " + initiator);
        int level = in.readInt();
        System.out.println("debug level:    " + level);

        preStageIdentifier = in.readInt();
        System.out.println("pre stage id:   " + preStageIdentifier);

        preStageDoneDirectory = (String) in.readObject();
        System.out.println("pre stage done dir: " + preStageDoneDirectory);

        numberPreStageJobs = in.readInt();
        System.out.println("#pre stage jobs:" + numberPreStageJobs);

        maxConcurrentJobs = in.readInt();
        System.out.println("max concurrent: " + maxConcurrentJobs);
        stagingType = (StagingType) in.readObject();
        System.out.println("staging type:   " + stagingType);
        List<WrappedJobInfo> infos = (List<WrappedJobInfo>) in.readObject();
        in.close();
        System.out.println("# wrapped jobs:" + infos.size());
        for (WrappedJobInfo info : infos) {
            System.out.println("  * " + info.getBrokerURI() + "\t"
                    + info.getJobStateFileName() + "\t" + info.getPreferences()
                    + "\t" + info.getJobDescription());
        }
        this.numberJobs = infos.size();
        if (preStageIdentifier > 0) {
            File prestageWaitFile = GAT.createFile(rewriteURI(new URI(
                    preStageDoneDirectory + "/" + preStageIdentifier),
                    initiator));
            while (!prestageWaitFile.exists()) {
                System.out.println("waiting for '" + prestageWaitFile
                        + "' to appear...");
                Thread.sleep(10000);
            }
        }
        for (WrappedJobInfo info : infos) {
            new Submitter(info).start();
        }
        synchronized (this) {
            while (jobsDone < numberJobs) {
                System.out.println("waiting for " + (numberJobs - jobsDone)
                        + " jobs");
                wait();
            }
        }
        System.out.println("DONE!");
        System.exit(0);
    }

    private AbstractJobDescription modify(AbstractJobDescription description,
            URI origin) {
        if (!(description instanceof JobDescription)) {
            return description;
        }
        JobDescription jobDescription = (JobDescription) description;
        if (jobDescription.getSoftwareDescription().getPreStaged() != null) {
            for (File file : jobDescription.getSoftwareDescription()
                    .getPreStaged().keySet()) {
                if (file.toGATURI().refersToLocalHost()) {
                    File target = jobDescription.getSoftwareDescription()
                            .getPreStaged().get(file);
                    if (target == null) {
                        target = file;
                    }
                    jobDescription.getSoftwareDescription().getPreStaged()
                            .remove(file);
                    try {
                        jobDescription.getSoftwareDescription().getPreStaged()
                                .put(
                                        GAT.createFile(rewriteURI(file
                                                .toGATURI(), origin)), target);
                    } catch (GATObjectCreationException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        if (jobDescription.getSoftwareDescription().getPostStaged() != null) {
            for (File file : jobDescription.getSoftwareDescription()
                    .getPostStaged().keySet()) {
                if (jobDescription.getSoftwareDescription().getPostStaged()
                        .get(file) == null) {
                    try {
                        jobDescription.getSoftwareDescription().getPreStaged()
                                .put(
                                        file,
                                        GAT.createFile(origin + "/"
                                                + file.getName()));
                    } catch (GATObjectCreationException e) {
                        e.printStackTrace();
                    }
                } else if (jobDescription.getSoftwareDescription()
                        .getPostStaged().get(file).toGATURI()
                        .refersToLocalHost()) {
                    File target = jobDescription.getSoftwareDescription()
                            .getPreStaged().get(file);
                    jobDescription.getSoftwareDescription().getPreStaged()
                            .remove(file);
                    try {
                        jobDescription.getSoftwareDescription().getPreStaged()
                                .put(
                                        file,
                                        GAT.createFile(rewriteURI(target
                                                .toGATURI(), origin)));
                    } catch (GATObjectCreationException e) {
                        e.printStackTrace();
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
                e.printStackTrace();
            }
        }
        if (jobDescription.getSoftwareDescription().getStderr() != null) {
            try {
                jobDescription.getSoftwareDescription().setStderr(
                        GAT.createFile(rewriteURI(jobDescription
                                .getSoftwareDescription().getStderr()
                                .toGATURI(), origin)));
            } catch (GATObjectCreationException e) {
                e.printStackTrace();
            }
        }
        if (jobDescription.getSoftwareDescription().getStdin() != null) {
            try {
                jobDescription.getSoftwareDescription().setStdin(
                        GAT.createFile(rewriteURI(
                                jobDescription.getSoftwareDescription()
                                        .getStdin().toGATURI(), origin)));
            } catch (GATObjectCreationException e) {
                e.printStackTrace();
            }
        }
        return description;
    }

    private URI rewriteURI(URI uri, URI origin) {
        try {
            if (uri.hasAbsolutePath()) {
                uri = origin.setPath(uri.getPath());
            } else {
                uri = origin.setPath(origin.getPath() + "/" + uri.getPath());
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
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

        public void run() {
            // if already max jobs running -> wait
            ResourceBroker broker = null;
            try {
                broker = GAT.createResourceBroker(info.getPreferences(), info
                        .getBrokerURI());
            } catch (GATObjectCreationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            synchronized (Wrapper.this) {
                while (jobsSubmitted - jobsDone == maxConcurrentJobs
                        || (jobsPreStaging - jobsDonePreStaging > 0 && stagingType == StagingType.SEQUENTIAL)) {
                    try {
                        Wrapper.this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                jobsSubmitted++;
                jobsPreStaging++;
                System.out.println("jobs running now: "
                        + (jobsSubmitted - jobsDone));
            }

            try {
                broker.submitJob(modify(info.getJobDescription(), initiator),
                        new JobListener(info.getJobStateFileName()),
                        "job.status");
            } catch (GATInvocationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    class JobListener implements MetricListener {

        private String filename;

        private JobState lastState = JobState.INITIAL;

        public JobListener(String filename) {
            this.filename = filename;
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
                File remoteFile = GAT.createFile(dest);
                File localFile = GAT.createFile(tmp.getPath());
                while (remoteFile.exists()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
                localFile.copy(dest);
                tmp.delete();
            } catch (GATObjectCreationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (GATInvocationException e) {
                e.printStackTrace();
            }
            // in case the previous state was pre staging and the current state
            // is something different (e.g. this job finished its pre staging).
            if (event.getValue() != JobState.PRE_STAGING
                    && lastState == JobState.PRE_STAGING) {
                synchronized (Wrapper.this) {
                    jobsDonePreStaging++;
                    Wrapper.this.notifyAll();
                }
                if (jobsDonePreStaging == numberPreStageJobs) {
                    try {
                        File preStageDoneFile = GAT
                                .createFile(rewriteURI(new URI(
                                        preStageDoneDirectory + "/"
                                                + (preStageIdentifier + 1)),
                                        initiator));
                        preStageDoneFile.createNewFile();
                    } catch (GATObjectCreationException e) {
                        System.err
                                .println("Done pre staging: failed to create file at '"
                                        + initiator
                                        + " ("
                                        + (preStageIdentifier + 1)
                                        + ") ': "
                                        + e);
                        e.printStackTrace();
                    } catch (IOException e) {
                        System.err
                                .println("Done pre staging: failed to create file at '"
                                        + initiator
                                        + " ("
                                        + (preStageIdentifier + 1)
                                        + ") ': "
                                        + e);
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        System.err
                                .println("Done pre staging: failed to create file at '"
                                        + initiator
                                        + " ("
                                        + (preStageIdentifier + 1)
                                        + ") ': "
                                        + e);
                        e.printStackTrace();
                    }
                }
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
            lastState = (JobState) event.getValue();
        }
    }
}
