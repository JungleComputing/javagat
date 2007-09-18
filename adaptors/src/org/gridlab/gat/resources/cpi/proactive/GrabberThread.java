package org.gridlab.gat.resources.cpi.proactive;

import java.util.ArrayList;

import org.gridlab.gat.Preferences;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

/**
 * This class provides a thread that initiates the cluster information.
 */
class GrabberThread extends Thread {

    /** Name of the ProActive descriptor file. */
    private String descriptor;

    /** The resource broker. */
    private ProActiveResourceBrokerAdaptor broker;

    /** Preferences as specified by the GAT user. */
    private Preferences preferences;

    /** List of nodes from this cluster. */
    private ArrayList<Node> nodes = new ArrayList<Node>();

    /**
     * Constructor, initializes the fields with the specified parameters
     * and starts the thread.
     * @param descriptor filename of the ProActive descriptor.
     * @param broker the resource broker.
     * @param preferences the preferences.
     */
    GrabberThread(String descriptor, ProActiveResourceBrokerAdaptor broker,
            Preferences preferences) {
        setDaemon(true);
        this.descriptor = descriptor;
        this.broker = broker;
        this.preferences = preferences;
        start();
    }

    public void run() {
        ProActiveDescriptor pad = null;
        String virtualNodeName
            = (String) preferences.get(
                    "ResourceBroker.ProActive.VirtualNodeName");
        try {
            // Get information from ProActive descriptor
            synchronized(this.getClass()) {
                // Ouch, can ProActive really not deal with doing this in
                // parallel? It seems to corrupt the node information when
                // processing multiple ProActive descriptors simultaneously.
                pad = ProActive.getProactiveDescriptor(descriptor);
                if (virtualNodeName != null) {
                    pad.activateMapping(virtualNodeName);
                    ProActiveResourceBrokerAdaptor.logger.info(
                            "virtualNodeName = " + virtualNodeName);
                    VirtualNode vn = pad.getVirtualNode(virtualNodeName);
                    Node[] crtNodes = vn.getNodes();
                    for (int i = 0; i < crtNodes.length; i++) {
                        nodes.add(crtNodes[i]);
                    }
                } else {
                    pad.activateMappings();
                    VirtualNode[] vns = pad.getVirtualNodes();
                    for (int i = 0; i < vns.length; i++) {
                        Node[] crtNodes = vns[i].getNodes();
                        for (int j = 0; j < crtNodes.length; j++) {
                            nodes.add(crtNodes[j]);
                        }
                    }
                }
            }

            broker.addNodes(descriptor, nodes, pad);

            /*
            System.out.println("vn.getNumberOfCreatedNodesAfterDeployment() = "
                    + vn.getNumberOfCreatedNodesAfterDeployment());
            System.out.println("vn.getNumberOfCurrentlyCreatedNodes() = "
                    + vn.getNumberOfCurrentlyCreatedNodes());
            */
        } catch (Exception e) {
            // Something failed for this cluster. Ignore it.
            ProActiveResourceBrokerAdaptor.logger.error(
                    "Exception in GrabberThread:", e);
            nodes.clear();
            broker.addNodes(descriptor, nodes, pad);
        }
    }
}
