package bbgt4tests;

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
	System.err.println("Usage: ...");
    }
    public static void main(String[] args) throws Exception {
	CmdLineParser parser = new CmdLineParser();
	CmdLineParser.Option cmdPrefKey = parser.addStringOption("prefkey"); 
	CmdLineParser.Option cmdPrefValue = parser.addStringOption("prefvalue");
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
	
	GATContext context = new GATContext();
	ResourceDescription rd = new HardwareResourceDescription();
	rd.addResourceAttribute("machine.node", node);
	SoftwareDescription sd = new SoftwareDescription();
	sd.setLocation("file:////bin/hostname");
	File stdout = GAT.createFile(context, "/home0/bbokodi/tmp/hostname.txt");
	sd.setStdout(stdout);
	JobDescription jd = new JobDescription(sd, rd);
	ResourceBroker broker = GAT.createResourceBroker(context, prefs);
	Job job = broker.submitJob(jd);
	while (job.getState() != Job.STOPPED &&
	       job.getState() != Job.SUBMISSION_ERROR) Thread.sleep(1000);
    }
}
