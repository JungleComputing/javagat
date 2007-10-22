
package org.gridlab.gat.steering.cpi.rmi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;

import org.gridlab.gat.steering.Steerable;
import org.gridlab.gat.steering.SteeringControl;
import org.gridlab.gat.steering.SteeringControlDefinition;
import org.gridlab.gat.steering.SteeringManager;
import org.gridlab.gat.steering.SteeredIDExistsException;
import org.gridlab.gat.steering.SteeredIDUnknownException;
import org.gridlab.gat.steering.NoSuchControlException;
import org.gridlab.gat.steering.cpi.SteeringManagerCpi;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;

import java.util.StringTokenizer;
import java.net.InetAddress;


public class RMISteeringManagerAdaptor extends SteeringManagerCpi
{
	String steeringServerLocation="//fs0.das2.cs.vu.nl:20000/SteeringServer";
	SteeringRMIServer RMIServer=null;

	String localHost=null;

	int localPort=1099;

	public RMISteeringManagerAdaptor(org.gridlab.gat.GATContext gatContext,
            org.gridlab.gat.Preferences preferences) throws GATObjectCreationException
	{
		super(gatContext, preferences);

		String prefLocation = (String) preferences.get("RMI_STEERING_SERVER_LOCATION");
		if(prefLocation!=null) steeringServerLocation=prefLocation;

		Integer prefPort = (Integer) preferences.get("RMI_LOCAL_REGISTRY_PORT");
                if(prefPort!=null) localPort=prefPort.intValue();

		try{localHost = InetAddress.getLocalHost().getHostName();}
		catch(Exception e){throw new GATObjectCreationException(e.toString());}

		try{RMIServer= (SteeringRMIServer) Naming.lookup(steeringServerLocation);}
		catch(RemoteException e){throw new GATObjectCreationException(e.toString());}
                catch(NotBoundException e){throw new GATObjectCreationException(e.toString());}
                catch(MalformedURLException e){throw new GATObjectCreationException(e.toString());}
	}

	public void registerSteered(String steeredObjectID, Steerable s) throws SteeredIDExistsException, GATInvocationException
	{
		//try{RMIServer.registerSteered(steeredObjectID, s);}

		StringTokenizer tok = new StringTokenizer(steeredObjectID, "/");

		String objName = null;
		while(tok.hasMoreTokens()) objName=tok.nextToken();

		try{
			System.out.println("RMISteeringManagerAdaptor: Registering steered object with local RMI registry: //"+localHost+":"+localPort+"/"+objName);
                	Naming.rebind("//"+localHost+":"+localPort+"/"+objName,(Remote)s);	
			System.out.println("RMIServer="+RMIServer);
			RMIServer.registerSteered(steeredObjectID);
		}
		catch(Exception e){e.printStackTrace();throw new GATInvocationException(e.toString());}
		// SteeredIDExistsException thrown as such ...
	}

        public void registerSteered(String steeredObjectID, Steerable s, boolean
 multicastGroup) throws SteeredIDExistsException, GATInvocationException
	{
		//try{RMIServer.registerSteered(steeredObjectID, s, multicastGroup);}
                StringTokenizer tok = new StringTokenizer(steeredObjectID, "/");

		String objName = null;
                while(tok.hasMoreTokens()) objName=tok.nextToken();

		try{
                	System.out.println("RMISteeringManagerAdaptor: Registering steered object with local RMI registry: //"+localHost+":"+localPort+"/"+objName);
                	Naming.rebind("//"+localHost+":"+localPort+"/"+objName,(Remote)s);
			RMIServer.registerSteered(steeredObjectID, multicastGroup);
		}
                catch(Exception e){throw new GATInvocationException(e.toString());}
	}

	public void unregisterSteered(String steeredObjectID, Steerable s) throws SteeredIDUnknownException, GATInvocationException
	{
                StringTokenizer tok = new StringTokenizer(steeredObjectID, "/");

		String objName = null;
                while(tok.hasMoreTokens()) objName=tok.nextToken();

		try{
			RMIServer.unregisterSteered(steeredObjectID);

                	System.out.println("RMISteeringManagerAdaptor: Unregistering steered object from local RMI registry: //"+localHost+":"+localPort+"/"+objName);
                	Naming.unbind("//"+localHost+":"+localPort+"/"+objName);
			UnicastRemoteObject.unexportObject((Remote) s, true);
		}
                catch(Exception e){throw new GATInvocationException(e.toString());}
	}

	public List getSteeredObjectsIDs() throws GATInvocationException
	{
                try{return RMIServer.getSteeredObjectsIDs();}
                catch(RemoteException e){throw new GATInvocationException(e.toString());}
	}

	public List getControlDefinitions(String steeredObjectID) throws SteeredIDUnknownException, GATInvocationException
	{
                try{return RMIServer.getControlDefinitions(steeredObjectID);}
                catch(RemoteException e){throw new GATInvocationException(e.toString());}
	}


	public Map executeControl(String steeredObjectID, SteeringControl sc) throws SteeredIDUnknownException, NoSuchControlException, GATInvocationException
	{
                try{return RMIServer.executeControl(steeredObjectID, sc);}
                catch(RemoteException e){throw new GATInvocationException(e.toString());}
	}
}

