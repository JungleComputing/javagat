package org.gridlab.gat.io.cpi.sftpTrilead;

import java.io.IOException;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;

public class SftpTrileadFileInputStreamAdaptor extends FileInputStreamCpi {
    
    protected static Logger logger = LoggerFactory.getLogger(SftpTrileadFileInputStreamAdaptor.class);
    
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileInputStreamCpi
                .getSupportedCapabilities();
        capabilities.put("available", true);
        capabilities.put("close", true);
        capabilities.put("mark", true);
        capabilities.put("markSupported", true);
        capabilities.put("read", true);
        capabilities.put("reset", true);
        capabilities.put("skip", true);

        return capabilities;
    }
    
    public static String getDescription() {
        return "The SftpTrilead FileInputStream Adaptor implements the FileInputStream object using the trilead ssh library.";
    }
    
    public static Preferences getSupportedPreferences() {
        Preferences preferences = FileInputStreamCpi.getSupportedPreferences();

        preferences.put("sftptrilead.strictHostKeyChecking", "false");
        preferences.put("sftptrilead.noHostKeyChecking", "true");
        preferences.put("sftptrilead.cipher.client2server", "<sftp default>");
        preferences.put("sftptrilead.cipher.server2client", "<sftp default>");
        preferences.put("sftptrilead.tcp.nodelay", "true");
        return preferences;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "sftptrilead", "sftp", "file"};
    }
        
    private final SftpTrileadConnection connection;
    
    private final SFTPv3FileHandle handle;
    
    private long filesize;

    private long available;
    
    private long currpos = 0L;
    
    private boolean closed = false;
    
    public SftpTrileadFileInputStreamAdaptor(GATContext gatContext, URI location)
        throws GATObjectCreationException {
                
        super(gatContext, location);

//      We don't have to handle the local case, the GAT engine will select
//      the local adaptor.
        if (location.refersToLocalHost()) {
            throw new GATObjectCreationException(
            "this adaptor cannot read local files");
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
            connection = SftpTrileadFileAdaptor.getConnection(gatContext, location, verifier,
                    client2serverCiphers, server2clientCiphers, tcpNoDelay);
        } catch (GATInvocationException e) {
            logger.debug("Could not create connection", e);
            throw new GATObjectCreationException("Could not create connection", e);
        }
        
        try {
            handle = connection.sftpClient.openFileRO(location.getPath());
        } catch (IOException e) {
            logger.debug("Could not open file", e);
            try {
                SftpTrileadFileAdaptor.closeConnection(connection, gatContext.getPreferences());
            } catch (Throwable e1) {
                // ignored
            }   
            throw new GATObjectCreationException("Could not open file", e);
        }
        
        SFTPv3FileAttributes attributes = null;
        try {
            attributes = connection.sftpClient.fstat(handle);
        } catch (Throwable e) {
            logger.debug("Could not get file attributes", e);
            try {
                connection.sftpClient.closeFile(handle);
            } catch (Throwable e1) {
                // ignored
            }
            try {
                SftpTrileadFileAdaptor.closeConnection(connection, gatContext.getPreferences());
            } catch (Throwable e1) {
                // ignored
            }   
            throw new GATObjectCreationException("Could not get file attributes", e);
        }
        
        filesize = attributes.size;
        available = filesize;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#available()
     */
    public int available() throws GATInvocationException {
        return (int) available;
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
     * @see java.io.InputStream#read()
     */
    public int read() throws GATInvocationException {
        byte[] b = new byte[1];
        int count = read(b, 0, 1);
        if (count < 0) {
            return -1;
        }
        return b[0] & 0377;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int offset, int len)
            throws GATInvocationException {
        if (closed) {
            throw new GATInvocationException("SftpTrileadFileInputStreamAdaptor: file already closed");
        }
        int cnt = 0;
        while (len > 0) {
            int sz = len > 32768 ? 32768 : len;
            try {
                sz = connection.sftpClient.read(handle, currpos, b, offset, sz);
            } catch (Throwable e) {
                logger.debug("SftpTrileadFileInputStreamAdaptor.read", e);
                throw new GATInvocationException("SftpTrileadFileInputStreamAdaptor", e);
            }
            if (sz < 0) {
                return cnt;
            }
            currpos += sz;
            offset += sz;
            available -= sz;
            len -= sz;
            cnt += sz;
        }
        return cnt;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] b)
            throws GATInvocationException {
        return read(b, 0, b.length);
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long arg0) throws GATInvocationException {
        if (closed) {
            throw new GATInvocationException("SftpTrileadFileInputStreamAdaptor: file already closed");
        }
        if (arg0 >= 0L && currpos < filesize) {
            long max = filesize - currpos;
            if (arg0 > max) {
                arg0 = max;
            }
            currpos += arg0;
            available -= arg0;
            return arg0;
        }
        return 0L;
    }

}
