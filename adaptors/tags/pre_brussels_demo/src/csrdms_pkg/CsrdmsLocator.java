/**
 * CsrdmsLocator.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package csrdms_pkg;

public class CsrdmsLocator extends org.apache.axis.client.Service implements
		csrdms_pkg.Csrdms {

	// gSOAP 2.3.8 generated service definition

	// Use to get a proxy class for csrdms
	private final java.lang.String csrdms_address = "httpg://litchi.zib.de:6667/csrdms-service";

	public java.lang.String getcsrdmsAddress() {
		return csrdms_address;
	}

	// The WSDD service name defaults to the port name.
	private java.lang.String csrdmsWSDDServiceName = "csrdms";

	public java.lang.String getcsrdmsWSDDServiceName() {
		return csrdmsWSDDServiceName;
	}

	public void setcsrdmsWSDDServiceName(java.lang.String name) {
		csrdmsWSDDServiceName = name;
	}

	public csrdms_pkg.CsrdmsPortType getcsrdms()
			throws javax.xml.rpc.ServiceException {
		java.net.URL endpoint;
		try {
			endpoint = new java.net.URL(csrdms_address);
		} catch (java.net.MalformedURLException e) {
			throw new javax.xml.rpc.ServiceException(e);
		}
		return getcsrdms(endpoint);
	}

	public csrdms_pkg.CsrdmsPortType getcsrdms(java.net.URL portAddress)
			throws javax.xml.rpc.ServiceException {
		try {
			csrdms_pkg.CsrdmsBindingStub _stub = new csrdms_pkg.CsrdmsBindingStub(
					portAddress, this);
			_stub.setPortName(getcsrdmsWSDDServiceName());
			return _stub;
		} catch (org.apache.axis.AxisFault e) {
			return null;
		}
	}

	/**
	 * For the given interface, get the stub implementation. If this service has
	 * no port for the given interface, then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(Class serviceEndpointInterface)
			throws javax.xml.rpc.ServiceException {
		try {
			if (csrdms_pkg.CsrdmsPortType.class
					.isAssignableFrom(serviceEndpointInterface)) {
				csrdms_pkg.CsrdmsBindingStub _stub = new csrdms_pkg.CsrdmsBindingStub(
						new java.net.URL(csrdms_address), this);
				_stub.setPortName(getcsrdmsWSDDServiceName());
				return _stub;
			}
		} catch (java.lang.Throwable t) {
			throw new javax.xml.rpc.ServiceException(t);
		}
		throw new javax.xml.rpc.ServiceException(
				"There is no stub implementation for the interface:  "
						+ (serviceEndpointInterface == null ? "null"
								: serviceEndpointInterface.getName()));
	}

	/**
	 * For the given interface, get the stub implementation. If this service has
	 * no port for the given interface, then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(javax.xml.namespace.QName portName,
			Class serviceEndpointInterface)
			throws javax.xml.rpc.ServiceException {
		if (portName == null) {
			return getPort(serviceEndpointInterface);
		}
		String inputPortName = portName.getLocalPart();
		if ("csrdms".equals(inputPortName)) {
			return getcsrdms();
		} else {
			java.rmi.Remote _stub = getPort(serviceEndpointInterface);
			((org.apache.axis.client.Stub) _stub).setPortName(portName);
			return _stub;
		}
	}

	public javax.xml.namespace.QName getServiceName() {
		return new javax.xml.namespace.QName("urn:csrdms", "csrdms");
	}

	private java.util.HashSet ports = null;

	public java.util.Iterator getPorts() {
		if (ports == null) {
			ports = new java.util.HashSet();
			ports.add(new javax.xml.namespace.QName("csrdms"));
		}
		return ports.iterator();
	}

}