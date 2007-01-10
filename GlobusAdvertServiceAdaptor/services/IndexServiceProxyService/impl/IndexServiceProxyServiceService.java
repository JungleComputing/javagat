package services.IndexServiceProxyService.impl;

import java.rmi.RemoteException;
import java.lang.Byte;
import java.util.Vector;

import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceContextException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.globus.axis.util.Util;

import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;
import org.globus.wsrf.security.Constants;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.globus.mds.aggregator.types.AggregatorContent;
import org.globus.mds.aggregator.types.AggregatorData;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceContextException;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.security.SecurityManager;
import org.globus.wsrf.utils.AddressingUtils;
import org.oasis.wsrf.properties.QueryExpressionType;
import org.oasis.wsrf.properties.QueryResourcePropertiesResponse;
import org.oasis.wsrf.properties.QueryResourceProperties_Element;
import org.oasis.wsrf.properties.QueryResourceProperties_PortType;
import org.oasis.wsrf.properties.WSResourcePropertiesServiceAddressingLocator;
//import org.oasis.wsrf.servicegroup.EntryType;
import org.oasis.wsrf.lifetime.Destroy;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import stubs.AdvertServiceEntryFactoryService.AddAdvertServiceEntry;
import stubs.AdvertServiceEntryFactoryService.AddAdvertServiceEntryResponse;
import stubs.AdvertServiceEntryFactoryService.AdvertServiceEntryFactoryPortType;
import stubs.AdvertServiceEntryFactoryService.service.AdvertServiceEntryFactoryServiceAddressingLocator;
import services.AdvertServiceEntry.impl.AdvertServiceEntryConstants;

import stubs.AdvertServiceEntryService.service.AdvertServiceEntryServiceAddressingLocator;
import stubs.AdvertServiceEntryService.AdvertServiceEntryPortType;

import stubs.IndexServiceProxyService.*;
import stubs.GAT.*;

import java.util.*;

public class IndexServiceProxyServiceService
{
    /* Added for logging */
    static final Log logger = LogFactory.getLog(IndexServiceProxyServiceService.class);
    private String indexURI = "http://127.0.0.2:8443/wsrf/services/DefaultIndexService";
    private String serviceURI;
	
    public IndexServiceProxyServiceService()
    {
	this.serviceURI = this.indexURI.substring(0, this.indexURI.lastIndexOf("/") + 1) + "GAT/AdvertServiceEntryFactoryService";
    }

    private void update(Entry entry) throws Exception
    {
	String xpathQuery = "//*[local-name()='Entry'][./*/*/*[local-name()='Path']/text()='" + entry.getPath() + "']";
	org.oasis.wsrf.servicegroup.EntryType[] entryTypes = queryIndexService(xpathQuery);
	if(entryTypes.length == 0)
	    throw new Exception("no entry for update");
        if(entryTypes.length != 1)
	    throw new Exception("more than one entry found for update");
	
	EndpointReferenceType memberServiceEPR = entryTypes[0].getMemberServiceEPR();
	AdvertServiceEntryServiceAddressingLocator advertServiceEntryLocator = new AdvertServiceEntryServiceAddressingLocator();
	AdvertServiceEntryPortType advertServiceEntry = null;
	
	try
	    {
		advertServiceEntry = advertServiceEntryLocator.getAdvertServiceEntryPortTypePort(memberServiceEPR);
	    }
	catch(ServiceException e)
	    {
		System.err.println("ERROR: Unable to obtain portType");
		e.printStackTrace();
		throw new Exception("ERROR: Unable to obtain portType", e);
	    }
	
	// Setup security options
	// klappt nicht wegen Versionskonflikt in
	// /usr/local/src/JavaGAT-1.7/adaptors/external/ cog-jglobus.jar und cog-jglobus-1.2.jar
	//((Stub) advertServiceEntry)._setProperty(Constants.GSI_TRANSPORT, Constants.SIGNATURE);
	//((Stub) advertServiceEntry)._setProperty(Constants.AUTHORIZATION, NoAuthorization.getInstance());

	try
	    {
		advertServiceEntry.setMetaData(entry.getMetaData());
		advertServiceEntry.setSerializedAdvertisable(entry.getSerializedAdvertisable());
	    }
	catch(RemoteException e)
	    {
		System.err.println("ERROR: Unable to invoke setMetaData- or setSerializedAdvertisable-operation");
		e.printStackTrace();
		throw new Exception("ERROR: Unable to invoke setMetaData- or setSerializedAdvertisable-operation", e);
	    }
    }

