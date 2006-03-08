package org.objectweb.proactive.GATAdaptor;

import java.lang.reflect.Method;
import java.io.Serializable;
import org.objectweb.proactive.GATAdaptor.LauncherJobListener;

public class ClassLauncher implements Runnable, Serializable {

    String classname;

    String[] args;

    LauncherJobListener listener;

    String id;

    public ClassLauncher(String classname, String[] args,
            LauncherJobListener listener, String id) {
        this.classname = classname;
        this.args = new String[args.length];
        for (int i = 0; i < args.length; i++)
            this.args[i] = args[i];
        this.listener = listener;
        this.id = id;
    }

    public void run() {
        try {
            listener.setStatus(id, new Integer(
                org.gridlab.gat.resources.Job.INITIAL));
            Class clazz = Class.forName(classname);
            Object o = clazz.newInstance();
            Class[] arguments = new Class[1];
            arguments[0] = args.getClass();
            Method main = clazz.getMethod("main", arguments);
            Object[] args1 = new Object[1];
            args1[0] = args;
            listener.setStatus(id, new Integer(
                org.gridlab.gat.resources.Job.RUNNING));
            main.invoke(o, args1);

            listener.setStatus(id, new Integer(
                org.gridlab.gat.resources.Job.STOPPED));
        } catch (Exception e) {
            e.printStackTrace();
            listener.setStatus(id, new Integer(
                org.gridlab.gat.resources.Job.SUBMISSION_ERROR));
        }
    }
}
