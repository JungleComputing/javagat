package org.gridlab.gat.resources.cpi.remoteSandbox;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
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
    public static void main(String[] args) {
        new RemoteSandbox().start(args);
    }

    public synchronized void processMetricEvent(MetricValue val) {
        notifyAll();
    }

    private File rewritePostStagedFile(GATContext gatContext,
            Preferences preferences, File orig, String destHostname) {
        if (orig == null)
            return null;

        URI location = orig.toGATURI();
        if (location.getHost() != null) {
            return orig;
        }

        String newLocation = "any://" + destHostname + "/" + location.getPath();

        File res = null;
        try {
            URI newURI = new URI(newLocation);

            System.err.println("rewrite of " + orig.toGATURI() + " to " + newURI);
            res = GAT.createFile(gatContext, preferences, newURI);
        } catch (Exception e) {
            System.err.println("could not rewrite poststage file" + orig + ":"
                    + e);
            System.exit(1);
        }
        return res;

    }

    public void start(String[] args) {
        System.err.println("RemoteSandbox started, initiator: " + args[1]);
        System.setProperty("gat.verbose", "true");
        GATContext gatContext = new GATContext();
        Preferences prefs = new Preferences();

        prefs.put("ResourceBroker.adaptor.name", "local");

        JobDescription description = null;
        try {
            System.err.println("opening descriptor file: " + args[0]);
            FileInputStream tmp = new FileInputStream(args[0]);
            ObjectInputStream in = new ObjectInputStream(tmp);
            description = (JobDescription) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("an error occurred: " + e);
            System.exit(1);
        }

        System.err.println("read job description: " + description);

        // modify the description to run it locally
        SoftwareDescription sd = description.getSoftwareDescription();
        sd.addAttribute("useLocalDisk", "false");

        // rewrite poststage files to go directly to their original destination
        // also stdout and stderr
        File stderr = sd.getStderr();
        sd.setStderr(rewritePostStagedFile(gatContext, prefs, stderr, args[1]));

        System.err.println("modified job description: " + description);

        ResourceBroker broker = null;
        try {
            broker = GAT.createResourceBroker(gatContext, prefs);
        } catch (GATObjectCreationException e) {
            System.err.println("could not create broker: " + e);
            System.exit(1);
        }

        try {
            Job job = broker.submitJob(description);
            MetricDefinition md = job.getMetricDefinitionByName("job.status");
            Metric m = md.createMetric(null);
            job.addMetricListener(this, m);

            synchronized (this) {
                while ((job.getState() != Job.STOPPED)
                        && (job.getState() != Job.SUBMISSION_ERROR)) {
                    wait();
                }
            }

            System.err.println("SubmitJobCallback: Job finished, state = "
                    + job.getInfo());
        } catch (Exception e) {
            System.err.println("an exception occurred: " + e);
            System.exit(1);
        }

        GAT.end();
        System.exit(0);
    }
}
