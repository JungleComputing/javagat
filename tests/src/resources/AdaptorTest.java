package resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

import file.AdaptorTestResult;
import file.AdaptorTestResultEntry;

public class AdaptorTest implements MetricListener {

    /**
     * @param args
     */
    public static void main(String[] args) {
        AdaptorTest a = new AdaptorTest();
        a.test(args[0], args[1]).print();
        GAT.end();
    }

    public AdaptorTestResult test(String adaptor, String host) {
        AdaptorTestResult adaptorTestResult = new AdaptorTestResult(adaptor,
                host);
        Preferences preferences = new Preferences();
        preferences.put("resourcebroker.adaptor.name", adaptor);
        preferences.put("file.adaptor.name", "sshtrilead,local");
        adaptorTestResult.put("submit job easy  ", submitJobEasy(preferences,
                host));
        adaptorTestResult.put("submit job parallel", submitJobParallel(
                preferences, host));
        adaptorTestResult.put("submit job stdout", submitJobStdout(preferences,
                host));
        adaptorTestResult.put("submit job stderr", submitJobStderr(preferences,
                host));
        adaptorTestResult.put("submit job prestage", submitJobPreStage(
                preferences, host));
        adaptorTestResult.put("submit job poststage", submitJobPostStage(
                preferences, host));
        adaptorTestResult.put("submit job environment", submitJobEnvironment(
                preferences, host));
        adaptorTestResult.put("job state consistency",
                submitJobStateConsistency(preferences, host));
        adaptorTestResult.put("job get info         ", submitJobGetInfo(
                preferences, host));
        return adaptorTestResult;
    }

