package org.gridlab.gat.io.cpi.sshtrilead;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;

import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

public class SshTrileadFileInputStreamAdaptor extends FileInputStreamCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileInputStreamCpi.getSupportedCapabilities();
        capabilities.put("close", true);
        capabilities.put("markSupported", true);
        capabilities.put("read", true);
        capabilities.put("skip", true);
        return capabilities;
    }
    
    public static String getDescription() {
        return "The SshTrilead FileInputStream Adaptor implements the FileInputStream object using the trilead ssh library.";
    }
    
    public static Preferences getSupportedPreferences() {
        Preferences preferences = FileInputStreamCpi.getSupportedPreferences();
        
        preferences.put("sshtrilead.caching.iswindows", "true");
        preferences.put("sshtrilead.caching.iscsh", "true");
        preferences
                .put(
                        "sshtrilead.cipher.client2server",
                        "aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc");
        preferences
                .put(
                        "sshtrilead.cipher.server2client",
                        "aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc");
        preferences.put("sshtrilead.tcp.nodelay", "false");
        preferences.put("sshtrilead.use.cached.connections", "true");
        preferences.put("sshtrilead.connect.timeout", "0");
        preferences.put("sshtrilead.kex.timeout", "0");
        
        // Added: preferences for hostkey checking. Defaults are what used to be ....
        preferences.put("sshtrilead.strictHostKeyChecking", "false");
        preferences.put("sshtrilead.noHostKeyChecking", "true");

        preferences.put("sftptrilead.strictHostKeyChecking", "false");
        preferences.put("sftptrilead.noHostKeyChecking", "true");
        return preferences;
    }
       
    public static String[] getSupportedSchemes() {
        return SshTrileadFileAdaptor.getSupportedSchemes();
    }
    
    private SshTrileadFileAdaptor file;
    
    private Session session;
    
    private InputStream sessionOutputStream;
    
    private InputStreamRunner job;
        
    public SshTrileadFileInputStreamAdaptor(GATContext gatContext, URI location) throws GATObjectCreationException, GATInvocationException {
        super(gatContext, location);
        
        if (location.refersToLocalHost()) {
            throw new AdaptorNotApplicableException("this adaptor cannot read local files");
        }
        
        file = new SshTrileadFileAdaptor(gatContext, location);
        
        if (! file.exists()) {
            throw new GATObjectCreationException("file does not exist");
        }
        if (file.isDirectory()) {
            throw new GATObjectCreationException("cannot read from directory");
        }
        if (! file.canRead()) {
            throw new GATObjectCreationException("cannot open file");
        }

        try {
            session = file.getSession();
        } catch(Throwable e) {
            throw new GATObjectCreationException("Could not create stream", e);
        }
        sessionOutputStream = session.getStdout();
        String command = "cat < " + SshTrileadFileAdaptor.protectAgainstShellMetas(file.getFixedPath());
        job = new InputStreamRunner(command);
        job.setDaemon(true);
        job.start();
    }
    
    public void mark(int readlimit) {
        sessionOutputStream.mark(readlimit);   
    }

    public boolean markSupported() {
        return sessionOutputStream.markSupported();
    }

    public int read() throws GATInvocationException {
        try {
            return sessionOutputStream.read();
        } catch (IOException e) {
            throw new GATInvocationException("got exception", e);
        }
    }

    public int read(byte[] b, int off, int len) throws GATInvocationException {
        try {
            return sessionOutputStream.read(b, off, len);
        } catch (IOException e) {
            throw new GATInvocationException("got exception", e);
        }
    }

    public int read(byte[] b) throws GATInvocationException {
        try {
            return sessionOutputStream.read(b);
        } catch (IOException e) {
            throw new GATInvocationException("got exception", e);
        }
    }

    public void reset() throws GATInvocationException {
        try {
            sessionOutputStream.reset();
        } catch (IOException e) {
            throw new GATInvocationException("got exception", e);
        }
    }

    public long skip(long n) throws GATInvocationException {
        try {
            return sessionOutputStream.skip(n);
        } catch (IOException e) {
            throw new GATInvocationException("got exception", e);
        }
    }

    public void close() throws GATInvocationException {
        if (session != null) {
            try {
                sessionOutputStream.close();
            } catch (IOException e) {
                // ignored
            }
            try {
                job.join();
            } catch(InterruptedException e) {
                // ignored
            }
            session = null;
        }
    }
    
    
    private class InputStreamRunner extends Thread {
        private final String command;
        
        public InputStreamRunner(String command) {
            this.command = command;
        }
        
        public void run() {
            try {
                execCommand(command);
            } catch (Exception e) {
                // TODO: what to do here?
            }
        }
        
        private String[] execCommand(String cmd) throws Exception {
            try {
                String[] result = new String[3];

                if (logger.isInfoEnabled()) {
                    logger.info("command: " + cmd);
                }
                session.execCommand(cmd);
                // see http://www.trilead.com/Products/Trilead-SSH-2-Java/FAQ/#blocking
                InputStream stderr = new StreamGobbler(session.getStderr());

                BufferedReader br = new BufferedReader(new InputStreamReader(stderr));
                StringBuffer err = new StringBuffer();
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    err.append(line);
                    err.append("\n");
                }
                result[SshTrileadFileAdaptor.STDERR] = err.toString();
                while (session.getExitStatus() == null) {
                    Thread.sleep(500);
                }
                result[SshTrileadFileAdaptor.EXIT_VALUE] = "" + session.getExitStatus();

                if (logger.isDebugEnabled()) {
                    logger.debug("STDERR: " + result[SshTrileadFileAdaptor.STDERR]);
                    logger.debug("EXIT:   " + result[SshTrileadFileAdaptor.EXIT_VALUE]);
                }
                return result;
            } finally {
                session.close();
            }
        }
    }

}
