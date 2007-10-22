
package org.gridlab.gat.steering;

import java.util.List;
import java.util.Map;
import org.gridlab.gat.GATInvocationException;


public interface SteeringManager
{
	public void registerSteered(String steeredObjectID, Steerable s) throws SteeredIDExistsException, GATInvocationException;

	public void registerSteered(String steeredObjectID, Steerable s, boolean
 multicastGroup) throws SteeredIDExistsException, GATInvocationException;

	public void unregisterSteered(String steeredObjectID, Steerable s) throws SteeredIDUnknownException, GATInvocationException;

	public List getSteeredObjectsIDs() throws GATInvocationException;

	public List getControlDefinitions(String steeredObjectID) throws SteeredIDUnknownException, GATInvocationException;

	public Map executeControl(String steeredObjectID, SteeringControl sc) throws SteeredIDUnknownException, NoSuchControlException, GATInvocationException;
}
