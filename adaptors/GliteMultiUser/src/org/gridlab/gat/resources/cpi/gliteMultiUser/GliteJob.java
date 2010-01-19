package org.gridlab.gat.resources.cpi.gliteMultiUser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.glite.wms.wmproxy.JobIdStructType;
import org.glite.wms.wmproxy.WMProxyAPI;
import org.globus.gsi.GlobusCredential;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.security.gliteMultiUser.GliteSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class GliteJob extends JobCpi {

	/**
	 * The Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GliteJob.class);

	/**
	 * The job description
	 */
	private JDL gLiteJobDescription;

	/**
	 * The job software description
	 */
	private SoftwareDescription swDescription;

	/** The job state */
	private volatile String gLiteState = null;

	/** The {@link URL} to the Workload Management System (WMS) */
	private URL wmsURL = null;

	/**
	 * is <code>true</code> if the Job is done and the poststaging has been
	 * done.
	 */
	private boolean outputDone = false;

	/** The id of the job. */
	private String gliteJobID;

	/** The WMProxy client */
	private WMProxyAPI api;

	/** The submission-time of the job */
	private volatile long submissiontime = -1L;

	/** The start-time of the job */
	private volatile long starttime = -1L;

	/** The stop-time of the job */
	private volatile long stoptime = -1L;

	/** An exception that might occurs. */
	private volatile GATInvocationException postStageException = null;

	/** The address of the CE where the job will be executed. */
	private volatile String destination = null;

	/** An id for the delegated voms-proxy-certificate */
	private final static String DELEGATION_ID = "delegationId";

	/** The path to the voms-proxy */
	private String vomsProxyPath = "";

	/** The voms proxy instance */
	private GlobusCredential vomsProxy = null;

	/**
	 * @param gatContext
	 * @param jobDescription
	 * @param sandbox
	 * @param brokerURI
	 * @throws GATInvocationException
	 * @throws GATObjectCreationException
	 */
	protected GliteJob(final GATContext gatContext, final JobDescription jobDescription, final Sandbox sandbox,
			final String brokerURI) throws GATInvocationException, GATObjectCreationException {
		super(gatContext, jobDescription, sandbox);

		// make it work with the axis services
		// the axis service will only accept the uri if the protocol is
		// known to them while any:// is not known to them, https:// will work
		String axisBrokerURI = brokerURI.replaceFirst("any://", "https://");

		try {
			this.wmsURL = new URL(axisBrokerURI);
			LOGGER.info("Create a new GLiteJob instance. The WMS-URL is: " + wmsURL.toString());
		} catch (MalformedURLException e) {
			throw new GATInvocationException("WMS-URL is not valid!", e);
		}

		// Create a voms-proxy instance for this job which is stored in proxy
		// directory.
		vomsProxy = GliteSecurityUtils.getVOMSProxy(gatContext, true);

		vomsProxyPath = GliteSecurityUtils.getPathToUserVomsProxy(gatContext);

		this.swDescription = this.jobDescription.getSoftwareDescription();

		if (swDescription.getExecutable() == null) {
			throw new GATInvocationException("The Job description does not contain an executable");
		}

		// Create Job Description Language File ...
		long jdlID = System.currentTimeMillis();
		String voName = GliteConstants.getVO(gatContext);

		ResourceDescription rd = jobDescription.getResourceDescription();
		this.gLiteJobDescription = new JDL(jdlID, swDescription, voName, rd);
	}

	/**
	 * Returns an instance of {@link WMProxyAPI}. If its not exits the method
	 * creates a new instance.
	 * 
	 * @return an instance of {@link WMProxyAPI}
	 * @throws GATInvocationException an exception that might occurs.
	 */
	private WMProxyAPI getWmProxy() throws GATInvocationException {
		if (api == null) {
			try {

				final String caDirectory = System.getProperty("CADIR");

				if (null == caDirectory || caDirectory.isEmpty()) {
					throw new GATInvocationException("No CA directory is specified as System property!");
				}

				api = new WMProxyAPI(wmsURL.toString(), vomsProxyPath, caDirectory);
				String proxy = api.grstGetProxyReq(DELEGATION_ID);
				api.grstPutProxy(DELEGATION_ID, proxy);
			} catch (Exception e) {
				throw new GATInvocationException("An error occurs during creating a new instance of GLiteJob.");
			}
		}

		return api;
	}

	/**
	 * JobSubmit via API
	 * {@link ResourceBroker#submitJob(org.gridlab.gat.resources.AbstractJobDescription)}
	 * 
	 * @return the id of the job
	 * @throws GATInvocationException An exception that might occurs
	 * @throws GATObjectCreationException An exception that might occurs
	 */
	public String submitJob() throws GATInvocationException, GATObjectCreationException {
		try {
			JobIdStructType jobIds = getWmProxy().jobSubmit(gLiteJobDescription.getJdlString(), DELEGATION_ID);

			gliteJobID = jobIds.getId();

			LOGGER.info("Job sucessfully submitted with id: " + gliteJobID);

			return gliteJobID;
		} catch (Exception e) {
			throw new GATInvocationException("Ann error occurs during submitting the job.", e);
		}
	}

	/**
	 * @see JobCpi#getInfo()
	 */
	public Map<String, Object> getInfo() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("state", this.state);
		map.put("glite.state", this.gLiteState);
		map.put("jobID", jobID);
		map.put("adaptor.job.id", gliteJobID);
		map.put("submissiontime", submissiontime);
		map.put("starttime", starttime);
		map.put("stoptime", stoptime);
		map.put("poststage.exception", postStageException);
		if (state == JobState.RUNNING) {
			map.put("hostname", destination);
		}
		map.put("glite.destination", destination);
		return map;
	}

	/**
	 * Queries the current state of the job via WebService.
	 */
	private void queryState() {
		// if (outputDone) { // API is ready with POST STAGING
		// this.gLiteState = "Cleared";
		// } else {
		// try {
		// final JobStatus js = lbPortType.jobStatus(gliteJobID, new
		// JobFlags());
		// final StatName state = js.getState();
		// this.gLiteState = state.toString();
		// this.destination = js.getDestination();
		// } catch (GenericFault e) {
		// LOGGER.error(e.toString());
		// } catch (RemoteException e) {
		// LOGGER.error("gLite Error: LoggingAndBookkeeping service only works in glite 3.1 or higher",
		// e);
		// }
		// }
	}

	/**
	 * Update the status of the job. Depended on the requested state some
	 * action, like file staging or sandbox cleanup, has to be done.
	 */
	private synchronized void updateState() {

		queryState();

		if ("Waiting".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.INITIAL;
		} else if ("Ready".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.INITIAL;
		} else if ("Scheduled".equalsIgnoreCase(gLiteState)) {

			// if state appears the first time, set the submission time
			// appropriately
			if (submissiontime == -1L) {
				submissiontime = System.currentTimeMillis();
			}

			state = Job.JobState.SCHEDULED;
		} else if ("Running".equalsIgnoreCase(gLiteState)) {

			// sometimes, scheduled state is skipped
			if (submissiontime == -1L) {
				submissiontime = System.currentTimeMillis();
			}

			// if running for the first time, set the start time appropriately
			if (starttime == -1L) {
				starttime = System.currentTimeMillis();
			}

			state = Job.JobState.RUNNING;
		} else if ("Done (Failed)".equalsIgnoreCase(gLiteState)) {

			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}

			state = Job.JobState.SUBMISSION_ERROR;
		} else if ("Submitted".equalsIgnoreCase(gLiteState)) {

			if (submissiontime == -1L) {
				submissiontime = System.currentTimeMillis();
			}

			state = Job.JobState.INITIAL;
		} else if ("Aborted".equalsIgnoreCase(gLiteState)) {

			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}

			state = Job.JobState.SUBMISSION_ERROR;
		} else if ("DONE".equalsIgnoreCase(gLiteState)) {
			state = Job.JobState.POST_STAGING;
		} else if ("Done (Success)".equalsIgnoreCase(gLiteState)) {

			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}

			state = Job.JobState.POST_STAGING;
		} else if ("Cancelled".equalsIgnoreCase(gLiteState)) {

			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}

			state = Job.JobState.SUBMISSION_ERROR;
		} else if ("Cleared".equalsIgnoreCase(gLiteState)) {

			if (stoptime == -1L) {
				stoptime = System.currentTimeMillis();
			}

			state = Job.JobState.STOPPED;
		} else {
			this.state = Job.JobState.UNKNOWN;
		}
	}

	/**
	 * Copies the output files like stdout and stderr to the defined URI via
	 * gridftp.
	 */
	public void receiveOutput() {
		// StringAndLongType[] list = null;
		//
		// try {
		// StringAndLongList sl = serviceStub.getOutputFileList(gliteJobID,
		// "gsiftp");
		// list = sl.getFile();
		// } catch (Exception e) {
		// LOGGER.error("Could not receive output due to security problems", e);
		// }
		//
		// if (list != null) {
		// GATContext newContext = (GATContext) gatContext.clone();
		// newContext.addPreference("File.adaptor.name", "GridFTP");
		// // Remove all the existing contexts and use the new one..
		// //
		// GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext);
		//
		// for (int i = 0; i < list.length; i++) {
		// try {
		// URI uri1 = new URI(list[i].getName());
		// URI uri2 = new URI(uri1.getScheme() + "://" + uri1.getHost() + ":" +
		// uri1.getPort() + "//"
		// + uri1.getPath());
		//
		// File f = GAT.createFile(newContext, uri2);
		// int name_begin = uri2.getPath().lastIndexOf('/') + 1;
		// File f2 = GAT.createFile(newContext, new
		// URI(uri2.getPath().substring(name_begin)));
		//
		// f.copy(destForPostStagedFile(f2));
		// } catch (GATInvocationException e) {
		// postStageException = e;
		// LOGGER.error(e.toString());
		// } catch (URISyntaxException e) {
		// postStageException = new GATInvocationException(e.toString());
		// LOGGER.error("An error occured when building URIs for the poststaged files",
		// e);
		// } catch (GATObjectCreationException e) {
		// postStageException = new GATInvocationException(e.toString());
		// LOGGER.error("Could not create GAT file when retrieving output", e);
		// }
		// }
		// }
		// outputDone = true;
	}

	/**
	 * Lookup the (local) destination to where the staged out file should be
	 * copied
	 * 
	 * @param output The staged out file
	 * @return The URI on the local harddrive to where the file should be copied
	 */
	private URI destForPostStagedFile(File output) {
		Map<File, File> postStagedFiles = swDescription.getPostStaged();
		File stdout = swDescription.getStdout();
		File stderr = swDescription.getStderr();
		String outputName = output.getName();
		URI destURI = null;

		if (stdout != null && outputName.equals(stdout.getName())) {
			destURI = stdout.toGATURI();
		} else if (stderr != null && outputName.equals(stderr.getName())) {
			destURI = stderr.toGATURI();
		} else {

			for (Map.Entry<File, File> psFile : postStagedFiles.entrySet()) {
				if (psFile != null && psFile.getValue() != null && psFile.getKey() != null) {
					String psFileName = psFile.getKey().getName();

					if (outputName.equals(psFileName)) {
						destURI = psFile.getValue().toGATURI();
						break;
					}
				}
			}
		}

		if (destURI == null) {
			destURI = output.toGATURI();
		}

		return destURI;
	}

	/**
	 * Stop the job submitted by this class Independent from whether the WMS
	 * will actually cancel the job and report the CANCELLED state back, the
	 * JobStatus poll thread will terminate after a fixed number of job updates
	 * after this has been called. The time interval the lookup thread will
	 * still wait till cancelation ater calling stop() is defined in the
	 * UDPATE_INTV_AFTER_JOB_KILL variable in the JobStatusLookUp Thread. This
	 * has become necessary because some jobs would hang forever in a state even
	 * after calling the stop method.
	 */
	public void stop() throws GATInvocationException {
		if (state == JobState.POST_STAGING || state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) {
			return;
		}

		try {
			getWmProxy().jobCancel(DELEGATION_ID);
		} catch (Exception e) {
			throw new GATInvocationException("Could not cancel job!", e);
		}
	}

}
