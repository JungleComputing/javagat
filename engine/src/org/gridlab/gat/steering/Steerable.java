
package org.gridlab.gat.steering;

import java.util.Map;
import java.util.List;


public interface Steerable
{
        public Map executeControl(SteeringControl c) throws NoSuchControlException, Exception; // exectutes c, returns a status Map object

        public List getControlDefinitions() throws Exception; // gets a list of available control definitions
}
