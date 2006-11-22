package org.gridlab.gat.resources.cpi.proactive;

import ibis.util.ThreadPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;

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

    /**
     * Number of ProActive descriptors for which an addNodes call is
     * still expected.
     */
    private int remainingCalls;

    /** Logger. */
    static final Logger logger
        = ibis.util.GetLogger.getLogger(ProActiveResourceBrokerAdaptor.class);

    /** Mutable int object. */
    static final class Counter {
        int counter;
        Counter(int c) {
            counter = c;
        }
    }

    /**
     * A runnable for deployment, since the multithreading deployment in
     * ProActive does not work.
     */
    private class ActivateNode implements Runnable {
        NodeInfo node;
        Object[] parameters;
        HashSet h;
        Counter count;

        public ActivateNode(NodeInfo node, Object[] params, HashSet h,
                Counter count) {
            this.node = node;
            this.parameters = params;
            this.h = h;
            this.count = count;
        }

        public void run() {
            try {
                logger.info("Launching on " + node.hostName);
                node.launcher = (Launcher) ProActive.newActive(
                    Launcher.class.getName(), parameters, node.node);
                logger.info("launcher = " + node.launcher);
                synchronized(h) {
                    h.add(node);
                }
                logger.info("Launched on " + node.hostName);
            } catch(Throwable e) {
                logger.warn("newActive failed for node " + node.hostName, e);
            } finally {
                synchronized(count) {
                    count.counter--;
                    if (count.counter == 0) {
                        count.notifyAll();
                    }
                }
            }
        }
    }

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
                .get("ResourceBroker.ProActive.Descriptors");
        if (preferences == null) {
            throw new GATObjectCreationException("No descriptors provided. Set"
                    + " the ResourceBroker.ProActive.Descriptors preference to "
                    + " a comma-separated list of ProActive descriptor xmls.");
        }
        StringTokenizer tok = new StringTokenizer(descriptors, ",");
        ArrayList xmls = new ArrayList();
        while (tok.hasMoreTokens()) {
            xmls.add(tok.nextToken());
        }

        remainingCalls = xmls.size();

        for (int i = 0; i < xmls.size(); i++) {
            // Spawn a JobWatcher thread for each descriptor.
            String descr = (String) xmls.get(i);
            JobWatcher watcher = new JobWatcher();
            descr2watcher.put(descr, watcher);
            try {
                JobWatcher stub
                        = (JobWatcher) ProActive.turnActive(watcher);
                descr2watcherStub.put(descr, stub);
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

        Node[] proActiveNodes = (Node[]) nodes.toArray(new Node[0]);
        Object[][] parameters = new Object[proActiveNodes.length][];
        NodeInfo[] nodeInfo = new NodeInfo[proActiveNodes.length];
        for (int i = 0; i < proActiveNodes.length; i++) {
            System.out.println("ProActiveNode = " + proActiveNodes[i]);
            nodeInfo[i] = new NodeInfo(proActiveNodes[i], watcher, descriptor,
                    this);
            parameters[i] = new Object[] { stub, proActiveNodes[i]};
        }

/*
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


        int nNodes = proActiveNodes.length;

        // Here, we should somehow figure out how many CPUs each node
        // represents. Then, we can increment nNodes with the surplus
        // CPUs and create NodeInfo structures for them ...

*/

        Counter count = new Counter(nodeInfo.length);
        for (int i = 0; i < nodeInfo.length; i++) {
            ThreadPool.createNew(new ActivateNode(nodeInfo[i], parameters[i], 
                        h, count), "ActivateThread");
        }
        synchronized(count) {
            while (count.counter != 0) {
                try {
                    count.wait();
                } catch(Exception e) {
                    // Ignored
                }
            }
        }

        int nNodes = h.size();

        synchronized(this) {
            remainingCalls--;
            descr2nodeset.put(descriptor, h);
            totalNodes += nNodes;
            availableNodes += nNodes;
            notifyAll();
        }
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
        if (jobList.size() != 0) {
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
                    // nodeInfo.node.getProActiveRuntime().killAllNodes();
                    nodeInfo.node.getProActiveRuntime().killRT(false);
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
        for (;;) {
            NodeInfo[] nodes;
            Job job;
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
                job = (Job) jobList.get(0);

                int nNodes = job.getNumNodes();

                if (nNodes == 0) {
                    jobList.remove(0);
                    continue;
                }

                logger.debug("Want " + nNodes + " nodes");

                if (job.softHostCount) {
                    while (availableNodes == 0) {
                        try {
                            wait();
                        } catch(Exception e) {
                            // ignored
                        }
                    }
                    // Check if the nodes are still needed.
                    // Maybe nodes became available because the job is
                    // done ...
                    nNodes = job.getNumNodes();
                    if (nNodes == 0) {
                        continue;
                    }
                } else {
                    while (availableNodes < nNodes) {
                        if (totalNodes < nNodes && remainingCalls == 0) {
                            job.submissionError(new GATInvocationException(
                                        "Not enough nodes"));
                            jobList.remove(0);
                            job = null;
                            break;
                        }
                        try {
                            wait();
                        } catch(Exception e) {
                            // ignored
                        }
                    }
                    if (job == null) {
                        continue;
                    }
                }

                int n = availableNodes;
                if (n > nNodes) {
                    n = nNodes;
                }

                nodes = obtainNodes(n);
            }
            try {
                job.startJob(nodes);
            } catch(Throwable e) {
                logger.warn("startJob threw exception: ", e);
            }
            
            synchronized(this) {
                if (job.getNumNodes() == 0) {
                    jobList.remove(0);
                }
            }
        }
    }
}
