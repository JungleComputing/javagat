package org.gridlab.gat.resources.security.unicore6;

import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.cpi.unicore6.Unicore6SiteResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unicore.hila.Location;
import eu.unicore.hila.Resource;
import eu.unicore.hila.exceptions.HiLAException;
import eu.unicore.hila.grid.Grid;
import eu.unicore.hila.grid.Site;

/**
 * 
 * @author Stefan Bozic
 */
public class HiLAHelper {

	/**
	 * the logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Unicore6SecurityUtils.class);

	/**
	 * Returns the available site names.
	 * 
	 * @param gatContext the gat context including security and runtime parameters
	 * @param registries the unicore registries to use
	 * @param username the name of the user which will be mapped to a saml assertion
	 * @return a list of available unicore sites
	 * @throws GATInvocationException will wrap an exception that might be thown by HiLA
	 */
	public static List<HardwareResource> findResources(GATContext gatContext, List<String> registries, String username)
			throws GATInvocationException {
		List<HardwareResource> resources = new ArrayList<HardwareResource>();
		
		try {
			Location loc = new Location("unicore6:/" + username + "@sites");
			//Location loc = new Location("unicore6:/sites");
			Resource siteCol =  loc.locate();
								
			List<Resource> sites = siteCol.getChildren();
			for (Resource resource : sites) {
				if (resource instanceof Site) {
					Site site = (Site) resource;
					HardwareResource hwRes = new Unicore6SiteResource(gatContext);
					hwRes.getResourceDescription().addResourceAttribute("sitename", site.getName());
					resources.add(hwRes);
				}

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resources;
	}

	/**
	 * Returns the available site names.
	 * 
	 * @param gatContext the gat context including security and runtime parameters
	 * @param registries the unicore registries to use
	 * @param username the name of the user which will be mapped to a saml assertion
	 * @param siteName the name of the site to obtain the extended sitename
	 * @return a list of available unicore sites
	 * @throws GATInvocationException will wrap an exception that might be thown by HiLA
	 */
	public static String getCurrentSiteNameWithTimeStamp(GATContext gatContext, List<String> registries,
			String username, String siteName) throws GATInvocationException {
		String siteNameWithTimeStamp = null;
		List<HardwareResource> resources = HiLAHelper.findResources(gatContext, registries, username);

		for (HardwareResource resource : resources) {
			String tempSitename = (String) resource.getResourceDescription().getResourceAttribute("sitename");

			// If the sitename contains an underscore it could contain a timestamp postfix. remove it from the sitename.
			if (tempSitename.contains("_")) {
				String timeStamp = tempSitename.substring(tempSitename.length() - 12); // 12 = lenght of timestamp

				try {
					Long.parseLong(timeStamp);

					String siteWithoutTimestamp = tempSitename.substring(0, tempSitename.length() - 13); // 13 = lenght
					// of
					// timestamp
					// +
					// underscore

					if (siteWithoutTimestamp.equals(siteName)) {
						siteNameWithTimeStamp = tempSitename;
						break;
					}
				} catch (NumberFormatException e) {
					LOGGER.debug("Cannot parse timstamp in sitename");
				}
			}
		}

		return siteNameWithTimeStamp;
	}

}
