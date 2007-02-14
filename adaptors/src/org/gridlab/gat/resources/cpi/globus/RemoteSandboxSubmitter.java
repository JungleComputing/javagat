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
            sd
                    .addAttribute(
                            "java.classpath",
                            "log4j.properties:lib/GAT.jar:lib/castor-0.9.6.jar:lib/commons-logging.jar:lib/log4j-1.2.13.jar:lib/xmlParserAPIs.jar"
                                    + "lib/castor-0.9.6-xml.jar:lib/colobus.jar:lib/ibis-util-1.4.jar:lib/xercesImpl.jar:lib/RemoteSandbox.jar");
            Map environment = new HashMap();
            environment.put("gat.adaptor.path", "lib");
            sd.setEnvironment(environment);

            sd.addPreStagedFile(GAT.createFile(origGatContext, newPreferences,
                    new URI("log4j.properties")));
            sd.addPreStagedFile(GAT.createFile(origGatContext, newPreferences,
                    new URI("engine/lib")));
            sd.addPreStagedFile(GAT.createFile(origGatContext, newPreferences,
                    new URI("adaptors/lib")));

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
