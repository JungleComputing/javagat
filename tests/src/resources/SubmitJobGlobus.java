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
public class SubmitJobGlobus implements MetricListener {
    public static void main(String[] args) throws Exception {
        new SubmitJobGlobus().start(args);
    }

    public synchronized void processMetricEvent(MetricValue val) {
        notifyAll();
    }

    public void start(String[] args) throws Exception {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "Globus");
        prefs.put("ResourceBroker.jobmanagerContact", args[1]);

        File outFile = GAT.createFile(context, prefs,
            new URI("any:///out"));
        File errFile = GAT.createFile(context, prefs,
            new URI("any:///err"));

        SoftwareDescription sd = new SoftwareDescription();
        sd.setLocation(new URI(args[0]));
        sd.setStdout(outFile);
        sd.setStderr(errFile);
        if(args.length == 3) {
            sd.addAttribute("queue", args[2]);
        }

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
        
        GAT.end();
    }
}
