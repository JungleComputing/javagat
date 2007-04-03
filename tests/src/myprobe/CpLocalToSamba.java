/**
 * Copy local file to samba location (using username, password)
 */
package myprobe;

import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbFile;
import jcifs.smb.NtlmPasswordAuthentication;
import org.gridlab.gat.URI;

import java.io.FileInputStream;

class CpLocalToSamba {
    public static void main(String argv[]) throws Exception {
	String loc1 = null;
	String loc2 = null;
	FileInputStream fis = null;
	SmbFileOutputStream fos = null;
	String usage = new String("Usage: localfile_loc smbfile_loc "+
				  "[username password]");
	try {
	    if(argv.length == 2) {
		loc1 = argv[0];
		loc2 = argv[1];
		fis = new FileInputStream(loc1);
		fos = new SmbFileOutputStream(loc2);
	    } else if(argv.length == 4) {
		loc1 = argv[0];
		loc2 = argv[1];
		String username = argv[2];
		String password = argv[3];
		String host = URI.create(loc2).getHost();
		NtlmPasswordAuthentication auth =  
		    new NtlmPasswordAuthentication( host, username, password );
		SmbFile smbf = new SmbFile(loc2, auth);
		fis = new FileInputStream(loc1);
		fos = new SmbFileOutputStream(smbf);
	    } else {
		System.out.println(usage);
		return;
	    }
	} catch(Exception e) {
	    System.out.println(usage);
	    System.err.println("Parameters: "+e);
	}
	
	long start = System.currentTimeMillis();
	byte[] buf = new byte[1048576];
	int i = 0;
	while((i=fis.read(buf))!=-1) {
	    fos.write(buf, 0, i);
	}
	long stop = System.currentTimeMillis();
	long time = stop-start;
	System.out.println("File copy took "+time+" millisec");
	fis.close();
	fos.close();
    }
}
