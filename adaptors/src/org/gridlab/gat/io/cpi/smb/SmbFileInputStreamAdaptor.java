package org.gridlab.gat.io.cpi.smb;

import org.gridlab.gat.io.cpi.FileInputStreamCpi;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextUtils;

import java.io.IOException;
import java.util.List;

import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFile;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;

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
	try {
	    in =  createStream();
	} catch(Exception e) {
	    throw new GATObjectCreationException("SmbFileInputStream: "+e);
	}
    }
    
    protected SmbFileInputStream createStream() throws GATInvocationException {
	SmbFileInputStream is = null;
	List l = 
	    SecurityContextUtils.getValidSecurityContextsByType(
               gatContext, preferences,
	       "org.gridlab.gat.security.PasswordSecurityContext", 
	       "smb", location.getHost(), 
	       location.getPort(SmbFile.DEFAULT_PORT));
	if((l==null) || (l.size() == 0)) {
	    try {
		is = new SmbFileInputStream(location.toString());
		return is;
	    } catch(Exception e) {
		throw new GATInvocationException("SmbFileInputStream: "+e);
	    }
	}
	
	PasswordSecurityContext c = (PasswordSecurityContext) l.get(0);
        String user = c.getUsername();
        String password = c.getPassword();

        String host = location.getHost();
        String path = location.getPath();
	NtlmPasswordAuthentication auth =  
	    new NtlmPasswordAuthentication( host, user, password );
	try {
	    SmbFile smbf = new SmbFile( location.toString(), auth );
	    is = new SmbFileInputStream(smbf);
	    return is;
	} catch(Exception e) {
	    throw new GATInvocationException("SmbFileInputStream: "+e);
	}
    }
	
    public int available() throws GATInvocationException {
	try {
            return in.available();
        } catch (IOException e) {
            throw new GATInvocationException("SmbFileInputStream", e);
        }
    }

    public void close() throws GATInvocationException {
        try {
            in.close();
        } catch (IOException e) {
            throw new GATInvocationException("SmbFileInputStream", e);
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
            throw new GATInvocationException("SmbFileInputStream", e);
        }
    }
    
    public int read(byte[] b, int offset, int len)
	throws GATInvocationException {
        try {
            return in.read(b, offset, len);
        } catch (IOException e) {
            throw new GATInvocationException("SmbFileInputStream", e);
        }
    }
    
    public int read(byte[] arg0) throws GATInvocationException {
        try {
            return in.read(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("SmbFileInputStream", e);
        }
    }
    
    public synchronized void reset() throws GATInvocationException {
        try {
            in.reset();
        } catch (IOException e) {
            throw new GATInvocationException("SmbFileInputStream", e);
        }
    }
    
    public long skip(long arg0) throws GATInvocationException {
        try {
            return in.skip(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("SmbFileInputStream", e);
        }
    }
}
    
