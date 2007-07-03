package tests.gt4;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.io.File;

import jargs.gnu.CmdLineParser;

public class TestResourceBroker {
    static void printUsage() {
	System.err.println("Usage: TestResourceBroker [perferences] \\ \n" +
			   " --exe executable \\ \n" +
			   " [--input inputfile] \\ \n" + 
			   " [--output outputfile] \\ \n" +
			   " [--error errotoutputfile \\ \n" +
			   " contact\n" +
			   "The contact is like that:\n" +
			   "[protocol://]{hostname|hostaddr}" +
			   "[:port][/service]).\n" +
			   "The preferences can passed like this:\n" +
			   "--perfkey key --perfvalue value\n");
    }
    public static void main(String[] args) throws Exception {
	CmdLineParser parser = new CmdLineParser();
	CmdLineParser.Option cmdPrefKey = parser.addStringOption("prefkey"); 
	CmdLineParser.Option cmdPrefValue = parser.addStringOption("prefvalue");
	CmdLineParser.Option cmdExe = parser.addStringOption("exe");
	CmdLineParser.Option cmdInput = parser.addStringOption("input");
	CmdLineParser.Option cmdOutput = parser.addStringOption("output");
	CmdLineParser.Option cmdError = parser.addStringOption("error");

	try {
	    parser.parse(args);
	}
	catch(Exception e) {
	    System.err.println(e);
	    printUsage();
	    System.exit(0);
	}
	String[] rargs=parser.getRemainingArgs();
	
	if(rargs.length==0) {
	    System.err.println("The contact is missing.");
	    printUsage();
	    System.exit(0);
	}
	String node = rargs[0];


	Preferences prefs = new Preferences();
	String prefKey = (String) parser.getOptionValue(cmdPrefKey);
	String prefValue = (String) parser.getOptionValue(cmdPrefValue);
	while( prefKey!=null && prefValue!=null ) {
	    prefs.put(prefKey, prefValue);
	    prefKey = (String) parser.getOptionValue(cmdPrefKey);
	    prefValue = (String) parser.getOptionValue(cmdPrefValue);
	}
	String exeName = (String) parser.getOptionValue(cmdExe);
	if(exeName==null) {
	    System.err.println("The --exe executable is missing.");
	    printUsage();
	    System.exit(0);
	}
	String stdinName = (String) parser.getOptionValue(cmdInput);
	if(stdinName!=null) {
	}
	String stdoutName = (String) parser.getOptionValue(cmdOutput);
	String stderrName = (String) parser.getOptionValue(cmdError);


	GATContext context = new GATContext();
	ResourceDescription rd = new HardwareResourceDescription();
	rd.addResourceAttribute("machine.node", node);
	SoftwareDescription sd = new SoftwareDescription();
	sd.setLocation(exeName);
	if(stdoutName != null) {
	    File stdout = GAT.createFile(context, stdoutName);
	    sd.setStdout(stdout);
	}
	if(stdinName != null) {
	    File stdin = GAT.createFile(context, stdinName);
	    sd.setStdin(stdin);
	}
	if(stderrName != null) {
	    File stderr = GAT.createFile(context, stderrName);
	    sd.setStderr(stderr);
	}
	JobDescription jd = new JobDescription(sd, rd);

	long start = System.currentTimeMillis();
	ResourceBroker broker = GAT.createResourceBroker(context, prefs);
	Job job = broker.submitJob(jd);
	while (job.getState() != Job.STOPPED &&
	       job.getState() != Job.SUBMISSION_ERROR) Thread.sleep(1000);
	long stop = System.currentTimeMillis();
	long time = stop-start;
	System.out.println("Job execution took " + time + " millisec");
    }
}
