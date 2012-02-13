package org.gridlab.gat.resources.cpi.sshsge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.lang.StringBuffer;

import java.net.URISyntaxException;

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
import org.gridlab.gat.engine.util.SshHelper;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperJobCpi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshSgeResourceBrokerAdaptor extends ResourceBrokerCpi implements MetricListener {

    protected static Logger logger = LoggerFactory
	    .getLogger(SshSgeResourceBrokerAdaptor.class);

    public static Map<String, Boolean> getSupportedCapabilities() {
	Map<String, Boolean> capabilities = ResourceBrokerCpi
		.getSupportedCapabilities();
	capabilities.put("submitJob", true);

	return capabilities;
    }

    static final String SSHSGE_NATIVE_FLAGS = "sshsge.native.flags";
    static final String SSHSGE_SCRIPT = "sshsge.script";
    static final String SSHSGE_SUBMITTER_SCHEME = "sshsge.submitter.scheme";

    public static String[] getSupportedSchemes() {
	return new String[] { "sshsge"};
    }
    
    public static Preferences getSupportedPreferences() {
        Preferences p = ResourceBrokerCpi.getSupportedPreferences();
        p.put(SSHSGE_NATIVE_FLAGS, "");
        p.put(SSHSGE_SCRIPT, "");
        p.put(SSHSGE_SUBMITTER_SCHEME, "ssh");
        return p;
    }

    public static void init() {
	GATEngine.registerUnmarshaller(SshSgeJob.class);
    }
    
    static GATContext getSubContext(GATContext context) {
        // Create a gatContext that can be used to submit qsub, qstat, etc commands.
        Preferences p = context.getPreferences();
        Preferences prefs = new Preferences();
        if (p != null) {
            prefs.putAll(p);
        }
        prefs.remove("resourcebroker.adaptor.name");
        prefs.remove("sshtrilead.stoppable");
        GATContext subContext = (GATContext) context.clone();
        subContext.removePreferences();
        subContext.addPreferences(prefs);
        return subContext;
    }
    
    static ResourceBroker subResourceBroker(GATContext context, URI broker) throws URISyntaxException, GATObjectCreationException {
        String subScheme;
        if (context.getPreferences() != null) {
            subScheme = (String) context.getPreferences().get(SSHSGE_SUBMITTER_SCHEME, "ssh");
        } else {
            subScheme = "ssh";
        }
        URI subBroker = broker.setScheme(subScheme);
        return GAT.createResourceBroker(context, subBroker);
    }
    
    private final ResourceBroker subBroker;
    private final GATContext subContext;
    
    public SshSgeResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
	    throws GATObjectCreationException, AdaptorNotApplicableException {

	super(gatContext, brokerURI);
	subContext = getSubContext(gatContext);
	try {
	    subBroker = subResourceBroker(subContext, brokerURI);
	} catch (Throwable e) {
	    throw new GATObjectCreationException("Could not create broker to submit SGE jobs", e);
	}
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
	    throw new GATInvocationException("Streaming I/O not supported by SshSge adaptor");
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

	SshSgeJob sshSgeJob = new SshSgeJob(gatContext, brokerURI, description, sandbox,
		subBroker, returnValueFile);
	
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

	String jobid = sshSgeSubmission(sshSgeJob, description, qsubFile, sandbox, jobStarterFile, jobScriptFile);

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

    java.io.File createQsubScript(JobDescription description, String returnValueFile, java.io.File jobStarterFile, java.io.File jobScriptFile,
	    int nproc)
	    throws GATInvocationException {

	String Queue = null;
	long Time = -1;
	Integer cpus = null;
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
	    String userScript = (String) gatContext.getPreferences().get(SSHSGE_SCRIPT);
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

		cpus = (Integer) rd_HashMap
			.get(HardwareResourceDescription.CPU_COUNT);
		if (cpus == null) {
		    cpus = description.getResourceCount();
		}

		if (cpus > 1 || nproc > 1) {
		    String jobType = getStringAttribute(description, SoftwareDescription.JOB_TYPE, "javagat");
		    job.addOption("pe", jobType + " " + cpus);
		}

		if (Time != -1L) {
		    job.addOption("l", "h_rt=" + (Time*60));
		}

		job.addOption("S", "/bin/sh");

		String nativeFlags = null;
		Object o = rd == null ? null : rd.getResourceAttribute(SSHSGE_NATIVE_FLAGS);
		if (o != null && o instanceof String) {
		    nativeFlags = (String) o;
		} else {
		    String s = sd == null ? null : sd.getStringAttribute(SSHSGE_NATIVE_FLAGS, null);
		    if (s != null) {
			    nativeFlags = s;
		    } else {
			o = gatContext.getPreferences().get(SSHSGE_NATIVE_FLAGS);
			if (o != null && o instanceof String) {
			    nativeFlags = (String) o;
			}
		    }
		}
		
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
		    job.addOption("o", sd.getStdout().getName());
		}

		if (sd.getStderr() != null) {
		    job.addOption("e", sd.getStderr().getName());
		}
		
		if (sd.getStdin() != null) {
		    job.addOption("i", sd.getStdin().getName());
		}
		
		job.println("trap 'echo retvalue = 1 > " + returnValueFile + " && exit 1' 1 2 3 15");
		if (jobStarterFile != null) {
		    job.println("/bin/sh " + jobStarterFile.getName() + " < /dev/null > /dev/null 2>&1 &");
		}
		job.println("/bin/sh " + jobScriptFile.getName());
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
	    job.println("case X$PE_HOSTFILE in");
	    job.println("X)  GAT_HOSTS=$HOSTNAME");
	    job.println("    ;;");
	    job.println("*)  GAT_HOSTS=`cat $PE_HOSTFILE | sed 's/ .*//'`");
	    job.println("    ;;");
	    job.println("esac");
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
	    
	    // Support DIRECTORY
	    String dir = sd.getStringAttribute(SoftwareDescription.DIRECTORY, null);
	    if (dir != null) {
	        job.println("cd " + SshHelper.protectAgainstShellMetas(dir));
	    }
	    
	    // Support environment.
	    Map<String, Object> env = sd.getEnvironment();
	    if (env != null) {
		Set<String> s = env.keySet();
		Object[] keys = s.toArray();

		for (int i = 0; i < keys.length; i++) {
		    String val = (String) env.get(keys[i]);
		    job.println(keys[i] + "=" + SshHelper.protectAgainstShellMetas(val) + " && export " + keys[i]);
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
	} catch (Throwable e) {
	    throw new GATInvocationException(
		    "Cannot create temporary job script file "
			    + temp.getAbsolutePath(), e);
	} finally {
	    if (job != null)
		job.close();
	}
	return temp;
    }


    private String sshSgeSubmission(SshSgeJob SgeJob, JobDescription description,
	    java.io.File qsubFile, Sandbox sandbox, java.io.File starter, java.io.File script) throws GATInvocationException {

	String host = getHostname();
	if (host == null) {
	    host = "localhost";
	}

	java.io.File qsubResultFile = null;

	try {
	    sandbox.prestage();
	    
	    // Create qsub job
	    SoftwareDescription sd = new SoftwareDescription();
	    sd.setExecutable("qsub");
	    sd.setArguments(qsubFile.getName());
	    sd.addAttribute(SoftwareDescription.SANDBOX_USEROOT, "true");
	    qsubResultFile = java.io.File.createTempFile("GAT", "tmp");
	    try {
	        sd.setStdout(GAT.createFile(subContext, qsubResultFile.getAbsolutePath()));
	    } catch (GATObjectCreationException e1) {
	        try {
	            sandbox.removeSandboxDir();
	        } catch(Throwable e) {
	            // ignore
	        }
	        throw new GATInvocationException("Could not create GAT object for temporary " + qsubResultFile.getAbsolutePath(), e1);
	    }
	    sd.addAttribute(SoftwareDescription.DIRECTORY, sandbox.getSandboxPath());
	    JobDescription jd = new JobDescription(sd);
	    if (logger.isDebugEnabled()) {
	        logger.debug("Submitting qsub job: " + sd);
	    }
	    Job job = subBroker.submitJob(jd, this, "job.status");
	    synchronized(job) {
	        while (job.getState() != Job.JobState.STOPPED && job.getState() != Job.JobState.SUBMISSION_ERROR) {
	            try {
	                job.wait();
	            } catch(InterruptedException e) {
	                // ignore
	            }
	        }
	    }
	    if (job.getState() != Job.JobState.STOPPED || job.getExitStatus() != 0) {
	        try {
	            sandbox.removeSandboxDir();
	        } catch(Throwable e) {
	            // ignore
	        }
	        logger.debug("jobState = " + job.getState() + ", exit status = " + job.getExitStatus());
	        throw new GATInvocationException("Could not submit qsub job");
	    }
	    
	    // submit success.
            BufferedReader in = new BufferedReader(new FileReader(qsubResultFile.getAbsolutePath()));
            String result = in.readLine();
            if (logger.isDebugEnabled()) {
                logger.debug("qsub result line = " + result);
            }

	    // Check for SGE qsub result ...
	    if (result.startsWith("Your job ")) {
		result = result.split(" ")[2];
	    }

	    return result;
	} catch (IOException e) {
	    try {
	        sandbox.removeSandboxDir();
	    } catch(Throwable e1) {
	        // ignore
	    }
	    throw new GATInvocationException("Got IOException", e);
	} finally {
	    qsubResultFile.delete();
	    qsubFile.delete();
	    script.delete();
	    if (starter != null) {
		starter.delete();
	    }
	}
    }

    @Override
    public void processMetricEvent(MetricEvent event) {
        if (event.getValue().equals(Job.JobState.STOPPED) || event.getValue().equals(Job.JobState.SUBMISSION_ERROR)) {
            synchronized(event.getSource()) {
                event.getSource().notifyAll();
            }
        }
    }
}
