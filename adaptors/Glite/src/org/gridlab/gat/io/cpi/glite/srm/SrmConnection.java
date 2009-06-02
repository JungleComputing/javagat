package org.gridlab.gat.io.cpi.glite.srm;

import gov.lbl.srm.StorageResourceManager.ArrayOfAnyURI;
import gov.lbl.srm.StorageResourceManager.ArrayOfTGetFileRequest;
import gov.lbl.srm.StorageResourceManager.ArrayOfTGroupPermission;
import gov.lbl.srm.StorageResourceManager.ArrayOfTPutFileRequest;
import gov.lbl.srm.StorageResourceManager.ArrayOfTUserPermission;
import gov.lbl.srm.StorageResourceManager.ISRM;
import gov.lbl.srm.StorageResourceManager.SRMServiceLocator;
import gov.lbl.srm.StorageResourceManager.SrmLsRequest;
import gov.lbl.srm.StorageResourceManager.SrmLsResponse;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetRequest;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetResponse;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToPutRequest;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToPutResponse;
import gov.lbl.srm.StorageResourceManager.SrmPutDoneRequest;
import gov.lbl.srm.StorageResourceManager.SrmPutDoneResponse;
import gov.lbl.srm.StorageResourceManager.SrmRmRequest;
import gov.lbl.srm.StorageResourceManager.SrmRmResponse;
import gov.lbl.srm.StorageResourceManager.SrmSetPermissionRequest;
import gov.lbl.srm.StorageResourceManager.SrmSetPermissionResponse;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfGetRequestRequest;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfGetRequestResponse;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfPutRequestRequest;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfPutRequestResponse;
import gov.lbl.srm.StorageResourceManager.TDirOption;
import gov.lbl.srm.StorageResourceManager.TFileType;
import gov.lbl.srm.StorageResourceManager.TGetFileRequest;
import gov.lbl.srm.StorageResourceManager.TGetRequestFileStatus;
import gov.lbl.srm.StorageResourceManager.TMetaDataPathDetail;
import gov.lbl.srm.StorageResourceManager.TPermissionMode;
import gov.lbl.srm.StorageResourceManager.TPermissionType;
import gov.lbl.srm.StorageResourceManager.TPutFileRequest;
import gov.lbl.srm.StorageResourceManager.TPutRequestFileStatus;
import gov.lbl.srm.StorageResourceManager.TReturnStatus;
import gov.lbl.srm.StorageResourceManager.TSURLReturnStatus;
import gov.lbl.srm.StorageResourceManager.TStatusCode;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.rpc.ServiceException;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedLong;
import org.apache.axis.types.URI.MalformedURIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.transport.HTTPSSender;
import org.globus.axis.util.Util;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.auth.NoAuthorization;
import org.gridlab.gat.io.attributes.GroupPrincipal;
import org.gridlab.gat.io.attributes.PosixFileAttributes;
import org.gridlab.gat.io.attributes.PosixFilePermission;
import org.gridlab.gat.io.attributes.UserPrincipal;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * Non-GAT specific functionality to connect with SRM v2.
 * 
 * @author Max Berger, Thomas Zangerl, Jerome Revillard
 * 
 */
public class SrmConnection {
    private static final String COULD_NOT_LOAD_CREDENTIALS = "Could not load Credentials";
    private static final String COULD_NOT_CREATE_SRM_CONNECTION_DUE_TO_MALFORMED_URI = "Could not create SRM connection due to malformed uri";
    private static final Logger LOGGER = LoggerFactory.getLogger(SrmConnection.class);
    private final ISRM service;
    private URI activeUploadURI;
    private String activeToken;
    
    private static final int MAX_SRM_REQUEST_TRY = 8;

