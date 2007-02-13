package org.gridlab.gat.io.cpi.smb;

import org.gridlab.gat.io.cpi.FileInputStreamCpi;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.AdaptorNotApplicableException;

import java.io.IOException;

import jcifs.smb.SmbFileInputStream;

public class SmbFileInputStreamAdaptor extends FileInputStreamCpi {
    SmbFileInputStream in;
    
    public SmbFileInputStreamAdaptor(GATContext gatContext, 
				     Preferences preferences, 
				     URI location) 
	throws IOException, GATObjectCreationException {
	super(gatContext, preferences, location);
	if( !location.isCompatible("smb" ) ) {
	    throw new AdaptorNotApplicableException("cannot handle this URI");
	}
	in =  new SmbFileInputStream(location.toString());
    }
    
    public int available() throws GATInvocationException {
	try {
            return in.available();
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }

    public void close() throws GATInvocationException {
        try {
            in.close();
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }

    public synchronized void mark(int arg0) {
        in.mark(arg0);
    }
    
    public boolean markSupported() {
        return in.markSupported();
    }

    public int read() throws GATInvocationException {
        try {
            return in.read();
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }
    
    public int read(byte[] b, int offset, int len)
	throws GATInvocationException {
        try {
            return in.read(b, offset, len);
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }
    
    public int read(byte[] arg0) throws GATInvocationException {
        try {
            return in.read(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }
    
    public synchronized void reset() throws GATInvocationException {
        try {
            in.reset();
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }
    
    public long skip(long arg0) throws GATInvocationException {
        try {
            return in.skip(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("DefaultFileInputStream", e);
        }
    }
}
    
