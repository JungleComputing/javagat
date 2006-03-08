/**
 * Igrid.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package igrid_pkg;

public interface Igrid extends javax.xml.rpc.Service {

	// gSOAP 2.6.2 generated service definition
	public java.lang.String getigridAddress();

	public igrid_pkg.IgridPortType getigrid()
			throws javax.xml.rpc.ServiceException;

	public igrid_pkg.IgridPortType getigrid(java.net.URL portAddress)
			throws javax.xml.rpc.ServiceException;
}