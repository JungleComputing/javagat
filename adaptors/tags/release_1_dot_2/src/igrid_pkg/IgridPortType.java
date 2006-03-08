/**
 * IgridPortType.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package igrid_pkg;

public interface IgridPortType extends java.rmi.Remote {

    // Service definition of function gridlab__register_webservice
    public int registerWebservice(java.lang.String name,
        java.lang.String wsdllocation, java.lang.String description,
        java.lang.String url, java.lang.String keywords, int validityTime)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__unregister_webservice
    public int unregisterWebservice(java.lang.String name,
        java.lang.String url, int allinst) throws java.rmi.RemoteException;

    // Service definition of function gridlab__istore
    public int istore(igrid_pkg.DimeData data) throws java.rmi.RemoteException;

    // Service definition of function gridlab__search
    public java.lang.String search(java.lang.String filter)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__update_webservice
    public int updateWebservice(java.lang.String name,
        java.lang.String keywords, java.lang.String description,
        java.lang.String url, int validityTime) throws java.rmi.RemoteException;

    // Service definition of function gridlab__register_service
    public int registerService(java.lang.String name,
        java.lang.String hostname, int port, java.lang.String protocol,
        int dport, java.lang.String description, java.lang.String keywords,
        int validityTime) throws java.rmi.RemoteException;

    // Service definition of function gridlab__unregister_service
    public int unregisterService(java.lang.String name,
        java.lang.String hostname, int port, int allinst)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__update_service
    public int updateService(java.lang.String name, java.lang.String keywords,
        java.lang.String description, java.lang.String url, int dport,
        int validityTime) throws java.rmi.RemoteException;

    // Service definition of function gridlab__lookup_webservice
    public java.lang.String lookupWebservice(java.lang.String name,
        java.lang.String hostname) throws java.rmi.RemoteException;

    // Service definition of function gridlab__lookup_service
    public java.lang.String lookupService(java.lang.String name,
        java.lang.String hostname, int dport, int sport, java.lang.String prot)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__lookup_firewall
    public java.lang.String lookupFirewall(java.lang.String name)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__lookup_system
    public java.lang.String lookupSystem(java.lang.String name,
        java.lang.String system) throws java.rmi.RemoteException;

    // Service definition of function gridlab__lookup_user
    public java.lang.String lookupUser(java.lang.String name)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__lookup_vo
    public java.lang.String lookupVo(java.lang.String name)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__lookup_ca
    public java.lang.String lookupCa(java.lang.String name)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__lookup_lrms
    public java.lang.String lookupLrms(java.lang.String name)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__lookup_cpu
    public java.lang.String lookupCpu(int mhz, int cache, int number, float load)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__lookup_memory
    public java.lang.String lookupMemory(int totalRam, int totalSwap,
        int freeRam, int freeSwap) throws java.rmi.RemoteException;

    // Service definition of function gridlab__lookup_network
    public java.lang.String lookupNetwork(java.lang.String address)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__lookup_device
    public java.lang.String lookupDevice(java.lang.String device)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__register_vo
    public int registerVo(java.lang.String name, java.lang.String helpDeskPN,
        java.lang.String restype, java.lang.String jobm,
        java.lang.String queue, java.lang.String fsPath,
        java.lang.String helpDeskURL, java.lang.String adminname,
        java.lang.String host, int validityTime)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__unregister_vo
    public int unregisterVo(java.lang.String name, java.lang.String host,
        int allinst) throws java.rmi.RemoteException;

    // Service definition of function gridlab__update_vo
    public int updateVo(java.lang.String name, java.lang.String restype,
        java.lang.String jobm, java.lang.String queue,
        java.lang.String helpDeskURL, java.lang.String adminname,
        java.lang.String host, int validityTime)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__register_firewall
    public int registerFirewall(java.lang.String hostname,
        java.lang.String ports, java.lang.String admindn,
        java.lang.String host, int validityTime)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__unregister_firewall
    public int unregisterFirewall(java.lang.String hostname)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__update_firewall
    public int updateFirewall(java.lang.String hostname,
        java.lang.String admindn, java.lang.String ports, int typePor,
        int validityTime) throws java.rmi.RemoteException;

    // Service definition of function gridlab__register_wsdlloc
    public int registerWsdlloc(java.lang.String name, java.lang.String wsdlloc)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__unregister_wsdlloc
    public int unregisterWsdlloc(java.lang.String name, java.lang.String wsdlloc)
        throws java.rmi.RemoteException;

    // Service definition of function gridlab__getServiceDescription
    public java.lang.String getServiceDescription()
        throws java.rmi.RemoteException;
}
