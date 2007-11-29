package resources;

import java.util.Hashtable;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitJobGridSAM {

    public static void main(String[] args) throws Exception {
        new SubmitJobGridSAM().start(args);
    }
    
    public void start(String args[]) throws Exception {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "Globus");
        
        SoftwareDescription sd = new SoftwareDescription();
        sd.setLocation("xxx");
        
        Hashtable<String, Object> hardwareAttributes = new Hashtable<String, Object>();

        ResourceDescription rd = new HardwareResourceDescription(
            hardwareAttributes);
        
        JobDescription jd = new JobDescription(sd, rd);
        
        ResourceBroker broker = GAT.createResourceBroker(context, prefs);
        Job job = broker.submitJob(jd);
    }
}
