/**
 * CsrdmsPortType.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package csrdms_pkg;

public interface CsrdmsPortType extends java.rmi.Remote {

	// Service definition of function ns__getHomeDirectory
	public void getHomeDirectory(java.lang.String usersubject,
			javax.xml.rpc.holders.StringHolder pathname,
			javax.xml.rpc.holders.StringHolder retcode)
			throws java.rmi.RemoteException;

	// Service definition of function ns__create
	public java.lang.String create(java.lang.String pathname,
			java.lang.String filetype) throws java.rmi.RemoteException;

	// Service definition of function ns__rm
	public java.lang.String rm(java.lang.String pathname)
			throws java.rmi.RemoteException;

	// Service definition of function ns__existsEntity
	public void existsEntity(java.lang.String pathname,
			javax.xml.rpc.holders.BooleanHolder result,
			javax.xml.rpc.holders.StringHolder ret)
			throws java.rmi.RemoteException;

	// Service definition of function ns__isFile
	public void isFile(java.lang.String pathname,
			javax.xml.rpc.holders.BooleanHolder file,
			javax.xml.rpc.holders.StringHolder ret)
			throws java.rmi.RemoteException;

	// Service definition of function ns__isDirectory
	public void isDirectory(java.lang.String pathname,
			javax.xml.rpc.holders.BooleanHolder directory,
			javax.xml.rpc.holders.StringHolder ret)
			throws java.rmi.RemoteException;

	// Service definition of function ns__setOwner
	public java.lang.String setOwner(java.lang.String pathname,
			java.lang.String owner) throws java.rmi.RemoteException;

	// Service definition of function ns__getOwner
	public void getOwner(java.lang.String pathname,
			javax.xml.rpc.holders.StringHolder owner,
			javax.xml.rpc.holders.StringHolder ret)
			throws java.rmi.RemoteException;

	// Service definition of function ns__mkdir
	public java.lang.String mkdir(java.lang.String pathname)
			throws java.rmi.RemoteException;

	// Service definition of function ns__mkdirhier
	public java.lang.String mkdirhier(java.lang.String pathname)
			throws java.rmi.RemoteException;

	// Service definition of function ns__rmdir
	public java.lang.String rmdir(java.lang.String pathname)
			throws java.rmi.RemoteException;

	// Service definition of function ns__ls
	public void ls(java.lang.String pathname,
			csrdms_pkg.holders.StringArrayHolder directoryContent,
			javax.xml.rpc.holders.StringHolder retcode)
			throws java.rmi.RemoteException;

	// Service definition of function ns__lsX
	public void lsX(java.lang.String pathname,
			csrdms_pkg.holders.PairOfStringBoolArrayHolder directoryContent,
			javax.xml.rpc.holders.StringHolder retcode)
			throws java.rmi.RemoteException;

	// Service definition of function ns__addLocation
	public java.lang.String addLocation(java.lang.String pathname,
			java.lang.String uri) throws java.rmi.RemoteException;

	// Service definition of function ns__removeLocation
	public java.lang.String removeLocation(java.lang.String pathname,
			java.lang.String uri) throws java.rmi.RemoteException;

	// Service definition of function ns__getLocations
	public void getLocations(java.lang.String pathname,
			csrdms_pkg.holders.StringArrayHolder locations,
			javax.xml.rpc.holders.StringHolder retcode)
			throws java.rmi.RemoteException;

	// Service definition of function ns__addAttribute
	public java.lang.String addAttribute(java.lang.String pathname,
			java.lang.String attribute, java.lang.String value)
			throws java.rmi.RemoteException;

	// Service definition of function ns__setAttribute
	public java.lang.String setAttribute(java.lang.String pathname,
			java.lang.String attribute, java.lang.String value)
			throws java.rmi.RemoteException;

	// Service definition of function ns__removeAttribute
	public java.lang.String removeAttribute(java.lang.String pathname,
			java.lang.String attribute) throws java.rmi.RemoteException;

	// Service definition of function ns__getAttributes
	public void getAttributes(java.lang.String pathname,
			csrdms_pkg.holders.PairOfStringArrayHolder attributes,
			javax.xml.rpc.holders.StringHolder retcode)
			throws java.rmi.RemoteException;

	// Service definition of function ns__getObjects
	public void getObjects(java.lang.String query,
			csrdms_pkg.holders.StringArrayHolder paths,
			javax.xml.rpc.holders.StringHolder retcode)
			throws java.rmi.RemoteException;

	// Service definition of function ns__getServiceDescription
	public void getServiceDescription(java.lang.String in,
			javax.xml.rpc.holders.StringHolder description,
			javax.xml.rpc.holders.StringHolder ret)
			throws java.rmi.RemoteException;

	// Service definition of function ns__replicateFileTo
	public java.lang.String replicateFileTo(java.lang.String pathname,
			java.lang.String url) throws java.rmi.RemoteException;

	// Service definition of function ns__fetchFileTo
	public java.lang.String fetchFileTo(java.lang.String pathname,
			java.lang.String url) throws java.rmi.RemoteException;

	// Service definition of function ns__replicateDirectoryTo
	public java.lang.String replicateDirectoryTo(java.lang.String pathname,
			java.lang.String url) throws java.rmi.RemoteException;

	// Service definition of function ns__fetchDirectoryTo
	public java.lang.String fetchDirectoryTo(java.lang.String pathname,
			java.lang.String url) throws java.rmi.RemoteException;
}