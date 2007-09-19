package org.gridlab.gat.io.cpi.rftgt4;

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
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;
import javax.xml.soap.SOAPElement;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
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
import org.globus.rft.generated.OverallStatus;
import org.globus.rft.generated.RFTFaultResourcePropertyType;
import org.globus.rft.generated.RFTOptionsType;
import org.globus.rft.generated.ReliableFileTransferFactoryPortType;
import org.globus.rft.generated.ReliableFileTransferPortType;
import org.globus.rft.generated.Start;
import org.globus.rft.generated.TransferRequestType;
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
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileCpi;
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
	RFTGT4FileAdaptor transfer;
	OverallStatus status;

	public RFTGT4NotifyCallback(RFTGT4FileAdaptor transfer) {
		super();
		this.transfer = transfer;
		this.status = null;
	}

	@SuppressWarnings("unchecked")
	public void deliver(List topicPath, EndpointReferenceType producer,
			Object messageWrapper) {
		try {
			ResourcePropertyValueChangeNotificationType message = ((ResourcePropertyValueChangeNotificationElementType) messageWrapper)
					.getResourcePropertyValueChangeNotification();
			this.status = (OverallStatus) message.getNewValue().get_any()[0]
					.getValueAsType(RFTConstants.OVERALL_STATUS_RESOURCE,
							OverallStatus.class);
			if (status.getFault() != null) {
				transfer.setFault(getFaultFromRP(status.getFault()));
			}
			// RunQueue.getInstance().add(this.resourceKey);
		} catch (Exception e) {
		}
		transfer.setStatus(status);
	}

	private BaseFaultType getFaultFromRP(RFTFaultResourcePropertyType fault) {
		if (fault == null) {
			return null;
		}

		if (fault.getDelegationEPRMissingFaultType() != null) {
			return fault.getDelegationEPRMissingFaultType();
		} else if (fault.getRftAuthenticationFaultType() != null) {
			return fault.getRftAuthenticationFaultType();
		} else if (fault.getRftAuthorizationFaultType() != null) {
			return fault.getRftAuthorizationFaultType();
		} else if (fault.getRftDatabaseFaultType() != null) {
			return fault.getRftDatabaseFaultType();
		} else if (fault.getRftRepeatedlyStartedFaultType() != null) {
			return fault.getRftRepeatedlyStartedFaultType();
		} else if (fault.getTransferTransientFaultType() != null) {
			return fault.getTransferTransientFaultType();
		} else if (fault.getRftTransferFaultType() != null) {
			return fault.getRftTransferFaultType();
		} else {
			return null;
		}
	}
}

