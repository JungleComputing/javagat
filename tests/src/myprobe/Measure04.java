/**
 * Copy samba file (optionally using username, password) to the
 * local machine (localfile_loc).
 */
package myprobe;

import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbFile;
import jcifs.smb.NtlmPasswordAuthentication;
import org.gridlab.gat.URI;

import java.io.FileInputStream;

class Measure04 {
    public static void main(String argv[]) throws Exception {
	String loc1 = null;
	String loc2 = null;
	SmbFileOutputStream fos = null;
	FileInputStream fis = null;
	String usage = new String("Usage: smbfile_loc "+
				  "[username password] localfile_loc");
	try {
	    if(argv.length == 2) {
		loc1 = argv[0];
		loc2 = argv[1];
	    } else if(argv.length == 4) {
		loc1 = argv[0];
		String username = argv[1];
		String password = argv[2];
		loc2 = argv[3];
		String host = URI.create(loc1).getHost();
		NtlmPasswordAuthentication auth =  
		    new NtlmPasswordAuthentication( host, username, password );
		SmbFile smbf = new SmbFile(loc1, auth);
		fis = new FileInputStream(loc2);
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
