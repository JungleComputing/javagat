package myprobe;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;

import java.io.PrintWriter;

public class SmbOutputStream {
    public static void main(String argv[]) throws Exception {
	URI loc =  null;
	String user = null;
	String password = null;
	GATContext context =  new GATContext();
	SecurityContext pwd = null;
	FileOutputStream fos =  null;

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
	
	fos = GAT.createFileOutputStream(context, loc.toString());
	PrintWriter pw = new PrintWriter(fos);
	pw.println("Hello world!");
	pw.close();
	fos.close();
    }
}
