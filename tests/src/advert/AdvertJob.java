/*
 * Created on Aug 16, 2004
 */
package advert;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author rob
 */
public class AdvertJob {
    public static void main(String[] args) throws Exception {
        GATContext c = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("AdvertService.adaptor.name", "local");
        prefs.put("ResourceBroker.adaptor.name", "globus");
        prefs.put("killJobsOnExit", "false");
        c.addPreferences(prefs);
        
        SoftwareDescription sd = new SoftwareDescription();
        sd.setLocation("any://" + args[0] + "//bin/hostname");

        File stdout = GAT.createFile(c, "hostname.txt");
        sd.setStdout(stdout);

        ResourceDescription rd = new HardwareResourceDescription();
        rd.addResourceAttribute("machine.node", args[0]);

        JobDescription jd = new JobDescription(sd, rd);
        ResourceBroker broker = GAT.createResourceBroker(c);
        Job job = broker.submitJob(jd);

        AdvertService a = GAT.createAdvertService(c);
        MetaData m = new MetaData();
        m.put("name", "testJob");
        a.add(job, m, "/rob/testJob");

        GAT.end();
        System.exit(0);
    }
}
