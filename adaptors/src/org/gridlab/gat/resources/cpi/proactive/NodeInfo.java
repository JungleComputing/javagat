package org.gridlab.gat.resources.cpi.proactive;

import org.objectweb.proactive.core.node.Node;

/**
 * Container for some information about a node.
 * Note that a ProActive node may have more than one cpu. In that case,
 * there should be more than one NodeInfo object for it, so each NodeInfo
 * object represents one schedulable cpu.
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

    /** ID of the instance currently running on this node, or null. */
    String instanceID;

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
     * Sets the instanceID for this node.
     */
    synchronized void setID(String id) {
        instanceID = id;
    }

    /**
     * Releases this node, and sets the suspect flag
     * if instructed to do so.
     * @param suspect the flag.
     */
    synchronized void release(boolean suspect) {
        instanceID = null;
        if (suspect) {
            suspect = true;
        }
        broker.releaseNode(this);
    }
}
