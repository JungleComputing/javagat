/**
 * DATA_browsingBindingStub.java
 *
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */
package DATA_browsing_services;

public class DATA_browsingBindingStub extends org.apache.axis.client.Stub
        implements DATA_browsing_services.DATA_browsingPortType {
    static org.apache.axis.description.OperationDesc[] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[9];

        org.apache.axis.description.OperationDesc oper;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DATAList");
        oper.addParameter(new javax.xml.namespace.QName("", "in-URL"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "verbose"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "retlist"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "response"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DATAConnectedList");
        oper.addParameter(new javax.xml.namespace.QName("", "in-URL"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "verbose"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "retlist"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "response"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DATAConnectedListStructured");
        oper.addParameter(new javax.xml.namespace.QName("", "in-URL"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "entries"),
            new javax.xml.namespace.QName("urn:DATA_browsing_services",
                "ArrayOfDirectory-entry"),
            DATA_browsing_services.DirectoryEntry[].class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "response"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DATAStopCache");
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DATAConnectedModTime");
        oper.addParameter(new javax.xml.namespace.QName("", "in-URL"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "seconds"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "long"), long.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "response"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DATAConnectedSize");
        oper.addParameter(new javax.xml.namespace.QName("", "in-URL"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "size"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "unsignedLong"), org.apache.axis.types.UnsignedLong.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "response"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DATAConnectedMkdir");
        oper.addParameter(new javax.xml.namespace.QName("", "in-URL"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "resp"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("isAlive");
        oper.addParameter(new javax.xml.namespace.QName("", "dump"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getServiceDescription");
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "description"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[8] = oper;
    }

    private java.util.Vector cachedSerClasses = new java.util.Vector();

    private java.util.Vector cachedSerQNames = new java.util.Vector();

    private java.util.Vector cachedSerFactories = new java.util.Vector();

    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    public DATA_browsingBindingStub() throws org.apache.axis.AxisFault {
        this(null);
    }

    public DATA_browsingBindingStub(java.net.URL endpointURL,
            javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        this(service);
        super.cachedEndpoint = endpointURL;
    }

    public DATA_browsingBindingStub(javax.xml.rpc.Service service)
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
        qName = new javax.xml.namespace.QName("urn:DATA_browsing_services",
            "Directory-entry");
        cachedSerQNames.add(qName);
        cls = DATA_browsing_services.DirectoryEntry.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("urn:DATA_browsing_services",
            "ArrayOfDirectory-entry");
        cachedSerQNames.add(qName);
        cls = DATA_browsing_services.DirectoryEntry[].class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(arraysf);
        cachedDeserFactories.add(arraydf);
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

    public void DATAList(java.lang.String inURL, int verbose,
            javax.xml.rpc.holders.StringHolder retlist,
            javax.xml.rpc.holders.StringHolder response)
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
            "urn:DATA_browsing_services", "DATAList"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { inURL,
            new java.lang.Integer(verbose) });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                retlist.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "retlist"));
            } catch (java.lang.Exception _exception) {
                retlist.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "retlist")), java.lang.String.class);
            }

            try {
                response.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "response"));
            } catch (java.lang.Exception _exception) {
                response.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "response")), java.lang.String.class);
            }
        }
    }

    public void DATAConnectedList(java.lang.String inURL, int verbose,
            javax.xml.rpc.holders.StringHolder retlist,
            javax.xml.rpc.holders.StringHolder response)
            throws java.rmi.RemoteException {
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
            "urn:DATA_browsing_services", "DATAConnectedList"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { inURL,
            new java.lang.Integer(verbose) });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                retlist.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "retlist"));
            } catch (java.lang.Exception _exception) {
                retlist.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "retlist")), java.lang.String.class);
            }

            try {
                response.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "response"));
            } catch (java.lang.Exception _exception) {
                response.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "response")), java.lang.String.class);
            }
        }
    }

    public void DATAConnectedListStructured(java.lang.String inURL,
            DATA_browsing_services.holders.ArrayOfDirectoryEntryHolder entries,
            javax.xml.rpc.holders.StringHolder response)
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
            "urn:DATA_browsing_services", "DATAConnectedListStructured"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { inURL });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                entries.value = (DATA_browsing_services.DirectoryEntry[]) _output
                    .get(new javax.xml.namespace.QName("", "entries"));
            } catch (java.lang.Exception _exception) {
                entries.value = (DATA_browsing_services.DirectoryEntry[]) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "entries")),
                        DATA_browsing_services.DirectoryEntry[].class);
            }

            try {
                response.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "response"));
            } catch (java.lang.Exception _exception) {
                response.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "response")), java.lang.String.class);
            }
        }
    }

    public java.lang.String DATAStopCache() throws java.rmi.RemoteException {
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
            "urn:DATA_browsing_services", "DATAStopCache"));

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

    public void DATAConnectedModTime(java.lang.String inURL,
            javax.xml.rpc.holders.LongHolder seconds,
            javax.xml.rpc.holders.StringHolder response)
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
            "urn:DATA_browsing_services", "DATAConnectedModTime"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { inURL });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                seconds.value = ((java.lang.Long) _output
                    .get(new javax.xml.namespace.QName("", "seconds")))
                    .longValue();
            } catch (java.lang.Exception _exception) {
                seconds.value = ((java.lang.Long) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "seconds")), long.class)).longValue();
            }

            try {
                response.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "response"));
            } catch (java.lang.Exception _exception) {
                response.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "response")), java.lang.String.class);
            }
        }
    }

    public void DATAConnectedSize(java.lang.String inURL,
            org.apache.axis.holders.UnsignedLongHolder size,
            javax.xml.rpc.holders.StringHolder response)
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
            "urn:DATA_browsing_services", "DATAConnectedSize"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { inURL });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);

            java.util.Map _output;
            _output = _call.getOutputParams();

            try {
                size.value = (org.apache.axis.types.UnsignedLong) _output
                    .get(new javax.xml.namespace.QName("", "size"));
            } catch (java.lang.Exception _exception) {
                size.value = (org.apache.axis.types.UnsignedLong) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "size")), org.apache.axis.types.UnsignedLong.class);
            }

            try {
                response.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "response"));
            } catch (java.lang.Exception _exception) {
                response.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "response")), java.lang.String.class);
            }
        }
    }

    public java.lang.String DATAConnectedMkdir(java.lang.String inURL)
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
            "urn:DATA_browsing_services", "DATAConnectedMkdir"));

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
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
            "urn:DATA_browsing_services", "isAlive"));

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
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
            "urn:DATA_browsing_services", "getServiceDescription"));

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
}
