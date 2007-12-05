/*
 * Created on May 19, 2004
 */
package resources;

import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
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
public class SubmitZorillaJob {
	public static void main(String[] args) {
		GATContext context = new GATContext();
		Preferences prefs = new Preferences();
		prefs.put("ResourceBroker.adaptor.name", "zorilla,local");
		prefs.put("FileInputStream.adaptor.name", "local");
		prefs.put("FileOutputStream.adaptor.name", "local");	
                prefs.put("File.adaptor.name", "local");    
                
		URI exe = null, out = null, err = null;
                
//              in = null;

		File outFile = null;
		File errFile = null;
//		File inFile = null;

		try {
			exe = new URI("file:////bin/date");
			out = new URI("any:///date.out");
			err = new URI("any:///date.err");
//			in = new URI("any:///date.in");
		} catch (URISyntaxException e) {
			System.err.println("syntax error in URI");
			System.exit(1);
		}

		try {
			outFile = GAT.createFile(context, prefs, out);
			errFile = GAT.createFile(context, prefs, err);
//			inFile = GAT.createFile(context, prefs, in);
		} catch (GATObjectCreationException e) {
			System.err.println("error creating file: " + e);
			System.exit(1);
		}

		SoftwareDescription sd = new SoftwareDescription();
		sd.setLocation(exe);
		sd.setStdout(outFile);
		sd.setStderr(errFile);
//                sd.addPreStagedFile(inFile);
                
                String[] arguments = {"date.in"};
                sd.setArguments(arguments);

		Hashtable<String, Object> hardwareAttributes = new Hashtable<String, Object>();


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

		Job[] jobs = new Job[1];

		try {
			for(int i = 0; i < jobs.length; i++) {
				jobs[i] = broker.submitJob(jd);
			}
		} catch (Exception e) {
			System.err.println("submission failed: " + e);
			e.printStackTrace();
			System.exit(1);
		}

				for(Job job: jobs) {
		while (true) {
			try {
				Map<String, Object> info = job.getInfo();
				System.err.print("job info: ");
				System.err.println(info);

				String state = (String) info.get("state");

				if ((state == null) || state.equals("STOPPED")
						|| state.equals("SUBMISSION_ERROR")) {
					System.err.println(job + " done");
					break;
				}

				Thread.sleep(1000);
			} catch (Exception e) {
				System.err.println("getInfo failed: " + e);
				e.printStackTrace();

				break;
			}
		}
}
	}
}