    /**
     * Create a new SRM connection to the given host. All operations called
     * later must include the same host in their URI.
     * 
     * @param host
     *            The Host to connect to.
     * @throws IOException
     *             if the connection fails.
     */
    public SrmConnection(String host, final String proxyPath) throws IOException {
        // Set provider
        LOGGER.debug("Registering httpg transport");
        SimpleProvider provider = new SimpleProvider();
        SimpleTargetedChain c = null;
        c = new SimpleTargetedChain(new HTTPSSender());
        provider.deployTransport("https", c);
        c = new SimpleTargetedChain(new HTTPSender());
        provider.deployTransport("http", c);
        c = new SimpleTargetedChain(
                new org.globus.axis.transport.GSIHTTPSender());
        provider.deployTransport("httpg", c);
        Util.registerTransport();

        // System.out.println(org.apache.axis.constants.Style.RPC);

        SRMServiceLocator locator = new SRMServiceLocator(provider);
        LOGGER.info("getting srm service at " + host);

        try {
            URL.setURLStreamHandlerFactory(new org.globus.net.GlobusURLStreamHandlerFactory());
        } catch (Error e) {
            // ignore
        }
        URI wsEndpoint;
        try {
            wsEndpoint = new URI("httpg", null, host, 8446, "/srm/managerv2",
                    null, null);
            this.service = locator.getsrm(new URL(wsEndpoint.toString()));

            LOGGER.info("Delegating proxy credentials");
            GlobusCredential credential = new GlobusCredential(proxyPath);
            GSSCredential gssCredential = new GlobusGSSCredentialImpl(credential, GSSCredential.INITIATE_AND_ACCEPT);
            ((Stub)this.service)._setProperty(GSIConstants.GSI_CREDENTIALS,gssCredential);
            ((Stub)this.service)._setProperty( GSIConstants.GSI_AUTHORIZATION, NoAuthorization.getInstance());
            ((Stub)this.service)._setProperty( GSIConstants.GSI_MODE, GSIConstants.GSI_MODE_NO_DELEG );
        } catch (MalformedURIException e) {
            LOGGER.warn(e.toString());
            throw new IOException(
                    SrmConnection.COULD_NOT_CREATE_SRM_CONNECTION_DUE_TO_MALFORMED_URI);
        } catch (MalformedURLException e) {
            LOGGER.warn(e.toString());
            throw new IOException(
                    SrmConnection.COULD_NOT_CREATE_SRM_CONNECTION_DUE_TO_MALFORMED_URI);
        } catch (ServiceException e) {
            LOGGER.warn(e.toString());
            throw new IOException("Could not connect to SRM endpoint");
        } catch (GlobusCredentialException e) {
            LOGGER.warn(e.toString());
            throw new IOException(SrmConnection.COULD_NOT_LOAD_CREDENTIALS);
        } catch (GSSException e) {
            LOGGER.warn(e.toString());
            throw new IOException(SrmConnection.COULD_NOT_LOAD_CREDENTIALS);
        }
    }

    public String getTURLForFileDownload(String uriSpec) throws IOException {

        URI uri = new URI(uriSpec);
        String transportURL = "";

        LOGGER.info("Creating get request for URI " + uriSpec);
        SrmPrepareToGetRequest srmPrepToGetReq = new SrmPrepareToGetRequest();
        srmPrepToGetReq.setAuthorizationID("SRMClient");

        // don't check out ANY directories
        // TDirOption dirOpt = new TDirOption(Boolean.FALSE, false, new
        // Integer(0));
        // dirOpt is not supported!
        TDirOption dirOpt = null;

        TGetFileRequest getFileRequest = new TGetFileRequest(uri, dirOpt);
        srmPrepToGetReq.setArrayOfFileRequests(new ArrayOfTGetFileRequest(
                new TGetFileRequest[] { getFileRequest }));

        LOGGER.info("Sending get request");

        SrmPrepareToGetResponse response = service
                .srmPrepareToGet(srmPrepToGetReq);

        String requestToken = response.getRequestToken();
        LOGGER.info("Received the following request token: " + requestToken);

        TReturnStatus status = response.getReturnStatus();
        TGetRequestFileStatus fileStatus = response.getArrayOfFileStatuses()
                .getStatusArray(0);

        LOGGER.info("File status for SURL "
                + fileStatus.getStatus().getStatusCode() + " "
                + fileStatus.getStatus().getExplanation());

        long period = 500;

        SrmStatusOfGetRequestRequest statusRequest = new SrmStatusOfGetRequestRequest();
        statusRequest.setRequestToken(requestToken);
        statusRequest.setArrayOfSourceSURLs(new ArrayOfAnyURI(new URI[] { uri }));
        
        if ( !status.getStatusCode().equals( TStatusCode.SRM_REQUEST_QUEUED ) ) {
        	String log= "Status: " + status.getStatusCode()+
    		"\n"+"Status exp: " + status.getExplanation()+
    		"\n"+"FileStatus: " + fileStatus.getStatus().getStatusCode()+
    		"\n"+"FileStatus exp: "+ fileStatus.getStatus().getExplanation();
        	throw new IOException("SRM Get Request error: "+log);
        }

        int retryCount = 0;
        do {
        	if(retryCount > 0){
	        	try {
	                Thread.sleep(period * retryCount);
	            } catch (InterruptedException e) {}
            }
        	SrmStatusOfGetRequestResponse statusResponse = service.srmStatusOfGetRequest(statusRequest);
            status = statusResponse.getReturnStatus();
            fileStatus = statusResponse.getArrayOfFileStatuses().getStatusArray(0);

            String log= "Status: " + status.getStatusCode()+
            		"\n"+"Status exp: " + status.getExplanation()+
            		"\n"+"FileStatus: " + fileStatus.getStatus().getStatusCode()+
            		"\n"+"FileStatus exp: "+ fileStatus.getStatus().getExplanation();
            		
            LOGGER.info(log);
        	
            retryCount++;
		} while ((!( status.getStatusCode().equals( TStatusCode.SRM_DONE ) || status.getStatusCode().equals( TStatusCode.SRM_SUCCESS ))) 
				&& retryCount <= MAX_SRM_REQUEST_TRY);
        
        if (status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            if (TStatusCode.SRM_FILE_PINNED.equals(fileStatus.getStatus().getStatusCode())) {
                transportURL = fileStatus.getTransferURL().toString();
                LOGGER.info("Received transfer URL: " + transportURL);
            }
        } else{
        	String log= "Status: " + status.getStatusCode()+
    		"\n"+"Status exp: " + status.getExplanation()+
    		"\n"+"FileStatus: " + fileStatus.getStatus().getStatusCode()+
    		"\n"+"FileStatus exp: "+ fileStatus.getStatus().getExplanation();
            throw new IOException("SRM Get Request error: "+log);
        }
        return transportURL;
    }

