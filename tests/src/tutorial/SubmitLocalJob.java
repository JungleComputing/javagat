package tutorial;

import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitLocalJob {
    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();
        context.addPreference("ResourceBroker.adaptor.name", "local");
        
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/pwd");
        //sd.setArguments(new String[]{"../script.sh"});
        
        Map<String, Object> attributes = new HashMap<String, Object>();
        //attributes.put("disableSandbox", "true");
        //attributes.put("sandboxRoot", "/home/rkemp/test123");
        
        sd.setAttributes(attributes);

        File stdout = GAT.createFile(context, "hostname.txt");
        sd.setStdout(stdout);

        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(context, new URI(
                "any:///"));
        Job job = broker.submitJob(jd);

        while ((job.getState() != Job.STOPPED)
                && (job.getState() != Job.SUBMISSION_ERROR)) {
            Thread.sleep(1000);
        }

        GAT.end();
    }
}
