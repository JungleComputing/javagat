/**
 * FileDir.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package grms_pkg;

public class FileDir implements java.io.Serializable {
    private java.lang.String name;

    private java.lang.String path;

    private grms_pkg.UrlType urltype;

    private grms_pkg.PathType pathtype;

    public FileDir() {
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public java.lang.String getPath() {
        return path;
    }

    public void setPath(java.lang.String path) {
        this.path = path;
    }

    public grms_pkg.UrlType getUrltype() {
        return urltype;
    }

    public void setUrltype(grms_pkg.UrlType urltype) {
        this.urltype = urltype;
    }

    public grms_pkg.PathType getPathtype() {
        return pathtype;
    }

    public void setPathtype(grms_pkg.PathType pathtype) {
        this.pathtype = pathtype;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FileDir)) return false;
        FileDir other = (FileDir) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
            && ((this.name == null && other.getName() == null) || (this.name != null && this.name
                .equals(other.getName())))
            && ((this.path == null && other.getPath() == null) || (this.path != null && this.path
                .equals(other.getPath())))
            && ((this.urltype == null && other.getUrltype() == null) || (this.urltype != null && this.urltype
                .equals(other.getUrltype())))
            && ((this.pathtype == null && other.getPathtype() == null) || (this.pathtype != null && this.pathtype
                .equals(other.getPathtype())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getPath() != null) {
            _hashCode += getPath().hashCode();
        }
        if (getUrltype() != null) {
            _hashCode += getUrltype().hashCode();
        }
        if (getPathtype() != null) {
            _hashCode += getPathtype().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
        FileDir.class);

    static {
        typeDesc
            .setXmlType(new javax.xml.namespace.QName("urn:grms", "FileDir"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("path");
        elemField.setXmlName(new javax.xml.namespace.QName("", "path"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("urltype");
        elemField.setXmlName(new javax.xml.namespace.QName("", "urltype"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:grms",
            "UrlType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pathtype");
        elemField.setXmlName(new javax.xml.namespace.QName("", "pathtype"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:grms",
            "PathType"));
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
