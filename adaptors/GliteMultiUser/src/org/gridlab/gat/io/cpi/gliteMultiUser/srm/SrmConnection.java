package org.gridlab.gat.io.cpi.gliteMultiUser.srm;

import gov.lbl.srm.StorageResourceManager.ArrayOfAnyURI;
import gov.lbl.srm.StorageResourceManager.ArrayOfString;
import gov.lbl.srm.StorageResourceManager.ArrayOfTGetFileRequest;
import gov.lbl.srm.StorageResourceManager.ArrayOfTGroupPermission;
import gov.lbl.srm.StorageResourceManager.ArrayOfTPutFileRequest;
import gov.lbl.srm.StorageResourceManager.ArrayOfTUserPermission;
import gov.lbl.srm.StorageResourceManager.ISRM;
import gov.lbl.srm.StorageResourceManager.SRMServiceLocator;
import gov.lbl.srm.StorageResourceManager.SrmLsRequest;
import gov.lbl.srm.StorageResourceManager.SrmLsResponse;
import gov.lbl.srm.StorageResourceManager.SrmMkdirRequest;
import gov.lbl.srm.StorageResourceManager.SrmMkdirResponse;
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
import gov.lbl.srm.StorageResourceManager.TOverwriteMode;
import gov.lbl.srm.StorageResourceManager.TPermissionMode;
import gov.lbl.srm.StorageResourceManager.TPermissionType;
import gov.lbl.srm.StorageResourceManager.TPutFileRequest;
import gov.lbl.srm.StorageResourceManager.TPutRequestFileStatus;
import gov.lbl.srm.StorageResourceManager.TReturnStatus;
import gov.lbl.srm.StorageResourceManager.TSURLReturnStatus;
import gov.lbl.srm.StorageResourceManager.TStatusCode;
import gov.lbl.srm.StorageResourceManager.TTransferParameters;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.rpc.ServiceException;

import org.apache.axis.MessageContext;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Call;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedLong;
import org.apache.axis.types.URI.MalformedURIException;
import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.transport.GSIHTTPSender;
import org.globus.axis.transport.HTTPSSender;
import org.globus.axis.util.Util;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.auth.NoAuthorization;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.io.attributes.GroupPrincipal;
import org.gridlab.gat.io.attributes.PosixFileAttributes;
import org.gridlab.gat.io.attributes.PosixFilePermission;
import org.gridlab.gat.io.attributes.UserPrincipal;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Non-GAT specific functionality to connect with SRM v2.
 * 
 * @author Max Berger, Thomas Zangerl, Jerome Revillard
 * @author Stefan Bozic
 * 
 */
public class SrmConnection {
	/** Error message */
	private static final String COULD_NOT_LOAD_CREDENTIALS = "Could not load Credentials";

	/** Error message */
	private static final String COULD_NOT_CREATE_SRM_CONNECTION_DUE_TO_MALFORMED_URI = "Could not create SRM connection due to malformed uri";

	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(SrmConnection.class);

	/** Storage Resource Manager Service class */
	private final ISRM service;

	/** The {@link URI} for the upload */
	private URI activeUploadURI;

	/** The security token */
	private String activeToken;

	/** Max. numbers of request retries */
	private static final int MAX_SRM_REQUEST_TRY = 8;

