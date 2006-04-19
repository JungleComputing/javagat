/**
 * DATA_movementLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */
package DATA_movement_services;

public class DATA_movementLocator extends org.apache.axis.client.Service
        implements DATA_movement_services.DATA_movement {
    // gSOAP 2.6.2 generated service definition
    // Use to get a proxy class for DATA_movement
    private final java.lang.String DATA_movement_address = "httpg://cluster3.zib.de:18090";

    // The WSDD service name defaults to the port name.
    private java.lang.String DATA_movementWSDDServiceName = "DATA_movement";

    private java.util.HashSet ports = null;

    public java.lang.String getDATA_movementAddress() {
        return DATA_movement_address;
    }

    public java.lang.String getDATA_movementWSDDServiceName() {
        return DATA_movementWSDDServiceName;
    }

    public void setDATA_movementWSDDServiceName(java.lang.String name) {
        DATA_movementWSDDServiceName = name;
    }

    public DATA_movement_services.DATA_movementPortType getDATA_movement()
            throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;

        try {
            endpoint = new java.net.URL(DATA_movement_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }

        return getDATA_movement(endpoint);
    }

    public DATA_movement_services.DATA_movementPortType getDATA_movement(
            java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            DATA_movement_services.DATA_movementStub _stub = new DATA_movement_services.DATA_movementStub(
                portAddress, this);
            _stub.setPortName(getDATA_movementWSDDServiceName());

            return _stub;
        } catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface)
            throws javax.xml.rpc.ServiceException {
        try {
            if (DATA_movement_services.DATA_movementPortType.class
                .isAssignableFrom(serviceEndpointInterface)) {
                DATA_movement_services.DATA_movementStub _stub = new DATA_movement_services.DATA_movementStub(
                    new java.net.URL(DATA_movement_address), this);
                _stub.setPortName(getDATA_movementWSDDServiceName());

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
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName,
            Class serviceEndpointInterface)
            throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }

        String inputPortName = portName.getLocalPart();

        if ("DATA_movement".equals(inputPortName)) {
            return getDATA_movement();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);

            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:DATA_movement_services",
            "DATA_movement");
    }

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("DATA_movement"));
        }

        return ports.iterator();
    }
}
