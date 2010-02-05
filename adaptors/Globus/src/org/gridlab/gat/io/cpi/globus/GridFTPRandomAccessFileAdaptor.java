package org.gridlab.gat.io.cpi.globus;

import java.io.IOException;

import org.globus.ftp.Buffer;
import org.globus.ftp.DataSink;
import org.globus.ftp.DataSource;
import org.globus.ftp.GridFTPClient;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridFTPRandomAccessFileAdaptor extends RandomAccessFileCpi {
    
    public static Preferences getSupportedPreferences() {
        Preferences p = RandomAccessFileCpi.getSupportedPreferences();
        p.put("ftp.connection.passive", "false");
        p.put("ftp.server.old", "false");
        p.put("ftp.server.noauthentication", "false");
        p.put("file.chmod", "<default is target umask>");
        return p;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "globus", "gsiftp", "file", ""};
    }
    
    protected static Logger logger = LoggerFactory.getLogger(GridFTPRandomAccessFileAdaptor.class);
    
    private GridFTPClient ftpClient;
    
    private long currentPos;
    
    private long size;
    
    private boolean closed = false;
    
    private final boolean readOnly; 
    
    private final String path;
    
    public GridFTPRandomAccessFileAdaptor(GATContext gatContext, URI location,
            String mode) throws GATObjectCreationException, GATInvocationException {
        super(gatContext, location, mode);
        
        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        if (location.refersToLocalHost()) {
            throw new GATObjectCreationException(
            "this adaptor cannot deal with local files");
        }
        
        try {
            ftpClient = (GridFTPClient) GridFTPFileAdaptor.doWorkCreateClient(gatContext, null, location);
        } catch (InvalidUsernameOrPasswordException e) {
            throw new GATObjectCreationException("Could not create ftp client");
        }
        
        path = location.getPath();
        
        try {
            boolean exists = ftpClient.exists(path);
            if ("r".equals(mode)) {
                if (! exists) {
                    throw new GATInvocationException("file does not exist");
                }
                readOnly = true;
                size = ftpClient.getSize(path);
            } else if ("rw".equals(mode) || "rws".equals(mode) || "rwd".equals(mode)) {
                if (! exists) {
                    GlobusFileAdaptor.setActiveOrPassive(ftpClient, gatContext.getPreferences());
                    ftpClient.put(path, GlobusFileAdaptor.emptySource, null);
                    if (gatContext.getPreferences().containsKey("file.chmod")) {
                        GlobusFileAdaptor.chmod(ftpClient, path, gatContext);
                    }                   
                    size = 0;
                } else {
                    size = ftpClient.getSize(path);
                }
                readOnly = false;
            } else {
                throw new GATObjectCreationException("Illegal mode: " + mode);
            }
        } catch (Throwable e) {
            logger.debug("Could not open file", e);
            try {
                GridFTPFileAdaptor.doWorkDestroyClient(ftpClient, location, gatContext.getPreferences());
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
            GridFTPFileAdaptor.doWorkDestroyClient(ftpClient, location, gatContext.getPreferences());
        } catch (Throwable e1) {
            logger.debug("GlobusRandomAccessFileAdaptor.close()", e1);
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
        
        if (currentPos >= size) {
            return -1;
        }
        if (len > size - currentPos) {
            len = (int)(size - currentPos);
        } 
           
        MyDataSink sink = new MyDataSink(buf, off, len, currentPos);
        try {
            GlobusFileAdaptor.setActiveOrPassive(ftpClient, gatContext.getPreferences());
            ftpClient.extendedGet(path, currentPos, len, sink, null);
        } catch(Throwable e) {
            throw new GATInvocationException("read failed", e);
        }
        len = sink.getSize();
        currentPos += len;
        return len;
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
        
        MyDataSource source = new MyDataSource(buf, off, len, currentPos);
        try {
            GlobusFileAdaptor.setActiveOrPassive(ftpClient, gatContext.getPreferences());
            ftpClient.extendedPut(path, currentPos, source, null);
        } catch(Throwable e) {
            throw new GATInvocationException("read failed", e);
        }
        currentPos += len;     
        if (currentPos > size) {
            size = currentPos;
        }
    }
    
    private static class MyDataSink implements DataSink {
        
        byte[] buf;
        int off;
        int len;
        int writtenLen = 0;
        long fileOffset;
        
        MyDataSink(byte[] buf, int off, int len, long fileOffset) {
            this.buf = buf;
            this.off = off;
            this.len = len;
            this.fileOffset = fileOffset;
        }

        public void close() throws IOException {
            // No resources to release.
        }

        public void write(Buffer buffer) throws IOException {
            if (logger.isDebugEnabled()) {
                
            }
            byte[] b = buffer.getBuffer();
            int l = buffer.getLength();
            long o = buffer.getOffset();
            if (logger.isDebugEnabled()) {
                logger.debug("Read buffer, len = " + l + ", fileOffset = " + o);
            }
            System.arraycopy(b, 0, buf, (int)(o - fileOffset), l);
            synchronized(this) {
                writtenLen += l;
            }
        }
        
        int getSize() {
            return writtenLen;
        }
    }
    
    private static class MyDataSource implements DataSource {

        Buffer buffer;
        
        MyDataSource(byte[] buf, int off, int len, long fileOffset) {
            if (off != 0) {
                byte[] b = new byte[len];
                System.arraycopy(buf, off, b, 0, len);
                buf = b;
            }
            buffer = new Buffer(buf, len, fileOffset);
        }
        
        public void close() throws IOException {
            // No resources to remove.
        }

        public synchronized Buffer read() throws IOException {
            Buffer b = buffer;
            buffer = null;
            return b;
        }
    }
}
