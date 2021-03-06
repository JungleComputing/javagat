package org.gridlab.gat.io.cpi.gt42;

import java.net.URISyntaxException;
import java.util.Map;

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.FileTransferTaskHandler;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.GeneralException;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Task;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

//This class only supports the URIs with gsiftp
@SuppressWarnings("serial")
public class GT42GridFTPFileAdaptor extends GT42FileAdaptor {
    
    public static String getDescription() {
        return "The GT42 GridFTP File Adaptor implements the File object for Globus 4.2 using the gsiftp protocol.";
    }
    public static String[] getSupportedSchemes() {
        return new String[] { "gt42", "gridftp", "gsiftp", "file", ""};
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = GT42FileAdaptor
                .getSupportedCapabilities();
        capabilities.put("copy", true);
        return capabilities;
    }

    /**
     * @param gatContext
     * @param location
     */
    public GT42GridFTPFileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {
        super(gatContext, location, "gsiftp");
    }

    /**
     * This method copies the physical file represented by this File instance to
     * a physical file identified by the passed URI. This method used by the
     * <code>copy</code> method. Tries to do 3rd party copy, if it is
     * supported. The destination
     * 
     * @param dest
     *                The new location
     * @param destProvider
     *                JavaCog provider of the destination. Possible values are:
     *                gt2ft, gsiftp, condor, ssh, gt4ft, local, gt4, gsiftp-old,
     *                gt3.2.1, gt2, ftp, webdav. Aliases: webdav <-> http; local
     *                <-> file; gsiftp-old <-> gridftp-old; gsiftp <-> gridftp;
     *                gt4 <-> gt3.9.5, gt4.0.2, gt4.0.1, gt4.0.0 For creating
     *                the destination service and security context uses the
     *                specified provider.
     * @throws GatInvocationException
     */
    synchronized protected void copyThirdParty(URI dest, String destProvider)
            throws GATInvocationException {
        if (logger.isDebugEnabled()) {
            logger.debug("GT42GridFileAdaptor file: start file copy with destination provider "
                            + destProvider);
        }
        Task task = new TaskImpl("my3rdpartycopy" + Math.random(),
                Task.FILE_TRANSFER);
        FileTransferSpecification spec = new FileTransferSpecificationImpl();
        spec.setSource(location.getPath());
        spec.setDestination(dest.getPath());
        /*
         * if(!dest.refersToLocalHost() && !location.refersToLocalHost()) {
         * if(GATEngine.DEBUG) { System.er r.println("GT4FileAdaptor file: set
         * thirdparty"); }
         */
        spec.setThirdParty(true);
        /* } */
        task.setSpecification(spec);

        // Source Service
        Service sourceService = new ServiceImpl(Service.FILE_TRANSFER);
        sourceService.setProvider(srcProvider);
        SecurityContext sourceSecurityContext = null;
        try {
            sourceSecurityContext = AbstractionFactory
                    .newSecurityContext(srcProvider);
        } catch (Exception e) {
            throw new GATInvocationException(e.getMessage());
        }
        sourceSecurityContext.setCredentials(getCredential(srcProvider, location));
        sourceService.setSecurityContext(sourceSecurityContext);

        ServiceContact sourceServiceContact = new ServiceContactImpl();
        sourceServiceContact.setHost(location.getHost());
        sourceServiceContact.setPort(location.getPort());
        sourceService.setServiceContact(sourceServiceContact);
        task.setService(Service.FILE_TRANSFER_SOURCE_SERVICE, sourceService);

        // Destination Service
        Service destinationService = new ServiceImpl(Service.FILE_TRANSFER);
        destinationService.setProvider(destProvider);
        SecurityContext destinationSecurityContext = null;
        try {
            destinationSecurityContext = AbstractionFactory
                    .newSecurityContext(destProvider);
        } catch (Exception e) {
            throw new GATInvocationException(e.getMessage());
        }
        destinationSecurityContext.setCredentials(getCredential(destProvider,
                dest));
        destinationService.setSecurityContext(destinationSecurityContext);
        ServiceContact destinationServiceContact = new ServiceContactImpl();
        destinationServiceContact.setHost(dest.getHost());
        destinationServiceContact.setPort(dest.getPort());
        destinationService.setServiceContact(destinationServiceContact);
        task.setService(Service.FILE_TRANSFER_DESTINATION_SERVICE,
                destinationService);
        
        // File Transfer
        FileTransferTaskHandler handler = new FileTransferTaskHandler();

        try {
            handler.submit(task);
        } catch (Exception e) {
            throw new GATInvocationException(e.getMessage());
        }
        try {
            task.waitFor();
        } catch (InterruptedException e) {
            throw new GATInvocationException(e.getMessage());
        }
        if (!task.isCompleted()) {
            throw new GATInvocationException(
                    "GT42GridFTPFileAdaptor: copy is failed.");
        }
        if (logger.isInfoEnabled()) {
            logger.info("GT42GriFTPFileAdaptor: copy1 done.");
        }
    }

