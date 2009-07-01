////////////////////////////////////////////////////////////////////
//
// GliteResourceBrokerAdaptor.java
// 
// Contributor(s):
// Dec/2007 - Andreas Havenstein 
//     for Max Planck Institute for Gravitational Physics
//     (Albert Einstein Institute) 
//     Astrophysical Relativity / eScience
// Jun,Jul/2008 - Thomas Zangerl 
//      for Distributed and Parallel Systems Research Group
//      University of Innsbruck
//      major enhancements
//
////////////////////////////////////////////////////////////////////

package org.gridlab.gat.resources.cpi.glite;

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
import org.gridlab.gat.security.glite.GliteSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for the Glite Job Submission (WMS) for JavaGAT.
 * <p>
 * Please read the documentation in <tt>/doc/GliteAdaptor</tt> for further
 * information!
 * 
 */
public class GliteResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static final String GLITE_RESOURCE_BROKER_ADAPTOR = "GliteResourceBrokerAdaptor";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ResourceBrokerCpi.class);

    private LDAPResourceFinder ldapResourceFinder;

    private final List<UriAndCount> resourceBrokerURIs = new ArrayList<UriAndCount>();

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

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
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
        GliteSecurityUtils.addGliteSecurityPreferences(preferences);
        preferences.put(GliteConstants.PREFERENCE_POLL_INTERVAL_SECS, "30");
        preferences.put(GliteConstants.PREFERENCE_DELETE_JDL, "true");
        preferences.put(GliteConstants.PREFERENCE_JOB_STOP_POSTSTAGE, "false");
        preferences.put(GliteConstants.PREFERENCE_JOB_STOP_ON_EXIT, "false");
        return preferences;
    }

    /**
     * Construct a GliteResourceBrokerAdaptor for the given broker URI Instead
     * of a WMS address also an ldap address may be given. In this case the
     * resource broker adaptor will pick some random suitable WMS for the
     * virtual organisation set in the preference VirtualOrganisation
     * 
     * @param gatContext
     * @param brokerURI
     * @throws GATObjectCreationException
     */
    public GliteResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws GATObjectCreationException {
        super(gatContext, brokerURI);
        if (brokerURI == null) {
            throw new GATObjectCreationException("brokerURI is null!");
        } else if ((brokerURI.getScheme() != null)
                && ((brokerURI.getScheme().equals("ldap") || brokerURI
                        .getScheme().equals("ldaps")))) {
            // if not a broker URI itself but an LDAP address is given, retrieve
            // a broker URI
            String vo = GliteConstants.getVO(gatContext);
            try {
                ldapResourceFinder = new LDAPResourceFinder(gatContext,
                        brokerURI);
                List<String> brokerURIs = ldapResourceFinder
                        .fetchWMSServers(vo);

                for (String uriStr : brokerURIs) {
                    try {
                        URI uri = new URI(uriStr);
                        resourceBrokerURIs.add(new UriAndCount(uri));
                    } catch (URISyntaxException use) {
                        LOGGER.warn("Invalid WMS URI in LDAP: " + uriStr);
                    }
                }

                if (resourceBrokerURIs.isEmpty()) {
                    throw new GATObjectCreationException(
                            "Could not find suitable WMS in LDAP for VO: " + vo);
                }

            } catch (NamingException e) {
                throw new GATObjectCreationException(
                        "Could connect to LDAP for VO: " + vo, e);
            }
        } else if ((brokerURI.isCompatible("http") || brokerURI
                .isCompatible("https"))) {
            resourceBrokerURIs.add(new UriAndCount(brokerURI));
        } else {
            throw new GATObjectCreationException("cannot handle scheme: "
                    + brokerURI.getScheme());
        }
    }

    private synchronized void ensureLdapFinderExists() throws NamingException {
        if (ldapResourceFinder == null) {
            ldapResourceFinder = new LDAPResourceFinder(gatContext);
        }
    }

    /** {@inheritDoc} */
    public List<HardwareResource> findResources(
            ResourceDescription resourceDescription)
            throws GATInvocationException {
        if (resourceDescription != null) {
            if (!resourceDescription.getDescription().isEmpty())
                throw new GATInvocationException(
                        "gLite findResources does not support any arguments");
        }
        try {
            ensureLdapFinderExists();
            String vo = GliteConstants.getVO(gatContext);
            List<String> queNames = ldapResourceFinder.fetchCEs(vo);
            List<HardwareResource> retList = new ArrayList<HardwareResource>(
                    queNames.size());
            for (String queName : queNames) {
                retList
                        .add(new GliteHardwareResource(this.gatContext, queName));
            }
            return retList;
        } catch (NamingException e) {
            throw new GATInvocationException(GLITE_RESOURCE_BROKER_ADAPTOR, e);
        }
    }

    public synchronized Job submitJob(AbstractJobDescription jobDescription,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {
        GATInvocationException ex = new GATInvocationException("No Resource Brokers given");
        Collections.sort(this.resourceBrokerURIs);
        for (UriAndCount uac : resourceBrokerURIs) {
            try {
                GliteJob job = new GliteJob(gatContext,
                        (JobDescription) jobDescription, null, uac.getURI()
                                .toString());

                if (listener != null && metricDefinitionName != null) {
                    Metric metric = job.getMetricDefinitionByName(
                            metricDefinitionName).createMetric(null);
                    job.addMetricListener(listener, metric);
                }
                uac.increaseCount();
                return job;
            } catch (GATObjectCreationException e) {
                uac.decreaseCount();
                LOGGER.info("Failed to submit to "+uac.getURI());
                ex = new GATInvocationException("Failed to submit Job", e);
            }            
        }
        throw ex;
    }
}
