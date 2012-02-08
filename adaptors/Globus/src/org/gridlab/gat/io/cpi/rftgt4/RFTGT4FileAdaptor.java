package org.gridlab.gat.io.cpi.rftgt4;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;
import javax.xml.soap.SOAPElement;

import org.apache.axis.AxisProperties;
import org.apache.axis.EngineConfigurationFactory;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.globus.axis.util.Util;
import org.globus.delegation.DelegationConstants;
import org.globus.delegation.DelegationException;
import org.globus.delegation.DelegationUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.jaas.JaasGssUtil;
import org.globus.rft.generated.BaseRequestType;
import org.globus.rft.generated.CreateReliableFileTransferInputType;
import org.globus.rft.generated.CreateReliableFileTransferOutputType;
import org.globus.rft.generated.DeleteRequestType;
import org.globus.rft.generated.DeleteType;
import org.globus.rft.generated.GetStatus;
import org.globus.rft.generated.RFTDatabaseFaultType;
import org.globus.rft.generated.RFTOptionsType;
import org.globus.rft.generated.ReliableFileTransferFactoryPortType;
import org.globus.rft.generated.ReliableFileTransferPortType;
import org.globus.rft.generated.RequestStatusType;
import org.globus.rft.generated.RequestStatusTypeEnumeration;
import org.globus.rft.generated.Start;
import org.globus.rft.generated.TransferRequestType;
import org.globus.rft.generated.TransferStatusTypeEnumeration;
import org.globus.rft.generated.TransferType;
import org.globus.transfer.reliable.client.BaseRFTClient;
import org.globus.transfer.reliable.service.RFTConstants;
import org.globus.wsrf.NotificationConsumerManager;
import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.WSNConstants;
import org.globus.wsrf.container.ContainerException;
import org.globus.wsrf.container.ServiceContainer;
import org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType;
import org.globus.wsrf.encoding.DeserializationException;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.IdentityAuthorization;
import org.globus.wsrf.impl.security.authorization.SelfAuthorization;
import org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.ContainerSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.GSISecureMsgAuthMethod;
import org.globus.wsrf.impl.security.descriptor.GSITransportAuthMethod;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.SecurityDescriptorException;
import org.globus.wsrf.security.SecurityManager;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.oasis.wsn.Subscribe;
import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.faults.BaseFaultType;
import org.oasis.wsrf.lifetime.SetTerminationTime;
import org.oasis.wsrf.properties.GetMultipleResourcePropertiesResponse;
import org.oasis.wsrf.properties.GetMultipleResourceProperties_Element;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType;

class RFTGT4NotifyCallback implements NotifyCallback {

    protected static Logger logger = LoggerFactory
            .getLogger(RFTGT4NotifyCallback.class);
       
    RFTGT4FileAdaptor transfer;

    public RFTGT4NotifyCallback(RFTGT4FileAdaptor transfer) {
        super();
        this.transfer = transfer;
    }

    @SuppressWarnings("rawtypes")
    public void deliver(List topicPath, EndpointReferenceType producer,
            Object messageWrapper) {
        try {
            ResourcePropertyValueChangeNotificationType message = ((ResourcePropertyValueChangeNotificationElementType) messageWrapper)
                    .getResourcePropertyValueChangeNotification();
            RequestStatusType status = (RequestStatusType) message
                    .getNewValue().get_any()[0].getValueAsType(
                    RFTConstants.REQUEST_STATUS_RESOURCE,
                    RequestStatusType.class);
            if (logger.isDebugEnabled()) {
                logger.debug("received status: " + status.getRequestStatus());
            }
            if (status.getRequestStatus().equals(
                    RequestStatusTypeEnumeration.Failed)) {
                logger.debug("fault: " + status.getFault());
            }
            transfer.setStatus(status.getRequestStatus());
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("status update failed: " + e);
            }
        }
    }
}

@SuppressWarnings("serial")
public class RFTGT4FileAdaptor extends FileCpi {
    public static final Authorization DEFAULT_AUTHZ = HostAuthorization
            .getInstance();

    Integer msgProtectionType = Constants.SIGNATURE;

    static final int TERM_TIME = 20;

    static final String PROTOCOL = "https";

    private static final String BASE_SERVICE_PATH = "/wsrf/services/";

