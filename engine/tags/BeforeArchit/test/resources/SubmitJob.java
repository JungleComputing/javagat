/*
 * Created on May 19, 2004
 */
package resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author rob
 */
public class SubmitJob {
	public static void main(String[] args) {
		GATContext context = new GATContext();

		Hashtable softwareAttributes = new Hashtable();

		URI exe = null;
		try {
			exe = new URI("/bin/date");
		} catch (URISyntaxException e) {
			System.err.println("syntax error in URI");
			System.exit(1);
		}

		softwareAttributes.put("location", exe);
		SoftwareDescription sd = new SoftwareDescription(softwareAttributes);

		Hashtable hardwareAttributes = new Hashtable();

		ResourceDescription rd = new HardwareResourceDescription(
				hardwareAttributes);

		JobDescription j = null;
		ResourceBroker broker = null;

		try {
			j = GAT.createJobDescription(context, sd, rd);
			broker = GAT.createResourceBroker(context);
		} catch (Exception e) {
			System.err.println("Could not create Job description: " + e);
			System.exit(1);
		}

		Job job = null;

		try {
			job = broker.submitJob(j);
		} catch (Exception e) {
			System.err.println("submission failed: " + e);
			e.printStackTrace();
		}
	}
}