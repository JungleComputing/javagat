/**
 * DATA_movementPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package DATA_movement_services;

public interface DATA_movementPortType extends java.rmi.Remote {

    // Service definition of function GridLabFileNS1__DATACopyFile
    public java.lang.String DATACopyFile(java.lang.String sourceURL, java.lang.String destURL, int useParallel, int noOverwrite, int maintainPermissions) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATACopyFileDefaults
    public java.lang.String DATACopyFileDefaults(java.lang.String sourceURL, java.lang.String destURL) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAMoveFile
    public java.lang.String DATAMoveFile(java.lang.String sourceURL, java.lang.String destURL, int useParallel, int noOverwrite, int maintainPermissions) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAMoveFileDefaults
    public java.lang.String DATAMoveFileDefaults(java.lang.String sourceURL, java.lang.String destURL) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATADeleteFileDefaults
    public java.lang.String DATADeleteFileDefaults(java.lang.String inURL) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__isAlive
    public int isAlive(int dump) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__getServiceDescription
    public java.lang.String getServiceDescription() throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATATransferFile
    public java.lang.String DATATransferFile(int operation, java.lang.String sourceURL, java.lang.String destURL, int useParallel, int noOverwrite, int maintainPermissions) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAinit_CopyFile
    public void DATAinitCopyFile(java.lang.String sourceURL, java.lang.String destURL, int useParallel, int noOverwrite, int maintainPermissions, javax.xml.rpc.holders.LongHolder id, javax.xml.rpc.holders.IntHolder type, javax.xml.rpc.holders.IntHolder status, javax.xml.rpc.holders.DoubleHolder progressPercentage, javax.xml.rpc.holders.IntHolder errorCode, javax.xml.rpc.holders.StringHolder errorString, javax.xml.rpc.holders.StringHolder userDN) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAinit_MoveFile
    public void DATAinitMoveFile(java.lang.String sourceURL, java.lang.String destURL, int useParallel, int noOverwrite, int maintainPermissions, javax.xml.rpc.holders.LongHolder id, javax.xml.rpc.holders.IntHolder type, javax.xml.rpc.holders.IntHolder status, javax.xml.rpc.holders.DoubleHolder progressPercentage, javax.xml.rpc.holders.IntHolder errorCode, javax.xml.rpc.holders.StringHolder errorString, javax.xml.rpc.holders.StringHolder userDN) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAinit_DeleteFile
    public void DATAinitDeleteFile(java.lang.String sourceURL, javax.xml.rpc.holders.LongHolder id, javax.xml.rpc.holders.IntHolder type, javax.xml.rpc.holders.IntHolder status, javax.xml.rpc.holders.DoubleHolder progressPercentage, javax.xml.rpc.holders.IntHolder errorCode, javax.xml.rpc.holders.StringHolder errorString, javax.xml.rpc.holders.StringHolder userDN) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAinit_TransferFile
    public void DATAinitTransferFile(int operation, java.lang.String sourceURL, java.lang.String destURL, int useParallel, int noOverwrite, int maintainPermissions, javax.xml.rpc.holders.LongHolder id, javax.xml.rpc.holders.IntHolder type, javax.xml.rpc.holders.IntHolder status, javax.xml.rpc.holders.DoubleHolder progressPercentage, javax.xml.rpc.holders.IntHolder errorCode, javax.xml.rpc.holders.StringHolder errorString, javax.xml.rpc.holders.StringHolder userDN) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATArestartFileTask
    public void DATArestartFileTask(javax.xml.rpc.holders.LongHolder id, javax.xml.rpc.holders.IntHolder type, javax.xml.rpc.holders.IntHolder status, javax.xml.rpc.holders.DoubleHolder progressPercentage, javax.xml.rpc.holders.IntHolder errorCode, javax.xml.rpc.holders.StringHolder errorString, javax.xml.rpc.holders.StringHolder userDN) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAgetFileTask
    public void DATAgetFileTask(javax.xml.rpc.holders.LongHolder id, javax.xml.rpc.holders.IntHolder type, javax.xml.rpc.holders.IntHolder status, javax.xml.rpc.holders.DoubleHolder progressPercentage, javax.xml.rpc.holders.IntHolder errorCode, javax.xml.rpc.holders.StringHolder errorString, javax.xml.rpc.holders.StringHolder userDN) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAwaitFileTask
    public void DATAwaitFileTask(javax.xml.rpc.holders.LongHolder id, javax.xml.rpc.holders.IntHolder type, javax.xml.rpc.holders.IntHolder status, javax.xml.rpc.holders.DoubleHolder progressPercentage, javax.xml.rpc.holders.IntHolder errorCode, javax.xml.rpc.holders.StringHolder errorString, javax.xml.rpc.holders.StringHolder userDN) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAstopFileTask
    public void DATAstopFileTask(javax.xml.rpc.holders.LongHolder id, javax.xml.rpc.holders.IntHolder type, javax.xml.rpc.holders.IntHolder status, javax.xml.rpc.holders.DoubleHolder progressPercentage, javax.xml.rpc.holders.IntHolder errorCode, javax.xml.rpc.holders.StringHolder errorString, javax.xml.rpc.holders.StringHolder userDN) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAdeleteFileTask
    public int DATAdeleteFileTask(long id) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAmultipleCopy
    public java.lang.String DATAmultipleCopy(DATA_movement_services.StringVector inURLs) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAStopCache
    public java.lang.String DATAStopCache() throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAConnectedMkdir
    public java.lang.String DATAConnectedMkdir(java.lang.String inURL) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_Mkdir
    public java.lang.String DATAFTPMkdir(java.lang.String inURL, int cached) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAConnectedModTime
    public void DATAConnectedModTime(java.lang.String inURL, javax.xml.rpc.holders.LongHolder seconds, javax.xml.rpc.holders.StringHolder response) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_ModTime
    public void DATAFTPModTime(java.lang.String inURL, int cached, javax.xml.rpc.holders.LongHolder seconds, javax.xml.rpc.holders.StringHolder response) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAConnectedSize
    public void DATAConnectedSize(java.lang.String inURL, org.apache.axis.holders.UnsignedLongHolder size, javax.xml.rpc.holders.StringHolder response) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_Size
    public void DATAFTPSize(java.lang.String inURL, int cached, org.apache.axis.holders.UnsignedLongHolder size, javax.xml.rpc.holders.StringHolder response) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATAConnectedList
    public void DATAConnectedList(java.lang.String inURL, int verbose, javax.xml.rpc.holders.StringHolder retlist, javax.xml.rpc.holders.StringHolder response) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_SimpleList
    public void DATAFTPSimpleList(java.lang.String inURL, int cached, javax.xml.rpc.holders.StringHolder retlist, javax.xml.rpc.holders.StringHolder response) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_List
    public void DATAFTPList(java.lang.String inURL, int verbose, int cached, javax.xml.rpc.holders.StringHolder retlist, javax.xml.rpc.holders.StringHolder response) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_VerboseList
    public void DATAFTPVerboseList(java.lang.String inURL, int cached, javax.xml.rpc.holders.StringHolder retlist, javax.xml.rpc.holders.StringHolder response) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_MachineListDirectory
    public void DATAFTPMachineListDirectory(java.lang.String inURL, int cached, javax.xml.rpc.holders.StringHolder retlist, javax.xml.rpc.holders.StringHolder response) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_MachineListFile
    public void DATAFTPMachineListFile(java.lang.String inURL, int cached, javax.xml.rpc.holders.StringHolder retlist, javax.xml.rpc.holders.StringHolder response) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_StructuredMachineListDirectory
    public void DATAFTPStructuredMachineListDirectory(java.lang.String inURL, int cached, DATA_movement_services.holders.ArrayOfDirectoryEntryHolder entries, javax.xml.rpc.holders.StringHolder response) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_StructuredMachineListFile
    public void DATAFTPStructuredMachineListFile(java.lang.String inURL, int cached, DATA_movement_services.holders.DirectoryEntryHolder file, javax.xml.rpc.holders.StringHolder response) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_Delete
    public java.lang.String DATAFTPDelete(java.lang.String inURL, int cached) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_Exists
    public java.lang.String DATAFTPExists(java.lang.String inURL, int cached) throws java.rmi.RemoteException;

    // Service definition of function GridLabFileNS1__DATA_FTP_Chmod
    public java.lang.String DATAFTPChmod(java.lang.String inURL, int mode, int cached) throws java.rmi.RemoteException;
}
