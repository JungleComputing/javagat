package file;

import java.net.URISyntaxException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

class Copy {
	public static void main(String[] args) throws URISyntaxException {
		URI uri = null;
		uri = new URI(new java.io.File("C:\\Documents and Settings\\user\\workspace\\JavaGAT\\bla.tmp").toURI());
		System.out.println("URI: " + uri);
		System.out.println(uri.getHost());
		System.out.println(uri.getPath());
		if (uri != null) {
			System.exit(1);
		}
		
		GATContext context = new GATContext();
		// context.addPreference("file.create", "true");
		context.addPreference("File.adaptor.name", args[0]);
		if (args.length > 3) {
			context.addPreference("file.chmod", args[3]);
		}
		System.err.println("------------FILE COPY TEST------------");

		URI src = null;
		URI dest = null;
		File file = null;

		try {
			src = new URI(args[1]);
			dest = new URI(args[2]);
			file = GAT.createFile(context, src);
		} catch (Exception e) {
			System.err.println("File creation failed: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		if (file == null) {
			System.err.println("Could not create a file object.");
			System.exit(1);
		}

		try {
			file.copy(dest);
			System.err.println("------------FILE COPY TEST OK---------");
		} catch (Exception e) {
			System.err.println("Could not copy file: " + e);
			e.printStackTrace();
			System.exit(1);
		}
	}
}
