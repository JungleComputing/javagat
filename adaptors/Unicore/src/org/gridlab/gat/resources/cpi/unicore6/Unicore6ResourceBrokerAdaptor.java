package org.gridlab.gat.resources.cpi.unicore6;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
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
import eu.unicore.hila.grid.job.JSDLBuilder;
import eu.unicore.hila.grid.unicore6.SiteLocator;
import eu.unicore.hila.grid.unicore6.Unicore6Grid;

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
	public eu.unicore.hila.grid.job.JobDescription createJobDescription(SoftwareDescription sd)
			throws GATInvocationException {

		JSDLBuilder jsdlBuilder = new JSDLBuilder();

		// FIXME: Change, whenever SofwareDescription offers JobName.
		jsdlBuilder.setTaskName("Unicore-GAT" + sd.getExecutable());

		jsdlBuilder.setExecutable("$PWD/" + sd.getExecutable()); // "$PWD" could be a problem with windows sites

		String[] jobArgs = sd.getArguments();

		if (jobArgs != null) {
			for (String argument : jobArgs) {
				jsdlBuilder.addArgument(argument);
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

		JobDefinitionDocument tempJobDefDocument = null;

		try {
			tempJobDefDocument = jsdlBuilder.getJsdl();
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore JSDL handling exception", e);
		}

		final JobDefinitionDocument jobDefDocument = tempJobDefDocument;

		eu.unicore.hila.grid.job.JobDescription jobDescription = new eu.unicore.hila.grid.job.JobDescription() {
			public Object getActualJobDescription() {
				return jobDefDocument;
			}
		};

		return jobDescription;
	}

	/**
	 * Returns the available site names.
	 */
	public List<HardwareResource> findResources(ResourceDescription resourceDescription) throws GATInvocationException {
		initSecurity(gatContext);

		List<HardwareResource> resources = new ArrayList<HardwareResource>();

		Location location;
		List<? extends Resource> sites = null;

		if (registries != null) {
			SiteLocator locator = SiteLocator.getInstance();

			try {
				location = new Location("unicore6:/");
				Unicore6Grid grid = Unicore6Grid.locate(location);

				if (username != null) {
					sites = locator.getAllSites(registries, grid.getProperties(), new Location("unicore6:/" + username
							+ "@sites/"));
				} else {
					sites = locator.getAllSites(registries, grid.getProperties(), new Location("unicore6:/sites/"));
				}

			} catch (HiLAException e) {
				throw new GATInvocationException("Error while retrieving sites.", e);
			}
		} else {
			if (username != null) {
				location = new Location(java.net.URI.create("unicore6:/" + username + "@sites/"));
			} else {
				location = new Location(java.net.URI.create("unicore6:/sites/"));
			}

			try {
				Resource resource = location.locate();
				sites = resource.getChildren();

			} catch (HiLAException e) {
				throw new GATInvocationException("Error while retrieving sites.", e);
			}
		}

		if (sites != null) {
			for (Resource site : sites) {
				if (site != null) {
					HardwareResource hwRes = new Unicore6SiteResource(gatContext);
					hwRes.getResourceDescription().addResourceAttribute("sitename", site.getName());
					resources.add(hwRes);
				}
			}
		}

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

		String host = "unicore6:" + brokerURI.getPath();

		LOGGER.debug("Unicore adaptor will use '" + host + "' as execution host");

		Unicore6Job unicoreJob = new Unicore6Job(gatContext, unicoreJobDescr, null);

		if (listener != null && metricDefinitionName != null) {
			Metric metric = unicoreJob.getMetricDefinitionByName(metricDefinitionName).createMetric(null);
			unicoreJob.addMetricListener(listener, metric);
		}

		try {
			Resource resource = new Location(host).locate();

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
