package org.gridlab.gat.resources.cpi.globus;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.util.Environment;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class RemoteSandboxSubmitter {
    GATContext origGatContext;

    Preferences origPreferences;

    static int wrapperCounter = 0;

    synchronized static int getCounter() {
        return wrapperCounter++;
    }

    public RemoteSandboxSubmitter(GATContext gatContext, Preferences preferences) {
        this.origGatContext = gatContext;
        this.origPreferences = preferences;
    }

    private java.io.File writeDescriptionToFile(JobDescription description)
        throws GATInvocationException {
        if (GATEngine.VERBOSE) {
            System.err.println("writing description: " + description);
        }
        java.io.File f = null;
        try {
            f = File.createTempFile("GAT", "jobDescription");
            FileOutputStream tmp = new FileOutputStream(f);
            ObjectOutputStream out = new ObjectOutputStream(tmp);
            out.writeObject(description);
            out.close();
        } catch (Exception e) {
            throw new GATInvocationException("RemoteSandboxSubmitter", e);
        }

        return f;
    }

    protected Job submitWrapper(JobDescription description, String contact)
        throws GATInvocationException {
        try {
            Preferences newPreferences = new Preferences(origPreferences);
            newPreferences.put("useLocalDisk", "false");

            SoftwareDescription origSd = description.getSoftwareDescription();
            if (origSd == null) {
                throw new GATInvocationException(
                    "The job description does not contain a software description");
            }

            SoftwareDescription sd = new SoftwareDescription();

            // start with all old attributes.
            // incorrect ones will be overwritten below
            //            sd.setAttributes(origSd.getAttributes());

            Map environment = new HashMap();
            Environment localEnv = new Environment();
            String localGATLocation = localEnv.getVar("GAT_LOCATION");
            int counter = getCounter();

            String getRemoteOutput = origSd.getStringAttribute(
                "getRemoteSandboxOutput", null);
            if (getRemoteOutput != null
                && getRemoteOutput.equalsIgnoreCase("true")) {
                File outFile = GAT.createFile(origGatContext, newPreferences,
                    new URI("any:///remoteSandbox." + counter + ".out"));
                File errFile = GAT.createFile(origGatContext, newPreferences,
                    new URI("any:///remoteSandbox." + counter + ".err"));
                sd.setStdout(outFile);
                sd.setStderr(errFile);
            }

            java.io.File preStageDoneFile = null;
            String preStageDoneFileLocation = "none";
            if (origSd.getBooleanAttribute("waitForPreStage", false)) {
                preStageDoneFile = java.io.File.createTempFile(
                    "JavaGATPrestageDone", "tmp");
                preStageDoneFile.deleteOnExit();
                preStageDoneFileLocation = "any://"
                    + GATEngine.getLocalHostName() + "/"
                    + preStageDoneFile.getCanonicalPath();
            }

            sd.setLocation(new URI(
                "java:org.gridlab.gat.resources.cpi.RemoteSandbox"));

            Object javaHome = origSd.getObjectAttribute("java.home");
            if (javaHome == null) {
                throw new GATInvocationException("java.home not set");
            }
            sd.addAttribute("java.home", javaHome);

            boolean remoteIsGatEnabled = false;
            String remoteEngineLibLocation = "./lib/";

            String remoteGatLocation = origSd.getStringAttribute(
                "remoteGatLocation", null);
            if (remoteGatLocation != null) {
                remoteEngineLibLocation = remoteGatLocation + "/lib/";
                remoteIsGatEnabled = true;
            }

            java.io.File engineDir = new java.io.File(localGATLocation + "/lib");
            String[] files = engineDir.list();
            String classPath = ".";
            for (int i = 0; i < files.length; i++) {
                classPath += ":" + remoteEngineLibLocation + files[i];
            }
            sd.addAttribute("java.classpath", classPath);

            if (remoteIsGatEnabled) {
                environment.put("gat.adaptor.path", remoteGatLocation
                    + "/lib/adaptors");
            } else {
                environment.put("gat.adaptor.path", "lib/adaptors");
            }
            sd.setEnvironment(environment);

            if (!remoteIsGatEnabled) {
                // prestage the gat itself
                sd.addPreStagedFile(GAT.createFile(origGatContext,
                    newPreferences, new URI(localGATLocation
                        + "/log4j.properties")));
                sd.addPreStagedFile(GAT.createFile(origGatContext,
                    newPreferences, new URI(localGATLocation + "/lib")));
            }

            java.io.File descriptorFile = writeDescriptionToFile(description);
            sd.addPreStagedFile(GAT.createFile(origGatContext, newPreferences,
                new URI(descriptorFile.getAbsolutePath())));

            String cwd = System.getProperty("user.dir");

            sd.setArguments(new String[] {
                descriptorFile.getName(),
                GATEngine.getLocalHostName(),
                preStageDoneFileLocation,
                cwd,
                ""
                    + origSd.getBooleanAttribute("verboseRemoteSandbox",
                        GATEngine.VERBOSE),
                ""
                    + origSd.getBooleanAttribute("debugRemoteSandbox",
                        GATEngine.DEBUG),
                ""
                    + origSd.getBooleanAttribute("timeRemoteSandbox",
                        GATEngine.TIMING) });

            String queue = origSd.getStringAttribute("queue", null);
            if (queue != null) {
                sd.addAttribute("queue", queue);
            }

            long maxTime = origSd.getLongAttribute("maxTime", -1);
            if (maxTime > 0) {
                sd.addAttribute("maxTime", new Long(maxTime));
            }

            long maxWallTime = origSd.getLongAttribute("maxWallTime", -1);
            if (maxWallTime > 0) {
                sd.addAttribute("maxWallTime", new Long(maxWallTime));
            }

            long maxCPUTime = origSd.getLongAttribute("maxCPUTime", -1);
            if (maxCPUTime > 0) {
                sd.addAttribute("maxCPUTime", new Long(maxCPUTime));
            }

            JobDescription jd = new JobDescription(sd);
            ResourceBroker broker = GAT.createResourceBroker(origGatContext,
                newPreferences);

            Job j = broker.submitJob(jd);

            // we can now safely delete the descriptor file, it has been prestaged.
            descriptorFile.delete();

            if (origSd.getBooleanAttribute("waitForPreStage", false)) {
                if (GATEngine.VERBOSE) {
                    System.err.println("waiting for prestage to complete");
                }

                while (true) {
                    int state = j.getState();
                    try {
                        if (state == Job.POST_STAGING || state == Job.STOPPED
                            || state == Job.SUBMISSION_ERROR
                            || !preStageDoneFile.exists()) {
                            if (GATEngine.VERBOSE) {
                                System.err
                                    .println("prestage completed, job state = "
                                        + state);
                            }
                            return j;
                        }
                    } catch (Exception e) {
                        if (GATEngine.DEBUG) {
                            System.err.println("warning exists failed: " + e);
                        }
                        // ignore
                    }
                    Thread.sleep(1000);
                }
            }

            return j;
        } catch (Exception e) {
            throw new GATInvocationException("RemoteSandboxSubmitter", e);
        }
    }
}
