package tests.gt4;

import org.gridlab.gat.*;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.*;

import jargs.gnu.CmdLineParser;

public class TestApplication {
    static void printUsage() {
	System.err.println("Usage: TestApplication ");
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
	URI location = null;

	if(rargs.length==0) {
	    printUsage();
	    System.exit(0);
	}
	String submissionNode = rargs[0];

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
	sd.setLocation("/usr/bin/zip");
	
	File stdout = GAT.createFile(context, "zip.out");
	sd.setStdout(stdout);
	
	File stderr = GAT.createFile(context, "zip.err");
	sd.setStderr(stderr);
	
	sd.setArguments(new String[] {"-r", "inputFiles.zip", "inputFiles"});
	
	sd.addPreStagedFile(GAT.createFile(context, "gsiftp://fs0.das3.cs.vu.nl//home0/bbokodi/inputFiles"));
	sd.addPostStagedFile(GAT.createFile(context, "inputFiles.zip"));
	
	
	ResourceDescription rd = new HardwareResourceDescription();
	rd.addResourceAttribute("machine.node", submissionNode);
	
	JobDescription jd = new JobDescription(sd, rd);
	ResourceBroker broker = GAT.createResourceBroker(context, prefs);
	Job job = broker.submitJob(jd);
	
	while ((job.getState() != Job.STOPPED)
	       && (job.getState() != Job.SUBMISSION_ERROR)) {
	    //System.err.println("job state = " + job.getInfo());
	    Thread.sleep(1000);
	}
	
	System.err.println("job DONE, state = " + job.getInfo());
	GAT.end();
    }
}
