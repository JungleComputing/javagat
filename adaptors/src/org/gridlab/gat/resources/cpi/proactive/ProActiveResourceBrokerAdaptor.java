package org.gridlab.gat.resources.cpi.proactive;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;

public class ProActiveResourceBrokerAdaptor extends ResourceBrokerCpi {

    private static Hashtable launcherTable = new Hashtable();

    String descriptorURLs[];

    private int totalNodes = 0;

    private int gotNodesFromDescriptors = 0;

    private ArrayList[] nodes;

    private int currentList = 0;

    private int currentIndex = 0;

    private ProActiveJobWatcher[] watchers;

    static final Logger logger
        = ibis.util.GetLogger.getLogger(ProActiveResourceBrokerAdaptor.class);

    public ProActiveResourceBrokerAdaptor(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {

        super(gatContext, preferences);

        if (preferences.containsKey("ResourceBroker.proActive.descriptors")) {
            String descriptors = (String) preferences
                .get("ResourceBroker.proActive.descriptors");
            StringTokenizer tok = new StringTokenizer(descriptors, ",");
            Vector xmls = new Vector();
            while (tok.hasMoreTokens()) {
                xmls.add(tok.nextToken());
            }
            descriptorURLs = (String[]) xmls.toArray(new String[xmls.size()]);
            nodes = new ArrayList[descriptorURLs.length];
            watchers = new ProActiveJobWatcher[nodes.length];
        } else {
            throw new GATObjectCreationException("No descriptors provided. Set"
                    + " the ResourceBroker.proActive.descriptors preference to "
                    + " a comma-separated list of ProActive descriptor xmls.");
        }

        for (int i = 0; i < descriptorURLs.length; i++) {
            // Spawn a JobWatcher thread for each descriptor.
            watchers[i] = new ProActiveJobWatcher(descriptorURLs[i], this,
                    preferences);
        }

        // The JobWatcher threads make an inventory of the nodes available.
        // Wait until at least one has made available some nodes, or all
        // of them don't have any nodes.
        synchronized (this) {
            while (gotNodesFromDescriptors < descriptorURLs.length &&
                    totalNodes == 0) {
                try {
                    wait();
                } catch(Exception e) {
                    // ignored
                }
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

            logger.info("Started launcher on: "
                    + nodeInf.getHostName() + ", url: "
                    + nodeInf.getURL());
        }
        return launcher;
    }

    synchronized void addNodes(String descriptor, ArrayList nodes) {
        for (int i = 0; i < descriptorURLs.length; i++) {
            if (descriptorURLs[i].equals(descriptor)) {
                this.nodes[i] = nodes;
                totalNodes += nodes.size();
                gotNodesFromDescriptors++;
                if (gotNodesFromDescriptors == descriptorURLs.length
                        || totalNodes != 0) {
                    notifyAll();
                }
                break;
            }
        }
    }

    synchronized void removeNode(int index) {
        totalNodes--;
        nodes[currentList].remove(index);
        currentIndex--;
    }

    public static void end() {
        for (Iterator i = launcherTable.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            Node n = (Node) e.getKey();
            logger.info("Killing active objects on node "
                    + n.getNodeInformation().getURL());
            try {
                Object[] objs = n.getActiveObjects();
                if (logger.isInfoEnabled() && objs != null) {
                    for (int j = 0; j < objs.length; j++) {
                        logger.info("Object " + j + ": " + objs[j]);
                    }
                }
                ProActiveRuntime rt = n.getProActiveRuntime();
                // rt.killNode(n.getNodeInformation().getName());
                rt.killRT(true);
                // n.killAllActiveObjects();
                // ProActiveLauncher l = (ProActiveLauncher) e.getValue();
                // l.die();
            } catch(Exception ex) {
                logger.info("Got exception from killRT, ignored:", ex);
            }
        }
    }

    private synchronized int getBestSiteCrawler() {
        if (totalNodes <= 0) {
            return -1;
        }
        while (nodes[currentList] == null
                || currentIndex >= nodes[currentList].size()) {
            currentIndex = 0;
            currentList++;
            if (currentList >= nodes.length) {
                currentList = 0;
            }
        }
        return currentIndex++;
    }

    public Job submitJob(JobDescription description)
        throws GATInvocationException {

        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "Job description does not contain a software description");
        }

        for (;;) {
            int index = getBestSiteCrawler();

            if (index == -1) {
                throw new GATInvocationException("No nodes available");
            }

            Node node = (Node) nodes[currentList].get(index);

            NodeInformation nodeInf = node.getNodeInformation();

            logger.info("node.getNodeInformation().getHostName() = "
                    + nodeInf.getHostName());

            ProActiveLauncher launcher;
            try {
                launcher = startLauncher(node, false);
            } catch (Exception e) {
                logger.warn("Failed to deploy launcher on node "
                        + nodeInf.getURL() + ", removing node ...");
                removeNode(index);
                continue;
            }

            try {
                return new ProActiveJob(gatContext, preferences, launcher,
                        description, node, watchers[currentList]);
            } catch(Throwable e) {
                logger.warn("Launcher on node " + nodeInf.getURL()
                        + " failed to launch. Pinging ...");
                boolean pingFailed = false;
                try {
                    IntWrapper iw = launcher.ping();
                    if (iw.intValue() != 0) {
                        throw new Exception("Whatever ...");
                    }
                } catch(Exception ex) {
                    pingFailed = true;
                }
                if (pingFailed) {
                    logger.warn("Redeploying launcher on node "
                            + nodeInf.getURL());
                    try {
                        launcher = startLauncher(node, true);
                    } catch (Exception ex) {
                        logger.warn("Failed to deploy launcher on node "
                                + nodeInf.getURL() + ", removing node ...");
                        removeNode(index);
                        continue;
                    }
                } else {
                    logger.error("Launcher ping succeeded, something "
                            + "else is wrong");
                    throw new GATInvocationException("Failed to launch", e);
                }
                try {
                    return new ProActiveJob(gatContext, preferences, launcher,
                            description, node, watchers[currentList]);
                } catch(Throwable ex) {
                    logger.error("Launcher on node " + nodeInf.getURL()
                        + " failed to launch, giving up");
                    throw new GATInvocationException("Failed to launch", ex);
                }
            }
        }
    }
}
