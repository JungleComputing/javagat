package org.gridlab.gat.resources.cpi.remoteSandbox;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Set;

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
/*
    private File rewritePreStagedFile(GATContext gatContext,
            Preferences preferences, File origSrc, File origDest,
            String srcHostname) {

        if (origSrc.toGATURI().getHost() != null
                && !origSrc.toGATURI().getHost().equals("localhost")) {
            return origSrc;
        }

        
        
        
        String newLocation =
                "any://" + srcHostname + "/" + origDest.toGATURI().getPath();

        File res = null;
        try {
            URI newURI = new URI(newLocation);

            System.err.println("rewrite of " + origDest.toGATURI() + " to "
                    + newURI);
            res = GAT.createFile(gatContext, preferences, newURI);
        } catch (Exception e) {
            System.err.println("could not rewrite poststage file" + origDest
                    + ":" + e);
            System.exit(1);
        }
        return res;
    }
*/
    private File rewritePostStagedFile(GATContext gatContext,
            Preferences preferences, File origSrc, File origDest,
            String destHostname) {
        if (origSrc == null && origDest == null) {
            return null;
        }

        if (origDest != null && origDest.toGATURI().getHost() != null
                && !origDest.toGATURI().getHost().equals("localhost")) {
            return origDest;
        }

        if (origDest == null) {
            String newLocation =
                    "any://" + destHostname + "/" + origSrc.getName();
            try {
                URI newURI = new URI(newLocation);

                System.err.println("rewrite of " + origSrc.getName() + " to "
                        + newURI);
                origDest = GAT.createFile(gatContext, preferences, newURI);
            } catch (Exception e) {
                System.err.println("could not rewrite poststage file "
                        + origDest + ":" + e);
                e.printStackTrace();
                System.exit(1);
            }
        }

        String newLocation =
                "any://" + destHostname + "/" + origDest.toGATURI().getPath();

        File res = null;
        try {
            URI newURI = new URI(newLocation);

            System.err.println("rewrite of " + origDest.toGATURI() + " to "
                    + newURI);
            res = GAT.createFile(gatContext, preferences, newURI);
        } catch (Exception e) {
            System.err.println("could not rewrite poststage file" + origDest
                    + ":" + e);
            System.exit(1);
        }

        return res;
    }

    public void start(String[] args) {
        System.err.println("RemoteSandbox started, initiator: " + args[1]);
        System.setProperty("gat.debug", "true");
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
        sd.setStderr(rewritePostStagedFile(gatContext, prefs, null, sd
                .getStderr(), args[1]));
        sd.setStdout(rewritePostStagedFile(gatContext, prefs, null, sd
                .getStdout(), args[1]));

        // @@@ also do stdin
        
        Map pre = sd.getPreStaged();
        Set tmp = pre.keySet();
        Object[] keys = tmp.toArray();
        File[] srcs = new File[keys.length];
        File[] dests = new File[keys.length];
        for(int i=0; i<keys.length; i++) {
            File src = (File) keys[i];
            File dest = (File) pre.get(src);
            srcs[i] = rewritePostStagedFile(gatContext, prefs, dest, src, args[1]);
            dests[i] = dest;
        }
        pre.clear();
        for(int i=0; i<keys.length; i++) {
            pre.put(srcs[i], dests[i]);
        }
        
        Map post = sd.getPostStaged();
        tmp = post.keySet();
        keys = tmp.toArray();
        for(int i=0; i<keys.length; i++) {
            File src = (File) keys[i];
            File dest = (File) post.get(src);
            dest = rewritePostStagedFile(gatContext, prefs, src, dest, args[1]);
            post.put(src, dest);
        }

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
