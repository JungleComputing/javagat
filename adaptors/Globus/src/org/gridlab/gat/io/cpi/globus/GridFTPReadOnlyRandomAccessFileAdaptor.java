package org.gridlab.gat.io.cpi.globus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.globus.ftp.Buffer;
import org.globus.ftp.DataSink;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The adaptor provides read only random access to a file.
 * @author Bastian Boegel, University of Ulm, Germany, 2011
 *
 */
public class GridFTPReadOnlyRandomAccessFileAdaptor extends RandomAccessFileCpi {

	protected static Logger logger = LoggerFactory.getLogger(GridFTPReadOnlyRandomAccessFileAdaptor.class);
	
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
        capabilities.put("toURI", true);
        capabilities.put("getFile", true);
        capabilities.put("close", false);
        capabilities.put("getFilePointer", false);
        capabilities.put("length", false);
        capabilities.put("read", false);
        capabilities.put("seek", false);
        capabilities.put("setLength", false);
        capabilities.put("skipBytes", false);
        capabilities.put("write", false);
        return capabilities;
    }

    /**
     * The {@link GridFTPClient} for the communication.
     */
	private GridFTPClient client = null;
	
	/**
	 * Current position in the file.
	 */
	private long currentPos = 0;
	
	/**
	 * Size of the file.
	 */
	private long fileSize = 0;
	
	/**
	 * The path of the file.
	 */
	private String path;
	
	/**
	 * Indicates, if the stream has been closed.
	 */
	boolean isClosed = false;
	
	/**
	 * Constructor.
	 * @param gatContext The {@link GATContext} with the credential.
	 * @param location The location containing resource, port and path.
	 * @param mode The mode (only "r" is supported).
	 * @throws GATInvocationException
	 */
	public GridFTPReadOnlyRandomAccessFileAdaptor(GATContext gatContext, URI location, String mode) throws GATInvocationException {
		super(gatContext, location, mode);
		if (gatContext == null) {
			throw new GATInvocationException("Missing GATContext!");
		}
		if (location == null) {
			throw new GATInvocationException("Missing location!");
		}
		if ((mode == null) || (mode.isEmpty())) {
			throw new GATInvocationException("Missing mode");
		}
		if (!mode.equals("r")) {
			throw new GATInvocationException("The GridFTPReadOnlyRandomAccessFileAdaptor can only be used in read mode (r)!");
		}
		path = location.getPath();
		if ((path == null) || (path.isEmpty())) {
			throw new GATInvocationException("No path given.");
		}
        
		Preferences readPrefs = gatContext.getPreferences();
        readPrefs.put("gridftp.mode", "eblock");
        readPrefs.put("ftp.connection.passive", "true");

		try {
			client = GridFTPFileAdaptor.doWorkCreateClient(gatContext, readPrefs, location);
			client.setType(GridFTPSession.TYPE_IMAGE);
			client.setPassive();
			client.setLocalActive();
		} catch (Exception e) {
			logger.error("Error creating GridFTPClient!", e);
			throw new GATInvocationException("Server error", e);
		}
		try {
			if (!client.exists(path)) {
				throw new GATInvocationException("Path does not exist!");
			}
			fileSize = client.getSize(path);
			if (fileSize <= 0) {
				throw new GATInvocationException("File is empty");
			}
		} catch (Exception e) {
			logger.error("Error checking file!", e);
			throw new GATInvocationException("Error checking file!", e);
		}
	} // public GridFTPReadOnlyRandomAccessFileAdaptor(GATContext gatContext, URI location, String mode) throws GATInvocationException

	/**
	 * Closes the GridFTPClient.
	 * @see org.gridlab.gat.io.cpi.RandomAccessFileCpi#close()
	 */
	@Override
	public void close() throws GATInvocationException {
		isClosed = true;
		if (client != null) {
			GridFTPFileAdaptor.doDestroyClient(gatContext, client, location, null);
		}
	} // public void close() throws GATInvocationException
	
	/**
	 * Returns the current position in the remote file.
	 * @see org.gridlab.gat.io.cpi.RandomAccessFileCpi#getFilePointer()
	 */
	@Override
	public long getFilePointer() throws GATInvocationException {
		return currentPos;
	} // public long getFilePointer() throws GATInvocationException
	
	/**
	 * Returns the file size.
	 * @see org.gridlab.gat.io.cpi.RandomAccessFileCpi#length()
	 */
	@Override
	public long length() throws GATInvocationException {
		return fileSize;
	} // public long length() throws GATInvocationException
	
	/**
	 * Relocates the file pointer to the given position.
	 * @see org.gridlab.gat.io.cpi.RandomAccessFileCpi#seek(long)
	 */
	@Override
	public void seek(long newPosition) throws GATInvocationException {
		if (isClosed) {
			throw new GATInvocationException("Stream is already closed!");
		}
		currentPos = newPosition;
	} // public void seek(long newPosition) throws GATInvocationException

	/**
	 * Reads from the current position data to the given array.
	 * @see org.gridlab.gat.io.cpi.RandomAccessFileCpi#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] buffer, int offset, int length) throws GATInvocationException {
		if (isClosed) {
			throw new GATInvocationException("Stream is already closed!");
		}
		if (currentPos >= fileSize) {
			return -1;
		}
		// avoid reading after end of file
        if (length > fileSize - currentPos) {
            length = (int)(fileSize - currentPos);
        }
        // don't write more then the buffer can take
        if ((offset + length) > buffer.length) {
        	length = buffer.length - offset;
        }

		MyDataSink sink = new MyDataSink(buffer, offset, length);
		try {
			client.extendedGet(path, currentPos, length, sink, null);
		} catch (Exception e) {
			throw new GATInvocationException("Error while reading data!", e);
		}
		currentPos += sink.getSize();
		return sink.getSize();
	} // public int read(byte[] buffer, int offset, int length) throws GATInvocationException
	
	/**
	 * Skip the given number of bytes, returns the actual number of skipped
	 * bytes.
	 * @see org.gridlab.gat.io.cpi.RandomAccessFileCpi#skipBytes(int)
	 */
	@Override
	public int skipBytes(int length) throws GATInvocationException {
		currentPos += length();
		if (currentPos > fileSize) {
			int skipped = length - (int)(currentPos - fileSize);
			return skipped;
		} else {
			return length;
		}
	} // public int skipBytes(int length) throws GATInvocationException
	
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
                logger.debug("buf.length = " + buf.length + ", off = " + off + ", len = " + len);
            }
            System.arraycopy(b, 0, buf, 0, Math.min(l, len));
            synchronized(this) {
                writtenLen += Math.min(l, len);
            }
        }
        
        int getSize() {
            return writtenLen;
        }
    }

} // public class GridFTPReadOnlyRandomAccessFileAdaptor extends RandomAccessFileCpi
