/**
 * StorageBoxPortType.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package org.storagebox.www.sbns;

public interface StorageBoxPortType extends java.rmi.Remote {
	public void addAttribute(java.lang.String namespace_name,
			java.lang.String global_oid, java.lang.String attribute_name,
			java.lang.String attribute_value) throws java.rmi.RemoteException;

	public void setAttribute(java.lang.String namespace_name,
			java.lang.String global_oid, java.lang.String attribute_name,
			java.lang.String attribute_value) throws java.rmi.RemoteException;

	public void removeAttribute(java.lang.String namespace_name,
			java.lang.String global_oid, java.lang.String attribute_name,
			java.lang.String attribute_value) throws java.rmi.RemoteException;

	public java.lang.String createObject(java.lang.String namespace_name,
			java.lang.String global_oid) throws java.rmi.RemoteException;

	public void deleteObject(java.lang.String namespace_name,
			java.lang.String global_oid) throws java.rmi.RemoteException;

	public org.storagebox.www.sbns.AttributeSet getAttributes(
			org.storagebox.www.sbns.StringSet namespaces,
			org.storagebox.www.sbns.StringSet objects,
			org.storagebox.www.sbns.StringSet attributes_names)
			throws java.rmi.RemoteException;

	public org.storagebox.www.sbns.AttributeSet query(
			org.storagebox.www.sbns.StringSet namespaces,
			java.lang.String query,
			org.storagebox.www.sbns.StringSet attributes_names)
			throws java.rmi.RemoteException;

	public void createAttribute(java.lang.String namespace_name,
			java.lang.String attribute_name, java.lang.String type_name,
			boolean single_valued) throws java.rmi.RemoteException;

	public org.storagebox.www.sbns.DatabaseSchema getSchema(
			java.lang.String namespace_name) throws java.rmi.RemoteException;
}