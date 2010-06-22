package org.gridlab.gat.io.cpi.sftpTrilead;

import java.io.IOException;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;

public class SftpTrileadRandomAccessFileAdaptor extends RandomAccessFileCpi {

    public static Preferences getSupportedPreferences() {
        Preferences preferences = RandomAccessFileCpi.getSupportedPreferences();

        preferences.put("sftptrilead.strictHostKeyChecking", "false");
        preferences.put("sftptrilead.noHostKeyChecking", "true");
        preferences.put("sftptrilead.cipher.client2server", "<sftp default>");
        preferences.put("sftptrilead.cipher.server2client", "<sftp default>");
        preferences.put("sftptrilead.tcp.nodelay", "true");
        return preferences;
    }
    
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = RandomAccessFileCpi
                .getSupportedCapabilities();
        capabilities.put("close", true);
        capabilities.put("getFilePointer", true);
        capabilities.put("length", true);
        capabilities.put("read", true);
        capabilities.put("seek", true);
        capabilities.put("skipBytes", true);
        capabilities.put("write", true);
        return capabilities;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "sftptrilead", "sftp", "file"};
    }
    
    protected static Logger logger = LoggerFactory
            .getLogger(SftpTrileadRandomAccessFileAdaptor.class);

    private final SftpTrileadConnection connection;
    
    private final SFTPv3FileHandle handle;
    
    private long currentPos;
    
    private long size;
    
    private boolean closed = false;
    
    private final boolean readOnly; 
    
    public SftpTrileadRandomAccessFileAdaptor(GATContext gatContext, URI location,
            String mode) throws GATObjectCreationException {
        super(gatContext, location, mode);
        
//      We don't have to handle the local case, the GAT engine will select
//      the local adaptor.
        if (location.refersToLocalHost()) {
            throw new GATObjectCreationException(
            "this adaptor cannot deal with local files");
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
            SFTPv3FileAttributes attributes = null;
            try {
                attributes = connection.sftpClient.stat(location.getPath());
            } catch (Throwable e) {
                logger.debug("Could not get file attributes", e);
            }
            if ("r".equals(mode)) {                
                handle = connection.sftpClient.openFileRO(location.getPath());
                size = attributes.size;
                readOnly = true;
            } else if ("rw".equals(mode) || "rws".equals(mode) || "rwd".equals(mode)) {
                if (attributes == null) {
                    handle = connection.sftpClient.createFile(location.getPath());
                    size = 0;
                } else {
                    size = attributes.size;
                    handle = connection.sftpClient.openFileRW(location.getPath());
                }
                readOnly = false;
            } else {
                throw new GATObjectCreationException("Illegal mode: " + mode);
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
        currentPos = 0;
    }
    
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

    public long getFilePointer() {
        return currentPos;
    }
    
    public long length() throws GATInvocationException {
        return size;
    }
    
    public void seek(long arg0) throws GATInvocationException {
        if (closed) {
            throw new GATInvocationException("seek: file is closed");
        }
        if (arg0 < 0) {
            throw new GATInvocationException("seek: arg < 0");
        }
        currentPos = arg0;
    }
    
    public int read(byte[] buf, int off, int len) throws GATInvocationException {
        if (closed) {
            throw new GATInvocationException("read: file is closed");
        }
        if (len == 0) {
            return 0;
        }
        if (len < 0 || off + len > buf.length) {
            throw new IndexOutOfBoundsException("read: illegal parameters");
        }
        try {
            int total = 0;
            while (len != 0) {
                int sz = Math.min(len, 32768);
                int length = connection.sftpClient.read(handle, currentPos, buf, off, sz);
                if (length < 0) {
                    if (total == 0) {
                        return -1;
                    }
                    return total;
                }
                len -= length;
                off += length;
                currentPos += length;
                total += length;
            }           
            return total;
        } catch(Throwable e) {
            throw new GATInvocationException("SftpTrileadRandomAccessFile", e);
        }
    }
    
    public int skipBytes(int len) throws GATInvocationException {
        if (closed) {
            throw new GATInvocationException("skipBytes: file is closed");
        }
        if (len <= 0) {
            return 0;
        }
        if (currentPos + len > size) {
            if (currentPos >= size) {
                return 0;
            }
            len = (int)(size - currentPos);
            currentPos = size;
            return len;
        }
        currentPos += len;
        return len;
    }
       
    public void write(byte[] buf, int off, int len) throws GATInvocationException {
        if (closed) {
            throw new GATInvocationException("write: file is closed");
        }
        if (readOnly) {
            throw new GATInvocationException("write: file is opened read-only");
        }
        if (len == 0) {
            return;
        }
        if (len < 0 || off + len > buf.length) {
            throw new IndexOutOfBoundsException("write: illegal parameters");
        }
        try {
            connection.sftpClient.write(handle, currentPos, buf, off, len);
            currentPos += len;
            if (currentPos > size) {
                size = currentPos;
            }
        } catch(IOException e) {
            throw new GATInvocationException("write gave exception", e);
        }
    }
}
