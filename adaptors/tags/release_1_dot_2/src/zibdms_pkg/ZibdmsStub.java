/**
 * ZibdmsStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package zibdms_pkg;

public class ZibdmsStub extends org.apache.axis.client.Stub implements
        zibdms_pkg.ZibdmsPortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();

    private java.util.Vector cachedSerQNames = new java.util.Vector();

    private java.util.Vector cachedSerFactories = new java.util.Vector();

    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc[] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[19];
        org.apache.axis.description.OperationDesc oper;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getHomeDirectory");
        oper.addParameter(new javax.xml.namespace.QName("", "usersubject"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "retcode"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("create");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "filetype"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "ret"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("rm");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "ret"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("existsEntity");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "result"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "boolean"), boolean.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "ret"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("isFile");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "file"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "boolean"), boolean.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "ret"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("isDirectory");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "directory"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "boolean"), boolean.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "ret"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("mkdir");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "ret"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("mkdirhier");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "ret"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("rmdir");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "ret"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("ls");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("", "directory-content"),
            new javax.xml.namespace.QName("urn:zibdms", "string-array"),
            zibdms_pkg.StringArray.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "retcode"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[9] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lsX");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("", "directory-content"),
            new javax.xml.namespace.QName("urn:zibdms",
                "pair-of-string-bool-array"),
            zibdms_pkg.PairOfStringBoolArray.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "retcode"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addLocation");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "uri"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "ret"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("removeLocation");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "uri"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "ret"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getLocations");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "locations"),
            new javax.xml.namespace.QName("urn:zibdms", "string-array"),
            zibdms_pkg.StringArray.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "retcode"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getServiceDescription");
        oper.addParameter(new javax.xml.namespace.QName("", "in"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "description"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "ret"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.OUT, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("replicateFileTo");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "url"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "ret"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("fetchFileTo");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "url"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "ret"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[16] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("replicateDirectoryTo");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "url"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "ret"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[17] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("fetchDirectoryTo");
        oper.addParameter(new javax.xml.namespace.QName("", "pathname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("", "url"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "ret"));
        oper.setStyle(org.apache.axis.enum.Style.RPC);
        oper.setUse(org.apache.axis.enum.Use.ENCODED);
        _operations[18] = oper;

    }

    public ZibdmsStub() throws org.apache.axis.AxisFault {
        this(null);
    }

    public ZibdmsStub(java.net.URL endpointURL, javax.xml.rpc.Service service)
        throws org.apache.axis.AxisFault {
        this(service);
        super.cachedEndpoint = endpointURL;
    }

    public ZibdmsStub(javax.xml.rpc.Service service)
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
        qName = new javax.xml.namespace.QName("urn:zibdms",
            "pair-of-string-bool");
        cachedSerQNames.add(qName);
        cls = zibdms_pkg.PairOfStringBool.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("urn:zibdms",
            "pair-of-string-bool-array");
        cachedSerQNames.add(qName);
        cls = zibdms_pkg.PairOfStringBoolArray.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("urn:zibdms", "string-array");
        cachedSerQNames.add(qName);
        cls = zibdms_pkg.StringArray.class;
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

    public void getHomeDirectory(java.lang.String usersubject,
        javax.xml.rpc.holders.StringHolder pathname,
        javax.xml.rpc.holders.StringHolder retcode)
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
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "getHomeDirectory"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { usersubject });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                pathname.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "pathname"));
            } catch (java.lang.Exception _exception) {
                pathname.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "pathname")), java.lang.String.class);
            }
            try {
                retcode.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "retcode"));
            } catch (java.lang.Exception _exception) {
                retcode.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "retcode")), java.lang.String.class);
            }
        }
    }

    public java.lang.String create(java.lang.String pathname,
        java.lang.String filetype) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "create"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
            pathname, filetype });

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

    public java.lang.String rm(java.lang.String pathname)
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
        _call
            .setOperationName(new javax.xml.namespace.QName("urn:zibdms", "rm"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { pathname });

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

    public void existsEntity(java.lang.String pathname,
        javax.xml.rpc.holders.BooleanHolder result,
        javax.xml.rpc.holders.StringHolder ret) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "existsEntity"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { pathname });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                result.value = ((java.lang.Boolean) _output
                    .get(new javax.xml.namespace.QName("", "result")))
                    .booleanValue();
            } catch (java.lang.Exception _exception) {
                result.value = ((java.lang.Boolean) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "result")), boolean.class)).booleanValue();
            }
            try {
                ret.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "ret"));
            } catch (java.lang.Exception _exception) {
                ret.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "ret")), java.lang.String.class);
            }
        }
    }

    public void isFile(java.lang.String pathname,
        javax.xml.rpc.holders.BooleanHolder file,
        javax.xml.rpc.holders.StringHolder ret) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "isFile"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { pathname });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                file.value = ((java.lang.Boolean) _output
                    .get(new javax.xml.namespace.QName("", "file")))
                    .booleanValue();
            } catch (java.lang.Exception _exception) {
                file.value = ((java.lang.Boolean) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "file")), boolean.class)).booleanValue();
            }
            try {
                ret.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "ret"));
            } catch (java.lang.Exception _exception) {
                ret.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "ret")), java.lang.String.class);
            }
        }
    }

    public void isDirectory(java.lang.String pathname,
        javax.xml.rpc.holders.BooleanHolder directory,
        javax.xml.rpc.holders.StringHolder ret) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "isDirectory"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { pathname });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                directory.value = ((java.lang.Boolean) _output
                    .get(new javax.xml.namespace.QName("", "directory")))
                    .booleanValue();
            } catch (java.lang.Exception _exception) {
                directory.value = ((java.lang.Boolean) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "directory")), boolean.class)).booleanValue();
            }
            try {
                ret.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "ret"));
            } catch (java.lang.Exception _exception) {
                ret.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "ret")), java.lang.String.class);
            }
        }
    }

    public java.lang.String mkdir(java.lang.String pathname)
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
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "mkdir"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { pathname });

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

    public java.lang.String mkdirhier(java.lang.String pathname)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "mkdirhier"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { pathname });

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

    public java.lang.String rmdir(java.lang.String pathname)
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
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "rmdir"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { pathname });

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

    public void ls(java.lang.String pathname,
        zibdms_pkg.holders.StringArrayHolder directoryContent,
        javax.xml.rpc.holders.StringHolder retcode)
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
        _call
            .setOperationName(new javax.xml.namespace.QName("urn:zibdms", "ls"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { pathname });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                directoryContent.value = (zibdms_pkg.StringArray) _output
                    .get(new javax.xml.namespace.QName("", "directory-content"));
            } catch (java.lang.Exception _exception) {
                directoryContent.value = (zibdms_pkg.StringArray) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "directory-content")), zibdms_pkg.StringArray.class);
            }
            try {
                retcode.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "retcode"));
            } catch (java.lang.Exception _exception) {
                retcode.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "retcode")), java.lang.String.class);
            }
        }
    }

    public void lsX(java.lang.String pathname,
        zibdms_pkg.holders.PairOfStringBoolArrayHolder directoryContent,
        javax.xml.rpc.holders.StringHolder retcode)
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
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "lsX"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { pathname });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                directoryContent.value = (zibdms_pkg.PairOfStringBoolArray) _output
                    .get(new javax.xml.namespace.QName("", "directory-content"));
            } catch (java.lang.Exception _exception) {
                directoryContent.value = (zibdms_pkg.PairOfStringBoolArray) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "directory-content")),
                        zibdms_pkg.PairOfStringBoolArray.class);
            }
            try {
                retcode.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "retcode"));
            } catch (java.lang.Exception _exception) {
                retcode.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "retcode")), java.lang.String.class);
            }
        }
    }

    public java.lang.String addLocation(java.lang.String pathname,
        java.lang.String uri) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "addLocation"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
            pathname, uri });

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

    public java.lang.String removeLocation(java.lang.String pathname,
        java.lang.String uri) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "removeLocation"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
            pathname, uri });

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

    public void getLocations(java.lang.String pathname,
        zibdms_pkg.holders.StringArrayHolder locations,
        javax.xml.rpc.holders.StringHolder retcode)
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
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "getLocations"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { pathname });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                locations.value = (zibdms_pkg.StringArray) _output
                    .get(new javax.xml.namespace.QName("", "locations"));
            } catch (java.lang.Exception _exception) {
                locations.value = (zibdms_pkg.StringArray) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "locations")), zibdms_pkg.StringArray.class);
            }
            try {
                retcode.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "retcode"));
            } catch (java.lang.Exception _exception) {
                retcode.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "retcode")), java.lang.String.class);
            }
        }
    }

    public void getServiceDescription(java.lang.String in,
        javax.xml.rpc.holders.StringHolder description,
        javax.xml.rpc.holders.StringHolder ret) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "getServiceDescription"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { in });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        } else {
            extractAttachments(_call);
            java.util.Map _output;
            _output = _call.getOutputParams();
            try {
                description.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "description"));
            } catch (java.lang.Exception _exception) {
                description.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "description")), java.lang.String.class);
            }
            try {
                ret.value = (java.lang.String) _output
                    .get(new javax.xml.namespace.QName("", "ret"));
            } catch (java.lang.Exception _exception) {
                ret.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                    .convert(_output.get(new javax.xml.namespace.QName("",
                        "ret")), java.lang.String.class);
            }
        }
    }

    public java.lang.String replicateFileTo(java.lang.String pathname,
        java.lang.String url) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "replicateFileTo"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
            pathname, url });

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

    public java.lang.String fetchFileTo(java.lang.String pathname,
        java.lang.String url) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "fetchFileTo"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
            pathname, url });

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

    public java.lang.String replicateDirectoryTo(java.lang.String pathname,
        java.lang.String url) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[17]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "replicateDirectoryTo"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
            pathname, url });

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

    public java.lang.String fetchDirectoryTo(java.lang.String pathname,
        java.lang.String url) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[18]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:zibdms",
            "fetchDirectoryTo"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
            pathname, url });

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
