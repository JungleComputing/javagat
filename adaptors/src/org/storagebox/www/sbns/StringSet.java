/**
 * StringSet.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */
package org.storagebox.www.sbns;

public class StringSet implements java.io.Serializable {
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
        StringSet.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName(
            "http://www.storagebox.org/sbns", "StringSet"));

        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("item");
        elemField.setXmlName(new javax.xml.namespace.QName("", "item"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
    }

    private java.lang.String[] item;

    private java.lang.Object __equalsCalc = null;

    private boolean __hashCodeCalc = false;

    public StringSet() {
    }

    public java.lang.String[] getItem() {
        return item;
    }

    public void setItem(java.lang.String[] item) {
        this.item = item;
    }

    public java.lang.String getItem(int i) {
        return item[i];
    }

    public void setItem(int i, java.lang.String value) {
        this.item[i] = value;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof StringSet)) {
            return false;
        }

        StringSet other = (StringSet) obj;

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
        _equals = true && (((this.item == null) && (other.getItem() == null)) || ((this.item != null) && java.util.Arrays
            .equals(this.item, other.getItem())));
        __equalsCalc = null;

        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }

        __hashCodeCalc = true;

        int _hashCode = 1;

        if (getItem() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getItem()); i++) {
                java.lang.Object obj = java.lang.reflect.Array
                    .get(getItem(), i);

                if ((obj != null) && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
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
