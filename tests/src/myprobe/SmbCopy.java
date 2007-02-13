package myprobe;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import java.io.FileOutputStream;

public class SmbCopy {
    public static void main(String[] argv) throws Exception {
	SmbFileInputStream in =  new SmbFileInputStream(argv[0]);
	FileOutputStream out = new FileOutputStream(argv[1]);
	byte[] buf=new byte[1024];
	int i;
	while((i=in.read(buf))!=-1) {
	    out.write(buf, 0, i);
	}
	in.close();
	out.close();
    }
}
