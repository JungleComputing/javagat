/**
 * WriteOperation.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */
package org.storagebox.www.sbns;

public class WriteOperation implements java.io.Serializable {
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
        WriteOperation.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName(
            "http://www.storagebox.org/sbns", "WriteOperation"));

        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operation");
        elemField.setXmlName(new javax.xml.namespace.QName("", "operation"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("storagebox");
        elemField.setXmlName(new javax.xml.namespace.QName("", "storagebox"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("object_id");
        elemField.setXmlName(new javax.xml.namespace.QName("", "object_id"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attribute_name");
        elemField
            .setXmlName(new javax.xml.namespace.QName("", "attribute_name"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attribute_value");
        elemField.setXmlName(new javax.xml.namespace.QName("",
            "attribute_value"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
    }

    private java.lang.String operation;

    private java.lang.String storagebox;

    private java.lang.String object_id;

    private java.lang.String attribute_name;

    private java.lang.String attribute_value;

    private java.lang.Object __equalsCalc = null;

    private boolean __hashCodeCalc = false;

    public WriteOperation() {
    }

    public java.lang.String getOperation() {
        return operation;
    }

    public void setOperation(java.lang.String operation) {
        this.operation = operation;
    }

    public java.lang.String getStoragebox() {
        return storagebox;
    }

    public void setStoragebox(java.lang.String storagebox) {
        this.storagebox = storagebox;
    }

    public java.lang.String getObject_id() {
        return object_id;
    }

    public void setObject_id(java.lang.String object_id) {
        this.object_id = object_id;
    }

    public java.lang.String getAttribute_name() {
        return attribute_name;
    }

    public void setAttribute_name(java.lang.String attribute_name) {
        this.attribute_name = attribute_name;
    }

    public java.lang.String getAttribute_value() {
        return attribute_value;
    }

    public void setAttribute_value(java.lang.String attribute_value) {
        this.attribute_value = attribute_value;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WriteOperation)) {
            return false;
        }

        WriteOperation other = (WriteOperation) obj;

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
            && (((this.operation == null) && (other.getOperation() == null)) || ((this.operation != null) && this.operation
                .equals(other.getOperation())))
            && (((this.storagebox == null) && (other.getStoragebox() == null)) || ((this.storagebox != null) && this.storagebox
                .equals(other.getStoragebox())))
            && (((this.object_id == null) && (other.getObject_id() == null)) || ((this.object_id != null) && this.object_id
                .equals(other.getObject_id())))
            && (((this.attribute_name == null) && (other.getAttribute_name() == null)) || ((this.attribute_name != null) && this.attribute_name
                .equals(other.getAttribute_name())))
            && (((this.attribute_value == null) && (other.getAttribute_value() == null)) || ((this.attribute_value != null) && this.attribute_value
                .equals(other.getAttribute_value())));
        __equalsCalc = null;

        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }

        __hashCodeCalc = true;

        int _hashCode = 1;

        if (getOperation() != null) {
            _hashCode += getOperation().hashCode();
        }

        if (getStoragebox() != null) {
            _hashCode += getStoragebox().hashCode();
        }

        if (getObject_id() != null) {
            _hashCode += getObject_id().hashCode();
        }

        if (getAttribute_name() != null) {
            _hashCode += getAttribute_name().hashCode();
        }

        if (getAttribute_value() != null) {
            _hashCode += getAttribute_value().hashCode();
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