	/**
	 * Create a new SRM connection to the given host. All operations called later must include the same host in their
	 * URI.
	 * 
	 * @param host The Host to connect to.
	 * @param proxyPath the path to the proxy credentials
	 * @throws IOException if the connection fails.
	 */
	public SrmConnection(String host, final String proxyPath) throws IOException {

		// Changing classloader
		ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
		// Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

		LOGGER.info("SavedClassloader: " + savedClassLoader);
		LOGGER.info("CurrentClassloader: " + this.getClass().getClassLoader());
		LOGGER.info("URLClassloader: " + URL.class.getClassLoader());
		LOGGER.info("UtilClassloader: " + Util.class.getClassLoader());

		// Set URL Handler for httpg
		HttpgURLStreamHandlerFactory httpgfac = new HttpgURLStreamHandlerFactory();

		try {
			URL.setURLStreamHandlerFactory(httpgfac);
			LOGGER.info("URLClassloader: " + URL.class.getClassLoader());
		} catch (Error e) {
			//In an application server this exception always accurs. So log only...
			LOGGER.debug("Cannot set HTTPG scheme for URLs", e);
		}

		// Set provider
		LOGGER.debug("Registering httpg transport");
		SimpleProvider provider = new SimpleProvider();
		SimpleTargetedChain c = null;

		c = new SimpleTargetedChain(new HTTPSSender());
		provider.deployTransport("https", c);

		c = new SimpleTargetedChain(new HTTPSender());
		provider.deployTransport("http", c);

		c = new SimpleTargetedChain(new GSIHTTPSender());
		provider.deployTransport("httpg", c);

		AxisClient ac = new AxisClient(provider);
		SRMServiceLocator locator = new SRMServiceLocator(ac.getClientEngine().getConfig());
		locator.setEngine(ac);

		// SRMServiceLocator locator = new SRMServiceLocator(provider);
		LOGGER.info("getting srm service at " + host);

		URI wsEndpoint;
		try {
			wsEndpoint = new URI("httpg", null, host, 8443, "/srm/managerv2", null, null);
			URL serviceUrl = new URL(wsEndpoint.getScheme(), wsEndpoint.getHost(), wsEndpoint.getPort(), wsEndpoint
					.getPath(), new org.globus.net.protocol.httpg.Handler());

			this.service = locator.getsrm(serviceUrl);

			LOGGER.info("Delegating proxy credentials");
			GlobusCredential credential = new GlobusCredential(proxyPath);
			GSSCredential gssCredential = new GlobusGSSCredentialImpl(credential, GSSCredential.INITIATE_AND_ACCEPT);
			((Stub) this.service)._setProperty(GSIConstants.GSI_CREDENTIALS, gssCredential);
			((Stub) this.service)._setProperty(GSIConstants.GSI_AUTHORIZATION, NoAuthorization.getInstance());
			((Stub) this.service)._setProperty(GSIConstants.GSI_MODE, GSIConstants.GSI_MODE_NO_DELEG);

			Util.reregisterTransport();
			if (MessageContext.getCurrentContext() != null)
				MessageContext.getCurrentContext().setClassLoader(this.getClass().getClassLoader());
			if (MessageContext.getCurrentContext() != null)
				MessageContext.getCurrentContext().setTransportName("httpg");

			Call.initialize();
			Call.addTransportPackage("org.globus.axis.transport");
			Call.setTransportForProtocol("httpg", org.globus.axis.transport.GSIHTTPTransport.class);

		} catch (MalformedURIException e) {
			LOGGER.warn(e.toString());
			throw new IOException(SrmConnection.COULD_NOT_CREATE_SRM_CONNECTION_DUE_TO_MALFORMED_URI);
		} catch (MalformedURLException e) {
			LOGGER.warn(e.toString());
			throw new IOException(SrmConnection.COULD_NOT_CREATE_SRM_CONNECTION_DUE_TO_MALFORMED_URI);
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

		// finally {
		// // Restoring original, previously saved, classloader
		// Thread.currentThread().setContextClassLoader(savedClassLoader);
		// }
	}

	/**
	 * Returns the transport url for the file to download.
	 * 
	 * @param uriSpec the uri of the file to download
	 * @return the transport url for the file to download.
	 * 
	 * @throws IOException an exception that might occurs
	 */
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

		// Transfer Parameters
		TTransferParameters transferParameters = new TTransferParameters();
		transferParameters.setArrayOfTransferProtocols(new ArrayOfString(new String[] { "gsiftp" }));

		TGetFileRequest getFileRequest = new TGetFileRequest(uri, dirOpt);
		srmPrepToGetReq.setArrayOfFileRequests(new ArrayOfTGetFileRequest(new TGetFileRequest[] { getFileRequest }));
		srmPrepToGetReq.setTransferParameters(transferParameters);

		LOGGER.info("Sending get request");

		SrmPrepareToGetResponse response = service.srmPrepareToGet(srmPrepToGetReq);

		String requestToken = response.getRequestToken();
		LOGGER.info("Received the following request token: " + requestToken);

		TReturnStatus status = response.getReturnStatus();
		TGetRequestFileStatus fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);

		LOGGER.info("File status for SURL " + fileStatus.getStatus().getStatusCode() + " "
				+ fileStatus.getStatus().getExplanation());

		long period = 500;

