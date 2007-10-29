package tutorial;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitRemoteJob2 {

	public static void main(String[] args) throws Exception {
		GATContext context = new GATContext();
		if (args.length == 0) {
			args = new String[] { "globus" };
		}
		context.addPreference("ResourceBroker.adaptor.name", args[0]);
		context.addPreference("ResourceBroker.jobmanagerContact",
				"fs0.das3.cs.vu.nl/jobmanager-sge");
		context.addPreference("ResourceBroker.jobmanager", args[1]);
		context.addPreference("singleRemoteGAT", "true");
		context.addPreference("File.adaptor.name", "local, gridftp, !ssh,");

		SoftwareDescription sd1 = new SoftwareDescription();
		sd1.addAttribute("getRemoteSandboxOutput", "true");
		sd1.addAttribute("getRemoteSandboxOutputURI",
				"any://fs1.das3.liacs.nl/rsout");
		sd1.addAttribute("remoteGatLocation", "../.tempGAT");
		sd1.addAttribute("waitForPreStage", "true");
		sd1.setLocation("/bin/sh");
		sd1.setArguments(new String[] { "/home0/rkemp/script.sh" });
		sd1.addAttribute("useRemoteSandbox", "true");
		sd1.addAttribute("java.home", new URI("/usr/local/package/jdk1.5"));
		sd1.addPreStagedFile(GAT.createFile(context, "bigfile"));
		
		File stdout1 = GAT.createFile(context, "1");
		sd1.setStdout(stdout1);
		JobDescription jd1 = new JobDescription(sd1);

		SoftwareDescription sd2 = new SoftwareDescription();
		sd2.setLocation("/bin/sh");
		sd2.setArguments(new String[] { "/home0/rkemp/script.sh" });
		sd2.addAttribute("useRemoteSandbox", "true");
		sd2.addAttribute("waitForPreStage", "true");
		sd2.addAttribute("java.home", new URI("/usr/local/package/jdk1.5"));
		sd2.addPreStagedFile(GAT.createFile(context, "bigfile"));
		File stdout2 = GAT.createFile(context, "2");
		sd2.setStdout(stdout2);
		JobDescription jd2 = new JobDescription(sd2);

		SoftwareDescription sd3 = new SoftwareDescription();
		sd3.addAttribute("getRemoteSandboxOutput", "true");
		sd3.addAttribute("getRemoteSandboxOutputURI",
				"any://fs1.das3.liacs.nl/rsout");
		sd3.addAttribute("remoteGatLocation", "../.tempGAT");
		sd3.addAttribute("waitForPreStage", "true");
		sd3.setLocation("/bin/sh");
		sd3.setArguments(new String[] { "/home0/rkemp/script.sh" });
		sd3.addAttribute("useRemoteSandbox", "true");
		sd3.addAttribute("java.home", new URI("/usr/local/package/jdk1.5"));
		sd3.addPreStagedFile(GAT.createFile(context, "bigfile"));
		File stdout3 = GAT.createFile(context, "3");
		sd3.setStdout(stdout3);
		JobDescription jd3 = new JobDescription(sd3);

		ResourceBroker broker = GAT.createResourceBroker(context);
		broker.beginMultiJob();
		Job job1 = broker.submitJob(jd1);
		Job job2 = broker.submitJob(jd2);
		broker.endMultiJob();
		Job job3 = broker.submitJob(jd3);

		while ((job1.getState() != Job.STOPPED)
				&& (job1.getState() != Job.SUBMISSION_ERROR)) {
			Thread.sleep(1000);
		}
		while ((job2.getState() != Job.STOPPED)
				&& (job2.getState() != Job.SUBMISSION_ERROR)) {
			Thread.sleep(1000);
		}
		while ((job3.getState() != Job.STOPPED)
				&& (job3.getState() != Job.SUBMISSION_ERROR)) {
			Thread.sleep(1000);
		}
		GAT.end();
		System.out.println("jobs DONE!");
	}
}
