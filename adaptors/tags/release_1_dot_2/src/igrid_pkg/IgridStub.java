/**
 * IgridStub.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis WSDL2Java emitter.
 */

package igrid_pkg;

public class IgridStub extends org.apache.axis.client.Stub implements
        igrid_pkg.IgridPortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();

    private java.util.Vector cachedSerQNames = new java.util.Vector();

    private java.util.Vector cachedSerFactories = new java.util.Vector();

    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc[] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[29];
        org.apache.axis.description.OperationDesc oper;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("register-webservice");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "wsdllocation"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "description"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "url"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "keywords"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "validity-time"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("unregister-webservice");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "url"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "allinst"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("istore");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "data"),
            new javax.xml.namespace.QName("urn:igrid", "dimeData"),
            igrid_pkg.DimeData.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "filter"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("update-webservice");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "keywords"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "description"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "url"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "validity-time"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("register-service");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "hostname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "port"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "protocol"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "dport"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "description"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "keywords"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "validity-time"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("unregister-service");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "hostname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "port"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "allinst"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("update-service");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "keywords"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "description"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "url"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "dport"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "validity-time"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup-webservice");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "hostname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup-service");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "hostname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "dport"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "sport"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "prot"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[9] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup-firewall");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup-system");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "system"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup-user");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup-vo");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup-ca");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup-lrms");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup-cpu");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "mhz"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "cache"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "number"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "load"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "float"), float.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[16] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup-memory");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "total-ram"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "total-swap"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "free-ram"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "free-swap"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[17] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup-network");
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "address"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[18] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup-device");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "device"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[19] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("register-vo");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "helpDeskPN"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "restype"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "jobm"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "queue"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "fs-path"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "helpDeskURL"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "adminname"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "host"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "validity-time"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[20] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("unregister-vo");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "host"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "allinst"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[21] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("update-vo");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "restype"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "jobm"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "queue"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "helpDeskURL"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "adminname"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "host"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "validity-time"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[22] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("register-firewall");
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "hostname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "ports"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "admindn"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "host"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "validity-time"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[23] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("unregister-firewall");
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "hostname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[24] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("update-firewall");
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "hostname"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "admindn"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "ports"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "type-por"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid",
            "validity-time"), new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[25] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("register-wsdlloc");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "wsdlloc"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[26] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("unregister-wsdlloc");
        oper.addParameter(new javax.xml.namespace.QName("urn:igrid", "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(
            new javax.xml.namespace.QName("urn:igrid", "wsdlloc"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[27] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getServiceDescription");
        oper.setReturnType(new javax.xml.namespace.QName(
            "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper
            .setReturnQName(new javax.xml.namespace.QName("urn:igrid", "result"));
        oper.setStyle(org.apache.axis.enum.Style.WRAPPED);
        oper.setUse(org.apache.axis.enum.Use.LITERAL);
        _operations[28] = oper;

    }

    public IgridStub() throws org.apache.axis.AxisFault {
        this(null);
    }

    public IgridStub(java.net.URL endpointURL, javax.xml.rpc.Service service)
        throws org.apache.axis.AxisFault {
        this(service);
        super.cachedEndpoint = endpointURL;
    }

    public IgridStub(javax.xml.rpc.Service service)
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
        qName = new javax.xml.namespace.QName("urn:igrid", "dimeData");
        cachedSerQNames.add(qName);
        cls = igrid_pkg.DimeData.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(simplesf);
        cachedDeserFactories.add(simpledf);

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
                    _call.setEncodingStyle(null);
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

    public int registerWebservice(java.lang.String name,
        java.lang.String wsdllocation, java.lang.String description,
        java.lang.String url, java.lang.String keywords, int validityTime)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "register-webservice"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            wsdllocation, description, url, keywords,
            new java.lang.Integer(validityTime) });

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

    public int unregisterWebservice(java.lang.String name,
        java.lang.String url, int allinst) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "unregister-webservice"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            url, new java.lang.Integer(allinst) });

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

    public int istore(igrid_pkg.DimeData data) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "istore"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { data });

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

    public java.lang.String search(java.lang.String filter)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "search"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { filter });

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

    public int updateWebservice(java.lang.String name,
        java.lang.String keywords, java.lang.String description,
        java.lang.String url, int validityTime) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "update-webservice"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            keywords, description, url, new java.lang.Integer(validityTime) });

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

    public int registerService(java.lang.String name,
        java.lang.String hostname, int port, java.lang.String protocol,
        int dport, java.lang.String description, java.lang.String keywords,
        int validityTime) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "register-service"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            hostname, new java.lang.Integer(port), protocol,
            new java.lang.Integer(dport), description, keywords,
            new java.lang.Integer(validityTime) });

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

    public int unregisterService(java.lang.String name,
        java.lang.String hostname, int port, int allinst)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "unregister-service"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            hostname, new java.lang.Integer(port),
            new java.lang.Integer(allinst) });

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

    public int updateService(java.lang.String name, java.lang.String keywords,
        java.lang.String description, java.lang.String url, int dport,
        int validityTime) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "update-service"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            keywords, description, url, new java.lang.Integer(dport),
            new java.lang.Integer(validityTime) });

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

    public java.lang.String lookupWebservice(java.lang.String name,
        java.lang.String hostname) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "lookup-webservice"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            hostname });

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

    public java.lang.String lookupService(java.lang.String name,
        java.lang.String hostname, int dport, int sport, java.lang.String prot)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "lookup-service"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            hostname, new java.lang.Integer(dport),
            new java.lang.Integer(sport), prot });

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

    public java.lang.String lookupFirewall(java.lang.String name)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "lookup-firewall"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name });

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

    public java.lang.String lookupSystem(java.lang.String name,
        java.lang.String system) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "lookup-system"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            system });

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

    public java.lang.String lookupUser(java.lang.String name)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "lookup-user"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name });

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

    public java.lang.String lookupVo(java.lang.String name)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "lookup-vo"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name });

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

    public java.lang.String lookupCa(java.lang.String name)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "lookup-ca"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name });

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

    public java.lang.String lookupLrms(java.lang.String name)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "lookup-lrms"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name });

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

    public java.lang.String lookupCpu(int mhz, int cache, int number, float load)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "lookup-cpu"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
            new java.lang.Integer(mhz), new java.lang.Integer(cache),
            new java.lang.Integer(number), new java.lang.Float(load) });

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

    public java.lang.String lookupMemory(int totalRam, int totalSwap,
        int freeRam, int freeSwap) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[17]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "lookup-memory"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
            new java.lang.Integer(totalRam), new java.lang.Integer(totalSwap),
            new java.lang.Integer(freeRam), new java.lang.Integer(freeSwap) });

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

    public java.lang.String lookupNetwork(java.lang.String address)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[18]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "lookup-network"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { address });

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

    public java.lang.String lookupDevice(java.lang.String device)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[19]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "lookup-device"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { device });

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

    public int registerVo(java.lang.String name, java.lang.String helpDeskPN,
        java.lang.String restype, java.lang.String jobm,
        java.lang.String queue, java.lang.String fsPath,
        java.lang.String helpDeskURL, java.lang.String adminname,
        java.lang.String host, int validityTime)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[20]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "register-vo"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            helpDeskPN, restype, jobm, queue, fsPath, helpDeskURL, adminname,
            host, new java.lang.Integer(validityTime) });

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

    public int unregisterVo(java.lang.String name, java.lang.String host,
        int allinst) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[21]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "unregister-vo"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            host, new java.lang.Integer(allinst) });

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

    public int updateVo(java.lang.String name, java.lang.String restype,
        java.lang.String jobm, java.lang.String queue,
        java.lang.String helpDeskURL, java.lang.String adminname,
        java.lang.String host, int validityTime)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[22]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "update-vo"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            restype, jobm, queue, helpDeskURL, adminname, host,
            new java.lang.Integer(validityTime) });

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

    public int registerFirewall(java.lang.String hostname,
        java.lang.String ports, java.lang.String admindn,
        java.lang.String host, int validityTime)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[23]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "register-firewall"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { hostname, ports, admindn, host,
                new java.lang.Integer(validityTime) });

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

    public int unregisterFirewall(java.lang.String hostname)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[24]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "unregister-firewall"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call
            .invoke(new java.lang.Object[] { hostname });

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

    public int updateFirewall(java.lang.String hostname,
        java.lang.String admindn, java.lang.String ports, int typePor,
        int validityTime) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[25]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "update-firewall"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
            hostname, admindn, ports, new java.lang.Integer(typePor),
            new java.lang.Integer(validityTime) });

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

    public int registerWsdlloc(java.lang.String name, java.lang.String wsdlloc)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[26]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "register-wsdlloc"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            wsdlloc });

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

    public int unregisterWsdlloc(java.lang.String name, java.lang.String wsdlloc)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[27]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "unregister-wsdlloc"));

        setRequestHeaders(_call);
        setAttachments(_call);
        java.lang.Object _resp = _call.invoke(new java.lang.Object[] { name,
            wsdlloc });

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
        _call.setOperation(_operations[28]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call
            .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:igrid",
            "getServiceDescription"));

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
