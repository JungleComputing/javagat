package org.gridlab.gat.resources.cpi;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.HardwareResourceDescription;
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
    protected GATContext gatContext;

    protected Preferences preferences;

    /**
     * This method constructs a ResourceBrokerCpi instance corresponding to the
     * passed GATContext.
     *
     * @param gatContext
     *            A GATContext which will be used to broker resources
     * @param preferences
     *            the preferences to be associated with this resource broker
     * @throws GATObjectCreationException
     *             no adaptor could be loaded
     */
    protected ResourceBrokerCpi(GATContext gatContext, Preferences preferences)
            throws GATObjectCreationException {
        this.gatContext = gatContext;
        this.preferences = preferences;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.ResourceBroker#findResources(org.gridlab.gat.resources.ResourceDescription)
     */
    public List<HardwareResource> findResources(ResourceDescription resourceDescription)
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
    public Job submitJob(JobDescription description)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    // utility methods
    protected URI getLocationURI(JobDescription description)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        URI u = sd.getLocation();

        if (u == null) {
            throw new GATInvocationException(
                    "The Job description does not contain a location");
        }

        return u;
    }

    protected boolean isJavaApplication(JobDescription description)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        String exeScheme = getLocationURI(description).getScheme();
        if (exeScheme != null && exeScheme.equals("java")) {
            return true;
        }

        return false;
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

    protected int getCPUCount(JobDescription description) {
        return getIntAttribute(description, "count", 1);
    }

    protected int getHostCount(JobDescription description) {
        return getIntAttribute(description, "hostCount", 1);
    }

    protected String getLocation(JobDescription description)
            throws GATInvocationException {
        URI u = getLocationURI(description);

        if (u == null) {
            throw new GATInvocationException(
                    "The Job description does not contain a location");
        }

        String location = u.toString();

        return location;
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

    public String getHostname(JobDescription description)
            throws GATInvocationException {
        String contactHostname = null;

        String contact =
                (String) preferences.get("ResourceBroker.jobmanagerContact");
        if (contact != null) {
            StringTokenizer st = new StringTokenizer(contact, ":/");
            contactHostname = st.nextToken();
        }

        ResourceDescription d = description.getResourceDescription();

        if (d == null) {
            return contactHostname;
        }

        if (!(d instanceof HardwareResourceDescription)) {
            if (contactHostname != null)
                return contactHostname;

            throw new GATInvocationException(
                    "Currently only hardware resource descriptions are supported");
        }

        Map<String, Object> m = d.getDescription();
        Set<String> keys = m.keySet();
        Iterator<String> i = keys.iterator();

        while (i.hasNext()) {
            String key = (String) i.next();
            Object val = m.get(key);

            if (key.equals("machine.node")) {
                if (val instanceof String) {
                    return (String) val;
                } else {
                    String[] hostList = (String[]) val;
                    return hostList[0];
                }
            }

            //            System.err.println("warning, ignoring key: " + key);
        }

        return contactHostname;
    }
}