    public PutResponse put(Entry entry) throws Exception
    {
	Entry oldEntry = get(entry.getPath());
	if(oldEntry != null)
	    {
		update(entry);
		return new PutResponse();
	    }

	// Create EPR
	EndpointReferenceType factoryEPR = new EndpointReferenceType();
	try
	    {
		factoryEPR.setAddress(new Address(this.serviceURI));
	    }
	catch(Exception e)
	    {
		System.err.println("ERROR: Malformed URI '" + this.serviceURI + "'");
		e.printStackTrace();
		throw new Exception("ERROR: Malformed URI '" + this.serviceURI + "'", e);
	    }
	
	// Get portType
	AdvertServiceEntryFactoryServiceAddressingLocator factoryLocator = new AdvertServiceEntryFactoryServiceAddressingLocator();
	AdvertServiceEntryFactoryPortType factory = null;
	
	try
	    {
		factory = factoryLocator.getAdvertServiceEntryFactoryPortTypePort(factoryEPR);
	    }
	catch(ServiceException e)
	    {
		System.err.println("ERROR: Unable to obtain portType");
		e.printStackTrace();
		throw new Exception("ERROR: Unable to obtain portType", e);
	    }
	
	// Setup security options
	//((Stub) factory)._setProperty(Constants.GSI_TRANSPORT, Constants.SIGNATURE);
	//((Stub) factory)._setProperty(Constants.AUTHORIZATION, NoAuthorization.getInstance());
	
	/* Invoke addAdvertServiceEntry operation */

	AddAdvertServiceEntry addAdvertServiceEntryRequest = new AddAdvertServiceEntry();
	addAdvertServiceEntryRequest.setPath(entry.getPath());
	addAdvertServiceEntryRequest.setMetaData(entry.getMetaData());
	addAdvertServiceEntryRequest.setSerializedAdvertisable(entry.getSerializedAdvertisable());

	// Perform invocation
	AddAdvertServiceEntryResponse addAdvertServiceEntryResponse = null;
	try
	    {
		addAdvertServiceEntryResponse = factory.addAdvertServiceEntry(addAdvertServiceEntryRequest);
	    }
	catch(RemoteException e)
	    {
		System.err.println("ERROR: Unable to invoke addAdvertServiceEntry operation");
		e.printStackTrace();
		throw new Exception("ERROR: Unable to invoke addAdvertServiceEntry operation", e);
	    }

	return new PutResponse();
    }

    public RemoveResponse remove(String path) throws Exception
    {
	String xpathQuery = "//*[local-name()='Entry'][./*/*/*[local-name()='Path']/text()='" + path + "']";
	org.oasis.wsrf.servicegroup.EntryType[] entryTypes = queryIndexService(xpathQuery);
	if(entryTypes.length == 0)
	    throw new Exception("no entry found");
	if(entryTypes.length != 1)
	    throw new Exception("more than one entry found");
	
	EndpointReferenceType memberServiceEPR = entryTypes[0].getMemberServiceEPR();
	remove(memberServiceEPR);
	return new RemoveResponse();
    }

    protected void remove(EndpointReferenceType epr) throws Exception
    {
	AdvertServiceEntryServiceAddressingLocator advertServiceEntryLocator = new AdvertServiceEntryServiceAddressingLocator();
	AdvertServiceEntryPortType advertServiceEntry = null;
	
	try
	    {
		advertServiceEntry = advertServiceEntryLocator.getAdvertServiceEntryPortTypePort(epr);
	    }
	catch(ServiceException e)
	    {
		System.err.println("ERROR: Unable to obtain portType");
		e.printStackTrace();
		throw new Exception("ERROR: Unable to obtain portType", e);
	    }
	
	// Setup security options
	//((Stub) advertServiceEntry)._setProperty(Constants.GSI_TRANSPORT, Constants.SIGNATURE);
	//((Stub) advertServiceEntry)._setProperty(Constants.AUTHORIZATION, NoAuthorization.getInstance());

	try
	    {
		advertServiceEntry.destroy(new Destroy());
	    }
	catch(RemoteException e)
	    {
		System.err.println("ERROR: Unable to invoke destroy operation");
		e.printStackTrace();
		throw new Exception("ERROR: Unable to invoke destroy operation", e);
	    }
    }

