package org.gridlab.gat.resources.cpi.wsgt4;

import java.io.File;
import java.net.MalformedURLException;
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
import javax.xml.rpc.Stub;
import javax.xml.soap.SOAPElement;

import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.addressing.AttributedURI;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.log4j.Logger;
import org.globus.axis.util.Util;
import org.globus.delegation.DelegationConstants;
import org.globus.delegation.DelegationException;
import org.globus.delegation.DelegationUtil;
import org.globus.exec.generated.CreateManagedJobInputType;
import org.globus.exec.generated.CreateManagedJobOutputType;
import org.globus.exec.generated.FaultResourcePropertyType;
import org.globus.exec.generated.FaultType;
import org.globus.exec.generated.JobDescriptionType;
import org.globus.exec.generated.ManagedJobFactoryPortType;
import org.globus.exec.generated.ManagedJobPortType;
import org.globus.exec.generated.StateChangeNotificationMessageType;
import org.globus.exec.generated.StateEnumeration;
import org.globus.exec.utils.ManagedExecutableJobConstants;
import org.globus.exec.utils.ManagedJobConstants;
import org.globus.exec.utils.ManagedJobFactoryConstants;
import org.globus.exec.utils.client.ManagedJobClientHelper;
import org.globus.exec.utils.client.ManagedJobFactoryClientHelper;
import org.globus.exec.utils.rsl.RSLHelper;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.jaas.JaasGssUtil;
import org.globus.rft.generated.DeleteRequestType;
import org.globus.rft.generated.TransferRequestType;
import org.globus.security.gridmap.GridMap;
import org.globus.wsrf.NotificationConsumerManager;
import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.WSNConstants;
import org.globus.wsrf.container.ServiceContainer;
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
import org.globus.wsrf.security.SecurityManager;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.ietf.jgss.GSSCredential;
import org.oasis.wsn.Subscribe;
import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.properties.GetMultipleResourcePropertiesResponse;
import org.oasis.wsrf.properties.GetMultipleResourceProperties_Element;
import org.w3c.dom.Element;

// import org.globus.wsrf.NotifyCallback;
// import java.util.List;
// import org.apache.axis.message.addressing.EndpointReferenceType;
// import org.globus.exec.generated.StateChangeNotificationMessageType;
// import org.globus.wsrf.encoding.ObjectDeserializer;
// import org.w3c.dom.Element;
// import org.globus.exec.generated.StateEnumeration;
// import org.globus.exec.generated.FaultResourcePropertyType;
// import org.globus.exec.generated.FaultType;

/**
 * Implements the <code>NotifyCallback</code> class, the <code>deliver</code>
 * method is called, when the gstate of the job changed.
 */
class WSGT4NotifyCallback implements NotifyCallback {

    protected static Logger logger = Logger.getLogger(WSGT4Job.class);

    WSGT4Job job;

    public WSGT4NotifyCallback(WSGT4Job job) {
        super();
        this.job = job;
    }

    @SuppressWarnings("unchecked")
    public void deliver(List topicPath, EndpointReferenceType producer,
            Object message) {
        try {
            StateChangeNotificationMessageType changeNotification = (StateChangeNotificationMessageType) ObjectDeserializer
                    .toObject((Element) message,
                            StateChangeNotificationMessageType.class);
            StateEnumeration gstate = changeNotification.getState();
            boolean holding = changeNotification.isHolding();
            if (gstate.equals(StateEnumeration.Failed)) {
                job.setFault(getFaultFromRP(changeNotification.getFault()));
            }
            if (gstate.equals(StateEnumeration.StageOut)
                    || gstate.equals(StateEnumeration.Done)
                    || gstate.equals(StateEnumeration.Failed)) {
                job.setExitCode(changeNotification.getExitCode());
            }

            synchronized (job) {
                if ((job.notificationConsumerManager != null)
                        && !job.notificationConsumerManager.isListening()) {
                    return;
                }
                job.setState(gstate, holding);
            }
        } catch (Exception e) {
            logger.warn("Notification message processing FAILED:"
                    + "Could not get value or set new status.");
            // no propagation of error here?
        }
    }

