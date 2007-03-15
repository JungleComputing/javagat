/*
 * Created on May 19, 2004
 */
package resources;

import java.util.Hashtable;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
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

/**
 * @author rob
 */
public class SubmitJobLocalDemo implements MetricListener {
    public static void main(String[] args) throws Exception {
        new SubmitJobLocalDemo().start(args);
    }

    public synchronized void processMetricEvent(MetricValue val) {
        notifyAll();
    }

    public void start(String[] args) throws Exception {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("adaptors.local", "true");

        File outFile = GAT.createFile(context, prefs, new URI(
            "file:///date.out"));
        File errFile = GAT.createFile(context, prefs, new URI(
            "file:///date.err"));
        File stageInFile = GAT.createFile(context, prefs, new URI(
            "file:////home/rob/testDir"));

        SoftwareDescription sd = new SoftwareDescription();
        sd.setLocation(new URI("file:////bin/date"));
        sd.setStdout(outFile);
        sd.setStderr(errFile);
        sd.addPreStagedFile(stageInFile);

        Hashtable hardwareAttributes = new Hashtable();
        ResourceDescription rd = new HardwareResourceDescription(
            hardwareAttributes);

        JobDescription jd = new JobDescription(sd, rd);
        ResourceBroker broker = GAT.createResourceBroker(context, prefs);

        Job job = broker.submitJob(jd);
        MetricDefinition md = job.getMetricDefinitionByName("job.status");
        Metric m = md.createMetric(null);
        job.addMetricListener(this, m);

        synchronized (this) {
            while ((job.getState() != Job.STOPPED)
                && (job.getState() != Job.SUBMISSION_ERROR)) {
                wait();
            }
        }

        System.err.println("SubmitJobCallback: Job finished, state = "
            + job.getInfo());
    }
}