    public Entries getEntries(GetEntries dummy) throws Exception
    {
	String xpathQuery = "//*[local-name()='Entry'][./*/*/*[local-name()='Path']]";
	org.oasis.wsrf.servicegroup.EntryType[] entryTypes = queryIndexService(xpathQuery);
	Entry[] entries = new Entry[entryTypes.length];
	for(int j=0;j<entryTypes.length;j++)
	    entries[j] = toEntry(entryTypes[j]);
	
	return new Entries(entries);
    }

    private MetaData toMetaData(MessageElement messageElement) throws Exception
    {
	Node metaDataNode = messageElement.getAsDOM();
	List<MetaDataEntry> metaDataEntries = new Vector<MetaDataEntry>();
	visitMetaDataNode(metaDataNode, metaDataEntries);
	return new MetaData(metaDataEntries.toArray(new MetaDataEntry[0]));
    }

    private void visitMetaDataNode(Node metaDataNode, List<MetaDataEntry> metaDataEntries)
    {
	NodeList metaDataEntryNodes = metaDataNode.getChildNodes();
        for(int i=0;i<metaDataEntryNodes.getLength();i++)
	    {
		Node metaDataEntryNode = metaDataEntryNodes.item(i);
		visitMetaDataEntryNode(metaDataEntryNode, metaDataEntries);
            }
    }
    
    private void visitMetaDataEntryNode(Node metaDataEntryNode, List<MetaDataEntry> metaDataEntries)
    {
	NodeList children = metaDataEntryNode.getChildNodes();
	String key = getString(children.item(0));
	String value = getString(children.item(1));
	MetaDataEntry entry = new MetaDataEntry();
	entry.setKey(key);
	entry.setValue(value);
	metaDataEntries.add(entry);
    }

    private String getString(Node node)
    {
	String str = node.getChildNodes().item(0).getNodeValue();
	return str;
    }

    private Entry toEntry(org.oasis.wsrf.servicegroup.EntryType entryType) throws Exception
    {
	String path = null;
	MetaData metaData = null;
	AggregatorContent content = (AggregatorContent) entryType.getContent();
	AggregatorData data = content.getAggregatorData();
	MessageElement[] dataEntries = data.get_any();
	Vector bytes = new Vector();
	for(int i=0;i<dataEntries.length;i++)
	    {
		MessageElement messageElement = dataEntries[i];
		String name = messageElement.getName();
		if(name.equals("Path"))
		    path = messageElement.getValue();
		else if(name.equals("MetaData"))
		    metaData = toMetaData(messageElement);
		else if(name.equals("SerializedAdvertisable"))
		    bytes.add(new Byte(messageElement.getValue()));
	    }
	
	int size = bytes.size();
	byte[] serializedAdvertisable = new byte[size];
	for(int i=0;i<size;i++)
	    serializedAdvertisable[i] = ((Byte)bytes.get(i)).byteValue();
	
	Entry result = new Entry();
	result.setPath(path);
	result.setMetaData(metaData);
	result.setSerializedAdvertisable(new String(serializedAdvertisable, "UTF8"));
	return result;
    }
    
    public Entry get(String path) throws Exception
    {
	String xpathQuery = "//*[local-name()='Entry'][./*/*/*[local-name()='Path']/text()='" + path + "']";
	org.oasis.wsrf.servicegroup.EntryType[] entryTypes = queryIndexService(xpathQuery);
	if(entryTypes.length != 1)
	    return null;
	else
	    return toEntry(entryTypes[0]);
    }

