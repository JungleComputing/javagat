package org.gridlab.gat.resources.cpi.sshslurm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
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
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperJobCpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshSlurmResourceBrokerAdaptor extends ResourceBrokerCpi implements
        MetricListener {

    protected static Logger logger = LoggerFactory
            .getLogger(SshSlurmResourceBrokerAdaptor.class);

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("submitJob", true);

        return capabilities;
    }

    static final String SSHSLURM_NATIVE_FLAGS = "sshslurm.native.flags";
    static final String SSHSLURM_SCRIPT = "sshslurm.script";
    static final String SSHSLURM_SUBMITTER_SCHEME = "sshslurm.submitter.scheme";

    public static String[] getSupportedSchemes() {
        return new String[] { "sshslurm" };
    }

    public static Preferences getSupportedPreferences() {
        Preferences p = ResourceBrokerCpi.getSupportedPreferences();
        p.put(SSHSLURM_NATIVE_FLAGS, "");
        p.put(SSHSLURM_SCRIPT, "");
        p.put(SSHSLURM_SUBMITTER_SCHEME, "ssh");
        return p;
    }

    public static void init() {
        GATEngine.registerUnmarshaller(SshSlurmJob.class);
    }

    static GATContext getSubContext(GATContext context) {
        // Create a gatContext that can be used to submit sbatch, squeue, etc
        // commands.
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

    static ResourceBroker subResourceBroker(GATContext context, URI broker)
            throws URISyntaxException, GATObjectCreationException {
        String subScheme;
        if (context.getPreferences() != null) {
            subScheme = (String) context.getPreferences().get(
                    SSHSLURM_SUBMITTER_SCHEME, "ssh");
        } else {
            subScheme = "ssh";
        }
        URI subBroker = broker.setScheme(subScheme);
        return GAT.createResourceBroker(context, subBroker);
    }

    private final ResourceBroker subBroker;
    private final GATContext subContext;

    public SshSlurmResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws GATObjectCreationException, AdaptorNotApplicableException {

        super(gatContext, brokerURI);
        subContext = getSubContext(gatContext);
        try {
            subBroker = subResourceBroker(subContext, brokerURI);
        } catch (Throwable e) {
            throw new GATObjectCreationException(
                    "Could not create broker to submit Slurm jobs", e);
        }
	// Detect if subBroker can actually submit Slurm jobs.
	// Check if squeue command exists?
	// So, execute "which squeue" and check exit status, should be 0.
	SoftwareDescription sd = new SoftwareDescription();
	sd.setExecutable("which");
	sd.setArguments("squeue");

	if (logger.isDebugEnabled()) {
	    logger.debug("Submitting test job: " + sd);
	}
	JobDescription jd = new JobDescription(sd);
	Job job;
	try {
	    job = subBroker.submitJob(jd, this, "job.status");
	} catch (Throwable e) {
	    throw new GATObjectCreationException("broker to submit Slurm jobs cannot submit test job", e);
	}
	synchronized(job) {
	    while (job.getState() != Job.JobState.STOPPED && job.getState() != Job.JobState.SUBMISSION_ERROR) {
		try {
		    job.wait();
		} catch(InterruptedException e) {
		    // ignore
		}
	    }
	}
	try {
	    if (job.getExitStatus() != 0) {
	        throw new GATObjectCreationException("broker to submit Slurm jobs could not find Slurm command");
	    }
	} catch (GATInvocationException e) {
	    throw new GATObjectCreationException("broker to submit Slurm jobs could not get exit status", e);
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

	JobDescription description = (JobDescription) ((JobDescription) abstractDescription).clone();
        SoftwareDescription sd = description.getSoftwareDescription();

        int nproc = description.getProcessCount();

        if (sd.streamingStderrEnabled() || sd.streamingStdinEnabled()
                || sd.streamingStdoutEnabled()) {
            throw new GATInvocationException(
                    "Streaming I/O not supported by SshSlurm adaptor");
        }

        String authority = getAuthority();
        if (authority == null) {
            authority = "localhost";
        }

        // Create filename for return value.
        String returnValueFile = ".rc." + Math.random();

        java.io.File sbatchFile = createSbatchScript(description,
                returnValueFile, nproc);

        try {
            // Careful, you may be running on windows, so add a scheme.
            sd.addPostStagedFile(GAT.createFile(gatContext, new URI(
                    returnValueFile)));
            sd.addPreStagedFile(GAT.createFile(
                    gatContext,
                    new URI("file:///"
                            + sbatchFile.getAbsolutePath().replace(
                                    File.separatorChar, '/'))));
        } catch (Throwable e) {
            throw new GATInvocationException("Error in file staging", e);
        }

        Sandbox sandbox = new Sandbox(gatContext, description, authority, null,
                true, true, true, true);

        SshSlurmJob sshSlurmJob = new SshSlurmJob(gatContext, brokerURI,
                description, sandbox, subBroker, returnValueFile);

        Job job = null;
        if (description instanceof WrapperJobDescription) {
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, sshSlurmJob,
                    listener, metricDefinitionName);
            listener = tmp;
            job = tmp;
        } else {
            job = sshSlurmJob;
        }
        if (listener != null && metricDefinitionName != null) {
            Metric metric = sshSlurmJob.getMetricDefinitionByName(
                    metricDefinitionName).createMetric(null);
            sshSlurmJob.addMetricListener(listener, metric);
        }

        String jobid = sshSlurmSubmission(sshSlurmJob, description, sbatchFile,
                sandbox);

        if (jobid != null) {
            sshSlurmJob.setState(Job.JobState.SCHEDULED);
            sshSlurmJob.setSoft(description.getSoftwareDescription());
            sshSlurmJob.setJobID(jobid);
            sshSlurmJob.startListener();
        } else {
            sandbox.removeSandboxDir();
            throw new GATInvocationException("Could not submit sshSlurm job");
        }

        return job;
    }

    java.io.File createSbatchScript(JobDescription description,
            String returnValueFile, int nproc)
            throws GATInvocationException {

        String Queue = null;
        long Time = -1;
        Integer nodes = null;
        String HwArg = null;
        java.io.File temp;
        SlurmScriptWriter job = null;
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
            temp = java.io.File.createTempFile("slurm", null);
        } catch (IOException e) {
            throw new GATInvocationException(
                    "Cannot create temporary slurm file");
        }

        try {
            job = new SlurmScriptWriter(
                    new BufferedWriter(new FileWriter(temp)));
            String userScript = (String) gatContext.getPreferences().get(
                    SSHSLURM_SCRIPT);
            if (userScript != null) {
                // a specified job script overrides everything, except for
                // pre-staging, post-staging,
                // and exit status.
                BufferedReader f = new BufferedReader(
                        new FileReader(userScript));
                for (;;) {
                    String s = f.readLine();
                    if (s == null) {
                        break;
                    }
                    job.print(s + "\n");
                }
            } else {
                job.print("#!/bin/sh\n");
                job.print("# sbatch script automatically generated by GAT SshSlurm adaptor\n");

                // Resources: queue, walltime, memory size, et cetera.
                Queue = (String) rd_HashMap.get("machine.queue");
                if (Queue == null) {
                    Queue = sd.getStringAttribute(
                            SoftwareDescription.JOB_QUEUE, null);
                }
                if (Queue != null) {
                    job.addOption("partition", Queue);
                }

                Time = sd.getLongAttribute(SoftwareDescription.WALLTIME_MAX,
                        -1L);

                // TODO wait, what?
                nodes = (Integer) rd_HashMap
                        .get(HardwareResourceDescription.CPU_COUNT);
                if (nodes == null) {
                    nodes = description.getResourceCount();
                }

                job.addOption("nodes", nodes);
                job.addOption("ntasks", nproc);
                
                // force exclusive node reservation?
//                job.addOption("exclusive", null);
                
                // help slurm in distributing tasks over nodes evenly
                int taskPerNode= nproc/nodes;
                if(nproc%nodes > 0) {
                    taskPerNode++;
                }
                job.addOption("tasks-per-node", taskPerNode);

                if (Time != -1L) {
                    job.addOption("time", String.valueOf(Time));
                }

                String nativeFlags = null;
                Object o = rd == null ? null : rd
                        .getResourceAttribute(SSHSLURM_NATIVE_FLAGS);
                if (o != null && o instanceof String) {
                    nativeFlags = (String) o;
                } else {
                    String s = sd == null ? null : sd.getStringAttribute(
                            SSHSLURM_NATIVE_FLAGS, null);
                    if (s != null) {
                        nativeFlags = s;
                    } else {
                        o = gatContext.getPreferences().get(
                                SSHSLURM_NATIVE_FLAGS);
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
                // job.addOption("cwd", null);

                // Name for the job.
                HwArg = (String) rd_HashMap.get("Jobname");
                if (HwArg == null) {
                    HwArg = brokerURI.getUserInfo();
                    if (HwArg == null || "".equals(HwArg)) {
                        HwArg = System.getProperty("user.name");
                    }
                }

                if (HwArg != null)
                    job.addOption("job-name", HwArg);

                // TODO how to solve in Slurm?
                // Combine stdout and stderr?
                // In SGE, "y" means that both streams
                // end up in stdout, "n" means separate streams.
                // HwArg = (String) rd_HashMap.get("YankEO");
                // if (HwArg != null) {
                // if (HwArg.equals("y")) {
                // job.addOption("j", "y");
                // } else {
                // job.addOption("j", HwArg);
                // }
                // }

                // Files for stdout and stderr.
                String out = "";
                if (sd.getStdout() != null) {
                    // job.addOption("output", sd.getStdout().getName());
                    // No, add the option to srun, otherwise we may get output from srun itself. --Ceriel
                    out = "-o " + sd.getStdout().getName() + " ";
                }

                String err = "";
                if (sd.getStderr() != null) {
                    // job.addOption("error", sd.getStderr().getName());
                    // No, add the option to srun, otherwise we may get error output from srun itself. --Ceriel
                    err = "-e " + sd.getStderr().getName() + " ";
                }

                if (sd.getStdin() != null) {
                    job.addOption("input", sd.getStdin().getName());
                }

                // Support environment.
                Map<String, Object> env = sd.getEnvironment();
                if (env != null) {
                    Set<String> s = env.keySet();
                    Object[] keys = s.toArray();

                    for (int i = 0; i < keys.length; i++) {
                        String val = (String) env.get(keys[i]);
                        job.print(keys[i] + "="
                                + SshHelper.protectAgainstShellMetas(val)
                                + " && export " + keys[i] + "\n");
                    }
                }
                
                job.print("trap 'echo retvalue = 1 > " + returnValueFile
                        + " && exit 1' 1 2 3 15\n");
                job.print("srun " + out + err + createSrunCommand(description) + "\n");
            }

            job.print("echo retvalue = $? > " + returnValueFile + "\n");
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

    private String sshSlurmSubmission(SshSlurmJob slurmJob,
            JobDescription description, java.io.File sbatchFile,
            Sandbox sandbox)
            throws GATInvocationException {

        String host = getHostname();
        if (host == null) {
            host = "localhost";
        }

        java.io.File slurmResultFile = null;

        try {
            sandbox.prestage();

            // Create sbatch job
            SoftwareDescription sd = new SoftwareDescription();
            sd.setExecutable("sbatch");
            sd.setArguments("-Q", sbatchFile.getName());
            sd.addAttribute(SoftwareDescription.SANDBOX_USEROOT, "true");
            slurmResultFile = java.io.File.createTempFile("GAT", "tmp");
            try {
                sd.setStdout(GAT.createFile(
                        subContext,
                        new URI("file:///"
                                + slurmResultFile.getAbsolutePath().replace(
                                        File.separatorChar, '/'))));
            } catch (Throwable e1) {
                try {
                    sandbox.removeSandboxDir();
                } catch (Throwable e) {
                    // ignore
                }
                throw new GATInvocationException(
                        "Could not create GAT object for temporary "
                                + slurmResultFile.getAbsolutePath(), e1);
            }
            sd.addAttribute(SoftwareDescription.DIRECTORY,
                    sandbox.getSandboxPath());
            JobDescription jd = new JobDescription(sd);
            if (logger.isDebugEnabled()) {
                logger.debug("Submitting slurm job: " + sd);
            }
            Job job = subBroker.submitJob(jd, this, "job.status");
            synchronized (job) {
                while (job.getState() != Job.JobState.STOPPED
                        && job.getState() != Job.JobState.SUBMISSION_ERROR) {
                    try {
                        job.wait();
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
            if (job.getState() != Job.JobState.STOPPED
                    || job.getExitStatus() != 0) {
                try {
                    sandbox.removeSandboxDir();
                } catch (Throwable e) {
                    // ignore
                }
                logger.debug("jobState = " + job.getState()
                        + ", exit status = " + job.getExitStatus());
                throw new GATInvocationException("Could not submit slurm job");
            }

            // submit success.
            BufferedReader in = new BufferedReader(new FileReader(
                    slurmResultFile.getAbsolutePath()));
            String result = in.readLine();
            if (logger.isDebugEnabled()) {
                logger.debug("sbatch result line = " + result);
            }

            // Check for Slurm sbatch result ...
            if (result.startsWith("Submitted batch job ")) {
                result = result.split(" ")[3];
            }

            return result;
        } catch (IOException e) {
            try {
                sandbox.removeSandboxDir();
            } catch (Throwable e1) {
                // ignore
            }
            throw new GATInvocationException("Got IOException", e);
        } finally {
            slurmResultFile.delete();
            sbatchFile.delete();
        }
    }

    @Override
    public void processMetricEvent(MetricEvent event) {
        if (event.getValue().equals(Job.JobState.STOPPED)
                || event.getValue().equals(Job.JobState.SUBMISSION_ERROR)) {
            synchronized (event.getSource()) {
                event.getSource().notifyAll();
            }
        }
    }
  
    String createSrunCommand(JobDescription description) {
        // Construct command.
        StringBuffer cmd = new StringBuffer();
        SoftwareDescription sd = description.getSoftwareDescription();
        cmd.append(SshHelper.protectAgainstShellMetas(sd.getExecutable()
                .toString()));
        if (sd.getArguments() != null) {
            String[] args = sd.getArguments();
            for (int i = 0; i < args.length; ++i) {
                cmd.append(" ");
                cmd.append(SshHelper.protectAgainstShellMetas(args[i]));
            }
        }
        return cmd.toString();
    }
}
