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
        System.getProperties().setProperty("user.name", "mwi300");
        
        prefs.put("ResourceBroker.adaptor.name", "GridSAM");
        prefs.put("File.adaptor.name", "Ssh");
        SoftwareDescription sd = new SoftwareDescription();
//        sd.setLocation("https://" + args[0] + "/gridsam/services/gridsam");
        
        
        sd.setLocation("file:////bin/sleep");
        sd.setArguments(new String[] {"5"});
        
        File f = GAT.createFile(context, "/crypted_disk/home/wojciech/crypt/vu/RA/input");
        sd.addPreStagedFile(f);

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
