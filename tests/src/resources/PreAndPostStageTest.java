/*
 * Created on Nov 8, 2006 by rob
 */
package resources;

import java.util.Hashtable;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author rob
 */
public class PreAndPostStageTest implements MetricListener {

    // arg0 = hostname arg 1 = job manager
    public static void main(String[] args) {
        new PreAndPostStageTest().start(args);
    }

    public synchronized void processMetricEvent(MetricEvent val) {
        System.err.println("SubmitJobCallback: Processing metric: "
                + val.getMetric() + ", value is " + val.getValue());

        String state = (String) val.getValue();

        if (state.equals("STOPPED") || state.equals("SUBMISSION_ERROR")) {
            notifyAll();
        }
    }

    public void start(String[] args) {
        System.err.println("----PRE AND POST STAGE TEST----");

        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", args[1]);

        SoftwareDescription sd = new SoftwareDescription();

        try {
            sd.setExecutable("/bin/date");

            sd.setStdout(GAT.createFile(context, prefs, new URI(
                    "any:///date.out")));
            sd.setStderr(GAT.createFile(context, prefs, new URI(
                    "any:///date.err")));

            // Different combinations of pre staged files.

            // only src given, local file
            sd.addPreStagedFile(GAT.createFile(context, prefs, new URI(
                    "any:////bin/echo")));

            // src and dest given, local file into sandbox
            sd.addPreStagedFile(GAT.createFile(context, prefs, new URI(
                    "any:////bin/bash")), GAT.createFile(context, prefs,
                    new URI("any:///local_bash_in_sandbox")));

            // src and dest given, local file into sandbox subdir
            sd.addPreStagedFile(GAT.createFile(context, prefs, new URI(
                    "any:////bin/sh")), GAT.createFile(context, prefs, new URI(
                    "any:///local_test_dir/local_sh_in_sandbox_in_subdir")));

            // src and dest given, local file to remote dir outside sandbox
            sd.addPreStagedFile(GAT.createFile(context, prefs, new URI(
                    "any:////bin/cat")), GAT.createFile(context, prefs,
                    new URI("any:////tmp/local_cat_outside_sandbox")));

            // src and dest given, local file to remote dir outside sandbox
            sd
                    .addPreStagedFile(
                            GAT.createFile(context, prefs, new URI(
                                    "any:////bin/cp")),
                            GAT
                                    .createFile(
                                            context,
                                            prefs,
                                            new URI(
                                                    "any:////tmp/test_dir/local_cp_outside_sandbox_in_subdir")));

            // only src given, remote file
            sd.addPreStagedFile(GAT.createFile(context, prefs, new URI(
                    "any://fs1.das2.liacs.nl//bin/cat")));

            // src and dest given, remote file into sandbox
            sd.addPreStagedFile(GAT.createFile(context, prefs, new URI(
                    "any://fs1.das2.liacs.nl//bin/cp")), GAT.createFile(
                    context, prefs, new URI("any:///cp_in_sandbox")));

            // src and dest given, remote file into sandbox subdir
            sd.addPreStagedFile(GAT.createFile(context, prefs, new URI(
                    "any://fs1.das2.liacs.nl//bin/date")), GAT.createFile(
                    context, prefs, new URI(
                            "any:///test_dir/date_in_sandbox_subdir")));

            // src and dest given, remote file to remote file outside sandbox
            sd.addPreStagedFile(GAT.createFile(context, prefs, new URI(
                    "any://fs1.das2.liacs.nl//bin/echo")), GAT
                    .createFile(context, prefs, new URI(
                            "any:////tmp/echo_outside_sandbox")));

            // src and dest given, remote file to remote dir outside sandbox
            sd
                    .addPreStagedFile(
                            GAT.createFile(context, prefs, new URI(
                                    "any://fs1.das2.liacs.nl//bin/sh")),
                            GAT
                                    .createFile(
                                            context,
                                            prefs,
                                            new URI(
                                                    "any:////tmp/remote_test_dir/sh_outside_sandbox_in_subdir")));

            // @@@ staging a directory (both in and out)

            // and now poststage some files

            // a file from the sandbox to the local dir, only specifing the
            // source
            sd.addPostStagedFile(GAT
                    .createFile(context, prefs, new URI("echo")));

            // a file from the sandbox to a local dir
            sd.addPostStagedFile(GAT.createFile(context, prefs, new URI(
                    "local_bash_in_sandbox")), GAT.createFile(context, prefs,
                    new URI("any:///local_bash_in_sandbox")));

            // a file from the sandbox to a thirdparty machine
            sd.addPostStagedFile(
                    GAT.createFile(context, prefs, new URI("cat")),
                    GAT.createFile(context, prefs, new URI(
                            "any://fs1.das2.liacs.nl/local_bash_in_sandbox")));

            // a file from outside the sandbox to a local dir
            sd.addPostStagedFile(GAT.createFile(context, prefs, new URI(
                    "/tmp/remote_test_dir/sh_outside_sandbox_in_subdir")));

            // a file from outside the sandbox to a third party machine
            sd.addPostStagedFile(GAT.createFile(context, prefs, new URI(
                    "/tmp/echo_outside_sandbox")), GAT.createFile(context,
                    prefs, new URI(
                            "any://fs1.das2.liacs.nl/echo_outside_sandbox")));

        } catch (Exception e) {
            System.err.println("error creating file: " + e);
            GAT.end();
            System.exit(1);
        }

        Hashtable<String, Object> hardwareAttributes = new Hashtable<String, Object>();
        hardwareAttributes.put("machine.node", args[0]);

        ResourceDescription rd = new HardwareResourceDescription(
                hardwareAttributes);
        JobDescription jd = new JobDescription(sd, rd);

        ResourceBroker broker = null;

        try {
            broker = GAT.createResourceBroker(context, prefs, new URI(
                    "any://localhost"));
        } catch (Exception e) {
            System.err.println("Could not create broker: " + e);
            GAT.end();
            System.exit(1);
        }

        Job job = null;

        try {
            job = broker.submitJob(jd);
        } catch (Exception e) {
            System.err.println("submission failed: " + e);
            e.printStackTrace();
            GAT.end();
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
            GAT.end();
            System.exit(1);
        }

        synchronized (this) {
            while ((job.getState() != Job.STOPPED)
                    && (job.getState() != Job.SUBMISSION_ERROR)) {
                try {
                    wait();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        System.err.println("---PRE AND POST STAGE TEST OK--");
        GAT.end();
    }
}
