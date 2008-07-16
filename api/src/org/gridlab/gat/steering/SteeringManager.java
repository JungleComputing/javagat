package org.gridlab.gat.steering;

import java.util.List;
import java.util.Map;
import org.gridlab.gat.GATInvocationException;

public interface SteeringManager {
    public void registerSteered(String steeredObjectID, Steerable s)
            throws SteeredIDExistsException, GATInvocationException;

    public void registerSteered(String steeredObjectID, Steerable s,
            boolean multicastGroup) throws SteeredIDExistsException,
            GATInvocationException;

    public void unregisterSteered(String steeredObjectID)
            throws SteeredIDUnknownException, GATInvocationException;

    public List<String> getSteeredObjectsIDs() throws GATInvocationException;

    public List<SteeringControlDefinition> getControlDefinitions(
            String steeredObjectID) throws SteeredIDUnknownException,
            GATInvocationException;

    public Map<String, Object> executeControl(String steeredObjectID,
            SteeringControl sc) throws SteeredIDUnknownException,
            NoSuchControlException, GATInvocationException;
}
