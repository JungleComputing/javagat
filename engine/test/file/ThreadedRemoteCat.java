package file;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInputStream;

class ThreadedRemoteCat extends Thread {

    private String uri;

    private String filename;

    public ThreadedRemoteCat(String loc, String fn) {
        uri = loc + "/" + fn;
        filename = fn;
    }

    public void run() {
        try {
            GATContext context = new GATContext();

            FileInputStream in = GAT.createFileInputStream(context,
                new URI(uri));
            InputStreamReader reader = new InputStreamReader(in); // these are
            BufferedReader buf = new BufferedReader(reader); // standard java classes

            String result = buf.readLine();
            new PrintStream(new FileOutputStream(filename), true)
                .println(result);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //GAT.end();
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out
                .println("Usage: ThreadedRemoteCat [scheme://]hostname filename [filename [filename ...]]");
            System.exit(66);
        }
        for (int i = 1; i < args.length; i++) {
            Thread th = new ThreadedRemoteCat(args[0], args[i]);
            th.start();
        }
    }
}
