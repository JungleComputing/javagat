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
import java.io.PrintWriter;

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
        SoftwareDescription sd = description.getSoftwareDescription();
	
	int nproc = description.getProcessCount();
	
        if (sd.streamingStderrEnabled() || sd.streamingStdinEnabled() || sd.streamingStdoutEnabled()) {
            throw new GATInvocationException("Streaming I/O not supported by SshPbs adaptor");
        }

	String authority = getAuthority();
	if (authority == null) {
	    authority = "localhost";
	}
	
	// Create filename for return value.
	String returnValueFile = ".rc." + Math.random();
        java.io.File jobScriptFile = createJobScript(description);
        java.io.File jobStarterFile = null;
        if (nproc > 1) {
            jobStarterFile = createJobStarter(description, nproc, jobScriptFile);
        }
        java.io.File qsubFile = createQsubScript(description, returnValueFile, jobStarterFile, jobScriptFile, nproc);
	
	try {
	    sd.addPostStagedFile(GAT.createFile(gatContext, new URI(returnValueFile)));
            sd.addPreStagedFile(GAT.createFile(gatContext, new URI(qsubFile.getAbsolutePath())));
            sd.addPreStagedFile(GAT.createFile(gatContext, new URI(jobScriptFile.getAbsolutePath())));
            if (jobStarterFile != null) {
                sd.addPreStagedFile(GAT.createFile(gatContext, new URI(jobStarterFile.getAbsolutePath())));
            }
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

	String jobid = sshPbsSubmission(sshPbsJob, description, qsubFile, sandbox, jobStarterFile, jobScriptFile);

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

    java.io.File createQsubScript(JobDescription description, String returnValueFile, java.io.File jobStarterFile, java.io.File jobScriptFile, int nproc)
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
	    String userScript = (String) gatContext.getPreferences().get(SSHPBS_SCRIPT);
	    if (userScript != null) {
		// a specified job script overrides everything, except for pre-staging, post-staging,
		// and exit status.
		BufferedReader f = new BufferedReader(new FileReader(userScript));
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
		    job.addOption("o", sd.getStdout().getName());
		}

		if (sd.getStderr() != null) {
		    job.addOption("e", sd.getStderr().getName());
		}

                job.println("trap 'echo retvalue = 1 > " + returnValueFile + " && exit 1' 1 2 3 15");
                if (jobStarterFile != null) {
                    job.println("/bin/sh " + jobStarterFile.getName() + " < /dev/null > /dev/null 2>&1 &");
                }
                job.println("/bin/sh " + jobScriptFile.getName() + "< " + (sd.getStdin() != null ? sd.getStdin() : "/dev/null"));
	    }
            job.println("echo retvalue = $? > " + returnValueFile);
            if (userScript == null && jobStarterFile != null) {
                job.println("while [ ! -e .gat_script." + (nproc-1) + " ] ; do sleep 1 ; done");
                job.println("for job in .gat_script.* ; do");
                job.println("  jobno=`expr $job : '.gat_script.\\(.*\\)'`");
                job.println("  while [ ! -e .gat_done.$jobno ] ; do");
                job.println("    sleep 2");
                job.println("  done");
                job.println("  cat .out.$jobno 2>/dev/null");
                job.println("  cat .err.$jobno 1>&2");
                job.println("done");
            }
	} catch (Throwable e) {
	    throw new GATInvocationException(
		    "Cannot create temporary qsub file "
			    + temp.getAbsolutePath(), e);
	} finally {
	    if (job != null)
		job.close();
	}
	return temp;
    }

    java.io.File createJobStarter(JobDescription description, int nproc, java.io.File jobScript)
        throws GATInvocationException {

        java.io.File temp;
        
        SoftwareDescription sd = description.getSoftwareDescription();

        try {
            temp = java.io.File.createTempFile("sge", null);
        } catch (IOException e) {
            throw new GATInvocationException("Cannot create file", e);
        }
        PrintWriter job = null;
        try {
            job = new PrintWriter(new BufferedWriter(new FileWriter(temp)));
            
            job.println("#!/bin/sh");
            job.println("# Job starter script.");
            job.println("# The jobs are distributed over the available nodes in round-robin fashion.");
            job.println("GAT_MYDIR=`pwd`");
            job.println("GAT_HOSTS=`cat $PBS_NODEFILE`");
            job.println("GAT_JOBNO=1");
            job.println("GAT_JOBS=" + nproc);
            job.println("set $GAT_HOSTS");
            job.println("shift");
            job.println("while :");
            job.println("do");
            job.println("  for GAT_HOST in \"$@\"");
            job.println("  do");
            job.println("    echo #!/bin/sh > .gat_script.$GAT_JOBNO");
            job.println("    echo cd $GAT_MYDIR >> .gat_script.$GAT_JOBNO");
            job.println("    echo trap \\\"touch .gat_done.$GAT_JOBNO\\\" 0 1 2 3 15 >> .gat_script.$GAT_JOBNO");
            job.println("    cat " + jobScript.getName() + " >> .gat_script.$GAT_JOBNO");
            job.println("    chmod +x .gat_script.$GAT_JOBNO");
            job.print(  "    ssh $GAT_HOST \"$GAT_MYDIR/.gat_script.$GAT_JOBNO");
            if (sd.getStdin() != null) {
                job.print(" < $GAT_MYDIR/" + sd.getStdin().getName());
            } else {
                job.print(" < /dev/null");
            }
            job.println(" > $GAT_MYDIR/.out.$GAT_JOBNO 2>$GAT_MYDIR/.err.$GAT_JOBNO &\"");
            job.println("    GAT_JOBNO=`expr $GAT_JOBNO + 1`");
            job.println("    if expr $GAT_JOBNO \\>= $GAT_JOBS > /dev/null ; then break 2 ; fi");
            job.println("  done");
            job.println("  set $GAT_HOSTS");
            job.println("done");
        } catch (Throwable e) {
            throw new GATInvocationException(
                    "Cannot create temporary job starter file "
                            + temp.getAbsolutePath(), e);
        } finally {
            if (job != null)
                job.close();
        }
        return temp;
    }
 
    java.io.File createJobScript(JobDescription description)
            throws GATInvocationException {
 
        java.io.File temp;
        
        SoftwareDescription sd = description.getSoftwareDescription();

        try {
            temp = java.io.File.createTempFile("sge", null);
        } catch (IOException e) {
            throw new GATInvocationException("Cannot create file", e);
        }
        PrintWriter job = null;
        try {
            job = new PrintWriter(new BufferedWriter(new FileWriter(temp)));
            
            job.println("#!/bin/sh");
            job.println("# job script");
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
            job.println("exit $?");
        } catch(Throwable e) {
            throw new GATInvocationException(
                    "Cannot create temporary job script file "
                        + temp.getAbsolutePath(), e);
        } finally {
            if (job != null) {
                job.close();
            }
        }
        return temp;
    }

    /**
     * Submits a PBS job via ssh on a PBS host, and transfers the qsub file to
     * the PBS host.
     * @return the job ID of the PBS job on the remote host
     */

    private String sshPbsSubmission(SshPbsJob PbsJob, JobDescription description,
            java.io.File qsubFile, Sandbox sandbox, java.io.File starter, java.io.File script) throws GATInvocationException {

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
            script.delete();
            if (starter != null) {
                starter.delete();
            }
	}
    }
}
