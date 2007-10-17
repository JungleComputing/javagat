package org.gridlab.gat.steering.cpi;

import java.util.List;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.steering.NoSuchControlException;
import org.gridlab.gat.steering.Steerable;
import org.gridlab.gat.steering.SteeredIDExistsException;
import org.gridlab.gat.steering.SteeredIDUnknownException;
import org.gridlab.gat.steering.SteeringControl;
import org.gridlab.gat.steering.SteeringControlDefinition;
import org.gridlab.gat.steering.SteeringManager;

/**
 * @author aagapi
 *
 *  SteeringManager interface impl
 */
public class SteeringManagerCpi implements SteeringManager {
    GATContext gatContext;

    Preferences preferences;

    /**
     * Create an instance of the SteeringManager using the provided preference.
     *
     * @param gatContext
     *            The context to use.
     * @param preferences
     *            The user preferences.
     */
    public SteeringManagerCpi(GATContext gatContext, Preferences preferences)
        throws GATObjectCreationException {
        this.gatContext = gatContext;
        this.preferences = preferences;
    }

    public void registerSteered(String steeredObjectID, Steerable s)
        throws SteeredIDExistsException, GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void registerSteered(String steeredObjectID, Steerable s,
        boolean multicastGroup) throws SteeredIDExistsException,
        GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void unregisterSteered(String steeredObjectID)
        throws SteeredIDUnknownException, GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<String> getSteeredObjectsIDs() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<SteeringControlDefinition> getControlDefinitions(String steeredObjectID)
        throws SteeredIDUnknownException, GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Map<String, Object> executeControl(String steeredObjectID, SteeringControl sc)
        throws SteeredIDUnknownException, NoSuchControlException,
        GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*    protected void checkName(String adaptor) throws GATObjectCreationException {
     GATEngine.checkName(preferences, "SteeringManager", adaptor);
     }
     */
}
