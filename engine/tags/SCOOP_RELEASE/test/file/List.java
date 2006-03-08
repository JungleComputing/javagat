package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

class List {

	public static void main(String[] args) {
		URI src = null;
		File file = null;

		System.err.println("-----REMOTE-FILE LIST TEST------------");
		GATContext context = new GATContext();
		Preferences prefs = new Preferences();
		//		prefs.put("file.adaptor.name", "gridftp");

		try {
			src = new URI("any://fs0.das2.cs.vu.nl//bin");
			file = GAT.createFile(context, prefs, src);
		} catch (Exception e) {
			System.err.println("File creation failed: " + e);
			System.exit(1);
		}

		try {
			String[] res = file.list();

			for (int i = 0; i < res.length; i++) {
				System.err.println("    " + res[i]);
			}

			System.err.println("-----REMOTE-FILE COPY TEST-OK---------");
		} catch (Exception e) {
			System.err.println("Could not list file:" + e);
			e.printStackTrace();
			System.exit(1);
		}
	}
}