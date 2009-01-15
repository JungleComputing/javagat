package org.gridlab.gat.io.cpi.glite.srm;

import gov.lbl.srm.StorageResourceManager.ArrayOfAnyURI;
import gov.lbl.srm.StorageResourceManager.ArrayOfTGetFileRequest;
import gov.lbl.srm.StorageResourceManager.ArrayOfTPutFileRequest;
import gov.lbl.srm.StorageResourceManager.ISRM;
import gov.lbl.srm.StorageResourceManager.SRMServiceLocator;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetRequest;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetResponse;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToPutRequest;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToPutResponse;
import gov.lbl.srm.StorageResourceManager.SrmPutDoneRequest;
import gov.lbl.srm.StorageResourceManager.SrmPutDoneResponse;
import gov.lbl.srm.StorageResourceManager.SrmRmRequest;
import gov.lbl.srm.StorageResourceManager.SrmRmResponse;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfGetRequestRequest;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfGetRequestResponse;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfPutRequestRequest;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfPutRequestResponse;
import gov.lbl.srm.StorageResourceManager.TDirOption;
import gov.lbl.srm.StorageResourceManager.TGetFileRequest;
import gov.lbl.srm.StorageResourceManager.TGetRequestFileStatus;
import gov.lbl.srm.StorageResourceManager.TPutFileRequest;
import gov.lbl.srm.StorageResourceManager.TPutRequestFileStatus;
import gov.lbl.srm.StorageResourceManager.TReturnStatus;
import gov.lbl.srm.StorageResourceManager.TSURLReturnStatus;
import gov.lbl.srm.StorageResourceManager.TStatusCode;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedLong;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;
import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.transport.HTTPSSender;
import org.globus.axis.util.Util;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.gridlab.gat.security.glite.GliteSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * Non-GAT specific functionality to connect with SRM v2.
 * 
 * @author Max Berger, Thomas Zangerl
 * 
 */
public class SrmConnection {
    private static final String COULD_NOT_LOAD_CREDENTIALS = "Could not load Credentials";
    private static final String COULD_NOT_CREATE_SRM_CONNECTION_DUE_TO_MALFORMED_URI = "Could not create SRM connection due to malformed uri";
    protected static Logger logger = Logger.getLogger(SrmConnection.class);
    private final ISRM service;
    private URI activeUploadURI;
    private String activeToken;

    /**
     * Create a new SRM connection to the given host. All operations called
     * later must include the same host in their URI.
     * 
     * @param host
     *            The Host to connect to.
     * @throws IOException
     *             if the connection fails.
     */
    public SrmConnection(String host) throws IOException {
        // Set provider
        logger.info("Registering httpg transport");
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
        logger.info("getting srm service at " + host);

        try {
            URL
                    .setURLStreamHandlerFactory(new org.globus.net.GlobusURLStreamHandlerFactory());
        } catch (Error e) {
            // ignore
        }
        URI wsEndpoint;
        try {
            wsEndpoint = new URI("httpg", null, host, 8446, "/srm/managerv2",
                    null, null);
            this.service = locator.getsrm(new URL(wsEndpoint.toString()));

            logger.info("Delegating proxy credentials");
            final String proxyPath = GliteSecurityUtils.getProxyPath();
            GlobusCredential credential = new GlobusCredential(proxyPath);
            GSSCredential gssCredential = new GlobusGSSCredentialImpl(
                    credential, 0);
            ((Stub) service)._setProperty(GSIConstants.GSI_CREDENTIALS,
                    gssCredential);
        } catch (MalformedURIException e) {
            logger.warn(e.toString());
            throw new IOException(
                    SrmConnection.COULD_NOT_CREATE_SRM_CONNECTION_DUE_TO_MALFORMED_URI);
        } catch (MalformedURLException e) {
            logger.warn(e.toString());
            throw new IOException(
                    SrmConnection.COULD_NOT_CREATE_SRM_CONNECTION_DUE_TO_MALFORMED_URI);
        } catch (ServiceException e) {
            logger.warn(e.toString());
            throw new IOException("Could not connect to SRM endpoint");
        } catch (GlobusCredentialException e) {
            logger.warn(e.toString());
            throw new IOException(SrmConnection.COULD_NOT_LOAD_CREDENTIALS);
        } catch (GSSException e) {
            logger.warn(e.toString());
            throw new IOException(SrmConnection.COULD_NOT_LOAD_CREDENTIALS);
        }
    }

