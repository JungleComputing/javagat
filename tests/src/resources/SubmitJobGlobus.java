/*
 * Created on May 19, 2004
 */
package resources;

import java.net.URISyntaxException;

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
 * @author rob
 */
public class SubmitJobGlobus implements MetricListener {
    public static void main(String[] args) {
        try {
            new SubmitJobGlobus().start(args);
        } catch (Exception e) {
            System.err.println("error: " + e);
            e.printStackTrace();
        } finally {
            GAT.end();
        }
    }

    public synchronized void processMetricEvent(MetricValue val) {
        System.out.println("state: " + val.getValue());
        notifyAll();
    }

    public void start(String[] args) throws GATInvocationException,
            GATObjectCreationException, URISyntaxException {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "Globus");
        prefs.put("File.adaptor.name", "GridFTP");

        File outFile = GAT.createFile(context, prefs, new URI("any:///out"));
        File errFile = GAT.createFile(context, prefs, new URI("any:///err"));

        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable(args[1]);
        sd.setArguments(args[2].split(","));
        sd.setStdout(outFile);
        sd.setStderr(errFile);
        sd.addAttribute("globus.exitvalue.enable", args[3]);
        sd.addAttribute("host.count", args[4]);
        if (args.length == 6) {
            sd.addAttribute("globus.queue", args[5]);
        }

        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(context, prefs,
                new URI(args[0]));

        long start = System.currentTimeMillis();
        Job job = broker.submitJob(jd);
        MetricDefinition md = job.getMetricDefinitionByName("job.status");
        Metric m = md.createMetric(null);
        job.addMetricListener(this, m);

        while ((job.getState() != Job.STOPPED)
                && (job.getState() != Job.SUBMISSION_ERROR)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        System.err.println("job took " + (end - start) + " ms");
        System.err.println("job exit status: " + job.getExitStatus());

        System.err.println("SubmitJobCallback: Job finished, state = "
                + job.getInfo());

    }
}
