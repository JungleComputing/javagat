/**
 * GrmsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */
package grms_pkg;

public class GrmsResponse implements java.io.Serializable {
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
        GrmsResponse.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:grms",
            "GrmsResponse"));

        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "errorCode"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("", "errorMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
    }

    private int errorCode;

    private java.lang.String errorMessage;

    private java.lang.Object __equalsCalc = null;

    private boolean __hashCodeCalc = false;

    public GrmsResponse() {
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public java.lang.String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(java.lang.String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GrmsResponse)) {
            return false;
        }

        GrmsResponse other = (GrmsResponse) obj;

        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }

        __equalsCalc = obj;

        boolean _equals;
        _equals = true
            && (this.errorCode == other.getErrorCode())
            && (((this.errorMessage == null) && (other.getErrorMessage() == null)) || ((this.errorMessage != null) && this.errorMessage
                .equals(other.getErrorMessage())));
        __equalsCalc = null;

        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }

        __hashCodeCalc = true;

        int _hashCode = 1;
        _hashCode += getErrorCode();

        if (getErrorMessage() != null) {
            _hashCode += getErrorMessage().hashCode();
        }

        __hashCodeCalc = false;

        return _hashCode;
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
