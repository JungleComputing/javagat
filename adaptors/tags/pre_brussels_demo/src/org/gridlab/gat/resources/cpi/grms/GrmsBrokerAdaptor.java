package org.gridlab.gat.resources.cpi.grms;

import grms_pkg.Grms;
import grms_pkg.GrmsResponse;
import grms_pkg.GrmsServiceLocator;
import grms_schema.Arguments;
import grms_schema.Environment;
import grms_schema.Executable;
import grms_schema.File;
import grms_schema.Grmsjob;
import grms_schema.Resource;
import grms_schema.Simplejob;
import grms_schema.Stderr;
import grms_schema.Stdin;
import grms_schema.Stdout;
import grms_schema.Value;
import grms_schema.Variable;
import grms_schema.types.BaseFileTypeType;
import grms_schema.types.ExecutableTypeType;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.StringHolder;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;
import org.globus.axis.transport.GSIHTTPSender;
import org.globus.axis.transport.GSIHTTPTransport;
import org.globus.axis.util.Util;
import org.globus.gsi.gssapi.auth.NoAuthorization;
import org.gridforum.jgss.ExtendedGSSManager;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.ietf.jgss.GSSCredential;

/**
 * @author rob
 */
public class GrmsBrokerAdaptor extends ResourceBrokerCpi {

	SimpleProvider p;

	Grms grms;

	/**
	 * @param gatContext
	 * @param preferences
	 * @throws Exception
	 */
	public GrmsBrokerAdaptor(GATContext gatContext, Preferences preferences)
			throws Exception {
		super(gatContext, preferences);

		checkName("grms");

		try {
			/*
			 * GrmsService grmsService = new GrmsService();
			 * grmsService.setProxy(cred);
			 * grmsService.setDelegationType((String)
			 * GSIConstants.GSI_MODE_FULL_DELEG);
			 * grmsService.setAuthorization((Authorization) new
			 * NoAuthorization()); grmsService.setTimeout(minutes * 60 * 1000);
			 * grmsService.setUrl(GRMSServiceURL); grms = grmsService.getgrms();
			 */

			/*
			 * stub._setProperty(GSIHTTPTransport.GSI_AUTHORIZATION, new
			 * org.globus.gsi.gssapi.auth.IdentityAuthorization(/C=PL/O=GRID/O=PSNC/CN=grms_devel/rage1.man.poznan.pl));
			 * 
			 * and
			 * 
			 * stub._setProperty(GSIHTTPTransport.GSI_AUTHORIZATION, new
			 * org.globus.gsi.gssapi.auth.NoAuthorization());
			 */

			// Prepare httpg handler.
			p = new SimpleProvider();
			p.deployTransport("httpg", new SimpleTargetedChain(
					new GSIHTTPSender()));
			Util.registerTransport();
			GrmsServiceLocator s = new GrmsServiceLocator();
			s.setEngineConfiguration(p);
			
			grms = s.getgrms();

			// turn on credential delegation, it is turned off by default.
			Stub stub = (Stub) grms;

			ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
					.getInstance();
			GSSCredential cred = manager
					.createCredential(GSSCredential.INITIATE_AND_ACCEPT);

			stub._setProperty(GSIHTTPTransport.GSI_CREDENTIALS, cred);
			stub._setProperty(GSIHTTPTransport.GSI_MODE,
					GSIHTTPTransport.GSI_MODE_FULL_DELEG);
			stub._setProperty(GSIHTTPTransport.GSI_AUTHORIZATION,
					new NoAuthorization());

		} catch (ServiceException e) {
			throw new AdaptorCreationException(e);
		}
	}

	protected void parseHardwareDescription(JobDescription description,
			Grmsjob j) throws GATInvocationException {
		ResourceDescription d = description.getResourceDescription();
		if (d == null) {
			return;
		}
		
		if(!(d instanceof HardwareResourceDescription)) {
			throw new GATInvocationException(
			"The GRMS adaptor currently only supports harware resource descriptions");
		}
		
		HardwareResourceDescription hd = (HardwareResourceDescription) d;
		Map m = d.getDescription();
		Set keys = m.keySet();
		Iterator i = keys.iterator();
		while(i.hasNext()) {
			String key = (String) i.next();
			String val = (String) m.get(key);
			
			if(key.equals("machine.node")) {
				Simplejob sj = j.getSimplejob();

				Resource r = new Resource();
				sj.setResource(r);
				r.setHostname(val);
			} else {
				System.err.println("GRMS: warning, ignoring key: " + key);
			}
		}
	}

