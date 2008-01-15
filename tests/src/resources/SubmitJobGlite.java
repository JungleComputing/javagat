/*
 * Created on July 31, 2007
 */
package resources;

import java.util.HashMap;
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
 * @author anna
 */
public class SubmitJobGlite implements MetricListener {

    public static void main(String[] args) {
        try {
            new SubmitJobGlite().start(args);
        } catch (Exception e) {
            System.err.println("error: " + e);
            e.printStackTrace();
        } finally {
            GAT.end();
        }
    }

    public synchronized void processMetricEvent(MetricValue val) {
        notifyAll();
    }

    public void start(String[] args) throws Exception {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "GLite");
        prefs.put("File.adaptor.name", "gridftp, local");
        String jobmanager;
        if (args.length > 1) {
            jobmanager = args[1];
        } else {
            jobmanager = "https://kop.nikhef.nl:7443/glite_wms_wmproxy_server";
        }

        prefs.put("ResourceBroker.jobmanagerContact", jobmanager);
        context.addPreferences(prefs);
        File outFile = GAT.createFile(context, prefs, new URI("std.out"));
        File errFile = GAT.createFile(context, prefs, new URI("std.err"));
        HashMap<String, Object> env = new HashMap<String, Object>();
        env.put("KEY_1", "value1");
        env.put("KEY_2", "value2");
        env.put("KEY_3", "value3");
        env.put("KEY_4", "value4");
        SoftwareDescription sd = new SoftwareDescription();
        sd.setEnvironment(env);
        sd.setExecutable(args[0]);
        sd.setStdout(outFile);
        sd.setStderr(errFile);
        sd.setVirtualOrganisation("pvier");
        sd.setArguments(new String[] { "*" });
        sd.addPreStagedFile(GAT.createFile(context, "test/stagein"), GAT
                .createFile(context, "test/stagein"));
        // sd.addPreStagedFile(GAT.createFile(context, "stagein2")); //,
        // GAT.createFile(context, "dir/stagein"));

        Hashtable<String, Object> hardwareAttributes = new Hashtable<String, Object>();

        ResourceDescription rd = new HardwareResourceDescription(
                hardwareAttributes);

        JobDescription jd = new JobDescription(sd, rd);
        ResourceBroker broker = GAT.createResourceBroker(context, prefs,
                new URI(jobmanager));

        Job job = broker.submitJob(jd);
        MetricDefinition md = job.getMetricDefinitionByName("job.status");
        Metric m = md.createMetric(null);
        job.addMetricListener(this, m);

        synchronized (this) {
            while ((job.getState() != Job.STOPPED)
                    && (job.getState() != Job.SUBMISSION_ERROR)) {
                System.out.println("job state: "
                        + Job.getStateString(job.getState()));
                Thread.sleep(5000);

            }
        }

        System.err.println("SubmitJobCallback: Job finished, state = "
                + job.getState());
    }

}
