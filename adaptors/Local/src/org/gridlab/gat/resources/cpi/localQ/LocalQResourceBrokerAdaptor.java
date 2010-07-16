package org.gridlab.gat.resources.cpi.localQ;

import java.util.Map;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperJobCpi;

/**
 * An instance of this class is used to reserve resources.
 * <p>
 * A resource can either be a hardware resource or a software resource. A
 * software resource is simply an executable it makes little sense to reserve
 * such. Thus an instance of this class can currently only reserve a hardware
 * resource.
 * <p>
 * If one wishes to reserve a hardware resource, one must first describe the
 * hardware resource that one wishes to reserve. This is accomplished by
 * creating an instance of the class HardwareResourceDescription which describes
 * the hardware resource that one wishes to reserve. After creating such an
 * instance of the class HardwareResourceDescription that describes the hardware
 * resource one wishes to reserve, one must specify the time period for which
 * one wishes to reserve the hardware resource. This is accomplished by creating
 * an instance of the class TimePeriod which specifies the time period for which
 * one wishes to reserve the hardware resource. Finally, one must obtain a
 * reservation for the desired hardware resource for the desired time period.
 * This is accomplished by calling the method ReserveHardwareResource() on an
 * instance of the class LocalResourceBrokerAdaptor with the appropriate
 * instance of HardwareResourceDescription and the appropriate instance of
 * TimePeriod.
 * <p>
 * In addition an instance of this class can be used to find hardware resources.
 * This is accomplished using the method FindHardwareResources(). This is
 * accomplished by creating an instance of the class HardwareResourceDescription
 * which describes the hardware resource that one wishes to find. After creating
 * such an instance of the class HardwareResourceDescription that describes the
 * hardware resource one wishes to find, one must find the corresponding
 * hardware resource. This is accomplished by calling the method
 * FindHardwareResources() on an instance of the class
 * LocalResourceBrokerAdaptor with the appropriate instance of
 * HardwareResourceDescription.
 */
public class LocalQResourceBrokerAdaptor extends ResourceBrokerCpi implements
        Runnable {

    public static String getDescription() {
        return "The LocalQ ResourceBroker Adaptor implements the ResourceBroker object using the Java ProcessBuilder facility, but in contrast with the Local ResourceBroker Adaptor, this one has a job queue and can run a number of jobs simultaneously, see the localq.max.concurrent.jobs preference.";
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();
        preferences.put("localq.max.concurrent.jobs", "<number of cores>");
        return preferences;
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("submitJob", true);

        return capabilities;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "localq", ""};
    }

    protected static Logger logger = LoggerFactory
            .getLogger(LocalQResourceBrokerAdaptor.class);

    private static boolean ended = false;

    private static synchronized boolean hasEnded() {
        return ended;
    }

    // called by the gat engine
    public static synchronized void end() {
        ended = true;
    }

    private final PriorityQueue<LocalQJob> queue;

    /**
     * This method constructs a LocalResourceBrokerAdaptor instance
     * corresponding to the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which will be used to broker resources
     */
    public LocalQResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws GATObjectCreationException {
        super(gatContext, brokerURI);
        
        // the brokerURI should point to the local host else throw exception
        if (!brokerURI.refersToLocalHost()) {
            throw new GATObjectCreationException(
                    "The LocalQResourceBrokerAdaptor doesn't refer to localhost, but to a remote host: "
                            + brokerURI.toString());
        }
        
        String path = brokerURI.getPath();
        if (path != null && ! path.equals("")) {
            throw new GATObjectCreationException(
                    "The LocalQResourceBrokerAdaptor does not understand the specified path: " + path);
        }

        queue = new PriorityQueue<LocalQJob>();

        Integer maxConcurrentJobs = (Integer) gatContext.getPreferences().get(
                "localq.max.concurrent.jobs");

        if (maxConcurrentJobs == null) {
            maxConcurrentJobs = Runtime.getRuntime().availableProcessors();
        }

        if (maxConcurrentJobs <= 0) {
            throw new GATObjectCreationException(
                    "cannot create local Q resource broker with "
                            + maxConcurrentJobs + " concurrent jobs");
        }

        for (int i = 0; i < maxConcurrentJobs; i++) {
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(AbstractJobDescription abstractDescription,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {
        if (!(abstractDescription instanceof JobDescription)) {
            throw new GATInvocationException(
                    "can only handle JobDescriptions: "
                            + abstractDescription.getClass());
        }

        JobDescription description = (JobDescription) abstractDescription;

        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        if (description.getProcessCount() != 1) {
            throw new GATInvocationException(
                    "Adaptor cannot handle: process count > 1: "
                            + description.getProcessCount());
        }

        if (description.getResourceCount() != 1) {
            throw new GATInvocationException(
                    "Adaptor cannot handle: resource count > 1: "
                            + description.getResourceCount());
        }

        String home = System.getProperty("user.home");
        if (home == null) {
            throw new GATInvocationException(
                    "local broker could not get user home dir");
        }

        Sandbox sandbox = new Sandbox(gatContext, description, "localhost",
                home, true, true, true, true);

        LocalQJob result = new LocalQJob(gatContext, this, description, sandbox);
        Job job = null;
        if (description instanceof WrapperJobDescription) {
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, result,
                    listener, metricDefinitionName);
            job = tmp;
            listener = tmp;
        } else {
            job = result;
        }
        if (listener != null && metricDefinitionName != null) {
            Metric metric = result.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            result.addMetricListener(listener, metric);
        }
 
        result.setState(Job.JobState.PRE_STAGING);
        sandbox.prestage();

        // add to queue
        synchronized (this) {
            // for (int i = 0; i < getProcessCount(description); i++) {
            queue.add(result);
            notifyAll();
            // }
        }

        return job;
    }

    private synchronized LocalQJob getJob() {
        while (!hasEnded()) {
            LocalQJob result = queue.poll();

            if (result != null) {
                return result;
            }

            try {
                wait(100);
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
        return null;
    }

    public void run() {
        while (true) {
            LocalQJob next = getJob();

            if (next == null) {
                // resource broker has ended, exit
                return;
            }

            try {
                next.run();
            } catch (Throwable t) {
                logger.error("error while running job: " + t);
                t.printStackTrace();
            }
        }
    }
}
