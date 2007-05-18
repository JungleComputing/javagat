package org.gridlab.gat.io.cpi.gt4;

import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.GeneralException;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Permissions;

import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.impl.common.task.FileOperationSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.FileTransferTaskHandler;
import org.globus.cog.abstraction.impl.common.execution.TaskHandlerImpl;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.MethodNotApplicableException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.cpi.FileCpi;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Collection;
import java.util.Iterator;

//This class only supports the URIs with gsiftp
public class GT4FileAdaptor extends FileCpi {
    FileResource resource = null;
    String srcProvider;
    String [] providers = { "gsiftp",
			    "local" };
    /**
     * @param gatContext
     * @param preferences
     * @param location
     */
    public GT4FileAdaptor(GATContext gatContext, Preferences preferences,
			  URI location, String prov) throws GATObjectCreationException {
        super(gatContext, preferences, location);
	srcProvider = prov;
    }


    /**
     * This method copies the physical file represented by this File instance to
     * a physical file identified by the passed URI.
     *
     * @param destination
     *            The new location
     * @throws java.io.IOException
     *             Upon non-remote IO problem
     */
    protected void copyThirdParty(URI dest, String destProvider)
	throws GATInvocationException {
	if(GATEngine.DEBUG) {
	    System.err.println("GT4GridFTP file: start file copy with destination provider "+ destProvider);
	} 
	Task task = new TaskImpl("my3rdpartycopy", Task.FILE_TRANSFER);
	FileTransferSpecification spec = new FileTransferSpecificationImpl();
	spec.setSource(location.getPath());
	spec.setDestination(dest.getPath());
	if(!dest.refersToLocalHost() && !location.refersToLocalHost()) {
	    if(GATEngine.DEBUG) {
		System.err.println("GT4GridFTP file: set thirdparty");
	    } 
	    spec.setThirdParty(true);
	}
	task.setSpecification(spec);
	
	Service sourceService = new ServiceImpl(Service.FILE_TRANSFER);
	sourceService.setProvider(srcProvider);
	SecurityContext sourceSecurityContext = null;
	try {
	    sourceSecurityContext =
		AbstractionFactory.newSecurityContext(srcProvider);
	}
	catch( Exception e ) {
	    throw new GATInvocationException(e.getMessage());
	}
	sourceSecurityContext.setCredentials(null);
	sourceService.setSecurityContext(sourceSecurityContext);
	ServiceContact sourceServiceContact =
	    new ServiceContactImpl();
	sourceServiceContact.setHost(location.getHost());
	sourceServiceContact.setPort(location.getPort());
	sourceService.setServiceContact(sourceServiceContact);
	task.setService(Service.FILE_TRANSFER_SOURCE_SERVICE, sourceService);
	
	Service destinationService = new ServiceImpl(Service.FILE_TRANSFER);
	destinationService.setProvider(destProvider);
 	SecurityContext destinationSecurityContext = null;
	try {
	    destinationSecurityContext = AbstractionFactory.newSecurityContext(destProvider);
	}
	catch( Exception e ) {
	    throw new GATInvocationException(e.getMessage());
	}
 	destinationSecurityContext.setCredentials(null);
 	destinationService.setSecurityContext(destinationSecurityContext);
 	ServiceContact destinationServiceContact =
	    new ServiceContactImpl();
	destinationServiceContact.setHost(dest.getHost());
	destinationServiceContact.setPort(dest.getPort());
 	destinationService.setServiceContact(destinationServiceContact);
	task.setService(Service.FILE_TRANSFER_DESTINATION_SERVICE, destinationService);
	FileTransferTaskHandler handler = new FileTransferTaskHandler();

	try {
	    handler.submit(task);
	}
	catch( Exception e ) {
	    throw new GATInvocationException(e.getMessage());
	}

	try { 
	    task.waitFor();
	} catch(InterruptedException e) {
	    throw new GATInvocationException(e.getMessage());
	}
	if(!task.isCompleted()) {
	    throw new GATInvocationException("GT4GridFTP file: copy is failed.");
	}
	if(GATEngine.VERBOSE) {
	    System.out.println("GT4GridFTP file: third party copy done.");
	}
    }

