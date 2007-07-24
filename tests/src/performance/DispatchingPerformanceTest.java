/*
 * Created on Jul 24, 2007 by rob
 */
package performance;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

public class DispatchingPerformanceTest {
    static final int DUMMY_COUNT = 100000;

    static final int ALL_COUNT = 1000;

    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("File.adaptor.name", "dummy");

        System.err.println("test with all file adaptors");
        runTest(context, null, ALL_COUNT);
        
        System.err.println("test with no file adaptors");
        runTest(context, prefs, DUMMY_COUNT);

        System.err.println("invocation test of an unimplemented method on all file adaptors");
        runInvocationTest(context, null, DUMMY_COUNT);
        GAT.end();
    }

    private static void runTest(GATContext context, Preferences prefs, int count) throws Exception {
        URI src = new URI("test");
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            GAT.createFile(context, prefs, src);
        }
        long end = System.currentTimeMillis();
        System.err.println("test took " + (end - start) + " ms");
        double timePerCall = ((double) (end - start)) / count;
        System.err.println("time per call is: " + timePerCall + " ms");
    }

    private static void runInvocationTest(GATContext context, Preferences prefs, int count) throws Exception {
        URI src = new URI("test");
        File f = GAT.createFile(context, prefs, src);
        
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            try {
                f.getMetricDefinitions();
            } catch (GATInvocationException e) {
                // ignore
            }
        }
        long end = System.currentTimeMillis();
        System.err.println("test took " + (end - start) + " ms");
        double timePerCall = ((double) (end - start)) / count;
        System.err.println("time per call is: " + timePerCall + " ms");
        
    }
}
