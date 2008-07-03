/*
 * Created on May 19, 2004
 */
package resources;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author rob
 */
public class SubmitJobWSGT4new implements MetricListener {
    public static void main(String[] args) {
        try {
            new SubmitJobWSGT4new().start(args);
        } catch (Exception e) {
            System.err.println("error: " + e);
            e.printStackTrace();
        } finally {
            GAT.end();
        }
    }

    public synchronized void processMetricEvent(MetricEvent val) {
        System.out.println("state changed: "
                + ((Job) val.getSource())
                        .getStateString(((Job) val.getSource()).getState()));
        notifyAll();
    }

    public void start(String[] args) throws Exception {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "wsgt4new");
        // prefs.put("File.adaptor.name", "Local,GridFTP");
        prefs.put("wsgt4.factory.type", "SGE");

        File outFile = GAT.createFile(context, prefs, new URI("any:///out"));
        File errFile = GAT.createFile(context, prefs, new URI("any:///err"));

        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable(args[0]);
        sd.setStdout(outFile);
        sd.setStderr(errFile);
        sd.addAttribute("count", 10);

        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(context, prefs,
                new URI(args[1]));

        long start = System.currentTimeMillis();
        Job job = broker.submitJob(jd, this, "job.status");

        synchronized (this) {
            while ((job.getState() != Job.STOPPED)
                    && (job.getState() != Job.SUBMISSION_ERROR)) {
                wait();
            }
        }
        long end = System.currentTimeMillis();
        System.err.println("job took " + (end - start) + " ms");
        System.err.println("exit status: " + job.getExitStatus());
    }
}
