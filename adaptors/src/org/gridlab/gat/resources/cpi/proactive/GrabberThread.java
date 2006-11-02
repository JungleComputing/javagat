package org.gridlab.gat.resources.cpi.proactive;

import java.util.ArrayList;
import java.util.HashMap;

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
    private ProActiveResourceBrokerAdaptor adaptor;

    /** Preferences as specified by the GAT user. */
    private Preferences preferences;

    /** List of nodes from this cluster. */
    private ArrayList nodes = new ArrayList();

    GrabberThread(String descriptor,
            ProActiveResourceBrokerAdaptor adaptor,
            Preferences preferences) {
        setDaemon(true);
        this.descriptor = descriptor;
        this.adaptor = adaptor;
        this.preferences = preferences;
        start();
    }

    public void run() {
        String virtualNodeName
            = (String) preferences.get(
                    "ResourceBroker.proActive.virtualNodeName");
        try {
            // Get information from ProActive descriptor
            ProActiveDescriptor pad
                    = ProActive.getProactiveDescriptor(descriptor);
            pad.activateMappings();
            if (virtualNodeName != null) {
                VirtualNode vn = pad.getVirtualNode(virtualNodeName);
                Node[] crtNodes = vn.getNodes();
                for (int i = 0; i < crtNodes.length; i++) {
                    nodes.add(crtNodes[i]);
                }
            } else {
                VirtualNode[] vns = pad.getVirtualNodes();
                for (int i = 0; i < vns.length; i++) {
                    Node[] crtNodes = vns[i].getNodes();
                    for (int j = 0; j < crtNodes.length; j++) {
                        nodes.add(crtNodes[j]);
                    }
                }
            }

            adaptor.addNodes(descriptor, nodes);

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
            adaptor.addNodes(descriptor, nodes);
        }
    }
}
