package tutorial20;

import org.gridlab.gat.GAT;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.Job.JobState;

public class SubmitRemoteJob {
    public static void main(String[] args) throws Exception {
        SoftwareDescription sd = new SoftwareDescription();
        // sd.setExecutable("/bin/hostname");
        sd.setExecutable("/bin/sleep");
        sd.setArguments("100");
        File stdout = GAT.createFile("hostname.txt");
        sd.setStdout(stdout);

        JobDescription jd = new JobDescription(sd);

        Preferences prefs = new Preferences();
        prefs.put("resourcebroker.adaptor.name", "globus");
        ResourceBroker broker = GAT.createResourceBroker(prefs,
                new URI(args[0]));
        Job job = broker.submitJob(jd);

        while ((job.getState() != JobState.STOPPED)
                && (job.getState() != JobState.SUBMISSION_ERROR)) {
            System.out.println(job.getState() + ": " + job.getJobID());
            Thread.sleep(1000);
        }
    }
}
