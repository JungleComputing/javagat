/**
 * JobHistory.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package grms_pkg;

public class JobHistory  implements java.io.Serializable {
    private java.lang.String hostName;
    private java.util.Calendar startTime;
    private java.util.Calendar localSubmissionTime;
    private java.util.Calendar localStartTime;
    private java.util.Calendar localFinishTime;
    private java.lang.String jobDescription;
    private java.lang.String applicationAccess;

    public JobHistory() {
    }

    public java.lang.String getHostName() {
        return hostName;
    }

    public void setHostName(java.lang.String hostName) {
        this.hostName = hostName;
    }

    public java.util.Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(java.util.Calendar startTime) {
        this.startTime = startTime;
    }

    public java.util.Calendar getLocalSubmissionTime() {
        return localSubmissionTime;
    }

    public void setLocalSubmissionTime(java.util.Calendar localSubmissionTime) {
        this.localSubmissionTime = localSubmissionTime;
    }

    public java.util.Calendar getLocalStartTime() {
        return localStartTime;
    }

    public void setLocalStartTime(java.util.Calendar localStartTime) {
        this.localStartTime = localStartTime;
    }

    public java.util.Calendar getLocalFinishTime() {
        return localFinishTime;
    }

    public void setLocalFinishTime(java.util.Calendar localFinishTime) {
        this.localFinishTime = localFinishTime;
    }

    public java.lang.String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(java.lang.String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public java.lang.String getApplicationAccess() {
        return applicationAccess;
    }

    public void setApplicationAccess(java.lang.String applicationAccess) {
        this.applicationAccess = applicationAccess;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof JobHistory)) return false;
        JobHistory other = (JobHistory) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.hostName==null && other.getHostName()==null) || 
             (this.hostName!=null &&
              this.hostName.equals(other.getHostName()))) &&
            ((this.startTime==null && other.getStartTime()==null) || 
             (this.startTime!=null &&
              this.startTime.equals(other.getStartTime()))) &&
            ((this.localSubmissionTime==null && other.getLocalSubmissionTime()==null) || 
             (this.localSubmissionTime!=null &&
              this.localSubmissionTime.equals(other.getLocalSubmissionTime()))) &&
            ((this.localStartTime==null && other.getLocalStartTime()==null) || 
             (this.localStartTime!=null &&
              this.localStartTime.equals(other.getLocalStartTime()))) &&
            ((this.localFinishTime==null && other.getLocalFinishTime()==null) || 
             (this.localFinishTime!=null &&
              this.localFinishTime.equals(other.getLocalFinishTime()))) &&
            ((this.jobDescription==null && other.getJobDescription()==null) || 
             (this.jobDescription!=null &&
              this.jobDescription.equals(other.getJobDescription()))) &&
            ((this.applicationAccess==null && other.getApplicationAccess()==null) || 
             (this.applicationAccess!=null &&
              this.applicationAccess.equals(other.getApplicationAccess())));
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
        if (getHostName() != null) {
            _hashCode += getHostName().hashCode();
        }
        if (getStartTime() != null) {
            _hashCode += getStartTime().hashCode();
        }
        if (getLocalSubmissionTime() != null) {
            _hashCode += getLocalSubmissionTime().hashCode();
        }
        if (getLocalStartTime() != null) {
            _hashCode += getLocalStartTime().hashCode();
        }
        if (getLocalFinishTime() != null) {
            _hashCode += getLocalFinishTime().hashCode();
        }
        if (getJobDescription() != null) {
            _hashCode += getJobDescription().hashCode();
        }
        if (getApplicationAccess() != null) {
            _hashCode += getApplicationAccess().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(JobHistory.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:grms", "jobHistory"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "hostName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("startTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "startTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("localSubmissionTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "localSubmissionTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("localStartTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "localStartTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("localFinishTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "localFinishTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("", "jobDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("applicationAccess");
        elemField.setXmlName(new javax.xml.namespace.QName("", "applicationAccess"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
