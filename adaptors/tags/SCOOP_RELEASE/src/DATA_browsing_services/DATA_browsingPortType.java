/**
 * DATA_browsingPortType.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package DATA_browsing_services;

public interface DATA_browsingPortType extends java.rmi.Remote {

	// Service definition of function ns__DATAList
	public void DATAList(java.lang.String inURL, int verbose,
			javax.xml.rpc.holders.StringHolder retlist,
			javax.xml.rpc.holders.StringHolder response)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAConnectedList
	public void DATAConnectedList(java.lang.String inURL, int verbose,
			javax.xml.rpc.holders.StringHolder retlist,
			javax.xml.rpc.holders.StringHolder response)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAConnectedListStructured
	public void DATAConnectedListStructured(java.lang.String inURL,
			DATA_browsing_services.holders.ArrayOfDirectoryEntryHolder entries,
			javax.xml.rpc.holders.StringHolder response)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAStopCache
	public java.lang.String DATAStopCache() throws java.rmi.RemoteException;

	// Service definition of function ns__DATAConnectedModTime
	public void DATAConnectedModTime(java.lang.String inURL,
			javax.xml.rpc.holders.LongHolder seconds,
			javax.xml.rpc.holders.StringHolder response)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAConnectedSize
	public void DATAConnectedSize(java.lang.String inURL,
			org.apache.axis.holders.UnsignedLongHolder size,
			javax.xml.rpc.holders.StringHolder response)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAConnectedMkdir
	public java.lang.String DATAConnectedMkdir(java.lang.String inURL)
			throws java.rmi.RemoteException;

	// Service definition of function ns__isAlive
	public int isAlive(int dump) throws java.rmi.RemoteException;

	// Service definition of function ns__getServiceDescription
	public java.lang.String getServiceDescription()
			throws java.rmi.RemoteException;
}