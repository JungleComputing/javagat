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
import org.gridlab.gat.io.cpi.FileCpi;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbException;


public class SmbFileAdaptor extends FileCpi {
    SmbFile smbf;
    public SmbFileAdaptor(GATContext gatContext, 
			  Preferences preferences,
			  URI location) 
	throws GATObjectCreationException, SmbException, MalformedURLException {
        super(gatContext, preferences, location);
	
        if (!location.isCompatible("smb") ) {
            throw new GATObjectCreationException("cannot handle this URI");
        }
	smbf = new SmbFile(location.toString());
    }
    
    public String[] list() throws GATInvocationException {
	String[] res;
	try {
	    res = smbf.list();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}
	return res;
    }
    
    public boolean exists() throws GATInvocationException {
	boolean res;
	try {
	    res = smbf.exists();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}
	return res;
    }

    public boolean isDirectory() throws GATInvocationException {
	boolean res;
	try {
	    res = smbf.isDirectory();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}
	return res;
    }
    
    public long length() throws GATInvocationException {
	long res;
	try {
	    res = smbf.length();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}
	return res;
    }

    public boolean mkdir() throws GATInvocationException {
	try {
	    smbf.mkdir();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}
	return true;
    }

    public boolean mkdirs() throws GATInvocationException {
	try {
	    smbf.mkdirs();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}
	return true;
    }
    
    public boolean delete() throws GATInvocationException {
	try {
	    smbf.delete();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	} 
	return true;
    }

    public boolean canRead() throws GATInvocationException {
	boolean res;
	try {
	    res = smbf.canRead();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	} 
	return res;
    }

    public boolean canWrite() throws GATInvocationException {
	boolean res;
	try {
	    res = smbf.canWrite();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	} 
	return res;
    }
    
    public boolean createNewFile() throws GATInvocationException {
	try {
	    smbf.createNewFile();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}  
	return true;
    }

    public boolean isFile() throws GATInvocationException {
	boolean res;
	try {
	    res = smbf.isFile();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	} 
	return res;
    }
    
    public boolean isHidden() throws GATInvocationException {
	boolean res;
	try {
	    res = smbf.isHidden();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	} 
	return res;	
    }
    
    public long lastModified() throws GATInvocationException {
	long res;
	try {
	    res = smbf.lastModified();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	} 
	return res;
    }

}
