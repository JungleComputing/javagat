// This class does not have to do anything, the CPI already provides all needed
// functionality.

package org.gridlab.gat.io.cpi.local;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.cpi.LogicalFileCpi;

public class LocalLogicalFileAdaptor extends LogicalFileCpi {

    /**
     * This constructor creates a LocalLogicalFileAdaptor corresponding to the
     * passed URI instance and uses the passed GATContext to broker resources.
     * 
     * @param location
     *            The URI of one physical file in this LocalLogicalFileAdaptor
     * @param gatContext
     *            The GATContext used to broker resources
     * @throws java.lang.Exception
     *             Thrown upon creation problems
     */
    public LocalLogicalFileAdaptor(GATContext gatContext,
        Preferences preferences, String name, Integer mode) throws Exception {
        super(gatContext, preferences, name, mode);

        checkName("local");
    }
}
