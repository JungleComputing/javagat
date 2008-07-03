/*
 * Created on May 19, 2004
 */
package resources;

import java.io.InputStream;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author roelof
 * 
 * args[0] adaptor args[1] executable args[2] arguments (multiple args in the
 * form a,b,c) args[3] broker URI
 */
public class SubmitJobStreamingOut {
    public static void main(String[] args) throws Exception {

        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", args[0]);
        prefs.put("File.adaptor.name", "sshtrilead, local, gridftp");
        prefs.put("FileInputStream.adaptor.name", "sftp");

        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable(args[1]);
        sd.setArguments(args[2].split(","));
        // sd.setStdout(System.out);
        sd.enableStreamingStdout(true);
        sd.setStderr(GAT.createFile(context, "err"));

        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(context, prefs,
                new URI(args[3]));
        Job job = broker.submitJob(jd);
        InputStream in = job.getStdout();

        System.out.println("--- job submitted ---");

        while (true) {
            int i = in.read();
            if (i == -1) {
                break;
            } else {
                System.out.print((char) i);
            }
        }

        System.out.println("--- job done! ---");
        System.out.println("job state: " + job.getStateString(job.getState()));
        System.out.println("exit val:  " + job.getExitStatus());

        GAT.end();
    }
}
