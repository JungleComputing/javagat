package org.gridlab.gat.resources.utils;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.gridlab.gat.resources.gt4.GlobusHardwareResource;
import org.gridlab.gat.resources.HardwareResource;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class XMLUtil {

	private static Element rootElement;
	private static Document document ;
	private static Element Host;
	private static Element CPU;
	private static Element Memory;
	private static Element OS;
	private static Element Disk;

	
	public static void createXMLDocument( org.w3c.dom.NodeList hostsList){
	
		rootElement= new Element("ROOT");
		document =new Document(rootElement);
				
		for(int i=0;i<hostsList.getLength();i++){
		
		Node host=hostsList.item(i);
		Node hostName=host.getAttributes().getNamedItem("ns1:Name");
		Host=new Element ("HOST"); 
		Host.setAttribute("NAME",hostName.getTextContent());
		rootElement.addContent(Host);
			
		NodeList hostParameters=host.getChildNodes();
		Node CPUSpeed=hostParameters.item(0).getAttributes().getNamedItem("ns1:ClockSpeed");
		Node CPUCount=hostParameters.item(3).getAttributes().getNamedItem("ns1:SMPSize");
		CPU=new Element ("CPU"); 
		CPU.setAttribute("CPU_SPEED",CPUSpeed.getTextContent());
		CPU.setAttribute("CPU_COUNT",CPUCount.getTextContent());	
		
		Node Memory_Size=hostParameters.item(1).getAttributes().getNamedItem("ns1:RAMSize");
		Node Memory_Available=hostParameters.item(1).getAttributes().getNamedItem("ns1:RAMAvailable");
		Memory=new Element("MEMORY");
		Memory.setAttribute("MEMORY_SIZE",Memory_Size.getTextContent());
		Memory.setAttribute("MEMORY_AVAILABLE",Memory_Available.getTextContent());
		
		Node OS_Type=hostParameters.item(2).getAttributes().getNamedItem("ns1:Name");
		Node OS_Release=hostParameters.item(2).getAttributes().getNamedItem("ns1:Release");
		OS=new Element("OS");
		OS.setAttribute("OS_TYPE",OS_Type.getTextContent());
		OS.setAttribute("OS_RELEASE",OS_Release.getTextContent());
		
		Node Disk_Size=hostParameters.item(4).getAttributes().getNamedItem("ns1:Size");
		Node Available_Disk_Size=hostParameters.item(4).getAttributes().getNamedItem("ns1:AvailableSpace");
		Disk=new Element("DISK");
		Disk.setAttribute("DISK_SIZE",Disk_Size.getTextContent());
		Disk.setAttribute("AVAILABLE_DISK_SIZE",Available_Disk_Size.getTextContent());
		
		Host.addContent(CPU);
		Host.addContent(Memory);
		Host.addContent(OS);
		Host.addContent(Disk);
	
	}/*
		XMLOutputter outputter=new XMLOutputter();
		try {
					outputter.output(document, new FileOutputStream(path));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
	}


	public static LinkedList<HardwareResource> matchResources(Map description) {
		
		LinkedList<HardwareResource> matchedResources=new LinkedList();
		int number_of_resources=description.size();

		
		/*try {
			Document document= saxBuilder.build(path);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		Element root= document.getRootElement();
		List hosts=root.getChildren();
		Iterator<Element> it= hosts.iterator();
		int i=1;
		while(it.hasNext()){
			int count=0;
		
			Element host=it.next();
			List hostParameters=host.getChildren();
			Iterator<Element> it1=hostParameters.iterator();
			while(it1.hasNext()){
				Element parameter=it1.next();
				List attributes=parameter.getAttributes();
				Iterator<Attribute> it2=attributes.iterator();
				while(it2.hasNext()){
					Attribute att=it2.next();					
					if(description.containsKey(att.getName()) )
				{	 
					Integer value=(Integer) description.get(att.getName());
					int attValue=Integer.parseInt(att.getValue());
					if(value.intValue()<= attValue)
					count++;					
				}
			}
			}
			if(count==number_of_resources){
				GlobusHardwareResource hd=new GlobusHardwareResource(host);
				//Aggiungi tutti i dati dell host
				matchedResources.add(hd);
			}
			i++;
		}
		
		return matchedResources;	
	}
	
	public static Document getDocument(){
		return document;
	}
	

}
