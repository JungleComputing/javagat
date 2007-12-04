package resources;

import java.util.HashMap;
import java.util.Map;

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
        prefs.put("File.adaptor.name", "Ssh,CommandlineSsh");
        SoftwareDescription sd = new SoftwareDescription();
//        sd.setLocation("https://" + args[0] + "/gridsam/services/gridsam");
        
        
        sd.setLocation("file:////home0/mwi300/sh/ec.sh");
        sd.setArguments(new String[] {"/etc/passwd", "/etc/passwd"});
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("stdout", "in/outputFile");
        sd.setAttributes(attributes );
        
        File f = GAT.createFile(context, "/crypted_disk/home/wojciech/crypt/vu/RA/in");
        File f2 = GAT.createFile(context, "/etc/passwd");
        File outputFile = GAT.createFile(context, "in/outputFile");
        File outputFileOut = GAT.createFile(context, "outputFile");
        sd.addPreStagedFile(f);
        sd.addPreStagedFile(f2);
        sd.addPostStagedFile(outputFile, outputFileOut);

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
