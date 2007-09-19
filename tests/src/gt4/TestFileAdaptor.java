package gt4;

import jargs.gnu.CmdLineParser;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

public class TestFileAdaptor {
    static void printUsage() {
	System.err.println("Usage: TestFileAdaptor command [argument] fileuri\n" +
			   "The fileuri is URI of a file. The commands are\n" +
			   "the next, some of these need argument. The" +
			   "prefkey and prefvalue parameters can be used" +
			   "several times.\n" +
			   "  --delete\tdeletes file\n" +
			   "  --prefkey key \tset preference key for GAT\n" +
			   "  --prefvalue value \tset preference value for " +
			   "GAT\n" +
			   "  --createfile\tcreates a file\n" +
			   "  --exists\tchecks the file is exists or not\n" +
			   "  --absolutepath\treturns the absolute path\n" +
			   "  --isdir\tchecks the file for directory\n" +
			   "  --lastmod\treturns the time of last " +
			   "modification in milliseconds\n" +
			   "  --setlastmod ms\tsets the last" +
			   "modification to ms given in milliseconds\n" +
			   "  --size\treturns the size of the file\n" +
			   "  --list\tlists the directory\n" +
			   "  --mkdir\tcreates a directory\n" +
			   "  --setreadoly\tsets the file read-only\n" +
			   "  --copy fileuri\tcopies file to destination URI\n" +
			   "  --canwrite\tchecks file for writing\n" +
			   "  --canread\tchecks file for reading\n");
    }

    public static void main(String[] args) throws Exception {
	CmdLineParser parser = new CmdLineParser();
	CmdLineParser.Option cmdDelete = parser.addBooleanOption("delete");
	CmdLineParser.Option cmdPrefKey = parser.addStringOption("prefkey"); 
	CmdLineParser.Option cmdPrefValue = parser.addStringOption("prefvalue"); 
	CmdLineParser.Option cmdCreateFile = parser.addBooleanOption("createfile");
	CmdLineParser.Option cmdExists = parser.addBooleanOption("exists");
	CmdLineParser.Option cmdAbsolutePath = parser.addBooleanOption("absolutepath");
	CmdLineParser.Option cmdIsDir = parser.addBooleanOption("isdir");
	CmdLineParser.Option cmdLastMod = parser.addBooleanOption("lastmod");
	CmdLineParser.Option cmdSetLastMod = parser.addLongOption("setlastmod");
	CmdLineParser.Option cmdSize = parser.addBooleanOption("size");
	CmdLineParser.Option cmdList = parser.addBooleanOption("list");
	CmdLineParser.Option cmdMkdir = parser.addBooleanOption("mkdir");
	CmdLineParser.Option cmdSetReadOnly = parser.addBooleanOption("setreadonly");
	CmdLineParser.Option cmdCopy = parser.addStringOption("copy");
	CmdLineParser.Option cmdCanWrite = parser.addBooleanOption("canwrite");
	CmdLineParser.Option cmdCanRead = parser.addBooleanOption("canread");
	CmdLineParser.Option cmdIsFile = parser.addBooleanOption("isfile");

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

	Preferences prefs = new Preferences();
	String prefKey = (String) parser.getOptionValue(cmdPrefKey);
	String prefValue = (String) parser.getOptionValue(cmdPrefValue);
	while( prefKey!=null && prefValue!=null ) {
	    prefs.put(prefKey, prefValue);
	    prefKey = (String) parser.getOptionValue(cmdPrefKey);
	    prefValue = (String) parser.getOptionValue(cmdPrefValue);
	}
	
	URI location = null;
	GATContext context = new GATContext();
	location = URI.create(rargs[0]);
	File f = GAT.createFile(context, prefs, location);

	if(parser.getOptionValue(cmdDelete)!=null) {
	    if(f.delete()) {
		System.out.println(location + ": file is deleted.");
	    }
	    else {
		System.out.println(location + ": operation failed.");
	    }
	}
	if(parser.getOptionValue(cmdCreateFile)!=null) {
	    if(f.createNewFile()) {
		System.out.println(location + ": new file created.");
	    }
	    else {
		System.out.println(location + ": operation failed.");
	    }
	}
	if(parser.getOptionValue(cmdExists)!=null) {
	    if(f.exists() ) {
		System.out.println(location + ": file exists.");
	    }
	    else {
		System.out.println(location + ": operation returned false.");
	    }
	}
	if(parser.getOptionValue(cmdAbsolutePath)!=null) {
	    System.out.println(f.getAbsolutePath());
	}
	if(parser.getOptionValue(cmdIsDir)!=null) {
	    if(f.isDirectory()) {
		System.out.println(location + ": is a directory.");
	    }
	    else {
		System.out.println(location + ": is not a directory.");
	    }
	}
	if(parser.getOptionValue(cmdLastMod)!=null) {
	    System.out.println(location + " was last modified in milliseconds: " + f.lastModified());
	}
	if(parser.getOptionValue(cmdSize)!=null) {
	    System.out.println(location + " file size: " + f.length());
	}
	if(parser.getOptionValue(cmdList)!=null) {
	    System.out.println(location + " file list:");
	    String[] dl = f.list();
	    for(int i=0; i<dl.length; i++) {
		System.out.println("entry: " + dl[i]);
	    }
	}
	if(parser.getOptionValue(cmdMkdir)!=null) {
	    if(f.mkdir()) {
		System.out.println(location + ": directory created.");
	    }
	    else {
		System.out.println(location + ": operation failed.");
	    }
	}
	Long t = (Long) parser.getOptionValue(cmdSetLastMod);
	if(t!=null) {
	    if(f.setLastModified(t)) {
		System.out.println(location + ": last modified time set.");
	    }
	    else {
		System.out.println(location + ": operation failed.");
	    }
	}
	if(parser.getOptionValue(cmdSetReadOnly)!=null) {
	    if(f.setReadOnly()) {
		System.out.println(location + ": file attempted to set read only.");
	    }
	    else {
		System.out.println(location + ": operation failed.");
	    }
	}
	if(parser.getOptionValue(cmdCanWrite)!=null) {
	    if(f.canWrite()) {
		System.out.println(location + ": file is writable.");
	    } else {
		System.out.println(location + ": file is not writable.");
	    }
	}
	if(parser.getOptionValue(cmdCanRead)!=null) {
	    if(f.canRead()) {
		System.out.println(location + ": file is readable.");
	    } else {
		System.out.println(location + ": file is not readable.");
	    }
	}
	if(parser.getOptionValue(cmdIsFile)!=null) {
	    if(f.isFile()) {
		System.out.println(location + ": that is a file.");
	    } else {
		System.out.println(location + ": is not a file.");
	    }
	}
	String dest = (String) parser.getOptionValue(cmdCopy);
	if(dest!=null) {
	    URI destination = URI.create(dest);
	    long start = System.currentTimeMillis();
	    f.copy(destination);
	    long stop = System.currentTimeMillis();
	    long time = stop-start;
	    System.out.println(location + ": file attempted to copy to destination " + dest);
	    System.out.println("File copy took " + time + " millisec");
	}
    }
}
