package myprobe;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;

public class SmbFileCreateFile {
    public static void main(String argv[]) throws Exception {
	URI loc = null;
	String user = null;
	String password = null;
	GATContext context = new GATContext();
	SecurityContext pwd = null;
	File f = null;
	
	try {
	    if(argv.length == 1) {
		loc = URI.create(argv[0]);
	    } else if(argv.length == 3 ) {
		loc = URI.create(argv[0]);
		user = argv[1];
		password = argv[2];
		pwd =  new PasswordSecurityContext(user, password);
		context.addSecurityContext(pwd);
	    } else {
		System.out.println("Usage: location [username password]");
		return;
	    }
	 
	} catch( Exception e ) {
	    System.out.println("Usage: location [username password]");
	    System.err.println("Parameters: "+e);
	    throw e;
	}
	f = GAT.createFile(context, loc.toString());
	
	if(f.createNewFile()) {
	    System.err.println("file created");
	}
    }
}
