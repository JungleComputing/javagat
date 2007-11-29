package resources;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitJobGridSAM {
    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();
        
        Preferences prefs = new Preferences();

        prefs.put("ResourceBroker.adaptor.name", "GridSAM");
        SoftwareDescription sd = new SoftwareDescription();
        sd.setLocation("file:////bin/hostname");

        File stdout = GAT.createFile(context, "hostname.txt");
        sd.setStdout(stdout);

        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(context, prefs);
        Job job = broker.submitJob(jd);

        while ((job.getState() != Job.STOPPED)
                && (job.getState() != Job.SUBMISSION_ERROR)) {
            Thread.sleep(1000);
        }

        GAT.end();
    }
}
