package org.gridlab.gat.resources.cpi.zorilla;

import ibis.zorilla.zoni.Callback;
import ibis.zorilla.zoni.CallbackReceiver;
import ibis.zorilla.zoni.JobInfo;
import ibis.zorilla.zoni.ZoniConnection;
import ibis.zorilla.zoni.ZoniProtocol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

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

    // update status of each job every minute
    public static final int TIMEOUT = 5000;

    private static final Logger logger = Logger
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

        String host = brokerURI.getHost();
        if (host == null) {
            host = "localhost";
        }

        int port = brokerURI.getPort(ZoniProtocol.DEFAULT_PORT);

        nodeSocketAddress = host + ":" + port;
        logger.debug("zorilla node address = " + nodeSocketAddress);

        callbackReceiver = new CallbackReceiver(this);

        try {
            ZoniConnection connection = new ZoniConnection(
                    getNodeSocketAddress(), null);
            connection.close();
        } catch (IOException e) {
            throw new GATObjectCreationException(
                    "could not reach zorilla node", e);
        }

        // start a thread to monitor jobs
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("zorilla job monitor");
        thread.start();
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

        ZorillaJob job = new ZorillaJob(gatContext, description, null, this);
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }
        job.startJob(getNodeSocketAddress(), getCallbackReceiver());

        synchronized (this) {
            jobs.put(job.getJobID(), job);
        }

        return job;
    }

    String getNodeSocketAddress() {
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

    private void updateJobInfos() throws IOException {
        logger.debug("updating job info for all jobs");
        ZoniConnection connection = null;

        for (ZorillaJob job : getJobs()) {
            if (connection == null) {
                connection = new ZoniConnection(getNodeSocketAddress(), null);
            }

            try {
                JobInfo info = connection.getJobInfo(job.getJobID());

                if (logger.isDebugEnabled()) {
                    logger.debug("retrieved new info: " + info);
                }

                job.setInfo(info);

                if (job.hasEnded()) {
                    // no need to update info any longer
                    removeJob(job.getJobID());
                }
            } catch (IOException e) {
                if (connection != null) {
                    connection.close();
                }
                connection = null;
                throw e;
            }
        }
    }

    public void run() {
        while (!hasEnded()) {
            try {
                updateJobInfos();
            } catch (IOException e) {
                logger.warn("could not update job infos of "
                        + nodeSocketAddress, e);
            }

            try {
                Thread.sleep(TIMEOUT);
            } catch (InterruptedException e) {
                // IGNORE
            }
        }

    }

    public CallbackReceiver getCallbackReceiver() {
        return callbackReceiver;
    }

}
