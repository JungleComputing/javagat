package tutorial;

import java.util.Hashtable;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
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

    public synchronized void processMetricEvent(MetricValue val) {
        String state = (String) val.getValue();

        System.err.println("SubmitJobCallback: Processing metric: "
            + val.getMetric() + ", value is " + state);

        if (state.equals("STOPPED") || state.equals("SUBMISSION_ERROR")) {
            notifyAll();
        }
    }

    Job submitJob(GATContext context, String hostname) throws Exception {
        File outFile = GAT.createFile(context, "any:///hostname.txt");
        File stageInDir = GAT.createFile(context, "any:////home/rob/testDir");

        SoftwareDescription sd = new SoftwareDescription();
        sd.setLocation("any://" + hostname + "//bin/hostname");
        sd.setStdout(outFile);
        sd.addPreStagedFile(stageInDir);

        Hashtable attributes = new Hashtable();
        attributes.put("machine.node", hostname);

        ResourceDescription rd = new HardwareResourceDescription(attributes);

        JobDescription jd = new JobDescription(sd, rd);
        ResourceBroker broker = GAT.createResourceBroker(context);
        Job job = broker.submitJob(jd);

        return job;
    }

    public void start(String[] args) throws Exception {
        GATContext context = new GATContext();

        Job job = submitJob(context, args[0]);

        MetricDefinition md = job.getMetricDefinitionByName("job.status");
        Metric m = md.createMetric();
        job.addMetricListener(this, m); // register callback for job.status

        // wait until job is done
        synchronized (this) {
            while ((job.getState() != Job.STOPPED)
                    && (job.getState() != Job.SUBMISSION_ERROR)) {
                wait();
            }
        }
    }
}
