/*
 * SshPbsBrokerAdaptor.java
 *
 * Created on July 8, 2008
 *
 */

package de.mpg.aei.gat.resources.cpi.sshpbs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.lang.StringBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.CommandRunner;
import org.gridlab.gat.engine.util.SshHelper;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperJobCpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Alexander Beck-Ratzka, AEI.
 * 
 */

public class SshPbsResourceBrokerAdaptor extends ResourceBrokerCpi {

    protected static Logger logger = LoggerFactory
	    .getLogger(SshPbsResourceBrokerAdaptor.class);

    public static Map<String, Boolean> getSupportedCapabilities() {
	Map<String, Boolean> capabilities = ResourceBrokerCpi
		.getSupportedCapabilities();
	capabilities.put("submitJob", true);

	return capabilities;
    }

    static final String SSH_PORT_STRING = "sshpbs.ssh.port";
    static final String SSHPBS_NATIVE_FLAGS = "sshpbs.native.flags";
    static final String SSHPBS_SCRIPT = "sshpbs.script";
    static final String SSH_STRICT_HOST_KEY_CHECKING = "sshpbs.StrictHostKeyChecking";

    public static String[] getSupportedSchemes() {
	return new String[] { "sshpbs"};
    }
    
    public static Preferences getSupportedPreferences() {
        Preferences p = ResourceBrokerCpi.getSupportedPreferences();
        p.put(SSH_PORT_STRING, "");        
        p.put(SSHPBS_NATIVE_FLAGS, "");
        p.put(SSHPBS_SCRIPT, "");
        p.put(SSH_STRICT_HOST_KEY_CHECKING, "false");
        return p;
    }

    private final SshHelper sshHelper;

    public static void init() {
	GATEngine.registerUnmarshaller(SshPbsJob.class);
    }
    
    public SshPbsResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
	    throws GATObjectCreationException, AdaptorNotApplicableException {

	super(gatContext, brokerURI);
	sshHelper = new SshHelper(gatContext, brokerURI, "sshpbs", SSH_PORT_STRING, SSH_STRICT_HOST_KEY_CHECKING);
    }

    public Job submitJob(AbstractJobDescription abstractDescription,
	    MetricListener listener, String metricDefinitionName)
	    throws GATInvocationException {

	if (!(abstractDescription instanceof JobDescription)) {
	    throw new GATInvocationException(
		    "can only handle JobDescriptions: "
			    + abstractDescription.getClass());
	}

	JobDescription description = (JobDescription) abstractDescription;
	
	int nproc = description.getProcessCount();
	
	if (nproc != 1) {
	    throw new GATInvocationException("SshPbs adaptor cannot start multiple processes.");
	}

	String authority = getAuthority();
	if (authority == null) {
	    authority = "localhost";
	}
	
	// Create filename for return value.
	String returnValueFile = ".rc." + Math.random();
	String qsubFileName = createQsubScript(description, returnValueFile);
	
	try {
	    description.getSoftwareDescription().addPostStagedFile(GAT.createFile(gatContext, new URI(returnValueFile)));
	    description.getSoftwareDescription().addPreStagedFile(GAT.createFile(gatContext, new URI(qsubFileName)));
	} catch (Throwable e) {
	    throw new GATInvocationException("Error in file staging", e);
	}
	
	Sandbox sandbox = new Sandbox(gatContext, description, authority, null,
		true, true, true, true);

	SshPbsJob sshPbsJob = new SshPbsJob(gatContext, brokerURI, description, sandbox,
		sshHelper, returnValueFile);
	
        Job job = null;
        if (description instanceof WrapperJobDescription) {
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, sshPbsJob,
                    listener, metricDefinitionName);
            listener = tmp;
            job = tmp;
        } else {
            job = sshPbsJob;
        }
	if (listener != null && metricDefinitionName != null) {
	    Metric metric = sshPbsJob.getMetricDefinitionByName(metricDefinitionName)
		    .createMetric(null);
	    sshPbsJob.addMetricListener(listener, metric);
	}
	
