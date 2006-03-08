/**
 * GrmsService.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package grms_pkg;

public interface GrmsService extends javax.xml.rpc.Service {
	public java.lang.String getgrmsAddress();

	public grms_pkg.Grms getgrms() throws javax.xml.rpc.ServiceException;

	public grms_pkg.Grms getgrms(java.net.URL portAddress)
			throws javax.xml.rpc.ServiceException;
}