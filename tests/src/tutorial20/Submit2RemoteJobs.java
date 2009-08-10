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

public class Submit2RemoteJobs {
    public static void main(String[] args) throws Exception {
      
    	SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/hostname");
        File stdout = GAT.createFile("hostname.txt");
        sd.setStdout(stdout);
        
        Preferences preferences = new Preferences();
        preferences.put("resourcebroker.adaptor.name", "gt42"); // ""
        //preferences.put("file.adaptor.name", "gt4gridftp");
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(preferences, new URI(args[0]));
        Job job = broker.submitJob(jd);

        while ((job.getState() != JobState.STOPPED)
                && (job.getState() != JobState.SUBMISSION_ERROR)) {
            Thread.sleep(1000);
        }   

        System.out.println("\n\n " +"FIRST JOB TERMINATED \n\n");
        
        SoftwareDescription sd1 = new SoftwareDescription();
        sd1.setExecutable("/bin/hostname");
        File stdout1 = GAT.createFile("hostname1.txt");
        sd1.setStdout(stdout1);
        
        Preferences preferences1 = new Preferences();
        preferences1.put("resourcebroker.adaptor.name", "wsgt4new"); // ""wsgt4new
        //preferences.put("file.adaptor.name", "gt4gridftp");
        JobDescription jd1 = new JobDescription(sd1);
        ResourceBroker broker1 = GAT.createResourceBroker(preferences1, new URI(args[1]));
        Job job1 = broker1.submitJob(jd1);

        while ((job1.getState() != JobState.STOPPED)
                && (job1.getState() != JobState.SUBMISSION_ERROR)) {
            Thread.sleep(1000);
        }
    
   }
}