/**
 * DirectoryEntry.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */
package DATA_movement_services;

public class DirectoryEntry implements java.io.Serializable {
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
        DirectoryEntry.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName(
            "urn:DATA_movement_services", "Directory-entry"));

        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("charset");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Charset"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("perm");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Perm"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("modify");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Modify"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("size");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Size"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "long"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "mode"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Type"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
    }

    private java.lang.String name;

    private java.lang.String charset;

    private java.lang.String perm;

    private java.lang.String modify;

    private long size;

    private int mode;

    private java.lang.String type;

    private java.lang.Object __equalsCalc = null;

    private boolean __hashCodeCalc = false;

    public DirectoryEntry() {
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public java.lang.String getCharset() {
        return charset;
    }

    public void setCharset(java.lang.String charset) {
        this.charset = charset;
    }

    public java.lang.String getPerm() {
        return perm;
    }

    public void setPerm(java.lang.String perm) {
        this.perm = perm;
    }

    public java.lang.String getModify() {
        return modify;
    }

    public void setModify(java.lang.String modify) {
        this.modify = modify;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public java.lang.String getType() {
        return type;
    }

    public void setType(java.lang.String type) {
        this.type = type;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DirectoryEntry)) {
            return false;
        }

        DirectoryEntry other = (DirectoryEntry) obj;

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
            && (((this.name == null) && (other.getName() == null)) || ((this.name != null) && this.name
                .equals(other.getName())))
            && (((this.charset == null) && (other.getCharset() == null)) || ((this.charset != null) && this.charset
                .equals(other.getCharset())))
            && (((this.perm == null) && (other.getPerm() == null)) || ((this.perm != null) && this.perm
                .equals(other.getPerm())))
            && (((this.modify == null) && (other.getModify() == null)) || ((this.modify != null) && this.modify
                .equals(other.getModify())))
            && (this.size == other.getSize())
            && (this.mode == other.getMode())
            && (((this.type == null) && (other.getType() == null)) || ((this.type != null) && this.type
                .equals(other.getType())));
        __equalsCalc = null;

        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }

        __hashCodeCalc = true;

        int _hashCode = 1;

        if (getName() != null) {
            _hashCode += getName().hashCode();
        }

        if (getCharset() != null) {
            _hashCode += getCharset().hashCode();
        }

        if (getPerm() != null) {
            _hashCode += getPerm().hashCode();
        }

        if (getModify() != null) {
            _hashCode += getModify().hashCode();
        }

        _hashCode += new Long(getSize()).hashCode();
        _hashCode += getMode();

        if (getType() != null) {
            _hashCode += getType().hashCode();
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
