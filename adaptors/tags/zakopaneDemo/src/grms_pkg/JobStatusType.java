/**
 * JobStatusType.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package grms_pkg;

public class JobStatusType implements java.io.Serializable {
	private java.lang.String _value_;

	private static java.util.HashMap _table_ = new java.util.HashMap();

	// Constructor
	protected JobStatusType(java.lang.String value) {
		_value_ = value;
		_table_.put(_value_, this);
	}

	public static final java.lang.String _QUEUED = "QUEUED";

	public static final java.lang.String _PREPROCESSING = "PREPROCESSING";

	public static final java.lang.String _PENDING = "PENDING";

	public static final java.lang.String _RUNNING = "RUNNING";

	public static final java.lang.String _STOPPED = "STOPPED";

	public static final java.lang.String _POSTPROCESSING = "POSTPROCESSING";

	public static final java.lang.String _FINISHED = "FINISHED";

	public static final java.lang.String _SUSPENDED = "SUSPENDED";

	public static final java.lang.String _FAILED = "FAILED";

	public static final java.lang.String _CANCELED = "CANCELED";

	public static final JobStatusType QUEUED = new JobStatusType(_QUEUED);

	public static final JobStatusType PREPROCESSING = new JobStatusType(
			_PREPROCESSING);

	public static final JobStatusType PENDING = new JobStatusType(_PENDING);

	public static final JobStatusType RUNNING = new JobStatusType(_RUNNING);

	public static final JobStatusType STOPPED = new JobStatusType(_STOPPED);

	public static final JobStatusType POSTPROCESSING = new JobStatusType(
			_POSTPROCESSING);

	public static final JobStatusType FINISHED = new JobStatusType(_FINISHED);

	public static final JobStatusType SUSPENDED = new JobStatusType(_SUSPENDED);

	public static final JobStatusType FAILED = new JobStatusType(_FAILED);

	public static final JobStatusType CANCELED = new JobStatusType(_CANCELED);

	public java.lang.String getValue() {
		return _value_;
	}

	public static JobStatusType fromValue(java.lang.String value)
			throws java.lang.IllegalStateException {
		JobStatusType enum = (JobStatusType) _table_.get(value);
		if (enum == null)
			throw new java.lang.IllegalStateException();
		return enum;
	}

	public static JobStatusType fromString(java.lang.String value)
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

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			JobStatusType.class);

	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("urn:grms",
				"JobStatusType"));
	}

	/**
	 * Return type metadata object
	 */
	public static org.apache.axis.description.TypeDesc getTypeDesc() {
		return typeDesc;
	}

}