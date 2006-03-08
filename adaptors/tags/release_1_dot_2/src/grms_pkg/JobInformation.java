/**
 * JobInformation.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package grms_pkg;

public class JobInformation implements java.io.Serializable {
    private java.lang.String project;

    private java.lang.String userDn;

    private grms_pkg.JobStatusType jobStatus;

    private java.util.Calendar submissionTime;

    private java.util.Calendar finishTime;

    private java.lang.String requestStatus;

    private int reqNumStatus;

    private java.lang.String errorDescription;

    private grms_pkg.JobHistory lastHistory;

    private int historyLength;

    public JobInformation() {
    }

    public java.lang.String getProject() {
        return project;
    }

    public void setProject(java.lang.String project) {
        this.project = project;
    }

    public java.lang.String getUserDn() {
        return userDn;
    }

    public void setUserDn(java.lang.String userDn) {
        this.userDn = userDn;
    }

    public grms_pkg.JobStatusType getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(grms_pkg.JobStatusType jobStatus) {
        this.jobStatus = jobStatus;
    }

    public java.util.Calendar getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(java.util.Calendar submissionTime) {
        this.submissionTime = submissionTime;
    }

    public java.util.Calendar getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(java.util.Calendar finishTime) {
        this.finishTime = finishTime;
    }

    public java.lang.String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(java.lang.String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public int getReqNumStatus() {
        return reqNumStatus;
    }

    public void setReqNumStatus(int reqNumStatus) {
        this.reqNumStatus = reqNumStatus;
    }

    public java.lang.String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(java.lang.String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public grms_pkg.JobHistory getLastHistory() {
        return lastHistory;
    }

    public void setLastHistory(grms_pkg.JobHistory lastHistory) {
        this.lastHistory = lastHistory;
    }

    public int getHistoryLength() {
        return historyLength;
    }

    public void setHistoryLength(int historyLength) {
        this.historyLength = historyLength;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof JobInformation)) return false;
        JobInformation other = (JobInformation) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
            && ((this.project == null && other.getProject() == null) || (this.project != null && this.project
                .equals(other.getProject())))
            && ((this.userDn == null && other.getUserDn() == null) || (this.userDn != null && this.userDn
                .equals(other.getUserDn())))
            && ((this.jobStatus == null && other.getJobStatus() == null) || (this.jobStatus != null && this.jobStatus
                .equals(other.getJobStatus())))
            && ((this.submissionTime == null && other.getSubmissionTime() == null) || (this.submissionTime != null && this.submissionTime
                .equals(other.getSubmissionTime())))
            && ((this.finishTime == null && other.getFinishTime() == null) || (this.finishTime != null && this.finishTime
                .equals(other.getFinishTime())))
            && ((this.requestStatus == null && other.getRequestStatus() == null) || (this.requestStatus != null && this.requestStatus
                .equals(other.getRequestStatus())))
            && this.reqNumStatus == other.getReqNumStatus()
            && ((this.errorDescription == null && other.getErrorDescription() == null) || (this.errorDescription != null && this.errorDescription
                .equals(other.getErrorDescription())))
            && ((this.lastHistory == null && other.getLastHistory() == null) || (this.lastHistory != null && this.lastHistory
                .equals(other.getLastHistory())))
            && this.historyLength == other.getHistoryLength();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getProject() != null) {
            _hashCode += getProject().hashCode();
        }
        if (getUserDn() != null) {
            _hashCode += getUserDn().hashCode();
        }
        if (getJobStatus() != null) {
            _hashCode += getJobStatus().hashCode();
        }
        if (getSubmissionTime() != null) {
            _hashCode += getSubmissionTime().hashCode();
        }
        if (getFinishTime() != null) {
            _hashCode += getFinishTime().hashCode();
        }
        if (getRequestStatus() != null) {
            _hashCode += getRequestStatus().hashCode();
        }
        _hashCode += getReqNumStatus();
        if (getErrorDescription() != null) {
            _hashCode += getErrorDescription().hashCode();
        }
        if (getLastHistory() != null) {
            _hashCode += getLastHistory().hashCode();
        }
        _hashCode += getHistoryLength();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
        JobInformation.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:grms",
            "jobInformation"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("project");
        elemField.setXmlName(new javax.xml.namespace.QName("", "project"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userDn");
        elemField.setXmlName(new javax.xml.namespace.QName("", "userDn"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "jobStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:grms",
            "JobStatusType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("submissionTime");
        elemField
            .setXmlName(new javax.xml.namespace.QName("", "submissionTime"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("finishTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "finishTime"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requestStatus");
        elemField
            .setXmlName(new javax.xml.namespace.QName("", "requestStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reqNumStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "reqNumStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("",
            "errorDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastHistory");
        elemField.setXmlName(new javax.xml.namespace.QName("", "lastHistory"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:grms",
            "jobHistory"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("historyLength");
        elemField
            .setXmlName(new javax.xml.namespace.QName("", "historyLength"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
        java.lang.String mechType, java.lang.Class _javaType,
        javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanSerializer(_javaType,
            _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
        java.lang.String mechType, java.lang.Class _javaType,
        javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType,
            _xmlType, typeDesc);
    }

}