    protected void copyToLocal(URI dest) throws GATInvocationException {
	try {
	    resource.getFile(location.getPath(), dest.getPath());
	} catch(FileNotFoundException e) {
	    throw new GATInvocationException(e.getMessage());
	} catch(GeneralException e) {
	    throw new GATInvocationException(e.getMessage());
	}
	    
    }

    public void copy(URI dest) throws GATInvocationException {
	if(determineIsDirectory()) {
	    copyDirectory(gatContext, preferences, toURI(), dest);
	    return;
	}
	if(dest.isLocal()) {
	    if(GATEngine.DEBUG) {
		System.err.println("GT4GridFTP file: copy remote to local");
	    }
	    copyToLocal(dest);
	    return;
	}
	//I do not handle the case, when the source is local
	if(dest.getScheme().equalsIgnoreCase("any")) {
	    for(int i=0; i<providers.length; i++) {
		try {
		    copyThirdParty(dest, providers[i]);
		    return;
		} catch(Exception e) {
		}
	    }
	} else {
	    copyThirdParty(dest, dest.getScheme());
	    return;
	}
	throw new GATInvocationException("GT4GridFTP file: thirdparty copy failed.");
    }
    
    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#canRead()
     */
    public boolean canRead() throws GATInvocationException {
	GridFile gf = null;
	try {
	    gf = resource.getGridFile(location.getPath());
	    return gf.userCanRead();
	} catch(FileNotFoundException e) {
	    throw new GATInvocationException();
	} catch(GeneralException e) {
	    throw new GATInvocationException();
	}
    }
    
    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#canWrite()
     */
    public boolean canWrite() throws GATInvocationException {
	GridFile gf = null;
	try {
	    gf = resource.getGridFile(location.getPath());
	} catch(FileNotFoundException e) {
	    throw new GATInvocationException();
	} catch(GeneralException e) {
	    throw new GATInvocationException();
	}
	return gf.userCanWrite();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#createNewFile()
     */
     public boolean createNewFile() {
	 throw new UnsupportedOperationException("Not implemented");
     }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#delete()
     */
     public boolean delete() {
         try {
	     resource.deleteFile(location.getPath());
	 } catch(Exception e) {
	     return false;
	 }
	 return true;
     }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#exists()
     */
     public boolean exists() {
	 try {
	     return resource.exists(location.getPath());
	 } catch(FileNotFoundException e) {
	     return false;
	 } catch(GeneralException e) {
	     if (GATEngine.VERBOSE) {
		 System.err.println("GT4FileAdaptor caught a GeneralException in method exists(): " + e);
	     }
	     return false;
	 }
     }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#getAbsoluteFile()
     */
    public org.gridlab.gat.io.File getAbsoluteFile() throws GATInvocationException {
	try {
	    return GAT.createFile(gatContext, preferences, getAbsolutePath());
	} catch(Exception e) {
	    throw new GATInvocationException("cannot create GAT.createFile: " + e);
	}
    }
    
    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#getAbsolutePath()
     */
    public String getAbsolutePath() throws GATInvocationException {
	if(location.isAbsolute()) {
	    return location.getPath();
	}
	GridFile gf = null;
	try {
	    gf = resource.getGridFile(location.getPath());
	    return gf.getAbsolutePathName();
	} catch(FileNotFoundException e) {
	    throw new GATInvocationException();
	} catch(GeneralException e) {
	    throw new GATInvocationException();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#getCanonicalFile()
     */
    public org.gridlab.gat.io.File getCanonicalFile() {
	throw new UnsupportedOperationException("Not implemented");
    }
    
    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#getCanonicalPath()
     */
    public String getCanonicalPath() throws GATInvocationException {
	throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#isDirectory()
     */
    public boolean isDirectory() throws GATInvocationException {
	//How should be handled the / in the in the end of the location?
	//Probably a bug in the Cog Toolkit?
	GridFile gf = null;
	try {
	    String path = location.getPath();
	    gf = resource.getGridFile(path);
	    if(gf==null && path.endsWith("/")) {
		gf = resource.getGridFile(path + '.');
	    }
	    if(gf==null) {
		throw new GATInvocationException("GridFile is null");
	    }
	    return gf.isDirectory();
	} catch(FileNotFoundException e) {
	    throw new GATInvocationException();
	} catch(GeneralException e) {
	    throw new GATInvocationException();
	}
    }
    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#isFile()
     */
    public boolean isFile() throws GATInvocationException {
	GridFile gf = null;
	try {
	    gf = resource.getGridFile(location.getPath());
	    return gf.isFile();
	} catch(FileNotFoundException e) {
	    throw new GATInvocationException();
	} catch(GeneralException e) {
	    throw new GATInvocationException();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#isHidden()
     */
    public boolean isHidden() {
	throw new UnsupportedOperationException("Not implemented");
    }
    
    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#lastModified()
     */
    public long lastModified() throws GATInvocationException {
	GridFile gf = null;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	Date d;
	try {
	    gf = resource.getGridFile(location.getPath());
	    d=sdf.parse(gf.getLastModified());
	    if(GATEngine.VERBOSE) {
		System.err.println("Last modified: "+gf.getLastModified());
	    }
	    return d.getTime();
	} catch(FileNotFoundException e) {
	    throw new GATInvocationException(e.getMessage());
	} catch(GeneralException e) {
	    throw new GATInvocationException(e.getMessage());
	} catch(ParseException e) {
	    throw new GATInvocationException(e.getMessage());
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#length()
     */
    public long length() throws GATInvocationException {
	GridFile gf = null;
	try {
	    gf = resource.getGridFile(location.getPath());
	    return gf.getSize();
	} catch(FileNotFoundException e) {
	    throw new GATInvocationException(e.getMessage());
	} catch(GeneralException e) {
	    throw new GATInvocationException(e.getMessage());
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#list()
     */
    public String[] list() throws GATInvocationException {
	Collection c;
	if(!isDirectory()) {
	    return null;
	}
	try {
	    c = resource.list(location.getPath());
	} catch(GeneralException e) {
	    throw new GATInvocationException(e.getMessage());
	} catch(DirectoryNotFoundException e) {
	    throw new GATInvocationException(e.getMessage());
	}
	String[] res = new String[c.size()];
	Iterator iterator = c.iterator();
	int i=0;
	while(iterator.hasNext()) {
	    GridFile element = (GridFile) iterator.next();
	    System.out.println(element.getName());
	    res[i]=element.getName();
	    i++;
	}
	return res;
    }
    

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#mkdir()
     */
    public boolean mkdir() throws GATInvocationException {
	try {
	    resource.createDirectory(location.getPath());
	} catch(GeneralException e) {
	    if(GATEngine.VERBOSE) {
		System.out.println(e);
	    }
	    return false;
	}
	return true;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#renameTo(java.io.File)
     */
//     public boolean renameTo(java.io.File arg0) throws GATInvocationException {
// 	resource.rename(location.getPath(), arg0.get
//     }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#setLastModified(long)
     */
    public boolean setLastModified(long arg0) throws GATInvocationException {
	GridFile gf = null;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	Date d = new Date(arg0);
	try {
	    gf = resource.getGridFile(location.getPath());
	} catch(FileNotFoundException e) {
	    throw new GATInvocationException(e.getMessage());
	} catch(GeneralException e) {
	    throw new GATInvocationException(e.getMessage());
	}
	System.out.println(sdf.format(d));
	gf.setLastModified(sdf.format(d));
	return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#setReadOnly()
     */
    public boolean setReadOnly() throws GATInvocationException {
	//That setReadOnly does not seems like to work
	GridFile gf = null;
	try {
	    gf = resource.getGridFile(location.getPath());
	} catch(FileNotFoundException e) {
	    throw new GATInvocationException(e.getMessage());
	} catch(GeneralException e) {
	    throw new GATInvocationException(e.getMessage());
	}
	Permissions perm = gf.getUserPermissions();
	perm.setWrite(false);
	gf.setUserPermissions(perm);
	return true;
    }
}
