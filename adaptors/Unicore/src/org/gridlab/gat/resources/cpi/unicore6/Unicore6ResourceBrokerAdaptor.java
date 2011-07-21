package org.gridlab.gat.resources.cpi.unicore6;

import java.util.List;
import java.util.Map;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.security.unicore6.HiLAHelper;
import org.gridlab.gat.resources.security.unicore6.Unicore6SecurityUtils;
import org.gridlab.gat.security.AssertionSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unicore.hila.Location;
import eu.unicore.hila.Resource;
import eu.unicore.hila.exceptions.HiLAException;
import eu.unicore.hila.exceptions.HiLALocationSyntaxException;
import eu.unicore.hila.grid.Site;
import eu.unicore.hila.job.model.JobModel;

/**
 * Resource Broker Adaptor for Unicore6.
 * 
 * @author Andreas Bender
 * 
 */
public class Unicore6ResourceBrokerAdaptor extends ResourceBrokerCpi {

	protected static final Logger LOGGER = LoggerFactory.getLogger(Unicore6ResourceBrokerAdaptor.class);

	protected String username = null;

	protected List<String> registries = null;

	/**
	 * @see ResourceBrokerCpi#getSupportedCapabilities()
	 */
	public static Map<String, Boolean> getSupportedCapabilities() {
		Map<String, Boolean> capabilities = ResourceBrokerCpi.getSupportedCapabilities();
		capabilities.put("findResources", true);
		capabilities.put("submitJob", true);

		return capabilities;
	}

	// /**
	// * @see ResourceBrokerCpi#getSupportedPreferences()
	// */
	// public static Preferences getSupportedPreferences() {
	// Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();
	//
	// preferences.put("registries", "true");
	//
	// return preferences;
	// }

	/**
	 * Constructor.
	 * 
	 * @param gatContext the GAT context
	 * @param brokerURI the broker URI like "unicore6:/<user-name>@sites/<site-name>"
	 * @throws GATObjectCreationException an exception might occur during creation
	 * @throws GATInvocationException is thrown when no registry is defined
	 */
	@SuppressWarnings("unchecked")
	public Unicore6ResourceBrokerAdaptor(GATContext gatContext, URI brokerURI) throws GATObjectCreationException,
			GATInvocationException {

		super(gatContext, brokerURI);

		if (brokerURI.getScheme() == null) {
			throw new AdaptorNotApplicableException("cannot handle this URI: " + brokerURI);
		}
		if (!brokerURI.refersToLocalHost()) {
			throw new AdaptorNotApplicableException("cannot handle this URI: " + brokerURI);
		}

		for (SecurityContext securityContext : gatContext.getSecurityContexts()) {
			if (securityContext instanceof AssertionSecurityContext) {
				username = securityContext.getUsername();
			}
		}

		registries = (List<String>) gatContext.getPreferences().get("registries");
		if (registries == null) {// registries may also be available in the HiLA preferences file
			throw new GATInvocationException("gatContext.getPreferences().get(\"registries\") is null");
		}
	}

	/**
	 * Registers an unmarshaller.
	 */
	public static void init() {
		GATEngine.registerUnmarshaller(Unicore6Job.class);
	}

	/**
	 * Creates the JSDL represented by a {@link JobDescription} based on the given {@link SoftwareDescription}.
	 * 
	 * @param sd the software description with the job parameters
	 * @return the job description
	 * @throws GATInvocationException an exception might occur during creation
	 */
	public JobModel createJobDescription(SoftwareDescription sd) throws GATInvocationException {

		JobModel model = new JobModel();

		model.setTaskName("Unicore-GAT-" + sd.getExecutable());		
		model.setExecutable(sd.getExecutable());

		String[] jobArgs = sd.getArguments();

		if (jobArgs != null) {
			for (String argument : jobArgs) {
				model.addArgument(argument);
			}
		}
		
		// Map<org.gridlab.gat.io.File, org.gridlab.gat.io.File>
		// preStagedFiles=sd.getPreStaged();
		// for(Entry<org.gridlab.gat.io.File, org.gridlab.gat.io.File>
		// preStagedFile:preStagedFiles.entrySet()){
		// jsdlBuilder.addImport(File.preStagedFile.getKey().getAbsoluteFile(),
		// preStagedFile.getValue().getAbsolutePath());
		// }unnecessary?

		// ResourcesType resources = ResourcesType.Factory.newInstance();
		// FileSystemType fileSystem = resources.addNewFileSystem();
		// fileSystem.setName("USPACE");// is this correct?
		// fileSystem.setDescription("Unicore working dir");
		// jsdlBuilder.setResources(resources);

		// JobDefinitionDocument tempJobDefDocument = null;
		//
		// try {
		// //model.
		// tempJobDefDocument = jsdlBuilder.getJsdl();
		// } catch (HiLAException e) {
		// throw new GATInvocationException("Unicore JSDL handling exception", e);
		// }
		//
		// final JobDefinitionDocument jobDefDocument = tempJobDefDocument;
		//
		// eu.unicore.hila.grid.job.JobDescription jobDescription = new eu.unicore.hila.grid.job.JobDescription() {
		// public Object getActualJobDescription() {
		// return jobDefDocument;
		// }
		// };

		return model;
	}

	/**
	 * Returns the available site names.
	 * 
	 * @param resourceDescription
	 * @return
	 * @throws GATInvocationException
	 */
	public List<HardwareResource> findResources(ResourceDescription resourceDescription) throws GATInvocationException {
		initSecurity(gatContext);

		List<HardwareResource> resources = HiLAHelper.findResources(gatContext, registries, username);
		return resources;
	}

