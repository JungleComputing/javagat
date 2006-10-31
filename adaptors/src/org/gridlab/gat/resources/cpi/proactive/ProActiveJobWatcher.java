package org.gridlab.gat.resources.cpi.proactive;

import java.util.ArrayList;

import org.gridlab.gat.Preferences;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

/**
 * This class provides a thread that keeps track of the status of all
 * ProActiveJobs. We cannot use a separate thread for each job, because
 * there can be many simultaneous jobs. On the other hand, this may be
 * too slow. We'll see. We now have one of these threads for each cluster.
 */
class ProActiveJobWatcher extends Thread {

    /** An array of ProActive jobs. */
    private ArrayList jobs = new ArrayList();

    /** Name of the ProActive descriptor file. */
    private String descriptor;

    /** The resource broker. */
    private ProActiveResourceBrokerAdaptor adaptor;

    /** Preferences as specified by the GAT user. */
    private Preferences preferences;

    /** List of nodes from this cluster. */
    private ArrayList nodes = new ArrayList();

    ProActiveJobWatcher(String descriptor,
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
        String mustStartLauncher
            = (String) preferences.get(
                    "ResourceBroker.proActive.startLauncherOnAllNodes");
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

            for (int i = 0; i < nodes.size(); i++) {
                try {
                    if (mustStartLauncher != null) {
                        adaptor.startLauncher((Node) nodes.get(i), false);
                    }
                } catch(Exception e) {
                    ProActiveResourceBrokerAdaptor.logger.error(
                            "Starting launcher on "
                            + ((Node) nodes.get(i)).getNodeInformation().getURL()
                            + " failed:", e);
                    nodes.remove(i);
                    i--;
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
                    "Exception in ProActiveWatcherThread:", e);
            nodes.clear();
            adaptor.addNodes(descriptor, nodes);
            return;
        }

        // Now, keep track of jobs submitted on nodes from this ProActive
        // descriptor. For scalability, we may in the future have to spawn
        // this part to a node on the cluster.
        // For now, we have a thread per cluster.
        // The only function is that it must initiate the post-staging,
        // when the job is no longer running. There is no way to post an
        // upcall for this, so the only way is to poll all jobs every
        // now and again.
        // (We could have a blocking method in the launcher, but then we need
        // a thread for each job).
        while (true) {
            ProActiveJob[] jbs;
            synchronized(this) {
                jbs = (ProActiveJob[]) jobs.toArray(new ProActiveJob[0]);
            }
            int finishedCount = 0;
            for (int i = 0; i < jbs.length; i++) {
                int status = jbs[i].setState();
                if (status != ProActiveJob.RUNNING) {
                    if (status == ProActiveJob.POST_STAGING) {
                        jbs[i].initiatePostStaging();
                    }
                    synchronized(this) {
                        jobs.remove(i - finishedCount);
                        finishedCount++;
                    }
                }
            }
            try {
                Thread.sleep(10*1000);
            } catch(Exception e) {
                // Ignored
            }
        }
    }

    /**
     * Adds a job to the job list to be watched by the ProActiveJobWatcher
     * thread.
     * @param job the job to be watched.
     */
    synchronized void addJob(ProActiveJob job) {
        jobs.add(job);
    }
}
