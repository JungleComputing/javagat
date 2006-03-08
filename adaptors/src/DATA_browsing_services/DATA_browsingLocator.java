/**
 * DATA_browsingLocator.java
 *
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */
package DATA_browsing_services;

public class DATA_browsingLocator extends org.apache.axis.client.Service
        implements DATA_browsing_services.DATA_browsing {
    // gSOAP 2.3.8 generated service definition
    // Use to get a proxy class for DATA_browsing
    private final java.lang.String DATA_browsing_address = "httpg://litchi.zib.de:18092";

    // The WSDD service name defaults to the port name.
    private java.lang.String DATA_browsingWSDDServiceName = "DATA_browsing";

    private java.util.HashSet ports = null;

    public java.lang.String getDATA_browsingAddress() {
        return DATA_browsing_address;
    }

    public java.lang.String getDATA_browsingWSDDServiceName() {
        return DATA_browsingWSDDServiceName;
    }

    public void setDATA_browsingWSDDServiceName(java.lang.String name) {
        DATA_browsingWSDDServiceName = name;
    }

    public DATA_browsing_services.DATA_browsingPortType getDATA_browsing()
            throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;

        try {
            endpoint = new java.net.URL(DATA_browsing_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }

        return getDATA_browsing(endpoint);
    }

    public DATA_browsing_services.DATA_browsingPortType getDATA_browsing(
            java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            DATA_browsing_services.DATA_browsingBindingStub _stub = new DATA_browsing_services.DATA_browsingBindingStub(
                portAddress, this);
            _stub.setPortName(getDATA_browsingWSDDServiceName());

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
            if (DATA_browsing_services.DATA_browsingPortType.class
                .isAssignableFrom(serviceEndpointInterface)) {
                DATA_browsing_services.DATA_browsingBindingStub _stub = new DATA_browsing_services.DATA_browsingBindingStub(
                    new java.net.URL(DATA_browsing_address), this);
                _stub.setPortName(getDATA_browsingWSDDServiceName());

                return _stub;
            }
        } catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }

        throw new javax.xml.rpc.ServiceException(
            "There is no stub implementation for the interface:  "
                + ((serviceEndpointInterface == null) ? "null"
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

        if ("DATA_browsing".equals(inputPortName)) {
            return getDATA_browsing();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);

            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:DATA_browsing_services",
            "DATA_browsing");
    }

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("DATA_browsing"));
        }

        return ports.iterator();
    }
}
