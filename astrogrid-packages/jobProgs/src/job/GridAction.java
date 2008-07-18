package job;

import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class GridAction {

	private GATContext context = new GATContext();

	private void copyFile() throws URISyntaxException, GATObjectCreationException, GATInvocationException {
	
		URI src = new URI("file:////home/ali/tmp/toCopied");
		URI dest = new URI("any://buran.aei.mpg.de.de/~/copied");
                //		URI dest = new URI("any://gramd1.d-grid.uni-hannover.de/~/copied");
                //		URI dest = new URI("any://srvgrid01.offis.uni-oldenburg.de/~/copied");

		File file = GAT.createFile(context, src);
		file.copy(dest);

	}
	
	private void deleteFile() throws GATObjectCreationException, URISyntaxException {
		
            //		URI src = new URI("any://gramd1.d-grid.uni-hannover.de//home/uhpa0008/copied");
		URI src = new URI("any://buran.aei.mpg.de.de/~/copied");
		
		File file = GAT.createFile(context, src);
		file.delete();
		
	}
	
	private void moveFile() throws GATObjectCreationException, URISyntaxException, GATInvocationException {
		

		URI src = new URI("file:////home/ali/tmp/toCopied");
		URI dest = new URI("any://buran.aei.mpg.de.de/~/copied");
                //		URI dest = new URI("any://gramd1.d-grid.uni-hannover.de//home/uhpa0008/copied");
                //		URI dest = new URI("any://srvgrid01.offis.uni-oldenburg.de/~/copied");

		File file = GAT.createFile(context, src);
		file.move(dest);
		
	}
	
	private void writeFile() throws GATObjectCreationException, URISyntaxException, GATInvocationException {

		
	}
	
	private void submitJob() throws GATObjectCreationException, URISyntaxException, GATInvocationException, InterruptedException {
		
		Preferences prefs = new Preferences();
		
//		prefs.put("File.adaptor.name", "gridftp");
//		prefs.put("ResourceBroker.adaptor.name", "globus");
//		prefs.put("ResourceBroker.jobmanagerContact", "udo-gt02.uni-hannover.de");
		context.addPreferences(prefs);
		
		File outFile = GAT.createFile(context, new URI("any:///out"));
		File errFile = GAT.createFile(context, new URI("any:///err"));
		
		SoftwareDescription swDescr = new SoftwareDescription();
		swDescr.setExecutable("/bin/sh");
		swDescr.setStdout(outFile);
		swDescr.setStderr(errFile);
		swDescr.addAttribute("globus.exitvalue.enable", "true");

		File[] preStagedFile = new File[2];
		preStagedFile[0] = GAT.createFile(context, "any:///date_script.sh");
		preStagedFile[1] = GAT.createFile(context, "any:///date.in");
		swDescr.setPreStaged(preStagedFile);
		
		File[] postStagedFile = new File[1];
		postStagedFile[0] = GAT.createFile(context, "any:///date.out");
		swDescr.setPostStaged(postStagedFile);
	
                String[] argsList = new String[1];
                argsList[0] = new String("date_script.sh");
                swDescr.setArguments(argsList);
		
		Hashtable hwAttr = new Hashtable();
		ResourceDescription hwDescr = new HardwareResourceDescription(hwAttr);
		
		JobDescription jobDescr = new JobDescription(swDescr, hwDescr);
		
		//String jobManagerContact = new String ("any://gramd1.d-grid.uni-hannover.de");
//		String jobManagerContact = new String ("any://localhost");
		String jobManagerContact = new String ("udo-gt02.uni-hannover.de");
		ResourceBroker broker = GAT.createResourceBroker(context, new URI(jobManagerContact));
		
		Job job = broker.submitJob(jobDescr);
		
		String state = null;
		
		while(true) {
			
			Map info = job.getInfo();
			System.err.print("job info: ");
			System.err.println(info);
				
			state = (String) info.get("state");
			
			Thread.sleep(1000);
			
			if (state.equals("STOPPED")) {
                System.out.println();
                System.out.println("JOB SUMMARY");
                System.out.println("================================================");
                System.out.println("Command:    "+ job.toString());
                System.out.println("Job ID:     "+ job.getJobID());
                System.out.println("Exit value: "+ job.getExitStatus());
                System.out.println("================================================");
                System.out.println();
                break;
			}
		}
		
	}

	/**
	 * @param args
	 * @throws URISyntaxException 
	 * @throws GATInvocationException 
	 * @throws GATObjectCreationException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws GATObjectCreationException, GATInvocationException, URISyntaxException, InterruptedException {
		
		
		
		//new GridAction().copyFile();
		//new GridAction().deleteFile();
		//new GridAction().moveFile();
		//new GridAction().printFile();
		new GridAction().submitJob();
		GAT.end();

	}

}