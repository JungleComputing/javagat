package org.gridlab.gat.resources.cpi.grms;

import grms_pkg.Grms;
import grms_pkg.GrmsResponse;
import grms_pkg.GrmsServiceLocator;
import grms_schema.Arguments;
import grms_schema.ArgumentsItem;
import grms_schema.Environment;
import grms_schema.Executable;
import grms_schema.ExecutableChoice;
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
import grms_schema.types.LocalrmnameType;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.StringHolder;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.Marshaller;
import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.transport.GSIHTTPSender;
import org.globus.axis.util.Util;
import org.globus.gsi.gssapi.GlobusGSSManagerImpl;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.globus.gsi.gssapi.auth.IdentityAuthorization;
import org.globus.gsi.gssapi.auth.NoAuthorization;
import org.globus.gsi.gssapi.auth.SelfAuthorization;
import org.gridforum.jgss.ExtendedGSSManager;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.IPUtils;
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

		// turn off all annoying cog prints
		if (!GATEngine.DEBUG) {
			Logger logger = Logger.getLogger(IdentityAuthorization.class
					.getName());
			logger.setLevel(Level.OFF);

			Logger logger2 = Logger.getLogger(GlobusGSSManagerImpl.class
					.getName());
			logger2.setLevel(Level.OFF);

			Logger logger3 = Logger
					.getLogger(HostAuthorization.class.getName());
			logger3.setLevel(Level.OFF);

			Logger logger4 = Logger
					.getLogger(SelfAuthorization.class.getName());
			logger4.setLevel(Level.OFF);

			Logger logger5 = Logger.getLogger(NoAuthorization.class.getName());

			logger5.setLevel(Level.OFF);
		}

		try {
			GrmsServiceLocator s = new GrmsServiceLocator();

			// Prepare httpg handler.
			p = new SimpleProvider();
			p.deployTransport("httpg", new SimpleTargetedChain(
					new GSIHTTPSender()));
			s.setEngineConfiguration(p);
			Util.registerTransport();

			grms = s.getgrms();

			// turn on credential delegation, it is turned off by default.
			Stub stub = (Stub) grms;

			ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
					.getInstance();
			GSSCredential cred = manager
					.createCredential(GSSCredential.INITIATE_AND_ACCEPT);

			stub._setProperty(GSIConstants.GSI_CREDENTIALS, cred);
			stub._setProperty(GSIConstants.GSI_MODE,
					GSIConstants.GSI_MODE_FULL_DELEG);

			stub
					._setProperty(
							GSIConstants.GSI_AUTHORIZATION,
							new IdentityAuthorization(
									"/C=PL/O=GRID/O=PSNC/CN=grms_devel/rage1.man.poznan.pl"));

			// set GRMS timeout
			stub.setTimeout(5 * 60 * 1000); // 5 minutes
		} catch (ServiceException e) {
			throw new GATObjectCreationException("grms", e);
		}
	}

	protected void parseHardwareDescription(JobDescription description,
			Grmsjob j) throws GATInvocationException {
		ResourceDescription d = description.getResourceDescription();
		if (d == null) {
			return;
		}

		if (!(d instanceof HardwareResourceDescription)) {
			throw new GATInvocationException(
					"The GRMS adaptor currently only supports hardware resource descriptions");
		}

		Map m = d.getDescription();
		Set keys = m.keySet();
		Iterator i = keys.iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			Object val = m.get(key);

			if (key.equals("machine.node")) {
				Simplejob sj = j.getSimplejob();

				String[] hostList = null;
				if (val instanceof String) {
					hostList = new String[1];
					hostList[0] = (String) val;
				} else {
					hostList = (String[]) val;
				}

				// @@@ not entirely correct: you want to specify a jobmanager
				// per host.
				for (int x = 0; x < hostList.length; x++) {
					Resource r = new Resource();
					sj.addResource(r);
					r.setHostname(hostList[x]);
					String jobManager = (String) preferences
							.get("ResourceBroker.jobmanager");
					if (jobManager != null) {
						if (jobManager.equals("pbs")) {
							r.setLocalrmname(LocalrmnameType.PBS);
						} else if (jobManager.equals("condor")) {
							r.setLocalrmname(LocalrmnameType.CONDOR);
						} else if (jobManager.equals("fork")) {
							r.setLocalrmname(LocalrmnameType.FORK);
						} else if (jobManager.equals("lsf")) {
							r.setLocalrmname(LocalrmnameType.LSF);
						} else {
							throw new GATInvocationException(
									"unknown resource manager");
						}
					}
				}
			} else {
				System.err.println("GRMS: warning, ignoring key: " + key);
			}
		}
	}

	protected String pathToFilename(String path) {
		int index = path.lastIndexOf('/');
		if (index == -1) {
			return path;
		}

		return path.substring(index + 1);
	}

	protected String getOutURI(org.gridlab.gat.io.File f) {
		URI u = f.toURI();
		String host = u.getHost();

		if (host == null) {
			host = IPUtils.getLocalHostName();
		}

		String protocol = u.getScheme();
		if (protocol == null || protocol.equals("file")
				|| protocol.equals("any")) {
			protocol = "gsiftp";
		}

		return protocol + "://" + host + "/" + u.getPath();
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

		ExecutableChoice ec = new ExecutableChoice();
		e.setExecutableChoice(ec);

		File f = new File();
		ec.setFile(f);
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
				ArgumentsItem ai = new ArgumentsItem();
				Value v = new Value();
				ai.setValue(v);
				a.addArgumentsItem(ai);
				v.setContent(args[i]);
			}
		}

		// now add the prestaged files
		Map pre = sd.getPreStaged();
		if (pre != null) {
			if (a == null) {
				a = new Arguments();
				e.setArguments(a);
			}

			Set keys = pre.keySet();
			Iterator i = keys.iterator();
			while (i.hasNext()) {
				org.gridlab.gat.io.File srcFile = (org.gridlab.gat.io.File) i
						.next();
				org.gridlab.gat.io.File destFile = (org.gridlab.gat.io.File) pre
						.get(srcFile);
				if (destFile != null) {
					/*
					 * The user wants his files in a specified location. This
					 * only works if: - The hostname is specified, in the URI -
					 * The job is submitted to a specific machine and the path
					 * is absolute.
					 */
					// TODO at this moment, only allow URIs with a hostname
					if (destFile.toURI().getHost() == null) {
						throw new GATInvocationException(
								"Cannot prestage file without a hostname with grms");
					}

					try {
						if (GATEngine.VERBOSE) {
							System.err
									.println("grms resource broker prestage:");
							System.err.println("  copy " + srcFile.toURI()
									+ " to " + destFile.toURI());
						}
						srcFile.copy(destFile.toURI());
					} catch (Exception exc) {
						throw new GATInvocationException("resource broker cpi",
								exc);
					}
				} else {
					ArgumentsItem ai = new ArgumentsItem();
					File pf = new File();
					ai.setFile(pf);
					a.addArgumentsItem(ai);
					URI u = srcFile.toURI();
					String path = u.getPath();
					String fileName = pathToFilename(path);
					pf.setName(fileName);
					pf.setType(BaseFileTypeType.IN);
					pf.setUrl(u.toString());
				}
			}
		}

		// now add the poststaged files
		Map post = sd.getPostStaged();
		if (post != null) {
			if (a == null) {
				a = new Arguments();
				e.setArguments(a);
			}

			Set keys = pre.keySet();
			Iterator i = keys.iterator();
			while (i.hasNext()) {
				org.gridlab.gat.io.File destFile = (org.gridlab.gat.io.File) i
						.next();
				org.gridlab.gat.io.File srcFile = (org.gridlab.gat.io.File) pre
						.get(destFile);
				if (srcFile != null) { // the user wants his files in a
					// specified
					// location

					try {
						if (GATEngine.VERBOSE) {
							System.err.println("resource broker cpi prestage:");
							System.err.println("  copy " + srcFile.toURI()
									+ " to " + destFile.toURI());
						}
						srcFile.copy(destFile.toURI());
					} catch (Exception exc) {
						throw new GATInvocationException("resource broker cpi",
								exc);
					}
				} else {
					ArgumentsItem ai = new ArgumentsItem();
					File pf = new File();
					ai.setFile(pf);
					a.addArgumentsItem(ai);
					URI u = destFile.toURI();
					String path = u.getPath();
					String fileName = pathToFilename(path);

					pf.setName(fileName);
					pf.setType(BaseFileTypeType.OUT);
					pf.setUrl(getOutURI(destFile));
					//				pf.setUrl(u.toString());
				}
			}
		}

		org.gridlab.gat.io.File stdin = sd.getStdin();
		if (stdin != null) {
			Stdin in = new Stdin();
			e.setStdin(in);
			in.setUrl(stdin.toURI().toString());
		}

		org.gridlab.gat.io.File stdout = sd.getStdout();
		if (stdout != null) {
			Stdout out = new Stdout();
			e.setStdout(out);
			out.setUrl(getOutURI(stdout));
			//			out.setUrl(stdout.toURI().toString());
		}

		org.gridlab.gat.io.File stderr = sd.getStderr();
		if (stderr != null) {
			Stderr err = new Stderr();
			e.setStderr(err);
			err.setUrl(getOutURI(stderr));
			//			err.setUrl(stderr.toURI().toString());
		}

		Map env = sd.getEnvironment();
		if (env != null) {
			Environment grmsenv = new Environment();
			e.setEnvironment(grmsenv);
			Set keys = env.keySet();
			Object[] elts = keys.toArray();
			for (int i = 0; i < elts.length; i++) {
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
			Marshaller m = new Marshaller(sw);
			m.setMarshalExtendedType(false);
			//		    j.marshal(sw);
			m.marshal(j);
			//			if (GATEngine.DEBUG) {
			System.err.println("JOB:\n" + sw);
			//			}
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