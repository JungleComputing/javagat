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
        context.addPreference("ResourceBroker.adaptor.name", "globus");
        context.addPreference("ResourceBroker.jobmanagerContact", "fs0.das3.cs.vu.nl/jobmanager-sge");
        context.addPreference("singleRemoteGAT", "true");
        
        SoftwareDescription sd1 = new SoftwareDescription();
        sd1.addAttribute("getRemoteSandboxOutput", "true");
        sd1.addAttribute("getRemoteSandboxOutputURI", "any://fs0.das2.cs.vu.nl/GAT/output1");
        sd1.setLocation("/bin/sh");
        sd1.setArguments(new String[]{"/home0/rkemp/script.sh"});
        sd1.addAttribute("useLocalDisk", "true");
        sd1.addAttribute("java.home", new URI("/usr/local/package/jdk1.5"));
        File stdout1 = GAT.createFile(context, "1");
        sd1.setStdout(stdout1);
        JobDescription jd1 = new JobDescription(sd1);
        
        SoftwareDescription sd2 = new SoftwareDescription();
        sd2.setLocation("/bin/sh");
        sd2.setArguments(new String[]{"/home0/rkemp/script.sh"});
        sd2.addAttribute("useLocalDisk", "true");
        sd2.addAttribute("java.home", new URI("/usr/local/package/jdk1.5"));
        File stdout2 = GAT.createFile(context, "2");
        sd2.setStdout(stdout2);
        JobDescription jd2 = new JobDescription(sd2);
        
        SoftwareDescription sd3 = new SoftwareDescription();
        sd3.addAttribute("getRemoteSandboxOutput", "true");
        sd3.addAttribute("getRemoteSandboxOutputURI", "any://fs0.das2.cs.vu.nl/GAT/output1");
        sd3.setLocation("/bin/sh");
        sd3.setArguments(new String[]{"/home0/rkemp/script.sh"});
        sd3.addAttribute("useLocalDisk", "true");
        sd3.addAttribute("java.home", new URI("/usr/local/package/jdk1.5"));
        File stdout3 = GAT.createFile(context, "3");
        sd3.setStdout(stdout3);
        JobDescription jd3 = new JobDescription(sd3);
        

        ResourceBroker broker = GAT.createResourceBroker(context);
        broker.beginMultiCoreJob();
        broker.submitJob(jd1);
        broker.submitJob(jd2);
        Job job = broker.endMultiCoreJob();
        Job job3 = broker.submitJob(jd3);
        

        /*while ((job1.getState() != Job.STOPPED)
            && (job1.getState() != Job.SUBMISSION_ERROR)) {
            System.err.println("job state = " + job1.getInfo());
            Thread.sleep(10000);
        }
        while ((job2.getState() != Job.STOPPED)
                && (job2.getState() != Job.SUBMISSION_ERROR)) {
                System.err.println("job state = " + job2.getInfo());
                Thread.sleep(10000);
       }*/
       while ((job.getState() != Job.STOPPED)
                && (job.getState() != Job.SUBMISSION_ERROR)) {
                System.err.println("job state = " + job.getInfo());
                Thread.sleep(10000);
       }
       while ((job3.getState() != Job.STOPPED)
               && (job3.getState() != Job.SUBMISSION_ERROR)) {
               System.err.println("job state = " + job.getInfo());
               Thread.sleep(10000);
       }
       /*System.err.println("job DONE, state = " + job1.getInfo());
       System.err.println("job DONE, state = " + job2.getInfo());*/
       GAT.end();
    }
}
