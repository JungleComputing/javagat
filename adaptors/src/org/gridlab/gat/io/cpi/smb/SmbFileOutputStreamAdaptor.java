package org.gridlab.gat.io.cpi.smb;

import org.gridlab.gat.io.cpi.FileOutputStreamCpi;
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

import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbFile;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;

public class SmbFileOutputStreamAdaptor extends FileOutputStreamCpi {
    SmbFileOutputStream out;
    public SmbFileOutputStreamAdaptor(GATContext gatContext,
					Preferences preferences, 
					URI location, Boolean append)
	throws IOException, GATObjectCreationException {
        super(gatContext, preferences, location, append);

        if (!location.isCompatible("smb") ) {
            throw new AdaptorNotApplicableException("cannot handle this URI");
        }
	try {
	    out = createStream();
	} catch(Exception e) {
	    throw new GATObjectCreationException("SmbFileOutputStream: "+e);
	}
    }

    protected SmbFileOutputStream createStream() 
	throws GATInvocationException {
	SmbFileOutputStream os = null;
	List l = 
	    SecurityContextUtils.getValidSecurityContextsByType(
               gatContext, preferences,
	       "org.gridlab.gat.security.PasswordSecurityContext", 
	       "smb", location.getHost(), 
	       location.getPort(SmbFile.DEFAULT_PORT));
	if((l==null) || (l.size() == 0)) {
	    try {
		os = new SmbFileOutputStream(location.toString());
		return os;
	    } catch(Exception e) {
		throw new GATInvocationException("SmbFileOutputStream: "+e);
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
	    os = new SmbFileOutputStream(smbf);
	    return os;
	} catch(Exception e) {
	    throw new GATInvocationException("SmbFileOutputStream: "+e);
	}
    }

    public void close() throws GATInvocationException {
        try {
            out.close();
        } catch (IOException e) {
            throw new GATInvocationException("local output stream", e);
        }
    }

    public void flush() throws GATInvocationException {
        try {
            out.flush();
        } catch (IOException e) {
            throw new GATInvocationException("local output stream", e);
        }
    }
    
    public void write(byte[] b, int offset, int len)
	throws GATInvocationException {
        try {
            out.write(b, offset, len);
        } catch (IOException e) {
            throw new GATInvocationException("local output stream", e);
        }
    }
 
    public void write(byte[] arg0) throws GATInvocationException {
        try {
            out.write(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("local output stream", e);
        }
    }
    
    public void write(int arg0) throws GATInvocationException {
        try {
            out.write(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("local output stream", e);
        }
    }    
}
    
