package org.gridlab.gat.resources.cpi.wsgt4;

import java.util.Map;
import java.util.Set;

import org.globus.exec.generated.JobDescriptionType;
import org.globus.exec.utils.rsl.RSLHelper;
import org.globus.exec.utils.rsl.RSLParseException;
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

/**
 * Implements the <code>ResourceBrokerCpi</code> abstract class.
 * @author Balazs Bokodi
 * @version 1.0
 * @since 1.0
 */
public class WSGT4ResourceBrokerAdaptor extends ResourceBrokerCpi {
    static final int DEFAULT_GRIDFTP_PORT=2811;
    protected GSSCredential getCred(JobDescription jobDescription) 
	throws GATInvocationException {
	GSSCredential cred = null;
	URI location = null;
	try {
	    location = new URI(getHostname(jobDescription));
	} catch(Exception e) {
	    throw new GATInvocationException("WSGT4Job: getSecurityContext, initialization of location failed, " + e);
	}
	try {
	    cred = 
		GlobusSecurityUtils.getGlobusCredential(gatContext, 
							preferences,
							"gt4gridftp", 
							location, 
							DEFAULT_GRIDFTP_PORT);
	} catch(Exception e) {
	    throw new GATInvocationException("WSGT4Job: could not initialize credentials, " + e);
	}
	return cred;
    }

    public WSGT4ResourceBrokerAdaptor(GATContext gatContext,
				    Preferences preferences) 
	throws GATObjectCreationException {
        super(gatContext, preferences);
	if(!GATEngine.VERBOSE) {
	    System.out.println("wsgt4resourcebroker");
	}
    }

    protected String createRSL(JobDescription description, Sandbox sandbox) 
	throws GATInvocationException {
	String rsl =  new String("<job>");
	SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException("The job description does not contain a software description");
        }

	
        getLocationURI(description).getPath();
	
	rsl += "<executable>";
	rsl += getLocationURI(description).getPath();
	rsl += "</executable>";
	
        String[] argsA = getArgumentsArray(description);

        if (argsA != null) {
            for (int i = 0; i < argsA.length; i++) {
                rsl += "<argument>";
		rsl += argsA[i];
		rsl += "</argument>";
            }
        }

        // set the environment
        Map<String, Object> env = sd.getEnvironment();
        if (env != null && !env.isEmpty()) {
            Set<String> s = env.keySet();
            Object[] keys = (Object[]) s.toArray();

            for (int i = 0; i < keys.length; i++) {
                String val = (String) env.get(keys[i]);
		rsl += "<environment>";
		rsl += "<name>" + keys[i] + "</name>";
                rsl += "<value>" + val + "</value>";
		rsl += "</environment>";
            }
        }
	
	rsl += "<count>";
	rsl += getCPUCount(description);
	rsl += "</count>";
	rsl += "<directory>";
        rsl += sandbox.getSandbox();
	rsl += "</directory>";

        org.gridlab.gat.io.File stdout = sd.getStdout();
        if (stdout != null) {
	    rsl += "<stdout>";
            rsl += sandbox.getRelativeStdout().getPath();
	    rsl += "</stdout>";
        }

        org.gridlab.gat.io.File stderr = sd.getStderr();
        if (stderr != null) {
            rsl += "<stderr>";
            rsl += sandbox.getRelativeStderr().getPath();
	    rsl += "</stderr>";
        }

        org.gridlab.gat.io.File stdin = sd.getStdin();
        if (stdin != null) {
	    rsl += "<stdin>";
            rsl += sandbox.getRelativeStdin().getPath();
	    rsl += "</stdin>";
        }

        if (GATEngine.VERBOSE) {
            System.err.println("RSL: " + rsl);
        }

	rsl += "</job>";
        return rsl;
    }

    public Job submitJob(JobDescription description)
        throws GATInvocationException {
	String host = getHostname(description);
	SoftwareDescription sd = description.getSoftwareDescription();
	if(sd == null) {
	    throw new GATInvocationException("WSGT4ResorceBroker: the job description does not contain a software description");
        }
	Sandbox sandbox = new Sandbox(gatContext, preferences, description, host, null, true, true, true, true);
	
	String rsl = createRSL(description, sandbox);
	JobDescriptionType gjobDescription = null;
	System.out.println(rsl);
	try {
	    gjobDescription = RSLHelper.readRSL(rsl);
	} catch(RSLParseException e) {
	    throw new GATInvocationException("WSGT4ResourceBroker: " + e);
	}
	GSSCredential cred = getCred(description);
	return new WSGT4Job(gatContext, 
			    preferences, 
			    description, 
			    sandbox, 
			    gjobDescription,
			    getHostname(description),
			    cred);
    }
}
