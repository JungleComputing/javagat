package myprobe;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.UniAddress;
import jcifs.smb.SmbSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SmbCat {
    public static void main(String[] args) throws Exception {
	//System.setProperty( "jcifs.smb.client.username", args[1] );
	//System.setProperty( "jcifs.smb.client.password", args[2] );
	//NtlmPasswordAuthentication auth = 
	//new NtlmPasswordAuthentication("mshome", "balazs", "juntUll");
	//SmbSession.logon(UniAddress.getByName("localhost"), auth);
	SmbFileInputStream in=null;
	in = new SmbFileInputStream(args[0]);
	InputStreamReader reader = new InputStreamReader(in);
	BufferedReader buf = new BufferedReader(reader);

	while(true) {
	    String result = buf.readLine();
	    if(result == null) {
		break;
	    }
	    System.out.println(result);
	}
	in.close();
    }
}
