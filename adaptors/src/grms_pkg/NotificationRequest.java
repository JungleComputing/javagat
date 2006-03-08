/**
 * NotificationRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */
package grms_pkg;

public class NotificationRequest implements java.io.Serializable {
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
        NotificationRequest.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:grms",
            "NotificationRequest"));

        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eventType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "eventType"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:grms",
            "EventType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("protocol");
        elemField.setXmlName(new javax.xml.namespace.QName("", "protocol"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:grms",
            "ProtocolType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("destination");
        elemField.setXmlName(new javax.xml.namespace.QName("", "destination"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("append");
        elemField.setXmlName(new javax.xml.namespace.QName("", "append"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("format");
        elemField.setXmlName(new javax.xml.namespace.QName("", "format"));
        elemField.setXmlType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
    }

    private grms_pkg.EventType eventType;

    private grms_pkg.ProtocolType protocol;

    private java.lang.String destination;

    private boolean append;

    private java.lang.String format;

    private java.lang.Object __equalsCalc = null;

    private boolean __hashCodeCalc = false;

    public NotificationRequest() {
    }

    public grms_pkg.EventType getEventType() {
        return eventType;
    }

    public void setEventType(grms_pkg.EventType eventType) {
        this.eventType = eventType;
    }

    public grms_pkg.ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(grms_pkg.ProtocolType protocol) {
        this.protocol = protocol;
    }

    public java.lang.String getDestination() {
        return destination;
    }

    public void setDestination(java.lang.String destination) {
        this.destination = destination;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public java.lang.String getFormat() {
        return format;
    }

    public void setFormat(java.lang.String format) {
        this.format = format;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NotificationRequest)) {
            return false;
        }

        NotificationRequest other = (NotificationRequest) obj;

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
            && (((this.eventType == null) && (other.getEventType() == null)) || ((this.eventType != null) && this.eventType
                .equals(other.getEventType())))
            && (((this.protocol == null) && (other.getProtocol() == null)) || ((this.protocol != null) && this.protocol
                .equals(other.getProtocol())))
            && (((this.destination == null) && (other.getDestination() == null)) || ((this.destination != null) && this.destination
                .equals(other.getDestination())))
            && (this.append == other.isAppend())
            && (((this.format == null) && (other.getFormat() == null)) || ((this.format != null) && this.format
                .equals(other.getFormat())));
        __equalsCalc = null;

        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }

        __hashCodeCalc = true;

        int _hashCode = 1;

        if (getEventType() != null) {
            _hashCode += getEventType().hashCode();
        }

        if (getProtocol() != null) {
            _hashCode += getProtocol().hashCode();
        }

        if (getDestination() != null) {
            _hashCode += getDestination().hashCode();
        }

        _hashCode += new Boolean(isAppend()).hashCode();

        if (getFormat() != null) {
            _hashCode += getFormat().hashCode();
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
