package gt4;

import jargs.gnu.CmdLineParser;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class TestApplication {
    static void printUsage() {
	System.err.println("Usage: TestApplication node [arguments] \n" + 
			   "The node is a hostname of the " +
			   "jobsubmission machine. \n" +
			   "The possible arguments: \n" +
			   "  --prefkey key --prefvaule value\t" +
			   "passes preferences to the GAT\n" +
			   "  --input directory\t" +
			   "the directory with the input files\n" +
			   "  --output file\t" +
			   "the output file name\n");
    }
    public static void main(String[] args) throws Exception {
	CmdLineParser parser = new CmdLineParser();
	CmdLineParser.Option cmdPrefKey = parser.addStringOption("prefkey");
	CmdLineParser.Option cmdPrefValue = parser.addStringOption("perfvalue");
	CmdLineParser.Option cmdInputFiles = parser.addStringOption("input");
	CmdLineParser.Option cmdOutputFile = parser.addStringOption("output");

	try {
	    parser.parse(args);
	} catch(Exception e) {
	    System.err.println(e);
	    printUsage();
	    System.exit(0);
	}
	
	String[] rargs = parser.getRemainingArgs();

	if(rargs.length==0) {
	    printUsage();
	    System.exit(0);
	}
	URI submissionNode = new URI(rargs[0]);

	Preferences prefs = new Preferences();
	String prefKey = (String) parser.getOptionValue(cmdPrefKey);
	String prefValue = (String) parser.getOptionValue(cmdPrefValue);
	while( prefKey!=null && prefValue!=null ) {
	    prefs.put(prefKey, prefValue);
	    prefKey = (String) parser.getOptionValue(cmdPrefKey);
	    prefValue = (String) parser.getOptionValue(cmdPrefValue);
	}
	
	String inputFiles = (String) parser.getOptionValue(cmdInputFiles);
	if(inputFiles == null) {
	    inputFiles = new String("inputFiles");
	}

	String outputFile = (String) parser.getOptionValue(cmdOutputFile);
	if(outputFile == null) {
	    outputFile = new String("inputFiles.zip");
	}
	
	GATContext context = new GATContext();
	
	SoftwareDescription sd = new SoftwareDescription();
	sd.setExecutable("/usr/bin/zip");
	
	File stdout = GAT.createFile(context, "zip.out");
	sd.setStdout(stdout);
	
	File stderr = GAT.createFile(context, "zip.err");
	sd.setStderr(stderr);
	
	sd.setArguments(new String[] {"-r", "inputFiles.zip", "inputFiles"});
	
	sd.addPreStagedFile(GAT.createFile(context, inputFiles));
	sd.addPostStagedFile(GAT.createFile(context, outputFile));
	
	
	ResourceDescription rd = new HardwareResourceDescription();
	//rd.addResourceAttribute("machine.node", submissionNode); obsolete
	
	JobDescription jd = new JobDescription(sd, rd);
	ResourceBroker broker = GAT.createResourceBroker(context, prefs, submissionNode);
	Job job = broker.submitJob(jd);
	
	while ((job.getState() != Job.JobState.STOPPED)
	       && (job.getState() != Job.JobState.SUBMISSION_ERROR)) {
	    //System.err.println("job state = " + job.getInfo());
	    Thread.sleep(1000);
	}
	
	System.err.println("job DONE, state = " + job.getInfo());
	GAT.end();
    }
}
