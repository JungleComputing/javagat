package NQueens;

import java.util.Hashtable;
import java.util.StringTokenizer;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class NQueensSolver {
    static GATContext context = new GATContext();

    private static File[] getStageIns(Task t)

    {
        boolean hasStdin = true, hasJars = true, hasInputs = true;
        int i = 0;

        if (t.stdinFile == null) hasStdin = false;
        else if (t.stdinFile == "") hasStdin = false;

        if (t.jars == null) hasJars = false;
        else if (t.jars.length == 0) hasJars = false;

        if (t.inputFiles == null) hasInputs = false;
        else if (t.inputFiles.length == 0) hasInputs = false;

        if (!hasStdin && !hasJars && !hasInputs) return null;

        File[] rv = new File[((hasInputs) ? t.inputFiles.length : 0)
            + ((hasJars) ? t.jars.length : 0) + ((hasStdin) ? 1 : 0)];

        try {
            for (i = 0; i < t.inputFiles.length; i++)
                rv[i] = GAT.createFile(context, new URI("any:///"
                    + t.inputFiles[i]));

            for (; i < t.inputFiles.length + t.jars.length; i++)
                rv[i] = GAT.createFile(context, new URI("any:///"
                    + t.jars[i - t.inputFiles.length]));

            if (hasStdin)
                rv[i] = GAT.createFile(context,
                    new URI("any:///" + t.stdinFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rv;
    }

    private static File[] getStageOuts(Task t) {
        boolean hasStdout = true, hasStderr = true;
        int i = 0;

        if (t.stdoutFile == null) hasStdout = false;
        else if (t.stdoutFile == "") hasStdout = false;

        if (t.stderrFile == null) hasStderr = false;
        else if (t.stderrFile == "") hasStderr = false;

        int noFiles = 0;
        if (hasStdout) noFiles++;
        if (hasStderr) noFiles++;
        if (t.outputFiles != null) noFiles += t.outputFiles.length;

        if (noFiles == 0) return null;

        File[] rv = new File[noFiles];

        try {
            for (i = 0; i < t.outputFiles.length; i++) {
                rv[i] = GAT.createFile(context, new URI("any:///"
                    + t.outputFiles[i]));
            }
            int index = i;

            if (hasStdout) {
                rv[index] = GAT.createFile(context, new URI("any:///"
                    + t.stdoutFile));
                ;
                index++;
            }

            if (hasStderr) {
                rv[index] = GAT.createFile(context, new URI("any:///"
                    + t.stderrFile));
                ;
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rv;
    }

    private static String getJarList(String[] jars) {
        if (jars == null) return null;
        if (jars.length == 0) return null;

        String rv = "";

        for (int i = 0; i < jars.length; i++)
            rv = rv + jars[i] + ":";

        rv += ".";

        return rv;
    }

    private static void enqueueStringList(String[] dest, String[] src,
        int startIndex) {
        for (int i = 0; i < src.length; i++)
            dest[startIndex + i] = src[i];
    }

    /*
     private static String printTask(Task t)
     {
     String rv="T("+t.taskNumber+", "+t.className+", "+t.stdoutFile+", "+t.stderrFile+", "+t.stdinFile+", ";

     for(int i=0;i<t.parameters.length;i++)
     rv+=t.parameters[i]+"; ";

     rv+=", ";

     for(int i=0;i<t.jars.length;i++)
     rv+=t.jars[i]+"; ";

     rv+=", ";

     for(int i=0;i<t.inputFiles.length;i++)
     rv+=t.inputFiles[i]+"; ";

     rv+=", ";

     for(int i=0;i<t.outputFiles.length;i++)
     rv+=t.outputFiles[i]+"; ";

     rv+=")";

     return rv;
     }
     */

    public static void main(String[] args) {
        String javaLocation = "/usr/local/sun-java/j2sdk1.4.2/j2sdk1.4.2/bin/java";
        String[] inputParams = new String[4];
        int jobIDNo = 0;
        String nameServerHost = "fs0.das2.cs.vu.nl";
        String nameServerPort = "32000";

        String extJobID = args[args.length - 4];
        String nameserver = args[args.length - 3];
        String descriptors = args[args.length - 2];
        //String registry = args[args.length-2];
        int maxHosts = Integer.parseInt(args[args.length - 1]);

        String jobID = extJobID + "_" + jobIDNo;
        StringTokenizer tok = new StringTokenizer(nameserver, ":");
        nameServerHost = tok.nextToken();
        nameServerPort = tok.nextToken();

        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "proActive");

        prefs.put("ResourceBroker.proActive.ibis.nameserver.jobID", jobID);
        prefs.put("ResourceBroker.proActive.ibis.nameserver.host",
            nameServerHost);
        prefs.put("ResourceBroker.proActive.ibis.nameserver.port",
            nameServerPort);
        prefs.put("ResourceBroker.proActive.descriptors", descriptors);

        ResourceBroker broker = null;

        try {
            broker = GAT.createResourceBroker(context, prefs);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        for (int hostID = 0; hostID < maxHosts; hostID++) {
            inputParams[0] = args[0];
            inputParams[1] = args[1];
            inputParams[2] = "stdout_" + jobID;
            inputParams[3] = "-satin-detailed-stats";
            try {
                String className = "MyNQueens";
                String protocol = "any";
                String stdoutFile = null;
                String stderrFile = "stderr_" + jobID;
                String stdinFile = null;
                // de trecut la linia de comanda ne-closed world
                String[] JVMParameters = new String[8];
                //String[] jars = new String[5];
                String[] jars = new String[1];
                String[] inputFiles = new String[0];
                String[] outputFiles = new String[1];

                outputFiles[0] = "stdout_" + jobID;

                // de vazut cu log4j
                JVMParameters[0] = "-Dibis.pool.total_hosts=" + maxHosts;
                JVMParameters[1] = "-Dibis.name_server.host=" + nameServerHost;
                JVMParameters[2] = "-Dibis.name_server.port=" + nameServerPort;
                JVMParameters[3] = "-Dibis.name_server.key=jobID_" + jobID;
                JVMParameters[4] = "-Dibis.pool.host_number=" + hostID;
                JVMParameters[5] = "-Dsatin.ft=true";
                JVMParameters[6] = "-Dibis.connect.control_links=RoutedMessages";
                JVMParameters[7] = "-Dibis.connect.data_links=AnyTCP";

                /*
                 jars[0]="ibis-rob.jar";
                 jars[1]="myNQueens-rob.jar";
                 jars[2]="ibis-connect.jar";
                 jars[3]="ibis-util.jar";
                 jars[4]="colobus.jar";
                 */

                jars[0] = "nqueen.jar";

                Task currentTask = new Task(className, stdoutFile, stderrFile,
                    stdinFile, inputParams, JVMParameters, jars, inputFiles,
                    outputFiles);

                /* File mainClass = */GAT.createFile(context, new URI(protocol
                    + ":///" + currentTask.className));

                File stdout = null, stderr = null, stdin = null;

                if (currentTask.stdoutFile != null)
                    if (currentTask.stdoutFile != "")
                        stdout = GAT.createFile(context, new URI(protocol
                            + ":///" + currentTask.stdoutFile));

                if (currentTask.stderrFile != null)
                    if (currentTask.stderrFile != "")
                        stderr = GAT.createFile(context, new URI(protocol
                            + ":///" + currentTask.stderrFile));

                if (currentTask.stdinFile != null)
                    if (currentTask.stdinFile != "")
                        stdin = GAT.createFile(context, new URI(protocol
                            + ":///" + currentTask.stdinFile));

                File[] stageIns = null;
                File[] stageOuts = null;

                stageIns = getStageIns(currentTask);
                stageOuts = getStageOuts(currentTask);

                String jarList = getJarList(currentTask.jars);

                String[] arguments = null;
                if (jarList != null) {
                    arguments = new String[currentTask.parameters.length
                        + currentTask.JVMParameters.length + 3];
                    arguments[0] = "-cp";
                    arguments[1] = jarList;
                    enqueueStringList(arguments, currentTask.JVMParameters, 2);
                    arguments[currentTask.JVMParameters.length + 2] = currentTask.className;
                    enqueueStringList(arguments, currentTask.parameters,
                        currentTask.JVMParameters.length + 3);
                } else {
                    arguments = new String[currentTask.parameters.length
                        + currentTask.JVMParameters.length + 1];
                    enqueueStringList(arguments, currentTask.JVMParameters, 0);
                    arguments[currentTask.JVMParameters.length] = currentTask.className;
                    enqueueStringList(arguments, currentTask.parameters,
                        currentTask.JVMParameters.length + 1);
                }

                SoftwareDescription sd = new SoftwareDescription();

                sd.setLocation(new URI("file:///" + javaLocation));

                if (stdout != null) sd.setStdout(stdout);
                if (stderr != null) sd.setStderr(stderr);
                if (stdin != null) sd.setStdin(stdin);

                if (stageIns != null) {
                    for(int i=0; i<stageIns.length; i++) {
                        sd.addPreStagedFile(stageIns[i]);
                    }
                }

                if (stageOuts != null) {
                    for(int i=0; i<stageOuts.length; i++) {
                        sd.addPostStagedFile(stageOuts[i]);
                    }
                }

                sd.setArguments(arguments);

                Hashtable ht = new Hashtable();

                ResourceDescription rd = new HardwareResourceDescription(ht);

                JobDescription jd = new JobDescription(sd, rd);

                /* Job j= */broker.submitJob(jd);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //System.exit(0);
    }
}