    protected org.oasis.wsrf.servicegroup.EntryType[] queryIndexService(String xpathQuery) throws Exception
    {
	/*
	 * QUERY INDEX SERVICE
	 */
	
	EndpointReferenceType indexEPR = new EndpointReferenceType();
	try
	    {
		indexEPR.setAddress(new Address(this.indexURI));
	    }
	catch(Exception e)
	    {
		System.err.println("ERROR: Malformed index URI '" + this.indexURI + "'");
		e.printStackTrace();
		throw new Exception("ERROR: Malformed index URI '" + this.indexURI + "'", e);
	    }
	
	// Get QueryResourceProperties portType
	WSResourcePropertiesServiceAddressingLocator queryLocator = new WSResourcePropertiesServiceAddressingLocator();
	QueryResourceProperties_PortType query = null;
	try
	    {
		query = queryLocator.getQueryResourcePropertiesPort(indexEPR);
	    }
	catch(ServiceException e)
	    {
		System.err.println("ERROR: Unable to obtain query portType");
		e.printStackTrace();
		throw new Exception("ERROR: Unable to obtain query portType", e);
	    }
	
	// Setup security options
	//((Stub) query)._setProperty(Constants.GSI_TRANSPORT, Constants.SIGNATURE);
	//((Stub) query)._setProperty(Constants.AUTHORIZATION, NoAuthorization.getInstance());
	
	// Create request to QueryResourceProperties
	QueryExpressionType queryExpr = new QueryExpressionType();
	try
	    {
		queryExpr.setDialect(new URI(WSRFConstants.XPATH_1_DIALECT));
	    }
	catch(Exception e)
	    {
		System.err.println("ERROR: Malformed URI (WSRFConstants.XPATH_1_DIALECT)");
		e.printStackTrace();
		throw new Exception("ERROR: Malformed URI (WSRFConstants.XPATH_1_DIALECT)", e);
	    }
	
	queryExpr.setValue(xpathQuery);
	QueryResourceProperties_Element queryRequest = new QueryResourceProperties_Element(queryExpr);
	
	// Invoke QueryResourceProperties
	QueryResourcePropertiesResponse queryResponse = null;
	try
	    {
		queryResponse = query.queryResourceProperties(queryRequest);
	    }
	catch(RemoteException e)
	    {
		System.err.println("ERROR: Unable to invoke QueryRP operation");
		e.printStackTrace();
		throw new Exception("ERROR: Unable to invoke QueryRP operation", e);
	    }
	
	// The response includes 0 or more entries from the index service.
	MessageElement[] entries = queryResponse.get_any();
	
	if(entries == null)
	    return new org.oasis.wsrf.servicegroup.EntryType[0];

	org.oasis.wsrf.servicegroup.EntryType[] entryTypes = new org.oasis.wsrf.servicegroup.EntryType[entries.length];
	for(int i=0;i<entries.length;i++)
	    {
		// We know that there is at least one entry with the specified path.
		try
		    {
			// Access information contained in the entry. First of all,
			// we need to deserialize the entry...
			entryTypes[i] = (org.oasis.wsrf.servicegroup.EntryType) ObjectDeserializer.toObject(entries[i], org.oasis.wsrf.servicegroup.EntryType.class);
		    }
		catch(Exception e)
		    {
			System.err.println("Error when accessing index service entry");
			e.printStackTrace();
			throw new Exception("Error when accessing index service entry", e);
		    }
	    }
	
	return entryTypes;
    }

    /*
     * Private method that gets a reference to the resource specified in the
     * endpoint reference.
     */
    private IndexServiceProxyServiceResource getResource() throws RemoteException
    {
	Object resource = null;
	try
	    {
		resource = ResourceContext.getResourceContext().getResource();
	    }
	catch(NoSuchResourceException e)
	    {
		throw new RemoteException("Specified resource does not exist", e);
	    }
	catch(ResourceContextException e)
	    {
		throw new RemoteException("Error during resource lookup", e);
	    }
	catch(Exception e)
	    {
		throw new RemoteException("", e);
	    }
	
	return ((IndexServiceProxyServiceResource)resource);
    }
}
