package tutorial;

import org.gridlab.gat.GAT;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitLocalJob {
    public static void main(String[] args) throws Exception {
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/sh");
        sd.setArguments("./myScript");
        sd.addPostStagedFile(GAT.createFile("test"), null);
        sd.addPreStagedFile(GAT.createFile("myScript"));

        Preferences preferences = new Preferences();
        preferences.put("resourcebroker.adaptor.name",
                "sshtrilead, commandlinessh");

        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = (args.length == 0) ? GAT.createResourceBroker(
                preferences, new URI("ssh://localhost")) : GAT
                .createResourceBroker(new URI("any://localhost"));
        Job job = broker.submitJob(jd);

        while ((job.getState() != Job.STOPPED)
                && (job.getState() != Job.SUBMISSION_ERROR)) {
            Thread.sleep(1000);
        }

        GAT.end();
        System.out.println("fine! job.getState(): "
                + job.getStateString(job.getState()));

    }
}
