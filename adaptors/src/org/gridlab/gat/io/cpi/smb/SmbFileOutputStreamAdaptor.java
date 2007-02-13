package org.gridlab.gat.io.cpi.smb;

import org.gridlab.gat.io.cpi.FileOutputStreamCpi;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.AdaptorNotApplicableException;

import java.io.IOException;

import jcifs.smb.SmbFileOutputStream;

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
	out =  new SmbFileOutputStream(location.toString());
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
    
