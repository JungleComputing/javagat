/*
 * Created on May 19, 2004
 */
package resources;

import java.net.URISyntaxException;
import java.util.Hashtable;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author rob
 */
public class SubmitJobCallback implements MetricListener {
	boolean exit = false;

	public static void main(String[] args) {
		new SubmitJobCallback().start(args);
	}

	public synchronized void ProcessMetricEvent(MetricValue val) {
		System.err.println("SubmitJobCallback: Processing metric: "
				+ val.getMetric() + ", value is " + val.getValue());

		String state = (String) val.getValue();

		if (state.equals("STOPPED") || state.equals("SUBMISSION_ERROR")) {
			exit = true;
			notifyAll();
		}
	}

	public void start(String[] args) {

		System.err.println("----RESOURCE SUBMISSION CALLBACK TEST----");
		GATContext context = new GATContext();
		Preferences prefs = new Preferences();
		prefs.put("ResourceBroker.adaptor.name", "globus");
		prefs.put("ResourceBroker.jobmanager", "pbs");

		URI exe = null;
		URI out = null;
		URI err = null;
		try {
			exe = new URI("file:////bin/date");
			out = new URI("any:///date.out");
			err = new URI("any:///date.err");
		} catch (URISyntaxException e) {
			System.err.println("syntax error in URI: " + e);
			System.exit(1);
		}

		File outFile = null;
		File errFile = null;

		try {
			outFile = GAT.createFile(context, prefs, out);
			errFile = GAT.createFile(context, prefs, err);
		} catch (GATObjectCreationException e) {
			System.err.println("error creating file: " + e);
			System.exit(1);
		}

		SoftwareDescription sd = new SoftwareDescription();
		sd.setLocation(exe);
		sd.setStdout(outFile);
		sd.setStderr(errFile);

		Hashtable hardwareAttributes = new Hashtable();

		hardwareAttributes.put("machine.node", "fs0.das2.cs.vu.nl");

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
			e.printStackTrace();
			System.exit(1);
		}

		try {
			MetricDefinition md = job.getMetricDefinitionByName("job.status");
			Metric m = md.createMetric(null);
			job.addMetricListener(this, m); // register my callback for
											// job.status events
		} catch (Exception e) {
			System.err.println("job monitoring failed: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		synchronized (this) {
			while (!exit) {
				try {
					wait();
				} catch (Exception e) {
					// Ignore
				}
			}
		}

		System.err.println("---RESOURCE SUBMISSION CALLBACK TEST OK--");
	}
}