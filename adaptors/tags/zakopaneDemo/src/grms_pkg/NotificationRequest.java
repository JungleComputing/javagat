/**
 * NotificationRequest.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package grms_pkg;

public class NotificationRequest implements java.io.Serializable {
	private java.lang.String jobId;

	private grms_pkg.EventType eventType;

	private grms_pkg.NotificationType notificationType;

	private java.lang.String listener;

	private java.lang.String[] users;

	public NotificationRequest() {
	}

	public java.lang.String getJobId() {
		return jobId;
	}

	public void setJobId(java.lang.String jobId) {
		this.jobId = jobId;
	}

	public grms_pkg.EventType getEventType() {
		return eventType;
	}

	public void setEventType(grms_pkg.EventType eventType) {
		this.eventType = eventType;
	}

	public grms_pkg.NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(grms_pkg.NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public java.lang.String getListener() {
		return listener;
	}

	public void setListener(java.lang.String listener) {
		this.listener = listener;
	}

	public java.lang.String[] getUsers() {
		return users;
	}

	public void setUsers(java.lang.String[] users) {
		this.users = users;
	}

	public java.lang.String getUsers(int i) {
		return users[i];
	}

	public void setUsers(int i, java.lang.String value) {
		this.users[i] = value;
	}

	private java.lang.Object __equalsCalc = null;

	public synchronized boolean equals(java.lang.Object obj) {
		if (!(obj instanceof NotificationRequest))
			return false;
		NotificationRequest other = (NotificationRequest) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true
				&& ((this.jobId == null && other.getJobId() == null) || (this.jobId != null && this.jobId
						.equals(other.getJobId())))
				&& ((this.eventType == null && other.getEventType() == null) || (this.eventType != null && this.eventType
						.equals(other.getEventType())))
				&& ((this.notificationType == null && other
						.getNotificationType() == null) || (this.notificationType != null && this.notificationType
						.equals(other.getNotificationType())))
				&& ((this.listener == null && other.getListener() == null) || (this.listener != null && this.listener
						.equals(other.getListener())))
				&& ((this.users == null && other.getUsers() == null) || (this.users != null && java.util.Arrays
						.equals(this.users, other.getUsers())));
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
		if (getJobId() != null) {
			_hashCode += getJobId().hashCode();
		}
		if (getEventType() != null) {
			_hashCode += getEventType().hashCode();
		}
		if (getNotificationType() != null) {
			_hashCode += getNotificationType().hashCode();
		}
		if (getListener() != null) {
			_hashCode += getListener().hashCode();
		}
		if (getUsers() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getUsers()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(getUsers(),
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
			NotificationRequest.class);

	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("urn:grms",
				"NotificationRequest"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("jobId");
		elemField.setXmlName(new javax.xml.namespace.QName("", "jobId"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("eventType");
		elemField.setXmlName(new javax.xml.namespace.QName("", "eventType"));
		elemField.setXmlType(new javax.xml.namespace.QName("urn:grms",
				"EventType"));
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("notificationType");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"notificationType"));
		elemField.setXmlType(new javax.xml.namespace.QName("urn:grms",
				"NotificationType"));
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("listener");
		elemField.setXmlName(new javax.xml.namespace.QName("", "listener"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("users");
		elemField.setXmlName(new javax.xml.namespace.QName("", "users"));
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