package tutorial;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class SubmitRemoteJob3 implements MetricListener {
	/*
	 * GT4 test
	 * 
	 * In order to get the wsgt4 adaptor working:
	 * 1. copy all the jars of $GLOBUS/lib to lib
	 * 2. copy activation.jar and mail
	 */
	
    public static void main(String[] args) throws Exception {
        try {
            new SubmitRemoteJob3().start(args);
        } catch (Throwable t) {
            System.err.println("an error occurred: " + t);
            GAT.end();
            System.exit(1);
        }

        GAT.end();
        System.exit(0);
    }
    
    public void start(String[] args) throws Exception {
        GATContext context = new GATContext();
        if (args.length == 0) {
        	args = new String[]{"wsgt4"};
        }
        context.addPreference("ResourceBroker.adaptor.name", args[0]);
        context.addPreference("File.adaptor.name", "ssh, local");
        //context.addPreference("ResourceBroker.jobmanagerContact", "fs0.das3.cs.vu.nl");
        context.addPreference("ResourceBroker.jobmanager", args[1]);
        
        SoftwareDescription sd1 = new SoftwareDescription();
        sd1.setLocation("/bin/hostname");
        //sd1.setArguments(new String[]{"/home0/rkemp/script.sh"});
        File stdout1 = GAT.createFile(context, "stdout");
        sd1.setStdout(stdout1);
        JobDescription jd1 = new JobDescription(sd1);
        
        ResourceBroker broker = GAT.createResourceBroker(context);
        Job job1 = broker.submitJob(jd1, this, "job.status");
        

        while ((job1.getState() != Job.STOPPED)
            && (job1.getState() != Job.SUBMISSION_ERROR)) {
            Thread.sleep(1000);
        }
        GAT.end();
        System.out.println("jobs DONE!");       
    }

    public void processMetricEvent(MetricValue val) {
        System.out.println("val: " + val.getValue());
    }
}
