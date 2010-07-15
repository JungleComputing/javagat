package org.gridlab.gat.io.cpi.sftpTrilead;

import java.io.IOException;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;

public class SftpTrileadFileOutputStreamAdaptor extends FileOutputStreamCpi {
    
    protected static Logger logger = LoggerFactory.getLogger(SftpTrileadFileOutputStreamAdaptor.class);
    
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileOutputStreamCpi
                .getSupportedCapabilities();
        
        capabilities.put("close", true);
        capabilities.put("flush", true);
        capabilities.put("write", true);

        return capabilities;
    }
    
    public static String getDescription() {
        return "The SftpTrilead FileOutputStream Adaptor implements the FileOutputStream object using the trilead ssh library.";
    }
    
    public static Preferences getSupportedPreferences() {
        Preferences preferences = FileOutputStreamCpi.getSupportedPreferences();

        preferences.put("sftptrilead.strictHostKeyChecking", "false");
        preferences.put("sftptrilead.noHostKeyChecking", "true");
        preferences.put("sftptrilead.cipher.client2server", "<sftp default>");
        preferences.put("sftptrilead.cipher.server2client", "<sftp default>");
        preferences.put("sftptrilead.tcp.nodelay", "true");
        preferences.put("sftptrilead.connect.timeout", "5000");
        preferences.put("sftptrilead.kex.timeout", "5000");
        return preferences;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "sftptrilead", "sftp", "file"};
    }
    
    private final SftpTrileadConnection connection;
    
    private final SFTPv3FileHandle handle;
    
    private long currpos = 0L;
    
    private boolean closed = false;
    
    public SftpTrileadFileOutputStreamAdaptor(GATContext gatContext, URI location, Boolean append)
        throws GATObjectCreationException {
                
        super(gatContext, location, append);
       
//      We don't have to handle the local case, the GAT engine will select
//      the local adaptor.
        if (location.refersToLocalHost()) {
            throw new GATObjectCreationException(
            "this adaptor cannot write local files");
        }
        
        Preferences p = gatContext.getPreferences();
        boolean noHostKeyChecking = ((String) p.get("sftptrilead.noHostKeyChecking", "true"))
                .equalsIgnoreCase("true");
        boolean strictHostKeyChecking = ((String) p.get("sftptrilead.strictHostKeyChecking", "false"))
                .equalsIgnoreCase("true");
        String client2serverCipherString = (String) p.get("sftptrilead.cipher.client2server");
        String[] client2serverCiphers = client2serverCipherString == null ? null 
                : client2serverCipherString.split(",");
        String server2clientCipherString = (String) p.get("sftptrilead.cipher.server2client");
        String[] server2clientCiphers = server2clientCipherString == null ? null
                : server2clientCipherString.split(",");               
        boolean tcpNoDelay = ((String) p.get("sftptrilead.tcp.nodelay", "true"))
                .equalsIgnoreCase("true");
        
        SftpTrileadHostVerifier verifier = new SftpTrileadHostVerifier(false, strictHostKeyChecking, noHostKeyChecking);
        
        try {
            connection = SftpTrileadFileAdaptor.getConnection(gatContext, location, verifier, client2serverCiphers, server2clientCiphers, tcpNoDelay);
        } catch (GATInvocationException e) {
            logger.debug("Could not create connection", e);
            throw new GATObjectCreationException("Could not create connection", e);
        }
        
        SFTPv3FileAttributes attributes = null;
        try {
            attributes = connection.sftpClient.stat(location.getPath());
        } catch (Throwable e) {
            logger.debug("Could not get file attributes", e);
        }
        
        try {
            if (append && attributes != null) {
                handle = connection.sftpClient.openFileRW(location.getPath());
                currpos = attributes.size;
            } else {
                handle = connection.sftpClient.createFileTruncate(location.getPath());
            }
        } catch (IOException e) {
            logger.debug("Could not open file", e);
            try {
                SftpTrileadFileAdaptor.closeConnection(connection, gatContext.getPreferences());
            } catch (Throwable e1) {
                // ignored
            }   
            throw new GATObjectCreationException("Could not open file", e);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#close()
     */
    public void close() throws GATInvocationException {
        closed = true;
        try {
            connection.sftpClient.closeFile(handle);
        } catch (Throwable e1) {
            logger.debug("sftpClient.closeFile: ", e1);
            // ignored
        }
        try {
            SftpTrileadFileAdaptor.closeConnection(connection, gatContext.getPreferences());
        } catch (Throwable e) {
            logger.debug("SftpTrileadFileAdaptor.closeConnection: ", e);
            // ignored
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws GATInvocationException {
        // nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte[] b, int offset, int len)
            throws GATInvocationException {
        if (closed) {
            throw new GATInvocationException("SftpTrileadFileOutputStreamAdaptor: file already closed");
        }
        try {
            while (len > 0) {
                int sz = len > 32768 ? 32768 : len;
                connection.sftpClient.write(handle, currpos, b, offset, sz);
                len -= sz;
                offset += sz;
                currpos += sz;
            }
        } catch (IOException e) {
            throw new GATInvocationException("SftpTrileadFileOutputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte[] arg0) throws GATInvocationException {
        write(arg0, 0, arg0.length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(int)
     */
    public void write(int arg0) throws GATInvocationException {
        byte[] b = new byte[1];
        b[0] = (byte) arg0;
        write(b, 0, 1);
    }

}
