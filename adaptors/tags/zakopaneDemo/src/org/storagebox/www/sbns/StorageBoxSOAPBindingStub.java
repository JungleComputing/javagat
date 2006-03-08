/**
 * StorageBoxSOAPBindingStub.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package org.storagebox.www.sbns;

public class StorageBoxSOAPBindingStub extends org.apache.axis.client.Stub
		implements org.storagebox.www.sbns.StorageBoxPortType {
	private java.util.Vector cachedSerClasses = new java.util.Vector();

	private java.util.Vector cachedSerQNames = new java.util.Vector();

	private java.util.Vector cachedSerFactories = new java.util.Vector();

	private java.util.Vector cachedDeserFactories = new java.util.Vector();

	static org.apache.axis.description.OperationDesc [] _operations;

	static {
		_operations = new org.apache.axis.description.OperationDesc[9];
		org.apache.axis.description.OperationDesc oper;
		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("addAttribute");
		oper.addParameter(new javax.xml.namespace.QName("", "namespace_name"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "global_oid"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "attribute_name"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "attribute_value"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[0] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("setAttribute");
		oper.addParameter(new javax.xml.namespace.QName("", "namespace_name"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "global_oid"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "attribute_name"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "attribute_value"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[1] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("removeAttribute");
		oper.addParameter(new javax.xml.namespace.QName("", "namespace_name"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "global_oid"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "attribute_name"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "attribute_value"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[2] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("createObject");
		oper.addParameter(new javax.xml.namespace.QName("", "namespace_name"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "global_oid"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		oper.setReturnClass(java.lang.String.class);
		oper.setReturnQName(new javax.xml.namespace.QName("", "created_goid"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[3] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("deleteObject");
		oper.addParameter(new javax.xml.namespace.QName("", "namespace_name"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "global_oid"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[4] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("getAttributes");
		oper.addParameter(new javax.xml.namespace.QName("", "namespaces"),
				new javax.xml.namespace.QName("http://www.storagebox.org/sbns",
						"StringSet"), org.storagebox.www.sbns.StringSet.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "objects"),
				new javax.xml.namespace.QName("http://www.storagebox.org/sbns",
						"StringSet"), org.storagebox.www.sbns.StringSet.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(
				new javax.xml.namespace.QName("", "attributes_names"),
				new javax.xml.namespace.QName("http://www.storagebox.org/sbns",
						"StringSet"), org.storagebox.www.sbns.StringSet.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.storagebox.org/sbns", "AttributeSet"));
		oper.setReturnClass(org.storagebox.www.sbns.AttributeSet.class);
		oper.setReturnQName(new javax.xml.namespace.QName("", "result_set"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[5] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("query");
		oper.addParameter(new javax.xml.namespace.QName("", "namespaces"),
				new javax.xml.namespace.QName("http://www.storagebox.org/sbns",
						"StringSet"), org.storagebox.www.sbns.StringSet.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "query"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(
				new javax.xml.namespace.QName("", "attributes_names"),
				new javax.xml.namespace.QName("http://www.storagebox.org/sbns",
						"StringSet"), org.storagebox.www.sbns.StringSet.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.storagebox.org/sbns", "AttributeSet"));
		oper.setReturnClass(org.storagebox.www.sbns.AttributeSet.class);
		oper.setReturnQName(new javax.xml.namespace.QName("", "result_set"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[6] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("createAttribute");
		oper.addParameter(new javax.xml.namespace.QName("", "namespace_name"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "attribute_name"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "type_name"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.addParameter(new javax.xml.namespace.QName("", "single_valued"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "boolean"),
				boolean.class, org.apache.axis.description.ParameterDesc.IN,
				false, false);
		oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[7] = oper;

		oper = new org.apache.axis.description.OperationDesc();
		oper.setName("getSchema");
		oper.addParameter(new javax.xml.namespace.QName("", "namespace_name"),
				new javax.xml.namespace.QName(
						"http://www.w3.org/2001/XMLSchema", "string"),
				java.lang.String.class,
				org.apache.axis.description.ParameterDesc.IN, false, false);
		oper.setReturnType(new javax.xml.namespace.QName(
				"http://www.storagebox.org/sbns", "DatabaseSchema"));
		oper.setReturnClass(org.storagebox.www.sbns.DatabaseSchema.class);
		oper
				.setReturnQName(new javax.xml.namespace.QName("",
						"database_schema"));
		oper.setStyle(org.apache.axis.enum.Style.RPC);
		oper.setUse(org.apache.axis.enum.Use.ENCODED);
		_operations[8] = oper;

	}

	public StorageBoxSOAPBindingStub() throws org.apache.axis.AxisFault {
		this(null);
	}

	public StorageBoxSOAPBindingStub(java.net.URL endpointURL,
			javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
		this(service);
		super.cachedEndpoint = endpointURL;
	}

	public StorageBoxSOAPBindingStub(javax.xml.rpc.Service service)
			throws org.apache.axis.AxisFault {
		if (service == null) {
			super.service = new org.apache.axis.client.Service();
		} else {
			super.service = service;
		}
		java.lang.Class cls;
		javax.xml.namespace.QName qName;
		java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
		java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
		java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
		java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
		java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
		java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
		java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
		java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
		qName = new javax.xml.namespace.QName("http://www.storagebox.org/sbns",
				"AttributeValue");
		cachedSerQNames.add(qName);
		cls = org.storagebox.www.sbns.AttributeValue.class;
		cachedSerClasses.add(cls);
		cachedSerFactories.add(beansf);
		cachedDeserFactories.add(beandf);

		qName = new javax.xml.namespace.QName("http://www.storagebox.org/sbns",
				"DatabaseAttributeSchema");
		cachedSerQNames.add(qName);
		cls = org.storagebox.www.sbns.DatabaseAttributeSchema.class;
		cachedSerClasses.add(cls);
		cachedSerFactories.add(beansf);
		cachedDeserFactories.add(beandf);

		qName = new javax.xml.namespace.QName("http://www.storagebox.org/sbns",
				"DatabaseSchema");
		cachedSerQNames.add(qName);
		cls = org.storagebox.www.sbns.DatabaseSchema.class;
		cachedSerClasses.add(cls);
		cachedSerFactories.add(beansf);
		cachedDeserFactories.add(beandf);

		qName = new javax.xml.namespace.QName("http://www.storagebox.org/sbns",
				"AttributeSet");
		cachedSerQNames.add(qName);
		cls = org.storagebox.www.sbns.AttributeSet.class;
		cachedSerClasses.add(cls);
		cachedSerFactories.add(beansf);
		cachedDeserFactories.add(beandf);

		qName = new javax.xml.namespace.QName("http://www.storagebox.org/sbns",
				"StringSet");
		cachedSerQNames.add(qName);
		cls = org.storagebox.www.sbns.StringSet.class;
		cachedSerClasses.add(cls);
		cachedSerFactories.add(beansf);
		cachedDeserFactories.add(beandf);

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
			// All the type mapping information is registered
			// when the first call is made.
			// The type mapping information is actually registered in
			// the TypeMappingRegistry of the service, which
			// is the reason why registration is only needed for the first call.
			synchronized (this) {
				if (firstCall()) {
					// must set encoding style before registering serializers
					_call
							.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
					_call
							.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
					for (int i = 0; i < cachedSerFactories.size(); ++i) {
						java.lang.Class cls = (java.lang.Class) cachedSerClasses
								.get(i);
						javax.xml.namespace.QName qName = (javax.xml.namespace.QName) cachedSerQNames
								.get(i);
						java.lang.Class sf = (java.lang.Class) cachedSerFactories
								.get(i);
						java.lang.Class df = (java.lang.Class) cachedDeserFactories
								.get(i);
						_call.registerTypeMapping(cls, qName, sf, df, false);
					}
				}
			}
			return _call;
		} catch (java.lang.Throwable t) {
			throw new org.apache.axis.AxisFault(
					"Failure trying to get the Call object", t);
		}
	}

	public void addAttribute(java.lang.String namespace_name,
			java.lang.String global_oid, java.lang.String attribute_name,
			java.lang.String attribute_value) throws java.rmi.RemoteException {
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
				"http://www.storagebox.org/sbns", "addAttribute"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				namespace_name, global_oid, attribute_name, attribute_value });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		}
		extractAttachments(_call);
	}

	public void setAttribute(java.lang.String namespace_name,
			java.lang.String global_oid, java.lang.String attribute_name,
			java.lang.String attribute_value) throws java.rmi.RemoteException {
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
				"http://www.storagebox.org/sbns", "setAttribute"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				namespace_name, global_oid, attribute_name, attribute_value });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		}
		extractAttachments(_call);
	}

	public void removeAttribute(java.lang.String namespace_name,
			java.lang.String global_oid, java.lang.String attribute_name,
			java.lang.String attribute_value) throws java.rmi.RemoteException {
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
				"http://www.storagebox.org/sbns", "removeAttribute"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				namespace_name, global_oid, attribute_name, attribute_value });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		}
		extractAttachments(_call);
	}

	public java.lang.String createObject(java.lang.String namespace_name,
			java.lang.String global_oid) throws java.rmi.RemoteException {
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
				"http://www.storagebox.org/sbns", "createObject"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				namespace_name, global_oid });

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

	public void deleteObject(java.lang.String namespace_name,
			java.lang.String global_oid) throws java.rmi.RemoteException {
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
				"http://www.storagebox.org/sbns", "deleteObject"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				namespace_name, global_oid });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		}
		extractAttachments(_call);
	}

	public org.storagebox.www.sbns.AttributeSet getAttributes(
			org.storagebox.www.sbns.StringSet namespaces,
			org.storagebox.www.sbns.StringSet objects,
			org.storagebox.www.sbns.StringSet attributes_names)
			throws java.rmi.RemoteException {
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
				"http://www.storagebox.org/sbns", "getAttributes"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				namespaces, objects, attributes_names });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			try {
				return (org.storagebox.www.sbns.AttributeSet) _resp;
			} catch (java.lang.Exception _exception) {
				return (org.storagebox.www.sbns.AttributeSet) org.apache.axis.utils.JavaUtils
						.convert(_resp,
								org.storagebox.www.sbns.AttributeSet.class);
			}
		}
	}

	public org.storagebox.www.sbns.AttributeSet query(
			org.storagebox.www.sbns.StringSet namespaces,
			java.lang.String query,
			org.storagebox.www.sbns.StringSet attributes_names)
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
				"http://www.storagebox.org/sbns", "query"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				namespaces, query, attributes_names });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			try {
				return (org.storagebox.www.sbns.AttributeSet) _resp;
			} catch (java.lang.Exception _exception) {
				return (org.storagebox.www.sbns.AttributeSet) org.apache.axis.utils.JavaUtils
						.convert(_resp,
								org.storagebox.www.sbns.AttributeSet.class);
			}
		}
	}

	public void createAttribute(java.lang.String namespace_name,
			java.lang.String attribute_name, java.lang.String type_name,
			boolean single_valued) throws java.rmi.RemoteException {
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
				"http://www.storagebox.org/sbns", "createAttribute"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
				namespace_name, attribute_name, type_name,
				new java.lang.Boolean(single_valued) });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		}
		extractAttachments(_call);
	}

	public org.storagebox.www.sbns.DatabaseSchema getSchema(
			java.lang.String namespace_name) throws java.rmi.RemoteException {
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
				"http://www.storagebox.org/sbns", "getSchema"));

		setRequestHeaders(_call);
		setAttachments(_call);
		java.lang.Object _resp = _call
				.invoke(new java.lang.Object[] { namespace_name });

		if (_resp instanceof java.rmi.RemoteException) {
			throw (java.rmi.RemoteException) _resp;
		} else {
			extractAttachments(_call);
			try {
				return (org.storagebox.www.sbns.DatabaseSchema) _resp;
			} catch (java.lang.Exception _exception) {
				return (org.storagebox.www.sbns.DatabaseSchema) org.apache.axis.utils.JavaUtils
						.convert(_resp,
								org.storagebox.www.sbns.DatabaseSchema.class);
			}
		}
	}

}