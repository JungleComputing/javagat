package tutorial20;

import org.gridlab.gat.GAT;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;

public class RemoteCopy {

	/**
	 * Copies a file over the grid.
	 * 
	 * @param args
	 *            the arguments. args[0] should contain the source, args[1] the
	 *            destination
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
	        Preferences prefs = new Preferences();
	        prefs.put("File.adaptor.name", "GridFTP");
		GAT.createFile(prefs, args[0]).copy(new URI(args[1]));
		GAT.end();
	}
}