	/**
	 * @see ResourceBrokerCpi#submitJob(AbstractJobDescription, MetricListener, String)
	 */
	public Job submitJob(AbstractJobDescription abstractDescription, MetricListener listener,
			String metricDefinitionName) throws GATInvocationException {

		if (!(abstractDescription instanceof JobDescription)) {
			throw new GATInvocationException("can only handle JobDescriptions: " + abstractDescription.getClass());
		}

		JobDescription unicoreJobDescr = (JobDescription) abstractDescription;

		SoftwareDescription softwareDescr = unicoreJobDescr.getSoftwareDescription();

		String brokerPath = "unicore6:" + brokerURI.getPath(); // The path of the uri as submitted by the user

		// get sitename
//		String siteNameWithTimeStamp = HiLAHelper.getCurrentSiteNameWithTimeStamp(gatContext, registries, username,
//				brokerPath.substring(brokerPath.lastIndexOf('/') + 1));
//
//		if (null == siteNameWithTimeStamp) {
//			throw new GATInvocationException("Cannot retrieve sitename with a timestamp!");
//		}

//		//The uri as submitted by the user
//		String siteUri = "unicore6:" + brokerPath.substring(0, brokerPath.lastIndexOf('/') + 1) + siteNameWithTimeStamp; 

		LOGGER.debug("Unicore adaptor will use '" + brokerPath + "' to submit the job");

		Unicore6Job unicoreJob = new Unicore6Job(gatContext, unicoreJobDescr, null);

		if (listener != null && metricDefinitionName != null) {
			Metric metric = unicoreJob.getMetricDefinitionByName(metricDefinitionName).createMetric(null);
			unicoreJob.addMetricListener(listener, metric);
		}

		try {
			Resource resource = new Location(brokerPath).locate();

			if (resource instanceof Site) {
				unicoreJob.setSite((Site) resource);
			} else {
				throw new GATInvocationException("Specified URI is not a site location.");
			}

			if (unicoreJob.getSite() == null) {
				throw new GATInvocationException("UnicoreResourceBrokerAdaptor unicoreJob.site = null");
			}

			eu.unicore.hila.grid.Job hilaJob = unicoreJob.getSite().submit(createJobDescription(softwareDescr));
			unicoreJob.setSubmissionTime();

			prestageFiles(softwareDescr, hilaJob.getWorkingDirectory());

			hilaJob.startASync();
			// Metadata metaData = hilaJob.getMetadata();
			String taskName = hilaJob.getTaskName();

			unicoreJob.setState(Job.JobState.SCHEDULED);
			unicoreJob.setJob(hilaJob);
			unicoreJob.setSoftwareDescription(softwareDescr);
			unicoreJob.setJobID(hilaJob.getId());
		} catch (HiLALocationSyntaxException e) {
			// e.printStackTrace();
			throw new GATInvocationException("Unicore6ResourceBrokerAdaptor", e);
		} catch (HiLAException e) {
			// e.printStackTrace();
			throw new GATInvocationException("Unicore6ResourceBrokerAdaptor", e);
		}
		return unicoreJob;
	}

	/**
	 * Initialize the security
	 * 
	 * @param gatContext the GAT context
	 * @throws GATInvocationException an exception that might occurs
	 */
	private void initSecurity(GATContext gatContext) throws GATInvocationException {
		Unicore6SecurityUtils.saveAssertion(gatContext);
	}

	/**
	 * Stages the input files to the unicore working directory.
	 * 
	 * @param sd the software description
	 * @param workingDir the working Directory
	 * @throws GATInvocationException
	 */
	private void prestageFiles(SoftwareDescription sd, eu.unicore.hila.grid.File workingDir)
			throws GATInvocationException {

		java.io.File locFile;

		Map<org.gridlab.gat.io.File, org.gridlab.gat.io.File> preStaged = null;
		/**
		 * * fill in with elements of software description. Right now this concerns pre and poststaging dataset.
		 */
		preStaged = sd.getPreStaged();
		if (preStaged != null) {
			for (java.io.File srcFile : preStaged.keySet()) {
				java.io.File destFile = preStaged.get(srcFile);

				String srcPath = srcFile.getPath();

				if (destFile == null) {
					LOGGER.debug("ignoring prestaged file, no destination set!");
					continue;
				}
				locFile = new java.io.File(srcPath);
				LOGGER.debug("Prestage: Name of destfile: '" + destFile.getName() + "'");

				try {
					eu.unicore.hila.grid.File remFile = (eu.unicore.hila.grid.File) workingDir.getChild(destFile
							.getName());
					remFile.importFromLocalFile(locFile, true).block();

					if (locFile.canExecute()) {
						try {
							remFile.chmod(true, false, true);
							
							if (remFile.isExecutable() == false) {
								LOGGER.warn("chmod failure of" + remFile.getName() + "might cause that the program can't be executed");
							}
						} catch (HiLAException e) {
							e.printStackTrace();
							LOGGER.warn("chmod failure of" + remFile.getName()
									+ "might cause that the program can't be executed");
						}
					}
				} catch (HiLAException e) {
					// e.printStackTrace();
					throw new GATInvocationException("UNICORE Adaptor: creating remfile for prestaging failed", e);
				}
			}
		}
	}
}