    public String getTURLForFileDownload(String uriSpec) throws IOException {

        URI uri = new URI(uriSpec);
        String transportURL = "";

        logger.info("Creating get request for URI " + uriSpec);
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

        logger.info("Sending get request");

        SrmPrepareToGetResponse response = service
                .srmPrepareToGet(srmPrepToGetReq);

        String requestToken = response.getRequestToken();
        logger.info("Received the following request token: " + requestToken);

        TReturnStatus status = response.getReturnStatus();
        TGetRequestFileStatus fileStatus = response.getArrayOfFileStatuses()
                .getStatusArray(0);

        logger.info("File status for SURL "
                + fileStatus.getStatus().getStatusCode() + " "
                + fileStatus.getStatus().getExplanation());

        long period = 1000;

        SrmStatusOfGetRequestRequest statusRequest = new SrmStatusOfGetRequestRequest();
        statusRequest.setRequestToken(requestToken);

        statusRequest
                .setArrayOfSourceSURLs(new ArrayOfAnyURI(new URI[] { uri }));

        while ((status.getStatusCode().equals(TStatusCode.SRM_REQUEST_QUEUED))
                || (status.getStatusCode()
                        .equals(TStatusCode.SRM_REQUEST_INPROGRESS))) {
            try {
                Thread.sleep(period);
            } catch (InterruptedException e) {
            }

            SrmStatusOfGetRequestResponse statusResponse = service
                    .srmStatusOfGetRequest(statusRequest);
            status = statusResponse.getReturnStatus();
            fileStatus = statusResponse.getArrayOfFileStatuses()
                    .getStatusArray(0);

            logger.info("Status: " + status.getStatusCode());
            logger.info("Status exp: " + status.getExplanation());
            logger
                    .info("FileStatus: "
                            + fileStatus.getStatus().getStatusCode());
            logger.info("FileStatus exp: "
                    + fileStatus.getStatus().getExplanation());

        }

        if (status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            if (TStatusCode.SRM_FILE_PINNED.equals(fileStatus.getStatus()
                    .getStatusCode())) {
                transportURL = fileStatus.getTransferURL().toString();
                logger.info("Received transfer URL: " + transportURL);
            }
        } else
            throw new IOException(status.getStatusCode() + ": "
                    + fileStatus.getStatus().getStatusCode());
        return transportURL;
    }

    public URI getTURLForFileUpload(String src, String dest) throws IOException {
        URI uri = new URI(dest);
        URI transportURL = null;
        File localFile = new File(src);

        logger.info("Creating put request for URI " + uri);
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

        logger.info("Sending put request");
        SrmPrepareToPutResponse response = service
                .srmPrepareToPut(srmPrepToPutReq);

        String requestToken = response.getRequestToken();
        this.activeToken = requestToken;

        logger.info("Received the following request token: " + requestToken);
        TPutRequestFileStatus fileStatus = response.getArrayOfFileStatuses()
                .getStatusArray(0);
        TReturnStatus status = response.getReturnStatus();

        logger.info("Temporary file status for SURL "
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

            logger.info("Status: " + status.getStatusCode());
            logger.info("Status exp: " + status.getExplanation());

            if (realResponse.getArrayOfFileStatuses() != null
                    && realResponse.getArrayOfFileStatuses().getStatusArray().length > 0) {
                fileStatus = realResponse.getArrayOfFileStatuses()
                        .getStatusArray(0);
            }

            logger
                    .info("FileStatus: "
                            + fileStatus.getStatus().getStatusCode());
            logger.info("FileStatus exp: "
                    + fileStatus.getStatus().getExplanation());

        }

        if (status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            if (TStatusCode.SRM_SPACE_AVAILABLE.equals(fileStatus.getStatus()
                    .getStatusCode())) {
                transportURL = fileStatus.getTransferURL();
                logger.info("Received transfer URL: " + transportURL);
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

        logger.info("Sending put-done request for URI " + activeUploadURI);

        SrmPutDoneResponse response = service.srmPutDone(putDoneRequest);

        TSURLReturnStatus fileStatus = null;
        if (response.getArrayOfFileStatuses() != null
                && response.getArrayOfFileStatuses().getStatusArray().length > 0) {
            fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
            logger.info("Received file status "
                    + fileStatus.getStatus().getStatusCode());
        }

        TReturnStatus status = response.getReturnStatus();
        logger.info("Received status " + status.getStatusCode());
        this.activeToken = null;
        this.activeUploadURI = null;
    }

    public void removeFile(String uri) throws IOException {
        SrmRmRequest removalRequest = new SrmRmRequest();
        removalRequest.setArrayOfSURLs(new ArrayOfAnyURI(new URI[] { new URI(
                uri) }));
        SrmRmResponse response = null;

        logger.info("Invoking delete request for URI " + uri);
        response = service.srmRm(removalRequest);

        if (response.getArrayOfFileStatuses() != null
                && response.getArrayOfFileStatuses().getStatusArray().length > 0) {
            TSURLReturnStatus fileStatus = response.getArrayOfFileStatuses()
                    .getStatusArray(0);
            logger.info("file status code "
                    + fileStatus.getStatus().getStatusCode());
        }

        TReturnStatus returnStatus = response.getReturnStatus();

        logger.info("Return status code " + returnStatus.getStatusCode());
        if (!returnStatus.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            throw new IOException(returnStatus.getStatusCode().toString());
        }
    }

}
