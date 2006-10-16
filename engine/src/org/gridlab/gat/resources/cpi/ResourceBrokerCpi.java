package org.gridlab.gat.resources.cpi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
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
    public List findResources(ResourceDescription resourceDescription)
            throws GATInvocationException, IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.ResourceBroker#reserveResource(org.gridlab.gat.resources.Resource,
     *      org.gridlab.gat.util.TimePeriod)
     */
    public Reservation reserveResource(Resource resource, TimePeriod timePeriod)
            throws GATInvocationException, IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.ResourceBroker#reserveResource(org.gridlab.gat.resources.ResourceDescription,
     *      org.gridlab.gat.util.TimePeriod)
     */
    public Reservation reserveResource(ResourceDescription resourceDescription,
            TimePeriod timePeriod) throws GATInvocationException, IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(JobDescription description)
            throws GATInvocationException, IOException {
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

    // utility methods
    protected int getIntAttribute(JobDescription description, String name,
            int defaultVal) {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            return defaultVal;
        }

        Map attributes = sd.getAttributes();
        Integer val = (Integer) attributes.get(name);

        if (val == null) return defaultVal;
        return val.intValue();
    }

    protected long getLongAttribute(JobDescription description, String name,
            long defaultVal) {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            return defaultVal;
        }

        Map attributes = sd.getAttributes();
        Long val = (Long) attributes.get(name);

        if (val == null) return defaultVal;
        return val.longValue();
    }

    protected boolean getBooleanAttribute(JobDescription description,
            String name, boolean defaultVal) {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            return defaultVal;
        }

        Map attributes = sd.getAttributes();
        Boolean val = (Boolean) attributes.get(name);

        if (val == null) return defaultVal;
        return val.booleanValue();
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

    /* Creates a file object for the destination of the preStaged src file */
    protected File resolvePreStagedFile(File srcFile, String host)
            throws GATInvocationException {
        URI src = srcFile.toURI();
        String path = new java.io.File(src.getPath()).getName();

        String dest = "any://";
        dest += ((src.getUserInfo() == null) ? "" : src.getUserInfo());
        dest += host;
        dest += ((src.getPort() == -1) ? "" : (":" + src.getPort()));
        dest += ("/" + path);

        try {
            URI destURI = new URI(dest);

            return GAT.createFile(gatContext, preferences, destURI);
        } catch (Exception e) {
            throw new GATInvocationException(
                "Resource broker generic preStage", e);
        }
    }

    /* also adds stdin to set of files to preStage */
    protected Map resolvePreStagedFiles(JobDescription description, String host)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        Map result = new HashMap();
        Map pre = sd.getPreStaged();

        if (pre != null) {
            Set keys = pre.keySet();
            Iterator i = keys.iterator();

            while (i.hasNext()) {
                File srcFile = (File) i.next();
                File destFile = (File) pre.get(srcFile);

                if (destFile != null) { // already set manually
                    result.put(srcFile, destFile);
                } else {
                    result.put(srcFile, resolvePreStagedFile(srcFile, host));
                }
            }
        }

        File stdin = sd.getStdin();

        if (stdin != null) {
            result.put(stdin, resolvePreStagedFile(stdin, host));
        }

        return result;
    }

    protected void preStageFiles(JobDescription description, String host)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        Map files = resolvePreStagedFiles(description, host);
        Set keys = files.keySet();
        Iterator i = keys.iterator();

        while (i.hasNext()) {
            File srcFile = (File) i.next();
            File destFile = (File) files.get(srcFile);

            try {
                if (GATEngine.VERBOSE) {
                    System.err.println("resource broker cpi prestage:");
                    System.err.println("  copy " + srcFile.toURI() + " to "
                        + destFile.toURI());
                }

                srcFile.copy(destFile.toURI());
            } catch (Throwable e) {
                throw new GATInvocationException("resource broker cpi", e);
            }
        }
    }

    protected File resolvePostStagedFile(File f, String host)
            throws GATInvocationException {
        File res = null;

        URI src = f.toURI();

        if (host == null) {
            host = "";
        }

        String dest = "any://";
        dest += ((src.getUserInfo() == null) ? "" : src.getUserInfo());
        dest += host;
        dest += ((src.getPort() == -1) ? "" : (":" + src.getPort()));
        dest += ("/" + f.getPath());

        URI destURI = null;

        try {
            destURI = new URI(dest);
        } catch (URISyntaxException e) {
            throw new GATInvocationException("resource broker cpi", e);
        }

        try {
            res = GAT.createFile(gatContext, preferences, destURI);
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("resource broker cpi", e);
        }

        return res;
    }

    protected Map resolvePostStagedFiles(JobDescription description, String host)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        Map result = new HashMap();
        Map post = sd.getPostStaged();

        if (post != null) {
            Set keys = post.keySet();
            Iterator i = keys.iterator();

            while (i.hasNext()) {
                File srcFile = (File) i.next();
                File destFile = (File) post.get(srcFile);

                if (destFile == null) {
                    try {
                        destFile =
                                GAT.createFile(gatContext, preferences, srcFile
                                    .getName());
                    } catch (GATObjectCreationException e) {
                        throw new GATInvocationException("resourcebroker cpi",
                            e);
                    }
                }

                result.put(resolvePostStagedFile(srcFile, host), destFile);
            }
        }

        File stdout = sd.getStdout();
        if (stdout != null) {
            try {
                File destFile =
                        GAT.createFile(gatContext, preferences, stdout.getName());
                result.put(resolvePostStagedFile(stdout, host), destFile);
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException("resourcebroker cpi", e);
            }
        }

        File stderr = sd.getStderr();
        if (stderr != null) {
            try {
                File destFile =
                        GAT.createFile(gatContext, preferences, stderr
                            .getName());
                result.put(resolvePostStagedFile(stderr, host), destFile);
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException("resourcebroker cpi", e);
            }
        }

        if (GATEngine.VERBOSE) {
            System.err.println("RESOLVE_POST: ");
            Set keys = result.keySet();
            Iterator i = keys.iterator();

            while (i.hasNext()) {
                File srcFile = (File) i.next();
                File destFile = (File) result.get(srcFile);
                System.err.println("    " + srcFile.toURI() + " -> "
                    + destFile.toURI());
            }
        }

        return result;
    }

    public void postStageFiles(JobDescription description, String host)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        Map files = resolvePostStagedFiles(description, host);
        Set keys = files.keySet();
        Iterator i = keys.iterator();

        GATInvocationException exceptions = new GATInvocationException();
        while (i.hasNext()) {
            File srcFile = (File) i.next();
            File destFile = (File) files.get(srcFile);

            try {
                if (!destFile.equals(srcFile)) {
                    if (GATEngine.VERBOSE) {
                        System.err.println("POSTSTAGE: " + srcFile.toURI()
                            + " to " + destFile.toURI());
                    }

                    srcFile.copy(destFile.toURI());
                }
            } catch (Exception e) {
                exceptions.add("resource broker cpi", e);
            }
        }

        if (exceptions.getNrChildren() != 0) {
            throw exceptions;
        }
    }

    public void removePostStagedFiles(JobDescription description, String host)
            throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }

        Map files = resolvePostStagedFiles(description, host);
        Set keys = files.keySet();
        Iterator i = keys.iterator();

        GATInvocationException exceptions = new GATInvocationException();

        while (i.hasNext()) {
            File srcFile = (File) i.next();

            try {
                if (GATEngine.VERBOSE) {
                    System.err.println("REMOVE POSTSTAGED: " + srcFile.toURI());
                }

                // Just try to delete it, even though it might not exists.
                // We catch the exception and continue anyway.
                srcFile.delete();
            } catch (Exception e) {
                exceptions.add("resource broker cpi", e);
            }
        }

        if (exceptions.getNrChildren() != 0) {
            throw exceptions;
        }
    }

    public String getHostname(JobDescription description)
            throws GATInvocationException {
        ResourceDescription d = description.getResourceDescription();

        if (d == null) {
            return null;
        }

        if (!(d instanceof HardwareResourceDescription)) {
            throw new GATInvocationException(
                "Currently only hardware resource descriptions are supported");
        }

        Map m = d.getDescription();
        Set keys = m.keySet();
        Iterator i = keys.iterator();

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

        return null;
    }

    protected File[] resolveDeletedFiles(JobDescription description, String host)
            throws GATInvocationException {

        SoftwareDescription sd = description.getSoftwareDescription();
        if (sd == null) {
            return null;
        }

        ArrayList res = new ArrayList();

        if (sd.deletePreStaged()) {
            Map tmp = resolvePreStagedFiles(description, host);
            res.addAll(tmp.values());
        }

        if (sd.deletePostStaged()) {
            Map tmp = resolvePostStagedFiles(description, host);
            res.addAll(tmp.keySet());
        }

        ArrayList files = sd.getDeletedFiles();
        for (int j = 0; j < files.size(); j++) {
            res.add(resolvePostStagedFile((File) files.get(j), host));
        }

        File[] result = new File[res.size()];
        return (File[]) res.toArray(result);
    }

    public void deleteFiles(JobDescription jobDescription, String host)
            throws GATInvocationException {
        File[] files = resolveDeletedFiles(jobDescription, host);

        GATInvocationException e = new GATInvocationException();
        for (int i = 0; i < files.length; i++) {
            try {
                if (GATEngine.VERBOSE) {
                    System.err.println("DELETE_FILE:" + files[i]);
                }
//                files[i].delete();
            } catch (Exception x) {
                e.add("resource broker", x);
            }
        }

        if (e.getNrChildren() != 0) throw e;
    }

    public void wipeFiles(JobDescription jobDescription, String host)
            throws GATInvocationException {
        /// @@@ try to wipe
        deleteFiles(jobDescription, host);
    }
}
