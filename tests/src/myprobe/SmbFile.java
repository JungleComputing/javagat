package myprobe;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;

public class SmbFile {
    public static void main(String argv[]) throws Exception {
	String comm =  null;
	URI loc = null;
	String user = null;
	String password = null;
	GATContext context = new GATContext();
	SecurityContext pwd = null;
	File f = null;

	String usage = new String("Usage: command location "+
				  "[username password]\ncommand: "+
				  "list, listFiles, exists, isDirectory, "+
				  "length, mkdir, "+
				  "mkdirs, delete, canRead, canWrite, "+
				  "createNewFile, isFile, isHidden, "+
				  "lastModified");
	
	try {
	    if(argv.length == 2) {
		comm = argv[0];
		loc = URI.create(argv[1]);
	    } else if(argv.length == 4 ) {
		comm = argv[0];
		loc = URI.create(argv[1]);
		user = argv[2];
		password = argv[3];
		pwd =  new PasswordSecurityContext(user, password);
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
	f = GAT.createFile(context, loc.toString());
	
	if(comm.equalsIgnoreCase("list")) {
	    String[] l=f.list();
	    for(String e: l) {
		System.out.println(e);
	    }
	}
	else if(comm.equalsIgnoreCase("listFiles")) {
	    File[] fl=f.listFiles();
	    for(File ff: fl) {
		System.out.println(ff.getName());
	    }
	}
	else if(comm.equalsIgnoreCase("exists")) {
	    System.out.println(f.exists());
	}
	else if(comm.equalsIgnoreCase("isDirectory")) {
	    System.out.println(f.isDirectory());
	}
	else if(comm.equalsIgnoreCase("length")) {
	    System.out.println(f.length());
	}
	else if(comm.equalsIgnoreCase("mkdir")) {
	    System.out.println("mkdir: "+f.mkdir());
	}
	else if(comm.equalsIgnoreCase("mkdirs")) {
	    System.out.println("mkdirs: "+f.mkdirs());
	}
	else if(comm.equalsIgnoreCase("delete")) {
	    System.out.println("delete: "+f.delete());
	}
	else if(comm.equalsIgnoreCase("canRead")) {
	    System.out.println(f.canRead());
	}
	else if(comm.equalsIgnoreCase("canWrite")) {
	    System.out.println(f.canWrite());
	}
	else if(comm.equalsIgnoreCase("createNewFile")) {
	    System.out.println("create new file: "+f.createNewFile());
	}
	else if(comm.equalsIgnoreCase("isFile")) {
	    System.out.println(f.isFile());
	}
	else if(comm.equalsIgnoreCase("isHidden")) {
	    System.out.println(f.isHidden());
	}
	else if(comm.equalsIgnoreCase("lastModified")) {
	    System.out.println(f.lastModified());
	}
	else {
	    System.out.println("Unknown command");
	    System.out.println(usage);
	    return;
	}
    }
}
