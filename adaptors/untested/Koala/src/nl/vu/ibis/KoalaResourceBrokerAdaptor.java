package nl.vu.ibis;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;

import nl.vu.ibis.KoalaJob;

import org.apache.log4j.PropertyConfigurator;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.PostStagedFile;
import org.gridlab.gat.resources.cpi.PostStagedFileSet;
import org.gridlab.gat.resources.cpi.PreStagedFile;
import org.gridlab.gat.resources.cpi.PreStagedFileSet;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

public class KoalaResourceBrokerAdaptor extends ResourceBrokerCpi {

    // The location of the Koala scheduler.
    private static InetSocketAddress schedulerAddress;

    // The default scheduling policy.
    private static String policy;

    // Is the configuration loaded ?
    private static boolean loadedConfiguration = false;

    private static boolean readKoalaConfiguration() {

        if (loadedConfiguration) {
            return true;
        }

        try {
            if (!Assist.readConfigFile("KOALA_STATUS").equals("STATUS_OK")) {
                return false;
            }

            PropertyConfigurator.configure(org.koala.common.Globals.KOALA_DIR
                    + "/etc/koala.log4j.properties");

            schedulerAddress = new InetSocketAddress(Assist
                    .readConfigFile("SCHEDULER_SITE"), Integer.parseInt(Assist
                    .readConfigFile("SCHED_PORT_LISTEN")));

            policy = Assist.readConfigFile("DPLACEMENT_POLICY");

        } catch (IOException e) {
            return false;
        }

        loadedConfiguration = true;

        return true;
    }

    public KoalaResourceBrokerAdaptor(GATContext context, URI brokerURI)
            throws GATObjectCreationException {
        super(context, brokerURI);

        // Prevent recursively using this resourcebroker by checking for a
        // magic preference.
        if (gatContext.getPreferences().containsKey("postKoala")) {
            throw new GATObjectCreationException("Preventing recursive call "
                    + "into the KoalaResourceBroker");
        }

        // Attempt to read the system wide Koala configuration.
        if (!readKoalaConfiguration()) {
            throw new GATObjectCreationException("Failed to load "
                    + "koala configuration!");
        }
    }

