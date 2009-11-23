package org.gridlab.gat.resources.cpi.glite;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.apache.axis.configuration.BasicClientConfig;
import org.glite.wms.wmproxy.WMProxyLocator;
import org.glite.wms.wmproxy.WMProxy_PortType;
import org.gridlab.gat.GATInvocationException;
import org.gridsite.www.namespaces.delegation_1.DelegationSoapBindingStub;

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
	public WMSService(final String brokerURI) throws GATInvocationException {
		try {

			// make it work with the axis services
			// the axis service will only accept the uri if the protocol is
			// known to them
			// while any:// is not known to them, https:// will work
			String axisBrokerURI = brokerURI.replaceFirst("any://", "https://");
			this.wmsURL = new URL(axisBrokerURI);

			// use engine configuration with settings hardcoded for a client
			// this seems to resolve multithreading issues
			WMProxyLocator serviceLocator = new WMProxyLocator(new BasicClientConfig());

			wmProxyServiceStub = serviceLocator.getWMProxy_PortType(wmsURL);

			delegationServiceStub = (DelegationSoapBindingStub) serviceLocator.getWMProxyDelegation_PortType(wmsURL);

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
