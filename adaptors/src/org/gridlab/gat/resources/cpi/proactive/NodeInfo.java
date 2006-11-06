package org.gridlab.gat.resources.cpi.proactive;

import org.objectweb.proactive.core.node.Node;

/**
 * Container for some information about a node.
 */
class NodeInfo {

    /** The host name of the node. */
    String hostName;

    /** The ProActive descriptor file name. */
    String descriptor;

    /** The resource broker. */
    ProActiveResourceBrokerAdaptor broker;

    /** The launcher for this node. */
    Launcher launcher;

    /** ProActive node representation. */
    Node node;

    /** The watcher for this node's cluster. */
    JobWatcher watcher;

    /** Number of jobs currently running on this node. */
    int jobCount = 0;

    /** Set to true if this node is suspected not to work. */
    boolean suspect = false;

    /**
     * Constructor, initializes fields from the specified parameters.
     * @param node the ProActive node.
     * @param w the cluster watcher.
     * @param broker the resource broker.
     */
    NodeInfo(Node node, JobWatcher w, String descriptor, 
            ProActiveResourceBrokerAdaptor b) {
        hostName = node.getNodeInformation().getHostName();
        this.node = node;
        this.watcher = w;
        this.broker = b;
        this.descriptor = descriptor;
    }

    /**
     * Increments the job count for this node.
     */
    synchronized void incrCount() {
        jobCount++;
    }

    /**
     * Decrements the job count for this node, and sets the suspect flag
     * if instructed to do so.
     */
    synchronized void decrCount(boolean suspect) {
        jobCount--;
        if (suspect) {
            suspect = true;
        }
        if (jobCount == 0) {
            broker.releaseNode(this);
        }
    }
}
