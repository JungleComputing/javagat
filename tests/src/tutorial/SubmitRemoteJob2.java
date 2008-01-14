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
        context.addPreference("singleRemoteGAT", "true");
        context.addPreference("File.adaptor.name", "local, gridftp, !ssh,");
        context.addPreference("FileOutputStream.adaptor.name", "!sftp");
        context.addPreference("FileInputStream.adaptor.name", "!sftp");
        context.addPreference("concurrentJobsPerNode", "2");

        SoftwareDescription sd1 = new SoftwareDescription();
        sd1.addAttribute("getWrapperOutput", "true");
        sd1.addAttribute("getWrapperOutputURI",
                "any://fs1.das3.liacs.nl/rsout");
        // sd1.addAttribute("remoteGatLocation", "../GAT");
        // sd1.addAttribute("waitForPreStage", "true");
        sd1.setExecutable("/bin/sh");
        sd1.setArguments(new String[] { "/home0/rkemp/script.sh" });
        sd1.addAttribute("useWrapper", "true");
        sd1.addAttribute("java.home", new URI("/usr/local/package/jdk1.5"));
        // sd1.addPreStagedFile(GAT.createFile(context, "bigfile"));
        File stdout1 = GAT.createFile(context, "1");
        sd1.setStdout(stdout1);
        JobDescription jd1 = new JobDescription(sd1);

        SoftwareDescription sd2 = new SoftwareDescription();
        sd2.setExecutable("/bin/sh");
        sd2.setArguments(new String[] { "/home0/rkemp/script.sh" });
        sd2.addAttribute("useWrapper", "true");
        // sd2.addAttribute("waitForPreStage", "true");
        sd2.addAttribute("java.home", new URI("/usr/local/package/jdk1.5"));
        // sd2.addPreStagedFile(GAT.createFile(context, "bigfile"));
        File stdout2 = GAT.createFile(context, "2");
        sd2.setStdout(stdout2);
        JobDescription jd2 = new JobDescription(sd2);
        
        SoftwareDescription sd3 = new SoftwareDescription();
        sd3.setExecutable("/bin/sh");
        sd3.setArguments(new String[] { "/home0/rkemp/script.sh" });
        sd3.addAttribute("useWrapper", "true");
        // sd2.addAttribute("waitForPreStage", "true");
        sd3.addAttribute("java.home", new URI("/usr/local/package/jdk1.5"));
        // sd2.addPreStagedFile(GAT.createFile(context, "bigfile"));
        File stdout3 = GAT.createFile(context, "3");
        sd3.setStdout(stdout3);
        JobDescription jd3 = new JobDescription(sd3);
        
        SoftwareDescription sd4 = new SoftwareDescription();
        sd4.setExecutable("/bin/sh");
        sd4.setArguments(new String[] { "/home0/rkemp/script.sh" });
        sd4.addAttribute("useWrapper", "true");
        // sd2.addAttribute("waitForPreStage", "true");
        sd4.addAttribute("java.home", new URI("/usr/local/package/jdk1.5"));
        // sd2.addPreStagedFile(GAT.createFile(context, "bigfile"));
        File stdout4 = GAT.createFile(context, "4");
        sd4.setStdout(stdout4);
        JobDescription jd4 = new JobDescription(sd4);        

        SoftwareDescription sd5 = new SoftwareDescription();
        sd5.addAttribute("getWrapperOutput", "true");
        sd5.addAttribute("getWrapperOutputURI",
                "any://fs1.das3.liacs.nl/rsout");
        // sd5.addAttribute("remoteGatLocation", "../GAT");
        // sd3.addAttribute("waitForPreStage", "true");
        sd5.setExecutable("/bin/sh");
        sd5.setArguments(new String[] { "/home0/rkemp/script.sh" });
        sd5.addAttribute("useWrapper", "true");
        sd5.addAttribute("java.home", new URI("/usr/local/package/jdk1.5"));
        // sd3.addPreStagedFile(GAT.createFile(context, "bigfile"));
        File stdout5 = GAT.createFile(context, "5");
        sd5.setStdout(stdout5);
        JobDescription jd5 = new JobDescription(sd5);

        ResourceBroker broker = GAT.createResourceBroker(context, new URI(
                "any://fs1.das3.liacs.nl/jobmanager-sge"));
        broker.beginMultiJob();
        Job job1 = broker.submitJob(jd1);
        Job job2 = broker.submitJob(jd2);
        Job job3 = broker.submitJob(jd3);
        Job job4 = broker.submitJob(jd4);
        broker.endMultiJob();
        Job job5 = broker.submitJob(jd5);

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
        while ((job4.getState() != Job.STOPPED)
                && (job4.getState() != Job.SUBMISSION_ERROR)) {
            Thread.sleep(1000);
        }
        while ((job5.getState() != Job.STOPPED)
                && (job5.getState() != Job.SUBMISSION_ERROR)) {
            Thread.sleep(1000);
        }
        GAT.end();
        System.out.println("jobs DONE!");
    }
}
