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
	    Preferences preferences = new Preferences();//rftgt4  gt42gridftp
        preferences.put("file.adaptor.name", "gt42gridftp");		
		GAT.createFile(preferences,args[0]).copy(new URI(args[1]));
		GAT.end();
	}
}