    private FaultType getFaultFromRP(FaultResourcePropertyType fault) {
        if (fault == null) {
            return null;
        }

        if (fault.getFault() != null) {
            return fault.getFault();
        } else if (fault.getCredentialSerializationFault() != null) {
            return fault.getCredentialSerializationFault();
        } else if (fault.getExecutionFailedFault() != null) {
            return fault.getExecutionFailedFault();
        } else if (fault.getFilePermissionsFault() != null) {
            return fault.getFilePermissionsFault();
        } else if (fault.getInsufficientCredentialsFault() != null) {
            return fault.getInsufficientCredentialsFault();
        } else if (fault.getInternalFault() != null) {
            return fault.getInternalFault();
        } else if (fault.getInvalidCredentialsFault() != null) {
            return fault.getInvalidCredentialsFault();
        } else if (fault.getInvalidPathFault() != null) {
            return fault.getInvalidPathFault();
        } else if (fault.getServiceLevelAgreementFault() != null) {
            return fault.getServiceLevelAgreementFault();
        } else if (fault.getStagingFault() != null) {
            return fault.getStagingFault();
        } else if (fault.getUnsupportedFeatureFault() != null) {
            return fault.getUnsupportedFeatureFault();
        } else {
            return null;
        }
    }
}

/**
 * Implements JobCpi abstract class. Wrappers a JavaCog task.
 * 
 * @author Balazs Bokodi
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("serial")
public class WSGT4Job extends JobCpi {
    /**
     * Initializes a job. Creates a task, sets up the listener and submits it.
     */

    private MetricDefinition statusMetricDefinition;

    NotificationConsumerManager notificationConsumerManager;
    GSSCredential proxy;
    StateEnumeration gstate;
    Authorization authorization;
    Integer msgProtectionType = Constants.SIGNATURE;
    Boolean limitedDelegation;
    boolean holding;
    String contactString;
    EndpointReferenceType factoryEndpoint;
    String factoryType;
    String submissionID;
    JobDescriptionType jobDescriptionType;
    String securityType;
    Calendar calendar;
    EndpointReferenceType notificationConsumerEPR;
    EndpointReferenceType notificationProducerEPR;
    EndpointReferenceType jobEndpointReference;
    FaultType fault;
    int exitCode;
    public static final Authorization DEFAULT_AUTHZ = HostAuthorization
            .getInstance();
    // private static final String DEFAULT_SECURITY_TYPE =
    // Constants.GSI_TRANSPORT;
    public static final Integer DEFAULT_MSG_PROTECTION = Constants.SIGNATURE;
    private static final String BASE_SERVICE_PATH = "/wsrf/services/";
    public static final int DEFAULT_DURATION_HOURS = 24;

    protected WSGT4Job(GATContext gatContext, Preferences preferences,
            JobDescription jobDescription, Sandbox sandbox) {
        super(gatContext, preferences, jobDescription, sandbox);

        gstate = null;
        authorization = null;
        limitedDelegation = new Boolean(false);

        factoryEndpoint = null;
        submissionID = null;
        factoryType = (String) preferences.get("resourcebroker.jobmanager");
        if (factoryType == null) {
            factoryType = ManagedJobFactoryConstants.DEFAULT_FACTORY_TYPE;
        }
        securityType = null;
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        notificationConsumerManager = null;
        notificationConsumerEPR = null;
        notificationProducerEPR = null;
        holding = false;
        fault = null;
        exitCode = 0;
        jobEndpointReference = null;
        // authorization = SelfAuthorization.getInstance();
        // authorization = new IdentityAuthorization("identity");
        authorization = HostAuthorization.getInstance();
        // msgProtectionType = Constants.ENCRYPTION;
        msgProtectionType = Constants.SIGNATURE;

        HashMap<String, Object> returnDef = new HashMap<String, Object>();
        returnDef.put("status", String.class);
        statusMetricDefinition = new MetricDefinition("job.status",
                MetricDefinition.DISCRETE, "String", null, null, returnDef);
        GATEngine.registerMetric(this, "getJobStatus", statusMetricDefinition);
        statusMetricDefinition.createMetric(null);
    }

    protected void setJobDescriptionType(JobDescriptionType jobDescription) {
        jobDescriptionType = jobDescription;
    }

    protected void setContactString(String contactString)
            throws GATInvocationException {
        this.contactString = contactString;
        URL factoryURL = null;
        try {
            factoryURL = ManagedJobFactoryClientHelper.getServiceURL(
                    contactString).getURL();
        } catch (MalformedURLException e) {
            throw new GATInvocationException("WSGT4Job: " + e);
        }

        try {
            factoryEndpoint = ManagedJobFactoryClientHelper.getFactoryEndpoint(
                    factoryURL, factoryType);
        } catch (Exception e) {
            throw new GATInvocationException("WSGT4Job: " + e);
        }
        submissionID = "uuid:" + UUIDGenFactory.getUUIDGen().nextUUID();
        setSecurityTypeFromEndpoint(factoryEndpoint);
        populateJobDescriptionEndpoints(factoryEndpoint);

        ManagedJobFactoryPortType factoryPort = null;
        try {
            factoryPort = getManagedJobFactoryPortType(factoryEndpoint);
        } catch (Exception e) {
            throw new GATInvocationException("WSGT4Job: " + e);
        }
        try {
            this.jobEndpointReference = createJobEndpoint(factoryPort);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GATInvocationException("WSGT4Job: " + e);
        }
    }

    protected void setCredential(GSSCredential cred) {
        this.proxy = cred;
    }

    public StateEnumeration ggetState() {
        return this.gstate;
    }

    public void setCredentials(GSSCredential newProxy) {
        if (this.proxy != null) {
            throw new IllegalArgumentException("Credentials already set");
        } else {
            this.proxy = newProxy;
        }
    }

    public void getProxyFromPath(String proxyPath) {
        if (proxyPath == null) {
            return;
        }
        try {
            ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
                    .getInstance();
            String handle = "X509_USER_PROXY=" + proxyPath;
            GSSCredential proxy = manager.createCredential(handle.getBytes(),
                    ExtendedGSSCredential.IMPEXP_MECH_SPECIFIC,
                    GSSCredential.DEFAULT_LIFETIME, null,
                    GSSCredential.INITIATE_AND_ACCEPT);
            setCredentials(proxy);
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("WGT4Job: getProxyFromPath failed");
            }
        }
    }

    void createJobDescription(String rsl) throws Exception {
        this.jobDescriptionType = RSLHelper.readRSL(rsl);
    }

    void createJobDescription(File f) throws Exception {
        this.jobDescriptionType = RSLHelper.readRSL(f);
    }

    private void setSecurityTypeFromEndpoint(EndpointReferenceType epr) {
        if (epr.getAddress().getScheme().equals("http")) {
            securityType = Constants.GSI_SEC_MSG;
        } else {
            Util.registerTransport();
            securityType = Constants.GSI_TRANSPORT;
        }
    }

    private void populateJobDescriptionEndpoints(
            EndpointReferenceType pfactoryEndpoint)
            throws GATInvocationException {
        EndpointReferenceType[] delegationFactoryEndpoints = fetchDelegationFactoryEndpoints(pfactoryEndpoint);
        EndpointReferenceType delegationEndpoint = delegate(
                delegationFactoryEndpoints[0], this.limitedDelegation
                        .booleanValue());
        this.jobDescriptionType.setJobCredentialEndpoint(delegationEndpoint);
        // separate delegation not supported
        this.jobDescriptionType
                .setStagingCredentialEndpoint(delegationEndpoint);
        // delegate to RFT
        populateStagingDescriptionEndpoints(pfactoryEndpoint,
                delegationFactoryEndpoints[1], this.jobDescriptionType);
    }

    public EndpointReferenceType[] fetchDelegationFactoryEndpoints(
            EndpointReferenceType factoryEndpoint)
            throws GATInvocationException {
        ManagedJobFactoryPortType factoryPort;
        try {
            factoryPort = getManagedJobFactoryPortType(factoryEndpoint);
        } catch (Exception e) {
            throw new GATInvocationException("WSGT4Job: " + e);
        }
        GetMultipleResourceProperties_Element request = new GetMultipleResourceProperties_Element();
        request
                .setResourceProperty(new QName[] {
                        ManagedJobFactoryConstants.RP_DELEGATION_FACTORY_ENDPOINT,
                        ManagedJobFactoryConstants.RP_STAGING_DELEGATION_FACTORY_ENDPOINT });
        GetMultipleResourcePropertiesResponse response;
        try {
            response = factoryPort.getMultipleResourceProperties(request);
        } catch (RemoteException e) {
            throw new GATInvocationException("WSGT4Job: " + e);
        }
        SOAPElement[] any = response.get_any();
        EndpointReferenceType epr1 = null;
        EndpointReferenceType epr2 = null;
        try {
            epr1 = (EndpointReferenceType) ObjectDeserializer.toObject(any[0],
                    EndpointReferenceType.class);
        } catch (DeserializationException e) {
            throw new GATInvocationException("WSGT4Job: " + e);
        }

        try {
            epr2 = (EndpointReferenceType) ObjectDeserializer.toObject(any[1],
                    EndpointReferenceType.class);
        } catch (DeserializationException e) {
            throw new GATInvocationException("WSGT4Job: " + e);
        }

        EndpointReferenceType[] endpoints = new EndpointReferenceType[] { epr1,
                epr2 };

        return endpoints;
    }

    private ManagedJobFactoryPortType getManagedJobFactoryPortType(
            EndpointReferenceType factoryEndpoint) throws Exception {
        ManagedJobFactoryPortType factoryPort = ManagedJobFactoryClientHelper
                .getPort(factoryEndpoint);
        setStubSecurityProperties((Stub) factoryPort);
        return factoryPort;
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

    public Authorization getAuthorization() {
        return (authorization == null) ? DEFAULT_AUTHZ : this.authorization;
    }

    public Integer getMessageProtectionType() {
        return (this.msgProtectionType == null) ? WSGT4Job.DEFAULT_MSG_PROTECTION
                : this.msgProtectionType;
    }

    private EndpointReferenceType createJobEndpoint(
            ManagedJobFactoryPortType factoryPort) throws Exception {
        // ((org.apache.axis.client.Stub)
        // factoryPort).setTimeout(this.axisStubTimeOut);
        CreateManagedJobInputType jobInput = new CreateManagedJobInputType();

        jobInput.setInitialTerminationTime(calendar);
        jobInput.setJobID(new AttributedURI(this.submissionID));
        jobInput.setJob(this.jobDescriptionType);
        Map<Object, Object> properties = new HashMap<Object, Object>();
        properties.put(ServiceContainer.CLASS,
                "org.globus.wsrf.container.GSIServiceContainer");
        if (this.proxy != null) {
            ContainerSecurityDescriptor containerSecDesc = new ContainerSecurityDescriptor();
            SecurityManager.getManager();
            containerSecDesc.setSubject(JaasGssUtil.createSubject(this.proxy));
            properties.put(ServiceContainer.CONTAINER_DESCRIPTOR,
                    containerSecDesc);
        }
        this.notificationConsumerManager = NotificationConsumerManager
                .getInstance(properties);

        this.notificationConsumerManager.startListening();

        try {
            List<Object> topicPath = new LinkedList<Object>();
            topicPath.add(ManagedJobConstants.RP_STATE);
            ResourceSecurityDescriptor securityDescriptor = new ResourceSecurityDescriptor();
            String authz = null;
            if (authorization == null) {
                authz = Authorization.AUTHZ_NONE;
            } else if (authorization instanceof HostAuthorization) {
                authz = Authorization.AUTHZ_NONE;
            } else if (authorization instanceof SelfAuthorization) {
                authz = Authorization.AUTHZ_SELF;
            } else if (authorization instanceof IdentityAuthorization) {
                GridMap gridMap = new GridMap();
                gridMap.map(((IdentityAuthorization) authorization)
                        .getIdentity(), "hhh");
                securityDescriptor.setGridMap(gridMap);
                authz = Authorization.AUTHZ_GRIDMAP;
            } else {
                // throw an sg
                return null;
            }
            securityDescriptor.setAuthz(authz);
            Vector<Object> authMethod = new Vector<Object>();
            if (this.securityType.equals(Constants.GSI_SEC_MSG)) {
                authMethod.add(GSISecureMsgAuthMethod.BOTH);
            } else {
                authMethod.add(GSITransportAuthMethod.BOTH);
            }
            securityDescriptor.setAuthMethods(authMethod);

            WSGT4NotifyCallback notifyCallback = new WSGT4NotifyCallback(this);
            notificationConsumerEPR = notificationConsumerManager
                    .createNotificationConsumer(topicPath, notifyCallback,
                            securityDescriptor);
            Subscribe subscriptionRequest = new Subscribe();
            subscriptionRequest.setConsumerReference(notificationConsumerEPR);
            TopicExpressionType topicExpression = new TopicExpressionType(
                    WSNConstants.SIMPLE_TOPIC_DIALECT,
                    ManagedJobConstants.RP_STATE);
            subscriptionRequest.setTopicExpression(topicExpression);
            jobInput.setSubscribe(subscriptionRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
            // throw sg
            /*
             * try { unbind(); } catch (Exception unbindEx) { throw sg }
             */
        }
        CreateManagedJobOutputType response = factoryPort
                .createManagedJob(jobInput);
        EndpointReferenceType jobEPR = response.getManagedJobEndpoint();
        this.notificationProducerEPR = response.getSubscriptionEndpoint();
        return jobEPR;
    }

    protected synchronized void setState(int state) {
        this.state = state;
    }

    void setState(StateEnumeration gstate, boolean holding) {
        this.gstate = gstate;
        this.holding = holding;
        if (gstate.equals(StateEnumeration.CleanUp)
                || gstate.equals(StateEnumeration.StageOut)) {
            setState(WSGT4Job.POST_STAGING);
        }
        if (gstate.equals(StateEnumeration.StageIn)) {
            setState(WSGT4Job.PRE_STAGING);
        }
        if (gstate.equals(StateEnumeration.Active)) {
            setState(WSGT4Job.RUNNING);
        }
        if (gstate.equals(StateEnumeration.Pending)) {
            setState(WSGT4Job.SCHEDULED);
        }
        if (gstate.equals(StateEnumeration.Done)) {
            setState(WSGT4Job.STOPPED);
            /*
             * try { stop(); } catch (GATInvocationException e) {
             * logger.info("stop() failed: " + e); }
             */
        }
        if (gstate.equals(StateEnumeration.Suspended)) {
            setState(WSGT4Job.STOPPED);
        }
        if (gstate.equals(StateEnumeration.Failed)) {
            setState(WSGT4Job.SUBMISSION_ERROR);
            /*
             * try { stop(); } catch (GATInvocationException e) {
             * logger.info("stop() failed: " + e); }
             */
        }
        if (gstate.equals(StateEnumeration.CleanUp)) {
            setState(WSGT4Job.UNKNOWN);
        }
    }

    void setFault(FaultType fault) {
        this.fault = fault;
        System.err.println("errcode: " + FaultType.getTypeDesc());
        System.err.println("gstate at setFault: " + gstate);
    }

    void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public void refreshStatus() throws Exception {
        ManagedJobPortType jobPort = ManagedJobClientHelper
                .getPort(this.jobEndpointReference);
        setStubSecurityProperties((Stub) jobPort);
        GetMultipleResourceProperties_Element request = new GetMultipleResourceProperties_Element();
        request.setResourceProperty(new QName[] { ManagedJobConstants.RP_STATE,
                ManagedJobConstants.RP_HOLDING, ManagedJobConstants.RP_FAULT,
                ManagedExecutableJobConstants.RP_EXIT_CODE });
        GetMultipleResourcePropertiesResponse response = jobPort
                .getMultipleResourceProperties(request);

        SOAPElement[] any = response.get_any();
        StateEnumeration gstate = (StateEnumeration) ObjectDeserializer
                .toObject(any[0], StateEnumeration.class);

        Boolean holding = (Boolean) ObjectDeserializer.toObject(any[1],
                Boolean.class);

        int exitCodeIndex = 0;
        if (gstate.equals(StateEnumeration.Failed)) {
            // set the fault
            FaultType fault = deserializeFaultRP(any[2]);
            this.setFault(fault);

            // where to find the exit code
            exitCodeIndex = 3;
        } else {
            // where to find the exit code
            exitCodeIndex = 2;
        }

        if ((gstate.equals(StateEnumeration.StageOut)
                || gstate.equals(StateEnumeration.Done) || gstate
                .equals(StateEnumeration.Failed))
                && (exitCodeIndex > 0) && (any.length == (exitCodeIndex + 1))) {
            Integer exitCodeWrapper = (Integer) ObjectDeserializer.toObject(
                    any[exitCodeIndex], Integer.class);
            this.exitCode = exitCodeWrapper.intValue();
        }
        this.setState(gstate, holding.booleanValue());
    }

    private FaultType getFaultFromRP(FaultResourcePropertyType fault) {
        if (fault == null) {
            return null;
        }

        if (fault.getFault() != null) {
            return fault.getFault();
        } else if (fault.getCredentialSerializationFault() != null) {
            return fault.getCredentialSerializationFault();
        } else if (fault.getExecutionFailedFault() != null) {
            return fault.getExecutionFailedFault();
        } else if (fault.getFilePermissionsFault() != null) {
            return fault.getFilePermissionsFault();
        } else if (fault.getInsufficientCredentialsFault() != null) {
            return fault.getInsufficientCredentialsFault();
        } else if (fault.getInternalFault() != null) {
            return fault.getInternalFault();
        } else if (fault.getInvalidCredentialsFault() != null) {
            return fault.getInvalidCredentialsFault();
        } else if (fault.getInvalidPathFault() != null) {
            return fault.getInvalidPathFault();
        } else if (fault.getServiceLevelAgreementFault() != null) {
            return fault.getServiceLevelAgreementFault();
        } else if (fault.getStagingFault() != null) {
            return fault.getStagingFault();
        } else if (fault.getUnsupportedFeatureFault() != null) {
            return fault.getUnsupportedFeatureFault();
        } else {
            return null;
        }
    }

    private FaultType deserializeFaultRP(SOAPElement any)
            throws DeserializationException {
        return getFaultFromRP((FaultResourcePropertyType) ObjectDeserializer
                .toObject(any, FaultResourcePropertyType.class));
    }

    private EndpointReferenceType delegate(
            EndpointReferenceType delegationFactoryEndpoint,
            boolean limitedDelegation) throws GATInvocationException {
        GlobusCredential credential = null;
        if (this.proxy != null) {
            credential = ((GlobusGSSCredentialImpl) this.proxy)
                    .getGlobusCredential();
        } else {
            try {
                credential = GlobusCredential.getDefaultCredential();
            } catch (GlobusCredentialException e) {
                throw new GATInvocationException("WSGT4Job: " + e);
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
            throw new GATInvocationException("WSGT4Job: " + e);
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
                    credential, certToSign, lifetime, !limitedDelegation,
                    secDesc);
        } catch (DelegationException e) {
            throw new GATInvocationException("WSGT4Job: " + e);
        }
        return credentialEndpoint;
    }

    private void populateStagingDescriptionEndpoints(
            EndpointReferenceType pfactoryEndpoint,
            EndpointReferenceType delegationFactoryEndpoint,
            JobDescriptionType jobDescription) throws GATInvocationException {
        TransferRequestType stageOut = jobDescription.getFileStageOut();
        TransferRequestType stageIn = jobDescription.getFileStageIn();
        DeleteRequestType cleanUp = jobDescription.getFileCleanUp();
        if ((stageOut != null) || (stageIn != null) || (cleanUp != null)) {
            String factoryAddress = pfactoryEndpoint.getAddress().toString();
            factoryAddress = factoryAddress.replaceFirst(
                    "ManagedJobFactoryService",
                    "ReliableFileTransferFactoryService");
            // delegate to RFT
            EndpointReferenceType transferCredentialEndpoint = delegate(
                    delegationFactoryEndpoint, true);
            // set delegated credential endpoint for stage-out
            if (stageOut != null) {
                stageOut
                        .setTransferCredentialEndpoint(transferCredentialEndpoint);
            }

            // set delegated credential endpoint for stage-in
            if (stageIn != null) {
                stageIn
                        .setTransferCredentialEndpoint(transferCredentialEndpoint);
            }

            // set delegated credential endpoint for clean up
            if (cleanUp != null) {
                cleanUp
                        .setTransferCredentialEndpoint(transferCredentialEndpoint);
            }
        }
    }

    public void stop() throws GATInvocationException {
        sandbox.retrieveAndCleanup(this);
    }

    public synchronized int getExitStatus() throws GATInvocationException {
        if (state != STOPPED)
            throw new GATInvocationException("not in RUNNING state");
        return 0; // We have to assume that the job ran correctly. Globus does
        // not return the exit code.
    }

    public synchronized Map<String, Object> getInfo()
            throws GATInvocationException {
        HashMap<String, Object> m = new HashMap<String, Object>();
        return m;
    }
}
