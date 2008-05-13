package tutorial;

import org.gridlab.gat.GAT;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitRemoteJob {
    public static void main(String[] args) throws Exception {
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/hostname");

        File stdout = GAT.createFile("hostname.txt");
        sd.setStdout(stdout);

        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(new URI("any://"
                + args[0]));
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
