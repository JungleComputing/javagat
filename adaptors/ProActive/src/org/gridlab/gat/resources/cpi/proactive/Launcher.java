package org.gridlab.gat.resources.cpi.proactive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.MessageSink;
import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

/**
 * This class takes care of launching JVMs to execute the application.
 * Each node runs an instance of this class.
 * It must be public, otherwise the stub generated by ProActive cannot
 * access it.
 */
@SuppressWarnings("serial")
public class Launcher implements Serializable, RunActive {
    static final String newline = System.getProperty("line.separator");

    /** Maps job id's to jvms. */
    private HashMap<String, Watcher> jobs = new HashMap<String, Watcher>();

    /** For debugging and error messages. */
    private final Logger logger = Logger.getLogger(Launcher.class);

    /** For making callbacks to maintain job administration. */
    final JobWatcher jobWatcher;

    /** Node on which this launcher is running. */
    final Node node;

    /** Collects output and passes it on to the jobWatcher. */
    class OutputLogger extends Thread implements RemoteProcessMessageLogger {
        ArrayList<String> messages = new ArrayList<String>();
        String jobID;
        boolean toStderr;
        boolean terminated = false;

        OutputLogger(String jobID, boolean toStderr) {
            this.jobID = jobID;
            this.toStderr = toStderr;
            this.setDaemon(true);
            start();
        }

        public synchronized void log(String message) {
            messages.add(message);
        }

        public synchronized void log(String message, Throwable e) {
            messages.add(message + e);
        }

        public synchronized void log(Throwable e) {
            messages.add("" + e);
        }

        protected synchronized void sendMessages() {
            if (messages.size() != 0) {
                StringBuffer sb = new StringBuffer();
                while (messages.size() != 0) {
                    sb.append((String) messages.remove(0)).append(newline);
                }
                if (toStderr) {
                    jobWatcher.addError(jobID, sb.toString());
                } else {
                    jobWatcher.addOutput(jobID, sb.toString());
                }
            }
        }

        public synchronized void terminate() {
            // Wait a while, to give ProActive the time to collect the
            // latest output.
            try {
                wait(3000);
            } catch(Exception e) {
                // ignored
            }
            terminated = true;
            notifyAll();
        }

        public void run() {
            for (;;) {
                synchronized(this) {
                    try {
                        wait(10000);
                    } catch(Exception e) {
                        // ignored
                    }
                    sendMessages();
                    if (terminated) {
                        return;
                    }
                }
            }
        }
    }

    /** Obtains input from the jobWatcher. */
    class InputSink implements MessageSink {
        private ArrayList<String> messages = new ArrayList<String>();
        private boolean done = false;

        public synchronized String getMessage() {
            String s = null;
            while (messages.size() == 0 && ! done) {
                try {
                    wait();
                } catch(Exception e) {
                    // ignored
                }
            }
            if (messages.size() > 0) {
                s = (String) messages.remove(0);
            }
            logger.info("Delivering message " + s);
            return s;
        }

        public synchronized boolean hasMessage() {
            return messages.size() != 0;
        }

        public synchronized boolean isActive() {
            return ! done;
        }

        public synchronized void setMessage(String m) {
            if (m == null) {
                done = true;
            } else {
                messages.add(m);
            }
            notifyAll();
        }
    }

    /**
     * Every time an application JVM is started, an accompanying thread
     * is started to keep track of its status.
     */
    class Watcher extends Thread {
        /** The JVM. */
        JVMProcessImpl jvm;

        /** The identification of the job. */
        String jobID;

        OutputLogger stdout, stderr;

        /**
         * Constructor, with specified initial values for the fields.
         * @param jvm the JVM.
         * @param jobID the job identification.
         */
        public Watcher(JVMProcessImpl jvm, String jobID, OutputLogger stdout,
                OutputLogger stderr) {
            this.jvm = jvm;
            this.jobID = jobID;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        /**
         * Waits until the JVM is finished, and notifies the watcher.
         */
        public void run() {
            while (! jvm.isFinished()) {
                try {
                    jvm.waitFor();
                } catch(Exception e) {
                    // ignored
                }
            }
            int eval = 0;
            try {
                eval = jvm.exitValue();
            } catch(Exception e) {
                // Is sometimes thrown, even after waitfor.
                // We ignore it, and lose the exit status.
            }
            stdout.terminate();
            stderr.terminate();
            synchronized(jobs) {
                jobs.remove(jobID);
            }
            for (int i = 0; i < 4; i++) {
                try {
                    jobWatcher.finishedJob(jobID, eval);
                    return;
                } catch(Throwable e) {
                    // ignored
                }
                try {
                    sleep(5000);
                } catch(Exception e2) {
                    // ignored
                }
            }
        }
    }

