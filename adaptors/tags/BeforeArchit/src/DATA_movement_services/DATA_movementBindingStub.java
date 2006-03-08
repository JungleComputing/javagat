/**
 * DATA_movementBindingStub.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package DATA_movement_services;

public class DATA_movementBindingStub extends org.apache.axis.client.Stub
		implements DATA_movement_services.DATA_movementPortType {
	private java.util.Vector cachedSerClasses = new java.util.Vector();

	private java.util.Vector cachedSerQNames = new java.util.Vector();

	private java.util.Vector cachedSerFactories = new java.util.Vector();

	private java.util.Vector cachedDeserFactories = new java.util.Vector();

	static org.apache.axis.description.OperationDesc [] _operations;

	static {
		_operations = new org.apache.axis.description.OperationDesc[17];
		org.apache.axis.description.OperationDesc oper;
		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATACopyFile");
		oper.addParameter(new javax.xml.namespace.QName("", "source-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "dest-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "max-retries"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "use-parallel"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		oper.setReturnClass(java.lang.String.class);
		oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[0] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATACopyFileDefaults");
		oper.addParameter(new javax.xml.namespace.QName("", "source-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "dest-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		oper.setReturnClass(java.lang.String.class);
		oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[1] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATAMoveFile");
		oper.addParameter(new javax.xml.namespace.QName("", "source-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "dest-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "max-retries"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "use-parallel"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		oper.setReturnClass(java.lang.String.class);
		oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[2] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATAMoveFileDefaults");
		oper.addParameter(new javax.xml.namespace.QName("", "source-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "dest-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		oper.setReturnClass(java.lang.String.class);
		oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[3] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATADeleteFileDefaults");
		oper.addParameter(new javax.xml.namespace.QName("", "in-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		oper.setReturnClass(java.lang.String.class);
		oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[4] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("isAlive");
		oper.addParameter(new javax.xml.namespace.QName("", "dump"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "int"));
		oper.setReturnClass(int.class);
		oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[5] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("getServiceDescription");
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		oper.setReturnClass(java.lang.String.class);
		oper.setReturnQName(new javax.xml.namespace.QName("", "description"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[6] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATATransferFile");
		oper.addParameter(new javax.xml.namespace.QName("", "operation"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "source-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "dest-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "max-retries"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "use-parallel"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		oper.setReturnClass(java.lang.String.class);
		oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[7] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATAinit-CopyFile");
		oper.addParameter(new javax.xml.namespace.QName("", "source-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "dest-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "max-retries"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "use-parallel"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "id"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "long"),
				long.class, org.apache.axis.description.ParameterDesc.OUT,
				false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "type"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "status"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("",
				"progress-percentage"), new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "double"), double.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-code"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-string"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "user-DN"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[8] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATAinit-MoveFile");
		oper.addParameter(new javax.xml.namespace.QName("", "source-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "dest-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "max-retries"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "use-parallel"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "id"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "long"),
				long.class, org.apache.axis.description.ParameterDesc.OUT,
				false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "type"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "status"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("",
				"progress-percentage"), new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "double"), double.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-code"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-string"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "user-DN"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[9] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATAinit-DeleteFile");
		oper.addParameter(new javax.xml.namespace.QName("", "source-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "id"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "long"),
				long.class, org.apache.axis.description.ParameterDesc.OUT,
				false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "type"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "status"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("",
				"progress-percentage"), new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "double"), double.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-code"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-string"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "user-DN"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[10] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATAinit-TransferFile");
		oper.addParameter(new javax.xml.namespace.QName("", "operation"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "source-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "dest-URL"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "max-retries"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "use-parallel"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "id"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "long"),
				long.class, org.apache.axis.description.ParameterDesc.OUT,
				false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "type"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "status"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("",
				"progress-percentage"), new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "double"), double.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-code"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-string"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "user-DN"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[11] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATArestartFileTask");
		oper.addParameter(new javax.xml.namespace.QName("", "id"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "long"),
				long.class, org.apache.axis.description.ParameterDesc.INOUT,
				false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "type"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "status"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("",
				"progress-percentage"), new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "double"), double.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-code"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-string"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "user-DN"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[12] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATAgetFileTask");
		oper.addParameter(new javax.xml.namespace.QName("", "id"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "long"),
				long.class, org.apache.axis.description.ParameterDesc.INOUT,
				false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "type"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "status"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("",
				"progress-percentage"), new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "double"), double.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-code"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-string"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "user-DN"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[13] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATAwaitFileTask");
		oper.addParameter(new javax.xml.namespace.QName("", "id"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "long"),
				long.class, org.apache.axis.description.ParameterDesc.INOUT,
				false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "type"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "status"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("",
				"progress-percentage"), new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "double"), double.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-code"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-string"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "user-DN"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[14] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATAstopFileTask");
		oper.addParameter(new javax.xml.namespace.QName("", "id"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "long"),
				long.class, org.apache.axis.description.ParameterDesc.INOUT,
				false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "type"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "status"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("",
				"progress-percentage"), new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "double"), double.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-code"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "int"), int.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "error-string"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "user-DN"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.OUT, false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[15] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("DATAdeleteFileTask");
		oper.addParameter(new javax.xml.namespace.QName("", "id"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "long"),
				long.class, org.apache.axis.description.ParameterDesc.IN,
				false, false);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "int"));
		oper.setReturnClass(int.class);
		oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[16] = oper;

	}

	public DATA_movementBindingStub() throws org.apache.axis.AxisFault {
		this(null);
	}

	public DATA_movementBindingStub(java.net.URL endpointURL,
			javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
		this(service);
		super.cachedEndpoint = endpointURL;
	}

	public DATA_movementBindingStub(javax.xml.rpc.Service service)
			throws org.apache.axis.AxisFault {
		if (service == null) {
			super.service = new org.apache.axis.client.Service();
		} else {
			super.service = service;
		}
	}

	private org.apache.axis.client.Call createCall()
			throws java.rmi.RemoteException {
		try {
			org.apache.axis.client.Call _call = (org.apache.axis.client.Call) super.service
					.createCall();
			if (super.maintainSessionSet) {
				_call.setMaintainSession(super.maintainSession);
			}
			if (super.cachedUsername != null) {
				_call.setUsername(super.cachedUsername);
			}
			if (super.cachedPassword != null) {
				_call.setPassword(super.cachedPassword);
			}
			if (super.cachedEndpoint != null) {
				_call.setTargetEndpointAddress(super.cachedEndpoint);
			}
			if (super.cachedTimeout != null) {
				_call.setTimeout(super.cachedTimeout);
			}
			if (super.cachedPortName != null) {
				_call.setPortName(super.cachedPortName);
			}
			java.util.Enumeration keys = super.cachedProperties.keys();
			while (keys.hasMoreElements()) {
				java.lang.String key = (java.lang.String) keys.nextElement();
				_call.setProperty(key, super.cachedProperties.get(key));
			}
			return _call;
		} catch (java.lang.Throwable t) {
			throw new org.apache.axis.AxisFault(
					"Failure trying to get the Call object", t);
		}
	}

	public java.lang.String DATACopyFile(java.lang.String sourceURL,
			java.lang.String destURL, int maxRetries, int useParallel)
			throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[0]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATACopyFile"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				sourceURL, destURL, new java.lang.Integer(maxRetries),
				new java.lang.Integer(useParallel) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			try {
				return (java.lang.String) _resp;
			} catch (java.lang.Exception _exception) {
				return (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_resp, java.lang.String.class);
			}
		}
	}

	public java.lang.String DATACopyFileDefaults(java.lang.String sourceURL,
			java.lang.String destURL) throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[1]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATACopyFileDefaults"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				sourceURL, destURL });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			try {
				return (java.lang.String) _resp;
			} catch (java.lang.Exception _exception) {
				return (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_resp, java.lang.String.class);
			}
		}
	}

	public java.lang.String DATAMoveFile(java.lang.String sourceURL,
			java.lang.String destURL, int maxRetries, int useParallel)
			throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[2]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATAMoveFile"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				sourceURL, destURL, new java.lang.Integer(maxRetries),
				new java.lang.Integer(useParallel) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			try {
				return (java.lang.String) _resp;
			} catch (java.lang.Exception _exception) {
				return (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_resp, java.lang.String.class);
			}
		}
	}

	public java.lang.String DATAMoveFileDefaults(java.lang.String sourceURL,
			java.lang.String destURL) throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[3]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATAMoveFileDefaults"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				sourceURL, destURL });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			try {
				return (java.lang.String) _resp;
			} catch (java.lang.Exception _exception) {
				return (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_resp, java.lang.String.class);
			}
		}
	}

	public java.lang.String DATADeleteFileDefaults(java.lang.String inURL)
			throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[4]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATADeleteFileDefaults"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] { inURL });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			try {
				return (java.lang.String) _resp;
			} catch (java.lang.Exception _exception) {
				return (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_resp, java.lang.String.class);
			}
		}
	}

	public int isAlive(int dump) throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[5]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "isAlive"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call
				.invoke(new java.lang.Object[] { new java.lang.Integer(dump) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			try {
				return ((java.lang.Integer) _resp).intValue();
			} catch (java.lang.Exception _exception) {
				return ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_resp, int.class)).intValue();
			}
		}
	}

	public java.lang.String getServiceDescription()
			throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[6]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "getServiceDescription"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			try {
				return (java.lang.String) _resp;
			} catch (java.lang.Exception _exception) {
				return (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_resp, java.lang.String.class);
			}
		}
	}

	public java.lang.String DATATransferFile(int operation,
			java.lang.String sourceURL, java.lang.String destURL,
			int maxRetries, int useParallel) throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[7]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATATransferFile"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				new java.lang.Integer(operation), sourceURL, destURL,
				new java.lang.Integer(maxRetries),
				new java.lang.Integer(useParallel) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			try {
				return (java.lang.String) _resp;
			} catch (java.lang.Exception _exception) {
				return (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_resp, java.lang.String.class);
			}
		}
	}

	public void DATAinitCopyFile(java.lang.String sourceURL,
			java.lang.String destURL, int maxRetries, int useParallel,
			javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[8]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATAinit-CopyFile"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				sourceURL, destURL, new java.lang.Integer(maxRetries),
				new java.lang.Integer(useParallel) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			java.util.Map _output;
			_output = _call.getOutputParams();
			try {
				id.value = ((java.lang.Long) _output
						.get(new javax.xml.namespace.QName("", "id")))
						.longValue();
			} catch (java.lang.Exception _exception) {
				id.value = ((java.lang.Long) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"id")), long.class)).longValue();
			}
			try {
				type.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "type")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				type.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"type")), int.class)).intValue();
			}
			try {
				status.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "status")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				status.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"status")), int.class)).intValue();
			}
			try {
				progressPercentage.value = ((java.lang.Double) _output
						.get(new javax.xml.namespace.QName("",
								"progress-percentage"))).doubleValue();
			} catch (java.lang.Exception _exception) {
				progressPercentage.value = ((java.lang.Double) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"progress-percentage")), double.class))
						.doubleValue();
			}
			try {
				errorCode.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "error-code")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				errorCode.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-code")), int.class)).intValue();
			}
			try {
				errorString.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "error-string"));
			} catch (java.lang.Exception _exception) {
				errorString.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-string")), java.lang.String.class);
			}
			try {
				userDN.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "user-DN"));
			} catch (java.lang.Exception _exception) {
				userDN.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"user-DN")), java.lang.String.class);
			}
		}
	}

	public void DATAinitMoveFile(java.lang.String sourceURL,
			java.lang.String destURL, int maxRetries, int useParallel,
			javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[9]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATAinit-MoveFile"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				sourceURL, destURL, new java.lang.Integer(maxRetries),
				new java.lang.Integer(useParallel) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			java.util.Map _output;
			_output = _call.getOutputParams();
			try {
				id.value = ((java.lang.Long) _output
						.get(new javax.xml.namespace.QName("", "id")))
						.longValue();
			} catch (java.lang.Exception _exception) {
				id.value = ((java.lang.Long) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"id")), long.class)).longValue();
			}
			try {
				type.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "type")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				type.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"type")), int.class)).intValue();
			}
			try {
				status.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "status")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				status.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"status")), int.class)).intValue();
			}
			try {
				progressPercentage.value = ((java.lang.Double) _output
						.get(new javax.xml.namespace.QName("",
								"progress-percentage"))).doubleValue();
			} catch (java.lang.Exception _exception) {
				progressPercentage.value = ((java.lang.Double) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"progress-percentage")), double.class))
						.doubleValue();
			}
			try {
				errorCode.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "error-code")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				errorCode.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-code")), int.class)).intValue();
			}
			try {
				errorString.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "error-string"));
			} catch (java.lang.Exception _exception) {
				errorString.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-string")), java.lang.String.class);
			}
			try {
				userDN.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "user-DN"));
			} catch (java.lang.Exception _exception) {
				userDN.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"user-DN")), java.lang.String.class);
			}
		}
	}

	public void DATAinitDeleteFile(java.lang.String sourceURL,
			javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[10]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATAinit-DeleteFile"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call
				.invoke(new java.lang.Object[] { sourceURL });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			java.util.Map _output;
			_output = _call.getOutputParams();
			try {
				id.value = ((java.lang.Long) _output
						.get(new javax.xml.namespace.QName("", "id")))
						.longValue();
			} catch (java.lang.Exception _exception) {
				id.value = ((java.lang.Long) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"id")), long.class)).longValue();
			}
			try {
				type.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "type")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				type.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"type")), int.class)).intValue();
			}
			try {
				status.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "status")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				status.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"status")), int.class)).intValue();
			}
			try {
				progressPercentage.value = ((java.lang.Double) _output
						.get(new javax.xml.namespace.QName("",
								"progress-percentage"))).doubleValue();
			} catch (java.lang.Exception _exception) {
				progressPercentage.value = ((java.lang.Double) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"progress-percentage")), double.class))
						.doubleValue();
			}
			try {
				errorCode.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "error-code")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				errorCode.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-code")), int.class)).intValue();
			}
			try {
				errorString.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "error-string"));
			} catch (java.lang.Exception _exception) {
				errorString.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-string")), java.lang.String.class);
			}
			try {
				userDN.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "user-DN"));
			} catch (java.lang.Exception _exception) {
				userDN.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"user-DN")), java.lang.String.class);
			}
		}
	}

	public void DATAinitTransferFile(int operation, java.lang.String sourceURL,
			java.lang.String destURL, int maxRetries, int useParallel,
			javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[11]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATAinit-TransferFile"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				new java.lang.Integer(operation), sourceURL, destURL,
				new java.lang.Integer(maxRetries),
				new java.lang.Integer(useParallel) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			java.util.Map _output;
			_output = _call.getOutputParams();
			try {
				id.value = ((java.lang.Long) _output
						.get(new javax.xml.namespace.QName("", "id")))
						.longValue();
			} catch (java.lang.Exception _exception) {
				id.value = ((java.lang.Long) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"id")), long.class)).longValue();
			}
			try {
				type.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "type")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				type.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"type")), int.class)).intValue();
			}
			try {
				status.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "status")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				status.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"status")), int.class)).intValue();
			}
			try {
				progressPercentage.value = ((java.lang.Double) _output
						.get(new javax.xml.namespace.QName("",
								"progress-percentage"))).doubleValue();
			} catch (java.lang.Exception _exception) {
				progressPercentage.value = ((java.lang.Double) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"progress-percentage")), double.class))
						.doubleValue();
			}
			try {
				errorCode.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "error-code")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				errorCode.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-code")), int.class)).intValue();
			}
			try {
				errorString.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "error-string"));
			} catch (java.lang.Exception _exception) {
				errorString.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-string")), java.lang.String.class);
			}
			try {
				userDN.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "user-DN"));
			} catch (java.lang.Exception _exception) {
				userDN.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"user-DN")), java.lang.String.class);
			}
		}
	}

	public void DATArestartFileTask(javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[12]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATArestartFileTask"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call
				.invoke(new java.lang.Object[] { new java.lang.Long(id.value) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			java.util.Map _output;
			_output = _call.getOutputParams();
			try {
				id.value = ((java.lang.Long) _output
						.get(new javax.xml.namespace.QName("", "id")))
						.longValue();
			} catch (java.lang.Exception _exception) {
				id.value = ((java.lang.Long) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"id")), long.class)).longValue();
			}
			try {
				type.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "type")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				type.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"type")), int.class)).intValue();
			}
			try {
				status.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "status")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				status.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"status")), int.class)).intValue();
			}
			try {
				progressPercentage.value = ((java.lang.Double) _output
						.get(new javax.xml.namespace.QName("",
								"progress-percentage"))).doubleValue();
			} catch (java.lang.Exception _exception) {
				progressPercentage.value = ((java.lang.Double) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"progress-percentage")), double.class))
						.doubleValue();
			}
			try {
				errorCode.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "error-code")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				errorCode.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-code")), int.class)).intValue();
			}
			try {
				errorString.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "error-string"));
			} catch (java.lang.Exception _exception) {
				errorString.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-string")), java.lang.String.class);
			}
			try {
				userDN.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "user-DN"));
			} catch (java.lang.Exception _exception) {
				userDN.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"user-DN")), java.lang.String.class);
			}
		}
	}

	public void DATAgetFileTask(javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[13]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATAgetFileTask"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call
				.invoke(new java.lang.Object[] { new java.lang.Long(id.value) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			java.util.Map _output;
			_output = _call.getOutputParams();
			try {
				id.value = ((java.lang.Long) _output
						.get(new javax.xml.namespace.QName("", "id")))
						.longValue();
			} catch (java.lang.Exception _exception) {
				id.value = ((java.lang.Long) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"id")), long.class)).longValue();
			}
			try {
				type.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "type")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				type.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"type")), int.class)).intValue();
			}
			try {
				status.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "status")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				status.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"status")), int.class)).intValue();
			}
			try {
				progressPercentage.value = ((java.lang.Double) _output
						.get(new javax.xml.namespace.QName("",
								"progress-percentage"))).doubleValue();
			} catch (java.lang.Exception _exception) {
				progressPercentage.value = ((java.lang.Double) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"progress-percentage")), double.class))
						.doubleValue();
			}
			try {
				errorCode.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "error-code")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				errorCode.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-code")), int.class)).intValue();
			}
			try {
				errorString.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "error-string"));
			} catch (java.lang.Exception _exception) {
				errorString.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-string")), java.lang.String.class);
			}
			try {
				userDN.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "user-DN"));
			} catch (java.lang.Exception _exception) {
				userDN.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"user-DN")), java.lang.String.class);
			}
		}
	}

	public void DATAwaitFileTask(javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[14]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATAwaitFileTask"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call
				.invoke(new java.lang.Object[] { new java.lang.Long(id.value) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			java.util.Map _output;
			_output = _call.getOutputParams();
			try {
				id.value = ((java.lang.Long) _output
						.get(new javax.xml.namespace.QName("", "id")))
						.longValue();
			} catch (java.lang.Exception _exception) {
				id.value = ((java.lang.Long) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"id")), long.class)).longValue();
			}
			try {
				type.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "type")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				type.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"type")), int.class)).intValue();
			}
			try {
				status.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "status")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				status.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"status")), int.class)).intValue();
			}
			try {
				progressPercentage.value = ((java.lang.Double) _output
						.get(new javax.xml.namespace.QName("",
								"progress-percentage"))).doubleValue();
			} catch (java.lang.Exception _exception) {
				progressPercentage.value = ((java.lang.Double) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"progress-percentage")), double.class))
						.doubleValue();
			}
			try {
				errorCode.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "error-code")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				errorCode.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-code")), int.class)).intValue();
			}
			try {
				errorString.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "error-string"));
			} catch (java.lang.Exception _exception) {
				errorString.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-string")), java.lang.String.class);
			}
			try {
				userDN.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "user-DN"));
			} catch (java.lang.Exception _exception) {
				userDN.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"user-DN")), java.lang.String.class);
			}
		}
	}

	public void DATAstopFileTask(javax.xml.rpc.holders.LongHolder id,
			javax.xml.rpc.holders.IntHolder type,
			javax.xml.rpc.holders.IntHolder status,
			javax.xml.rpc.holders.DoubleHolder progressPercentage,
			javax.xml.rpc.holders.IntHolder errorCode,
			javax.xml.rpc.holders.StringHolder errorString,
			javax.xml.rpc.holders.StringHolder userDN)
			throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[15]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATAstopFileTask"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call
				.invoke(new java.lang.Object[] { new java.lang.Long(id.value) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			java.util.Map _output;
			_output = _call.getOutputParams();
			try {
				id.value = ((java.lang.Long) _output
						.get(new javax.xml.namespace.QName("", "id")))
						.longValue();
			} catch (java.lang.Exception _exception) {
				id.value = ((java.lang.Long) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"id")), long.class)).longValue();
			}
			try {
				type.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "type")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				type.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"type")), int.class)).intValue();
			}
			try {
				status.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "status")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				status.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"status")), int.class)).intValue();
			}
			try {
				progressPercentage.value = ((java.lang.Double) _output
						.get(new javax.xml.namespace.QName("",
								"progress-percentage"))).doubleValue();
			} catch (java.lang.Exception _exception) {
				progressPercentage.value = ((java.lang.Double) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"progress-percentage")), double.class))
						.doubleValue();
			}
			try {
				errorCode.value = ((java.lang.Integer) _output
						.get(new javax.xml.namespace.QName("", "error-code")))
						.intValue();
			} catch (java.lang.Exception _exception) {
				errorCode.value = ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-code")), int.class)).intValue();
			}
			try {
				errorString.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "error-string"));
			} catch (java.lang.Exception _exception) {
				errorString.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"error-string")), java.lang.String.class);
			}
			try {
				userDN.value = (java.lang.String) _output
						.get(new javax.xml.namespace.QName("", "user-DN"));
			} catch (java.lang.Exception _exception) {
				userDN.value = (java.lang.String) org.apache.axis.utils.JavaUtils
						.convert(_output.get(new javax.xml.namespace.QName("",
								"user-DN")), java.lang.String.class);
			}
		}
	}

	public int DATAdeleteFileTask(long id) throws java.rmi.RemoteException {
		if (super.cachedEndpoint == null) {
			throw new org.apache.axis.NoEndPointException();
		}
		org.apache.axis.client.Call _call = createCall();
		_call.setOperation(_operations[16]);
		_call.setUseSOAPAction(true);
		_call.setSOAPActionURI("");
		_call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		_call.setOperationName(new javax.xml.namespace.QName(
				"urn:DATA_movement_services", "DATAdeleteFileTask"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call
				.invoke(new java.lang.Object[] { new java.lang.Long(id) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			try {
				return ((java.lang.Integer) _resp).intValue();
			} catch (java.lang.Exception _exception) {
				return ((java.lang.Integer) org.apache.axis.utils.JavaUtils
						.convert(_resp, int.class)).intValue();
			}
		}
	}

}