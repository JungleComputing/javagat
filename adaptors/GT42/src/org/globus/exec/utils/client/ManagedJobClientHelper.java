package org.globus.exec.utils.client;

import org.apache.axis.AxisProperties;
import org.apache.axis.EngineConfigurationFactory;
import org.globus.axis.message.addressing.EndpointReferenceType;
import org.globus.exec.generated.ManagedJobPortType;
import org.globus.exec.generated.service.ManagedJobServiceAddressingLocator;
import org.globus.exec.utils.service.ManagedJobHelper;
import org.globus.wsrf.utils.AddressingUtils;

/**
 * This class is extended by GAT to set the right client-config.wsdd for the
 * globus axis client.
 */
public class ManagedJobClientHelper {

    public static ManagedJobPortType getPort(EndpointReferenceType endpoint)
            throws Exception {
        // Set the GlobusEngineConfigurationFactory for the globus axis client
        if (AxisProperties
                .getProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME) == null) {
            AxisProperties
                    .setProperty(
                            EngineConfigurationFactory.SYSTEM_PROPERTY_NAME,
                            "org.gridlab.gat.resources.cpi.gt42.GlobusEngineConfigurationFactory");
        }

        // ManagedJobServiceAddressingLocator locator = new
        // ManagedJobServiceAddressingLocator();
        return new ManagedJobServiceAddressingLocator()
                .getManagedJobPortTypePort(endpoint);
    }

    /**
     * Returns a resource-qualified endpoint reference to a managed job service.
     * 
     * @param serviceAddress
     *            String The URL to the ManagedJobService
     * @param resourceID
     *            String The job ID, used as a key for the resource
     * @throws Exception
     * @return EndpointReferenceType The EndpointReference to the service
     */
    public static EndpointReferenceType getEndpoint(String serviceAddress,
            String resourceID) throws Exception {
        EndpointReferenceType endpoint = AddressingUtils
                .createEndpointReference(serviceAddress, ManagedJobHelper
                        .getResourceKey(resourceID));
        return endpoint;
    }

    public static EndpointReferenceType getEndpoint(String handle)
            throws Exception {
        return ManagedJobHelper.getEndpoint(handle);
    }

    public static String getHandle(EndpointReferenceType endpoint) {
        return ManagedJobHelper.getHandle(endpoint);
    }

}
