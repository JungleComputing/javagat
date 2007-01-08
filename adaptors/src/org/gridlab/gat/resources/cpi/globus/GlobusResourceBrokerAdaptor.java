/*
 * Created on Oct 14, 2004
 */
package org.gridlab.gat.resources.cpi.globus;

import ibis.util.IPUtils;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.globus.common.ResourceManagerContact;
import org.globus.gram.Gram;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.internal.GRAMConstants;
import org.globus.gsi.gssapi.auth.NoAuthorization;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.PostStagedFile;
import org.gridlab.gat.resources.cpi.PostStagedFileSet;
import org.gridlab.gat.resources.cpi.PreStagedFile;
import org.gridlab.gat.resources.cpi.PreStagedFileSet;
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
            Sandbox sandbox, PreStagedFileSet pre, PostStagedFileSet post)
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
                args += ("\"" + argsA[i] + "\" ");
            }
        }

        String rsl = "& (executable = " + exe + ")";

        if (args.length() != 0) {
            rsl += (" (arguments = " + args + ")");
        }

        rsl += " (count = " + getCPUCount(description) + ")";

        rsl += " (hostCount = " + getHostCount(description) + ")";

        if (sandbox != null) {
            rsl += " (directory = " + sandbox.getSandbox() + ")";
        }

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

        // stage in files with gram
        if(pre != null) {
            for(int i=0; i<pre.size(); i++) {
                PreStagedFile f = pre.getFile(i);
                
                if(!f.getResolvedSrc().toGATURI().refersToLocalHost()) {
                    throw new GATInvocationException("Currently, we cannot stage in remote files with gram");
                }
                
                String s = "(file_stage_in = (file:///" + f.getResolvedSrc().getPath() + " " + f.getResolvedDest().getPath() + "))";
                rsl += s;
            }
        }

        if(post != null) {
            for(int i=0; i<post.size(); i++) {
                PostStagedFile f = post.getFile(i);

                if(!f.getResolvedDest().toGATURI().refersToLocalHost()) {
                    throw new GATInvocationException("Currently, we cannot stage out remote files with gram");
                }

                String s = "(file_stage_out = (" + f.getResolvedSrc().getPath() + " gsiftp://" + IPUtils.getLocalHostName() + "/" + f.getResolvedDest().getPath() + "))";
                rsl += s;
            }
        }
        
        org.gridlab.gat.io.File stdout = sd.getStdout();
        if (stdout != null) {
            if (sandbox != null) {
                rsl +=
                        (" (stdout = " + sandbox.getRelativeStdout().getPath() + ")");
            }
        }

        org.gridlab.gat.io.File stderr = sd.getStderr();
        if (stderr != null) {
            if (sandbox != null) {
                rsl +=
                        (" (stderr = " + sandbox.getRelativeStderr().getPath() + ")");
            }
        }

        org.gridlab.gat.io.File stdin = sd.getStdin();
        if (stdin != null) {
            if (sandbox != null) {
                rsl +=
                        (" (stdin = " + sandbox.getRelativeStdin().getPath() + ")");
            }
        }

        // set the environment
        Map env = sd.getEnvironment();
        if (env != null && !env.isEmpty()) {
            Set s = env.keySet();
            Object[] keys = (Object[]) s.toArray();
            rsl += "(environment = ";

            for (int i = 0; i < keys.length; i++) {
                String val = (String) env.get(keys[i]);
                rsl += "(" + keys[i] + " \"" + val + "\")";
            }
            rsl += ")";
        }

        String queue = getStringAttribute(description, "queue", null);
        if(queue != null) {
            rsl += " (queue = " + queue + ")";            
        }
        
        if (GATEngine.VERBOSE) {
            System.err.println("RSL: " + rsl);
        }

        return rsl;
    }

    protected String createChmodRSL(JobDescription description, String host,
            String chmodLocation, Sandbox sandbox, String executable) {
        String rsl = "& (executable = " + chmodLocation + ")";

        rsl += " (arguments = \"+x\" \"" + executable + "\")";

        if (GATEngine.DEBUG) {
            System.err.println("CHMOD RSL: " + rsl);
        }

        return rsl;
    }

    protected String getResourceManagerContact(JobDescription description)
            throws GATInvocationException {
        String res = null;
        String contact = (String) preferences
        .get("ResourceBroker.jobmanagerContact");
        String jobManager = (String) preferences
            .get("ResourceBroker.jobmanager");
        Object jobManagerPort = preferences
        .get("ResourceBroker.jobmanagerPort");

        // if the contact string is set, ignore all other properties
        if(contact != null) {
            return contact;
        }
        
        String hostname = getHostname(description);

        if (hostname != null) {
            res = hostname;

            if(jobManagerPort != null) {
                res += (":" + jobManagerPort);
            }

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

    private void runChmod(GSSCredential credential, JobDescription description,
            String host, String chmodLocation, Sandbox sandbox, File resolvedExe)
            throws GATInvocationException {
        if (GATEngine.VERBOSE) {
            System.err.println("running " + chmodLocation + " on " + host
                + "/jobmanager-fork to set executable bit on");
        }
        String chmodRsl =
                createChmodRSL(description, host, chmodLocation, sandbox,
                    resolvedExe.getPath());
        GramJob j = new GramJob(credential, chmodRsl);
        try {
            Gram.request(host + "/jobmanager-fork", j);
        } catch (GramException e) {
            if (GATEngine.VERBOSE) {
                System.err.println("could not run chmod on executable: "
                    + GramError.getGramErrorString(e.getErrorCode()));
            }
            // ignore
            return;
        } catch (GSSException e2) {
            throw new CouldNotInitializeCredentialException("globus", e2);
        }

        while (true) {
            try {
                Gram.jobStatus(j);
            } catch (Exception e) {
                // ignore
            }
            int status = j.getStatus();

            if (GATEngine.DEBUG) {
                System.err.println("chmod status = " + status);
            }
            if (status == GRAMConstants.STATUS_DONE) {
                return;
            }
            if (status == GRAMConstants.STATUS_FAILED) {
                return;
            }

            try {
                Thread.sleep(500);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public Job submitJob(JobDescription description)
            throws GATInvocationException {

        boolean useGramSandbox = false;
        
        String s = (String) preferences.get("useGramSandbox");
        if(s != null && s.equalsIgnoreCase("true")) {
            useGramSandbox = true;
        }
        
        if (useGramSandbox) {
            return submitJobNoSandbox(description);
        } else {
            return submitJobSandbox(description);
        }
    }

    public Job submitJobSandbox(JobDescription description)
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
            credential =
                    GlobusSecurityUtils.getGlobusCredential(gatContext,
                        preferences, "gram", hostUri,
                        ResourceManagerContact.DEFAULT_PORT);
        } catch (CouldNotInitializeCredentialException e) {
            throw new GATInvocationException("globus", e);
        } catch (CredentialExpiredException e) {
            throw new GATInvocationException("globus", e);
        }

        Sandbox sandbox =
                new Sandbox(gatContext, preferences, description, host, null,
                    true, true, true, true);

        // If we staged in the executable, we have to do a chmod.
        // Globus loses the executable bit :-(
        File resolvedExe = sandbox.getResolvedExecutable();
        if (resolvedExe != null) {
            try {
                runChmod(credential, description, host, "/bin/chmod", sandbox,
                    resolvedExe);
            } catch (Exception e) {
                // ignore
            }
            try {
                runChmod(credential, description, host, "/usr/bin/chmod",
                    sandbox, resolvedExe);
            } catch (Exception e) {
                // ignore
            }
        }

        String rsl = createRSL(description, host, sandbox, null, null);
        GramJob j = new GramJob(credential, rsl);
        GlobusJob res =
                new GlobusJob(gatContext, preferences, this, description, j,
                    sandbox);
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

    public Job submitJobNoSandbox(JobDescription description)
            throws GATInvocationException {
        System.err.println("HOST1");
        String host = getHostname(description);
        System.err.println("HOST2: " + host);
        String contact = getResourceManagerContact(description);
        System.err.println("HOST_DONE: " + host);

        
        URI hostUri;
        try {
            hostUri = new URI(host);
        } catch (Exception e) {
            throw new GATInvocationException("globus broker", e);
        }

        GSSCredential credential = null;
        try {
            credential =
                    GlobusSecurityUtils.getGlobusCredential(gatContext,
                        preferences, "gram", hostUri,
                        ResourceManagerContact.DEFAULT_PORT);
        } catch (CouldNotInitializeCredentialException e) {
            throw new GATInvocationException("globus", e);
        } catch (CredentialExpiredException e) {
            throw new GATInvocationException("globus", e);
        }

        PreStagedFileSet pre =
                new PreStagedFileSet(gatContext, preferences, description,
                    host, null, false);

        PostStagedFileSet post = new PostStagedFileSet(gatContext, preferences, description,
            host, null, false, false);

        String rsl = createRSL(description, host, null, pre, post);
        GramJob j = new GramJob(credential, rsl);
        GlobusJob res =
                new GlobusJob(gatContext, preferences, this, description, j,
                    null);
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
