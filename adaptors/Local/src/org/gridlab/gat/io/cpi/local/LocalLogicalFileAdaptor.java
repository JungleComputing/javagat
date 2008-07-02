// This class does not have to do anything, the CPI already provides all needed
// functionality.
package org.gridlab.gat.io.cpi.local;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.LogicalFileCpi;

@SuppressWarnings("serial")
public class LocalLogicalFileAdaptor extends LogicalFileCpi {
    /**
     * This constructor creates a LocalLogicalFileAdaptor corresponding to the
     * passed URI instance and uses the passed GATContext to broker resources.
     * 
     * @param location
     *                The URI of one physical file in this
     *                LocalLogicalFileAdaptor
     * @param gatContext
     *                The GATContext used to broker resources
     * @throws java.lang.Exception
     *                 Thrown upon creation problems
     */
    public LocalLogicalFileAdaptor(GATContext gatContext, String name,
            Integer mode) throws Exception {
        super(gatContext, name, mode);
    }
    
    protected URI getClosestFile(URI loc) {
        // first check: same hostname
        for (URI file : files) {
            if (file.getHost().equalsIgnoreCase(loc.getHost())) {
                return file;
            }
        }
        // check for same suffix. The more parts of the suffix are the same, the closer the location
        String locationPart = loc.getHost();
        while (locationPart.contains(".")) {
            int position = locationPart.indexOf(".");
            for (URI file : files) {
                if (file.getHost().endsWith(locationPart.substring(position))) {
                    return file;
                }
            }
            locationPart = locationPart.substring(position);
        }
        int separatorPosition = loc.getHost().indexOf(".");
        if (separatorPosition > 0) {
            for (URI file : files) {
                if (file.getHost().endsWith(loc.getHost().substring(separatorPosition))) {
                    return file;
                }
            }
        }
        return files.get(0);
    }
}