    private AdaptorTestResultEntry submitJobEasy(Preferences preferences,
            String host) {
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/echo");
        sd.setArguments("test", "1", "2", "3");
        Map<String, Object> attributes = new HashMap<String, Object>();

        sd.setAttributes(attributes);
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker;
        try {
            broker = GAT.createResourceBroker(preferences, new URI("any://"
                    + host));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        } catch (URISyntaxException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        long start = System.currentTimeMillis();
        try {
            broker.submitJob(jd, this, "job.status");
        } catch (GATInvocationException e) {
            e.printStackTrace();
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
        long stop = System.currentTimeMillis();
        return new AdaptorTestResultEntry(true, (stop - start), null);

    }

    private AdaptorTestResultEntry submitJobParallel(Preferences preferences,
            String host) {
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/echo");
        sd.setArguments("test", "1", "2", "3");
        sd.addAttribute("host.count", 2);
        sd.addAttribute("process.count", 2);
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker;
        try {
            broker = GAT.createResourceBroker(preferences, new URI("any://"
                    + host));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        } catch (URISyntaxException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        long start = System.currentTimeMillis();
        try {
            broker.submitJob(jd, this, "job.status");
        } catch (GATInvocationException e) {
            e.printStackTrace();
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
        long stop = System.currentTimeMillis();
        return new AdaptorTestResultEntry(true, (stop - start), null);

    }

    private AdaptorTestResultEntry submitJobStdout(Preferences preferences,
            String host) {
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/echo");
        sd.setArguments("test", "1", "2", "3");
        try {
            sd.setStdout(GAT.createFile("stdout"));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker;
        try {
            broker = GAT.createResourceBroker(preferences, new URI("any://"
                    + host));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        } catch (URISyntaxException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        long start = System.currentTimeMillis();
        try {
            broker.submitJob(jd, this, "job.status");
        } catch (GATInvocationException e) {
            e.printStackTrace();
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
        long stop = System.currentTimeMillis();
        String result;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new java.io.FileInputStream("stdout")));
            result = reader.readLine();
            reader.close();
        } catch (Exception e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        return new AdaptorTestResultEntry(result.equals("test 1 2 3"),
                (stop - start), null);

    }

    private AdaptorTestResultEntry submitJobStderr(Preferences preferences,
            String host) {
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/ls");
        sd.setArguments("floep");
        try {
            sd.setStderr(GAT.createFile("stderr"));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker;
        try {
            broker = GAT.createResourceBroker(preferences, new URI("any://"
                    + host));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        } catch (URISyntaxException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        long start = System.currentTimeMillis();
        try {
            broker.submitJob(jd, this, "job.status");
        } catch (GATInvocationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
        long stop = System.currentTimeMillis();
        String result;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new java.io.FileInputStream("stderr")));
            result = reader.readLine();
            reader.close();
        } catch (Exception e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        return new AdaptorTestResultEntry(result != null
                && result.startsWith("/bin/ls:")
                && result.endsWith(": No such file or directory"),
                (stop - start), null);

    }

    private AdaptorTestResultEntry submitJobPreStage(Preferences preferences,
            String host) {
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/ls");
        sd.setArguments("floep");
        java.io.File floep = new java.io.File("floep");
        if (!floep.exists()) {
            try {
                floep.createNewFile();
            } catch (IOException e) {
                return new AdaptorTestResultEntry(false, 0L, e);
            }
            floep.deleteOnExit();
        }
        try {
            sd.addPreStagedFile(GAT.createFile("floep"));
            sd.setStdout(GAT.createFile("stdout"));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker;
        try {
            broker = GAT.createResourceBroker(preferences, new URI("any://"
                    + host));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        } catch (URISyntaxException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        long start = System.currentTimeMillis();
        try {
            broker.submitJob(jd, this, "job.status");
        } catch (GATInvocationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
        long stop = System.currentTimeMillis();
        String result;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new java.io.FileInputStream("stdout")));
            result = reader.readLine();
            reader.close();
        } catch (Exception e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        return new AdaptorTestResultEntry(result != null, (stop - start), null);

    }

    private AdaptorTestResultEntry submitJobPostStage(Preferences preferences,
            String host) {
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/touch");
        sd.setArguments("flap.txt");
        try {
            sd.addPostStagedFile(GAT.createFile("flap.txt"));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker;
        try {
            broker = GAT.createResourceBroker(preferences, new URI("any://"
                    + host));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        } catch (URISyntaxException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        long start = System.currentTimeMillis();
        try {
            broker.submitJob(jd, this, "job.status");
        } catch (GATInvocationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
        long stop = System.currentTimeMillis();
        return new AdaptorTestResultEntry(
                new java.io.File("flap.txt").exists(), (stop - start), null);

    }

    private AdaptorTestResultEntry submitJobEnvironment(
            Preferences preferences, String host) {
        SoftwareDescription sd = new SoftwareDescription();
        Map<String, Object> env = new HashMap<String, Object>();
        env.put("JAVAGAT_TEST_KEY", "javagat-test-value");
        sd.setEnvironment(env);
        sd.setExecutable("/usr/bin/env");
        try {
            sd.setStdout(GAT.createFile("stdout"));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker;
        try {
            broker = GAT.createResourceBroker(preferences, new URI("any://"
                    + host));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        } catch (URISyntaxException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        long start = System.currentTimeMillis();
        try {
            broker.submitJob(jd, this, "job.status");
        } catch (GATInvocationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
        long stop = System.currentTimeMillis();
        boolean success = false;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new java.io.FileInputStream("stdout")));
            while (true) {
                String result = reader.readLine();
                if (result == null) {
                    break;
                }
                if (result.contains("JAVAGAT_TEST_KEY")
                        && result.contains("javagat-test-value")) {
                    success = true;
                }
            }
            reader.close();
        } catch (Exception e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        return new AdaptorTestResultEntry(success, (stop - start), null);

    }

    private AdaptorTestResultEntry submitJobStateConsistency(
            Preferences preferences, String host) {
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/sleep");
        sd.setArguments("2");
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker;
        try {
            broker = GAT.createResourceBroker(preferences, new URI("any://"
                    + host));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        } catch (URISyntaxException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        JobStateMetricListener listener = new JobStateMetricListener();
        long start = System.currentTimeMillis();
        try {
            broker.submitJob(jd, listener, "job.status");
        } catch (GATInvocationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        synchronized (listener) {
            try {
                listener.wait();
            } catch (InterruptedException e) {

            }
        }
        long stop = System.currentTimeMillis();
        return new AdaptorTestResultEntry(listener.getException() == null,
                (stop - start), listener.getException());

    }

    class JobStateMetricListener implements MetricListener {

        private String state = Job.INITIAL_STRING;

        private Exception e;

        public void processMetricEvent(MetricEvent val) {
            String newState = (String) val.getValue();
            if (newState.equals(Job.INITIAL_STRING)
                    && !(state.equals(Job.INITIAL_STRING))) {
                e = new Exception(newState + " occurs after " + state
                        + " but shouldn't");
            }
            if (newState.equals(Job.PRE_STAGING_STRING)
                    && (state.equals(Job.SCHEDULED_STRING)
                            || state.equals(Job.POST_STAGING_STRING)
                            || state.equals(Job.RUNNING_STRING)
                            || state.equals(Job.STOPPED_STRING) || state
                            .equals(Job.SUBMISSION_ERROR_STRING))) {
                e = new Exception(newState + " occurs after " + state
                        + " but shouldn't");
            }
            if (newState.equals(Job.SCHEDULED_STRING)
                    && (state.equals(Job.POST_STAGING_STRING)
                            || state.equals(Job.RUNNING_STRING)
                            || state.equals(Job.STOPPED_STRING) || state
                            .equals(Job.SUBMISSION_ERROR_STRING))) {
                e = new Exception(newState + " occurs after " + state
                        + " but shouldn't");
            }
            if (newState.equals(Job.RUNNING_STRING)
                    && (state.equals(Job.POST_STAGING_STRING)
                            || state.equals(Job.STOPPED_STRING) || state
                            .equals(Job.SUBMISSION_ERROR_STRING))) {
                e = new Exception(newState + " occurs after " + state
                        + " but shouldn't");
            }
            if (newState.equals(Job.POST_STAGING_STRING)
                    && (state.equals(Job.STOPPED_STRING) || state
                            .equals(Job.SUBMISSION_ERROR_STRING))) {
                e = new Exception(newState + " occurs after " + state
                        + " but shouldn't");
            }
            state = newState;
            if (state.equals(Job.STOPPED_STRING)) {
                synchronized (this) {
                    notifyAll();
                }
            }
        }

        public Exception getException() {
            return e;
        }
    }

    private AdaptorTestResultEntry submitJobGetInfo(Preferences preferences,
            String host) {
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/sleep");
        sd.setArguments("2");
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker;
        try {
            broker = GAT.createResourceBroker(preferences, new URI("any://"
                    + host));
        } catch (GATObjectCreationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        } catch (URISyntaxException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }
        Job job = null;
        Exception exception = null;
        long start = System.currentTimeMillis();
        try {
            job = broker.submitJob(jd);
        } catch (GATInvocationException e) {
            return new AdaptorTestResultEntry(false, 0L, e);
        }

        while (job.getState() != Job.STOPPED) {
            Map<String, Object> info = null;
            try {
                info = job.getInfo();
            } catch (GATInvocationException e) {
                exception = e;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                continue;
            }
            if (info == null) {
                exception = new Exception("getInfo returns null");
            } else {
                if (!info.containsKey("state")) {
                    exception = new Exception(
                            "getInfo doesn't contain a key 'state'");
                }
                if (!info.containsKey("hostname")) {
                    exception = new Exception(
                            "getInfo doesn't contain a key 'hostname'");
                } else {
                    if (info.get("state").equals(Job.RUNNING_STRING)
                            && info.get("hostname") == null) {
                        exception = new Exception(
                                "inconsistent getInfo: state=RUNNING, hostname=null");
                    }
                }
                if (!info.containsKey("submissiontime")) {
                    exception = new Exception(
                            "getInfo doesn't contain a key 'submissiontime'");
                }
                if (!info.containsKey("starttime")) {
                    exception = new Exception(
                            "getInfo doesn't contain a key 'starttime'");
                }
                if (!info.containsKey("stoptime")) {
                    exception = new Exception(
                            "getInfo doesn't contain a key 'stoptime'");
                }
                if (!info.containsKey("poststage.exception")) {
                    exception = new Exception(
                            "getInfo doesn't contain a key 'poststage.exception'");
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                exception = e;
            }
        }

        long stop = System.currentTimeMillis();
        return new AdaptorTestResultEntry(exception == null, (stop - start),
                exception);

    }

    public void processMetricEvent(MetricEvent val) {
        if (val.getValue().equals(Job.STOPPED_STRING)) {
            synchronized (this) {
                notifyAll();
            }
        }
    }
}
