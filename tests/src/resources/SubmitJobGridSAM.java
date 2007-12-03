package resources;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitJobGridSAM {
    
    private Logger logger = Logger.getLogger(SubmitJobGridSAM.class);
    
    public void start(String[] args) throws Exception {
        GATContext context = new GATContext();
        
        Preferences prefs = new Preferences();
        
        prefs.put("ResourceBroker.adaptor.name", "GridSAM");
        SoftwareDescription sd = new SoftwareDescription();
//        sd.setLocation("https://" + args[0] + "/gridsam/services/gridsam");
        
        
        sd.setLocation("file:////bin/sleep");
        sd.setArguments(new String[] {"15"});
//        
//        File stdout = GAT.createFile(context, "hostname.txt");
//        sd.setStdout(stdout);
//        
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(context, prefs);
        Job job = broker.submitJob(jd);
        
        while (true) {
            int state = job.getState();
            if (state == Job.STOPPED) {
                logger.info("job done, exit code=" + job.getExitStatus());
                break;
            } else if (state == Job.SUBMISSION_ERROR) {
                logger.info("job error, breaking");
                break;
            }
            Thread.sleep(1000);           
        }
        
        GAT.end();
    }
    
    public static void main(String[] args) throws Exception {
        new SubmitJobGridSAM().start(args);
    }
}
