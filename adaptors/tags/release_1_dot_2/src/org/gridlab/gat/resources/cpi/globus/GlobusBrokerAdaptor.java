/*
 * Created on Oct 14, 2004
 */
package org.gridlab.gat.resources.cpi.globus;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.globus.common.ResourceManagerContact;
import org.globus.gram.Gram;
import org.globus.gram.GramJob;
import org.globus.gsi.gssapi.auth.NoAuthorization;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;

/**
 * @author rob
 */
public class GlobusBrokerAdaptor extends ResourceBrokerCpi {
    public GlobusBrokerAdaptor(GATContext gatContext, Preferences preferences)
        throws GATObjectCreationException {
        super(gatContext, preferences);
        checkName("globus");

        // turn off all annoying cog prints
        if (!GATEngine.DEBUG) {
            Logger logger = Logger.getLogger(NoAuthorization.class.getName());
            logger.setLevel(Level.OFF);
        }
    }

    protected String createRSL(JobDescription description)
        throws GATInvocationException {

        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        String exe = getLocationURI(description).getPath();

        // parse the arguments
        String args = "";
        String[] argsA = getArgumentsArray(description);
        if (argsA != null) {
            for (int i = 0; i < argsA.length; i++) {
                args += "\"" + argsA[i] + "\" ";
            }
        }

        String rsl = "& (executable = " + exe + ")";

        if (args.length() != 0) {
            rsl += " (arguments = " + args + ")";
        }
        rsl += " (count = 1)";

        org.gridlab.gat.io.File stdout = sd.getStdout();
        if (stdout != null) {
            rsl += " (stdout = " + stdout.getPath() + ")";
        }

        org.gridlab.gat.io.File stderr = sd.getStderr();
        if (stderr != null) {
            rsl += " (stderr = " + stderr.getPath() + ")";
        }

        if (GATEngine.VERBOSE) {
            System.err.println("RSL: " + rsl);
        }
        return rsl;
    }

    String getResourceManagerContact(JobDescription description)
        throws GATInvocationException {
        String res = null;
        String jobManager = (String) preferences
            .get("ResourceBroker.jobmanager");

        String hostname = getHostname(description);
        if (hostname != null) {
            res = hostname;
            if (jobManager != null) {
                res += "/jobmanager-" + jobManager;
            }

            if (GATEngine.VERBOSE) {
                System.err.println("Resource manager contact = " + res);
            }
            return res;
        }

        throw new GATInvocationException(
            "The Globus resource broker needs a hostname");
    }

    /*
     String getHostname(JobDescription description)
     throws GATInvocationException {
     ResourceDescription d = description.getResourceDescription();
     if (d == null) {
     return null;
     }

     if (!(d instanceof HardwareResourceDescription)) {
     throw new GATInvocationException(
     "The Globus adaptor currently only supports hardware resource descriptions");
     }

     Map m = d.getDescription();
     Set keys = m.keySet();
     Iterator i = keys.iterator();
     while (i.hasNext()) {
     String key = (String) i.next();
     Object val = m.get(key);

     if (key.equals("machine.node")) {
     if(val instanceof String) {
     return (String) val;
     } else {
     String[] hostList = (String[]) val;
     return hostList[0];
     }
     }
     System.err.println("Globus adaptor: warning, ignoring key: " + key);
     }

     return null;
     }
     */
    public Job submitJob(JobDescription description)
        throws GATInvocationException, IOException {

        String host = getHostname(description);
        if (host != null) {
            removePostStagedFiles(description, host);
            preStageFiles(description, host);
        }

        String rsl = createRSL(description);
        String contact = getResourceManagerContact(description);

        URI hostUri;
        try {
            hostUri = new URI(host);
        } catch (Exception e) {
            throw new GATInvocationException("globus broker", e);
        }
        GSSCredential credential = GlobusSecurityUtils.getGlobusCredential(
            gatContext, preferences, "gridftp", hostUri,
            ResourceManagerContact.DEFAULT_PORT);

        GramJob j = new GramJob(credential, rsl);
        GlobusJob res = new GlobusJob(this, description, j);
        j.addListener(res);

        try {
            Gram.request(contact, j);
        } catch (Throwable t) {
            throw new GATInvocationException("globus", t);
        }

        return res;
    }
}