    /**
     * Public noargs constructor, required by ProActive.
     */
    public Launcher() {
        jobWatcher = null;
        node = null;
    }

    /**
     * Constructor with specified watcher on the specified node.
     * @param jobWatcher the watcher to notify.
     */
    public Launcher(JobWatcher jobWatcher, Node node) {
        this.jobWatcher = jobWatcher;
        this.node = node;
    }

    /**
     * ProActive activity handler.
     * @param body the active body.
     */
    public void runActivity(Body body) {
        Service service = new Service(body);

        while (body.isAlive() && body.isActive()) {
            service.blockingServeOldest();
            logger.info("Served request");
        }
        logger.info("runActivity terminates");
    }

    /**
     * Launches a new JVM for the application specified in the parameters.
     * It returns a StringWrapper, to allow for asynchronous launch.
     * For that to work, the return class type may not be final.
     * @param classname the name of the class to run.
     * @param jvmArgs JVM parameters.
     * @param progArgs application arguments.
     * @param classpath the classpath.
     * @param jobID identification for this job.
     * @return a string wrapper containing the job id or <code>null</code>
     * in case of failure.
     */
    public StringWrapper launch(String classname, String jvmArgs,
            String progArgs, String classpath, String jobID) {

        logger.info("Serving launch");
        OutputLogger stdout = new OutputLogger(jobID, false);
        OutputLogger stderr = new OutputLogger(jobID, true);
        // Create the JVM process and set its parameters.
        JVMProcessImpl jvm = new JVMProcessImpl(stdout, stderr);
        jvm.setOutputMessageSink(new InputSink());

        jvm.setClassname(classname);

        jvm.setLog4jFile(null);
        jvm.setPolicyFile(null);

        String save1 = null;
        String save2 = null;
        if (classpath != null) {
            // Reset some properties that make JVMProcessImpl add options
            // to the resulting command line to set the ProActive classloader
            // for the application. We don't want that.
            save1 = System.getProperty("proactive.classloader");
            save2 = System.getProperty("java.system.class.loader");
            if ("enable".equals(save1)) {
                System.setProperty("proactive.classloader", "disable");
            } else {
                save1 = null;
            }
            if ("org.objectweb.proactive.core.classloader.ProActiveClassLoader".equals(save2)) {
                System.setProperty("java.system.class.loader", "");
            } else {
                save2 = null;
            }
            jvm.setClasspath(classpath);
        }

        jvm.setParameters(progArgs);

        jvm.setJvmOptions(jvmArgs);

        // Try and run it, spawn watcher thread if this succeeds.
        try {
            Watcher w = new Watcher(jvm, jobID, stdout, stderr);
            w.setDaemon(true);
            synchronized(jobs) {
                jobs.put(jobID, w);
            }
            node.getProActiveRuntime().createVM(jvm);
            //jvm.startProcess();
            w.start();
            if (save1 != null) {
                System.setProperty("proactive.classloader", save1);
            }
            if (save2 != null) {
                System.setProperty("java.system.class.loader", save2);
            }

            jobWatcher.startedJob(jobID);
        } catch (Exception e) {
            logger.warn("Got exception during createVM:",  e);
            return new StringWrapper(null);
        }
        return new StringWrapper(jobID);
    }

    /**
     * Stops the specified job if present.
     * @param id the job identification.
     */
    public void stopJob(String id) {
        Watcher w;
        logger.info("Serving stopJob");
        synchronized(jobs) {
            w = (Watcher) jobs.get(id);
        }
        if (w != null) {
            w.jvm.stopProcess();
        }
    }

    /**
     * Provides a job with input.
     * @param id the job identification.
     * @param input the input.
     */
    public void provideInput(String id, String input) {
        Watcher w;
        logger.info("Serving provideInput to " + id + ": " + input);
        synchronized(jobs) {
            w = (Watcher) jobs.get(id);
        }
        if (w != null) {
            w.jvm.getOutputMessageSink().setMessage(input);
        }
    }

    /**
     * Test method. Returns an int, so is synchronous.
     * @return 0.
     */
    public int ping() {
        return 0;
    }

    /**
     * Termination. Synchronous method.
     */
    public int terminate() {
        synchronized(jobs) {
            for (Iterator<String> i = jobs.keySet().iterator(); i.hasNext();) {
                Watcher w = (Watcher) jobs.get(i.next());
                if (w != null) {
                    w.jvm.stopProcess();
                }
            }
        }
        try {
            ProActive.getBodyOnThis().terminate();
        } catch(Exception e) {
            // ignored
        }
        return 0;
    }
}