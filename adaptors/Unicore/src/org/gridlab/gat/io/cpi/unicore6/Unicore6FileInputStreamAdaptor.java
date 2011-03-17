package org.gridlab.gat.io.cpi.unicore6;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;

import eu.unicore.hila.Location;
import eu.unicore.hila.Resource;
import eu.unicore.hila.exceptions.HiLAException;
import eu.unicore.hila.grid.File;

/**
 * FileInputStreamAdaptor for Unicore6.
 * 
 * @author Andreas Bender
 */
public class Unicore6FileInputStreamAdaptor extends FileInputStreamCpi {

	/**
	 * The {@link InputStream} instance that is used for this adaptor.
	 */
	protected InputStream in;

	/**
	 * Constructs a new instance of Unicore6FileInputStreamAdaptor.
	 * 
	 * @param gatContext the GAT context
	 * @param location the stream location
	 * @throws GATObjectCreationException an exception that might occur
	 */
	public Unicore6FileInputStreamAdaptor(GATContext gatContext, URI location) throws GATObjectCreationException {
		super(gatContext, location);

		if (!location.isCompatible("unicore6")) {
			throw new AdaptorNotApplicableException("cannot handle this URI: " + location);
		}

		// now try to create a stream.
		try {
			in = createStream();
		} catch (GATInvocationException e) {
			throw new GATObjectCreationException("unicore6 inputstream", e);
		}
	}

	protected InputStream createStream() throws GATInvocationException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		File hiLAFile;

		Location loc = new Location(location.toJavaURI());
		try {
			Resource resource = loc.locate(loc);
			if (resource instanceof File) {
				hiLAFile = (File) resource;
			} else {
				throw new GATInvocationException("URI does not match a HiLA file location");
			}
			hiLAFile.exportToStream(out).block();
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileInputStreamAdaptor: ", e);
		}

		byte[] fileData = out.toByteArray();
		InputStream input = new ByteArrayInputStream(fileData);

		return input;
	}

	/**
	 * @see org.gridlab.gat.io.cpi.FileInputStreamCpi#available()
	 */
	public int available() throws GATInvocationException {
		try {
			return in.available();
		} catch (IOException e) {
			throw new GATInvocationException("Unicore6FileInputStream", e);
		}
	}

	/**
	 * @see org.gridlab.gat.io.cpi.FileInputStreamCpi#close()
	 */
	public void close() throws GATInvocationException {
		try {
			in.close();
		} catch (IOException e) {
			throw new GATInvocationException("Unicore6FileInputStream", e);
		}
	}

	/**
	 * @see org.gridlab.gat.io.cpi.FileInputStreamCpi#mark(int)
	 */
	public synchronized void mark(int arg0) {
		in.mark(arg0);
	}

	/**
	 * @see org.gridlab.gat.io.cpi.FileInputStreamCpi#markSupported()
	 */
	public boolean markSupported() {
		return in.markSupported();
	}

	/**
	 * @see org.gridlab.gat.io.cpi.FileInputStreamCpi#read()
	 */
	public int read() throws GATInvocationException {
		try {
			return in.read();
		} catch (IOException e) {
			throw new GATInvocationException("Unicore6FileInputStream", e);
		}
	}

	/**
	 * @see org.gridlab.gat.io.cpi.FileInputStreamCpi#read(byte[], int, int)
	 */
	public int read(byte[] b, int offset, int len) throws GATInvocationException {
		try {
			return in.read(b, offset, len);
		} catch (IOException e) {
			throw new GATInvocationException("Unicore6FileInputStream", e);
		}
	}

	/**
	 * @see org.gridlab.gat.io.cpi.FileInputStreamCpi#read(byte[])
	 */
	public int read(byte[] arg0) throws GATInvocationException {
		try {
			return in.read(arg0);
		} catch (IOException e) {
			throw new GATInvocationException("Unicore6FileInputStream", e);
		}
	}

	/**
	 * @see org.gridlab.gat.io.cpi.FileInputStreamCpi#reset()
	 */
	public synchronized void reset() throws GATInvocationException {
		try {
			in.reset();
		} catch (IOException e) {
			throw new GATInvocationException("Unicore6FileInputStream", e);
		}
	}

	/**
	 * @see org.gridlab.gat.io.cpi.FileInputStreamCpi#skip(long)
	 */
	public long skip(long arg0) throws GATInvocationException {
		try {
			return in.skip(arg0);
		} catch (IOException e) {
			throw new GATInvocationException("Unicore6FileInputStream", e);
		}
	}

}
