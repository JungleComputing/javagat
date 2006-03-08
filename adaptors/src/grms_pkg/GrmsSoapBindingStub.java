/**
 * GrmsSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */
package grms_pkg;

public class GrmsSoapBindingStub extends org.apache.axis.client.Stub implements
        grms_pkg.Grms {
    static org.apache.axis.description.OperationDesc[] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[31];

        org.apache.axis.description.OperationDesc oper;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("findResources");
        oper.addParameter(new javax.xml.namespace.QName("",
            "resourceDefinition"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "resources"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOf_xsd_string"),
            java.lang.String[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "findResourcesReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("submitJob");
        oper.addParameter(new javax.xml.namespace.QName("", "jobDefinition"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("", "submitJobReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("migrateJob");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "jobDefinition"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "migrateJobReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("migrateJob");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "migrateJobReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("suspendJob");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "jobDefinition"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "suspendJobReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("suspendJob");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "suspendJobReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("resumeJob");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "jobDefinition"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("", "resumeJobReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("resumeJob");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("", "resumeJobReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getJobsList");
        oper.addParameter(new javax.xml.namespace.QName("", "jobsList"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOf_xsd_string"),
            java.lang.String[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getJobsListReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getJobsList");
        oper.addParameter(new javax.xml.namespace.QName("", "status"),
            new javax.xml.namespace.QName("urn:grms", "JobStatusType"),
            grms_pkg.JobStatusType.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "jobsList"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOf_xsd_string"),
            java.lang.String[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getJobsListReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[9] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getProjectJobsList");
        oper.addParameter(new javax.xml.namespace.QName("", "project"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "jobsList"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOf_xsd_string"),
            java.lang.String[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getProjectJobsListReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getProjectJobsList");
        oper.addParameter(new javax.xml.namespace.QName("", "project"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "status"),
            new javax.xml.namespace.QName("urn:grms", "JobStatusType"),
            grms_pkg.JobStatusType.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "jobsList"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOf_xsd_string"),
            java.lang.String[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getProjectJobsListReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("registerApplicationAccess");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("", "service_location"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "pid"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "registerApplicationAccessReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("unregisterApplicationAccess");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "unregisterApplicationAccessReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getApplicationAccess");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("", "service_location"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "pid"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getApplicationAccessReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("cancelJob");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("", "cancelJobReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getServiceDescription");
        oper.addParameter(new javax.xml.namespace.QName("", "type"),
            new javax.xml.namespace.QName("urn:grms", "DescriptionType"),
            grms_pkg.DescriptionType.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getServiceDescriptionReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[16] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getAllJobsList");
        oper.addParameter(new javax.xml.namespace.QName("", "status"),
            new javax.xml.namespace.QName("urn:grms", "JobStatusType"),
            grms_pkg.JobStatusType.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "jobsList"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOf_xsd_string"),
            java.lang.String[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getAllJobsListReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[17] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("testJobDescription");
        oper.addParameter(new javax.xml.namespace.QName("", "JobDescription"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "testJobDescriptionReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[18] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getJobInfo");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "jobInfo"),
            new javax.xml.namespace.QName("urn:grms", "jobInformation"),
            grms_pkg.JobInformation.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getJobInfoReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[19] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getJobHistory");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "history"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOfjobHistory"),
            grms_pkg.JobHistory[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getJobHistoryReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[20] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("registerNotification");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "request"),
            new javax.xml.namespace.QName("urn:grms", "NotificationRequest"),
            grms_pkg.NotificationRequest.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "notificationId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "registerNotificationReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[21] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getNotificationsList");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "notificationIds"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOf_xsd_string"),
            java.lang.String[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getNotificationsListReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[22] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("unregisterNotification");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "notificationId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "unregisterNotificationReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[23] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getNotificationInformation");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "notificationId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("",
            "notificationRequest"), new javax.xml.namespace.QName("urn:grms",
            "NotificationRequest"), grms_pkg.NotificationRequest.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getNotificationInformationReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[24] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addOutputFileDirs");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "items"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOfFileDir"),
            grms_pkg.FileDir[].class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "result"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOfGrmsResponse"),
            grms_pkg.GrmsResponse[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("",
            "addOutputFileDirsReturn"), new javax.xml.namespace.QName(
            "urn:grms", "GrmsResponse"), grms_pkg.GrmsResponse.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "errors"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[25] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getOutputFileDirs");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "items"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOfFileDir"),
            grms_pkg.FileDir[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getOutputFileDirsReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[26] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteOutputFileDirs");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "items"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOfFileDir"),
            grms_pkg.FileDir[].class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "result"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOfGrmsResponse"),
            grms_pkg.GrmsResponse[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("",
            "deleteOutputFileDirsReturn"), new javax.xml.namespace.QName(
            "urn:grms", "GrmsResponse"), grms_pkg.GrmsResponse.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "errors"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[27] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addCheckpointFileDirs");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "items"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOfFileDir"),
            grms_pkg.FileDir[].class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "result"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOfGrmsResponse"),
            grms_pkg.GrmsResponse[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("",
            "addCheckpointFileDirsReturn"), new javax.xml.namespace.QName(
            "urn:grms", "GrmsResponse"), grms_pkg.GrmsResponse.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "errors"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[28] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getCheckpointFileDirs");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "items"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOfFileDir"),
            grms_pkg.FileDir[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));
        oper.setReturnClass(grms_pkg.GrmsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("",
            "getCheckpointFileDirsReturn"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[29] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteCheckpointFileDirs");
        oper.addParameter(new javax.xml.namespace.QName("", "jobId"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "items"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOfFileDir"),
            grms_pkg.FileDir[].class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "result"),
            new javax.xml.namespace.QName("urn:grms", "ArrayOfGrmsResponse"),
            grms_pkg.GrmsResponse[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("",
            "deleteCheckpointFileDirsReturn"), new javax.xml.namespace.QName(
            "urn:grms", "GrmsResponse"), grms_pkg.GrmsResponse.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "errors"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[30] = oper;
    }

    private java.util.Vector cachedSerClasses = new java.util.Vector();

    private java.util.Vector cachedSerQNames = new java.util.Vector();

    private java.util.Vector cachedSerFactories = new java.util.Vector();

    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    public GrmsSoapBindingStub() throws org.apache.axis.AxisFault {
        this(null);
    }

    public GrmsSoapBindingStub(java.net.URL endpointURL,
            javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        this(service);
        super.cachedEndpoint = endpointURL;
    }

    public GrmsSoapBindingStub(javax.xml.rpc.Service service)
            throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }

        java.lang.Class cls;
        javax.xml.namespace.QName qName;
        java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
        java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
        java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
        java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
        java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
        java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
        java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
        java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
        qName = new javax.xml.namespace.QName("urn:grms", "ProtocolType");
        cachedSerQNames.add(qName);
        cls = grms_pkg.ProtocolType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("urn:grms", "ArrayOfFileDir");
        cachedSerQNames.add(qName);
        cls = grms_pkg.FileDir[].class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(arraysf);
        cachedDeserFactories.add(arraydf);

        qName = new javax.xml.namespace.QName("urn:grms", "ArrayOfGrmsResponse");
        cachedSerQNames.add(qName);
        cls = grms_pkg.GrmsResponse[].class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(arraysf);
        cachedDeserFactories.add(arraydf);

        qName = new javax.xml.namespace.QName("urn:grms", "jobHistory");
        cachedSerQNames.add(qName);
        cls = grms_pkg.JobHistory.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("urn:grms", "PathType");
        cachedSerQNames.add(qName);
        cls = grms_pkg.PathType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("urn:grms", "FileDir");
        cachedSerQNames.add(qName);
        cls = grms_pkg.FileDir.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("urn:grms", "jobInformation");
        cachedSerQNames.add(qName);
        cls = grms_pkg.JobInformation.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("urn:grms", "UrlType");
        cachedSerQNames.add(qName);
        cls = grms_pkg.UrlType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("urn:grms", "DescriptionType");
        cachedSerQNames.add(qName);
        cls = grms_pkg.DescriptionType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("urn:grms", "EventType");
        cachedSerQNames.add(qName);
        cls = grms_pkg.EventType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("urn:grms", "ArrayOf_xsd_string");
        cachedSerQNames.add(qName);
        cls = java.lang.String[].class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(arraysf);
        cachedDeserFactories.add(arraydf);

        qName = new javax.xml.namespace.QName("urn:grms", "GrmsResponse");
        cachedSerQNames.add(qName);
        cls = grms_pkg.GrmsResponse.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("urn:grms", "JobStatusType");
        cachedSerQNames.add(qName);
        cls = grms_pkg.JobStatusType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("urn:grms", "ArrayOfjobHistory");
        cachedSerQNames.add(qName);
        cls = grms_pkg.JobHistory[].class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(arraysf);
        cachedDeserFactories.add(arraydf);

        qName = new javax.xml.namespace.QName("urn:grms", "NotificationRequest");
        cachedSerQNames.add(qName);
        cls = grms_pkg.NotificationRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
    }

    private org.apache.axis.client.Call createCall()
            throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = (org.apache.axis.client.Call) super.service
                .createCall();

            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }

            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }

            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }

            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }

            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }

            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }

            java.util.Enumeration keys = super.cachedProperties.keys();

            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }

            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call
                        .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
                    _call
                        .setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);

                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses
                            .get(i);
                        javax.xml.namespace.QName qName = (javax.xml.namespace.QName) cachedSerQNames
                            .get(i);
                        java.lang.Class sf = (java.lang.Class) cachedSerFactories
                            .get(i);
                        java.lang.Class df = (java.lang.Class) cachedDeserFactories
                            .get(i);
                        _call.registerTypeMapping(cls, qName, sf, df, false);
                    }
                }
            }

            return _call;
        } catch (java.lang.Throwable t) {
            throw new org.apache.axis.AxisFault(
                "Failure trying to get the Call object", t);
        }
    }

    public grms_pkg.GrmsResponse findResources(
            java.lang.String resourceDefinition,
            grms_pkg.holders.ArrayOf_xsd_stringHolder resources)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "findResources"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { resourceDefinition });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                resources.value = (java.lang.String[]) _output
                    .get(new javax.xml.namespace.QName("", "resources"));
            } catch (java.lang.Exception _exception) {
                resources.value = (java.lang.String[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "resources")), java.lang.String[].class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse submitJob(java.lang.String jobDefinition,
            javax.xml.rpc.holders.StringHolder jobId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "submitJob"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { jobDefinition });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                jobId.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "jobId"));
            } catch (java.lang.Exception _exception) {
                jobId.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "jobId")), java.lang.String.class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse migrateJob(java.lang.String jobId,
            java.lang.String jobDefinition) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "migrateJob"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId,
            jobDefinition });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse migrateJob(java.lang.String jobId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "migrateJob"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse suspendJob(java.lang.String jobId,
            java.lang.String jobDefinition) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "suspendJob"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId,
            jobDefinition });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse suspendJob(java.lang.String jobId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "suspendJob"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse resumeJob(java.lang.String jobId,
            java.lang.String jobDefinition) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "resumeJob"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId,
            jobDefinition });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse resumeJob(java.lang.String jobId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "resumeJob"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse getJobsList(
            grms_pkg.holders.ArrayOf_xsd_stringHolder jobsList)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getJobsList"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                jobsList.value = (java.lang.String[]) _output
                    .get(new javax.xml.namespace.QName("", "jobsList"));
            } catch (java.lang.Exception _exception) {
                jobsList.value = (java.lang.String[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "jobsList")), java.lang.String[].class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse getJobsList(grms_pkg.JobStatusType status,
            grms_pkg.holders.ArrayOf_xsd_stringHolder jobsList)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getJobsList"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { status });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                jobsList.value = (java.lang.String[]) _output
                    .get(new javax.xml.namespace.QName("", "jobsList"));
            } catch (java.lang.Exception _exception) {
                jobsList.value = (java.lang.String[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "jobsList")), java.lang.String[].class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse getProjectJobsList(java.lang.String project,
            grms_pkg.holders.ArrayOf_xsd_stringHolder jobsList)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getProjectJobsList"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { project });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                jobsList.value = (java.lang.String[]) _output
                    .get(new javax.xml.namespace.QName("", "jobsList"));
            } catch (java.lang.Exception _exception) {
                jobsList.value = (java.lang.String[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "jobsList")), java.lang.String[].class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse getProjectJobsList(java.lang.String project,
            grms_pkg.JobStatusType status,
            grms_pkg.holders.ArrayOf_xsd_stringHolder jobsList)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getProjectJobsList"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { project,
            status });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                jobsList.value = (java.lang.String[]) _output
                    .get(new javax.xml.namespace.QName("", "jobsList"));
            } catch (java.lang.Exception _exception) {
                jobsList.value = (java.lang.String[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "jobsList")), java.lang.String[].class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse registerApplicationAccess(
            java.lang.String jobId, java.lang.String service_location, int pid)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "registerApplicationAccess"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId,
            service_location, new java.lang.Integer(pid) });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse unregisterApplicationAccess(
            java.lang.String jobId) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "unregisterApplicationAccess"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse getApplicationAccess(java.lang.String jobId,
            javax.xml.rpc.holders.StringHolder service_location,
            javax.xml.rpc.holders.IntHolder pid)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getApplicationAccess"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                service_location.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "service_location"));
            } catch (java.lang.Exception _exception) {
                service_location.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "service_location")), java.lang.String.class);
            }

            try {
                pid.value = ((java.lang.Integer) _output
                    .get(new javax.xml.namespace.QName("", "pid"))).intValue();
            } catch (java.lang.Exception _exception) {
                pid.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "pid")), int.class)).intValue();
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse cancelJob(java.lang.String jobId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "cancelJob"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public java.lang.String getServiceDescription(grms_pkg.DescriptionType type)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getServiceDescription"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { type });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_resp, java.lang.String.class);
            }
        }
    }

    public grms_pkg.GrmsResponse getAllJobsList(grms_pkg.JobStatusType status,
            grms_pkg.holders.ArrayOf_xsd_stringHolder jobsList)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[17]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getAllJobsList"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { status });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                jobsList.value = (java.lang.String[]) _output
                    .get(new javax.xml.namespace.QName("", "jobsList"));
            } catch (java.lang.Exception _exception) {
                jobsList.value = (java.lang.String[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "jobsList")), java.lang.String[].class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse testJobDescription(
            java.lang.String jobDescription) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[18]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "testJobDescription"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { jobDescription });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse getJobInfo(java.lang.String jobId,
            grms_pkg.holders.JobInformationHolder jobInfo)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[19]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getJobInfo"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                jobInfo.value = (grms_pkg.JobInformation) _output
                    .get(new javax.xml.namespace.QName("", "jobInfo"));
            } catch (java.lang.Exception _exception) {
                jobInfo.value = (grms_pkg.JobInformation) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "jobInfo")), grms_pkg.JobInformation.class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse getJobHistory(java.lang.String jobId,
            grms_pkg.holders.ArrayOfjobHistoryHolder history)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[20]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getJobHistory"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                history.value = (grms_pkg.JobHistory[]) _output
                    .get(new javax.xml.namespace.QName("", "history"));
            } catch (java.lang.Exception _exception) {
                history.value = (grms_pkg.JobHistory[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "history")), grms_pkg.JobHistory[].class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse registerNotification(java.lang.String jobId,
            grms_pkg.NotificationRequest request,
            javax.xml.rpc.holders.StringHolder notificationId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[21]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "registerNotification"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId,
            request });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                notificationId.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "notificationId"));
            } catch (java.lang.Exception _exception) {
                notificationId.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "notificationId")), java.lang.String.class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse getNotificationsList(java.lang.String jobId,
            grms_pkg.holders.ArrayOf_xsd_stringHolder notificationIds)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[22]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getNotificationsList"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                notificationIds.value = (java.lang.String[]) _output
                    .get(new javax.xml.namespace.QName("", "notificationIds"));
            } catch (java.lang.Exception _exception) {
                notificationIds.value = (java.lang.String[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "notificationIds")), java.lang.String[].class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse unregisterNotification(java.lang.String jobId,
            java.lang.String notificationId) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[23]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "unregisterNotification"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId,
            notificationId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public grms_pkg.GrmsResponse getNotificationInformation(
            java.lang.String jobId, java.lang.String notificationId,
            grms_pkg.holders.NotificationRequestHolder notificationRequest)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[24]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getNotificationInformation"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId,
            notificationId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                notificationRequest.value = (grms_pkg.NotificationRequest) _output
                    .get(new javax.xml.namespace.QName("",
                        "notificationRequest"));
            } catch (java.lang.Exception _exception) {
                notificationRequest.value = (grms_pkg.NotificationRequest) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "notificationRequest")),
                        grms_pkg.NotificationRequest.class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public void addOutputFileDirs(java.lang.String jobId,
            grms_pkg.FileDir[] items,
            grms_pkg.holders.ArrayOfGrmsResponseHolder result,
            grms_pkg.holders.GrmsResponseHolder addOutputFileDirsReturn,
            javax.xml.rpc.holders.IntHolder errors)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[25]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "addOutputFileDirs"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId,
            items });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                result.value = (grms_pkg.GrmsResponse[]) _output
                    .get(new javax.xml.namespace.QName("", "result"));
            } catch (java.lang.Exception _exception) {
                result.value = (grms_pkg.GrmsResponse[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "result")), grms_pkg.GrmsResponse[].class);
            }

            try {
                addOutputFileDirsReturn.value = (grms_pkg.GrmsResponse) _output
                    .get(new javax.xml.namespace.QName("",
                        "addOutputFileDirsReturn"));
            } catch (java.lang.Exception _exception) {
                addOutputFileDirsReturn.value = (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "addOutputFileDirsReturn")),
                        grms_pkg.GrmsResponse.class);
            }

            try {
                errors.value = ((java.lang.Integer) _output
                    .get(new javax.xml.namespace.QName("", "errors")))
                    .intValue();
            } catch (java.lang.Exception _exception) {
                errors.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "errors")), int.class)).intValue();
            }
        }
    }

    public grms_pkg.GrmsResponse getOutputFileDirs(java.lang.String jobId,
            grms_pkg.holders.ArrayOfFileDirHolder items)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[26]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getOutputFileDirs"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                items.value = (grms_pkg.FileDir[]) _output
                    .get(new javax.xml.namespace.QName("", "items"));
            } catch (java.lang.Exception _exception) {
                items.value = (grms_pkg.FileDir[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "items")), grms_pkg.FileDir[].class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public void deleteOutputFileDirs(java.lang.String jobId,
            grms_pkg.FileDir[] items,
            grms_pkg.holders.ArrayOfGrmsResponseHolder result,
            grms_pkg.holders.GrmsResponseHolder deleteOutputFileDirsReturn,
            javax.xml.rpc.holders.IntHolder errors)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[27]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "deleteOutputFileDirs"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId,
            items });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                result.value = (grms_pkg.GrmsResponse[]) _output
                    .get(new javax.xml.namespace.QName("", "result"));
            } catch (java.lang.Exception _exception) {
                result.value = (grms_pkg.GrmsResponse[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "result")), grms_pkg.GrmsResponse[].class);
            }

            try {
                deleteOutputFileDirsReturn.value = (grms_pkg.GrmsResponse) _output
                    .get(new javax.xml.namespace.QName("",
                        "deleteOutputFileDirsReturn"));
            } catch (java.lang.Exception _exception) {
                deleteOutputFileDirsReturn.value = (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "deleteOutputFileDirsReturn")),
                        grms_pkg.GrmsResponse.class);
            }

            try {
                errors.value = ((java.lang.Integer) _output
                    .get(new javax.xml.namespace.QName("", "errors")))
                    .intValue();
            } catch (java.lang.Exception _exception) {
                errors.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "errors")), int.class)).intValue();
            }
        }
    }

    public void addCheckpointFileDirs(java.lang.String jobId,
            grms_pkg.FileDir[] items,
            grms_pkg.holders.ArrayOfGrmsResponseHolder result,
            grms_pkg.holders.GrmsResponseHolder addCheckpointFileDirsReturn,
            javax.xml.rpc.holders.IntHolder errors)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[28]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "addCheckpointFileDirs"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId,
            items });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                result.value = (grms_pkg.GrmsResponse[]) _output
                    .get(new javax.xml.namespace.QName("", "result"));
            } catch (java.lang.Exception _exception) {
                result.value = (grms_pkg.GrmsResponse[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "result")), grms_pkg.GrmsResponse[].class);
            }

            try {
                addCheckpointFileDirsReturn.value = (grms_pkg.GrmsResponse) _output
                    .get(new javax.xml.namespace.QName("",
                        "addCheckpointFileDirsReturn"));
            } catch (java.lang.Exception _exception) {
                addCheckpointFileDirsReturn.value = (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "addCheckpointFileDirsReturn")),
                        grms_pkg.GrmsResponse.class);
            }

            try {
                errors.value = ((java.lang.Integer) _output
                    .get(new javax.xml.namespace.QName("", "errors")))
                    .intValue();
            } catch (java.lang.Exception _exception) {
                errors.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "errors")), int.class)).intValue();
            }
        }
    }

    public grms_pkg.GrmsResponse getCheckpointFileDirs(java.lang.String jobId,
            grms_pkg.holders.ArrayOfFileDirHolder items)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[29]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "getCheckpointFileDirs"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                items.value = (grms_pkg.FileDir[]) _output
                    .get(new javax.xml.namespace.QName("", "items"));
            } catch (java.lang.Exception _exception) {
                items.value = (grms_pkg.FileDir[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "items")), grms_pkg.FileDir[].class);
            }

            try {
                return (grms_pkg.GrmsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_resp, grms_pkg.GrmsResponse.class);
            }
        }
    }

    public void deleteCheckpointFileDirs(java.lang.String jobId,
            grms_pkg.FileDir[] items,
            grms_pkg.holders.ArrayOfGrmsResponseHolder result,
            grms_pkg.holders.GrmsResponseHolder deleteCheckpointFileDirsReturn,
            javax.xml.rpc.holders.IntHolder errors)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[30]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:grms",
            "deleteCheckpointFileDirs"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { jobId,
            items });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                result.value = (grms_pkg.GrmsResponse[]) _output
                    .get(new javax.xml.namespace.QName("", "result"));
            } catch (java.lang.Exception _exception) {
                result.value = (grms_pkg.GrmsResponse[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "result")), grms_pkg.GrmsResponse[].class);
            }

            try {
                deleteCheckpointFileDirsReturn.value = (grms_pkg.GrmsResponse) _output
                    .get(new javax.xml.namespace.QName("",
                        "deleteCheckpointFileDirsReturn"));
            } catch (java.lang.Exception _exception) {
                deleteCheckpointFileDirsReturn.value = (grms_pkg.GrmsResponse) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "deleteCheckpointFileDirsReturn")),
                        grms_pkg.GrmsResponse.class);
            }

            try {
                errors.value = ((java.lang.Integer) _output
                    .get(new javax.xml.namespace.QName("", "errors")))
                    .intValue();
            } catch (java.lang.Exception _exception) {
                errors.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "errors")), int.class)).intValue();
            }
        }
    }
}