    public URI getTURLForFileUpload(String src, String dest) throws IOException {
        URI uri = new URI(dest);
        URI transportURL = null;
        File localFile = new File(src);

        LOGGER.info("Creating put request for URI " + uri);
        SrmPrepareToPutRequest srmPrepToPutReq = new SrmPrepareToPutRequest();
        srmPrepToPutReq.setAuthorizationID("SRMClient");

        UnsignedLong expFileSize = new UnsignedLong(localFile.length());
        TPutFileRequest putFileRequest = new TPutFileRequest(uri, expFileSize);
        TPutFileRequest[] putFileRequests = new TPutFileRequest[] { putFileRequest };

        srmPrepToPutReq.setArrayOfFileRequests(new ArrayOfTPutFileRequest(
                putFileRequests));
        srmPrepToPutReq
                .setUserRequestDescription("Some user request description");
        srmPrepToPutReq.setDesiredFileLifeTime(60);
        srmPrepToPutReq.setDesiredPinLifeTime(60);

        LOGGER.info("Sending put request");
        SrmPrepareToPutResponse response = service
                .srmPrepareToPut(srmPrepToPutReq);

        String requestToken = response.getRequestToken();
        this.activeToken = requestToken;

        LOGGER.info("Received the following request token: " + requestToken);
        TPutRequestFileStatus fileStatus = response.getArrayOfFileStatuses()
                .getStatusArray(0);
        TReturnStatus status = response.getReturnStatus();

        LOGGER.info("Temporary file status for SURL "
                + fileStatus.getStatus().getStatusCode());

        long period = 1000L;
        SrmStatusOfPutRequestRequest realRequest = new SrmStatusOfPutRequestRequest();
        realRequest.setRequestToken(requestToken);

        realRequest.setArrayOfTargetSURLs(new ArrayOfAnyURI(new URI[] { uri }));

        while ((status.getStatusCode().equals(TStatusCode.SRM_REQUEST_QUEUED))
                || (status.getStatusCode()
                        .equals(TStatusCode.SRM_REQUEST_INPROGRESS))) {
            try {
                Thread.sleep(period);
            } catch (InterruptedException e) {
            }

            SrmStatusOfPutRequestResponse realResponse = service
                    .srmStatusOfPutRequest(realRequest);
            status = realResponse.getReturnStatus();

            LOGGER.info("Status: " + status.getStatusCode());
            LOGGER.info("Status exp: " + status.getExplanation());

            if (realResponse.getArrayOfFileStatuses() != null
                    && realResponse.getArrayOfFileStatuses().getStatusArray().length > 0) {
                fileStatus = realResponse.getArrayOfFileStatuses()
                        .getStatusArray(0);
            }

            LOGGER
                    .info("FileStatus: "
                            + fileStatus.getStatus().getStatusCode());
            LOGGER.info("FileStatus exp: "
                    + fileStatus.getStatus().getExplanation());

        }

        if (status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            if (TStatusCode.SRM_SPACE_AVAILABLE.equals(fileStatus.getStatus()
                    .getStatusCode())) {
                transportURL = fileStatus.getTransferURL();
                LOGGER.info("Received transfer URL: " + transportURL);
            }
        } else
            throw new IOException(status.getStatusCode() + ": "
                    + fileStatus.getStatus().getStatusCode());
        this.activeUploadURI = uri;
        return transportURL;
    }