    // NOTE: This is based on the globus resource broker. The main difference is
    // that koala needs some annotations in the RSL which indicate the
    // file size. The sandbox support has also been removed, since we
    // don't actually submit this RSL anyway.
    //
    // TODO: Support jobs consisting of multiple components
    protected String createRSL(JobDescription description, String host,
            Sandbox sandbox, PreStagedFileSet pre, PostStagedFileSet post)
            throws GATInvocationException {

        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        String rsl = "";
        String args = "";

        String exe = getExecutable(description);
        rsl += "& (executable = " + exe + ")";

        // parse the arguments
        String[] argsA = getArgumentsArray(description);

        if (argsA != null) {
            for (int i = 0; i < argsA.length; i++) {
                args += (" \"" + argsA[i] + "\" ");
            }
        }
        if (args.length() != 0) {
            rsl += (" (arguments = " + args + ")");
        }

        rsl += " (count = " + getCPUCount(description) + ")";

        rsl += " (hostCount = " + getHostCount(description) + ")";

        String jobType = getStringAttribute(description, "job.type", null);
        if (jobType != null) {
            rsl += " (jobType = " + jobType + ")";
        }

        if (sandbox != null) {
            rsl += " (directory = " + sandbox.getSandbox() + ")";
        }

        long maxTime = getLongAttribute(description, "time.max", -1);
        if (maxTime > 0) {
            rsl += " (maxTime = " + maxTime + ")";
        }

        long maxWallTime = getLongAttribute(description, "walltime.max", -1);
        if (maxWallTime > 0) {
            rsl += " (maxWallTime = " + maxWallTime + ")";
        }

        long maxCPUTime = getLongAttribute(description, "cputime.max", -1);
        if (maxCPUTime > 0) {
            rsl += " (maxCPUTime = " + maxCPUTime + ")";
        }

        // stage in files with gram
        if (pre != null) {
            for (int i = 0; i < pre.size(); i++) {
                PreStagedFile f = pre.getFile(i);

                if (!f.getResolvedSrc().toGATURI().refersToLocalHost()) {
                    throw new GATInvocationException(
                            "Currently, we cannot stage in remote files with gram");
                }

                /*
                 * String s = "(file_stage_in = (file:///" +
                 * f.getResolvedSrc().getPath() + " " +
                 * f.getResolvedDest().getPath() + "))";
                 */

                // Add file size here ....
                String s = "(file_stage_in = (file:///"
                        + f.getResolvedSrc().getPath() + ":1))";
                rsl += s;
            }
        }

        if (post != null) {
            for (int i = 0; i < post.size(); i++) {
                PostStagedFile f = post.getFile(i);

                if (!f.getResolvedDest().toGATURI().refersToLocalHost()) {
                    throw new GATInvocationException(
                            "Currently, we cannot stage out remote files with gram");
                }

                String s = "(file_stage_out = (" + f.getResolvedSrc().getPath()
                        + " gsiftp://" + GATEngine.getLocalHostName() + "/"
                        + f.getResolvedDest().getPath() + "))";
                rsl += s;
            }
        }

        org.gridlab.gat.io.File stdout = sd.getStdout();
        if (stdout != null) {
            if (sandbox != null) {
                rsl += (" (stdout = " + sandbox.getRelativeStdout().getPath() + ")");
            }
        }

        org.gridlab.gat.io.File stderr = sd.getStderr();
        if (stderr != null) {
            if (sandbox != null) {
                rsl += (" (stderr = " + sandbox.getRelativeStderr().getPath() + ")");
            }
        }

        org.gridlab.gat.io.File stdin = sd.getStdin();
        if (stdin != null) {
            if (sandbox != null) {
                rsl += (" (stdin = " + sandbox.getRelativeStdin().getPath() + ")");
            }
        }

        // set the environment
        Map<String, Object> env = sd.getEnvironment();
        if (env != null && !env.isEmpty()) {
            Set<String> s = env.keySet();
            Object[] keys = (Object[]) s.toArray();
            rsl += "(environment = ";

            for (int i = 0; i < keys.length; i++) {
                String val = (String) env.get(keys[i]);
                rsl += "(" + keys[i] + " \"" + val + "\")";
            }
            rsl += ")";
        }

        String queue = getStringAttribute(description, "globus.queue", null);
        if (queue != null) {
            rsl += " (queue = " + queue + ")";
        }

        if (GATEngine.VERBOSE) {
            System.err.println("RSL: " + rsl);
        }

        return rsl;
    }

    public Job submitJob(AbstractJobDescription abstractDescription,
            MetricListener listener, String metricName)
            throws GATInvocationException {

        if (!(abstractDescription instanceof JobDescription)) {
            throw new GATInvocationException(
                    "can only handle JobDescriptions: "
                            + abstractDescription.getClass());
        }

        JobDescription description = (JobDescription) abstractDescription;

        System.err.println("@@@ in koala.submit job");
        System.err.println("@@@ creating prestaged");

        PreStagedFileSet pre = new PreStagedFileSet(gatContext, description,
                null, null, false);

        System.err.println("@@@ creating poststaged");

        PostStagedFileSet post = new PostStagedFileSet(gatContext, description,
                null, null, false, false);

        System.err.println("@@@ creating rsl");

        String rsl = createRSL(description, null, null, pre, post);

        System.err.println("@@@ Generated KOALA RSL:\n" + rsl);

        KoalaJob job = new KoalaJob(gatContext, description, schedulerAddress,
                policy, rsl);

        System.err.println("@@@ submit to scheduler!");

        try {
            job.submitToScheduler();
        } catch (IOException e) {
            throw new GATInvocationException("Failed to submit to Koala "
                    + "scheduler!", e);
        }

        return job;
    }

}
