
package org.gridlab.gat.steering.cpi.rmi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;

import org.gridlab.gat.steering.SteeringControl;
import org.gridlab.gat.steering.SteeringControlDefinition;
import org.gridlab.gat.steering.SteeringManager;
import org.gridlab.gat.steering.SteeredIDExistsException;
import org.gridlab.gat.steering.SteeredIDUnknownException;
import org.gridlab.gat.steering.NoSuchControlException;
import org.gridlab.gat.steering.cpi.SteeringManagerCpi;

import org.gridlab.gat.steering.cpi.rmi.SteerableRMI;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;


public class SteeringRMIServerImpl extends UnicastRemoteObject implements SteeringRMIServer
{
	Map helped;

	public SteeringRMIServerImpl() throws RemoteException
	{
		helped = new Hashtable();
	}

	public void registerSteered(String steeredObjectID) throws SteeredIDExistsException, NotBoundException, MalformedURLException, RemoteException
	{
		if(helped.containsKey(steeredObjectID)) throw new SteeredIDExistsException();
		SteerableRMI s = (SteerableRMI) Naming.lookup(steeredObjectID);
		helped.put(steeredObjectID, s);
	}

        public void registerSteered(String steeredObjectID, boolean multicastGroup) throws SteeredIDExistsException, NotBoundException, MalformedURLException, RemoteException
	{
		if(!multicastGroup) registerSteered(steeredObjectID);
		else ; // to do
	}

	public void unregisterSteered(String steeredObjectID) throws SteeredIDUnknownException, RemoteException
	{
		if(!helped.containsKey(steeredObjectID)) throw new SteeredIDUnknownException();
		helped.remove(steeredObjectID);
	}

	public List getSteeredObjectsIDs() throws RemoteException
	{
		Vector rv = new Vector();

		rv.addAll(helped.keySet());

		return rv;
	}

	public List getControlDefinitions(String steeredObjectID) throws SteeredIDUnknownException, RemoteException
	{
		if(!helped.containsKey(steeredObjectID)) throw new SteeredIDUnknownException();
		return ((SteerableRMI) helped.get(steeredObjectID)).getControlDefinitions();
	}


	public Map executeControl(String steeredObjectID, SteeringControl sc) throws SteeredIDUnknownException, NoSuchControlException, RemoteException
	{
		if(!helped.containsKey(steeredObjectID)) throw new SteeredIDUnknownException();
		SteerableRMI s = (SteerableRMI) helped.get(steeredObjectID);
		return s.executeControl(sc);
	}
}

