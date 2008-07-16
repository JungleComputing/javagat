/*
 * Created on May 8, 2008
 */
package resources;

import org.gridlab.gat.GAT;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author roelof
 */
public class SubmitJobSshTrilead implements MetricListener {

    public static void main(String[] args) {
        try {
            new SubmitJobSshTrilead().start(args);
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
        String[] additionalPreferences = null;
        if (args.length == 4) {
            additionalPreferences = args[3].split(",");
        }
        Preferences preferences = new Preferences();
        preferences.put("ResourceBroker.adaptor.name", "sshtrilead");
        preferences.put("file.adaptor.name", "sshtrilead, local");
        if (additionalPreferences != null) {
            for (String additionalPreference : additionalPreferences) {
                preferences.put(additionalPreference.split("=")[0],
                        additionalPreference.split("=")[1]);
            }
        }
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable(args[1]);
        sd.setArguments(args[2]);
        sd.setStdout(GAT.createFile("stdout"));
        sd.addPreStagedFile(GAT.createFile("Sleep.class"));
        // sd.setStderr(GAT.createFile("stderr"));
        JobDescription jd = new JobDescription(sd);

        ResourceBroker broker = GAT.createResourceBroker(preferences, new URI(
                args[0]));

        Job job = broker.submitJob(jd, this, "job.status");

        while (job.getState() != Job.JobState.STOPPED
                && job.getState() != Job.JobState.SUBMISSION_ERROR) {
            Thread.sleep(2000);
        }

    }
}