    public void finalizeFileUpload() throws IOException {
        SrmPutDoneRequest putDoneRequest = new SrmPutDoneRequest();
        putDoneRequest.setArrayOfSURLs(new ArrayOfAnyURI(
                new URI[] { activeUploadURI }));
        putDoneRequest.setAuthorizationID("SRMClient");
        putDoneRequest.setRequestToken(activeToken);

        LOGGER.info("Sending put-done request for URI " + activeUploadURI);

        SrmPutDoneResponse response = service.srmPutDone(putDoneRequest);

        TSURLReturnStatus fileStatus = null;
        if (response.getArrayOfFileStatuses() != null
                && response.getArrayOfFileStatuses().getStatusArray().length > 0) {
            fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
            LOGGER.info("Received file status "
                    + fileStatus.getStatus().getStatusCode());
        }

        TReturnStatus status = response.getReturnStatus();
        LOGGER.info("Received status " + status.getStatusCode());
        this.activeToken = null;
        this.activeUploadURI = null;
    }

    public void removeFile(String uri) throws IOException {
        SrmRmRequest removalRequest = new SrmRmRequest();
        removalRequest.setArrayOfSURLs(new ArrayOfAnyURI(new URI[] { new URI(
                uri) }));
        SrmRmResponse response = null;

        LOGGER.info("Invoking delete request for URI " + uri);
        response = service.srmRm(removalRequest);

        if (response.getArrayOfFileStatuses() != null
                && response.getArrayOfFileStatuses().getStatusArray().length > 0) {
            TSURLReturnStatus fileStatus = response.getArrayOfFileStatuses()
                    .getStatusArray(0);
            LOGGER.info("file status code "
                    + fileStatus.getStatus().getStatusCode());
        }

        TReturnStatus returnStatus = response.getReturnStatus();

        LOGGER.info("Return status code " + returnStatus.getStatusCode());
        if (!returnStatus.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            throw new IOException(returnStatus.getStatusCode().toString());
        }
    }

    public SRMPosixFile ls(String uri) throws IOException {
    	SrmLsRequest srmLsRequest = new SrmLsRequest();
    	srmLsRequest.setArrayOfSURLs(new ArrayOfAnyURI(new URI[] { new URI(uri) }));
    	srmLsRequest.setAllLevelRecursive(false);
    	srmLsRequest.setFullDetailedList(true);
    	LOGGER.info("Invoking getPermissions request for URI " + uri);
    	SrmLsResponse response = service.srmLs(srmLsRequest);
    	TReturnStatus returnStatus = response.getReturnStatus();
    	LOGGER.info("Return status code " + returnStatus.getStatusCode());
    	if (!returnStatus.getStatusCode().equals(TStatusCode.SRM_SUCCESS)){
    		throw new IOException(returnStatus.getExplanation()+" ("+returnStatus.getStatusCode()+")");
    	}
    	return new SRMPosixFile(response.getDetails().getPathDetailArray()[0]);
    }
    
    public void setPermissions(String uri, TPermissionType tPermissionType, 
    		TPermissionMode ownerTPermissionMode,
    		ArrayOfTGroupPermission arrayOfTGroupPermissions, 
    		TPermissionMode otherTPermissionMode,
    		ArrayOfTUserPermission arrayOfTUserPermissions) throws IOException {
        SrmSetPermissionRequest setPermissionRequest = new SrmSetPermissionRequest();
        setPermissionRequest.setSURL(new URI(uri));
        setPermissionRequest.setPermissionType(tPermissionType);
        setPermissionRequest.setOwnerPermission(ownerTPermissionMode);
        setPermissionRequest.setOtherPermission(otherTPermissionMode);
        setPermissionRequest.setArrayOfUserPermissions(arrayOfTUserPermissions);
        setPermissionRequest.setArrayOfGroupPermissions(arrayOfTGroupPermissions);
        
        SrmSetPermissionResponse response = null;

        LOGGER.info("Invoking setPermissions request for URI " + uri);
        response = service.srmSetPermission(setPermissionRequest);
        
        TReturnStatus returnStatus = response.getReturnStatus();
    	LOGGER.info("Return status code " + returnStatus.getStatusCode());
    	if (!returnStatus.getStatusCode().equals(TStatusCode.SRM_SUCCESS)){
    		throw new IOException(returnStatus.getExplanation()+" ("+returnStatus.getStatusCode()+")");
    	}
    }
    
