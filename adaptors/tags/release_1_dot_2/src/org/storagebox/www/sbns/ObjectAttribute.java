/**
 * ObjectAttribute.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.storagebox.www.sbns;

public class ObjectAttribute implements java.io.Serializable {
    private java.lang.String attribute_name;

    private java.lang.String attribute_value;

    private java.lang.String source_namespace;

    public ObjectAttribute() {
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

    public java.lang.String getSource_namespace() {
        return source_namespace;
    }

    public void setSource_namespace(java.lang.String source_namespace) {
        this.source_namespace = source_namespace;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ObjectAttribute)) return false;
        ObjectAttribute other = (ObjectAttribute) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
            && ((this.attribute_name == null && other.getAttribute_name() == null) || (this.attribute_name != null && this.attribute_name
                .equals(other.getAttribute_name())))
            && ((this.attribute_value == null && other.getAttribute_value() == null) || (this.attribute_value != null && this.attribute_value
                .equals(other.getAttribute_value())))
            && ((this.source_namespace == null && other.getSource_namespace() == null) || (this.source_namespace != null && this.source_namespace
                .equals(other.getSource_namespace())));
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
        if (getAttribute_name() != null) {
            _hashCode += getAttribute_name().hashCode();
        }
        if (getAttribute_value() != null) {
            _hashCode += getAttribute_value().hashCode();
        }
        if (getSource_namespace() != null) {
            _hashCode += getSource_namespace().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
        ObjectAttribute.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName(
            "http://www.storagebox.org/sbns", "ObjectAttribute"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
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
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("source_namespace");
        elemField.setXmlName(new javax.xml.namespace.QName("",
            "source_namespace"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
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