		SrmStatusOfGetRequestRequest statusRequest = new SrmStatusOfGetRequestRequest();
		statusRequest.setRequestToken(requestToken);
		statusRequest.setArrayOfSourceSURLs(new ArrayOfAnyURI(new URI[] { uri }));

		if (!status.getStatusCode().equals(TStatusCode.SRM_REQUEST_QUEUED)) {
			String log = "Status: " + status.getStatusCode() + "\n" + "Status exp: " + status.getExplanation() + "\n"
					+ "FileStatus: " + fileStatus.getStatus().getStatusCode() + "\n" + "FileStatus exp: "
					+ fileStatus.getStatus().getExplanation();
			throw new IOException("SRM Get Request error: " + log);
		}

		int retryCount = 0;
		do {
			if (retryCount > 0) {
				try {
					Thread.sleep(period * retryCount);
				} catch (InterruptedException e) {
				}
			}
			SrmStatusOfGetRequestResponse statusResponse = service.srmStatusOfGetRequest(statusRequest);
			status = statusResponse.getReturnStatus();
			fileStatus = statusResponse.getArrayOfFileStatuses().getStatusArray(0);

			String log = "Status: " + status.getStatusCode() + "\n" + "Status exp: " + status.getExplanation() + "\n"
					+ "FileStatus: " + fileStatus.getStatus().getStatusCode() + "\n" + "FileStatus exp: "
					+ fileStatus.getStatus().getExplanation();

			LOGGER.info(log);

			retryCount++;
		} while ((!(status.getStatusCode().equals(TStatusCode.SRM_DONE) || status.getStatusCode().equals(
				TStatusCode.SRM_SUCCESS)))
				&& retryCount <= MAX_SRM_REQUEST_TRY);

