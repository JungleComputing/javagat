// This class does not have to do anything, the CPI already provides all needed
// functionality.
package org.gridlab.gat.io.cpi.generic;

import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.LogicalFileCpi;

@SuppressWarnings("serial")
public class GenericLogicalFileAdaptor extends LogicalFileCpi {
    /**
     * This constructor creates a LocalLogicalFileAdaptor corresponding to the
     * passed URI instance and uses the passed GATContext to broker resources.
     * @throws java.lang.Exception
     *                 Thrown upon creation problems
     */
    public GenericLogicalFileAdaptor(GATContext gatContext, String name,
            Integer mode) throws Exception {
        super(gatContext, name, mode);
    }

    public URI getClosestURI(URI location) throws GATInvocationException {
        return getClosestURI(location, files);
    }

    private URI getClosestURI(URI location, List<URI> otherLocations)
            throws GATInvocationException {
        if (otherLocations == null || otherLocations.size() == 0) {
            throw new GATInvocationException("No files in logical file '"
                    + name + "' to compare with");
        }
        // first check: same hostname
        for (URI file : otherLocations) {
            if (file.getHost() != null
                    && file.getHost().equalsIgnoreCase(location.getHost())) {
                return file;
            }
        }
        // check for same suffix. The more parts of the suffix are the same, the
        // closer the location
        String locationPart = location.getHost();
        if (locationPart != null) {
            while (locationPart.contains(".")) {
                int position = locationPart.indexOf(".");
                for (URI file : otherLocations) {
                    if (file.getHost() != null
                            && file.getHost().endsWith(
                                    locationPart.substring(position))) {
                        return file;
                    }
                }
                // assuming the a hostname never ends with a dot "."
                locationPart = locationPart.substring(position + 1);
            }
            int separatorPosition = location.getHost().indexOf(".");
            if (separatorPosition > 0) {
                for (URI file : otherLocations) {
                    if (file.getHost() != null
                            && file.getHost().endsWith(
                                    location.getHost().substring(
                                            separatorPosition))) {
                        return file;
                    }
                }
            }
        }
        // return first
        return otherLocations.get(0);
    }

    public List<URI> getOrderedURIs(URI location) throws GATInvocationException {
        if (files == null || files.size() == 0) {
            throw new GATInvocationException("No files in logical file '"
                    + name + "' to order");
        }
        List<URI> tmp = new ArrayList<URI>();
        for (URI uri : files) {
            tmp.add(uri);
        }
        List<URI> result = new ArrayList<URI>();
        for (int i = 0; i < files.size(); i++) {
            URI closest = getClosestURI(location, tmp);
            tmp.remove(tmp.indexOf(closest));
            result.add(closest);
        }
        return result;
    }
}
