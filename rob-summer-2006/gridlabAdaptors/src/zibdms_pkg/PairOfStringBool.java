/**
 * PairOfStringBool.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */
package zibdms_pkg;

public class PairOfStringBool implements java.io.Serializable {
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
        PairOfStringBool.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:zibdms",
            "pair-of-string-bool"));

        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pathname");
        elemField.setXmlName(new javax.xml.namespace.QName("", "pathname"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("file");
        elemField.setXmlName(new javax.xml.namespace.QName("", "file"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(elemField);
    }

    private java.lang.String pathname;

    private boolean file;

    private java.lang.Object __equalsCalc = null;

    private boolean __hashCodeCalc = false;

    public PairOfStringBool() {
    }

    public java.lang.String getPathname() {
        return pathname;
    }

    public void setPathname(java.lang.String pathname) {
        this.pathname = pathname;
    }

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PairOfStringBool)) {
            return false;
        }

        PairOfStringBool other = (PairOfStringBool) obj;

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
            && (((this.pathname == null) && (other.getPathname() == null)) || ((this.pathname != null) && this.pathname
                .equals(other.getPathname()))) && (this.file == other.isFile());
        __equalsCalc = null;

        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }

        __hashCodeCalc = true;

        int _hashCode = 1;

        if (getPathname() != null) {
            _hashCode += getPathname().hashCode();
        }

        _hashCode += new Boolean(isFile()).hashCode();
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
