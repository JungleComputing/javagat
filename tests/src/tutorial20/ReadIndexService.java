package tutorial20;

import org.gridlab.gat.GAT;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.Job.JobState;

public class ReadIndexService {



	    public static void main(String[] args) throws Exception {
	        SoftwareDescription sd = new SoftwareDescription();
	        sd.setExecutable("/bin/hostname");
	        File stdout = GAT.createFile("hostname.txt");
	        sd.setStdout(stdout);
	        
	        Preferences preferences = new Preferences();
	        preferences.put("resourcebroker.adaptor.name", "wsgt4new"); // "gt42"wsgt4new
	        //preferences.put("file.adaptor.name", "gt4gridftp");
//	 provare se posso scegliere anche il file adaptor
	        JobDescription jd = new JobDescription(sd);
	        ResourceBroker broker = GAT.createResourceBroker(preferences, new URI(args[0]));
	        HardwareResourceDescription hd=new HardwareResourceDescription();
	        hd.addResourceAttribute("AVAILABLE_DISK_SIZE", 150000);
	        hd.addResourceAttribute("MEMORY_AVAILABLE", 1900);
	        broker.findResources(hd);

	        
	    }
	}

