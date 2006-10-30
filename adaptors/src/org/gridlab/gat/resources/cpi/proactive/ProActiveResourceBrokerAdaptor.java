package org.gridlab.gat.resources.cpi.proactive;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.MethodNotApplicableException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;

class GrabberThread extends Thread {
    String descriptor;

    ProActiveResourceBrokerAdaptor adaptor;

    Preferences preferences;

    public GrabberThread(String descriptor,
            ProActiveResourceBrokerAdaptor adaptor,
            Preferences preferences) {
        this.descriptor = descriptor;
        this.adaptor = adaptor;
        this.preferences = preferences;
    }

    public void run() {
        String virtualNodeName
            = (String) preferences.get("ResourceBroker.proActive.virtualNodeName");
        String mustStartLauncher
            = (String) preferences.get("ResourceBroker.proActive.startLauncherOnAllNodes");
        try {
            ProActiveDescriptor pad = ProActive.getProactiveDescriptor(descriptor);
            Node[] crtNodes = null;
            pad.activateMappings();
            if (virtualNodeName != null) {
                VirtualNode vn = pad.getVirtualNode(virtualNodeName);
                crtNodes = vn.getNodes();
            } else {
                VirtualNode[] vns = pad.getVirtualNodes();
                Node[][] nodes = new Node[vns.length][];
                int length = 0;
                for (int i = 0; i < vns.length; i++) {
                    nodes[i] = vns[i].getNodes();
                    length += nodes[i].length;
                }
                int index = 0;
                crtNodes = new Node[length];
                for (int i = 0; i < nodes.length; i++) {
                    for (int j = 0; j < nodes[i].length; j++) {
                        crtNodes[index] = nodes[i][j];
                        index++;
                    }
                }
            }

            for (int j = 0; j < crtNodes.length; j++) {
                try {
                    if (mustStartLauncher != null) {
                        adaptor.startLauncher(crtNodes[j], false);
                    }
                    adaptor.gridNodesVector.add(crtNodes[j]);
                } catch(Exception e) {
                    System.out.println("Starting launcher on "
                            + crtNodes[j].getNodeInformation().getURL()
                            + " failed:");
                    e.printStackTrace();
                }
            }

            /*
            System.out.println("vn.getNumberOfCreatedNodesAfterDeployment() = "
                    + vn.getNumberOfCreatedNodesAfterDeployment());
            System.out.println("vn.getNumberOfCurrentlyCreatedNodes() = "
                    + vn.getNumberOfCurrentlyCreatedNodes());
            */
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

public class ProActiveResourceBrokerAdaptor extends ResourceBrokerCpi {

    Vector gridNodesVector = new Vector();

    private static Hashtable launcherTable = new Hashtable();

    private int gridNodesIndex = 0;

    private static final int MAX_WAIT_TRIES=10;

    public ProActiveResourceBrokerAdaptor(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {

        super(gatContext, preferences);

        String[] descriptorURLs;

        if (preferences.containsKey("ResourceBroker.proActive.descriptors")) {
            String descriptors = (String) preferences
                .get("ResourceBroker.proActive.descriptors");
            StringTokenizer tok = new StringTokenizer(descriptors, ",");
            Vector xmls = new Vector();
            while (tok.hasMoreTokens()) {
                xmls.add(tok.nextToken());
            }
            descriptorURLs = (String[]) xmls.toArray(new String[xmls.size()]);
        } else {
            throw new GATObjectCreationException("No descriptors provided. Set"
                    + " the ResourceBroker.proActive.descriptors preference to "
                    + " a comma-separated list of ProActive descriptor xmls.");
        }

        for (int i = 0; i < descriptorURLs.length; i++) {
            try {
                new GrabberThread(descriptorURLs[i], this, preferences).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int waitTries=0;

        while (gridNodesVector.size() == 0 && waitTries < MAX_WAIT_TRIES ) {
            // TODO: FIX THIS
            System.out.println("Trying to initialize grid from ProActive site descriptors ... attempt "+waitTries+"/"+MAX_WAIT_TRIES);
            waitTries ++;
            try {
                Thread.sleep(20000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    ProActiveLauncher startLauncher(Node node, boolean force) throws Exception {
        NodeInformation nodeInf = node.getNodeInformation();
        ProActiveLauncher launcher 
            = (ProActiveLauncher) launcherTable.get(node);

        if (force || launcher == null) {
            if (launcher != null) {
                launcherTable.remove(node);
            }
            launcher = (ProActiveLauncher) ProActive.newActive(
                    ProActiveLauncher.class.getName(), null, node);

            launcherTable.put(node, launcher);

            System.out.println("Started launcher on: "
                    + nodeInf.getHostName() + ", url: "
                    + nodeInf.getURL());
        }
        return launcher;
    }

    public static void end() {
        for (Iterator i = launcherTable.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            Node n = (Node) e.getKey();
            System.out.println("Killing active objects on node "
                    + n.getNodeInformation().getURL());
            try {
                Object[] objs = n.getActiveObjects();
                if (objs != null) {
                    for (int j = 0; j < objs.length; j++) {
                        System.out.println("Object " + j + ": " + objs[j]);
                    }
                }
                ProActiveRuntime rt = n.getProActiveRuntime();
                // rt.killNode(n.getNodeInformation().getName());
                rt.killRT(true);
                // n.killAllActiveObjects();
                // ProActiveLauncher l = (ProActiveLauncher) e.getValue();
                // l.die();
            } catch(Exception ex) {
                // ex.printStackTrace();
                // ignored
            }
        }
    }

    private int getBestSiteCrawler() {
        if (gridNodesVector == null || gridNodesVector.size() == 0) {
            return -1;
        }

        int rv = gridNodesIndex++;

        if (gridNodesIndex == gridNodesVector.size()) gridNodesIndex = 0;

        return rv;
    }

    public Job submitJob(JobDescription description)
        throws GATInvocationException, IOException {

        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "Job description does not contain a software description");
        }

        for (;;) {
            int index = getBestSiteCrawler();

            if (index <= -1) {
                throw new GATInvocationException(
                        "There are no nodes available, cannot launch job.");
            }

            Node node = (Node) gridNodesVector.get(index);
            NodeInformation nodeInf = node.getNodeInformation();

            System.out.println("node.getNodeInformation().getHostName() = "
                    + nodeInf.getHostName());

            ProActiveLauncher launcher;
            try {
                launcher = startLauncher(node, false);
            } catch (Exception e) {
                System.out.println("Failed to deploy launcher on node "
                        + nodeInf.getURL() + ", removing node ...");
                gridNodesVector.remove(node);
                continue;
            }

            try {
                return new ProActiveJob(gatContext, preferences, launcher,
                        description, node);
            } catch(Throwable e) {
                System.out.println("Launcher on node " + nodeInf.getURL()
                        + " failed to launch. Pinging ...");
                boolean pingFailed = false;
                try {
                    launcher.ping();
                } catch(Exception ex) {
                    pingFailed = true;
                }
                if (pingFailed) {
                    System.out.println("Redeploying launcher on node "
                            + nodeInf.getURL());
                    try {
                        launcher = startLauncher(node, true);
                    } catch (Exception ex) {
                        System.out.println("Failed to deploy launcher on node "
                                + nodeInf.getURL() + ", removing node ...");
                        gridNodesVector.remove(node);
                        continue;
                    }
                } else {
                    System.out.println("Launcher ping succeeded, something "
                            + "else is wrong");
                    throw new GATInvocationException("Failed to launch", e);
                }
                try {
                    return new ProActiveJob(gatContext, preferences, launcher,
                            description, node);
                } catch(Throwable ex) {
                    System.out.println("Launcher on node " + nodeInf.getURL()
                        + " failed to launch, giving up");
                    throw new GATInvocationException("Failed to launch", ex);
                }
            }
        }
    }
}