    public static final int DEFAULT_DURATION_HOURS = 24;

    public static final Integer DEFAULT_MSG_PROTECTION = Constants.SIGNATURE;

    public static final String DEFAULT_FACTORY_PORT = "8443";

    private static final int DEFAULT_GRIDFTP_PORT = 2811;
    
    public static String getDescription() {
        return "The RFTGT4 File Adaptor implements the File object on Globus 4.0 using the Globus Reliable File Transfer (RFT) protocol.";
    }

    public static String[] getSupportedSchemes() {
        return new String[] { "rftgt4", "gsiftp", "gridftp", "file", ""};
    }
    
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("delete", true);
        capabilities.put("copy", true);
        return capabilities;
    }
    
    // instance initializer sets personalized
    // EngineConfigurationFactory for the axis client.
    static {
        if (System.getProperty("GLOBUS_LOCATION") == null) {
            String globusLocation = System.getProperty("gat.adaptor.path")
                    + java.io.File.separator + "GlobusAdaptor"
                    + java.io.File.separator;
            System.setProperty("GLOBUS_LOCATION", globusLocation);
        }
        if (AxisProperties.getProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME) == null) {
            AxisProperties.setProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME,
            "org.gridlab.gat.resources.cpi.wsgt4new.GlobusEngineConfigurationFactory");
        }
    }

    NotificationConsumerManager notificationConsumerManager;

    EndpointReferenceType notificationConsumerEPR;

    EndpointReferenceType notificationProducerEPR;

    String securityType;

    String factoryUrl;

    GSSCredential proxy;

    Authorization authorization;

    String host;

    String status;

    BaseFaultType fault;

    String rftgt4Location;

    ReliableFileTransferFactoryPortType factoryPort;

    private boolean localFile;

    /**
     * Creates new GAT RFTGT4 file object.
     * 
     * @param gatContext
     *                GAT context
     * @param location
     *                FILE location URI
     */
    public RFTGT4FileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {
        super(gatContext, location);
        if (toURI().isCompatible("file") && toURI().refersToLocalHost()) {
            localFile = true;
        }
        // TODO: may be it is possible on the local host...
        if (!location.hasAbsolutePath()) {
            throw new AdaptorNotApplicableException(
                    "cannot handle relative paths: " + location.getPath());
        }

        try {
            rftgt4Location = URItoRFTGT4String(location);
        } catch (URISyntaxException e) {
            throw new GATObjectCreationException(
                    "unable to create a valid rft URI: " + location, e);
        } catch(GATInvocationException e) {
            throw new GATObjectCreationException("Cannot deal with relative paths in absolute URI's", e);
        }

        this.host = location.getHost();
        if (this.host == null) {
            this.host = getLocalHost();
        }
        this.securityType = Constants.GSI_SEC_MSG;
        this.authorization = null;
        this.proxy = null;

        try {
            proxy = GlobusSecurityUtils.getGlobusCredential(gatContext,
                    "globus", location, DEFAULT_GRIDFTP_PORT);
        } catch (CouldNotInitializeCredentialException e) {
            throw new GATObjectCreationException("globus", e);
        } catch (CredentialExpiredException e) {
            throw new GATObjectCreationException("globus", e);
        } catch (InvalidUsernameOrPasswordException e) {
            throw new GATObjectCreationException("globus", e);
        }

        this.notificationConsumerManager = null;
        this.notificationConsumerEPR = null;
        this.notificationProducerEPR = null;
        this.status = null;
        this.fault = null;
        factoryPort = null;
        this.factoryUrl = PROTOCOL + "://" + host + ":" + DEFAULT_FACTORY_PORT
                + BASE_SERVICE_PATH + RFTConstants.FACTORY_NAME;
    }

    protected synchronized boolean copy2(String destStr)
            throws GATInvocationException {
        EndpointReferenceType credentialEndpoint = getCredentialEPR();

        TransferType[] transferArray = new TransferType[1];
        transferArray[0] = new TransferType();
        transferArray[0].setSourceUrl(rftgt4Location);
        transferArray[0].setDestinationUrl(destStr);
        RFTOptionsType rftOptions = new RFTOptionsType();
        rftOptions.setBinary(Boolean.TRUE);
        // rftOptions.setIgnoreFilePermErr(false);
        TransferRequestType request = new TransferRequestType();
        request.setRftOptions(rftOptions);
        request.setTransfer(transferArray);
        request.setTransferCredentialEndpoint(credentialEndpoint);
        setRequest(request);

        return status.equals(RequestStatusTypeEnumeration.Done.toString())
                || status.equals(TransferStatusTypeEnumeration.Finished
                        .toString());
    }

    public String URItoRFTGT4String(URI in) throws URISyntaxException, GATInvocationException {
        if (in.isAbsolute()) {
            if (! in.hasAbsolutePath()) {
                throw new GATInvocationException("Cannot deal with relative paths in absolute URIs");
            }
            in = in.setPath(in.getPath().substring(1));
        }
        String rftgt4String = fixURI(in, "gsiftp").toString();
        if (in.getHost() == null) {
            rftgt4String = rftgt4String.replace("gsiftp:", "gsiftp://"
                    + getLocalHost());
        }
        // Uncomment this code if the ${GLOBUS_USER_HOME} will work
        // try {
        // URI fixedURI = new URI(rftgt4String);
        // if (fixedURI.getPath() != null) {
        // if (!(fixedURI.getPath().startsWith(File.separator))) {
        // rftgt4String = rftgt4String.replace(fixedURI.getPath(),
        // "${GLOBUS_USER_HOME}/"
        // + fixedURI.getPath());
        // }
        // }
        // } catch (URISyntaxException e) {
        // // ignore
        // }
        return rftgt4String;
    }

    private String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (Exception e) {
            return "localhost";
        }
    }

    public void copy(URI dest) throws GATInvocationException {
        // System.out.println("\ncopy " + location + " -> " + dest);
        boolean destIsLocal = dest.isCompatible("file") && dest.refersToLocalHost();
        if (localFile) {
            if (destIsLocal) {
                throw new GATInvocationException("Local-to-local copy not implemented");
            }
        }
        if (! destIsLocal && (dest.getScheme() == null || ! recognizedScheme(dest.getScheme(), getSupportedSchemes()))) {
            throw new GATInvocationException("RFTGT4FileAdaptor: cannot handle this URI " + dest);
        }
        
        try {
            if (!copy2(URItoRFTGT4String(dest))) {
                throw new GATInvocationException(
                        "RFTGT4FileAdaptor: file copy failed");
            }
        } catch (URISyntaxException e) {
            throw new GATInvocationException(
                    "unable to create valid rft uri for destination: " + dest,
                    e);
        }
    }

    protected void subscribe(ReliableFileTransferPortType rft)
            throws GATInvocationException {
        Map<Object, Object> properties = new HashMap<Object, Object>();
        properties.put(ServiceContainer.CLASS,
                "org.globus.wsrf.container.GSIServiceContainer");
        if (this.proxy != null) {
            ContainerSecurityDescriptor containerSecDesc = new ContainerSecurityDescriptor();
            SecurityManager.getManager();
            try {
                containerSecDesc.setSubject(JaasGssUtil
                        .createSubject(this.proxy));
            } catch (GSSException e) {
                throw new GATInvocationException(
                        "RFTGT4FileAdaptor: ContainerSecurityDescriptor failed, "
                                + e);
            }
            properties.put(ServiceContainer.CONTAINER_DESCRIPTOR,
                    containerSecDesc);
        }
        this.notificationConsumerManager = NotificationConsumerManager
                .getInstance(properties);
        try {
            this.notificationConsumerManager.startListening();
        } catch (ContainerException e) {
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: NotificationConsumerManager failed, "
                            + e);
        }
        List<Object> topicPath = new LinkedList<Object>();
        topicPath.add(RFTConstants.REQUEST_STATUS_RESOURCE);

        ResourceSecurityDescriptor securityDescriptor = new ResourceSecurityDescriptor();
        String authz = null;
        if (authorization == null) {
            authz = Authorization.AUTHZ_NONE;
        } else if (authorization instanceof HostAuthorization) {
            authz = Authorization.AUTHZ_NONE;
        } else if (authorization instanceof SelfAuthorization) {
            authz = Authorization.AUTHZ_SELF;
        } else if (authorization instanceof IdentityAuthorization) {
            // not supported
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: identity authorization not supported");
        } else {
            // throw an sg
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: set authorization failed");
        }
        securityDescriptor.setAuthz(authz);
        Vector<Object> authMethod = new Vector<Object>();
        if (this.securityType.equals(Constants.GSI_SEC_MSG)) {
            authMethod.add(GSISecureMsgAuthMethod.BOTH);
        } else {
            authMethod.add(GSITransportAuthMethod.BOTH);
        }
        try {
            securityDescriptor.setAuthMethods(authMethod);
        } catch (SecurityDescriptorException e) {
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: setAuthMethods failed, " + e);
        }

        RFTGT4NotifyCallback notifyCallback = new RFTGT4NotifyCallback(this);
        try {
            notificationConsumerEPR = notificationConsumerManager
                    .createNotificationConsumer(topicPath, notifyCallback,
                            securityDescriptor);
        } catch (ResourceException e) {
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: createNotificationConsumer failed, "
                            + e);
        }
        Subscribe subscriptionRequest = new Subscribe();
        subscriptionRequest.setConsumerReference(notificationConsumerEPR);
        TopicExpressionType topicExpression = null;
        try {
            topicExpression = new TopicExpressionType(
                    WSNConstants.SIMPLE_TOPIC_DIALECT,
                    RFTConstants.REQUEST_STATUS_RESOURCE);
        } catch (MalformedURIException e) {
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: create TopicExpressionType failed, "
                            + e);
        }
        subscriptionRequest.setTopicExpression(topicExpression);
        try {
            rft.subscribe(subscriptionRequest);
        } catch (RemoteException e) {
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: subscription failed, " + e);
        }
    }

    protected EndpointReferenceType getCredentialEPR()
            throws GATInvocationException {
        this.status = null;
        URL factoryURL = null;
        try {
            factoryURL = new URL(factoryUrl);
        } catch (MalformedURLException e) {
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: set factoryURL failed, " + e);
        }
        try {
            factoryPort = BaseRFTClient.rftFactoryLocator
                    .getReliableFileTransferFactoryPortTypePort(factoryURL);
        } catch (ServiceException e) {
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: set factoryPort failed, " + e);
        }
        setSecurityTypeFromURL(factoryURL);
        return populateRFTEndpoints(factoryPort);
    }

    protected void setRequest(BaseRequestType request)
            throws GATInvocationException {
        CreateReliableFileTransferInputType input = new CreateReliableFileTransferInputType();
        if (request instanceof TransferRequestType) {
            input.setTransferRequest((TransferRequestType) request);
        } else {
            input.setDeleteRequest((DeleteRequestType) request);
        }
        Calendar termTimeDel = Calendar.getInstance();
        termTimeDel.add(Calendar.MINUTE, TERM_TIME);
        input.setInitialTerminationTime(termTimeDel);
        CreateReliableFileTransferOutputType response = null;
        try {
            response = factoryPort.createReliableFileTransfer(input);
        } catch (RemoteException e) {
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: set createReliableFileTransfer failed, "
                            + e);
        }
        EndpointReferenceType reliableRFTEndpoint = response
                .getReliableTransferEPR();
        ReliableFileTransferPortType rft = null;
        try {
            rft = BaseRFTClient.rftLocator
                    .getReliableFileTransferPortTypePort(reliableRFTEndpoint);
        } catch (ServiceException e) {
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: getReliableFileTransferPortTypePort failed, "
                            + e);
        }
        setStubSecurityProperties((Stub) rft);
        subscribe(rft);
        Calendar termTime = Calendar.getInstance();
        termTime.add(Calendar.MINUTE, TERM_TIME);
        SetTerminationTime reqTermTime = new SetTerminationTime();
        reqTermTime.setRequestedTerminationTime(termTime);
        try {
            rft.setTerminationTime(reqTermTime);
        } catch (RemoteException e) {
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: setTerminationTime failed, " + e);
        }
        try {
            rft.start(new Start());
        } catch (RemoteException e) {
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: start failed, " + e);
        }

        // wait until status is either 'Done' or 'Failed', wait for upcall and
        // do active polling.
        GetStatus parameters = new GetStatus(rftgt4Location);
        while (status == null
                || !(status
                        .equals(RequestStatusTypeEnumeration.Done.toString())
                        || status.equals(RequestStatusTypeEnumeration.Failed
                                .toString())
                        || status.equals(TransferStatusTypeEnumeration.Failed
                                .toString()) || status
                        .equals(TransferStatusTypeEnumeration.Finished
                                .toString()))) {
            synchronized (this) {
                try {
                    TransferStatusTypeEnumeration newStatus = rft.getStatus(
                            parameters).getTransferStatus().getStatus();
                    if (newStatus != null) {
                        status = newStatus.toString();
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    continue;
                } catch (RFTDatabaseFaultType e) {
                    throw new GATInvocationException("RFTGT4FileAdaptor: " + e);
                } catch (RemoteException e) {
                    throw new GATInvocationException("RFTGT4FileAdaptor: " + e);
                }
            }
        }
    }

    protected boolean delete2() throws GATInvocationException {
        EndpointReferenceType credentialEndpoint = getCredentialEPR();

        DeleteType[] deleteArray = new DeleteType[1];
        deleteArray[0] = new DeleteType();
        deleteArray[0].setFile(rftgt4Location);

        DeleteRequestType request = new DeleteRequestType();
        request.setDeletion(deleteArray);
        request.setTransferCredentialEndpoint(credentialEndpoint);

        setRequest(request);

        return status.equals(RequestStatusTypeEnumeration.Done.toString())
                || status.equals(TransferStatusTypeEnumeration.Finished
                        .toString());
    }

    public boolean delete() throws GATInvocationException {
        if (localFile) {
            super.delete();
        }

        boolean result = delete2();
        // try recursive directory deletion
        if (!result && !rftgt4Location.endsWith("/")) {
            rftgt4Location += "/";
            result = delete2();
            return result;
        }
        return result;
    }

    private void setSecurityTypeFromURL(URL url) {
        if (url.getProtocol().equals("http")) {
            securityType = Constants.GSI_SEC_MSG;
        } else {
            Util.registerTransport();
            securityType = Constants.GSI_TRANSPORT;
        }
    }

    private void setStubSecurityProperties(Stub stub) {
        ClientSecurityDescriptor secDesc = new ClientSecurityDescriptor();

        if (this.securityType.equals(Constants.GSI_SEC_MSG)) {
            secDesc.setGSISecureMsg(this.getMessageProtectionType());
        } else {
            secDesc.setGSITransport(this.getMessageProtectionType());
        }

        secDesc.setAuthz(getAuthorization());

        if (this.proxy != null) {
            // set proxy credential
            secDesc.setGSSCredential(this.proxy);
        }

        stub._setProperty(Constants.CLIENT_DESCRIPTOR, secDesc);
    }

    public Integer getMessageProtectionType() {
        return (this.msgProtectionType == null) ? RFTGT4FileAdaptor.DEFAULT_MSG_PROTECTION
                : this.msgProtectionType;
    }

    public Authorization getAuthorization() {
        return (authorization == null) ? DEFAULT_AUTHZ : this.authorization;
    }

    private EndpointReferenceType populateRFTEndpoints(
            ReliableFileTransferFactoryPortType factoryPort)
            throws GATInvocationException {
        EndpointReferenceType[] delegationFactoryEndpoints = fetchDelegationFactoryEndpoints(factoryPort);
        EndpointReferenceType delegationEndpoint = delegate(delegationFactoryEndpoints[0]);
        return delegationEndpoint;
    }

    private EndpointReferenceType delegate(
            EndpointReferenceType delegationFactoryEndpoint)
            throws GATInvocationException {
        GlobusCredential credential = null;
        if (this.proxy != null) {
            credential = ((GlobusGSSCredentialImpl) this.proxy)
                    .getGlobusCredential();
        } else {
            try {
                credential = GlobusCredential.getDefaultCredential();
            } catch (GlobusCredentialException e) {
                throw new GATInvocationException("RFTGT4FileAdaptor: " + e);
            }
        }

        int lifetime = DEFAULT_DURATION_HOURS * 60 * 60;

        ClientSecurityDescriptor secDesc = new ClientSecurityDescriptor();
        if (this.securityType.equals(Constants.GSI_SEC_MSG)) {
            secDesc.setGSISecureMsg(this.getMessageProtectionType());
        } else {
            secDesc.setGSITransport(this.getMessageProtectionType());
        }
        secDesc.setAuthz(getAuthorization());

        if (this.proxy != null) {
            secDesc.setGSSCredential(this.proxy);
        }

        // Get the public key to delegate on.
        X509Certificate[] certsToDelegateOn = null;
        try {
            certsToDelegateOn = DelegationUtil.getCertificateChainRP(
                    delegationFactoryEndpoint, secDesc);
        } catch (DelegationException e) {
            throw new GATInvocationException("RFTGT4FileAdaptor: " + e);
        }
        X509Certificate certToSign = certsToDelegateOn[0];

        // FIXME remove when there is a DelegationUtil.delegate(EPR, ...)
        String protocol = delegationFactoryEndpoint.getAddress().getScheme();
        String host = delegationFactoryEndpoint.getAddress().getHost();
        int port = delegationFactoryEndpoint.getAddress().getPort();
        String factoryUrl = protocol + "://" + host + ":" + port
                + BASE_SERVICE_PATH + DelegationConstants.FACTORY_PATH;

        // send to delegation service and get epr.
        EndpointReferenceType credentialEndpoint = null;
        try {
            credentialEndpoint = DelegationUtil.delegate(factoryUrl,
                    credential, certToSign, lifetime, false, secDesc);
        } catch (DelegationException e) {
            throw new GATInvocationException("RFTGT4FileAdaptor: " + e);
        }
        return credentialEndpoint;
    }

    public EndpointReferenceType[] fetchDelegationFactoryEndpoints(
            ReliableFileTransferFactoryPortType factoryPort)
            throws GATInvocationException {
        GetMultipleResourceProperties_Element request = new GetMultipleResourceProperties_Element();
        request
                .setResourceProperty(new QName[] { RFTConstants.DELEGATION_ENDPOINT_FACTORY });
        GetMultipleResourcePropertiesResponse response;
        try {
            response = factoryPort.getMultipleResourceProperties(request);
        } catch (RemoteException e) {
            // e.printStackTrace();
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: getMultipleResourceProperties, " + e);
        }
        SOAPElement[] any = response.get_any();

        EndpointReferenceType epr1 = null;
        try {
            epr1 = (EndpointReferenceType) ObjectDeserializer.toObject(any[0],
                    EndpointReferenceType.class);
        } catch (DeserializationException e) {
            throw new GATInvocationException(
                    "RFTGT4FileAdaptor: ObjectDeserializer, " + e);
        }
        EndpointReferenceType[] endpoints = new EndpointReferenceType[] { epr1 };
        return endpoints;
    }

    synchronized void setStatus(RequestStatusTypeEnumeration status) {
        this.status = status.toString();
        notifyAll();
    }

    /*
     * private BaseFaultType getFaultFromRP(RFTFaultResourcePropertyType fault) {
     * if (fault == null) { return null; }
     * 
     * if (fault.getRftTransferFaultType() != null) { return
     * fault.getRftTransferFaultType(); } else if
     * (fault.getDelegationEPRMissingFaultType() != null) { return
     * fault.getDelegationEPRMissingFaultType(); } else if
     * (fault.getRftAuthenticationFaultType() != null) { return
     * fault.getRftAuthenticationFaultType(); } else if
     * (fault.getRftAuthorizationFaultType() != null) { return
     * fault.getRftAuthorizationFaultType(); } else if
     * (fault.getRftDatabaseFaultType() != null) { return
     * fault.getRftDatabaseFaultType(); } else if
     * (fault.getRftRepeatedlyStartedFaultType() != null) { return
     * fault.getRftRepeatedlyStartedFaultType(); } else if
     * (fault.getTransferTransientFaultType() != null) { return
     * fault.getTransferTransientFaultType(); } else { return null; } }
     */

    /*
     * private BaseFaultType deserializeFaultRP(SOAPElement any) throws
     * Exception { return getFaultFromRP((RFTFaultResourcePropertyType)
     * ObjectDeserializer .toObject(any, RFTFaultResourcePropertyType.class)); }
     */

    void setFault(BaseFaultType fault) {
        this.fault = fault;
    }

}
