package tutorial20;

import java.net.URISyntaxException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitWithWrapper implements MetricListener {

    /**
     * @param args
     * @throws GATObjectCreationException
     * @throws GATInvocationException
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    public static void main(String[] args) {
        try {
            Preferences preferences = new Preferences();
            preferences.put("resourcebroker.adaptor.name", args[0]);
            SoftwareDescription sd = new SoftwareDescription();
            sd.setExecutable("/bin/sleep");
            sd.setArguments("10");
            sd.addPreStagedFile(GAT.createFile("largefile"));
            sd.addAttribute("wrapper.enable", "true");
            sd.addAttribute("wrapper.output", "true");
            sd.addAttribute("wrapper.prestage", "sequential");
            JobDescription jd = new JobDescription(sd);
            ResourceBroker broker = GAT.createResourceBroker(preferences,
                    new URI(args[1]));
            Job lastJob = null;
            SubmitWithWrapper listener = new SubmitWithWrapper();
            for (int i = 0; i < Integer.parseInt(args[2]); i++) {
                System.out.println("--- begin multi job " + i);
                broker.beginMultiJob();
                for (int j = 0; j < Integer.parseInt(args[3]); j++) {
                    System.out.println("    submit sub job " + j);
                    lastJob = broker.submitJob(jd, listener, "job.status");
                }
                System.out.println("--- end multi job " + i);
                broker.endMultiJob();
            }
            while (lastJob.getState() != Job.STOPPED
                    && lastJob.getState() != Job.SUBMISSION_ERROR) {
                System.out
                        .println("waiting for last job, its current state is: '"
                                + lastJob.getState() + "'");
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        GAT.end();
    }

    public void processMetricEvent(MetricEvent val) {
        System.out.println("job '" + val.getSource() + "' changed to '"
                + val.getValue() + "'");
    }

}
