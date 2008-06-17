package org.gridlab.gat.advert.cpi.glite;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.apache.log4j.Logger;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.SoftwareResourceDescription;

public class LDAPResourceFinder {
	
    protected static final Logger logger = Logger.getLogger(LDAPResourceFinder.class);
	
	public static final String LDAP_SERVER_NAME = "bdii101.grid.ucy.ac.cy";
	public static final int LDAP_SERVER_PORT = 2170;
	
	private static final String START_DN = "Mds-Vo-name=local,o=Grid";
	
	private String ldapContact;
	private DirContext ctx;
	private Hashtable < String, String > env;
	
	private SearchControls globalSearchControls;
	
	
	public LDAPResourceFinder() throws NamingException {
		String lcgContact = System.getenv("LCG_GFAL_INFOSYS");
		
		if (lcgContact == null) {
			this.ldapContact = LDAP_SERVER_NAME + ":" + LDAP_SERVER_PORT;
		} else {
			this.ldapContact = lcgContact;
		}
		
		globalSearchControls = new SearchControls();
		// search everything within the given context
		globalSearchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		// return all found elements
		globalSearchControls.setCountLimit(0);
		// take as much time as you need
		globalSearchControls.setTimeLimit(0);
		// return all attributes
		globalSearchControls.setReturningAttributes(null);
		
		env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://" + ldapContact);
		
		// construct the ldap connection from the environement params
		ctx = new InitialLdapContext(env, null);
	}
	
	public List<String> fetchWMSServers(final String voName) throws NamingException {
		ArrayList<String> wmsServers = new ArrayList<String>();
		
		String filter = "(&(GlueServiceType=org.glite.wms.WMProxy)(GlueServiceOwner=" + voName + "))";
		NamingEnumeration<SearchResult> results = ctx.search(START_DN, filter, globalSearchControls);
		
		while (results.hasMore()) {
			SearchResult result = (SearchResult) results.nextElement();
			Attribute wmsServerAtt = result.getAttributes().get("GlueServiceAccessPointURL");
			String wmsServer = (String) wmsServerAtt.get();
			System.out.println(wmsServer);
			wmsServers.add(wmsServer);
		}
		
		return wmsServers;
	}
	
	public List<String> fetchCEs(final String voName) throws NamingException {
		ArrayList<String> results = new ArrayList<String>();	
		
		String filter = "(&(objectClass=GlueCE)(GlueCEAccessControlBaseRule=VO:" + voName + "))";
		NamingEnumeration<SearchResult> clusters = ctx.search(START_DN, filter, globalSearchControls);
		
		while (clusters.hasMore()) {
			SearchResult result = (SearchResult) clusters.nextElement();
			Attribute ceAtt = result.getAttributes().get("GlueCEInfoContactString");
			String ceURL = (String) ceAtt.get();
			// TODO use a logger...
			System.out.println(ceURL);
			results.add(ceURL);
		}
		
		return results;
	}
	
	
// only used for testing
//	public static void main(final String [] args) throws Exception {
//		SoftwareResourceDescription srd = new SoftwareResourceDescription();
//		HardwareResourceDescription hrd = new HardwareResourceDescription();
//		
//		List<String> gliteOSNames = new ArrayList<String>();
//		gliteOSNames.add("ScientificSL");
//		gliteOSNames.add("ScientificCERNSLC");
//		gliteOSNames.add("Scientific Linux CERN");
//		srd.addResourceAttribute("glite.OS", gliteOSNames);
//		
//		List<String> gliteProcessorNames = new ArrayList<String>();
//		gliteProcessorNames.add("PIV");
//		gliteProcessorNames.add("P4");
//		gliteProcessorNames.add("PIII");
//		srd.addResourceAttribute("glite.Processor", gliteProcessorNames);
//		
//		srd.addResourceAttribute("memory.size", new Float(1.0));
//		
//		LDAPResourceFinder finder = new LDAPResourceFinder();
//		//finder.findResources("voce");
//		finder.fetchCEs("compchem");
//	}
	
}
