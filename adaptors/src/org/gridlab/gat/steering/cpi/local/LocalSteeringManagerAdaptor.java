
package org.gridlab.gat.steering.cpi.local;

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


public class LocalSteeringManagerAdaptor extends SteeringManagerCpi
{
	Map helped;

	public LocalSteeringManagerAdaptor(org.gridlab.gat.GATContext gatContext, org.gridlab.gat.Preferences preferences) throws GATObjectCreationException
	{
		super(gatContext, preferences);

		/*
        	try {
            		checkName("local");
        	} catch (Exception e) {
            		e.printStackTrace();
        	}
		*/

		helped = new Hashtable();
	}

	public void registerSteered(String steeredObjectID, Steerable s) throws SteeredIDExistsException, GATInvocationException
	{
		if(helped.containsKey(steeredObjectID)) throw new SteeredIDExistsException();
		helped.put(steeredObjectID, s);
	}

        public void registerSteered(String steeredObjectID, Steerable s, boolean
 multicastGroup) throws SteeredIDExistsException, GATInvocationException
	{
		if(!multicastGroup) registerSteered(steeredObjectID, s);
		else ; // to do
	}

	public void unregisterSteered(String steeredObjectID) throws SteeredIDUnknownException, GATInvocationException
	{
		if(!helped.containsKey(steeredObjectID)) throw new SteeredIDUnknownException();
		helped.remove(steeredObjectID);
	}

	public List getSteeredObjectsIDs() throws GATInvocationException
	{
		Vector rv = new Vector();

		rv.addAll(helped.keySet());

		return rv;
	}

	public List getControlDefinitions(String steeredObjectID) throws SteeredIDUnknownException, GATInvocationException
	{
		if(!helped.containsKey(steeredObjectID)) throw new SteeredIDUnknownException();
		return ((Steerable) helped.get(steeredObjectID)).getControlDefinitions();
	}


	public Map executeControl(String steeredObjectID, SteeringControl sc) throws SteeredIDUnknownException, NoSuchControlException, GATInvocationException
	{
		if(!helped.containsKey(steeredObjectID)) throw new SteeredIDUnknownException();
		Steerable s = (Steerable) helped.get(steeredObjectID);
		try
		{
			return s.executeControl(sc);
		}
		catch(NoSuchControlException e)
		{
			throw e;
		}
	}
}
