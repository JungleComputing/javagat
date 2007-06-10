package org.gridlab.gat.resources.cpi.gt4;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.TimePeriod;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
/**
 * Implements the <code>ResourceBrokerCpi</code> abstract class.
 * @author Balazs Bokodi
 * @version 1.0
 * @since 1.0
 */
public class GT4ResourceBrokerAdaptor extends ResourceBrokerCpi {
    static final int DEFAULT_GRIDFTP_PORT=2811;
    public GT4ResourceBrokerAdaptor(GATContext gatContext,
				    Preferences preferences) 
	throws GATObjectCreationException {
        super(gatContext, preferences);
	//Logger logger = Logger.getLogger(NoAuthorization.class.getName());
	//logger.setLevel(Level.OFF);
	if(!GATEngine.VERBOSE) {
	    System.out.println("gt4resourcebroker");
	}
    }
    
    /**
     * Returns a <code>SecurityContext</code> object.
     * The <code>GlobusSecurityUtils</code> is used to get the
     * credential. If the credential is null, then the <code>SecurityContext</code>
     * tries the default globus credential.
     * @param jobDescription the hostname is extracted from <code>jobDescription</code>.
     * The hostname used by the <code>GlobusSecurityUtils</code>.
     * @throws GATInvocationException
     */
    protected SecurityContext getSecurityContext(JobDescription jobDescription) 
	throws GATInvocationException {
	SecurityContext securityContext = null;
	try {
	    securityContext = AbstractionFactory.newSecurityContext("gt4");
	} catch(Exception e) {
	    throw new GATInvocationException("GT4ResourceBrokerAdaptor: cannot create SecurityContext: "+e);
	}
	GSSCredential cred = null;
	URI location = null;
	try {
	    location = new URI(getHostname(jobDescription));
	} catch(Exception e) {
	    throw new GATInvocationException("GT4ResourceBrokerAdaptor: getSecurityContext, initialization of location failed, "+e);
	}
	try {
	    cred = GlobusSecurityUtils.getGlobusCredential(gatContext, preferences,
							   "gt4gridftp", location, 
							   DEFAULT_GRIDFTP_PORT);
	} catch(Exception e) {
	    throw new GATInvocationException("GT4GridFTPFileAdaptor: could not initialize credentials, " + e);
	}
	securityContext.setCredentials(cred);
	return securityContext;
    }
    /**
     * Creates the job subbmission service.
     * The factory contact string is taken from
     * the variable <code>jd</code>.
     * @param jd the factory contact string is extracted 
     * from the <code>JobDescription</code> with the
     * <code>getHostname</code> method.
     */
    protected Service createService(JobDescription jd)
    throws GATInvocationException {
	Service service = new ServiceImpl(Service.JOB_SUBMISSION);
	service.setProvider("GT4.0.0");
	SecurityContext securityContext = null;
	securityContext = getSecurityContext(jd);

	service.setSecurityContext(securityContext);

	ServiceContact serviceContact = new ServiceContactImpl(getHostname(jd));
	service.setServiceContact(serviceContact);
	return service;
    }

    /**
     * Creates a <code>JobSpecification</code> object.
     * The specifications are get out from the 
     * variable <code>jd</code> and are passed to
     * variable <code>spec</code>, which is a
     * member of JavaCog <code>JobSpecification</code> 
     * class.
     * The <code>JobSpecification</code> is initialized
     * with the relative pathes of sandbox (standard input, output,
     * error, directory), and the enviroment variables are passed also.
     * @param jd
     * @param sandbox initialized with the proper path.
     */
    protected JobSpecification createJobSpecification(JobDescription jd, 
						      Sandbox sandbox) 
	throws GATInvocationException {
	JobSpecification spec = new JobSpecificationImpl();
	SoftwareDescription sd = jd.getSoftwareDescription();
	if(sd == null) {
            throw new GATInvocationException("GT4ResourceBrokerAdaptor: software description is missing");
        }
	String exe = getLocationURI(jd).getPath();
	spec.setExecutable(exe);
	spec.setBatchJob(true);
	String args[] = getArgumentsArray(jd);
	if(args != null) {
	    for(int i=0; i<args.length; i++) {
		spec.addArgument(args[i]);
	    }
	}
	spec.setDirectory(sandbox.getSandbox());
	if(sandbox.getRelativeStdin()!=null) {
	    spec.setStdInput(sandbox.getRelativeStdin().getPath());
	}
	if(sandbox.getRelativeStdout()!=null) {
	    spec.setStdOutput(sandbox.getRelativeStdout().getPath());
	}
	if(sandbox.getRelativeStderr()!=null) {
	    spec.setStdError(sandbox.getRelativeStderr().getPath());
	}

	Map env = sd.getEnvironment();
        if (env != null && !env.isEmpty()) {
            Set s = env.keySet();
            Object[] keys = (Object[]) s.toArray();
            for (int i = 0; i < keys.length; i++) {
                String val = (String) env.get(keys[i]);
		spec.addEnvironmentVariable((String) keys[i], val);
            }
        }

	return spec;
    }

    /**
     * Creates a new <code>GT4Job</code> and submits to globus.
     * Creates the <code>sandbox</code> object also, that is used
     * to isolate job current directory.
     * @param description contains the properies of the job
     * @throws GATInvocationException
     * @throws IOException is not thrown
     * @return GT4Job reference
     */
    public Job submitJob(JobDescription description)
        throws GATInvocationException, IOException {
	String host = getHostname(description);
	SoftwareDescription sd = description.getSoftwareDescription();
	if(sd == null) {
	    throw new GATInvocationException("GT4ResorceBroker: the job description does not contain a software description");
      }
	Sandbox sandbox = new Sandbox(gatContext, preferences, description, host, null, true, true, true, true);
	JobSpecification spec = createJobSpecification(description, sandbox);
	Service service = createService(description);

	return new GT4Job(gatContext, preferences, description, sandbox, spec, service);
    }
}
