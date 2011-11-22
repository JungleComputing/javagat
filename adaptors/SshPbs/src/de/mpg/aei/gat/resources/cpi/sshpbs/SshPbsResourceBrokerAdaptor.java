/*
 * SshPbsBrokerAdaptor.java
 *
 * Created on July 8, 2008
 *
 */

package de.mpg.aei.gat.resources.cpi.sshpbs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.lang.StringBuffer;

import java.util.ArrayList;
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
import org.gridlab.gat.io.File;
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
import de.mpg.aei.gat.resources.cpi.sshpbs.SshPbsSecurityUtils;

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

    private static final String SSH_PORT_STRING = "sshpbs.ssh.port";
    private static final String SSHPBS_NATIVE_FLAGS = "sshpbs.native.flags";

    public static final int SSH_PORT = 22;

    public static String[] getSupportedSchemes() {
	return new String[] { "sshpbs", "sshsge", ""};
    }
    
    public static Preferences getSupportedPreferences() {
        Preferences p = ResourceBrokerCpi.getSupportedPreferences();
        p.put(SSH_PORT_STRING, "" + SSH_PORT);        
        p.put(SSHPBS_NATIVE_FLAGS, "");
        return p;
    }
    
    private final int ssh_port;

    private Map<String, String> securityInfo;

    public SshPbsResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
	    throws GATObjectCreationException, AdaptorNotApplicableException {

	super(gatContext, brokerURI);

	if (brokerURI.getScheme() == null) {
	    throw new AdaptorNotApplicableException("cannot handle this URI: "
		    + brokerURI);
	}

	// allow ssh port override.
	ssh_port = getPort(gatContext, brokerURI);
	
	securityInfo = getSecurityInfo(gatContext, brokerURI);
    }

    static int getPort(GATContext gatContext, URI brokerURI) {
	int portno;
	if (brokerURI.getPort() != -1) {
	    portno = brokerURI.getPort();
	} else {
	    String port = (String) gatContext.getPreferences().get(
		    SSH_PORT_STRING);
	    if (port != null) {
		portno = Integer.parseInt(port);
	    } else {
		portno = SSH_PORT;
	    }
	}
	return portno;
    }

    static Map<String, String> getSecurityInfo(GATContext gatContext,
	    URI brokerURI) throws GATObjectCreationException {

	Map<String, String> securityInfo;

	if (logger.isDebugEnabled()) {
	    logger.debug("context: " + gatContext);
	    logger.debug("broker: " + brokerURI);
	}
	try {
	    securityInfo = SshPbsSecurityUtils.getSshCredential(gatContext,
		    "sshpbs", brokerURI, getPort(gatContext, brokerURI));
	} catch (Throwable e) {
	    logger.info("SshPbsResourceBrokerAdaptor: failed to retrieve credentials"
		    + e);
	    securityInfo = null;
	}

	if (securityInfo == null) {
	    throw new GATObjectCreationException(
		    "Unable to retrieve user info for authentication");
	}

	if (securityInfo.containsKey("privatekeyfile")) {
	    if (logger.isDebugEnabled()) {
		logger.debug("key file argument not supported yet");
	    }
	}

	return securityInfo;
    }

    public URI getBrokerURI() {
	return brokerURI;
    }

    public static void init() {
	GATEngine.registerUnmarshaller(SshPbsJob.class);
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
	    throw new GATInvocationException("SGE/PBS adaptor cannot start multiple processes.");
	}

	String authority = getAuthority();
	if (authority == null) {
	    authority = "localhost";
	}
		
	String host = getHostname();
	if (host == null) {
	    host = "localhost";
	}

	if (logger.isDebugEnabled()) {
	    logger.debug("SshPbs adaptor will use '" + host + "' as execution host");
	}
	
	// Create filename for return value.
	String returnValueFile = ".rc." + Math.random();
	try {
	    description.getSoftwareDescription().addPostStagedFile(GAT.createFile(gatContext, new URI(returnValueFile)));
	} catch (Throwable e) {
	    // O well, we tried.
	}
	
	Sandbox sandbox = new Sandbox(gatContext, description, authority, null,
		true, true, true, true);

	SshPbsJob sshPbsJob = new SshPbsJob(gatContext, this, description, sandbox,
		securityInfo, returnValueFile);
	
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

	String jobid = PBS_Submit(sshPbsJob, description, sandbox, returnValueFile);

	if (jobid != null) {
	    sshPbsJob.setState(Job.JobState.SCHEDULED);
	    sshPbsJob.setSoft(description.getSoftwareDescription());
	    sshPbsJob.setJobID(jobid);
	    sshPbsJob.startListener();
	} else {
	    throw new GATInvocationException("Could not submit sshPbs job");
	}

	return job;
    }

    /**
     * The method submit is for the submission of sshPbs jobs. It submit qsub
     * via ssh on the remote host, and retrieves the job ID.
     * 
     * @param Map
     *            <String, String> containing the security info
     * @author Alexander Beck-Ratzka, AEI, July 2010.
     */

    private String PBS_Submit(SshPbsJob PbsJob, JobDescription description, Sandbox sandbox, String returnValueFile)
	    throws GATInvocationException {

	String jobid = null;

	/**
	 * Create the qsub script...
	 */

	String qsubFileName = createQsubScript(description, sandbox, returnValueFile);
	if (qsubFileName != null) {
	    jobid = sshPbsSubmission(PbsJob, description, qsubFileName,
		    sandbox);
	} else {
	    return (null);
	}

	return (jobid);
    }

    String createQsubScript(JobDescription description, Sandbox sandbox, String returnValueFile)
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
	    job.println("#!/bin/sh");
	    job.println("# qsub script automatically generated by GAT SshPbs adaptor");

	    // Resources: queue, walltime, memory size, et cetera.
	    Queue = (String) rd_HashMap.get("machine.queue");
	    if (Queue == null) {
		Queue = sd.getStringAttribute(
			SoftwareDescription.JOB_QUEUE, null);
	    }
	    if (Queue == null) {
		Queue = new String("");
	    } else {
		job.addOption("q", Queue);
	    }

	    Time = sd.getLongAttribute(SoftwareDescription.WALLTIME_MAX, -1L);

	    Filesize = (String) rd_HashMap.get("file.size");
	    if (Filesize == null) {
		Filesize = new String("");
	    }

	    Memsize = (Float) rd_HashMap.get(HardwareResourceDescription.MEMORY_SIZE);

	    String lString = null;

	    Nodes = (Integer) rd_HashMap
		    .get(HardwareResourceDescription.CPU_COUNT);
	    if (Nodes == null) {
		Nodes = description.getResourceCount();
	    }

	    if (Nodes > 1) {
		String jobType = getStringAttribute(description, SoftwareDescription.JOB_TYPE, "prun");
		job.addSgeOption("pe", jobType + " " + Nodes);
	    }

	    if (Time != -1L) {
		job.addSgeOption("l", "h_rt=" + (Time*60));
	    }

	    lString = "";
	    if (Time != -1L) {
		// Reformat time.
		int minutes = (int) (Time % 60);

		lString += "walltime=" + (Time / 60) + ":" + (minutes / 10) + (minutes % 10) + ":00,";
	    }
	    if (Filesize != null) {
		lString += "file=" + Filesize + ",";
	    }
	    if (Memsize != null) {
		lString += "mem=" + Memsize + ",";
	    }
	    lString += "nodes=" + Nodes;

	    job.addPbsOption("l", lString);

	    String nativeFlags = (String) gatContext.getPreferences().get(SSHPBS_NATIVE_FLAGS);
	    if (nativeFlags != null) {
		job.addString(nativeFlags);
	    }

	    // Set working dir for SGE. Note: this is the default for PBS.
	    job.addSgeOption("cwd", null);

	    // Name for the job.
	    HwArg = (String) rd_HashMap.get("Jobname");
	    if (HwArg == null) {
		HwArg = System.getProperty("user.name");
	    }

	    if (HwArg != null)
		job.addOption("N", HwArg);

	    // Combine stdout and stderr?
	    // Different for PBS and SGE: in PBS: "eo" means that both streams
	    // end up in stderr, "oe" means that both streams end up in stdout,
	    // "n" means separate streams. In SGE, "y" means that both streams
	    // end up in stdout, "n" means separate streams.
	    HwArg = (String) rd_HashMap.get("YankEO");
	    if (HwArg != null) {
		if (HwArg.equals("y")) {
		    job.addSgeOption("j", "y");
		    job.addPbsOption("j", "oe");
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

	    String sandboxDir = sandbox.getSandboxPath();
	    if (sandboxDir == null) {
		sandboxDir = "$HOME";
	    } else {
		java.io.File f = new java.io.File(sandbox.getSandboxPath());
		if (! f.isAbsolute()) {
		    sandboxDir = "$HOME/" + sandboxDir;
		}
	    }
	    
	    String directory = sd.getStringAttribute(
		    SoftwareDescription.DIRECTORY, null);
	    if (directory == null) {
		directory = sandboxDir;
	    }

	    // Set working directory of job.
	    job.println("cd " + directory);

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
	    cmd.append(protectAgainstShellMetas(sd.getExecutable().toString()));
	    if (sd.getArguments() != null) {
		String[] args = sd.getArguments();
		for (int i = 0; i < args.length; ++i) {
		    cmd.append(" ");
		    cmd.append(protectAgainstShellMetas(args[i]));
		}
	    }
	    job.println(cmd.toString());

	    job.println("echo \"retvalue = $?\" > " + sandboxDir + "/" + returnValueFile);

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

    /**
     * Submits a PBS job via ssh on a PBS host, and transfers the qsub file to
     * the PBS host.
     * 
     * @param qsubFileName
     * @return the job ID of the PBS job on the remote host
     */

    String sshPbsSubmission(SshPbsJob PbsJob, JobDescription description,
	    String qsubFileName, Sandbox sandbox) throws GATInvocationException {

	String username = securityInfo.get("username");
	// String password = securityInfo.get("password");

	String host = getHostname();
	if (host == null) {
	    host = "localhost";
	}

	ArrayList<String> command = new ArrayList<String>();
	ArrayList<String> scpCommand = new ArrayList<String>();

	
	try {
	    File qsubFile = GAT.createFile(gatContext, qsubFileName);
	    // sd.addPreStagedFile(qsubFile);

	    // prestaging by sandbox, the qsubFile will be prestaged separately by scp.
	    sandbox.prestage();

	    scpCommand.add("/usr/bin/scp");
	    if (ssh_port != SSH_PORT) {
		scpCommand.add("-p");
		scpCommand.add("" + ssh_port);
	    }
	    scpCommand.add(qsubFileName);
	    if (sandbox.getSandboxPath() != null) {
		scpCommand.add(username + "@" + host + ":"
			+ sandbox.getSandboxPath().toString() + "/"
			+ qsubFile.getName().toString());
	    } else {
		scpCommand.add(username + "@" + host + ":"
			+ qsubFile.getName().toString());
	    }

	    String ScpRes[] = SshPbsJob.singleResult(scpCommand);

	    /**
	     * and create the ssh command for qsub...
	     */

	    command.add("/usr/bin/ssh");
	    if (ssh_port != SSH_PORT) {
		command.add("-p");
		command.add("" + ssh_port);
	    }
	    command.add("-o");
	    command.add("BatchMode=yes");
	    // command.add("-t");
	    // command.add("-t");
	    command.add(username + "@" + host);
	    if (sandbox.getSandboxPath() != null) {
		command.add("cd");
		command.add(sandbox.getSandboxPath());
		command.add("&&");
	    }
	    command.add("qsub");

	    command.add(qsubFile.getName());

	    String[] PbsJobID = SshPbsJob.singleResult(command);
	    String result = PbsJobID[0];
	    // Check for SGE qsub result ...
	    if (result.startsWith("Your job ")) {
		result = result.split(" ")[2];
	    }
	    
	    try {
		qsubFile.delete();
	    } catch(Throwable e) {
		logger.debug("Could not remove qsub script");
	    }

	    return result;

	} catch (GATObjectCreationException e) {
	    throw new GATInvocationException("Could not create qsub script", e);
	} catch (IOException e) {
	    throw new GATInvocationException("Got IOException", e);
	}
    }
    
    // Protect against special characters for (most) unix shells.
    private static String protectAgainstShellMetas(String s) {
        char[] chars = s.toCharArray();
        StringBuffer b = new StringBuffer();
        b.append('\'');
        for (char c : chars) {
            if (c == '\'') {
                b.append('\'');
                b.append('\\');
                b.append('\'');
            }
            b.append(c);
        }
        b.append('\'');
        return b.toString();
    }
}
