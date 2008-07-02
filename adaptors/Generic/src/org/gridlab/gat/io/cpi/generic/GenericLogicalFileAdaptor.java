// This class does not have to do anything, the CPI already provides all needed
// functionality.
package org.gridlab.gat.io.cpi.generic;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.LogicalFileCpi;

@SuppressWarnings("serial")
public class GenericLogicalFileAdaptor extends LogicalFileCpi {
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
    public GenericLogicalFileAdaptor(GATContext gatContext, String name,
            Integer mode) throws Exception {
        super(gatContext, name, mode);
    }

    public URI getClosestURI(URI loc) throws GATInvocationException {
        if (files == null || files.size() == 0) {
            throw new GATInvocationException("No files in logical file '"
                    + name + "' to compare with");
        }
        // first check: same hostname
        for (URI file : files) {
            if (file.getHost().equalsIgnoreCase(loc.getHost())) {
                return file;
            }
        }
        // check for same suffix. The more parts of the suffix are the same, the
        // closer the location
        String locationPart = loc.getHost();
        while (locationPart.contains(".")) {
            int position = locationPart.indexOf(".");
            for (URI file : files) {
                if (file.getHost().endsWith(locationPart.substring(position))) {
                    return file;
                }
            }
            // assuming the a hostname never ends with a dot "."
            locationPart = locationPart.substring(position + 1);
        }
        int separatorPosition = loc.getHost().indexOf(".");
        if (separatorPosition > 0) {
            for (URI file : files) {
                if (file.getHost().endsWith(
                        loc.getHost().substring(separatorPosition))) {
                    return file;
                }
            }
        }
        // return first
        return files.get(0);
    }
}
