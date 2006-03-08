/**
 * IgridLocator.java
 *
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */
package igrid_pkg;

public class IgridLocator extends org.apache.axis.client.Service implements
        igrid_pkg.Igrid {
    // gSOAP 2.6.2 generated service definition
    // Use to get a proxy class for igrid
    private final java.lang.String igrid_address = "httpg://mds.gridlab.org:19000";

    // The WSDD service name defaults to the port name.
    private java.lang.String igridWSDDServiceName = "igrid";

    private java.util.HashSet ports = null;

    public java.lang.String getigridAddress() {
        return igrid_address;
    }

    public java.lang.String getigridWSDDServiceName() {
        return igridWSDDServiceName;
    }

    public void setigridWSDDServiceName(java.lang.String name) {
        igridWSDDServiceName = name;
    }

    public igrid_pkg.IgridPortType getigrid()
            throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;

        try {
            endpoint = new java.net.URL(igrid_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }

        return getigrid(endpoint);
    }

    public igrid_pkg.IgridPortType getigrid(java.net.URL portAddress)
            throws javax.xml.rpc.ServiceException {
        try {
            igrid_pkg.IgridStub _stub = new igrid_pkg.IgridStub(portAddress,
                this);
            _stub.setPortName(getigridWSDDServiceName());

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
            if (igrid_pkg.IgridPortType.class
                .isAssignableFrom(serviceEndpointInterface)) {
                igrid_pkg.IgridStub _stub = new igrid_pkg.IgridStub(
                    new java.net.URL(igrid_address), this);
                _stub.setPortName(getigridWSDDServiceName());

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

        if ("igrid".equals(inputPortName)) {
            return getigrid();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);

            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:igrid", "igrid");
    }

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("igrid"));
        }

        return ports.iterator();
    }
}
