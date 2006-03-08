package file;

import java.net.URI;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.io.File;

class RemoteCopy {

	public static void main(String[] args) {
		URI src = null;
		URI dest = null;
		File file = null;

		System.err.println("-----REMOTE-FILE COPY TEST------------");
		GATContext context = new GATContext();

		try {
			src = new URI("file://fs0.das2.cs.vu.nl//bin/echo");
			dest = new URI(
					"file://litchi.zib.de//mnt/kiwi/people/gridlab/glab014/hiha.dat");
			file = GAT.createFile(context, src);
		} catch (Exception e) {
			System.err.println("File creation failed: " + e);
			System.exit(1);
		}

		try {
			file.copy(dest);
			System.err.println("-----REMOTE-FILE COPY TEST-OK---------");
		} catch (Exception e) {
			System.err.println("Could not copy file: " + e);
			e.printStackTrace();
			System.exit(1);
		}
	}
}
