/*
 * Created on May 19, 2004
 */
package resources;

import java.net.URISyntaxException;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author rob
 */
public class StopJobTest {
    public static void main(String[] args) {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();

        URI exe = null;

        try {
            exe = new URI("any://mars.cs.vu.nl//bin/sleep");
        } catch (URISyntaxException e) {
            System.err.println("syntax error in URI");
            System.exit(1);
        }

        SoftwareDescription sd = new SoftwareDescription();
        sd.setLocation(exe);
        sd.setArguments(new String[] {"100000"});

        JobDescription jd = null;
        ResourceBroker broker = null;

        ResourceDescription rd = new HardwareResourceDescription();
        rd.addResourceAttribute("machine.node", "mars.cs.vu.nl");

        try {
            jd = new JobDescription(sd, rd);
            broker = GAT.createResourceBroker(context, prefs);
        } catch (Exception e) {
            System.err.println("Could not create Job description: " + e);
            System.exit(1);
        }

        Job job = null;

        try {
            job = broker.submitJob(jd);

            Thread.sleep(1000);
            
            job.stop();
        } catch (Exception e) {
            System.err.println("submission failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        
        while (true) {
            try {
                Map info = job.getInfo();
                System.err.print("job info: ");
                System.err.println(info);

                String state = (String) info.get("state");

                if ((state == null) || state.equals("STOPPED")
                    || state.equals("SUBMISSION_ERROR")) {
                    break;
                }

                Thread.sleep(10000);
            } catch (Exception e) {
                System.err.println("getInfo failed: " + e);
                e.printStackTrace();

                break;
            }
        }
    }
}
