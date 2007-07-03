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
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.gridlab.gat.io.File;
import org.ietf.jgss.GSSCredential;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Collection;
import java.util.Iterator;
import java.net.URISyntaxException;

//This class only supports the URIs with gsiftp
public class GT4LocalFileAdaptor extends GT4FileAdaptor {
    /**
     * @param gatContext
     * @param preferences
     * @param location
     */
    public GT4LocalFileAdaptor(GATContext gatContext, Preferences preferences,
			  URI location) throws GATObjectCreationException {
        super(gatContext, preferences, location, "local");
    }
    
    synchronized protected void copyThirdParty(URI dest, String destProvider)
	throws GATInvocationException {
	if(GATEngine.DEBUG) {
	    System.err.println("GT4LocalFileAdaptor file: start file copy with destination provider "+ destProvider);
	} 
	Task task = new TaskImpl("my3rdpartycopy" + Math.random(), Task.FILE_TRANSFER);
	FileTransferSpecification spec = new FileTransferSpecificationImpl();
	spec.setSource(location.getPath());
	spec.setDestination(dest.getPath());
	/*if(!dest.refersToLocalHost() && !location.refersToLocalHost()) {
	    if(GATEngine.DEBUG) {
		System.err.println("GT4FileAdaptor file: set thirdparty");
		} */
	//spec.setThirdParty(true);
	/*}*/
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
	sourceSecurityContext.setCredentials(getCredential(destProvider, dest));
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
	    destinationSecurityContext = 
		AbstractionFactory.newSecurityContext(destProvider);
	}
	catch( Exception e ) {
	    throw new GATInvocationException(e.getMessage());
	}
 	destinationSecurityContext.setCredentials(getCredential(destProvider, dest));
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
	    throw new GATInvocationException("GT4LocalFileAdaptor: copy is failed.");
	}
	System.out.println("GT4LocalFileAdaptor: third party copy done.");
	if(GATEngine.VERBOSE) {
	    System.out.println("GT4LocalFileAdaptor: third party copy done.");
	}
    }

    /**
     * Copies a remote file to the local machine with the 
     * <code>FileResource getFile</code> method.
     * @param dest Destination location, points a local file.
     * @throws GATInvocationException
     */
    protected void copyToLocal(URI dest) throws GATInvocationException {
	try {
	    resource.getFile(location.getPath(), dest.getPath());
	} catch(FileNotFoundException e) {
	    throw new GATInvocationException(e.getMessage());
	} catch(GeneralException e) {
	    throw new GATInvocationException(e.getMessage());
	}
	System.out.println("GT4LocalFileAdaptor: copy done.");
	if(GATEngine.VERBOSE) {
	    System.out.println("GT4LocalFileAdaptor: copy done.");
	}
    }
    
    /**
     * Copies the file to the location represented by 
     * <code>URI dest</code>.
     * If the destination is on the local machine
     * is calls the <code>copyToLocal</code> method.
     * In other cases the <code>copyThirdParty</code>
     * method is called. It passes a provider string to the call,
     * and it tries the copy with all JavaCog provider.
     * @param dest destination location of the file copy
     * @throws GATInvocationException 
     *
     */
    public void copy(URI dest) throws GATInvocationException {
	System.out.println("gt4 local copy: " + srcProvider + " "
			   + location + " -> " + dest);
	//determinate dest is a directory, and pass the filename if it is, otherwise it will fail
	if(determineIsDirectory()) {
	    String destStr = null;
	    if(!dest.toString().endsWith("/")) {
		destStr = dest.toString()+"/";
		try {
		    dest = new URI(destStr);
		} catch(URISyntaxException e) {
		    throw new GATInvocationException("GT4LocalFileAdaptor: copy, " + e);
		}
	    }
	}

	File destinationFile = null;
	try { 
	    destinationFile = GAT.createFile(gatContext, preferences, dest);
	} catch(GATObjectCreationException e) {
	    //throw new GATInvocationException("GT4FileAdaptor: copy, " + e);
	    //give a try anyway
	    destinationFile = null;
	}

	//fix the filename, if the destination is a directory
	if(destinationFile != null) {
	    try {
		if(destinationFile.isDirectory()) {
 		    String destStr;
		    if(dest.toString().endsWith("/")) {
			destStr = dest.toString()+getName();
		    } else {
			destStr = dest.toString()+"/"+getName();
		    }
		    try {
			dest = new URI(destStr);
		    } catch(URISyntaxException e) {
			throw new GATInvocationException("GT4LocalFileAdaptor: copy, " + e);
		    }
		}
	    } catch(GATInvocationException e) {
		//leave everything as it is
	    }
	}

	if(determineIsDirectory()) {
	    copyDirectory(gatContext, preferences, toURI(), dest);
	    return;
	}
	if(dest.isLocal()) {
	    if(GATEngine.DEBUG) {
		System.err.println("GT4LocalFileAdaptor: copy remote to local");
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
	throw new GATInvocationException("GT4LocalFileAdaptor: thirdparty copy failed.");
    }
}
