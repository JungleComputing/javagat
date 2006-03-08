/**
 * AttributeSchema.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */
package org.storagebox.www.sbns;

public class AttributeSchema implements java.io.Serializable {
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
        AttributeSchema.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName(
            "http://www.storagebox.org/sbns", "AttributeSchema"));

        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attribute_name");
        elemField
            .setXmlName(new javax.xml.namespace.QName("", "attribute_name"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("display_name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "display_name"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("value_type");
        elemField.setXmlName(new javax.xml.namespace.QName("", "value_type"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("single_valued");
        elemField
            .setXmlName(new javax.xml.namespace.QName("", "single_valued"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(elemField);
    }

    private java.lang.String attribute_name;

    private java.lang.String display_name;

    private java.lang.String value_type;

    private boolean single_valued;

    private java.lang.Object __equalsCalc = null;

    private boolean __hashCodeCalc = false;

    public AttributeSchema() {
    }

    public java.lang.String getAttribute_name() {
        return attribute_name;
    }

    public void setAttribute_name(java.lang.String attribute_name) {
        this.attribute_name = attribute_name;
    }

    public java.lang.String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(java.lang.String display_name) {
        this.display_name = display_name;
    }

    public java.lang.String getValue_type() {
        return value_type;
    }

    public void setValue_type(java.lang.String value_type) {
        this.value_type = value_type;
    }

    public boolean isSingle_valued() {
        return single_valued;
    }

    public void setSingle_valued(boolean single_valued) {
        this.single_valued = single_valued;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AttributeSchema)) {
            return false;
        }

        AttributeSchema other = (AttributeSchema) obj;

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
            && (((this.attribute_name == null) && (other.getAttribute_name() == null)) || ((this.attribute_name != null) && this.attribute_name
                .equals(other.getAttribute_name())))
            && (((this.display_name == null) && (other.getDisplay_name() == null)) || ((this.display_name != null) && this.display_name
                .equals(other.getDisplay_name())))
            && (((this.value_type == null) && (other.getValue_type() == null)) || ((this.value_type != null) && this.value_type
                .equals(other.getValue_type())))
            && (this.single_valued == other.isSingle_valued());
        __equalsCalc = null;

        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }

        __hashCodeCalc = true;

        int _hashCode = 1;

        if (getAttribute_name() != null) {
            _hashCode += getAttribute_name().hashCode();
        }

        if (getDisplay_name() != null) {
            _hashCode += getDisplay_name().hashCode();
        }

        if (getValue_type() != null) {
            _hashCode += getValue_type().hashCode();
        }

        _hashCode += new Boolean(isSingle_valued()).hashCode();
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
