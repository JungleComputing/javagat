/**
 * StorageBoxPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */
package org.storagebox.www.sbns;

public interface StorageBoxPortType extends java.rmi.Remote {
    public void addAttribute(java.lang.String storagebox,
            java.lang.String object_id, java.lang.String attribute_name,
            java.lang.String attribute_value) throws java.rmi.RemoteException;

    public void setAttribute(java.lang.String storagebox,
            java.lang.String object_id, java.lang.String attribute_name,
            java.lang.String attribute_value) throws java.rmi.RemoteException;

    public void removeAttribute(java.lang.String storagebox,
            java.lang.String object_id, java.lang.String attribute_name,
            java.lang.String attribute_value) throws java.rmi.RemoteException;

    public java.lang.String createObject(java.lang.String storagebox,
            java.lang.String object_id) throws java.rmi.RemoteException;

    public void deleteObject(java.lang.String storagebox,
            java.lang.String object_id) throws java.rmi.RemoteException;

    public void executeWriteOperations(
            org.storagebox.www.sbns.WriteOperations write_operations)
            throws java.rmi.RemoteException;

    public org.storagebox.www.sbns.ObjectSet getAttributes(
            org.storagebox.www.sbns.StringSet storageboxes,
            org.storagebox.www.sbns.StringSet object_ids,
            org.storagebox.www.sbns.StringSet attribute_names)
            throws java.rmi.RemoteException;

    public org.storagebox.www.sbns.ObjectSet query(
            org.storagebox.www.sbns.StringSet storageboxes,
            java.lang.String query,
            org.storagebox.www.sbns.StringSet attribute_names)
            throws java.rmi.RemoteException;

    public void createAttribute(java.lang.String storagebox,
            java.lang.String attribute_name, java.lang.String type_name,
            boolean single_valued) throws java.rmi.RemoteException;

    public void deleteAttribute(java.lang.String storagebox,
            java.lang.String attibute_name) throws java.rmi.RemoteException;

    public org.storagebox.www.sbns.Schema getSchema(java.lang.String storagebox)
            throws java.rmi.RemoteException;

    public void setAttributeProperty(java.lang.String storagebox,
            java.lang.String attribute_name, java.lang.String property,
            java.lang.String value) throws java.rmi.RemoteException;

    public int findReplace(java.lang.String storagebox, java.lang.String query,
            org.storagebox.www.sbns.StorageBoxObject find,
            org.storagebox.www.sbns.StorageBoxObject replace_object)
            throws java.rmi.RemoteException;
}
