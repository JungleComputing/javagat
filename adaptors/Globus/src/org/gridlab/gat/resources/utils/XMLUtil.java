package org.gridlab.gat.resources.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.cpi.wsgt4new.WSGT4newResourceBrokerAdaptor;
import org.gridlab.gat.resources.gt4.GlobusHardwareResource;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtil {

	private Element rootElement;

	private Document document;

	private Element Host;

	private Element CPU;

	private Element Memory;

	private Element OS;

	private Element Disk;

	private Element Network;

	private Element PrLoad;


	public  void createXMLDocument(org.w3c.dom.NodeList hostsList) {
		rootElement = new Element("ROOT");
		document = new Document(rootElement);
	
		for (int i = 0; i < hostsList.getLength(); i++) {
			Node host = hostsList.item(i);
			Node hostName = host.getAttributes().getNamedItem("ns1:Name");
			Host = new Element("HOST");
			Host.setAttribute("NAME", hostName.getTextContent());
			rootElement.addContent(Host);

			NodeList hostParameters = host.getChildNodes();
			Node CPU_Speed = hostParameters.item(0).getAttributes()
					.getNamedItem("ns1:ClockSpeed");
			Node CPU_Count = hostParameters.item(3).getAttributes()
					.getNamedItem("ns1:SMPSize");
			Node Cache_L1 = hostParameters.item(0).getAttributes()
					.getNamedItem("ns1:CacheL1");
			Node Cache_L1D = hostParameters.item(0).getAttributes()
					.getNamedItem("ns1:CacheL1D");
			Node Cache_L1I = hostParameters.item(0).getAttributes()
					.getNamedItem("ns1:CacheL1I");
			Node CacheL2 = hostParameters.item(0).getAttributes().getNamedItem(
					"ns1:CacheL2");

			CPU = new Element("CPU");
			CPU.setAttribute("cpu.speed", CPU_Speed.getTextContent());
			CPU.setAttribute("cpu.count", CPU_Count.getTextContent());
			CPU.setAttribute("cpu.cache.l1", Cache_L1.getTextContent());
			CPU.setAttribute("cpu.cache.l1d", Cache_L1D.getTextContent());
			CPU.setAttribute("cpu.cache.l1i", Cache_L1I.getTextContent());
			CPU.setAttribute("cpu.cache.l2", CacheL2.getTextContent());

			Node Memory_Size = hostParameters.item(1).getAttributes()
					.getNamedItem("ns1:RAMSize");
			Node Memory_Available = hostParameters.item(1).getAttributes()
					.getNamedItem("ns1:RAMAvailable");
			Node Virtual_Available_Memory = hostParameters.item(1)
					.getAttributes().getNamedItem("ns1:VirtualAvailable");
			Node Virtual_Size = hostParameters.item(1).getAttributes()
					.getNamedItem("ns1:VirtualSize");

			Memory = new Element("MEMORY");
			Memory.setAttribute("memory.size", Memory_Size.getTextContent());
			Memory.setAttribute("memory.size.available", Memory_Available
					  		.getTextContent());
			Memory.setAttribute("memory.virtual.size.available",
					Virtual_Available_Memory.getTextContent());
			Memory.setAttribute("memory.virtual.size", Virtual_Size
					.getTextContent());

			Node OS_Name = hostParameters.item(2).getAttributes().getNamedItem(
					"ns1:Name");
			Node OS_Release = hostParameters.item(2).getAttributes()
					.getNamedItem("ns1:Release");
			Node OS_Type = hostParameters.item(0).getAttributes().getNamedItem(
					"ns1:InstructionSet");

			OS = new Element("OS");
			OS.setAttribute("os.name", OS_Name.getTextContent());
			OS.setAttribute("os.release", OS_Release.getTextContent());
			OS.setAttribute("os.type", OS_Type.getTextContent());

			Node Disk_Size = hostParameters.item(4).getAttributes()
					.getNamedItem("ns1:Size");
			Node Available_Disk_Size = hostParameters.item(4).getAttributes()
					.getNamedItem("ns1:AvailableSpace");
			Node Read_Only_Disk = hostParameters.item(4).getAttributes()
					.getNamedItem("ns1:ReadOnly");
			Node Disk_Root = hostParameters.item(4).getAttributes()
					.getNamedItem("ns1:Root");

			Disk = new Element("DISK");
			Disk.setAttribute("disk.size", Disk_Size.getTextContent());
			Disk.setAttribute("disk.size.available", Available_Disk_Size
					.getTextContent());
			Disk.setAttribute("disk.readonly", Read_Only_Disk
							.getTextContent());
			Disk.setAttribute("disk.root", Disk_Root.getTextContent());

			Node IP_Address = hostParameters.item(5).getAttributes()
					.getNamedItem("ns1:IPAddress");
			Node Inbound_IP = hostParameters.item(5).getAttributes()
					.getNamedItem("ns1:InboundIP");
			Node Outbound_IP = hostParameters.item(5).getAttributes()
					.getNamedItem("ns1:OutboundIP");
			Node MTU = hostParameters.item(5).getAttributes().getNamedItem(
					"ns1:MTU");

			Network = new Element("NETWORK");
			Network.setAttribute("network.ip", IP_Address.getTextContent());
			Network.setAttribute("network.inboundip", Inbound_IP.getTextContent());
			Network.setAttribute("network.outboundip", Outbound_IP.getTextContent());
			Network.setAttribute("network.mtu", MTU.getTextContent());

			Node prLoad1Min = hostParameters.item(6).getAttributes()
					.getNamedItem("ns1:Last1Min");
			Node prLoad5Min = hostParameters.item(6).getAttributes()
					.getNamedItem("ns1:Last5Min");
			Node prLoad15Min = hostParameters.item(6).getAttributes()
					.getNamedItem("ns1:Last15Min");

			PrLoad = new Element("PROCESSOR_LOAD");
			PrLoad.setAttribute("processor.load.1min", prLoad1Min
					.getTextContent());
			PrLoad.setAttribute("processor.load.5min", prLoad5Min
					.getTextContent());
			PrLoad.setAttribute("processor.load.15min", prLoad15Min
					.getTextContent());

			Host.addContent(CPU);
			Host.addContent(Memory);
			Host.addContent(OS);
			Host.addContent(Disk);
			Host.addContent(Network);
			Host.addContent(PrLoad);

		}/*
			 * XMLOutputter outputter=new XMLOutputter(); try {
			 * outputter.output(document, new FileOutputStream(path)); } catch
			 * (FileNotFoundException e1) { // TODO Auto-generated catch block
			 * e1.printStackTrace(); } catch (IOException e1) { // TODO
			 * Auto-generated catch block e1.printStackTrace(); }
			 */
	}

	public LinkedList<HardwareResource> matchResources(Map description, WSGT4newResourceBrokerAdaptor broker) {

		LinkedList<HardwareResource> matchedResources = new LinkedList();
		int number_of_resources = description.size();

		/*
		 * try { Document document= saxBuilder.build(path); } catch
		 * (JDOMException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */
		Element root = document.getRootElement();
		List hosts = root.getChildren();
		Iterator<Element> it = hosts.iterator();
		int i = 1;
		while (it.hasNext()) {
			int count = 0;

			Element host = it.next();
			List hostParameters = host.getChildren();
			Iterator<Element> it1 = hostParameters.iterator();
			while (it1.hasNext()) {
				Element parameter = it1.next();
				List attributes = parameter.getAttributes();
				Iterator<Attribute> it2 = attributes.iterator();
				while (it2.hasNext()) {
					Attribute att = it2.next();
					if (description.containsKey(att.getName())) {
						if (description.get(att.getName()) instanceof Integer) {
							Integer value = (Integer) description.get(att
									.getName());
							int attValue = Integer.parseInt(att.getValue());
							if (value.intValue() <= attValue)
								count++;
						} else {// is a String
							String value = (String) description.get(att
									.getName());
							String attValue = att.getValue();
							if (value.equals(attValue))
								count++;
						}
					}
				}
			}
			if (count == number_of_resources) {
				GlobusHardwareResource hd = new GlobusHardwareResource(host, broker);
				// Aggiungi tutti i dati dell host
				matchedResources.add(hd);
			}
			i++;
		}

		return matchedResources;
	}

	public Document getDocument() {
		return this.document;
	}

	public HardwareResource getResourceByName(String hostname, WSGT4newResourceBrokerAdaptor broker) {
		Element root = document.getRootElement();
		List hosts = root.getChildren();
		Iterator<Element> hostIterator = hosts.iterator();
	
		while (hostIterator.hasNext()) {
			
			Element host = hostIterator.next();
			String name=host.getAttributeValue("NAME");
			if(name.equals(hostname)){
				//I have found the host to retrieve
			GlobusHardwareResource hd = new GlobusHardwareResource(host, broker);
			return hd;
				 
			}
		}
		//host not found then return null
		return null;		
	}
}
