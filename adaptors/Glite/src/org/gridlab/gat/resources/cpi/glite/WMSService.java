package org.gridlab.gat.resources.cpi.glite;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.To;
import org.apache.axis.types.URI.MalformedURIException;
import org.glite.wms.wmproxy.WMProxyLocator;
import org.glite.wms.wmproxy.WMProxy_PortType;
import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.transport.HTTPSSender;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;
import org.globus.wsrf.security.Constants;
import org.gridlab.gat.GATInvocationException;
import org.gridsite.www.namespaces.delegation_1.DelegationSoapBindingStub;
import org.ietf.jgss.GSSCredential;

public class WMSService {
	
	private final WMProxy_PortType wmProxyServiceStub;
	private final DelegationSoapBindingStub delegationServiceStub;
	private final URL wmsURL;

	/**
	 * Construct the service stubs necessary to communicate with the workload
	 * management (WM) node
	 * 
	 * @param brokerURI
	 *            The URI of the WM
	 * @throws GATInvocationException
	 */
	public WMSService(final String brokerURI, String proxyFile, GSSCredential userCredential) throws GATInvocationException {
		try {

			// make it work with the axis services
			// the axis service will only accept the uri if the protocol is
			// known to them
			// while any:// is not known to them, https:// will work.
			String axisBrokerURI;
			if (brokerURI.startsWith("glite://")) {
			    axisBrokerURI = brokerURI.replaceFirst("glite://", "https://");
			} else {
			    axisBrokerURI = brokerURI.replaceFirst("any://", "https://");
			}
			this.wmsURL = new URL(axisBrokerURI);
			// use engine configuration with settings hardcoded for a client
			// this seems to resolve multithreading issues
			// Set provider
			SimpleProvider provider = new SimpleProvider();
			SimpleTargetedChain c =  new SimpleTargetedChain(new HTTPSSender());
			provider.deployTransport("https", c);
			c =  new SimpleTargetedChain(new HTTPSSender());
			provider.deployTransport("http", c);
			
			WMProxyLocator serviceLocator = new WMProxyLocator(provider);
			
			AddressingHeaders headers = new AddressingHeaders();
			try {
				headers.setTo(new To(axisBrokerURI));
			} catch (MalformedURIException e) {}
			wmProxyServiceStub = serviceLocator.getWMProxy_PortType(wmsURL);
            ((Stub)this.wmProxyServiceStub)._setProperty(GSIConstants.GSI_CREDENTIALS,userCredential);
            ((Stub)this.wmProxyServiceStub)._setProperty(Constants.GSI_TRANSPORT, Constants.ENCRYPTION);
            ((Stub)this.wmProxyServiceStub)._setProperty(Constants.AUTHORIZATION, NoAuthorization.getInstance());
            ((Stub)this.wmProxyServiceStub)._setProperty(org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS, headers);
            
			delegationServiceStub = (DelegationSoapBindingStub) serviceLocator.getWMProxyDelegation_PortType(wmsURL);
            ((Stub)this.delegationServiceStub)._setProperty(GSIConstants.GSI_CREDENTIALS,userCredential);
            ((Stub)this.delegationServiceStub)._setProperty(Constants.GSI_TRANSPORT, Constants.ENCRYPTION);
            ((Stub)this.delegationServiceStub)._setProperty(Constants.AUTHORIZATION, NoAuthorization.getInstance());
            ((Stub)this.delegationServiceStub)._setProperty(org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS, headers);
		} catch (MalformedURLException e) {
			throw new GATInvocationException("Broker URI is malformed!", e);
		} catch (ServiceException e) {
			throw new GATInvocationException("Could not get service stub for WMS-Node!", e);
		}
	}
	
	public WMProxy_PortType getWMProxyServiceStub() {
		return wmProxyServiceStub;
	}

	public DelegationSoapBindingStub getDelegationServiceStub() {
		return delegationServiceStub;
	}
	
	public URL getWmsURL() {
		return wmsURL;
	}

}
