package org.gridlab.gat.resources.cpi;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class RemoteSandbox implements MetricListener {
	protected static Logger logger = Logger.getLogger(RemoteSandbox.class);

	boolean verbose = false;

	boolean debug = false;

	boolean timing = false;

	private String initiator;

	private Map<Job, String> jobMap = new HashMap<Job, String>();

	public static void main(String[] args) {
		new RemoteSandbox().start(args);
	}

	public synchronized void processMetricEvent(MetricValue val) {
		Job job = (Job) val.getSource();
		GATContext gatContext = new GATContext();
		FileWriter writer = null;
		try {
			URI local = new URI(".JavaGATstatus" + jobMap.get(job));
			URI dest = new URI("any://" + initiator + "/.JavaGATstatus"
					+ jobMap.get(job));
			File localFile = GAT.createFile(gatContext, local);
			File remoteFile = GAT.createFile(gatContext, dest);
			localFile.createNewFile();
			writer = new FileWriter(localFile);
			writer.write(job.getState());
			writer.flush();
			writer.close();
			while (remoteFile.exists()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			localFile.copy(dest);
			localFile.delete();
		} catch (GATObjectCreationException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		} catch (IOException e) {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e1) {
					if (logger.isDebugEnabled()) {
						logger.debug(e1);
					}
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		} catch (URISyntaxException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		} catch (GATInvocationException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}
		notifyAll();
	}

	private File rewriteStagedFile(GATContext gatContext,
			Preferences preferences, File origSrc, File origDest,
			String destHostname, String remoteCWD) {
		if (origSrc == null && origDest == null) {
			return null;
		}

		// leave remote files untouched
		if (origDest != null && origDest.toGATURI().getHost() != null
				&& !origDest.toGATURI().getHost().equals("localhost")) {
			return origDest;
		}

		String newPath = null;
		if (origDest == null) {
			newPath = origSrc.getName();
		} else {
			newPath = origDest.toGATURI().getPath();

			// if we have a relative path without a hostname in the URI,
			// it means that the file is relative to CWD.
			if (origDest.toGATURI().getHost() == null && !origDest.isAbsolute()) {
				newPath = remoteCWD + "/" + newPath;
			}
		}

		String newLocation = "any://" + destHostname + "/" + newPath;

		File res = null;
		try {
			URI newURI = new URI(newLocation);

			if (logger.isInfoEnabled()) {
				logger.info("rewrite of " + newPath + " to " + newURI);
			}
			res = GAT.createFile(gatContext, preferences, newURI);
		} catch (Exception e) {
			if (logger.isInfoEnabled()) {
				logger.info("could not rewrite poststage file" + newPath + ":"
						+ e);
			}
			System.exit(1);
		}

		return res;
	}

	public void start(String[] args) {
		final String descriptorFile = args[0];
		final String initiator = args[1];
		final String remoteCWD = args[2];
		this.initiator = initiator;
		verbose = args[3].equalsIgnoreCase("true");
		debug = args[4].equalsIgnoreCase("true");
		timing = args[5].equalsIgnoreCase("true");
		String jobIDsString = args[6];

		if (verbose) {
			System.setProperty("gat.verbose", "true");
			logger.setLevel(Level.INFO);
		}
		if (debug) {
			System.setProperty("gat.debug", "true");
			logger.setLevel(Level.DEBUG);
		}
		if (timing) {
			System.setProperty("gat.timing", "true");
		}

		if (logger.isInfoEnabled()) {
			logger.info("RemoteSandbox started, initiator: " + initiator);
		}

		JobDescription[] descriptions = null;
		Preferences preferences = null;
		String[] preStageDoneLocations = null;
		try {
			if (logger.isInfoEnabled()) {
				logger.info("opening descriptor file: " + descriptorFile);
			}
			java.io.FileInputStream tmp = new java.io.FileInputStream(
					descriptorFile);
			ObjectInputStream in = new ObjectInputStream(tmp);
			descriptions = (JobDescription[]) in.readObject();
			preferences = (Preferences) in.readObject();
			preferences.remove("ResourceBroker.jobmanagerContact");
			preStageDoneLocations = (String[]) in.readObject();
			in.close();
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("an error occurred: " + e);
				StringWriter writer = new StringWriter();
				e.printStackTrace(new PrintWriter(writer));
				logger.debug(e.toString());
			}
			System.exit(1);
		}
		Job[] jobs = new Job[descriptions.length];
		String[] jobIDs = jobIDsString.split(",");
		for (int currentDescription = 0; currentDescription < descriptions.length; currentDescription++) {
			// modify the description to run it locally
			SoftwareDescription sd = descriptions[currentDescription]
					.getSoftwareDescription();
			sd.addAttribute("useRemoteSandbox", "false");

			if (logger.isInfoEnabled()) {
				logger.info("read job description: "
						+ descriptions[currentDescription]);
			}

			GATContext gatContext = new GATContext();
			preferences.put("ResourceBroker.adaptor.name", "local");

			// rewrite poststage files to go directly to their original
			// destination
			// also stdout and stderr
			sd.setStderr(rewriteStagedFile(gatContext, preferences, null, sd
					.getStderr(), initiator, remoteCWD));
			sd.setStdout(rewriteStagedFile(gatContext, preferences, null, sd
					.getStdout(), initiator, remoteCWD));
			sd.setStdin(rewriteStagedFile(gatContext, preferences, sd
					.getStdin(), null, initiator, remoteCWD));

			Map<File, File> pre = sd.getPreStaged();
			Set<File> tmp = pre.keySet();
			Object[] keys = tmp.toArray();
			File[] srcs = new File[keys.length];
			File[] dests = new File[keys.length];
			for (int i = 0; i < keys.length; i++) {
				File src = (File) keys[i];
				File dest = (File) pre.get(src);
				srcs[i] = rewriteStagedFile(gatContext, preferences, dest, src,
						initiator, remoteCWD);
				dests[i] = dest;
			}
			pre.clear();
			for (int i = 0; i < keys.length; i++) {
				pre.put(srcs[i], dests[i]);
			}

			Map<File, File> post = sd.getPostStaged();
			tmp = post.keySet();
			keys = tmp.toArray();
			for (int i = 0; i < keys.length; i++) {
				File src = (File) keys[i];
				File dest = (File) post.get(src);
				dest = rewriteStagedFile(gatContext, preferences, src, dest,
						initiator, remoteCWD);
				post.put(src, dest);
			}

			if (logger.isInfoEnabled()) {
				logger.info("modified job description: "
						+ descriptions[currentDescription]);
			}

			ResourceBroker broker = null;
			try {
				broker = GAT.createResourceBroker(gatContext, preferences);
			} catch (GATObjectCreationException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("could not create broker: " + e);
				}
				System.exit(1);
			}
			Job job;
			try {
				if (sd.getBooleanAttribute("waitForPreStage", false)) {
					File preStageDoneFile = GAT.createFile(gatContext,
							preferences, "any://" + initiator + "/"
									+ preStageDoneLocations[currentDescription]);
					while (!preStageDoneFile.exists()) {
						Thread.sleep(500);
					}
					job = broker.submitJob(descriptions[currentDescription]);
					jobMap.put(job, jobIDs[currentDescription]);

					// sandbox is created and prestagedfiles are prestaged,
					// during
					// the submitJob method

					if (logger.isDebugEnabled()) {
						logger.debug("deleting prestageDoneFile at "
								+ preStageDoneLocations[currentDescription]);
					}

					if (!preStageDoneFile.delete()) {
						if (logger.isInfoEnabled()) {
							logger.info("could not delete preStageDone file");
						}
						System.exit(1);
					}
					if (logger.isInfoEnabled()) {
						logger.info("deleting prestageDoneFile at "
								+ preStageDoneLocations[currentDescription] + " DONE");
					}
				} else {
					if (logger.isInfoEnabled()) {
						logger.info("not waiting for preStage");
					}
					job = broker.submitJob(descriptions[currentDescription]);
					jobMap.put(job, jobIDs[currentDescription]);

				}
				jobs[currentDescription] = job;

				MetricDefinition md = job
						.getMetricDefinitionByName("job.status");
				Metric m = md.createMetric(null);
				job.addMetricListener(this, m);
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("an exception occurred: " + e);
				}
				System.exit(1);
			}
		}
		synchronized (this) {
			for (int i = 0; i < jobs.length; i++) {
				while ((jobs[i].getState() != Job.STOPPED)
						&& (jobs[i].getState() != Job.SUBMISSION_ERROR)) {
					try {
						wait();
					} catch (InterruptedException e) {
						if (logger.isDebugEnabled()) {
							logger.debug("an exception occurred: " + e);
						}
						System.exit(1);
					}
				}
			}
		}
		if (logger.isInfoEnabled()) {
			for (int i = 0; i < jobs.length; i++) {
				try {
					logger.info("SubmitJobCallback: Job finished, state = "
							+ jobs[i].getInfo());
				} catch (GATInvocationException e) {
					if (logger.isDebugEnabled()) {
						logger.debug("an exception occurred: " + e);
					}
					System.exit(1);
				}
			}
		}
		GAT.end();
		System.exit(0);
	}
}
