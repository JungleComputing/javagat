package org.gridlab.gat.io.cpi.sshtrilead;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

public class SshTrileadFileOutputStreamAdaptor extends FileOutputStreamCpi {
    
    protected static Logger logger = LoggerFactory.getLogger(SshTrileadFileOutputStreamAdaptor.class);

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileOutputStreamCpi
                .getSupportedCapabilities();
        
        capabilities.put("close", true);
        capabilities.put("flush", true);
        capabilities.put("write", true);

        return capabilities;
    }
    
    public static String getDescription() {
        return "The SshTrilead FileOutputStream Adaptor implements the FileOutputStream object using the trilead ssh library. Trilead ssh is an open source full java ssh library. On the server side, the 'scp' program must be in the PATH. Connections with a remote ssh server can be made by using the username + password, username + keyfile, or with only a username, depending on the client and server settings.";
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = FileOutputStreamCpi.getSupportedPreferences();
        
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
        preferences.put("sshtrilead.connect.timeout", "5000");
        preferences.put("sshtrilead.kex.timeout", "5000");
        
        // Added: preferences for hostkey checking. Defaults are what used to be ....
        preferences.put("sshtrilead.strictHostKeyChecking", "false");
        preferences.put("sshtrilead.noHostKeyChecking", "true");
        
        preferences.put("file.chmod", SshTrileadFileAdaptor.DEFAULT_MODE);

        preferences.put("sftptrilead.strictHostKeyChecking", "false");
        preferences.put("sftptrilead.noHostKeyChecking", "true");
        return preferences;
    }
    
    public static String[] getSupportedSchemes() {
        return SshTrileadFileAdaptor.getSupportedSchemes();
    }
    
    private SshTrileadFileAdaptor file;
    
    private Session session;
    
    private OutputStream sessionInputStream;
    
    private OutputStreamRunner job;
    
    public SshTrileadFileOutputStreamAdaptor(GATContext gatContext,
            URI location, Boolean append) throws GATObjectCreationException,
                    GATInvocationException {
        super(gatContext, location, append);
        
        if (location.isCompatible("file") && location.refersToLocalHost()) {
            throw new GATObjectCreationException("this adaptor cannot write local files");
        }
        
        file = new SshTrileadFileAdaptor(gatContext, location);
        
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new GATObjectCreationException("cannot write to directory");
            }
            if (! file.canWrite()) {
                throw new GATObjectCreationException("cannot write file");
            }
        } else {
            if (! file.createNewFile()) {
                throw new GATObjectCreationException("cannot create file");
            }
        }

        try {
            if(logger.isInfoEnabled()) {
        	logger.info("SshTrileadFileOutputStreamAdaptor: closing session");
            }
            session = file.getSession();
        } catch(Throwable e) {
            throw new GATObjectCreationException("Could not create stream", e);
        }
        sessionInputStream = session.getStdin();
        String command = "cat " + (append ? ">>" : ">")
            + SshTrileadFileAdaptor.protectAgainstShellMetas(file.getFixedPath());
        job = new OutputStreamRunner(command);
        job.setDaemon(true);
        job.start();
    }

    public void close() throws GATInvocationException {
        if (session != null) {
            try {
                sessionInputStream.close();
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

    public void flush() throws GATInvocationException {
        if (session == null) {
            throw new GATInvocationException("SshTrileadFileOutputStreamAdaptor: file already closed");
        }
        try {
            sessionInputStream.flush();
        } catch (IOException e) {
            throw new GATInvocationException("flush gave exception", e);
        }
    }

    public void write(byte[] arg0, int arg1, int arg2)
            throws GATInvocationException {
        if (session == null) {
            throw new GATInvocationException("SshTrileadFileOutputStreamAdaptor: file already closed");
        }  
        try {
            sessionInputStream.write(arg0, arg1, arg2);
        } catch (IOException e) {
            throw new GATInvocationException("flush gave exception", e);
        }
    }

    public void write(byte[] arg0) throws GATInvocationException {
        if (session == null) {
            throw new GATInvocationException("SshTrileadFileOutputStreamAdaptor: file already closed");
        }
        try {
            sessionInputStream.write(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("flush gave exception", e);
        }
    }

    public void write(int arg0) throws GATInvocationException {
        if (session == null) {
            throw new GATInvocationException("SshTrileadFileOutputStreamAdaptor: file already closed");
        }
        try {
            sessionInputStream.write(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("flush gave exception", e);
        }
    }
    
    private class OutputStreamRunner extends Thread {
        private final String command;
        
        public OutputStreamRunner(String command) {
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
                InputStream stdout = new StreamGobbler(session.getStdout());
                InputStream stderr = new StreamGobbler(session.getStderr());
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

                StringBuffer out = new StringBuffer();
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    out.append(line);
                    out.append("\n");
                }
                br.close();
                result[SshTrileadFileAdaptor.STDOUT] = out.toString();
                br = new BufferedReader(new InputStreamReader(stderr));
                StringBuffer err = new StringBuffer();
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    err.append(line);
                    err.append("\n");
                }
                br.close();
                result[SshTrileadFileAdaptor.STDERR] = err.toString();
                
                // Sometimes hangs here??? Added count. --Ceriel
                int sleepcount = 0;
                Integer exitValue = session.getExitStatus();
                while (sleepcount < 5 && exitValue == null) {
                    Thread.sleep(100);
                    exitValue = session.getExitStatus();
                    sleepcount++;
                }
                if (exitValue == null) {
            	// No exit status available; Assume 0.
                    result[SshTrileadFileAdaptor.EXIT_VALUE] = "0";
                } else {
                    result[SshTrileadFileAdaptor.EXIT_VALUE] = "" + exitValue;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("STDOUT: " + result[SshTrileadFileAdaptor.STDOUT]);
                    logger.debug("STDERR: " + result[SshTrileadFileAdaptor.STDERR]);
                    logger.debug("EXIT:   " + result[SshTrileadFileAdaptor.EXIT_VALUE]);
                }
                return result;
            } finally {
        	if(logger.isInfoEnabled()) {
        	    logger.info("SshTrileadFileOutputStreamAdaptor: closing session");
        	}
                session.close();
            }
        }
    }
}
