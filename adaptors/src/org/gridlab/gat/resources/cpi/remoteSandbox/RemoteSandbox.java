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
    boolean verbose = false;
    boolean debug = false;
    boolean timing = false;
    
    public static void main(String[] args) {
        new RemoteSandbox().start(args);
    }

    public synchronized void processMetricEvent(MetricValue val) {
        notifyAll();
    }

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

                if(verbose) {
                System.err.println("rewrite of " + origSrc.getName() + " to "
                        + newURI);
                }
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

            if(verbose) {
            System.err.println("rewrite of " + origDest.toGATURI() + " to "
                    + newURI);
            }
            res = GAT.createFile(gatContext, preferences, newURI);
        } catch (Exception e) {
            System.err.println("could not rewrite poststage file" + origDest
                    + ":" + e);
            System.exit(1);
        }

        return res;
    }

    public void start(String[] args) {
        final String descriptorFile = args[0];
        final String initiator = args[1];
        verbose = args[2].equalsIgnoreCase("true");  
        debug = args[3].equalsIgnoreCase("true");  
        timing = args[4].equalsIgnoreCase("true");  

        if(verbose) {
            System.setProperty("gat.verbose", "true");
        }
        if(debug) {
            System.setProperty("gat.debug", "true");           
        }
        if(timing) {
            System.setProperty("gat.timing", "true");
        }
        
        if(verbose) {
            System.err.println("RemoteSandbox started, initiator: " + initiator);
        }

        JobDescription description = null;
        try {
            if(verbose) {
                System.err.println("opening descriptor file: " + descriptorFile);
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

        if(verbose) {
            System.err.println("read job description: " + description);
        }

        GATContext gatContext = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "local");
        
        // rewrite poststage files to go directly to their original destination
        // also stdout and stderr
        sd.setStderr(rewritePostStagedFile(gatContext, prefs, null, sd
                .getStderr(), args[1]));
        sd.setStdout(rewritePostStagedFile(gatContext, prefs, null, sd
                .getStdout(), args[1]));

        sd.setStdin(rewritePostStagedFile(gatContext, prefs, sd
                .getStdin(), null, args[1]));
        
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

        if(verbose) {
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
            MetricDefinition md = job.getMetricDefinitionByName("job.status");
            Metric m = md.createMetric(null);
            job.addMetricListener(this, m);

            synchronized (this) {
                while ((job.getState() != Job.STOPPED)
                        && (job.getState() != Job.SUBMISSION_ERROR)) {
                    wait();
                }
            }

            if(verbose) {
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
