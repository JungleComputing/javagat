/**
 * PathType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */
package grms_pkg;

public class PathType implements java.io.Serializable {
    private static java.util.HashMap _table_ = new java.util.HashMap();

    public static final java.lang.String _FILE = "FILE";

    public static final java.lang.String _DIRECTORY = "DIRECTORY";

    public static final PathType FILE = new PathType(_FILE);

    public static final PathType DIRECTORY = new PathType(_DIRECTORY);

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
        PathType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:grms",
            "PathType"));
    }

    private java.lang.String _value_;

    // Constructor
    protected PathType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_, this);
    }

    public java.lang.String getValue() {
        return _value_;
    }

    public static PathType fromValue(java.lang.String value)
            throws java.lang.IllegalStateException {
        PathType enum = (PathType) _table_.get(value);

        if (enum == null) {
            throw new java.lang.IllegalStateException();
        }

        return enum;
    }

    public static PathType fromString(java.lang.String value)
            throws java.lang.IllegalStateException {
        return fromValue(value);
    }

    public boolean equals(java.lang.Object obj) {
        return (obj == this);
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public java.lang.String toString() {
        return _value_;
    }

    public java.lang.Object readResolve() throws java.io.ObjectStreamException {
        return fromValue(_value_);
    }

    public static org.apache.axis.encoding.Serializer getSerializer(
            java.lang.String mechType, java.lang.Class _javaType,
            javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.EnumSerializer(_javaType,
            _xmlType);
    }

    public static org.apache.axis.encoding.Deserializer getDeserializer(
            java.lang.String mechType, java.lang.Class _javaType,
            javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.EnumDeserializer(_javaType,
            _xmlType);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }
}
