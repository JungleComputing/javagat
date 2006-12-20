package org.gridlab.gat.advert.cpi.globus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.rmi.RemoteException;
import java.lang.Byte;
import java.util.Vector;
import java.util.NoSuchElementException;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.globus.axis.util.Util;

import stubs.AdvertServiceEntryFactoryService.AddAdvertServiceEntry;
import stubs.AdvertServiceEntryFactoryService.AddAdvertServiceEntryResponse;
import stubs.AdvertServiceEntryFactoryService.AdvertServiceEntryFactoryPortType;
import stubs.AdvertServiceEntryFactoryService.service.AdvertServiceEntryFactoryServiceAddressingLocator;
import services.AdvertServiceEntry.impl.AdvertServiceEntryConstants;

import stubs.AdvertServiceEntryService.service.AdvertServiceEntryServiceAddressingLocator;
import stubs.AdvertServiceEntryService.AdvertServiceEntryPortType;

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
import org.oasis.wsrf.servicegroup.EntryType;
import org.oasis.wsrf.lifetime.Destroy;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.gridlab.gat.advert.MetaData;

class IndexService
{
    private static final String serviceURI = "https://127.0.0.2:8443/wsrf/services/GAT/AdvertServiceEntryFactoryService";
    private static final String indexURI = "https://127.0.0.2:8443/wsrf/services/DefaultIndexService";

    static
    {
	Util.registerTransport();
    }
    
    private MetaData toMetaData(MessageElement messageElement) throws Exception
    {
	Node metaDataNode = messageElement.getAsDOM();
	MetaData metaData = new MetaData();
	visitMetaDataNode(metaDataNode, metaData);
	return metaData;
    }

    private void visitMetaDataNode(Node metaDataNode, MetaData metaData)
    {
	NodeList metaDataEntryNodes = metaDataNode.getChildNodes();
        for(int i=0;i<metaDataEntryNodes.getLength();i++)
	    {
		Node metaDataEntryNode = metaDataEntryNodes.item(i);
		visitMetaDataEntryNode(metaDataEntryNode, metaData);
            }
    }
    
    private void visitMetaDataEntryNode(Node metaDataEntryNode, MetaData metaData)
    {
	NodeList children = metaDataEntryNode.getChildNodes();
	String key = getString(children.item(0));
	String value = getString(children.item(1));
	metaData.put(key, value);
    }

    private String getString(Node node)
    {
	String str = node.getChildNodes().item(0).getNodeValue();
	return str;
    }

    public Entry get(String path) throws Exception
    {
	String xpathQuery = "//*[local-name()='Entry'][./*/*/*[local-name()='Path']/text()='" + path + "']";
	EntryType[] entryTypes = queryIndexService(xpathQuery);
	if(entryTypes.length != 1)
	    return null;
	
	EntryType entryType = entryTypes[0];
	Entry entry = new Entry();
	AggregatorContent content = (AggregatorContent) entryType.getContent();
	AggregatorData data = content.getAggregatorData();
	MessageElement[] dataEntries = data.get_any();
	Vector bytes = new Vector();
	for(int i=0;i<dataEntries.length;i++)
	    {
		MessageElement messageElement = dataEntries[i];
		String name = messageElement.getName();
		if(name.equals("Path"))
		    entry.path = messageElement.getValue();
		else if(name.equals("MetaData"))
		    entry.metaData = toMetaData(messageElement);
		else if(name.equals("SerializedAdvertisable"))
		    bytes.add(new Byte(messageElement.getValue()));
	    }
	
	int size = bytes.size();
	byte[] serializedAdvertisable = new byte[size];
	for(int i=0;i<size;i++)
	    serializedAdvertisable[i] = ((Byte)bytes.get(i)).byteValue();
	
	entry.serializedAdvertisable = new String(serializedAdvertisable);
	return entry;
    }
    
