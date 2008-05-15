/*
 * Created on Aug 16, 2004
 */
package advert;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
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
        Preferences prefs = new Preferences();
        prefs.put("AdvertService.adaptor.name", "local");
        prefs.put("ResourceBroker.adaptor.name", "globus");
        prefs.put("jobs.killonexit", "false");

        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/hostname");

        File stdout = GAT.createFile("hostname.txt");
        sd.setStdout(stdout);

        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(prefs, new URI(
                "any://" + args[0]));
        Job job = broker.submitJob(jd);

        AdvertService a = GAT.createAdvertService(prefs);
        MetaData m = new MetaData();
        m.put("name", "testJob");
        a.add(job, m, "/testJob");

        GAT.end();
        System.exit(0);
    }
}
