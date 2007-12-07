package resources;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitJobGridSAM {
    
    private Logger logger = Logger.getLogger(SubmitJobGridSAM.class);
    
    private class GridSAMMetricListener implements MetricListener {

        public void processMetricEvent(MetricValue val) {
            logger.info("got process event, val=" + val);
        }
        
    }
    
    public void start(String[] args) throws Exception {
        GATContext context = new GATContext();
        
        Preferences prefs = new Preferences();
        System.getProperties().setProperty("user.name", "mwi300");
        
        prefs.put("ResourceBroker.adaptor.name", "GridSAM");
        prefs.put("File.adaptor.name", "Ssh, CommandlineSsh");
        prefs.put("ResourceBroker.jobmanagerContact", "https://localhost:18443/gridsam/services/gridsam");
        SoftwareDescription sd = new SoftwareDescription();
//        sd.setLocation("https://" + args[0] + "/gridsam/services/gridsam");
        
        
//        sd.setLocation("file:////home0/mwi300/sh/ec.sh");
        sd.setLocation("file:////bin/sleep");
        
        sd.setArguments(new String[] {"15"});
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("maxCPUTime", "60");
        attributes.put("maxMemory", "90");
//        attributes.put("stdout", "in/outputFile");
        File outputFile = GAT.createFile(context, "in/outputFile");
//        sd.setStdout(outputFile);
        sd.setAttributes(attributes );

        File f = GAT.createFile(context, "/crypted_disk/home/wojciech/crypt/vu/RA/in");
        File f2 = GAT.createFile(context, "/etc/passwd");
        sd.addPreStagedFile(f);
        sd.addPreStagedFile(f2);

        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(context, prefs);
//        Job job = broker.submitJob(jd);
        Job job = broker.submitJob(jd, new GridSAMMetricListener(), "job.status");
        
        while (true) {
            int state = job.getState();
            if (logger.isDebugEnabled()) {
                logger.debug("state=" + state);
            }
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
