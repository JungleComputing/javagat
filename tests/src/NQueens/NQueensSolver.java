package NQueens;

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

    static ArrayList<RunJob> jobList = new ArrayList<RunJob>();

    HashMap<String, Object> globalEnv = new HashMap<String, Object>();
    String globalClassPath = null;

    private class RunJob {
        String application;
        String numHosts = null;
        boolean numHostsIsSoft = false;
        String params = null;
        String outPrefix = null;
        String errPrefix = null;
        String inFilename = null;
        String classPath = null;
        HashMap<String, Object> env = new HashMap<String, Object>();

        public RunJob(String application) {
            if (! application.startsWith("java:")) {
                application = "java:" + application;
            }
            this.application = application;
            env = new HashMap<String, Object>(globalEnv);
        }

        public void setParams(String params) {
            if (this.params != null) {
                error("params set twice for job " + application);
            }
            this.params = params;
        }

        public void setOutputPrefix(String outPrefix) {
            if (this.outPrefix != null) {
                error("outPrefix set twice for job " + application);
            }
            this.outPrefix = outPrefix;
        }

        public void setErrorPrefix(String errPrefix) {
            if (this.errPrefix != null) {
                error("errPrefix set twice for job " + application);
            }
            this.errPrefix = errPrefix;
        }

        public void setInput(String inFilename) {
            if (this.inFilename != null) {
                error("inFilename set twice for job " + application);
            }
            this.inFilename = inFilename;
        }

        public void setClassPath(String classPath) {
            if (this.classPath != null) {
                error("classPath set twice for job " + application);
            }
            this.classPath = classPath;
        }

        public void setNumHosts(String numHosts, boolean numHostsIsSoft) {
            if (this.numHosts != null) {
                error("numHosts set twice for job " + application);
            }
            this.numHosts = numHosts;
            this.numHostsIsSoft = numHostsIsSoft;
        }

        public void add() {
            if (classPath == null) {
                classPath = globalClassPath;
            }
            jobList.add(this);
            if (numHosts == null) {
                System.err.println("Warning: numHosts not set for job "
                        + application);
                System.err.println("   Using default (1).");
                numHosts = "1";
            }
        }

        public String toString() {
            String str = application;
            if (params != null) {
                str += " " + params;
            }
            str += " (on " + numHosts + " hosts)";
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
                + "[ -descr \"<descriptorlist>\" |"
                + " -vn <virtualNodeName> |"
                + " -cp <globalclasspath> |"
                + " -prop <globalprop>=<val> ]*"
                + " [-job <javaclass> ["
                + " -cp <classpath> |"
                + " -param \"<params>\" |"
                + " -nh <numHosts> |"
                + " -softnh <numHosts> |"
                + " -prop <prop>=<val> |"
                + " -in <inputFile> |"
                + " -out <outputFilePrefix> |"
                + " -err <errorFilePrefix>]* ]*");
    }

    public static String getarg(String[] args, int index) {
        if (index >= args.length) {
            error("Missing argument for " + args[index-1]);
            usage();
            System.exit(1);
        }
        return args[index];
    }

    public static void error(String s) {
        System.out.println(s);
        usage();
        System.exit(1);
    }

    public void start(String[] args) {
        String descriptors = null;
        String virtualNodeName = null;
        RunJob currJob = null;

        for (int i = 0; i < args.length; i++) {
            if (false) {
            } else if (args[i].equals("-descr")) {
                if (descriptors != null) {
                    error("You can specify only one descriptor list");
                }
                descriptors = getarg(args, ++i);
            } else if (args[i].equals("-vn")) {
                if (virtualNodeName != null) {
                    error("You can specify only one virtualNodeName");
                }
                virtualNodeName = getarg(args, ++i);
            } else if (args[i].equals("-prop")) {
                String temp = getarg(args, ++i);
                StringTokenizer tok = new StringTokenizer(temp, "=");
                String propname = tok.nextToken();
                String propval = tok.nextToken();
                globalEnv.put(propname, propval);
            } else if (args[i].equals("-cp")) {
                globalClassPath = getarg(args, ++i);
            } else if (args[i].equals("-job")) {
                String application = getarg(args, ++i);
                currJob = new RunJob(application);
                for (++i; i < args.length; i++) {
                    if (args[i].equals("-param")) {
                        currJob.setParams(getarg(args, ++i));
                    } else if (args[i].equals("-out")) {
                        currJob.setOutputPrefix(getarg(args, ++i));
                    } else if (args[i].equals("-in")) {
                        currJob.setInput(getarg(args, ++i));
                    } else if (args[i].equals("-err")) {
                        currJob.setErrorPrefix(getarg(args, ++i));
                    } else if (args[i].equals("-cp")) {
                        currJob.setClassPath(getarg(args, ++i));
                    } else if (args[i].equals("-nh")) {
                        currJob.setNumHosts(getarg(args, ++i), false);
                    } else if (args[i].equals("-softnh")) {
                        currJob.setNumHosts(getarg(args, ++i), true);
                    } else if (args[i].equals("-prop")) {
                        String temp = getarg(args, ++i);
                        StringTokenizer tok = new StringTokenizer(temp, "=");
                        String propname = tok.nextToken();
                        String propval = tok.nextToken();
                        currJob.env.put(propname, propval);
                    } else {
                        i--;
                        break;
                    }
                }
                currJob.add();
            } else {
                System.err.println("Unrecognized option: " + args[i]);
                usage();
                System.exit(1);
            }
        }

        if (descriptors == null) {
            error("No ProActive descriptors provided");
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
            broker = GAT.createResourceBroker(context, prefs, new URI(virtualNodeName));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        Job[] jobs = new Job[jobList.size()];

        nJobs = jobs.length;
        for (int i = 0; i < jobs.length; i++) {
            RunJob job = (RunJob) jobList.get(i);

            SoftwareDescription sd = new SoftwareDescription();

            HashMap<String, Object> attrib = new HashMap<String, Object>();
            attrib.put("hostCount", job.numHosts);
            if (job.numHostsIsSoft) {
                attrib.put("softHostCount", "");
            }
            if (job.classPath != null) {
                attrib.put("classpath", job.classPath);
            }
            sd.setAttributes(attrib);

            try {
                sd.setExecutable(job.application);
            } catch(Exception e) {
                System.err.println("Error in URI " + job.application
                        + ", job skipped");
                e.printStackTrace();
                nJobs--;
                continue;
            }

            sd.setEnvironment(job.env);

            if (job.params != null) {
                sd.setArguments(new String[] { job.params });
            }

            File outFile = null;
            File errFile = null;
            File inFile = null;

            if (job.outPrefix != null) {
                try {
                    outFile = GAT.createFile(context, prefs,
                            new URI("any:///" + job.outPrefix + "." + i));
                } catch(Exception e) {
                    System.err.println("Could not createFile " + outFile
                            + ", using stdout instead");
                    e.printStackTrace();
                }
                if (outFile != null) {
                    sd.setStdout(outFile);
                }
            }
            if (job.errPrefix != null) {
                try {
                    errFile = GAT.createFile(context, prefs,
                            new URI("any:///" + job.errPrefix + "." + i));
                } catch(Exception e) {
                    System.err.println("Could not createFile " + errFile
                            + ", using stderr instead");
                    e.printStackTrace();
                }
                if (errFile != null) {
                    sd.setStderr(errFile);
                }
            }
            if (job.inFilename != null) {
                if (job.inFilename.equals("-")) {
                    prefs.put("ResourceBroker.ProActive.needsStdin", "yes");
                } else {
                    try {
                        inFile = GAT.createFile(context, prefs,
                                new URI("any:///" + job.inFilename));
                    } catch(Exception e) {
                        System.err.println("Could not createFile " + job.inFilename
                                + ", using stdin instead");
                        e.printStackTrace();
                    }
                    if (inFile != null) {
                        sd.setStdin(inFile);
                    }
                }
            } else {
                prefs.put("ResourceBroker.ProActive.needsStdin", "");
            }
 
            Hashtable<String, Object> ht = new Hashtable<String, Object>();
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
                Map<String, Object> info;
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
