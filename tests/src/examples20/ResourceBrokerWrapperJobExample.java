package examples20;

import java.net.URISyntaxException;

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
import org.gridlab.gat.resources.WrapperJob;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.WrapperSoftwareDescription;
import org.gridlab.gat.resources.WrapperJobDescription.StagingType;

public class ResourceBrokerWrapperJobExample implements MetricListener {

    /**
     * @param args
     * @throws URISyntaxException
     * @throws GATInvocationException
     * @throws GATObjectCreationException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws GATObjectCreationException,
            GATInvocationException, URISyntaxException, InterruptedException {
        new ResourceBrokerWrapperJobExample().start();
    }

    public void start() throws URISyntaxException, GATObjectCreationException,
            InterruptedException, GATInvocationException {
        WrapperSoftwareDescription wsd = new WrapperSoftwareDescription();
        wsd.setStdout(GAT.createFile("wrapper.stdout"));
        wsd.setStderr(GAT.createFile("wrapper.stderr"));
        wsd.setExecutable("/usr/local/package/jdk1.6.0/bin/java");
        // wsd.setExecutable("/usr/bin/java");
        WrapperJobDescription wjd = new WrapperJobDescription(wsd);

        JobDescription[] wrappedJobs = new JobDescription[30];
        for (int i = 0; i < wrappedJobs.length; i++) {
            SoftwareDescription sd = new SoftwareDescription();
            // sd.setExecutable("/bin/pwd");
            sd.setExecutable("/bin/sleep");
            sd.setArguments("" + (int) (30 * Math.random()));
            sd.addPreStagedFile(GAT.createFile("largefile"));
            sd.setStdout(GAT.createFile("stdout." + i));
            Preferences preferences = new Preferences();
            preferences.put("resourcebroker.adaptor.name", "local");
            wrappedJobs[i] = new JobDescription(sd);
            wjd.setMaxConcurrentJobs(4);
            wjd.setPreStagingType(StagingType.SEQUENTIAL);
            wjd.add(wrappedJobs[i], new URI("any://localhost"), preferences);
        }
        Preferences wrapperPreferences = new Preferences();
        wrapperPreferences.put("resourcebroker.adaptor.name", "globus");
        // wrapperPreferences.put("resourcebroker.adaptor.name", "local");
        ResourceBroker broker = GAT.createResourceBroker(wrapperPreferences,
                new URI("any://fs0.das3.cs.vu.nl/jobmanager-sge"));
        // new URI("any://localhost"));
        WrapperJob wrapperJob = (WrapperJob) broker.submitJob(wjd, this,
                "job.status");

        Job[] jobs = new Job[wrappedJobs.length];
        for (int i = 0; i < jobs.length; i++) {
            jobs[i] = wrapperJob.getJob(wrappedJobs[i]);
            if (jobs[i] == null) {
                System.out.println("i: " + i + " wrappedJobs[i]: "
                        + wrappedJobs[i]);
            }
        }
        while (wrapperJob.getState() != Job.JobState.STOPPED) {
            System.out.println("wrapp: " + wrapperJob.getState());
            for (Job job : jobs) {
                System.out.println("job: " + job.getState());
            }
            System.out.println();
            Thread.sleep(1000);
        }

        GAT.end();

    }

    public void start2() throws URISyntaxException, GATObjectCreationException,
            InterruptedException, GATInvocationException {
        WrapperSoftwareDescription wsd = new WrapperSoftwareDescription();
        wsd.setStdout(GAT.createFile("wrapper.stdout"));
        wsd.setStderr(GAT.createFile("wrapper.stderr"));
        wsd.setExecutable("/usr/local/package/jdk1.6.0/bin/java");
        wsd.setGATLocation(new URI("any://fs0.das3.cs.vu.nl/.JavaGATtest"));
        // wsd.setExecutable("/usr/bin/java");
        WrapperJobDescription wjd = new WrapperJobDescription(wsd);

        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/ls");
        sd.setStdout(GAT.createFile("wrapped.stdout"));
        sd.setStderr(GAT.createFile("wrapped.stderr"));
        sd.addAttribute("process.count", "10");
        sd.addAttribute("host.count", "2");
        sd.addPreStagedFile(GAT.createFile("bla"));
        Preferences preferences = new Preferences();
        preferences.put("resourcebroker.adaptor.name", "globus");
        JobDescription wrappedJob = new JobDescription(sd);
        wjd.add(wrappedJob, new URI("any://fs0.das3.cs.vu.nl/jobmanager-sge"),
                preferences);
        Preferences wrapperPreferences = new Preferences();

        wrapperPreferences.put("resourcebroker.adaptor.name", "sshtrilead");
        ResourceBroker broker = GAT.createResourceBroker(wrapperPreferences,
                new URI("any://fs0.das3.cs.vu.nl"));
        WrapperJob wrapperJob = (WrapperJob) broker.submitJob(wjd, this,
                "job.status");

        while (wrapperJob.getState() != Job.JobState.STOPPED) {
            System.out.println("wrapp: " + wrapperJob.getState());
            System.out.println("job: "
                    + wrapperJob.getJob(wrappedJob).getState());
            System.out.println();
            Thread.sleep(1000);
        }
        GAT.end();

    }

    public void processMetricEvent(MetricEvent val) {
        System.out.println(val);

    }
}
