package org.gridlab.gat.steering;

import java.util.List;
import java.util.Map;

public interface Steerable {
    public Map<String, Object> executeControl(SteeringControl c)
            throws NoSuchControlException; // exectutes c, returns a status Map
                                            // object

    public List<SteeringControlDefinition> getControlDefinitions(); // gets a
                                                                    // list of
                                                                    // available
                                                                    // control
                                                                    // definitions
}
