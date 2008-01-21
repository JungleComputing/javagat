/*
 * Created on May 19, 2004
 */
package resources;

import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author rob
 */
public class SubmitJavaJob implements MetricListener {
    public static void main(String[] args) {
        try {
            new SubmitJavaJob().start(args);
        } catch (Exception e) {
            System.err.println("error: " + e);
            e.printStackTrace();
        } finally {
            GAT.end();
        }
    }

    public synchronized void processMetricEvent(MetricValue val) {
        notifyAll();
    }

    public void start(String[] args) throws Exception {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("ResourceBroker.adaptor.name", "Globus");
        SoftwareDescription sd = new SoftwareDescription();

        File outFile = GAT.createFile(context, prefs, new URI("any:///out"));
        File errFile = GAT.createFile(context, prefs, new URI("any:///err"));
        sd.setStdout(outFile);
        sd.setStderr(errFile);
        sd.setExecutable("java:org.gridlab.gat.resources.cpi.wrapper.Wrapper");

        sd.addAttribute("wrapper.java.home", new URI("/home/rob/contrib/jdk1.5.0_09"));
        sd.addAttribute("wrapper.java.flags", "-server");
        sd
                .addAttribute(
                        "wrapper.java.classpath",
                        "lib/GAT.jar:lib/castor-0.9.6.jar:lib/commons-logging.jar:lib/log4j-1.2.13.jar:lib/xmlParserAPIs.jar"
                                + "lib/castor-0.9.6-xml.jar:lib/ibis-util-1.4.jar:lib/xercesImpl.jar:lib/RemoteSandbox.jar");
        Map<String, Object> environment = new HashMap<String, Object>();
        environment.put("gat.adaptor.path", "lib");
        sd.setEnvironment(environment);

        sd.addPreStagedFile(GAT.createFile(context, prefs,
                new URI("engine/lib")));
        sd.addPreStagedFile(GAT.createFile(context, prefs, new URI(
                "adaptors/lib")));

        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(context, prefs,
                new URI(args[0]));

        Job job = broker.submitJob(jd);
        MetricDefinition md = job.getMetricDefinitionByName("job.status");
        Metric m = md.createMetric(null);
        job.addMetricListener(this, m);

        synchronized (this) {
            while ((job.getState() != Job.STOPPED)
                    && (job.getState() != Job.SUBMISSION_ERROR)) {
                wait();
            }
        }

        System.err.println("SubmitJobCallback: Job finished, state = "
                + job.getInfo());
    }
}
