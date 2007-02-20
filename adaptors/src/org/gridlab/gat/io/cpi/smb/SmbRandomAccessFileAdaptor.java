package org.gridlab.gat.io.cpi.smb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;

import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;
import org.gridlab.gat.AdaptorNotApplicableException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbRandomAccessFile;
import jcifs.smb.SmbFile;

public class SmbRandomAccessFileAdaptor extends RandomAccessFileCpi {
    SmbRandomAccessFile smbraf;
    
    public SmbRandomAccessFileAdaptor(GATContext gatContext,
				      Preferences preferences, 
				      URI location, 
				      String mode)
        throws GATObjectCreationException {
        super(gatContext, preferences, location, mode);
	if(!location.isCompatible("smb")) {
	    throw new AdaptorNotApplicableException("cannot handle this URI");
        }
	try {
	    SmbFile smbf =  new SmbFile(location.toString());
	    smbraf = new SmbRandomAccessFile(smbf, mode);
	} catch( Exception e ) {
	    throw new GATObjectCreationException("smb randomaccess file", e);
        }
    }
    
    public URI toURI() {
	return location;
    }

    public void close() throws GATInvocationException {
	try {
	    smbraf.close();
	} catch( SmbException e ) {
	    throw new GATInvocationException("smb random access file", e);
        }
    }

    public long getFilePointer() throws GATInvocationException {
	long res = 0;
	try {
	    res = smbraf.getFilePointer();
	} catch( SmbException e ) {
	    throw new GATInvocationException("smb random access file", e);
        }
	return res;
    }
    
    public long length() throws GATInvocationException {
	long res;
	try {
	    res = smbraf.length();
	} catch( SmbException e ) {
	    throw new GATInvocationException("smb random access file", e);
        }
	return res;	
    }

    public int read() throws GATInvocationException {
	int res;
	try {
	    res = smbraf.read();
	} catch( SmbException e ) {
	    throw new GATInvocationException("smb random access file", e);
        }
	return res;
    }

    public int read(byte[] arg0, int arg1, int arg2) 
	throws GATInvocationException {
	int res;
	try {
	    res = smbraf.read(arg0, arg1, arg2);
	} catch( SmbException e ) {
	    throw new GATInvocationException("smb random access file", e);
        }
	return res;
    }
    
    public int read(byte[] arg0) throws GATInvocationException {
	int res;
	try {
	    res = smbraf.read(arg0);
	} catch( SmbException e ) {
	    throw new GATInvocationException("smb random access file", e);
        }
	return res;
    }


    public void seek(long arg0) throws GATInvocationException {
	try {
	    smbraf.seek(arg0);
	} catch( SmbException e ) {
	    throw new GATInvocationException("smb random access file", e);
        }
    }

    public void setLength(long arg0) throws GATInvocationException {
	try {
	    smbraf.setLength(arg0);
	} catch( SmbException e ) {
	    throw new GATInvocationException("smb random access file", e);
        }
    }

    public int skipBytes(int arg0) throws GATInvocationException {
	int res;
	try {
	    res=smbraf.skipBytes(arg0);
	} catch( SmbException e ) {
	    throw new GATInvocationException("smb random access file", e);
        }
	return res;
    }

    public void write(byte[] arg0, int arg1, int arg2) 
	throws GATInvocationException {
	try {
	    smbraf.write(arg0, arg1, arg2);
	} catch( SmbException e ) {
	    throw new GATInvocationException("smb random access file", e);
        }
    }

    public void write(byte[] arg0) throws GATInvocationException {
	try {
	    smbraf.write(arg0);
	} catch( SmbException e ) {
	    throw new GATInvocationException("smb random access file", e);
        }
    }

    public void write(int arg0) throws GATInvocationException {
	try {
	    smbraf.write(arg0);
	} catch( SmbException e ) {
	    throw new GATInvocationException("smb random access file", e);
        }
    }
}
