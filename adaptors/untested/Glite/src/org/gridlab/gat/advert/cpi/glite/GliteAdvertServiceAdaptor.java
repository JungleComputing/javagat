package org.gridlab.gat.advert.cpi.glite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.advert.cpi.AdvertServiceCpi;


/**
 * This adaptor will be used to obtain information from an LDAP information
 * service, since to date there is only an Advert-Service concept in JavaGAT but
 * none explicitly for information services.
 * Nevertheless, it can also be extended for assisting in publishing and
 * obtaining user generated data
 * 
 * @author thomas
 *
 */
public class GliteAdvertServiceAdaptor extends AdvertServiceCpi {

	private static final String QUERY_METHOD_KEY = "glite.query.type";
	
	private String voName = "";
	
	public GliteAdvertServiceAdaptor(GATContext context) {
		super(context);
		
		Preferences prefs = context.getPreferences();
		voName = (String) prefs.get("VirtualOrganisation");
	}
	
	/**
	 * Queries in LDAP for all available WMS nodes if query contains
	 * key glite.query.type with value wms
	 * @param query
	 * @return
	 */
	public String [] find (MetaData query) throws GATInvocationException {
		
		String [] result = null;
		
		if (query.get(QUERY_METHOD_KEY) != null) {
			String ldapQueryType = query.get(QUERY_METHOD_KEY);
			
			if ("wms".equals(ldapQueryType)) {
				result = findWMSServers();
			} else if ("ce".equals(ldapQueryType)) {
				result = findCEs();
			}
		}
		
		
		return result;
	}
	
	private String [] findCEs() throws GATInvocationException {
		
		try {
			LDAPResourceFinder finder = new LDAPResourceFinder();
			List<String> ces = finder.fetchCEs(voName);
			
			return listToStringArray(ces);
		} catch (NamingException e) {
			throw new GATInvocationException("Could not get CEs from LDAP information service", e);
		}
	}
		

	private String [] findWMSServers() throws GATInvocationException {
		
		try {
			LDAPResourceFinder finder = new LDAPResourceFinder();
			List<String> wmsServers = finder.fetchWMSServers(voName);
			
			return listToStringArray(wmsServers);
			
			
		} catch (NamingException e) {
			throw new GATInvocationException("Could not get WMS Servers from LDAP information service", e);
		}
	}
	

	private String [] listToStringArray(List<String> input) {
		String [] result = new String[input.size()];
		
		int i = 0;
		for (String wmsServer : input) {
			result[i++] = wmsServer;
		}
		
		return result;
	}
	
	public static void main(String [] args) throws Exception {
		GATContext context = new GATContext();
		context.addPreference("VirtualOrganisation", "voce");
		GliteAdvertServiceAdaptor adaptor = new GliteAdvertServiceAdaptor(context);
		String [] wmses = adaptor.findWMSServers();
		
		for (int i = 0; i < wmses.length; i++) {
			System.out.println("wms found: " + wmses[i]);
		}
	}
}