    /**
     * Copies a remote file to the local machine with the
     * <code>FileResource getFile</code> method.
     * 
     * @param dest
     *                Destination location, points a local file.
     * @throws GATInvocationException
     */
    protected void copyToLocal(URI dest) throws GATInvocationException {
        try {
            resourceStart();
            resource.getFile(location.getPath(), dest.getPath());
        } catch (FileNotFoundException e) {
            throw new GATInvocationException("copy to local failed1", e);
        } catch (GeneralException e) {
            e.printStackTrace();
            throw new GATInvocationException("copy to local failed2", e);
        } finally {
            resourceStop();
        }
        if (logger.isInfoEnabled()) {
            logger.info("GT42GriFTPFileAdaptor: copy2 done.");
        }
    }

    /**
     * This method fails when the source is a file and the target is a directory
     * that doesn't exist (it throws an exception).
     * 
     * This method fails when the source is a directory and the target doesn't
     * exist (it throws an exception)
     * 
     * @param dest
     * @throws GATInvocationException
     */
    protected void copyToRemote(URI dest) throws GATInvocationException {
        if (dest.getScheme() == null || ! recognizedScheme(dest.getScheme(), getSupportedSchemes())) {
            throw new GATInvocationException("GT42FileAdaptor: Cannot handle this URI "
                    + dest);
        }
       
    	FileResource remoteResource = null;
        try {
            remoteResource = AbstractionFactory.newFileResource(srcProvider);
        } catch (Exception e) {
            throw new GATInvocationException("gt42gridftp", e);
        }
        remoteResource.setName("gt42file: " + Math.random());
        SecurityContext securityContext = null;
        try {
            securityContext = AbstractionFactory
                    .newSecurityContext(srcProvider);
            securityContext.setCredentials(getCredential(srcProvider, dest));
        } catch (Exception e) {
            throw new GATInvocationException("gt42gridftp", e);
        }
        remoteResource.setSecurityContext(securityContext);
        ServiceContact serviceContact = new ServiceContactImpl(dest.getHost(),
                dest.getPort());
        remoteResource.setServiceContact(serviceContact);
        try {
            remoteResource.start();
        } catch (Exception e) {
            throw new GATInvocationException("gt42gridftp", e);
        }
        try {
            remoteResource.putFile(location.getPath(), dest.getPath());
        } catch (FileNotFoundException e) {
            throw new GATInvocationException("gt42gridftp", e);
        } catch (GeneralException e) {
            throw new GATInvocationException("gt42gridftp", e);
        }
    }

    /**
     * Copies the file to the location represented by <code>URI dest</code>.
     * If the destination is on the local machine is calls the
     * <code>copyToLocal</code> method. In other cases the
     * <code>copyThirdParty</code> method is called. It passes a provider
     * string to the call, and it tries the copy with all JavaCog provider.
     * 
     * @param dest
     *                destination location of the file copy
     * @throws GATInvocationException
     * 
     */
    public void copy(URI dest) throws GATInvocationException {
        // determinate dest is a directory, and pass the filename if it is,
        // otherwise it will fail
        boolean destIsLocal = dest.isCompatible("file") && dest.refersToLocalHost();
        if (destIsLocal && localFile) {
            throw new GATInvocationException("local-->local copy not implemented by GT42 adaptor");
        }
        if (determineIsDirectory()) {
            copyDirectory(gatContext, null, toURI(), dest);
            return;
        }

        File destinationFile = null;
        try {
            destinationFile = GAT.createFile(gatContext, dest);
        } catch (GATObjectCreationException e) {
            // throw new GATInvocationException("GT4FileAdaptor: copy, " + e);
            // give a try anyway
            destinationFile = null;
        }
        // fix the filename, if the destination is a directory
        if (destinationFile != null) {
            try {
                if (destinationFile.isDirectory()
                        || dest.getPath().endsWith("/")) {
                    String destStr = null;
                    if (dest.toString().endsWith("/")) {
                        destStr = dest.toString() + getName();
                    } else {
                        destStr = dest.toString() + "/" + getName();
                    }

                    try {
                        dest = new URI(destStr);
                    } catch (URISyntaxException e) {
                        throw new GATInvocationException(
                                "GT42FileAdaptor: copy, " + e);
                    }
                }
            } catch (GATInvocationException e) {
                // leave everything as it is
            }
        }
        if (destIsLocal) {
            if (logger.isDebugEnabled()) {
                logger.debug("GT42GridFTPFileAdaptor: copy remote to local");
            }
            copyToLocal(dest);
            return;
        } else if (localFile) {
            copyToRemote(dest);
            return;
        } else if (dest.getScheme().equalsIgnoreCase("any")) {
            for (int i = 0; i < providers.length; i++) {
                try {
                    copyThirdParty(dest, providers[i]);
                    return;
                } catch (Exception e) {
                }
            }
        } else {
            copyThirdParty(dest, dest.getScheme());
            return;
        }
        throw new GATInvocationException(
                "GT42GridFTP file: thirdparty copy failed.");
    }
}