    private void update(Entry entry) throws Exception
    {
	String xpathQuery = "//*[local-name()='Entry'][./*/*/*[local-name()='Path']/text()='" + entry.path + "']";
	EntryType[] entryTypes = queryIndexService(xpathQuery);
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
		advertServiceEntry.setMetaData(MetaDataUtils.toAdvertServiceEntryServiceMetaDataType(entry.metaData));
		advertServiceEntry.setSerializedAdvertisable(entry.serializedAdvertisable.getBytes());
	    }
	catch(RemoteException e)
	    {
		System.err.println("ERROR: Unable to invoke setMetaData- or setSerializedAdvertisable-operation");
		e.printStackTrace();
		throw new Exception("ERROR: Unable to invoke setMetaData- or setSerializedAdvertisable-operation", e);
	    }
    }

    public void put(Entry entry) throws Exception
    {
	Entry oldEntry = get(entry.path);
	if(oldEntry != null)
	    {
		update(entry);
		return;
	    }

	// Create EPR
	EndpointReferenceType factoryEPR = new EndpointReferenceType();
	try
	    {
		factoryEPR.setAddress(new Address(serviceURI));
	    }
	catch(Exception e)
	    {
		System.err.println("ERROR: Malformed URI '" + serviceURI + "'");
		e.printStackTrace();
		throw new Exception("ERROR: Malformed URI '" + serviceURI + "'", e);
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
	addAdvertServiceEntryRequest.setPath(entry.path);
	addAdvertServiceEntryRequest.setMetaData(MetaDataUtils.toAdvertServiceEntryFactoryServiceMetaDataType(entry.metaData));
	addAdvertServiceEntryRequest.setSerializedAdvertisable(entry.serializedAdvertisable.getBytes());

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
    }

    public void remove(String path) throws Exception
    {
	String xpathQuery = "//*[local-name()='Entry'][./*/*/*[local-name()='Path']/text()='" + path + "']";
	EntryType[] entryTypes = queryIndexService(xpathQuery);
	if(entryTypes.length == 0)
	    throw new NoSuchElementException("no entry found");
	if(entryTypes.length != 1)
	    throw new NoSuchElementException("more than one entry found");
	
	EndpointReferenceType memberServiceEPR = entryTypes[0].getMemberServiceEPR();
	remove(memberServiceEPR);
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
    
    public Entry[] getEntries() throws Exception
    {
	String xpathQuery = "//*[local-name()='Entry'][./*/*/*[local-name()='Path']]";
	EntryType[] entryTypes = queryIndexService(xpathQuery);
	Entry[] entries = new Entry[entryTypes.length];
	for(int j=0;j<entryTypes.length;j++)
	    {
		EntryType entryType = entryTypes[j];
		Entry entry = new Entry();
		AggregatorContent content = (AggregatorContent) entryType.getContent();
		AggregatorData data = content.getAggregatorData();
		MessageElement[] dataEntries = data.get_any();
		Vector bytes = new Vector();
		for(int i=0;i<dataEntries.length;i++)
		    {
			MessageElement messageElement = dataEntries[i];
			String name = messageElement.getName();
			if(name.equals("Path"))
			    entry.path = messageElement.getValue();
			else if(name.equals("MetaData"))
			    entry.metaData = toMetaData(messageElement);
			else if(name.equals("SerializedAdvertisable"))
			    bytes.add(new Byte(messageElement.getValue()));
		    }
		
		int size = bytes.size();
		byte[] serializedAdvertisable = new byte[size];
		for(int i=0;i<size;i++)
		    serializedAdvertisable[i] = ((Byte)bytes.get(i)).byteValue();
		
		entry.serializedAdvertisable = new String(serializedAdvertisable);
		entries[j] = entry;
	    }
	
	return entries;
    }

    protected EntryType[] queryIndexService(String xpathQuery) throws Exception
    {
	/*
	 * QUERY INDEX SERVICE
	 */
	
	EndpointReferenceType indexEPR = new EndpointReferenceType();
	try
	    {
		indexEPR.setAddress(new Address(indexURI));
	    }
	catch(Exception e)
	    {
		System.err.println("ERROR: Malformed index URI '" + indexURI + "'");
		e.printStackTrace();
		throw new Exception("ERROR: Malformed index URI '" + indexURI + "'", e);
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
	    {
		System.err.println("no entries found.");
		return new EntryType[0];
	    }

	EntryType[] entryTypes = new EntryType[entries.length];
	for(int i=0;i<entries.length;i++)
	    {
		// We know that there is at least one entry with the specified path.
		try
		    {
			// Access information contained in the entry. First of all,
			// we need to deserialize the entry...
			entryTypes[i] = (EntryType) ObjectDeserializer.toObject(entries[i], EntryType.class);
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
}
