package org.gridlab.gat.resources.cpi.proactive;

import ibis.util.TypedProperties;

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
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;

/**
 * This class implements the JavaGat resource broker for ProActive.
 */
public class ProActiveResourceBrokerAdaptor extends ResourceBrokerCpi
        implements Runnable {
    
    static final int MAXTHREADS = TypedProperties.intProperty(
            "JavaGat.ProActive.NewActive.Parallel", 1); // safe default
    static final int MAXNODESPERWATCHER = TypedProperties.intProperty(
            "JavaGat.ProActive.NewActive.MaxNodesPerWatcher", 16);

    /** Set of available nodes. */
    private static HashSet availableNodeSet = new HashSet();

    /** Set of all nodes. */
    private static HashSet nodeSet = new HashSet();

    /** List of ProActiveDescriptors. */
    private static ArrayList pads = new ArrayList();

    /** Current watcher. */
    JobWatcher watcher = null;

    /** Current watcher stub. */
    JobWatcher stub = null;

    /** Count for current watcher. */
    int jobWatcherCount = 0;

    /** List of jobs to schedule. */
    private ArrayList jobList = new ArrayList();

    /**
     * Number of ProActive descriptors for which an addNodes call is
     * still expected.
     */
    private int remainingCalls;

    /** Logger. */
    static final Logger logger
        = ibis.util.GetLogger.getLogger(ProActiveResourceBrokerAdaptor.class);

    /**
     * A runnable for deployment, since the multithreading deployment in
     * ProActive does not work.
     */
    private class ActivateNode implements Runnable {
        NodeInfo node;
        Object[] parameters;

        public ActivateNode(NodeInfo node, Object[] params) {
            this.node = node;
            this.parameters = params;
        }

        public void run() {
            try {
                node.launcher = (Launcher) ProActive.newActive(
                    Launcher.class.getName(), parameters, node.node);
                synchronized(availableNodeSet) {
                    availableNodeSet.add(node);
                    nodeSet.add(node);
                    if (jobList.size() != 0) {
                        availableNodeSet.notifyAll();
                    }
                }
                logger.info("newActive Launcher on " + node.hostName);
            } catch(Throwable e) {
                logger.warn("newActive Launcher failed for node " 
                        + node.hostName, e);
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

        Runtime.getRuntime().addShutdownHook(
            new Thread("Nameserver ShutdownHook") {
                public void run() {
                    logger.info("Shutdown hook triggered");
                    try {
                        end();
                    } catch(Throwable e) {
                        // ignored
                    }
                }
            });

        for (int i = 0; i < xmls.size(); i++) {
            String descr = (String) xmls.get(i);
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
    void addNodes(String descriptor, ArrayList nodes, ProActiveDescriptor pad) {
        synchronized(availableNodeSet) {
            if (pad == null) {
                remainingCalls--;
                availableNodeSet.notifyAll();
                return;
            }
            pads.add(pad);
        }

        logger.debug("Adding " + nodes.size() + " nodes");

        Node[] proActiveNodes = (Node[]) nodes.toArray(new Node[0]);
        Object[][] parameters = new Object[proActiveNodes.length][];
        NodeInfo[] nodeInfo = new NodeInfo[proActiveNodes.length];
        for (int i = 0; i < proActiveNodes.length; i++) {
            System.out.println("ProActiveNode = " + proActiveNodes[i]);
            if (watcher == null || jobWatcherCount >= MAXNODESPERWATCHER) {
                JobWatcher newWatcher = new JobWatcher();
                JobWatcher newStub;
                try {
                    newStub = (JobWatcher) ProActive.turnActive(newWatcher);
                } catch(Exception e) {
                    try {
                        Thread.sleep(1000);
                    } catch(Exception e2) {
                        // ignored
                    }
                    try {
                        newStub = (JobWatcher) ProActive.turnActive(newWatcher);
                    } catch(Exception e3) {
                        // Tried twice. What now??? Use old watcher/stub
                        logger.error("Could not create stub", e3);
                        newWatcher = watcher;
                        newStub = stub;
                    }
                }
                watcher = newWatcher;
                stub = newStub;
                jobWatcherCount = 0;
                if (watcher == null) {
                    logger.error("Could not create turnActive watcher");
                    return;
                }
            }
            jobWatcherCount++;
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

        Threader threader = Threader.createThreader(MAXTHREADS);
        for (int i = 0; i < nodeInfo.length; i++) {
            threader.submit(new ActivateNode(nodeInfo[i], parameters[i]));
        }
        threader.waitForAll();

        synchronized(availableNodeSet) {
            remainingCalls--;
            pads.add(pad);
            availableNodeSet.notifyAll();
        }
    }

    /**
     * Reserves and returns the specified number of nodes.
     * @param n the number of nodes requested.
     * @return an array containing the nodes.
     */
    private NodeInfo[] obtainNodes(int n) {
        NodeInfo[] nodes = new NodeInfo[n];
        HashSet h = new HashSet();

        logger.debug("ObtainNodes: n = " + n + ", size = "
                + availableNodeSet.size());

        synchronized(availableNodeSet) {
            int index = 0;
            for (Iterator i = availableNodeSet.iterator(); i.hasNext();) {
                NodeInfo nodeInfo = (NodeInfo) i.next();
                nodes[index++] = nodeInfo;
                h.add(nodeInfo);
                if (index == n) {
                    break;
                }
            }
            availableNodeSet.removeAll(h);
            logger.debug("ObtainNodes: afterwards: size = " + availableNodeSet.size());
        }
        return nodes;
    }

    /**
     * Makes the specified node available for allocation.
     * @param node the node.
     */
    void releaseNode(NodeInfo node) {
        synchronized(availableNodeSet) {
            if (node.suspect) {
                // TODO: possibly try and rescue this node ???
                // Restart launcher on it ???
                nodeSet.remove(node);
                logger.warn("Remove node " + node.hostName);
                return;
            }
            availableNodeSet.add(node);
            if (jobList.size() != 0) {
                availableNodeSet.notifyAll();
            }
        }
    }

    /**
     * Invoked by the JavaGat when the user calls GAT.end().
     * Cleans up by killing all nodes.
     */
    public static void end() {
        if (nodeSet.size() != 0) {
            Threader threader = Threader.createThreader(MAXTHREADS);
            for (Iterator i = nodeSet.iterator(); i.hasNext();) {
                final NodeInfo nodeInfo = (NodeInfo) i.next();
                threader.submit(new Thread("Terminator") {
                    public void run() {
                        logger.info("Sending terminate to node "
                                + nodeInfo.hostName);
                        synchronized(nodeInfo) {
                            try {
                                nodeInfo.launcher.terminate();
                            } catch(Throwable ex) {
                                // ignored
                            }
                        }
                    }
                });
            }

            nodeSet.clear();

            threader.waitForAll();

            try {
                Thread.sleep(5000);
            } catch(Exception e) {
                // ignored
            }
        }

        for (int i = 0; i < pads.size(); i++) {
            ProActiveDescriptor pad = (ProActiveDescriptor) pads.get(i);
            try {
                pad.killall(false);
            } catch(Exception e) {
                // ignored
            }
        }
        pads.clear();
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

        synchronized(availableNodeSet) {
            jobList.add(submittedJob);
            if (jobList.size() == 1) {
                availableNodeSet.notifyAll();
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
            synchronized(availableNodeSet) {
                // Obtain the first job from the joblist.
                while (jobList.size() == 0) {
                    try {
                        availableNodeSet.wait();
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
                    while (availableNodeSet.size() == 0
                            && (nodeSet.size() != 0 || remainingCalls > 0)) {
                        try {
                            availableNodeSet.wait();
                        } catch(Exception e) {
                            // ignored
                        }
                    }
                    // Check if the nodes are still needed.
                    // Maybe nodes became available because the job is
                    // done ...
                    if (availableNodeSet.size() == 0 && nodeSet.size() == 0) {
                        job.submissionError(new GATInvocationException(
                                "No nodes available"));
                        jobList.remove(0);
                        continue;
                    }
                    nNodes = job.getNumNodes();
                    if (nNodes == 0) {
                        continue;
                    }
                } else {
                    while (availableNodeSet.size() < nNodes) {
                        if (nodeSet.size() < nNodes && remainingCalls == 0) {
                            job.submissionError(new GATInvocationException(
                                    "Not enough nodes"));
                            jobList.remove(0);
                            job = null;
                            break;
                        }
                        try {
                            availableNodeSet.wait();
                        } catch(Exception e) {
                            // ignored
                        }
                    }
                    if (job == null) {
                        continue;
                    }
                }

                int n = availableNodeSet.size();
                if (n > nNodes) {
                    n = nNodes;
                }

                nodes = obtainNodes(n);
            }
            try {
                job.startJob(nodes);
                logger.info("Adding " + nodes.length + " nodes to job " + job);
            } catch(Throwable e) {
                logger.warn("startJob threw exception: ", e);
            }
            
            synchronized(availableNodeSet) {
                if (job.getNumNodes() == 0) {
                    jobList.remove(0);
                }
            }
        }
    }
}
