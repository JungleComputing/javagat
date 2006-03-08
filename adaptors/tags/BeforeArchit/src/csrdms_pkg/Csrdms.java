/**
 * Csrdms.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package csrdms_pkg;

public interface Csrdms extends javax.xml.rpc.Service {

	// gSOAP 2.3.8 generated service definition
	public java.lang.String getcsrdmsAddress();

	public csrdms_pkg.CsrdmsPortType getcsrdms()
			throws javax.xml.rpc.ServiceException;

	public csrdms_pkg.CsrdmsPortType getcsrdms(java.net.URL portAddress)
			throws javax.xml.rpc.ServiceException;
}