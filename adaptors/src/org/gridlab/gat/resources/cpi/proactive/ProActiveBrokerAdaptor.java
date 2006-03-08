package org.gridlab.gat.resources.cpi.proactive;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.GATAdaptor.ProActiveLauncher;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

class GrabberThread extends Thread {
    String descriptor;

    ProActiveDescriptor pad;

    VirtualNode vn;

    Hashtable launcherTable;

    Vector gridNodesVector;

    public GrabberThread(String descriptor, Hashtable launcherTable,
            Vector gridNodesVector) {
        this.descriptor = descriptor;
        this.launcherTable = launcherTable;
        this.gridNodesVector = gridNodesVector;
    }

    public void run() {
        try {
            pad = ProActive.getProactiveDescriptor(descriptor);
            pad.activateMappings();
            vn = pad.getVirtualNode("plugtest");
            Node[] crtNodes = vn.getNodes();

            for (int j = 0; j < crtNodes.length; j++) {
                startLauncher(crtNodes[j]);
            }

            System.out.println("vn.getNumberOfCreatedNodesAfterDeployment() = "
                + vn.getNumberOfCreatedNodesAfterDeployment());
            System.out.println("vn.getNumberOfCurrentlyCreatedNodes() = "
                + vn.getNumberOfCurrentlyCreatedNodes());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void startLauncher(Node node) {
        Object o = launcherTable.get(node.getNodeInformation().getURL());

        ProActiveLauncher launcher = null;

        if (o != null) launcher = (ProActiveLauncher) o;

        if (launcher == null)
            try {
                launcher = (ProActiveLauncher) ProActive.newActive(
                    ProActiveLauncher.class.getName(), null, node);

                launcherTable.put(node.getNodeInformation().getURL(), launcher);

                gridNodesVector.add(node);

                System.out.println("Started launcher on: "
                    + node.getNodeInformation().getHostName() + ", url: "
                    + node.getNodeInformation().getURL());
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

}

public class ProActiveBrokerAdaptor extends ResourceBrokerCpi {

    ProActiveDescriptor[] pads;

    VirtualNode[] vns;

    Node[] gridNodes;

    VirtualNode[] nodeMappings;

    private Vector gridNodesVector = new Vector();

    //	private Vector nodeMappingsVector = new Vector();
    private Hashtable launcherTable = new Hashtable();

    //	private Vector frontends = new Vector();
    private int gridNodesIndex = 0;

    String[] descriptorURLs = null;

    //	private boolean VERBOSE = true;
    //	private boolean DEBUG = true;

    private static int MAX_WAIT_TRIES=10;

    public ProActiveBrokerAdaptor(GATContext gatContext, Preferences preferences)
            throws GATObjectCreationException {
        super(gatContext, preferences);
        checkName("proActive");

        if(true) throw new GATObjectCreationException("proactive adaptor is disabled for now");
        
        if (preferences.containsKey("ResourceBroker.proActive.descriptors")) {
            String descriptors = (String) preferences
                .get("ResourceBroker.proActive.descriptors");
            StringTokenizer tok = new StringTokenizer(descriptors, ",");
            Vector xmls = new Vector();
            while (tok.hasMoreTokens())
                xmls.add(tok.nextToken());
            descriptorURLs = (String[]) xmls.toArray(new String[xmls.size()]);
        } else {
            //descriptorURLs = new String[1];
            //descriptorURLs[0] = "/home3/aagapi/RA/NQueens/adaptor1.xml";
		throw new GATObjectCreationException("No descriptors provided. Set the ResourceBroker.proActive.descriptors preferences object to use the Pro Active adaptor.");
        }

        for (int i = 0; i < descriptorURLs.length; i++) {
            try {
                new GrabberThread(descriptorURLs[i], launcherTable,
                    gridNodesVector).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

	int waitTries=0;

        while (gridNodesVector.size() == 0 && waitTries < MAX_WAIT_TRIES ) {
            System.out.println("Trying to initialize grid from ProActive site descriptors ... attempt "+waitTries+"/"+MAX_WAIT_TRIES);
	    waitTries ++;
            try {
                Thread.sleep(20000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     private ResourceDescription constructResourceDescription(String hostname)
     {
     Hashtable ht = new Hashtable();
     ht.put("machine.node", hostname);
     return new HardwareResourceDescription(ht);
     }
     */

    private int getBestSiteCrawler() {
        int rv = -1;

        if (gridNodesVector == null) return -1;

        if (gridNodesVector.size() == 0) return -1;

        rv = gridNodesIndex++;

        if (gridNodesIndex == gridNodesVector.size()) gridNodesIndex = 0;

        return rv;
    }

    void preStage(JobDescription description, String host) {
        try {
            java.util.Map files = resolvePreStagedFiles(description, host);
            java.util.Set keys = files.keySet();
            java.util.Iterator i = keys.iterator();
            while (i.hasNext()) {
                File srcFile = (File) i.next();

                Runtime.getRuntime().exec(
                    "scp " + srcFile.getName() + " " + host + ":.").waitFor();
                Thread.sleep(100);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public Job submitJob(JobDescription description)
            throws GATInvocationException, IOException {
        //		ResourceDescription rd = null;
        boolean classpathNext = false;
        HashMap JVMParams = new HashMap();
        String crtJVMParamPair = null;
        //		boolean firstTime=false, firstTimeFrontend=true;

        int index = getBestSiteCrawler();

        Node node = null;

        if (index > -1)
            node = (Node) gridNodesVector.get(index);
        else
            throw new GATInvocationException(
                "There are no nodes available, cannot launch job.");

        Object o = launcherTable.get(node.getNodeInformation().getURL());

        ProActiveLauncher launcher = null;

        if (o != null) launcher = (ProActiveLauncher) o;

        if (launcher == null)
            try {
                launcher = (ProActiveLauncher) ProActive.newActive(
                    ProActiveLauncher.class.getName(), null, node);

                launcherTable.put(node.getNodeInformation().getURL(), launcher);
            } catch (Exception e) {
                e.printStackTrace();
            }

        String[] args = description.getSoftwareDescription().getArguments();

        String classname = null;
        String classpath = null;
        Vector arguments = new Vector();

        for (int i = 0; i < args.length; i++) {
            String crt = args[i];
            if (crt.startsWith("-")) {
                if (crt.equals("-cp")) classpathNext = true;
                if (crt.startsWith("-D")) {
                    crtJVMParamPair = crt.substring(2);
                    JVMParams.put(crtJVMParamPair.substring(0, crtJVMParamPair
                        .indexOf("=")), crtJVMParamPair
                        .substring(crtJVMParamPair.indexOf("=") + 1));
                }
                if (classname != null) arguments.add(crt);
            } else {
                if (classpathNext == true) {
                    classpath = crt;
                    classpathNext = false;
                } else if (classname == null)
                    classname = crt;
                else
                    arguments.add(crt);
            }
        }

        System.out.println("java -cp " + classpath + " params: " + JVMParams
            + " " + classname + " arguments: " + arguments);

        //System.out.println("launcher = " + launcher);

        System.out.println("node.getNodeInformation().getHostName() = "
            + node.getNodeInformation().getHostName());

        String jobID = null;
        boolean sameJVM = false;

        try {
            launcher.launch(classname, (String[]) arguments
                .toArray(new String[arguments.size()]), JVMParams, node,
                sameJVM);
        } catch (Throwable ex) {
            System.out.println("Launcher on node "
                + node.getNodeInformation().getURL()
                + " failed to launch, redeploying it ...");
            try {
                launcherTable.remove(node.getNodeInformation().getURL());
                launcher = (ProActiveLauncher) ProActive.newActive(
                    ProActiveLauncher.class.getName(), null, node);

                launcherTable.put(node.getNodeInformation().getURL(), launcher);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                launcher.launch(classname, (String[]) arguments
                    .toArray(new String[arguments.size()]), JVMParams, node,
                    sameJVM);
            } catch (Exception e) {
                System.out.println("Failed deploying launcher on node "
                    + node.getNodeInformation().getURL()
                    + ", removing node ...");
                gridNodesVector.remove(node);
                e.printStackTrace();
            }
            ex.printStackTrace();
        }

        try {
            Thread.sleep(100);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        // super.postStageFiles(description, node.getNodeInformation().getHostName());

        ProActiveJob paj = new ProActiveJob(launcher, description, jobID, node);
        /*
         try
         {
         pad.killall(false);
         }
         catch(Exception e)
         {
         e.printStackTrace();
         }

         */
        return paj;
    }
}
