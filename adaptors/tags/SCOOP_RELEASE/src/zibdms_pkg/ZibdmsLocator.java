/**
 * ZibdmsLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package zibdms_pkg;

public class ZibdmsLocator extends org.apache.axis.client.Service implements zibdms_pkg.Zibdms {

    // gSOAP 2.6.2 generated service definition

    // Use to get a proxy class for zibdms
    private final java.lang.String zibdms_address = "httpg://cluster3.zib.de:4711/zibdms-service";

    public java.lang.String getzibdmsAddress() {
        return zibdms_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String zibdmsWSDDServiceName = "zibdms";

    public java.lang.String getzibdmsWSDDServiceName() {
        return zibdmsWSDDServiceName;
    }

    public void setzibdmsWSDDServiceName(java.lang.String name) {
        zibdmsWSDDServiceName = name;
    }

    public zibdms_pkg.ZibdmsPortType getzibdms() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(zibdms_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getzibdms(endpoint);
    }

    public zibdms_pkg.ZibdmsPortType getzibdms(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            zibdms_pkg.ZibdmsStub _stub = new zibdms_pkg.ZibdmsStub(portAddress, this);
            _stub.setPortName(getzibdmsWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (zibdms_pkg.ZibdmsPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                zibdms_pkg.ZibdmsStub _stub = new zibdms_pkg.ZibdmsStub(new java.net.URL(zibdms_address), this);
                _stub.setPortName(getzibdmsWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("zibdms".equals(inputPortName)) {
            return getzibdms();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:zibdms", "zibdms");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("zibdms"));
        }
        return ports.iterator();
    }

}
