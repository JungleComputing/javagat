/**
 * StorageBoxService.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package org.storagebox.www.sbns;

public interface StorageBoxService extends javax.xml.rpc.Service {

	// Generated by the powers of evil (other than gSOAP)
	public java.lang.String getStorageBoxPortAddress();

	public org.storagebox.www.sbns.StorageBoxPortType getStorageBoxPort()
			throws javax.xml.rpc.ServiceException;

	public org.storagebox.www.sbns.StorageBoxPortType getStorageBoxPort(
			java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}