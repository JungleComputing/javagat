package bbgt4tests;

import org.gridlab.gat.URI;

public class TestGAT {
    public static void main(String[] args) throws Exception {
	URI uri = new URI(args[0]);
	System.out.println(uri.isLocal());
	System.out.println(uri.refersToLocalHost());
    }
}