		if (status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
			if (TStatusCode.SRM_FILE_PINNED.equals(fileStatus.getStatus().getStatusCode())) {
				transportURL = fileStatus.getTransferURL().toString();
				LOGGER.info("Received transfer URL: " + transportURL);
			}
		} else {
			String log = "Status: " + status.getStatusCode() + "\n" + "Status exp: " + status.getExplanation() + "\n"
					+ "FileStatus: " + fileStatus.getStatus().getStatusCode() + "\n" + "FileStatus exp: "
					+ fileStatus.getStatus().getExplanation();
			throw new IOException("SRM Get Request error: " + log);
		}
		return transportURL;
	}

	/**
	 * Returns the transport url for the file to upload.
	 * 
	 * @param src the url of the source file
	 * @param dest the url of the destination file
	 * 
	 * @return the uri for the file to upload
	 * 
	 * @throws IOException an exception that might occurs
	 */
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

		srmPrepToPutReq.setArrayOfFileRequests(new ArrayOfTPutFileRequest(putFileRequests));
		srmPrepToPutReq.setOverwriteOption(TOverwriteMode.ALWAYS);
		srmPrepToPutReq.setAuthorizationID("SRMClient");

		// //Transfer Parameters
		TTransferParameters transferParameters = new TTransferParameters();
		// transferParameters.setConnectionType(TConnectionType.WAN);
		// transferParameters.setAccessPattern(TAccessPattern.TRANSFER_MODE);
		transferParameters.setArrayOfTransferProtocols(new ArrayOfString(
		// new String[] { "srm", "gsiftp", "dcap", "http" }));
				new String[] { "gsiftp" }));

		srmPrepToPutReq.setTransferParameters(transferParameters);
		LOGGER.info("Sending put request");
		SrmPrepareToPutResponse response = service.srmPrepareToPut(srmPrepToPutReq);

		String requestToken = response.getRequestToken();
		this.activeToken = requestToken;

		LOGGER.info("Received the following request token: " + requestToken);

		// FIXME I dont know why the Thread has to sleep here. The code is from Max Berger
		// sleep 4 sec like in the srmcp command
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e1) {
			LOGGER.warn("Sleep interrupted", e1);
		}

		TPutRequestFileStatus fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
		TReturnStatus status = response.getReturnStatus();

		LOGGER.info("Temporary file status for SURL " + fileStatus.getStatus().getStatusCode());

		long period = 1000L;
		SrmStatusOfPutRequestRequest realRequest = new SrmStatusOfPutRequestRequest();
		realRequest.setRequestToken(requestToken);

		realRequest.setArrayOfTargetSURLs(new ArrayOfAnyURI(new URI[] { uri }));

		while ((status.getStatusCode().equals(TStatusCode.SRM_REQUEST_QUEUED))
				|| (status.getStatusCode().equals(TStatusCode.SRM_REQUEST_INPROGRESS))) {
			try {
				Thread.sleep(period);
			} catch (InterruptedException e) {
			}

			SrmStatusOfPutRequestResponse realResponse = service.srmStatusOfPutRequest(realRequest);
			status = realResponse.getReturnStatus();

			LOGGER.info("Status: " + status.getStatusCode());
			LOGGER.info("Status exp: " + status.getExplanation());

			if (realResponse.getArrayOfFileStatuses() != null
					&& realResponse.getArrayOfFileStatuses().getStatusArray().length > 0) {
				fileStatus = realResponse.getArrayOfFileStatuses().getStatusArray(0);
			}

			LOGGER.info("FileStatus: " + fileStatus.getStatus().getStatusCode());
			LOGGER.info("FileStatus exp: " + fileStatus.getStatus().getExplanation());
			LOGGER.info("EstimatedWaitTime: " + fileStatus.getEstimatedWaitTime());
			LOGGER.info("RemainingFileLifetime: " + fileStatus.getRemainingFileLifetime());
			LOGGER.info("RemainingPinLifetime: " + fileStatus.getRemainingPinLifetime());
		}

		if (status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
			if (TStatusCode.SRM_SPACE_AVAILABLE.equals(fileStatus.getStatus().getStatusCode())) {
				transportURL = fileStatus.getTransferURL();
				LOGGER.info("Received transfer URL: " + transportURL);
			}
		} else {
			LOGGER.info("The status returned is: " + status.getExplanation());
			throw new IOException(status.getStatusCode() + ": " + fileStatus.getStatus().getStatusCode());
		}

		this.activeUploadURI = uri;
		return transportURL;
	}

	/**
	 * Finalize the file upload. Sends a put-done request and sets the active token to <code>null</code>.
	 * 
	 * @throws IOException an excpetion that might occurs
	 */
	public void finalizeFileUpload() throws IOException {
		SrmPutDoneRequest putDoneRequest = new SrmPutDoneRequest();
		putDoneRequest.setArrayOfSURLs(new ArrayOfAnyURI(new URI[] { activeUploadURI }));
		putDoneRequest.setAuthorizationID("SRMClient");
		putDoneRequest.setRequestToken(activeToken);

		LOGGER.info("Sending put-done request for URI " + activeUploadURI);

		SrmPutDoneResponse response = service.srmPutDone(putDoneRequest);

		TSURLReturnStatus fileStatus = null;
		if (response.getArrayOfFileStatuses() != null && response.getArrayOfFileStatuses().getStatusArray().length > 0) {
			fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
			LOGGER.info("Received file status " + fileStatus.getStatus().getStatusCode());
		}

		TReturnStatus status = response.getReturnStatus();
		LOGGER.info("Received status " + status.getStatusCode());
		this.activeToken = null;
		this.activeUploadURI = null;
	}

	/**
	 * Removes a file with the given uri from the SE.
	 * 
	 * @param uri the uri of the file to remove
	 * @throws IOException an exception that might occurs
	 */
	public void removeFile(String uri) throws IOException {
		SrmRmRequest removalRequest = new SrmRmRequest();
		removalRequest.setArrayOfSURLs(new ArrayOfAnyURI(new URI[] { new URI(uri) }));
		SrmRmResponse response = null;

		LOGGER.info("Invoking delete request for URI " + uri);
		response = service.srmRm(removalRequest);

		if (response.getArrayOfFileStatuses() != null && response.getArrayOfFileStatuses().getStatusArray().length > 0) {
			TSURLReturnStatus fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
			LOGGER.info("file status code " + fileStatus.getStatus().getStatusCode());
		}

		TReturnStatus returnStatus = response.getReturnStatus();

		LOGGER.info("Return status code " + returnStatus.getStatusCode());
		if (!returnStatus.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
			throw new IOException(returnStatus.getStatusCode().toString());
		}
	}

	/**
	 * Invokes a ls with the given uri.
	 * 
	 * @param uri the uri to list
	 * 
	 * @return a SRMPosixFile that holds the listing
	 * @throws IOException an exception that might occurs
	 */
	public SRMPosixFile ls(String uri) throws IOException {
		SrmLsRequest srmLsRequest = new SrmLsRequest();
		srmLsRequest.setArrayOfSURLs(new ArrayOfAnyURI(new URI[] { new URI(uri) }));
		srmLsRequest.setAllLevelRecursive(false);
		srmLsRequest.setFullDetailedList(true);
		LOGGER.info("Invoking getPermissions request for URI " + uri);
		SrmLsResponse response = service.srmLs(srmLsRequest);
		TReturnStatus returnStatus = response.getReturnStatus();
		LOGGER.info("Return status code " + returnStatus.getStatusCode());
		if (!returnStatus.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
			throw new IOException(returnStatus.getExplanation() + " (" + returnStatus.getStatusCode() + ")");
		}

		return new SRMPosixFile(response.getDetails().getPathDetailArray()[0]);
	}

	/**
	 * Invokes a ls with the given uri.
	 * 
	 * @param uri the uri to list
	 * 
	 * @return a SRMPosixFile that holds the listing
	 * @throws IOException an exception that might occurs
	 */
	public List<String> realLs(String uri) throws IOException {
		URI requestUri = new URI(uri);
		
		SrmLsRequest srmLsRequest = new SrmLsRequest();
		srmLsRequest.setArrayOfSURLs(new ArrayOfAnyURI(new URI[] { requestUri }));
		srmLsRequest.setAllLevelRecursive(false);
		srmLsRequest.setFullDetailedList(true);
		LOGGER.info("Invoking getPermissions request for URI " + uri);
		SrmLsResponse response = service.srmLs(srmLsRequest);
		TReturnStatus returnStatus = response.getReturnStatus();
		LOGGER.info("Return status code " + returnStatus.getStatusCode());
		if (!returnStatus.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
			LOGGER.info(returnStatus.getStatusCode() + returnStatus.getExplanation());
			throw new IOException(returnStatus.getExplanation() + " (" + returnStatus.getStatusCode() + ")");
		}

		List<String> paths = new ArrayList<String>();

		for (TMetaDataPathDetail detail : response.getDetails().getPathDetailArray()) {
			//paths.add(fileName);

			for (TMetaDataPathDetail subDetail : detail.getArrayOfSubPaths().getPathDetailArray()) {
				String fileName = subDetail.getPath().replace(requestUri.getPath(), "");
				paths.add(fileName);
			}
		}

		return paths;
	}

	/**
	 * Creates a new directory on a SRM resource.
	 * 
	 * @param uri the URI which represents the directory to create
	 * @return <code>true</code> if the directory has been created successfully.
	 * @throws GATInvocationException an exception that might occurs
	 */
	public boolean mkDir(String uri) throws GATInvocationException {
		boolean sucessful = false;

		LOGGER.debug("mkDir : " + uri.toString());

		// Extract the path and the new directory name from the uri
		URI newDirUri;
		try {
			newDirUri = new URI(uri);
		} catch (MalformedURIException e1) {
			throw new GATInvocationException("Malformed URI for new directory", e1);
		}

		String newDirPath = newDirUri.getPath();

		if (newDirPath.endsWith("/")) {
			newDirPath = newDirPath.substring(0, newDirPath.length() - 1);
			try {
				newDirUri.setPath(newDirPath);
			} catch (MalformedURIException e) {
				e.printStackTrace();
			}
		}

		LOGGER.debug("Srm createDirectory() : " + newDirPath);

		// TODO Implement exists() and throw an exception if the directory already exists
		// if (exists(filePath)) {
		// throw new GATInvocationException("SRM create dir: dir already exists " + filePath);
		// }

		try {
			SrmMkdirRequest srmMkdirRequest = new SrmMkdirRequest();
			srmMkdirRequest.setSURL(newDirUri);
			SrmMkdirResponse srmMkdirResponse = service.srmMkdir(srmMkdirRequest);

			if (srmMkdirResponse != null) {
				if (srmMkdirResponse.getReturnStatus() != null) {
					if (!srmMkdirResponse.getReturnStatus().getStatusCode().equals(TStatusCode.SRM_SUCCESS)
							&& !srmMkdirResponse.getReturnStatus().getStatusCode().equals(TStatusCode.SRM_DONE)
							&& !srmMkdirResponse.getReturnStatus().getStatusCode().equals(
									TStatusCode.SRM_REQUEST_INPROGRESS)
							&& !srmMkdirResponse.getReturnStatus().getStatusCode().equals(
									TStatusCode.SRM_REQUEST_QUEUED)) {
						LOGGER.error("SrmFileManager createDirectory() failed: status code: "
								+ srmMkdirResponse.getReturnStatus().getStatusCode().toString() + ", explanation: "
								+ srmMkdirResponse.getReturnStatus().getExplanation());
						throw new GATInvocationException("SRM mkDir failed: status code: "
								+ srmMkdirResponse.getReturnStatus().getStatusCode().toString() + ", explanation: "
								+ srmMkdirResponse.getReturnStatus().getExplanation());
					}

					LOGGER.info("mkdir() successful: status code: "
							+ srmMkdirResponse.getReturnStatus().getStatusCode().toString() + ", explanation: "
							+ srmMkdirResponse.getReturnStatus().getExplanation());
					sucessful = true;
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new GATInvocationException("SRM create directory error:" + uri, e);
		}

		return sucessful;
	}

	/**
	 * Changes the permission to a given uri.
	 * 
	 * @param uri the uri where to changes permission
	 * @param tPermissionType the type of permission
	 * @param ownerTPermissionMode the owner permissions
	 * @param arrayOfTGroupPermissions the group permissions
	 * @param otherTPermissionMode the other permissions
	 * @param arrayOfTUserPermissions the array of user permissions
	 * 
	 * @throws IOException an exception that might occurrs
	 */
	public void setPermissions(String uri, TPermissionType tPermissionType, TPermissionMode ownerTPermissionMode,
			ArrayOfTGroupPermission arrayOfTGroupPermissions, TPermissionMode otherTPermissionMode,
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
		if (!returnStatus.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
			throw new IOException(returnStatus.getExplanation() + " (" + returnStatus.getStatusCode() + ")");
		}
	}

	/**
	 * Implementation of the {@link PosixFileAttributes} interface that defines the look of a POSIX compatible file.
	 * 
	 * @author Stefan Bozic
	 */
	public static class SRMPosixFile implements PosixFileAttributes {

		/** Meta data for the file. */
		final private TMetaDataPathDetail tMetaDataPathDetail;

		/** the file permission */
		final private Set<PosixFilePermission> perms;

		/** the owner of the file */
		final private String owner;

		/** the group of the file */
		final private String group;

		/** the permissions for the owner */
		final private TPermissionMode ownerTPermissionMode;

		/** the permissions for the group */
		final private TPermissionMode groupTPermissionMode;

		/** the permissions for others */
		final private TPermissionMode otherTPermissionMode;

		/**
		 * Constructor
		 * 
		 * @param tMetaDataPathDetail metadata path details
		 * @throws IOException an exception that might occurs
		 */
		protected SRMPosixFile(TMetaDataPathDetail tMetaDataPathDetail) throws IOException {
			this.tMetaDataPathDetail = tMetaDataPathDetail;

			this.perms = new HashSet<PosixFilePermission>();

			this.owner = tMetaDataPathDetail.getOwnerPermission().getUserID();
			ownerTPermissionMode = tMetaDataPathDetail.getOwnerPermission().getMode();
			if (ownerTPermissionMode.toString().contains("R")) {
				perms.add(PosixFilePermission.OWNER_READ);
			}
			if (ownerTPermissionMode.toString().contains("W")) {
				perms.add(PosixFilePermission.OWNER_WRITE);
			}
			if (ownerTPermissionMode.toString().contains("X")) {
				perms.add(PosixFilePermission.OWNER_EXECUTE);
			}

			this.group = tMetaDataPathDetail.getGroupPermission().getGroupID();
			groupTPermissionMode = tMetaDataPathDetail.getGroupPermission().getMode();
			if (groupTPermissionMode.toString().contains("R")) {
				perms.add(PosixFilePermission.GROUP_READ);
			}
			if (groupTPermissionMode.toString().contains("W")) {
				perms.add(PosixFilePermission.GROUP_WRITE);
			}
			if (groupTPermissionMode.toString().contains("X")) {
				perms.add(PosixFilePermission.GROUP_EXECUTE);
			}

			otherTPermissionMode = tMetaDataPathDetail.getOtherPermission();
			if (otherTPermissionMode.toString().contains("R")) {
				perms.add(PosixFilePermission.OTHERS_READ);
			}
			if (otherTPermissionMode.toString().contains("W")) {
				perms.add(PosixFilePermission.OTHERS_WRITE);
			}
			if (otherTPermissionMode.toString().contains("X")) {
				perms.add(PosixFilePermission.OTHERS_EXECUTE);
			}
		}

		/**
		 * Returns the group of this file
		 * 
		 * @return the group of this file
		 */
		public GroupPrincipal group() {
			return new SRMGroup(group);
		}

		/**
		 * Returns the owner of this file
		 * 
		 * @return the owner of this file
		 */
		public UserPrincipal owner() {
			return new SRMUser(owner);
		}

		/**
		 * Returns the {@link Set} of permissions for this file
		 * 
		 * @return the {@link Set} of permissions for this file
		 */
		public Set<PosixFilePermission> permissions() {
			return perms;
		}

		/**
		 * Returns the creation time for this file
		 * 
		 * @return the creation time for this file
		 */
		public long creationTime() {
			return (tMetaDataPathDetail.getCreatedAtTime() != null ? tMetaDataPathDetail.getCreatedAtTime()
					.getTimeInMillis() : -1L);
		}

		/**
		 * Returns the fileKey. Currently its always <code>null</code>
		 * 
		 * @return the fileKey
		 */
		public Object fileKey() {
			return null;
		}

		/**
		 * Returns <code>true</code> if this file is a directory
		 * 
		 * @return <code>true</code> if this file is a directory
		 */
		public boolean isDirectory() {
			return tMetaDataPathDetail.getType().equals(TFileType.DIRECTORY);
		}

		/**
		 * Returns <code>true</code> if this file is neither a regular file nor a directory
		 * 
		 * @return <code>true</code> if this file is neither a regular file nor a directory
		 */
		public boolean isOther() {
			return (!isDirectory() && !isRegularFile() && !isOther());
		}

		/**
		 * Returns <code>true</code> if this file is a regular file.
		 * 
		 * @return <code>true</code> if this file is a regular file.
		 */
		public boolean isRegularFile() {
			return tMetaDataPathDetail.getType().equals(TFileType.FILE);
		}

		/**
		 * Returns <code>true</code> if this file is a symbolic link.
		 * 
		 * @return <code>true</code> if this file is a symbolic link.
		 */
		public boolean isSymbolicLink() {
			return tMetaDataPathDetail.getType().equals(TFileType.LINK);
		}

		/**
		 * Returns the last access time for the file
		 * 
		 * @return the last access time for the file
		 */
		public long lastAccessTime() {
			return -1L;
		}

		/**
		 * Returns the last modification time for the file
		 * 
		 * @return the last modification time for the file
		 */
		public long lastModifiedTime() {
			return (tMetaDataPathDetail.getLastModificationTime() != null ? tMetaDataPathDetail
					.getLastModificationTime().getTimeInMillis() : -1L);
		}

		/**
		 * Return the number of links for the file. Currently always 0.
		 * 
		 * @return 0
		 */
		public int linkCount() {
			return 0;
		}

		/**
		 * Return the time resolution for the access and modification time.
		 * 
		 * @return the time resolution for the access and modification time.
		 */
		public TimeUnit resolution() {
			return TimeUnit.MILLISECONDS;
		}

		/**
		 * Return the size of the file.
		 * 
		 * @return the size of the file.
		 */
		public long size() {
			return (tMetaDataPathDetail.getSize() != null ? tMetaDataPathDetail.getSize().longValue() : -1L);
		}

		/**
		 * Return the permissions of the owner
		 * 
		 * @return the permissions of the owner
		 */
		public TPermissionMode getOwnerTPermissionMode() {
			return ownerTPermissionMode;
		}

		/**
		 * Return the permissions of the group
		 * 
		 * @return the permissions of the group
		 */
		public TPermissionMode getGroupTPermissionMode() {
			return groupTPermissionMode;
		}

		/**
		 * Return the permissions for the others
		 * 
		 * @return the permissions for the others
		 */
		public TPermissionMode getOtherTPermissionMode() {
			return otherTPermissionMode;
		}
	}
}