    public static class SRMPosixFile implements PosixFileAttributes{
    	final private TMetaDataPathDetail tMetaDataPathDetail;
    	final private Set<PosixFilePermission> perms;
    	final private String owner;
    	final private String group;
    	final private TPermissionMode ownerTPermissionMode;
    	final private TPermissionMode groupTPermissionMode;
    	final private TPermissionMode otherTPermissionMode;

		protected SRMPosixFile(TMetaDataPathDetail tMetaDataPathDetail) throws IOException {    
    		this.tMetaDataPathDetail = tMetaDataPathDetail;
    		
    		this.perms = new HashSet<PosixFilePermission>();
    		
    		this.owner = tMetaDataPathDetail.getOwnerPermission().getUserID();
    		ownerTPermissionMode = tMetaDataPathDetail.getOwnerPermission().getMode();
    		if(ownerTPermissionMode.toString().contains("R")){
    			perms.add(PosixFilePermission.OWNER_READ);
    		}
    		if(ownerTPermissionMode.toString().contains("W")){
    			perms.add(PosixFilePermission.OWNER_WRITE);
    		}
    		if(ownerTPermissionMode.toString().contains("X")){
    			perms.add(PosixFilePermission.OWNER_EXECUTE);
    		}
    		
    		this.group = tMetaDataPathDetail.getGroupPermission().getGroupID();
    		groupTPermissionMode = tMetaDataPathDetail.getGroupPermission().getMode();
    		if(groupTPermissionMode.toString().contains("R")){
    			perms.add(PosixFilePermission.GROUP_READ);
    		}
    		if(groupTPermissionMode.toString().contains("W")){
    			perms.add(PosixFilePermission.GROUP_WRITE);
    		}
    		if(groupTPermissionMode.toString().contains("X")){
    			perms.add(PosixFilePermission.GROUP_EXECUTE);
    		}
    		
    		otherTPermissionMode = tMetaDataPathDetail.getOtherPermission();
    		if(otherTPermissionMode.toString().contains("R")){
    			perms.add(PosixFilePermission.OTHERS_READ);
    		}
    		if(otherTPermissionMode.toString().contains("W")){
    			perms.add(PosixFilePermission.OTHERS_WRITE);
    		}
    		if(otherTPermissionMode.toString().contains("X")){
    			perms.add(PosixFilePermission.OTHERS_EXECUTE);
    		}
		}

		public GroupPrincipal group() {
			return new SRMGroup(group);
		}

		public UserPrincipal owner() {
			return new SRMUser(owner);
		}

		public Set<PosixFilePermission> permissions() {
			return perms;
		}

		public long creationTime() {
			return (tMetaDataPathDetail.getCreatedAtTime() != null ? tMetaDataPathDetail.getCreatedAtTime().getTimeInMillis(): -1L);
		}

		public Object fileKey() {
			return null;
		}

		public boolean isDirectory() {
			return tMetaDataPathDetail.getType().equals(TFileType.DIRECTORY);
		}

		public boolean isOther() {
			return (!isDirectory() && !isRegularFile() && !isOther());
		}

		public boolean isRegularFile() {
			return tMetaDataPathDetail.getType().equals(TFileType.FILE);
		}

		public boolean isSymbolicLink() {
			return tMetaDataPathDetail.getType().equals(TFileType.LINK);
		}

		public long lastAccessTime() {
			return -1L;
		}

		public long lastModifiedTime() {
			return (tMetaDataPathDetail.getLastModificationTime() != null ? tMetaDataPathDetail.getLastModificationTime().getTimeInMillis(): -1L);
		}

		public int linkCount() {
			return 0;
		}

		public TimeUnit resolution() {
			return TimeUnit.MILLISECONDS;
		}

		public long size() {
			return (tMetaDataPathDetail.getSize() != null ? tMetaDataPathDetail.getSize().longValue(): -1L);
		}
    	
		public TPermissionMode getOwnerTPermissionMode() {
			return ownerTPermissionMode;
		}

		public TPermissionMode getGroupTPermissionMode() {
			return groupTPermissionMode;
		}

		public TPermissionMode getOtherTPermissionMode() {
			return otherTPermissionMode;
		}
		
    }
}