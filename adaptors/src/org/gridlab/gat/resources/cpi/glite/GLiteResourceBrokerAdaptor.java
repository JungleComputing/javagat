/*
 * Created on July 25, 2007
 */
package org.gridlab.gat.resources.cpi.glite;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.glite.jdl.JobAd;
import org.glite.jdl.JobAdException;
import org.glite.wms.wmproxy.AuthorizationFaultException;
import org.glite.wms.wmproxy.AuthenticationFaultException;
import org.glite.wms.wmproxy.InvalidArgumentFaultException;
import org.glite.wms.wmproxy.JobIdStructType;
import org.glite.wms.wmproxy.NoSuitableResourcesFaultException;
import org.glite.wms.wmproxy.ServiceException;
import org.glite.wms.wmproxy.StringAndLongList;
import org.glite.wms.wmproxy.StringAndLongType;
import org.glite.wms.wmproxy.WMProxyAPI;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

/**
 * @author anna
 */

public class GLiteResourceBrokerAdaptor extends ResourceBrokerCpi {

	private String delegationId;

	public GLiteResourceBrokerAdaptor(GATContext gatContext,
			Preferences preferences) throws GATObjectCreationException {
		super(gatContext, preferences);
	}

	private String getDelegationId() {
		if (delegationId == null)
			delegationId = "delegationId" + Math.round(Math.random() * 10000.0);
		return delegationId;
	}

	/* connect to WMProxy */
	private WMProxyAPI connectToWMProxy(String delegationId)
			throws GATInvocationException {
		WMProxyAPI client = null;
		try {
			// TODO: where to find host, port and proxy file?
			String host = "mu12.matrix.sara.nl";
			String port = "7443";
			String proxyFile = "/tmp/x509up_u1380";

			client = new WMProxyAPI("https://" + host + ":" + port
					+ "/glite_wms_wmproxy_server", proxyFile);
			// delegation of user credentials
			String proxy = client.grstGetProxyReq(delegationId);
			client.grstPutProxy(delegationId, proxy);
		} catch (Exception e) {
			throw new GATInvocationException(
					"Unable to connect to WMProxy service", e);
		}
		return client;
	}

	/* check correctness of jobAd */
	private void checkJobAd(JobAd jobAd) throws GATInvocationException {
		try {
			jobAd.checkAll();
		} catch (IllegalArgumentException iae) {
			throw new GATInvocationException(
					"Wrong type/format/value for an attribute in JobDescription",
					iae);
		} catch (JobAdException jae) {
			throw new GATInvocationException(
					"One or more values do not match with semantic rule", jae);
		}
	}

	private String getJobType(SoftwareDescription sd, String type) {
		// now only "Normal" type of job is supported
		String jobType = "Normal";
		return jobType;
	}

	private String getExecutable(JobDescription description)
			throws GATInvocationException {
		URI location = getLocationURI(description);
		return location.getPath();
	}

