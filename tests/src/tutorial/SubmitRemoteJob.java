package tutorial;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitRemoteJob {
    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();

        SoftwareDescription sd = new SoftwareDescription();
        sd.setLocation("any://" + args[0] + "//bin/hostname");

        File stdout = GAT.createFile(context, "hostname.txt");
        sd.setStdout(stdout);

        ResourceDescription rd = new HardwareResourceDescription();
        rd.addResourceAttribute("machine.node", args[0]);

        JobDescription jd = new JobDescription(sd, rd);
        ResourceBroker broker = GAT.createResourceBroker(context);
        Job job = broker.submitJob(jd);

        while ((job.getState() != Job.STOPPED)
            && (job.getState() != Job.SUBMISSION_ERROR)) {
            System.err.println("job state = " + job.getInfo());
            Thread.sleep(1000);
        }

        System.err.println("job DONE, state = " + job.getInfo());
        GAT.end();
    }
}
