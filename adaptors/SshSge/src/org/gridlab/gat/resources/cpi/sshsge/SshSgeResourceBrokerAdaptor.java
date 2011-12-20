package org.gridlab.gat.resources.cpi.sshsge;

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

public class SshSgeResourceBrokerAdaptor extends ResourceBrokerCpi {

    protected static Logger logger = LoggerFactory
	    .getLogger(SshSgeResourceBrokerAdaptor.class);

    public static Map<String, Boolean> getSupportedCapabilities() {
	Map<String, Boolean> capabilities = ResourceBrokerCpi
		.getSupportedCapabilities();
	capabilities.put("submitJob", true);

	return capabilities;
    }

    static final String SSH_PORT_STRING = "sshsge.ssh.port";
    static final String SSHSGE_NATIVE_FLAGS = "sshsge.native.flags";
    static final String SSHSGE_SCRIPT = "sshsge.script";
    static final String SSH_STRICT_HOST_KEY_CHECKING = "sshsge.StrictHostKeyChecking";

    public static String[] getSupportedSchemes() {
	return new String[] { "sshsge"};
    }
    
    public static Preferences getSupportedPreferences() {
        Preferences p = ResourceBrokerCpi.getSupportedPreferences();
        p.put(SSH_PORT_STRING, "");        
        p.put(SSHSGE_NATIVE_FLAGS, "");
        p.put(SSHSGE_SCRIPT, "");
        p.put(SSH_STRICT_HOST_KEY_CHECKING, "false");
        return p;
    }

    private final SshHelper sshHelper;

    public static void init() {
	GATEngine.registerUnmarshaller(SshSgeJob.class);
    }
    
    public SshSgeResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
	    throws GATObjectCreationException, AdaptorNotApplicableException {

	super(gatContext, brokerURI);
	sshHelper = new SshHelper(gatContext, brokerURI, "sshsge", SSH_PORT_STRING, SSH_STRICT_HOST_KEY_CHECKING);
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
	    throw new GATInvocationException("SshSge adaptor cannot start multiple processes.");
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
	    // TODO!
	}
	
	Sandbox sandbox = new Sandbox(gatContext, description, authority, null,
		true, true, true, true);

	SshSgeJob sshSgeJob = new SshSgeJob(gatContext, brokerURI, description, sandbox,
		sshHelper, returnValueFile);
	
        Job job = null;
        if (description instanceof WrapperJobDescription) {
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, sshSgeJob,
                    listener, metricDefinitionName);
            listener = tmp;
            job = tmp;
        } else {
            job = sshSgeJob;
        }
	if (listener != null && metricDefinitionName != null) {
	    Metric metric = sshSgeJob.getMetricDefinitionByName(metricDefinitionName)
		    .createMetric(null);
	    sshSgeJob.addMetricListener(listener, metric);
	}
	
	java.io.File jobFile = new java.io.File(qsubFileName);

	String jobid = sshSgeSubmission(sshSgeJob, description, jobFile, sandbox);

	if (jobid != null) {
	    sshSgeJob.setState(Job.JobState.SCHEDULED);
	    sshSgeJob.setSoft(description.getSoftwareDescription());
	    sshSgeJob.setJobID(jobid);
	    sshSgeJob.startListener();
	} else {
	    sandbox.removeSandboxDir();
	    throw new GATInvocationException("Could not submit sshSge job");
	}

	return job;
    }

    String createQsubScript(JobDescription description, String returnValueFile)
	    throws GATInvocationException {

	String Queue = null;
	long Time = -1;
	Integer Nodes = null;
	String HwArg = null;
	java.io.File temp;
	SgeScriptWriter job = null;
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
	    temp = java.io.File.createTempFile("sge", null);
	} catch (IOException e) {
	    throw new GATInvocationException("Cannot create temporary qsub file");
	}
	    
	try {
	    job = new SgeScriptWriter(
		    new BufferedWriter(new FileWriter(temp)));
	    String jobName = (String) gatContext.getPreferences().get(SSHSGE_SCRIPT);
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
		job.println("# qsub script automatically generated by GAT SshSge adaptor");

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

		Nodes = (Integer) rd_HashMap
			.get(HardwareResourceDescription.CPU_COUNT);
		if (Nodes == null) {
		    Nodes = description.getResourceCount();
		}

		if (Nodes > 1) {
		    String jobType = getStringAttribute(description, SoftwareDescription.JOB_TYPE, "prun");
		    job.addOption("pe", jobType + " " + Nodes);
		}

		if (Time != -1L) {
		    job.addOption("l", "h_rt=" + (Time*60));
		}

		job.addOption("S", "/bin/sh");

		String nativeFlags = (String) gatContext.getPreferences().get(SSHSGE_NATIVE_FLAGS);
		if (nativeFlags != null) {
		    String[] splits = nativeFlags.split("##");
		    for (String s : splits) {
			job.addString(s);
		    }
		}

		// Set working dir.
		job.addOption("cwd", null);

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
		// In SGE, "y" means that both streams
		// end up in stdout, "n" means separate streams.
		HwArg = (String) rd_HashMap.get("YankEO");
		if (HwArg != null) {
		    if (HwArg.equals("y")) {
			job.addOption("j", "y");
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
	} catch (Throwable e) {
	    throw new GATInvocationException(
		    "Cannot create temporary qsub file"
			    + temp.getAbsolutePath());
	} finally {
	    if (job != null)
		job.close();
	}
	return (temp.getAbsolutePath().toString());
    }

    private String sshSgeSubmission(SshSgeJob SgeJob, JobDescription description,
	    java.io.File qsubFile, Sandbox sandbox) throws GATInvocationException {

	String host = getHostname();
	if (host == null) {
	    host = "localhost";
	}

	try {	    
	    sandbox.prestage();
	    // Create the ssh command for qsub...
	    ArrayList<String> command = sshHelper.getSshCommand(false);
	    if (sandbox.getSandboxPath() != null) {
		command.add("cd");
		command.add(SshHelper.protectAgainstShellMetas(sandbox.getSandboxPath()));
		command.add("&&");
	    }
	    CommandRunner runner = sshHelper.runSshCommand(command, "qsub", qsubFile.getName());
	    List<String> sgeJobID = runner.outputAsLines();
	    if (runner.getExitCode() != 0) {
		throw new GATInvocationException("qsub gave exit status " + runner.getExitCode());
	    }

	    String result = sgeJobID.get(0);
	    // Check for SGE qsub result ...
	    if (result.startsWith("Your job ")) {
		result = result.split(" ")[2];
	    }

	    return result;
	} catch (IOException e) {
	    throw new GATInvocationException("Got IOException", e);
	} finally {
	    qsubFile.delete();
	}
    }
}
