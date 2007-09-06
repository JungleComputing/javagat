package tutorial;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitLocalJob {
	public static void main(String[] args) throws Exception {
		GATContext context = new GATContext();

		// context.addPreference("ResourceBroker.adaptor.name", "local");
		SoftwareDescription sd = new SoftwareDescription();
		sd.setLocation("file:////bin/hostname");

		File stdout = GAT.createFile(context, "hostname.txt");
		sd.setStdout(stdout);

		JobDescription jd = new JobDescription(sd);
		ResourceBroker broker = GAT.createResourceBroker(context);
		Job job = broker.submitJob(jd);

		while ((job.getState() != Job.STOPPED)
				&& (job.getState() != Job.SUBMISSION_ERROR)) {
			Thread.sleep(1000);
		}

		GAT.end();
	}
}
