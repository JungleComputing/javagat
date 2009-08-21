package org.gridlab.gat.resources.cpi.wsgt4new;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class QueryingThread extends Thread{

	WSGT4newResourceBrokerAdaptor broker;
	
	int numberOfListeners;
	
	boolean death=false; 
	
	public QueryingThread(WSGT4newResourceBrokerAdaptor broker){
		this.broker=broker;
		setDaemon(true);
	}
	
	public  synchronized void addListener(){
		numberOfListeners++;
	}
	public  synchronized void removeListener(){
		numberOfListeners--;
	}
	public  synchronized int getNumberOfListeners(){
		return numberOfListeners;
	}
	
	public void run(){
		while(!death){
				MessageElement[] entries = broker.queryDefaultIndexService();
				
				if (entries == null || entries.length == 0) {
					System.out.println("Lunghezza 0 e mo so cazzi");
				} else {
							
							Element root=null;
							try {
								root = entries[0].getAsDOM();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							NodeList hosts=root.getChildNodes();
							broker.updateXMLDocument(hosts);
							System.out.println("\nXML Document Updated\n");
				
				}
				try {
					sleep(5*60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//Query the Index Service every 5 minutes
				
		}
		}

	public void killQueryingThread() {
		this.death=true;
		System.out.println("Querying Thread Killed");
	}
	}
