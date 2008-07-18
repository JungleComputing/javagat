package tutorial;

import org.gridlab.gat.GAT;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitJobCallback implements MetricListener {
    public static void main(String[] args) throws Exception {
        try {
            new SubmitJobCallback().start(args);
        } catch (Throwable t) {
            System.err.println("an error occurred: " + t);
            GAT.end();
            System.exit(1);
        }

        GAT.end();
        System.exit(0);
    }

    public synchronized void processMetricEvent(MetricEvent val) {
        String state = (String) val.getValue();

        System.err.println("SubmitJobCallback: Processing metric: "
                + val.getMetric() + ", value is " + state);

        if (state.equals("STOPPED") || state.equals("SUBMISSION_ERROR")) {
            notifyAll();
        }
    }

    Job submitJob(String hostname, String metricName) throws Exception {
        // create the references to the output file of this job and the
        // directory that should be pre staged.
        File outFile = GAT.createFile("any:///hostname.txt");
        File stageInDir = GAT.createFile("any:///testDir");

        // now construct the software description and the job description
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/hostname");
        sd.setStdout(outFile);
        sd.addPreStagedFile(stageInDir);
        JobDescription jd = new JobDescription(sd);

        // construct the broker and use this broker to submit the job
        ResourceBroker broker = GAT.createResourceBroker(new URI("any://"
                + hostname));
        Job job = broker.submitJob(jd, this, metricName);

        return job;
    }

    public void start(String[] args) throws Exception {
        Job job = submitJob(args[0], "job.status");

        // wait until job is done
        synchronized (this) {
            while ((job.getState() != Job.STOPPED)
                    && (job.getState() != Job.SUBMISSION_ERROR)) {
                wait();
            }
        }
    }
}
