package resources;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
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
        prefs.put("File.adaptor.name", "commandlinessh");
//        prefs.put("File.adaptor.name", "ssh");
        prefs.put("ResourceBroker.jobmanagerContact", "https://localhost:18443/gridsam/services/gridsam");
        SoftwareDescription sd = new SoftwareDescription();
//        sd.setLocation("https://" + args[0] + "/gridsam/services/gridsam");
        
        
        sd.setLocation("file:////home0/mwi300/sh/ec.sh");
//        sd.setLocation("file:////bin/sleep");
        
        sd.setArguments(new String[] {"3"});
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("maxCPUTime", "60");
        attributes.put("maxMemory", "90");
        attributes.put("sandboxRoot", "/tmp");
        
        Map<String, Object> env = new HashMap<String, Object>();
        env.put("ENV1", "env1val");
        env.put("GRIDSAMISCOOL", "no");
        env.put("ENV2", 12);
        attributes.put("environment", env);
//        attributes.put("stdout", "in/outputFile");
        File outputFile = GAT.createFile(context, new URI("any:///outputFile"));
        File stdin = GAT.createFile(context, new URI("any:///standardInput"));
//        File f3 = GAT.createFile(context, prefs, "/tmp/outputFile");
        sd.setStdout(outputFile);
        sd.setStdin(stdin);
        sd.setAttributes(attributes);

        File f = GAT.createFile(context, new URI("any:////crypted_disk/home/wojciech/crypt/vu/RA/inputFile"));
//        File f2 = GAT.createFile(context, prefs, "/etc/passwd");
        sd.addPreStagedFile(f);
//        sd.addPreStagedFile(f2);
//        sd.addPostStagedFile(outputFile);
        
        ResourceDescription rd = new HardwareResourceDescription();
        rd.addResourceAttribute("machine.node", "fsBogus.das3.cs.vu.nl");

        JobDescription jd = new JobDescription(sd, rd);
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
