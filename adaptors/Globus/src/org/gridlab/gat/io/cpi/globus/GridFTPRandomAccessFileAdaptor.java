package org.gridlab.gat.io.cpi.globus;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.globus.ftp.Buffer;
import org.globus.ftp.DataSink;
import org.globus.ftp.DataSource;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.RetrieveOptions;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridFTPRandomAccessFileAdaptor extends RandomAccessFileCpi {
    
    public static Preferences getSupportedPreferences() {
        Preferences p = RandomAccessFileCpi.getSupportedPreferences();
        p.put("ftp.connection.protection", "<default taken from globus>");
        p.put("ftp.server.old", "false");
        p.put("ftp.server.noauthentication", "false");
        p.put("file.chmod", "<default is target umask>");
        p.put("gridftp.authentication.retry", "0");
        p.put("ftp.clientwaitinterval", "" + GlobusFileAdaptor.DEFAULT_WAIT_INTERVAL);
        return p;
    }
    
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
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
        return new String[] { "globus", "gsiftp", "file", ""};
    }
    
    protected static Logger logger = LoggerFactory.getLogger(GridFTPRandomAccessFileAdaptor.class);
    
    private static final int NEW = 0;
    
    private static final int CLOSED = 1;
    
    private static final int READING = 2;
    
    private static final int WRITING = 3;
    
    private static final int SEEKING = 4;
    
    private int state = NEW;
    
    private GridFTPClient writeFtpClient = null;
    
    private GridFTPClient readFtpClient = null;
    
    private long currentPos;
    
    private long size;
    
    private final boolean readOnly; 
    
    private final String path;
    
    private Preferences writePrefs = null;
    
    private Preferences readPrefs = null;
    
    private DataWriter dataWriter = null;
    
    public GridFTPRandomAccessFileAdaptor(GATContext gatContext, URI location,
            String mode) throws GATObjectCreationException, GATInvocationException {
        super(gatContext, location, mode);
        
        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        if (location.refersToLocalHost()) {
            throw new GATObjectCreationException(
            "this adaptor cannot deal with local files");
        }
        
        readPrefs = gatContext.getPreferences();
        readPrefs.put("gridftp.mode", "eblock");
        readPrefs.put("ftp.connection.passive", "false");
    
        try {
            readFtpClient = GridFTPFileAdaptor.doWorkCreateClient(gatContext, readPrefs, location);
            readFtpClient.setMode(GridFTPSession.MODE_EBLOCK);
            readFtpClient.setType(GridFTPSession.TYPE_IMAGE);
            readFtpClient.setOptions(new RetrieveOptions(1));
            GlobusFileAdaptor.setActiveOrPassive(readFtpClient, readPrefs);
        } catch(GATInvocationException e) {
            throw e;
        } catch(Throwable e) {
            throw new GATObjectCreationException("Could not create ftp client");
        }
        
        path = location.getPath();
        
        try {
            boolean exists = readFtpClient.exists(path);
            if ("r".equals(mode)) {
                if (! exists) {
                    throw new GATInvocationException("file does not exist");
                }
                readOnly = true;
                size = readFtpClient.getSize(path);
            } else if ("rw".equals(mode) || "rws".equals(mode) || "rwd".equals(mode)) {
                
                writePrefs = gatContext.getPreferences();
                writePrefs.put("gridftp.mode", "eblock");
                writePrefs.put("ftp.connection.passive", "true");
                
                writeFtpClient = GridFTPFileAdaptor.doWorkCreateClient(gatContext, writePrefs, location);
                
                if (! exists) {
                    GlobusFileAdaptor.setActiveOrPassive(writeFtpClient, writePrefs);
                    writeFtpClient.put(path, GlobusFileAdaptor.emptySource, null);
                    if (gatContext.getPreferences().containsKey("file.chmod")) {
                        GlobusFileAdaptor.chmod(writeFtpClient, path, gatContext);
                    }                   
                    size = 0;
                } else {
                    size = writeFtpClient.getSize(path);
                }
                writeFtpClient.setMode(GridFTPSession.MODE_EBLOCK);
                writeFtpClient.setType(GridFTPSession.TYPE_IMAGE);
                writeFtpClient.setOptions(new RetrieveOptions(1));
                GlobusFileAdaptor.setActiveOrPassive(writeFtpClient, writePrefs);
                readOnly = false;
            } else {
                throw new GATObjectCreationException("Illegal mode: " + mode);
            }
        } catch (Throwable e) {
            logger.debug("Could not open file", e);
            close();
            throw new GATObjectCreationException("Could not open file", e);
        }
        currentPos = 0;
    }
    
    public void close() {
        if (state == WRITING) {
            dataWriter.finish();
        }
        state = CLOSED;
        try {
            if (writeFtpClient != null) {
                GridFTPFileAdaptor.doWorkDestroyClient(writeFtpClient, location, writePrefs);
            }
        } catch (Throwable e) {
            logger.debug("GlobusRandomAccessFileAdaptor.close()", e);
            // ignored
        }
        try {
            GridFTPFileAdaptor.doWorkDestroyClient(readFtpClient, location, readPrefs);
        } catch (Throwable e) {
            logger.debug("GlobusRandomAccessFileAdaptor.close()", e);
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
        if (state == CLOSED) {
            throw new GATInvocationException("seek: file is closed");
        }
        if (arg0 < 0) {
            throw new GATInvocationException("seek: arg < 0");
        }
        if (state == WRITING) {
            dataWriter.finish();
        }
        state = SEEKING;
        currentPos = arg0;
    }
    
    public int read(byte[] buf, int off, int len) throws GATInvocationException {
        if (state == CLOSED) {
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
        if (state == WRITING) {
            dataWriter.finish();
        }
        state = READING;
        MyDataSink sink = new MyDataSink(buf, off, len);
        if (logger.isDebugEnabled()) {
            logger.debug("Doing extendedGet ...");
        }
        try {
            // GlobusFileAdaptor.setActiveOrPassive(readFtpClient, readPrefs);
            readFtpClient.extendedGet(path, currentPos, len, sink, null);
        } catch(Throwable e) {
            throw new GATInvocationException("read failed", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("ExtendedGet done");
        }
        len = sink.getSize();
        currentPos += len;
        return len;
    }
    
    public int skipBytes(int len) throws GATInvocationException {
        if (state == CLOSED) {
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
        }
        if (state == WRITING) {
            dataWriter.finish();
        }
        state = SEEKING;
        currentPos += len;
        return len;
    }
       
    public void write(byte[] buf, int off, int len) throws GATInvocationException {
        if (state == CLOSED) {
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
        if (state != WRITING) {
            dataWriter = new DataWriter(writeFtpClient, currentPos, path);
        }
        state = WRITING;
        dataWriter.add(buf, off, len);
        Throwable e = dataWriter.waitForEmptyList();
        if (e != null) {
            dataWriter.finish();
            throw new GATInvocationException("write failed", e);
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
        
        MyDataSink(byte[] buf, int off, int len) {
            this.buf = buf;
            this.off = off;
            this.len = len;
        }

        public void close() throws IOException {
            // No resources to release.
        }

        public void write(Buffer buffer) throws IOException {
            byte[] b = buffer.getBuffer();
            int l = buffer.getLength();
            long o = buffer.getOffset();
            if (logger.isDebugEnabled()) {
                logger.debug("Read buffer, b.length = " + b.length + ", l = " + l + ", o = " + o);
                logger.debug("buf.length = " + buf.length + ", off = " + off
                        + ", len = " + len);
            }
            System.arraycopy(b, 0, buf, off + (int) o, l);
            synchronized(this) {
                writtenLen += l;
            }
        }
        
        int getSize() {
            return writtenLen;
        }
    }
  
    
    private static class DataWriter implements DataSource, Runnable {

        private LinkedList<Buffer> list = new LinkedList<Buffer>();
        
        private final GridFTPClient client;
        
        private long offset = 0;
        
        private final long baseOffset;

        private boolean finished = false;
        
        private boolean done = false;
        
        private final String fileName;
        
        private Throwable exception = null;
        
        private boolean readerWaiting = false;
        
        public DataWriter(GridFTPClient client, long baseOffset, String fileName) {
            this.client = client;
            this.baseOffset = baseOffset;
            this.fileName = fileName;
            Thread t = new Thread(this);
            t.setDaemon(true);
            t.start();
        }

        public void add(byte[] buf, int off, int len) {

            // Copy, because we don't know when we can touch the buffer again.
            byte[] b = new byte[len];
            System.arraycopy(buf, off, b, 0, len);

            synchronized(this) {
                Buffer buffer = new Buffer(b, len, offset);
                offset += len;
                list.add(buffer);
                notifyAll();
            }
        }
        
        public synchronized Throwable waitForEmptyList() {
            // Wait until client is ready to read the next buffer. Can we get
            // closer to actually knowing that the buffer is written?
            while (! finished && ! readerWaiting) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // ignored
                }
            }
            return getException();
        }
        
        public synchronized void finish() {
            finished = true;
            notifyAll();
            while (! done) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // ignored
                }
            }
        }

        public void close() {
        }

        public synchronized Buffer read() throws IOException {
            while (! finished && list.isEmpty()) {
                readerWaiting = true;
                notifyAll();
                try {
                    wait();
                } catch(InterruptedException e) {
                    // ignored
                }
                readerWaiting = false;
            }
            if (! list.isEmpty()) {
                return list.remove();
            }
            return null;
        }
        
        public synchronized Throwable getException() {
            return exception;
        }

        public void run() {
            try {
                client.extendedPut(fileName, baseOffset, this, null);
            } catch(Throwable e) {
                synchronized(this) {
                    exception = e;
                }
            }
            synchronized(this) {
                done = true;
                notifyAll();
            }
        }
    }
}
