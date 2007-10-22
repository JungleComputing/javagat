
package org.gridlab.gat.steering.cpi.rmi;

import org.gridlab.gat.steering.Steerable;
import org.gridlab.gat.steering.SteeringControl;
import org.gridlab.gat.steering.NoSuchControlException;

import java.util.Map;
import java.util.List;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SteerableRMI extends Steerable, Remote
{
        public Map executeControl(SteeringControl c) throws NoSuchControlException, RemoteException; // executes c, returns a status Map object

        public List getControlDefinitions() throws RemoteException; // gets a list of available control definitions
}