/**
 * This abstract class implementes the
 * {@link org.gridlab.gat.io.cpi.FileCpi FileCpi} class. Represents an Globus
 * file. That implementation uses the JavaCog abstraction layer. The subclasses
 * represent different File adaptors, using different JavaCog abstraction layer
 * providers.
 * 
 * @author Balazs Bokodi
 * @version 1.0
 * @since 1.0
 */
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
	NotificationConsumerManager notificationConsumerManager;
	EndpointReferenceType notificationConsumerEPR;
	EndpointReferenceType notificationProducerEPR;
	String securityType;
	String factoryUrl;
	GSSCredential proxy;
	Authorization authorization;
	String host;
	OverallStatus status;
	BaseFaultType fault;
	String locationStr;
	ReliableFileTransferFactoryPortType factoryPort;

	/**
	 * Creates new GAT GT4 file object. The constructor is called by the
	 * subclasses.
	 * 
	 * @param gatContext
	 *            GAT context
	 * @param preferences
	 *            GAT preferences
	 * @param location
	 *            FILE location URI
	 * @param prov
	 *            marks the JavaCog provider, possible values are: gt2ft,
	 *            gsiftp, condor, ssh, gt4ft, local, gt4, gsiftp-old, gt3.2.1,
	 *            gt2, ftp, webdav. Aliases: webdav <-> http; local <-> file;
	 *            gsiftp-old <-> gridftp-old; gsiftp <-> gridftp; gt4 <->
	 *            gt3.9.5, gt4.0.2, gt4.0.1, gt4.0.0
	 */
	public RFTGT4FileAdaptor(GATContext gatContext, Preferences preferences,
			URI location) throws GATObjectCreationException {
		super(gatContext, preferences, location);
		if (!location.isCompatible("gsiftp")
				&& !location.isCompatible("gridftp")) {
			throw new GATObjectCreationException("cannot handle this URI");
		}
		this.host = location.getHost();
		this.securityType = Constants.GSI_SEC_MSG;
		this.authorization = null;
		this.proxy = null;
		this.notificationConsumerManager = null;
		this.notificationConsumerEPR = null;
		this.notificationProducerEPR = null;
		this.status = null;
		this.fault = null;
		factoryPort = null;
		this.factoryUrl = PROTOCOL + "://" + host + ":" + DEFAULT_FACTORY_PORT
				+ BASE_SERVICE_PATH + RFTConstants.FACTORY_NAME;
		locationStr = setLocationStr(location);
	}

	String setLocationStr(URI location) {
		if (location.getScheme().equals("any")) {
			return "gsiftp://" + location.getHost() + ":" + location.getPort()
					+ "/" + location.getPath();
		} else {
			return location.toString();
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
	 *            destination location of the file copy
	 * @throws GATInvocationException
	 * 
	 */
	protected boolean copy2(String destStr) throws GATInvocationException {
		EndpointReferenceType credentialEndpoint = getCredentialEPR();

		TransferType[] transferArray = new TransferType[1];
		transferArray[0] = new TransferType();
		transferArray[0].setSourceUrl(locationStr);
		transferArray[0].setDestinationUrl(destStr);

		RFTOptionsType rftOptions = new RFTOptionsType();
		rftOptions.setBinary(Boolean.TRUE);
		// rftOptions.setIgnoreFilePermErr(false);
		TransferRequestType request = new TransferRequestType();
		request.setRftOptions(rftOptions);
		request.setTransfer(transferArray);
		request.setTransferCredentialEndpoint(credentialEndpoint);
		setRequest(request);

		while (!transfersDone()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new GATInvocationException("RFTGT4FileAdaptor: " + e);
			}
		}
		return transfersSucc();
	}

	public void copy(URI dest) throws GATInvocationException {
		System.out.println("rftgt4 copy " + location + " -> " + dest);
		String destUrl = setLocationStr(dest);
		if (!copy2(destUrl)) {
			throw new GATInvocationException(
					"RFTGT4FileAdaptor: file copy failed");
		}
	}

	public void subscribe(ReliableFileTransferPortType rft)
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
		topicPath.add(RFTConstants.OVERALL_STATUS_RESOURCE);
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
					RFTConstants.OVERALL_STATUS_RESOURCE);
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
	}

	protected boolean delete2() throws GATInvocationException {
		EndpointReferenceType credentialEndpoint = getCredentialEPR();

		DeleteType[] deleteArray = new DeleteType[1];
		deleteArray[0] = new DeleteType();
		deleteArray[0].setFile(locationStr);

		DeleteRequestType request = new DeleteRequestType();
		request.setDeletion(deleteArray);
		request.setTransferCredentialEndpoint(credentialEndpoint);

		setRequest(request);
		while (!transfersDone()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new GATInvocationException("RFTGT4FileAdaptor: " + e);
			}
		}
		return transfersSucc();
	}

	public boolean delete() throws GATInvocationException {
		boolean result = delete2();
		// try recursive directory deletion
		if (!result && !locationStr.endsWith("/")) {
			locationStr += "/";
			return delete2();
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

	synchronized void setStatus(OverallStatus status) {
		this.status = status;
	}

	public int transfersActive() {
		if (status == null) {
			return 1;
		}
		return status.getTransfersActive();
	}

	public int transfersFinished() {
		if (status == null) {
			return 0;
		}
		return status.getTransfersFinished();
	}

	public int transfersCancelled() {
		if (status == null) {
			return 0;
		}
		return status.getTransfersCancelled();
	}

	public int transfersFailed() {
		if (status == null) {
			return 0;
		}
		return status.getTransfersFailed();
	}

	public int transfersPending() {
		if (status == null) {
			return 1;
		}
		return status.getTransfersPending();
	}

	public int transfersRestarted() {
		if (status == null) {
			return 0;
		}
		return status.getTransfersRestarted();
	}

	public boolean transfersDone() {
		return (transfersActive() == 0 && transfersPending() == 0 && transfersRestarted() == 0);
	}

	public boolean transfersSucc() {
		return (transfersDone() && transfersFailed() == 0 && transfersCancelled() == 0);
	}

/*	private BaseFaultType getFaultFromRP(RFTFaultResourcePropertyType fault) {
		if (fault == null) {
			return null;
		}

		if (fault.getRftTransferFaultType() != null) {
			return fault.getRftTransferFaultType();
		} else if (fault.getDelegationEPRMissingFaultType() != null) {
			return fault.getDelegationEPRMissingFaultType();
		} else if (fault.getRftAuthenticationFaultType() != null) {
			return fault.getRftAuthenticationFaultType();
		} else if (fault.getRftAuthorizationFaultType() != null) {
			return fault.getRftAuthorizationFaultType();
		} else if (fault.getRftDatabaseFaultType() != null) {
			return fault.getRftDatabaseFaultType();
		} else if (fault.getRftRepeatedlyStartedFaultType() != null) {
			return fault.getRftRepeatedlyStartedFaultType();
		} else if (fault.getTransferTransientFaultType() != null) {
			return fault.getTransferTransientFaultType();
		} else {
			return null;
		}
	}*/

	/*private BaseFaultType deserializeFaultRP(SOAPElement any) throws Exception {
		return getFaultFromRP((RFTFaultResourcePropertyType) ObjectDeserializer
				.toObject(any, RFTFaultResourcePropertyType.class));
	}*/

	void setFault(BaseFaultType fault) {
		this.fault = fault;
	}

}
