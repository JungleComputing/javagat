package gjf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class GridJobFarming1 implements MetricListener {

	final static String version = "$Id: GridJobFarming1.java,v 1.26 2007/01/23 14:40:23 kscover Exp kscover $";

	// Count of nodes started and finished
	int jobsSubmitted = 0;
	int jobsStopped = 0;

	// List of jobs submitted
	Job jobList[];

	// Job parameters including name
	String[][] jobParams;

	// Name of the output directory
	String outputDirGlobal;

	// The resource broker
	ResourceBroker broker;

	public synchronized void processMetricEvent(MetricValue val) {
		notifyAll();
	}

	// ************************************************************************************
	// Submit a job to the queue for execution
	Job submitJob(final GATContext context, final String jobManagerContact,
			final String globusQueueName, final String nodeDiskPath,
			final String shPath, final String javaPath,
			final String remoteGatLocation, final String jobName,
			final String scriptFileName, final String[] arguments,
			final String prestageDir, final String inputDir,
			final String outputDir, final HashMap environmentMap,
			final int maxExecTime) throws Exception {

		SoftwareDescription sd = new SoftwareDescription();
		sd.setLocation("any:///" + shPath);
		sd.setArguments(arguments);

		// Prestage
		sd.addPreStagedFile(GAT.createFile(context, scriptFileName));
		sd.setEnvironment(environmentMap);
		sd.addAttribute("maxWallTime", "" + maxExecTime);

		// put the files on the local disk of the node
		sd.addAttribute("java.home", new URI(javaPath.substring(0, javaPath
				.length()
				- "/bin/java".length())));
		sd.addAttribute("useLocalDisk", "true");
		sd.addAttribute("waitForPreStage", "true");
		sd.addAttribute("getRemoteSandboxOutput", "true");
		sd.addAttribute("sandboxRoot", nodeDiskPath);
		// sd.addAttribute("remoteGatLocation", "../tempGAT");
		sd.addAttribute("getRemoteSandboxOutputURI",
				"any://fs0.das2.cs.vu.nl/GAT/out/gjf");

		if (remoteGatLocation != null) {
			sd.addAttribute("remoteGatLocation", remoteGatLocation);
		}

		if (globusQueueName != null)
			sd.addAttribute("queue", globusQueueName);

		if (!prestageDir.equals("-")) {
			// Prestage all the files and directories in the prestageDir
			final File prestageFile = new File(prestageDir);
			if (!prestageFile.isDirectory()) {
				throw new Exception("The prestageDir [" + prestageDir
						+ "] is not directory");
			}
			final String[] prestageList = prestageFile.list();
			for (int listIdx = 0; listIdx < prestageList.length; listIdx++)
				sd.addPreStagedFile(GAT.createFile(context, prestageDir + "/"
						+ prestageList[listIdx]));
		}
		if (!inputDir.equals("-")) {
			// Prestage all the files and directories in the inputDir starting
			// with the job name
			final File inputFile = new File(inputDir);
			if (!inputFile.isDirectory()) {
				throw new Exception("The inputDir [" + inputDir
						+ "] is not directory");
			}
			final String[] inputList = inputFile.list();
			for (int listIdx = 0; listIdx < inputList.length; listIdx++) {
				if (inputList[listIdx].startsWith(jobName))
					sd.addPreStagedFile(GAT.createFile(context, inputDir + "/"
							+ inputList[listIdx]));
			}
		}

		// Post stage
		final File outputFile = new File(outputDir);
		if (!outputFile.isDirectory()) {
			throw new Exception("The outputDir [" + outputDir
					+ "] is not directory");
		}
		sd.addPostStagedFile(GAT.createFile(context, "Output"), GAT.createFile(
				context, outputDir));

		// Standard in and out
		sd.setStdout(GAT.createFile(context, "any://fs0.das2.cs.vu.nl/" + outputDir + "/" + jobName
				+ ".stdout"));
		sd.setStderr(GAT.createFile(context, outputDir + "/" + jobName
				+ ".stderr"));
		sd.setWipePostStaged(true);
		sd.setWipePreStaged(true);
		JobDescription jd = new JobDescription(sd);
		Job job = broker.submitJob(jd);
		jobsSubmitted++;

		return job;
	}

	void start(String[] args) {

		System.out.println(version);

		// Check number of command line parameters
		final int numArgsExpected = 8;
		if (args.length != numArgsExpected) {
			System.out.println("GridJobFarming1");
			System.out
					.println("   prestageDir: directory that contains files and directories to be uploaded to every sandbox");
			System.out
					.println("                If - then nothing is to be uploaded from the prestage directory");
			System.out
					.println("      inputDir: any files or directories in inputDir starting with the job name will be uploaded to the job sandbox");
			System.out
					.println("                If - then nothing is to be uploaded from the input directory");
			System.out
					.println("     outputDir: all files and directories in the Output directory of every sandbox will be downloaded to this directory");
			System.out
					.println("                Also, the stdout, stderr and lock files will be placed in this directory");
			System.out.println("scriptFileName: Linux shell script file name");
			System.out
					.println(" paramFileName: file name with each line starting with the job name and followed by any job parameters");
			System.out
					.println("   clusterName: name of the cluster (and queue) where to run the jobs");
			System.out
					.println("                Das2Fs0: fs0 nodes of the DAS2 cluster (fs0.das2.cs.vu.nl)");
			System.out
					.println("                Das2Fs1: fs1 nodes of the DAS2 cluster (fs1.das2.liacs.nl)");
			System.out
					.println("                Das2Fs2: fs2 nodes of the DAS2 cluster (fs2.das2.nikhef.nl)");
			System.out
					.println("                Das3Fs0: fs0 nodes of the DAS3 cluster (fs0.das3.cs.vu.nl)");
			System.out
					.println("                Matrix:  nodes of the Matrix cluster (ui.matrix.sara.nl)");
			System.out
					.println("                Nikhef:  nodes of the Nikhef cluster (nikhef.nl)");
			System.out
					.println("   maxExecTime: maximum time (in minutes) a job has to execute");
			System.out
					.println(" maxJobsAtOnce: maximum number of jobs to run at one time");
			System.out.println();
			System.out
					.println("Note: The scriptFile must include the following:");
			System.out
					.println("   1) Create a directory in the sandbox called Output");
			System.out
					.println("   2) Chmod the mode of an executable file to executable before running");
			System.out
					.println("   3) The last line of the shell script should be: echo JobDone > Output/${JobName}.done");
			System.out.println();

			System.out.println("args.length = " + args.length + " should be "
					+ numArgsExpected);
			for (int i = 0; i < args.length; i++)
				System.out.println(i + ": " + args[i]);
			System.out.println();
			System.exit(-1);
		}

		final String prestageDir = args[0];
		final String inputDir = args[1];
		final String outputDir = args[2];
		final String scriptFileName = args[3];
		final String paramFileName = args[4];
		final String clusterName = args[5];
		final int maxExecTime = Integer.valueOf(args[6]).intValue();
		final int maxJobsAtOnce = Integer.valueOf(args[7]).intValue();

		System.out.println("     prestageDir: " + prestageDir);
		System.out.println("        inputDir: " + inputDir);
		System.out.println("       outputDir: " + outputDir);
		System.out.println("  scriptFileName: " + scriptFileName);
		System.out.println("   paramFileName: " + paramFileName);
		System.out.println("     clusterName: " + clusterName);
		System.out.println("     maxExecTime: " + maxExecTime + " min");
		System.out.println("   maxJobsAtOnce: " + maxJobsAtOnce);
		System.out.println();
		System.out.flush();

		outputDirGlobal = outputDir;

		try {

			// READ IN THE PARAMETER FILES

			// Count the number of jobs; All line whose first character is # are
			// ignored
			final int jobNum;
			{
				String line;
				int jobCnt = 0;
				final BufferedReader paramBR = new BufferedReader(
						new FileReader(paramFileName));
				do {
					line = paramBR.readLine();
					if (line != null && !line.substring(0, 1).equals("#"))
						jobCnt++;
				} while (line != null);
				jobNum = jobCnt;
			}
			System.out.println("Number jobs in paramFile: " + jobNum);

			// Read in job names and parameters for each job
			jobParams = new String[jobNum][];
			{
				String line;
				int jobIdx = 0;
				final BufferedReader paramBR = new BufferedReader(
						new FileReader(paramFileName));
				do {
					line = paramBR.readLine();
					if (line != null && !line.substring(0, 1).equals("#")) {
						jobParams[jobIdx] = line.split(" ");
						System.out.println("job params: " + jobIdx + ":  "
								+ line);
						jobIdx++;
					}
				} while (line != null);
			}
			System.out.println();

			// SELECT THE CLUSTERS FOR THE NODES

			// Select the cluster for the nodes
			final String jobManagerContact;
			final String globusQueueName;
			final String javaPath;
			final String shPath;
			final String nodeDiskPath;
			final String remoteGatLocation;
			final GATContext context = new GATContext();

			// Force the use of the globus middleware for file access. No other
			// middleware will be tried.
			context.addPreference("File.adaptor.name", "GridFTP");
			context.addPreference("FileOutputStream.adaptor.name", "GridFTP");
			context.addPreference("FileInputStream.adaptor.name", "GridFTP");
			context.addPreference("singleRemoteGAT", "true");
			

			{
				final Preferences prefs = new Preferences();
				if (clusterName.equals("Das2Fs0")) {
					jobManagerContact = "fs0.das2.cs.vu.nl";
					globusQueueName = null;
					javaPath = "/usr/local/sun-java/jdk1.5/bin/java";
					shPath = "/bin/sh";
					nodeDiskPath = "/var/tmp";
					remoteGatLocation = "/usr/local/VU/JavaGAT/JavaGAT-1.6.4";
				} else if (clusterName.equals("Das2Fs1")) {
					jobManagerContact = "fs1.das2.liacs.nl";
					globusQueueName = null;
					javaPath = "/usr/local/sun-java/jdk1.5/bin/java";
					shPath = "/bin/sh";
					nodeDiskPath = "/tmp";
					remoteGatLocation = "/usr/local/VU/JavaGAT/JavaGAT-1.6.4";
				} else if (clusterName.equals("Das2Fs2")) {
					jobManagerContact = "fs2.das2.nikhef.nl";
					globusQueueName = null;
					javaPath = "/usr/local/sun-java/jdk1.5/bin/java";
					shPath = "/bin/sh";
					nodeDiskPath = "/tmp";
					remoteGatLocation = "/usr/local/VU/JavaGAT/JavaGAT-1.6.4";
				} else if (clusterName.equals("Das3Fs0")) {
					jobManagerContact = "fs0.das3.cs.vu.nl/jobmanager-sge";
					globusQueueName = null;
					javaPath = "/usr/local/package/jdk1.5/bin/java";
					shPath = "/bin/sh";
					nodeDiskPath = "/local";
					remoteGatLocation = null;
				} else if (clusterName.equals("Matrix4hrs")) {
					jobManagerContact = "mu6.matrix.sara.nl/jobmanager-pbs";
					globusQueueName = "short";
					javaPath = "/usr/java/j2sdk1.4.2_12/bin/java";
					shPath = "/bin/sh";
					nodeDiskPath = "/tmp";
					remoteGatLocation = null;
				} else if (clusterName.equals("Matrix33hrs")) {
					jobManagerContact = "mu6.matrix.sara.nl/jobmanager-pbs";
					globusQueueName = "medium";
					javaPath = "/usr/java/j2sdk1.4.2_12/bin/java";
					shPath = "/bin/sh";
					nodeDiskPath = "/tmp";
					remoteGatLocation = null;
				} else if (clusterName.equals("Matrix72hrs")) {
					jobManagerContact = "mu6.matrix.sara.nl/jobmanager-pbs";
					globusQueueName = "long";
					javaPath = "/usr/java/j2sdk1.4.2_12/bin/java";
					shPath = "/bin/sh";
					nodeDiskPath = "/tmp";
					remoteGatLocation = null;
				} else if (clusterName.equals("Nikhef4hrs")) {
					jobManagerContact = "tbn20.nikhef.nl:2119/jobmanager-pbs";
					globusQueueName = "qshort";
					javaPath = "/usr/java/jdk1.5.0_11/bin/java";
					shPath = "/bin/sh";
					nodeDiskPath = "/tmp";
					remoteGatLocation = null;
				} else if (clusterName.equals("Nikhef60hrs")) {
					jobManagerContact = "tbn20.nikhef.nl:2119/jobmanager-pbs";
					globusQueueName = "qlong";
					javaPath = "/usr/java/jdk1.5.0_11/bin/java";
					shPath = "/bin/sh";
					nodeDiskPath = "/tmp";
					remoteGatLocation = null;
				} else {
					throw new Exception("Unknown cluster name: " + clusterName);
				}
				prefs
						.put("ResourceBroker.jobmanagerContact",
								jobManagerContact);
				prefs.put("ResourceBroker.adaptor.name", "globus");
				context.addPreferences(prefs);
			}

			// Setup queue and cluster dependent environment variables for the
			// sh script
			HashMap environmentMap = new HashMap();
			environmentMap.put("JG_JAVA_PATH", javaPath);
			environmentMap.put("JG_SH_PATH", shPath);
			environmentMap.put("JG_NODE_DISK_PATH", nodeDiskPath);

			// Print out queue and cluster information
			System.out.println("jobManagerContact: " + jobManagerContact);
			System.out.println("  globusQueueName: " + globusQueueName);
			System.out.println("Sh script environment variables:");
			System.out.println(environmentMap);

			// Check the script file exists
			final File scriptFile = new File(scriptFileName);
			if (!scriptFile.isFile()) {
				throw new Exception("scriptFileName: " + scriptFileName
						+ " is not a file");
			}

			// Submit one job for each data set core name
			jobList = new Job[jobNum];
			int jobsSubmittedCnt = 0;
			broker = GAT.createResourceBroker(context);

			broker.beginMultiCoreJob();			
			for (int jobIdx = 0; jobIdx < jobNum; jobIdx++) {

				// Wait to avoid exceeding maxJobsAtOnce before submitting the
				// next job
				while (jobsSubmitted - jobsStopped >= maxJobsAtOnce) {
					Thread.sleep(1000);
				}

				// Setup the arguments for the shell on the core
				final String jobName = jobParams[jobIdx][0];
				final String[] arguments = new String[1 + jobParams[jobIdx].length];
				arguments[0] = scriptFile.getName();
				for (int paramIdx = 0; paramIdx < jobParams[jobIdx].length; paramIdx++)
					arguments[paramIdx + 1] = jobParams[jobIdx][paramIdx];

				// Submit the job if it is not already flagged as done
				final File jobDoneFile = new File(outputDir + "/" + jobName
						+ ".done");
				if (jobDoneFile.exists()) {
					jobList[jobIdx] = null;
				} else {
					jobList[jobIdx] = submitJob(context, jobManagerContact,
							globusQueueName, nodeDiskPath, shPath, javaPath,
							remoteGatLocation, jobName, scriptFileName,
							arguments, prestageDir, inputDir, outputDir,
							environmentMap, maxExecTime);

					// Flag that the job is submitted
					final File jobFile = new File(outputDir + "/" + jobName
							+ ".sbmt");
					if (jobFile.exists())
						jobFile.delete();
					jobFile.createNewFile();

					jobsSubmittedCnt++;
				}
			}

			Job job = broker.endMultiCoreJob();
			MetricDefinition md = job.getMetricDefinitionByName("job.status");
			Metric m = md.createMetric(null);
			job.addMetricListener(this, m); // register my callback for
											// job.status
											// events


			if (jobsSubmittedCnt > 0) {
				synchronized (this) {
					// Wait until all jobs stopped or get submission error
					boolean jobsStopped = true;
					while (jobsStopped) {
						wait();
						jobsStopped = false;
						if (job != null) {
							jobsStopped = jobsStopped || (job.getState() != Job.STOPPED && job.getState() != Job.SUBMISSION_ERROR);
							
						}
						/*for (int jobIdx = 0; jobIdx < jobNum; jobIdx++) {
							if (jobList[jobIdx] != null) {
								jobsStopped = jobsStopped
										|| (jobList[jobIdx].getState() != Job.STOPPED && jobList[jobIdx]
												.getState() != Job.SUBMISSION_ERROR);
							}
						}*/
					}
				}
			}

			// Print out the job information
			System.out.println();
			System.out.println("All jobs done");
			/*for (int jobIdx = 0; jobIdx < jobNum; jobIdx++) {
				final String jobName = jobParams[jobIdx][0];
				if (jobList[jobIdx] == null)
					System.out.printf("%4d  %-20s  %s ", jobIdx, jobName,
							"Already done" + "\n");
				else
					System.out.printf("%4d  %-20s  %s ", jobIdx, jobName,
							jobList[jobIdx].getInfo() + "\n");
			}
			System.out.println();*/

		} catch (Exception e) {
			System.err.println("an exception occurred: " + e);
			e.printStackTrace();
		} finally {
			GAT.end();
		}
	}

	public static void main(String[] args) {
		GridJobFarming1 a = new GridJobFarming1();
		a.start(args);
	}
}
