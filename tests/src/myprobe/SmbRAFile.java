package myprobe;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.RandomAccessFile;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;

import java.io.PrintWriter;

public class SmbRAFile {
    public static void main(String argv[]) throws Exception {
	URI loc = null;
	String user = null;
	String password = null;
	GATContext context = new GATContext();
	SecurityContext pwd = null;
	RandomAccessFile f = null;
	
	try {
	    if(argv.length == 1) {
		loc = URI.create(argv[0]);
	    } else if(argv.length == 3) {
		loc = URI.create(argv[0]);
		user = argv[1];
		password = argv[2];
		pwd = new PasswordSecurityContext(user, password);
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
	byte[] buf = new byte[1024];
	String m = new String("--Hello world!--");
	f = GAT.createRandomAccessFile(context, loc, "rw");
	f.seek(f.length()/2);
	f.write(m.getBytes());

	f.seek(0);
	f.skipBytes(3);
	System.err.println("Cat from position: "+
			   f.getFilePointer() );
	int i;
	while( (i=f.read(buf))!=-1 ) {
	    System.out.write(buf, 0, i);
	}
	f.close();
    }
}