	private void setEnvironment(JobAd jobAd, SoftwareDescription sd)
			throws Exception {
		HashMap environment = (HashMap) sd.getEnvironment();
		if (environment == null)
			return;
		Iterator it = environment.keySet().iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			String value = (String) environment.get(name);
			jobAd.addAttribute("Environment", name + "=" + value);
		}
	}

	private void setStdInputOutputError(JobAd jobAd, SoftwareDescription sd)
			throws Exception {
		File stdin = sd.getStdin();
		File stdout = sd.getStdin();
		File stderr = sd.getStdin();
		// i have some doubts whether to use getName or getPath here
		if (stdin != null)
			jobAd.setAttribute("StdInput", stdin.getPath());
		if (stdout != null)
			jobAd.setAttribute("StdOutput", stdout.getPath());
		if (stderr != null)
			jobAd.setAttribute("StdError", stderr.getPath());
	}

	private void setInputSandbox(JobAd jobAd, SoftwareDescription sd)
			throws Exception {
		HashMap files = (HashMap) sd.getPreStaged();
		if (files == null)
			return;
		Iterator it = files.keySet().iterator();
		while (it.hasNext()) {
			File src = (File) it.next();
			File dest = (File) files.get(src);
			if (dest == null)
				jobAd.addAttribute("InputSandbox", src.getPath());
			else
				jobAd.addAttribute("InputSandbox", dest.getName());
		}
		// all files are put into one directory (without tree structure)
		// maybe names are enough?
	}

	private void setOutputSandbox(JobAd jobAd, SoftwareDescription sd)
			throws Exception {
		HashMap files = (HashMap) sd.getPostStaged();
		if (files == null)
			return;
		Iterator it = files.keySet().iterator();
		while (it.hasNext()) {
			File src = (File) it.next();
			jobAd.addAttribute("OutputSandbox", src.getPath());
		}
	}

	/* create jdl description of the job */
	/* for now only softwareDescription is supported */
	private JobAd createJobDescription(JobDescription description)
			throws GATInvocationException {
		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null)
			throw new GATInvocationException(
					"The job description does not contain a software description");
		JobAd jobAd = new JobAd();
		try {
			// Type options: "Job", "DAG", "Collection" (only Job is now
			// supported)
			jobAd.setAttribute("Type", "Job");
			// JobType options: "Normal", "Interactive", "MPICH",
			// "Partitinable", "Checkpointable", "Parametric"
			// "Partitinable" and "Parametric" options are not available for
			// DAGs and Collections
			jobAd.setAttribute("JobType", getJobType(sd, "Job"));
			// Executable (here only the path not the URI is needed and correct)
			// if executable is not already on the remote CE WN it should be in
			// prestage files
			jobAd.setAttribute("Executable", getExecutable(description));
			// Arguments
			jobAd.setAttribute("Arguments", getArguments(description));
			// Environment
			setEnvironment(jobAd, sd);
			// StdInput, StdOutput, StdError
			setStdInputOutputError(jobAd, sd);
			// InputSandbox
			setInputSandbox(jobAd, sd);
			// OutputSandbox
			setOutputSandbox(jobAd, sd);
			// parsing
			// directory (String): working directory - not supported
			// count (Integer/String): number of executables to run. - not
			// supported
			// hostCount (Integer/String): number of hosts to distribute on. -
			// not supported
			// maxTime (Long/String): maximal time in minutes. - not supported
			// TODO: maxWallTime (Long/String): maximal WALL time in minutes.
			// TODO: maxCPUTime (Long/String): maximal CPU time in minutes.
			// TODO: queue (String): target queue name.
			// project (String): project account to use. - not supported
			// TODO: minMemory (Integer/String): minimal required memory in MB.
			// maxMemory (Integer/String): maximal required memory in MB. - not
			// supported (why use it?)
			// TODO: (check if possible) saveState (Boolean/String): keep job
			// data persistent for restart.
			// TODO: (check if possible) restart=ID (String): restart job with
			// given ID.
		} catch (Exception e) {
			throw new GATInvocationException(
					"Exception while parsing softwareDescription", e);
		}
		checkJobAd(jobAd);
		return jobAd;
	}

	private JobAd createJobDescription(ResourceDescription resourceDescription)
			throws GATInvocationException {
		JobAd jobAd = new JobAd();
		// TODO: creating jdl
		checkJobAd(jobAd);
		return jobAd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#findResources(org.gridlab.gat.resources.ResourceDescription)
	 */
	public List findResources(ResourceDescription resourceDescription)
			throws GATInvocationException {
		// throw new UnsupportedOperationException("Not implemented");
		WMProxyAPI client = connectToWMProxy(delegationId);
		ArrayList resources = null;
		JobAd jobAd = createJobDescription(resourceDescription);
		String jdlString = jobAd.toString();
		// CE Ids satisfying the job Requirements specified in the JDL, ordered
		// according to the decreasing Rank
		StringAndLongList matchingResources = null;
		try {
			matchingResources = client.jobListMatch(jdlString, delegationId);
		} catch (AuthorizationFaultException afe) {
			throw new GATInvocationException(
					"The client is not authorized to perform this operation",
					afe);
		} catch (AuthenticationFaultException aufe) {
			throw new GATInvocationException(
					"A generic authentication problem occurred", aufe);
		} catch (NoSuitableResourcesFaultException nsrfe) {
			// no suitable resources matching job requirements have been found.
			return resources;
		} catch (InvalidArgumentFaultException iafe) {
			throw new GATInvocationException(
					"The given job JDL expression is not valid", iafe);
		} catch (ServiceException se) {
			throw new GATInvocationException(
					"Unknown error occured during the execution of the remote method call to the WMProxy server",
					se);
		} catch (Exception e) {
			throw new GATInvocationException(
					"Unknown error occured during the execution of the remote method call to the WMProxy server",
					e);
		}
		if (matchingResources != null) {
			resources = new ArrayList();
			StringAndLongType[] matchingResourcesList = matchingResources
					.getFile();
			for (int i = 0; i < matchingResourcesList.length; i++) {
				resources.add(matchingResourcesList[i].getName());
			}
		}
		// CE Ids satisfying the job Requirements specified in the JDL
		return resources;
	}

	public boolean isDryRun(JobDescription description) throws GATInvocationException {
		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null)
			throw new GATInvocationException(
					"The job description does not contain a software description");
		HashMap attributes = (HashMap) sd.getAttributes();
		if (attributes == null)
			return false;
		Boolean dryRun = (Boolean) attributes.get("dryRun");
		if (dryRun != null)
			return dryRun.booleanValue();
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
	 */
	public Job submitJob(JobDescription description)
			throws GATInvocationException {
		// throw new UnsupportedOperationException("Not implemented");
		boolean hasPrestageFiles = false;
		String delegationId = getDelegationId();
		WMProxyAPI client = connectToWMProxy(delegationId);
		JobAd jobAd = createJobDescription(description);
		long startTime = System.currentTimeMillis();
		if (isDryRun(description)) {
			GLiteJob job = new GLiteJob(gatContext, preferences, description,
					client, startTime, null);
			job.setState(Job.STOPPED);
			return job;
		}
		String jdlString = jobAd.toString();

		JobIdStructType jobId = null;
		try {
			if (hasPrestageFiles) {
				jobId = client.jobRegister(jdlString, delegationId);
				// TODO: copying files to CE WN
				client.jobStart(jobId.getId());
			} else {
				jobId = client.jobSubmit(jdlString, delegationId);
			}
		} catch (Exception e) {
			throw new GATInvocationException("Exception at job subbmission", e);
		}
		// TODO: create sandbox!!!
		GLiteJob job = new GLiteJob(gatContext, preferences, description, null,
				client, startTime, jobId);
		return job;
	}

}
