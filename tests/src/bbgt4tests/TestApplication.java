package bbgt4tests;

import org.gridlab.gat.*;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.*;

public class TestApplication {
    public static void main(String[] args) throws Exception {
	Preferences prefs = new Preferences();
	prefs.put("ResourceBroker.adaptor.name","GT4ResourceBrokerAdaptor");
	prefs.put("File.adaptor.name", "GT4GridFTPFileAdaptor");
	GATContext context = new GATContext();
	
	//      Preferences prefs = new Preferences();
	//      prefs.put("ResourceBroker.adaptor.name", "globus");
	//      prefs.put("File.adaptor.name", "gridftp");
	//      context.addPreferences(prefs);
	
	SoftwareDescription sd = new SoftwareDescription();
	sd.setLocation("/usr/bin/zip");
	
	File stdout = GAT.createFile(context, "zip.out");
	sd.setStdout(stdout);
	
	File stderr = GAT.createFile(context, "zip.err");
	sd.setStderr(stderr);
	
	sd.setArguments(new String[] {"-r", "inputFiles.zip", "inputFiles"});
	
	sd.addPreStagedFile(GAT.createFile(context, "gsiftp://fs0.das3.cs.vu.nl//home0/bbokodi/inputFiles"));
	sd.addPostStagedFile(GAT.createFile(context, "gsiftp://fs0.das3.cs.vu.nl/inputFiles.zip"));
	
	
	ResourceDescription rd = new HardwareResourceDescription();
	rd.addResourceAttribute("machine.node", "fs2.das3.science.uva.nl");
	
	JobDescription jd = new JobDescription(sd, rd);
	ResourceBroker broker = GAT.createResourceBroker(context);
	Job job = broker.submitJob(jd);
	
	while ((job.getState() != Job.STOPPED)
	       && (job.getState() != Job.SUBMISSION_ERROR)) {
	    System.err.println("job state = " + job.getInfo());
	    Thread.sleep(1000);
	}
	
	System.err.println("job DONE, state = " + job.getInfo());
	GAT.end();
    }
}
