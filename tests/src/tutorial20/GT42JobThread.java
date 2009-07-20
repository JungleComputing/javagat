package tutorial20;

import java.net.URISyntaxException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.Job.JobState;

public class GT42JobThread extends Thread{

	URI Uri= null;
	
	public GT42JobThread(String uri){
		try {
			Uri= new URI(uri);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
	
	System.out.println("\n GT42JobThread STARTED \n");
	SoftwareDescription sd = new SoftwareDescription();
    sd.setExecutable("/bin/hostname");
    File stdout;
	try {
		stdout = GAT.createFile("hostname1.txt");
	
    sd.setStdout(stdout);
	
    Preferences preferences = new Preferences();
    preferences.put("resourcebroker.adaptor.name", "gt42"); // ""
    //preferences.put("file.adaptor.name", "gt4gridftp");
    JobDescription jd = new JobDescription(sd);
    ResourceBroker broker;
	broker = GAT.createResourceBroker(preferences, Uri);
	
    Job job = broker.submitJob(jd);
	
    while ((job.getState() != JobState.STOPPED)
            && (job.getState() != JobState.SUBMISSION_ERROR)) {
        Thread.sleep(1000);
    }
    System.out.println("\n GT42JobThread TERMINATED \n");
    
	} catch (GATObjectCreationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (GATInvocationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}
}
