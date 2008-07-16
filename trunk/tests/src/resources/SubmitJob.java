/*
 * Created on May 19, 2004
 */
package resources;

import java.net.URISyntaxException;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author rob
 */
public class SubmitJob {
    public static void main(String[] args) {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "commandlineSsh");

        String exe = "/bin/date";
        URI out = null, err = null;

        File outFile = null;
        File errFile = null;

        try {
            out = new URI("any:///date.out");
            err = new URI("any:///date.err");
        } catch (URISyntaxException e) {
            System.err.println("syntax error in URI");
            System.exit(1);
        }

        try {
            outFile = GAT.createFile(context, prefs, out);
            errFile = GAT.createFile(context, prefs, err);
        } catch (GATObjectCreationException e) {
            System.err.println("error creating file: " + e);
            System.exit(1);
        }

        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable(exe);
        sd.setStdout(outFile);
        sd.setStderr(errFile);

        JobDescription jd = null;
        ResourceBroker broker = null;

        try {
            jd = new JobDescription(sd);
            broker = GAT.createResourceBroker(context, prefs, new URI(
                    "any://fs0.das2.cs.vu.nl"));
        } catch (Exception e) {
            System.err.println("Could not create Job description: " + e);
            System.exit(1);
        }

        Job job = null;

        try {
            job = broker.submitJob(jd);
        } catch (Exception e) {
            System.err.println("submission failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        while (true) {
            try {
                Map<String, Object> info = job.getInfo();
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
