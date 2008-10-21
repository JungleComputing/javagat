package org.gridlab.gat.resources.cpi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * Capability provider interface to the ResourceBroker class. <p/>Capability
 * provider wishing to provide the functionality of the ResourceBroker class
 * must extend this class and implement all of the abstract methods in this
 * class. Each abstract method in this class mirrors the corresponding method in
 * this ResourceBroker class and will be used to implement the corresponding
 * method in the ResourceBroker class at runtime.
 */
public abstract class ResourceBrokerCpi implements ResourceBroker {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
        capabilities.put("beginMultiJob", false);
        capabilities.put("endMultiJob", false);
        capabilities.put("findResources", false);
        capabilities.put("reserveResource", false);
        capabilities.put("submitJob", false);
        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = new Preferences();
        preferences.put("job.stop.poststage", "true");
        preferences.put("job.stop.on.exit", "true");
        return preferences;
    }

    protected GATContext gatContext;

    protected URI brokerURI;

    /**
     * This method constructs a ResourceBrokerCpi instance corresponding to the
     * passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which will be used to broker resources
     * @throws GATObjectCreationException
     *                 no adaptor could be loaded
     */
    protected ResourceBrokerCpi(GATContext gatContext, URI brokerURI)
            throws GATObjectCreationException {
        this.gatContext = gatContext;
        this.brokerURI = brokerURI;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#beginMultiCoreJob()
     */
    public void beginMultiJob() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#endMultiCoreJob()
     */
    public Job endMultiJob() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#findResources(org.gridlab.gat.resources.ResourceDescription)
     */
    public List<HardwareResource> findResources(
            ResourceDescription resourceDescription)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#reserveResource(org.gridlab.gat.resources.Resource,
     *      org.gridlab.gat.engine.util.TimePeriod)
     */
    public Reservation reserveResource(Resource resource, TimePeriod timePeriod)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#reserveResource(org.gridlab.gat.resources.ResourceDescription,
     *      org.gridlab.gat.engine.util.TimePeriod)
     */
    public Reservation reserveResource(ResourceDescription resourceDescription,
            TimePeriod timePeriod) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public final Job submitJob(AbstractJobDescription description)
            throws GATInvocationException {
        return submitJob(description, null, null);
    }

    public Job submitJob(AbstractJobDescription description,
            MetricListener listener, String metricName)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public final Job[] submitJob(JobDescription[] description)
            throws GATInvocationException {
        return submitJob(description, null, null);
    }

    public Job[] submitJob(JobDescription[] description,
            MetricListener listener, String metricName)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    // utility methods
    protected String getExecutable(JobDescription description)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        String path = sd.getExecutable();

        if (path == null) {
            throw new GATInvocationException(
                    "The Job description does not contain an executable");
        }

        return path;
    }

    // utility methods
    protected int getIntAttribute(JobDescription description, String name,
            int defaultVal) {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            return defaultVal;
        }

        return sd.getIntAttribute(name, defaultVal);
    }

    protected long getLongAttribute(JobDescription description, String name,
            long defaultVal) {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            return defaultVal;
        }
        return sd.getLongAttribute(name, defaultVal);
    }

    protected String getStringAttribute(JobDescription description,
            String name, String defaultVal) {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            return defaultVal;
        }
        return sd.getStringAttribute(name, defaultVal);
    }

    protected boolean getBooleanAttribute(JobDescription description,
            String name, boolean defaultVal) {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            return defaultVal;
        }
        return sd.getBooleanAttribute(name, defaultVal);
    }

    protected String[] getArgumentsArray(JobDescription description)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        return sd.getArguments();
    }

    protected String getArguments(JobDescription description)
            throws GATInvocationException {
        String[] arguments = getArgumentsArray(description);
        String argString = "";

        if (arguments == null) {
            return "";
        }

        for (int i = 0; i < arguments.length; i++) {
            argString += (" " + arguments[i]);
        }

        return argString;
    }

    protected String getHostname() {
        return brokerURI.getHost();
    }

    protected String getAuthority() {
        return brokerURI.getAuthority();
    }

    protected String getScheme() {
        return brokerURI.getScheme();
    }

    public String toString() {
        return brokerURI.toString();
    }

}
