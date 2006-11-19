package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class NQueensSolver implements MetricListener {

    private static class RunJob {
        String application;
        String nHosts;
        String params;

        public RunJob(String application, String nHosts, String params) {
            if (! application.startsWith("java:")) {
                application = "java:" + application;
            }
            this.application = application;
            this.nHosts = nHosts;
            this.params = params;
        }

        public String toString() {
            String str = application;
            if (params != null) {
                str += " " + params;
            }
            str += " (on " + nHosts + " hosts)";
            return str;
        }
    }


    static final GATContext context = new GATContext();

    int nJobs;

    int finishedJobs;

    public synchronized void processMetricEvent(MetricValue val) {
        System.err.println("SubmitJobCallback: Processing metric: "
                + val.getMetric() + ", value is " + val.getValue());
        String state = (String) val.getValue();
        if (state.equals("STOPPED") || state.equals("SUBMISSION_ERROR")) {
            finishedJobs++;
            if (finishedJobs == nJobs) {
                notifyAll();
            }
        }
    }

    public static void main(String[] args) {
        new NQueensSolver().start(args);
    }

    public static void usage() {
        System.err.println("Usage: java NQueensSolver "
                + "-descr \"<descriptorlist>\" [-ns <nameserver>:<port>] "
                + "[-vn <virtualNodeName>] [-cp <classpath>] "
                + "[-out <outputFilePrefix>] [-err <errorFilePrefix>] "
                + "[-job <javaclass> <nhosts> [\"<params>\" | --]]*");
    }

    public static String getarg(String[] args, int index) {
        if (index >= args.length) {
            System.err.println("Missing argument for " + args[index-1]);
            usage();
            System.exit(1);
        }
        return args[index];
    }

    public void start(String[] args) {
        String nameServerHost = null;
        String nameServerPort = null;
        String descriptors = null;
        String virtualNodeName = null;
        String outPrefix = null;
        String errPrefix = null;
        String inFilename = null;
        String classPath = ":.:nqueen.jar:ibis.jar";

        ArrayList jobList = new ArrayList();

        for (int i = 0; i < args.length; i++) {
            if (false) {
            } else if (args[i].equals("-descr")) {
                descriptors = getarg(args, ++i);
            } else if (args[i].equals("-ns")) {
                String temp = getarg(args, ++i);
                StringTokenizer tok = new StringTokenizer(temp, ":");
                nameServerHost = tok.nextToken();
                nameServerPort = tok.nextToken();
            } else if (args[i].equals("-job")) {
                String application = getarg(args, ++i);
                String nhosts = getarg(args, ++i);
                String params = getarg(args, ++i);
                if ("--".equals(params)) {
                    params = null;
                }
                jobList.add(new RunJob(application, nhosts, params));
            } else if (args[i].equals("-vn")) {
                virtualNodeName = getarg(args, ++i);
            } else if (args[i].equals("-out")) {
                outPrefix = getarg(args, ++i);
            } else if (args[i].equals("-in")) {
                inFilename = getarg(args, ++i);
            } else if (args[i].equals("-err")) {
                errPrefix = getarg(args, ++i);
            } else if (args[i].equals("-cp")) {
                classPath = getarg(args, ++i);
            } else {
                System.err.println("Unrecognized option: " + args[i]);
                usage();
                System.exit(1);
            }
        }

        if (descriptors == null) {
            System.err.println("No ProActive descriptors provided");
            usage();
            System.exit(1);
        }

        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "ProActive");
        prefs.put("ResourceBroker.ProActive.Descriptors", descriptors);
        if (virtualNodeName != null) {
            prefs.put("ResourceBroker.ProActive.VirtualNodeName",
                    virtualNodeName);
        }

        ResourceBroker broker = null;

        try {
            broker = GAT.createResourceBroker(context, prefs);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        Job[] jobs = new Job[jobList.size()];

        nJobs = jobs.length;
        for (int i = 0; i < jobs.length; i++) {
            RunJob job = (RunJob) jobList.get(i);

            SoftwareDescription sd = new SoftwareDescription();

            HashMap attrib = new HashMap();
            attrib.put("hostCount", job.nHosts);
            attrib.put("softHostCount", "");
            attrib.put("classpath", classPath);
            sd.setAttributes(attrib);

            try {
                sd.setLocation(new URI(job.application));
            } catch(Exception e) {
                System.err.println("Error in URI " + job.application
                        + ", job skipped");
                e.printStackTrace();
                nJobs--;
                continue;
            }

            HashMap env = new HashMap();
            env.put("ibis.pool.total_hosts", job.nHosts);
            if (nameServerHost != null) {
                env.put("ibis.name_server.host", nameServerHost);
                env.put("ibis.name_server.port", nameServerPort);
                env.put("ibis.name_server.key",
                        "key_" + i + "_" + System.currentTimeMillis());
                // What else should we put here?
            }
            sd.setEnvironment(env);

            if (job.params != null) {
                sd.setArguments(new String[] { job.params });
            }

            File outFile = null;
            File errFile = null;
            File inFile = null;

            if (outPrefix != null) {
                try {
                    outFile = GAT.createFile(context, prefs,
                            new URI("any:///" + outPrefix + i));
                } catch(Exception e) {
                    System.err.println("Could not createFile " + outFile
                            + ", using stdout instead");
                    e.printStackTrace();
                }
                if (outFile != null) {
                    sd.setStdout(outFile);
                }
            }
            if (errPrefix != null) {
                try {
                    errFile = GAT.createFile(context, prefs,
                            new URI("any:///" + errPrefix + i));
                } catch(Exception e) {
                    System.err.println("Could not createFile " + errFile
                            + ", using stderr instead");
                    e.printStackTrace();
                }
                if (errFile != null) {
                    sd.setStderr(errFile);
                }
            }
            if (inFilename != null) {
                try {
                    inFile = GAT.createFile(context, prefs,
                            new URI("any:///" + inFilename));
                } catch(Exception e) {
                    System.err.println("Could not createFile " + inFilename
                            + ", using stdin instead");
                    e.printStackTrace();
                }
                if (inFile != null) {
                    sd.setStdin(inFile);
                }
            }
 
            Hashtable ht = new Hashtable();
            ResourceDescription rd = new HardwareResourceDescription(ht);
            JobDescription jd = new JobDescription(sd, rd);
            try {
                jobs[i] = broker.submitJob(jd);
            } catch(Exception e) {
                System.err.println("submitJob of job \"" + job
                        + "\" failed");
                e.printStackTrace();
                nJobs--;
                continue;
            }
            try {
                MetricDefinition md
                        = jobs[i].getMetricDefinitionByName("job.status");
                Metric m = md.createMetric(null);
                jobs[i].addMetricListener(this, m); // register callback.
            } catch(Exception e) {
                System.err.println("Callback registration for job \"" + job
                        + "\" failed, stopping job");
                try {
                    jobs[i].stop();
                } catch(Exception e2) {
                    // Ignored, what can we do here?
                }
                nJobs--;
                continue;
            }
        }

        synchronized(this) {
            while (finishedJobs < nJobs) {
                try {
                    wait();
                } catch(Exception e) {
                    // Ignored
                }
            }
        }

        for (int i = 0; i < jobs.length; i++) {
            if (jobs[i] != null) {
                Map info;
                try {
                    info = jobs[i].getInfo();
                } catch(Exception e) {
                    continue;
                }
                String state = (String) info.get("state");
                if ("SUBMISSION_ERROR".equals(state)) {
                    Throwable e = (Throwable) info.get("submissionError");
                    if (e != null) {
                        System.err.println("Exception for job "
                                + jobList.get(i));
                        e.printStackTrace();
                    }
                }
            }
        }

        GAT.end();
        System.exit(0); // Needed to end some ProActive threads ...
    }
}
