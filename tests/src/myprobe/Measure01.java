package myprobe;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;

class Measure01 {
    public static void main(String argv[]) throws Exception {
	URI loc1 =  null;
	URI loc2 =  null;
	String user1 = null;
	String password1 = null;
	GATContext context =  new GATContext();
	SecurityContext pwd = null;
	String usage = new String("Usage: location1 [username password] "+
				  "location2");

	try {
	    if(argv.length == 2) {
		loc1 = URI.create(argv[0]);
		loc2 = URI.create(argv[1]);
	    } else if(argv.length == 4 ) {
		loc1 = URI.create(argv[0]);
		user1 = argv[1];
		password1 = argv[2];
		loc2 = URI.create(argv[3]);
		pwd =  new PasswordSecurityContext(user1, password1);
		context.addSecurityContext(pwd);
	    } else {
		System.out.println(usage);
		return;
	    }
	} catch( Exception e ) {
	    System.out.println(usage);
	    System.err.println("Parameters: "+e);
	    throw e;
	}

	FileInputStream fis = GAT.createFileInputStream(context, loc1);
	FileOutputStream fos = GAT.createFileOutputStream(context, loc2);
	long start = System.currentTimeMillis();
	byte[] buf = new byte[1024];
	int i = 0;
	while((i=fis.read(buf))!=-1) {
	    fos.write(buf, 0, i);
	}
	long stop = System.currentTimeMillis();
	long time = stop-start;
	System.out.println("File copy took "+time+" seconds");
	fis.close();
	fos.close();
    }
}
