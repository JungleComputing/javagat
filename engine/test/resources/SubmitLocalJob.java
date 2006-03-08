/*
 * Created on May 19, 2004
 */
package resources;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

import java.net.URISyntaxException;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author rob
 */
public class SubmitLocalJob {
    public static void main(String[] args) {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "local");

        URI exe = null;

        try {
            exe = new URI("file:////bin/date");
        } catch (URISyntaxException e) {
            System.err.println("syntax error in URI");
            System.exit(1);
        }

        SoftwareDescription sd = new SoftwareDescription();
        sd.setLocation(exe);

        try {
            File stdout = GAT.createFile(context, new URI(
                "file:////home/rob/test1.dat"));
            sd.setStdout(stdout);

            File[] prestaged = new File[2];
            prestaged[0] = GAT
                .createFile(context, new URI("file:////bin/echo"));
            prestaged[1] = GAT.createFile(context, new URI(
                "file:////bin/hostname"));
            sd.setPreStaged(prestaged);
        } catch (Exception e) {
            System.err.println("could not create stdout file");
        }

        Hashtable hardwareAttributes = new Hashtable();

        ResourceDescription rd = new HardwareResourceDescription(
            hardwareAttributes);

        JobDescription jd = null;
        ResourceBroker broker = null;

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
        } catch (Exception e) {
            System.err.println("submission failed: " + e);
            System.exit(1);
        }

        while (true) {
            try {
                Map info = job.getInfo();
                System.err.print("job info: ");
                System.err.println(info);

                Exception pe = (Exception) info.get("postStageError");

                if (pe != null) {
                    pe.printStackTrace();
                }

                String state = (String) info.get("state");

                if (state.equals("STOPPED") || state.equals("SUBMISSION_ERROR")) {
                    break;
                }

                Thread.sleep(10000);
            } catch (Exception e) {
                System.err.println("getInfo failed: " + e);

                break;
            }
        }
    }
}
