package org.gridlab.gat.engine;

import java.net.InetAddress;
import java.util.Properties;

/**
 * Some utilities that deal with IPv4 addresses.
 */
public class IPUtils {

	static final boolean DEBUG = false;

	private static InetAddress localaddress = null;

	private IPUtils() {
	}
	
	/**
	 * Returns true if the specified address is a loopback address.
	 * Loopback means in the 127.*.*.* range.
	 * @param addr the specified address.
	 * @return <code>true</code> if <code>addr</code> is a loopback address.
	 */
	public static boolean isLoopbackAddress(InetAddress addr) {
		byte[] a = addr.getAddress();

		if(a.length != 4) {
			System.err.println("WARNING: IPUtils: this only works for IP v 4 addresses");
			return false;
		}

		return a[0] == 127;
	}

	/**
	 * Returns true if the specified address is a link local address.
	 * Link local means in the 169.254.*.* range.
	 * @param addr the specified address.
	 * @return <code>true</code> if <code>addr</code> is a link local address.
	 */
	public static boolean isLinkLocalAddress(InetAddress addr) {
		byte[] a = addr.getAddress();

		if(a.length != 4) {
			System.err.println("WARNING: IPUtils: this only works for IP v 4 addresses");
			return false;
		}

		long address = a[0] << 24 | a[1] << 16 | 
			a[2] << 8 | a[0];

		return (address >>> 24 & 0xff) == 169 && 
			(address >>> 16 & 0xff) == 254;
	}

	/**
	 * Returns true if the specified address is a site local address.
	 * Site local means in the 10.*.*.*, or the 172.[16-32].*.*, or the 192.168.*.* range.
	 * @param addr the specified address.
	 * @return <code>true</code> if <code>addr</code> is a site local address.
	 */
	public static boolean isSiteLocalAddress(InetAddress addr) {
		byte[] a = addr.getAddress();

		if(a.length != 4) {
			System.err.println("WARNING: IPUtils: this only works for IP v 4 addresses");
			return false;
		}

		long address = a[0] << 24 | a[1] << 16 | 
			a[2] << 8 | a[0];

		return (address >>> 24 & 0xff) == 10 || 
			(address >>> 24 & 0xff) == 172 && (address >>> 16 & 0xf0) == 16 || 
			(address >>> 24 & 0xff) == 192 && (address >>> 16 & 0xff) == 168;
	}

	/**
	 * Returns true if the specified address is an external address.
	 * External means not a site local, link local, or loopback address.
	 * @param addr the specified address.
	 * @return <code>true</code> if <code>addr</code> is an external address.
	 */
	public static boolean isExternalAddress(InetAddress addr) {
		if(isLoopbackAddress(addr)) return false;
		if(isSiteLocalAddress(addr)) return false;
		if(isLinkLocalAddress(addr)) return false;

		return true;
	}
	
	/**
	 * Returns the {@link java.net.InetAddress} associated with the
	 * local host.
	 */
	public static InetAddress getLocalHostAddress() {
		if (localaddress == null) {
		    localaddress = doWorkGetLocalHostAddress();

		    // To make sure that a hostname is filled in:
		    String tmp = localaddress.getHostName();

		    if (DEBUG) {
			System.err.println("Found address: " + localaddress);
		    }
		}
		return localaddress;
	}

	private static InetAddress doWorkGetLocalHostAddress() {
		InetAddress external = null;
		InetAddress internal = null;
		InetAddress[] all = null;

		Properties p = System.getProperties();

		String myIp = p.getProperty("ibis.ip.address");
		if (myIp != null) {
			try {
				external = InetAddress.getByName(myIp);
				return external;
			} catch (java.net.UnknownHostException e) {
				System.err.println("IP addres property specified, but could not resolve it");
			}
		}

		// backwards compatibility
		myIp = p.getProperty("ip_address");
		if (myIp != null) {
			try {
				external = InetAddress.getByName(myIp);
				return external;
			} catch (java.net.UnknownHostException e) {
				System.err.println("IP addres property specified, but could not resolve it");
			}
		}

		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			all = InetAddress.getAllByName(hostname);
		} catch (java.net.UnknownHostException e) {
			System.err.println("IP addres property specified, but could not resolve it");
		}

		if(all != null) {
			for(int i=0; i<all.length; i++) {
				if(DEBUG) {
					System.err.println("trying address: " + all[i] +
							   (isExternalAddress(all[i]) ? " EXTERNAL" : " LOCAL"));
				}
				if(isExternalAddress(all[i])) {
					if(external == null) {
						external = all[i];
					} else {
						System.err.println("WARNING, this machine has more than one external " +
								   "IP address, using " +
								   external);
						return external;
					}
				}
				if (internal == null && ! isLoopbackAddress(all[i])) {
				    internal = all[i];
				}
			}
		}

		if(external == null) {
			external = internal;
			if (external == null) {
				try {
					InetAddress a = InetAddress.getLocalHost();
					if(a == null) {
						System.err.println("Could not find local IP address, you should specify the -Dibis.ip.address=A.B.C.D option");
						return null;
					}
					String name = a.getHostName();
					external = InetAddress.getByName(InetAddress.getByName(name).getHostAddress());
				} catch (java.net.UnknownHostException e) {
					System.err.println("Could not find local IP address, you should specify the -Dibis.ip.address=A.B.C.D option");
					return null;
				}
			}
		}

		return external;
	}
}
