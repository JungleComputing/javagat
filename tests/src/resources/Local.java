package resources;

import java.util.Hashtable;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class Local {
    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();

        File stdout = GAT.createFile(context, new URI("file:///stdout"));
        File stderr = GAT.createFile(context, new URI("file:///stderr"));

        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/date");

        sd.setStdout(stdout);
        sd.setStderr(stderr);

        Hashtable<String, Object> ht = new Hashtable<String, Object>();
        ResourceDescription rd = new HardwareResourceDescription(ht);

        JobDescription jd = new JobDescription(sd, rd);

        ResourceBroker broker = GAT.createResourceBroker(context, new URI(
                "any://localhost"));

        Job job = broker.submitJob(jd);

        Job.JobState state = job.getState();

        while ((state != Job.JobState.STOPPED)
                && (state != Job.JobState.SUBMISSION_ERROR)) {
            try {
                System.out.println("Sleeping!");
                Thread.sleep(1000);
            } catch (Exception e) {
                // ignore
            }

            state = job.getState();
        }

        if (state == Job.JobState.SUBMISSION_ERROR) {
            System.out.println("ERROR");
        } else {
            System.out.println("OK");
        }
    }
}
