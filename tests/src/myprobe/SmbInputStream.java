package myprobe;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;

public class SmbInputStream {
    public static void main(String argv[]) throws Exception {
	URI loc =  null;
	String user = null;
	String password = null;
	GATContext context =  new GATContext();
	SecurityContext pwd = null;
	FileInputStream fis =  null;

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
	
	fis = GAT.createFileInputStream(context, loc.toString());
	
	byte[] buf = new byte[1024];
	int i;
	/*System.err.println("try read() "+fis.read());
	i = fis.read(buf, 1, 3);
	System.err.print("try read(buf, off, len) ");
	System.err.write(buf, 0, i);
	System.err.println("try skip(2)");
	fis.skip(2);
	System.err.println("try read(buf)");*/
	while((i=fis.read(buf))!=-1) {
	    System.out.write(buf, 0, i);
	}
	fis.close();
    }
}