	protected String pathToFilename(String path) {
		int index = path.lastIndexOf('/');
		if(index == -1) {
			return path;
		}
		
		return path.substring(index+1);
	}
	
	protected void parseSoftwareDescription(JobDescription description,
			Grmsjob j) throws GATInvocationException {
		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		// @@@ for now we only do simple Jobs.
		Simplejob sj = new Simplejob();
		j.setSimplejob(sj);

		Executable e = new Executable();
		sj.setExecutable(e);

		// The executable type can always be changed later...
		e.setType(ExecutableTypeType.SINGLE);
		e.setCount(1);

		File f = new File();
		e.setFile(f);
		f.setName(pathToFilename(getLocation(description)));
		f.setType(BaseFileTypeType.IN);
		f.setUrl(getLocation(description));

		// parse the arguments
		Arguments a = null;
		String[] args = getArgumentsArray(description);
		if (args != null) {
			a = new Arguments();
			e.setArguments(a);

			for (int i = 0; i < args.length; i++) {
				Value v = new Value();
				a.addValue(v);
				v.setContent(args[i]);
			}
		}

		// now add the prestaged files
		org.gridlab.gat.io.File[] pre = sd.getPreStaged();
		if (pre != null) {
			if (a == null) {
				a = new Arguments();
				e.setArguments(a);
			}

			for (int i = 0; i < pre.length; i++) {
				File pf = new File();
				a.addFile(pf);
				URI u = pre[i].toURI();
				String path = u.getPath();
				String fileName = pathToFilename(path);
				pf.setName(fileName);
				pf.setType(BaseFileTypeType.IN);
				pf.setUrl(u.toString());
			}
		}

		// now add the poststaged files
		org.gridlab.gat.io.File[] post = sd.getPostStaged();
		if (post != null) {
			if (a == null) {
				a = new Arguments();
				e.setArguments(a);
			}

			for (int i = 0; i < pre.length; i++) {
				File pf = new File();
				a.addFile(pf);
				URI u = post[i].toURI();
				String path = u.getPath();
				String fileName = pathToFilename(path);

				pf.setName(fileName);
				pf.setType(BaseFileTypeType.OUT);
				pf.setUrl(u.toString());
			}
		}

		org.gridlab.gat.io.File stdin = sd.getStdin();
		if(stdin != null) {
			Stdin in = new Stdin();
			e.setStdin(in);
				in.setUrl(stdin.toURI().toString());
		}
		
		org.gridlab.gat.io.File stdout = sd.getStdout();
		if(stdout != null) {
			Stdout out = new Stdout();
			e.setStdout(out);
			out.setUrl(stdout.toURI().toString());
		}
		
		org.gridlab.gat.io.File stderr = sd.getStderr();
		if(stderr != null) {
			Stderr err = new Stderr();
			e.setStderr(err);
			err.setUrl(stderr.toURI().toString());
		}
		
		Map env = sd.getEnvironment();
		if(env != null) {
			Environment grmsenv = new Environment();
			e.setEnvironment(grmsenv);
			Set keys = env.keySet();
			Object[] elts = keys.toArray();
			for(int i=0; i<elts.length; i++) {
				String key = (String) elts[i];
				String val = (String) env.get(key);
				Variable var = new Variable();
				var.setName(key);
				var.setContent(val);
				grmsenv.addVariable(var);
			}
		}
	}

	protected String createGrmsJobDescription(JobDescription description)
			throws GATInvocationException {
		Grmsjob j = new Grmsjob();
		j.setAppid("grms_job");

		parseSoftwareDescription(description, j);

		parseHardwareDescription(description, j);
		
		// Now create the XML string
		StringWriter sw = new StringWriter();

		try {
			j.marshal(sw);
			if(GATEngine.DEBUG) {
				System.err.println("JOB:\n" + sw);
			}
		} catch (Exception ex) {
			System.err.println("VALIDATION ERROR ON JOB");
			throw new GATInvocationException(
					"validation error creating grms job");
		}

		return sw.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
	 */
	public Job submitJob(JobDescription description)
			throws GATInvocationException, IOException {
		String jobDescription = createGrmsJobDescription(description);

		// Return Value: - GrmsResponse.errorCode
		// 0 - success >0 - error code
		// Also returns jobId in jobId field.
		StringHolder jobId = new StringHolder();
		GrmsResponse res = grms.submitJob(jobDescription, jobId);

		if (res.getErrorCode() != 0) {
			throw new GATInvocationException(res.getErrorMessage());
		}

		return new GrmsJob(this, description, jobId.value);
	}

	Grms getGrms() {
		return grms;
	}
}