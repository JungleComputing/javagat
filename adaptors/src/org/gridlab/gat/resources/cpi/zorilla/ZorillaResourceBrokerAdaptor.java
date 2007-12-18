package org.gridlab.gat.resources.cpi.zorilla;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ibis.zorilla.zoni.Callback;
import ibis.zorilla.zoni.CallbackReceiver;
import ibis.zorilla.zoni.JobInfo;
import ibis.zorilla.zoni.ZoniConnection;
import ibis.zorilla.zoni.ZoniProtocol;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

/**
 * 
 * @author ndrost
 * 
 */
public class ZorillaResourceBrokerAdaptor extends ResourceBrokerCpi implements
        Callback, Runnable {

    // update status of each job every minute
    public static final int TIMEOUT = 60000;

    private static final Logger logger =
        Logger.getLogger(ZorillaResourceBrokerAdaptor.class);

    private static boolean ended = false;

    private static synchronized boolean hasEnded() {
        return ended;
    }

    // called by the gat engine
    public static synchronized void end() {
        ended = true;
    }

    private static int parsePort(String string) throws GATInvocationException {
        int port;
        try {
            port = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            throw new GATInvocationException("could not parse port", e);
        }

        if (port <= 0) {
            throw new GATInvocationException("invalid port "
                    + "(must be non-zero positive number): " + string);
        }
        return port;
    }

    private static InetSocketAddress parseSocketAddress(String string)
            throws GATInvocationException {
        int port = ZoniProtocol.DEFAULT_PORT;

        String[] strings = string.split(":");

        if (strings.length > 2) {
            throw new GATInvocationException("illegal address format: "
                    + string);
        } else if (strings.length == 2) {
            // format was "host:port, extract port number"
            port = parsePort(strings[1]);
        }

        InetAddress address = null;
        try {
            address = InetAddress.getByName(strings[0]);
        } catch (UnknownHostException e) {
            throw new GATInvocationException("invalid address: " + string
                    + " exception: " + e);
        }

        return new InetSocketAddress(address, port);
    }

    private final InetSocketAddress nodeSocketAddress;

    private final Map<String, ZorillaJob> jobs;
    
    //receives callbacks from the zorilla node.
    private final CallbackReceiver callbackReceiver;

    /**
     * Constructs a LocalResourceBrokerAdaptor instance corresponding to the
     * passed GATContext.
     * 
     * @param gatContext
     *            A GATContext which will be used to broker resources
     */
    public ZorillaResourceBrokerAdaptor(GATContext gatContext,
            Preferences preferences) throws Exception {
        super(gatContext, preferences);

        String addressString = (String) preferences.get("zorilla.node.address");

        if (addressString == null) {
            // localhost address on default port
            nodeSocketAddress =
                new InetSocketAddress(InetAddress.getByName(null),
                        ZoniProtocol.DEFAULT_PORT);
        } else {
            nodeSocketAddress = parseSocketAddress(addressString);
        }

        jobs = new HashMap<String, ZorillaJob>();
        
        callbackReceiver = new CallbackReceiver(this);
        
        // start a thread to monitor jobs
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("zorilla job monitor");
        thread.start();
    }

    /**
     * This method attempts to reserve the specified hardware resource for the
     * specified time period. Upon reserving the specified hardware resource
     * this method returns a Reservation. Upon failing to reserve the specified
     * hardware resource this method returns an error.
     * 
     * @param resourceDescription
     *            A description, a HardwareResourceDescription, of the hardware
     *            resource to reserve
     * @param timePeriod
     *            The time period, a TimePeriod , for which to reserve the
     *            hardware resource
     */
    public Reservation reserveResource(ResourceDescription resourceDescription,
            TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method attempts to find one or more matching hardware resources.
     * Upon finding the specified hardware resource(s) this method returns a
     * java.util.List of HardwareResource instances. Upon failing to find the
     * specified hardware resource this method returns an error.
     * 
     * @param resourceDescription
     *            A description, a HardwareResoucreDescription, of the hardware
     *            resource(s) to find
     * @return java.util.List of HardwareResources upon success
     */
    public List<HardwareResource> findResources(
            ResourceDescription resourceDescription) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(JobDescription description, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {
        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        String host = getHostname(description);

        if (host != null) {
            throw new GATInvocationException(
                    "cannot specify host with the Zorilla adaptor");
        }

        ZorillaJob job =
            new ZorillaJob(gatContext, preferences, description, null);
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(
                    metricDefinitionName).createMetric(null);
            job.addMetricListener(listener, metric);
        }
        job.startJob(getNodeSocketAddress(), getCallbackReceiver());

        synchronized (this) {
            jobs.put(job.getJobID(), job);
        }

        return job;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#reserveResource(org.gridlab.gat.resources.Resource,
     *      org.gridlab.gat.engine.util.TimePeriod)
     */
    public Reservation reserveResource(Resource resource, TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }

    InetSocketAddress getNodeSocketAddress() {
        return nodeSocketAddress;
    }

    // zoni callback with update of job info
    public synchronized void callback(JobInfo info) {
        if (logger.isDebugEnabled()) {
            logger.debug("got new job info: " + info);
        }
        
        ZorillaJob job = jobs.get(info.getJobID());

        if (job == null) {
            logger.warn("could not update job info: job not in active list: "
                    + info);
            return;
        }

        job.setInfo(info);
    }

    private synchronized ZorillaJob[] getJobs() {
        return jobs.values().toArray(new ZorillaJob[0]);
    }

    private synchronized void removeJob(String jobID) {
        jobs.remove(jobID);
    }

    private void updateJobInfos() {
        logger.debug("updating job info for all jobs");
        ZoniConnection connection = null;

        for (ZorillaJob job : getJobs()) {
            if (connection == null) {
                try {
                    connection =
                        new ZoniConnection(getNodeSocketAddress(), null,
                                ZoniProtocol.TYPE_CLIENT);
                } catch (Exception e) {
                    logger.error("could not connect to zorilla node to"
                            + " update node info: ", e);
                    return;
                }
            }

            try {
                JobInfo info = connection.getJobInfo(job.getJobID());
                
                if (logger.isDebugEnabled()) {
                    logger.debug("retrieved new info: " + info);
                }
                
                job.setInfo(info);
                
                if (job.hasEnded()) {
                    //no need to update info any longer
                    removeJob(job.getJobID());
                }
            } catch (Exception e) {
                logger.error("could not update state for " + job, e);
                connection = null;
            }
        }
    }

    public void run() {
        while (!hasEnded()) {
            updateJobInfos();

            try {
                Thread.sleep(TIMEOUT);
            } catch (InterruptedException e) {
                //IGNORE
            }
        }

    }

    public CallbackReceiver getCallbackReceiver() {
        return callbackReceiver;
    }

}
