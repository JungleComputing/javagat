/*
 * Created on Oct 14, 2004
 */
package org.gridlab.gat.resources.cpi.globus;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.globus.common.ResourceManagerContact;
import org.globus.gram.Gram;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gsi.gssapi.auth.NoAuthorization;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
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
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * @author rob
 */
public class GlobusResourceBrokerAdaptor extends ResourceBrokerCpi {
    public GlobusResourceBrokerAdaptor(GATContext gatContext,
        Preferences preferences) throws GATObjectCreationException {
        super(gatContext, preferences);
        // turn off all annoying cog prints
        if (!GATEngine.DEBUG) {
            Logger logger = Logger.getLogger(NoAuthorization.class.getName());
            logger.setLevel(Level.OFF);
        }
    }

    protected String createRSL(JobDescription description, String host,
        Sandbox sandbox) throws GATInvocationException {
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
                args += ("\"" + argsA[i] + "\" ");
            }
        }

        String rsl = "& (executable = " + exe + ")";

        if (args.length() != 0) {
            rsl += (" (arguments = " + args + ")");
        }

        rsl += " (count = " + getCPUCount(description) + ")";

        rsl += " (hostCount = " + getHostCount(description) + ")";

        rsl += " (directory = " + sandbox.getSandbox() + ")";

        long maxTime = getLongAttribute(description, "maxTime", -1);
        if (maxTime > 0) {
            rsl += " (maxTime = " + maxTime + ")";
        }

        long maxWallTime = getLongAttribute(description, "maxWallTime", -1);
        if (maxWallTime > 0) {
            rsl += " (maxWallTime = " + maxWallTime + ")";
        }

        long maxCPUTime = getLongAttribute(description, "maxCPUTime", -1);
        if (maxCPUTime > 0) {
            rsl += " (maxCPUTime = " + maxCPUTime + ")";
        }

        org.gridlab.gat.io.File stdout = sd.getStdout();
        if (stdout != null) {
            rsl += (" (stdout = "
                + sandbox.getRelativeStdout().getPath() + ")");
        }

        org.gridlab.gat.io.File stderr = sd.getStderr();
        if (stderr != null) {
            rsl += (" (stderr = "
                + sandbox.getRelativeStderr().getPath() + ")");
        }

        org.gridlab.gat.io.File stdin = sd.getStdin();
        if (stdin != null) {
            rsl += (" (stdin = "
                + sandbox.getRelativeStdin().getPath() + ")");
        }

        // set the environment
        Map env = sd.getEnvironment();
        if (env != null && !env.isEmpty()) {
            Set s = env.keySet();
            String[] keys = (String[]) s.toArray();
            rsl += "(environment = ";

            for (int i = 0; i < keys.length; i++) {
                String val = (String) env.get(keys[i]);
                rsl += "(" + keys[i] + "\"" + val + "\")";
            }
            rsl += ")";
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
                res += ("/jobmanager-" + jobManager);
            }

            if (GATEngine.VERBOSE) {
                System.err.println("Resource manager contact = " + res);
            }

            return res;
        }

        throw new GATInvocationException(
            "The Globus resource broker needs a hostname");
    }

    public Job submitJob(JobDescription description)
        throws GATInvocationException {
        String host = getHostname(description);
        String contact = getResourceManagerContact(description);

        URI hostUri;
        try {
            hostUri = new URI(host);
        } catch (Exception e) {
            throw new GATInvocationException("globus broker", e);
        }

        GSSCredential credential = null;
        try {
            credential = GlobusSecurityUtils.getGlobusCredential(gatContext,
                preferences, "gram", hostUri,
                ResourceManagerContact.DEFAULT_PORT);
        } catch (CouldNotInitializeCredentialException e) {
            throw new GATInvocationException("globus", e);
        } catch (CredentialExpiredException e) {
            throw new GATInvocationException("globus", e);
        }

        Sandbox sandbox = new Sandbox(gatContext, preferences, description, host, null, true, true, true);

        String rsl = createRSL(description, host, sandbox);
        GramJob j = new GramJob(credential, rsl);
        GlobusJob res = new GlobusJob(this, description, j, sandbox);
        j.addListener(res);

        try {
            Gram.request(contact, j);
        } catch (GramException e) {
            throw new GATInvocationException("globus", e); // no idea what went wrong 
        } catch (GSSException e2) {
            throw new GATInvocationException("globus",
                new CouldNotInitializeCredentialException("globus", e2));
        }

        return res;
    }

    public static void end() {
        if (GATEngine.DEBUG) {
            System.err.println("globus adaptor end");
        }

        try {
            Gram.deactivateAllCallbackHandlers();
        } catch (Throwable t) {
            if (GATEngine.VERBOSE) {
                System.err
                    .println("WARNING, globus job could not deactivate callback: "
                        + t);
            }
        }
    }
}
