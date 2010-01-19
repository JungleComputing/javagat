package org.gridlab.gat.resources.cpi.gliteMultiUser;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a resource broker class for GLite 3.1 It works with the Java
 * WMProxy-API which can be used for multiuser support.
 * 
 * @author Stefan Bozic
 */
public class GliteMultiUserResourceBrokerAdaptor extends ResourceBrokerCpi {

	/** Name of the Adaptor */
	public static final String GLITE_RESOURCE_BROKER_ADAPTOR = "GliteMultiUserResourceBrokerAdaptor";

	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(GliteMultiUserResourceBrokerAdaptor.class);

	/** LDAP Service class */
	private LDAPResourceFinder ldapResourceFinder;

	/** ResourceBroker Collection */
	private final List<UriAndCount> resourceBrokerURIs = new ArrayList<UriAndCount>();

	/**
	 * Defines a Map, that holds the information which capabilities are
	 * supported by this adaptor.
	 * 
	 * @return a Map, that holds the information which capabilities are
	 *         supported by this adaptor.
	 */
	public static Map<String, Boolean> getSupportedCapabilities() {
		Map<String, Boolean> capabilities = ResourceBrokerCpi.getSupportedCapabilities();
		capabilities.put("beginMultiJob", false);
		capabilities.put("endMultiJob", false);
		capabilities.put("findResources", true);
		capabilities.put("reserveResource", false);
		capabilities.put("submitJob", true);
		return capabilities;
	}

	/**
	 * Used by CreateDefaultPropertiesFile to generate default
	 * javagat.properties.
	 * 
	 * @return Properties and their default values.
	 */
	public static Preferences getSupportedPreferences() {
		Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();

		// GliteSecurityUtils.addGliteSecurityPreferences(preferences);

		preferences.put(GliteConstants.PREFERENCE_POLL_INTERVAL_SECS, "30");
		preferences.put(GliteConstants.PREFERENCE_DELETE_JDL, "true");
		preferences.put(GliteConstants.PREFERENCE_JOB_STOP_POSTSTAGE, "false");
		preferences.put(GliteConstants.PREFERENCE_JOB_STOP_ON_EXIT, "false");
		return preferences;
	}

	/**
	 * Construct a {@link GliteMultiUserResourceBrokerAdaptor} for the given
	 * broker {@link URI}. Instead of a WMS address also an ldap address may be
	 * given. In this case the resource broker adaptor will pick some random
	 * suitable WMS for the virtual organisation set in the preference
	 * VirtualOrganisation.
	 * 
	 * @param gatContext the GAT context.
	 * @param brokerURI the {@link URI} to the WMProxy.
	 * @throws GATObjectCreationException an exception that might occurs.
	 */
	public GliteMultiUserResourceBrokerAdaptor(GATContext gatContext, URI brokerURI) throws GATObjectCreationException {
		super(gatContext, brokerURI);
		if (brokerURI == null) {
			throw new GATObjectCreationException("brokerURI is null!");
		} else if ((brokerURI.getScheme() != null)
				&& ((brokerURI.getScheme().equals("ldap") || brokerURI.getScheme().equals("ldaps")))) {
			// if not a broker URI itself but an LDAP address is given, retrieve
			// a broker URI
			String vo = GliteConstants.getVO(gatContext);
			try {
				ldapResourceFinder = new LDAPResourceFinder(gatContext, brokerURI);
				List<String> brokerURIs = ldapResourceFinder.fetchWMSServers(vo);

				for (String uriStr : brokerURIs) {
					try {
						URI uri = new URI(uriStr);
						resourceBrokerURIs.add(new UriAndCount(uri));
					} catch (URISyntaxException use) {
						LOGGER.warn("Invalid WMS URI in LDAP: " + uriStr);
					}
				}

				if (resourceBrokerURIs.isEmpty()) {
					throw new GATObjectCreationException("Could not find suitable WMS in LDAP for VO: " + vo);
				}

			} catch (NamingException e) {
				throw new GATObjectCreationException("Could connect to LDAP for VO: " + vo, e);
			}
		} else if ((brokerURI.isCompatible("http") || brokerURI.isCompatible("https"))) {
			resourceBrokerURIs.add(new UriAndCount(brokerURI));
		} else {
			throw new GATObjectCreationException("cannot handle scheme: " + brokerURI.getScheme());
		}
	}

	/**
	 * Helper method that checks that an LdapFinder instance exits. If not it
	 * create creates a new one.
	 * 
	 * @throws NamingException
	 */
	private synchronized void ensureLdapFinderExists() throws NamingException {
		if (ldapResourceFinder == null) {
			ldapResourceFinder = new LDAPResourceFinder(gatContext);
		}
	}

	/** {@inheritDoc} */
	public List<HardwareResource> findResources(ResourceDescription resourceDescription) throws GATInvocationException {
		if (resourceDescription != null) {
			if (!resourceDescription.getDescription().isEmpty())
				throw new GATInvocationException("gLite findResources does not support any arguments");
		}
		try {
			ensureLdapFinderExists();
			String vo = GliteConstants.getVO(gatContext);
			List<String> queNames = ldapResourceFinder.fetchCEs(vo);
			List<HardwareResource> retList = new ArrayList<HardwareResource>(queNames.size());
			for (String queName : queNames) {
				retList.add(new GliteHardwareResource(this.gatContext, queName));
			}
			return retList;
		} catch (NamingException e) {
			throw new GATInvocationException(GLITE_RESOURCE_BROKER_ADAPTOR, e);
		}
	}

	/** {@inheritDoc} */
	public synchronized Job submitJob(AbstractJobDescription jobDescription, MetricListener listener,
			String metricDefinitionName) throws GATInvocationException {
		GATInvocationException ex = new GATInvocationException("No Resource Brokers given");
		Collections.sort(this.resourceBrokerURIs);
		for (UriAndCount uac : resourceBrokerURIs) {
			try {
				GliteJob job = new GliteJob(gatContext, (JobDescription) jobDescription, null, uac.getURI().toString());
				job.submitJob();

				if (listener != null && metricDefinitionName != null) {
					Metric metric = job.getMetricDefinitionByName(metricDefinitionName).createMetric(null);
					job.addMetricListener(listener, metric);
				}
				uac.increaseCount();
				return job;
			} catch (GATObjectCreationException e) {
				uac.decreaseCount();
				LOGGER.info("Failed to submit to " + uac.getURI());
				ex = new GATInvocationException("Failed to submit Job", e);
			}
		}
		throw ex;
	}

	/**
	 * Inner class for holding a counter on an {@link URI}. This class is taken
	 * from the Glite adaptor.
	 * 
	 * @author Stefan Bozic
	 */
	private static class UriAndCount implements Comparable<UriAndCount> {
		private static final int LIMIT = 3;
		private final URI uri;
		private int count;

		public UriAndCount(URI uri) {
			this.uri = uri;
			this.count = 0;
		}

		/** {@inheritDoc} */
		public int compareTo(UriAndCount o) {
			return o.count - this.count;
		}

		public URI getURI() {
			return uri;
		}

		public void increaseCount() {
			if (count < LIMIT)
				count++;
		}

		public void decreaseCount() {
			if (count > -LIMIT)
				count--;
		}

	}

}
