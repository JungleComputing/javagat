package org.gridlab.gat.io.cpi.glite.srm;

import gov.lbl.srm.StorageResourceManager.ArrayOfAnyURI;
import gov.lbl.srm.StorageResourceManager.ArrayOfTGetFileRequest;
import gov.lbl.srm.StorageResourceManager.ISRM;
import gov.lbl.srm.StorageResourceManager.SRMServiceLocator;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetRequest;
import gov.lbl.srm.StorageResourceManager.SrmPrepareToGetResponse;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfGetRequestRequest;
import gov.lbl.srm.StorageResourceManager.SrmStatusOfGetRequestResponse;
import gov.lbl.srm.StorageResourceManager.TDirOption;
import gov.lbl.srm.StorageResourceManager.TGetFileRequest;
import gov.lbl.srm.StorageResourceManager.TGetRequestFileStatus;
import gov.lbl.srm.StorageResourceManager.TReturnStatus;
import gov.lbl.srm.StorageResourceManager.TStatusCode;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.apache.axis.types.URI;
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
 * 
 * @author Max Berger, Thomas Zangerl
 *
 */
public class SrmConnection {
    protected static Logger logger = Logger.getLogger(SrmConnection.class);
    private final ISRM service;

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
        logger.info("getting srm service");

        URL
                .setURLStreamHandlerFactory(new org.globus.net.GlobusURLStreamHandlerFactory());
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
            throw new IOException(
                    "Could not create SRM connection due to malformed uri", e);
        } catch (MalformedURLException e) {
            throw new IOException(
                    "Could not create SRM connection due to malformed uri", e);
        } catch (ServiceException e) {
            throw new IOException("Could not connect to SRM endpoint", e);
        } catch (GlobusCredentialException e) {
            throw new IOException("Could not load Credentials", e);
        } catch (GSSException e) {
            throw new IOException("Could not load Credentials", e);
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

        }

        return transportURL;
    }

}
