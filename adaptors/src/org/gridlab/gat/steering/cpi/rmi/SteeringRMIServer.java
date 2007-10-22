
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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;


public interface SteeringRMIServer extends Remote
{
        public void registerSteered(String steeredObjectID) throws SteeredIDExistsException, NotBoundException, MalformedURLException, RemoteException;

        public void registerSteered(String steeredObjectID, boolean multicastGroup) throws SteeredIDExistsException, NotBoundException, MalformedURLException, RemoteException;

        public void unregisterSteered(String steeredObjectID) throws SteeredIDUnknownException, RemoteException;

        public List getSteeredObjectsIDs() throws RemoteException;

        public List getControlDefinitions(String steeredObjectID) throws SteeredIDUnknownException, RemoteException;

        public Map executeControl(String steeredObjectID, SteeringControl sc) throws SteeredIDUnknownException, NoSuchControlException, RemoteException;
}

