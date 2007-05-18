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

public class GT4ResourceBrokerAdaptor extends ResourceBrokerCpi {
    public GT4ResourceBrokerAdaptor(GATContext gatContext,
				       Preferences preferences) 
	throws GATObjectCreationException {
        super(gatContext, preferences);
    }
    
    protected Service createService(JobDescription jd)
    throws GATInvocationException {
	Service service = new ServiceImpl(Service.JOB_SUBMISSION);
	service.setProvider("GT4.0.0");
	SecurityContext securityContext = null;
	try {
	    securityContext = AbstractionFactory.newSecurityContext("GT4.0.0");
	} catch(InvalidProviderException e) {
	    throw new GATInvocationException("GT4ResourceBrokerAdaptor: " + e);
	} catch(ProviderMethodException e) {
	    throw new GATInvocationException("GT4ResourceBrokerAdaptor: " + e);
	}
	securityContext.setCredentials(null);
	service.setSecurityContext(securityContext);

	ServiceContact serviceContact = new ServiceContactImpl(getHostname(jd));
	service.setServiceContact(serviceContact);
	return service;
    }

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
	System.out.println(sandbox.getSandbox());
// 	Check the return value is null if is not set:
// 	String fn = sandbox.getRelativeStdout().getPath();
	if(sandbox.getRelativeStdin()!=null) {
	    spec.setStdInput(sandbox.getRelativeStdin().getPath());
	}
	if(sandbox.getRelativeStdout()!=null) {
	    spec.setStdOutput(sandbox.getRelativeStdout().getPath());
	    System.out.println(sandbox.getRelativeStdout().getPath());
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
