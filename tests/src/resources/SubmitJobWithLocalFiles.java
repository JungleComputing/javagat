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
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author rob
 */
public class SubmitJobWithLocalFiles implements MetricListener {
    public static void main(String[] args) {
        try {
            new SubmitJobWithLocalFiles().start(args);
        } catch (Exception e) {
            System.err.println("error: " + e);
            e.printStackTrace();
        } finally {
            GAT.end();
        }
    }

    public synchronized void processMetricEvent(MetricEvent val) {
        notifyAll();
    }

    public void start(String[] args) throws Exception {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "Globus");
        SoftwareDescription sd = new SoftwareDescription();

        File outFile = GAT.createFile(context, prefs, new URI("any:///out"));
        File errFile = GAT.createFile(context, prefs, new URI("any:///err"));
        sd.setStdout(outFile);
        sd.setStderr(errFile);
        sd.setExecutable("/bin/hostname");
        sd.addAttribute("wrapper.enable", "true");
        sd.addAttribute("wrapper.java.home", new URI(
                "/usr/local/sun-java/jdk1.5"));
        sd.addAttribute("sandbox.root", "/tmp");

        sd.addPreStagedFile(GAT.createFile(context, prefs, new URI(
                "any://fs0.das2.cs.vu.nl/testDir")));
        sd.addPostStagedFile(GAT.createFile(context, prefs, new URI(
                "any:////proc/cpuinfo")));

        Hashtable<String, Object> hardwareAttributes = new Hashtable<String, Object>();

        ResourceDescription rd = new HardwareResourceDescription(
                hardwareAttributes);

        JobDescription jd = new JobDescription(sd, rd);
        ResourceBroker broker = GAT.createResourceBroker(context, prefs,
                new URI(args[0]));

        Job job = broker.submitJob(jd);
        MetricDefinition md = job.getMetricDefinitionByName("job.status");
        Metric m = md.createMetric(null);
        job.addMetricListener(this, m);

        synchronized (this) {
            while ((job.getState() != Job.JobState.STOPPED)
                    && (job.getState() != Job.JobState.SUBMISSION_ERROR)) {
                wait();
            }
        }

        System.err.println("SubmitJobCallback: Job finished, state = "
                + job.getInfo());
    }
}