	java.io.File jobFile = new java.io.File(qsubFileName);

	String jobid = sshPbsSubmission(sshPbsJob, description, jobFile, sandbox);

	if (jobid != null) {
	    sshPbsJob.setState(Job.JobState.SCHEDULED);
	    sshPbsJob.setSoft(description.getSoftwareDescription());
	    sshPbsJob.setJobID(jobid);
	    sshPbsJob.startListener();
	} else {
	    sandbox.removeSandboxDir();
	    throw new GATInvocationException("Could not submit sshPbs job");
	}

	return job;
    }

    String createQsubScript(JobDescription description, String returnValueFile)
	    throws GATInvocationException {

	String Queue = null;
	long Time = -1;
	String Filesize = null;
	Float Memsize = null;
	Integer Nodes = null;
	String HwArg = null;
	java.io.File temp;
	PbsScriptWriter job = null;
	HashMap<String, Object> rd_HashMap = null;
	
	SoftwareDescription sd = description.getSoftwareDescription();
	ResourceDescription rd = description.getResourceDescription();

	// Corrected initialization of rd_HashMap: rd may be null ... --Ceriel
	if (rd != null) {
	    rd_HashMap = (HashMap<String, Object>) rd.getDescription();
	}
	if (rd_HashMap == null) {
	    rd_HashMap = new HashMap<String, Object>();
	}

	try {
	    temp = java.io.File.createTempFile("pbs", null);
	} catch (IOException e) {
	    throw new GATInvocationException("Cannot create temporary qsub file");
	}
	    
	try {
	    job = new PbsScriptWriter(
		    new BufferedWriter(new FileWriter(temp)));
	    String jobName = (String) gatContext.getPreferences().get(SSHPBS_SCRIPT);
	    if (jobName != null) {
		// a specified job script overrides everything, except for pre-staging, post-staging,
		// and exit status.
		BufferedReader f = new BufferedReader(new FileReader(jobName));
		for (;;) {
		    String s = f.readLine();
		    if (s == null) {
			break;
		    }
		    job.println(s);
		}
	    } else {
		job.println("#!/bin/sh");
		job.println("# qsub script automatically generated by GAT SshPbs adaptor");

		// Resources: queue, walltime, memory size, et cetera.
		Queue = (String) rd_HashMap.get("machine.queue");
		if (Queue == null) {
		    Queue = sd.getStringAttribute(
			    SoftwareDescription.JOB_QUEUE, null);
		}
		if (Queue != null) {
		    job.addOption("q", Queue);
		}

		Time = sd.getLongAttribute(SoftwareDescription.WALLTIME_MAX, -1L);

		Filesize = (String) rd_HashMap.get("file.size");

		Memsize = (Float) rd_HashMap.get(HardwareResourceDescription.MEMORY_SIZE);

		Nodes = (Integer) rd_HashMap
			.get(HardwareResourceDescription.CPU_COUNT);
		if (Nodes == null) {
		    Nodes = description.getResourceCount();
		}

		if (Time != -1L) {
		    // Reformat time.
		    int minutes = (int) (Time % 60);
		    job.addOption("l", "walltime=" + (Time / 60) + ":" + (minutes / 10) + (minutes % 10) + ":00");
		}

		if (Filesize != null) {
		    job.addOption("l", "file=" + Filesize);
		}

		if (Memsize != null) {
		    job.addOption("l", "mem=" + Memsize);
		}

		if (Nodes != 1) {
		    job.addOption("l", "nodes=" + Nodes);
		}

		String nativeFlags = (String) gatContext.getPreferences().get(SSHPBS_NATIVE_FLAGS);
		if (nativeFlags != null) {
		    String[] splits = nativeFlags.split("##");
		    for (String s : splits) {
			job.addString(s);
		    }
		}

		job.addOption("S", "/bin/sh");

		// Name for the job.
		HwArg = (String) rd_HashMap.get("Jobname");
		if (HwArg == null) {
		    HwArg = brokerURI.getUserInfo();
		    if (HwArg == null || "".equals(HwArg)) {
			HwArg = System.getProperty("user.name");
		    }
		}

		if (HwArg != null)
		    job.addOption("N", HwArg);

		// Combine stdout and stderr?
		// In PBS: "eo" means that both streams end up in stderr,
		// "oe" means that both streams end up in stdout,
		// "n" means separate streams.
		HwArg = (String) rd_HashMap.get("YankEO");
		if (HwArg != null) {
		    if (HwArg.equals("y")) {
			job.addOption("j", "oe");
		    } else {
			job.addOption("j", HwArg);
		    }
		}

		// Files for stdout and stderr.
		if (sd.getStdout() != null) {
		    job.addOption("o", sd.getStdout().getName().toString());
		}

		if (sd.getStderr() != null) {
		    job.addOption("e", sd.getStderr().getName().toString());
		}

		// Support environment.
		Map<String, Object> env = sd.getEnvironment();
		if (env != null) {
		    Set<String> s = env.keySet();
		    Object[] keys = s.toArray();

		    for (int i = 0; i < keys.length; i++) {
			String val = (String) env.get(keys[i]);
			job.println(keys[i] + "=" + val + " && export " + keys[i]);
		    }
		}

		// Construct command.
		StringBuffer cmd = new StringBuffer();

		// cmd.append("$HOME/" + sandbox.getSandboxPath() + "/" +
		// sd.getExecutable().toString());
		// No, that assumes that the executable is in
		// the sandbox. We don't know that. --Ceriel
		cmd.append(SshHelper.protectAgainstShellMetas(sd.getExecutable().toString()));
		if (sd.getArguments() != null) {
		    String[] args = sd.getArguments();
		    for (int i = 0; i < args.length; ++i) {
			cmd.append(" ");
			cmd.append(SshHelper.protectAgainstShellMetas(args[i]));
		    }
		}
		job.println(cmd.toString());
	    }

	    job.println("echo \"retvalue = $?\" > " + returnValueFile);
	    // job.println("echo \"retvalue = $?\" > " + returnValueFile);
	} catch (Throwable e) {
	    throw new GATInvocationException(
		    "Cannot create temporary qsub file"
			    + temp.getAbsolutePath(), e);
	} finally {
	    if (job != null)
		job.close();
	}
	return (temp.getAbsolutePath().toString());
    }

    /**
     * Submits a PBS job via ssh on a PBS host, and transfers the qsub file to
     * the PBS host.
     * @return the job ID of the PBS job on the remote host
     */

    private String sshPbsSubmission(SshPbsJob PbsJob, JobDescription description,
	    java.io.File qsubFile, Sandbox sandbox) throws GATInvocationException {

	String host = getHostname();
	if (host == null) {
	    host = "localhost";
	}

	try {
	    CommandRunner runner;
	    
	    sandbox.prestage();

	    ArrayList<String> command = sshHelper.getSshCommand(false);
	    if (sandbox.getSandboxPath() != null) {
		command.add("cd");
		command.add(SshHelper.protectAgainstShellMetas(sandbox.getSandboxPath()));
		command.add("&&");
		command.add("qsub");
		command.add("-d");
		command.add("`pwd`");	// Not to be protected, so added separately.
		runner = sshHelper.runSshCommand(command, qsubFile.getName());
	    } else {
		runner = sshHelper.runSshCommand(command, "qsub", qsubFile.getName());
	    }
	    List<String> PbsJobID = runner.outputAsLines();
	    if (runner.getExitCode() != 0) {
		throw new GATInvocationException("qsub gave exit status " + runner.getExitCode());
	    }

	    String result = PbsJobID.get(0);

	    return result;

	} catch (IOException e) {
	    throw new GATInvocationException("Got IOException", e);
	} finally {
	    qsubFile.delete();
	}
    }
}
