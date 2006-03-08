/**
 * StorageBoxServiceLocator.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package org.storagebox.www.sbns;

public class StorageBoxServiceLocator extends org.apache.axis.client.Service
		implements org.storagebox.www.sbns.StorageBoxService {

	// Generated by the powers of evil (other than gSOAP)

	// Use to get a proxy class for StorageBoxPort
	private final java.lang.String StorageBoxPort_address = "http://localhost:80";

	public java.lang.String getStorageBoxPortAddress() {
		return StorageBoxPort_address;
	}

	// The WSDD service name defaults to the port name.
	private java.lang.String StorageBoxPortWSDDServiceName = "StorageBoxPort";

	public java.lang.String getStorageBoxPortWSDDServiceName() {
		return StorageBoxPortWSDDServiceName;
	}

	public void setStorageBoxPortWSDDServiceName(java.lang.String name) {
		StorageBoxPortWSDDServiceName = name;
	}

	public org.storagebox.www.sbns.StorageBoxPortType getStorageBoxPort()
			throws javax.xml.rpc.ServiceException {
		java.net.URL endpoint;
		try {
			endpoint = new java.net.URL(StorageBoxPort_address);
		} catch (java.net.MalformedURLException e) {
			throw new javax.xml.rpc.ServiceException(e);
		}
		return getStorageBoxPort(endpoint);
	}

	public org.storagebox.www.sbns.StorageBoxPortType getStorageBoxPort(
			java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
		try {
			org.storagebox.www.sbns.StorageBoxSOAPBindingStub _stub = new org.storagebox.www.sbns.StorageBoxSOAPBindingStub(
					portAddress, this);
			_stub.setPortName(getStorageBoxPortWSDDServiceName());
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
			if (org.storagebox.www.sbns.StorageBoxPortType.class
					.isAssignableFrom(serviceEndpointInterface)) {
				org.storagebox.www.sbns.StorageBoxSOAPBindingStub _stub = new org.storagebox.www.sbns.StorageBoxSOAPBindingStub(
						new java.net.URL(StorageBoxPort_address), this);
				_stub.setPortName(getStorageBoxPortWSDDServiceName());
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
		if ("StorageBoxPort".equals(inputPortName)) {
			return getStorageBoxPort();
		} else {
			java.rmi.Remote _stub = getPort(serviceEndpointInterface);
			((org.apache.axis.client.Stub) _stub).setPortName(portName);
			return _stub;
		}
	}

	public javax.xml.namespace.QName getServiceName() {
		return new javax.xml.namespace.QName("http://www.storagebox.org/sbns",
				"StorageBoxService");
	}

	private java.util.HashSet ports = null;

	public java.util.Iterator getPorts() {
		if (ports == null) {
			ports = new java.util.HashSet();
			ports.add(new javax.xml.namespace.QName("StorageBoxPort"));
		}
		return ports.iterator();
	}

}