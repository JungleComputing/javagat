/*
 * Created on May 19, 2004
 */
package resources;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
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
public class GlobusTest implements MetricListener {
    public static void main(String[] args) {
        try {
            new GlobusTest().start();
        } catch (Exception e) {
            System.err.println("error: " + e);
            e.printStackTrace();
        } finally {
            GAT.end();
        }
    }

    public synchronized void processMetricEvent(MetricValue val) {
        System.err.println("job status changed to: " + val);
        notifyAll();
    }

    public void start() throws Exception {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "Globus");
        prefs.put("File.adaptor.name", "GridFTP");
        prefs.put("ResourceBroker.jobmanagerContact",
            "fs0.das3.cs.vu.nl/jobmanager-sge");

        SoftwareDescription sd = new SoftwareDescription();
        sd.setLocation(new URI("/bin/sleep"));
        sd.setArguments(new String[] { "1000" });

        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(context, prefs);

        for (int i = 0; i < 25; i++) {
            Job job = broker.submitJob(jd);
            MetricDefinition md = job.getMetricDefinitionByName("job.status");
            Metric m = md.createMetric(null);
            job.addMetricListener(this, m);
            
            Thread.sleep(5000);
            job.stop(); // cancel the job
        }
    }
}
