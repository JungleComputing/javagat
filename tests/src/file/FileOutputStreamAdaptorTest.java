/*
 * Created on Oct 25, 2005
 */
package file;

import java.io.IOException;
import java.net.URISyntaxException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class FileOutputStreamAdaptorTest {

    public static void main(String[] args) {
        FileOutputStreamAdaptorTest a = new FileOutputStreamAdaptorTest();
        a.test(args[0], args[1]).print();
    }

    public AdaptorTestResult test(String adaptor, String host) {

        AdaptorTestResult adaptorTestResult = new AdaptorTestResult(adaptor,
                host);

        Preferences preferences = new Preferences();
        preferences.put("fileoutputstream.adaptor.name", adaptor);
        FileOutputStream out = null;
        try {
            out = GAT.createFileOutputStream("any://" + host + "/"
                    + "JavaGAT-test-fileoutputstream");
        } catch (GATObjectCreationException e) {
        }
        byte[] large = new byte[10 * 1024 * 1024];
        for (int i = 0; i < large.length; i++) {
            large[i] = 'a';
        }
        adaptorTestResult.put("write (small)", writeTest(out, "test\n"));
        adaptorTestResult.put("write (large)",
                writeTest(out, new String(large)));
        adaptorTestResult.put("flush        ", flushTest(out));
        adaptorTestResult.put("close        ", closeTest(out));

        run(host, "fileoutputstream-adaptor-test-clean.sh");

        return adaptorTestResult;

    }

    private void run(String host, String script) {
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable("/bin/sh");
        sd.setArguments(script);
        try {
            sd.addPreStagedFile(GAT.createFile(script));
        } catch (GATObjectCreationException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Preferences preferences = new Preferences();
        preferences.put("resourcebroker.adaptor.name", "sshtrilead,local");
        ResourceBroker broker = null;
        try {
            broker = GAT.createResourceBroker(preferences, new URI("any://"
                    + host));
        } catch (GATObjectCreationException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Job job = null;
        try {
            job = broker.submitJob(new JobDescription(sd));
        } catch (GATInvocationException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while (job.getState() != Job.STOPPED
                && job.getState() != Job.SUBMISSION_ERROR) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private AdaptorTestResultEntry writeTest(FileOutputStream out, String text) {
        long start = System.currentTimeMillis();
        try {
            out.write(text.getBytes());
        } catch (IOException e) {
            return new AdaptorTestResultEntry(false, 0, e);
        }
        long stop = System.currentTimeMillis();
        return new AdaptorTestResultEntry(true, (stop - start), null);
    }

    private AdaptorTestResultEntry flushTest(FileOutputStream out) {
        long start = System.currentTimeMillis();
        try {
            out.flush();
        } catch (IOException e) {
            return new AdaptorTestResultEntry(false, 0, e);
        }
        long stop = System.currentTimeMillis();
        return new AdaptorTestResultEntry(true, (stop - start), null);
    }

    private AdaptorTestResultEntry closeTest(FileOutputStream out) {
        long start = System.currentTimeMillis();
        try {
            out.close();
        } catch (IOException e) {
            return new AdaptorTestResultEntry(false, 0, e);
        }
        long stop = System.currentTimeMillis();
        return new AdaptorTestResultEntry(true, (stop - start), null);
    }
}
