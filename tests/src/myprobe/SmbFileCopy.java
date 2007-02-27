package myprobe;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;

public class SmbFileCopy {
    public static void main(String argv[]) throws Exception {
	URI loc1 = null;
	URI loc2 = null;
	GATContext context = new GATContext();
	File f1 = null;
	
	try {
	    if(argv.length == 2) {
		loc1 = URI.create(argv[0]);
		loc2 = URI.create(argv[1]);
	    } else {
		System.out.println("Usage: location1 location2");
		return;
	    }
	} catch( Exception e ) {
	    System.out.println("Usage: location1 location2");
	    System.err.println("Parameters: "+e);
	    throw e;
	}
	f1 = GAT.createFile(context, loc1.toString());
	f1.copy(loc2);
    }
}
