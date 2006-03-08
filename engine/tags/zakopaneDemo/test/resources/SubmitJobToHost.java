/*
 * Created on May 19, 2004
 */
package resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
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
public class SubmitJobToHost {
	public static void main(String[] args) {
		GATContext context = new GATContext();
		Preferences prefs = new Preferences();
		prefs.put("resources.adaptor.name", "globus");

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
					"file://fs0.das2.cs.vu.nl//home/rob/test1.dat"));
			sd.setStdout(stdout);

			File[] prestaged = new File[2];
			prestaged[0] = GAT.createFile(context, new URI(
					"file://fs0.das2.cs.vu.nl//bin/echo"));
			prestaged[1] = GAT.createFile(context, new URI(
					"file://fs0.das2.cs.vu.nl//bin/hostname"));
			sd.setPreStaged(prestaged);

		} catch (Exception e) {
			System.err.println("could not create files");
			System.exit(1);
		}

		Hashtable hardwareAttributes = new Hashtable();

		hardwareAttributes.put("machine.node", "litchi.zib.de");

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
			//			e.printStackTrace();
			System.exit(1);
		}

		while (true) {
			try {
				Map info = job.getInfo();
				System.err.print("job info: ");
				System.err.println(info);
				String state = (String) info.get("state");
				if (state.equals("STOPPED") || state.equals("SUBMISSION_ERROR"))
					break;
				Thread.sleep(10000);
			} catch (Exception e) {
				System.err.println("getInfo failed: " + e);
				//				e.printStackTrace();
				break;
			}
		}
	}
}