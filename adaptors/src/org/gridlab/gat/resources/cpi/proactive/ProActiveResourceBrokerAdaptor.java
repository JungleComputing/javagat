package org.gridlab.gat.resources.cpi.proactive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;

/**
 * This class implements the JavaGat resource broker for ProActive.
 */
public class ProActiveResourceBrokerAdaptor extends ResourceBrokerCpi
        implements Runnable {

    /** Maps a ProActive descriptor filename to a JobWatcher object. */
    private static HashMap descr2watcher = new HashMap();

    /** Maps a ProActive descriptor filename to a JobWatcher stub. */
    private static HashMap descr2watcherStub = new HashMap();

    /** Maps a ProActive descriptor filename to a set of NodeInfo. */
    private static HashMap descr2nodeset = new HashMap();

    /** List of jobs to schedule. */
    private ArrayList jobList = new ArrayList();

    /** Total number of nodes, as obtained from the descriptors. */
    private int totalNodes = 0;

    /** Number of nodes not scheduled to a job. */
    private int availableNodes = 0;

    /** Every cluster has its own job watcher. */
    private JobWatcher[] watchers;

    /** A JobWatcher is a ProActive object, so has a stub. */
    private JobWatcher[] watcherStubs;

    /** Job to be scheduled next. */
    private Job nextJob = null;

    /**
     * Number of ProActive descriptors for which an addNodes call is
     * still expected.
     */
    private int remainingCalls;

    /** Logger. */
    static final Logger logger
        = ibis.util.GetLogger.getLogger(ProActiveResourceBrokerAdaptor.class);

    /**
     * Constructs a ResourceBroker for ProActive.
     * @param gatContext the JavaGat context.
     * @param preferences the preferences.
     */
    public ProActiveResourceBrokerAdaptor(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {

        super(gatContext, preferences);

        // First, obtain ProActiver descriptors. */
        String descriptors = (String) preferences
                .get("ResourceBroker.proActive.descriptors");
        if (preferences == null) {
            throw new GATObjectCreationException("No descriptors provided. Set"
                    + " the ResourceBroker.proActive.descriptors preference to "
                    + " a comma-separated list of ProActive descriptor xmls.");
        }
        StringTokenizer tok = new StringTokenizer(descriptors, ",");
        ArrayList xmls = new ArrayList();
        while (tok.hasMoreTokens()) {
            xmls.add(tok.nextToken());
        }
        watchers = new JobWatcher[xmls.size()];
        watcherStubs = new JobWatcher[watchers.length];

        remainingCalls = watchers.length;

        for (int i = 0; i < xmls.size(); i++) {
            // Spawn a JobWatcher thread for each descriptor.
            String descr = (String) xmls.get(i);
            watchers[i] = new JobWatcher();
            descr2watcher.put(descr, watchers[i]);
            try {
                watcherStubs[i]
                        = (JobWatcher) ProActive.turnActive(watchers[i]);
                descr2watcherStub.put(descr, watcherStubs[i]);
            } catch(Exception e) {
                throw new GATObjectCreationException(
                        "Could not turn job watcher active", e);
            }
            // Spawn a grabber thread for each descriptor.
            new GrabberThread(descr, this, preferences);
        }

        // And finally, start a scheduler thread.
        Thread t = new Thread(this, "Resource broker thread");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Adds the specified nodes, that were found on the specified descriptor,
     * to the available nodes, provided that a launcher could be started
     * on them.
     * @param descriptor the ProActive descriptor URL.
     * @param nodes the list of nodes.
     */
    void addNodes(String descriptor, ArrayList nodes) {
        HashSet h = new HashSet();
        JobWatcher watcher = (JobWatcher) descr2watcher.get(descriptor);
        JobWatcher stub = (JobWatcher) descr2watcherStub.get(descriptor);
        logger.debug("Adding " + nodes.size() + " nodes");

        synchronized(this) {
            remainingCalls--;
        }

        // Set up parameters for parallel creation of launchers.
        Node[] proActiveNodes = (Node[]) nodes.toArray(new Node[0]);
        Object[][] parameters = new Object[proActiveNodes.length][];
        NodeInfo[] nodeInfo = new NodeInfo[proActiveNodes.length];
        for (int i = 0; i < proActiveNodes.length; i++) {
            nodeInfo[i] = new NodeInfo(proActiveNodes[i], watcher, descriptor,
                    this);
            parameters[i] = new Object[] { stub, proActiveNodes[i]};
            h.add(nodeInfo[i]);
        }

        // Create launchers in parallel.
        Object[] launchers;
        try {
            launchers = ProActive.newActiveInParallel(
                    Launcher.class.getName(), parameters, proActiveNodes);
        } catch(Throwable e) {
            logger.error("Internal error, launch creation failed ...", e);
            return;
        }

        // Process result of launcher creation.
        for (int i = 0; i < nodeInfo.length; i++) {
            nodeInfo[i].launcher = (Launcher) launchers[i];
        }
        synchronized(this) {
            descr2nodeset.put(descriptor, h);
            totalNodes += nodeInfo.length;
            availableNodes += nodeInfo.length;
            notifyAll();
        }
        logger.debug("Added " + nodeInfo.length + " nodes");
    }

    /**
     * Reserves and returns the specified number of nodes.
     * @param n the number of nodes requested.
     * @return an array containing the nodes.
     */
    private synchronized NodeInfo[] obtainNodes(int n) {
        NodeInfo[] nodes = new NodeInfo[n];
        int count = 0;

        // Should we sort the clusters here???
        // Do a "best fit"? Or "largest cluster first"???
        for (Iterator d = descr2nodeset.values().iterator(); d.hasNext();) {
            HashSet h = (HashSet) d.next();
            int index = count;
            for (Iterator i = h.iterator(); i.hasNext();) {
                NodeInfo nodeInfo = (NodeInfo) i.next();
                nodes[count++] = nodeInfo;
                if (count == n) {
                    break;
                }
            }
            for (int i = index; i < count; i++) {
                h.remove(nodes[i]);
            }
        }
        availableNodes -= count;
        if (count != n) {
            // Should not get here.
            logger.warn("Available node count is wrong!");
        }
        return nodes;
    }

    /**
     * Makes the specified node available for allocation.
     * @param node the node.
     */
    synchronized void releaseNode(NodeInfo node) {
        if (node.suspect) {
            // TODO: possibly try and rescue this node ???
            // Restart launcher on it ???
            totalNodes--;
            logger.warn("Remove node " + node.hostName);
            return;
        }
        HashSet h = (HashSet) descr2nodeset.get(node.descriptor);
        h.add(node);
        availableNodes++;
        if (nextJob != null && nextJob.getNumNodes() <= availableNodes) {
            notifyAll();
        }
    }

    /**
     * Invoked by the JavaGat when the user calls GAT.end().
     * Cleans up by killing all nodes.
     */
    public static void end() {
        for (Iterator d = descr2nodeset.values().iterator(); d.hasNext();) {
            HashSet h = (HashSet) d.next();
            for (Iterator i =h.iterator(); i.hasNext();) {
                NodeInfo nodeInfo = (NodeInfo) i.next();
                logger.info("Killing active objects on node "
                        + nodeInfo.hostName);
                try {
                    nodeInfo.node.getProActiveRuntime().killRT(true);
                } catch(Exception ex) {
                    // logger.info("Got exception from killRT, ignored:", ex);
                    // print removed, killRT always seems to give an
                    // exception: EOFException, unmarshallReturnHeader,
                    // which is understandable, because we are killing the
                    // other side, after all.
                }
            }
        }
    }

    /**
     * Submits a job described by the specified description to the job
     * queue and returns it.
     * @param description the job description.
     * @return the job.
     * @exception GATInvocationException when something goes wrong.
     */
    public org.gridlab.gat.resources.Job submitJob(JobDescription description)
        throws GATInvocationException {

        Job submittedJob;

        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "Job description does not contain a software description");
        }

        submittedJob = new Job(gatContext, preferences, description, this);

        synchronized(this) {
            jobList.add(submittedJob);
            if (jobList.size() == 1) {
                notifyAll();
            }
        }

        return submittedJob;
    }

    /**
     * Very simple-minded scheduling thread. It tries to schedule jobs
     * first-in, first-out (FIFO).
     */
    public void run() {
        Job job;
        for (;;) {
            NodeInfo[] nodes;
            synchronized(this) {
                // Obtain the first job from the joblist.
                while (jobList.size() == 0) {
                    try {
                        wait();
                    } catch(Exception e) {
                        // ignored
                    }
                }
                logger.debug("Got job to schedule");
                nextJob = (Job) jobList.remove(0);

                int nNodes = nextJob.getNumNodes();

                logger.debug("Want " + nNodes + " nodes");


                // Obtain enough nodes for it.
                while (availableNodes < nNodes) {
                    if (totalNodes < nNodes && remainingCalls <= 0) {
                        nextJob.submissionError(new GATInvocationException(
                                    "Not enough nodes available (" + totalNodes
                                    + "<" + nNodes + ")"));
                        nextJob = null;
                        break;
                    }
                    try {
                        wait();
                    } catch(Exception e) {
                        // ignored
                    }
                }

                if (nextJob != null) {
                    logger.debug("Got nodes to schedule the job on");
                    nodes = obtainNodes(nNodes);
                    job = nextJob;
                    nextJob = null;
                } else {
                    continue;
                }
            }
            try {
                job.startJob(nodes);
            } catch(Throwable e) {
                logger.warn("startJob threw exception: ", e);
            }
        }
    }
}
