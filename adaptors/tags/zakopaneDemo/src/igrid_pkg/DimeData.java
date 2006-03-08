/**
 * DimeData.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package igrid_pkg;

public class DimeData implements java.io.Serializable,
		org.apache.axis.encoding.SimpleType {
	private byte[] value;

	public DimeData() {
	}

	// Simple Types must have a String constructor
	public DimeData(byte[] value) {
		this.value = value;
	}

	public DimeData(java.lang.String value) {
		this.value = org.apache.axis.types.HexBinary.decode(value);
	}

	// Simple Types must have a toString for serializing the value
	public java.lang.String toString() {
		return value == null ? null : org.apache.axis.types.HexBinary
				.encode(value);
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	private java.lang.Object __equalsCalc = null;

	public synchronized boolean equals(java.lang.Object obj) {
		if (!(obj instanceof DimeData))
			return false;
		DimeData other = (DimeData) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true && ((this.value == null && other.getValue() == null) || (this.value != null && java.util.Arrays
				.equals(this.value, other.getValue())));
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
		if (getValue() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getValue()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(getValue(),
						i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			DimeData.class);

	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("urn:igrid",
				"dimeData"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("value");
		elemField.setXmlName(new javax.xml.namespace.QName("", "value"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "base64Binary"));
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
		return new org.apache.axis.encoding.ser.SimpleSerializer(_javaType,
				_xmlType, typeDesc);
	}

	/**
	 * Get Custom Deserializer
	 */
	public static org.apache.axis.encoding.Deserializer getDeserializer(
			java.lang.String mechType, java.lang.Class _javaType,
			javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.SimpleDeserializer(_javaType,
				_xmlType, typeDesc);
	}

}