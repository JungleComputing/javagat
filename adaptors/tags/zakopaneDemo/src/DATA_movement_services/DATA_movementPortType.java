/**
 * DATA_movementPortType.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package DATA_movement_services;

public interface DATA_movementPortType extends java.rmi.Remote {

	// Service definition of function ns__DATACopyFile
	public java.lang.String DATACopyFile(java.lang.String sourceURL,
			java.lang.String destURL, int maxRetries, int useParallel)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATACopyFileDefaults
	public java.lang.String DATACopyFileDefaults(java.lang.String sourceURL,
			java.lang.String destURL) throws java.rmi.RemoteException;

	// Service definition of function ns__DATAMoveFile
	public java.lang.String DATAMoveFile(java.lang.String sourceURL,
			java.lang.String destURL, int maxRetries, int useParallel)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAMoveFileDefaults
	public java.lang.String DATAMoveFileDefaults(java.lang.String sourceURL,
			java.lang.String destURL) throws java.rmi.RemoteException;

	// Service definition of function ns__DATADeleteFileDefaults
	public java.lang.String DATADeleteFileDefaults(java.lang.String inURL)
			throws java.rmi.RemoteException;

	// Service definition of function ns__isAlive
	public int isAlive(int dump) throws java.rmi.RemoteException;

	// Service definition of function ns__getServiceDescription
	public java.lang.String getServiceDescription()
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATATransferFile
	public java.lang.String DATATransferFile(int operation,
			java.lang.String sourceURL, java.lang.String destURL,
			int maxRetries, int useParallel) throws java.rmi.RemoteException;

	// Service definition of function ns__DATAinit_CopyFile
	public void DATAinitCopyFile(java.lang.String sourceURL,
			java.lang.String destURL, int maxRetries, int useParallel,
			javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAinit_MoveFile
	public void DATAinitMoveFile(java.lang.String sourceURL,
			java.lang.String destURL, int maxRetries, int useParallel,
			javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAinit_DeleteFile
	public void DATAinitDeleteFile(java.lang.String sourceURL,
			javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAinit_TransferFile
	public void DATAinitTransferFile(int operation, java.lang.String sourceURL,
			java.lang.String destURL, int maxRetries, int useParallel,
			javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATArestartFileTask
	public void DATArestartFileTask(javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAgetFileTask
	public void DATAgetFileTask(javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAwaitFileTask
	public void DATAwaitFileTask(javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAstopFileTask
	public void DATAstopFileTask(javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException;

	// Service definition of function ns__DATAdeleteFileTask
	public int DATAdeleteFileTask(long id) throws java.rmi.RemoteException;
}