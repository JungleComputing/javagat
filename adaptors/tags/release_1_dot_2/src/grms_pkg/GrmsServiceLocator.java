/**
 * GrmsServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package grms_pkg;

public class GrmsServiceLocator extends org.apache.axis.client.Service
        implements grms_pkg.GrmsService {

    // Use to get a proxy class for grms
    //    private final java.lang.String grms_address = "httpg://rage1.man.poznan.pl:8543/axis/services/grms";
    private final java.lang.String grms_address = "httpg://rage1.man.poznan.pl:8443/axis/services/grms";

    public java.lang.String getgrmsAddress() {
        return grms_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String grmsWSDDServiceName = "grms";

    public java.lang.String getgrmsWSDDServiceName() {
        return grmsWSDDServiceName;
    }

    public void setgrmsWSDDServiceName(java.lang.String name) {
        grmsWSDDServiceName = name;
    }

    public grms_pkg.Grms getgrms() throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(grms_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getgrms(endpoint);
    }

    public grms_pkg.Grms getgrms(java.net.URL portAddress)
        throws javax.xml.rpc.ServiceException {
        try {
            grms_pkg.GrmsSoapBindingStub _stub = new grms_pkg.GrmsSoapBindingStub(
                portAddress, this);
            _stub.setPortName(getgrmsWSDDServiceName());
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
            if (grms_pkg.Grms.class.isAssignableFrom(serviceEndpointInterface)) {
                grms_pkg.GrmsSoapBindingStub _stub = new grms_pkg.GrmsSoapBindingStub(
                    new java.net.URL(grms_address), this);
                _stub.setPortName(getgrmsWSDDServiceName());
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
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName,
        Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("grms".equals(inputPortName)) {
            return getgrms();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:grms", "grmsService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("grms"));
        }
        return ports.iterator();
    }

}
