/**
 * Grms.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package grms_pkg;

public interface Grms extends java.rmi.Remote {

	// Definition of WebService interface for job submitting
	// GrmsResponse submitJob(String jobDefinition, String
	// jobId) Input Parameters: - String jobDefinition
	// - XML document describing job to be submitted Output
	// Parameters: - String JobId -> unique Job ID
	// Return Value: - GrmsResponse.errorCode
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse submitJob(java.lang.String jobDefinition,
			javax.xml.rpc.holders.StringHolder jobId)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for job migration
	// GrmsResponse migrateJob(String jobId, String jobDefinition)
	// Input Parameters: - String jobId - id
	// of job to be migrated - String jobDefinition - XML
	// document describing migration Output Parameters:
	// - Return Value: - GrmsResponse.errorCode
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse migrateJob(java.lang.String jobId,
			java.lang.String jobDefinition) throws java.rmi.RemoteException;

	// Definition of WebService interface for job migration
	// GrmsResponse migrateJob(String jobId)
	// Input Parameters: - String jobId - id of job to be
	// migrated Output Parameters: -
	// Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse migrateJob(java.lang.String jobId)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for job suspending
	// GrmsResponse suspendJob(String jobId, String jobDefinition)
	// Input Parameters: - String jobId - id
	// of job to be suspended - String jobDefinition - XML
	// document describing migration Output Parameters:
	// - Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse suspendJob(java.lang.String jobId,
			java.lang.String jobDefinition) throws java.rmi.RemoteException;

	// Definition of WebService interface for job suspending
	// int suspendJob(String jobId) Input Parameters:
	// - String jobId - id of job to be suspended
	// Output Parameters: - Return Value:
	// - GrmsResponse.errorCode: 0 -
	// success >0 - error code
	public grms_pkg.GrmsResponse suspendJob(java.lang.String jobId)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for job resuming
	// GrmsResponse resumeJob(String jobId, String jobDefinition)
	// Input Parameters: - String jobId - id
	// of job to be migrated - String jobDefinition - XML
	// document describing resumed Output Parameters:
	// - Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse resumeJob(java.lang.String jobId,
			java.lang.String jobDefinition) throws java.rmi.RemoteException;

	// Definition of WebService interface for job resumeing
	// GrmsResponse resumeJob(String jobId) Input
	// Parameters: - String jobId - id of job to be resumed
	// Output Parameters: - Return
	// Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	public grms_pkg.GrmsResponse resumeJob(java.lang.String jobId)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for obtaining list
	// of jobs belonging to the user GrmsResponse getJobsList(String[]
	// jobsList) Input Parameters: -
	// Output Parameters: - String[] - array of job
	// IDs belonging to the user Return Value:
	// - GrmsResponse.errorCode: 0 - success
	// >0 - error code
	public grms_pkg.GrmsResponse getJobsList(
			grms_pkg.holders.ArrayOf_xsd_stringHolder jobsList)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for obtaining list
	// of jobs in given state belonging to the user int getJobsList(enum
	// JobStatusType status, String[] jobsList) Input Parameters:
	// - enum JobStatusType status - status of the job
	// Output Parameters: - String[] - array of
	// job IDs belonging to the user Return Value:
	// - GrmsResponse.errorCode: 0 - success
	// >0 - error code
	public grms_pkg.GrmsResponse getJobsList(grms_pkg.JobStatusType status,
			grms_pkg.holders.ArrayOf_xsd_stringHolder jobsList)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for registering application
	// access GrmsResponse registerApplicationAccess(String
	// jobId, String service_location, int pid) Input Parameters:
	// - String jobId - job identifier -
	// String service_location - location of the application/service -
	// int pod - ProcessID Output Parameters:
	// - Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse registerApplicationAccess(
			java.lang.String jobId, java.lang.String service_location, int pid)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for testing the description
	// of the job GrmsResponse testJobDescription(String jobId)
	// Input Parameters: - String jobId - job
	// identifier Output Parameters: Return Value:
	// - GrmsResponse.errorCode: 0 -
	// success >0 - error code
	public grms_pkg.GrmsResponse testJobDescription(
			java.lang.String jobDescription) throws java.rmi.RemoteException;

	// Definition of WebService interface for getting the information
	// of job GrmsResponse getJobInfo(String jobId, jobInformation
	// jobInfo) Input Parameters: - String
	// jobId - job identifier Output Parameters:
	// - jobInformation jobInfo - information about the job jobInformation
	// { String userDn; enum JobStatusType jobStatus; Calendar
	// submitionTime; Calendar finishTime; String requestStatus;
	// int reqNumStatus; String errorDescription; jobHistory
	// history; } Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse getJobInfo(java.lang.String jobId,
			grms_pkg.holders.JobInformationHolder jobInfo)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for getting the history
	// of job GrmsResponse getJobHistory(String jobId, jobHistory[]
	// history) Input Parameters: - String
	// jobId - job identifier Output Parameters:
	// - jobHistory[] history - information about the history jobHistory
	// { String hostName; Calendar startTime; Calendar
	// localStartTime; Calendar localFinishTime; String jobDescription;
	// } Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse getJobHistory(java.lang.String jobId,
			grms_pkg.holders.ArrayOfjobHistoryHolder history)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for finding resources
	// GrmsResponse findResources( String resourceDefinition,
	// String[] resources) Input Parameters:
	// - String resourceDefinition - XML document describing resource
	// preferences
	// Output Parameters: - String[] resources
	// -> array of resources (resourceManagerContact) that match user
	// preferences
	// Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse findResources(
			java.lang.String resourceDefinition,
			grms_pkg.holders.ArrayOf_xsd_stringHolder resources)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for canceling a job
	// GrmsResponse cancelJob( String jobId)
	// Input Parameters: - String jobId - job identifier
	// Output Parameters: - Return
	// Value: - GrmsResponse: 0 - success
	// >0 - error code
	public grms_pkg.GrmsResponse cancelJob(java.lang.String jobId)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for getting the description
	// of the service String getServiceDescription( DescriptionType
	// description) Input Parameters: - DescriptionType
	// type - type of the description (enum SHORT, FULL) Output
	// Parameters: - Return Value:
	// - String - containing the description of the service
	// 
	public java.lang.String getServiceDescription(grms_pkg.DescriptionType type)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for obtaining list
	// of all jobs in given state GrmsResponse getJobsList(enum
	// JobStatusType status, String[] jobsList) Input Parameters:
	// - enum JobStatusType status Output Parameters:
	// - String[] - array of job IDs belonging to the user
	// Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse getAllJobsList(grms_pkg.JobStatusType status,
			grms_pkg.holders.ArrayOf_xsd_stringHolder jobsList)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for registering listner
	// for notifications GrmsResponse registerNotification(String
	// client, NotificationRequest request, String notificationId)
	// Input Parameters: - String client - client
	// identifier - NotificationRequest request: - String jobId -
	// job identifier - enum eventType = {STATUS, REQUEST} -
	// enum notificationType = {PROFILE, SMS, MAIL} - String listener
	// - url of service waiting for notifications - String[] users
	// - users whom the notification should be forwarded to
	// Output Parameters: - String notificationId - identifier
	// of the notification Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse registerNotification(java.lang.String client,
			grms_pkg.NotificationRequest request,
			javax.xml.rpc.holders.StringHolder notificationId)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for unregistering
	// listner for notifications GrmsResponse unregisterNotification(
	// String notificationId) Input Parameters: -
	// String notificationId - identifier of the notification Output
	// Parameters: - Return Value:
	// - GrmsResponse.errorCode: 0 - success
	// >0 - error code
	public grms_pkg.GrmsResponse unregisterNotification(
			java.lang.String notificationId) throws java.rmi.RemoteException;

	// Definition of WebService interface for registering new
	// output files/directories GrmsResponse AddOutFileDirs(
	// String jobId, FileDir[] items, OUT int errors, OUT int[] result)
	// Input Parameters: - String jobId - job identifier
	// - FileDir[] items - files/directories to be added FileDir:
	// - String name - name of the file - String path - path of the
	// file - enum UrlType urlType = {PHISICAL, LOGICAL} - enum PathType
	// pathType = {FILE, DIRECTORY} Output Parameters: - int
	// errors - number of errors in 'result' array - int[] result
	// - results of operation (for each item) Return Value:
	// - GrmsResponse.errorCode: 0 -
	// success >0 - error code
	public void addOutFileDirs(java.lang.String jobId,
			grms_pkg.FileDir[] items,
			grms_pkg.holders.ArrayOfGrmsResponseHolder result,
			grms_pkg.holders.GrmsResponseHolder addOutFileDirsReturn,
			javax.xml.rpc.holders.IntHolder errors)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for getting registered
	// output files/directories GrmsResponse getOutFileDirs(
	// String jobId, OUT FileDir[] items) Input Parameters:
	// Output Parameters: - String jobId - job identifier
	// - FileDir[] items - registered output files/directories
	// For definition of FileDir see AddOutFileDir operation
	// Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse getOutFileDirs(java.lang.String jobId,
			grms_pkg.holders.ArrayOfFileDirHolder items)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for unregistering
	// output files/directories GrmsResponse DeleteOutFileDirs(
	// String jobId, FileDir[] items, OUT int errors, OUT int[] result)
	// For definition of FileDir see AddOutFileDir operation.
	// Input Parameters: - String jobId - job identifier
	// - FileDir[] items - files/directories to be added Output
	// Parameters: - int errors - number of errors in 'result' array
	// - int[] result - results of operation (for each item)
	// Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public void deleteOutFileDirs(java.lang.String jobId,
			grms_pkg.FileDir[] items,
			grms_pkg.holders.ArrayOfGrmsResponseHolder result,
			grms_pkg.holders.GrmsResponseHolder deleteOutFileDirsReturn,
			javax.xml.rpc.holders.IntHolder errors)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for registering new
	// checkpoint files/directories GrmsResponse AddCheckpointFileDirs(
	// String jobId, FileDir[] items, OUT int errors, OUT int[] result)
	// Input Parameters: - String jobId - job identifier
	// - FileDir[] items - files/directories to be added For
	// definition of FileDir see the AddOutFIleDirs operation Output
	// Parameters: - int errors - number of errors in 'result' array
	// - int[] result - results of operation (for each item)
	// Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public void addCheckpointFileDirs(java.lang.String jobId,
			grms_pkg.FileDir[] items,
			grms_pkg.holders.ArrayOfGrmsResponseHolder result,
			grms_pkg.holders.GrmsResponseHolder addCheckpointFileDirsReturn,
			javax.xml.rpc.holders.IntHolder errors)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for getting registered
	// checkpoint files/directories GrmsResponse getCheckpointFileDirs(
	// String jobId, OUT FileDir[] items) Input Parameters:
	// Output Parameters: - String jobId - job identifier
	// - FileDir[] items - registered output files/directories
	// For definition of FileDir see AddOutFileDir operation
	// Return Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	// 
	public grms_pkg.GrmsResponse getCheckpointFileDirs(java.lang.String jobId,
			grms_pkg.holders.ArrayOfFileDirHolder items)
			throws java.rmi.RemoteException;

	// Definition of WebService interface for unregistering
	// checkpoint files/directories GrmsResponse DeleteCheckpointFileDirs(
	// String jobId, FileDir[] items, OUT int, OUT int[] result) For
	// definition of FileDir see AddOutFileDir operation. Input
	// Parameters: - String jobId - job identifier - FileDir[]
	// items - files/directories to be added Output Parameters:
	// - int errors - number of errors in 'result' array -
	// int[] result - results of operation (for each item) Return
	// Value: - GrmsResponse.errorCode:
	// 0 - success >0 - error code
	public void deleteCheckpointFileDirs(java.lang.String jobId,
			grms_pkg.FileDir[] items,
			grms_pkg.holders.ArrayOfGrmsResponseHolder result,
			grms_pkg.holders.GrmsResponseHolder deleteCheckpointFileDirsReturn,
			javax.xml.rpc.holders.IntHolder errors)
			throws java.rmi.RemoteException;
}