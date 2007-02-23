package org.gridlab.gat.resources.cpi.globus;

import ibis.util.IPUtils;

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
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.util.Environment;

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
            Map environment = new HashMap();
            Environment localEnv = new Environment();
            String localGATLocation = localEnv.getVar("GAT_LOCATION");
            int counter = getCounter();

            String getRemoteOutput = origSd.getStringAttribute("getRemoteSandboxOutput", null);
            if (getRemoteOutput != null
                    && getRemoteOutput.equalsIgnoreCase("true")) {
                File outFile =
                        GAT.createFile(origGatContext, newPreferences, new URI(
                                "any:///remoteSandbox." + counter + ".out"));
                File errFile =
                        GAT.createFile(origGatContext, newPreferences, new URI(
                                "any:///remoteSandbox." + counter + ".err"));
                sd.setStdout(outFile);
                sd.setStderr(errFile);
            }

            String preStageDoneFileLocation = "any://" + IPUtils.getLocalHostName() +
            "//tmp/.JavaGATPrestageDone." + counter;
            
            sd.setLocation(new URI(
                            "java:org.gridlab.gat.resources.cpi.RemoteSandbox"));

            
            Object javaHome = origSd.getObjectAttribute("java.home");
            if (javaHome == null) {
                throw new GATInvocationException("java.home not set");
            }
            sd.addAttribute("java.home", javaHome);

            boolean remoteIsGatEnabled = false;
            String remoteEngineLibLocation = "./lib/";

            String remoteGatLocation = origSd.getStringAttribute("remoteGatLocation", null);
            if (remoteGatLocation != null) {
                remoteEngineLibLocation = remoteGatLocation + "/engine/lib/";
                remoteIsGatEnabled = true;
            }

            // TODO replace with local "find" in engine lib dir
            String classPath =
                    "." + ":" + remoteEngineLibLocation + "GAT.jar" + ":"
                            + remoteEngineLibLocation + "castor-0.9.6.jar"
                            + ":" + remoteEngineLibLocation
                            + "commons-logging-1.1.jar" + ":"
                            + remoteEngineLibLocation + "log4j-1.2.13.jar"
                            + ":" + remoteEngineLibLocation
                            + "xmlParserAPIs.jar" + ":"
                            + remoteEngineLibLocation + "castor-0.9.6-xml.jar"
                            + ":" + remoteEngineLibLocation + "colobus.jar"
                            + ":" + remoteEngineLibLocation
                            + "ibis-util-1.4.jar" + ":"
                            + remoteEngineLibLocation + "xercesImpl.jar";

            sd.addAttribute("java.classpath", classPath);

            if (remoteIsGatEnabled) {
                environment.put("gat.adaptor.path", remoteGatLocation
                        + "/adaptors/lib");
            } else {
                environment.put("gat.adaptor.path", "lib");
            }
            sd.setEnvironment(environment);

            if (!remoteIsGatEnabled) {
                // prestage the gat itself
                sd.addPreStagedFile(GAT.createFile(origGatContext,
                        newPreferences, new URI(localGATLocation
                                + "/log4j.properties")));
                sd.addPreStagedFile(GAT.createFile(origGatContext,
                        newPreferences, new URI(localGATLocation
                                + "/engine/lib")));
                sd.addPreStagedFile(GAT.createFile(origGatContext,
                        newPreferences, new URI(localGATLocation
                                + "/adaptors/lib")));
            }

            java.io.File descriptorFile = writeDescriptionToFile(description);
            sd.addPreStagedFile(GAT.createFile(origGatContext, newPreferences,
                    new URI(descriptorFile.getAbsolutePath())));

            sd.setArguments(new String[] {
                    descriptorFile.getName(),
                    IPUtils.getLocalHostName(),
                    preStageDoneFileLocation,
                    ""
                            + origSd.getBooleanAttribute(
                                    "verboseRemoteSandbox", GATEngine.VERBOSE),
                    ""
                            + origSd.getBooleanAttribute("debugRemoteSandbox",
                                    GATEngine.DEBUG),
                    ""
                            + origSd.getBooleanAttribute("timeRemoteSandbox",
                                    GATEngine.TIMING) });

            JobDescription jd = new JobDescription(sd);
            ResourceBroker broker =
                    GAT.createResourceBroker(origGatContext, newPreferences);

            Job j = broker.submitJob(jd);
            
            // we can now safely delete the descriptor file, it has been prestaged.
            descriptorFile.delete();
            
            if(origSd.getBooleanAttribute("waitForPreStage", false)) {
                if(GATEngine.VERBOSE) {
                    System.err.println("waiting for prestage to complete");
                }
                java.io.File f = new java.io.File("/tmp/.JavaGATPrestageDone." + counter);
                while(true) {
                    int state = j.getState();
                    try {
                        if(state == Job.POST_STAGING 
                                || state == Job.STOPPED 
                                || state == Job.SUBMISSION_ERROR 
                                || f.exists()) {
                            try {
                                f.delete();
                            } catch (Exception e) {
                                if(GATEngine.DEBUG) {
                                    System.err.println("warning delete failed: " + e);
                                }
                                // ignore
                            }
                            if(GATEngine.VERBOSE) {
                                System.err.println("prestage completed, job state = " + state);
                            }
                            return j;
                        }
                    } catch (Exception e) {
                        if(GATEngine.DEBUG) {
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
