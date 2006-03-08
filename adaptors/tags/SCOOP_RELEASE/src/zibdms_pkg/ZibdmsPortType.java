/**
 * ZibdmsPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package zibdms_pkg;

public interface ZibdmsPortType extends java.rmi.Remote {

    // Service definition of function GridLabLogicalfileNS1__getHomeDirectory
    public void getHomeDirectory(java.lang.String usersubject, javax.xml.rpc.holders.StringHolder pathname, javax.xml.rpc.holders.StringHolder retcode) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__create
    public java.lang.String create(java.lang.String pathname, java.lang.String filetype) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__rm
    public java.lang.String rm(java.lang.String pathname) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__existsEntity
    public void existsEntity(java.lang.String pathname, javax.xml.rpc.holders.BooleanHolder result, javax.xml.rpc.holders.StringHolder ret) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__isFile
    public void isFile(java.lang.String pathname, javax.xml.rpc.holders.BooleanHolder file, javax.xml.rpc.holders.StringHolder ret) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__isDirectory
    public void isDirectory(java.lang.String pathname, javax.xml.rpc.holders.BooleanHolder directory, javax.xml.rpc.holders.StringHolder ret) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__mkdir
    public java.lang.String mkdir(java.lang.String pathname) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__mkdirhier
    public java.lang.String mkdirhier(java.lang.String pathname) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__rmdir
    public java.lang.String rmdir(java.lang.String pathname) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__ls
    public void ls(java.lang.String pathname, zibdms_pkg.holders.StringArrayHolder directoryContent, javax.xml.rpc.holders.StringHolder retcode) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__lsX
    public void lsX(java.lang.String pathname, zibdms_pkg.holders.PairOfStringBoolArrayHolder directoryContent, javax.xml.rpc.holders.StringHolder retcode) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__addLocation
    public java.lang.String addLocation(java.lang.String pathname, java.lang.String uri) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__removeLocation
    public java.lang.String removeLocation(java.lang.String pathname, java.lang.String uri) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__getLocations
    public void getLocations(java.lang.String pathname, zibdms_pkg.holders.StringArrayHolder locations, javax.xml.rpc.holders.StringHolder retcode) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__getServiceDescription
    public void getServiceDescription(java.lang.String in, javax.xml.rpc.holders.StringHolder description, javax.xml.rpc.holders.StringHolder ret) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__replicateFileTo
    public java.lang.String replicateFileTo(java.lang.String pathname, java.lang.String url) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__fetchFileTo
    public java.lang.String fetchFileTo(java.lang.String pathname, java.lang.String url) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__replicateDirectoryTo
    public java.lang.String replicateDirectoryTo(java.lang.String pathname, java.lang.String url) throws java.rmi.RemoteException;

    // Service definition of function GridLabLogicalfileNS1__fetchDirectoryTo
    public java.lang.String fetchDirectoryTo(java.lang.String pathname, java.lang.String url) throws java.rmi.RemoteException;
}
