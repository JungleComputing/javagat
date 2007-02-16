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
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.util.Environment;

public class RemoteSandboxSubmitter {
    GATContext origGatContext;

    Preferences origPreferences;

    public RemoteSandboxSubmitter(GATContext gatContext, Preferences preferences) {
        this.origGatContext = gatContext;
        this.origPreferences = preferences;
    }

    private java.io.File writeDescriptionToFile(JobDescription description)
            throws GATInvocationException {
        System.err.println("writing description: " + description);
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

            File outFile =
                    GAT.createFile(origGatContext, newPreferences, new URI(
                            "any:///wrapper.out"));
            File errFile =
                    GAT.createFile(origGatContext, newPreferences, new URI(
                            "any:///wrapper.err"));
            sd.setStdout(outFile);
            sd.setStderr(errFile);
            sd.setLocation(new URI(
                            "java:org.gridlab.gat.resources.cpi.remoteSandbox.RemoteSandbox"));

            Map attributes = origSd.getAttributes();
            Object javaHome = attributes.get("java.home");
            if (javaHome == null) {
                throw new GATInvocationException("java.home not set");
            }

            sd.addAttribute("java.home", javaHome);
            
//            Environment env = new Environment();
 //           String GATLocation = env.getVar("GAT_LOCATION");
//            String GATEngineLibLocation = GATLocation + "/engine/lib/";            

            String GATLocation=".";
            String GATEngineLibLocation = "./lib/";
            String classPath = "."
                + ":" + GATEngineLibLocation + "GAT.jar"
                + ":" + GATEngineLibLocation + "castor-0.9.6.jar"
                + ":" + GATEngineLibLocation + "commons-logging.jar"
                + ":" + GATEngineLibLocation + "log4j-1.2.13.jar"
                + ":" + GATEngineLibLocation + "xmlParserAPIs.jar"
                + ":" + GATEngineLibLocation + "castor-0.9.6-xml.jar"
                + ":" + GATEngineLibLocation + "colobus.jar"
                + ":" + GATEngineLibLocation + "ibis-util-1.4.jar"
                + ":" + GATEngineLibLocation + "xercesImpl.jar"
                + ":" + GATLocation + "/adaptors/lib/RemoteSandbox.jar";
                
            sd.addAttribute("java.classpath", classPath);
            Map environment = new HashMap();
            environment.put("gat.adaptor.path", "lib");
            sd.setEnvironment(environment);

            sd.addPreStagedFile(GAT.createFile(origGatContext, newPreferences,
                    new URI(GATLocation + "/log4j.properties")));
            sd.addPreStagedFile(GAT.createFile(origGatContext, newPreferences,
                    new URI(GATLocation + "/engine/lib")));
            sd.addPreStagedFile(GAT.createFile(origGatContext, newPreferences,
                    new URI(GATLocation + "/adaptors/lib")));

            java.io.File descriptorFile = writeDescriptionToFile(description);
            sd.addPreStagedFile(GAT.createFile(origGatContext, newPreferences,
                    new URI(descriptorFile.getAbsolutePath())));
            sd.setArguments(new String[] { descriptorFile.getName(), IPUtils.getLocalHostName() });

            JobDescription jd = new JobDescription(sd);
            ResourceBroker broker =
                    GAT.createResourceBroker(origGatContext, newPreferences);

            return broker.submitJob(jd);
        } catch (Exception e) {
            throw new GATInvocationException("RemoteSandboxSubmitter", e);
        }
    }
}
