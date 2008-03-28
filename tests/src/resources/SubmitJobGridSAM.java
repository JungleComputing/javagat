package resources;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitJobGridSAM {
    
    private Logger logger = Logger.getLogger(SubmitJobGridSAM.class);
    
    private class GridSAMMetricListener implements MetricListener {

        public void processMetricEvent(MetricEvent val) {
            logger.info("got process event, val=" + val);
        }
        
    }
    
    public void start(String[] args) throws Exception {
        GATContext context = new GATContext();
        
        Preferences prefs = new Preferences();
        // System.getProperties().setProperty("user.name", "mwi300");
        
        prefs.put("ResourceBroker.adaptor.name", "GridSAM");

        prefs.put("ResourceBroker.sandbox.host", "localhost:4567");
//        prefs.put("File.adaptor.name", "commandlinessh");
//        prefs.put("File.adaptor.name", "ssh");
        SoftwareDescription sd = new SoftwareDescription();

        sd.setExecutable("/usr/bin/printenv");
        sd.setArguments(new String[] { } );
        
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("maxCPUTime", "60");
        attributes.put("maxMemory", "90");
        attributes.put("sandbox.root", "/tmp");
        
        Map<String, Object> env = new HashMap<String, Object>();
        env.put("ENV1", "env1val");
        env.put("GRIDSAMISCOOL", "no");
        env.put("ENV2", 12);
        attributes.put("environment", env);
        sd.setAttributes(attributes);
        sd.setStdout(GAT.createFile(context, prefs, new URI("printenv.out")));

        ResourceDescription rd = new HardwareResourceDescription();
        rd.addResourceAttribute("machine.node", "fs0.das3.cs.vu.nl");

        JobDescription jd = new JobDescription(sd, rd);
        ResourceBroker broker = GAT.createResourceBroker(context, prefs,
                new URI("https://localhost:18443/gridsam/services/gridsam"));
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
