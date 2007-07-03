package org.gridlab.gat.resources.cpi;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
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
    boolean verbose = false;

    boolean debug = false;

    boolean timing = false;

    public static void main(String[] args) {
        new RemoteSandbox().start(args);
    }

    public synchronized void processMetricEvent(MetricValue val) {
        notifyAll();
    }

    private File rewriteStagedFile(GATContext gatContext,
            Preferences preferences, File origSrc, File origDest,
            String destHostname, String remoteCWD) {
        if (origSrc == null && origDest == null) {
            return null;
        }

        // leave remote files untouched
        if (origDest != null && origDest.toGATURI().getHost() != null
                && !origDest.toGATURI().getHost().equals("localhost")) {
            return origDest;
        }

        URI origDestURI = null;
        
        if (origDest == null) {
            String origSrcName = origSrc.getName();
            
            // if it is a relative path, and no hostname is given, the URI is
            // relative to the CWD, not the host entry point.
            if(origSrc.toGATURI().getHost() == null && !origSrc.isAbsolute()) {
                origSrcName = remoteCWD + "/" + origSrcName;
            }
            
            String newLocation =
                    "any://" + destHostname + "/" + origSrcName;
            try {
                origDestURI = new URI(newLocation);

                if (verbose) {
                    System.err.println("rewrite of " + origSrc.getName()
                            + " to " + origDestURI);
                }
            } catch (URISyntaxException e) {
                System.err.println("could not rewrite poststage file "
                        + newLocation + ":" + e);
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            origDestURI = origDest.toGATURI();
        }

        String newLocation =
                "any://" + destHostname + "/" + origDestURI.getPath();

        File res = null;
        try {
            URI newURI = new URI(newLocation);

            if (verbose) {
                System.err.println("rewrite of " + origDestURI + " to "
                        + newURI);
            }
            res = GAT.createFile(gatContext, preferences, newURI);
        } catch (Exception e) {
            System.err.println("could not rewrite poststage file" + origDestURI
                    + ":" + e);
            System.exit(1);
        }

        return res;
    }

    public void start(String[] args) {
        final String descriptorFile = args[0];
        final String initiator = args[1];
        final String preStageDoneLocation = args[2];
        final String remoteCWD = args[3];
        verbose = args[4].equalsIgnoreCase("true");
        debug = args[5].equalsIgnoreCase("true");
        timing = args[6].equalsIgnoreCase("true");

        if (verbose) {
            System.setProperty("gat.verbose", "true");
        }
        if (debug) {
            System.setProperty("gat.debug", "true");
        }
        if (timing) {
            System.setProperty("gat.timing", "true");
        }

        if (verbose) {
            System.err
                    .println("RemoteSandbox started, initiator: " + initiator);
        }

        JobDescription description = null;
        try {
            if (verbose) {
                System.err
                        .println("opening descriptor file: " + descriptorFile);
            }
            FileInputStream tmp = new FileInputStream(descriptorFile);
            ObjectInputStream in = new ObjectInputStream(tmp);
            description = (JobDescription) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("an error occurred: " + e);
            System.exit(1);
        }

        // modify the description to run it locally
        SoftwareDescription sd = description.getSoftwareDescription();
        sd.addAttribute("useLocalDisk", "false");

        if (verbose) {
            System.err.println("read job description: " + description);
        }

        GATContext gatContext = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "local");

        // rewrite poststage files to go directly to their original destination
        // also stdout and stderr
        sd.setStderr(rewriteStagedFile(gatContext, prefs, null, sd
                .getStderr(), initiator, remoteCWD));
        sd.setStdout(rewriteStagedFile(gatContext, prefs, null, sd
                .getStdout(), initiator, remoteCWD));

        sd.setStdin(rewriteStagedFile(gatContext, prefs, sd.getStdin(),
                null, initiator, remoteCWD));

        Map pre = sd.getPreStaged();
        Set tmp = pre.keySet();
        Object[] keys = tmp.toArray();
        File[] srcs = new File[keys.length];
        File[] dests = new File[keys.length];
        for (int i = 0; i < keys.length; i++) {
            File src = (File) keys[i];
            File dest = (File) pre.get(src);
            srcs[i] =
                    rewriteStagedFile(gatContext, prefs, dest, src, initiator, remoteCWD);
            dests[i] = dest;
        }
        pre.clear();
        for (int i = 0; i < keys.length; i++) {
            pre.put(srcs[i], dests[i]);
        }

        Map post = sd.getPostStaged();
        tmp = post.keySet();
        keys = tmp.toArray();
        for (int i = 0; i < keys.length; i++) {
            File src = (File) keys[i];
            File dest = (File) post.get(src);
            dest = rewriteStagedFile(gatContext, prefs, src, dest, initiator, remoteCWD);
            post.put(src, dest);
        }

        if (verbose) {
            System.err.println("modified job description: " + description);
        }

        ResourceBroker broker = null;
        try {
            broker = GAT.createResourceBroker(gatContext, prefs);
        } catch (GATObjectCreationException e) {
            System.err.println("could not create broker: " + e);
            System.exit(1);
        }

        try {
            Job job = broker.submitJob(description);

            if (sd.getBooleanAttribute("waitForPreStage", false)) {
                if (verbose) {
                    System.err.println("writing prestageDoneFile at "
                            + preStageDoneLocation);
                }

                File preStageDoneFile =
                        GAT.createFile(gatContext, prefs, preStageDoneLocation);

                if (!preStageDoneFile.createNewFile()) {
                    System.err.println("could not create preStageDone file");
                    System.exit(1);
                }
                if (verbose) {
                    System.err.println("writing prestageDoneFile at "
                            + preStageDoneLocation + " DONE");
                }
            } else {
                if (verbose) {
                    System.err.println("not waiting for preStage");
                }                
            }

            MetricDefinition md = job.getMetricDefinitionByName("job.status");
            Metric m = md.createMetric(null);
            job.addMetricListener(this, m);

            synchronized (this) {
                while ((job.getState() != Job.STOPPED)
                        && (job.getState() != Job.SUBMISSION_ERROR)) {
                    wait();
                }
            }

            if (verbose) {
                System.err.println("SubmitJobCallback: Job finished, state = "
                        + job.getInfo());
            }
        } catch (Exception e) {
            System.err.println("an exception occurred: " + e);
            System.exit(1);
        }

        GAT.end();
        System.exit(0);
    }
}
