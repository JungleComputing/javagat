package org.objectweb.proactive.GATAdaptor;

import java.io.Serializable;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger;

import java.util.HashMap;

public class ProActiveLauncher implements Serializable, InitActive, RunActive,
        LauncherJobListener {
    HashMap statuses = new HashMap();

    String nodeID = "";

    int jobCounter = 0;

    //	Node node;

    public ProActiveLauncher() {
    }

    /*
     public ProActiveLauncher(Node node)
     {
     this.node = node;
     }
     */
    public void initActivity(Body body) {
        nodeID = body.getID().toString();
    }

    public void runActivity(Body body) {
        Service service = new Service(body);

        while (body.isActive()) {
            service.waitForRequest();
            service.serveAll("launch");
            service.serveAll("setStatus");
            service.serveAll("getStatus");
            service.serveAll("getNode");
        }
    }

    public void setupParams(HashMap JVMParams) {
        Object[] entries = JVMParams.entrySet().toArray();

        for (int i = 0; i < entries.length; i++)
            System.setProperty((String) ((java.util.Map.Entry) entries[i])
                .getKey(), (String) ((java.util.Map.Entry) entries[i])
                .getValue());
    }

    public String paramString(HashMap JVMParams) {
        String rv = "";
        Object[] entries = JVMParams.entrySet().toArray();

        for (int i = 0; i < entries.length; i++)
            rv += "-D" + (String) ((java.util.Map.Entry) entries[i]).getKey()
                + "=" + (String) ((java.util.Map.Entry) entries[i]).getValue()
                + " ";

        return rv;
    }

    /*
     public Node getNode()
     {
     return node;
     }
     */

    public String launch(String classname, String[] args, HashMap JVMParams,
            Node node, boolean sameJVM)
    //public String launch(String classname, java.util.Vector argms, HashMap JVMParams, boolean sameJVM)
    {
        String jobID = nodeID + "_" + jobCounter;
        String classpath = System.getProperty("java.class.path");
        jobCounter++;
        //String[] args = (String[]) argms.toArray(new String[argms.size()]);

        //if(sameJVM.booleanValue())
        if (sameJVM) {
            setupParams(JVMParams);
            new Thread(new ClassLauncher(classname, args, this, jobID)).start();
        } else {
            JVMProcessImpl jvm = new JVMProcessImpl(
                new StandardOutputMessageLogger());
            jvm.setClassname(classname);
            String paramString = "";
            for (int i = 0; i < args.length; i++) {
                paramString += args[i];
                if (i < args.length - 1) paramString += " ";
            }
            jvm.setParameters(paramString);
            System.out.println("JVM classpath = " + classpath);
            if (classpath != null) jvm.setClasspath(classpath);
            System.out.println("Arguments: " + paramString);
            jvm.setJvmOptions(paramString(JVMParams));
            try {
                node.getProActiveRuntime().createVM(jvm);
                //jvm.startProcess();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jobID;
    }

    public void setStatus(String id, Integer status) {
        statuses.remove(id);
        statuses.put(id, status);
    }

    public int getStatus(String id) {
        return ((Integer) statuses.get(id)).intValue();
    }
}
