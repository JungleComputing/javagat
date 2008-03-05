/*
 * Created on May 19, 2004
 */
package resources;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

/**
 * @author rob
 */
public class SubmitJobSsh implements MetricListener {
    public static void main(String[] args) {
        try {
            new SubmitJobSsh().start(args);
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
        prefs.put("ResourceBroker.adaptor.name", "Ssh");
        

        SoftwareDescription descs[] = new SoftwareDescription[5];
        JobDescription jds[] = new JobDescription[descs.length];
        for (int i = 0; i < descs.length; i++) {
            descs[i] = new SoftwareDescription();
            descs[i].setExecutable(args[0]);
            descs[i].setArguments(new String[] { args[1] });
            descs[i].setStdout(GAT.createFile(context, new URI("any:///out.1." + i)));
            descs[i].setStderr(GAT.createFile(context, new URI("any:///err.1." + i)));
            jds[i] = new JobDescription(descs[i]);
        }
        ResourceBroker broker = GAT.createResourceBroker(context, prefs, new URI(args[2]));
        
        SoftwareDescription descs2[] = new SoftwareDescription[5];
        JobDescription jds2[] = new JobDescription[descs2.length];
        for (int i = 0; i < descs2.length; i++) {
            descs2[i] = new SoftwareDescription();
            descs2[i].setExecutable(args[0]);
            descs2[i].setArguments(new String[] { args[1] });
            descs2[i].setStdout(GAT.createFile(context, new URI("any:///out.2." + i)));
            descs2[i].setStderr(GAT.createFile(context, new URI("any:///err.2." + i)));
            jds2[i] = new JobDescription(descs2[i]);
        }
        ResourceBroker broker2 = GAT.createResourceBroker(context, prefs, new URI(args[3]));

        for (int i = 0; i < jds.length; i++) {
            broker.submitJob(jds[i]);
            broker2.submitJob(jds2[i]);
            Thread.sleep(1111);
        }
        Thread.sleep(40000);
    }
}
