package org.gridlab.gat.resources.cpi.zorilla;

import ibis.smartsockets.virtual.VirtualSocketFactory;
import ibis.zorilla.zoni.Callback;
import ibis.zorilla.zoni.CallbackReceiver;
import ibis.zorilla.zoni.JobInfo;
import ibis.zorilla.zoni.ZoniConnection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.WrapperJobCpi;

/**
 * 
 * @author ndrost
 * 
 */
public class ZorillaResourceBrokerAdaptor extends ResourceBrokerCpi implements
        Callback, Runnable {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("submitJob", true);

        return capabilities;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "zorilla"};
    }

    // update status of each job every minute
    public static final int TIMEOUT = 5000;

    private static final Logger logger = LoggerFactory
            .getLogger(ZorillaResourceBrokerAdaptor.class);

    private static boolean ended = false;

    private static synchronized boolean hasEnded() {
        return ended;
    }

    // called by the gat engine
    public static synchronized void end() {
        ended = true;
    }

    private final String nodeSocketAddress;

    private final VirtualSocketFactory socketFactory;

    private final Map<String, ZorillaJob> jobs;

    // receives callbacks from the zorilla node.
    private final CallbackReceiver callbackReceiver;

    public ZorillaResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws Exception {
        super(gatContext, brokerURI);

        jobs = new HashMap<String, ZorillaJob>();

        if (brokerURI == null) {
            throw new GATObjectCreationException(
                    "broker URI (zorilla node address) not specified");
        }

        logger.debug("broker URI = " + brokerURI);

        nodeSocketAddress = brokerURI.getSchemeSpecificPart();

        String hubs = (String) gatContext.getPreferences().get(
                "ibis.hub.addresses");

        socketFactory = ZoniConnection.getFactory(hubs);

        logger.debug("zorilla node address = " + nodeSocketAddress);

        Object o = gatContext.getPreferences().get("zorilla.wait.for.node");
        String wait = o != null ? o.toString() : null;

        if (wait != null && wait.equalsIgnoreCase("true")) {
            logger.info("Checking if node " + getNodeSocketAddress()
                    + " exists");
            // try to connect to zorilla
            try {
                ZoniConnection connection = new ZoniConnection(
                        getNodeSocketAddress(), socketFactory, null, 30000,
                        true);
                connection.close();
            } catch (IOException e) {
                throw new GATObjectCreationException(
                        "could not reach zorilla node: ", e);
            }
        }

        callbackReceiver = new CallbackReceiver(this, socketFactory);

        // start a thread to monitor jobs
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("zorilla job monitor");
        thread.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources
     * .JobDescription)
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

        ZorillaJob zorillaJob = new ZorillaJob(gatContext, description, null,
                this);
        Job job;
        if (description instanceof WrapperJobDescription) {
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, zorillaJob,
                    listener, metricDefinitionName);
            listener = tmp;
            job = tmp;
        } else {
            job = zorillaJob;
        }
        if (listener != null && metricDefinitionName != null) {
            Metric metric = zorillaJob.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            zorillaJob.addMetricListener(listener, metric);
        }

        zorillaJob.startJob(getNodeSocketAddress(), socketFactory,
                getCallbackReceiver());

        logger.debug("ZorillaResourceBroker.submitJob: zorilla job started: "
                + zorillaJob.getZorillaJobID());

        synchronized (this) {
            jobs.put("" + zorillaJob.getZorillaJobID(), zorillaJob);
        }

        return job;
    }

    String getNodeSocketAddress() {
        return nodeSocketAddress;
    }

    VirtualSocketFactory getSocketFactory() {
        return socketFactory;
    }

    // zoni callback with update of job info
    public synchronized void callback(JobInfo info) {
        if (logger.isDebugEnabled()) {
            logger.debug("got new job info: " + info);
        }

        ZorillaJob job = jobs.get(info.getJobID());

        if (job == null) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("could not update job info: job not in active list: "
                                + info);
            }
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

    private void updateJobInfos() throws Exception {
        logger.debug("updating job info for all jobs");
        ZoniConnection connection = null;

        for (ZorillaJob job : getJobs()) {
            if (connection == null) {
                connection = new ZoniConnection(getNodeSocketAddress(),
                        socketFactory, null);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("getting new info for " + job);
            }

            if (job.hasEnded()) {
                // no need to update info any longer
                removeJob(job.getZorillaJobID());
            }

            try {
                JobInfo info = connection.getJobInfo(job.getZorillaJobID());

                if (logger.isDebugEnabled()) {
                    logger.debug("retrieved new info: " + info);
                }

                job.setInfo(info);
            } catch (IOException e) {
                if (connection != null) {
                    connection.close();
                }
                connection = null;
                throw e;
            }
        }
        if (connection != null) {
            connection.close();
        }
    }

    public void run() {
        while (!hasEnded()) {
            try {
                updateJobInfos();
            } catch (Exception e) {
                logger.warn("could not update job infos of "
                        + nodeSocketAddress, e);
            }

            try {
                Thread.sleep(TIMEOUT);
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
        socketFactory.end();
    }

    public CallbackReceiver getCallbackReceiver() {
        return callbackReceiver;
    }

}
